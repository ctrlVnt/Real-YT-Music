package com.ctrlvnt.rytm.ui.fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.net.toUri
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.ctrlvnt.rytm.R
import com.ctrlvnt.rytm.ui.TutorialActivity
import com.ctrlvnt.rytm.utils.setLocale
import com.shakebugs.shake.Shake
import com.shakebugs.shake.ShakeScreen

class Settings : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        setupClickablePreference("back"){
            requireActivity().supportFragmentManager.popBackStack()
        }

        val languagePref: ListPreference? = findPreference("app_language")
        languagePref?.setOnPreferenceChangeListener { _, newValue ->
            val langCode = newValue.toString()
            setLocale(langCode, requireContext(), requireActivity())
            true
        }

        setupClickablePreference("buy_me_a_coffee") {
            openUrl("https://buymeacoffee.com/v3ntuz")
        }

        setupClickablePreference("report_bug") {
            openUrl("https://github.com/ctrlVnt/Real-YT-Music/issues")
        }

        setupClickablePreference("visit_website") {
            openUrl("https://riccardoventurini.dev/")
        }

        setupClickablePreference("rate_app") {
            openUrl("https://play.google.com/store/apps/details?id=com.ctrlvnt.rytm")
        }

        setupClickablePreference("github") {
            openUrl("https://github.com/ctrlVnt/Real-YT-Music")
        }

        setupClickablePreference("tutorial") {
            val intent = Intent(requireContext(), TutorialActivity::class.java)
            startActivity(intent)
        }

        setupClickablePreference("send_email") {
            val i = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("giordanobruno227@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "I have a suggestion for RYTM!")
            }
            try {
                startActivity(Intent.createChooser(i, "Send mail..."))
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show()
            }
        }
        setupClickablePreference("report_bug"){
            Shake.show(ShakeScreen.NEW)
        }
    }

    private fun setupClickablePreference(key: String, action: () -> Unit) {
        findPreference<Preference>(key)?.setOnPreferenceClickListener {
            action()
            true
        }
    }

    private fun openUrl(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(browserIntent)
    }
}