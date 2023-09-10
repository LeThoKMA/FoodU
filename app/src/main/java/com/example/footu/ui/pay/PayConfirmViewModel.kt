package com.example.footu.ui.pay

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.footu.Request.ItemBillRequest
import com.example.footu.Request.UserOrderRequest
import com.example.footu.base.BaseViewModel
import com.example.footu.model.DetailItemChoose
import com.example.footu.model.PromotionUser
import com.example.footu.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PayConfirmViewModel @Inject constructor(
    val apiService: ApiService,
) : BaseViewModel() {

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _promotions = MutableLiveData<List<PromotionUser>>()
    val promotions: LiveData<List<PromotionUser>> = _promotions

    init {
        getPromotions()
    }

    fun confirmBill(
        itemList: List<DetailItemChoose>,
        promotionUser: PromotionUser? = null,
        price: Int,
    ) {
        viewModelScope.launch {
            val billItems = mutableListOf<ItemBillRequest>()
            itemList.forEach { billItems.add(ItemBillRequest(it.id, it.count, it.totalPrice)) }
            val request = UserOrderRequest(billItems, promotionUser?.id, price)
            flow {
                emit(apiService.doPayment(request))
            }.flowOn(Dispatchers.IO)
                .onStart { onRetrievePostListStart() }
                .onCompletion { onRetrievePostListFinish() }
                .catch { handleApiError(it) }
                .collect {
                    _message.postValue(it.message!!)
                }
        }
    }

    fun getPromotions() {
        viewModelScope.launch {
            flow {
                emit(apiService.getPromotions())
            }.flowOn(Dispatchers.IO)
                .onStart { onRetrievePostListStart() }
                .onCompletion { onRetrievePostListFinish() }
                .catch { handleApiError(it) }
                .collect {
                    _promotions.postValue(it.data ?: emptyList())
                }
        }
    }
}
