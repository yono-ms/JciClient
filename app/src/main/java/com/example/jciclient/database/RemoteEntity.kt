package com.example.jciclient.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import jcifs.smb.NtlmPasswordAuthenticator

@Entity(tableName = "remote_entity")
data class RemoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "remote_name") val remoteName: String,
    @ColumnInfo(name = "domain_name") val domainName: String,
    @ColumnInfo(name = "share_name") val shareName: String,
    @ColumnInfo(name = "account_name") val accountName: String,
    @ColumnInfo(name = "account_password") val accountPassword: String,
) {
    fun credentials(): NtlmPasswordAuthenticator {
        return NtlmPasswordAuthenticator(
            domainName,
            accountName,
            accountPassword
        )
    }
}
