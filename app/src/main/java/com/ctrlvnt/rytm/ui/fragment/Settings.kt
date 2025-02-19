package com.ctrlvnt.rytm.ui.fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ctrlvnt.rytm.R


class Settings : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        val backButton: ImageButton = rootView.findViewById(R.id.back_button)
        val buymeacoffeeButton : Button = rootView.findViewById(R.id.buymeacoffee)
        val sendmemailButton : Button = rootView.findViewById(R.id.sendmeamail)

        backButton.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }

        buymeacoffeeButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/v3ntuz"))
            startActivity(browserIntent)
        }

        sendmemailButton.setOnClickListener {
            val i = Intent(Intent.ACTION_SEND)
            i.setType("message/rfc822")
            i.putExtra(Intent.EXTRA_EMAIL, arrayOf("giordanobruno227@gmail.com"))
            i.putExtra(Intent.EXTRA_SUBJECT, "I have a suggestion for RYTM!")
            try {
                startActivity(Intent.createChooser(i, "Send mail..."))
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(
                    context,
                    "There are no email clients installed.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val mTextView: TextView = rootView.findViewById(R.id.my_info)
        mTextView.movementMethod = LinkMovementMethod.getInstance()

        val mTextViewAbout: TextView = rootView.findViewById(R.id.about)
        mTextViewAbout.movementMethod = LinkMovementMethod.getInstance()

        return rootView
    }
}