package com.example.footu.ui.login

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.footu.MyPreference
import com.example.footu.Request.RegisterRequest
import com.example.footu.base.BaseViewModel
import com.example.footu.hilt.NetworkModule
import com.example.footu.model.LoginRequest
import com.example.footu.network.ApiService
import com.example.footu.utils.toast
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
    @ApplicationContext
    val context: Context,
) : BaseViewModel() {

    private val _doLogin = MutableLiveData<Boolean>()
    val doLogin: LiveData<Boolean> = _doLogin
    private var myPreference: MyPreference = MyPreference().getInstance(context)!!

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            flow { emit(apiService.login(LoginRequest(email, password))) }.flowOn(Dispatchers.IO)
                .onStart { onRetrievePostListStart() }
                .onCompletion { onRetrievePostListFinish() }
                .catch { Toast.makeText(context, it.message, Toast.LENGTH_LONG).show() }
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
                .catch { Toast.makeText(context, it.message, Toast.LENGTH_LONG).show() }
                .collect {
                    it.data?.let { it1 -> myPreference.saveUser(it1, password) }
                    _doLogin.postValue(true)
                }
        }
    }

    fun register(phone: String, name: String, pass: String, passRepeat: String) {
        if (phone.isBlank()) {
            context.toast("Số điện thoại không được để trống")
            return
        }

        if (name.isBlank()) {
            context.toast("Tên không được để trống")
            return
        }

        if (pass.isBlank()) {
            context.toast("Mật khẩu không được để trống")
            return
        }

        if (passRepeat.isBlank()) {
            context.toast("Hãy nhập lại mật khẩu")
            return
        }

        if (pass != passRepeat) {
            context.toast("Mật khẩu không trùng khớp")
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
