package com.example.footu.ui.Order

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.footu.Request.ItemBillRequest
import com.example.footu.Response.BillResponse
import com.example.footu.Response.CategoryResponse
import com.example.footu.base.BaseViewModel
import com.example.footu.model.DetailItemChoose
import com.example.footu.model.Item
import com.example.footu.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    val apiService: ApiService,
) : BaseViewModel() {
    val dataItems = MutableLiveData<ArrayList<Item>>()

    private val _price = MutableLiveData<Int>()
    val price: LiveData<Int> = _price

    private val _confirm = MutableLiveData<BillResponse?>()
    val confirm: LiveData<BillResponse?> = _confirm

    private val _category = MutableLiveData<List<CategoryResponse>?>()
    val category: MutableLiveData<List<CategoryResponse>?> = _category

    val message = MutableLiveData<String>()

    var mapDetailItemChoose: HashMap<Int, DetailItemChoose> = hashMapOf()
    var list: ArrayList<Item?> = arrayListOf()

    var size = 0
    var totalPrice = 0

    init {
        fetchItems()
    }

    fun addItemToBill(item: DetailItemChoose) {
        if (item.flag == null || !item.flag!!) {
            val itemRemove = DetailItemChoose(item.id, item.name, 0, 0, 0, null, false)
            mapDetailItemChoose.put(itemRemove.id ?: 0, itemRemove)
        } else {
            mapDetailItemChoose.put(item.id ?: 0, item)
        }
        var total = 0
        for (item in mapDetailItemChoose) {
            total += item.value.totalPrice ?: 0
        }
        this.totalPrice = total
        _price.postValue(total)
    }

    fun payConfirm(list: List<DetailItemChoose>) {
        if (totalPrice > 0) {
            viewModelScope.launch {
                val bilLRequest: MutableList<ItemBillRequest> = mutableListOf()
                list.forEach { bilLRequest.add(ItemBillRequest(it.id, it.count, it.totalPrice)) }
                flow { emit(apiService.makeBill(bilLRequest)) }
                    .onStart { onRetrievePostListStart() }
                    .onCompletion {
                        onRetrievePostListFinish()
                    }
                    .catch { Log.e("TAG", it.toString()) }
                    .collect {
                        if (it.data != null) {
                            _confirm.postValue(it.data)
                        }
                    }
            }
        } else {
            message.postValue("Vui lòng chọn đồ uống trước")
        }
    }

    private fun fetchItems() {
        viewModelScope.launch {
            flow {
                emit(
                    apiService.getCategory(),
                )
            }.onStart { onRetrievePostListStart() }
                .onCompletion { onRetrievePostListFinish() }
                .collect {
                }
        }
    }

    fun getProductByType(id: Int) {
        viewModelScope.launch {
            flow { emit(apiService.getProductByType(id)) }
                .onStart { onRetrievePostListStart() }
                .onCompletion { onRetrievePostListFinish() }
                .collect {
                    dataItems.postValue(it.data as ArrayList<Item>?)
                }
        }
    }
}
