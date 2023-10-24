package com.ctrlvnt.rytm.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ctrlvnt.rytm.R
import com.ctrlvnt.rytm.data.YouTubeApiManager
import com.ctrlvnt.rytm.data.model.SearchResponse
import com.ctrlvnt.rytm.data.model.Snippet
import com.ctrlvnt.rytm.data.model.VideoId
import com.ctrlvnt.rytm.data.model.VideoItem
import com.ctrlvnt.rytm.ui.MainActivity
import com.ctrlvnt.rytm.ui.adapter.VideoAdapter
import com.ctrlvnt.rytm.utils.APIKEY
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class HomeActivity : Fragment() {

    lateinit var cronologia:RecyclerView
    lateinit var searchBar: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home_activity, container, false)
        searchBar = rootView.findViewById(R.id.search_titles)
        val appName: TextView = rootView.findViewById(R.id.welcome)
        val settingsButton: ImageButton =  rootView.findViewById(R.id.settings)
        val playlistsButton: FloatingActionButton = rootView.findViewById(R.id.playlist)
        val cronologiaText: TextView = rootView.findViewById(R.id.last_search_text)
        val bottomPart: ImageView = rootView.findViewById(R.id.bottom)
        val logo: ImageView = rootView.findViewById(R.id.logo)

       cronologia = rootView.findViewById(R.id.last_search)
        val layoutManager = LinearLayoutManager(context)
        cronologia.layoutManager = layoutManager

        val videos = MainActivity.database.videoDao().getAll()
        val videoItems = videos?.map {
            var videoid = VideoId(it.id)

            var snippet = Snippet(Html.fromHtml(it.title, Html.FROM_HTML_MODE_LEGACY).toString(), Html.fromHtml(it.channelTitle, Html.FROM_HTML_MODE_LEGACY).toString())
            VideoItem(videoid, snippet)
        } ?: emptyList()

        val videoAdapter = VideoAdapter(videoItems, { videoItem ->
            showDeleteConfirmationDialog(videoItem)
        }, "home")

        cronologia.adapter = videoAdapter

        val recyclerView = rootView.findViewById<RecyclerView>(R.id.songs_list)
        recyclerView.adapter = VideoAdapter(videoItems, null, "home")

        settingsButton.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_activity, Settings())
                .addToBackStack(null)
                .commit()
        }

        playlistsButton.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_activity, Playlist_fragment())
                .addToBackStack(null)
                .commit()
        }

        searchBar.setOnClickListener {
            searchBar.isIconified = false
        }

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String?): Boolean {
                if (!text.isNullOrBlank()) {
                    titleSearch(text, rootView)
                }
                return true
            }

            override fun onQueryTextChange(text: String?): Boolean {
                if (!text.isNullOrBlank()) {

                    /*logo.startAnimation(AlphaAnimation(1.0f, 0.0f).apply {
                        duration = 500
                        fillAfter = true
                    })
                    bottomPart.startAnimation(AlphaAnimation(1.0f, 0.0f).apply {
                        duration = 500
                        fillAfter = true
                    })*/

                    logo.visibility = View.GONE
                    appName.visibility = View.GONE
                    settingsButton.visibility = View.GONE

                    cronologia.visibility = View.GONE
                    cronologiaText.visibility = View.GONE
                    playlistsButton.visibility = View.GONE
                    bottomPart.visibility = View.GONE
                }else{
                    clearRecyclerView(rootView)
                    logo.visibility = View.VISIBLE
                    appName.visibility = View.VISIBLE
                    settingsButton.visibility = View.VISIBLE
                    cronologia.visibility = View.VISIBLE
                    cronologiaText.visibility = View.VISIBLE
                    playlistsButton.visibility = View.VISIBLE
                    bottomPart.visibility = View.VISIBLE
                }
                return true
            }
        })

        return rootView
    }

    private fun showDeleteConfirmationDialog(videoItem: VideoItem) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle(R.string.delete_confirmation_title)
        alertDialogBuilder.setMessage(R.string.delete_confirmation)

        alertDialogBuilder.setPositiveButton(R.string.delete) { _, _ ->
            MainActivity.database.videoDao().delete(videoItem.id.videoId)
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
        val videoItems = videos?.map {
            var videoid = VideoId(it.id)
            var snippet = Snippet(it.title, it.channelTitle)
            VideoItem(videoid, snippet)
        } ?: emptyList()
        cronologia.adapter = VideoAdapter(videoItems, { videoItem ->
            showDeleteConfirmationDialog(videoItem)
        },"home")
    }

    private fun titleSearch(searchQuery : String, rootView: View){
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.songs_list)
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        val apiManager = YouTubeApiManager()

        val locale = Locale.getDefault()
        val country = locale.country

        apiManager.searchVideos(searchQuery, APIKEY, country, object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (response.isSuccessful) {
                    val videos = response.body()?.items
                    val videoItems = videos?.map {
                        VideoItem(it.id, it.snippet)
                    } ?: emptyList()

                    // Creare un'istanza dell'adapter e collegarla alla RecyclerView
                    val recyclerView = rootView.findViewById<RecyclerView>(R.id.songs_list)
                    recyclerView.adapter = VideoAdapter(videoItems, null,"home")
                } else {
                    val errorBody = response.errorBody()?.string()
                    try {
                        val errorJson = JSONObject(errorBody)
                        val errorMessage = errorJson.getJSONObject("error").getString("message")
                        Toast.makeText(requireContext(), "$errorMessage", Toast.LENGTH_LONG).show()
                    } catch (e: JSONException) {
                        Log.e("API Error", "Errore nell'analisi del JSON dell'errore", e)
                        Toast.makeText(requireContext(), "Errore sconosciuto", Toast.LENGTH_SHORT).show()
                    }

                    Log.e("API Error", response.toString())
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Log.e("ERROR", t.message.toString())
            }
        })
    }

    private fun clearRecyclerView(rootView: View) {
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.songs_list)
        recyclerView.adapter = null
    }
}