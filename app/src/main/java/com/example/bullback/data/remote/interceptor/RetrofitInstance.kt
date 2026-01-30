package com.example.bullback.data.remote.interceptor

import com.example.bullback.data.remote.api.AuthApi
import com.example.bullback.data.remote.api.WatchlistApiService
import com.example.bullback.utlis.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val watchlistApi: WatchlistApiService by lazy {
        retrofit.create(WatchlistApiService::class.java)
    }
}