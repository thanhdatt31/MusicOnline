package com.example.musiconline.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.musiconline.repository.MainRepository
import com.example.musiconline.repository.RoomRepository

class RoomViewModelProviderFactory(
    val app: Application,
    private val roomRepository: RoomRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomViewModel::class.java)) {
            return RoomViewModel(app, roomRepository) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }

}