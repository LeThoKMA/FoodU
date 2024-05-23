package com.example.footu.ui.shipper

// import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallConfigProvider
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.footu.MyPreference
import com.example.footu.base.BaseViewModel
import com.example.footu.dagger2.App
import com.example.footu.model.OrderShipModel
import com.example.footu.model.User
import com.example.footu.network.ApiService
import com.example.footu.utils.toast
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import com.zegocloud.uikit.service.defines.ZegoUIKitUser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.Collections
import javax.inject.Inject

@HiltViewModel
class OrderDetailViewModel
    @Inject
    constructor(
        private val apiService: ApiService,
        @ApplicationContext
        private val context: Context,
    ) : BaseViewModel() {
        private val _onSuccess = MutableStateFlow(false)
        val onSuccess: StateFlow<Boolean> = _onSuccess.asStateFlow()

        private lateinit var orderDetail: OrderShipModel
        private var user: User? = null

        init {
            user = MyPreference.getInstance()?.getUser()
            val userID = user?.id
            val userName = user?.fullname
            initCallService(userID.toString(), userName.toString())
        }

        private fun initCallService(
            userID: String,
            userName: String,
        ) {
            val appID: Long = 190669332
            val appSign = "c5663e686bbe551ff22eedafeb47c6b9f842f1636b1373bf2fe1494931e9f38a"

            val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()
            // callInvitationConfig.notifyWhenAppRunningInBackgroundOrQuit = true
            //      callInvitationConfig.provider =
//            ZegoUIKitPrebuiltCallConfigProvider { invitationData ->
//                var config: ZegoUIKitPrebuiltCallConfig? = null
//                val isVideoCall = invitationData.type == ZegoInvitationType.VIDEO_CALL.value
//                val isGroupCall = invitationData.invitees.size > 1
//                config = if (isVideoCall && isGroupCall) {
//                    ZegoUIKitPrebuiltCallConfig.groupVideoCall()
//                } else if (!isVideoCall && isGroupCall) {
//                    ZegoUIKitPrebuiltCallConfig.groupVoiceCall()
//                } else if (!isVideoCall) {
//                    ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall()
//                } else {
//                    ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall()
//                }
//                config
//            }
            val notificationConfig = ZegoNotificationConfig()
            notificationConfig.sound = "zego_uikit_sound_call"
            notificationConfig.channelID = "CallInvitation"
            notificationConfig.channelName = "CallInvitation"
            ZegoUIKitPrebuiltCallInvitationService.init(
                App.getInstance(),
                appID,
                appSign,
                userID,
                userName,
                callInvitationConfig,
            )
        }

        fun acceptOrder(item: OrderShipModel) {
            viewModelScope.launch {
                flow { emit(apiService.acceptOrder(item.id)) }
                    .onStart { onRetrievePostListStart() }
                    .onCompletion {
                        onRetrievePostListFinish()
                    }
                    .catch { handleApiError(it) }
                    .collect {
                        context.toast(it.message.toString())
                        _onSuccess.value = true
                        //        SocketIoManage.mSocket?.emit("send_locate", "1")
                    }
            }
        }

        fun eventDone(id: Int) {
            viewModelScope.launch {
                flow { emit(apiService.doneOrder(id)) }
                    .onStart { onRetrievePostListStart() }
                    .onCompletion {
                        onRetrievePostListFinish()
                    }
                    .catch { handleApiError(it) }
                    .collect {
                        context.toast(it.message.toString())
                        _onSuccess.value = true
                    }
            }
        }

        fun setupCall(
            targetUserID: String,
            targetUserName: String,
        ) {
            val button = ZegoSendCallInvitationButton(context)
            button.setIsVideoCall(true)
            button.resourceID = "zego_uikit_call"
            button.setInvitees(
                Collections.singletonList(
                    ZegoUIKitUser(
                        targetUserID,
                        targetUserName,
                    ),
                ),
            )
        }

//    private fun calculateRoute() {
//        viewModelScope.launch {
// //            val origin =
// //                "${MyLocationWorker.location.latitude},${MyLocationWorker.location.longitude}"
// //            val destination = "${orderDetail.lat}, ${orderDetail.longitude}"
//            flow {
//                emit(
//                    apiMapService.getDirections(
//                        "22.257368,106.4759865",
//                        "22.2541191,106.4700219",
//                        context.getString(R.string.map_api_key),
//                    ),
//                )
//            }.onStart { _onSuccess.value = false }
//                .onCompletion { _onSuccess.value = true }
//                .collect {
//                    Timber.tag(">>>>>>>>>>>>").e(it.routes.toString())
//                }
//        }
//    }

        fun setOrderDetail(orderShipModel: OrderShipModel) {
            orderDetail = orderShipModel
        }
    }
