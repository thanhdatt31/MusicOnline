package com.example.musiconline.ui

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.musiconline.R
import com.example.musiconline.adapter.ViewpagerAdapter
import com.example.musiconline.databinding.ActivityPlayerBinding
import com.example.musiconline.model.Song
import com.example.musiconline.service.MyService
import com.example.musiconline.ulti.Const

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var connection: ServiceConnection
    private var mBound: Boolean = false
    private lateinit var mService: MyService
    private var mAudioList: ArrayList<Song> = arrayListOf()
    private var mPosition = 0
    private var handler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initService()
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewPager.apply {
            adapter = ViewpagerAdapter(supportFragmentManager)
        }
        binding.tabLayout.apply {
            setupWithViewPager(binding.viewPager)
        }

    }

    private fun initService() {
        connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as MyService.BinderAudio
                mService = binder.getService()
                mAudioList = mService.getListAudioLiveData().value!!
                mPosition = mService.getPosition().value!!
                handleLayout(mAudioList, mPosition)
                mService.getListAudioLiveData().observe(this@PlayerActivity,{
                    handler.removeCallbacks(runnable)
                    mAudioList = it
                    mPosition = mService.getPosition().value!!
                    handleLayout(mAudioList, mPosition)
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
        binding.tvDuration.text =
            Const.durationConverter((mAudioList[mPosition].duration * 1000).toLong())
        binding.seekBar.max = mAudioList[mPosition].duration * 1000
        binding.seekBar.setOnSeekBarChangeListener(@SuppressLint("AppCompatCustomView")
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