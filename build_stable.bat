@echo off
echo 正在使用穩定模式構建 Zettelkasten App...
echo.

REM 停止所有現有的 Gradle daemon
gradlew --stop

REM 清理項目
echo 正在清理項目...
gradlew clean --no-daemon --no-configuration-cache --no-build-cache

REM 構建 Debug APK
echo 正在構建 Debug APK...
gradlew assembleDebug --no-daemon --no-configuration-cache --no-build-cache

echo.
echo 構建完成！
echo APK 位置: app\build\outputs\apk\debug\app-debug.apk
pause 
