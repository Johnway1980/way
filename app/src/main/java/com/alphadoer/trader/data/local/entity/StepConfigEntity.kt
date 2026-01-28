package com.alphadoer.trader.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.alphadoer.trader.data.local.converters.ListStringConverter
import com.alphadoer.trader.data.local.converters.MapStringStringConverter

/**
 * 步骤配置实体
 */
@Entity(tableName = "step_config")
@TypeConverters(ListStringConverter::class, MapStringStringConverter::class)
data class StepConfigEntity(
    @PrimaryKey
    @ColumnInfo(name = "step_id")
    val stepId: String,
    
    @ColumnInfo(name = "step_type")
    val stepType: String, // StepType的name
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "icon")
    val icon: String?,
    
    @ColumnInfo(name = "estimated_duration")
    val estimatedDuration: Long,
    
    @ColumnInfo(name = "dependencies")
    val dependencies: List<String>, // 依赖的步骤ID列表
    
    @ColumnInfo(name = "time_window_start")
    val timeWindowStart: String?, // HH:mm格式
    
    @ColumnInfo(name = "time_window_end")
    val timeWindowEnd: String?, // HH:mm格式
    
    @ColumnInfo(name = "route")
    val route: String,
    
    @ColumnInfo(name = "required")
    val required: Boolean,
    
    @ColumnInfo(name = "order")
    val order: Int
)
