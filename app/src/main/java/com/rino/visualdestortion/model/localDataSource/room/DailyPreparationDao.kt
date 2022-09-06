package com.rino.visualdestortion.model.localDataSource.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyPreparationDao {

    @Query("SELECT * FROM DailyPreparation")
    fun getAllData(): Flow<List<DailyPreparation>>

    @Query("SELECT * FROM DailyPreparation WHERE serviceTypeID=:serviceTypeID and date=:date")
    fun getDailyPreparation_By_ServiceTypeID(serviceTypeID: String, date: String): DailyPreparation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDailyPreparation(dailyPreparation: DailyPreparation)

    @Query("DELETE FROM DailyPreparation")
    fun deleteAll()

    @Query("DELETE FROM DailyPreparation WHERE serviceTypeID=:serviceTypeID and date=:date")
    fun deleteBy_ServiceTypeID(serviceTypeID: String, date: String)

}