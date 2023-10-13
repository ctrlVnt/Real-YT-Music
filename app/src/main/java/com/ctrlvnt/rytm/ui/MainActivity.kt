package com.ctrlvnt.rytm.ui

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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


        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_activity, HomeActivity())
                .commit()
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        database = LocalDataBase.getDatabase(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUserLeaveHint() {
        enterPictureInPictureMode(
            PictureInPictureParams.Builder()
            .setAspectRatio(Rational(2, 3))
            .build())
    }

}
