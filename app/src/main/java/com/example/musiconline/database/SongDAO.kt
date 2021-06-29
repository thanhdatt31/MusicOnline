package com.example.musiconline.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.example.musiconline.model.Song

@Dao
interface SongDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)

    @Delete
    suspend fun deleteSong(song: Song)

    @Query("SELECT * FROM song ORDER BY favor_id DESC")
    suspend fun getAllSong(): List<Song>
}