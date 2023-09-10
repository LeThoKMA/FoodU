package com.example.footu.network

import com.example.footu.Request.ChangePassRequest
import com.example.footu.Request.ConfirmBillRequest
import com.example.footu.Request.ItemBillRequest
import com.example.footu.Request.RegisterRequest
import com.example.footu.Request.UserOrderRequest
import com.example.footu.Response.BaseResponse
import com.example.footu.Response.BaseResponseNoBody
import com.example.footu.Response.BillDetailResponse
import com.example.footu.Response.BillResponse
import com.example.footu.Response.LoginResponse
import com.example.footu.model.Item
import com.example.footu.model.ItemStatistic
import com.example.footu.model.LoginRequest
import com.example.footu.model.OrderItem
import com.example.footu.model.PromotionUser
import com.example.footu.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("entries")
    suspend fun test(): String

    @POST("login")
    suspend fun login(@Body user: LoginRequest): BaseResponse<LoginResponse>

    @GET("home/all")
    suspend fun getItems(): BaseResponse<List<Item>>

    @POST("bill")
    suspend fun makeBill(@Body request: List<ItemBillRequest>): BaseResponse<BillResponse>

    @GET("user")
    suspend fun fetchUserInfo(): BaseResponse<User>

    @POST("bill/status")
    suspend fun confirmBill(@Body request: ConfirmBillRequest): BaseResponseNoBody

    @GET("bill/all")
    suspend fun getOrderList(@Query("page") page: Int): BaseResponse<List<OrderItem>?>

    @GET("bill/detail/{id}")
    suspend fun getOrderDetail(@Path("id") id: Int): BaseResponse<BillDetailResponse>

    @GET("statistic/year")
    suspend fun getYearToStatistic(): BaseResponse<List<Int>>

    @GET("statistic/year/{year}")
    suspend fun getYearToStatistic(@Path("year") year: Int): BaseResponse<List<ItemStatistic>>

    @GET("statistic/today")
    suspend fun getStatisticInToday(): BaseResponse<ItemStatistic>

    @GET("banner")
    suspend fun getBannerList(): BaseResponse<List<String>>

    @GET("promotion")
    suspend fun getPromotions(): BaseResponse<List<PromotionUser>>

    @POST("payment")
    suspend fun doPayment(@Body request: UserOrderRequest): BaseResponseNoBody

    @GET("logout")
    suspend fun logout(): BaseResponseNoBody

    @PUT("user/password")
    suspend fun changePass(@Body request: ChangePassRequest): BaseResponseNoBody

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): BaseResponse<LoginResponse>
}
