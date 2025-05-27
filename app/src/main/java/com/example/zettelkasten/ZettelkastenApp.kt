package com.example.zettelkasten

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.zettelkasten.data.local.ZettelkastenDatabase
import com.example.zettelkasten.utils.ThemeManager
import java.util.Locale

class ZettelkastenApp : Application() {
    
    companion object {
        const val TAG = "ZettelkastenApp"
        lateinit var instance: ZettelkastenApp
            private set
        
        // 數據庫實例
        lateinit var database: ZettelkastenDatabase
            private set
            
        // 主題管理器
        lateinit var themeManager: ThemeManager
            private set
            
        // 從版本1到版本2的遷移 (移除links表)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // 保留卡片和標籤數據，但刪除links表
                    // 檢查links表是否存在，如果存在則刪除
                    database.execSQL("DROP TABLE IF EXISTS links")
                    Log.d(TAG, "Migration 1->2: Links table removed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Migration 1->2 error: ${e.message}", e)
                }
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // 設置未捕獲異常處理
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception: ${throwable.message}", throwable)
            // 如果需要可以將崩潰信息保存到文件
        }
        
        try {
            instance = this
            
            // 強制使用中文
            setLocale(this, "zh")
            
            // 初始化本地 Room 數據庫 - 使用更安全的初始化方式
            database = Room.databaseBuilder(
                applicationContext,
                ZettelkastenDatabase::class.java,
                "zettelkasten_database"
            )
            .addMigrations(MIGRATION_1_2) // 添加遷移策略
            .fallbackToDestructiveMigration() // 如果無法遷移，允許丟失數據
            .allowMainThreadQueries() // 在開發階段允許主線程查詢，生產環境應移除此選項
            .build()
            
            // 初始化主題管理器
            themeManager = ThemeManager(applicationContext)
            
            Log.d(TAG, "Application initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing application: ${e.message}", e)
        }
    }
    
    // 設置應用語言的方法
    private fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        
        context.createConfigurationContext(configuration)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
    
    override fun attachBaseContext(base: Context) {
        try {
            // 在創建新的Context時應用中文設置
            val locale = Locale("zh")
            Locale.setDefault(locale)
            
            val config = Configuration(base.resources.configuration)
            config.setLocale(locale)
            val context = base.createConfigurationContext(config)
            
            super.attachBaseContext(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error in attachBaseContext: ${e.message}", e)
            super.attachBaseContext(base)
        }
    }
} 
