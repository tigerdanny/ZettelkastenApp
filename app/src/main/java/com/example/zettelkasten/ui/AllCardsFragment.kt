package com.example.zettelkasten.ui

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
import com.example.zettelkasten.databinding.FragmentAllCardsBinding
import com.example.zettelkasten.ui.adapters.CardPagerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllCardsFragment : Fragment() {

    private var _binding: FragmentAllCardsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var cardPagerAdapter: CardPagerAdapter
    private var cards: List<Card> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllCardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCardPager()
        setupClickListeners()
        
        // 設置FAB點擊事件
        binding.fabAddCard.setOnClickListener {
            // 導航到創建卡片頁面
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, EditCardFragment())
                .addToBackStack(null)
                .commit()
        }

        // 加載所有卡片
        loadAllCards()
    }
    
    override fun onResume() {
        super.onResume()
        // 每次頁面恢復時重新加載卡片
        loadAllCards()
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
                updateIndicators(position)
            }
        })
    }
    
    private fun setupClickListeners() {
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
            if (currentPosition < cards.size - 1) {
                binding.cardViewPager.currentItem = currentPosition + 1
            }
        }
        
        // 位置指示器點擊事件（可選：顯示快速跳轉選單）
        binding.positionIndicator.setOnClickListener {
            // 未來可以添加快速跳轉功能
        }
    }
    
    private fun updateIndicators(currentPosition: Int) {
        val totalCards = cards.size
        
        if (totalCards <= 1) {
            hideAllIndicators()
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
            binding.leftIndicator.alpha = 0.3f  // 半透明顯示，表示不可用
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
            binding.rightIndicator.alpha = 0.3f  // 半透明顯示，表示不可用
            binding.rightIndicator.isClickable = false
        }
    }
    
    private fun hideAllIndicators() {
        binding.leftIndicator.visibility = View.GONE
        binding.rightIndicator.visibility = View.GONE
        binding.positionIndicator.visibility = View.GONE
    }
    
    private fun loadAllCards() {
        lifecycleScope.launch {
            try {
                showLoading(true)
                
                cards = withContext(Dispatchers.IO) {
                    ZettelkastenApp.database.cardDao().getAllCards().first()
                }
                
                if (cards.isNotEmpty()) {
                    showEmptyView(false)
                    cardPagerAdapter.updateData(cards)
                    
                    // 初始顯示指示器
                    updateIndicators(binding.cardViewPager.currentItem)
                } else {
                    showEmptyView(true)
                }
                
                showLoading(false)
            } catch (e: Exception) {
                showEmptyView(true)
                showLoading(false)
            }
        }
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
            binding.cardViewPager.visibility = View.GONE
            hideAllIndicators()
        } else {
            binding.emptyView.visibility = View.GONE
            binding.cardViewPager.visibility = View.VISIBLE
        }
    }
    
    private fun showLoading(show: Boolean) {
        // 如果需要，可以在這裡添加載入指示器邏輯
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
