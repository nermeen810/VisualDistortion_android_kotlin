package com.rino.visualdestortion.model.pojo.addService

import com.google.gson.annotations.SerializedName

data class Municipalites(
    @SerializedName("id") var id: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("districts") var districts: ArrayList<Districts> = arrayListOf(),
    @SerializedName("serviceMaps") var serviceMaps: String? = null
)
