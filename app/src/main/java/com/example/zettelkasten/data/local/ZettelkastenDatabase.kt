package com.example.zettelkasten.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.zettelkasten.data.local.converters.DateConverter
import com.example.zettelkasten.data.local.converters.ListConverter
import com.example.zettelkasten.data.local.dao.CardDao
import com.example.zettelkasten.data.local.dao.TagDao
import com.example.zettelkasten.data.local.entities.Card
import com.example.zettelkasten.data.local.entities.Tag
import com.example.zettelkasten.data.local.entities.CardTagCrossRef

/**
 * 卡片盒筆記應用的Room數據庫
 */
@Database(
    entities = [
        Card::class, 
        Tag::class,
        CardTagCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(DateConverter::class, ListConverter::class)
abstract class ZettelkastenDatabase : RoomDatabase() {
    
    /**
     * 獲取卡片DAO
     */
    abstract fun cardDao(): CardDao
    
    /**
     * 獲取標籤DAO
     */
    abstract fun tagDao(): TagDao
} 
