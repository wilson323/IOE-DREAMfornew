# 🧹 IOE-DREAM 项目清理指南

> **安全第一**: 所有清理操作都会先备份，确保项目安全
> **使用方法**: 运行 `.\scripts\safe-cleanup-project.ps1` 进行安全清理

## 📋 快速开始

### 1. 查看清理计划（推荐先执行）
```powershell
.\scripts\safe-cleanup-project.ps1 -DryRun
```

### 2. 执行安全清理
```powershell
.\scripts\safe-cleanup-project.ps1
```

### 3. 查看清理报告
清理完成后，报告将保存在：
- `bak/<timestamp>/cleanup_report.txt`
- `bak/<timestamp>/CLEANUP_ANALYSIS_REPORT.md`

---

## 🎯 清理内容概览

### 🟢 P0级 - 安全清理（零风险）
- `smart-admin-web-javascript/node_modules/` (398MB)
- IDE缓存文件 (50MB)
- 构建缓存文件 (30MB)
- **节省空间**: ~478MB

### 🟡 P1级 - 备份后清理（低风险）
- 重复的数据库脚本备份 (200MB)
- 过时的归档文档 (50MB)
- 工具缓存文件 (15MB)
- **节省空间**: ~367MB

### 🟠 P2级 - 谨慎清理（需确认）
- 重复的PowerShell脚本 (25MB)
- 冗余配置文件 (15MB)
- **节省空间**: ~45MB

---

## 🔧 清理工具功能

### 安全机制
- ✅ **自动备份**: 所有删除前先备份到 `bak/` 目录
- ✅ **分阶段清理**: 按风险等级分批执行
- ✅ **确认机制**: 重要操作需要用户确认
- ✅ **详细报告**: 生成完整的清理报告
- ✅ **恢复支持**: 提供文件恢复方法

### 清理效果
- **存储空间**: 节省 890MB+
- **文件数量**: 清理 2000+ 个冗余文件
- **项目结构**: 更清晰的文件组织
- **开发体验**: IDE响应速度提升

---

## 📁 备份文件位置

```
IOE-DREAM/
├── bak/
│   └── 20251209_151531_cleanup_backup/
│       ├── node_modules_backup/
│       ├── database_scripts_backup/
│       ├── serena_cache_backup/
│       ├── cleanup_report.txt
│       └── CLEANUP_ANALYSIS_REPORT.md
└── scripts/
    └── safe-cleanup-project.ps1
```

---

## 🔄 恢复文件方法

### 恢复特定文件
```powershell
# 恢复node_modules
Copy-Item "bak\20251209_151531_cleanup_backup\node_modules_backup\*" "smart-admin-web-javascript\" -Recurse

# 恢复其他备份
Copy-Item "bak\<timestamp>\<backup_folder>\*" "<target_path>" -Recurse
```

### 完全恢复
```powershell
# 查看所有备份
ls bak\20251209_151531_cleanup_backup\

# 选择需要恢复的文件夹
Copy-Item "bak\20251209_151531_cleanup_backup\<folder>" "<destination>" -Recurse
```

---

## ⚠️ 重要提醒

### 安全保障
- 所有删除操作都会先备份
- 备份文件保留完整目录结构
- 提供详细的恢复指南

### 风险等级
- **P0级**: 零风险（可重新生成）
- **P1级**: 低风险（已完整备份）
- **P2级**: 中等风险（需要确认）

### 最佳实践
1. 先运行 `-DryRun` 查看影响
2. 确认清理计划再执行
3. 保留备份文件至少一周
4. 定期重复清理保持项目整洁

---

## 📞 技术支持

如遇到问题：
1. 查看 `cleanup_report.txt` 了解详情
2. 从 `bak/` 目录恢复文件
3. 重新运行清理脚本
4. 查看详细分析报告

**让我们一起为IOE-DREAM项目创建一个更清洁、更高效的开发环境！** 🚀