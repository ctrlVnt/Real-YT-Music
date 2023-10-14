package com.ctrlvnt.rytm.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ctrlvnt.rytm.R
import com.ctrlvnt.rytm.data.database.entities.Video
import com.ctrlvnt.rytm.data.model.VideoItem
import com.ctrlvnt.rytm.ui.MainActivity
import com.ctrlvnt.rytm.ui.fragment.YouTubePlayerSupport

class VideoAdapter(private val videoList: List<VideoItem>, private val onItemLongClick: ((VideoItem) -> Unit)? = null) :
    RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(videoItem: VideoItem)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoTitle: TextView = itemView.findViewById(R.id.video_title)
        val channelTitle: TextView = itemView.findViewById(R.id.channel_title)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.onItemClick(videoList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return VideoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val currentItem = videoList[position]
        holder.videoTitle.text = currentItem.snippet.title
        holder.channelTitle.text = currentItem.snippet.channelTitle

        holder.itemView.setOnClickListener {
            var video = Video(videoList[position].id.videoId, videoList[position].snippet.title, videoList[position].snippet.channelTitle)
            if (!exist(video)){
                MainActivity.database.insertVideo(video)
            }
            val fragment = YouTubePlayerSupport.newInstance(videoList[position].id.videoId)
            val transaction = (holder.itemView.context as AppCompatActivity)
                .supportFragmentManager.beginTransaction()
                .replace(R.id.main_activity, fragment)
                .addToBackStack(null)
                .commit()
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClick?.let { it1 -> it1(currentItem) }
            true
        }
    }

    private fun exist(video: Video): Boolean {
        val count = MainActivity.database.alreadyExist(video)
        return count > 0
    }

    override fun getItemCount() = videoList.size

}
