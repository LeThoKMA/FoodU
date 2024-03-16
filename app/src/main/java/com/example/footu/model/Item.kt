package com.example.footu.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("name")
    val name: String? = "",
    @SerializedName("unitPrice")
    val price: Int? = 0,
    @SerializedName("quantity")
    val amount: Int? = 0,
    @SerializedName("imageLinks")
    val imgUrl: List<String>? = listOf(),
    val description: String? = "",
) : java.io.Serializable, Parcelable
