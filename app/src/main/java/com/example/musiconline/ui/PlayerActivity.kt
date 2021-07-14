package com.example.musiconline.ui

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.musiconline.R
import com.example.musiconline.adapter.ViewpagerAdapter
import com.example.musiconline.databinding.ActivityPlayerBinding
import com.example.musiconline.model.Song
import com.example.musiconline.repository.RoomRepository
import com.example.musiconline.service.MyService
import com.example.musiconline.ulti.Const
import com.example.musiconline.ulti.Const.REFRESH_LIST
import com.example.musiconline.ulti.Const.REPEAT_ALL
import com.example.musiconline.ulti.Const.REPEAT_OFF
import com.example.musiconline.ulti.Const.REPEAT_ONE
import com.example.musiconline.viewmodel.RoomViewModel
import kotlinx.coroutines.*
import java.lang.Runnable

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var connection: ServiceConnection
    private var mBound: Boolean = false
    private lateinit var mService: MyService
    private var mAudioList: ArrayList<Song> = arrayListOf()
    private var mPosition = 0
    private var handler = Handler()
    private lateinit var song: Song
    private var isOfflineSong: MutableLiveData<Boolean> = MutableLiveData()
    private var isFavorite: MutableLiveData<Boolean> = MutableLiveData()
    private var isClicked = false
    private var isRepeat = REPEAT_OFF
    private val viewModel by lazy {
        ViewModelProvider(
            this,
            RoomViewModel.RoomViewModelProviderFactory(this.application, RoomRepository())
        )
            .get(RoomViewModel::class.java)
    }

    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initService()
        getFavoriteListSongData()
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewPager.apply {
            adapter = ViewpagerAdapter(supportFragmentManager)
        }
        binding.tabLayout.apply {
            setupWithViewPager(binding.viewPager)
        }
        isOfflineSong.observe(this, {
            when (it) {
                false -> {
                    binding.btnDownload.setImageResource(R.drawable.ic_downable)
                    if (mService.resultSearchSong.value == null) {
                        binding.btnDownload.setOnClickListener {
                            val url =
                                "http://api.mp3.zing.vn/api/streaming/audio/${mService.getListAudioLiveData().value!![mService.getPosition().value!!].id}/128"
                            val request = DownloadManager.Request(Uri.parse(url))
                            val title = URLUtil.guessFileName(url, null, null)
                            val cookie = CookieManager.getInstance().getCookie(title)
                            request.addRequestHeader("cookie", cookie)
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS,
                                "${mService.getListAudioLiveData().value!![mService.getPosition().value!!].title}.mp3"
                            )
                            val downloadManager =
                                getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            val idDownload = downloadManager.enqueue(request)
                            Toast.makeText(
                                this@PlayerActivity,
                                "Download ${mService.getListAudioLiveData().value!![mService.getPosition().value!!].title}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        binding.btnDownload.setOnClickListener {
                            val url =
                                "http://api.mp3.zing.vn/api/streaming/audio/${mService.resultSearchSong.value!!.id}/128"
                            val request =
                                android.app.DownloadManager.Request(android.net.Uri.parse(url))
                            val title = android.webkit.URLUtil.guessFileName(url, null, null)
                            val cookie = android.webkit.CookieManager.getInstance().getCookie(title)
                            request.addRequestHeader("cookie", cookie)
                            request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setDestinationInExternalPublicDir(
                                android.os.Environment.DIRECTORY_DOWNLOADS,
                                "${mService.resultSearchSong.value!!.name}.mp3"
                            )
                            val downloadManager =
                                getSystemService(android.content.Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
                            val idDownload = downloadManager.enqueue(request)
                            Toast.makeText(
                                this@PlayerActivity,
                                "Download ${mService.resultSearchSong.value!!.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                true -> {
                    binding.btnDownload.setImageResource(R.drawable.ic_undownloadable)
                    binding.btnDownload.setOnClickListener {
                        Toast.makeText(this, "Offline music", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        isFavorite.observe(this, {
            when (it) {
                false -> {
                    binding.btnFavor.setImageResource(R.drawable.ic_unfavor)
                    binding.btnFavor.setOnClickListener {
                        if (mService.resultSearchSong.value == null) {
                            viewModel.insertSong(this, mAudioList[mService.getPosition().value!!])
                        } else {
                            val thumb =
                                "https://photo-resize-zmp3.zadn.vn/w94_r1x1_jpeg/${song.thumbnail}"
                            val songInputToRoom = Song(
                                song.artists_names,
                                song.code,
                                song.duration,
                                song.id,
                                song.position,
                                thumb,
                                song.title,
                                song.uri,
                                song.favor_id
                            )
                            viewModel.insertSong(this, songInputToRoom)
                        }
                        Toast.makeText(this, "Added to favorite !", Toast.LENGTH_SHORT).show()
                        sendBroadcast()
                        isFavorite.postValue(true)
                    }

                }
                true -> {
                    binding.btnFavor.setImageResource(R.drawable.ic_favor)
                    if (mService.resultSearchSong.value == null) {
                        if (mAudioList[mPosition].thumbnail != null) {
                            binding.btnFavor.setOnClickListener {
                                viewModel.deleteSong(
                                    this,
                                    mAudioList[mService.getPosition().value!!].id!!
                                )
                                Toast.makeText(
                                    this,
                                    "Deleted from favorite list !",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                sendBroadcast()
                                isFavorite.postValue(false)
                            }
                        } else {
                            binding.btnFavor.setOnClickListener {
                                viewModel.deleteSpecificSongByUri(
                                    this,
                                    mAudioList[mService.getPosition().value!!].uri.toString()
                                )
                                Toast.makeText(
                                    this,
                                    "Deleted from favorite list !",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                sendBroadcast()
                                isFavorite.postValue(false)
                            }
                        }
                    } else {
                        binding.btnFavor.setOnClickListener {
                            song.id?.let { it1 -> viewModel.deleteSong(this, it1) }
                            Toast.makeText(this, "Deleted from favorite list !", Toast.LENGTH_SHORT)
                                .show()
                            sendBroadcast()
                            isFavorite.postValue(false)
                        }
                    }


                }
            }
        })
    }

    private fun sendBroadcast() {
        val intent = Intent(REFRESH_LIST)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun getFavoriteListSongData() {
        viewModel.getFavoriteListSong(this).observe(this, {
            isFavorite.postValue(false)
            if (mService.resultSearchSong.value == null) {
                if (mAudioList[mService.getPosition().value!!].thumbnail != null) {
                    for (i in it) {
                        if (i.id == mAudioList[mService.getPosition().value!!].id) {
                            isFavorite.postValue(true)
                            break
                        }
                    }
                } else {
                    for (i in it) {
                        if (i.uri == mAudioList[mService.getPosition().value!!].uri) {
                            isFavorite.postValue(true)
                            break
                        }
                    }
                }
            } else {
                val resultSong = mService.resultSearchSong.value!!
                song = Song(
                    resultSong.artist,
                    null,
                    resultSong.duration.toInt(),
                    resultSong.id,
                    null,
                    resultSong.thumb,
                    resultSong.name,
                    null,
                    null
                )
                for (i in it) {
                    if (i.id == song.id) {
                        isFavorite.postValue(true)
                        break
                    }
                }
            }


        })
    }


    private fun initService() {
        connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as MyService.BinderAudio
                mService = binder.getService()
                if (mService.resultSearchSong.value == null) {
                    mAudioList = mService.getListAudioLiveData().value!!
                    mPosition = mService.getPosition().value!!
                    handleLayout()
                    updateSeekBar()
                } else {
                    handleLayout()
                }

                mService.getListAudioLiveData().observe(this@PlayerActivity, {
                    handler.removeCallbacks(runnable)
                    mAudioList = it
                    mPosition = mService.getPosition().value!!
                    handleLayout()
                })
                mService.getPosition().observe(this@PlayerActivity, {
                    seekBarSetUp()
                    getFavoriteListSongData()
                })
                mService.resultSearchSong.observe(this@PlayerActivity, {
                    if (it == null) {
                        if (mAudioList[mPosition].thumbnail != null) {
                            isOfflineSong.postValue(false)
                        } else {
                            isOfflineSong.postValue(true)
                        }
                    } else {
                        isOfflineSong.postValue(false)
                    }
                })
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                mBound = false
            }

        }
        Intent(this, MyService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun handleLayout() {
        mService.getStatusPlayer().observe(this, {
            when (it) {
                true -> {
                    binding.btnPlayPause.apply {
                        setImageResource(R.drawable.icons8_pause_100)
                        setOnClickListener {
                            mService.pauseMusic()
                        }
                    }
                    updateSeekBar()
                }
                false -> {
                    binding.btnPlayPause.apply {
                        setImageResource(R.drawable.icons8_play_100)
                        setOnClickListener {
                            mService.resumeMusic()
                        }
                    }
                }
            }
        })
        binding.btnDown.setOnClickListener {
            finish()
        }
        isClicked = restoreShuffleMode()
        if (isClicked) {
            binding.btnShuffle.setImageResource(R.drawable.ic_baseline_shuffle_24_selected)
        } else {
            binding.btnShuffle.setImageResource(R.drawable.ic_baseline_shuffle_24_white)
        }
        isRepeat = restoreRepeatMode()
        when (isRepeat) {
            REPEAT_OFF -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_24_white)
            }
            REPEAT_ONE -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_one_24_selected)
            }
            REPEAT_ALL -> {
                binding.btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_24)
            }
        }
        binding.btnShuffle.setOnClickListener {
            isClicked = when (isClicked) {
                false -> {
                    saveIsPlayShuffle(true)
                    binding.btnShuffle.setImageResource(R.drawable.ic_baseline_shuffle_24_selected)
                    true
                }
                true -> {
                    saveIsPlayShuffle(false)
                    binding.btnShuffle.setImageResource(R.drawable.ic_baseline_shuffle_24_white)
                    false
                }
            }
        }
        binding.btnRepeat.setOnClickListener {
            isRepeat = when (isRepeat) {
                REPEAT_OFF -> {
                    saveIsRepeat(REPEAT_ONE)
                    binding.btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_one_24_selected)
                    REPEAT_ONE
                }
                REPEAT_ONE -> {
                    saveIsRepeat(REPEAT_ALL)
                    binding.btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_24)
                    REPEAT_ALL
                }
                else -> {
                    saveIsRepeat(REPEAT_OFF)
                    binding.btnRepeat.setImageResource(R.drawable.ic_baseline_repeat_24_white)
                    REPEAT_OFF
                }
            }
        }
        binding.btnNext.setOnClickListener {
            mService.nextMusic()
        }
        binding.btnPrevious.setOnClickListener {
            mService.previousMusic()
        }
    }

    private fun updateSeekBar() {
        binding.tvCurrentTime.text = Const.durationConverter(mService.getCurrentPosition().toLong())
        seekBarSetUp()
        handler.postDelayed(runnable, 100)
    }

    private fun seekBarSetUp() {
        binding.seekBar.progress = mService.getCurrentPosition()
        if (mService.resultSearchSong.value == null) {
            if (mAudioList[mService.getPosition().value!!].duration.toString().length < 4) {
                binding.tvDuration.text =
                    Const.durationConverter((mAudioList[mService.getPosition().value!!].duration * 1000).toLong())
                binding.seekBar.max = mAudioList[mService.getPosition().value!!].duration * 1000
            } else {
                binding.tvDuration.text =
                    Const.durationConverter((mAudioList[mService.getPosition().value!!].duration).toLong())
                binding.seekBar.max = mAudioList[mService.getPosition().value!!].duration
            }
        } else {
            if (!this::song.isInitialized) {
                val resultSong = mService.resultSearchSong.value!!
                song = Song(
                    resultSong.artist,
                    null,
                    resultSong.duration.toInt(),
                    resultSong.id,
                    null,
                    resultSong.thumb,
                    resultSong.name,
                    null,
                    null
                )
            }
            binding.tvDuration.text =
                Const.durationConverter((song.duration * 1000).toLong())
            binding.seekBar.max = song.duration * 1000
        }

        binding.seekBar.setOnSeekBarChangeListener(
            @SuppressLint("AppCompatCustomView")
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        sendProgress(progress)
                        binding.tvCurrentTime.text = Const.durationConverter(progress.toLong())
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    //
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

            })
    }

    private fun saveIsPlayShuffle(b: Boolean) {
        val pref = applicationContext.getSharedPreferences("myPrefs", MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean("isPlayShuffle", b)
        editor.apply()
    }

    private fun saveIsRepeat(mode: Int) {
        val pref = applicationContext.getSharedPreferences("myPrefs", MODE_PRIVATE)
        val editor = pref.edit()
        editor.putInt("isRepeat", mode)
        editor.apply()
    }

    private fun restoreShuffleMode(): Boolean {
        val pref = applicationContext.getSharedPreferences("myPrefs", MODE_PRIVATE)
        return pref.getBoolean("isPlayShuffle", false)
    }

    private fun restoreRepeatMode(): Int {
        val pref = applicationContext.getSharedPreferences("myPrefs", MODE_PRIVATE)
        return pref.getInt("isRepeat", REPEAT_OFF)
    }

    private fun sendProgress(progress: Int) {
        val intent = Intent("progress_from_activity")
        intent.putExtra("progress", progress)
        sendBroadcast(intent)
    }

    var runnable = Runnable { updateSeekBar() }

    override fun onDestroy() {
        unbindService(connection)
        mBound = false
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}