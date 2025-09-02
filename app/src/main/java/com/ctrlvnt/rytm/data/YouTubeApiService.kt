package com.ctrlvnt.rytm.data

import com.ctrlvnt.rytm.data.model.SearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {
    @GET("search")
    fun searchVideos(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("regionCode") regionCode: String,
        @Query("part") part: String = "snippet",
        @Query("maxResults") maxResults: Int = 20,
        @Query("type") type: String = "video"
    ): Call<SearchResponse>
}