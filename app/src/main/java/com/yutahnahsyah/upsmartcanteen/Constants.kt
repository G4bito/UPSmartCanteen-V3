package com.yutahnahsyah.upsmartcanteen

object Constants {
  private const val IP_ADDRESS = "192.168.18.41"
  private const val PORT = "3000"

  const val BASE_URL = "http://$IP_ADDRESS:$PORT/"

  fun getFullImageUrl(imagePath: String?): String? {
    if (imagePath.isNullOrEmpty()) return null
    val cleanPath = imagePath.trim().removePrefix("/")
    return "$BASE_URL$cleanPath"
  }
}
