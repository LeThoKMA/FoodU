package com.example.footu.model

import java.security.PublicKey

data class LoginRequest(
    val username: String,
    val password: String,
    val publicKey: String
)
