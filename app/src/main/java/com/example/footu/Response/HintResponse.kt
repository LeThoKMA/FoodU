package com.example.footu.Response

import com.example.footu.model.User

data class HintResponse(
    val id: Int,
    val user1: User,
    val user2: User
)
