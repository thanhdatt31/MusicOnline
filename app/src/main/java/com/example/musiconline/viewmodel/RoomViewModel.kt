package com.example.musiconline.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
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

    fun deleteSong(context: Context, id: String) {
        roomRepository.deleteSong(context, id)
    }

    fun deleteSpecificSongByUri(context: Context, uri: String) {
        roomRepository.deleteSongByUri(context, uri)
    }

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
}
