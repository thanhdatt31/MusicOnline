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
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musiconline.adapter.ResultSongAdapter
import com.example.musiconline.databinding.FragmentSearchBinding
import com.example.musiconline.model.ResultSong
import com.example.musiconline.model.Song
import com.example.musiconline.repository.MainRepository
import com.example.musiconline.service.MyService
import com.example.musiconline.ulti.Resource
import com.example.musiconline.viewmodel.MainViewModel
import com.example.musiconline.viewmodel.ViewModelProviderFactory
import java.util.*


class SearchFragment : Fragment(), SearchView.OnQueryTextListener {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var resultSongAdapter = ResultSongAdapter()
    private var mAudioList: ArrayList<Song> = arrayListOf()
    private lateinit var viewModel: MainViewModel
    private lateinit var mService: MyService
    private var mBound: Boolean = false
    private var mPosition = 0
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        Intent(requireContext(), MyService::class.java).also { intent ->
            requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
//            songAdapter.setOnClickListener(onClicked)
            resultSongAdapter.setOnClickListener(onClicked)
        }
        setupViewModel()
        binding.searchView.setOnQueryTextListener(this)
    }

    private fun setupViewModel() {
        val repository = MainRepository()
        val factory = ViewModelProviderFactory(requireActivity().application, repository)
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)
        getSearchResult()
    }

    private fun getSearchResult() {
        viewModel.searchResultData.observe(viewLifecycleOwner, { it ->
            when (it) {
                is Resource.Success -> {
                    hideProgressBar()
                    it.data?.let {
                        if(!it.data.isNullOrEmpty()){
                            binding.recyclerView.visibility = View.VISIBLE
                            resultSongAdapter.setData(it.data[0].song)
                            binding.recyclerView.adapter = resultSongAdapter
                        } else {
                            binding.recyclerView.visibility = View.GONE
                        }
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

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            viewModel.query = newText
            viewModel.getSearchResult()
        }

        return true
    }

    private fun showProgressBar() {
        binding.progress.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progress.visibility = View.GONE
    }

    private val onClicked = object : ResultSongAdapter.OnItemClickListener {
        override fun onClicked(resultSong: ResultSong) {
            mService.setDataOnline(resultSong)
            mService.playAudioOnline(resultSong)
        }


    }
}