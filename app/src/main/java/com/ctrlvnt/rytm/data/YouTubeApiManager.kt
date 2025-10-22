package com.ctrlvnt.rytm.data

import com.ctrlvnt.rytm.data.model.PlaylistItemsResponse
import com.ctrlvnt.rytm.data.model.SearchResponse
import com.ctrlvnt.rytm.data.model.VideoItem
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class YouTubeApiManager {
    private val BASE_URL = "https://www.googleapis.com/youtube/v3/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(YouTubeApiService::class.java)

    fun searchVideos(query: String, apiKey: String, geoCode: String, callback: Callback<SearchResponse>) {
        val call = apiService.searchVideos(apiKey, query, geoCode)
        call.enqueue(callback)
    }

    fun getPlaylistsVideos(apiKey: String, playlistId: String, callback: Callback<PlaylistItemsResponse>) {
        val call = apiService.getPlaylistItems(apiKey, playlistId)
        call.enqueue(callback)
    }
}
