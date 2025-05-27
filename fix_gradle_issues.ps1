# Zettelkasten App - Gradle问题修复脚本 (PowerShell版本)

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "  Zettelkasten App - Gradle问题修复脚本" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "步骤1: 停止Gradle守护进程..." -ForegroundColor Yellow
& .\gradlew --stop

Write-Host ""
Write-Host "步骤2: 清理所有缓存..." -ForegroundColor Yellow
& .\gradlew cleanCache
$gradleUserHome = "$env:USERPROFILE\.gradle"
$paths = @(
    "$gradleUserHome\caches",
    "$gradleUserHome\wrapper",
    ".gradle",
    "build",
    "app\build"
)

foreach ($path in $paths) {
    if (Test-Path $path) {
        Write-Host "删除: $path" -ForegroundColor Gray
        Remove-Item -Path $path -Recurse -Force -ErrorAction SilentlyContinue
    }
}

Write-Host ""
Write-Host "步骤3: 重新下载Gradle wrapper..." -ForegroundColor Yellow
& .\gradlew wrapper --gradle-version=8.12

Write-Host ""
Write-Host "步骤4: 验证Gradle版本..." -ForegroundColor Yellow
& .\gradlew --version

Write-Host ""
Write-Host "步骤5: 同步项目..." -ForegroundColor Yellow
& .\gradlew build --refresh-dependencies

Write-Host ""
Write-Host "===========================================" -ForegroundColor Green
Write-Host "  修复完成！现在可以在Android Studio中同步项目" -ForegroundColor Green
Write-Host "===========================================" -ForegroundColor Green
Write-Host ""

Read-Host "按任意键继续..." 
