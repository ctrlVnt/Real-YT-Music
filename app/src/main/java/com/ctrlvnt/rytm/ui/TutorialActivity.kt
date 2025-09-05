package com.ctrlvnt.rytm.ui

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.ctrlvnt.rytm.R
import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;
import com.github.appintro.AppIntroPageTransformerType;

class TutorialActivity : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make sure you don't call setContentView!

        // Call addSlide passing your Fragments.
        // You can use AppIntroFragment to use a pre-built fragment
        addSlide(AppIntroFragment.createInstance(
            title = getString(R.string.intro_title_welcome),
            description = getString(R.string.intro_desc_welcome),
            imageDrawable = R.drawable.cover
        ))
        addSlide(AppIntroFragment.createInstance(
            title = getString(R.string.intro_title_home),
            description = getString(R.string.intro_desc_home),
            imageDrawable = R.drawable.slide1
        ))
        addSlide(AppIntroFragment.createInstance(
            title = getString(R.string.intro_title_player),
            description = getString(R.string.intro_desc_player),
            imageDrawable = R.drawable.slide2
        ))
        addSlide(AppIntroFragment.createInstance(
            title = getString(R.string.intro_title_pip),
            description = getString(R.string.intro_desc_pip),
            imageDrawable = R.drawable.slide4
        ))
        addSlide(AppIntroFragment.createInstance(
            title = getString(R.string.intro_title_limit),
            description = getString(R.string.intro_desc_limit),
            imageDrawable = R.drawable.slide3
        ))
        addSlide(AppIntroFragment.createInstance(
            title = getString(R.string.intro_title_end),
            description = getString(R.string.intro_desc_end),
            imageDrawable = R.drawable.settings
        ))
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Decide what to do when the user clicks on "Skip"
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        // Decide what to do when the user clicks on "Done"
        finish()
    }
}