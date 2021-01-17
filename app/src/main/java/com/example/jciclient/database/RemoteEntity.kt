package com.example.jciclient.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_entity")
data class RemoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "domain_name") val domainName: String,
    @ColumnInfo(name = "account_name") val accountName: String,
    @ColumnInfo(name = "account_password") val accountPassword: String,
)
