package com.rino.visualdestortion.model.remoteDataSource


import com.rino.visualdestortion.model.pojo.addService.AddServiceResponse
import com.rino.visualdestortion.model.pojo.addService.QRCode
import com.rino.visualdestortion.model.pojo.dailyPraperation.CheckDailyPreparationResponse
import com.rino.visualdestortion.model.pojo.dailyPraperation.GetDailyPraprationData
import com.rino.visualdestortion.model.pojo.dailyPraperation.TodayDailyPrapration
import com.rino.visualdestortion.model.pojo.history.*
import com.rino.visualdestortion.model.pojo.home.HomeServicesResponse
import com.rino.visualdestortion.model.pojo.login.LoginRequest
import com.rino.visualdestortion.model.pojo.login.LoginResponse
import com.rino.visualdestortion.model.pojo.login.RefreshTokenRequest
import com.rino.visualdestortion.model.pojo.resetPassword.RequestOTP
import com.rino.visualdestortion.model.pojo.resetPassword.ResetPasswordRequest
import com.rino.visualdestortion.model.pojo.resetPassword.ResponseOTP
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*


interface ApiService {
    @POST("api/auth/token")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("api/auth/refreshToken")
    suspend fun refreshToken(@Body refreshTokenRequest: RefreshTokenRequest): Response<LoginResponse>

    @POST("api/auth/resetPassword")
    suspend fun requestOTP(@Body requestOTP: RequestOTP): Response<ResponseOTP>

    @POST("api/auth/confirmResetPW")
    suspend fun resetPassword(@Body resetPasswordRequest: ResetPasswordRequest): Response<ResponseOTP>

    @Multipart
    @POST("api/form/storeForm")
    suspend fun setServiceForm(
        @Header("Authorization") auth: String,
        @Part("serviceTypeId") serviceTypeId: RequestBody,
        @Part("sectorName") sectorName: RequestBody,
        @Part("municipalityName") municipalityName: RequestBody,
        @Part("districtName") districtName: RequestBody,
        @Part("streetName") streetName: RequestBody,
        @Part("lat") lat: RequestBody,
        @Part("lng") lng: RequestBody,
        @Part beforeImg: MultipartBody.Part,
        @Part duringImg: MultipartBody.Part?,
        @Part afterImg: MultipartBody.Part,
        @Part("mSquare") mSquare: Int?,
        @Part("mCube") mCube: Float?,
        @Part("numberR") numberR: Int?,
        @Part("notes") notes: RequestBody?

    ): Response<QRCode?>

//                               ,@PartMap WorkersTypesList : HashMap<String, RequestBody>
//                               ,@PartMap equipmentList : HashMap<String, RequestBody>
//                               ,@Part("Percentage"      ) percentage: RequestBody ?

    @GET("api/form/createFrom")
    suspend fun getServiceForm(@Header("Authorization") auth: String): Response<AddServiceResponse>

    @GET("api/home/HomeApi")
    suspend fun getHomeData(@Header("Authorization") auth: String): Response<HomeServicesResponse>

    @GET("api/History/GetServiceTypeHistory")
    suspend fun getHistoryData(@Header("Authorization") auth: String): Response<AllHistoryResponse>

    @GET("api/History/serviceTypeHistory/{serviceTypeId}")
    suspend fun getFilteredHistory(
        @Header("Authorization") auth: String,
        @Path("serviceTypeId") serviceTypeId: Int,
        @Query("period") period: String
    ): Response<FilteredHistoryResponse>

    @GET("api/History/SeeAllHistory/{serviceTypeId}")
    suspend fun getHistoryDataByService(
        @Header("Authorization") auth: String,
        @Path("serviceTypeId") serviceTypeId: Int,
        @Query("period") period: String,
        @Query("pageNumber") pageNumber: Int
    ): Response<HistoryByServiceIdResponse>

    @POST("api/History/SearchByServiceId")
    suspend fun searchHistoryDataByService(
        @Header("Authorization") auth: String,
        @Body searchRequest: SearchRequest
    ): Response<SearchResponse>


    @GET("api/DailyPreparations/isPrepared")
    suspend fun isDailyPrepared(@Header("Authorization") auth: String): Response<CheckDailyPreparationResponse>


    @Multipart
    @POST("api/DailyPreparations/")
    suspend fun setDailyPreparation(
        @Header("Authorization") auth: String,
        @PartMap WorkersTypesList: HashMap<String, RequestBody>,
        @PartMap equipmentList: HashMap<String, RequestBody>
    ): Response<Void>

    @GET("api/DailyPreparations")
    suspend fun getDailyPreparation(@Header("Authorization") auth: String): Response<TodayDailyPrapration>

    @GET("api/DailyPreparations/DailyPrepData")
    suspend fun getCreateDailyPreparation(@Header("Authorization") auth: String): Response<GetDailyPraprationData>


    @Multipart
    @PUT("api/DailyPreparations/")
    suspend fun editDailyPreparation(
        @Header("Authorization") auth: String,
        @PartMap WorkersTypesList: HashMap<String, RequestBody>,
        @PartMap equipmentList: HashMap<String, RequestBody>
    ): Response<Void>
}