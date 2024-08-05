package com.ctrlvnt.rytm.ui.fragment

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
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
        val backButton: ImageButton = rootView.findViewById(R.id.back_button)

        backButton.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }

        val mTextView: TextView = rootView.findViewById(R.id.my_info)
        mTextView.movementMethod = LinkMovementMethod.getInstance()

        val mTextViewAbout: TextView = rootView.findViewById(R.id.about)
        mTextViewAbout.movementMethod = LinkMovementMethod.getInstance()

        return rootView
    }
}