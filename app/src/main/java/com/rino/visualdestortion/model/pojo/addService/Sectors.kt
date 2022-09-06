package com.rino.visualdestortion.model.pojo.addService

import com.google.gson.annotations.SerializedName

data class Sectors(
    @SerializedName("id") var id: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("serviceMaps") var serviceMaps: String? = null,
    @SerializedName("municipalites") var municipalites: ArrayList<Municipalites> = arrayListOf()
)
