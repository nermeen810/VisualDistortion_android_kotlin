package com.rino.visualdestortion.model.pojo.home

import com.google.gson.annotations.SerializedName

data class Permissions(

    @SerializedName("route") var route: String,
    @SerializedName("actions") var actions: ArrayList<String> = arrayListOf()
)
