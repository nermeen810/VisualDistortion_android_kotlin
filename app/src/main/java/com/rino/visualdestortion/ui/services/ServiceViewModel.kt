package com.rino.visualdestortion.ui.services

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rino.visualdestortion.model.localDataSource.room.DailyPreparation
import com.rino.visualdestortion.model.pojo.addService.AddServiceResponse
import com.rino.visualdestortion.model.pojo.home.HomeServicesResponse
import com.rino.visualdestortion.model.pojo.home.ServiceTypes
import com.rino.visualdestortion.model.remoteDataSource.Result
import com.rino.visualdestortion.model.reposatory.ModelRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ServiceViewModel(application: Application) : AndroidViewModel(application) {
    private val modelRepository: ModelRepo = ModelRepo(application)
    private var _navToAddService: MutableLiveData<ServiceTypes> = MutableLiveData()
    private var _navToDailyPreparation: MutableLiveData<ServiceTypes> = MutableLiveData()
    private var _setError = MutableLiveData<String>()
    private var _loading = MutableLiveData<Int>(View.GONE)
    private var _getServicesData = MutableLiveData<HomeServicesResponse>()
    private var _getDailyPreparation = MutableLiveData<DailyPreparation?>()

    val navToAddService: LiveData<ServiceTypes>
        get() = _navToAddService

    val navToDailyPreparation: LiveData<ServiceTypes>
        get() = _navToDailyPreparation

    val loading: LiveData<Int>
        get() = _loading

    val setError: LiveData<String>
        get() = _setError

    val getServicesData: LiveData<HomeServicesResponse>
        get() = _getServicesData

    val getDailyPreparation: LiveData<DailyPreparation?>
        get() = _getDailyPreparation

    fun navToAddService(service: ServiceTypes) {
        _navToAddService.value = service
    }

    fun navToDailyPreparation(service: ServiceTypes) {
        _navToDailyPreparation.value = service
    }

    fun getDailyPreparationByServiceID(serviceTypeID: String, date: String) {

        viewModelScope.launch(Dispatchers.IO) {
            val dailyPreparation =
                modelRepository.getDailyPreparation_By_ServiceTypeID(serviceTypeID, date)
            Log.e("getDailyPreparation_By_ServiceTypeID", dailyPreparation.toString())
            _getDailyPreparation.postValue(dailyPreparation)
        }
    }


    fun getServicesData() {
        _loading.postValue(View.VISIBLE)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = modelRepository.getHomeData()) {
                is Result.Success -> {
                    // _loading.postValue(View.GONE)
                    Log.i("getServiceData:", "${result.data}")
                    _getServicesData.postValue(result.data!!)
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

}