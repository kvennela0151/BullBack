package com.example.bullback.data.remote

import com.example.bullback.data.remote.interceptor.AuthInterceptor
import com.example.bullback.data.repository.TokenRepository
import com.example.bullback.utlis.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking


object RetrofitClient {

    private var tokenRepository: TokenRepository? = null

    @Volatile
    private var retrofit: Retrofit? = null

    fun setTokenRepository(repo: TokenRepository) {
        tokenRepository = repo
        retrofit = null
    }

    private fun createRetrofit(): Retrofit {

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(
                AuthInterceptor(
                    tokenRepository
                        ?: throw IllegalStateException("TokenRepository not set")
                )
            )
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun getRetrofit(): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: createRetrofit().also { retrofit = it }
        }
    }

    fun <T> createService(serviceClass: Class<T>): T {
        return getRetrofit().create(serviceClass)
    }
}
