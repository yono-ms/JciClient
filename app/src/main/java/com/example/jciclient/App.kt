package com.example.jciclient

import android.app.Application
import androidx.room.Room
import com.example.jciclient.database.AppDatabase

class App : Application() {
    companion object {
        lateinit var db: AppDatabase
    }

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database")
            .fallbackToDestructiveMigration().build()
    }
}