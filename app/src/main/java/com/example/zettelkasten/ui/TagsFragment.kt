package com.example.zettelkasten.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.zettelkasten.R
import com.example.zettelkasten.ZettelkastenApp
import com.example.zettelkasten.data.local.entities.Card
import com.example.zettelkasten.data.local.entities.Tag
import com.example.zettelkasten.data.model.TagStats
import com.example.zettelkasten.databinding.FragmentTagsBinding
import com.example.zettelkasten.ui.adapters.CardPagerAdapter
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TagsFragment : Fragment() {

    private var _binding: FragmentTagsBinding? = null
    private val binding get() = _binding!!
    
    private var allTagStats: List<TagStats> = emptyList()
    private var selectedTag: Tag? = null
    private var selectedCards: List<Card> = emptyList()
    private var preSelectedTagId: Long = -1L
    private var hasInitiallyLoaded: Boolean = false
    
    private lateinit var cardPagerAdapter: CardPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTagsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 檢查是否有傳入的標籤參數
        arguments?.let {
            preSelectedTagId = it.getLong("selected_tag_id", -1L)
        }

        setupCardPager()
        setupFAB()
        loadTagsData()
    }
    
    override fun onResume() {
        super.onResume()
        // 只有在初始加載完成後才重新加載數據，避免覆蓋預選標籤
        if (hasInitiallyLoaded) {
            loadTagsData()
        }
    }
    
    private fun setupCardPager() {
        cardPagerAdapter = CardPagerAdapter(emptyList()) { card ->
            navigateToCardDetail(card)
        }
        
        binding.cardViewPager.adapter = cardPagerAdapter
        binding.cardViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        
        // 設置頁面變換效果，創造堆疊感
        binding.cardViewPager.setPageTransformer { page, position ->
            val scaleFactor = 0.85f
            val alpha = 0.5f + (1 - Math.abs(position)) * 0.5f
            
            page.scaleX = scaleFactor + (1 - scaleFactor) * (1 - Math.abs(position))
            page.scaleY = scaleFactor + (1 - scaleFactor) * (1 - Math.abs(position))
            page.alpha = alpha
            
            // 添加 Z 軸效果，讓當前卡片更突出
            if (Math.abs(position) < 0.5) {
                page.translationZ = 10f
            } else {
                page.translationZ = 0f
            }
        }
        
        // 監聽頁面變化，更新指示器
        binding.cardViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateCardIndicators(position)
            }
        })
        
        setupCardClickListeners()
    }
    
    private fun setupCardClickListeners() {
        // 左箭頭點擊事件
        binding.leftIndicator.setOnClickListener {
            val currentPosition = binding.cardViewPager.currentItem
            if (currentPosition > 0) {
                binding.cardViewPager.currentItem = currentPosition - 1
            }
        }
        
        // 右箭頭點擊事件
        binding.rightIndicator.setOnClickListener {
            val currentPosition = binding.cardViewPager.currentItem
            if (currentPosition < selectedCards.size - 1) {
                binding.cardViewPager.currentItem = currentPosition + 1
            }
        }
    }
    
    private fun setupFAB() {
        binding.fabAddTag.setOnClickListener {
            // 跳轉到創建卡片頁面，讓用戶通過創建卡片來添加標籤
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, EditCardFragment())
                .addToBackStack(null)
                .commit()
        }
    }
    
    private fun loadTagsData() {
        lifecycleScope.launch {
            try {
                // 獲取所有標籤及其統計信息
                val tagStats = withContext(Dispatchers.IO) {
                    calculateTagStats()
                }
                
                allTagStats = sortTagStats(tagStats)
                
                updateUI()
                
                // 選擇標籤：優先選擇傳入的標籤，否則選擇第一個標籤
                if (allTagStats.isNotEmpty()) {
                    val targetTag = if (preSelectedTagId != -1L) {
                        // 查找指定的標籤
                        val foundTagStats = allTagStats.find { it.tag.id == preSelectedTagId }
                        foundTagStats?.tag
                    } else {
                        allTagStats.first().tag
                    }
                    
                    targetTag?.let {
                        selectTag(it)
                    }
                }
                
                // 標記初始加載已完成，並清除預選標籤ID
                if (!hasInitiallyLoaded) {
                    hasInitiallyLoaded = true
                    preSelectedTagId = -1L
                }
                
            } catch (e: Exception) {
                showEmptyView(true)
            }
        }
    }
    
    private fun sortTagStats(tagStats: List<TagStats>): List<TagStats> {
        // 如果有預選標籤，將其置頂
        return if (preSelectedTagId != -1L) {
            val selectedTagStats = tagStats.find { it.tag.id == preSelectedTagId }
            val otherTagStats = tagStats.filter { it.tag.id != preSelectedTagId }
                .sortedByDescending { it.cardCount }
            
            if (selectedTagStats != null) {
                listOf(selectedTagStats) + otherTagStats
            } else {
                tagStats.sortedByDescending { it.cardCount }
            }
        } else {
            tagStats.sortedByDescending { it.cardCount }
        }
    }
    
    private suspend fun calculateTagStats(): List<TagStats> {
        val allTags = ZettelkastenApp.database.tagDao().getAllTags().first()
        val cardDao = ZettelkastenApp.database.cardDao()
        
        return allTags.map { tag ->
            // 計算使用該標籤的卡片數量
            val cardsWithTag = cardDao.getAllCards().first().filter { card ->
                ZettelkastenApp.database.tagDao().getTagsForCardSync(card.id).any { it.id == tag.id }
            }
            TagStats(tag, cardsWithTag.size)
        }.filter { it.cardCount > 0 } // 只顯示有被使用的標籤
    }
    
    private fun selectTag(tag: Tag) {
        selectedTag = tag
        loadCardsForSelectedTag()
        updateTagCloudSelection()
    }
    
    private fun loadCardsForSelectedTag() {
        lifecycleScope.launch {
            selectedCards = withContext(Dispatchers.IO) {
                selectedTag?.let { tag ->
                    val allCards = ZettelkastenApp.database.cardDao().getAllCards().first()
                    allCards.filter { card ->
                        ZettelkastenApp.database.tagDao().getTagsForCardSync(card.id).any { it.id == tag.id }
                    }.sortedByDescending { it.modifiedAt }
                } ?: emptyList()
            }
            
            updateCardDisplay()
        }
    }
    
    private fun updateUI() {
        updateStatistics()
        updateTagCloud()
        
        if (allTagStats.isEmpty()) {
            showEmptyView(true)
        } else {
            showEmptyView(false)
        }
    }
    
    private fun updateStatistics() {
        binding.totalTagsCount.text = allTagStats.size.toString()
        val totalUsage = allTagStats.sumOf { it.cardCount }
        binding.totalUsageCount.text = totalUsage.toString()
    }
    
    private fun updateTagCloud() {
        binding.tagCloudLayout.removeAllViews()
        
        if (allTagStats.isEmpty()) {
            return
        }
        
        val minCount = allTagStats.minOfOrNull { it.cardCount } ?: 1
        val maxCount = allTagStats.maxOfOrNull { it.cardCount } ?: 1
        
        allTagStats.forEach { tagStats ->
            val chip = createTagChip(tagStats, minCount, maxCount)
            binding.tagCloudLayout.addView(chip)
        }
    }
    
    private fun updateTagCloudSelection() {
        // 更新標籤雲中的選中狀態
        for (i in 0 until binding.tagCloudLayout.childCount) {
            val chip = binding.tagCloudLayout.getChildAt(i) as? Chip
            chip?.let {
                val isSelected = chip.tag == selectedTag?.id
                if (isSelected) {
                    chip.chipStrokeWidth = 3f
                    chip.chipStrokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#6200EE"))
                } else {
                    chip.chipStrokeWidth = 0f
                }
            }
        }
    }
    
    private fun updateCardDisplay() {
        if (selectedCards.isEmpty()) {
            showCardEmptyView(true)
        } else {
            showCardEmptyView(false)
            cardPagerAdapter.updateData(selectedCards)
            updateCardIndicators(0)
        }
    }
    
    private fun updateCardIndicators(currentPosition: Int) {
        val totalCards = selectedCards.size
        
        if (totalCards <= 1) {
            hideAllCardIndicators()
            return
        }
        
        // 更新左右箭頭指示器的可見性和可點擊狀態
        updateLeftIndicator(currentPosition)
        updateRightIndicator(currentPosition, totalCards)
        
        // 更新位置指示器
        binding.currentPosition.text = (currentPosition + 1).toString()
        binding.totalCards.text = totalCards.toString()
        binding.positionIndicator.visibility = View.VISIBLE
    }
    
    private fun updateLeftIndicator(currentPosition: Int) {
        if (currentPosition > 0) {
            binding.leftIndicator.visibility = View.VISIBLE
            binding.leftIndicator.alpha = 1.0f
            binding.leftIndicator.isClickable = true
        } else {
            binding.leftIndicator.visibility = View.VISIBLE
            binding.leftIndicator.alpha = 0.3f
            binding.leftIndicator.isClickable = false
        }
    }
    
    private fun updateRightIndicator(currentPosition: Int, totalCards: Int) {
        if (currentPosition < totalCards - 1) {
            binding.rightIndicator.visibility = View.VISIBLE
            binding.rightIndicator.alpha = 1.0f
            binding.rightIndicator.isClickable = true
        } else {
            binding.rightIndicator.visibility = View.VISIBLE
            binding.rightIndicator.alpha = 0.3f
            binding.rightIndicator.isClickable = false
        }
    }
    
    private fun hideAllCardIndicators() {
        binding.leftIndicator.visibility = View.GONE
        binding.rightIndicator.visibility = View.GONE
        binding.positionIndicator.visibility = View.GONE
    }
    
    private fun createTagChip(tagStats: TagStats, minCount: Int, maxCount: Int): Chip {
        val chip = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_tag_cloud, binding.tagCloudLayout, false) as Chip
        
        // 設置文字
        chip.text = tagStats.getDisplayText()
        chip.tag = tagStats.tag.id // 存儲標籤ID用於比較
        
        // 根據使用頻率調整大小
        val sizeMultiplier = tagStats.getDisplaySize(minCount, maxCount)
        val baseTextSize = 14f
        chip.textSize = baseTextSize * sizeMultiplier
        
        // 設置顏色
        val chipColor = getTagColor(tagStats.tag)
        chip.chipBackgroundColor = android.content.res.ColorStateList.valueOf(chipColor)
        
        // 根據背景顏色調整文字顏色
        chip.setTextColor(getContrastColor(chipColor))
        
        // 設置點擊事件
        chip.setOnClickListener {
            selectTag(tagStats.tag)
        }
        
        return chip
    }
    
    private fun getTagColor(tag: Tag): Int {
        // 如果標籤有自定義顏色，使用它
        tag.colorHex?.let { colorHex ->
            try {
                return Color.parseColor(colorHex)
            } catch (e: Exception) {
                // 如果顏色格式錯誤，使用默認邏輯
            }
        }
        
        // 否則根據標籤名稱生成一致的顏色
        val colors = arrayOf(
            "#E3F2FD", "#E8F5E8", "#FFF3E0", "#FCE4EC", "#F3E5F5",
            "#E0F2F1", "#F1F8E9", "#FFF8E1", "#FFEBEE", "#E8EAF6"
        )
        
        val index = Math.abs(tag.name.hashCode()) % colors.size
        return Color.parseColor(colors[index])
    }
    
    private fun getContrastColor(backgroundColor: Int): Int {
        // 計算背景顏色的亮度
        val r = Color.red(backgroundColor)
        val g = Color.green(backgroundColor)
        val b = Color.blue(backgroundColor)
        val brightness = (r * 299 + g * 587 + b * 114) / 1000
        
        // 根據亮度選擇對比色
        return if (brightness > 128) Color.BLACK else Color.WHITE
    }
    
    private fun navigateToCardDetail(card: Card) {
        // 導航到卡片詳情頁面
        val fragment = CardDetailFragment()
        val args = Bundle()
        args.putLong("card_id", card.id)
        fragment.arguments = args
        
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }
    
    private fun showEmptyView(show: Boolean) {
        if (show) {
            binding.emptyView.visibility = View.VISIBLE
            binding.tagCloudContainer.visibility = View.GONE
            binding.cardStackContainer.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.tagCloudContainer.visibility = View.VISIBLE
            binding.cardStackContainer.visibility = View.VISIBLE
        }
    }
    
    private fun showCardEmptyView(show: Boolean) {
        if (show) {
            binding.cardViewPager.visibility = View.GONE
            hideAllCardIndicators()
        } else {
            binding.cardViewPager.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
