package com.example.jciclient.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExternalDao {
    @Insert
    suspend fun insert(externalEntity: ExternalEntity)

    @Delete
    suspend fun delete(externalEntity: ExternalEntity)

    @Query("SELECT * FROM external_entity")
    suspend fun getAll(): List<ExternalEntity>

    @Query("SELECT * FROM external_entity ORDER BY ext")
    fun getLiveData(): LiveData<List<ExternalEntity>>
}