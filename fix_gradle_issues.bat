@echo off
echo ===========================================
echo   Zettelkasten App - Gradle问题修复脚本
echo ===========================================
echo.

echo 步骤1: 停止Gradle守护进程...
call gradlew --stop

echo.
echo 步骤2: 清理所有缓存...
call gradlew cleanCache
rmdir /s /q "%USERPROFILE%\.gradle\caches" 2>nul
rmdir /s /q "%USERPROFILE%\.gradle\wrapper" 2>nul
rmdir /s /q ".gradle" 2>nul
rmdir /s /q "build" 2>nul
rmdir /s /q "app\build" 2>nul

echo.
echo 步骤3: 重新下载Gradle wrapper...
call gradlew wrapper --gradle-version=8.12

echo.
echo 步骤4: 验证Gradle版本...
call gradlew --version

echo.
echo 步骤5: 同步项目...
call gradlew build --refresh-dependencies

echo.
echo ===========================================
echo   修复完成！现在可以在Android Studio中同步项目
echo ===========================================
echo.
pause 
