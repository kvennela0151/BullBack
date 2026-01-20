package com.example.bullback.data.remote.interceptor

import android.util.Log
import com.example.bullback.data.repository.TokenRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(
    private val tokenRepository: TokenRepository
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()

        // Skip auth endpoints
        if (isAuthEndpoint(url)) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking {
            tokenRepository.getToken()
        }

        if (token.isNullOrBlank()) {
            Log.d("AuthInterceptor", "No token found")
            return chain.proceed(originalRequest)
        }

        Log.d("AuthInterceptor", "Using token: ${token.take(10)}...")

        val request = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(request)
    }

    private fun isAuthEndpoint(url: String): Boolean {
        val authEndpoints = listOf(
            "api/v1/auth/login",
            "api/v1/auth/register",
            "api/v1/auth/instant-demo",
            "api/v1/users/check-username"
        )
        return authEndpoints.any { url.contains(it) }
    }
}