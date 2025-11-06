package com.ctrlvnt.rytm.ui.fragment


import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ctrlvnt.rytm.R
import com.ctrlvnt.rytm.data.YouTubeApiManager
import com.ctrlvnt.rytm.data.database.entities.PlaylistVideo
import com.ctrlvnt.rytm.data.database.entities.SaveMinutes
import com.ctrlvnt.rytm.data.database.entities.Video
import com.ctrlvnt.rytm.data.model.SearchResponse
import com.ctrlvnt.rytm.data.model.Snippet
import com.ctrlvnt.rytm.data.model.Thumbnail
import com.ctrlvnt.rytm.data.model.Thumbnails
import com.ctrlvnt.rytm.data.model.VideoId
import com.ctrlvnt.rytm.data.model.VideoItem
import com.ctrlvnt.rytm.ui.MainActivity
import com.ctrlvnt.rytm.ui.adapter.VideoAdapter
import com.ctrlvnt.rytm.utils.apikey.APIKEY
import com.ctrlvnt.rytm.utils.extractYoutubeId
import com.ctrlvnt.rytm.utils.fetchYoutubeVideoAsync
import com.ctrlvnt.rytm.utils.performYouTubeSearch
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Collections
import java.util.Locale
import kotlin.random.Random


class YouTubePlayerSupport : Fragment(), VideoAdapter.OnItemClickListener {

