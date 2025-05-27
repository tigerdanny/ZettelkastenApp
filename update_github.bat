@echo off
echo ===========================================
echo   Zettelkasten App - GitHub 更新脚本
echo ===========================================
echo.

echo 正在检查Git状态...
git status

echo.
echo 添加所有更改到暂存区...
git add .

echo.
echo 提交更改...
set /p commit_msg="请输入提交信息（或按Enter使用默认信息）: "
if "%commit_msg%"=="" set commit_msg=Update: 项目文件更新

git commit -m "%commit_msg%"

echo.
echo 推送到GitHub...
git push origin main

echo.
echo ===========================================
echo   更新完成！
echo ===========================================
echo.
pause 
