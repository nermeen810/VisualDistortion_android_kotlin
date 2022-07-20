package com.rino.visualdestortion.model.reposatory

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import com.google.gson.Gson
import com.rino.visualdestortion.model.localDataSource.MySharedPreference
import com.rino.visualdestortion.model.localDataSource.PreferenceDataSource
import com.rino.visualdestortion.model.localDataSource.room.DailyPreparation
import com.rino.visualdestortion.model.localDataSource.room.DailyPreparationDataSource
import com.rino.visualdestortion.model.localDataSource.sharedPref.Preference
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
import com.rino.visualdestortion.model.remoteDataSource.ApiDataSource
import com.rino.visualdestortion.model.remoteDataSource.ApiInterface
import com.rino.visualdestortion.model.remoteDataSource.Result
import com.rino.visualdestortion.utils.PREF_FILE_NAME
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import java.net.SocketTimeoutException

class ModelRepo(application: Application) : RemoteRepo, LocalRepo {
    private val apiDataSource: ApiInterface = ApiDataSource()
    private val preference =
        MySharedPreference(
            application.applicationContext.getSharedPreferences(
                PREF_FILE_NAME,
                Context.MODE_PRIVATE
            )
        )
    private val sharedPreference: Preference = PreferenceDataSource(preference)

    private val localDataSource: DailyPreparationDataSource =
        DailyPreparationDataSource(application)

    override suspend fun login(loginRequest: LoginRequest?): Result<LoginResponse?> {
        var result: Result<LoginResponse?> = Result.Loading
        try {
            val response = apiDataSource.login(loginRequest)
            if (response.isSuccessful) {
                result = Result.Success(response.body())
                Log.i("ModelRepository", "Result $result")
            } else {
                Log.i("ModelRepository", "Error${response.errorBody()}")
                when (response.code()) {
                    400 -> {
                        Log.e("Error 400", "Bad Request")
                        result = Result.Error(Exception("خطأ فى البريد الكتروني او كلمة المرور"))
                    }
                    404 -> {
                        Log.e("Error 404", "Not Found")
                        //  result = Result.Error(Exception("Not Found"))
                    }
                    500 -> {
                        Log.e("Error 500", "Server Error")
                        result = Result.Error(Exception("server is down"))
                    }
                    502 -> {
                        Log.e("Error 502", "Time out")
                        result =
                            Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                    }
                    else -> {
                        Log.e("Error", "Generic Error")
                        result = Result.Error(Exception("Error"))
                    }
                }
            }

        } catch (e: IOException) {
            result = Result.Error(e)
            Log.e("ModelRepository", "IOException ${e.message}")
            Log.e("ModelRepository", "IOException ${e.localizedMessage}")

        }
        return result
    }

    override suspend fun refreshToken(refreshTokenRequest: RefreshTokenRequest?): Result<LoginResponse?> {
        var result: Result<LoginResponse?> = Result.Loading

        try {
            val response = apiDataSource.refreshToken(refreshTokenRequest!!)
            if (response.isSuccessful) {
                result = Result.Success(response.body())
                Log.i("ModelRepository", "Result $result")
                setToken(response.body()?.token!!)
                setRefreshToken(response.body()?.refreshToken!!)
            } else {
                Log.i("ModelRepository", "Error${response.message()}")
                when (response.code()) {
                    400 -> {
                        Log.e("Error 400", "Bad Request")
                        result = Result.Error(Exception("Login Required"))
                        logout()
                        Log.i("ModelRepository refresh token:", "Result $result")

                    }
                    404 -> {
                        Log.e("Error 404", "Not Found")
                    }
                    500 -> {
                        Log.e("Error 500", "Server Error")
                        result = Result.Error(Exception("server is down"))
                    }
                    502 -> {
                        Log.e("Error 502", "Time out")
                        result =
                            Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                    }
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }
            }

        } catch (e: IOException) {
            result = Result.Error(e)
            Log.e("ModelRepository", "IOException ${e.message}")
            Log.e("ModelRepository", "IOException ${e.localizedMessage}")

        }
        return result
    }

