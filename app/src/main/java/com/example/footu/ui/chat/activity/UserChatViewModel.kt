package com.example.footu.ui.chat.activity

import androidx.lifecycle.viewModelScope
import com.example.footu.MyPreference
import com.example.footu.Request.HintRequest
import com.example.footu.base.BaseViewModel
import com.example.footu.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserChatViewModel @Inject constructor(
    val apiService: ApiService,
) : BaseViewModel() {
    private val user = MyPreference.getInstance()?.getUser()

    private fun getHintIdAndMessageData(otherId: Int) {
        viewModelScope.launch {
            flow {
                user?.id?.let {
                    emit(apiService.checkHintId(HintRequest(userId1 = it, userId2 = otherId)))
                }
            }

        }
    }
}
