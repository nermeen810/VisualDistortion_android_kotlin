package com.rino.visualdestortion.model.pojo.history

import com.google.gson.annotations.SerializedName

data class HistoryByServiceIdResponse(
    @SerializedName("index") var index: Int? = null,
    @SerializedName("totalPages") var totalPages: Int? = null,
    @SerializedName("hasPreviousPage") var hasPreviousPage: Boolean? = null,
    @SerializedName("hasNextPage") var hasNextPage: Boolean? = null,
    @SerializedName("data") var data: ArrayList<ServiceData> = arrayListOf()
)

data class EquipmentList(
    @SerializedName("name") var name: String? = null,
    @SerializedName("count") var count: Int? = null
)

data class WorkerstList(
    @SerializedName("title") var title: String? = null,
    @SerializedName("count") var count: Int? = null
)


