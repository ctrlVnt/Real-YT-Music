package com.ctrlvnt.rytm.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ctrlvnt.rytm.data.database.entities.PlaylistVideo
import com.ctrlvnt.rytm.data.database.entities.Video

@Dao
interface PlaylistVideoDao {

    @Query("SELECT videoId AS id, title, channelTitle  FROM playlistvideo WHERE playlistName = :playlistName")
    fun getPlaylistVideos(playlistName: String): List<Video>

    @Insert
    fun insertVideoToPlaylist(playlistVideo: PlaylistVideo)

}