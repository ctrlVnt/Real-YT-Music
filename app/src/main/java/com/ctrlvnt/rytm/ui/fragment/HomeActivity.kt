package com.ctrlvnt.rytm.ui.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ctrlvnt.rytm.R
import com.ctrlvnt.rytm.data.YouTubeApiManager
import com.ctrlvnt.rytm.data.database.entities.Playlist
import com.ctrlvnt.rytm.data.model.SearchResponse
import com.ctrlvnt.rytm.data.model.Snippet
import com.ctrlvnt.rytm.data.model.Thumbnail
import com.ctrlvnt.rytm.data.model.Thumbnails
import com.ctrlvnt.rytm.data.model.VideoId
import com.ctrlvnt.rytm.data.model.VideoItem
import com.ctrlvnt.rytm.ui.MainActivity
import com.ctrlvnt.rytm.ui.adapter.PlaylistAdapter
import com.ctrlvnt.rytm.ui.adapter.VideoAdapter
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.invoke.ConstantCallSite
import java.util.Locale
import androidx.core.net.toUri
import androidx.core.content.edit
import com.ctrlvnt.rytm.utils.apikey.APIKEY
import com.ctrlvnt.rytm.utils.extractPlaylistId
import com.ctrlvnt.rytm.utils.extractYoutubeId
import com.ctrlvnt.rytm.utils.generateRandomName
import com.ctrlvnt.rytm.utils.performYouTubeSearch
import com.ctrlvnt.rytm.utils.savePlaylistFromApi
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.graphics.findFirstRoot

class HomeActivity : Fragment() {

