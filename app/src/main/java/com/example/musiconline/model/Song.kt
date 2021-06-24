package com.example.musiconline.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Song(
    @SerializedName("artists_names")
    @Expose
    val artists_names: String,
    @SerializedName("code")
    @Expose
    val code: String,
    @SerializedName("duration")
    @Expose
    val duration: Int,
    @SerializedName("id")
    @Expose
    val id: String,
    @SerializedName("position")
    @Expose
    val position: Int,
    @SerializedName("thumbnail")
    @Expose
    val thumbnail: String,
    @SerializedName("title")
    @Expose
    val title: String
)