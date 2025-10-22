package com.ctrlvnt.rytm.data.model

data class SearchResponse(
    val items: List<VideoItem>
)

data class VideoItem(
    val kind: String,
    val id: VideoId,
    val snippet: Snippet
)

data class VideoId(
    val videoId: String? = null,
    val playlistId: String? = null
)

data class Snippet(
    val title: String,
    val channelTitle: String,
    val thumbnails: Thumbnails
)

data class Thumbnails(
    val default: Thumbnail,
    val medium: Thumbnail,
    val high: Thumbnail
)

data class Thumbnail(
    val url: String
)

data class PlaylistItemsResponse(
    val items: List<PlaylistItem>
)

data class PlaylistItem(
    val kind: String,
    val snippet: PlaylistSnippet
)

data class PlaylistSnippet(
    val title: String,
    val channelTitle: String,
    val thumbnails: Thumbnails,
    val resourceId: ResourceId
)

data class ResourceId(
    val kind: String,
    val videoId: String
)