package com.yutahnahsyah.upsmartcanteen

data class UserRequest(
  val email: String,
  val password: String
)

data class RegisterRequest(
  val employee_id: String,
  val full_name: String,
  val email: String,
  val password: String,
  val department: String
)

data class AuthResponse(
  val message: String?,
  val token: String?,
  val user: UserData?
)

data class UserData(
  val employee_id: String,
  val full_name: String,
  val email: String,
  val department: String,
  val profile_picture_url: String?
)
