package com.example.footu.ui.shipper.home

import androidx.lifecycle.viewModelScope
import com.example.footu.base.BaseViewModel
import com.example.footu.model.OrderShipModel
import com.example.footu.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderShipViewModel @Inject constructor(
    private val api: ApiService,
) : BaseViewModel() {
    private val _state: MutableStateFlow<State> = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        getOrderShip()
//        viewModelScope.launch {
//            SocketIoManage.mSocket?.on("pendingPrepaidBill") { args ->
//                val count = _state.value.count + 1
//                _state.update {
//                    it.copy(count = count)
//                }
//                onShowSnackbar("Có đơn đặt mới")
//            }
//        }
    }

    fun getOrderShip() {
        viewModelScope.launch {
            flow { emit(api.getOrderShipList()) }
                .onStart { onRetrievePostListStart() }
                .onCompletion { onRetrievePostListFinish() }
                .catch { handleApiError(it) }
                .collect { data ->
                    _state.update { it.copy(orderList = data.data ?: emptyList()) }
                }
        }
    }

    fun onDeleteItem(item: OrderShipModel) {
        val list = _state.value.orderList.toMutableList()
        list.remove(item)
        _state.update { it.copy(orderList = list) }
    }

    fun hideSnackbar() {
        onHideSnackbar()
    }
}

data class State(
    val count: Int = 0,
    val orderList: List<OrderShipModel> = listOf(),
)
