package com.example.zettelkasten.ui

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.zettelkasten.R
import com.example.zettelkasten.ZettelkastenApp
import com.example.zettelkasten.databinding.DialogSetPasswordBinding
import com.example.zettelkasten.utils.PasswordUtils
import com.example.zettelkasten.utils.AppPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var prefManager: AppPreferenceManager
    
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        
        prefManager = AppPreferenceManager.getInstance(requireContext())
        
        setupPreferences()
    }
    
    private fun setupPreferences() {
        setupDarkModePreference()
        setupPasswordPreference()
        setupBiometricPreference()
        setupClearDataPreference()
        setupAppVersionPreference()
    }
    
    private fun setupDarkModePreference() {
        val darkModePreference = findPreference<SwitchPreferenceCompat>("dark_mode")
        darkModePreference?.apply {
            isChecked = prefManager.isDarkModeEnabled
            
            setOnPreferenceChangeListener { _, newValue ->
                val isDarkMode = newValue as Boolean
                prefManager.isDarkModeEnabled = isDarkMode
                
                // 應用深色模式
                AppCompatDelegate.setDefaultNightMode(
                    if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES 
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
                
                true
            }
        }
    }
    
    private fun setupPasswordPreference() {
        val passwordPreference = findPreference<Preference>("set_password")
        passwordPreference?.apply {
            updatePasswordPreference()
            
            setOnPreferenceClickListener {
                if (prefManager.isPasswordEnabled) {
                    showRemovePasswordDialog()
                } else {
                    showSetPasswordDialog()
                }
                true
            }
        }
    }
    
    private fun setupBiometricPreference() {
        val biometricPreference = findPreference<SwitchPreferenceCompat>("biometric_enabled")
        biometricPreference?.apply {
            val isPasswordSet = prefManager.isPasswordEnabled
            val isBiometricAvailable = isBiometricSupported()
            
            // 設置可見性和狀態
            isVisible = isPasswordSet && isBiometricAvailable
            isChecked = prefManager.isBiometricEnabled
            
            // 更新說明文字
            summary = when {
                !isPasswordSet -> "請先設置密碼"
                !isBiometricAvailable -> getBiometricUnavailableReason()
                prefManager.isBiometricEnabled -> "已啟用 - 可使用生物識別快速解鎖"
                else -> "未啟用 - 點擊開啟生物識別快速解鎖"
            }
            
            setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                
                if (enabled && !isBiometricSupported()) {
                    // 再次檢查生物識別支持
                    Toast.makeText(
                        context,
                        getBiometricUnavailableReason(),
                        Toast.LENGTH_LONG
                    ).show()
                    false // 不允許開啟
                } else {
                    prefManager.isBiometricEnabled = enabled
                    
                    // 更新說明文字
                    summary = if (enabled) {
                        "已啟用 - 可使用生物識別快速解鎖"
                    } else {
                        "未啟用 - 點擊開啟生物識別快速解鎖"
                    }
                    
                    Toast.makeText(
                        context,
                        if (enabled) "生物識別已啟用 - 下次啟動應用時可使用" else "生物識別已禁用",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    true
                }
            }
        }
    }
    
    private fun setupClearDataPreference() {
        val clearDataPreference = findPreference<Preference>("clear_data")
        clearDataPreference?.setOnPreferenceClickListener {
            showClearDataDialog()
            true
        }
    }
    
    private fun setupAppVersionPreference() {
        val versionPreference = findPreference<Preference>("app_version")
        versionPreference?.apply {
            try {
                val packageInfo = requireContext().packageManager.getPackageInfo(
                    requireContext().packageName, 0
                )
                summary = "版本 ${packageInfo.versionName} (${packageInfo.versionCode})"
            } catch (e: PackageManager.NameNotFoundException) {
                summary = "版本信息不可用"
            }
            
            setOnPreferenceClickListener {
                showAboutDialog()
                true
            }
        }
    }
    
    private fun updatePasswordPreference() {
        val passwordPreference = findPreference<Preference>("set_password")
        val biometricPreference = findPreference<SwitchPreferenceCompat>("biometric_enabled")
        
        if (prefManager.isPasswordEnabled) {
            passwordPreference?.apply {
                title = "修改密碼"
                summary = "密碼已設置，點擊修改或移除"
            }
            biometricPreference?.isVisible = isBiometricSupported()
        } else {
            passwordPreference?.apply {
                title = "設置密碼"
                summary = "設置應用密碼以保護您的數據"
            }
            biometricPreference?.isVisible = false
        }
    }
    
    private fun showSetPasswordDialog() {
        val dialogBinding = DialogSetPasswordBinding.inflate(LayoutInflater.from(requireContext()))
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()
        
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialogBinding.btnConfirm.setOnClickListener {
            val password = dialogBinding.etPassword.text.toString()
            val confirmPassword = dialogBinding.etConfirmPassword.text.toString()
            
            when {
                password.isEmpty() -> {
                    dialogBinding.etPassword.error = "請輸入密碼"
                }
                password.length < 4 -> {
                    dialogBinding.etPassword.error = "密碼至少需要4個字符"
                }
                password != confirmPassword -> {
                    dialogBinding.etConfirmPassword.error = "密碼不匹配"
                }
                else -> {
                    // 設置密碼
                    val hashedPassword = PasswordUtils.hashPassword(password)
                    prefManager.passwordHash = hashedPassword
                    prefManager.isPasswordEnabled = true
                    
                    updatePasswordPreference()
                    setupBiometricPreference()
                    
                    Toast.makeText(requireContext(), "密碼設置成功", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
        
        dialog.show()
    }
    
    private fun showRemovePasswordDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("移除密碼")
            .setMessage("確定要移除應用密碼嗎？這將禁用所有安全功能。")
            .setPositiveButton("確定") { _, _ ->
                prefManager.clearPasswordSettings()
                updatePasswordPreference()
                setupBiometricPreference()
                Toast.makeText(requireContext(), "密碼已移除", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun showClearDataDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("清除所有數據")
            .setMessage("這將刪除所有卡片、標籤和連結。此操作無法撤銷。")
            .setPositiveButton("確定清除") { _, _ ->
                clearAllData()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun clearAllData() {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // 清除數據庫數據
                    ZettelkastenApp.database.clearAllTables()
                }
                
                Toast.makeText(requireContext(), "所有數據已清除", Toast.LENGTH_SHORT).show()
                
                // 重新啟動應用
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "清除數據失敗: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showAboutDialog() {
        val packageInfo = try {
            requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("關於 Zettelkasten")
            .setMessage("""
                版本: ${packageInfo?.versionName ?: "未知"}
                構建版本: ${packageInfo?.versionCode ?: "未知"}
                
                Zettelkasten是一個筆記管理應用，
                幫助您組織和連接您的想法。
                
                開發者: Danny Wang
            """.trimIndent())
            .setPositiveButton("確定", null)
            .show()
    }
    
    private fun isBiometricSupported(): Boolean {
        val biometricManager = BiometricManager.from(requireContext())
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }
    
    private fun getBiometricUnavailableReason(): String {
        val biometricManager = BiometricManager.from(requireContext())
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "此設備不支持生物識別"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "生物識別硬件暫時不可用"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "請先在系統設置中註冊指紋或面部識別"
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "需要系統安全更新"
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> "不支持的生物識別類型"
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> "生物識別狀態未知"
            else -> "生物識別不可用"
        }
    }
} 
