package com.example.footu.Response

import com.google.gson.annotations.SerializedName

data class PointResponse(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("scaledLatitude")
    val scaledLatitude: Int,
    @SerializedName("scaledLongitude")
    val scaledLongitude: Int,
)
