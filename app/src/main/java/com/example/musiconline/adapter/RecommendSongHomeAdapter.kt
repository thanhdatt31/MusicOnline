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
import com.example.musiconline.model.Song
import com.example.musiconline.ulti.Const
import java.io.Serializable

class RecommendSongHomeAdapter : RecyclerView.Adapter<RecommendSongHomeAdapter.ViewHolder>() {
    private lateinit var context: Context
    private var songList: ArrayList<Song> = arrayListOf()
    var listener: OnItemClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgThumb: ImageView = itemView.findViewById(R.id.img_thumb)
        var title: TextView = itemView.findViewById(R.id.tv_title)
        var duration: TextView = itemView.findViewById(R.id.tv_duration)
        var chart : TextView = itemView.findViewById(R.id.tv_pos)
        var artist : TextView = itemView.findViewById(R.id.tv_artist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.item_song_chart, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.chart.text = "#${(position + 1)}"
        val song: Song = songList[position]
        holder.title.text = song.title
        holder.artist.text = song.artists_names
        if (song.thumbnail != null) {
            val oldThumb = song.thumbnail
            val newThumb = oldThumb.replace("w94", "w320")
            Glide.with(holder.itemView.context)
                .load(newThumb)
                .centerCrop()
                .into(holder.imgThumb)
        } else {
            if (song.uri != null) {
                Glide.with(holder.itemView.context)
                    .load(Const.getAlbumBitmap(holder.itemView.context, song.uri))
                    .centerCrop()
                    .into(holder.imgThumb)
            }

        }
        if (song.duration.toString().length < 4) {
            holder.duration.text = Const.durationConverter((song.duration * 1000).toLong())
        } else {
            holder.duration.text = Const.durationConverter((song.duration).toLong())
        }

        holder.itemView.setOnClickListener {
            listener!!.onClicked(position)
        }
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    fun setData(data: ArrayList<Song>) {
        this.songList = data
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onClicked(position: Int)
    }

    fun setOnClickListener(listener1: OnItemClickListener) {
        listener = listener1
    }

}