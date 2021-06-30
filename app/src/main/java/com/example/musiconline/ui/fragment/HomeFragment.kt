package com.example.musiconline.ui.fragment

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musiconline.R
import com.example.musiconline.adapter.SongAdapter
import com.example.musiconline.databinding.FragmentHomeBinding
import com.example.musiconline.model.Song
import com.example.musiconline.repository.MainRepository
import com.example.musiconline.service.MyService
import com.example.musiconline.ui.PlayerActivity
import com.example.musiconline.ulti.Const
import com.example.musiconline.ulti.Resource
import com.example.musiconline.viewmodel.MainViewModel
import com.example.musiconline.viewmodel.ViewModelProviderFactory

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private var songAdapter = SongAdapter()
    private var mAudioList: ArrayList<Song> = arrayListOf()
    private lateinit var mService: MyService
    private var mBound: Boolean = false
    private var mPosition = 0
    private lateinit var song: Song
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MyService.BinderAudio
            mService = binder.getService()
            mService.getPosition().observe(this@HomeFragment, {
                mPosition = it
            })
            mAudioList = mService.getListAudio()
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
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
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

    override fun onStart() {
        super.onStart()
        Intent(requireContext(), MyService::class.java).also { intent ->
            requireActivity().startService(intent)
            requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        requireActivity().unbindService(connection)
        mBound = false
        Log.d("datnt", "onDestroy: ")
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        init()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun init() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            songAdapter.setOnClickListener(onClicked)
        }
        setupViewModel()
    }

    private fun setupViewModel() {
        val repository = MainRepository()
        val factory = ViewModelProviderFactory(requireActivity().application, repository)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)
        getTopSong()
    }

    private fun getTopSong() {
        viewModel.topSongData.observe(viewLifecycleOwner, { it ->
            when (it) {
                is Resource.Success -> {
                    hideProgressBar()
                    it.data?.let {
                        this.mAudioList = it.data.song
                        songAdapter.setData(mAudioList)
                        binding.recyclerView.adapter = songAdapter
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
                is Resource.Error -> {
                    hideProgressBar()
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun showProgressBar() {
        binding.progress.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progress.visibility = View.GONE
    }

    private val onClicked = object : SongAdapter.OnItemClickListener {
        override fun onClicked(position: Int) {
            mPosition = position
            mService.setListAudioAndPosition(mAudioList, position)
            mService.playAudio()
            showProgressBar()
        }

    }

}