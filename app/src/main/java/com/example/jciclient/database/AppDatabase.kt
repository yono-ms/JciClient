package com.example.jciclient.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RemoteEntity::class, FileEntity::class, ExternalEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun remoteDao(): RemoteDao
    abstract fun fileDao(): FileDao
    abstract fun externalDao(): ExternalDao
}