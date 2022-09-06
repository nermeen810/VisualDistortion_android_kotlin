package com.rino.visualdestortion.model.pojo.dailyPraperation

import com.google.gson.annotations.SerializedName

data class GetDailyPraprationData(
    @SerializedName("equipmentList") var equipmentList: ArrayList<EquipmentList> = arrayListOf(),
    @SerializedName("workerTypesList") var workerTypesList: ArrayList<WorkerTypesList> = arrayListOf()
)


data class EquipmentList(

    @SerializedName("name") var name: String? = null,
    @SerializedName("id") var id: Int? = null

)

data class WorkerTypesList(

    @SerializedName("name") var name: String? = null,
    @SerializedName("id") var id: Int? = null

)