package com.example.musiconline.ulti

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.musiconline.R
import java.util.concurrent.TimeUnit

object Const {
    const val BASE_URL = "https://mp3.zing.vn/xhr/"
    const val CHANNEL_ID = "music"
    const val MUSIC_NOTIFICATION_ID = 1
    const val SEND_ACTION_FROM_NOTIFICATION = "SEND_ACTION_FROM_NOTIFICATION"
    const val ACTION_PAUSE = 1
    const val ACTION_RESUME = 2
    const val ACTION_CLEAR = 3
    const val ACTION_NEXT = 5
    const val ACTION_PREVIOUS = 6
    const val REFRESH_LIST = "REFRESH_LIST"
    fun durationConverter(duration: Long): String {
        return String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(duration),
            TimeUnit.MILLISECONDS.toSeconds(duration) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        )
    }
    fun getAlbumBitmap(context: Context, audioUri: Uri): Bitmap {
        val mmr = MediaMetadataRetriever()
        val art: Bitmap
        val bfo = BitmapFactory.Options()
        mmr.setDataSource(context, audioUri)
        val rawArt: ByteArray? = mmr.embeddedPicture
        return if (null != rawArt) {
            art = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size, bfo)
            art
        } else {
            BitmapFactory.decodeResource(context.resources, R.drawable.img_beat)
        }
    }
}