package com.example.bullback.utlis


import com.example.bullback.data.model.positions.PositionsItem
import kotlin.math.abs

object FinanceCalculator {

    /** -----------------------------
     *  SINGLE POSITION PNL
     *  ----------------------------- */
    fun calculatePnl(position: PositionsItem, liveLtp: Double?): Double {
        val netQty = position.netQuantity.toDoubleOrNull() ?: 0.0
        val avgPrice = position.averagePrice.toDoubleOrNull() ?: 0.0
        val realized = position.realizedPnl.toDoubleOrNull() ?: 0.0

        // Closed → use backend truth
        if (netQty == 0.0) return realized

        val ltp = liveLtp ?: avgPrice
        val qty = abs(netQty)

        return if (position.side == "BUY") {
            (ltp - avgPrice) * qty
        } else {
            (avgPrice - ltp) * qty
        }
    }

    /** -----------------------------
     *  TOTAL PNL (OPEN + CLOSED)
     *  ----------------------------- */
    fun calculateTotalPnl(
        positions: List<PositionsItem>,
        livePrices: Map<String, Double>
    ): Double {
        var total = 0.0

        positions.forEach { pos ->
            total += calculatePnl(pos, livePrices[pos.instrumentToken])
        }

        return total
    }

    /** --------------------------------------------
     *  UTILISED FUNDS = sum(abs(qty) * avgPrice)
     *  -------------------------------------------- */
    fun calculateUtilisedFunds(positions: List<PositionsItem>): Double {
        var utilised = 0.0

        positions.forEach { pos ->
            val qty = pos.netQuantity.toDoubleOrNull() ?: 0.0
            val avgPrice = pos.averagePrice.toDoubleOrNull() ?: 0.0

            utilised += abs(qty) * avgPrice
        }

        return utilised
    }
}
