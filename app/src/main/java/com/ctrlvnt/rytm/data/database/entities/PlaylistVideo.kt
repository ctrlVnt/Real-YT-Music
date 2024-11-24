package com.ctrlvnt.rytm.data.database.entities

import androidx.room.Entity

@Entity(tableName = "playlistvideo", primaryKeys = ["playlistName", "videoId"])
data class PlaylistVideo(
    var playlistName: String,
    var videoId: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUrl: String
)
