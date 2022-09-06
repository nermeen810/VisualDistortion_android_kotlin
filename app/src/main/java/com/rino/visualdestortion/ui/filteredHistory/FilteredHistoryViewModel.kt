package com.rino.visualdestortion.ui.filteredHistory

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rino.visualdestortion.model.pojo.history.*
import com.rino.visualdestortion.model.remoteDataSource.Result
import com.rino.visualdestortion.model.reposatory.ModelRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FilteredHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val modelRepository: ModelRepo = ModelRepo(application)
    private var _setError = MutableLiveData<String>()
    private var _loading = MutableLiveData<Int>(View.GONE)
    private var _getHistoryData = MutableLiveData<FilteredHistoryResponse?>()
    private var _getSearchHistoryData = MutableLiveData<SearchResponse?>()
    private var _navToSeeAll: MutableLiveData<String> = MutableLiveData()
    private var _navToTaskDetails: MutableLiveData<ServiceData> = MutableLiveData()
    var serviceId = 0

    companion object {
        var periodTimeList_en =
            arrayListOf("lastyear", "year", "lastmonth", "month", "lastweek", "week", "all")
        var lastSelectedPos = periodTimeList_en.size - 1
    }

    val navToTaskDetails: LiveData<ServiceData>
        get() = _navToTaskDetails

    val navToSeeAll: LiveData<String>
        get() = _navToSeeAll

    val loading: LiveData<Int>
        get() = _loading

    val setError: LiveData<String>
        get() = _setError

    val getSearchHistoryData: LiveData<SearchResponse?>
        get() = _getSearchHistoryData

    val getHistoryData: LiveData<FilteredHistoryResponse?>
        get() = _getHistoryData

    fun navToSeeAll(period: String) {
        _navToSeeAll.value = period
    }

    fun navToServiceDetails(item: ServiceData) {
        _navToTaskDetails.value = item
    }

    fun getHistoryData(serviceID: Int, period: String = "all") {
        _loading.postValue(View.VISIBLE)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = modelRepository.getFilteredHistory(serviceID, period)) {
                is Result.Success -> {
                    // _loading.postValue(View.GONE)
                    Log.i("getHistoryData:", "${result.data}")
                    _getHistoryData.postValue(result.data)
                    _loading.postValue(View.GONE)

                }
                is Result.Error -> {
                    Log.e("getHistoryData:", "${result.exception.message}")
                    _setError.postValue(result.exception.message)
                    _loading.postValue(View.GONE)

                }
                is Result.Loading -> {
                    Log.i("getHistoryData", "Loading")
                    _loading.postValue(View.VISIBLE)
                }
            }
        }
    }

    fun searchHistoryDataByService(searchRequest: SearchRequest) {
        _loading.postValue(View.VISIBLE)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = modelRepository.searchHistoryDataByService(searchRequest)) {
                is Result.Success -> {
                    // _loading.postValue(View.GONE)
                    Log.i("searchHistoryDataByService:", "${result.data}")
                    _getSearchHistoryData.postValue(result.data)
                    _loading.postValue(View.GONE)

                }
                is Result.Error -> {
                    Log.e("searchHistoryDataByService:", "${result.exception.message}")
                    _setError.postValue(result.exception.message)
                    _loading.postValue(View.GONE)

                }
                is Result.Loading -> {
                    Log.i("searchHistoryDataByService", "Loading")
                    _loading.postValue(View.VISIBLE)
                }
            }
        }

    }


}