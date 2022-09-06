package com.rino.visualdestortion.model.pojo.dailyPraperation

import com.google.gson.annotations.SerializedName

data class TodayDailyPrapration(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("prepEquipments") var prepEquipments: ArrayList<PrepEquipments> = arrayListOf(),
    @SerializedName("prepWorkers") var prepWorkers: ArrayList<PrepWorkers> = arrayListOf(),
    @SerializedName("equipmentTypes") var equipmentTypes: ArrayList<EquipmentTypes> = arrayListOf(),
    @SerializedName("workerTypes") var workerTypes: ArrayList<WorkerTypes> = arrayListOf()
)

data class PrepEquipments(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name: String,
    @SerializedName("count") var count: Int
)

data class PrepWorkers(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name: String,
    @SerializedName("count") var count: Int
)

data class EquipmentTypes(
    @SerializedName("name") var name: String,
    @SerializedName("id") var id: Int
)

data class WorkerTypes(
    @SerializedName("name") var name: String,
    @SerializedName("id") var id: Int
)