package com.alphadoer.trader.presentation.auctionobservation

import com.alphadoer.trader.domain.model.AuctionObservation

/**
 * 集合竞价观察UI状态
 */
data class AuctionObservationUiState(
    val observation: AuctionObservation? = null,
    val marketSentiment: AuctionObservation.MarketSentiment = AuctionObservation.MarketSentiment.NEUTRAL,
    val feeling: Int = 3, // 1-5
    val keyObservations: List<String> = emptyList(),
    val volumeAnalysis: String = "",
    val priceTrend: AuctionObservation.PriceTrend = AuctionObservation.PriceTrend.FLAT,
    val focusStocks: List<AuctionObservation.StockObservation> = emptyList(),
    val notes: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)
