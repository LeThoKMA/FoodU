package com.example.footu.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderShipModel(
    val billItemList: List<BillItem>,
    val customer: User,
    val id: Int,
    val time: String,
    val totalPrice: Int,
    val address: String
) : Parcelable
