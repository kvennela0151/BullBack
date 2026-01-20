package com.example.bullback.data.repository

import android.content.Context
import com.example.bullback.data.model.auth.*
import com.example.bullback.data.remote.api.AuthApi
import com.example.bullback.utlis.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Response
import android.util.Log
import com.example.bullback.data.remote.RetrofitClient
import com.example.bullback.data.remote.api.MarketApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody


class AuthRepository private constructor(
    private val context: Context
) {

    private val tokenRepository = TokenRepository.getInstance(context)

    private val authApi: AuthApi by lazy {
        RetrofitClient.setTokenRepository(tokenRepository)
        RetrofitClient.createService(AuthApi::class.java)
    }

    private val marketApi: MarketApi by lazy {
        RetrofitClient.createService(MarketApi::class.java)
    }


    private fun String.toFormData() =
        toRequestBody("text/plain".toMediaTypeOrNull())

    private fun String?.toOptionalFormData() =
        this?.toFormData()

    /* ---------------------------------------------------
       LOGIN
    --------------------------------------------------- */

    suspend fun login(username: String, password: String): Resource<LoginResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val response = authApi.login(
                    username.toFormData(),
                    password.toFormData()
                )

                val result = handleResponse(response)

                if (result is Resource.Success) {
                    val token = result.data.accessToken.trim()

                    if (token.isNotEmpty()) {
                        //  Clear old token before saving new
                        tokenRepository.clearToken()
                        tokenRepository.saveToken(token)

                        Log.d("AuthRepository", "✅ New token saved: ${token.take(30)}")
                    }
                }

                result
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login error", e)
            Resource.Error("Login failed: ${e.message}")
        }
    }


    /* ---------------------------------------------------
       SIGNUP
    --------------------------------------------------- */

    suspend fun signup(
        fullName: String,
        username: String,
        email: String,
        phone: String,
        password: String,
        referralCode: String? = null
    ): Resource<SignupResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val response = authApi.signup(
                    fullName = fullName.toFormData(),
                    username = username.toFormData(),
                    email = email.toFormData(),
                    phone = phone.toFormData(),
                    password = password.toFormData(),
                    referralCode = referralCode.toOptionalFormData()
                )

                handleResponse(response)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Signup error", e)
            Resource.Error("Signup failed: ${e.message}")
        }
    }

    /* ---------------------------------------------------
       CHECK USERNAME
    --------------------------------------------------- */

    suspend fun checkUsername(username: String): Resource<CheckUsernameResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val response = authApi.checkUsername(username)
                handleResponse(response)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Username check error", e)
            Resource.Error("Username check failed")
        }
    }

    /* ---------------------------------------------------
       DEMO LOGIN
    --------------------------------------------------- */

    suspend fun demoLogin(): Resource<LoginResponse> {
        return try {
            withContext(Dispatchers.IO) {
                val response = authApi.demoLogin()
                val result = handleResponse(response)

                if (result is Resource.Success) {
                    val rawToken = result.data.accessToken
                        .removePrefix("Bearer ")
                        .trim()

                    tokenRepository.saveToken(rawToken)
                }

                result
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Demo login error", e)
            Resource.Error("Demo login failed")
        }
    }

    /* ---------------------------------------------------
       USER PROFILE
    --------------------------------------------------- */

    suspend fun getProfile(): Resource<User> {
        return try {
            withContext(Dispatchers.IO) {
                val response = authApi.getProfile()
                handleResponse(response)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Profile error", e)
            Resource.Error("Failed to load profile")
        }
    }

    /* ---------------------------------------------------
       TOP COMMODITIES
    --------------------------------------------------- */

    suspend fun getTopCommodities(): Resource<List<CommodityData>> {
        return try {
            withContext(Dispatchers.IO) {

                val token = tokenRepository.getToken()

                if (token.isNullOrEmpty()) {
                    return@withContext Resource.Error("Token not found")
                }

                val authHeader = "Bearer $token"

                Log.d("MARKET_API", "Using header: ${authHeader.take(30)}...")

                val response = marketApi.getTopCommodities(authHeader)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status) {
                        Resource.Success(body.data)
                    } else {
                        Resource.Error("Empty response")
                    }
                } else {
                    Resource.Error("Error ${response.code()}")
                }
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Commodities error", e)
            Resource.Error(e.message ?: "Unknown error")
        }
    }



    /* ---------------------------------------------------
       RESPONSE HANDLER
    --------------------------------------------------- */

    private suspend fun <T> handleResponse(
        response: Response<T>
    ): Resource<T> {
        return try {
            if (response.isSuccessful) {
                response.body()?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Empty response")
            } else {
                val code = response.code()

                if (code == 401) {
                    tokenRepository.clearToken()
                }

                Resource.Error("Error ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Response error", e)
            Resource.Error("Response processing failed")
        }
    }

    /* ---------------------------------------------------
       TOKEN HELPERS
    --------------------------------------------------- */

    suspend fun getToken(): String? =
        tokenRepository.getToken()

    fun getTokenSync(): String? =
        runBlocking { tokenRepository.getToken() }

    suspend fun hasToken(): Boolean =
        tokenRepository.hasToken()

    fun hasTokenSync(): Boolean =
        runBlocking { tokenRepository.hasToken() }


    /* ---------------------------------------------------
       SINGLETON
    --------------------------------------------------- */

    companion object {
        @Volatile
        private var INSTANCE: AuthRepository? = null

        fun getInstance(context: Context): AuthRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = AuthRepository(context.applicationContext)
                // Initialize RetrofitClient with token repository
                RetrofitClient.setTokenRepository(instance.tokenRepository)
                INSTANCE = instance
                instance
            }
        }
    }
}