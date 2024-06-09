package com.example.footu.Request

data class RegisterRequest(
    val username: String,
    val password: String,
    val fullname: String,
    val phone: String,
    val publicKey: String? = null,
)
