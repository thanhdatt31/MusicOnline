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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musiconline.adapter.SongAdapter
import com.example.musiconline.databinding.FragmentHomeBinding
import com.example.musiconline.model.Song
import com.example.musiconline.repository.MainRepo
import com.example.musiconline.service.MyService
import com.example.musiconline.ulti.Resource
import com.example.musiconline.viewmodel.MainViewModel
import com.example.musiconline.viewmodel.ViewModelProviderFactory

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel
    var songAdapter = SongAdapter()
    private var mAudioList: ArrayList<Song> = arrayListOf()
    private lateinit var mService: MyService
    private var mBound: Boolean = false
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
            mService.getListAudioAndPosition(mAudioList, position)
            mService.playAudio()
        }

    }
}