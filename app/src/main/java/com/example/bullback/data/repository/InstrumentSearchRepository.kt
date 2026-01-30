package com.example.bullback.data.repository

import com.example.bullback.data.model.auth.watchlist.instrumentsearch.SearchInstrument
import com.example.bullback.data.remote.api.WatchlistApiService
import com.example.bullback.utlis.Resource


class InstrumentSearchRepository(
    private val api: WatchlistApiService
) {

    suspend fun search(query: String, segment: String): Resource<List<SearchInstrument>> {
        return try {
            val response = api.searchInstruments(query, segment)
            if (response.isSuccessful && response.body()?.status == true) {
                Resource.Success(response.body()!!.data ?: emptyList())
            } else {
                Resource.Error(response.body()?.message ?: "Search failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

}
