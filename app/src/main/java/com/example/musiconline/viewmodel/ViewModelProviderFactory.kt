package com.example.musiconline.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musiconline.repository.MainRepo

class ViewModelProviderFactory(
    val app: Application,
    val mainRepo: MainRepo
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(app, mainRepo) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }

}