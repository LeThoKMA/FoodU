package com.example.footu.ui.login

//import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallConfigProvider
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.footu.MyPreference
import com.example.footu.Request.RegisterRequest
import com.example.footu.base.BaseViewModel
import com.example.footu.dagger2.App
import com.example.footu.hilt.NetworkModule
import com.example.footu.model.LoginRequest
import com.example.footu.network.ApiService
import com.example.footu.utils.AppKey
import com.example.footu.utils.toast
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    val apiService: ApiService,
    val appInstance: App,
    @ApplicationContext context: Context,
) : BaseViewModel() {

    private val _doLogin = MutableLiveData<Int?>()
    val doLogin: LiveData<Int?> = _doLogin
    private var myPreference: MyPreference? = MyPreference.getInstance()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            flow {
                emit(
                    apiService.login(
                        LoginRequest(
                            email,
                            password,
                            AppKey.getPublicKey()
                        )
                    )
                )
            }.flowOn(Dispatchers.IO)
                .onStart { onRetrievePostListStart() }
                .onCompletion { onRetrievePostListFinish() }
                .catch { Toast.makeText(appInstance, "Error", Toast.LENGTH_LONG).show() }
                .collect {
                    if (it.data?.token?.isNotBlank() == true) {
                        NetworkModule.mToken = it.data.token
                        fetchUserInfo(email, password)
                    }
                }
        }
    }

    private fun fetchUserInfo(email: String, password: String) {
        viewModelScope.launch {
            flow { emit(apiService.fetchUserInfo()) }.flowOn(Dispatchers.IO)
                .onStart { onRetrievePostListStart() }
                .onCompletion { onRetrievePostListFinish() }
                .catch { Toast.makeText(appInstance, it.message, Toast.LENGTH_LONG).show() }
                .collect {
                    it.data?.let { it1 ->
                        myPreference?.saveUser(it1, password)
                        initCallService(it1.id.toString(), it1.fullname.toString())
                    }
                    _doLogin.postValue(it.data?.role)
                }
        }
    }

    private fun initCallService(userID: String, userName: String) {
        val appID: Long = 190669332
        val appSign = "c5663e686bbe551ff22eedafeb47c6b9f842f1636b1373bf2fe1494931e9f38a"

        val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()
        // callInvitationConfig.notifyWhenAppRunningInBackgroundOrQuit = true
//        callInvitationConfig.provider =
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
//        val notificationConfig = ZegoNotificationConfig()
//        notificationConfig.sound = "zego_uikit_sound_call"
//        notificationConfig.channelID = "CallInvitation"
//        notificationConfig.channelName = "CallInvitation"
//        ZegoUIKitPrebuiltCallInvitationService.init(
//            App.app,
//            appID,
//            appSign,
//            userID,
//            userName,
//            callInvitationConfig,
//        )
    }

    fun register(phone: String, name: String, pass: String, passRepeat: String) {
        if (phone.isBlank()) {
            appInstance.toast("Số điện thoại không được để trống")
            return
        }

        if (name.isBlank()) {
            appInstance.toast("Tên không được để trống")
            return
        }

        if (pass.isBlank()) {
            appInstance.toast("Mật khẩu không được để trống")
            return
        }

        if (passRepeat.isBlank()) {
            appInstance.toast("Hãy nhập lại mật khẩu")
            return
        }

        if (pass != passRepeat) {
            appInstance.toast("Mật khẩu không trùng khớp")
            return
        }

        viewModelScope.launch {
            flow { emit(apiService.register(RegisterRequest(phone, pass, name))) }
                .onStart { onRetrievePostListStart() }
                .onCompletion { onRetrievePostListFinish() }
                .catch { handleApiError(it) }
                .collect {
                    if (it.data?.token?.isNotBlank() == true) {
                        NetworkModule.mToken = it.data.token
                        fetchUserInfo(phone, pass)
                    }
                }
        }
    }
}
