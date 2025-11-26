package com.ctrlvnt.rytm.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ctrlvnt.rytm.R
import com.ctrlvnt.rytm.data.database.entities.Video
import com.ctrlvnt.rytm.data.model.VideoItem
import com.ctrlvnt.rytm.ui.MainActivity
import com.ctrlvnt.rytm.ui.fragment.YouTubePlayerSupport
import androidx.core.graphics.toColorInt
import com.ctrlvnt.rytm.data.YouTubeApiManager
import com.ctrlvnt.rytm.data.database.entities.Playlist
import com.ctrlvnt.rytm.data.database.entities.PlaylistVideo
import com.ctrlvnt.rytm.data.model.PlaylistItemsResponse
import com.ctrlvnt.rytm.data.model.SearchResponse
import com.ctrlvnt.rytm.utils.apikey.APIKEY
import com.ctrlvnt.rytm.utils.savePlaylistFromApi
import com.google.android.material.button.MaterialButton
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Collections

class VideoAdapter(private val videoList: List<VideoItem>,
                   private val onItemLongClick: ((VideoItem) -> Unit)? = null, //I hae this just for home
                   private val currentFragmentTag: String,
                   private val blackText: Boolean = false
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private var branoInRiproduzionePosition: Int? = null

    fun setBranoInRiproduzionePosition(position: Int?) {
        branoInRiproduzionePosition = position
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(videoItem: VideoItem)
    }

    private var onItemClickListener: OnItemClickListener? = null

    private var onPlaybackClickListener: OnPlaybackClickListener? = null
    fun setOnPlaybackClickListener(listener: OnPlaybackClickListener) {
        this.onPlaybackClickListener = listener
    }
    interface OnPlaybackClickListener {
        fun onPlaybackClick(videoItem: VideoItem)
    }

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoTitle: TextView = itemView.findViewById(R.id.video_title)
        val channelTitle: TextView = itemView.findViewById(R.id.channel_title)
        val isPlaylist: MaterialButton = itemView.findViewById(R.id.isPlaylist)

        val videoThumbnail: ImageView = itemView.findViewById(R.id.video_thumbnail)

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

    override fun onBindViewHolder(holder: VideoViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val currentItem = videoList[position]

        holder.videoTitle.text = Html.fromHtml(currentItem.snippet.title, Html.FROM_HTML_MODE_LEGACY).toString()
        holder.channelTitle.text = Html.fromHtml(currentItem.snippet.channelTitle, Html.FROM_HTML_MODE_LEGACY).toString()

        if (currentItem.id.videoId == null) {
            holder.isPlaylist.visibility = View.VISIBLE
        } else {
            holder.isPlaylist.visibility = View.GONE
        }

        if (position == branoInRiproduzionePosition) {
            holder.videoTitle.setTypeface(null, Typeface.BOLD)
        } else {
            holder.videoTitle.setTypeface(null, Typeface.NORMAL)
        }

        if (blackText) {
            holder.videoTitle.setTextColor("#282828".toColorInt())
            holder.channelTitle.setTextColor("#282828".toColorInt())
        }

        Glide.with(holder.videoThumbnail.context)
            .load(currentItem.snippet.thumbnails.medium.url)
            .into(holder.videoThumbnail)

        if (currentFragmentTag == "home") {
            holder.itemView.setOnClickListener {
                if(currentItem.id.videoId == null){
                    savePlaylistFromApi(holder.itemView.context, currentItem, currentItem.snippet.title)
                }
                else{
                    val video = Video(
                        videoList[position].id.videoId.toString(),
                        videoList[position].snippet.title,
                        videoList[position].snippet.channelTitle,
                        videoList[position].snippet.thumbnails.medium.url
                    )
                    if (!exist(video)) {
                        //Add video to history
                        MainActivity.database.insertVideo(video)
                    }
                    val fragment = YouTubePlayerSupport.newInstance(videoList[position].id.videoId.toString(), "")
                    (holder.itemView.context as AppCompatActivity).supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slow_fade, 0, R.anim.slow_fade, 0)
                        .replace(R.id.main_activity, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        } else {
            holder.itemView.setOnClickListener {
                branoInRiproduzionePosition = position
                notifyDataSetChanged()
                onPlaybackClickListener?.onPlaybackClick(videoList[position])
            }
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClick?.invoke(currentItem)
            true
        }
    }


    private fun exist(video: Video): Boolean {
        val count = MainActivity.database.alreadyExist(video)
        return count > 0
    }

    override fun getItemCount() = videoList.size

    fun moveItem(from: Int, to: Int) {
        if (from < to) {
            for (i in from until to) {
                Collections.swap(videoList, i, i + 1)
            }
        } else {
            for (i in from downTo to + 1) {
                Collections.swap(videoList, i, i - 1)
            }
        }
        notifyItemMoved(from, to)
    }

    fun getItemAt(position: Int): VideoItem {
        return videoList[position]
    }

}
