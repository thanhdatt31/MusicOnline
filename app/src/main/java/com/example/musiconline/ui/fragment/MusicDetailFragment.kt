package com.example.musiconline.ui.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.musiconline.databinding.FragmentMusicDetailBinding
import com.example.musiconline.model.ResultSong
import com.example.musiconline.model.Song
import com.example.musiconline.service.MyService
import com.example.musiconline.ulti.Const


class MusicDetailFragment : Fragment() {
    private var _binding: FragmentMusicDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var connection: ServiceConnection
    private var mBound: Boolean = false
    private lateinit var mService: MyService
    private var mAudioList: ArrayList<Song> = arrayListOf()
    private var mPosition = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMusicDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initService()
    }

    private fun initService() {
        connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as MyService.BinderAudio
                mService = binder.getService()
                if (mService.resultSearchSong.value == null) {
                    mAudioList = mService.getListAudioLiveData().value!!
                    mPosition = mService.getPosition().value!!
                    handleLayout(mAudioList, mPosition)
                } else {
                    val resultSong = mService.resultSearchSong.value!!
                    handleLayoutSearchSong(resultSong)
                }
                mService.getListAudioLiveData().observe(this@MusicDetailFragment, {
                    mAudioList = it
                    handleLayout(mAudioList, mService.getPosition().value!!)
                })
                mService.getPosition().observe(this@MusicDetailFragment, {
                    handleLayout(mAudioList, it)
                })
                mService.resultSearchSong.observe(viewLifecycleOwner, {
                    if (it != null) {
                        handleLayoutSearchSong(it)
                    }
                })
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                mBound = false
            }

        }
        Intent(requireContext(), MyService::class.java).also { intent ->
            requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun handleLayoutSearchSong(resultSong: ResultSong) {
        val thumb = "https://photo-resize-zmp3.zadn.vn/w320_r1x1_jpeg/${resultSong.thumb}"
        Glide.with(requireContext())
            .load(thumb)
            .into(binding.imgAlbumFull)
        binding.tvSongTitleFull.text = resultSong.name
        binding.tvArtistTitleFull.text = resultSong.artist
    }

    private fun handleLayout(mAudioList: ArrayList<Song>, mPosition: Int) {
        val song: Song = mAudioList[mPosition]
        val oldThumb = song.thumbnail
        val newThumb = oldThumb?.replace("w94", "w320")
        if (song.thumbnail != null) {
            Glide.with(requireContext())
                .load(newThumb)
                .into(binding.imgAlbumFull)
        } else {
            Glide.with(requireContext())
                .load(Const.getAlbumBitmap(requireContext(), song.uri!!))
                .into(binding.imgAlbumFull)
        }
        binding.tvSongTitleFull.text = song.title
        binding.tvArtistTitleFull.text = song.artists_names
    }
}