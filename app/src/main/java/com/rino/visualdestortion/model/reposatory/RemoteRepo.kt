package com.rino.visualdestortion.model.reposatory

import com.rino.visualdestortion.model.pojo.addService.AddServiceResponse
import com.rino.visualdestortion.model.pojo.addService.FormData
import com.rino.visualdestortion.model.pojo.addService.QRCode
import com.rino.visualdestortion.model.pojo.home.HomeServicesResponse
import com.rino.visualdestortion.model.pojo.login.LoginRequest
import com.rino.visualdestortion.model.pojo.login.LoginResponse
import com.rino.visualdestortion.model.pojo.login.RefreshTokenRequest
import com.rino.visualdestortion.model.pojo.resetPassword.RequestOTP
import com.rino.visualdestortion.model.pojo.resetPassword.ResetPasswordRequest
import com.rino.visualdestortion.model.pojo.resetPassword.ResponseOTP
import com.rino.visualdestortion.model.remoteDataSource.Result
import retrofit2.Response

interface RemoteRepo {

      suspend fun login(loginRequest: LoginRequest?): Result<LoginResponse?>

      suspend fun refreshToken(refreshTokenRequest: RefreshTokenRequest?): Result<LoginResponse?>

      suspend fun requestOTP(requestOTP: RequestOTP): Result<ResponseOTP?>

      suspend fun resetPassword(resetPasswrdRequest: ResetPasswordRequest): Result<ResponseOTP?>

      suspend fun setServiceForm(serviceForm: FormData): Result<QRCode?>

      suspend fun getServiceForm(): Result<AddServiceResponse?>

      suspend fun getHomeData(): Result<HomeServicesResponse?>

}