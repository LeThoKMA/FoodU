package com.example.footu.ui.pay

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.footu.EncryptPreference
import com.example.footu.R
import com.example.footu.Request.ItemBillRequest
import com.example.footu.Request.UserOrderRequest
import com.example.footu.base.BaseViewModel
import com.example.footu.model.DetailItemChoose
import com.example.footu.model.PromotionUser
import com.example.footu.model.UserLocationModel
import com.example.footu.network.ApiService
import com.example.footu.socket.SocketIoManage
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.geojson.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class PayConfirmViewModel
    @Inject
    constructor(
        val apiService: ApiService,
        @ApplicationContext context: Context,
        val sharePref: EncryptPreference,
    ) : BaseViewModel() {
        private val _message = MutableLiveData<String>()
        val message: LiveData<String> = _message

        private val _promotions = MutableLiveData<List<PromotionUser>>()
        val promotions: LiveData<List<PromotionUser>> = _promotions

        private val _isShowDialog = MutableLiveData(false)
        val isShowDialog: LiveData<Boolean> = _isShowDialog

        val user = sharePref.getUser()

        private var idOrder = -1
        private val accessToken = context.getString(R.string.mapbox_access_token)

        private val _address = MutableStateFlow("")
        val address: StateFlow<String> = _address

        private val _urlRedirect = MutableLiveData<String>()
        val urlRedirect: LiveData<String> = _urlRedirect

        init {
            getPromotions()
        }

        fun confirmBill(
            itemList: List<DetailItemChoose>,
            promotionUser: PromotionUser? = null,
            price: Int,
            type: Int,
            latLong: Pair<Double, Double>,
        ) {
            viewModelScope.launch {
                val billItems = mutableListOf<ItemBillRequest>()
                itemList.forEach {
                    billItems.add(
                        ItemBillRequest(
                            it.id,
                            it.count,
                            it.totalPrice,
                            it.size?.ordinal,
                            it.textDescription,
                        ),
                    )
                }
                val request = UserOrderRequest(billItems, promotionUser?.id, price)
                val userLocationModel =
                    UserLocationModel(user!!.id, user.fullname, latLong.first, latLong.second)
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
            latLong: Pair<Double, Double>,
            onFindSuccess: () -> Unit,
        ) {
            viewModelScope.launch {
                val billItems = mutableListOf<ItemBillRequest>()
                itemList.forEach { billItems.add(ItemBillRequest(it.id, it.count, it.totalPrice)) }
                val request =
                    UserOrderRequest(
                        billItems,
                        promotionUser?.id,
                        price,
                        address,
                        lat = latLong.first,
                        longitude = latLong.second,
                    )
                flow { emit(apiService.doOrderPending(request)) }
                    .onStart { _isShowDialog.postValue(true) }
                    .onCompletion { createSocket { onFindSuccess() } }
                    .catch { handleApiError(it) }
                    .collect {
                        idOrder = it.data?.id!!
                    }
            }
        }

        fun getAddress(
            latitude: Double,
            longitude: Double,
        ) {
            viewModelScope.launch {
                val mapboxGeocoding =
                    MapboxGeocoding.builder()
                        .accessToken(accessToken)
                        .query(Point.fromLngLat(longitude, latitude))
                        .build()
                mapboxGeocoding.enqueueCall(
                    object : Callback<GeocodingResponse?> {
                        override fun onResponse(
                            call: Call<GeocodingResponse?>,
                            response: Response<GeocodingResponse?>,
                        ) {
                            if (response.body() != null && response.body()?.features()
                                    ?.isNotEmpty() == true
                            ) {
                                val feature: CarmenFeature? = response.body()?.features()?.get(0)
                                _address.value = feature?.placeName().toString()

                                Log.d("Address", address!!.toString())
                                // Ở đây bạn có thể hiển thị địa chỉ trong map hoặc làm bất kỳ hành động nào khác cần thiết.
                            } else {
                                Log.d("Address", "No address found")
                            }
                        }

                        override fun onFailure(
                            call: Call<GeocodingResponse?>,
                            t: Throwable,
                        ) {
                            handleApiError(t)
                        }
                    },
                )
            }
        }

        fun requestPaymentVnPay(
            amount: Int,
            orderInfo: String,
        ) {
            viewModelScope.launch {
                flow { emit(apiService.vnPaySubmitOrder(amount, orderInfo)) }
                    .onStart { onRetrievePostListStart() }
                    .onCompletion { onRetrievePostListFinish() }
                    .catch { handleApiError(it) }
                    .collect {
                        Log.e(">>>>>>>>>>>", it.data.toString())
                        _urlRedirect.postValue(it.data?.url)
                    }
            }
        }

        private fun createSocket(onFindSuccess: () -> Unit) {
            viewModelScope.launch {
                val socket = SocketIoManage.mSocket
                socket?.on("${user?.id}") { args ->
                    val receivedData = args[0] as Boolean
                    _isShowDialog.postValue(false)
                    if (receivedData) {
                        onFindSuccess.invoke()
                        socket.off("${user?.id}")
                    } else {
                        _message.postValue("Không có shipper nhận hàng")
                    }
                }
            }
            // .invokeOnCompletion { SocketIoManage.mSocket?.off("${user?.id}") }
        }
    }
