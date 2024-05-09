package com.example.footu.Response

data class TotalMessageResponse(
    val totalPage: Int,
    val messageList: List<MessageResponse> = emptyList()
)