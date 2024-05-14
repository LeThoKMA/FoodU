package com.example.footu.Response

import com.example.footu.model.User
import com.google.gson.annotations.SerializedName

data class HintMessageResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("lastMessage")
    val lastMessage: MessageResponse?,
    @Transient
    var otherUser: User? = null
)
