package com.example.footu.model

import android.os.Parcelable
import com.example.footu.ItemSize
import kotlinx.parcelize.Parcelize

@Parcelize
data class DetailItemChoose(
    var id: Int = 0,
    var name: String = "",
    var count: Int = 1,
    var price: Int = 0,
    var size: ItemSize? = ItemSize.M,
    var imgUrl: List<String> = listOf(),
    var flag: Boolean = false,
    var textDescription: String = "",
) : Parcelable {
    val totalPrice: Int get() = (price * count * ((size ?: ItemSize.M).value)).toInt()
    val priceForSize: Int get() = (price * (size ?: ItemSize.M).value).toInt()
}
