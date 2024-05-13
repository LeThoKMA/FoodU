package com.example.footu.Response

import com.example.footu.model.User

data class HintMessageResponse(
    val id: Long,
    val messageResponse: MessageResponse?,
    var otherUser: User? = null
)
