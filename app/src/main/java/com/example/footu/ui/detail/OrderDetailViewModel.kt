package com.example.footu.ui.detail

import androidx.lifecycle.viewModelScope
import com.example.footu.MyPreferencee
import com.example.footu.base.BaseViewModel
import com.example.footu.model.OrderShipModel
import com.example.footu.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    val apiService: ApiService,
    private val sharePref: MyPreferencee,
) : BaseViewModel() {
    private val _ordersDetail = MutableStateFlow<List<OrderShipModel>>(emptyList())
    val ordersDetail: StateFlow<List<OrderShipModel>> = _ordersDetail
    private val user = sharePref.getUser()

    init {
        viewModelScope.launch {
            flow { emit(apiService.getOrdersDetail(user!!.id)) }.onStart { onRetrievePostListStart() }
                .onCompletion { onRetrievePostListFinish() }
                .catch { handleApiError(it) }
                .collect {
                    _ordersDetail.value = it.data ?: emptyList()
                }
        }
    }
}
