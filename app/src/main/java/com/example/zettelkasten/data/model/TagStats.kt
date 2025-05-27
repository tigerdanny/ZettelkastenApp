package com.example.zettelkasten.data.model

import com.example.zettelkasten.data.local.entities.Tag

/**
 * 標籤統計數據
 */
data class TagStats(
    val tag: Tag,
    val cardCount: Int
) {
    /**
     * 根據使用頻率計算顯示大小
     */
    fun getDisplaySize(minCardCount: Int, maxCardCount: Int): Float {
        if (maxCardCount == minCardCount) return 1.0f
        
        val ratio = (cardCount - minCardCount).toFloat() / (maxCardCount - minCardCount)
        // 返回 0.8f 到 1.4f 之間的大小倍數
        return 0.8f + (ratio * 0.6f)
    }
    
    /**
     * 獲取顯示文本
     */
    fun getDisplayText(): String {
        return "${tag.name} ($cardCount)"
    }
} 