    lateinit var cronologia:RecyclerView
    lateinit var playlists: RecyclerView
    lateinit var searchBar: SearchView
    lateinit var historyText: TextView
    lateinit var historyImg: ImageView
    lateinit var trashButton: ImageButton
    lateinit var noPlaylist: TextView

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home_activity, container, false)
        searchBar = rootView.findViewById(R.id.search_titles)
        historyText = rootView.findViewById(R.id.empty_history)
        historyImg = rootView.findViewById(R.id.empty_history_img)
        trashButton = rootView.findViewById(R.id.delete_last_search)
        val appName: TextView = rootView.findViewById(R.id.welcome)
        val settingsButton: ImageButton =  rootView.findViewById(R.id.settings)
        val playlistsButton: TextView = rootView.findViewById(R.id.playlist_btn)
        val cronologiaText: TextView = rootView.findViewById(R.id.last_search_text)
        val bottomPart: ImageView = rootView.findViewById(R.id.bottom)
        val logo: ImageView = rootView.findViewById(R.id.logo)
        val subHome: ConstraintLayout = rootView.findViewById(R.id.subhome)
        val addButton: Button = rootView.findViewById(R.id.add_playlist)
        val importButton: Button = rootView.findViewById(R.id.import_playlist)
        val searchButton: Button = rootView.findViewById(R.id.search_button_modern)
        val explainText: TextView = rootView.findViewById(R.id.explain)
        val banner: TextView = rootView.findViewById(R.id.global_limit_banner)
        val noResultsText = rootView.findViewById<TextView>(R.id.no_results_text)
        noPlaylist = rootView.findViewById(R.id.no_playlists)
        searchButton.visibility = View.GONE
        explainText.visibility = View.GONE

        activity?.window?.decorView?.setBackgroundColor(resources.getColor(R.color.background))

       cronologia = rootView.findViewById(R.id.last_search)
        val layoutManager = LinearLayoutManager(context)
        cronologia.layoutManager = layoutManager

        playlists = rootView.findViewById(R.id.playlist_list)
        val layoutManagerPlaylists = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL, false)
        playlists.layoutManager = layoutManagerPlaylists

        val playlistsData = MainActivity.database.playlistDao().getAllPlaylists()
        val playlistAdapter = PlaylistAdapter(playlistsData) { playlistItem ->
            showDeleteConfirmationDialogPlaylist(playlistItem)
        }
        playlists.adapter = playlistAdapter

        if(playlistsData.isEmpty()){
            noPlaylist.visibility = View.VISIBLE
        }else{
            noPlaylist.visibility = View.GONE
        }

        val videos = MainActivity.database.videoDao().getAll()
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

        if(videos.isEmpty()){
            historyImg.visibility = View.VISIBLE
            historyText.visibility = View.VISIBLE
            trashButton.visibility = View.GONE
        }else{
            historyImg.visibility = View.GONE
            historyText.visibility = View.GONE
            trashButton.visibility = View.VISIBLE
        }

        if(playlistsData.isEmpty()){
            noPlaylist.visibility = View.VISIBLE
        }else{
            noPlaylist.visibility = View.GONE
        }

        val videoAdapter = VideoAdapter(videoItems, { videoItem ->
            showDeleteConfirmationDialog(videoItem)
        }, "home", blackText = true)

        cronologia.adapter = videoAdapter

        val recyclerView = rootView.findViewById<RecyclerView>(R.id.songs_list)
        recyclerView.adapter = VideoAdapter(videoItems, null, "home")

        trashButton.setOnClickListener{
            showConfirmationDialog()
        }

        settingsButton.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade, 0, R.anim.slow_fade, 0)
                .replace(R.id.main_activity, Settings())
                .addToBackStack(null)
                .commit()
        }

        addButton.setOnClickListener{
            showCustomDialog()
        }

        importButton.setOnClickListener {
            showCustomDialogInsertLink()
        }

        searchBar.setOnClickListener {
            searchBar.isIconified = false
        }

        searchButton.setOnClickListener {
            val query = searchBar.query?.toString()
            if (!query.isNullOrBlank()) {
                launchSearch(query, rootView, searchButton, explainText)
            }
        }


        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String?): Boolean {
                launchSearch(text.toString(), rootView, searchButton, explainText)
                return true
            }

            override fun onQueryTextChange(text: String?): Boolean {
                if (!text.isNullOrBlank()) {
                    explainText.visibility = View.VISIBLE
                    searchButton.visibility = View.VISIBLE
                    logo.visibility = View.GONE
                    appName.visibility = View.GONE
                    settingsButton.visibility = View.GONE
                    historyImg.visibility = View.GONE
                    historyText.visibility = View.GONE
                    cronologia.visibility = View.GONE
                    playlists.visibility = View.GONE
                    cronologiaText.visibility = View.GONE
                    playlistsButton.visibility = View.GONE
                    bottomPart.visibility = View.GONE
                    subHome.visibility = View.GONE
                    noPlaylist.visibility = View.GONE
                    addButton.visibility = View.GONE
                    importButton.visibility = View.GONE
                    trashButton.visibility = View.GONE
                }else{
                    clearRecyclerView(rootView)
                    if(videos.isEmpty()){
                        historyImg.visibility = View.VISIBLE
                        historyText.visibility = View.VISIBLE
                        trashButton.visibility = View.GONE
                    }else{
                        historyImg.visibility = View.GONE
                        historyText.visibility = View.GONE
                        trashButton.visibility = View.VISIBLE
                    }
                    if(playlistsData.isEmpty()){ //To change when will be playlist list
                        noPlaylist.visibility = View.VISIBLE
                    }
                    explainText.visibility = View.GONE
                    searchButton.visibility = View.GONE
                    banner.visibility = View.GONE
                    noResultsText.visibility = View.GONE
                    logo.visibility = View.VISIBLE
                    appName.visibility = View.VISIBLE
                    settingsButton.visibility = View.VISIBLE
                    cronologia.visibility = View.VISIBLE
                    playlists.visibility = View.VISIBLE
                    cronologiaText.visibility = View.VISIBLE
                    playlistsButton.visibility = View.VISIBLE
                    bottomPart.visibility = View.VISIBLE
                    subHome.visibility = View.VISIBLE
                    addButton.visibility = View.VISIBLE
                    importButton.visibility = View.VISIBLE
                }
                return true
            }
        })

        showDialogEveryTenOpens()
        return rootView
    }

    private fun showCustomDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext(), R.style.RoundedAlertDialog)
        val dialogView = layoutInflater.inflate(R.layout.dialog_text_edit, null)

        val editTextName: EditText = dialogView.findViewById(R.id.editTextName)

        builder.setView(dialogView)
            .setTitle(R.string.add_playlist)
            .setPositiveButton("OK") { dialog, _ ->
                val name =  editTextName.text.toString()

                if (name.isNotBlank() && MainActivity.database.playlistDao().alreadyExist(name) == 0) {
                    val newPlaylist = Playlist(playlistName = name)
                    MainActivity.database.playlistDao().insertPlaylist(newPlaylist)

                    val updatedPlaylists = MainActivity.database.playlistDao().getAllPlaylists()
                    (playlists.adapter as PlaylistAdapter).updatePlaylistList(updatedPlaylists)
                    noPlaylist.visibility = View.GONE
                } else {
                    if(name.isBlank()){
                        Toast.makeText(requireContext(), R.string.error_empty_name, Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(requireContext(), R.string.error_already_exist, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(R.string.restore) { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun showCustomDialogInsertLink() {
        val builder = MaterialAlertDialogBuilder(requireContext(), R.style.RoundedAlertDialog)
        val dialogView = layoutInflater.inflate(R.layout.dialog_text_edit, null)

        val linkCell: EditText = dialogView.findViewById(R.id.editTextName)
        linkCell.setHint("Link")

        builder.setView(dialogView)
            .setTitle(R.string.import_popup_title)
            .setPositiveButton("import") { dialog, _ ->
                val link =  linkCell.text.toString()

                if (link.isNotBlank()) {
                    val playlistId = extractPlaylistId(link)

                    if(playlistId != null) {
                        val fakeItem = VideoItem(
                            kind = "youtube#playlist",
                            id = VideoId(videoId = null, playlistId = playlistId),
                            snippet = Snippet(
                                title = "",
                                channelTitle = "",
                                thumbnails = Thumbnails(
                                    default = Thumbnail(""),
                                    medium = Thumbnail(""),
                                    high = Thumbnail("")
                                )
                            )
                        )
                        savePlaylistFromApi(requireContext(), fakeItem, generateRandomName()+" (to rename)")
                        Toast.makeText(requireContext(), "Playlist saved", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(requireContext(), "Not valid link", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if(link.isBlank()){
                        Toast.makeText(requireContext(), R.string.error_empty_name, Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(requireContext(), R.string.error_already_exist, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(R.string.restore) { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }
    
    fun launchSearch(query: String, rootView: View, searchButton: Button, explainText: TextView){
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
                val playlistId = extractPlaylistId(query)

                if(playlistId != null){
                    val fakeItem = VideoItem(
                        kind = "youtube#playlist",
                        id = VideoId(videoId = null, playlistId = playlistId),
                        snippet = Snippet(
                            title = "",
                            channelTitle = "",
                            thumbnails = Thumbnails(
                                default = Thumbnail(""),
                                medium = Thumbnail(""),
                                high = Thumbnail("")
                            )
                        )
                    )

                    savePlaylistFromApi(requireContext(), fakeItem, generateRandomName()+" (to rename)")
                }else{
                    Toast.makeText(requireContext(), "Not valid link", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            explainText.visibility = View.GONE
            searchButton.visibility = View.GONE

            performYouTubeSearch(
                context = requireContext(),
                rootView = rootView,
                recyclerViewId = R.id.songs_list,
                searchQuery = query
            )
        }
    }


    private fun showDialogEveryTenOpens() {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val launchCount = sharedPreferences.getInt("launch_count", 0) + 1

        // Save updated launch count
        sharedPreferences.edit { putInt("launch_count", launchCount) }

        // Show dialog every 10th launch
        if (launchCount % 7 == 0) {
            MaterialAlertDialogBuilder(requireContext(), R.style.RoundedAlertDialog)
                .setTitle(R.string.support_title)
                .setMessage(R.string.support_message)
                .setPositiveButton(R.string.support_positive) { _, _ ->
                    val url = "https://buymeacoffee.com/v3ntuz"
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = url.toUri()
                    startActivity(intent)
                }
                .setNegativeButton(R.string.support_negative) { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }


    private fun showDeleteConfirmationDialog(videoItem: VideoItem) {
        val alertDialogBuilder = MaterialAlertDialogBuilder(requireContext(), R.style.RoundedAlertDialog)
        alertDialogBuilder.setTitle(R.string.delete_confirmation_title)
        alertDialogBuilder.setMessage(R.string.delete_confirmation)

        alertDialogBuilder.setPositiveButton(R.string.delete) { _, _ ->
            MainActivity.database.videoDao().delete(videoItem.id.videoId.toString())
            refreshAdapter()
        }
        alertDialogBuilder.setNegativeButton(R.string.restore) { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun refreshAdapter() {
        val videos = MainActivity.database.videoDao().getAll()
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
        cronologia.adapter = VideoAdapter(videoItems, { videoItem ->
            showDeleteConfirmationDialog(videoItem)
        },"home", blackText = true)

        if(videos.isEmpty()){
            historyImg.visibility = View.VISIBLE
            historyText.visibility = View.VISIBLE
            trashButton.visibility = View.GONE
        }
    }

    private fun clearRecyclerView(rootView: View) {
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.songs_list)
        recyclerView.adapter = null
    }

    private fun showConfirmationDialog() {
        val alertDialogBuilder = MaterialAlertDialogBuilder(requireContext(), R.style.RoundedAlertDialog)
        alertDialogBuilder.setTitle(getString(R.string.confirm_delete_history))
        alertDialogBuilder.setMessage(getString(R.string.confirm_delete_history_message))

        alertDialogBuilder.setPositiveButton(getString(R.string.accept)) { dialog, _ ->
            MainActivity.database.deleteAllVideos()
            refreshAdapter()
            Toast.makeText(requireContext(), getString(R.string.deleted_history), Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton(getString(R.string.restore)) { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun showDeleteConfirmationDialogPlaylist(playlistItem: Playlist) {
        val alertDialogBuilder = MaterialAlertDialogBuilder(requireContext(), R.style.RoundedAlertDialog)
        alertDialogBuilder.setTitle(R.string.delete_confirmation_title)
        alertDialogBuilder.setMessage(R.string.delete_confirmation)

        alertDialogBuilder.setPositiveButton(R.string.delete) { _, _ ->
            MainActivity.database.deletePlaylist(playlistItem)
            refreshAdapterPlaylist()
        }
        alertDialogBuilder.setNegativeButton(R.string.restore) { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun refreshAdapterPlaylist() {
        val playlistsList = MainActivity.database.playlistDao().getAllPlaylists()
        playlists.adapter = PlaylistAdapter(playlistsList) { playlistItem ->
            showDeleteConfirmationDialogPlaylist(playlistItem)
        }
        if(playlistsList.isEmpty()){
            noPlaylist.visibility = View.VISIBLE
        }else{
            noPlaylist.visibility = View.GONE
        }
    }

    private fun logIn(context: Context) {
        val webView = view?.findViewById<WebView>(R.id.webView)
        webView?.visibility= View.VISIBLE
        webView?.settings.apply {
            this?.javaScriptEnabled = true
            this?.domStorageEnabled = true
            this?.savePassword = true
            this?.saveFormData = true
        }

        webView?.loadUrl(
            "https://accounts.google.com/ServiceLogin?service=youtube&uilel=3&passive=true&continue=https%3A%2F%2Fwww.youtube.com%2Fsignin%3Faction_handle_signin%3Dtrue%26app%3Dm%26hl%3Dtr%26next%3Dhttps%253A%252F%252Fm.youtube.com%252F"
        )

        webView?.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val url = request.url.toString()

                // se il WebView viene rediretto su youtube.com → login completato
                if (
                    url.startsWith("https://m.youtube.com") ||
                    url.startsWith("https://www.youtube.com")
                ) {
                    Log.d("TAG", "Logged in")
                    Toast.makeText(context, "Logged in", Toast.LENGTH_SHORT).show()
                }

                return false // lascia caricare la pagina
            }
        }
    }

}
