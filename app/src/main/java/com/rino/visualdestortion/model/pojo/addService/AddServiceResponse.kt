package com.rino.visualdestortion.model.pojo.addService

import com.google.gson.annotations.SerializedName

data class AddServiceResponse(
    @SerializedName("sectors") var sectors: ArrayList<Sectors>? = arrayListOf(),
    @SerializedName("serviceTypes") var serviceTypes: ArrayList<ServiceTypes> = arrayListOf()

)
