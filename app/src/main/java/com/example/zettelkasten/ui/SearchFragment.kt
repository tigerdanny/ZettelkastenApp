package com.example.zettelkasten.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.zettelkasten.R
import com.example.zettelkasten.ZettelkastenApp
import com.example.zettelkasten.data.local.entities.Card
import com.example.zettelkasten.databinding.FragmentSearchBinding
import com.example.zettelkasten.ui.adapters.CardPagerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private var searchResults: List<Card> = emptyList()
    private var currentSearchQuery: String = ""
    private var currentFilter: String = "all"
    private lateinit var cardPagerAdapter: CardPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCardPager()
        setupSearch()
        setupFilters()
        setupCardNavigation()
        setupKeyboardHandling()
        
        // 顯示初始狀態
        showInitialState()
    }

    private fun setupCardPager() {
        cardPagerAdapter = CardPagerAdapter(emptyList()) { card ->
            navigateToCardDetail(card)
        }
        
        binding.searchResultsPager.adapter = cardPagerAdapter
        binding.searchResultsPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        
        // 設置頁面變換效果，創造堆疊感
        binding.searchResultsPager.setPageTransformer { page, position ->
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
        binding.searchResultsPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateCardIndicators(position)
            }
        })
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { 
                    currentSearchQuery = it
                    performSearch(it, currentFilter)
                    // 隱藏鍵盤
                    hideKeyboard()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText ?: ""
                if (currentSearchQuery.isBlank()) {
                    showInitialState()
                } else if (currentSearchQuery.length >= 2) {
                    // 當輸入至少2個字符時才開始搜索
                    performSearch(currentSearchQuery, currentFilter)
                }
                return true
            }
        })

        // 設置SearchView的焦點監聽器
        binding.searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // 當獲得焦點時，滾動到頂部
                binding.scrollContainer.smoothScrollTo(0, 0)
            }
        }
    }

    private fun setupFilters() {
        binding.filterChipGroup.setOnCheckedChangeListener { group, checkedId ->
            currentFilter = when (checkedId) {
                binding.filterTitle.id -> "title"
                binding.filterContent.id -> "content"
                binding.filterTags.id -> "tags"
                else -> "all"
            }
            
            // 如果有搜索查詢，重新執行搜索
            if (currentSearchQuery.isNotBlank()) {
                performSearch(currentSearchQuery, currentFilter)
            }
        }
    }

    private fun setupCardNavigation() {
        // 左箭頭點擊事件
        binding.leftIndicator.setOnClickListener {
            val currentPosition = binding.searchResultsPager.currentItem
            if (currentPosition > 0) {
                binding.searchResultsPager.currentItem = currentPosition - 1
            }
        }
        
        // 右箭頭點擊事件
        binding.rightIndicator.setOnClickListener {
            val currentPosition = binding.searchResultsPager.currentItem
            if (currentPosition < searchResults.size - 1) {
                binding.searchResultsPager.currentItem = currentPosition + 1
            }
        }
    }

    private fun setupKeyboardHandling() {
        // 點擊外部區域時隱藏鍵盤
        binding.root.setOnClickListener {
            hideKeyboard()
            binding.searchView.clearFocus()
        }

        // 點擊結果區域時也隱藏鍵盤
        binding.searchResultsContainer.setOnClickListener {
            hideKeyboard()
            binding.searchView.clearFocus()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun performSearch(query: String, filter: String) {
        lifecycleScope.launch {
            try {
                val results = withContext(Dispatchers.IO) {
                    searchCards(query, filter)
                }
                
                searchResults = results
                updateSearchResults()
                
            } catch (e: Exception) {
                // 處理錯誤
                showNoResultsState("搜尋時發生錯誤，請稍後再試")
            }
        }
    }

    private suspend fun searchCards(query: String, filter: String): List<Card> {
        val allCards = ZettelkastenApp.database.cardDao().getAllCards().first()
        val lowercaseQuery = query.lowercase()
        
        return allCards.filter { card ->
            when (filter) {
                "title" -> card.title.lowercase().contains(lowercaseQuery)
                "content" -> card.content.lowercase().contains(lowercaseQuery)
                "tags" -> {
                    val cardTags = ZettelkastenApp.database.tagDao().getTagsForCardSync(card.id)
                    cardTags.any { tag -> tag.name.lowercase().contains(lowercaseQuery) }
                }
                else -> { // "all"
                    card.title.lowercase().contains(lowercaseQuery) ||
                    card.content.lowercase().contains(lowercaseQuery) ||
                    run {
                        val cardTags = ZettelkastenApp.database.tagDao().getTagsForCardSync(card.id)
                        cardTags.any { tag -> tag.name.lowercase().contains(lowercaseQuery) }
                    }
                }
            }
        }.sortedByDescending { it.modifiedAt } // 按修改時間排序
    }

    private fun updateSearchResults() {
        if (searchResults.isEmpty()) {
            showNoResultsState()
        } else {
            showSearchResults()
        }
    }

    private fun showInitialState() {
        binding.initialState.visibility = View.VISIBLE
        binding.noResultsState.visibility = View.GONE
        binding.searchResultsContainer.visibility = View.GONE
        binding.searchStatsLayout.visibility = View.GONE
    }

    private fun showNoResultsState(customMessage: String? = null) {
        binding.initialState.visibility = View.GONE
        binding.noResultsState.visibility = View.VISIBLE
        binding.searchResultsContainer.visibility = View.GONE
        binding.searchStatsLayout.visibility = View.GONE
        
        customMessage?.let {
            binding.noResultsSubtitle.text = it
        }
    }

    private fun showSearchResults() {
        binding.initialState.visibility = View.GONE
        binding.noResultsState.visibility = View.GONE
        binding.searchResultsContainer.visibility = View.VISIBLE
        binding.searchStatsLayout.visibility = View.VISIBLE
        
        // 更新統計信息
        binding.searchResultsCount.text = searchResults.size.toString()
        
        // 更新卡片顯示
        cardPagerAdapter.updateData(searchResults)
        updateCardIndicators(0)
    }

    private fun updateCardIndicators(currentPosition: Int) {
        val totalCards = searchResults.size
        
        if (totalCards <= 1) {
            hideAllCardIndicators()
            return
        }
        
        // 更新左右箭頭指示器的可見性和可點擊狀態
        updateLeftIndicator(currentPosition)
        updateRightIndicator(currentPosition, totalCards)
        
        // 更新位置指示器
        binding.currentPosition.text = (currentPosition + 1).toString()
        binding.totalResults.text = totalCards.toString()
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

    private fun navigateToCardDetail(card: Card) {
        // 隱藏鍵盤
        hideKeyboard()
        
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
