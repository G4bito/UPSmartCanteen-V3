package com.yutahnahsyah.upsmartcanteenfrontend.data.model

data class StallCart(
    val stallName: String,
    val items: List<FoodItem>,
    val subtotal: Double
)
