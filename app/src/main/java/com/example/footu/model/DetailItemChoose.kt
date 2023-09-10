package com.example.footu.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DetailItemChoose(
    var id: Int? = 0,
    var name: String? = "",
    var count: Int? = 0,
    var totalPrice: Int? = 0,
    var price: Int? = 0,
    var imgUrl: List<String>? = listOf(),
    var flag: Boolean? = false,
) : Parcelable
