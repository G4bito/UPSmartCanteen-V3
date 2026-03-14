package com.yutahnahsyah.upsmartcanteenfrontend

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.Part

interface ApiService {
  @POST("api/registerUser")
  suspend fun registerUser(@Body request: RegisterRequest): Response<AuthResponse>

  @POST("api/loginUser")
  suspend fun loginUser(@Body request: UserRequest): Response<AuthResponse>

  @GET("api/getUser")
  suspend fun getUserProfile(
    @Header("Authorization") token: String
  ): Response<UserData>

  @PUT("api/editUser")
  suspend fun editUserProfile(
    @Header("Authorization") token: String,
    @Body request: RegisterRequest
  ): Response<AuthResponse>

  @Multipart
  @POST("api/uploadProfilePic")
  suspend fun uploadProfilePicture(
    @Header("Authorization") token: String,
    @Part image: MultipartBody.Part
  ): Response<AuthResponse>
}