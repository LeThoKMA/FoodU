package com.example.footu.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
@Parcelize
data class PromotionModel(
    @SerializedName("applyingPrice")
    var applyingPrice: Int? = 0,
    @SerializedName("id")
    var id: Int? = 0,
    @SerializedName("percentage")
    var percentage: Int = 0,
) : Parcelable
