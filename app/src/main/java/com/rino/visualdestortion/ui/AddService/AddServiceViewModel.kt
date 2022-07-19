package com.rino.visualdestortion.ui.AddService

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rino.visualdestortion.model.localDataSource.room.DailyPreparation
import com.rino.visualdestortion.model.pojo.addService.AddServiceResponse
import com.rino.visualdestortion.model.pojo.addService.Districts
import com.rino.visualdestortion.model.pojo.addService.FormData
import com.rino.visualdestortion.model.pojo.addService.QRCode
import com.rino.visualdestortion.model.pojo.login.LoginRequest
import com.rino.visualdestortion.model.remoteDataSource.Result
import com.rino.visualdestortion.model.reposatory.ModelRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddServiceViewModel(application: Application) : AndroidViewModel(application) {
    private val modelRepository: ModelRepo = ModelRepo(application)

    private var _setError = MutableLiveData<String>()
    private var _loading = MutableLiveData<Int>(View.GONE)
    private var _selectedStreet = MutableLiveData<String>()
    private var _selectedDistrict = MutableLiveData<Districts>()
    private var _isSelectedStreet = MutableLiveData<Boolean>()
    private var _isSelectedDistrict = MutableLiveData<Boolean>()
    private var _getDailyPreparation = MutableLiveData<DailyPreparation?>()
    private var _getServicesData = MutableLiveData<AddServiceResponse>()
    private var _setServiceForm = MutableLiveData<QRCode?>()



    val loading: LiveData<Int>
        get() = _loading

    val setError: LiveData<String>
        get() = _setError

    val selectedDistrict: LiveData<Districts>
        get() = _selectedDistrict

    val selectedStreet: LiveData<String>
        get() = _selectedStreet

    val isSelectedDistrict: LiveData<Boolean>
        get() = _isSelectedDistrict

    val isSelectedStreet: LiveData<Boolean>
        get() = _isSelectedStreet

    val getServicesData: LiveData<AddServiceResponse>
        get() = _getServicesData

    val setServiceForm: MutableLiveData<QRCode?>
        get() = _setServiceForm

    val getDailyPreparation: LiveData<DailyPreparation?>
        get() = _getDailyPreparation
    var before = false
    var during = false
    var after = false
    fun viewLoading(viewStatus :Int){
        _loading.value = viewStatus
    }

    fun getServicesData() {
        _loading.postValue(View.VISIBLE)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = modelRepository.getServiceForm()) {
                is Result.Success -> {
                    _loading.postValue(View.GONE)
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
    fun selectStreet(street:String){
        _selectedStreet.value = street
    }
    fun selectDistrict(district:Districts){
        _selectedDistrict.value = district
    }
    fun setIsSelectStreet(street:Boolean){
        _isSelectedStreet.value = street
    }
    fun setIsSelectDistrict(district:Boolean){
        _isSelectedDistrict.value = district
    }
    fun setFormData(serviceForm: FormData) {
        _loading.postValue(View.VISIBLE)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = modelRepository.setServiceForm(serviceForm)) {
                is Result.Success -> {
                    _loading.postValue(View.GONE)
                    Log.i("setServiceForm:", "${result.data}")
                    _setServiceForm.postValue(result.data)
                    _loading.postValue(View.GONE)

                }
                is Result.Error -> {
                    Log.e("setServiceForm:", "${result.exception.message}")
                    _setError.postValue(result.exception.message)
                    _loading.postValue(View.GONE)

                }
                is Result.Loading -> {
                    Log.i("setServiceForm", "Loading")
                    _loading.postValue(View.VISIBLE)
                }
            }
        }

    }
   fun addDailyPreparation(dailyPreparation: DailyPreparation){
       viewModelScope.launch(Dispatchers.IO) {
         modelRepository.insertDailyPreparation(dailyPreparation)
       }
   }

    fun getDailyPreparationByServiceID(serviceTypeID: String,date :String) {
    //    _loading.postValue(View.VISIBLE)
        viewModelScope.launch(Dispatchers.IO) {
            val dailyPreparation = modelRepository.getDailyPreparation_By_ServiceTypeID(serviceTypeID,date)
            _getDailyPreparation.postValue(dailyPreparation)
     //       _loading.postValue(View.GONE)
        }
    }

    fun isFirstTimeLaunch(): Boolean {
        return modelRepository.getFirstTimeLaunch()
    }

    fun setFirstTimeLaunch(firstTimeLaunch: Boolean) {
        modelRepository.setFirstTimeLaunch(firstTimeLaunch)
    }
}