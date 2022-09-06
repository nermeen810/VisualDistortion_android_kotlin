package com.rino.visualdestortion.ui.error

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rino.visualdestortion.model.remoteDataSource.Result
import com.rino.visualdestortion.model.reposatory.ModelRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ErrorViewModel(application: Application) : AndroidViewModel(application) {
    private val modelRepository: ModelRepo = ModelRepo(application)
    private val _isPrepared = MutableLiveData<Boolean>()
    private var _setError = MutableLiveData<String>()
    private var _loading = MutableLiveData<Int>(View.GONE)

    val isPrepared: LiveData<Boolean>
        get() = _isPrepared

    val loading: LiveData<Int>
        get() = _loading

    val setError: LiveData<String>
        get() = _setError

    fun isTodayPrepared() {

        viewModelScope.launch(Dispatchers.IO) {
            when (val result = modelRepository.isDailyPrepared()) {
                is Result.Success -> {
                    //  _loading.postValue(View.GONE)
                    Log.i("login:", "${result.data}")
                    if (result.data?.isPrepared == true) {
                        withContext(Dispatchers.Main) {
                            _isPrepared.postValue(true)
                            Log.i("isPrepared:", (result.data?.isPrepared ?: true).toString())
                        }
                    } else {
                        _isPrepared.postValue(false)
                    }
                }
                is Result.Error -> {
                    Log.e("isDailyPrepared:", "${result.exception.message}")
                    //  _loading.postValue(View.GONE)
                    _setError.postValue(result.exception.message)


                }
                is Result.Loading -> {
                    Log.i("isDailyPrepared", "Loading")
                    //    _loading.postValue(View.VISIBLE)
                }
            }
        }
    }

    fun isLogin(): Boolean {
        return modelRepository.isLogin()
    }
}