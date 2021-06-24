package com.example.musiconline.service

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.musiconline.model.Song

class MyService : Service() {
    private val mIBinder = BinderAudio()
    private var mAudioList: ArrayList<Song> = arrayListOf()
    private var mPosition = 0
    private var musicPlayer: MediaPlayer = MediaPlayer()
    inner class BinderAudio : Binder() {
        fun getService(): MyService = this@MyService
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mIBinder
    }

    fun getListAudioAndPosition(data: ArrayList<Song>, position: Int) {
        if (mAudioList.isNotEmpty()) {
            mAudioList.clear()
            mAudioList.addAll(data)
        } else {
            mAudioList.addAll(data)
        }
        mPosition = position
//        Log.d("datnt", "onStartCommand: ")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("datnt", "onStartCommand: ")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.d("datnt", "onDestroy: ")
        super.onDestroy()
    }

    fun playAudio() {
            if(checkPositionAndList()){
                musicPlayer.reset()
                musicPlayer.setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
                musicPlayer.setDataSource("http://api.mp3.zing.vn/api/streaming/audio/ZWBIF86E/320")
                musicPlayer.prepare()
                musicPlayer.start()
        }

    }

    private fun checkPositionAndList(): Boolean {
        return mPosition != 0 && mAudioList.size != 0
    }
}