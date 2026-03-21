package com.yutahnahsyah.upsmartcanteen

import com.yutahnahsyah.upsmartcanteen.data.model.Stall
import com.yutahnahsyah.upsmartcanteen.data.model.FoodItem
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

data class PlaceOrderRequest(
    val stall_id: Int,
    val payment_type: String,
    val order_remarks: String? = ""
)

data class AddToCartRequest(
    val item_id: Int,
    val quantity: Int
)

data class CartResponse(
    val message: String
)

data class OrderResponse(
    val order_id: Int,
    val status: String,
    val total_price: Double,
    val order_date: String,
    val stall_name_snapshot: String? = null
)

interface ApiService {
  @POST("api/registerUser")
  suspend fun registerUser(@Body request: RegisterRequest): Response<AuthResponse>

  @POST("api/loginUser")
  suspend fun loginUser(@Body request: UserRequest): Response<AuthResponse>

  @GET("api/getUser")
  suspend fun getUserProfile(
    @Header("Authorization") token: String
  ): Response<UserData>

  // FIXED: Added missing editUserProfile method
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

  @GET("api/stalls")
  suspend fun getStalls(): Response<List<Stall>>

  @GET("api/stalls/active")
  suspend fun getActiveStalls(): Response<List<Stall>>

  @GET("api/stallMenu/{stallId}")
  suspend fun getFoodsByStall(
    @Path("stallId") stallId: Int
  ): Response<List<FoodItem>>

  @GET("api/allMenuItems")
  suspend fun getAllMenuItems(): Response<List<FoodItem>>

  @GET("api/myCart")
  suspend fun getMyCart(
    @Header("Authorization") token: String
  ): Response<List<FoodItem>>

  @POST("api/addToCart")
  suspend fun addToCart(
    @Header("Authorization") token: String,
    @Body request: AddToCartRequest
  ): Response<CartResponse>

  @DELETE("api/clearStallCart/{stallId}")
  suspend fun clearStallCart(
    @Header("Authorization") token: String,
    @Path("stallId") stallId: Int
  ): Response<CartResponse>

  @POST("api/placeOrder")
  suspend fun placeOrder(
    @Header("Authorization") token: String,
    @Body request: PlaceOrderRequest
  ): Response<CartResponse>

  @GET("api/myOrders")
  suspend fun getMyOrders(
    @Header("Authorization") token: String
  ): Response<List<OrderResponse>>
}
