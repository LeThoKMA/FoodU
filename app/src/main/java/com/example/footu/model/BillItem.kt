package com.example.footu.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BillItem(
    @SerializedName("product")
    val item: Item? = Item(),
    @SerializedName("quantity")
    val quantity: Int? = 0,
    @SerializedName("price")
    val price: Int? = 0,
    val size: Int? = 0,
    val description: String? = "",
) : Parcelable
