package com.example.zettelkasten.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

/**
 * 應用主題管理器
 */
class ThemeManager(private val context: Context) {
    
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    
    companion object {
        // 主題模式常量
        const val MODE_LIGHT = "light"
        const val MODE_DARK = "dark"
        const val MODE_SYSTEM = "system"
        
        // 主題色常量
        const val THEME_BLUE = "blue"
        const val THEME_GREEN = "green"
        const val THEME_PURPLE = "purple"
        const val THEME_RED = "red"
        const val THEME_ORANGE = "orange"
        
        // 字體大小常量
        const val FONT_SIZE_SMALL = "small"
        const val FONT_SIZE_NORMAL = "normal"
        const val FONT_SIZE_LARGE = "large"
        const val FONT_SIZE_EXTRA_LARGE = "extra_large"
    }
    
    /**
     * 鍵名常量
     */
    private object PreferenceKeys {
        const val THEME_MODE = "theme_mode"
        const val THEME_COLOR = "theme_color"
        const val FONT_SIZE = "font_size"
        const val FONT_FAMILY = "font_family"
        const val USE_BIOMETRIC = "use_biometric"
        const val APP_LOCKED = "app_locked"
    }
    
    /**
     * 獲取當前主題模式
     */
    fun getThemeMode(): String {
        return prefs.getString(PreferenceKeys.THEME_MODE, MODE_SYSTEM) ?: MODE_SYSTEM
    }
    
    /**
     * 設置主題模式
     */
    fun setThemeMode(mode: String) {
        prefs.edit().putString(PreferenceKeys.THEME_MODE, mode).apply()
        
        // 根據選擇設置系統主題
        when (mode) {
            MODE_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            MODE_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
    
    /**
     * 獲取當前主題色
     */
    fun getThemeColor(): String {
        return prefs.getString(PreferenceKeys.THEME_COLOR, THEME_BLUE) ?: THEME_BLUE
    }
    
    /**
     * 設置主題色
     */
    fun setThemeColor(color: String) {
        prefs.edit().putString(PreferenceKeys.THEME_COLOR, color).apply()
    }
    
    /**
     * 獲取當前字體大小
     */
    fun getFontSize(): String {
        return prefs.getString(PreferenceKeys.FONT_SIZE, FONT_SIZE_NORMAL) ?: FONT_SIZE_NORMAL
    }
    
    /**
     * 設置字體大小
     */
    fun setFontSize(size: String) {
        prefs.edit().putString(PreferenceKeys.FONT_SIZE, size).apply()
    }
    
    /**
     * 獲取當前字體
     */
    fun getFontFamily(): String {
        return prefs.getString(PreferenceKeys.FONT_FAMILY, "sans-serif") ?: "sans-serif"
    }
    
    /**
     * 設置字體
     */
    fun setFontFamily(fontFamily: String) {
        prefs.edit().putString(PreferenceKeys.FONT_FAMILY, fontFamily).apply()
    }
    
    /**
     * 獲取生物識別設置
     */
    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(PreferenceKeys.USE_BIOMETRIC, false)
    }
    
    /**
     * 設置生物識別
     */
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PreferenceKeys.USE_BIOMETRIC, enabled).apply()
    }
    
    /**
     * 獲取應用鎖定狀態
     */
    fun isAppLocked(): Boolean {
        return prefs.getBoolean(PreferenceKeys.APP_LOCKED, false)
    }
    
    /**
     * 設置應用鎖定狀態
     */
    fun setAppLocked(locked: Boolean) {
        prefs.edit().putBoolean(PreferenceKeys.APP_LOCKED, locked).apply()
    }
    
    /**
     * 應用初始化時設置主題
     */
    fun applyTheme() {
        // 設置日/夜模式
        when (getThemeMode()) {
            MODE_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            MODE_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
} 