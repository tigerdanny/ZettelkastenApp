package com.example.zettelkasten.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 標籤實體
 */
@Entity(
    tableName = "tags",
    indices = [Index(value = ["name"], unique = true)]
)
data class Tag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * 標籤名稱
     */
    val name: String,
    
    /**
     * 標籤顏色
     */
    var colorHex: String? = null,
    
    /**
     * 標籤描述
     */
    var description: String? = null
) 