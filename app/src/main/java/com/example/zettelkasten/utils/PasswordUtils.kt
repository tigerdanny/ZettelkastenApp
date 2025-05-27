package com.example.zettelkasten.utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

object PasswordUtils {
    
    private const val SALT_LENGTH = 16
    private const val HASH_ALGORITHM = "SHA-256"
    
    /**
     * 生成鹽值
     */
    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return salt
    }
    
    /**
     * 使用鹽值對密碼進行哈希
     */
    private fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        digest.update(salt)
        return digest.digest(password.toByteArray())
    }
    
    /**
     * 加密密碼，返回包含鹽值和哈希值的字符串
     */
    fun hashPassword(password: String): String {
        val salt = generateSalt()
        val hash = hashPassword(password, salt)
        
        // 將鹽值和哈希值組合，使用Base64編碼
        val combined = salt + hash
        return Base64.getEncoder().encodeToString(combined)
    }
    
    /**
     * 驗證密碼
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        return try {
            val combined = Base64.getDecoder().decode(storedHash)
            
            // 提取鹽值和哈希值
            val salt = combined.sliceArray(0 until SALT_LENGTH)
            val hash = combined.sliceArray(SALT_LENGTH until combined.size)
            
            // 使用相同的鹽值對輸入密碼進行哈希
            val inputHash = hashPassword(password, salt)
            
            // 比較哈希值
            hash.contentEquals(inputHash)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 檢查密碼強度
     */
    fun isPasswordStrong(password: String): Boolean {
        if (password.length < 4) return false
        return true
    }
    
    /**
     * 獲取密碼強度提示
     */
    fun getPasswordStrengthHint(password: String): String {
        return when {
            password.isEmpty() -> "請輸入密碼"
            password.length < 4 -> "密碼至少需要4個字符"
            else -> "密碼強度良好"
        }
    }
} 
