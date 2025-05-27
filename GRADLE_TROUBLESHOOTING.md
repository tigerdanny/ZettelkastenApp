# Gradle 问题排查指南

## 🚨 常见问题

### 问题1: Gradle版本冲突
```
Could not install Gradle distribution from 'https://services.gradle.org/distributions/gradle-8.2-bin.zip'
```

**原因分析：**
- 不同IDE或项目副本中可能有不同的Gradle配置
- Gradle wrapper版本与AGP版本不兼容
- 缓存文件冲突

### 问题2: 路径权限问题
```
Could not create parent directory for lock file
```

**原因分析：**
- Windows权限不足
- 文件被其他进程占用
- 路径过长或包含特殊字符

## 🛠️ 解决方案

### 方法1: 使用自动修复脚本

#### Windows批处理版本
```cmd
.\fix_gradle_issues.bat
```

#### PowerShell版本
```powershell
.\fix_gradle_issues.ps1
```

### 方法2: 手动修复步骤

#### 步骤1: 停止所有Gradle进程
```cmd
.\gradlew --stop
```

#### 步骤2: 清理缓存
```cmd
# 删除项目缓存
rmdir /s /q .gradle
rmdir /s /q build
rmdir /s /q app\build

# 删除用户缓存
rmdir /s /q "%USERPROFILE%\.gradle\caches"
rmdir /s /q "%USERPROFILE%\.gradle\wrapper"
```

#### 步骤3: 重新生成Wrapper
```cmd
.\gradlew wrapper --gradle-version=8.12
```

#### 步骤4: 验证版本
```cmd
.\gradlew --version
```

### 方法3: Android Studio设置

1. **File → Settings → Build → Gradle**
2. 选择 "Use Gradle from: 'gradle-wrapper.properties' file"
3. 设置 Gradle JVM 为 JDK 17
4. 点击 "Apply" 和 "OK"

## ⚙️ 推荐配置

### gradle-wrapper.properties
```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.12-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

### build.gradle (Project level)
```gradle
buildscript {
    ext {
        kotlin_version = '1.9.22'
        room_version = '2.6.1'
        nav_version = '2.7.6'
        agp_version = '8.2.0'  // 與Gradle 8.12兼容
    }
}
```

### gradle.properties
```properties
org.gradle.jvmargs=-Xmx4096m
kotlin.jvm.target=17
android.useAndroidX=true
org.gradle.configureondemand=false
org.gradle.daemon=true
org.gradle.parallel=true
```

## 🔧 版本兼容性表

| Gradle版本 | AGP版本 | JDK版本 | 狀態 |
|-----------|---------|---------|------|
| 8.12      | 8.2.0   | 17      | ✅ 推薦 |
| 8.11.1    | 8.1.0   | 17      | ✅ 可用 |
| 8.10      | 8.0.0   | 17      | ✅ 可用 |
| 8.2       | 8.2.0   | 17      | ⚠️ 可能有問題 |

## 🚀 預防措施

1. **統一開發環境**
   - 確保所有開發者使用相同的Gradle版本
   - 使用項目提供的wrapper而不是全局Gradle

2. **定期清理**
   - 定期運行清理腳本
   - 在切換分支後清理緩存

3. **版本管理**
   - 在更新Gradle版本前檢查兼容性
   - 保持AGP和Gradle版本同步

## 📞 獲取幫助

如果以上方法都無法解決問題：

1. 檢查 [Gradle官方文檔](https://gradle.org/releases/)
2. 查看 [Android Gradle Plugin發布說明](https://developer.android.com/studio/releases/gradle-plugin)
3. 在 [GitHub Issues](https://github.com/tigerdanny/ZettelkastenApp/issues) 中報告問題

## 📋 檢查清單

在報告問題前，請確認：

- [ ] 已停止所有Gradle守護進程
- [ ] 已清理所有緩存
- [ ] 已重新生成wrapper
- [ ] 已確認JDK版本為17
- [ ] 已檢查網絡連接
- [ ] 已確認路徑權限

---

*最後更新: 2025-01-14* 
