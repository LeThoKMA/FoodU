package com.example.footu.Response

import com.google.gson.annotations.SerializedName

data class BaseResponseNoBody(
    @SerializedName("message")
    val message: String? = "",
)
