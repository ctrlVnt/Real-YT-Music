package com.ctrlvnt.rytm.ui

import android.Manifest
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Rational
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ctrlvnt.rytm.R
import com.ctrlvnt.rytm.data.database.LocalDataBase
import com.ctrlvnt.rytm.ui.fragment.HomeActivity
import com.ctrlvnt.rytm.ui.fragment.Settings
import com.ctrlvnt.rytm.ui.fragment.YouTubePlayerSupport
import com.ctrlvnt.rytm.utils.apikey.SHAKEKEY
import com.ctrlvnt.rytm.utils.extractYoutubeId
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale
import com.shakebugs.shake.Shake


class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var database: LocalDataBase
            private set
    }

    lateinit var bottomNav : BottomNavigationView

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Shake.start(this, SHAKEKEY)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)!!
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_activity, HomeActivity())
                        .commitNow()
                    true
                }
                R.id.settings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_activity, Settings())
                        .commitNow()
                    true
                }
                else -> {
                    false
                }
            }
        }

        window.decorView.apply {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController =
                WindowCompat.getInsetsController(window, window.decorView)
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.navigationBars())
        }

        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        window.decorView.setBackgroundColor(resources.getColor(R.color.background))

        conditionandprivacyaccept(sharedPrefs)
        request_notification_api13_permission()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        database = LocalDataBase.getDatabase(this)

        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrEmpty()) {
                val videoId = extractYoutubeId(sharedText)
                if (videoId != null) {
                    openVideoFragment(videoId)
                    return
                }
            }
        }else if (intent?.action == Intent.ACTION_VIEW){ // here if app opened when you click directly the link
            val sharedText = intent.dataString
            if (sharedText != null) {
                val urlRegex = "(https?://[\\s\\S]*)".toRegex()
                val match = urlRegex.find(sharedText)
                val cleanUri = match?.value?.trim()?.toUri()
                val videoId = extractVideoIdFromUrl(cleanUri)
                if (videoId != null) {
                    openVideoFragment(videoId)
                    return
                }
            }

        }

        val uri = intent?.data
        val videoId = uri?.getQueryParameter("v")
        if (videoId != null) {
            val fragment = YouTubePlayerSupport.newInstance(videoId, "home")
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_activity, HomeActivity())
                .commitNow()

            supportFragmentManager.beginTransaction()
                .replace(R.id.main_activity, fragment)
                .addToBackStack(null)
                .commit()
        } else if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_activity, HomeActivity())
                .commit()
        }
        checkAndShowUpdatePopup(this)
    }

    private fun openVideoFragment(videoId: String) {
        val fragment = YouTubePlayerSupport.newInstance(videoId, "fromoutside")

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_activity, HomeActivity())
            .commitNow()

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_activity, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun setBottomNavVisibility(isVisible: Boolean) {
        if (isVisible) {
            bottomNav.visibility = android.view.View.VISIBLE
        } else {
            bottomNav.visibility = android.view.View.GONE
        }
    }

    private fun conditionandprivacyaccept(sharedPrefs: SharedPreferences) {
        if (!sharedPrefs.getBoolean("terms_accepted", false)) {
            
            val intent = Intent(this, TutorialActivity::class.java)
            startActivity(intent)

            val dialogView = layoutInflater.inflate(R.layout.dialog_terms_and_privacy, null)
            val alertDialogBuilder = MaterialAlertDialogBuilder(this, R.style.RoundedAlertDialog)
            alertDialogBuilder.setView(dialogView)

            val mTextView: TextView = dialogView.findViewById(R.id.terms_condition)
            mTextView.movementMethod = LinkMovementMethod.getInstance()

            alertDialogBuilder.setTitle(getString(R.string.terms_title))
            alertDialogBuilder.setPositiveButton(getString(R.string.accept)) { dialog, _ ->
                sharedPrefs.edit {
                    putBoolean("terms_accepted", true)
                }
                dialog.dismiss()
            }
            alertDialogBuilder.setCancelable(false)
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }

    private fun request_notification_api13_permission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf<String>(Manifest.permission.POST_NOTIFICATIONS),
                    22
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUserLeaveHint() {

        val layoutParams = window.attributes
        layoutParams.screenBrightness =
            WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.attributes = layoutParams

        enterPictureInPictureMode(
            PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .build())
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("app_lang", "en") ?: "en"
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun getAppVersionCode(context: Context): Long {
        return context.packageManager
            .getPackageInfo(context.packageName, 0).longVersionCode
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun checkAndShowUpdatePopup(context: Context) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val savedVersion = prefs.getLong("last_seen_version", -1)
        val currentVersion = getAppVersionCode(context)

        if (currentVersion > savedVersion) {
            showUpdateDialog(context)
            prefs.edit { putLong("last_seen_version", currentVersion) }
        }
    }

    private fun extractVideoIdFromUrl(uri: Uri?): String? {
        if (uri == null) return null

        return when (uri.host) {
            "youtu.be" -> {
                uri.pathSegments.firstOrNull()
            }
            "www.youtube.com", "m.youtube.com", "youtube.com" -> {
                if (uri.pathSegments.contains("shorts")) {
                    uri.pathSegments.getOrNull(1)
                } else {
                    uri.getQueryParameter("v")
                }
            }
            else -> null
        }
    }

    fun showUpdateDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("What's New in This Update")
            .setMessage(
                """
            You’ve just installed a new version! Here’s what’s new:
            
            • Added possible Sign-In YT thing solution
            • Lock screen button for videos from out of RYTM
           
            """.trimIndent()
            )
            .setPositiveButton("OK", null)
            .show()
    }
}
