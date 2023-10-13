package com.ctrlvnt.rytm.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
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
            MainActivity.database.deleteAllVideos()
            Toast.makeText(this.context, getString(R.string.deleted_caches), Toast.LENGTH_SHORT).show()
        }
        return rootView
    }
}