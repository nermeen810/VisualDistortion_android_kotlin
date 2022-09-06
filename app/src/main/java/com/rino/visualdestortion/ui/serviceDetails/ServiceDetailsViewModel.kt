package com.rino.visualdestortion.ui.serviceDetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.rino.visualdestortion.model.reposatory.ModelRepo

class ServiceDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val modelRepository: ModelRepo = ModelRepo(application)


}