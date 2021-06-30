package com.example.musiconline.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SearchResult(
    @SerializedName("data")
    @Expose
    var data: List<DataSearch>
)