package com.example.bullback.data.repository

import com.example.bullback.data.model.auth.watchlist.addwatchlist.AddWatchlistRequest
import com.example.bullback.data.model.auth.watchlist.addwatchlist.AddWatchlistResponse
import com.example.bullback.data.model.auth.watchlist.addwatchlist.RemoveWatchlistResponse
import com.example.bullback.data.model.auth.watchlist.addwatchlist.WatchlistData
import com.example.bullback.data.model.auth.watchlist.deletewatchlist.RemoveWatchlistRequest
import com.example.bullback.data.remote.api.WatchlistApiService
import com.example.bullback.utlis.Resource
import retrofit2.Response

class WatchlistRepository(
    private val api: WatchlistApiService
) {

    suspend fun getWatchlist(): Resource<List<WatchlistData>> {
        return try {
            val response = api.getWatchlist()
            if (response.isSuccessful && response.body()?.status == true) {
                Resource.Success(response.body()!!.data)
            } else {
                Resource.Error("Failed to load watchlist")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun addInstrument(request: AddWatchlistRequest): Resource<Unit> {
        return try {
            val response: Response<AddWatchlistResponse> = api.addInstrument(request)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to add instrument")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun removeSymbols(
        watchlistId: Int,
        tokens: List<String>
    ): Resource<RemoveWatchlistResponse> {
        return try {
            val response = api.removeSymbols(
                RemoveWatchlistRequest(
                    id = watchlistId,
                    symbols = tokens
                )
            )
            if (response.isSuccessful && response.body()?.status == true) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to remove symbols")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

}

