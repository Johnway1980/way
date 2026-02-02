package com.alphadoer.trader.data.local.converters

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

/**
 * Room TypeConverter: Map<String, String> ↔ String
 */
class MapStringStringConverter {
    private val moshi = Moshi.Builder().build()
    private val mapType = Types.newParameterizedType(
        Map::class.java,
        String::class.java,
        String::class.java
    )
    private val adapter = moshi.adapter<Map<String, String>>(mapType)

    @TypeConverter
    fun fromString(value: String?): Map<String, String> {
        return if (value.isNullOrBlank()) {
            emptyMap()
        } else {
            adapter.fromJson(value) ?: emptyMap()
        }
    }

    @TypeConverter
    fun toString(map: Map<String, String>?): String {
        return if (map.isNullOrEmpty()) {
            "{}"
        } else {
            adapter.toJson(map) ?: "{}"
        }
    }
}
