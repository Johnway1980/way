package com.alphadoer.trader.data.repository

import android.util.Log
import com.alphadoer.trader.data.local.dao.AIAnalysisCacheDao
import com.alphadoer.trader.data.local.entity.AIAnalysisCacheEntity
import com.alphadoer.trader.domain.model.SectorSelectionRecord
import com.alphadoer.trader.domain.repository.SectorSelectionRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 使用通用 AI 缓存表持久化板块选择记录
 */
class SectorSelectionRepositoryImpl @Inject constructor(
    private val aiAnalysisCacheDao: AIAnalysisCacheDao
) : SectorSelectionRepository {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(SectorSelectionRecord::class.java)

    override suspend fun saveSelection(record: SectorSelectionRecord): Result<Unit> = try {
        val json = adapter.toJson(record) ?: "{}"
        val key = "sector_selection_${'$'}{record.date}_${'$'}{record.sectorCode}"
        val entity = AIAnalysisCacheEntity.create(
            cacheKey = key,
            analysisType = "SECTOR_SELECTION",
            content = json,
            summary = record.sectorName,
            confidence = null,
            expiresAt = null
        )
        aiAnalysisCacheDao.insertCache(entity)
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("SectorSelectionRepo", "保存失败: ${'$'}{e.message}", e)
        Result.failure(e)
    }

    override suspend fun deleteSelection(id: String): Result<Unit> = try {
        aiAnalysisCacheDao.deleteCacheByKey(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getSelectionsByDate(date: String): Flow<List<SectorSelectionRecord>> {
        return aiAnalysisCacheDao.getCachesByTypeFlow("SECTOR_SELECTION").map { list ->
            list.mapNotNull { entity ->
                try {
                    val rec = adapter.fromJson(entity.content)
                    if (rec?.date == date) rec else null
                } catch (e: Exception) {
                    Log.e("SectorSelectionRepo", "解析失败: ${'$'}{e.message}", e)
                    null
                }
            }
        }
    }
}
