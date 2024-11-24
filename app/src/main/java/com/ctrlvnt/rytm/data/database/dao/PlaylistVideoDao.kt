package com.ctrlvnt.rytm.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ctrlvnt.rytm.data.database.entities.PlaylistVideo
import com.ctrlvnt.rytm.data.database.entities.Video

@Dao
interface PlaylistVideoDao {

    @Query("SELECT videoId AS id, title, channelTitle, thumbnailUrl  FROM playlistvideo WHERE playlistName = :playlistName")
    fun getPlaylistVideos(playlistName: String): List<Video>

    @Insert
    fun insertVideoToPlaylist(playlistVideo: PlaylistVideo)

    @Query("DELETE FROM playlistvideo WHERE playlistName = :playlistName")
    fun deletePlaylistVideos(playlistName: String)

    @Query("DELETE FROM playlistvideo WHERE playlistName = :playlistName AND videoId = :video")
    fun deleteVideoFromPlaylist(playlistName: String, video: String)

    @Query("UPDATE playlistvideo SET playlistName = :newName WHERE playlistName = :oldName")
    fun updatePlaylistName(oldName: String, newName: String)
    @Query("SELECT COUNT(*) FROM playlistvideo WHERE playlistName = :playlistName AND videoId = :video")
     fun alreadyExist(playlistName:String, video:String): Int
}