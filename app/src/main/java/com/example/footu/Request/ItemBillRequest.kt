package com.example.footu.Request

import com.google.gson.annotations.SerializedName

data class ItemBillRequest(
    @SerializedName("productId")
    val id: Int? = 0,
    @SerializedName("quantity")
    val count: Int? = 0,
    @SerializedName("price")
    val totalPrice: Int? = 0,
)
