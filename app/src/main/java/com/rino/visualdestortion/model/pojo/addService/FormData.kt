package com.rino.visualdestortion.model.pojo.addService

import android.graphics.Bitmap
import okhttp3.MultipartBody
import retrofit2.http.Field

 class FormData(){
   lateinit var serviceTypeId: String
   lateinit var sectorName: String
   lateinit var municipalityName: String
   lateinit var districtName: String
   lateinit var streetName: String
   lateinit var lat: String
   lateinit var lng: String
   var beforeImg: MultipartBody.Part? = null
   var afterImg: MultipartBody.Part? = null
   lateinit var WorkersTypesList: Map<Long,Int>
   lateinit var equipmentList: Map<Long,Int>
     var mSquare: Int? = null
     var mCube: Int? = null
     var numberR: Int? = null
     var notes: String? = null
   lateinit var percentage: String

 }
