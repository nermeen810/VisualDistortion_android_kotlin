package com.rino.visualdestortion.ui.editDailyPreparation

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rino.visualdestortion.model.pojo.dailyPraperation.TodayDailyPrapration
import com.rino.visualdestortion.model.remoteDataSource.Result
import com.rino.visualdestortion.model.reposatory.ModelRepo
import com.rino.visualdestortion.ui.dailyPreparation.EquipmentItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditDailyPViewModel (application: Application) : AndroidViewModel(application) {
    private val modelRepository: ModelRepo = ModelRepo(application)

    private var _setError = MutableLiveData<String>()
    private var _loading = MutableLiveData<Int>(View.GONE)
    //   private var _getServicesData = MutableLiveData<AddServiceResponse>()
    private var _getDailyPreparation = MutableLiveData<TodayDailyPrapration?>()
    private var _equipmentsDeleteItem = MutableLiveData<EquipmentItem>()
    private var _workerTypeDeleteItem = MutableLiveData<EquipmentItem>()

    val loading: LiveData<Int>
        get() = _loading

    val setError: LiveData<String>
        get() = _setError

//    val getServicesData: LiveData<AddServiceResponse>
//        get() = _getServicesData

    val getDailyPreparation: MutableLiveData<TodayDailyPrapration?>
        get() = _getDailyPreparation

    val equipmentsDeleteItem: LiveData<EquipmentItem>
        get() = _equipmentsDeleteItem

    val workerTypeDeleteItem: LiveData<EquipmentItem>
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
    fun setEquipmentDeletedItem(equipmentItem: EquipmentItem) {
        _equipmentsDeleteItem.value = equipmentItem
    }

    fun setWorkerTypeDeletedItem(workerTypeItem: EquipmentItem) {
        _workerTypeDeleteItem.value = workerTypeItem
    }
}