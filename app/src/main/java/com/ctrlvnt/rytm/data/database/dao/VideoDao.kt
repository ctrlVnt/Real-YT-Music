package com.ctrlvnt.rytm.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ctrlvnt.rytm.data.database.entities.Video

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos")
    fun getAll(): List<Video>

    @Insert
    fun insert(yourEntity: Video)
}