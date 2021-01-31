package com.example.jciclient.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RemoteEntity::class, FileEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun remoteDao(): RemoteDao
    abstract fun fileDao(): FileDao
}