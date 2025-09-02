package com.ctrlvnt.rytm.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "save_minutes")
data class SaveMinutes(
    @PrimaryKey val videoId: String,
    val minutes: Float
)
