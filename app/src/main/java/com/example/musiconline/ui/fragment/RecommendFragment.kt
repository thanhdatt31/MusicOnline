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
import com.example.musiconline.adapter.RecommendAdapter
import com.example.musiconline.databinding.FragmentRecommendBinding
import com.example.musiconline.model.Song
import com.example.musiconline.repository.MainRepository
import com.example.musiconline.service.MyService
import com.example.musiconline.ulti.Resource
import com.example.musiconline.viewmodel.MainViewModel

class RecommendFragment : Fragment() {
    private var _binding: FragmentRecommendBinding? = null
    private val binding get() = _binding!!
    private lateinit var connection: ServiceConnection
    private var mBound: Boolean = false
    private lateinit var mService: MyService
    private var mAudioList: ArrayList<Song> = arrayListOf()
    private var mNewAudioList: ArrayList<Song> = arrayListOf()
    private var mPosition = 0
    private var songAdapter = RecommendAdapter()
    private var mListOfflineSong: ArrayList<Song>? = arrayListOf()
    private val viewModel by lazy {
        ViewModelProvider(
            this,
            MainViewModel.ViewModelProviderFactory(requireActivity().application, MainRepository())
        )
            .get(MainViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecommendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initService()
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


    private fun initService() {
        connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as MyService.BinderAudio
                mService = binder.getService()
                if (mService.resultSearchSong.value == null) {
                    mAudioList = mService.getListAudioLiveData().value!!
                    mPosition = mService.getPosition().value!!
                    if (mAudioList[mPosition].thumbnail == null) {
                        setupViewModel(null)
                    } else {
                        mAudioList[mPosition].id?.let { setupViewModel(it) }
                    }
                } else {
                    val resultSong = mService.resultSearchSong.value!!
                    setupViewModel(resultSong.id)
                }

            }

            override fun onServiceDisconnected(name: ComponentName?) {
                mBound = false
            }

        }
        Intent(requireContext(), MyService::class.java).also { intent ->
            requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun getRecommendSong() {
        viewModel.recommendSongData.observe(this, { it ->
            when (it) {
                is Resource.Success -> {
                    hideProgressBar()
                    it.data?.let {
                        mNewAudioList = it.data.items
                        mService.mAudioList.addAll(mNewAudioList)
                        songAdapter.setData(mNewAudioList)
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


    private fun setupViewModel(id: String?) {
        if (id.isNullOrEmpty()) {
            getOfflineSong()
        } else {
            viewModel.id = id
            viewModel.getRecommendSong()
            getRecommendSong()
        }

    }

    private fun getOfflineSong() {
        viewModel.offlineSongData.observe(viewLifecycleOwner, {
            mListOfflineSong = it
            songAdapter.setData(it)
            binding.recyclerView.adapter = songAdapter
        })
    }

    private val onClicked = object : RecommendAdapter.OnItemClickListener {
        override fun onClicked(position: Int) {
            mService.mPosition.value = position
            if (mListOfflineSong.isNullOrEmpty()) {
                mService.setListAudioAndPosition(mNewAudioList, position)
            } else {
                mService.setListAudioAndPosition(mListOfflineSong!!, position)
            }
            refreshRecommendList()
            mService.playAudio()
        }

    }

    private fun refreshRecommendList() {
        mAudioList = mService.getListAudioLiveData().value!!
        mPosition = mService.getPosition().value!!
        val song: Song = mAudioList[mPosition]
        viewModel.id = song.id.toString()
        viewModel.getRecommendSong()
        getRecommendSong()
    }
}