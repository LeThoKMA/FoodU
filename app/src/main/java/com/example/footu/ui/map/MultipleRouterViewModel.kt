package com.example.footu.ui.map

import android.content.Context
import android.location.Location
import androidx.lifecycle.viewModelScope
import com.example.footu.base.BaseViewModel
import com.mapbox.geojson.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MultipleRouterViewModel
    @Inject
    constructor(
        @ApplicationContext context: Context,
        private val mapRepository: MapRepository,
    ) : BaseViewModel() {
        private val mUiStateFlow: MutableStateFlow<UiState> = MutableStateFlow(UiState.Routers())
        val uiState: StateFlow<UiState> = mUiStateFlow

        init {
            getRouters()
        }

        fun handleEvent(event: Event) {
            when (event) {
                is Event.Position -> {
                    viewModelScope.launch {
                        flow { emit(event.location) }
                            .collect {
                                mUiStateFlow.value = UiState.Position(it)
                            }
                    }
                }

                else -> {}
            }
        }

        private fun getRouters() {
            viewModelScope.launch {
                mapRepository.getRouters().onStart { onRetrievePostListStart() }
                    .map {
                        println(it.data)
                        val hashMap = hashMapOf<Int?, Point>()
                        it.data?.forEach { pointResponse ->
                            hashMap[pointResponse.id] =
                                Point.fromLngLat(pointResponse.longitude, pointResponse.latitude)
                        }
                        println(hashMap)

                        hashMap
                    }
                    .catch { handleApiError(it) }
                    .onCompletion { onRetrievePostListFinish() }
                    .collect {
                        mUiStateFlow.value = UiState.Routers(it)
                    }
            }
        }

        sealed class Event {
            data class Position(val location: Location?) : Event()
        }

        sealed class UiState {
            data class Routers(val routers: HashMap<Int?, Point> = hashMapOf()) : UiState()

            data class Position(val location: Location? = null) : UiState()
        }
    }
