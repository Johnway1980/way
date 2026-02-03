package com.alphadoer.trader.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.alphadoer.trader.data.local.converters.MapStringStringConverter

/**
 * 流程状态实体
 */
@Entity(tableName = "process_state")
@TypeConverters(MapStringStringConverter::class)
data class ProcessStateEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "date")
    val date: String, // yyyy-MM-dd
    
    @ColumnInfo(name = "step_states_json")
    val stepStatesJson: String, // JSON格式的步骤状态
    
    @ColumnInfo(name = "overall_progress")
    val overallProgress: Double,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
