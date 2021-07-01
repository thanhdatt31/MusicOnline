package com.example.musiconline.ui.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musiconline.R
import com.example.musiconline.adapter.SongAdapter
import com.example.musiconline.databinding.FragmentOfflineMusicBinding
import com.example.musiconline.model.Song
import com.example.musiconline.repository.MainRepository
import com.example.musiconline.service.MyService
import com.example.musiconline.ui.PlayerActivity
import com.example.musiconline.ulti.Const
import com.example.musiconline.ulti.Const.getAlbumBitmap
import com.example.musiconline.viewmodel.MainViewModel
import com.example.musiconline.viewmodel.ViewModelProviderFactory


class OfflineMusicFragment : Fragment() {
    private var _binding: FragmentOfflineMusicBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private var mListOfflineSong: ArrayList<Song>? = arrayListOf()
    private var songAdapter = SongAdapter()
    private lateinit var connection: ServiceConnection
    private var mBound: Boolean = false
    private lateinit var mService: MyService
    private var mPosition = 0
    private lateinit var song: Song
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfflineMusicBinding.inflate(inflater, container, false)
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
                mService.getStatusPlayer().observe(viewLifecycleOwner, {
                    song = if (mService.resultSearchSong.value == null) {
                        mService.getPosition().value!!.let { it1 ->
                            mService.getListAudioLiveData().value!![it1]
                        }
                    } else {
                        val resultSong = mService.resultSearchSong.value!!
                        Song(
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
                    when (it) {
                        true -> {
                            if (binding.viewMini.visibility != View.VISIBLE) {
                                binding.viewMini.visibility = View.VISIBLE
                            }
                            binding.btnPlayPause.setImageResource(R.drawable.ic_baseline_pause_24)
                            binding.btnPlayPause.setOnClickListener {
                                mService.pauseMusic()
                            }
                            handleMusicDetails(song)
                        }
                        false -> {
                            if (binding.viewMini.visibility != View.VISIBLE) {
                                binding.viewMini.visibility = View.VISIBLE
                            }
                            binding.btnPlayPause.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                            binding.btnPlayPause.setOnClickListener {
                                mService.resumeMusic()
                            }
                            handleMusicDetails(song)
                        }
                    }
                })
                mService.getStatusService().observe(viewLifecycleOwner, {
                    when (it) {
                        false -> {
                            if (binding.viewMini.visibility == View.VISIBLE) {
                                binding.viewMini.visibility = View.GONE
                            }
                        }
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

    private fun handleMusicDetails(song: Song) {
        binding.viewMini.setOnClickListener {
            startActivity(Intent(requireContext(), PlayerActivity::class.java))
        }
        binding.tvSongTitle.text = song.title
        binding.tvArtistTitle.text = song.artists_names
        if (song.thumbnail != null) {
            if (song.thumbnail.contains("zmp3")) {
                Glide.with(requireContext())
                    .load(song.thumbnail)
                    .into(binding.imgAlbum)
            } else {
                val thumb = "https://photo-resize-zmp3.zadn.vn/w94_r1x1_jpeg/${song.thumbnail}"
                Glide.with(requireContext())
                    .load(thumb)
                    .into(binding.imgAlbum)
            }
        } else {
            Glide.with(requireContext())
                .load(Const.getAlbumBitmap(requireContext(), song.uri!!))
                .into(binding.imgAlbum)
        }
        binding.btnNextMini.setOnClickListener {
            mService.nextMusic()
        }
        binding.btnPreviousMini.setOnClickListener{
            mService.previousMusic()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        init()
    }

    private fun setupViewModel() {
        val repository = MainRepository()
        val factory = ViewModelProviderFactory(requireActivity().application, repository)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)
        getOfflineSong()
    }

    private fun getOfflineSong() {
        viewModel.offlineSongData.observe(viewLifecycleOwner, {
            mListOfflineSong = it
            songAdapter.setData(it)
            binding.recyclerView.adapter = songAdapter
        })
    }

    private fun init() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            songAdapter.setOnClickListener(onClicked)
        }
    }

    private val onClicked = object : SongAdapter.OnItemClickListener {
        override fun onClicked(position: Int) {
            mPosition = position
            mListOfflineSong?.let { mService.setListAudioAndPosition(it, position) }
            mService.playAudio()
        }

    }
}