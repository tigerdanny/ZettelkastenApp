@echo off
echo 正在執行無緩存構建...

:: 停止 Gradle 守護程序
call gradlew.bat --stop

:: 清理構建
call gradlew.bat clean --no-daemon --no-configuration-cache --no-build-cache

:: 執行構建
call gradlew.bat assembleDebug --no-daemon --no-configuration-cache --no-build-cache --rerun-tasks

echo 構建完成。
pause 
