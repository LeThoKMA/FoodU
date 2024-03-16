package com.example.footu.ui.shipper.picked

import androidx.lifecycle.viewModelScope
import com.example.footu.base.BaseViewModel
import com.example.footu.dagger2.App
import com.example.footu.model.OrderShipModel
import com.example.footu.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderShipPickedViewModel @Inject constructor(
    private val apiService: ApiService,
    private val appInstance: App,
) :
    BaseViewModel() {

    private val _uiState = MutableStateFlow(State())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            flow { emit(apiService.getOrdersPicked()) }
                .onStart { onRetrievePostListStart() }
                .onCompletion { onRetrievePostListFinish() }
                .catch { handleApiError(it) }
                .collect { data ->
                    _uiState.update {
                        it.copy(orders = data.data ?: listOf())
                    }
                }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            flow { emit(apiService.getOrdersPicked()) }
                .onStart { onRetrievePostListStart() }
                .onCompletion { onRetrievePostListFinish() }
                .catch { handleApiError(it) }
                .collect { data ->
                    _uiState.update {
                        it.copy(orders = data.data ?: listOf())
                    }
                }
        }
    }

    fun onDeleteItem(item: OrderShipModel) {
        val list = _uiState.value.orders.toMutableList()
        list.remove(item)
        _uiState.update { it.copy(orders = list) }
    }

    fun hideSnackbar() {
        onHideSnackbar()
    }

    data class State(var orders: List<OrderShipModel> = listOf())
}
