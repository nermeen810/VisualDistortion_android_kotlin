package com.rino.visualdestortion.model.pojo.login

import com.google.gson.annotations.SerializedName

data class RefreshTokenRequest(
    @SerializedName("Token") var Token: String? = null,
    @SerializedName("RefreshToken") var RefreshToken: String? = null
)