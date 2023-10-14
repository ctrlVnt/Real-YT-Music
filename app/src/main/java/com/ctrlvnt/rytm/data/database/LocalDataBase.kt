package com.ctrlvnt.rytm.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ctrlvnt.rytm.data.database.dao.PlaylistDao
import com.ctrlvnt.rytm.data.database.dao.VideoDao
import com.ctrlvnt.rytm.data.database.entities.Playlist
import com.ctrlvnt.rytm.data.database.entities.Video

@Database(entities = [Video::class, Playlist::class], version = 2)
abstract class LocalDataBase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun playlistDao(): PlaylistDao

    fun insertVideo(video: Video) {
        videoDao().insert(video)
    }

    fun deleteVideo(video: Video) {
        videoDao().delete(video.id)
    }

    fun deleteAllVideos(){
        videoDao().deleteAll()
    }

    fun alreadyExist(video: Video):Int {
        return videoDao().alreadyExist(video.id)
    }

    fun deletePlaylist(playlist: Playlist){
        playlistDao().deletePlaylist(playlist)
    }

    fun updatePlaylistName(playlist: Playlist){
        playlistDao().updatePlaylistName(playlist.id, playlist.playlistName)
    }

    companion object {
        @Volatile
        private var INSTANCE: LocalDataBase? = null

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `playlist` " +
                        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`playlistName` TEXT NOT NULL)")
            }
        }
        fun getDatabase(context: Context): LocalDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, LocalDataBase::class.java,
                    "video_database"
                ).allowMainThreadQueries().addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}