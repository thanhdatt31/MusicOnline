package com.example.musiconline.network

import com.example.musiconline.model.RecommendSong
import com.example.musiconline.model.TopSong
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface API {
    @GET("chart-realtime?songId=0&videoId=0&albumId=0&chart=song&time=-1")
    suspend fun fetchTopSong(): Response<TopSong>

    @GET("recommend?type=audio")
    suspend fun fetchMusicRecommend(@Query("id") id: String): Response<RecommendSong>

}