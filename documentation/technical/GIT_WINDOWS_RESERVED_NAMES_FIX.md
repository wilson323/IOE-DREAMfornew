# Git Windows 保留设备名称问题修复

## 问题描述

在 Windows 系统中，Git 操作时遇到以下错误：

```
error: invalid path 'nul'
error: unable to add 'nul' to index
fatal: adding files failed
```

## 问题原因

Windows 系统中有一些保留的设备名称，这些名称不能用作文件名：

- **NUL** - 空设备（类似 Linux 的 `/dev/null`）
- **CON** - 控制台
- **PRN** - 打印机
- **AUX** - 辅助设备
- **COM1-COM9** - 串行端口
- **LPT1-LPT9** - 并行端口

当 Git 尝试添加或跟踪这些名称的文件时，会因为 Windows 文件系统的限制而失败。

## 解决方案

### 1. 更新 .gitignore 文件

已在 `.gitignore` 文件中添加了所有 Windows 保留设备名称的忽略规则：

```gitignore
### Windows Reserved Names ###
# Windows 保留的设备名称，不能用作文件名
# 这些名称在 Windows 中是特殊设备，会导致 Git 操作失败
nul
CON
PRN
AUX
COM1
COM2
COM3
COM4
COM5
COM6
COM7
COM8
COM9
LPT1
LPT2
LPT3
LPT4
LPT5
LPT6
LPT7
LPT8
LPT9
```

### 2. 从 Git 索引中移除（如果已存在）

如果这些文件已经被添加到 Git 索引中，需要先移除：

```powershell
# 从索引中移除（不删除实际文件）
git rm --cached nul

# 或者如果文件不存在，直接提交移除操作
git commit -m "chore: 移除 Windows 保留设备名称文件"
```

### 3. 删除实际文件（如果存在）

```powershell
# 检查文件是否存在
if (Test-Path "nul") {
    Remove-Item "nul" -Force
}
```

## 预防措施

1. **代码审查**：在代码审查时注意是否有脚本或程序可能创建这些保留名称的文件
2. **CI/CD 检查**：在 CI/CD 流程中添加检查，防止提交这些文件
3. **开发规范**：在开发规范中明确禁止使用 Windows 保留设备名称作为文件名

## 相关资源

- [Microsoft Docs: Naming Files, Paths, and Namespaces](https://docs.microsoft.com/en-us/windows/win32/fileio/naming-a-file)
- [Git Documentation: gitignore](https://git-scm.com/docs/gitignore)

## Git 重命名检测优化

### 问题

Git 在处理大量文件时可能出现以下警告：

```
warning: exhaustive rename detection was skipped due to too many files.
warning: you may want to set your diff.renameLimit variable to at least 1814
```

### 解决方案

已设置项目本地 Git 配置：

```powershell
git config diff.renameLimit 2000
```

这将允许 Git 在检测文件重命名时处理更多文件，避免跳过详尽检测。

### 验证配置

```powershell
# 检查配置
git config --get diff.renameLimit

# 应该输出: 2000
```

## 修复日期

2025-01-30

## 修复人员

AI Assistant (Claude)
