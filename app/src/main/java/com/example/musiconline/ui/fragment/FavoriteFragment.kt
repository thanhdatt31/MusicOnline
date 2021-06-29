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
import com.example.musiconline.databinding.FragmentFavoriteBinding
import com.example.musiconline.model.Song
import com.example.musiconline.repository.RoomRepository
import com.example.musiconline.service.MyService
import com.example.musiconline.ui.PlayerActivity
import com.example.musiconline.ulti.Const
import com.example.musiconline.viewmodel.RoomViewModel
import com.example.musiconline.viewmodel.RoomViewModelProviderFactory


class FavoriteFragment : Fragment() {
    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RoomViewModel
    private var songAdapter = SongAdapter()
    private lateinit var connection: ServiceConnection
    private var mBound: Boolean = false
    private lateinit var mService: MyService
    private var mPosition = 0
    private var mListFavoriteSong: List<Song>? = arrayListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initService()
        setupViewModel()
    }

    private fun setupViewModel() {
        val repository = RoomRepository()
        val factory = RoomViewModelProviderFactory(requireActivity().application, repository)
        viewModel = ViewModelProvider(this, factory).get(RoomViewModel::class.java)
        getFavoriteListSongData()
    }

    private fun getFavoriteListSongData() {
        viewModel.getFavoriteListSong(requireContext()).observe(this, {
            mListFavoriteSong = it
            songAdapter.setData(it as ArrayList<Song>)
            binding.recyclerView.adapter = songAdapter
        })
    }

    private fun initService() {
        connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as MyService.BinderAudio
                mService = binder.getService()
                mService.getStatusPlayer().observe(viewLifecycleOwner, {
                    val song: Song = mService.getPosition().value!!.let { it1 ->
                        mService.getListAudioLiveData().value!![it1]
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
            Glide.with(requireContext())
                .load(song.thumbnail)
                .into(binding.imgAlbum)
        } else {
            Glide.with(requireContext())
                .load(Const.getAlbumBitmap(requireContext(), song.uri))
                .into(binding.imgAlbum)
        }
        binding.btnNextMini.setOnClickListener {
            mService.nextMusic()
        }
        binding.btnPreviousMini.setOnClickListener {
            mService.previousMusic()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
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
            mService.setListAudioAndPosition(mListFavoriteSong as ArrayList<Song>, position)
//            mListOfflineSong?.let { mService.setListAudioAndPosition(it, position) }
            mService.playAudio()
        }

    }
}