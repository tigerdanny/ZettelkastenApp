package com.example.zettelkasten.data.local.dao

import androidx.room.*
import com.example.zettelkasten.data.local.entities.CardTagCrossRef
import com.example.zettelkasten.data.local.entities.Tag
import com.example.zettelkasten.data.local.relations.TagWithCards
import kotlinx.coroutines.flow.Flow

/**
 * 標籤數據訪問對象
 */
@Dao
interface TagDao {
    /**
     * 插入新標籤 (同步版本)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTagSync(tag: Tag): Long
    
    /**
     * 插入新標籤 (協程版本)
     */
    suspend fun insertTag(tag: Tag): Long = insertTagSync(tag)
    
    /**
     * 插入多個標籤 (同步版本)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTagsSync(tags: List<Tag>): List<Long>
    
    /**
     * 插入多個標籤 (協程版本)
     */
    suspend fun insertTags(tags: List<Tag>): List<Long> = insertTagsSync(tags)
    
    /**
     * 更新標籤 (同步版本)
     */
    @Update
    fun updateTagSync(tag: Tag)
    
    /**
     * 更新標籤 (協程版本)
     */
    suspend fun updateTag(tag: Tag) = updateTagSync(tag)
    
    /**
     * 刪除標籤 (同步版本)
     */
    @Delete
    fun deleteTagSync(tag: Tag)
    
    /**
     * 刪除標籤 (協程版本)
     */
    suspend fun deleteTag(tag: Tag) = deleteTagSync(tag)
    
    /**
     * 添加卡片標籤關聯 (同步版本)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addTagToCardSync(cardTagCrossRef: CardTagCrossRef)
    
    /**
     * 添加卡片標籤關聯 (協程版本)
     */
    suspend fun addTagToCard(cardTagCrossRef: CardTagCrossRef) = addTagToCardSync(cardTagCrossRef)
    
    /**
     * 移除卡片標籤關聯 (同步版本)
     */
    @Delete
    fun removeTagFromCardSync(cardTagCrossRef: CardTagCrossRef)
    
    /**
     * 移除卡片標籤關聯 (協程版本)
     */
    suspend fun removeTagFromCard(cardTagCrossRef: CardTagCrossRef) = removeTagFromCardSync(cardTagCrossRef)
    
    /**
     * 獲取所有標籤
     */
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<Tag>>
    
    /**
     * 獲取指定ID的標籤
     */
    @Query("SELECT * FROM tags WHERE id = :tagId")
    fun getTagById(tagId: Long): Flow<Tag?>
    
    /**
     * 獲取指定標籤名稱的標籤 (同步版本)
     */
    @Query("SELECT * FROM tags WHERE name = :tagName LIMIT 1")
    fun getTagByNameSync(tagName: String): Tag?
    
    /**
     * 獲取指定標籤名稱的標籤 (協程版本)
     */
    suspend fun getTagByName(tagName: String): Tag? = getTagByNameSync(tagName)
    
    /**
     * 搜索標籤
     */
    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchTags(query: String): Flow<List<Tag>>
    
    /**
     * 獲取卡片的所有標籤
     */
    @Query("""
        SELECT t.* 
        FROM tags t 
        INNER JOIN card_tag_cross_ref ct ON t.id = ct.tagId
        WHERE ct.cardId = :cardId
        ORDER BY t.name ASC
    """)
    fun getTagsForCard(cardId: Long): Flow<List<Tag>>
    
    /**
     * 獲取卡片的所有標籤（同步版本）
     */
    @Query("""
        SELECT t.* 
        FROM tags t 
        INNER JOIN card_tag_cross_ref ct ON t.id = ct.tagId
        WHERE ct.cardId = :cardId
        ORDER BY t.name ASC
    """)
    fun getTagsForCardSync(cardId: Long): List<Tag>
    
    /**
     * 獲取指定標籤及其所有卡片
     */
    @Transaction
    @Query("SELECT * FROM tags WHERE id = :tagId")
    fun getTagWithCards(tagId: Long): Flow<TagWithCards?>
    
    /**
     * 獲取所有帶卡片的標籤
     */
    @Transaction
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTagsWithCards(): Flow<List<TagWithCards>>
} 
