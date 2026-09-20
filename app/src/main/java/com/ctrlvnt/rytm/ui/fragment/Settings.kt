package com.ctrlvnt.rytm.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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
import com.ctrlvnt.rytm.ui.services.GithubIssueReporter

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
            showBugReportDialog()
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

    private fun showBugReportDialog() {
        val context = requireContext()

        val titleInput = android.widget.EditText(context).apply {
            hint = getString(R.string.bug_report_title_hint)
        }
        val descriptionInput = EditText(context).apply {
            hint = getString(R.string.bug_report_description_hint)
            minLines = 3
        }

        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 32, 48, 0)
            addView(titleInput)
            addView(descriptionInput)
        }

        MaterialAlertDialogBuilder(context, R.style.RoundedAlertDialog)
            .setTitle(getString(R.string.bug_report_dialog_title))
            .setView(layout)
            .setPositiveButton(getString(R.string.bug_report_send)) { dialog, _ ->
                val title = titleInput.text.toString().trim()
                val description = descriptionInput.text.toString().trim()

                if (title.isEmpty()) {
                    Toast.makeText(context, getString(R.string.bug_report_title_required), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                Toast.makeText(context, getString(R.string.bug_report_sending), Toast.LENGTH_SHORT).show()

                GithubIssueReporter.reportBug(
                    context = context,
                    title = title,
                    description = description.ifEmpty { null },
                    onSuccess = { issueUrl ->
                        requireActivity().runOnUiThread {
                            showSuccessDialog(issueUrl)
                        }
                    },
                    onError = {
                        requireActivity().runOnUiThread {
                            Toast.makeText(context, getString(R.string.bug_report_failed), Toast.LENGTH_LONG).show()
                        }
                    }
                )
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showSuccessDialog(issueUrl: String) {
        val context = requireContext()

        val messageView = TextView(context).apply {
            text = getString(R.string.bug_report_success_message, issueUrl)
            autoLinkMask = Linkify.WEB_URLS
            movementMethod = LinkMovementMethod.getInstance()
            setPadding(48, 32, 48, 32)
        }

        MaterialAlertDialogBuilder(context, R.style.RoundedAlertDialog)
            .setTitle(getString(R.string.bug_report_success_title))
            .setView(messageView)
            .setPositiveButton(getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .show()
    }
}