package com.example.zettelkasten.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zettelkasten.R
import com.example.zettelkasten.ZettelkastenApp
import com.example.zettelkasten.data.local.entities.Card
import com.example.zettelkasten.databinding.FragmentHomeBinding
import com.example.zettelkasten.ui.adapters.CardAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var cardAdapter: CardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化RecyclerView
        setupRecyclerView()
        
        // 加載數據
        loadData()
    }
    
    override fun onResume() {
        super.onResume()
        // 在Fragment恢復顯示時重新加載數據
        loadData()
    }
    
    private fun setupRecyclerView() {
        binding.recentCardsRecyclerView.layoutManager = LinearLayoutManager(context)
        cardAdapter = CardAdapter(emptyList()) { card ->
            navigateToCardDetail(card)
        }
        binding.recentCardsRecyclerView.adapter = cardAdapter
    }
    
    private fun navigateToCardDetail(card: Card) {
        val detailFragment = CardDetailFragment()
        val args = Bundle()
        args.putLong("card_id", card.id)
        detailFragment.arguments = args
        
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun loadData() {
        lifecycleScope.launch {
            // 更新統計信息
            updateStatistics()
            
            // 獲取最近的卡片
            val recentCards = withContext(Dispatchers.IO) {
                try {
                    ZettelkastenApp.database.cardDao().getAllCards().first().take(5)
                } catch (e: Exception) {
                    emptyList()
                }
            }
            
            // 更新UI
            updateCardsDisplay(recentCards)
        }
    }
    
    private fun updateCardsDisplay(cards: List<Card>) {
        if (cards.isEmpty()) {
            // 顯示空狀態
            binding.recentCardsRecyclerView.visibility = View.GONE
            binding.emptyCardsMessage.visibility = View.VISIBLE
        } else {
            // 顯示卡片列表
            binding.recentCardsRecyclerView.visibility = View.VISIBLE
            binding.emptyCardsMessage.visibility = View.GONE
            cardAdapter.updateData(cards)
        }
    }

    private suspend fun updateStatistics() {
        withContext(Dispatchers.IO) {
            try {
                // 獲取卡片總數
                val cardCount = ZettelkastenApp.database.cardDao().getAllCards().first().size
                
                // 獲取標籤總數
                val tagCount = ZettelkastenApp.database.tagDao().getAllTags().first().size
                
                withContext(Dispatchers.Main) {
                    binding.totalCardsCount.text = cardCount.toString()
                    binding.totalTagsCount.text = tagCount.toString()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.totalCardsCount.text = "0"
                    binding.totalTagsCount.text = "0"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
