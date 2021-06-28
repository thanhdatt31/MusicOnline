package com.example.musiconline.repository

import com.example.musiconline.network.RetrofitInstance

class MainRepository {
    suspend fun getListTopSong() = RetrofitInstance.mp3Api.fetchTopSong()
    suspend fun getListRecommend(id : String) = RetrofitInstance.mp3Api.fetchMusicRecommend(id)
}