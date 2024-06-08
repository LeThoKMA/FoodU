package com.example.footu.ui.chat.activity

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.example.footu.MyPreferencee
import com.example.footu.Request.HintRequest
import com.example.footu.Request.MessageRequest
import com.example.footu.Response.HintResponse
import com.example.footu.Response.MessageResponse
import com.example.footu.base.BaseViewModel
import com.example.footu.dagger2.App
import com.example.footu.network.ApiService
import com.example.footu.socket.SocketIoManage
import com.example.footu.utils.AppKey
import com.example.footu.utils.byteArrayToString
import com.example.footu.utils.convertVideoToByteArray
import com.example.footu.utils.generateRandomIV
import com.example.footu.utils.optimizeAndConvertImageToByteArray
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class UserChatViewModel
@Inject
constructor(
    val apiService: ApiService,
    private val sharePref: MyPreferencee,
) : BaseViewModel() {
    private val user = sharePref.getUser()
    private val _stateFlow = MutableStateFlow<StateUi>(StateUi.TotalMessage())
    private var hintResponse: HintResponse? = null
    private var totalPage = 0
    val stateFlow: StateFlow<StateUi> = _stateFlow

    fun getHintIdAndMessageData(otherId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            flow {
                user?.id?.let {
                    emit(apiService.checkHintId(HintRequest(userId1 = it, userId2 = otherId)))
                }
            }.collect {
                hintResponse = it.data
                if (user?.id == hintResponse?.user1?.id) {
                    hintResponse?.user2?.publicKey?.let { it1 ->
                        AppKey.calculateKey(
                            it1,
                        )
                    }
                } else {
                    hintResponse?.user1?.publicKey?.let { it1 -> AppKey.calculateKey(it1) }
                }
                hintResponse?.id?.let { data ->
                    fetchDataMessage(data)
                }
            }
        }
    }

    private fun fetchDataMessage(
        id: Int,
        page: Int = 0,
    ) {
        viewModelScope.launch {
            flow { emit(apiService.fetchMessage(id, page)) }.onStart {
                onRetrievePostListStart()
            }.catch {
                handleApiError(it)
            }.onCompletion {
                onRetrievePostListFinish()
            }
                .map {
                    totalPage = it.data?.totalPage ?: 0
                    it.data?.messageList?.map { message ->
                        val isSendByUser = message.fromUser.id == user.id
                        if (message.type == 0) {
                            message.copy(
                                messageId = message.messageId,
                                hintId = message.hintId,
                                fromUser = message.fromUser,
                                toUser = message.toUser,
                                content = AppKey.decrypt(message.content, message.iv),
                                time = message.time,
                                isSendByUser = isSendByUser,
                            )
                        } else {
                            message.copy(
                                messageId = message.messageId,
                                hintId = message.hintId,
                                fromUser = message.fromUser,
                                toUser = message.toUser,
                                byteArray = AppKey.decryptByteArray(message.content, message.iv),
                                time = message.time,
                                content = "",
                                isSendByUser = isSendByUser,
                            )
                        }
                    }
                        ?.filter { messageResponse ->
                            messageResponse.content != null || messageResponse.byteArray?.isNotEmpty() == true
                        }
                }
                .flowOn(Dispatchers.IO)
                .collect {
                    _stateFlow.value = StateUi.TotalMessage(it)
                }
        }
    }

    fun loadMoreDataMessage(page: Int = 0) {
        if (page <= totalPage) {
            viewModelScope.launch {
                flow { emit(apiService.fetchMessage(hintResponse?.id!!, page)) }.onStart {
                    onRetrievePostListStart()
                }.catch {
                    handleApiError(it)
                }.onCompletion {
                    onRetrievePostListFinish()
                }
                    .map {
                        totalPage = it.data?.totalPage ?: 0
                        it.data?.messageList?.map { message ->
                            val isSendByUser = message.fromUser.id == user.id
                            if (message.type == 0) {
                                message.copy(
                                    messageId = message.messageId,
                                    hintId = message.hintId,
                                    fromUser = message.fromUser,
                                    toUser = message.toUser,
                                    content = AppKey.decrypt(message.content, message.iv),
                                    time = message.time,
                                    isSendByUser = isSendByUser,
                                )
                            } else {
                                message.copy(
                                    messageId = message.messageId,
                                    hintId = message.hintId,
                                    fromUser = message.fromUser,
                                    toUser = message.toUser,
                                    byteArray =
                                    AppKey.decryptByteArray(
                                        message.content,
                                        message.iv,
                                    ),
                                    time = message.time,
                                    content = "",
                                    isSendByUser = isSendByUser,
                                )
                            }
                        }
                            ?.filter { messageResponse ->
                                messageResponse.content != null || messageResponse.byteArray?.isNotEmpty() == true
                            }
                    }
                    .flowOn(Dispatchers.IO)
                    .collect {
                        _stateFlow.value = StateUi.TotalMessage(it)
                    }
            }
        }
    }

    fun setupSocket() {
        SocketIoManage.mSocket?.on("chat:${hintResponse?.id}") { args ->
            val receivedData =
                Gson().fromJson(args[0].toString(), MessageResponse::class.java)
            onSendMessage(receivedData)
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            val iv = generateRandomIV()
            AppKey.encryptFlow(content, iv).map {
                val toUserId =
                    if (user?.id != hintResponse?.user1?.id) hintResponse?.user1?.id else hintResponse?.user2?.id
                val messageRequest =
                    MessageRequest(
                        hintResponse?.id ?: 0,
                        user?.id ?: 0,
                        toUserId ?: 0,
                        it,
                        iv.byteArrayToString(),
                    )
                return@map JSONObject(
                    GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create()
                        .toJson(messageRequest),
                )
            }.flowOn(Dispatchers.Default)
                .collect {
                    SocketIoManage.mSocket?.emit("chat:${hintResponse?.id}", it)
                }
        }
    }

    fun sendImage(bitmap: Bitmap) {
        viewModelScope.launch {
            val iv = generateRandomIV()
            val byteArrayMessage = optimizeAndConvertImageToByteArray(bitmap) ?: return@launch
            AppKey.encryptByteArrFlow(byteArrayMessage, iv).map {
                val toUserId =
                    if (user?.id != hintResponse?.user1?.id) hintResponse?.user1?.id else hintResponse?.user2?.id
                val messageRequest =
                    MessageRequest(
                        hintResponse?.id ?: 0,
                        user?.id ?: 0,
                        toUserId ?: 0,
                        it,
                        iv.byteArrayToString(),
                        type = 1,
                    )
                return@map JSONObject(
                    GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create()
                        .toJson(messageRequest),
                )
            }.flowOn(Dispatchers.Default)
                .collect {
                    SocketIoManage.mSocket?.emit("chat:${hintResponse?.id}", it)
                }
        }
    }

    fun sendVideo(uri: Uri) {
        viewModelScope.launch {
            val iv = generateRandomIV()
            val byteArrayMessage =
                convertVideoToByteArray(App.app.applicationContext, uri) ?: return@launch
            AppKey.encryptByteArrFlow(byteArrayMessage, iv).map {
                val toUserId =
                    if (user?.id != hintResponse?.user1?.id) hintResponse?.user1?.id else hintResponse?.user2?.id
                val messageRequest =
                    MessageRequest(
                        hintResponse?.id ?: 0,
                        user?.id ?: 0,
                        toUserId ?: 0,
                        it,
                        iv.byteArrayToString(),
                        type = 2,
                    )
                return@map JSONObject(
                    GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create()
                        .toJson(messageRequest),
                )
            }.flowOn(Dispatchers.Default)
                .collect {
                    SocketIoManage.mSocket?.emit("chat:${hintResponse?.id}", it)
                }
        }
    }

    private fun onSendMessage(messageResponse: MessageResponse) {
        when (messageResponse.type) {
            0 -> decryptTextMessage(messageResponse)
            else -> decryptMediaMessage(messageResponse)
        }
    }

    private fun decryptTextMessage(receivedData: MessageResponse) {
        viewModelScope.launch {
            AppKey.decryptFlow(
                receivedData.content,
                receivedData.iv,
            ).map {
                val messageResponse =
                    receivedData.copy(
                        content = it,
                    )
                messageResponse
            }.flowOn(Dispatchers.Default).collect {
                _stateFlow.value = StateUi.Message(it)
            }
        }
    }

    private fun decryptMediaMessage(receivedData: MessageResponse) {
        viewModelScope.launch {
            AppKey.decryptByteArrFlow(
                receivedData.content ?: "",
                receivedData.iv,
            ).map {
                val messageResponse =
                    receivedData.copy(
                        byteArray = it,
                        content = "",
                    )
                messageResponse
            }.flowOn(Dispatchers.Default).collect {
                _stateFlow.value = StateUi.Message(it)
            }
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
        SocketIoManage.mSocket?.off()
        super.onCleared()
    }
}
