package com.example.footu.Request

data class MessageRequest(
    val hintId: Int,
    val fromUserId: Int,
    val toUserId: Int,
    val content: String,
    val iv: String,
    val type:Int = 0
)