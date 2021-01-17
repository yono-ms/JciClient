package com.example.jciclient.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RemoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun remoteDao(): RemoteDao
}