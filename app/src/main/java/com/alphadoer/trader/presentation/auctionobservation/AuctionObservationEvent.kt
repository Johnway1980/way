package com.alphadoer.trader.presentation.auctionobservation

import com.alphadoer.trader.domain.model.AuctionObservation

/**
 * 集合竞价观察事件
 */
sealed class AuctionObservationEvent {
    data class MarketSentimentChanged(val sentiment: AuctionObservation.MarketSentiment) : AuctionObservationEvent()
    data class FeelingChanged(val feeling: Int) : AuctionObservationEvent()
    data class AddKeyObservation(val observation: String) : AuctionObservationEvent()
    data class RemoveKeyObservation(val index: Int) : AuctionObservationEvent()
    data class VolumeAnalysisChanged(val analysis: String) : AuctionObservationEvent()
    data class PriceTrendChanged(val trend: AuctionObservation.PriceTrend) : AuctionObservationEvent()
    data class AddStockObservation(val stock: AuctionObservation.StockObservation) : AuctionObservationEvent()
    data class RemoveStockObservation(val stockCode: String) : AuctionObservationEvent()
    data class NotesChanged(val notes: String) : AuctionObservationEvent()
    object SaveObservation : AuctionObservationEvent()
    object LoadObservation : AuctionObservationEvent()
    object ClearError : AuctionObservationEvent()
}
