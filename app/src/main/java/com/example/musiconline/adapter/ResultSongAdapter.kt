package com.example.musiconline.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musiconline.R
import com.example.musiconline.model.ResultSong
import com.example.musiconline.model.Song
import com.example.musiconline.ulti.Const
import java.io.Serializable

class ResultSongAdapter: RecyclerView.Adapter<ResultSongAdapter.ViewHolder>() {
    private lateinit var context: Context
    private var songList: ArrayList<ResultSong> = arrayListOf()
    var listener: ResultSongAdapter.OnItemClickListener? = null
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), Serializable {
        var imgThumb: ImageView = itemView.findViewById(R.id.img_thumb)
        var title: TextView = itemView.findViewById(R.id.tv_title)
        var duration: TextView = itemView.findViewById(R.id.tv_duration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.item_audio, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resultSong = songList[position]
        holder.title.text = resultSong.name
        holder.duration.text = Const.durationConverter((resultSong.duration.toInt() * 1000).toLong())
        val thumb = "https://photo-resize-zmp3.zadn.vn/w320_r1x1_jpeg/${resultSong.thumb}"
        Glide.with(holder.itemView.context)
            .load(thumb)
            .centerCrop()
            .into(holder.imgThumb)
        holder.itemView.setOnClickListener{
            listener!!.onClicked(resultSong)
        }
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    fun setData(data : ArrayList<ResultSong>){
        songList = data
        notifyDataSetChanged()
    }
    interface OnItemClickListener {
        fun onClicked(resultSong: ResultSong)
    }

    fun setOnClickListener(listener1: OnItemClickListener) {
        listener = listener1
    }
}