package com.example.bullback.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bullback.data.local.entity.TokenEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveToken(token: TokenEntity)

    @Query("SELECT token FROM tokens LIMIT 1")
    suspend fun getToken(): String?

    @Query("DELETE FROM tokens")
    suspend fun clearToken()

    @Query("SELECT COUNT(*) FROM tokens")
    suspend fun hasToken(): Int

    // Add this for Flow support
    @Query("SELECT * FROM tokens LIMIT 1")
    fun getTokenFlow(): Flow<TokenEntity?>
}