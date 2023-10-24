package com.ctrlvnt.rytm.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.ctrlvnt.rytm.data.database.entities.Playlist

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlist")
    fun getAllPlaylists(): List<Playlist>

    @Insert
    fun insertPlaylist(playlist: Playlist)

    @Query("SELECT * FROM playlist WHERE id = :playlistId")
    fun getPlaylistById(playlistId: Long): Playlist

    @Delete
    fun deletePlaylist(playlist: Playlist)

    @Query("UPDATE playlist SET playlistName = :newName WHERE playlistName = :oldName")
    fun updatePlaylistName(oldName: String, newName: String)

    @Query("SELECT COUNT(*) FROM playlist WHERE playlistName = :playlistName")
    fun alreadyExist(playlistName: String): Int
}