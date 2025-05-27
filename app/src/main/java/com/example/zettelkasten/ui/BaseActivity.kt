package com.example.zettelkasten.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale
import com.example.zettelkasten.utils.AppPreferenceManager

/**
 * 所有活動的基礎類，確保語言設置的一致性
 */
open class BaseActivity : AppCompatActivity() {
    
    private lateinit var preferenceManager: AppPreferenceManager

    override fun attachBaseContext(base: Context) {
        // 為每個活動應用中文設置
        val locale = Locale("zh")
        Locale.setDefault(locale)
        
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        val context = base.createConfigurationContext(config)
        
        super.attachBaseContext(context)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        preferenceManager = AppPreferenceManager.getInstance(this)
        
        // 應用保存的主題設置
        applyTheme()
        
        super.onCreate(savedInstanceState)
        
        // 檢查是否需要密碼鎖定
        checkPasswordLock()
    }
    
    override fun onResume() {
        super.onResume()
        
        // 當應用回到前台時，檢查密碼鎖定
        if (this::class.java != PasswordLockActivity::class.java) {
            checkPasswordLock()
        }
    }
    
    override fun onPause() {
        super.onPause()
        
        // 當應用進入後台時，設置鎖定狀態
        if (preferenceManager.isPasswordEnabled && this::class.java != PasswordLockActivity::class.java) {
            preferenceManager.isAppLocked = true
        }
    }
    
    private fun updateLocale() {
        val locale = Locale("zh")
        Locale.setDefault(locale)
        
        val resources = resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    private fun applyTheme() {
        val nightMode = if (preferenceManager.isDarkModeEnabled) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    private fun checkPasswordLock() {
        // 如果密碼已啟用且應用被鎖定，跳轉到密碼鎖定頁面
        if (preferenceManager.isPasswordEnabled && 
            preferenceManager.isAppLocked && 
            this::class.java != PasswordLockActivity::class.java) {
            
            val intent = Intent(this, PasswordLockActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
    
    /**
     * 手動解鎖應用（在PasswordLockActivity中調用）
     */
    fun unlockApp() {
        preferenceManager.isAppLocked = false
    }
} 
