package com.rino.visualdestortion.model.pojo.history

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("id") var id: String? = null,
    @SerializedName("serviceNumber") var serviceNumber: Int? = null,
    @SerializedName("notes") var notes: String? = null,
    @SerializedName("createdDate") var createdDate: String? = null,
    @SerializedName("beforeImg") var beforeImg: String? = null,
    @SerializedName("duringImg") var duringImg: String? = null,
    @SerializedName("afterImg") var afterImg: String? = null,
    @SerializedName("longtitude") var longtitude: String? = null,
    @SerializedName("latitude") var latitude: String? = null,
    @SerializedName("fullLocation") var fullLocation: String? = null,
    @SerializedName("qrCodeImg") var qrCodeImg: String? = null,
    @SerializedName("percentage") var percentage: Int? = null,
    @SerializedName("sqrd") var sqrd: String? = null,
    @SerializedName("replyCount") var replyCount: String? = null,
    @SerializedName("quantityCubed") var quantityCubed: String? = null,
    @SerializedName("equipmentList") var equipmentList: ArrayList<EquipmentsList> = arrayListOf(),
    @SerializedName("workerstList") var workerstList: ArrayList<WorkertList> = arrayListOf()
)

data class EquipmentsList(
    @SerializedName("name") var name: String? = null,
    @SerializedName("count") var count: Int? = null,
    @SerializedName("status") var status: Boolean? = null
)

data class WorkertList(

    @SerializedName("title") var title: String? = null,
    @SerializedName("count") var count: Int? = null,
    @SerializedName("status") var status: Boolean? = null

)