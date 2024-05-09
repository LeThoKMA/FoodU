package com.example.footu.utils

import java.security.SecureRandom

fun generateRandomIV(): ByteArray {
    val secureRandom = SecureRandom()
    val iv = ByteArray(12) // 96 bits IV for GCM
    secureRandom.nextBytes(iv)
    return iv
}