package com.yutahnahsyah.upsmartcanteen.data.model

import com.google.gson.annotations.SerializedName

data class FoodItem(
  @SerializedName("cart_item_id")
  val cart_item_id: Int = 0,  // ← add this

  @SerializedName("item_id")
  val item_id: Int,

  @SerializedName("stall_id")
  val stall_id: Int,

  @SerializedName("stall_name")
  val stall_name: String?,

  @SerializedName("item_name")
  val name: String,

  @SerializedName("description")
  val description: String?,

  @SerializedName("price")
  var price: Double,

  @SerializedName("category")
  val category: String,

  @SerializedName("stock_qty")
  var stock_qty: Int,

  @SerializedName("item_image_url")
  val image_url: String?,

  @SerializedName("is_available")
  val is_available: Boolean,

  @SerializedName("quantity")
  var quantity: Int = 1
)