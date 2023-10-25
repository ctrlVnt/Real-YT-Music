package com.ctrlvnt.rytm.ui

import android.Manifest
import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
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

        request_notification_api13_permission()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_activity, HomeActivity())
                .commit()
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        database = LocalDataBase.getDatabase(this)
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

    override fun onDestroy() {
        super.onDestroy()
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.cancel(1)
    }
}
