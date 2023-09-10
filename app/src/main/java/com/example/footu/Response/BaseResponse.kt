package com.example.footu.Response

data class BaseResponse<T>(
    val message: String? = "",
    val data: T?
)
