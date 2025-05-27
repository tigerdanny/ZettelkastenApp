# Zettelkasten - 智能卡片筆記應用

<div align="center">

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![API](https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=23)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

一個基於卡片盒筆記法（Zettelkasten）理念的現代化Android筆記應用，幫助您構建個人知識網絡。

</div>

## 📋 作品展示文檔

### 🎯 完整作品報告
- 📄 **[HTML格式作品報告](APP_Portfolio_Report.html)** - 完整的專案介紹、技術架構和功能展示
- 🎤 **[PPT格式作品簡報](APP_Portfolio_Presentation.html)** - 10張幻燈片的專業簡報展示
- 🎨 **[布局設計展示](Layout_Showcase.html)** - 所有16個布局文件的詳細HTML轉換和視覺化展示

### 📖 詳細文檔
- 📋 [設置功能詳解](SETTINGS_FEATURES.md) - 深入了解所有設置選項
- 🔍 [搜索功能預覽](search_preview.html) - 搜索功能的視覺化展示
- 🏷️ [標籤雲預覽](tag_cloud_preview.html) - 標籤雲界面演示
- 📊 [統計功能預覽](statistics_preview.html) - 數據統計圖表展示
- 📝 [更新日誌](CHANGELOG.md) - 版本更新記錄和變更說明

## ✨ 功能特色

### 📝 核心筆記功能
- **原子化卡片**：創建獨立的知識卡片，每個卡片專注一個概念
- **富文本編輯**：支持格式化文本，讓筆記更加豐富
- **卡片連結**：建立卡片間的關聯，形成知識網絡
- **實時保存**：自動保存編輯內容，不丟失任何想法

### 🏷️ 智能標籤系統
- **標籤管理**：為卡片添加多個標籤，靈活分類
- **標籤雲視圖**：視覺化展示標籤使用頻率
- **快速篩選**：通過標籤快速找到相關卡片
- **批量操作**：批量編輯標籤，提高管理效率

### 🔍 強大搜索功能
- **全文搜索**：搜索標題和內容中的任何文字
- **標籤搜索**：通過標籤組合精確定位卡片
- **搜索歷史**：記錄搜索歷史，快速重複搜索
- **智能建議**：搜索時提供智能建議

### 📊 數據統計分析
- **寫作統計**：跟蹤每日/週/月的寫作量
- **卡片統計**：查看卡片數量變化趨勢
- **標籤分析**：了解標籤使用模式
- **可視化圖表**：直觀的統計圖表展示

### 🔒 安全與隱私
- **應用鎖定**：密碼保護您的私人筆記
- **生物識別**：支持指紋/面部識別快速解鎖
- **本地存儲**：所有數據存儲在本地，保護隱私
- **數據加密**：密碼採用安全加密存儲

### 🎨 個性化體驗
- **深色模式**：護眼的深色主題
- **Material Design**：現代化的界面設計
- **響應式布局**：適配不同屏幕尺寸
- **流暢動畫**：優雅的過渡動畫效果

## 🏗️ 技術架構

### 核心技術棧
- **語言**：Kotlin 100%
- **架構模式**：MVVM + Repository Pattern
- **數據庫**：Room Database (SQLite)
- **併發處理**：Kotlin Coroutines + Flow
- **UI框架**：Android Jetpack + Material Design Components

### 主要依賴
```gradle
// 核心框架
Android Jetpack (Navigation, Lifecycle, ViewModel)
Room Database 2.6.1
Kotlin Coroutines 1.7.3

// UI & UX
Material Design Components 1.11.0
ViewPager2 + Fragments
Biometric Authentication 1.2.0

// 數據可視化
MPAndroidChart 3.1.0
FlexboxLayout 3.0.0

// 開發工具
View Binding
Kotlin Kapt
Gradle 8.12
```

