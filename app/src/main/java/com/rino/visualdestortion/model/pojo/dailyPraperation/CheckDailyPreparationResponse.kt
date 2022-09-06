package com.rino.visualdestortion.model.pojo.dailyPraperation

import com.google.gson.annotations.SerializedName

data class CheckDailyPreparationResponse(
    @SerializedName("isPrepared") var isPrepared: Boolean? = null

)