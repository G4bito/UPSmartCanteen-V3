package com.yutahnahsyah.upsmartcanteen

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
  private val logging = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
  }

  private val httpClient = OkHttpClient.Builder()
    .addInterceptor(logging)
    .build()

  val instance: ApiService by lazy {
    val retrofit = Retrofit.Builder()
      .baseUrl(Constants.BASE_URL)
      .client(httpClient)
      .addConverterFactory(GsonConverterFactory.create())
      .build()
    retrofit.create(ApiService::class.java)
  }
}