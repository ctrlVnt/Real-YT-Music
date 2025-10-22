package com.ctrlvnt.rytm.utils

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ctrlvnt.rytm.R
import com.ctrlvnt.rytm.data.YouTubeApiManager
import com.ctrlvnt.rytm.data.model.SearchResponse
import com.ctrlvnt.rytm.data.model.VideoItem
import com.ctrlvnt.rytm.ui.adapter.VideoAdapter
import com.ctrlvnt.rytm.utils.apikey.APIKEY
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

/**
 * Effettua una ricerca su YouTube e popola una RecyclerView con i risultati.
 *
 * @param context Context dell’app (necessario per Toast o stringhe)
 * @param rootView View root del layout corrente
 * @param recyclerViewId ID della RecyclerView da riempire
 * @param searchQuery Testo della ricerca
 * @param showLimitDialog Se true, mostra un dialog invece del Toast in caso di limite API
 * @param onLimitReached (opzionale) callback da eseguire se viene raggiunto il limite API
 */
fun performYouTubeSearch(
    context: Context,
    rootView: View,
    recyclerViewId: Int,
    searchQuery: String,
) {
    val recyclerView = rootView.findViewById<RecyclerView>(recyclerViewId)
    recyclerView.layoutManager = LinearLayoutManager(context)

    val apiManager = YouTubeApiManager()
    val locale = Locale.getDefault()
    var country = locale.country

    if (locale.language == "en") country = "us"
    else if (locale.language == "hi") country = "in"

    apiManager.searchVideos(searchQuery, APIKEY, country, object : Callback<SearchResponse> {
        override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
            if (response.isSuccessful) {
                val videos = response.body()?.items ?: emptyList()
                val videoItems = videos.map { VideoItem(it.kind, it.id, it.snippet) }

                recyclerView.adapter = VideoAdapter(videoItems, null, "home")

            } else {
                val errorBody = response.errorBody()?.string()
                try {
                    val errorJson = JSONObject(errorBody ?: "")
                    val errorMessage = errorJson.getJSONObject("error").getString("message")
                    showLimitReachedDialog(context)

                } catch (e: JSONException) {
                    Log.e("YouTubeSearch", "Errore parsing JSON", e)
                    Toast.makeText(context, context.getString(R.string.generic_error), Toast.LENGTH_SHORT).show()
                }
            }
        }

        override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
            Log.e("YouTubeSearch", "Errore API: ${t.message}")
            Toast.makeText(context, context.getString(R.string.generic_error), Toast.LENGTH_SHORT).show()
        }
    })
}

fun showLimitReachedDialog(context: Context) {
    MaterialAlertDialogBuilder(context)
        .setTitle(R.string.limit_title)
        .setMessage(R.string.limit_message)
        .setPositiveButton("OK", null)
        .show()
}

fun extractYoutubeId(url: String): String? {
    val shortRegex = "(?<=youtu\\.be/)[^?&]*".toRegex()

    val longRegex = "(?<=v=)[^#&?]*".toRegex()

    return shortRegex.find(url)?.value ?: longRegex.find(url)?.value
}