package com.alphadoer.trader.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alphadoer.trader.data.local.converters.ListStringConverter
import com.alphadoer.trader.data.local.converters.LocalDateTimeConverter
import com.alphadoer.trader.data.local.converters.MapStringStringConverter
import com.alphadoer.trader.data.local.dao.AIAnalysisCacheDao
import com.alphadoer.trader.data.local.dao.MistakePatternDao
import com.alphadoer.trader.data.local.dao.ProcessStateDao
import com.alphadoer.trader.data.local.dao.SectorDao
import com.alphadoer.trader.data.local.dao.StepConfigDao
import com.alphadoer.trader.data.local.dao.StockDao
import com.alphadoer.trader.data.local.dao.TradeJournalDao
import com.alphadoer.trader.data.local.dao.TradeMistakeDao
import com.alphadoer.trader.data.local.dao.TradeRecordDao
import com.alphadoer.trader.data.local.entity.AIAnalysisCacheEntity
import com.alphadoer.trader.data.local.entity.MistakePatternEntity
import com.alphadoer.trader.data.local.entity.ProcessStateEntity
import com.alphadoer.trader.data.local.entity.SectorEntity
import com.alphadoer.trader.data.local.entity.StepConfigEntity
import com.alphadoer.trader.data.local.entity.StockEntity
import com.alphadoer.trader.data.local.entity.TradeJournalEntity
import com.alphadoer.trader.data.local.entity.TradeMistakeEntity
import com.alphadoer.trader.data.local.entity.TradeRecordEntity

/**
 * AlphaDoer Room 数据库
 * 
 * 数据库版本管理：
 * - Version 1: 初始版本，包含所有基础表
 */
@Database(
    entities = [
        TradeJournalEntity::class,
        TradeRecordEntity::class,
        AIAnalysisCacheEntity::class,
        StockEntity::class,
            SectorEntity::class,
            MistakePatternEntity::class,
            ProcessStateEntity::class,
            StepConfigEntity::class,
            TradeMistakeEntity::class
    ],
            version = 3,
    exportSchema = false // 暂时禁用导出以避免Windows权限问题
)
@TypeConverters(
    ListStringConverter::class,
    LocalDateTimeConverter::class,
    MapStringStringConverter::class
)
abstract class AlphaDoerDatabase : RoomDatabase() {
    
    abstract fun tradeJournalDao(): TradeJournalDao
    abstract fun tradeRecordDao(): TradeRecordDao
    abstract fun aiAnalysisCacheDao(): AIAnalysisCacheDao
    abstract fun stockDao(): StockDao
    abstract fun sectorDao(): SectorDao
            abstract fun mistakePatternDao(): MistakePatternDao
            abstract fun processStateDao(): ProcessStateDao
            abstract fun stepConfigDao(): StepConfigDao
            abstract fun tradeMistakeDao(): TradeMistakeDao
    
    companion object {
        const val DATABASE_NAME = "alpha_doer.db"
    }
}
