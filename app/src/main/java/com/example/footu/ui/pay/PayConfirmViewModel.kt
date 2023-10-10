package com.example.footu.ui.pay

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.footu.MyPreference
import com.example.footu.Request.ItemBillRequest
import com.example.footu.Request.UserOrderRequest
import com.example.footu.base.BaseViewModel
import com.example.footu.dagger2.App
import com.example.footu.model.DetailItemChoose
import com.example.footu.model.PromotionUser
import com.example.footu.network.ApiService
import com.example.footu.socket.SocketIoManage
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
    private val app: App,
) : BaseViewModel() {

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _promotions = MutableLiveData<List<PromotionUser>>()
    val promotions: LiveData<List<PromotionUser>> = _promotions

    private val _isShowDialog = MutableLiveData(false)
    val isShowDialog: LiveData<Boolean> = _isShowDialog

    val user = MyPreference.getInstance(app)?.getUser()

    private var idOrder = -1

    init {
        getPromotions()
    }

    fun confirmBill(
        itemList: List<DetailItemChoose>,
        promotionUser: PromotionUser? = null,
        price: Int,
        type: Int,
    ) {
        viewModelScope.launch {
            val billItems = mutableListOf<ItemBillRequest>()
            itemList.forEach { billItems.add(ItemBillRequest(it.id, it.count, it.totalPrice)) }
            val request = UserOrderRequest(billItems, promotionUser?.id, price)
            flow {
                if (type == 0) {
                    emit(apiService.doPayment(request))
                } else {
                    emit(apiService.doPaymentForOrderShip(idOrder))
                }
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

    fun listenResponseByShipper(
        itemList: List<DetailItemChoose>,
        promotionUser: PromotionUser? = null,
        price: Int,
        address: String,
        onFindSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            val billItems = mutableListOf<ItemBillRequest>()
            itemList.forEach { billItems.add(ItemBillRequest(it.id, it.count, it.totalPrice)) }
            val request = UserOrderRequest(billItems, promotionUser?.id, price, address)
            flow { emit(apiService.doOrderPending(request)) }
                .onStart { _isShowDialog.postValue(true) }
                .onCompletion { createSocket { onFindSuccess() } }
                .catch { handleApiError(it) }
                .collect {
                    idOrder = it.data?.id!!
                }
        }
    }

    fun createSocket(onFindSuccess: () -> Unit) {
        viewModelScope.launch {
            val userID = MyPreference.getInstance(app)?.getUser()?.id
            SocketIoManage.mSocket?.on("$userID") { args ->
                val receivedData = args[0] as Boolean
                _isShowDialog.postValue(false)
                if (receivedData) {
                    onFindSuccess.invoke()
                } else {
                    _message.postValue("Không có shipper nhận hàng")
                }
            }
        }
    }
}
