package com.rino.visualdestortion.model.pojo.history

import com.google.gson.annotations.SerializedName

data class SearchRequest(
    @SerializedName("id") var id: Int,
    @SerializedName("serviceType") var serviceType: Int,
    @SerializedName("period") var period: String? = null
)
//data class SearchRequestWithPeriod(
//    @SerializedName("id"          ) var id          : Int? = null,
//    @SerializedName("serviceType" ) var serviceType : Int? = null,
//    @SerializedName("period"      ) var period : Int? = null
//)