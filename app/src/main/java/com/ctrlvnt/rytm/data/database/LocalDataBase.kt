package com.ctrlvnt.rytm.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ctrlvnt.rytm.data.database.dao.VideoDao
import com.ctrlvnt.rytm.data.database.entities.Video

@Database(entities = [Video::class], version = 1)
abstract class LocalDataBase : RoomDatabase() {
    abstract fun videoDao(): VideoDao
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

    companion object {
        @Volatile
        private var INSTANCE: LocalDataBase? = null
        fun getDatabase(context: Context): LocalDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, LocalDataBase::class.java,
                    "video_database"
                ).allowMainThreadQueries().build()
                INSTANCE = instance
                instance
            }
        }
    }
}