package com.yutahnahsyah.upsmartcanteen.data.model

data class StallCart(
    val stallId: Int,
    val stallName: String,
    val items: List<FoodItem>,
    val subtotal: Double
)
