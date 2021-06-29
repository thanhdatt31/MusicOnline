package com.example.musiconline.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.musiconline.model.Song
import com.example.musiconline.repository.RoomRepository
import kotlinx.coroutines.launch

class RoomViewModel(
    app: Application,
    private val roomRepository: RoomRepository
) : AndroidViewModel(app) {
    fun insertSong(context: Context, song: Song) {
        roomRepository.insertSong(context, song)
    }

    fun getFavoriteListSong(context: Context): MutableLiveData<List<Song>> {
        val liveData: MutableLiveData<List<Song>> = MutableLiveData()
        viewModelScope.launch {
            liveData.postValue(roomRepository.getFavoriteListSong(context))
        }
        return liveData
    }

}