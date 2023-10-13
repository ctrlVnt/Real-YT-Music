package com.ctrlvnt.rytm.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class Video(
    @PrimaryKey var id: String,
    val title: String,
    val channelTitle: String
)