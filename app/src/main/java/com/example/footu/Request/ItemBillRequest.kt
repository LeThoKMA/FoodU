package com.example.footu.Request

import com.example.footu.ItemSize
import com.google.gson.annotations.SerializedName

data class ItemBillRequest(
    @SerializedName("productId")
    val id: Int? = 0,
    @SerializedName("quantity")
    val count: Int? = 0,
    @SerializedName("price")
    val totalPrice: Int? = 0,
    val size: Int? = ItemSize.M.ordinal,
    val description: String? = "",
)
