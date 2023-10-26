package com.ctrlvnt.rytm.ui

import android.Manifest
import android.app.PictureInPictureParams
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Rational
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import com.ctrlvnt.rytm.R
import com.ctrlvnt.rytm.data.database.LocalDataBase
import com.ctrlvnt.rytm.ui.fragment.HomeActivity


class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var database: LocalDataBase
            private set
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        conditionandprivacyaccept(sharedPrefs)
        request_notification_api13_permission()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_activity, HomeActivity())
                .commit()
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        database = LocalDataBase.getDatabase(this)
    }

    private fun conditionandprivacyaccept(sharedPrefs: SharedPreferences) {
        if (!sharedPrefs.getBoolean("terms_accepted", false)) {
            val dialogView = layoutInflater.inflate(R.layout.dialog_terms_and_privacy, null)
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setView(dialogView)

            val mTextView: TextView = dialogView.findViewById(R.id.terms_condition)
            mTextView.movementMethod = LinkMovementMethod.getInstance()

            alertDialogBuilder.setTitle(getString(R.string.terms_title))
            alertDialogBuilder.setPositiveButton(getString(R.string.accept)) { dialog, _ ->
                val editor = sharedPrefs.edit()
                editor.putBoolean("terms_accepted", true)
                editor.apply()
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
        enterPictureInPictureMode(
            PictureInPictureParams.Builder()
            .setAspectRatio(Rational(2, 3))
            .build())
    }
}
