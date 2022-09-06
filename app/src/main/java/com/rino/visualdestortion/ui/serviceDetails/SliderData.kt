package com.rino.visualdestortion.ui.serviceDetails

import android.os.Parcel
import android.os.Parcelable

class SliderData(var imgURL: String?, var title: String?) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(p0: Parcel?, p1: Int) {

    }

    companion object CREATOR : Parcelable.Creator<SliderData> {
        override fun createFromParcel(parcel: Parcel): SliderData {
            return SliderData(parcel)
        }

        override fun newArray(size: Int): Array<SliderData?> {
            return arrayOfNulls(size)
        }
    }
}