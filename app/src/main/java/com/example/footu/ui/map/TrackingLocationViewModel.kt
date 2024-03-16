package com.example.footu.ui.map

import android.content.Context
import android.location.Location
import androidx.lifecycle.viewModelScope
import com.example.footu.base.BaseViewModel
import com.example.footu.model.ShipperLocation
import com.example.footu.socket.SocketIoManage
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackingLocationViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : BaseViewModel() {
    private val _locationStateFlow: MutableStateFlow<ShipperLocation?> = MutableStateFlow(null)
    val locationStateFlow: StateFlow<ShipperLocation?> = _locationStateFlow

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
                val receivedData = Gson().fromJson(args[0].toString(), ShipperLocation::class.java)
                _locationStateFlow.value = receivedData
            }
        }
    }

    sealed class Event {
        data class Position(val location: Location?) : Event()
    }
}
