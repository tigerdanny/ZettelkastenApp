package com.example.zettelkasten.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.zettelkasten.R
import com.example.zettelkasten.ZettelkastenApp
import com.example.zettelkasten.data.local.entities.Card
import com.example.zettelkasten.data.local.entities.Tag
import com.example.zettelkasten.data.local.relations.TagWithCards
import com.example.zettelkasten.databinding.FragmentStatisticsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// 簡單的標籤統計數據類
data class TagStats(
    val tag: Tag,
    val usageCount: Int
)

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private var allCards: List<Card> = emptyList()
    private var allTagStats: List<TagStats> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        loadStatisticsData()
    }

    private fun setupClickListeners() {
        // 查看全部標籤按鈕點擊事件
        binding.viewAllTagsBtn.setOnClickListener {
            navigateToTagsPage()
        }
    }

    private fun navigateToTagsPage() {
        val fragment = TagsFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun loadStatisticsData() {
        lifecycleScope.launch {
            try {
                val (cards, tagStats) = withContext(Dispatchers.IO) {
                    val cardsData = ZettelkastenApp.database.cardDao().getAllCards().first()
                    val tagsWithCards = ZettelkastenApp.database.tagDao().getAllTagsWithCards().first()
                    
                    // 計算每個標籤的使用次數
                    val tagStatsData = tagsWithCards.map { tagWithCards ->
                        TagStats(
                            tag = tagWithCards.tag,
                            usageCount = tagWithCards.cards.size
                        )
                    }.filter { it.usageCount > 0 } // 只包含有使用的標籤
                    
                    Pair(cardsData, tagStatsData)
                }
                
                allCards = cards
                allTagStats = tagStats
                
                updateStatisticsDisplay()
                
            } catch (e: Exception) {
                showEmptyState()
            }
        }
    }

    private fun updateStatisticsDisplay() {
        if (allCards.isEmpty()) {
            showEmptyState()
            return
        }

        hideEmptyState()
        
        // 更新快速統計
        updateQuickStats()
        
        // 更新創建活動統計
        updateCreationActivityStats()
        
        // 更新熱門標籤
        updatePopularTags()
        
        // 更新內容統計
        updateContentStats()
    }

    private fun updateQuickStats() {
        binding.totalCardsCount.text = allCards.size.toString()
        binding.totalTagsCount.text = allTagStats.size.toString()
        
        val totalTagUsage = allTagStats.sumOf { it.usageCount }
        binding.totalTagUsageCount.text = totalTagUsage.toString()
    }

    private fun updateCreationActivityStats() {
        val calendar = Calendar.getInstance()
        val todayStart = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        val weekStart = calendar.apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }.time
        
        val todayCards = allCards.count { it.createdAt >= todayStart }
        val weekCards = allCards.count { it.createdAt >= weekStart }
        
        binding.todayCardsCount.text = todayCards.toString()
        binding.weekCardsCount.text = weekCards.toString()
    }

    private fun updatePopularTags() {
        val popularTagsList = binding.popularTagsList
        popularTagsList.removeAllViews()
        
        val topTags = allTagStats.sortedByDescending { it.usageCount }.take(5)
        
        if (topTags.isEmpty()) {
            val noTagsView = createNoTagsView()
            popularTagsList.addView(noTagsView)
            return
        }
        
        topTags.forEachIndexed { index, tagStats ->
            val tagView = createPopularTagView(index + 1, tagStats)
            popularTagsList.addView(tagView)
        }
    }

    private fun createPopularTagView(rank: Int, tagStats: TagStats): View {
        val view = layoutInflater.inflate(R.layout.item_popular_tag, null)
        
        val rankText = view.findViewById<TextView>(R.id.tag_rank)
        val tagName = view.findViewById<TextView>(R.id.tag_name)
        val usageCount = view.findViewById<TextView>(R.id.usage_count)
        
        rankText.text = "#$rank"
        tagName.text = tagStats.tag.name
        usageCount.text = "${tagStats.usageCount} 次"
        
        // 設置排名顏色
        val rankColor = when (rank) {
            1 -> requireContext().getColor(android.R.color.holo_orange_dark)
            2 -> requireContext().getColor(android.R.color.darker_gray)
            3 -> requireContext().getColor(android.R.color.holo_orange_light)
            else -> requireContext().getColor(R.color.textColorSecondary)
        }
        rankText.setTextColor(rankColor)
        
        return view
    }

    private fun createNoTagsView(): View {
        val textView = TextView(requireContext())
        textView.text = "暫無標籤數據"
        textView.setTextColor(requireContext().getColor(R.color.textColorSecondary))
        textView.textSize = 14f
        textView.setPadding(16, 16, 16, 16)
        return textView
    }

    private fun updateContentStats() {
        if (allCards.isEmpty()) {
            binding.averageContentLength.text = "0"
            binding.longestCardLength.text = "0"
            return
        }
        
        val contentLengths = allCards.map { it.content.length }
        val averageLength = contentLengths.average().toInt()
        val longestLength = contentLengths.maxOrNull() ?: 0
        
        binding.averageContentLength.text = averageLength.toString()
        binding.longestCardLength.text = longestLength.toString()
    }

    private fun showEmptyState() {
        binding.emptyStatsView.visibility = View.VISIBLE
        binding.quickStatsCard.visibility = View.GONE
        binding.creationActivityCard.visibility = View.GONE
        binding.popularTagsCard.visibility = View.GONE
        binding.contentStatsCard.visibility = View.GONE
    }

    private fun hideEmptyState() {
        binding.emptyStatsView.visibility = View.GONE
        binding.quickStatsCard.visibility = View.VISIBLE
        binding.creationActivityCard.visibility = View.VISIBLE
        binding.popularTagsCard.visibility = View.VISIBLE
        binding.contentStatsCard.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        // 每次返回頁面時重新加載數據
        loadStatisticsData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
