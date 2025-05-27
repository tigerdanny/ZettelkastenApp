package com.example.zettelkasten.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.zettelkasten.R
import com.example.zettelkasten.ZettelkastenApp
import com.example.zettelkasten.data.local.entities.Card
import com.example.zettelkasten.data.local.entities.CardTagCrossRef
import com.example.zettelkasten.data.local.entities.Tag
import com.example.zettelkasten.databinding.FragmentEditCardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class EditCardFragment : Fragment() {

    private var _binding: FragmentEditCardBinding? = null
    private val binding get() = _binding!!
    
    private var cardId: Long = -1 // -1 表示新建卡片
    private var card: Card? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 獲取卡片ID（如果是編輯模式）
        arguments?.let {
            cardId = it.getLong("card_id", -1)
            if (cardId != -1L) {
                // 加載卡片數據
                loadCardData()
            }
        }

        // 設置按鈕點擊事件
        setupButtons()
    }

    private fun loadCardData() {
        if (cardId <= 0) return
        
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                ZettelkastenApp.database.cardDao().getCardByIdSync(cardId)?.let {
                    card = it
                    withContext(Dispatchers.Main) {
                        binding.titleEditText.setText(it.title)
                        binding.contentEditText.setText(it.content)
                        
                        // 加載卡片標籤
                        val tags = ZettelkastenApp.database.tagDao().getTagsForCardSync(it.id)
                        if (tags.isNotEmpty()) {
                            val tagNames = tags.joinToString(", ") { tag -> tag.name }
                            binding.tagsEditText.setText(tagNames)
                        }
                    }
                }
            }
        }
    }

    private fun setupButtons() {
        binding.saveButton.setOnClickListener {
            saveCard()
        }

        binding.cancelButton.setOnClickListener {
            // 返回上一頁
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun saveCard() {
        val title = binding.titleEditText.text.toString().trim()
        val content = binding.contentEditText.text.toString().trim()
        val tags = binding.tagsEditText.text.toString().trim()
        
        // 驗證輸入
        if (title.isEmpty()) {
            Toast.makeText(context, "請輸入標題", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 保存卡片到本地數據庫
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val cardToSave = if (card != null) {
                        // 更新現有卡片
                        card!!.title = title
                        card!!.content = content
                        card!!.modifiedAt = Date()
                        ZettelkastenApp.database.cardDao().updateCardSync(card!!)
                        card!!
                    } else {
                        // 創建新卡片
                        val newCard = Card(
                            title = title,
                            content = content,
                            createdAt = Date(),
                            modifiedAt = Date()
                        )
                        val newId = ZettelkastenApp.database.cardDao().insertCardSync(newCard)
                        newCard.copy(id = newId)
                    }
                    
                    // 處理標籤
                    if (tags.isNotEmpty()) {
                        // 解析標籤
                        val tagsList = tags.split(",", ";", "，", "；", " ")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                        
                        // 清除舊有標籤關聯（如果是更新卡片）
                        val existingTags = ZettelkastenApp.database.tagDao().getTagsForCardSync(cardToSave.id)
                        existingTags.forEach { tag ->
                            ZettelkastenApp.database.tagDao().removeTagFromCardSync(
                                CardTagCrossRef(cardToSave.id, tag.id)
                            )
                        }
                        
                        // 添加新標籤
                        tagsList.forEach { tagName ->
                            // 檢查標籤是否已存在
                            var tag = ZettelkastenApp.database.tagDao().getTagByNameSync(tagName)
                            
                            // 如果標籤不存在，創建新標籤
                            if (tag == null) {
                                val newTag = Tag(name = tagName)
                                val tagId = ZettelkastenApp.database.tagDao().insertTagSync(newTag)
                                tag = newTag.copy(id = tagId)
                            }
                            
                            // 添加卡片與標籤關聯
                            ZettelkastenApp.database.tagDao().addTagToCardSync(
                                CardTagCrossRef(cardToSave.id, tag.id)
                            )
                        }
                    }
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, R.string.card_saved, Toast.LENGTH_SHORT).show()
                        
                        // 在保存完成後導航到卡片詳情頁面，而不是簡單地返回
                        val detailFragment = CardDetailFragment()
                        val args = Bundle()
                        args.putLong("card_id", cardToSave.id)
                        detailFragment.arguments = args
                        
                        // 替換當前的fragment
                        requireActivity().supportFragmentManager.popBackStack()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.nav_host_fragment, detailFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "保存失敗: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
