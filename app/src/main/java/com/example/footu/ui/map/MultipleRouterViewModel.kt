package com.example.footu.ui.map

import android.location.Location
import androidx.lifecycle.viewModelScope
import com.example.footu.base.BaseViewModel
import com.example.footu.model.OrderShipModel
import com.example.footu.model.PointWithId
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.style.expressions.dsl.generated.get
import dagger.hilt.android.lifecycle.HiltViewModel
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
        private val mapRepository: MapRepository,
    ) : BaseViewModel() {
        private val mUiStateFlow: MutableStateFlow<UiState> = MutableStateFlow(UiState.Position())
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

                is Event.DetailBill -> {
                    getDetailBill(event.idBill)
                }

                else -> {}
            }
        }

        private fun getDetailBill(idBill: Int) {
            viewModelScope.launch {
                mapRepository.getDetailPendingBill(idBill).onStart { onRetrievePostListStart() }
                    .catch { handleApiError(it) }
                    .onCompletion { onRetrievePostListFinish() }
                    .collect {
                        mUiStateFlow.value = UiState.DetailBill(it.data)
                    }
            }
        }

        private fun getRouters() {
            viewModelScope.launch {
                mapRepository.getRouters().onStart { onRetrievePostListStart() }
                    .map {
                        it.data?.map { pointResponse ->
                            PointWithId(
                                pointResponse.id,
                                Point.fromLngLat(pointResponse.longitude, pointResponse.latitude),
                            )
                        }
                    }
                    .catch { handleApiError(it) }
                    .onCompletion { onRetrievePostListFinish() }
                    .collect {
                        it?.let { mUiStateFlow.value = UiState.Routers(it) }
                    }
            }
        }

        sealed class Event {
            data class Position(val location: Location?) : Event()

            data class DetailBill(val idBill: Int) : Event()
        }

        sealed class UiState {
            data class Routers(val routers: List<PointWithId> = listOf()) : UiState()

            data class Position(val location: Location? = null) : UiState()

            data class DetailBill(val data: OrderShipModel? = null) : UiState()
        }
    }
