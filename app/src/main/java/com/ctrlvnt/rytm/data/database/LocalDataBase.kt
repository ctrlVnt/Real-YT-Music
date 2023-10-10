package com.ctrlvnt.rytm.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ctrlvnt.rytm.data.database.dao.VideoDao
import com.ctrlvnt.rytm.data.database.entities.Video

@Database(entities = [Video::class], version = 1)
abstract class LocalDataBase : RoomDatabase() {
    abstract fun yourDao(): VideoDao
}