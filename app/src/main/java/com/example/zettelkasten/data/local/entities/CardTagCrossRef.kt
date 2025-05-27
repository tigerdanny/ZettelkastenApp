package com.example.zettelkasten.data.local.entities

import androidx.room.Entity

/**
 * 卡片與標籤多對多關聯表
 */
@Entity(
    tableName = "card_tag_cross_ref",
    primaryKeys = ["cardId", "tagId"]
)
data class CardTagCrossRef(
    val cardId: Long,
    val tagId: Long
) 