package com.rino.visualdestortion.model.localDataSource.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken

class MapConverter {
    @TypeConverter
    fun stringToMap(value: String): Map<Long, Int> {
        return Gson().fromJson(value, object : TypeToken<Map<Long, Int>>() {}.type)
    }

    @TypeConverter
    fun mapToString(value: Map<Long, Int>?): String {
        return if (value == null) "" else Gson().toJson(value)
    }
}