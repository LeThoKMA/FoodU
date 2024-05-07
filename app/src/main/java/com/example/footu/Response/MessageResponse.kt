package com.example.footu.Response

import com.example.footu.MyPreference
import com.example.footu.model.User

data class MessageResponse(
    val messageId: Long,
    val hintId: Long,
    val fromUser: User,
    val toUser: User,
    val content: String,
    val iv: String,
    val time: String,
    val type: Int = 0,
) {
    val isSendByUser get() = fromUser.id == MyPreference.getInstance()?.getUser()?.id
}
