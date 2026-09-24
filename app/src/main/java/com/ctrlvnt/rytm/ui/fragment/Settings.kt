package com.ctrlvnt.rytm.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.ctrlvnt.rytm.R
import com.ctrlvnt.rytm.ui.TutorialActivity
import com.ctrlvnt.rytm.utils.setLocale
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class Settings : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val languagePref: ListPreference? = findPreference("app_language")
        languagePref?.setOnPreferenceChangeListener { _, newValue ->
            val langCode = newValue.toString()
            setLocale(langCode, requireContext(), requireActivity())
            true
        }

        val systemNavBarPref: SwitchPreferenceCompat? = findPreference("show_system_nav_bar")
        systemNavBarPref?.setOnPreferenceChangeListener { _, newValue ->
            val show = newValue as Boolean
            applySystemNavBarVisibility(show)
            true
        }

        val versionPref: Preference? = findPreference("version")

        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        versionPref?.summary = getString(R.string.settings_version, packageInfo.versionName)

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
            val shareText = getString(R.string.share_app_text, playStoreLink)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_app_subject))
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app_chooser_title)))
        }

        setupClickablePreference("report_bug"){
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_activity, BugReportFragment())
                .addToBackStack(null)
                .commit()
        }

        setupClickablePreference("faq") {
            MaterialAlertDialogBuilder(requireContext(), R.style.RoundedAlertDialog)
                .setTitle(getString(R.string.faq_dialog_title))
                .setMessage(getString(R.string.faq_dialog_message))
                .setPositiveButton(getString(R.string.got_it)) { dialog, _ ->
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

    private fun applySystemNavBarVisibility(show: Boolean) {
        val window = requireActivity().window
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)

        if (show) {
            insetsController.show(WindowInsetsCompat.Type.navigationBars())
        } else {
            insetsController.hide(WindowInsetsCompat.Type.navigationBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}