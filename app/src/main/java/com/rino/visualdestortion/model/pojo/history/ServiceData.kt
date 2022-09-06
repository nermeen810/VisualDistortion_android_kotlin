package com.rino.visualdestortion.model.pojo.history

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class ServiceData(

    @SerializedName("id") var id: String? = null,
    @SerializedName("serviceNumber") var serviceNumber: Int? = null,
    @SerializedName("notes") var notes: String? = null,
    @SerializedName("createdDate") var createdDate: String? = null,
    @SerializedName("beforeImg") var beforeImg: String? = null,
    @SerializedName("duringImg") var duringImg: String? = null,
    @SerializedName("afterImg") var afterImg: String? = null,
    @SerializedName("longtitude") var longtitude: String? = null,
    @SerializedName("latitude") var latitude: String? = null,
    @SerializedName("fullLocation") var fullLocation: String? = null,
    @SerializedName("qrCodeImg") var qrCodeImg: String? = null,
    @SerializedName("sqrd") var sqrd: String? = null,
    @SerializedName("replyCount") var replyCount: String? = null,
    @SerializedName("quantityCubed") var quantityCubed: String? = null,
    @SerializedName("equipmentList") var equipmentList: ArrayList<EquipmentList> = arrayListOf(),
    @SerializedName("workerstList") var workerstList: ArrayList<WorkerstList> = arrayListOf()

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readArrayList(null) as ArrayList<EquipmentList>,
        parcel.readArrayList(null) as ArrayList<WorkerstList>
    ) {
    }

    constructor(it: SearchResponse) :
            this(
                it.id,
                it.serviceNumber,
                it.notes,
                it.createdDate,
                it.beforeImg,
                it.duringImg,
                it.afterImg,
                it.longtitude,
                it.latitude,
                it.fullLocation,
                it.qrCodeImg,
                it.sqrd,
                it.replyCount,
                it.quantityCubed,
                arrayListOf(),
                arrayListOf()
            ) {
        for (item in it.equipmentList) {
            this.equipmentList.add(EquipmentList(item.name, item.count))
        }
        for (item in it.workerstList) {
            this.workerstList.add(WorkerstList(item.title, item.count))
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeValue(serviceNumber)
        parcel.writeString(notes)
        parcel.writeString(createdDate)
        parcel.writeString(beforeImg)
        parcel.writeString(duringImg)
        parcel.writeString(afterImg)
        parcel.writeString(fullLocation)
        parcel.writeString(qrCodeImg)
        parcel.writeString(sqrd)
        parcel.writeString(replyCount)
        parcel.writeString(quantityCubed)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ServiceData> {
        override fun createFromParcel(parcel: Parcel): ServiceData {
            return ServiceData(parcel)
        }

        override fun newArray(size: Int): Array<ServiceData?> {
            return arrayOfNulls(size)
        }
    }
}