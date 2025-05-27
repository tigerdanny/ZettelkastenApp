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
import com.example.zettelkasten.data.local.entities.Tag
import com.example.zettelkasten.databinding.FragmentCardDetailBinding
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class CardDetailFragment : Fragment() {

    private var _binding: FragmentCardDetailBinding? = null
    private val binding get() = _binding!!
    
    private var cardId: Long = -1
    private var card: Card? = null
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 獲取卡片ID
        arguments?.let {
            cardId = it.getLong("card_id", -1)
            if (cardId != -1L) {
                loadCardData()
            } else {
                Toast.makeText(context, "卡片ID無效", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
        
        // 設置返回按鈕點擊事件
        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // 設置按鈕點擊事件
        setupButtons()
    }

    private fun loadCardData() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                ZettelkastenApp.database.cardDao().getCardByIdSync(cardId)?.let {
                    card = it
                    withContext(Dispatchers.Main) {
                        displayCardData(it)
                    }
                } ?: run {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "找不到卡片", Toast.LENGTH_SHORT).show()
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                }
            }
        }
    }

    private fun displayCardData(card: Card) {
        binding.cardTitle.text = card.title
        binding.cardContent.text = card.content
        
        // 顯示創建和修改日期
        val datesText = "${getString(R.string.card_created, dateFormat.format(card.createdAt))}\n" +
                "${getString(R.string.card_modified, dateFormat.format(card.modifiedAt))}"
        binding.cardDates.text = datesText
        
        // 顯示標籤
        lifecycleScope.launch {
            val tags = withContext(Dispatchers.IO) {
                ZettelkastenApp.database.tagDao().getTagsForCardSync(card.id)
            }
            
            binding.tagsChipGroup.removeAllViews()
            if (tags.isNotEmpty()) {
                binding.tagsChipGroup.visibility = View.VISIBLE
                tags.forEach { tag ->
                    val chip = Chip(requireContext())
                    chip.text = tag.name
                    chip.isClickable = true
                    chip.setOnClickListener {
                        navigateToTagsPage(tag)
                    }
                    binding.tagsChipGroup.addView(chip)
                }
            } else {
                binding.tagsChipGroup.visibility = View.GONE
            }
        }
    }

    private fun navigateToTagsPage(tag: Tag) {
        // 創建TagsFragment實例
        val tagsFragment = TagsFragment()
        
        // 創建Bundle傳遞標籤信息
        val args = Bundle()
        args.putLong("selected_tag_id", tag.id)
        args.putString("selected_tag_name", tag.name)
        tagsFragment.arguments = args
        
        // 導航到標籤頁面
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, tagsFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setupButtons() {
        // 設置編輯按鈕
        binding.editButton.setOnClickListener {
            val fragment = EditCardFragment()
            val args = Bundle()
            args.putLong("card_id", cardId)
            fragment.arguments = args
            
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit()
        }
        
        // 設置刪除按鈕
        binding.deleteButton.setOnClickListener {
            showDeleteConfirmation()
        }
    }
    
    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete)
            .setMessage(R.string.confirm_delete)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.delete) { dialog, _ ->
                deleteCard()
                dialog.dismiss()
            }
            .show()
    }
    
    private fun deleteCard() {
        card?.let {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    ZettelkastenApp.database.cardDao().deleteCardSync(it)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, R.string.card_deleted, Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
