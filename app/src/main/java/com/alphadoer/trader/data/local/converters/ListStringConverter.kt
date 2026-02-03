package com.alphadoer.trader.data.local.converters

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

/**
 * Room TypeConverter: List<String> ↔ String
 */
class ListStringConverter {
    private val moshi = Moshi.Builder().build()
    private val listStringType = Types.newParameterizedType(List::class.java, String::class.java)
    private val adapter = moshi.adapter<List<String>>(listStringType)

    @TypeConverter
    fun fromString(value: String?): List<String> {
        return if (value.isNullOrBlank()) {
            emptyList()
        } else {
            adapter.fromJson(value) ?: emptyList()
        }
    }

    @TypeConverter
    fun toString(list: List<String>?): String {
        return if (list.isNullOrEmpty()) {
            "[]"
        } else {
            adapter.toJson(list) ?: "[]"
        }
    }
}
