package com.rino.visualdestortion.model.pojo.addService

import com.google.gson.annotations.SerializedName

data class Districts(
    @SerializedName("id") var id: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("streets") var streets: ArrayList<Streets> = arrayListOf(),
    @SerializedName("serviceMaps") var serviceMaps: String? = null
)