    companion object {
        fun newInstance(videoId: String, playlistName: String): YouTubePlayerSupport {
            val fragment = YouTubePlayerSupport()
            val args = Bundle()
            args.putString("video_id", videoId)
            args.putString("playlist_name", playlistName)
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
    private lateinit var timer: ImageButton
    private var timerOption: Boolean = false
    private var exitTimer: CountDownTimer? = null
    lateinit var searchBar: SearchView
    private var originalMarginTop: Int = 0
    private var indexVideo = 0
    private val googlePlayServicesAvailabilityRequestCode = 1
    private var saveMinutesHandler: Handler? = null
    private var saveMinutesRunnable: Runnable? = null
    private var isFullscreen = false


    // With this tracker we can change the logic of control notification and, maybe, implement headphone controls
    private val tracker = YouTubePlayerTracker()

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
        timer = rootView.findViewById(R.id.timer)
        prevButton = rootView.findViewById(R.id.prev_video)
        nextButton = rootView.findViewById(R.id.next_video)
        buttonPannel = rootView.findViewById(R.id.button_pannel)
        youTubePlayerView = rootView.findViewById(R.id.youtube_player_view)
        playlistAdd = rootView.findViewById(R.id.add_playlist)
        playlisName = rootView.findViewById(R.id.playlist_name)
        searchBar = rootView.findViewById(R.id.search_bar_player)
        val videos: MutableList<Video>

        val minutes: Float = MainActivity.database.getMinutesByVideoId(videoId.toString())

        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isSaveEnabled = prefs.getBoolean("save_minutes_enabled", false)

        if (isSaveEnabled) {
            startSaveMinutesTimer(videoId.toString())
        }

        var lock = false
        var repeatOption = false
        var shuffleOption = false

        overlay.visibility = View.GONE

        videoList = rootView.findViewById(R.id.playlist)
        val layoutManager = LinearLayoutManager(context)
        videoList.layoutManager = layoutManager
        
        

        if(playlistTitle == null || playlistTitle == ""){
            playlisName.text = getString(R.string.prev_search)
            buttonEditName.visibility = View.GONE
            videos = MainActivity.database.videoDao().getAll()

        }else{
            playlisName.text = playlistTitle
            videos = MainActivity.database.playlisVideotDao().getPlaylistVideos(playlistTitle)
        }

        if(playlistTitle == "fromoutside"){
            buttonPannel.visibility = View.GONE
            playlistAdd.visibility = View.GONE
            playlisName.visibility = View.GONE
            buttonEditName.visibility = View.GONE

            fetchYoutubeVideoAsync(videoId.toString()) { video ->
                if (video != null) {
                    // Save in history
                    MainActivity.database.insertVideo(video)
                } else {
                    Toast.makeText(requireContext(), "Data of video can't be fetched now, but enjoy", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val videoItems = videos?.map {
            val videoId = VideoId(it.id)
            val thumbnails = Thumbnails(
                Thumbnail(it.thumbnailUrl),
                Thumbnail(it.thumbnailUrl),
                Thumbnail(it.thumbnailUrl)
            )
            val snippet = Snippet(it.title, it.channelTitle, thumbnails)
            VideoItem("youtube#video", videoId, snippet)
        } ?.toMutableList() ?: mutableListOf()

        val videoAdapter = VideoAdapter(videoItems, null, "yt_player")

        videoAdapter.setOnPlaybackClickListener(object : VideoAdapter.OnPlaybackClickListener {
            override fun onPlaybackClick(videoItem: VideoItem) {
                onItemClick(videoItem)
            }
        })
        videoList.adapter = videoAdapter


        val itemTouchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                source: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val sourcePosition = source.getBindingAdapterPosition()
                val targetPosition = target.getBindingAdapterPosition()

                videoAdapter.moveItem(sourcePosition, targetPosition)
                Collections.swap(videos, sourcePosition, targetPosition)

                val title: String = playlistTitle.toString() //vedere cosa stampa
                videos.forEachIndexed { index, video ->
                    MainActivity.database.playlisVideotDao().updateVideoPosition(title, video.id, index)
                }
                indexVideo = targetPosition
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val videoItem = videoAdapter.getItemAt(position) // Serve un metodo nel tuo adapter

                if (playlistTitle != null && playlistTitle.isNotEmpty()) {
                    showDeleteConfirmationDialog(videoItem, playlistTitle!!)
                } else {
                    // Se non hai una playlist, semplicemente resetti l'item per evitare che rimanga "swiped"
                    videoAdapter.notifyItemChanged(position)
                }
            }

        })
        itemTouchHelper.attachToRecyclerView(videoList)

        repeat.setOnClickListener {
            repeatOption = !repeatOption
            if(shuffleOption){
                shuffleOption = false
            }
            if (repeatOption){
                val color = ContextCompat.getColor(requireContext(), R.color.red)
                repeat.setColorFilter(color)
            }else{
                repeat.clearColorFilter()
            }
        }

        shuffle.setOnClickListener {
            if(videos.size > 2){
                shuffleOption = !shuffleOption
                if(repeatOption){
                    repeatOption = false;
                }
                if (shuffleOption){
                    val color = ContextCompat.getColor(requireContext(), R.color.red)
                    shuffle.setColorFilter(color)
                }else{
                    shuffle.clearColorFilter()
                }
            }
        }

        timer.setOnClickListener {
            showExitTimerDialog()
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
                timer.visibility = View.GONE

                (activity as? AppCompatActivity)?.let {
                    val layoutParams = it.window.attributes
                    layoutParams.screenBrightness = 0f
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
                timer.visibility = View.VISIBLE

                (activity as? AppCompatActivity)?.let {
                    val layoutParams = it.window.attributes
                    layoutParams.screenBrightness =
                        WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                    it.window.attributes = layoutParams
                }
            }
        }

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String?): Boolean {
                launchSearch(text.toString(), rootView)
                return true
            }

            override fun onQueryTextChange(text: String?): Boolean {
                if(text.isNullOrBlank()){
                    val recyclerView = rootView.findViewById<RecyclerView>(R.id.videos_list_player)
                    recyclerView.adapter = null
                }
                return true
            }
        })

        val nextVideo = videos
        var shuffleMode = mutableListOf<Int>()
        var shuffleindex = 0

        /*FULL-PLAYBACK MODE*/
        viewLifecycleOwner.lifecycle.addObserver(youTubePlayerView) //comment if you want to use full playback mode
        //youTubePlayerView.enableBackgroundPlayback(true) //uncomment and enjoy !

