package com.example.zettelkasten.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room數據庫列表轉換器
 */
class ListConverter {
    /**
     * 將字符串列表轉換為JSON字符串
     */
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return if (value == null) null else Gson().toJson(value)
    }

    /**
     * 將JSON字符串轉換為字符串列表
     */
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) return null
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }
    
    /**
     * 將長整型列表轉換為JSON字符串
     */
    @TypeConverter
    fun fromLongList(value: List<Long>?): String? {
        return if (value == null) null else Gson().toJson(value)
    }

    /**
     * 將JSON字符串轉換為長整型列表
     */
    @TypeConverter
    fun toLongList(value: String?): List<Long>? {
        if (value == null) return null
        val listType = object : TypeToken<List<Long>>() {}.type
        return Gson().fromJson(value, listType)
    }
} 