# Git 提交消息生成失败问题修复

## 问题描述

在尝试提交大量文件（500+ 文件）时，VS Code 的 Git 扩展生成提交消息失败：

```
Failed to generate commit message: [invalid_argument] Error
```

## 问题原因

1. **文件数量过多**: 当暂存的文件数量超过 500 个时，VS Code 的 AI 提交消息生成功能可能会因为输入过长而失败
2. **提交消息模板问题**: Git commit template 可能包含特殊字符或格式问题
3. **Git 配置问题**: 某些 Git 配置可能导致提交消息生成失败

## 解决方案

### 方案 1: 手动编写提交消息（推荐）

对于大量文件的提交，建议手动编写提交消息：

```powershell
# 查看暂存的文件统计
git diff --cached --stat

# 手动提交
git commit -m "feat: 添加 microservices-common 模块完整实现

- 添加监控管理模块 (LogManagementManager, MetricsCollectorManager 等)
- 添加通知管理模块 (NotificationConfig, NotificationTemplate 等)
- 添加组织架构模块 (Area, Device, Employee 等)
- 添加 RBAC 权限管理模块
- 添加工作流管理模块
- 添加系统配置和字典管理模块
- 添加工具类 (AESUtil, DataMaskUtil, JsonUtil 等)
- 更新 .gitignore 添加 Windows 保留名称和压缩文件忽略规则
- 添加相关测试和配置文件"
```

### 方案 2: 分批提交

将大量文件分成多个逻辑组进行提交：

```powershell
# 1. 提交 .gitignore 更改
git reset HEAD .
git add .gitignore
git commit -m "chore: 更新 .gitignore 添加 Windows 保留名称和压缩文件"

# 2. 提交 microservices-common 核心模块
git add microservices/microservices-common/src/main/java/net/lab1024/sa/common/monitor/
git add microservices/microservices-common/src/main/java/net/lab1024/sa/common/notification/
git commit -m "feat: 添加监控和通知管理模块"

# 3. 继续分批提交其他模块...
```

### 方案 3: 禁用自动生成提交消息

在 VS Code 设置中禁用自动生成提交消息：

1. 打开 VS Code 设置 (Ctrl+,)
2. 搜索 `git.enableSmartCommit`
3. 取消勾选 "Git: Enable Smart Commit"
4. 或者搜索 `git.generateCommitMessage` 并禁用

### 方案 4: 使用 Git 命令行

直接使用 Git 命令行进行提交，避免 VS Code 扩展的问题：

```powershell
# 查看暂存的文件
git status --short

# 提交所有暂存的文件
git commit -m "feat: 添加 microservices-common 模块完整实现"
```

## 推荐的提交消息格式

根据项目规范，提交消息应遵循以下格式：

```
<type>(<scope>): <subject>

<body>

<footer>
```

**类型 (type)**:
- `feat`: 新功能
- `fix`: 修复问题
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建/工具链相关

**示例**:

```
feat(common): 添加 microservices-common 模块完整实现

- 实现监控管理模块，包括日志管理、指标收集、性能监控等
- 实现通知管理模块，支持多种通知渠道和模板管理
- 实现组织架构模块，包括区域、设备、员工管理
- 实现 RBAC 权限管理模块
- 实现工作流管理模块
- 添加系统配置和字典管理功能
- 添加工具类支持加密、脱敏、JSON 处理等

Closes #123
```

## 预防措施

1. **定期提交**: 避免积累大量文件后一次性提交
2. **使用分支**: 在功能分支上开发，完成后合并到主分支
3. **分批提交**: 将相关文件分组提交，保持提交历史的清晰
4. **使用 Git hooks**: 配置 pre-commit hook 进行代码检查

## 相关文档

- [Git 提交消息规范](./GIT_COMMIT_MESSAGE_STANDARDS.md)
- [Git 配置修复](./GIT_CONFIGURATION_SUMMARY.md)

## 修复日期

2025-01-30

## 修复人员

AI Assistant (Claude)
