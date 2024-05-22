package com.example.footu.ui.orderlist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.footu.Response.BillDetailResponse
import com.example.footu.base.BaseViewModel
import com.example.footu.model.OrderItem
import com.example.footu.network.ApiService
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

class OrderListViewModel(val context: Context) : BaseViewModel() {
    @Inject
    lateinit var apiService: ApiService

    private val _orderList = MutableLiveData<List<OrderItem>>()
    val orderList: LiveData<List<OrderItem>> = _orderList

    private val _orderDetail = MutableLiveData<BillDetailResponse>()
    val orderDetail: LiveData<BillDetailResponse> = _orderDetail

    var page = 1

    init {
        fetchOrderList()
    }

    private fun fetchOrderList() {
        viewModelScope.launch {
            flow { emit(apiService.getOrderList(page)) }
                .onStart { onRetrievePostListStart() }
                .onCompletion {
                    onRetrievePostListFinish()
                    page++
                }
                .catch { handleApiError(it) }
                .collect {
                    it.data?.let { _orderList.postValue(it) }
                }
        }
    }

    fun getOrderDetail(id: Int) {
        viewModelScope.launch {
            flow { emit(apiService.getOrderDetail(id)) }
                .onStart { onRetrievePostListStart() }
                .onCompletion { onRetrievePostListFinish() }
                .collect {
                    it.data?.let {
                        _orderDetail.postValue(it)
                    }
                }
        }
    }
}
