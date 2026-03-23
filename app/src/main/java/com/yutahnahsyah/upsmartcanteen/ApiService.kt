package com.yutahnahsyah.upsmartcanteen

import com.yutahnahsyah.upsmartcanteen.data.model.Notification
import com.yutahnahsyah.upsmartcanteen.data.model.Stall
import com.yutahnahsyah.upsmartcanteen.data.model.FoodItem
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

data class ForgotPasswordRequest(val email: String)
data class VerifyOtpRequest(val email: String, val otp: String)
data class ResetPasswordRequest(val resetToken: String, val newPassword: String)
data class MessageResponse(val message: String)
data class ResetTokenResponse(val resetToken: String)
data class FcmTokenRequest(val fcm_token: String)

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

data class PlaceOrderResponse(
  val message: String,
  val order: OrderResponse? = null
)

data class OrderResponse(
  val order_id: Int,
  val status: String,
  val total_price: Double,
  val order_time: String? = null,
  val stall_name_snapshot: String? = null,
  val completed_at: String? = null,
  val cancelled_at: String? = null
)

data class UpdateCartItemRequest(val quantity: Int)

data class CartValidationResponse(
  val valid: Boolean,
  val unavailableItems: List<Int>,
  val stallClosed: Boolean,
  val stallInactive: Boolean
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
  ): Response<PlaceOrderResponse>

  @GET("api/myOrders")
  suspend fun getMyOrders(
    @Header("Authorization") token: String
  ): Response<List<OrderResponse>>

  @POST("api/forgotPassword")
  suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<MessageResponse>

  @POST("api/verifyOtp")
  suspend fun verifyOtp(@Body body: VerifyOtpRequest): Response<ResetTokenResponse>

  @POST("api/resetPassword")
  suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<MessageResponse>

  @POST("api/saveFcmToken")
  suspend fun saveFcmToken(
    @Header("Authorization") token: String,
    @Body body: FcmTokenRequest
  ): Response<MessageResponse>

  @DELETE("api/removeFromCart/{cartItemId}")
  suspend fun removeCartItem(
    @Header("Authorization") token: String,
    @Path("cartItemId") cartItemId: Int
  ): Response<CartResponse>

  @PUT("api/updateCartItem/{cartItemId}")
  suspend fun updateCartItem(
    @Header("Authorization") token: String,
    @Path("cartItemId") cartItemId: Int,
    @Body request: UpdateCartItemRequest
  ): Response<CartResponse>

  @GET("api/validateCart")
  suspend fun validateCart(
    @Header("Authorization") token: String
  ): Response<CartValidationResponse>

  // ✅ NEW — fetch notifications from DB
  @GET("api/notifications")
  suspend fun getNotifications(
    @Header("Authorization") token: String
  ): Response<List<Notification>>
}