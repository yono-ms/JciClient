package com.example.jciclient

import android.app.Application
import androidx.room.Room
import com.example.jciclient.database.AppDatabase
import java.net.Socket
import java.util.*

class App : Application() {
    companion object {
        lateinit var db: AppDatabase

        val cifsProperties by lazy {
            Properties().apply {
                setProperty("jcifs.smb.client.minVersion", "SMB202")
                setProperty("jcifs.smb.client.maxVersion", "SMB300")
            }
        }

        fun getPort(): Int {
            return Socket().use {
                it.bind(null)
                it.localPort
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database")
            .fallbackToDestructiveMigration().build()
    }
}