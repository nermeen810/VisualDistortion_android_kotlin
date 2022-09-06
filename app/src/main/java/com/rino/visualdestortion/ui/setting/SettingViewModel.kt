package com.rino.visualdestortion.ui.setting

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.rino.visualdestortion.model.reposatory.ModelRepo

class SettingViewModel(application: Application) : AndroidViewModel(application) {
    private val modelRepository: ModelRepo = ModelRepo(application)

    fun logout() {
        modelRepository.logout()
    }

}