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
import com.example.musiconline.viewmodel.RoomViewModel
import com.example.musiconline.viewmodel.RoomViewModelProviderFactory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var connection: ServiceConnection
    private var mBound: Boolean = false
    private lateinit var mService: MyService
    private var mAudioList: ArrayList<Song> = arrayListOf()
    private var mListFavoriteSong: List<Song> = arrayListOf()
    private var mPosition = 0
    private var handler = Handler()
    private lateinit var viewModel: RoomViewModel
    private var isOfflineSong: MutableLiveData<Boolean> = MutableLiveData()
    var isFavorite: MutableLiveData<Boolean> = MutableLiveData()

    @DelicateCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initService()
        setupViewModel()
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
                    binding.btnDownload.setImageResource(R.drawable.ic_baseline_arrow_circle_down_24_white)
                    binding.btnDownload.setOnClickListener {
                        val url =
                            "http://api.mp3.zing.vn/api/streaming/audio/${mAudioList[mPosition].id}/128"
                        val request = DownloadManager.Request(Uri.parse(url))
                        val title = URLUtil.guessFileName(url, null, null)
                        val cookie = CookieManager.getInstance().getCookie(title)
                        request.addRequestHeader("cookie", cookie)
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"${mAudioList[mPosition].title}.mp3")
                        val downloadManager =
                           getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        val idDownload = downloadManager.enqueue(request)

                    }
                }
                true -> {
                    binding.btnDownload.setImageResource(R.drawable.ic_baseline_arrow_circle_down_24)
                    binding.btnDownload.setOnClickListener {
                        Toast.makeText(this, "Offline music", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        isFavorite.observe(this, {
            when (it) {
                false -> {
                    binding.btnFavor.setImageResource(R.drawable.ic_baseline_favorite_24)
                    binding.btnFavor.setOnClickListener {
                        viewModel.insertSong(this, mAudioList[mPosition])
                        Toast.makeText(this, "Added to favorite !", Toast.LENGTH_SHORT).show()
                        sendBroadcast()
                        isFavorite.postValue(true)
                    }

                }
                true -> {
                    binding.btnFavor.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                    if (mAudioList[mPosition].thumbnail != null) {
                        binding.btnFavor.setOnClickListener {
                            viewModel.deleteSong(this, mAudioList[mPosition].id!!)
                            Toast.makeText(this, "Deleted from favorite list !", Toast.LENGTH_SHORT)
                                .show()
                            sendBroadcast()
                            isFavorite.postValue(false)
                        }
                    } else {
                        binding.btnFavor.setOnClickListener {
                            viewModel.deleteSpecificSongByUri(
                                this,
                                mAudioList[mPosition].uri.toString()
                            )
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

    private fun setupViewModel() {
        val repository = RoomRepository()
        val factory = RoomViewModelProviderFactory(application, repository)
        viewModel = ViewModelProvider(this, factory).get(RoomViewModel::class.java)
        getFavoriteListSongData()
    }

    private fun getFavoriteListSongData() {
        viewModel.getFavoriteListSong(this).observe(this, {
            isFavorite.postValue(false)
            if (mAudioList[mPosition].thumbnail != null) {
                for (i in it) {
                    if (i.id == mAudioList[mPosition].id) {
                        isFavorite.postValue(true)
                        break
                    }
                }
            } else {
                for (i in it) {
                    if (i.uri == mAudioList[mPosition].uri) {
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
                mAudioList = mService.getListAudioLiveData().value!!
                mPosition = mService.getPosition().value!!
                handleLayout(mAudioList, mPosition)
                mService.getListAudioLiveData().observe(this@PlayerActivity, {
                    handler.removeCallbacks(runnable)
                    mAudioList = it
                    mPosition = mService.getPosition().value!!
                    handleLayout(mAudioList, mPosition)
                    if (it[mPosition].thumbnail != null) {
                        isOfflineSong.postValue(false)
                    } else {
                        isOfflineSong.postValue(true)
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

    private fun handleLayout(mAudioList: ArrayList<Song>, mPosition: Int) {
        mService.getStatusPlayer().observe(this, {
            when (it) {
                true -> {
                    binding.btnPlayPause.apply {
                        setImageResource(R.drawable.ic_baseline_pause_circle_outline_24_white)
                        setOnClickListener {
                            mService.pauseMusic()
                        }
                    }
                    updateSeekBar()
                }
                false -> {
                    binding.btnPlayPause.apply {
                        setImageResource(R.drawable.ic_baseline_play_circle_outline_24_white)
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


    }

    private fun updateSeekBar() {
        binding.tvCurrentTime.text = Const.durationConverter(mService.getCurrentPosition().toLong())
        seekBarSetUp()
        handler.postDelayed(runnable, 100)
    }

    private fun seekBarSetUp() {
        binding.seekBar.progress = mService.getCurrentPosition()
        if (mAudioList[mPosition].duration.toString().length < 4) {
            binding.tvDuration.text =
                Const.durationConverter((mAudioList[mPosition].duration * 1000).toLong())
            binding.seekBar.max = mAudioList[mPosition].duration * 1000
        } else {
            binding.tvDuration.text =
                Const.durationConverter((mAudioList[mPosition].duration).toLong())
            binding.seekBar.max = mAudioList[mPosition].duration
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
                sendProgress(seekBar!!.progress)
            }

        })
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