package com.example.bullback.ui.profile.marginSettings

import com.example.bullback.data.model.auth.MarginUiModel
import com.example.bullback.data.remote.RetrofitClient
import com.example.bullback.data.remote.api.AuthApi
import com.example.bullback.data.repository.AuthRepository
import com.example.bullback.utlis.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class MarginRepository(
    private val authRepository: AuthRepository
) {

    suspend fun getMarginSettings(): Result<List<MarginUiModel>> {
        return try {
            val profile = authRepository.getProfile()

            if (profile is Resource.Success) {
                val segmentSettings = profile.data.settings.segmentSettings // Map<String, MarginApiModel>

                val margins = segmentSettings.map { (exchange, value) ->
                    MarginUiModel(
                        exchange = exchange,
                        tradingAllowed = value.tradeAllowed,
                        intraday = value.intradayLeverage,
                        holding = value.holdingLeverage,
                        strikeRange = value.strikeRange,
                        maxLot = value.maxLot,
                        maxOrderLot = value.maxOrderLot,
                        commissionType = value.commissionType,
                        commissionValue = value.commissionValue.toString()
                    )
                }

                Result.success(margins)
            } else {
                Result.failure(Exception("Failed to load profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}