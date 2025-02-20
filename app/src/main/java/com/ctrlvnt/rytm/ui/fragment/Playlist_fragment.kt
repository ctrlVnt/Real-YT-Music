package com.ctrlvnt.rytm.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ctrlvnt.rytm.R
import com.ctrlvnt.rytm.data.database.entities.Playlist
import com.ctrlvnt.rytm.ui.MainActivity
import com.ctrlvnt.rytm.ui.adapter.PlaylistAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Playlist_fragment : Fragment(){

    lateinit var playlistList: RecyclerView
    lateinit var playlistText: TextView
    lateinit var playlistImg: ImageView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_playlist, container, false)
        val addButton: FloatingActionButton = rootView.findViewById(R.id.add_playlist)
        val backButton: ImageButton = rootView.findViewById(R.id.back_button)
        playlistText = rootView.findViewById(R.id.empty_playlists)
        playlistImg = rootView.findViewById(R.id.empty_playlists_img)

        playlistList = rootView.findViewById(R.id.list_playlist)
        val layoutManager = GridLayoutManager(context, 2)
        playlistList.layoutManager = layoutManager
        val playlists = MainActivity.database.playlistDao().getAllPlaylists()

        val playlistAdapter = PlaylistAdapter(playlists) { playlistItem ->
            showDeleteConfirmationDialog(playlistItem)
        }
        playlistList.adapter = playlistAdapter

        if(playlists.isEmpty()){
            playlistImg.visibility = View.VISIBLE
            playlistText.visibility = View.VISIBLE
        }else{
            playlistImg.visibility = View.GONE
            playlistText.visibility = View.GONE
        }

        addButton.setOnClickListener{
            showCustomDialog()
        }

        backButton.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }

        return rootView
    }

    private fun showCustomDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_text_edit, null)

        val editTextName: EditText = dialogView.findViewById(R.id.editTextName)

        builder.setView(dialogView)
            .setTitle(R.string.add_playlist)
            .setPositiveButton("OK") { dialog, _ ->
                val name =  editTextName.text.toString()

                if (name.isNotBlank() && MainActivity.database.playlistDao().alreadyExist(name) == 0) {
                    val newPlaylist = Playlist(playlistName = name)
                    MainActivity.database.playlistDao().insertPlaylist(newPlaylist)

                    val updatedPlaylists = MainActivity.database.playlistDao().getAllPlaylists()
                    (playlistList.adapter as PlaylistAdapter).updatePlaylistList(updatedPlaylists)
                    playlistImg.visibility = View.GONE
                    playlistText.visibility = View.GONE
                } else {
                    if(name.isBlank()){
                        Toast.makeText(requireContext(), R.string.error_empty_name, Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(requireContext(), R.string.error_already_exist, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(R.string.restore) { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun showDeleteConfirmationDialog(playlistItem: Playlist) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle(R.string.delete_confirmation_title)
        alertDialogBuilder.setMessage(R.string.delete_confirmation)

        alertDialogBuilder.setPositiveButton(R.string.delete) { _, _ ->
            MainActivity.database.deletePlaylist(playlistItem)
            refreshAdapter()
        }
        alertDialogBuilder.setNegativeButton(R.string.restore) { dialog, _ ->
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
        if(playlists.isEmpty()){
            playlistImg.visibility = View.VISIBLE
            playlistText.visibility = View.VISIBLE
        }
    }
}