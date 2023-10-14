package com.ctrlvnt.rytm.ui.fragment


import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ctrlvnt.rytm.R
import com.ctrlvnt.rytm.data.model.Snippet
import com.ctrlvnt.rytm.data.model.VideoId
import com.ctrlvnt.rytm.data.model.VideoItem
import com.ctrlvnt.rytm.ui.MainActivity
import com.ctrlvnt.rytm.ui.adapter.VideoAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlin.random.Random


class YouTubePlayerSupport : Fragment() {

    lateinit var cronologia: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_youtube_player, container, false)
        val videoId = arguments?.getString("video_id")
        val playlistTitle = arguments?.getString("playlist_name")
        val lockButton: FloatingActionButton = rootView.findViewById(R.id.lock_screen)
        val overlay: Button = rootView.findViewById(R.id.mask_lock)
        val youTubePlayerView: YouTubePlayerView = rootView.findViewById(R.id.youtube_player_view)
        val repeat: ImageButton = rootView.findViewById(R.id.repeat)
        val shuffle: ImageButton = rootView.findViewById(R.id.shuffle)
        val playlistAdd: ImageButton = rootView.findViewById(R.id.add_playlist)
        val playlisName: TextView = rootView.findViewById(R.id.playlist_name)

        var lock = false
        var repeatOption = false
        var shuffleOption = false

        if(playlistTitle == null){
            playlisName.setText("Cronologia")
        }

        repeat.setOnClickListener {
            repeatOption = !repeatOption
            if (repeatOption){
                repeat.setColorFilter(resources.getColor(R.color.purple_200), PorterDuff.Mode.SRC_IN)
            }else{
                repeat.clearColorFilter()
            }
        }

        shuffle.setOnClickListener {
            shuffleOption = !shuffleOption
            if (shuffleOption){
                shuffle.setColorFilter(resources.getColor(R.color.purple_200), PorterDuff.Mode.SRC_IN)
            }else{
                shuffle.clearColorFilter()
            }
        }

        overlay.visibility = View.GONE

        cronologia = rootView.findViewById(R.id.playlist)
        val layoutManager = LinearLayoutManager(context)
        cronologia.layoutManager = layoutManager

        val videos = MainActivity.database.videoDao().getAll()
        val videoItems = videos?.map {
            var videoid = VideoId(it.id)
            var snippet = Snippet(it.title, it.channelTitle)
            VideoItem(videoid, snippet)
        } ?: emptyList()

        cronologia.adapter = VideoAdapter(videoItems)

        lockButton.setOnClickListener {
            lock = !lock

            if(lock){
                lockButton.setImageResource(R.drawable.baseline_lock_24)
                overlay.visibility = View.VISIBLE

                (activity as? AppCompatActivity)?.let {
                    val layoutParams = it.window.attributes
                    layoutParams.screenBrightness = 0.1f
                    it.window.attributes = layoutParams
                }
            }else{
                lockButton.setImageResource(R.drawable.baseline_lock_open_24)
                overlay.visibility = View.GONE

                (activity as? AppCompatActivity)?.let {
                    val layoutParams = it.window.attributes
                    layoutParams.screenBrightness =
                        WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                    it.window.attributes = layoutParams
                }
            }
        }

        val nextVideo = MainActivity.database.videoDao().getAll()

        viewLifecycleOwner.lifecycle.addObserver(youTubePlayerView)
        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {

            var indexVideo = if (nextVideo.size > 1) nextVideo.size - 1 else 0

            override fun onReady(youTubePlayer: YouTubePlayer) {
                videoId?.let {
                    youTubePlayer.loadVideo(it, 0f)
                }
            }
            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {
                super.onStateChange(youTubePlayer, state)
                if (state == PlayerConstants.PlayerState.ENDED){
                    if(repeatOption){
                        videoId?.let {
                            youTubePlayer.loadVideo(it, 0f)
                        }
                    }else if(shuffleOption) {
                        val randomIndex = Random.nextInt(0, nextVideo.size)
                        youTubePlayer.loadVideo(nextVideo[randomIndex].id, 0f)
                    }else{
                        if(indexVideo < 0){
                            indexVideo = nextVideo.size - 1
                        }
                        if(nextVideo.size == 0){
                            indexVideo = 0
                        }
                        youTubePlayer.loadVideo(nextVideo[indexVideo].id, 0f)
                        indexVideo--
                    }
                }
            }
        })

        youTubePlayerView.addFullscreenListener(object : FullscreenListener {
           override fun onEnterFullscreen(fullscreenView: View, exitFullscreen: () -> Unit) {
               lockButton.visibility = View.GONE
            }

            override fun onExitFullscreen() {
                lockButton.visibility = View.VISIBLE
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
