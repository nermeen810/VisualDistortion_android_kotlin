package com.rino.visualdestortion.ui.history

import android.app.Application
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rino.visualdestortion.model.localDataSource.room.DailyPreparation
import com.rino.visualdestortion.model.pojo.history.AllHistoryResponse
import com.rino.visualdestortion.model.pojo.history.Data
import com.rino.visualdestortion.model.pojo.home.HomeServicesResponse
import com.rino.visualdestortion.model.pojo.home.ServiceTypes
import com.rino.visualdestortion.model.remoteDataSource.Result
import com.rino.visualdestortion.model.reposatory.ModelRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val modelRepository: ModelRepo = ModelRepo(application)
    private var _setError = MutableLiveData<String>()
    private var _loading = MutableLiveData<Int>(View.GONE)
    private var _getHistoryData = MutableLiveData<AllHistoryResponse?>()
    private var _navToHistoryOfTask = MutableLiveData<Int>()

    val navToHistoryOfTask: LiveData<Int>
        get() = _navToHistoryOfTask

    val loading: LiveData<Int>
        get() = _loading

    val setError: LiveData<String>
        get() = _setError

    val getHistoryData: LiveData<AllHistoryResponse?>
        get() = _getHistoryData


    fun getHistoryData() {
        _loading.postValue(View.VISIBLE)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = modelRepository.getHistoryData()) {
                is Result.Success -> {
                    _loading.postValue(View.GONE)
                    Log.i("getServiceData:", "${result.data}")
                    _getHistoryData.postValue(result.data)
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

    fun navToHistoryById(serviceID: Int) {
        _navToHistoryOfTask.value = serviceID
    }
}