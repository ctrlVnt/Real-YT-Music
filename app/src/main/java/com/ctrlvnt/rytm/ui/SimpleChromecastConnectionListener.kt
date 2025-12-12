package com.ctrlvnt.rytm.ui

import android.util.Log
import androidx.annotation.NonNull
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener


class SimpleChromecastConnectionListener : ChromecastConnectionListener {
    public override fun onChromecastConnecting() {
        Log.d(javaClass.getSimpleName(), "onChromecastConnecting")
    }

    public override fun onChromecastConnected(@NonNull chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
        Log.d(javaClass.getSimpleName(), "onChromecastConnected")
        initializeCastPlayer(chromecastYouTubePlayerContext)
    }

    public override fun onChromecastDisconnected() {
        Log.d(javaClass.getSimpleName(), "onChromecastDisconnected")
    }

    fun initializeCastPlayer(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
        chromecastYouTubePlayerContext.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(@NonNull youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo("S0Q4gqBUs7c", 0f)
            }
        })
    }
}