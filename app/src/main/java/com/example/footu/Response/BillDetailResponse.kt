package com.example.footu.Response

import android.os.Parcelable
import com.example.footu.model.BillItem
import com.example.footu.model.PromotionModel
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BillDetailResponse(
    @SerializedName("billItemList")
    var billItemList: List<BillItem>? = listOf(),
    @SerializedName("id")
    var id: Int? = 0,
    @SerializedName("status")
    var status: Int? = 0,
    @SerializedName("time")
    var time: String? = "",
    @SerializedName("totalPrice")
    var totalPrice: Int? = 0,
    @SerializedName("usedPromotion")
    var usedPromotion: PromotionModel? = PromotionModel(),
) : Parcelable
