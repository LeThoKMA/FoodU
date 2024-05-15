package com.example.footu.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.security.PublicKey

@Parcelize
data class User(
    @SerializedName("fullname")
    var fullname: String? = "",
    @SerializedName("id")
    var id: Int = 0,
    @SerializedName("role")
    var role: Int? = 0,
    @SerializedName("username")
    var username: String? = "",
    var email: String? = "",
    var phone: String? = "",
    val publicKey: String? = ""
) :  Parcelable
