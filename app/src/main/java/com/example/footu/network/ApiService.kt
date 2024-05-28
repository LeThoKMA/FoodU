package com.example.footu.network

import com.example.footu.Request.ChangePassRequest
import com.example.footu.Request.ConfirmBillRequest
import com.example.footu.Request.HintRequest
import com.example.footu.Request.ItemBillRequest
import com.example.footu.Request.RegisterRequest
import com.example.footu.Request.UserOrderRequest
import com.example.footu.Response.BaseResponse
import com.example.footu.Response.BaseResponseNoBody
import com.example.footu.Response.BillDetailResponse
import com.example.footu.Response.BillResponse
import com.example.footu.Response.CategoryResponse
import com.example.footu.Response.HintMessageResponse
import com.example.footu.Response.HintResponse
import com.example.footu.Response.LoginResponse
import com.example.footu.Response.PointResponse
import com.example.footu.Response.TotalMessageResponse
import com.example.footu.model.Item
import com.example.footu.model.ItemStatistic
import com.example.footu.model.LoginRequest
import com.example.footu.model.OrderItem
import com.example.footu.model.OrderShipModel
import com.example.footu.model.PromotionUser
import com.example.footu.model.RegisterFirebaseModel
import com.example.footu.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/login")
    suspend fun login(
        @Body user: LoginRequest,
    ): BaseResponse<LoginResponse>

    @GET("home/all")
    suspend fun getItems(): BaseResponse<List<Item>>

    @POST("bill")
    suspend fun makeBill(
        @Body request: List<ItemBillRequest>,
    ): BaseResponse<BillResponse>

    @GET("user")
    suspend fun fetchUserInfo(): BaseResponse<User>

    @POST("bill/status")
    suspend fun confirmBill(
        @Body request: ConfirmBillRequest,
    ): BaseResponseNoBody

    @GET("bill/all")
    suspend fun getOrderList(
        @Query("page") page: Int,
    ): BaseResponse<List<OrderItem>?>

    @GET("bill/detail/{id}")
    suspend fun getOrderDetail(
        @Path("id") id: Int,
    ): BaseResponse<BillDetailResponse>

    @GET("statistic/year")
    suspend fun getYearToStatistic(): BaseResponse<List<Int>>

    @GET("statistic/year/{year}")
    suspend fun getYearToStatistic(
        @Path("year") year: Int,
    ): BaseResponse<List<ItemStatistic>>

    @GET("statistic/today")
    suspend fun getStatisticInToday(): BaseResponse<ItemStatistic>

    @GET("banner")
    suspend fun getBannerList(): BaseResponse<List<String>>

    @GET("promotion")
    suspend fun getPromotions(): BaseResponse<List<PromotionUser>>

    @POST("pending-prepaid")
    suspend fun doOrderPending(
        @Body request: UserOrderRequest,
    ): BaseResponse<OrderShipModel>

    @POST("bill/prepaid")
    suspend fun doPayment(
        @Body request: UserOrderRequest,
    ): BaseResponseNoBody

    @GET("logout")
    suspend fun logout(): BaseResponseNoBody

    @PUT("user/password")
    suspend fun changePass(
        @Body request: ChangePassRequest,
    ): BaseResponseNoBody

    @POST("register")
    suspend fun register(
        @Body request: RegisterRequest,
    ): BaseResponse<LoginResponse>

    @GET("pending-prepaid/all")
    suspend fun getOrderShipList(): BaseResponse<List<OrderShipModel>>

    @PUT("pending-prepaid/receive/{id}")
    suspend fun acceptOrder(
        @Path("id") id: Int,
    ): BaseResponseNoBody

    @PUT("pending-prepaid/complete/{id}")
    suspend fun doneOrder(
        @Path("id") id: Int,
    ): BaseResponseNoBody

    @GET("pending-prepaid/received")
    suspend fun getOrdersPicked(): BaseResponse<List<OrderShipModel>>

    @POST("device")
    suspend fun registerFirebase(
        @Body request: RegisterFirebaseModel,
    ): BaseResponseNoBody

    @POST("pending-prepaid/payment/{id}")
    suspend fun doPaymentForOrderShip(
        @Path("id") id: Int,
    ): BaseResponseNoBody

    @GET("category")
    suspend fun getCategory(): BaseResponse<List<CategoryResponse>>

    @GET("home/{id}")
    suspend fun getProductByType(
        @Path("id") id: Int,
    ): BaseResponse<List<Item>>

    @GET("pending-prepaid/customer/{id}")
    suspend fun getOrdersDetail(
        @Path("id") id: Int,
    ): BaseResponse<List<OrderShipModel>>

    @POST("message/hint")
    suspend fun checkHintId(
        @Body request: HintRequest,
    ): BaseResponse<HintResponse>

    @GET("message/{id}")
    suspend fun fetchMessage(
        @Path("id") id: Int,
        @Query("page") page: Int,
    ): BaseResponse<TotalMessageResponse>

    @GET("message")
    suspend fun getAllHintMessage(
        @Query("id") id: Int,
    ): BaseResponse<List<HintMessageResponse>>

    @GET("pending-prepaid/routers")
    suspend fun getRouters(): BaseResponse<List<PointResponse>>

    @GET("pending-prepaid/detail")
    suspend fun getDetailPendingBill(
        @Query("id") id: Int,
    ): BaseResponse<OrderShipModel>
}
