package com.example.musiconline.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.musiconline.model.TopSong
import com.example.musiconline.repository.MainRepo
import com.example.musiconline.ulti.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class MainViewModel(
    app: Application,
    private val mainRepo: MainRepo
) : AndroidViewModel(app) {
    val topSongData: MutableLiveData<Resource<TopSong>> = MutableLiveData()

    init {
        getTopSong()
    }

    private fun getTopSong() = viewModelScope.launch {
        fetchTopSong()
    }

    private suspend fun fetchTopSong() {
        topSongData.postValue(Resource.Loading())
        val response = mainRepo.getListTopSong()
        topSongData.postValue(handleTopSongResponse(response))
    }

    private fun handleTopSongResponse(response: Response<TopSong>): Resource<TopSong> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
    }
}
