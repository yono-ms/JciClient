package com.example.jciclient.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RemoteDao {
    @Insert
    suspend fun insertAll(vararg remoteEntity: RemoteEntity)

    @Delete
    suspend fun delete(remoteEntity: RemoteEntity)

    @Query("SELECT * FROM remote_entity")
    fun getAllLiveData(): LiveData<List<RemoteEntity>>
}