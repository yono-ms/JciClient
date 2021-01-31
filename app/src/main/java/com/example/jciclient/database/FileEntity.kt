package com.example.jciclient.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import jcifs.smb.SmbFile

@Entity(tableName = "file_entity")
data class FileEntity(
    @PrimaryKey
    @ColumnInfo(name = "path") val path: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "content_type") val contentType: String?,
    @ColumnInfo(name = "directory") val directory: Boolean,
    @ColumnInfo(name = "file") val file: Boolean,
    @ColumnInfo(name = "parent") val parent: String,
) {
    companion object {
        fun from(smbFile: SmbFile): FileEntity {
            return FileEntity(
                smbFile.path,
                smbFile.name,
                smbFile.contentType,
                smbFile.isDirectory,
                smbFile.isFile,
                smbFile.parent
            )
        }
    }
}
