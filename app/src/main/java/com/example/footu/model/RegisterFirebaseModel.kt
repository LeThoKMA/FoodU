package com.example.footu.model

import com.google.gson.annotations.SerializedName

data class RegisterFirebaseModel(
    @SerializedName("firebaseToken")
    val firebaseToken: String,
    @SerializedName("deviceKey")
    val deviceKey: String?,
)
