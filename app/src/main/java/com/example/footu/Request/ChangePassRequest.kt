package com.example.footu.Request

data class ChangePassRequest(
    val oldPassword: String,
    val newPassword: String,
)
