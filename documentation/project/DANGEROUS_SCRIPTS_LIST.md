# 🚨 危险脚本清单

**创建日期**: 2025-11-22 01:20:16
**清理标准**: 基于SCRIPY_SECURITY_ANALYSIS_REPORT.md
**隔离位置**: scripts/quarantined-scripts

---

## 📊 清理统计

- **总脚本数量**: 0
- **危险脚本数量**: 0
- **安全脚本数量**: 0
- **已隔离脚本数量**: 0

## 🚫 隔离的危险脚本


以下脚本已移动到隔离目录，禁止执行：

- **fix-encoding-issues.py** (Python脚本)
  - **主要风险**: 编码批量修复操作

- **system-encoding-converter.py** (Python脚本)
  - **主要风险**: 编码批量修复操作

- **system-encoding-validator.py** (Python脚本)
  - **主要风险**: 编码批量修复操作

- **zero-garbage-encoding-fix.sh** (Shell脚本)
  - **主要风险**: 编码批量修复操作


## ⚠️ 安全建议

1. **严禁执行隔离脚本**
   - 隔离目录中的脚本绝对禁止执行
   - 如需使用，必须经过技术负责人审批

2. **手动审查替代**
   - 将批量操作改为逐个文件处理
   - 使用IDE内置工具替代脚本

3. **建立检查机制**
   - 定期运行此清理脚本
   - 集成到CI/CD流水线

4. **权限控制**
   - 对隔离目录设置只读权限
   - 限制对脚本的修改权限

## ✅ 安全脚本推荐

以下脚本已经验证为安全，可以继续使用：

### 技术迁移检查类
- `technology-migration-zero-tolerance-check.sh`
- `quick-tech-migration-check.sh`
- `pre-commit-technology-migration-check.sh`
- `incremental-compile-error-monitor.sh`

### 监控检查类
- `quality-monitoring-dashboard.sh`
- `permission-monitor.sh`
- `quick-check.sh`

### 质量保障类
- `commit-guard.sh`
- `dev-standards-check.sh`
- `code-quality-check.sh`

---

**注意**: 此清单会随着脚本清理更新而自动更新。
