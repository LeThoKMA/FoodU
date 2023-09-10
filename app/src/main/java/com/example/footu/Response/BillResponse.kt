package com.example.footu.Response

import com.example.footu.model.PromotionModel

data class BillResponse(
    val id: Int? = 0,
    val totalPrice: Int? = 0,
    val givenPromotion: List<PromotionModel>? = emptyList(),
) {
    val promotion: Int
        get() {
            if (givenPromotion?.isNotEmpty() == true) {
                return givenPromotion.maxOf { it1 -> it1.percentage }
            } else {
                return 0
            }
        }
}
