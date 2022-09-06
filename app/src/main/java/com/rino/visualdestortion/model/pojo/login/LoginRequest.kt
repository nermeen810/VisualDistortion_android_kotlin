package com.rino.visualdestortion.model.pojo.login

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("Email") var email: String? = null,
    @SerializedName("Password") var password: String? = null
)