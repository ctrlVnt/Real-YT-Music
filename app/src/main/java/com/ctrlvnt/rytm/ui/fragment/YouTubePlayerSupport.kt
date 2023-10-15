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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ctrlvnt.rytm.R
import com.ctrlvnt.rytm.data.database.entities.PlaylistVideo
import com.ctrlvnt.rytm.data.database.entities.Video
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
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlin.random.Random


class YouTubePlayerSupport : Fragment(), VideoAdapter.OnItemClickListener {

    private lateinit var videoList: RecyclerView
    private lateinit var youTubePlayerView: YouTubePlayerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_youtube_player, container, false)
        val videoId = arguments?.getString("video_id")
        val playlistTitle = arguments?.getString("playlist_name")
        val lockButton: FloatingActionButton = rootView.findViewById(R.id.lock_screen)
        val overlay: Button = rootView.findViewById(R.id.mask_lock)
        youTubePlayerView = rootView.findViewById(R.id.youtube_player_view)
        val repeat: ImageButton = rootView.findViewById(R.id.repeat)
        val shuffle: ImageButton = rootView.findViewById(R.id.shuffle)
        val playlistAdd: ImageButton = rootView.findViewById(R.id.add_playlist)
        val playlisName: TextView = rootView.findViewById(R.id.playlist_name)
        val videos: List<Video>

        var lock = false
        var repeatOption = false
        var shuffleOption = false

        videoList = rootView.findViewById(R.id.playlist)
        val layoutManager = LinearLayoutManager(context)
        videoList.layoutManager = layoutManager

        if(playlistTitle == null || playlistTitle == ""){
            playlisName.text = "Cronologia"

            videos = MainActivity.database.videoDao().getAll()

        }else{
            playlisName.text = playlistTitle

            videos = MainActivity.database.playlisVideotDao().getPlaylistVideos(playlistTitle)
        }

        val videoItems = videos?.map {
            var videoid = VideoId(it.id)
            var snippet = Snippet(it.title, it.channelTitle)
            VideoItem(videoid, snippet)
        } ?: emptyList()

        val videoAdapter = VideoAdapter(videoItems, null, "yt_player")
        videoAdapter.setOnPlaybackClickListener(object : VideoAdapter.OnPlaybackClickListener {
            override fun onPlaybackClick(videoItem: VideoItem) {
                onItemClick(videoItem)
            }
        })
        videoList.adapter = videoAdapter

        repeat.setOnClickListener {
            repeatOption = !repeatOption
            if (repeatOption){
                repeat.setColorFilter(resources.getColor(R.color.yt_red), PorterDuff.Mode.SRC_IN)
            }else{
                repeat.clearColorFilter()
            }
        }

        shuffle.setOnClickListener {
            shuffleOption = !shuffleOption
            if (shuffleOption){
                shuffle.setColorFilter(resources.getColor(R.color.yt_red), PorterDuff.Mode.SRC_IN)
            }else{
                shuffle.clearColorFilter()
            }
        }

        overlay.visibility = View.GONE

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

        val nextVideo = videos

        viewLifecycleOwner.lifecycle.addObserver(youTubePlayerView)
        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {

            var indexVideo = if (nextVideo.size > 1) nextVideo.size - 1 else 0

            override fun onReady(youTubePlayer: YouTubePlayer) {
                videoId?.let {
                    youTubePlayer.loadVideo(it, 0f)

                    playlistAdd.setOnClickListener {
                        showPlaylistDialog(videoId)
                    }

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

    private fun showPlaylistDialog(videoId: String) {
        val video = MainActivity.database.videoDao().getVideo(videoId)
        val playlists = MainActivity.database.playlistDao().getAllPlaylists()
        val playlistNames = playlists.map { it.playlistName }.toTypedArray()

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Seleziona una playlist")
            .setItems(playlistNames) { _, which ->

                val selectedPlaylist = playlists[which]

                val element = PlaylistVideo(selectedPlaylist.playlistName, video.id, video.title, video.channelTitle)

                MainActivity.database.playlisVideotDao().insertVideoToPlaylist(element)
            }

        val dialog = builder.create()
        dialog.show()
    }


    companion object {
        fun newInstance(videoId: String, playlist_name: String): YouTubePlayerSupport {
            val fragment = YouTubePlayerSupport()
            val args = Bundle()
            args.putString("video_id", videoId)
            args.putString("playlist_name", playlist_name)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onItemClick(videoItem: VideoItem) {
        val videoId = videoItem.id

        val playerCallback = object : YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(videoId.videoId, 0f)
            }
        }

        youTubePlayerView.getYouTubePlayerWhenReady(playerCallback)
    }
}
