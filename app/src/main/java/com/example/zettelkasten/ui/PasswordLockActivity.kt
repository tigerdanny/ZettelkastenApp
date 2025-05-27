package com.example.zettelkasten.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.zettelkasten.databinding.ActivityPasswordLockBinding
import com.example.zettelkasten.utils.PasswordUtils
import com.example.zettelkasten.utils.AppPreferenceManager
import java.util.concurrent.Executor

class PasswordLockActivity : FragmentActivity() {

    private lateinit var binding: ActivityPasswordLockBinding
    private lateinit var preferenceManager: AppPreferenceManager
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = AppPreferenceManager.getInstance(this)
        setupBiometric()
        setupUI()
        
        // 如果支持生物識別且已啟用，顯示生物識別選項
        if (isBiometricSupported() && preferenceManager.isBiometricEnabled) {
            binding.btnBiometric.visibility = View.VISIBLE
            binding.tvBiometricHint.visibility = View.VISIBLE
            
            // 更新提示文字
            binding.tvInstructions.text = "使用生物識別或密碼解鎖應用"
            
            // 延遲500ms自動觸發生物識別，讓用戶看到界面
            binding.btnBiometric.postDelayed({
                showBiometricPrompt()
            }, 500)
        }
    }

    private fun setupUI() {
        binding.btnUnlock.setOnClickListener {
            validatePassword()
        }

        binding.btnBiometric.setOnClickListener {
            showBiometricPrompt()
        }

        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                validatePassword()
                true
            } else {
                false
            }
        }
    }

    private fun validatePassword() {
        val inputPassword = binding.etPassword.text.toString()
        
        if (inputPassword.isEmpty()) {
            binding.etPassword.error = "請輸入密碼"
            return
        }
        
        val storedPasswordHash = preferenceManager.passwordHash

        if (storedPasswordHash != null && PasswordUtils.verifyPassword(inputPassword, storedPasswordHash)) {
            unlockApp()
        } else {
            showPasswordError()
        }
    }

    private fun showPasswordError() {
        binding.tvError.visibility = View.VISIBLE
        binding.etPassword.error = "密碼錯誤"
        binding.etPassword.selectAll()
        
        // 清除錯誤提示（延遲3秒）
        binding.tvError.postDelayed({
            binding.tvError.visibility = View.GONE
            binding.etPassword.error = null
        }, 3000)
    }

    private fun unlockApp() {
        preferenceManager.isAppLocked = false
        Toast.makeText(this, "解鎖成功", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setupBiometric() {
        executor = ContextCompat.getMainExecutor(this)
        
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                        // 用戶取消，顯示密碼輸入提示
                        binding.tvInstructions.text = "請輸入密碼解鎖應用"
                    }
                    BiometricPrompt.ERROR_LOCKOUT,
                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                        Toast.makeText(applicationContext, "生物識別已鎖定，請使用密碼", Toast.LENGTH_LONG).show()
                        binding.tvInstructions.text = "生物識別已鎖定，請輸入密碼"
                    }
                    else -> {
                        Toast.makeText(applicationContext, "生物識別錯誤: $errString", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(applicationContext, "生物識別成功", Toast.LENGTH_SHORT).show()
                unlockApp()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext, "生物識別失敗，請重試", Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("解鎖 Zettelkasten")
            .setSubtitle("使用您的指紋、面部或其他生物識別信息")
            .setDescription("快速安全地解鎖您的筆記應用")
            .setNegativeButtonText("使用密碼")
            .build()
    }

    private fun isBiometricSupported(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                // 無生物識別硬件
                false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                // 硬件不可用
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // 未註冊生物識別
                Toast.makeText(this, "請先在系統設置中註冊生物識別", Toast.LENGTH_LONG).show()
                false
            }
            else -> false
        }
    }

    private fun showBiometricPrompt() {
        if (isBiometricSupported()) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            Toast.makeText(this, "生物識別不可用，請使用密碼", Toast.LENGTH_SHORT).show()
            binding.tvInstructions.text = "請輸入密碼解鎖應用"
        }
    }

    override fun onBackPressed() {
        // 禁止返回鍵，必須解鎖才能進入應用
        // 可以選擇性地讓用戶退出應用
        moveTaskToBack(true)
    }
} 
