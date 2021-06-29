package com.example.musiconline.viewmodel

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.musiconline.model.RecommendSong
import com.example.musiconline.model.Song
import com.example.musiconline.model.TopSong
import com.example.musiconline.repository.MainRepository
import com.example.musiconline.ulti.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class MainViewModel(
    app: Application,
    private val mainRepository: MainRepository
) : AndroidViewModel(app) {
    val topSongData: MutableLiveData<Resource<TopSong>> = MutableLiveData()
    val recommendSongData: MutableLiveData<Resource<RecommendSong>> = MutableLiveData()
    val offlineSongData: MutableLiveData<ArrayList<Song>> = MutableLiveData()
    var listOfflineSong: ArrayList<Song> = arrayListOf()
    var id: String = "ZOA80W7W"

    init {
        getTopSong()
        getRecommendSong()
        getOfflineSong()
    }

    private fun getRecommendSong() = viewModelScope.launch {
        fetchMusicRecommend()
    }

    private suspend fun fetchMusicRecommend() {
        recommendSongData.postValue(Resource.Loading())
        val response = mainRepository.getListRecommend(id)
        recommendSongData.postValue(handleRecommendSongResponse(response))
    }


    private fun getTopSong() = viewModelScope.launch((Dispatchers.IO)) {
        fetchTopSong()
    }

    private fun getOfflineSong() = viewModelScope.launch(Dispatchers.IO) {
        fetchOfflineSong()
    }

    private fun fetchOfflineSong() {
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ARTIST,
        )
        getApplication<Application>().contentResolver.query(
            collection,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val name =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val artist =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                val duration =
                    cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                val song = Song(artist, null, duration, null, null, null, name, contentUri,null)
                listOfflineSong.add(song)
            }
            offlineSongData.postValue(listOfflineSong)
        }

    }

    private suspend fun fetchTopSong() {
        topSongData.postValue(Resource.Loading())
        val response = mainRepository.getListTopSong()
        topSongData.postValue(handleTopSongResponse(response))
    }


    private fun handleRecommendSongResponse(response: Response<RecommendSong>): Resource<RecommendSong> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return Resource.Success(resultResponse)
            }
        }
        return Resource.Error(response.message())
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
