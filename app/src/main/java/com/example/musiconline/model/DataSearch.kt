package com.example.musiconline.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class DataSearch(
    @SerializedName("song")
    @Expose
    var song: ArrayList<ResultSong>,
)