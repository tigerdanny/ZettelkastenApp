package com.example.zettelkasten.data.local.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.zettelkasten.data.local.entities.Card
import com.example.zettelkasten.data.local.entities.CardTagCrossRef
import com.example.zettelkasten.data.local.entities.Tag

/**
 * 表示卡片及其關聯標籤
 */
data class CardWithTags(
    @Embedded val card: Card,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = CardTagCrossRef::class,
            parentColumn = "cardId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>
) 