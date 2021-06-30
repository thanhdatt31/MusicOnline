package com.example.musiconline.model

import android.net.Uri
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "Song")
data class Song(
    @SerializedName("artists_names")
    @Expose
    val artists_names: String,
    @SerializedName("code")
    @Expose
    @NonNull
    val code: String?,
    @SerializedName("duration")
    @Expose
    @NonNull
    val duration: Int,
    @SerializedName("id")
    @Expose
    @NonNull
    val id: String?,
    @SerializedName("position")
    @Expose
    @NonNull
    val position: Int?,
    @SerializedName("thumbnail")
    @Expose
    @NonNull
    val thumbnail: String?,
    @SerializedName("title")
    @Expose
    val title: String,
    val uri: Uri?,
    @PrimaryKey(autoGenerate = true)
    var favor_id: Int?

)