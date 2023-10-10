package com.ctrlvnt.rytm.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos")
    fun getAll(): List<VideoDao>

    @Insert
    fun insert(yourEntity: VideoDao)
}