package com.example.footu.ui.Order

import com.example.footu.model.DetailItemChoose
import com.example.footu.model.Item

interface OrderInterface {
    fun addItemToBill(item: DetailItemChoose)
    fun detailItem(item: Item)
}
