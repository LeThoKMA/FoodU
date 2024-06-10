package com.example.footu.ui.map

import android.location.Location
import androidx.lifecycle.viewModelScope
import com.example.footu.base.BaseViewModel
import com.example.footu.model.ShipperLocation
import com.example.footu.socket.SocketIoManage
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackingLocationViewModel
    @Inject
    constructor() : BaseViewModel() {
//    private val _locationStateFlow: MutableStateFlow<LocationState?> = MutableStateFlow(null)
//    val locationStateFlow: StateFlow<LocationState?> = _locationStateFlow

        private val _locationStateFlow = Channel<ShipperLocation>(Channel.UNLIMITED)
        val locationStateFlow = _locationStateFlow.receiveAsFlow()

        fun handleEvent(event: Event) {
            when (event) {
                is Event.Position -> {
                    viewModelScope.launch {
                        flow { emit(event.location) }
                            .collect {
                            }
                    }
                }

                else -> {}
            }
        }

        fun startFollowingShipper(id: Int) {
            viewModelScope.launch {
                SocketIoManage.mSocket?.on("$id") { args ->
                    viewModelScope.launch {
                        val receivedData = Gson().fromJson(args[0].toString(), ShipperLocation::class.java)
                        println(receivedData)
                        _locationStateFlow.send(receivedData)
                    }
//                _locationStateFlow.update {
//                    it?.copy(oldLocation = it.newLocation, newLocation = receivedData)
//                }
                }
            }
        }

        override fun onCleared() {
            SocketIoManage.mSocket?.off()
            super.onCleared()
        }

        sealed class Event {
            data class Position(val location: Location?) : Event()
        }

        data class LocationState(
            var newLocation: ShipperLocation? = null,
            var oldLocation: ShipperLocation? = null,
        )
    }
