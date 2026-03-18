package com.yutahnahsyah.upsmartcanteenfrontend

import com.google.gson.annotations.SerializedName

data class FoodItem(
    @SerializedName("item_id") val item_id: Int,
    @SerializedName("stall_id") val stall_id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("image_url") val image_url: String?,
    @SerializedName("is_available") val is_available: Boolean
)