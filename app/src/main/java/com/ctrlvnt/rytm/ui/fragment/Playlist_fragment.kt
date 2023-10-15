package com.ctrlvnt.rytm.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ctrlvnt.rytm.R
import com.ctrlvnt.rytm.data.database.entities.Playlist
import com.ctrlvnt.rytm.ui.MainActivity
import com.ctrlvnt.rytm.ui.adapter.PlaylistAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Playlist_fragment : Fragment(){

    lateinit var playlistList: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_playlist, container, false)
        val addButton: FloatingActionButton = rootView.findViewById(R.id.add_playlist)

        playlistList = rootView.findViewById(R.id.list_playlist)
        val layoutManager = LinearLayoutManager(context)
        playlistList.layoutManager = layoutManager
        val playlists = MainActivity.database.playlistDao().getAllPlaylists()

        val playlistAdapter = PlaylistAdapter(playlists) { playlistItem ->
            showDeleteConfirmationDialog(playlistItem)
        }
        playlistList.adapter = playlistAdapter

        addButton.setOnClickListener{
            showCustomDialog()
        }

        return rootView
    }

    private fun showCustomDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_text_edit, null)

        val editTextName: EditText = dialogView.findViewById(R.id.editTextName)

        builder.setView(dialogView)
            .setTitle("Aggiungi Playlist")
            .setPositiveButton("OK") { dialog, _ ->
                val name =  editTextName.text.toString()
                val newPlaylist = Playlist(playlistName = name)
                MainActivity.database.playlistDao().insertPlaylist(newPlaylist)

                val updatedPlaylists = MainActivity.database.playlistDao().getAllPlaylists()
                (playlistList.adapter as PlaylistAdapter).updatePlaylistList(updatedPlaylists)
            }
            .setNegativeButton("Annulla") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun showDeleteConfirmationDialog(playlistItem: Playlist) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Eliminare questo elemento?")
        alertDialogBuilder.setMessage("Sei sicuro di voler eliminare questo elemento?")

        alertDialogBuilder.setPositiveButton("Elimina") { _, _ ->
            MainActivity.database.deletePlaylist(playlistItem)
            refreshAdapter()
        }
        alertDialogBuilder.setNegativeButton("Annulla") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun refreshAdapter() {
        val playlists = MainActivity.database.playlistDao().getAllPlaylists()
        playlistList.adapter = PlaylistAdapter(playlists) { playlistItem ->
            showDeleteConfirmationDialog(playlistItem)
        }
    }
}