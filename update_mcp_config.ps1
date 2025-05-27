# 更新 Cursor MCP 配置以添加 GitHub MCP Server
$mcpConfigPath = "$env:USERPROFILE\.cursor\mcp.json"

# GitHub MCP Server 配置
$mcpConfig = @{
    mcpServers = @{
        "GitHub MCP Server" = @{
            command = "npx"
            args = @(
                "-y",
                "@smithery-ai/github"
            )
            env = @{
                GITHUB_TOKEN = "YOUR_GITHUB_TOKEN_HERE"
            }
        }
    }
}

# 转换为 JSON 并写入文件
$mcpConfig | ConvertTo-Json -Depth 10 | Set-Content -Path $mcpConfigPath -Encoding UTF8

Write-Host "GitHub MCP Server 配置已添加到 $mcpConfigPath"
Write-Host ""
Write-Host "重要提醒："
Write-Host "1. 您需要将 'YOUR_GITHUB_TOKEN_HERE' 替换为您的真实 GitHub Token"
Write-Host "2. 创建 GitHub Token 的步骤："
Write-Host "   - 访问 https://github.com/settings/tokens"
Write-Host "   - 点击 'Generate new token' -> 'Generate new token (classic)'"
Write-Host "   - 勾选需要的权限范围（例如 repo, user, issues 等）"
Write-Host "   - 复制生成的 token 并替换配置文件中的占位符"
Write-Host "3. 配置完成后重启 Cursor 使配置生效" 
