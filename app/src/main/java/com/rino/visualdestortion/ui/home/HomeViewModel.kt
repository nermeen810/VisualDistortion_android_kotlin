package com.rino.visualdestortion.ui.home

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rino.visualdestortion.model.reposatory.ModelRepo

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val modelRepository: ModelRepo = ModelRepo(application)

    private var _setError = MutableLiveData<String>()
    private var _loading = MutableLiveData<Int>(View.GONE)

    val loading: LiveData<Int>
        get() = _loading

    val setError: LiveData<String>
        get() = _setError


}