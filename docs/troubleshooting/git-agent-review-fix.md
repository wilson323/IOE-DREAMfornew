# Git Agent Review 错误修复指南

## 问题描述
`Failed to gather Agent Review context. Caused by: Error when executing 'git':`

## 诊断结果
✅ Git 已正确安装（版本 2.52.0.windows.1）
✅ Git 仓库状态正常
✅ 远程仓库配置正确
✅ 基本 Git 命令执行正常

## 可能原因
1. Cursor Agent Review 功能尝试执行特定 Git 命令时失败
2. Git 命令执行超时
3. Cursor 内部错误
4. 权限问题

## 解决方案

### 方案 1: 重启 Cursor IDE
1. 完全关闭 Cursor
2. 重新打开 Cursor
3. 重新尝试 Agent Review 功能

### 方案 2: 检查 Git 路径配置
在 Cursor 设置中检查 Git 路径是否正确：
- 设置 → Git → Git Path
- 确保指向正确的 Git 可执行文件（通常在 `C:\Program Files\Git\cmd\git.exe`）

### 方案 3: 清理 Git 缓存
```powershell
cd D:\IOE-DREAM
git gc --prune=now
```

### 方案 4: 检查 Git 钩子
虽然 hooks 目录不存在，但可以创建：
```powershell
New-Item -ItemType Directory -Path "D:\IOE-DREAM\.git\hooks" -Force
```

### 方案 5: 更新 Git 到最新版本
当前版本：2.52.0.windows.1
建议更新到最新版本：https://git-scm.com/download/win

### 方案 6: 检查 Cursor 日志
1. 打开 Cursor
2. 查看输出面板（View → Output）
3. 选择 "Git" 或 "Agent Review" 通道
4. 查看具体错误信息

### 方案 7: 临时禁用 Agent Review
如果问题持续，可以临时禁用该功能：
- 设置 → Features → Agent Review → 禁用

## 验证步骤
执行以下命令验证 Git 正常工作：
```powershell
cd D:\IOE-DREAM
git status
git log --oneline -5
git diff --stat
```

## 当前仓库状态
- 分支：main
- 未提交更改：1 个文件
  - `BackendVerificationStrategy.java`
- 领先远程：49 个提交

## 建议操作
1. 先提交当前更改
2. 然后重试 Agent Review 功能
3. 如果问题持续，查看 Cursor 日志获取详细错误信息
