package com.rino.visualdestortion.model.pojo.addService

import com.google.gson.annotations.SerializedName

data class Equipment(

    @SerializedName("id") var id: Int? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("formEquipments") var formEquipments: String? = null
)