        val iFramePlayerOptions = IFramePlayerOptions.Builder(requireContext())
            .controls(1)
            .fullscreen(1)
            .build()

        val youTubePlayerView = rootView.findViewById<YouTubePlayerView>(R.id.youtube_player_view)
        val fullscreenViewContainer = rootView.findViewById<FrameLayout>(R.id.about)

        youTubePlayerView.enableAutomaticInitialization = false

        youTubePlayerView.addFullscreenListener(object : FullscreenListener {
            override fun onEnterFullscreen(fullscreenView: View, exitFullscreen: () -> Unit) {
                isFullscreen = true

                // the video will continue playing in fullscreenView
                //youTubePlayerView.visibility = View.GONE
                fullscreenViewContainer.visibility = View.VISIBLE
                fullscreenViewContainer.addView(fullscreenView)


                // optionally request landscape orientation
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }

            override fun onExitFullscreen() {
                isFullscreen = false

                // the video will continue playing in the player
                //youTubePlayerView.visibility = View.VISIBLE
                fullscreenViewContainer.visibility = View.GONE
                fullscreenViewContainer.removeAllViews()
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        })


        youTubePlayerView.initialize(object : AbstractYouTubePlayerListener() {

            override fun onReady(youTubePlayer: YouTubePlayer) {

                youTubePlayer.addListener(tracker)

                videoId?.let {
                    val position = nextVideo.indexOfFirst { video -> video.id == it }

                    indexVideo = position
                    videoAdapter.setBranoInRiproduzionePosition(position)
                    youTubePlayer.loadVideo(it, minutes)
                    MainActivity.database.deleteMinutes(videoId) //to reset every time

                    playlistAdd.setOnClickListener {
                            showPlaylistDialog(nextVideo[indexVideo])
                    }

                    nextButton.setOnClickListener{
                        if(!repeatOption){
                            if(shuffleOption){
                                if(shuffleindex >= shuffleMode.size - 1){
                                    var randomIndex: Int
                                    do{
                                        randomIndex = Random.nextInt(0, nextVideo.size)
                                    }while (randomIndex == indexVideo)

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
                    }
                    prevButton.setOnClickListener{

                        if(!repeatOption){
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
            }
            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {
                super.onStateChange(youTubePlayer, state)
                if (state == PlayerConstants.PlayerState.ENDED){
                    val playlistName = arguments?.getString("playlist_name")
                    MainActivity.database.deleteMinutes(videoId.toString())
                    if(repeatOption || playlistName == "fromoutside"){
                        videoId?.let {
                            youTubePlayer.loadVideo(it, 0f)

                            playlistAdd.setOnClickListener {
                                showPlaylistDialog(nextVideo[indexVideo])
                            }
                        }
                    }else if(shuffleOption) {
                        var randomIndex: Int
                        do{
                            randomIndex = Random.nextInt(0, nextVideo.size)
                        }while (randomIndex == indexVideo)
                        videoAdapter.setBranoInRiproduzionePosition(randomIndex)
                        youTubePlayer.loadVideo(nextVideo[randomIndex].id, 0f)
                        playlistAdd.setOnClickListener {
                            showPlaylistDialog(nextVideo[randomIndex])
                        }
                        shuffleMode.add(randomIndex)
                    }else{
                        //if there is one video
                        if(nextVideo.size == 1){
                            indexVideo = 0
                        //if the video is the firstone, I'll return to lastone
                        }else if(indexVideo < 0){
                            indexVideo = nextVideo.size - 1
                        //if if the last video to the list return to firstone
                        } else if (indexVideo >= nextVideo.size - 1) {
                            indexVideo = 0
                        }else{
                            indexVideo++
                        }
                        videoAdapter.setBranoInRiproduzionePosition(indexVideo)
                        youTubePlayer.loadVideo(nextVideo[indexVideo].id, 0f)
                    }
                }
            }
        }, iFramePlayerOptions)
        originalMarginTop = (youTubePlayerView.layoutParams as ViewGroup.MarginLayoutParams).topMargin

        return rootView
    }



    fun launchSearch(query: String, rootView: View){
        if (query.startsWith("https://")) {
            val videoId = extractYoutubeId(query)
            if (videoId != null) {
                val fragment = YouTubePlayerSupport().apply {
                    arguments = Bundle().apply {
                        putString("video_id", videoId)
                        putString("playlist_name", "fromoutside")
                    }
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_activity, fragment)
                    .addToBackStack(null)
                    .commit()

            } else {
                Toast.makeText(requireContext(), "Link non valido", Toast.LENGTH_SHORT).show()
            }
        } else {
            performYouTubeSearch(
                context = requireContext(),
                rootView = rootView,
                recyclerViewId = R.id.videos_list_player,
                searchQuery = query
            )
        }
    }

    private fun startSaveMinutesTimer(videoId: String) {
        saveMinutesHandler = Handler(Looper.getMainLooper())
        saveMinutesRunnable = object : Runnable {
            override fun run() {
                val currentSeconds = tracker.currentSecond
                MainActivity.database.saveMinutesVideo(
                    SaveMinutes(videoId, currentSeconds)
                )
                MainActivity.database.updateMinutes(videoId, currentSeconds)

                saveMinutesHandler?.postDelayed(this, 10_000)
            }
        }
        saveMinutesHandler?.post(saveMinutesRunnable!!)
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
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

    private fun showPlaylistDialog(video: Video) {
        val playlists = MainActivity.database.playlistDao().getAllPlaylists()

        if (playlists.isEmpty()) {
            Toast.makeText(requireContext(), R.string.create_playlist_first, Toast.LENGTH_LONG).show()
            return
        }

        val playlistNames = playlists.map { it.playlistName }.toTypedArray()

        val builder = MaterialAlertDialogBuilder(requireContext(), R.style.RoundedAlertDialog)
        builder.setTitle(R.string.select_playlist)
            .setItems(playlistNames) { _, which ->

                val selectedPlaylist = playlists[which]

                val element = PlaylistVideo(selectedPlaylist.playlistName, video.id, video.title, video.channelTitle, video.thumbnailUrl, playlists.size - 1)

                if(!alreadyExist(selectedPlaylist.playlistName, video.id)){
                    MainActivity.database.playlisVideotDao().insertVideoToPlaylist(element)
                }else{
                    Toast.makeText(requireContext(), R.string.error_element_playlist_already_exist, Toast.LENGTH_SHORT).show()
                }
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun alreadyExist(playlistName: String, videoId: String): Boolean{
        return MainActivity.database.playlisVideotDao().alreadyExist(playlistName, videoId) > 0
    }

    private fun showDeleteConfirmationDialog(videoItem: VideoItem, playlistName: String) {
        val alertDialogBuilder = MaterialAlertDialogBuilder(requireContext(), R.style.RoundedAlertDialog)
        alertDialogBuilder.setTitle(R.string.delete_confirmation_title)
        alertDialogBuilder.setMessage(R.string.delete_confirmation)

        alertDialogBuilder.setPositiveButton(R.string.delete) { _, _ ->
            MainActivity.database.playlisVideotDao().deleteVideoFromPlaylist(playlistName, videoItem.id.videoId.toString())
            refreshAdapter(playlistName)
        }
        alertDialogBuilder.setNegativeButton(R.string.restore) { dialog, _ ->
            refreshAdapter(playlistName)
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun refreshAdapter(playlistName:String) {
        val videos = MainActivity.database.playlisVideotDao().getPlaylistVideos(playlistName)

        val videoItems = videos.map {
            val videoId = VideoId(it.id)
            val thumbnails = Thumbnails(
                Thumbnail(it.thumbnailUrl),
                Thumbnail(it.thumbnailUrl),
                Thumbnail(it.thumbnailUrl)
            )
            val snippet = Snippet(it.title, it.channelTitle, thumbnails)
            VideoItem("youtube#video", videoId, snippet)
        } ?: emptyList()

        val videoAdapter = VideoAdapter(videoItems,null, "yt_player")
        videoAdapter.setOnPlaybackClickListener(object : VideoAdapter.OnPlaybackClickListener {
            override fun onPlaybackClick(videoItem: VideoItem) {
                onItemClick(videoItem)
            }
        })
        videoList.adapter = videoAdapter
    }

    private fun showCustomDialog(currentName: String) {
        val builder = MaterialAlertDialogBuilder(requireContext(), R.style.RoundedAlertDialog)
        val dialogView = layoutInflater.inflate(R.layout.dialog_text_edit, null)

        val editTextName: EditText = dialogView.findViewById(R.id.editTextName)
        editTextName.setText(currentName)

        builder.setView(dialogView)
            .setTitle(R.string.edit_playlist_name)
            .setPositiveButton("OK") { dialog, _ ->
                val name =  editTextName.text.toString()
                if (name.isNotBlank() && MainActivity.database.playlistDao().alreadyExist(name) == 0) {
                    MainActivity.database.editPlaylistName(currentName, name)
                    playlisName.text = name
                }else {
                    if (name.isBlank()) {
                        Toast.makeText(
                            requireContext(),
                            R.string.error_empty_name,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            R.string.error_already_exist,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton(R.string.restore) { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    override fun onItemClick(videoItem: VideoItem) {
        val videoId = videoItem.id.videoId.toString()
        val video = Video(
            id = videoId,
            title = videoItem.snippet.title,
            channelTitle = videoItem.snippet.channelTitle,
            thumbnailUrl = videoItem.snippet.thumbnails.default.url
        )



        val playerCallback = object : YouTubePlayerCallback {
            override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(videoId, 0f)
            }
        }

        playlistAdd.setOnClickListener {
            showPlaylistDialog(video)
        }
        youTubePlayerView.getYouTubePlayerWhenReady(playerCallback)
    }


    fun showExitTimerDialog() {
        val editText = EditText(requireContext())
        editText.hint = "Enter minutes"

        if (!timerOption) {
            val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.RoundedAlertDialog)
                .setTitle("Exit Timer")
                .setMessage("Enter the number of minutes after which the app will close:")
                .setView(editText)
                .setPositiveButton("Start Timer") { _, _ ->
                    val input = editText.text.toString()
                    val minutes = input.toLongOrNull()
                    if (minutes != null && minutes > 0) {
                        timerOption = true
                        val color = ContextCompat.getColor(requireContext(), R.color.red)
                        timer.setColorFilter(color)
                        startExitTimer(minutes)
                    } else {
                        Toast.makeText(requireContext(), "Invalid input", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .create()
            dialog.show()
        }else{
            val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.RoundedAlertDialog)
                .setTitle("Exit Timer")
                .setMessage("Timer is already running, do you want to stop it?")
                .setPositiveButton("Yes"){ _, _ ->
                    timerOption = false
                    timer.clearColorFilter()
                    cancelExitTimer()
                    Toast.makeText(requireContext(), "Exit timer cancelled", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("No", null)
                .create()
            dialog.show()
        }

    }

    fun startExitTimer(minutes: Long) {
        val millis = minutes * 60 * 1000

        Toast.makeText(requireContext(), "App will close in $minutes minute(s)", Toast.LENGTH_SHORT).show()

        exitTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Optional: update UI every second
            }

            override fun onFinish() {
                requireActivity().finish()
            }
        }.start()
    }

    fun cancelExitTimer() {
        exitTimer?.cancel()
        exitTimer = null
        stopSaveMinutesTimer()
    }

    private fun stopSaveMinutesTimer() {
        saveMinutesHandler?.removeCallbacks(saveMinutesRunnable!!)
        saveMinutesHandler = null
        saveMinutesRunnable = null
    }



    override fun onDestroy() {
        super.onDestroy()
        val notificationManager = NotificationManagerCompat.from(requireContext())
        notificationManager.cancelAll()
        cancelExitTimer()
    }
}
