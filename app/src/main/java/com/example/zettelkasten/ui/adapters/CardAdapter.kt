package com.example.zettelkasten.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zettelkasten.R
import com.example.zettelkasten.ZettelkastenApp
import com.example.zettelkasten.data.local.entities.Card
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CardAdapter(
    private var cards: List<Card> = emptyList(),
    private val onCardClick: (Card) -> Unit
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    private val cardTagsLoadScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.card_title)
        val contentTextView: TextView = itemView.findViewById(R.id.card_content)
        val tagsTextView: TextView = itemView.findViewById(R.id.card_tags)
        val dateTextView: TextView = itemView.findViewById(R.id.card_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
        
        holder.titleTextView.text = card.title
        // 顯示部分內容，最多顯示100個字符
        holder.contentTextView.text = if (card.content.length > 100) {
            card.content.substring(0, 97) + "..."
        } else {
            card.content
        }
        
        // 顯示標籤（异步加載）
        holder.tagsTextView.text = "加載標籤..."
        // 使用tag儲存當前卡片ID，防止回收復用時數據錯亂
        val currentCardId = card.id
        holder.tagsTextView.tag = currentCardId
        
        // 非同步加載標籤
        cardTagsLoadScope.launch {
            val tags = withContext(Dispatchers.IO) {
                try {
                    ZettelkastenApp.database.tagDao().getTagsForCardSync(card.id)
                } catch (e: Exception) {
                    emptyList()
                }
            }
            
            // 確保視圖還沒有被回收復用到其他卡片
            if (holder.tagsTextView.tag == currentCardId) {
                withContext(Dispatchers.Main) {
                    if (tags.isNotEmpty()) {
                        val tagText = tags.joinToString(", ") { it.name }
                        holder.tagsTextView.text = tagText
                        holder.tagsTextView.visibility = View.VISIBLE
                    } else {
                        holder.tagsTextView.visibility = View.GONE
                    }
                }
            }
        }
        
        holder.dateTextView.text = dateFormat.format(card.modifiedAt)
        
        holder.itemView.setOnClickListener {
            onCardClick(card)
        }
    }

    override fun getItemCount(): Int = cards.size

    fun updateData(newCards: List<Card>) {
        cards = newCards
        notifyDataSetChanged()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        cardTagsLoadScope.cancel() // 取消所有協程
    }
} 