    override suspend fun requestOTP(requestOTP: RequestOTP): Result<ResponseOTP?> {
        var result: Result<ResponseOTP?> = Result.Loading
        try {
            val response = apiDataSource.requestOTP(requestOTP)
            if (response.isSuccessful) {
                result = Result.Success(response.body())
                Log.i("ModelRepository", "Result $result")
            } else {
                Log.i("ModelRepository", "Error${response.errorBody()?.string()}")
                when (response.code()) {
                    400 -> {
                        Log.e("Error 400", "Bad Request")
                        result = Result.Error(Exception("Couldn't find user"))
                    }
                    404 -> {
                        Log.e("Error 404", "Not Found")
                        result = Result.Error(Exception("Not Found"))
                    }
                    500 -> {
                        Log.e("Error 500", "Server Error")
                        result = Result.Error(Exception("Server is down"))
                    }
                    502 -> {
                        Log.e("Error 502", "Time out")
                        result =
                            Result.Error(Exception("حدث خطأ أثناء الاتصال بالانترنت برجاء المحاولة مرة أخرى"))
                    }
                    else -> {
                        Log.e("Error", "Generic Error")
                        //      result = Result.Error(Exception("Error"))
                    }
                }
            }

        } catch (e: IOException) {
            val message: String
            if (e is SocketTimeoutException) {
                message = "حدث خطأ أثناء الاتصال بالانترنت برجاء المحاولة مرة أخرى."
                result = Result.Error(java.lang.Exception(message))
            } else {
                result = Result.Error(e)
                Log.e("ModelRepository", "IOException ${e.message}")
                Log.e("ModelRepository", "IOException ${e.localizedMessage}")
            }
        }
        return result
    }

    override suspend fun resetPassword(resetPasswrdRequest: ResetPasswordRequest): Result<ResponseOTP?> {
        var result: Result<ResponseOTP?> = Result.Loading
        try {
            val response = apiDataSource.resetPassword(resetPasswrdRequest)
            if (response.isSuccessful) {
                result = Result.Success(response.body())
                Log.i("ModelRepository", "Result $result")
            } else {
                val gson = Gson()
                val eventResponse =
                    gson.fromJson(response.errorBody()?.string(), ResponseOTP::class.java)
                Log.i("ModelRepository", "Error${response.errorBody()?.string()}")
                when (response.code()) {
                    400 -> {
                        Log.e("Error 400", "Bad Request")
                        result = Result.Error(Exception(eventResponse.status))
                    }
                    404 -> {
                        Log.e("Error 404", "Not Found")
                        result = Result.Error(Exception(eventResponse.status))
                    }

                    500 -> {
                        Log.e("Error 500", "Server Error")
                        result = Result.Error(Exception("Server is down"))

                    }
                    502 -> {
                        Log.e("Error 502", "Time out")
                        result =
                            Result.Error(Exception("حدث خطأ أثناء الاتصال بالانترنت برجاء المحاولة مرة أخرى"))
                    }
                    else -> {
                        Log.e("Error", "Generic Error")
                        //      result = Result.Error(Exception("Error"))
                    }
                }
            }

        } catch (e: IOException) {
            val message: String
            if (e is SocketTimeoutException) {
                message = "حدث خطأ أثناء الاتصال  برجاء المحاولة مرة أخرى."
                result = Result.Error(java.lang.Exception(message))
            } else {
                result = Result.Error(e)
                Log.e("ModelRepository", "IOException ${e.message}")
                Log.e("ModelRepository", "IOException ${e.localizedMessage}")
            }
        }
        return result
    }

