package com.rino.visualdestortion.ui.editDailyPreparation

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rino.visualdestortion.model.localDataSource.room.DailyPreparation
import com.rino.visualdestortion.model.pojo.dailyPraperation.PrepEquipments
import com.rino.visualdestortion.model.pojo.dailyPraperation.PrepWorkers
import com.rino.visualdestortion.model.pojo.dailyPraperation.TodayDailyPrapration
import com.rino.visualdestortion.model.remoteDataSource.Result
import com.rino.visualdestortion.model.reposatory.ModelRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditDailyPViewModel(application: Application) : AndroidViewModel(application) {
    private val modelRepository: ModelRepo = ModelRepo(application)

    private var _setError = MutableLiveData<String>()
    private var _loading = MutableLiveData(View.GONE)
    private var _getDailyPreparation = MutableLiveData<TodayDailyPrapration?>()
    private var _editDailyPreparation = MutableLiveData<Boolean>()
    private var _equipmentsDeleteItem = MutableLiveData<PrepEquipments>()
    private var _workerTypeDeleteItem = MutableLiveData<PrepWorkers>()

    val loading: LiveData<Int>
        get() = _loading

    val setError: LiveData<String>
        get() = _setError

    val getDailyPreparation: MutableLiveData<TodayDailyPrapration?>
        get() = _getDailyPreparation

    val editDailyPreparation: MutableLiveData<Boolean>
        get() = _editDailyPreparation

    val equipmentsDeleteItem: LiveData<PrepEquipments>
        get() = _equipmentsDeleteItem

    val workerTypeDeleteItem: LiveData<PrepWorkers>
        get() = _workerTypeDeleteItem

    fun getTodayPreparation() {
        _loading.postValue(View.VISIBLE)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = modelRepository.getDailyPreparation()) {
                is Result.Success -> {
                    _loading.postValue(View.GONE)
                    Log.i("getTodayPreparation:", "${result.data}")
                    _getDailyPreparation.postValue(result.data)
                    _loading.postValue(View.GONE)

                }
                is Result.Error -> {
                    Log.e("getServiceData:", "${result.exception.message}")
                    _setError.postValue(result.exception.message)
                    _loading.postValue(View.GONE)

                }
                is Result.Loading -> {
                    Log.i("getServiceData", "Loading")
                    _loading.postValue(View.VISIBLE)
                }
            }
        }
    }

    fun editDailyPreparation(
        WorkersTypesList: Map<Long, Int>,
        equipmentList: Map<Long, Int>
    ) {
        _loading.postValue(View.VISIBLE)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result =
                modelRepository.editDailyPreparation(WorkersTypesList, equipmentList)) {
                is Result.Success -> {
                    _loading.postValue(View.GONE)
                    Log.i("editDailyPreparation:", "${result.data}")
                    _editDailyPreparation.postValue(true)
                    _loading.postValue(View.GONE)

                }
                is Result.Error -> {
                    Log.e("editDailyPreparation:", "${result.exception.message}")
                    _editDailyPreparation.postValue(false)
                    _setError.postValue(result.exception.message)
                    _loading.postValue(View.GONE)

                }
                is Result.Loading -> {
                    Log.i("editDailyPreparation", "Loading")
                    _loading.postValue(View.VISIBLE)
                }
            }
        }

    }

    fun addDailyPreparation(dailyPreparation: DailyPreparation) {
        viewModelScope.launch(Dispatchers.IO) {
            modelRepository.insertDailyPreparation(dailyPreparation)
        }
    }

    fun getDailyPreparationByServiceID(serviceTypeID: String, date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            modelRepository.getDailyPreparation_By_ServiceTypeID(serviceTypeID, date)
        }
    }

    fun setEquipmentDeletedItem(equipmentItem: PrepEquipments) {
        _equipmentsDeleteItem.value = equipmentItem
    }

    fun setWorkerTypeDeletedItem(workerTypeItem: PrepWorkers) {
        _workerTypeDeleteItem.value = workerTypeItem
    }
}