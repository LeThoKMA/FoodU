package com.example.footu.ui.chat

import androidx.lifecycle.viewModelScope
import com.example.footu.MyPreference
import com.example.footu.MyPreferencee
import com.example.footu.Response.HintMessageResponse
import com.example.footu.base.BaseViewModel
import com.example.footu.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HintChatViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sharePref: MyPreferencee
) : BaseViewModel() {

    private val user = sharePref.getUser()
    private val _uiState = MutableStateFlow(mutableListOf<HintMessageResponse>())
    val uiState: StateFlow<MutableList<HintMessageResponse>> = _uiState

    init {
        getAllHintMessage()
    }

    private fun getAllHintMessage() {
        viewModelScope.launch {
            flow { emit(apiService.getAllHintMessage(user.id)) }
                .map {
                    it.data?.filter { data -> data.messageResponse != null }?.map { data ->
                        val dmpUser =
                            if (data.messageResponse?.fromUser?.id == user.id) data.messageResponse.toUser else data.messageResponse?.fromUser
                        data.copy(otherUser = dmpUser)
                    }
                }
                .collect {
                    _uiState.value = it?.toMutableList() ?: mutableListOf()
                }

        }
    }
}
