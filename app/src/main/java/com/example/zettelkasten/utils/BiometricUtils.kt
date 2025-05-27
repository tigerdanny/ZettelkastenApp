package com.example.zettelkasten.utils

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.zettelkasten.R
import java.util.concurrent.Executor

/**
 * 生物識別工具類
 */
object BiometricUtils {

    /**
     * 檢查設備是否支持生物識別
     */
    fun canAuthenticate(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(context, "此設備不支持生物識別", Toast.LENGTH_SHORT).show()
                false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(context, "生物識別功能暫時不可用", Toast.LENGTH_SHORT).show()
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(context, "請先在系統設置中註冊您的生物特徵", Toast.LENGTH_LONG).show()
                false
            }
            else -> false
        }
    }

    /**
     * 顯示生物識別驗證對話框
     *
     * @param activity 顯示對話框的活動
     * @param title 對話框標題
     * @param subtitle 對話框副標題
     * @param description 對話框描述
     * @param negativeButtonText 取消按鈕文字
     * @param onAuthSuccess 驗證成功回調
     * @param onAuthError 驗證失敗回調
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        description: String,
        negativeButtonText: String,
        onAuthSuccess: () -> Unit,
        onAuthError: (Int, CharSequence) -> Unit
    ) {
        // 設置執行器
        val executor: Executor = ContextCompat.getMainExecutor(activity)
        
        // 創建生物識別回調
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onAuthSuccess()
            }
            
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onAuthError(errorCode, errString)
            }
        }
        
        // 創建生物識別提示
        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        
        // 配置提示選項
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButtonText(negativeButtonText)
            .setConfirmationRequired(false)
            .build()
        
        // 顯示提示
        biometricPrompt.authenticate(promptInfo)
    }
} 