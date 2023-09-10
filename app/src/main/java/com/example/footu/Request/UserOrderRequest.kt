package com.example.footu.Request

import com.google.gson.annotations.SerializedName

data class UserOrderRequest(
    @SerializedName("billItemList")
    var billItemList: List<ItemBillRequest> = listOf(),
    @SerializedName("promotionId")
    var promotionList: Int? = null,
    @SerializedName("totalPrice")
    var totalPrice: Int = 0,
)
