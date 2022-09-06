package com.rino.visualdestortion.model.pojo.resetPassword

import com.google.gson.annotations.SerializedName

data class RequestOTP(@SerializedName("Email") var email: String? = null)
