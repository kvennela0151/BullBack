package com.example.bullback.data.repository

import android.content.Context
import com.example.bullback.data.local.AppDatabase
import com.example.bullback.data.local.entity.TokenEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class TokenRepository private constructor(context: Context) {

    private val tokenDao = AppDatabase.getInstance(context).tokenDao()

    suspend fun saveToken(token: String) {
        val tokenEntity = TokenEntity(token = token)
        tokenDao.saveToken(tokenEntity)
    }

    suspend fun getToken(): String? {
        return tokenDao.getToken()
    }

    fun getTokenSync(): String? {
        return runBlocking {
            tokenDao.getToken()
        }
    }


    suspend fun hasToken(): Boolean {
        return tokenDao.hasToken() > 0
    }

    suspend fun clearToken() {
        tokenDao.clearToken()
    }

    fun getTokenFlow(): Flow<String?> {
        return tokenDao.getTokenFlow().map { it?.token }
    }

    companion object {
        @Volatile
        private var INSTANCE: TokenRepository? = null

        fun getInstance(context: Context): TokenRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = TokenRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}