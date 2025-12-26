# Agent Review Git问题修复指南

## 问题描述

**错误信息**: `Failed to gather Agent Review context. Caused by: Error when executing 'git'`

## 问题分析

### 根本原因

1. **Git钩子兼容性问题**: 项目中的Git钩子（`pre-commit`, `commit-msg`）使用bash脚本，在Windows环境下可能无法正常执行
2. **Agent Review触发钩子**: Cursor的Agent Review功能在执行Git操作时可能触发了这些钩子
3. **钩子执行失败**: 当钩子执行失败时，会导致整个Git操作失败

### 诊断结果

- ✅ Git基本功能正常
- ✅ Git配置正确
- ✅ SSH连接正常
- ⚠️ Git钩子使用bash脚本（Windows兼容性问题）
- ⚠️ `git ls-files`命令在某些情况下失败

## 解决方案

### 方案1: 临时禁用Git钩子（快速修复）

**适用场景**: 需要立即使用Agent Review功能

```powershell
# 临时禁用钩子（仅当前会话）
$env:GIT_HOOKS_DISABLED = "1"

# 或者重命名钩子目录
Rename-Item .git\hooks .git\hooks.disabled

# 使用完后恢复
Rename-Item .git\hooks.disabled .git\hooks
```

### 方案2: 修复Git钩子兼容性（推荐）

**适用场景**: 长期解决方案，确保钩子在Windows下正常工作

#### 2.1 检查钩子是否可执行

```powershell
# 检查钩子文件
Get-ChildItem .git\hooks -File | Where-Object { $_.Name -notlike '*.sample' } | ForEach-Object {
    Write-Host "钩子: $($_.Name)"
    $firstLine = Get-Content $_.FullName -First 1
    Write-Host "  首行: $firstLine"
}
```

#### 2.2 将bash钩子转换为PowerShell（如果需要）

如果钩子必须执行，可以考虑：
- 使用Git Bash来执行bash脚本
- 或者将关键逻辑转换为PowerShell脚本

### 方案3: 配置Git跳过钩子（开发环境）

**适用场景**: 开发时不需要钩子检查

```powershell
# 设置Git环境变量跳过钩子
$env:SKIP_GIT_HOOKS = "1"

# 或者在Git配置中设置
git config core.hooksPath /dev/null  # Linux/Mac
# Windows下需要其他方法
```

### 方案4: 修复Agent Review配置

**适用场景**: Cursor/IDE配置问题

1. **检查Cursor Git设置**
   - 打开Cursor设置
   - 搜索"git"
   - 检查Git集成相关设置

2. **重启Cursor**
   - 完全关闭Cursor
   - 重新打开项目

3. **清除Cursor缓存**
   ```powershell
   # Cursor缓存目录（可能需要根据实际路径调整）
   Remove-Item -Recurse -Force "$env:APPDATA\Cursor\Cache\*"
   ```

## 快速修复脚本

创建了 `scripts\fix-agent-review-git.ps1` 脚本，自动执行修复：

```powershell
.\scripts\fix-agent-review-git.ps1
```

## 验证修复

修复后，验证Agent Review是否正常工作：

1. 在Cursor中打开任意文件
2. 尝试使用Agent Review功能
3. 检查是否还有Git错误

## 预防措施

### 1. Git钩子最佳实践

- ✅ 钩子脚本应该跨平台兼容
- ✅ 使用PowerShell或Python等跨平台脚本
- ✅ 添加平台检测逻辑
- ✅ 钩子失败不应该阻止只读操作（如`git log`, `git show`）

### 2. 钩子改进建议

当前钩子问题：
- `pre-commit`: 使用bash，Windows下可能失败
- `commit-msg`: 使用bash，Windows下可能失败

改进方向：
- 将关键检查逻辑改为PowerShell
- 或者添加Windows兼容层
- 确保钩子失败不影响只读操作

## 相关文件

- 诊断脚本: `scripts\diagnose-git-issue.ps1`
- Git钩子目录: `.git\hooks\`
- Git配置: `.git\config`

## 更新记录

- 2025-01-30: 创建文档，分析Agent Review Git问题
