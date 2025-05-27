# GitHub MCP Server 配置完成 ✅

## 已完成的工作

✅ **GitHub MCP Server** 已成功添加到您的 Cursor 配置中！

配置文件位置：`C:\Users\DannyWang.DESKTOP-KA0T737\.cursor\mcp.json`

## 下一步操作

### 1. 获取 GitHub Personal Access Token

您需要创建一个 GitHub Personal Access Token 来替换配置中的 `YOUR_GITHUB_TOKEN_HERE`：

1. 访问 [GitHub Token 设置页面](https://github.com/settings/tokens)
2. 点击 **"Generate new token"** → **"Generate new token (classic)"**
3. 设置 Token 信息：
   - **Token name**: `Cursor MCP Server`
   - **Expiration**: 选择适当的过期时间（建议90天或更长）
   - **Select scopes**: 勾选以下权限：
     - ✅ `repo` (完整仓库访问权限)
     - ✅ `user` (用户信息读取权限)
     - ✅ `read:org` (组织信息读取权限)
     - ✅ `gist` (Gist 管理权限)
     - ✅ `workflow` (GitHub Actions 工作流权限)

4. 点击 **"Generate token"**
5. **重要**：立即复制生成的 token（只会显示一次）

### 2. 更新配置文件

将复制的 GitHub Token 替换到配置文件中：

```powershell
# 使用您喜欢的编辑器打开配置文件
notepad "$env:USERPROFILE\.cursor\mcp.json"
```

或者使用 PowerShell 快速替换：

```powershell
$configPath = "$env:USERPROFILE\.cursor\mcp.json"
$content = Get-Content $configPath -Raw
$content = $content -replace "YOUR_GITHUB_TOKEN_HERE", "your_actual_github_token_here"
Set-Content $configPath $content
```

### 3. 重启 Cursor

配置完成后，需要重启 Cursor 使配置生效。

## 使用方法

### 在 Cursor 中使用 GitHub MCP Server

1. **打开 Cursor 的 Agent 模式**（在聊天框左下角选择 "Agent"）

2. **可用的 GitHub 功能**：
   - 创建仓库
   - 搜索仓库
   - 读取文件内容
   - 创建或更新文件
   - 管理 Issues
   - 查看 Pull Requests
   - 分析代码

3. **示例使用命令**：

```text
# 创建新仓库
在 GitHub 上创建一个名为 "my-new-project" 的私有仓库

# 搜索仓库
搜索关于 "android kotlin" 的热门仓库

# 读取文件
读取我的仓库 "ZettelkastenApp" 中的 README.md 文件

# 创建文件
在我的仓库中创建一个新的 CHANGELOG.md 文件

# 查看 Issues
显示我的仓库中的所有开放 Issues
```

## 配置验证

您可以在 Cursor 中验证 MCP Server 是否正常工作：

1. 打开 Cursor Settings
2. 进入 "Features" → "MCP"
3. 查看 "GitHub MCP Server" 是否显示为已连接
4. 查看可用工具列表中是否包含 GitHub 相关工具

## 常见问题

### Q: 配置后 GitHub MCP Server 无法工作？
A: 
- 确保 GitHub Token 有正确的权限
- 检查网络连接
- 重启 Cursor
- 查看 Cursor 的错误日志

### Q: 某些 GitHub 操作失败？
A: 
- 检查 Token 权限是否足够
- 确保您对目标仓库有相应的访问权限
- 检查仓库是否存在

### Q: 如何更新 GitHub Token？
A: 
- 在 GitHub 上生成新的 Token
- 更新 `mcp.json` 配置文件
- 重启 Cursor

## 安全提醒

⚠️ **重要安全提醒**：
- GitHub Token 具有重要权限，请妥善保管
- 不要将包含 Token 的配置文件分享给他人
- 定期更新 Token
- 如果 Token 泄露，立即在 GitHub 上撤销并重新生成

## 进阶使用

您现在可以结合您的 Android 项目使用 GitHub MCP Server：
- 自动化版本管理
- 自动生成 Release Notes
- 管理项目 Issues 和 Milestones
- 代码审查自动化
- 与团队协作

祝您使用愉快！🚀 
