package com.example.zettelkasten.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class AppPreferenceManager private constructor(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        PreferenceManager.getDefaultSharedPreferences(context)
    
    companion object {
        @Volatile
        private var INSTANCE: AppPreferenceManager? = null
        
        // Keys
        const val DARK_MODE_KEY = "dark_mode"
        const val PASSWORD_ENABLED_KEY = "password_enabled"
        const val PASSWORD_HASH_KEY = "password_hash"
        const val BIOMETRIC_ENABLED_KEY = "biometric_enabled"
        const val APP_LOCKED_KEY = "app_locked"
        
        fun getInstance(context: Context): AppPreferenceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppPreferenceManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // Dark mode
    var isDarkModeEnabled: Boolean
        get() = sharedPreferences.getBoolean(DARK_MODE_KEY, false)
        set(value) = sharedPreferences.edit().putBoolean(DARK_MODE_KEY, value).apply()
    
    // Password settings
    var isPasswordEnabled: Boolean
        get() = sharedPreferences.getBoolean(PASSWORD_ENABLED_KEY, false)
        set(value) = sharedPreferences.edit().putBoolean(PASSWORD_ENABLED_KEY, value).apply()
    
    var passwordHash: String?
        get() = sharedPreferences.getString(PASSWORD_HASH_KEY, null)
        set(value) = sharedPreferences.edit().putString(PASSWORD_HASH_KEY, value).apply()
    
    // Biometric settings
    var isBiometricEnabled: Boolean
        get() = sharedPreferences.getBoolean(BIOMETRIC_ENABLED_KEY, false)
        set(value) = sharedPreferences.edit().putBoolean(BIOMETRIC_ENABLED_KEY, value).apply()
    
    // App lock state
    var isAppLocked: Boolean
        get() = sharedPreferences.getBoolean(APP_LOCKED_KEY, true)
        set(value) = sharedPreferences.edit().putBoolean(APP_LOCKED_KEY, value).apply()
    
    // Clear all password related settings
    fun clearPasswordSettings() {
        sharedPreferences.edit()
            .remove(PASSWORD_ENABLED_KEY)
            .remove(PASSWORD_HASH_KEY)
            .remove(BIOMETRIC_ENABLED_KEY)
            .putBoolean(APP_LOCKED_KEY, false)
            .apply()
    }
    
    // Check if any setting exists
    fun hasSettings(): Boolean {
        return sharedPreferences.all.isNotEmpty()
    }
} 
