package com.example.jciclient

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.jciclient.database.AppDatabase
import com.example.jciclient.database.ExternalEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var db: AppDatabase

    @Test
    fun test() = runBlocking {
        assertNotNull("db is null.", db)
        db.externalDao().getAll().let {
            assertEquals("getAll check zero.", 0, it.size)
        }
        db.externalDao().insert(ExternalEntity("mp4"))
        db.externalDao().getAll().let {
            assertEquals("getAll check 1.", 1, it.size)
            db.externalDao().delete(it.first())
        }
        db.externalDao().getAll().let {
            assertEquals("getAll check zero.", 0, it.size)
        }
        delay(1000)
    }

    @Before
    fun before() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "test_database"
        ).build()
    }

    @After
    fun after() {
        db.clearAllTables()
    }
}