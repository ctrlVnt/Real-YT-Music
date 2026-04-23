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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shakebugs.shake.Shake
import com.shakebugs.shake.ShakeScreen

class Settings : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val languagePref: ListPreference? = findPreference("app_language")
        languagePref?.setOnPreferenceChangeListener { _, newValue ->
            val langCode = newValue.toString()
            setLocale(langCode, requireContext(), requireActivity())
            true
        }

        val versionPref: Preference? = findPreference("version")

        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        versionPref?.summary = "version " + packageInfo.versionName

        setupClickablePreference("buy_me_a_coffee") {
            openUrl("https://buymeacoffee.com/v3ntuz")
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

        setupClickablePreference("share_app") {
            val playStoreLink = "https://play.google.com/store/apps/details?id=com.ctrlvnt.rytm"
            val shareText = "Check out RYTM! Watch YouTube videos without asd ! Download from PlayStore now : $playStoreLink"

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "RYTM - Real YT Music")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            startActivity(Intent.createChooser(shareIntent, "Share RYTM via..."))
        }

        setupClickablePreference("report_bug"){
            Shake.show(ShakeScreen.NEW)
        }

        setupClickablePreference("faq") {
            val faqMessage = """
        Sometimes, YouTube may ask you to sign in to confirm you are not a bot. 
        
        This is a server-side check enforced directly by YouTube, not a bug in RYTM. Google occasionally flags anonymous traffic to prevent spam and restrict ad-blockers. (RYTM is not an ad-blocker)
        
        Here is what you can do to fix it:
        
        • Clear Cache: Go to your phone's Settings > Apps > RYTM > Storage and clear the cache. This can sometimes reset your anonymous session.
        
        • Switch Network: Changing from Wi-Fi to mobile data, or temporarily using a VPN, can bypass temporary IP blocks.
        
        You can retry later, sometimes the situation resolves itself. 
    """.trimIndent()

            MaterialAlertDialogBuilder(requireContext(), R.style.RoundedAlertDialog)
                .setTitle("FAQ: YouTube Sign-in Requests")
                .setMessage(faqMessage)
                .setPositiveButton("Got it") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
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