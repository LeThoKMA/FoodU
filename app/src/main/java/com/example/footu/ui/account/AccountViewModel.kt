package com.example.footu.ui.account

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.footu.MyPreferencee
import com.example.footu.Request.ChangePassRequest
import com.example.footu.base.BaseViewModel
import com.example.footu.network.ApiService
import com.example.footu.utils.toast
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    var apiService: ApiService,
    @ApplicationContext
    var context: Context,
    private val sharePref: MyPreferencee
) : BaseViewModel() {
    val logout = MutableLiveData<Boolean>()
    val message = MutableLiveData<String>()

    fun logout() {
        viewModelScope.launch {
            flow { emit(apiService.logout()) }
                .catch { handleApiError(it) }
                .collect {
                    sharePref.logout()
                    logout.postValue(true)
                }
        }
    }

    fun changePass(old: String, new: String, repeat: String) {
        viewModelScope.launch {
            if (new != repeat) {
                context.toast("Mật khẩu không trùng khớp")
                return@launch
            }
            if (new.length < 6) {
                context.toast("Mật khẩu tối thiểu 6 kí tự")
                return@launch
            }
            flow {
                emit(
                    apiService.changePass(
                        ChangePassRequest(
                            old,
                            new,
                        ),
                    ),
                )
            }.onStart { onRetrievePostListStart() }
                .onCompletion { onRetrievePostListFinish() }
                .catch { handleApiError(it) }
                .collect {
                    it.message?.let { it1 -> message.postValue(it1) }
                }
        }
    }
}
