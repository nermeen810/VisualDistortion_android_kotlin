package com.rino.visualdestortion.model.pojo.login

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token") var token: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("refreshToken") var refreshToken: String? = null

)
