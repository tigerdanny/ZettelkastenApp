package com.example.zettelkasten.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.zettelkasten.data.local.entities.Card
import com.example.zettelkasten.data.local.relations.CardWithTags
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 卡片數據訪問對象
 */
@Dao
interface CardDao {
    /**
     * 插入新卡片 (同步版本)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCardSync(card: Card): Long
    
    /**
     * 插入新卡片 (協程版本)
     */
    suspend fun insertCard(card: Card): Long = insertCardSync(card)
    
    /**
     * 更新卡片 (同步版本)
     */
    @Update
    fun updateCardSync(card: Card)
    
    /**
     * 更新卡片 (協程版本)
     */
    suspend fun updateCard(card: Card) = updateCardSync(card)
    
    /**
     * 刪除卡片 (同步版本)
     */
    @Delete
    fun deleteCardSync(card: Card)
    
    /**
     * 刪除卡片 (協程版本)
     */
    suspend fun deleteCard(card: Card) = deleteCardSync(card)
    
    /**
     * 獲取所有卡片
     */
    @Query("SELECT * FROM cards ORDER BY modifiedAt DESC")
    fun getAllCards(): Flow<List<Card>>
    
    /**
     * 獲取指定ID的卡片 (LiveData 版本)
     */
    @Query("SELECT * FROM cards WHERE id = :cardId")
    fun getCardById(cardId: Long): LiveData<Card>
    
    /**
     * 獲取指定ID的卡片 (同步版本)
     */
    @Query("SELECT * FROM cards WHERE id = :cardId")
    fun getCardByIdSync(cardId: Long): Card?
    
    /**
     * 搜索卡片
     */
    @Query("SELECT * FROM cards WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY modifiedAt DESC")
    fun searchCards(query: String): Flow<List<Card>>
    
    /**
     * 獲取在指定日期創建的卡片
     */
    @Query("SELECT * FROM cards WHERE createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    fun getCardsByDateRange(startDate: Date, endDate: Date): Flow<List<Card>>
    
    /**
     * 獲取指定卡片及其所有標籤
     */
    @Transaction
    @Query("SELECT * FROM cards WHERE id = :cardId")
    fun getCardWithTags(cardId: Long): Flow<CardWithTags?>
    
    /**
     * 獲取所有卡片及其標籤
     */
    @Transaction
    @Query("SELECT * FROM cards ORDER BY modifiedAt DESC")
    fun getAllCardsWithTags(): Flow<List<CardWithTags>>
    
    /**
     * 獲取指定時間段內新增的卡片數量
     */
    @Query("SELECT COUNT(*) FROM cards WHERE createdAt BETWEEN :startDate AND :endDate")
    fun getCardCountInDateRange(startDate: Date, endDate: Date): Flow<Int>
} 
