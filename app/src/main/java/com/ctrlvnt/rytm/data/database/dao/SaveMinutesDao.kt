package com.ctrlvnt.rytm.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ctrlvnt.rytm.data.database.entities.SaveMinutes

@Dao
interface SaveMinutesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(minutes: SaveMinutes)

    @Query("SELECT minutes FROM save_minutes WHERE videoId = :id LIMIT 1")
    fun getMinutesByVideoId(id: String): Float

    @Query("UPDATE save_minutes SET minutes = :value WHERE videoId = :id")
    fun updateMinutes(id: String, value: Float)

    @Query("DELETE FROM save_minutes WHERE videoId = :id")
    fun deleteByVideoId(id: String)
}