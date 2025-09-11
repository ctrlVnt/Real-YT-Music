package com.ctrlvnt.rytm.ui.fragment

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ctrlvnt.rytm.R
import java.util.Locale
import androidx.core.net.toUri
import androidx.core.content.edit
import com.ctrlvnt.rytm.ui.TutorialActivity


class Settings : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        val backButton: ImageButton = rootView.findViewById(R.id.back_button)
        val buymeacoffeeButton : Button = rootView.findViewById(R.id.buymeacoffee)
        val sendmemailButton : Button = rootView.findViewById(R.id.sendmeamail)
        val settingsButton : ImageButton = rootView.findViewById(R.id.language)
        val flagEmojiTextView: TextView = rootView.findViewById(R.id.flag_emoji)
        val visitmywebsiteButton : Button = rootView.findViewById(R.id.visitwebsite)
        val rateAppButton : Button = rootView.findViewById(R.id.rate_app)
        val tutorialButton : Button = rootView.findViewById(R.id.see_tutorial)
        val saveMinutesToggle: com.google.android.material.switchmaterial.SwitchMaterial = rootView.findViewById(R.id.save_minutes_toggle)

        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isSaveEnabled = prefs.getBoolean("save_minutes_enabled", false)
        saveMinutesToggle.isChecked = isSaveEnabled

        saveMinutesToggle.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit {
                putBoolean("save_minutes_enabled", isChecked)
            }
        }

        val flagEmoji = getFlagEmojiForLanguage(Locale.getDefault().language)
        flagEmojiTextView.text = flagEmoji

        backButton.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }

        buymeacoffeeButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, "https://buymeacoffee.com/v3ntuz".toUri())
            startActivity(browserIntent)
        }

        visitmywebsiteButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, "https://riccardoventurini.dev/".toUri())
            startActivity(browserIntent)
        }

        rateAppButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=com.ctrlvnt.rytm".toUri())
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

        settingsButton.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), settingsButton)
            popupMenu.menuInflater.inflate(R.menu.language_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.lang_en -> setLocale("en")
                    R.id.lang_ar -> setLocale("ar")
                    R.id.lang_de -> setLocale("de")
                    R.id.lang_es -> setLocale("es")
                    R.id.lang_fr -> setLocale("fr")
                    R.id.lang_hi -> setLocale("hi")
                    R.id.lang_it -> setLocale("it")
                    R.id.lang_ja -> setLocale("ja")
                    R.id.lang_pl -> setLocale("pl")
                    R.id.lang_pt -> setLocale("pt")
                    R.id.lang_ru -> setLocale("ru")
                    R.id.lang_uk -> setLocale("uk")
                    else -> false
                }
                true
            }

            popupMenu.show()
        }

        tutorialButton.setOnClickListener {
            val intent = Intent(requireContext(), TutorialActivity::class.java)
            startActivity(intent)
        }

        val mTextViewAbout: TextView = rootView.findViewById(R.id.about)
        mTextViewAbout.movementMethod = LinkMovementMethod.getInstance()

        return rootView
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit { putString("app_lang", languageCode) }

        requireActivity().recreate()
    }

    fun getFlagEmojiForLanguage(languageCode: String): String {
        return when (languageCode) {
            "en" -> "🇺🇸"
            "ar" -> "🇸🇦"
            "de" -> "🇩🇪"
            "es" -> "🇪🇸"
            "fr" -> "🇫🇷"
            "hi" -> "🇮🇳"
            "it" -> "🇮🇹"
            "ja" -> "🇯🇵"
            "pl" -> "🇵🇱"
            "pt" -> "🇵🇹"
            "ru" -> "🇷🇺"
            "uk" -> "🇺🇦"
            else -> "🌐"
        }
    }
}