    override suspend fun setServiceForm(serviceForm: FormData): Result<QRCode?> {
        var result: Result<QRCode?> = Result.Loading
        try {
            val response = apiDataSource.setServiceForm("Bearer ${getToken()}", serviceForm)
            Log.i("ModelRepositoryForm", "response :  $response")
            if (response?.isSuccessful == true) {
                result = Result.Success(response.body())
                Log.i("ModelRepositoryForm", "Result $result")
            } else {
                Log.i("ModelRepositoryForm", "Error${response?.errorBody()}")
                Log.i("ModelRepositoryForm", "code${response?.code()}")
                when (response?.code()) {
                    400 -> {
                        Log.e("Error 400", "Bad Request")
                        Log.e("Error 400", "SetForm: " + response.errorBody()?.string())
                        result = Result.Error(Exception("Bad Request SetForm"))
                    }
                    404 -> {
                        Log.e("Error 404", "Not Found")
                    }
                    500 -> {
                        Log.e("Error 500", "Server Error")
                        result = Result.Error(Exception("server is down"))
                    }
                    401 -> {
                        Log.e("Error 401", "Not Auth please, logout and login again")
                        //         result = Result.Error(Exception("Not Auth please, logout and login again"))
                        if (isLogin()) {
                            Log.i(
                                "Model Repo:",
                                "isLogin:" + isLogin() + ", token:" + getToken() + ",  refresh token:" + getRefreshToken()
                            )

                            val res = refreshToken(RefreshTokenRequest(getToken(), getRefreshToken()))
                            when(res) {
                                is Result.Success -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                                }
                                is Result.Error -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                                }
                            }
                        }
                        else {
                            result =
                                Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                        }
                    }
                    502 -> {
                        Log.e("Error 502", "Time out")
                        result =
                            Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                    }
                    else -> {
                        Log.e("Error", "Generic Error ${response?.code()}")
                    }
                }

            }

        } catch (e: IOException) {
            var message: String
            if (e is SocketTimeoutException) {
                message = "حدث خطأ أثناء الاتصال بالانترنت برجاء المحاولة مرة أخرى."
                result = Result.Error(java.lang.Exception(message))
            } else {
                result = Result.Error(e)
                Log.e("ModelRepository", "IOException ${e.message}")
                Log.e("ModelRepository", "IOException ${e.localizedMessage}")
            }
        }
        return result
    }

    override suspend fun getServiceForm(): Result<AddServiceResponse?> {
        var result: Result<AddServiceResponse?> = Result.Loading
        try {
            Log.i("ModelRepository:@@", "Token ${getToken()}")

            val response = apiDataSource.getServiceForm("Bearer " + getToken())
            if (response.isSuccessful) {
                result = Result.Success(response.body())
                Log.i("ModelRepository", "Result $result")
            } else {
                Log.i("ModelRepository", "Error${response.message()}")
                when (response.code()) {
                    400 -> {
                        Log.e("Error 400", "Bad Request")
                        result = Result.Error(Exception("Bad Request getData"))
                    }
                    404 -> {
                        Log.e("Error 404", "Not Found")
                    }
                    500 -> {
                        Log.e("Error 500", "Server Error")
                        result = Result.Error(Exception("server is down"))
                    }
                    401 -> {
                        Log.e("Error 401", "Not Auth please, logout and login again")
                        //         result = Result.Error(Exception("Not Auth please, logout and login again"))
                        if (isLogin()) {
                            Log.i(
                                "Model Repo:",
                                "isLogin:" + isLogin() + ", token:" + getToken() + ",  refresh token:" + getRefreshToken()
                            )

                            val res = refreshToken(RefreshTokenRequest(getToken(), getRefreshToken()))
                            when(res) {
                                is Result.Success -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                                }
                                is Result.Error -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                                }
                            }
                        }
                        else {
                            result =
                                Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                        }
                    }
                    502 -> {
                        Log.e("Error 502", "Time out")
                        result =
                            Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                    }
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }
            }

        } catch (e: IOException) {
            var message: String
            if (e is SocketTimeoutException) {
                message = "حدث خطأ أثناء الاتصال بالانترنت برجاء المحاولة مرة أخرى."
                result = Result.Error(java.lang.Exception(message))
            } else {
                result = Result.Error(e)
                Log.e("ModelRepository", "IOException ${e.message}")
                Log.e("ModelRepository", "IOException ${e.localizedMessage}")
            }
        }

        return result
    }

    override suspend fun getHomeData(): Result<HomeServicesResponse?> {
        var result: Result<HomeServicesResponse?> = Result.Loading
        try {
            Log.i("ModelRepository:@@", "Token ${getToken()}")

            val response = apiDataSource.getHomeData("Bearer " + getToken())
            if (response.isSuccessful) {
                result = Result.Success(response.body())
                Log.i("ModelRepository", "Result $result")
            } else {
                Log.i("ModelRepository", "Error${response.errorBody()?.string()}")
                when (response.code()) {
                    400 -> {
                        Log.e("Error 400", "Bad Request")
                        result = Result.Error(Exception("Bad Request getData"))
                    }
                    404 -> {
                        Log.e("Error 404", "Not Found")
                        result = Result.Error(Exception("Not Found"))
                    }
                    500 -> {
                        Log.e("Error 500", "Server Error")
                        result = Result.Error(Exception("server is down"))
                    }
                    401 -> {
                        Log.e("Error 401", "Not Auth please, logout and login again")
                        //         result = Result.Error(Exception("Not Auth please, logout and login again"))
                        if (isLogin()) {
                            Log.i(
                                "Model Repo:",
                                "isLogin:" + isLogin() + ", token:" + getToken() + ",  refresh token:" + getRefreshToken()
                            )

                            val res = refreshToken(RefreshTokenRequest(getToken(), getRefreshToken()))
                            when(res) {
                                is Result.Success -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                                }
                                is Result.Error -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                                }
                            }
                        }
                        else {
                            result =
                                Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                        }
                    }
                    502 -> {
                        Log.e("Error 502", "Time out")
                        result =
                            Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                    }
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }
            }

        } catch (e: IOException) {
            var message: String
            if (e is SocketTimeoutException) {
                message = "حدث خطأ أثناء الاتصال بالانترنت برجاء المحاولة مرة أخرى."
                result = Result.Error(java.lang.Exception(message))
            } else {
                result = Result.Error(e)
                Log.e("ModelRepository", "IOException ${e.message}")
                Log.e("ModelRepository", "IOException ${e.localizedMessage}")
            }
        }
        return result
    }

    override suspend fun getHistoryData(): Result<AllHistoryResponse?> {
        var result: Result<AllHistoryResponse?> = Result.Loading
        try {
            Log.i("ModelRepository:@@", "Token ${getToken()}")

            val response = apiDataSource.getHistoryData("Bearer " + getToken())
            if (response.isSuccessful) {
                result = Result.Success(response.body())
                Log.i("ModelRepository", "Resulttt $result")
            } else {
                Log.i("ModelRepository", "Error${response.errorBody()?.string()}")
                when (response.code()) {
                    400 -> {
                        Log.e("Error 400", "Bad Request")
                        result = Result.Error(Exception("Bad Request getData"))
                    }
                    404 -> {
                        Log.e("Error 404", "Not Found")
                        result = Result.Error(Exception("Not Found"))
                    }
                    500 -> {
                        Log.e("Error 500", "Server Error")
                        result = Result.Error(Exception("server is down"))
                    }
                    401 -> {
                        Log.e("Error 401", "Not Auth please, logout and login again")
                        //         result = Result.Error(Exception("Not Auth please, logout and login again"))
                        if (isLogin()) {
                            Log.i(
                                "Model Repo:",
                                "isLogin:" + isLogin() + ", token:" + getToken() + ",  refresh token:" + getRefreshToken()
                            )

                            val res = refreshToken(RefreshTokenRequest(getToken(), getRefreshToken()))
                            when(res) {
                                is Result.Success -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                                }
                                is Result.Error -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                                }
                            }
                    }
                        else {
                            result =
                               Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                        }
                    }
                    502 -> {
                        Log.e("Error 502", "Time out")
                        result =
                            Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                    }
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }
            }

        } catch (e: IOException) {
            var message: String
            if (e is SocketTimeoutException) {
                message = "حدث خطأ أثناء الاتصال بالانترنت برجاء المحاولة مرة أخرى."
                result = Result.Error(java.lang.Exception(message))
            } else {
                result = Result.Error(e)
                Log.e("ModelRepository", "IOException ${e.message}")
                Log.e("ModelRepository", "IOException ${e.localizedMessage}")
            }
        }
        return result
    }

    override suspend fun getFilteredHistory(
        serviceTypeId: Int,
        period: String
    ): Result<FilteredHistoryResponse?> {
        var result: Result<FilteredHistoryResponse?> = Result.Loading
        try {
            Log.i("ModelRepository:@@", "Token ${getToken()}")
            val response = apiDataSource.getFilteredHistory(
                "Bearer " + getToken(),
                serviceTypeId,
                period
            )
            if (response.isSuccessful) {
                result = Result.Success(response.body())
                Log.i("ModelRepository", "Resulttt $result")
                when (response.code()) {
                    204 -> {
                        Log.e("Error 204", "No content")
                        result = Result.Error(Exception("No content"))
                    }
                }
            } else {
                Log.i("ModelRepository", "Error${response.errorBody()?.string()}")
                when (response.code()) {
                    400 -> {
                        Log.e("Error 400", "Bad Request")
                        result = Result.Error(Exception("Bad Request"))
                    }
                    404 -> {
                        Log.e("Error 404", "Not Found")
                        result = Result.Error(Exception("Not Found"))
                    }
                    500 -> {
                        Log.e("Error 500", "Server Error")
                        result = Result.Error(Exception("server is down"))
                    }
                    204 -> {
                        Log.e("Error 204", "No content")
                        result = Result.Error(Exception("No content"))
                    }
                    401 -> {
                        Log.e("Error 401", "Not Auth please, logout and login again")
                        //         result = Result.Error(Exception("Not Auth please, logout and login again"))
                        if (isLogin()) {
                            Log.i(
                                "Model Repo:",
                                "isLogin:" + isLogin() + ", token:" + getToken() + ",  refresh token:" + getRefreshToken()
                            )

                            val res = refreshToken(RefreshTokenRequest(getToken(), getRefreshToken()))
                            when(res) {
                                is Result.Success -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                                }
                                is Result.Error -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                                }
                            }
                        }
                        else {
                            result =
                                Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                        }
                    }
                    502 -> {
                        Log.e("Error 502", "Time out")
                        result =
                            Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                    }
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }
            }

        } catch (e: IOException) {
            var message: String
            if (e is SocketTimeoutException) {
                message = "حدث خطأ أثناء الاتصال بالانترنت برجاء المحاولة مرة أخرى."
                result = Result.Error(java.lang.Exception(message))
            } else {
                result = Result.Error(e)
                Log.e("ModelRepository", "IOException ${e.message}")
                Log.e("ModelRepository", "IOException ${e.localizedMessage}")
            }
        }
        return result
    }

    override suspend fun getHistoryDataByService(
        serviceTypeId: Int,
        period: String,
        pageNumber: Int

    ): Result<HistoryByServiceIdResponse?> {
        var result: Result<HistoryByServiceIdResponse?> = Result.Loading
        try {
            Log.i("ModelRepository:@@", "Token ${getToken()}")
            val response = apiDataSource.getHistoryDataByService(
                "Bearer " + getToken(),
                serviceTypeId,
                period,
                pageNumber

            )
            if (response.isSuccessful) {
                result = Result.Success(response.body())
                Log.i("ModelRepository", "Resulttt $result")
            } else {
                Log.i("ModelRepository", "Error${response.errorBody()?.string()}")
                when (response.code()) {
                    400 -> {
                        Log.e("Error 400", "Bad Request")
                        result = Result.Error(Exception("Bad Request"))
                    }
                    404 -> {
                        Log.e("Error 404", "Not Found")
                        result = Result.Error(Exception("Not Found"))
                    }
                    500 -> {
                        Log.e("Error 500", "Server Error")
                        result = Result.Error(Exception("server is down"))
                    }
                    401 -> {
                        Log.e("Error 401", "Not Auth please, logout and login again")
                        //         result = Result.Error(Exception("Not Auth please, logout and login again"))
                        if (isLogin()) {
                            Log.i(
                                "Model Repo:",
                                "isLogin:" + isLogin() + ", token:" + getToken() + ",  refresh token:" + getRefreshToken()
                            )

                            val res = refreshToken(RefreshTokenRequest(getToken(), getRefreshToken()))
                            when(res) {
                                is Result.Success -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                                }
                                is Result.Error -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                                }
                            }
                        }
                        else {
                            result =
                                Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                        }
                    }
                    502 -> {
                        Log.e("Error 502", "Time out")
                        result =
                            Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                    }
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }
            }

        } catch (e: IOException) {
            var message: String
            if (e is SocketTimeoutException) {
                message = "حدث خطأ أثناء الاتصال بالانترنت برجاء المحاولة مرة أخرى."
                result = Result.Error(java.lang.Exception(message))
            } else {
                result = Result.Error(e)
                Log.e("ModelRepository", "IOException ${e.message}")
                Log.e("ModelRepository", "IOException ${e.localizedMessage}")
            }
        }
        return result
    }

    override suspend fun searchHistoryDataByService(searchRequest: SearchRequest): Result<SearchResponse?> {
        var result: Result<SearchResponse?> = Result.Loading
        try {
            Log.i("ModelRepository:@@", "Token ${getToken()}")
            val response =
                apiDataSource.searchHistoryDataByService("Bearer " + getToken(), searchRequest)
            if (response.isSuccessful) {
                result = Result.Success(response.body())
                Log.i("ModelRepository", "Resulttt $result")
            } else {
                Log.i("ModelRepository", "Error ${response.errorBody()?.string()}")
                when (response.code()) {
                    400 -> {
                        Log.e("Error 400", "Bad Request")
                        result = Result.Error(Exception("Bad Request"))
                    }
                    404 -> {
                        Log.e("Error 404", "Not Found")
                    }
                    500 -> {
                        Log.e("Error 500", "Server Error")
                        result = Result.Error(Exception("server is down"))
                    }
                    401 -> {
                        Log.e("Error 401", "Not Auth please, logout and login again")
                        //         result = Result.Error(Exception("Not Auth please, logout and login again"))
                        if (isLogin()) {
                            Log.i(
                                "Model Repo:",
                                "isLogin:" + isLogin() + ", token:" + getToken() + ",  refresh token:" + getRefreshToken()
                            )

                            val res = refreshToken(RefreshTokenRequest(getToken(), getRefreshToken()))
                            when(res) {
                                is Result.Success -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                                }
                                is Result.Error -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                                }
                            }
                        }
                        else {
                            result =
                                Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                        }
                    }
                    502 -> {
                        Log.e("Error 502", "Time out")
                        result =
                            Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                    }
                    else -> {

                        Log.e("Error", "Generic Error${response.code()}")
                    }
                }
            }

        } catch (e: IOException) {
            var message: String
            if (e is SocketTimeoutException) {
                message = "حدث خطأ أثناء الاتصال بالانترنت برجاء المحاولة مرة أخرى."
                result = Result.Error(java.lang.Exception(message))
            } else {
                result = Result.Error(e)
                Log.e("ModelRepository", "IOException ${e.message}")
                Log.e("ModelRepository", "IOException ${e.localizedMessage}")
            }
        }
        return result
    }

    override suspend fun isDailyPrepared(): Result<CheckDailyPreparationResponse?> {
        var result: Result<CheckDailyPreparationResponse?> = Result.Loading
        try {
            Log.i("ModelRepository:@@", "Token ${getToken()}")

            val response = apiDataSource.isDailyPrepared("Bearer " + getToken())
            if (response.isSuccessful) {
                result = Result.Success(response.body())
                Log.i("ModelRepository", "Resulttt $result")
            } else {
                Log.i("ModelRepository", "Error${response.errorBody()?.string()}")
                when (response.code()) {
                    400 -> {
                            Log.e("Error 400", "Bad Request")
                            result = Result.Error(Exception("Bad Request isDailyPrepared"))

                    }
                    404 -> {
                        Log.e("Error 404", "Not Found")
                        result = Result.Error(Exception("Not Found"))
                    }
                    500 -> {
                        Log.e("Error 500", "Server Error")
                        result = Result.Error(Exception("server is down"))
                    }
                    401 -> {
                        Log.e("Error 401", "Not Auth please, logout and login again")
                        if (isLogin()) {
                            Log.i(
                                "Model Repo:",
                                "isDailyPrepared:" + isLogin() + ", token:" + getToken() + ",  refresh token:" + getRefreshToken()
                            )
                            val refreshResponse =refreshToken(RefreshTokenRequest(getToken(), getRefreshToken()))
                    when(refreshResponse){
                        is Result.Success -> {
                            Log.i("refresh token :", "${refreshResponse.data}")
                            result = isDailyPrepared()

                        }
                        is Result.Error -> {
                            Log.e("refresh token :", "${refreshResponse.exception.message}")
                            result = Result.Error(Exception("login required, logout and login again"))


                        }

                    }
                        }
                    }
                    502 -> {
                        Log.e("Error 502", "Time out")
                        result =
                            Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                    }
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }
            }

        } catch (e: IOException) {
            var message: String
            if (e is SocketTimeoutException) {
                message = "حدث خطأ أثناء الاتصال بالانترنت برجاء المحاولة مرة أخرى."
                result = Result.Error(java.lang.Exception(message))
            } else {
                result = Result.Error(e)
                Log.e("ModelRepository", "IOException ${e.message}")
                Log.e("ModelRepository", "IOException ${e.localizedMessage}")
            }
        }
        return result
    }

    override suspend fun setDailyPreparation(
        WorkersTypesList: Map<Long, Int>,
        equipmentList: Map<Long, Int>
    ): Result<Void?> {
        var result: Result<Void?> = Result.Loading
        try {

            val response = apiDataSource.setDailyPreparation(
                "Bearer ${getToken()}",
                WorkersTypesList,
                equipmentList
            )
            if (response.isSuccessful) {
                result = Result.Success(response.body())
                Log.i("ModelRepositoryForm", "Result $result")
            } else {
                Log.i("ModelRepositoryForm", "Error${response.errorBody()}")
                when (response.code()) {
                    400 -> {
                        Log.e("Error 400", "Bad Request")
                        Log.e("Error 400", "setDailyPreparation: " + response.errorBody()?.string())
                        result = Result.Error(Exception("Bad Request setDailyPreparation"))
                    }
                    404 -> {
                        Log.e("Error 404", "Not Found")
                    }
                    500 -> {
                        Log.e("Error 500", "Server Error")
                        result = Result.Error(Exception("server is down"))
                    }
                    401 -> {
                        Log.e("Error 401", "Not Auth please, logout and login again")
                        //         result = Result.Error(Exception("Not Auth please, logout and login again"))
                        if (isLogin()) {
                            Log.i(
                                "Model Repo:",
                                "isLogin:" + isLogin() + ", token:" + getToken() + ",  refresh token:" + getRefreshToken()
                            )

                            val res = refreshToken(RefreshTokenRequest(getToken(), getRefreshToken()))
                            when(res) {
                                is Result.Success -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                                }
                                is Result.Error -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                                }
                            }
                        }
                        else {
                            result =
                                Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                        }
                    }
                    502 -> {
                        Log.e("Error 502", "Time out")
                        result =
                            Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                    }
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }

            }

        } catch (e: IOException) {
            var message: String
            if (e is SocketTimeoutException) {
                message = "حدث خطأ أثناء الاتصال بالانترنت برجاء المحاولة مرة أخرى."
                result = Result.Error(java.lang.Exception(message))
            } else {
                result = Result.Error(e)
                Log.e("ModelRepository", "IOException ${e.message}")
                Log.e("ModelRepository", "IOException ${e.localizedMessage}")
            }
        }
        return result
    }

    override suspend fun getCreateDailyPreparation(): Result<GetDailyPraprationData?> {
        var result: Result<GetDailyPraprationData?> = Result.Loading
        try {
            Log.i("ModelRepository:@@", "Token ${getToken()}")

            val response = apiDataSource.getCreateDailyPreparation("Bearer " + getToken())
            if (response.isSuccessful) {
                result = Result.Success(response.body())
                Log.i("ModelRepository", "Resulttt $result")
            } else {
                Log.i("ModelRepository", "Error${response.errorBody()?.string()}")
                when (response.code()) {
                    400 -> {
                        Log.e("Error 400", "Bad Request getDailyPreparation")
                        result = Result.Error(Exception("Bad Request getDailyPreparation"))
                    }
                    404 -> {
                        Log.e("Error 404", "Not Found")
                        result = Result.Error(Exception("Not Found"))
                    }
                    500 -> {
                        Log.e("Error 500", "Server Error")
                        result = Result.Error(Exception("server is down"))
                    }
                    401 -> {
                        Log.e("Error 401", "Not Auth please, logout and login again")
                        //         result = Result.Error(Exception("Not Auth please, logout and login again"))
                        if (isLogin()) {
                            Log.i(
                                "Model Repo:",
                                "isLogin:" + isLogin() + ", token:" + getToken() + ",  refresh token:" + getRefreshToken()
                            )

                            val res = refreshToken(RefreshTokenRequest(getToken(), getRefreshToken()))
                            when(res) {
                                is Result.Success -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                                }
                                is Result.Error -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                                }
                            }
                        }
                        else {
                            result =
                                Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                        }
                    }
                    502 -> {
                        Log.e("Error 502", "Time out")
                        result =
                            Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                    }
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }
            }

        } catch (e: IOException) {
            var message: String
            if (e is SocketTimeoutException) {
                message = "حدث خطأ أثناء الاتصال بالانترنت برجاء المحاولة مرة أخرى."
                result = Result.Error(java.lang.Exception(message))
            } else {
                result = Result.Error(e)
                Log.e("ModelRepository", "IOException ${e.message}")
                Log.e("ModelRepository", "IOException ${e.localizedMessage}")
            }
        }
        return result
    }

    override suspend fun getDailyPreparation(): Result<TodayDailyPrapration?> {
        var result: Result<TodayDailyPrapration?> = Result.Loading
        try {
            Log.i("ModelRepository:@@", "Token ${getToken()}")

            val response = apiDataSource.getDailyPreparation("Bearer " + getToken())
            if (response.isSuccessful) {
                result = Result.Success(response.body())
                Log.i("ModelRepository", "Resulttt $result")
            } else {
                Log.i("ModelRepository", "Error${response.errorBody()?.string()}")
                when (response.code()) {
                    400 -> {
                        Log.e("Error 400", "Bad Request getDailyPreparation")
                        result = Result.Error(Exception("Bad Request getDailyPreparation"))
                    }
                    404 -> {
                        Log.e("Error 404", "Not Found")
                        result = Result.Error(Exception("Not Found"))
                    }
                    500 -> {
                        Log.e("Error 500", "Server Error")
                        result = Result.Error(Exception("server is down"))
                    }
                    401 -> {
                        Log.e("Error 401", "Not Auth please, logout and login again")
                        //         result = Result.Error(Exception("Not Auth please, logout and login again"))
                        if (isLogin()) {
                            Log.i(
                                "Model Repo:",
                                "isLogin:" + isLogin() + ", token:" + getToken() + ",  refresh token:" + getRefreshToken()
                            )

                            val res = refreshToken(RefreshTokenRequest(getToken(), getRefreshToken()))
                            when(res) {
                                is Result.Success -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                                }
                                is Result.Error -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                                }
                            }
                        }
                        else {
                            result =
                                Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                        }
                    }
                    502 -> {
                        Log.e("Error 502", "Time out")
                        result =
                            Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                    }
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }
            }

        } catch (e: IOException) {
            var message: String
            if (e is SocketTimeoutException) {
                message = "حدث خطأ أثناء الاتصال بالانترنت برجاء المحاولة مرة أخرى."
                result = Result.Error(java.lang.Exception(message))
            } else {
                result = Result.Error(e)
                Log.e("ModelRepository", "IOException ${e.message}")
                Log.e("ModelRepository", "IOException ${e.localizedMessage}")
            }
        }
        return result
    }

    override suspend fun editDailyPreparation(
        WorkersTypesList: Map<Long, Int>,
        equipmentList: Map<Long, Int>
    ): Result<Void?> {
        var result: Result<Void?> = Result.Loading
        try {

            val response = apiDataSource.editDailyPreparation(
                "Bearer ${getToken()}",
                WorkersTypesList,
                equipmentList
            )
            if (response.isSuccessful) {
                result = Result.Success(response.body())
                Log.i("ModelRepositoryForm", "Result $result")
            } else {
                Log.i("ModelRepositoryForm", "Error${response.errorBody()}")
                when (response.code()) {
                    400 -> {
                        Log.e("Error 400", "Bad Request")
                        Log.e(
                            "Error 400",
                            "editDailyPreparation: " + response.errorBody()?.string()
                        )
                        result = Result.Error(Exception("Bad Request editDailyPreparation"))
                    }
                    404 -> {
                        Log.e("Error 404", "Not Found")
                    }
                    500 -> {
                        Log.e("Error 500", "Server Error")
                        result = Result.Error(Exception("server is down"))
                    }
                    401 -> {
                        Log.e("Error 401", "Not Auth please, logout and login again")
                        //         result = Result.Error(Exception("Not Auth please, logout and login again"))
                        if (isLogin()) {
                            Log.i(
                                "Model Repo:",
                                "isLogin:" + isLogin() + ", token:" + getToken() + ",  refresh token:" + getRefreshToken()
                            )

                            val res = refreshToken(RefreshTokenRequest(getToken(), getRefreshToken()))
                            when(res) {
                                is Result.Success -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                                }
                                is Result.Error -> {
                                    result = Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                                }
                            }
                        }
                        else {
                            result =
                                Result.Error(Exception("حدث حطأ برجاء تسجيل الخروج ثم اعادة تسجيل الدخول"))
                        }
                    }
                    502 -> {
                        Log.e("Error 502", "Time out")
                        result =
                            Result.Error(Exception("حدث حطأ برجاء اعادة المحاولة "))
                    }
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }

            }

        } catch (e: IOException) {
            var message: String
            if (e is SocketTimeoutException) {
                message = "حدث خطأ أثناء الاتصال بالانترنت برجاء المحاولة مرة أخرى."
                result = Result.Error(java.lang.Exception(message))
            } else {
                result = Result.Error(e)
                Log.e("ModelRepository", "IOException ${e.message}")
                Log.e("ModelRepository", "IOException ${e.localizedMessage}")
            }
        }
        return result
    }

    override fun getAllDailyPreparation(): Flow<List<DailyPreparation>> {
        return localDataSource.getAllData()
    }

    override fun getDailyPreparation_By_ServiceTypeID(
        serviceTypeID: String,
        date: String
    ): DailyPreparation? {
        return localDataSource.getDailyPreparation_By_ServiceTypeID(serviceTypeID, date)
    }

    override fun insertDailyPreparation(dailyPreparation: DailyPreparation) {
        localDataSource.insertDailyPreparation(dailyPreparation)
    }

    override fun deleteAllDailyPreparation() {
        localDataSource.deleteAll()
    }

    override fun delete_By_ServiceTypeID(serviceTypeID: String, date: String) {
        localDataSource.deleteBy_ServiceTypeID(serviceTypeID, date)
    }

    override fun getFirstTimeLaunch(): Boolean {
        return sharedPreference.getFirstTimeLaunch()
    }

    override fun setFirstTimeLaunch(firstTimeLaunch: Boolean) {
        sharedPreference.setFirstTimeLaunch(firstTimeLaunch)
    }

    override fun isLogin(): Boolean {
        return sharedPreference.isLogin()
    }

    override fun setLogin(login: Boolean) {
        sharedPreference.setLogin(login)
    }

    override fun setEmail(email: String) {
        sharedPreference.setEmail(email)
    }

    override fun getEmail(): String {
        return sharedPreference.getEmail()
    }

    override fun setPass(pass: String) {
        sharedPreference.setPass(pass)
    }

    override fun getPass(): String {
        return sharedPreference.getPass()
    }

    override fun setToken(token: String) {
        sharedPreference.setToken(token)
    }

    override fun getToken(): String {
        return sharedPreference.getToken()
    }

    override fun setRefreshToken(refreshToken: String) {
        sharedPreference.setRefreshToken(refreshToken)
    }

    override fun getRefreshToken(): String {
        return sharedPreference.getRefreshToken()
    }

    override fun logout() {
        sharedPreference.setLogin(false)
        sharedPreference.setToken("")
        sharedPreference.setRefreshToken("")
        sharedPreference.setPass("")
        sharedPreference.setEmail("")
    }


}