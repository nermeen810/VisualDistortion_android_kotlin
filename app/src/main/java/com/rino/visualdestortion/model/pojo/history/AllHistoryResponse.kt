package com.rino.visualdestortion.model.pojo.history

import com.google.gson.annotations.SerializedName

data class AllHistoryResponse(
    @SerializedName("data") var data: ArrayList<ServiceTypeData>? = arrayListOf(),
    @SerializedName("dateTime") var dateTime: String?
)

data class ServiceTypeData(
    @SerializedName("serviceId") var serviceId: Int? = null,
    @SerializedName("serviceName") var serviceName: String? = null,
    @SerializedName("numberOfTasks") var numberOfTasks: Int? = null,
    @SerializedName("dateFrom") var dateFrom: String? = null,
    @SerializedName("dateTo") var dateTo: String? = null,
    @SerializedName("numberOfDays") var numberOfDays: Int? = null
)