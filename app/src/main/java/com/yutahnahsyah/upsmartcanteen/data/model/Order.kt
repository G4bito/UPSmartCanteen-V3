package com.yutahnahsyah.upsmartcanteen.data.model

data class Order(
  val orderId: String,
  val date: String,
  val status: String,
  val totalPrice: Double,
  val items: String
)
