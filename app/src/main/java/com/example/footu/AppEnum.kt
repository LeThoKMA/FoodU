package com.example.footu

enum class OrderStatus(val value: Int) {
    PENDING(1),
    PAID(2),
    CANCELLED(3),
}

enum class ItemSize(val value: Double) {
    S(0.75),
    M(1.0),
    L(1.5),
}

enum class TypeItem {
    COFFEE,
    CAKE,
    FREEZE,
    TEA,
}
