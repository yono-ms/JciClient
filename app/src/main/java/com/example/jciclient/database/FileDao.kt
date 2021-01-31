package com.example.jciclient.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FileDao {
    @Insert
    suspend fun insertAll(vararg fileEntity: FileEntity)

    @Delete
    suspend fun delete(fileEntity: FileEntity)

    @Query("SELECT * FROM file_entity")
    suspend fun getAll(): List<FileEntity>

    @Query("SELECT * FROM file_entity WHERE parent = :parent ORDER BY file, name")
    suspend fun get(parent: String): List<FileEntity>

    @Query("SELECT * FROM file_entity WHERE parent = :parent ORDER BY file, name")
    fun getLiveData(parent: String): LiveData<List<FileEntity>>

    @Transaction
    suspend fun deleteAll() {
        getAll().forEach { e ->
            delete(e)
        }
    }
}