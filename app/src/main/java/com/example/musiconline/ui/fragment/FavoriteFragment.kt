package com.example.musiconline.ui.fragment

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musiconline.R
import com.example.musiconline.adapter.RecommendAdapter
import com.example.musiconline.adapter.SongAdapter
import com.example.musiconline.databinding.FragmentFavoriteBinding
import com.example.musiconline.model.Song
import com.example.musiconline.repository.RoomRepository
import com.example.musiconline.service.MyService
import com.example.musiconline.ui.PlayerActivity
import com.example.musiconline.ulti.Const
import com.example.musiconline.ulti.Const.REFRESH_LIST
import com.example.musiconline.viewmodel.RoomViewModel
import com.example.musiconline.viewmodel.RoomViewModelProviderFactory


class FavoriteFragment : Fragment() {
    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RoomViewModel
    private var songAdapter = RecommendAdapter()
    private lateinit var connection: ServiceConnection
    private var mBound: Boolean = false
    private lateinit var mService: MyService
    private var mPosition = 0
    private var mListFavoriteSong: List<Song>? = arrayListOf()
    private lateinit var song: Song
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

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(broadcastReceiver, IntentFilter(REFRESH_LIST))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
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
                            hideProgressBar()
                            binding.btnPlayPause.setImageResource(R.drawable.icons8_pause_100)
                            binding.btnPlayPause.setOnClickListener {
                                mService.pauseMusic()
                            }
                            handleMusicDetails(song)
                        }
                        false -> {
                            if (binding.viewMini.visibility != View.VISIBLE) {
                                binding.viewMini.visibility = View.VISIBLE
                            }
                            binding.btnPlayPause.setImageResource(R.drawable.icons8_play_100)
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
            showProgressBar()
        }
        binding.btnPreviousMini.setOnClickListener{
            mService.previousMusic()
            showProgressBar()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(),2)
            setHasFixedSize(true)
            songAdapter.setOnClickListener(onClicked)
        }
    }

    private val onClicked = object : RecommendAdapter.OnItemClickListener {
        override fun onClicked(position: Int) {
            mPosition = position
            mService.setListAudioAndPosition(mListFavoriteSong as ArrayList<Song>, position)
            mService.playAudio()
        }

    }
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (isAdded) {
                getFavoriteListSongData()
            }
        }
    }
    private fun showProgressBar() {
        binding.progress.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progress.visibility = View.GONE
    }
}