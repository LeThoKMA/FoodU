package com.example.footu.model

data class PromotionUser(
    val id: Int,
    val percentage: Int,
    val promotionDetail: String,
    val image: String,
    var isPicked: Boolean = false,
)
