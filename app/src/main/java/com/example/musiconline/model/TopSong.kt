package com.example.musiconline.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TopSong(
    @SerializedName("data")
    @Expose
    var data: Data
)