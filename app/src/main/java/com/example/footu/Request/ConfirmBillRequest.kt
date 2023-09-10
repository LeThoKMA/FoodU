package com.example.footu.Request

data class ConfirmBillRequest(
    val id: Int,
    val status: Int,
    val newTotalPrice: Int,
)
