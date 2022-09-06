package com.rino.visualdestortion.model.pojo.home

import com.google.gson.annotations.SerializedName

data class HomeServicesResponse(
    @SerializedName("serviceTypes") var serviceTypes: ArrayList<ServiceTypes> = arrayListOf(),
    @SerializedName("permissions") var permissions: ArrayList<Permissions> = arrayListOf()
)
