package com.example.musiconline.network

import com.example.musiconline.model.TopSong
import retrofit2.Response
import retrofit2.http.GET

interface API {
    @GET("chart-realtime?songId=0&videoId=0&albumId=0&chart=song&time=-1")
    suspend fun fetchTopSong(): Response<TopSong>
}