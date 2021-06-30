package com.example.musiconline.network

import com.example.musiconline.model.RecommendSong
import com.example.musiconline.model.SearchResult
import com.example.musiconline.model.TopSong
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface APISearch {
    @GET("complete?type=artist,song,key,code&num=500")
    suspend fun fetchSearchResult(@Query("query") query: String): Response<SearchResult>
}