package com.rino.visualdestortion.ui.login

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rino.visualdestortion.model.pojo.dailyPraperation.CheckDailyPreparationResponse
import com.rino.visualdestortion.model.pojo.login.LoginRequest
import com.rino.visualdestortion.model.remoteDataSource.Result
import com.rino.visualdestortion.model.reposatory.ModelRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val modelRepository: ModelRepo = ModelRepo(application)

    private var _setError = MutableLiveData<String>()
    private var _loading = MutableLiveData<Int>(View.GONE)
    private val _isLogin = MutableLiveData<Boolean>()
    private val _isPrepared = MutableLiveData<Boolean>()

    val isLogin: LiveData<Boolean>
        get() = _isLogin

    val isPrepared: LiveData<Boolean>
        get() = _isPrepared

    val loading: LiveData<Int>
        get() = _loading

    val setError: LiveData<String>
        get() = _setError


    fun login(loginRequest: LoginRequest?) {
        _loading.postValue(View.VISIBLE)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = loginRequest?.let { modelRepository.login(it) }) {
                is Result.Success -> {
                    _loading.postValue(View.GONE)
                    // isTodayPrepared()
                    Log.i("login:", "${result.data}")
                    if (result.data?.status == "Authenticated") {
                        withContext(Dispatchers.Main) {
                            _isLogin.postValue(true)
                            modelRepository.setLogin(true)
                            loginRequest.email?.let { modelRepository.setEmail(it) }
                            loginRequest.password?.let { modelRepository.setPass(it) }
                            result.data.token?.let { modelRepository.setToken(it) }
                            result.data.refreshToken?.let { modelRepository.setRefreshToken(it) }
                            Log.i("login:", "valid email")

                        }
                    }
                }
                is Result.Error -> {
                    Log.e("login:", "${result.exception.message}")
                    _loading.postValue(View.GONE)
                    _setError.postValue(result.exception.message)


                }
                is Result.Loading -> {
                    Log.i("login", "Loading")
                    _loading.postValue(View.VISIBLE)
                }
            }
        }

    }

    fun isTodayPrepared() {
        _loading.postValue(View.VISIBLE)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = modelRepository.isDailyPrepared()) {
                is Result.Success -> {
                    _loading.postValue(View.GONE)
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
                    Log.e("login:", "${result.exception.message}")
                    _loading.postValue(View.GONE)
                    _setError.postValue(result.exception.message)


                }
                is Result.Loading -> {
                    Log.i("login", "Loading")
                    _loading.postValue(View.VISIBLE)
                }
            }
        }
    }

}
