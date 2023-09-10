package com.example.footu.model

import com.google.gson.annotations.SerializedName

data class Item(
    @SerializedName("id")
    var id: Int? = 0,
    @SerializedName("name")
    var name: String? = "",
    @SerializedName("unitPrice")
    var price: Int? = 0,
    @SerializedName("quantity")
    var amount: Int? = 0,
    @SerializedName("imageLinks")
    var imgUrl: List<String>? = listOf(),
) : java.io.Serializable
