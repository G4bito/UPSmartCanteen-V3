package com.yutahnahsyah.upsmartcanteen.data.model

import com.google.gson.annotations.SerializedName

data class Stall(
    @SerializedName("stall_id")
    val stall_id: Int,

    @SerializedName("stall_name")
    val stall_name: String,

    @SerializedName("location")
    val location: String,

    @SerializedName("stall_image_url")
    val stall_image_url: String?,

    @SerializedName("is_active")
    val is_active: Boolean
)
