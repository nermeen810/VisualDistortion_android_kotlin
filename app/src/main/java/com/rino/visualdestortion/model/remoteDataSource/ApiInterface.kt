package com.rino.visualdestortion.model.remoteDataSource

import com.rino.visualdestortion.model.pojo.addService.AddServiceResponse
import com.rino.visualdestortion.model.pojo.addService.FormData
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
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiInterface {

    suspend fun login(loginRequest: LoginRequest?): Response<LoginResponse>

    suspend fun refreshToken(refreshTokenRequest: RefreshTokenRequest): Response<LoginResponse>

    suspend fun requestOTP(requestOTP: RequestOTP): Response<ResponseOTP>

    suspend fun resetPassword(resetpasswordRequest: ResetPasswordRequest): Response<ResponseOTP>

    suspend fun setServiceForm(auth: String, serviceForm: FormData): Response<QRCode?>?

    suspend fun getServiceForm(auth: String): Response<AddServiceResponse>

    suspend fun getHomeData(auth: String): Response<HomeServicesResponse>

    suspend fun getHistoryData(auth: String): Response<AllHistoryResponse>

    suspend fun getFilteredHistory(
        auth: String,
        serviceTypeId: Int,
        period: String
    ): Response<FilteredHistoryResponse>

    suspend fun getHistoryDataByService(
        auth: String,
        serviceTypeId: Int,
        period: String,
        pageNumber: Int
    ): Response<HistoryByServiceIdResponse>

    suspend fun searchHistoryDataByService(
        auth: String,
        searchRequest: SearchRequest
    ): Response<SearchResponse>

    suspend fun isDailyPrepared(auth: String): Response<CheckDailyPreparationResponse>

    suspend fun getCreateDailyPreparation(auth: String): Response<GetDailyPraprationData>

    suspend fun setDailyPreparation(
        auth: String, WorkersTypesList: Map<Long, Int>, equipmentList: Map<Long, Int>
    ): Response<Void>

    suspend fun getDailyPreparation(auth: String): Response<TodayDailyPrapration>


    suspend fun editDailyPreparation(
        auth: String, WorkersTypesList: Map<Long, Int>, equipmentList: Map<Long, Int>
    ): Response<Void>

}
