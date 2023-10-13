package com.ctrlvnt.rytm.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home_activity, container, false)
        val searchBar: SearchView = rootView.findViewById(R.id.search_titles)
        val appName: TextView = rootView.findViewById(R.id.welcome)
        val settingsButton: ImageButton =  rootView.findViewById(R.id.settings)
        val playlistsButton: ImageButton = rootView.findViewById(R.id.playlist)
        val line: View = rootView.findViewById(R.id.line)
        val cronologiaText: TextView = rootView.findViewById(R.id.last_search_text)
        val cronologia:RecyclerView = rootView.findViewById(R.id.last_search)

        val layoutManager = LinearLayoutManager(context)
        cronologia.layoutManager = layoutManager

        val videos = MainActivity.database.videoDao().getAll()
        val videoItems = videos?.map {
            var videoid = VideoId(it.id)
            var snippet = Snippet(it.title, it.channelTitle)
            VideoItem(videoid, snippet)
        } ?: emptyList()
        cronologia.adapter = VideoAdapter(videoItems)
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.songs_list)
        recyclerView.adapter = VideoAdapter(videoItems)

        settingsButton.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_activity, Settings())
                .addToBackStack(null)
                .commit()
        }

        playlistsButton.visibility = View.GONE //da riattivare in seguito

        playlistsButton.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_activity, Playlist())
                .addToBackStack(null)
                .commit()
        }

        searchBar.setOnClickListener {
            searchBar.isIconified = false
        }

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(title: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrBlank()) {
                    appName.visibility = View.GONE
                    settingsButton.visibility = View.GONE
                    line.visibility = View.GONE
                    cronologia.visibility = View.GONE
                    cronologiaText.visibility = View.GONE
                    //playlistsButton.visibility = View.GONE
                    //youtubeLogo.visibility = View.GONE
                    titleSearch(newText, rootView)
                }else{
                    clearRecyclerView(rootView)
                    appName.visibility = View.VISIBLE
                    settingsButton.visibility = View.VISIBLE
                    line.visibility = View.VISIBLE
                    cronologia.visibility = View.VISIBLE
                    cronologiaText.visibility = View.VISIBLE
                    //playlistsButton.visibility = View.VISIBLE
                    //youtubeLogo.visibility = View.VISIBLE
                }
                return true
            }
        })

        return rootView
    }

    /*private fun startAnimation() {
        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed(object : Runnable {
            override fun run() {
                val animation = ObjectAnimator.ofFloat(youtubeLogo, "rotation", 0f, 360f)
                animation.duration = 2000L
                animation.start()

                handler.postDelayed(this, 5000L)
            }
        }, 5000L)
    }*/

    private fun titleSearch(searchQuery : String, rootView: View){
        val recyclerView = rootView.findViewById<RecyclerView>(R.id.songs_list)
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        val apiManager = YouTubeApiManager()

        apiManager.searchVideos(searchQuery, APIKEY, object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (response.isSuccessful) {
                    val videos = response.body()?.items
                    val videoItems = videos?.map {
                        VideoItem(it.id, it.snippet)
                    } ?: emptyList()

                    // Creare un'istanza dell'adapter e collegarla alla RecyclerView
                    val recyclerView = rootView.findViewById<RecyclerView>(R.id.songs_list)
                    recyclerView.adapter = VideoAdapter(videoItems)
                } else {
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