package com.ctrlvnt.rytm.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ctrlvnt.rytm.R
import com.ctrlvnt.rytm.ui.fragment.HomeActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_activity, HomeActivity())
                .commit()
        }
    }
}
