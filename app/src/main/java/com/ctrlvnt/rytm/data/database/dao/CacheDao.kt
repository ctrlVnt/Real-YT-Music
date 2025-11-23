package com.ctrlvnt.rytm.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ctrlvnt.rytm.data.database.entities.CacheEntity

@Dao
interface CacheDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(video: CacheEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(videos: List<CacheEntity>)

    @Query("SELECT * FROM cache WHERE videoId = :id LIMIT 1")
    fun getById(id: String): CacheEntity?

    @Query("SELECT * FROM cache WHERE title LIKE '%' || :query || '%'")
    fun searchByTitle(query: String): List<CacheEntity>
}