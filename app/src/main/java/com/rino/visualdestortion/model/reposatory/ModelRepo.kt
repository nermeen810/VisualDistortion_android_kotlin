package com.rino.visualdestortion.model.reposatory

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.rino.visualdestortion.model.localDataSource.MySharedPreference
import com.rino.visualdestortion.model.localDataSource.sharedPref.Preference
import com.rino.visualdestortion.model.localDataSource.PreferenceDataSource
import com.rino.visualdestortion.model.localDataSource.room.DailyPreparation
import com.rino.visualdestortion.model.localDataSource.room.DailyPreparationDataSource
import com.rino.visualdestortion.model.pojo.addService.AddServiceResponse
import com.rino.visualdestortion.model.pojo.addService.FormData
import com.rino.visualdestortion.model.pojo.addService.QRCode
import com.rino.visualdestortion.model.pojo.dailyPraperation.CheckDailyPreparationResponse
import com.rino.visualdestortion.model.pojo.dailyPraperation.GetDailyPraprationData
import com.rino.visualdestortion.model.pojo.dailyPraperation.TodayDailyPrapration
import com.rino.visualdestortion.model.pojo.history.AllHistoryResponse
import com.rino.visualdestortion.model.pojo.history.HistoryByServiceIdResponse
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

class ModelRepo (application: Application):RemoteRepo,LocalRepo{
    private val apiDataSource: ApiInterface = ApiDataSource()
    private val preference =
        MySharedPreference(application.applicationContext.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE))
    private val sharedPreference: Preference = PreferenceDataSource(preference)

    private val localDataSource: DailyPreparationDataSource = DailyPreparationDataSource(application)

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
                        result = Result.Error(Exception("Email or Password invalid"))
                    }
                    404 -> {
                        Log.e("Error 404", "Not Found")
                        result = Result.Error(Exception("Not Found"))
                    }
                    500 -> {
                        Log.e("Error 500", "Server Error")
                        result = Result.Error(Exception("server is down"))
                    }
                    else -> {
                        Log.e("Error", "Generic Error")
                        result = Result.Error(Exception("Error"))
                    }
                }
            }

        }catch (e: IOException){
            result = Result.Error(e)
            Log.e("ModelRepository","IOException ${e.message}")
            Log.e("ModelRepository","IOException ${e.localizedMessage}")

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
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }
            }

        }catch (e: IOException){
            result = Result.Error(e)
            Log.e("ModelRepository","IOException ${e.message}")
            Log.e("ModelRepository","IOException ${e.localizedMessage}")

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
                    else -> {
                        Log.e("Error", "Generic Error")
                  //      result = Result.Error(Exception("Error"))
                    }
                }
            }

        }catch (e: IOException){
            result = Result.Error(e)
            Log.e("ModelRepository","IOException ${e.message}")
            Log.e("ModelRepository","IOException ${e.localizedMessage}")

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
                val eventResponse = gson.fromJson(response.errorBody()?.string(),ResponseOTP::class.java)
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
                    else -> {
                        Log.e("Error", "Generic Error")
                        //      result = Result.Error(Exception("Error"))
                    }
                }
            }

        }catch (e: IOException){
            result = Result.Error(e)
            Log.e("ModelRepository","IOException ${e.message}")
            Log.e("ModelRepository","IOException ${e.localizedMessage}")

        }
        return result
    }

    override suspend fun setServiceForm(serviceForm: FormData):Result<QRCode?> {
        var result: Result<QRCode?> = Result.Loading
        try {
            val response = apiDataSource.setServiceForm("Bearer ${getToken()}",serviceForm)
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
                        Log.e("Error 400", "SetForm: "+response.errorBody()?.string())
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
                        Log.e("Error 401", "Not Auth")
                        if(isLogin()){
                            Log.i("Model Repo:", "isLogin:"+isLogin()+", token:"+getToken()+",  refresh token:"+getRefreshToken())
                          refreshToken(RefreshTokenRequest(getToken(),getRefreshToken()))
                         // refreshToken(RefreshTokenRequest("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzdXBlcnVzZXIxIiwianRpIjoiNmU5NTcyZTAtZjNmYS00YWIwLTg0ZGMtZWVlYmYwNzE5MjE3IiwiZW1haWwiOiJheW1hbm9tYXJhNTVAZ21haWwuY29tIiwiaXNzIjoiaHR0cHM6Ly9hbWFuYXQtamVkZGFoLXN0YWdpbmcuYXp1cmV3ZWJzaXRlcy5uZXQiLCJhdWQiOiJodHRwczovL2FtYW5hdC1qZWRkYWgtc3RhZ2luZy5henVyZXdlYnNpdGVzLm5ldCIsInVpZCI6IjkxZmJkN2YxLTY0ZDctNGUzZC1iMDBiLWYwOWJiNTc5MzE1MiIsIm5iZiI6MTY0MjU4NjAxNCwiZXhwIjoxNjQyNjA3NjE0LCJpYXQiOjE2NDI1ODYwMTR9.mQq6kbudPaODn65aENzqivqbKxH7rqNfOuuZgP8oCQ0","02VBOXu+meD+7qGEfkgy082o3uef7bJjBdLKbqpfY8E="))
                          // refreshToken(RefreshTokenRequest("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJheW1hbm9tYXJhNTVAZ21haWwuY29tIiwianRpIjoiNGZlMDQ5NjQtZDRjNC00ZWQ3LTkwOTAtNDhhZWJlMjBhYzJhIiwiZW1haWwiOiJheW1hbm9tYXJhNTVAZ21haWwuY29tIiwiaXNzIjoiaHR0cHM6Ly9hbWFuYXQtamVkZGFoLXN0YWdpbmcuYXp1cmV3ZWJzaXRlcy5uZXQiLCJhdWQiOiJodHRwczovL2FtYW5hdC1qZWRkYWgtc3RhZ2luZy5henVyZXdlYnNpdGVzLm5ldCIsInVpZCI6IjQ1ZmVjYzlkLTI1NjAtNGNlMC04YTY4LTZlMjcyYzM1MDQ2ZiIsIm5iZiI6MTY0MjUwMTA1OCwiZXhwIjoxNjQyNTIyNjU4LCJpYXQiOjE2NDI1MDEwNTh9.MLjmtA69E__oy4aBAcicmMUcSmScYkyD6nK57c4oXCE","l1FAdwyASSqQxJVvAclqv5JkmHgoXWacweK5/iL0L/8="))
                    }}
                    else -> {
                        Log.e("Error", "Generic Error ${response?.code()}")
                    }
                }

            }

        }catch (e: IOException){
            result = Result.Error(e)
            Log.e("ModelRepository","IOException ${e.message}")
            Log.e("ModelRepository","IOException ${e.localizedMessage}")

        }
        return result
    }

    override suspend fun getServiceForm(): Result<AddServiceResponse?> {
        var result: Result<AddServiceResponse?> = Result.Loading
        try {
            Log.i("ModelRepository:@@", "Token ${getToken()}")

            val response = apiDataSource.getServiceForm("Bearer "+getToken())
            if (response.isSuccessful) {
                result = Result.Success(response.body())
                Log.i("ModelRepository", "Result $result")
            } else {
                Log.i("ModelRepository", "Error${response.message().toString()}")
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
                        Log.e("Error 401", "Not Auth")
                        if(isLogin())
                        {
                            Log.i("Model Repo:", "isLogin:"+isLogin()+", token:"+getToken()+",  refresh token:"+getRefreshToken())
                      //  refreshToken(RefreshTokenRequest("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzdXBlcnVzZXIxIiwianRpIjoiNmU5NTcyZTAtZjNmYS00YWIwLTg0ZGMtZWVlYmYwNzE5MjE3IiwiZW1haWwiOiJheW1hbm9tYXJhNTVAZ21haWwuY29tIiwiaXNzIjoiaHR0cHM6Ly9hbWFuYXQtamVkZGFoLXN0YWdpbmcuYXp1cmV3ZWJzaXRlcy5uZXQiLCJhdWQiOiJodHRwczovL2FtYW5hdC1qZWRkYWgtc3RhZ2luZy5henVyZXdlYnNpdGVzLm5ldCIsInVpZCI6IjkxZmJkN2YxLTY0ZDctNGUzZC1iMDBiLWYwOWJiNTc5MzE1MiIsIm5iZiI6MTY0MjU4NjAxNCwiZXhwIjoxNjQyNjA3NjE0LCJpYXQiOjE2NDI1ODYwMTR9.mQq6kbudPaODn65aENzqivqbKxH7rqNfOuuZgP8oCQ0","02VBOXu+meD+7qGEfkgy082o3uef7bJjBdLKbqpfY8E="))
                          refreshToken(RefreshTokenRequest(getToken(),getRefreshToken()))
                         //   refreshToken(RefreshTokenRequest("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJheW1hbm9tYXJhNTVAZ21haWwuY29tIiwianRpIjoiNGZlMDQ5NjQtZDRjNC00ZWQ3LTkwOTAtNDhhZWJlMjBhYzJhIiwiZW1haWwiOiJheW1hbm9tYXJhNTVAZ21haWwuY29tIiwiaXNzIjoiaHR0cHM6Ly9hbWFuYXQtamVkZGFoLXN0YWdpbmcuYXp1cmV3ZWJzaXRlcy5uZXQiLCJhdWQiOiJodHRwczovL2FtYW5hdC1qZWRkYWgtc3RhZ2luZy5henVyZXdlYnNpdGVzLm5ldCIsInVpZCI6IjQ1ZmVjYzlkLTI1NjAtNGNlMC04YTY4LTZlMjcyYzM1MDQ2ZiIsIm5iZiI6MTY0MjUwMTA1OCwiZXhwIjoxNjQyNTIyNjU4LCJpYXQiOjE2NDI1MDEwNTh9.MLjmtA69E__oy4aBAcicmMUcSmScYkyD6nK57c4oXCE","l1FAdwyASSqQxJVvAclqv5JkmHgoXWacweK5/iL0L/8="))
                    }}
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }
            }

        }catch (e: IOException){
            result = Result.Error(e)
            Log.e("ModelRepository","IOException ${e.message}")
            Log.e("ModelRepository","IOException ${e.localizedMessage}")

        }
        return result    }

    override suspend fun getHomeData(): Result<HomeServicesResponse?> {
        var result: Result<HomeServicesResponse?> = Result.Loading
        try {
            Log.i("ModelRepository:@@", "Token ${getToken()}")

            val response = apiDataSource.getHomeData("Bearer "+getToken())
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
                        Log.e("Error 401", "Not Auth")
                        //result = Result.Error(Exception("Not Auth please, logout and login again"))
                        if(isLogin()){
                            Log.i("Model Repo:", "isLogin:"+isLogin()+", token:"+getToken()+",  refresh token:"+getRefreshToken())
                        //refreshToken(RefreshTokenRequest("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzdXBlcnVzZXIxIiwianRpIjoiNmU5NTcyZTAtZjNmYS00YWIwLTg0ZGMtZWVlYmYwNzE5MjE3IiwiZW1haWwiOiJheW1hbm9tYXJhNTVAZ21haWwuY29tIiwiaXNzIjoiaHR0cHM6Ly9hbWFuYXQtamVkZGFoLXN0YWdpbmcuYXp1cmV3ZWJzaXRlcy5uZXQiLCJhdWQiOiJodHRwczovL2FtYW5hdC1qZWRkYWgtc3RhZ2luZy5henVyZXdlYnNpdGVzLm5ldCIsInVpZCI6IjkxZmJkN2YxLTY0ZDctNGUzZC1iMDBiLWYwOWJiNTc5MzE1MiIsIm5iZiI6MTY0MjU4NjAxNCwiZXhwIjoxNjQyNjA3NjE0LCJpYXQiOjE2NDI1ODYwMTR9.mQq6kbudPaODn65aENzqivqbKxH7rqNfOuuZgP8oCQ0","02VBOXu+meD+7qGEfkgy082o3uef7bJjBdLKbqpfY8E="))
                         refreshToken(RefreshTokenRequest(getToken(),getRefreshToken()))
                        //   refreshToken(RefreshTokenRequest("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJheW1hbm9tYXJhNTVAZ21haWwuY29tIiwianRpIjoiNGZlMDQ5NjQtZDRjNC00ZWQ3LTkwOTAtNDhhZWJlMjBhYzJhIiwiZW1haWwiOiJheW1hbm9tYXJhNTVAZ21haWwuY29tIiwiaXNzIjoiaHR0cHM6Ly9hbWFuYXQtamVkZGFoLXN0YWdpbmcuYXp1cmV3ZWJzaXRlcy5uZXQiLCJhdWQiOiJodHRwczovL2FtYW5hdC1qZWRkYWgtc3RhZ2luZy5henVyZXdlYnNpdGVzLm5ldCIsInVpZCI6IjQ1ZmVjYzlkLTI1NjAtNGNlMC04YTY4LTZlMjcyYzM1MDQ2ZiIsIm5iZiI6MTY0MjUwMTA1OCwiZXhwIjoxNjQyNTIyNjU4LCJpYXQiOjE2NDI1MDEwNTh9.MLjmtA69E__oy4aBAcicmMUcSmScYkyD6nK57c4oXCE","l1FAdwyASSqQxJVvAclqv5JkmHgoXWacweK5/iL0L/8="))
                    }}
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }
            }

        }catch (e: IOException){
            result = Result.Error(e)
            Log.e("ModelRepository","IOException ${e.message}")
            Log.e("ModelRepository","IOException ${e.localizedMessage}")

        }
        return result
    }

    override suspend fun getHistoryData(): Result<AllHistoryResponse?> {
        var result: Result<AllHistoryResponse?> = Result.Loading
        try {
            Log.i("ModelRepository:@@", "Token ${getToken()}")

            val response = apiDataSource.getHistoryData("Bearer "+getToken())
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
                        if(isLogin()){
                            Log.i("Model Repo:", "isLogin:"+isLogin()+", token:"+getToken()+",  refresh token:"+getRefreshToken())
                        refreshToken(RefreshTokenRequest(getToken(),getRefreshToken()))
                        //   refreshToken(RefreshTokenRequest("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJheW1hbm9tYXJhNTVAZ21haWwuY29tIiwianRpIjoiNGZlMDQ5NjQtZDRjNC00ZWQ3LTkwOTAtNDhhZWJlMjBhYzJhIiwiZW1haWwiOiJheW1hbm9tYXJhNTVAZ21haWwuY29tIiwiaXNzIjoiaHR0cHM6Ly9hbWFuYXQtamVkZGFoLXN0YWdpbmcuYXp1cmV3ZWJzaXRlcy5uZXQiLCJhdWQiOiJodHRwczovL2FtYW5hdC1qZWRkYWgtc3RhZ2luZy5henVyZXdlYnNpdGVzLm5ldCIsInVpZCI6IjQ1ZmVjYzlkLTI1NjAtNGNlMC04YTY4LTZlMjcyYzM1MDQ2ZiIsIm5iZiI6MTY0MjUwMTA1OCwiZXhwIjoxNjQyNTIyNjU4LCJpYXQiOjE2NDI1MDEwNTh9.MLjmtA69E__oy4aBAcicmMUcSmScYkyD6nK57c4oXCE","l1FAdwyASSqQxJVvAclqv5JkmHgoXWacweK5/iL0L/8="))
                    }}
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }
            }

        }catch (e: IOException){
            result = Result.Error(e)
            Log.e("ModelRepository","IOException ${e.message}")
            Log.e("ModelRepository","IOException ${e.localizedMessage}")

        }
        return result
    }

    override suspend fun getHistoryDataByService(serviceTypeId: Int,pageNumber: Int,period:String): Result<HistoryByServiceIdResponse?> {
        var result: Result<HistoryByServiceIdResponse?> = Result.Loading
        try {
            Log.i("ModelRepository:@@", "Token ${getToken()}")

            val response = apiDataSource.getHistoryDataByService("Bearer "+getToken(),serviceTypeId,pageNumber,period)
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
                   //     result = Result.Error(Exception("Not Auth please, logout and login again"))
                        if(isLogin()) {
                            Log.i("Model Repo:",
                                "isLogin:" + isLogin() + ", token:" + getToken() + ",  refresh token:" + getRefreshToken()
                            )
                            refreshToken(RefreshTokenRequest(getToken(), getRefreshToken()))
                            //   refreshToken(RefreshTokenRequest("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJheW1hbm9tYXJhNTVAZ21haWwuY29tIiwianRpIjoiNGZlMDQ5NjQtZDRjNC00ZWQ3LTkwOTAtNDhhZWJlMjBhYzJhIiwiZW1haWwiOiJheW1hbm9tYXJhNTVAZ21haWwuY29tIiwiaXNzIjoiaHR0cHM6Ly9hbWFuYXQtamVkZGFoLXN0YWdpbmcuYXp1cmV3ZWJzaXRlcy5uZXQiLCJhdWQiOiJodHRwczovL2FtYW5hdC1qZWRkYWgtc3RhZ2luZy5henVyZXdlYnNpdGVzLm5ldCIsInVpZCI6IjQ1ZmVjYzlkLTI1NjAtNGNlMC04YTY4LTZlMjcyYzM1MDQ2ZiIsIm5iZiI6MTY0MjUwMTA1OCwiZXhwIjoxNjQyNTIyNjU4LCJpYXQiOjE2NDI1MDEwNTh9.MLjmtA69E__oy4aBAcicmMUcSmScYkyD6nK57c4oXCE","l1FAdwyASSqQxJVvAclqv5JkmHgoXWacweK5/iL0L/8="))
                        } }
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }
            }

        }catch (e: IOException){
            result = Result.Error(e)
            Log.e("ModelRepository","IOException ${e.message}")
            Log.e("ModelRepository","IOException ${e.localizedMessage}")

        }
        return result
    }

    override suspend fun isDailyPrepared(): Result<CheckDailyPreparationResponse?> {
        var result: Result<CheckDailyPreparationResponse?> = Result.Loading
        try {
            Log.i("ModelRepository:@@", "Token ${getToken()}")

            val response = apiDataSource.isDailyPrepared("Bearer "+getToken())
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
                       // result = Result.Error(Exception("Not Auth please, logout and login again"))
                        if(isLogin()) {
                            Log.i(
                                "Model Repo:",
                                "isDailyPrepared:" + isLogin() + ", token:" + getToken() + ",  refresh token:" + getRefreshToken()
                            )
                            refreshToken(RefreshTokenRequest(getToken(), getRefreshToken()))
                        } }
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }
            }

        }catch (e: IOException){
            result = Result.Error(e)
            Log.e("ModelRepository","IOException ${e.message}")
            Log.e("ModelRepository","IOException ${e.localizedMessage}")

        }
        return result
    }

    override suspend fun setDailyPreparation(
        WorkersTypesList: Map<Long, Int>,
        equipmentList: Map<Long, Int>
    ): Result<Void?> {
        var result: Result<Void?> = Result.Loading
        try {

            val response = apiDataSource.setDailyPreparation("Bearer ${getToken()}",WorkersTypesList,equipmentList)
            if (response.isSuccessful) {
                result = Result.Success(response.body())
                Log.i("ModelRepositoryForm", "Result $result")
            } else {
                Log.i("ModelRepositoryForm", "Error${response.errorBody()}")
                when (response.code()) {
                    400 -> {
                        Log.e("Error 400", "Bad Request")
                        Log.e("Error 400", "setDailyPreparation: "+response.errorBody()?.string())
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
                        Log.e("Error 401", "Not Auth")
                        if(isLogin()){
                            Log.i("Model Repo:", "isLogin:"+isLogin()+", token:"+getToken()+",  refresh token:"+getRefreshToken())
                        refreshToken(RefreshTokenRequest("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzdXBlcnVzZXIxIiwianRpIjoiNmU5NTcyZTAtZjNmYS00YWIwLTg0ZGMtZWVlYmYwNzE5MjE3IiwiZW1haWwiOiJheW1hbm9tYXJhNTVAZ21haWwuY29tIiwiaXNzIjoiaHR0cHM6Ly9hbWFuYXQtamVkZGFoLXN0YWdpbmcuYXp1cmV3ZWJzaXRlcy5uZXQiLCJhdWQiOiJodHRwczovL2FtYW5hdC1qZWRkYWgtc3RhZ2luZy5henVyZXdlYnNpdGVzLm5ldCIsInVpZCI6IjkxZmJkN2YxLTY0ZDctNGUzZC1iMDBiLWYwOWJiNTc5MzE1MiIsIm5iZiI6MTY0MjU4NjAxNCwiZXhwIjoxNjQyNjA3NjE0LCJpYXQiOjE2NDI1ODYwMTR9.mQq6kbudPaODn65aENzqivqbKxH7rqNfOuuZgP8oCQ0","02VBOXu+meD+7qGEfkgy082o3uef7bJjBdLKbqpfY8E="))
                    }
                        }
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }

            }

        }catch (e: IOException){
            result = Result.Error(e)
            Log.e("ModelRepository","IOException ${e.message}")
            Log.e("ModelRepository","IOException ${e.localizedMessage}")

        }
        return result
    }

    override suspend fun getCreateDailyPreparation(): Result<GetDailyPraprationData?> {
        var result: Result<GetDailyPraprationData?> = Result.Loading
        try {
            Log.i("ModelRepository:@@", "Token ${getToken()}")

            val response = apiDataSource.getCreateDailyPreparation("Bearer "+getToken())
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
                        result = Result.Error(Exception("Not Auth please, logout and login again"))
                        if(isLogin()){
                            Log.i("Model Repo:", "isLogin:"+isLogin()+", token:"+getToken()+",  refresh token:"+getRefreshToken())
                            refreshToken(RefreshTokenRequest(getToken(),getRefreshToken()))
                        }
                    }
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }
            }

        }catch (e: IOException){
            result = Result.Error(e)
            Log.e("ModelRepository","IOException ${e.message}")
            Log.e("ModelRepository","IOException ${e.localizedMessage}")

        }
        return result
    }

    override suspend fun getDailyPreparation(): Result<TodayDailyPrapration?> {
        var result: Result<TodayDailyPrapration?> = Result.Loading
        try {
            Log.i("ModelRepository:@@", "Token ${getToken()}")

            val response = apiDataSource.getDailyPreparation("Bearer "+getToken())
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
                        result = Result.Error(Exception("Not Auth please, logout and login again"))
                        if (isLogin()) {
                            Log.i(
                                "Model Repo:",
                                "isLogin:" + isLogin() + ", token:" + getToken() + ",  refresh token:" + getRefreshToken()
                            )
                            refreshToken(RefreshTokenRequest(getToken(), getRefreshToken()))
                        }
                    }
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }
            }

        }catch (e: IOException){
            result = Result.Error(e)
            Log.e("ModelRepository","IOException ${e.message}")
            Log.e("ModelRepository","IOException ${e.localizedMessage}")

        }
        return result
    }
    override suspend fun editDailyPreparation(
        WorkersTypesList: Map<Long, Int>,
        equipmentList: Map<Long, Int>
    ): Result<Void?> {
        var result: Result<Void?> = Result.Loading
        try {

            val response = apiDataSource.editDailyPreparation("Bearer ${getToken()}",WorkersTypesList,equipmentList)
            if (response.isSuccessful) {
                result = Result.Success(response.body())
                Log.i("ModelRepositoryForm", "Result $result")
            } else {
                Log.i("ModelRepositoryForm", "Error${response.errorBody()}")
                when (response.code()) {
                    400 -> {
                        Log.e("Error 400", "Bad Request")
                        Log.e("Error 400", "editDailyPreparation: "+response.errorBody()?.string())
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
                        result = Result.Error(Exception("Not Auth please, logout and login again"))
                        if (isLogin()) {
                            Log.i(
                                "Model Repo:",
                                "isLogin:" + isLogin() + ", token:" + getToken() + ",  refresh token:" + getRefreshToken()
                            )
                            refreshToken(RefreshTokenRequest(getToken(), getRefreshToken()))
                        }
                    }
                    else -> {
                        Log.e("Error", "Generic Error")
                    }
                }

            }

        }catch (e: IOException){
            result = Result.Error(e)
            Log.e("ModelRepository","IOException ${e.message}")
            Log.e("ModelRepository","IOException ${e.localizedMessage}")

        }
        return result
    }

    override fun getAllDailyPreparation(): Flow<List<DailyPreparation>> {
      return localDataSource.getAllData()
    }

    override fun getDailyPreparation_By_ServiceTypeID(serviceTypeID: String,date :String): DailyPreparation? {
       return localDataSource.getDailyPreparation_By_ServiceTypeID(serviceTypeID,date)
    }

    override  fun insertDailyPreparation(dailyPreparation: DailyPreparation) {
      localDataSource.insertDailyPreparation(dailyPreparation)
    }

    override  fun deleteAllDailyPreparation() {
     localDataSource.deleteAll()
    }

    override fun delete_By_ServiceTypeID(serviceTypeID: String,date :String) {
        localDataSource.deleteBy_ServiceTypeID(serviceTypeID,date)
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