### 項目結構
```
app/src/main/
├── java/com/example/zettelkasten/
│   ├── data/               # 數據層
│   │   ├── local/         # Room數據庫
│   │   │   ├── entities/  # 數據實體
│   │   │   ├── dao/       # 數據訪問對象
│   │   │   └── converters/ # 類型轉換器
│   │   └── repository/    # 數據倉庫
│   ├── ui/                # UI層
│   │   ├── adapters/      # RecyclerView適配器
│   │   ├── fragments/     # Fragment組件
│   │   └── activities/    # Activity組件
│   ├── utils/             # 工具類
│   └── ZettelkastenApp.kt # 應用入口
└── res/                   # 資源文件
    ├── layout/           # 布局文件
    ├── drawable/         # 圖標資源
    ├── values/           # 字符串、顏色、樣式
    └── xml/              # 偏好設置
```

## 📱 系統需求

- **Android版本**：Android 6.0 (API 23) 或更高
- **存儲空間**：至少 50MB 可用空間
- **內存**：建議 2GB RAM 或更高
- **生物識別**：可選，支持指紋/面部識別的設備

## 🚀 快速開始

### 開發環境設置

1. **克隆項目**
   ```bash
   git clone https://github.com/your-username/zettelkasten-android.git
   cd zettelkasten-android
   ```

2. **開發工具要求**
   - Android Studio Arctic Fox 或更新版本
   - JDK 17
   - Android SDK 34
   - Gradle 8.12

3. **構建項目**
   ```bash
   ./gradlew assembleDebug
   ```

4. **運行應用**
   ```bash
   ./gradlew installDebug
   ```

### 構建配置

項目使用 Gradle 8.12 和 Android Gradle Plugin 8.2.0：

```gradle
// build.gradle (Project)
buildscript {
    ext {
        kotlin_version = '1.9.22'
        room_version = '2.6.1'
        agp_version = '8.10.0'
    }
}

// gradle.properties
org.gradle.jvmargs=-Xmx4096m
kotlin.jvm.target=17
android.useAndroidX=true
```

## 📖 使用指南

### 基本操作

1. **創建首張卡片**
   - 點擊浮動操作按鈕 (+)
   - 輸入標題和內容
   - 添加相關標籤
   - 保存卡片

2. **建立卡片連結**
   - 在卡片內容中提及其他卡片
   - 使用 [[卡片標題]] 語法創建連結
   - 系統自動識別並建立關聯

3. **使用標籤系統**
   - 為卡片添加描述性標籤
   - 通過標籤頁面管理所有標籤
   - 使用標籤快速篩選相關卡片

4. **搜索與探索**
   - 使用搜索功能查找特定內容
   - 瀏覽標籤雲發現相關主題
   - 查看統計頁面了解寫作習慣

### 高級功能

- **安全設置**：在設置中啟用密碼和生物識別保護
- **深色模式**：根據個人喜好切換主題
- **數據管理**：在設置中管理和備份您的數據

## 🤝 貢獻指南

我們歡迎任何形式的貢獻！

### 提交問題
- 使用 GitHub Issues 報告 bug
- 提出新功能建議
- 改進文檔和代碼

### 開發貢獻
1. Fork 這個項目
2. 創建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送分支 (`git push origin feature/AmazingFeature`)
5. 創建 Pull Request

### 代碼規範
- 遵循 Kotlin 編碼規範
- 添加適當的註釋和文檔
- 保持代碼簡潔和可讀性
- 編寫單元測試

## 📄 許可證

本項目基於 [MIT 許可證](LICENSE) 開源。

## 👨‍💻 開發者

**Danny Wang**

- 專注於知識管理和移動應用開發
- 致力於創建高質量的用戶體驗

## 🙏 致謝

- 感謝 [Zettelkasten 方法論](https://zettelkasten.de/) 的啟發
- 感謝 Android 開源社區的支持
- 感謝所有貢獻者的努力

## 📞 聯繫方式

- 📧 Email: [your-email@example.com]
- 🐛 Bug Report: [GitHub Issues](https://github.com/your-username/zettelkasten-android/issues)
- 💡 Feature Request: [GitHub Discussions](https://github.com/your-username/zettelkasten-android/discussions)

---

<div align="center">

**如果這個項目對您有幫助，請給我們一個 ⭐ Star！**

[⬆ 回到頂部](#zettelkasten---智能卡片筆記應用)

</div> 
