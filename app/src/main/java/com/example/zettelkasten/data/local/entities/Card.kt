package com.example.zettelkasten.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 筆記卡片實體
 */
@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * 卡片標題
     */
    var title: String,
    
    /**
     * 卡片內容
     */
    var content: String,
    
    /**
     * 創建日期
     */
    val createdAt: Date = Date(),
    
    /**
     * 最後修改日期
     */
    var modifiedAt: Date = Date(),
    
    /**
     * 卡片是否已加密
     */
    var isEncrypted: Boolean = false,
    
    /**
     * 卡片顏色（如深色模式或不同主題色）
     */
    var colorHex: String? = null
) 