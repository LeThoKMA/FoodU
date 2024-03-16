package com.example.footu.ui.map

import android.content.Context
import android.location.Location
import androidx.lifecycle.viewModelScope
import com.example.footu.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouterViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : BaseViewModel() {
    private val _locationStateFlow: MutableStateFlow<Location?> = MutableStateFlow(null)
    val locationStateFlow: StateFlow<Location?> = _locationStateFlow
    fun handleEvent(event: Event) {
        when (event) {
            is Event.Position -> {
                viewModelScope.launch {
                    flow { emit(event.location) }
                        .collect {
                            _locationStateFlow.value = it
                        }
                }
            }

            else -> {}
        }
    }

    sealed class Event {
        data class Position(val location: Location?) : Event()
    }
}
