# Git提交并合并到主分支操作指南

## 快速操作

### 方法1：使用自动化脚本（推荐）

```powershell
# 在项目根目录执行
.\scripts\git-commit-and-merge.ps1 -CommitMessage "feat(scripts): 优化启动脚本和测试脚本"
```

### 方法2：手动操作步骤

#### 步骤1：检查当前状态
```powershell
cd D:\IOE-DREAM
git status
```

#### 步骤2：添加所有更改
```powershell
git add -A
# 或添加特定文件
git add scripts/*.ps1
```

#### 步骤3：提交更改
```powershell
git commit -m "feat(scripts): 优化启动脚本和测试脚本，修复语法检查问题

- 优化start-all-complete.ps1启动脚本
- 添加test-startup.ps1测试脚本
- 修复check-syntax.ps1和test-syntax.ps1语法检查
- 更新设备通讯服务配置
- 完善启动脚本文档"
```

#### 步骤4：推送到远程仓库
```powershell
# 获取当前分支名
$currentBranch = git branch --show-current
git push origin $currentBranch
```

#### 步骤5：合并到主分支

**如果当前不在主分支：**
```powershell
# 切换到主分支（main或master）
git checkout main  # 或 git checkout master

# 合并当前分支
git merge $currentBranch -m "merge: 合并 $currentBranch 到主分支"

# 推送主分支
git push origin main  # 或 git push origin master
```

**如果当前已在主分支：**
```powershell
# 直接推送即可
git push origin main  # 或 git push origin master
```

## Commit Message 规范

根据项目规范，commit message应遵循以下格式：

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type类型：
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建/工具相关

### 示例：
```
feat(scripts): 优化启动脚本和测试脚本

- 优化start-all-complete.ps1启动脚本
- 添加test-startup.ps1测试脚本
- 修复check-syntax.ps1和test-syntax.ps1语法检查
```

## 注意事项

1. **提交前检查**：
   - 确保代码已通过测试
   - 检查是否有敏感信息
   - 确认符合代码规范

2. **合并前验证**：
   - 确保远程仓库已配置
   - 检查是否有冲突
   - 验证主分支名称（main或master）

3. **错误处理**：
   - 如果推送失败，检查远程仓库配置：`git remote -v`
   - 如果合并冲突，先解决冲突再提交
   - 如果权限不足，联系仓库管理员

## 自动化脚本说明

脚本位置：`scripts/git-commit-and-merge.ps1`

**功能**：
- 自动检测未提交的更改
- 添加所有更改到暂存区
- 提交更改
- 推送到远程仓库
- 自动检测主分支（main或master）
- 如果不在主分支，自动切换并合并
- 推送合并后的主分支

**使用方法**：
```powershell
# 使用默认提交信息
.\scripts\git-commit-and-merge.ps1

# 使用自定义提交信息
.\scripts\git-commit-and-merge.ps1 -CommitMessage "你的提交信息"
```

## 故障排查

### 问题1：推送失败
```powershell
# 检查远程仓库配置
git remote -v

# 如果未配置，添加远程仓库
git remote add origin <repository-url>
```

### 问题2：合并冲突
```powershell
# 查看冲突文件
git status

# 解决冲突后
git add <resolved-files>
git commit -m "merge: 解决合并冲突"
```

### 问题3：权限不足
- 检查GitHub访问令牌
- 确认仓库访问权限
- 联系仓库管理员

## 相关文档

- [Git工作流规范](./GIT_WORKFLOW.md)
- [代码提交规范](./COMMIT_CONVENTION.md)
- [分支管理策略](./BRANCH_STRATEGY.md)
