package com.ctrlvnt.rytm.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.ctrlvnt.rytm.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Playlist : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_playlist, container, false)
        val addButton: FloatingActionButton = rootView.findViewById(R.id.add_playlist)

        addButton.setOnClickListener{
            showCustomDialog()
        }

        return rootView
    }

    private fun showCustomDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_playlist, null)

        val editTextName: EditText = dialogView.findViewById(R.id.editTextName)

        builder.setView(dialogView)
            .setTitle("Aggiungi Playlist")
            .setPositiveButton("OK") { dialog, _ ->
                val name =  editTextName.text.toString()

               //to do...
            }
            .setNegativeButton("Annulla") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}