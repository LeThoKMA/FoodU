package com.example.footu.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("fullname")
    var fullname: String? = "",
    @SerializedName("id")
    var id: Int? = 0,
    @SerializedName("role")
    var role: Boolean? = false,
    @SerializedName("username")
    var username: String? = "",
) : java.io.Serializable
