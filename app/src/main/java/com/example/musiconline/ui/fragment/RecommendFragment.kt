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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musiconline.adapter.SongAdapter
import com.example.musiconline.databinding.FragmentRecommendBinding
import com.example.musiconline.model.Song
import com.example.musiconline.repository.MainRepository
import com.example.musiconline.service.MyService
import com.example.musiconline.ulti.Resource
import com.example.musiconline.viewmodel.MainViewModel
import com.example.musiconline.viewmodel.ViewModelProviderFactory

class RecommendFragment : Fragment() {
    private lateinit var viewModel: MainViewModel
    private var _binding: FragmentRecommendBinding? = null
    private val binding get() = _binding!!
    private lateinit var connection: ServiceConnection
    private var mBound: Boolean = false
    private lateinit var mService: MyService
    private var mAudioList: ArrayList<Song> = arrayListOf()
    private var mNewAudioList: ArrayList<Song> = arrayListOf()
    private var mPosition = 0
    private var songAdapter = SongAdapter()
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
                mAudioList = mService.getListAudioLiveData().value!!
                mPosition = mService.getPosition().value!!
                if(mAudioList[mPosition].thumbnail == null){
                    Toast.makeText(requireContext(), "alo", Toast.LENGTH_SHORT).show()
                } else {
                    mAudioList[mPosition].id?.let { setupViewModel(it) }
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
           when(it){
               is Resource.Success -> {
                   hideProgressBar()
                   it.data?.let {
                       mNewAudioList = it.data.items
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


    private fun setupViewModel(id: String) {
        val repository = MainRepository()
        val factory = ViewModelProviderFactory(requireActivity().application, repository)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)
        viewModel.id = id
        getRecommendSong()
    }

    private val onClicked = object : SongAdapter.OnItemClickListener {
        override fun onClicked(position: Int) {
            mPosition = position
            mService.setListAudioAndPosition(mNewAudioList, position)
            mService.playAudioOnline()
//            startActivity(Intent(requireContext(),PlayerActivity::class.java))
        }

    }
}