package com.yutahnahsyah.upsmartcanteen.data.model

data class Notification(
  val id: String,
  val title: String,
  val message: String,
  val is_read: Boolean,
  val created_at: String?
)