package com.rino.visualdestortion.model.localDataSource.room

import android.app.Application
import kotlinx.coroutines.flow.Flow

class DailyPreparationDataSource {
    private var dailyPreparationDao: DailyPreparationDao

    constructor(application: Application) {
        dailyPreparationDao = DailyPreparationDB.getDatabase(application).DailyPreparationDao()
    }

    fun getAllData(): Flow<List<DailyPreparation>> {
        return dailyPreparationDao.getAllData()
    }

    fun getDailyPreparation_By_ServiceTypeID(
        serviceTypeID: String,
        date: String
    ): DailyPreparation? {
        return dailyPreparationDao.getDailyPreparation_By_ServiceTypeID(serviceTypeID, date)
    }

    fun insertDailyPreparation(dailyPreparation: DailyPreparation) {
        dailyPreparationDao.insertDailyPreparation(dailyPreparation)
    }

    fun deleteAll() {
        dailyPreparationDao.deleteAll()
    }

    fun deleteBy_ServiceTypeID(serviceTypeID: String, date: String) {
        dailyPreparationDao.deleteBy_ServiceTypeID(serviceTypeID, date)
    }

}