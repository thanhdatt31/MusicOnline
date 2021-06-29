package com.example.musiconline.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.musiconline.model.Song
import com.example.musiconline.ulti.UriConverters

@Database(entities = [Song::class], version = 1, exportSchema = false)
@TypeConverters(UriConverters::class)
abstract class SongDatabase : RoomDatabase() {
    companion object {
        var songDatabase: SongDatabase? = null
        fun getDatabase(context: Context): SongDatabase {
            if (songDatabase == null) {
                songDatabase = Room.databaseBuilder(
                    context.applicationContext,
                    SongDatabase::class.java,
                    "favor_song.db"
                )
                    .build()
            }
            return songDatabase!!
        }
    }

    abstract fun songDAO(): SongDAO
}