package com.rino.visualdestortion.model.pojo.resetPassword

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequest(
    @SerializedName("Email") var email: String? = null,
    @SerializedName("OTP") var otp: String? = null,
    @SerializedName("NewPassword") var newPassword: String? = null
)
