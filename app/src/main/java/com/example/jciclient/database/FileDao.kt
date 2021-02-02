package com.example.jciclient.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg fileEntity: FileEntity)

    @Delete
    suspend fun delete(fileEntity: FileEntity)

    @Query("SELECT * FROM file_entity")
    suspend fun getAll(): List<FileEntity>

    @Query("SELECT * FROM file_entity WHERE remote_id = :remoteId AND path = :path LIMIT 1")
    suspend fun get(remoteId: Int, path: String): FileEntity?

    @Query("SELECT * FROM file_entity WHERE remote_id = :remoteId AND parent = :parent ORDER BY file, name")
    suspend fun getChildren(remoteId: Int, parent: String): List<FileEntity>

    @Query("SELECT COUNT(*) FROM file_entity WHERE remote_id = :remoteId AND parent = :parent")
    suspend fun getChildrenCount(remoteId: Int, parent: String): Int

    @Query("SELECT * FROM file_entity WHERE remote_id = :remoteId AND parent = :parent ORDER BY file, name")
    fun getLiveData(remoteId: Int, parent: String): LiveData<List<FileEntity>>

    @Transaction
    suspend fun deleteAll() {
        getAll().forEach { e ->
            delete(e)
        }
    }

    @Transaction
    suspend fun deleteAllChildren(remoteId: Int, parent: String) {
        getChildren(remoteId, parent).forEach { e ->
            delete(e)
        }
    }
}