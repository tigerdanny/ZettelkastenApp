package com.example.zettelkasten.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zettelkasten.R
import com.example.zettelkasten.ZettelkastenApp
import com.example.zettelkasten.data.local.entities.Card
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class CardPagerAdapter(
    private var cards: List<Card> = emptyList(),
    private val onCardClicked: (Card) -> Unit
) : RecyclerView.Adapter<CardPagerAdapter.CardViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    private val cardTagsLoadScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.card_title)
        val contentTextView: TextView = view.findViewById(R.id.card_content)
        val tagsTextView: TextView = view.findViewById(R.id.card_tags)
        val dateTextView: TextView = view.findViewById(R.id.card_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_stack, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
        
        holder.titleTextView.text = card.title
        holder.contentTextView.text = card.content
        holder.dateTextView.text = dateFormat.format(card.modifiedAt)
        
        // 加載標籤
        val currentCardId = card.id
        holder.tagsTextView.tag = currentCardId
        holder.tagsTextView.text = "標籤載入中..."
        
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
                if (tags.isNotEmpty()) {
                    val tagText = tags.joinToString(", ") { it.name }
                    holder.tagsTextView.text = tagText
                    holder.tagsTextView.visibility = View.VISIBLE
                } else {
                    holder.tagsTextView.visibility = View.GONE
                }
            }
        }
        
        // 設置卡片點擊事件
        holder.itemView.setOnClickListener {
            onCardClicked(card)
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
