package com.ctrlvnt.rytm.ui.fragment

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.ctrlvnt.rytm.R
import com.ctrlvnt.rytm.ui.MainActivity

class Settings : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        val cachesButton: Button = rootView.findViewById(R.id.delete_caches)

        cachesButton.setOnClickListener{
            showConfirmationDialog()
        }

        val mTextView: TextView = rootView.findViewById(R.id.my_info)
        mTextView.movementMethod = LinkMovementMethod.getInstance()

        val mTextViewAbout: TextView = rootView.findViewById(R.id.about)
        mTextViewAbout.movementMethod = LinkMovementMethod.getInstance()

        return rootView
    }

    private fun showConfirmationDialog() {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle(getString(R.string.confirm_delete_history))
        alertDialogBuilder.setMessage(getString(R.string.confirm_delete_history_message))

        alertDialogBuilder.setPositiveButton(getString(R.string.accept)) { dialog, _ ->
            MainActivity.database.deleteAllVideos()
            Toast.makeText(requireContext(), getString(R.string.deleted_history), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton(getString(R.string.restore)) { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

}