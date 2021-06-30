package com.example.musiconline.repository

import android.content.Context
import com.example.musiconline.database.SongDatabase
import com.example.musiconline.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoomRepository {

    var songDatabase: SongDatabase? = null
    private fun initializeDB(context: Context): SongDatabase {
        return SongDatabase.getDatabase(context)
    }

    fun insertSong(context: Context, song: Song) {
        songDatabase = initializeDB(context)
        CoroutineScope(Dispatchers.IO).launch {
            songDatabase!!.songDAO().insertSong(song)
        }
    }
    fun deleteSongByUri(context: Context ,uri : String) {
        songDatabase = initializeDB(context)
        CoroutineScope(Dispatchers.IO).launch {
            songDatabase!!.songDAO().deleteSpecificSongByUri(uri)
        }
    }
    fun deleteSong(context: Context, id : String) {
        songDatabase = initializeDB(context)
        CoroutineScope(Dispatchers.IO).launch {
            songDatabase!!.songDAO().deleteSpecificSong(id)
        }
    }

    suspend fun getFavoriteListSong(context: Context): List<Song> {
        songDatabase = initializeDB(context)
        return songDatabase!!.songDAO().getAllSong()
    }

}