package com.example.musiconline.repository

import com.example.musiconline.network.RetrofitInstance

class MainRepo {
    suspend fun getListTopSong() = RetrofitInstance.mp3Api.fetchTopSong()
}