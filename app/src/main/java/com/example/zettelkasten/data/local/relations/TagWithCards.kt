package com.example.zettelkasten.data.local.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.example.zettelkasten.data.local.entities.Card
import com.example.zettelkasten.data.local.entities.CardTagCrossRef
import com.example.zettelkasten.data.local.entities.Tag

/**
 * 表示標籤及其關聯卡片
 */
data class TagWithCards(
    @Embedded val tag: Tag,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = CardTagCrossRef::class,
            parentColumn = "tagId",
            entityColumn = "cardId"
        )
    )
    val cards: List<Card>
) 