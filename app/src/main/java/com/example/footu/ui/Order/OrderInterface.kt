package com.example.footu.ui.Order

interface OrderInterface {
    fun detailItem(position: Int)

    fun plusItem(position: Int)
    fun subtractItem(position: Int)

    fun editCount(position: Int)

    fun selectItem(flag: Boolean, position: Int)
}
