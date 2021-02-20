package com.example.jciclient.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "external_entity")
data class ExternalEntity(
    @PrimaryKey
    @ColumnInfo(name = "ext") val ext: String,
)
