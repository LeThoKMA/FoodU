package com.example.footu.ui.chat

import androidx.lifecycle.viewModelScope
import com.example.footu.EncryptPreference
import com.example.footu.Response.HintMessageResponse
import com.example.footu.base.BaseViewModel
import com.example.footu.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HintChatViewModel @Inject constructor(
    private val apiService: ApiService,
    private val sharePref: EncryptPreference
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
                    it.data?.map { data ->
                        val dmpUser =
                            if (data.lastMessage?.fromUser?.id == user.id) data.lastMessage.toUser else data.lastMessage?.fromUser
                        data.copy(otherUser = dmpUser)
                    }
                }
                .collect {
                    _uiState.value = it?.toMutableList() ?: mutableListOf()
                }

        }
    }
}
