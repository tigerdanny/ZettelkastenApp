@echo off
echo 正在清除所有 Gradle 和構建緩存...

:: 停止 Gradle 守護程序
call gradlew.bat --stop

:: 刪除專案緩存
rmdir /S /Q .gradle
rmdir /S /Q build
rmdir /S /Q app\build
rmdir /S /Q buildSrc\.gradle
rmdir /S /Q buildSrc\build

:: 刪除 Gradle 緩存
rmdir /S /Q %USERPROFILE%\.gradle\caches
rmdir /S /Q %USERPROFILE%\.gradle\daemon
rmdir /S /Q %USERPROFILE%\.gradle\wrapper
rmdir /S /Q %USERPROFILE%\.gradle\native
rmdir /S /Q %USERPROFILE%\.gradle\notifications
rmdir /S /Q %USERPROFILE%\.gradle\configuration-cache

:: 刪除 Android 構建緩存
rmdir /S /Q %USERPROFILE%\.android\build-cache
rmdir /S /Q %USERPROFILE%\.android\cache

:: 刪除 IDE 緩存
rmdir /S /Q .idea\caches
rmdir /S /Q .idea\libraries

echo 緩存清除完成。
echo 請重新啟動 Android Studio。
pause 
