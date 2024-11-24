package com.ctrlvnt.rytm.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ctrlvnt.rytm.data.database.dao.PlaylistDao
import com.ctrlvnt.rytm.data.database.dao.PlaylistVideoDao
import com.ctrlvnt.rytm.data.database.dao.VideoDao
import com.ctrlvnt.rytm.data.database.entities.Playlist
import com.ctrlvnt.rytm.data.database.entities.PlaylistVideo
import com.ctrlvnt.rytm.data.database.entities.Video

@Database(entities = [Video::class, Playlist::class, PlaylistVideo::class], version = 6)
abstract class LocalDataBase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlisVideotDao(): PlaylistVideoDao

    fun insertVideo(video: Video) {
        videoDao().insert(video)
    }

    fun deleteAllVideos(){
        videoDao().deleteAll()
    }

    fun alreadyExist(video: Video):Int {
        return videoDao().alreadyExist(video.id)
    }

    fun editPlaylistName(oldName: String, newName: String){
        playlistDao().updatePlaylistName(oldName, newName)
        playlisVideotDao().updatePlaylistName(oldName,newName)
    }

    fun deletePlaylist(playlistItem: Playlist){
        playlistDao().deletePlaylist(playlistItem)
        playlisVideotDao().deletePlaylistVideos(playlistItem.playlistName)
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

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `playlistvideo` " +
                        "(`playlistName` TEXT PRIMARY KEY NOT NULL, " +
                        "`videoId` TEXT NOT NULL)")
            }
        }

        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `playlistvideo_new` " +
                        "(`playlistName` TEXT PRIMARY KEY NOT NULL, " +
                        "`videoId` TEXT NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`channelTitle` TEXT NOT NULL)")

                database.execSQL("INSERT INTO `playlistvideo_new` " +
                        "(`playlistName`, `videoId`, `title`, `channelTitle`) " +
                        "SELECT `playlistName`, `videoId`, '', '' FROM `playlistvideo`")

                database.execSQL("DROP TABLE `playlistvideo`")

                database.execSQL("ALTER TABLE `playlistvideo_new` RENAME TO `playlistvideo`")
            }
        }

        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `playlistvideo_new` " +
                        "(`playlistName` TEXT NOT NULL, " +
                        "`videoId` TEXT NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`channelTitle` TEXT NOT NULL, " +
                        "PRIMARY KEY(`playlistName`, `videoId`))")

                database.execSQL("INSERT INTO `playlistvideo_new` " +
                        "(`playlistName`, `videoId`, `title`, `channelTitle`) " +
                        "SELECT `playlistName`, `videoId`, 'title', 'channelTitle' FROM `playlistvideo`")

                database.execSQL("DROP TABLE `playlistvideo`")

                database.execSQL("ALTER TABLE `playlistvideo_new` RENAME TO `playlistvideo`")

            }
        }

        val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `playlistvideo_new` " +
                        "(`playlistName` TEXT NOT NULL, " +
                        "`videoId` TEXT NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`channelTitle` TEXT NOT NULL, " +
                        "`thumbnailUrl` TEXT NOT NULL, " +
                        "PRIMARY KEY(`playlistName`, `videoId`))")

                database.execSQL("INSERT INTO `playlistvideo_new` " +
                        "(`playlistName`, `videoId`, `title`, `channelTitle`) " +
                        "SELECT `playlistName`, `videoId`, 'title', 'channelTitle', '' FROM `playlistvideo`")

                database.execSQL("DROP TABLE `playlistvideo`")

                database.execSQL("ALTER TABLE `playlistvideo_new` RENAME TO `playlistvideo`")

                database.execSQL("ALTER TABLE `videos` ADD COLUMN `thumbnailUrl` TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): LocalDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, LocalDataBase::class.java,
                    "video_database"
                ).allowMainThreadQueries()
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .addMigrations(MIGRATION_3_4)
                    .addMigrations(MIGRATION_4_5)
                    .addMigrations(MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}