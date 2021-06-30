package com.example.musiconline.network

import com.example.musiconline.ulti.Const.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object {
        private val retrofitMp3 by lazy {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }
        val mp3Api by lazy {
            retrofitMp3.create(API::class.java)
        }
        private val retrofitSearchMp3 by lazy {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            Retrofit.Builder()
                .baseUrl("http://ac.mp3.zing.vn/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }
        val mp3ApiSearch by lazy {
            retrofitSearchMp3.create(APISearch::class.java)
        }
    }


}