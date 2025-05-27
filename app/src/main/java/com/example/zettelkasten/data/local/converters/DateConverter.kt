package com.example.zettelkasten.data.local.converters

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room數據庫日期類型轉換器
 */
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
} 