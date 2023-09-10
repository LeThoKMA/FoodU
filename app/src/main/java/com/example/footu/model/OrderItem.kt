package com.example.footu.model

import com.google.gson.annotations.SerializedName

data class OrderItem(
    @SerializedName("id")
    var id: Int? = 0,
    @SerializedName("status")
    var status: Int? = 0,
    @SerializedName("time")
    var time: String? = "",
    @SerializedName("totalPrice")
    var totalPrice: Int? = 0,
)
