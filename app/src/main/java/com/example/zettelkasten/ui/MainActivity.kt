package com.example.zettelkasten.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.zettelkasten.R
import com.example.zettelkasten.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 設置窗口標誌，使狀態欄透明且內容可延伸到狀態欄
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 我們已經從布局中移除了toolbar，不需要隱藏它
        
        // 設置Navigation Drawer，不使用toolbar
        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.app_name,
            R.string.app_name
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        // 設置導航抽屜的項目點擊監聽器
        binding.navView.setNavigationItemSelectedListener(this)

        // 默認顯示首頁片段
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, HomeFragment())
                .commit()
            binding.navView.setCheckedItem(R.id.nav_home)
            // 不設置標題，讓界面更乾淨
        }

        // 設置浮動操作按鈕的點擊事件
        binding.fabAddCard.setOnClickListener {
            // 導航到創建卡片頁面
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, EditCardFragment())
                .addToBackStack(null)
                .commit()
            
            // 隱藏浮動按鈕
            binding.fabAddCard.hide()
        }
        
        // 監聽返回堆疊變化，以便在返回時恢復浮動按鈕
        supportFragmentManager.addOnBackStackChangedListener {
            updateFabVisibility()
        }
    }
    
    // 根據當前顯示的fragment決定是否顯示FAB
    private fun updateFabVisibility() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        if (currentFragment is EditCardFragment || currentFragment is CardDetailFragment) {
            binding.fabAddCard.hide()
        } else {
            binding.fabAddCard.show()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // 處理導航抽屜的項目點擊
        val fragment: Fragment = when (item.itemId) {
            R.id.nav_home -> HomeFragment()
            R.id.nav_all_cards -> AllCardsFragment()
            R.id.nav_tags -> TagsFragment()
            R.id.nav_search -> SearchFragment()
            R.id.nav_statistics -> StatisticsFragment()
            R.id.nav_settings -> SettingsFragment()
            R.id.nav_about -> AboutFragment()
            else -> HomeFragment()
        }

        // 切換到選擇的片段
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()

        // 不設置標題，保持界面乾淨
        
        // 關閉導航抽屜
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        
        // 更新浮動按鈕可見性
        updateFabVisibility()
        
        return true
    }

    override fun onBackPressed() {
        // 如果導航抽屜打開，則關閉它，否則進行正常的返回操作
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
            
            // 檢查當前顯示的Fragment，更新浮動按鈕的顯示狀態
            updateFabVisibility()
        }
    }
} 
