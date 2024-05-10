package com.example.footu.ui.chat.activity

import androidx.lifecycle.viewModelScope
import com.example.footu.MyPreference
import com.example.footu.Request.HintRequest
import com.example.footu.Request.MessageRequest
import com.example.footu.Response.HintResponse
import com.example.footu.Response.MessageResponse
import com.example.footu.base.BaseViewModel
import com.example.footu.network.ApiService
import com.example.footu.socket.SocketIoManage
import com.example.footu.utils.AppKey
import com.example.footu.utils.byteArrayToString
import com.example.footu.utils.generateRandomIV
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class UserChatViewModel @Inject constructor(
    val apiService: ApiService,
) : BaseViewModel() {
    private val user = MyPreference.getInstance()?.getUser()
    private val _stateFlow = MutableStateFlow<StateUi>(StateUi.TotalMessage())
    private var hintResponse: HintResponse? = null
    val stateFlow: StateFlow<StateUi> = _stateFlow

    fun getHintIdAndMessageData(otherId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                user?.id?.let {
                    emit(apiService.checkHintId(HintRequest(userId1 = it, userId2 = otherId)))
                }
            }.collect {
                hintResponse = it.data
                it.data?.let { data ->
                    if (user?.id == data.user1.id) {
                        data.user1.publicKey?.let { it1 ->
                            AppKey.calculateKey(
                                it1
                            )
                        }
                    } else {
                        data.user2.publicKey?.let { it1 -> AppKey.calculateKey(it1) }
                    }
                    println(">>>>>>>>>>>" + hintResponse)
                    fetchDataMessage(data.id)
                }
            }
        }
    }

    private suspend fun fetchDataMessage(id: Int) {
        flow { emit(apiService.fetchMessage(id, 0)) }.onStart {
            onRetrievePostListStart()
        }.catch {
            handleApiError(it)
        }.onCompletion {
            onRetrievePostListFinish()
        }
            .map {
                if (it.data?.messageList?.isNotEmpty() == true) {
                    it.data.messageList.map { message ->
                        message.copy(
                            messageId = message.messageId,
                            hintId = message.hintId,
                            fromUser = message.fromUser,
                            toUser = message.toUser,
                            content = AppKey.decrypt(message.content, message.iv),
                            time = message.time,
                        )
                    }
                }
                it.data?.messageList
            }
            .collect {
                _stateFlow.value = StateUi.TotalMessage(it)
            }
    }

    fun setupSocket() {
        viewModelScope.launch {
            SocketIoManage.mSocket?.on("chat:${hintResponse?.id}") { args ->
                val receivedData = Gson().fromJson(args[0].toString(), MessageResponse::class.java)
                val contentDecrypt = AppKey.decrypt(
                    receivedData.content,
                    receivedData.iv,
                )
                val messageResponse = receivedData.copy(
                    content = contentDecrypt,
                )
                _stateFlow.value = StateUi.Message(messageResponse)
            }
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            val iv = generateRandomIV()
            val encryptMessage = AppKey.encrypt(content, iv)
            val toUserId =
                if (user?.id != hintResponse?.user1?.id) hintResponse?.user1?.id else hintResponse?.user2?.id
            val messageRequest = MessageRequest(
                hintResponse?.id ?: 0,
                user?.id ?: 0,
                toUserId ?: 0,
                encryptMessage,
                iv.byteArrayToString(),
            )
            val obj = JSONObject(
                GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create()
                    .toJson(messageRequest),
            )
            SocketIoManage.mSocket?.emit("chat:${hintResponse?.id}", obj)
        }
    }

    sealed class StateUi {
        data class TotalMessage(
            val messageList: List<MessageResponse>? = listOf(),
        ) : StateUi()

        data class Message(
            val messageResponse: MessageResponse? = null,
        ) : StateUi()
    }

    override fun onCleared() {
        SocketIoManage.mSocket?.off("chat:${hintResponse?.id}")
        super.onCleared()
    }
}
