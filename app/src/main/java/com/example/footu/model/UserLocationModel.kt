package com.example.footu.model

data class UserLocationModel(
    val id: Int,
    val name: String? = "",
    val lat: Double? = 0.0,
    val longitude: Double? = 0.0,
)
