package com.ctrlvnt.rytm.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_cache")
data class SearchCache(
    @PrimaryKey val query: String,
    val timestamp: Long,
    val jsonResults: String
)