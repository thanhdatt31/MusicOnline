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
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.example.musiconline.R
import com.example.musiconline.model.ResultSong
import com.example.musiconline.model.Song
import com.example.musiconline.ui.MainActivity
import com.example.musiconline.ulti.Const.ACTION_CLEAR
import com.example.musiconline.ulti.Const.ACTION_NEXT
import com.example.musiconline.ulti.Const.ACTION_PAUSE
import com.example.musiconline.ulti.Const.ACTION_PREVIOUS
import com.example.musiconline.ulti.Const.ACTION_RESUME
import com.example.musiconline.ulti.Const.CHANNEL_ID
import com.example.musiconline.ulti.Const.MUSIC_NOTIFICATION_ID
import com.example.musiconline.ulti.Const.SEND_ACTION_FROM_NOTIFICATION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyService : Service(), MediaPlayer.OnPreparedListener {
    private val mIBinder = BinderAudio()
    private var mAudioList: ArrayList<Song> = arrayListOf()
    private var mPosition: MutableLiveData<Int> = MutableLiveData()
    private var isPlaying: MutableLiveData<Boolean> = MutableLiveData()
    private var audioListLiveData: MutableLiveData<ArrayList<Song>> = MutableLiveData()
    private var isServiceWorking: MutableLiveData<Boolean> = MutableLiveData()
    private var musicPlayer: MediaPlayer = MediaPlayer()
    var resultSearchSong: MutableLiveData<ResultSong> = MutableLiveData()
    private lateinit var thumbnail: Bitmap

    inner class BinderAudio : Binder() {
        fun getService(): MyService = this@MyService
    }

    override fun onBind(intent: Intent?): IBinder {
        return mIBinder
    }

    fun setListAudioAndPosition(data: ArrayList<Song>, position: Int) {
        if (mAudioList.isNotEmpty()) {
            mAudioList.clear()
            mAudioList.addAll(data)
            audioListLiveData.value = mAudioList
            mPosition.value = position

        } else {
            mAudioList.addAll(data)
            audioListLiveData.value = mAudioList
            mPosition.value = position
        }
        resultSearchSong.value = null
    }

    fun getListAudioLiveData(): MutableLiveData<ArrayList<Song>> {
        return audioListLiveData
    }

    fun getListAudio(): ArrayList<Song> {
        return mAudioList
    }

    fun getCurrentPosition(): Int {
        return musicPlayer.currentPosition
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        registerReceiver(broadcastReceiver, IntentFilter(SEND_ACTION_FROM_NOTIFICATION))
        registerReceiver(broadcastReceiver, IntentFilter("progress_from_activity"))
    }

    private fun getBitmap() {
        GlobalScope.launch(Dispatchers.IO) {
            thumbnail = Glide.with(this@MyService)
                .asBitmap()
                .load(mAudioList[mPosition.value!!].thumbnail)
                .submit()
                .get()
        }
    }

    fun getPosition(): MutableLiveData<Int> {
        return mPosition
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
                .setContentIntent(pendingIntent)
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.sessionToken)
                )
//                .setLargeIcon(
//                    thumbnail
//                )
                .addAction(
                    R.drawable.ic_baseline_arrow_left_24, "Back", getPendingIntent(
                        this,
                        ACTION_PREVIOUS
                    )
                )
        if (resultSearchSong.value == null) {
            notificationBuilder.setContentTitle(mAudioList[mPosition.value!!].title)
                .setContentText(mAudioList[mPosition.value!!].artists_names)
        } else {
            notificationBuilder.setContentTitle(resultSearchSong.value!!.name)
                .setContentText(resultSearchSong.value!!.artist)
        }
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
                    "progress_from_activity" -> {
                        val progress = intent.getIntExtra("progress", 0)
                        musicPlayer.seekTo(progress)
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
                isServiceWorking.postValue(false)
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

    fun playAudio() = GlobalScope.launch(Dispatchers.IO) {
        if (checkPositionAndList()) {
            musicPlayer.reset()
            musicPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            if (mAudioList[mPosition.value!!].thumbnail != null) {
                val url =
                    "http://api.mp3.zing.vn/api/streaming/audio/${mAudioList[mPosition.value!!].id}/128"
                musicPlayer.setDataSource(url)
            } else {
                musicPlayer.setDataSource(this@MyService, mAudioList[mPosition.value!!].uri!!)
            }
            musicPlayer.prepareAsync()
            musicPlayer.setOnPreparedListener(this@MyService)
            isServiceWorking.postValue(true)
        }

    }

    fun playAudioOnline(resultSong: ResultSong) {
        musicPlayer.reset()
        musicPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        val url =
            "http://api.mp3.zing.vn/api/streaming/audio/${resultSong.id}}/128"
        musicPlayer.setDataSource(url)
        musicPlayer.prepare()
        musicPlayer.start()
        isServiceWorking.postValue(true)
        isPlaying.postValue(musicPlayer.isPlaying)
        showNotification()
    }

    fun getStatusService(): MutableLiveData<Boolean> {
        return isServiceWorking
    }

    fun setDataOnline(data: ResultSong) {
        this.resultSearchSong.value = data
    }

    fun getStatusPlayer(): MutableLiveData<Boolean> {
        return isPlaying
    }

    fun previousMusic() {
        musicPlayer.pause()
        mPosition.value = mPosition.value?.minus(1)
        playAudio()
    }

    override fun onDestroy() {
        isServiceWorking.postValue(false)
        super.onDestroy()
    }


    fun nextMusic() {
        musicPlayer.pause()
        mPosition.value = mPosition.value?.plus(1)
        playAudio()
    }

    fun pauseMusic(){
        musicPlayer.pause()
        showNotification()
        isPlaying.postValue(musicPlayer.isPlaying)
    }

    fun resumeMusic() {
        musicPlayer.start()
        showNotification()
        isPlaying.postValue(musicPlayer.isPlaying)
    }

    private fun checkPositionAndList(): Boolean {
        return mAudioList.size != 0
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mp?.start()
        showNotification()
        isPlaying.postValue(mp?.isPlaying)
    }

}