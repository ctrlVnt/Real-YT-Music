package com.ctrlvnt.rytm.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ctrlvnt.rytm.data.database.entities.Video

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY id DESC")
    fun getAll(): MutableList<Video>

    @Query("SELECT * FROM videos WHERE id = :videoId")
    fun getVideo(videoId: String): Video

    @Insert
    fun insert(yourEntity: Video)

    @Query("DELETE FROM videos WHERE id = :videoId")
    fun delete(videoId: String)

    @Query("DELETE FROM videos")
    fun deleteAll()

    @Query("SELECT COUNT(*) FROM videos WHERE id = :videoId")
    fun alreadyExist(videoId: String): Int
}