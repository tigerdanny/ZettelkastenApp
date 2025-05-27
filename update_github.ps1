# Zettelkasten App - GitHub 更新脚本 (PowerShell版本)

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "  Zettelkasten App - GitHub 更新脚本" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "正在检查Git状态..." -ForegroundColor Yellow
git status

Write-Host ""
Write-Host "添加所有更改到暂存区..." -ForegroundColor Yellow
git add .

Write-Host ""
$commitMsg = Read-Host "请输入提交信息（或按Enter使用默认信息）"
if ([string]::IsNullOrEmpty($commitMsg)) {
    $commitMsg = "Update: 项目文件更新"
}

Write-Host "提交更改..." -ForegroundColor Yellow
git commit -m $commitMsg

Write-Host ""
Write-Host "推送到GitHub..." -ForegroundColor Yellow
git push origin main

Write-Host ""
Write-Host "===========================================" -ForegroundColor Green
Write-Host "  更新完成！" -ForegroundColor Green
Write-Host "===========================================" -ForegroundColor Green
Write-Host ""

Read-Host "按任意键继续..." 
