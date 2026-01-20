package com.example.bullback.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tokens")
data class TokenEntity(
    @PrimaryKey
    @ColumnInfo(name = "token")
    val token: String
)


