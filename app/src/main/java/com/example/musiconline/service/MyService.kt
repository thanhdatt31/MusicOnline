package com.example.musiconline.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.example.musiconline.R
import com.example.musiconline.model.Song
import com.example.musiconline.ui.MainActivity
import com.example.musiconline.ulti.Const.ACTION_CLEAR
import com.example.musiconline.ulti.Const.ACTION_NEXT
import com.example.musiconline.ulti.Const.ACTION_PAUSE
import com.example.musiconline.ulti.Const.ACTION_PREVIOUS
import com.example.musiconline.ulti.Const.ACTION_RESUME
import com.example.musiconline.ulti.Const.ACTION_START
import com.example.musiconline.ulti.Const.CHANNEL_ID
import com.example.musiconline.ulti.Const.MUSIC_NOTIFICATION_ID
import com.example.musiconline.ulti.Const.SEND_ACTION_FROM_NOTIFICATION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyService : Service() {
    private val mIBinder = BinderAudio()
    private var mAudioList: ArrayList<Song> = arrayListOf()
    private var mPosition = 0
    private var musicPlayer: MediaPlayer = MediaPlayer()
    private lateinit var thumbnail: Bitmap
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
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        registerReceiver(broadcastReceiver, IntentFilter(SEND_ACTION_FROM_NOTIFICATION))
    }

    private fun getBitmap() {
        GlobalScope.launch(Dispatchers.IO) {
            thumbnail = Glide.with(this@MyService)
                .asBitmap()
                .load(mAudioList[mPosition].thumbnail)
                .submit()
                .get()
        }
    }
    fun getPosition() : Int{
        return mPosition
    }
    fun playAudio() {
        if (checkPositionAndList()) {
            getBitmap()
            musicPlayer.reset()
            musicPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            val urlMp3 =
                "http://api.mp3.zing.vn/api/streaming/audio/${mAudioList[mPosition].id}/128"
            musicPlayer.setDataSource(urlMp3)
            musicPlayer.prepare()
            musicPlayer.start()
            showNotification()
            sendDataToActivity(ACTION_START)
        }

    }

    private fun sendDataToActivity(action: Int) {
        val intent = Intent("send_data_to_activity")
        val bundle = Bundle()
        bundle.putInt("action", action)
        intent.putExtras(bundle)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    fun getStatusPlayer(): Boolean {
        return musicPlayer.isPlaying
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "My Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        serviceChannel.setSound(null, null)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    private fun showNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val mediaSessionCompat = MediaSessionCompat(this, "tag")
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(mAudioList[mPosition].title)
                .setContentText(mAudioList[mPosition].artists_names)
                .setContentIntent(pendingIntent)
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.sessionToken)
                )
                .setLargeIcon(
                    thumbnail
                )
                .addAction(
                    R.drawable.ic_baseline_arrow_left_24, "Back", getPendingIntent(
                        this,
                        ACTION_PREVIOUS
                    )
                )
        if (musicPlayer.isPlaying) {
            notificationBuilder
                .addAction(
                    R.drawable.ic_baseline_pause_24_black, "Pause", getPendingIntent(
                        this,
                        ACTION_PAUSE
                    )
                )
        } else {
            notificationBuilder
                .addAction(
                    R.drawable.ic_baseline_play_arrow_24_black, "Resume", getPendingIntent(
                        this,
                        ACTION_RESUME
                    )
                )
        }
            .addAction(
                R.drawable.ic_baseline_arrow_right_24, "Next", getPendingIntent(
                    this,
                    ACTION_NEXT
                )
            )
            .addAction(
                R.drawable.ic_baseline_cancel_24, "Cancel", getPendingIntent(
                    this,
                    ACTION_CLEAR
                )
            )

        val notification = notificationBuilder.build()
        startForeground(MUSIC_NOTIFICATION_ID, notification)
    }

    private fun getPendingIntent(context: Context, action: Int): PendingIntent {
        val intent = Intent(SEND_ACTION_FROM_NOTIFICATION)
        intent.putExtra("action_music", action)
        return PendingIntent.getBroadcast(
            context.applicationContext,
            action,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when (intent.action) {
                    SEND_ACTION_FROM_NOTIFICATION -> {
                        val actionMusic: Int = intent.getIntExtra("action_music", 0)
                        handleActionMusic(actionMusic)
                    }
                }
            }
        }

    }

    private fun handleActionMusic(actionMusic: Int) {
        when (actionMusic) {
            ACTION_CLEAR -> {
                stopForeground(true)
                musicPlayer.stop()
                musicPlayer.reset()
            }
            ACTION_PAUSE -> {
                pauseMusic()
            }
            ACTION_RESUME -> {
                resumeMusic()
            }
            ACTION_NEXT -> {
                nextMusic()
            }
            ACTION_PREVIOUS -> {
                previousMusic()
            }
        }
    }

    fun previousMusic() {
        musicPlayer.pause()
        mPosition -= 1
        playAudio()
        sendDataToActivity(ACTION_START)
    }

    fun nextMusic() {
        musicPlayer.pause()
        mPosition += 1
        playAudio()
        sendDataToActivity(ACTION_START)
    }

    fun pauseMusic(){
        musicPlayer.pause()
        showNotification()
        sendDataToActivity(ACTION_PAUSE)
    }

    fun resumeMusic(){
        musicPlayer.start()
        showNotification()
        sendDataToActivity(ACTION_RESUME)
    }

    private fun checkPositionAndList(): Boolean {
        return mPosition != 0 && mAudioList.size != 0
    }

}