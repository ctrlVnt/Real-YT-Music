package com.ctrlvnt.rytm.ui.fragment


import android.content.res.Configuration
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlin.random.Random


class YouTubePlayerSupport : Fragment(), VideoAdapter.OnItemClickListener {

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

    private lateinit var videoList: RecyclerView
    private lateinit var youTubePlayerView: YouTubePlayerView
    private lateinit var playlisName: TextView
    private lateinit var playlistAdd: ImageButton
    private lateinit var buttonPannel: ConstraintLayout
    private lateinit var prevButton: ImageButton
    private lateinit var nextButton: ImageButton
    private var originalMarginTop: Int = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_youtube_player, container, false)
        val videoId = arguments?.getString("video_id")
        val playlistTitle = arguments?.getString("playlist_name")
        val lockButton: FloatingActionButton = rootView.findViewById(R.id.lock_screen)
        val overlay: Button = rootView.findViewById(R.id.mask_lock)
        val repeat: ImageButton = rootView.findViewById(R.id.repeat)
        val shuffle: ImageButton = rootView.findViewById(R.id.shuffle)
        val buttonEditName: ImageButton = rootView.findViewById(R.id.edit_playlist_name)
        prevButton = rootView.findViewById(R.id.prev_video)
        nextButton = rootView.findViewById(R.id.next_video)
        buttonPannel = rootView.findViewById(R.id.button_pannel)
        youTubePlayerView = rootView.findViewById(R.id.youtube_player_view)
        playlistAdd = rootView.findViewById(R.id.add_playlist)
        playlisName = rootView.findViewById(R.id.playlist_name)
        val videos: List<Video>

        var lock = false
        var repeatOption = false
        var shuffleOption = false

        overlay.visibility = View.GONE

        videoList = rootView.findViewById(R.id.playlist)
        val layoutManager = LinearLayoutManager(context)
        videoList.layoutManager = layoutManager

        if(playlistTitle == null || playlistTitle == ""){
            playlisName.text = "Cronologia"
            buttonEditName.visibility = View.GONE
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

        val videoAdapter = VideoAdapter(videoItems,{ videoItem ->
            if (playlistTitle != null && playlistTitle != "") {
                showDeleteConfirmationDialog(videoItem, playlistTitle)
            }
        }, "yt_player")

        videoAdapter.setOnPlaybackClickListener(object : VideoAdapter.OnPlaybackClickListener {
            override fun onPlaybackClick(videoItem: VideoItem) {
                onItemClick(videoItem)
            }
        })
        videoList.adapter = videoAdapter

        repeat.setOnClickListener {
            repeatOption = !repeatOption
            if (repeatOption){
                repeat.setColorFilter(resources.getColor(R.color.red), PorterDuff.Mode.SRC_IN)
            }else{
                repeat.clearColorFilter()
            }
        }

        shuffle.setOnClickListener {
            shuffleOption = !shuffleOption
            if (shuffleOption){
                shuffle.setColorFilter(resources.getColor(R.color.red), PorterDuff.Mode.SRC_IN)
            }else{
                shuffle.clearColorFilter()
            }
        }

        buttonEditName.setOnClickListener {
            if (playlistTitle != null) {
                showCustomDialog(playlistTitle)
            }
        }

        lockButton.setOnClickListener {
            lock = !lock

            if(lock){
                lockButton.setImageResource(R.drawable.baseline_lock_24)
                overlay.visibility = View.VISIBLE
                playlistAdd.visibility = View.GONE
                prevButton.visibility = View.GONE
                nextButton.visibility = View.GONE
                shuffle.visibility = View.GONE
                repeat.visibility = View.GONE

                (activity as? AppCompatActivity)?.let {
                    val layoutParams = it.window.attributes
                    layoutParams.screenBrightness = 0.1f
                    it.window.attributes = layoutParams
                }
            }else{
                lockButton.setImageResource(R.drawable.baseline_lock_open_24)
                overlay.visibility = View.GONE
                playlistAdd.visibility = View.VISIBLE
                prevButton.visibility = View.VISIBLE
                nextButton.visibility = View.VISIBLE
                shuffle.visibility = View.VISIBLE
                repeat.visibility = View.VISIBLE

                (activity as? AppCompatActivity)?.let {
                    val layoutParams = it.window.attributes
                    layoutParams.screenBrightness =
                        WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                    it.window.attributes = layoutParams
                }
            }
        }

        val nextVideo = videos
        var shuffleMode = mutableListOf<Int>()
        var shuffleindex = 0

        viewLifecycleOwner.lifecycle.addObserver(youTubePlayerView) //comment if you use playback mode
        //youTubePlayerView.enableBackgroundPlayback(true) //not legal, to comment!
        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {

            var indexVideo = if (nextVideo.size > 1) nextVideo.size - 1 else 0

            override fun onReady(youTubePlayer: YouTubePlayer) {
                videoId?.let {
                    val position = nextVideo.indexOfFirst { video -> video.id == it }
                    indexVideo = position
                    videoAdapter.setBranoInRiproduzionePosition(position)
                    youTubePlayer.loadVideo(it, 0f)
                    indexVideo++

                    playlistAdd.setOnClickListener {
                        showPlaylistDialog(videoId)
                    }

                    nextButton.setOnClickListener{
                        if(shuffleOption){
                            if(shuffleindex >= shuffleMode.size - 1){
                                val randomIndex = Random.nextInt(0, nextVideo.size)
                                videoAdapter.setBranoInRiproduzionePosition(randomIndex)
                                youTubePlayer.loadVideo(nextVideo[randomIndex].id, 0f)
                                shuffleMode.add(randomIndex)
                                shuffleindex++
                            }else{
                                shuffleindex++
                                videoAdapter.setBranoInRiproduzionePosition(shuffleMode[shuffleindex])
                                youTubePlayer.loadVideo(nextVideo[shuffleMode[shuffleindex]].id, 0f)
                            }
                        }else {
                            indexVideo++
                            if (indexVideo >= nextVideo.size) {
                                indexVideo = 0
                            }
                            videoAdapter.setBranoInRiproduzionePosition(indexVideo)
                            youTubePlayer.loadVideo(nextVideo[indexVideo].id, 0f)
                        }
                    }
                    prevButton.setOnClickListener{
                        if(shuffleOption){
                            if(shuffleindex <= 0){
                                shuffleindex = shuffleMode.size
                            }
                            shuffleindex--
                            videoAdapter.setBranoInRiproduzionePosition(shuffleMode[shuffleindex])
                            youTubePlayer.loadVideo(nextVideo[shuffleMode[shuffleindex]].id, 0f)
                        }else {
                            indexVideo--
                            if (indexVideo < 0) {
                                indexVideo = nextVideo.size - 1
                            }
                            videoAdapter.setBranoInRiproduzionePosition(indexVideo)
                            youTubePlayer.loadVideo(nextVideo[indexVideo].id, 0f)
                        }
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

                            playlistAdd.setOnClickListener {
                                showPlaylistDialog(videoId)
                            }
                        }
                    }else if(shuffleOption) {
                        val randomIndex = Random.nextInt(0, nextVideo.size)
                        videoAdapter.setBranoInRiproduzionePosition(randomIndex)
                        youTubePlayer.loadVideo(nextVideo[randomIndex].id, 0f)
                        playlistAdd.setOnClickListener {
                            showPlaylistDialog(nextVideo[randomIndex].id)
                        }
                        shuffleMode.add(randomIndex)
                    }else{
                        if(indexVideo < 0){
                            indexVideo = nextVideo.size - 1
                        }
                        if(nextVideo.size == 0){
                            indexVideo = 0
                        }
                        videoAdapter.setBranoInRiproduzionePosition(indexVideo)
                        youTubePlayer.loadVideo(nextVideo[indexVideo].id, 0f)
                        indexVideo++

                        playlistAdd.setOnClickListener {
                            showPlaylistDialog(nextVideo[indexVideo+1].id)
                        }
                    }
                }
            }
        })
        originalMarginTop = (youTubePlayerView.layoutParams as ViewGroup.MarginLayoutParams).topMargin

        return rootView
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            //activity?.window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN)
            activity?.actionBar?.hide()

            val params = youTubePlayerView.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = 0
            val displayMetrics = resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            params.height = screenHeight
            youTubePlayerView.layoutParams = params
            buttonPannel.visibility = View.GONE
            playlistAdd.visibility = View.GONE
        } else {
            val params = youTubePlayerView.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = originalMarginTop
            youTubePlayerView.layoutParams = params

            activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            activity?.actionBar?.show()

            buttonPannel.visibility = View.VISIBLE
            playlistAdd.visibility = View.VISIBLE
        }
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

                if(!alreadyExist(selectedPlaylist.playlistName, videoId)){
                    MainActivity.database.playlisVideotDao().insertVideoToPlaylist(element)
                }else{
                    Toast.makeText(requireContext(), "Video già presente in questa playlist", Toast.LENGTH_SHORT).show()
                }
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun alreadyExist(playlistName: String, videoId: String): Boolean{
        return MainActivity.database.playlisVideotDao().alreadyExist(playlistName, videoId) > 0
    }

    private fun showDeleteConfirmationDialog(videoItem: VideoItem, playlistName: String) {
        val alertDialogBuilder = android.app.AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Eliminare questo elemento?")
        alertDialogBuilder.setMessage("Sei sicuro di voler eliminare questo elemento?")

        alertDialogBuilder.setPositiveButton("Elimina") { _, _ ->
            MainActivity.database.playlisVideotDao().deleteVideoFromPlaylist(playlistName, videoItem.id.videoId)
            refreshAdapter(playlistName)
        }
        alertDialogBuilder.setNegativeButton("Annulla") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun refreshAdapter(playlistName:String) {
        val videos = MainActivity.database.playlisVideotDao().getPlaylistVideos(playlistName)

        val videoItems = videos?.map {
            var videoid = VideoId(it.id)
            var snippet = Snippet(it.title, it.channelTitle)
            VideoItem(videoid, snippet)
        } ?: emptyList()

        val videoAdapter = VideoAdapter(videoItems,{ videoItem ->
            if (playlistName != null && playlistName != "") {
                showDeleteConfirmationDialog(videoItem, playlistName)
            }
        }, "yt_player")
        videoAdapter.setOnPlaybackClickListener(object : VideoAdapter.OnPlaybackClickListener {
            override fun onPlaybackClick(videoItem: VideoItem) {
                onItemClick(videoItem)
            }
        })
        videoList.adapter = videoAdapter
    }

    private fun showCustomDialog(currentName: String) {
        val builder = android.app.AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_text_edit, null)

        val editTextName: EditText = dialogView.findViewById(R.id.editTextName)
        editTextName.setText(currentName)

        builder.setView(dialogView)
            .setTitle("Modifica nome Playlist")
            .setPositiveButton("OK") { dialog, _ ->
                val name =  editTextName.text.toString()
                if (name.isNotBlank() && MainActivity.database.playlistDao().alreadyExist(name) == 0) {
                    MainActivity.database.editPlaylistName(currentName, name)
                    playlisName.text = name
                }else {
                    if (name.isBlank()) {
                        Toast.makeText(
                            requireContext(),
                            "Il nome della playlist non può essere vuoto",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Il nome inserito è già esistente",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Annulla") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    override fun onItemClick(videoItem: VideoItem) {
        val videoId = videoItem.id

        val playerCallback = object : YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(videoId.videoId, 0f)
            }
        }

        playlistAdd.setOnClickListener {
            showPlaylistDialog(videoId.videoId)
        }
        youTubePlayerView.getYouTubePlayerWhenReady(playerCallback)
    }
}
