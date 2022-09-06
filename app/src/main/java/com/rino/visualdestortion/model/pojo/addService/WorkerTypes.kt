package com.rino.visualdestortion.model.pojo.addService

import com.google.gson.annotations.SerializedName

data class WorkerTypes(
    @SerializedName("id") var id: Int? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("formWorkers") var formWorkers: String? = null
)
