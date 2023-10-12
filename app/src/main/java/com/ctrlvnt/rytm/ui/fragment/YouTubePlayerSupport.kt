package com.ctrlvnt.rytm.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ctrlvnt.rytm.R
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView


class YouTubePlayerSupport : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_youtube_player, container, false)

        val videoId = arguments?.getString("video_id")

        //full screen
        val iFramePlayerOptions = IFramePlayerOptions.Builder()
            .controls(1) // enable full screen button
            .fullscreen(1)
            .build()

        val youTubePlayerView: YouTubePlayerView = rootView.findViewById(R.id.youtube_player_view)

        //viewLifecycleOwner.lifecycle.addObserver(youTubePlayerView) //consigliato
        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                //youTubePlayerView.enableBackgroundPlayback(true) //background music, not legal
                videoId?.let {
                    youTubePlayer.loadVideo(it, 0f)
                }
            }
        })

        return rootView
    }

    companion object {
        fun newInstance(videoId: String): YouTubePlayerSupport {
            val fragment = YouTubePlayerSupport()
            val args = Bundle()
            args.putString("video_id", videoId)
            fragment.arguments = args
            return fragment
        }
    }
}
