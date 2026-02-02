package com.alphadoer.trader.domain.repository

import com.alphadoer.trader.domain.model.SectorSelectionRecord
import kotlinx.coroutines.flow.Flow

/**
 * 板块选择记录仓库
 */
interface SectorSelectionRepository {
    suspend fun saveSelection(record: SectorSelectionRecord): Result<Unit>
    suspend fun deleteSelection(id: String): Result<Unit>
    fun getSelectionsByDate(date: String): Flow<List<SectorSelectionRecord>>
}
