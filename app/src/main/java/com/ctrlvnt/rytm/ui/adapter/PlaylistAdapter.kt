package com.ctrlvnt.rytm.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.ctrlvnt.rytm.R
import com.ctrlvnt.rytm.data.database.entities.Playlist
import com.ctrlvnt.rytm.ui.MainActivity
import com.ctrlvnt.rytm.ui.fragment.YouTubePlayerSupport

class PlaylistAdapter(private var playlistList: List<Playlist>, private val onItemLongClick: ((Playlist) -> Unit)? = null) :
    RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    fun updatePlaylistList(newPlaylists: List<Playlist>) {
        playlistList = newPlaylists
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(playlistItem: Playlist)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: PlaylistAdapter.OnItemClickListener) {
        this.onItemClickListener = listener
    }

    fun getPlaylistAtPosition(position: Int): Playlist {
        return playlistList[position]
    }

    inner class PlaylistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playlistTitle: TextView = itemView.findViewById(R.id.playlist_title_item)

        init {
            itemView.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val playlistToDelete = playlistList[position]
                }
                true
            }

            itemView.setOnClickListener{
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.onItemClick(playlistList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val currentItem = playlistList[position]
        holder.playlistTitle.text = currentItem.playlistName

        holder.itemView.setOnClickListener {

            var videos = MainActivity.database.playlisVideotDao().getPlaylistVideos(playlistList[position].playlistName)

            if (videos.isEmpty()){

            }else{
                val fragment = YouTubePlayerSupport.newInstance(videos[0].id, playlistList[position].playlistName)
                val transaction = (holder.itemView.context as AppCompatActivity)
                    .supportFragmentManager.beginTransaction()
                    .replace(R.id.main_activity, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClick?.let { it1 -> it1(currentItem) }
            true
        }
    }

    override fun getItemCount() = playlistList.size
}