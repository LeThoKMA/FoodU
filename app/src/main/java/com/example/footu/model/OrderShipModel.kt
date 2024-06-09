package com.example.footu.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderShipModel(
    val billItemList: List<BillItem>,
    val customer: User?,
    val shipper: User?,
    val id: Int,
    val time: String,
    val totalPrice: Int,
    val address: String,
    val lat: Double?,
    val longitude: Double?,
) : Parcelable {
    val isPicked get() = shipper != null
}
