package com.ctrlvnt.rytm.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ctrlvnt.rytm.R
import com.ctrlvnt.rytm.data.database.entities.Playlist

class PlaylistAdapter(private var playlistList: List<Playlist>, private val onItemLongClick: ((Playlist) -> Unit)? = null) :
    RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    fun updatePlaylistList(newPlaylists: List<Playlist>) {
        playlistList = newPlaylists
        notifyDataSetChanged()
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
                    // Chiedi conferma per l'eliminazione
                    val playlistToDelete = playlistList[position]
                }
                true
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

        holder.itemView.setOnLongClickListener {
            onItemLongClick?.let { it1 -> it1(currentItem) }
            true
        }
    }

    override fun getItemCount() = playlistList.size
}