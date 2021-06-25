package com.example.musiconline.ui.fragment

import android.content.*
import android.graphics.Bitmap
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.musiconline.R
import com.example.musiconline.adapter.SongAdapter
import com.example.musiconline.databinding.FragmentHomeBinding
import com.example.musiconline.model.Song
import com.example.musiconline.repository.MainRepo
import com.example.musiconline.service.MyService
import com.example.musiconline.ui.PlayerActivity
import com.example.musiconline.ulti.Const.ACTION_PAUSE
import com.example.musiconline.ulti.Const.ACTION_RESUME
import com.example.musiconline.ulti.Const.ACTION_START
import com.example.musiconline.ulti.Resource
import com.example.musiconline.viewmodel.MainViewModel
import com.example.musiconline.viewmodel.ViewModelProviderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private var songAdapter = SongAdapter()
    private var mAudioList: ArrayList<Song> = arrayListOf()
    private lateinit var mService: MyService
    private var mBound: Boolean = false
    private var mPosition = 0
    private lateinit var thumbnail: Bitmap
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MyService.BinderAudio
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(requireContext(), MyService::class.java).also { intent ->
            requireActivity().startService(intent)
            requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(broadcastReceiver, IntentFilter("send_data_to_activity"))
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
        val repository = MainRepo()
        val factory = ViewModelProviderFactory(requireActivity().application, repository)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)
        getTopSong()
    }

    private fun getTopSong() {
        viewModel.topSongData.observe(requireActivity(), { it ->
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
            getBitmap(mPosition)
            mService.getListAudioAndPosition(mAudioList, position)
            mService.playAudio()

        }

    }

    private fun handleViewMini(action: Int) {
        when (action) {
            ACTION_START -> {
                mPosition = mService.getPosition()
                getBitmap(mPosition)
                binding.viewMini.visibility = View.VISIBLE
                binding.tvArtistTitle.text = mAudioList[mPosition].artists_names
                binding.tvSongTitle.text = mAudioList[mPosition].title
                binding.imgAlbum.setImageBitmap(thumbnail)
                binding.btnNextMini.setOnClickListener {
                    mService.nextMusic()
                }
                binding.btnPreviousMini.setOnClickListener {
                    mService.previousMusic()
                }
                binding.btnPlayPause.setImageResource(R.drawable.ic_baseline_pause_24)
                binding.btnPlayPause.setOnClickListener {
                    mService.pauseMusic()
                }
                binding.viewMini.setOnClickListener {
                    startActivity(Intent(requireContext(), PlayerActivity::class.java))
                }
            }
            ACTION_PAUSE -> {
                binding.btnPlayPause.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                binding.btnPlayPause.setOnClickListener {
                    mService.resumeMusic()
                }
            }
            ACTION_RESUME -> {
                binding.btnPlayPause.setImageResource(R.drawable.ic_baseline_pause_24)
                binding.btnPlayPause.setOnClickListener {
                    mService.pauseMusic()
                }
            }
        }


    }

    private fun getBitmap(position: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            thumbnail = Glide.with(requireContext())
                .asBitmap()
                .load(mAudioList[position].thumbnail)
                .submit()
                .get()
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when (intent.action) {
                    "send_data_to_activity" -> {
                        val bundle = intent.extras
                        if (bundle != null) {
                            handleViewMini(bundle.getInt("action"))
                        }
                    }
                }
            }
        }

    }

}