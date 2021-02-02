package com.example.jciclient.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import jcifs.smb.SmbFile

@Entity(tableName = "file_entity", primaryKeys = ["remote_id", "path"])
data class FileEntity(
    @ColumnInfo(name = "remote_id") val remoteId: Int,
    @ColumnInfo(name = "path") val path: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "content_type") val contentType: String?,
    @ColumnInfo(name = "directory") val directory: Boolean,
    @ColumnInfo(name = "file") val file: Boolean,
    @ColumnInfo(name = "parent") val parent: String,
    @ColumnInfo(name = "last_modified") val lastModified: Long,
) {
    companion object {
        fun from(remoteId: Int, smbFile: SmbFile): FileEntity {
            return FileEntity(
                remoteId,
                smbFile.path,
                smbFile.name,
                smbFile.contentType,
                smbFile.isDirectory,
                smbFile.isFile,
                smbFile.parent,
                smbFile.lastModified,
            )
        }
    }
}
