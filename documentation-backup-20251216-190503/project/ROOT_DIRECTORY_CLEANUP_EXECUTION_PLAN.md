# 根目录清理执行计划

> **创建时间**: 2025-01-30
> **执行状态**: 待执行
> **优先级**: P0

---

## 📋 清理目标

清理根目录下的冗余文档和临时文件，整合到对应目录，保持根目录整洁。

---

## 🎯 清理分类

### 1. 报告文件归档（约80个）

**目标目录**: `documentation/archive/root-reports/`

**文件模式**:
- `*_REPORT.md`
- `*_GUIDE.md` (部分)
- `*_FIX*.md`
- `*_ANALYSIS*.md`
- `*_SUMMARY*.md`
- `*_COMPLETE*.md`
- `*_FINAL*.md`
- `*_EXECUTION*.md`
- `*_STATUS*.md`
- `*_VERIFICATION*.md`
- `*_OPTIMIZATION*.md`
- `*_IMPLEMENTATION*.md`
- `*_PROGRESS*.md`
- `*_TECHNICAL_DEBT*.md`
- `*_CLEANUP*.md`
- `*_CONSOLIDATION*.md`

### 2. 指南文件分类移动（约10个）

#### 部署相关 → `documentation/deployment/`
- `DEPLOYMENT_OPTIMIZATION_BEST_PRACTICES.md`
- `DEPLOYMENT_SUMMARY.md`
- `QUICK_DOCKER_DEPLOYMENT.md`
- `DOCKER_DEPLOYMENT_GUIDE.md`

#### 开发相关 → `documentation/02-开发指南/`
- `DEVELOPMENT_QUICK_START.md`
- `QUICK_START.md`
- `MANUAL_BUILD_GUIDE.md`

#### 快速修复 → `documentation/guide/`
- `QUICK_FIX_DATABASE.md`
- `QUICK_FIX_NACOS.md`
- `QUICK_PUSH.md`

#### 脚本使用 → `scripts/`
- `SCRIPT_STATUS.md`
- `SCRIPTS_USAGE_GUIDE.md`
- `start-ps1-features.md`

#### 技术文档 → `documentation/technical/`
- `DOCUMENTATION_CONSOLIDATION_AND_OPENSPEC_PROPOSAL.md`
- `TECHNICAL_DEBT_PREVENTION_GUIDE.md`

#### 监控系统 → `documentation/monitoring/`
- `MONITORING_ALERT_SYSTEM_DESIGN.md`

### 3. 脚本文件移动（4个）

**目标目录**: `scripts/`

- `build-local-ps1.ps1`
- `start.ps1`
- `fix-vue-encoding-simple.py`
- `fix-vue-encoding.py`

### 4. 临时文件删除（约10个）

- `*.log` (3个启动日志)
- `*.txt` (error.txt, common-service-logs.txt, views_tree.txt, MANUAL_COMMANDS.txt)
- `*.html` (index.html)
- `*.css` (styles.css)
- `*.js` (script.js)
- `nul`

### 5. 保留的核心文件

- `README.md`
- `CLAUDE.md`
- `.cursorrules`
- `.gitignore`
- `docker-compose*.yml`
- `Dockerfile*`
- `pom.xml`

---

## 🔧 执行步骤

### 步骤1: 批量归档报告文件

```powershell
# 创建归档目录
New-Item -ItemType Directory -Path "documentation\archive\root-reports" -Force

# 批量移动报告文件
$patterns = @("*_REPORT.md", "*_FIX*.md", "*_ANALYSIS*.md", "*_SUMMARY*.md", 
              "*_COMPLETE*.md", "*_FINAL*.md", "*_EXECUTION*.md", "*_STATUS*.md",
              "*_VERIFICATION*.md", "*_OPTIMIZATION*.md", "*_IMPLEMENTATION*.md",
              "*_PROGRESS*.md", "*_TECHNICAL_DEBT*.md", "*_CLEANUP*.md", 
              "*_CONSOLIDATION*.md")

foreach ($pattern in $patterns) {
    Get-ChildItem -Filter $pattern | Move-Item -Destination "documentation\archive\root-reports\" -Force
}
```

### 步骤2: 分类移动指南文件

```powershell
# 部署相关
Move-Item "DEPLOYMENT_OPTIMIZATION_BEST_PRACTICES.md" "documentation\deployment\" -Force
Move-Item "DEPLOYMENT_SUMMARY.md" "documentation\deployment\" -Force
Move-Item "QUICK_DOCKER_DEPLOYMENT.md" "documentation\deployment\" -Force
Move-Item "DOCKER_DEPLOYMENT_GUIDE.md" "documentation\deployment\" -Force

# 开发相关
Move-Item "DEVELOPMENT_QUICK_START.md" "documentation\02-开发指南\" -Force
Move-Item "QUICK_START.md" "documentation\02-开发指南\" -Force
Move-Item "MANUAL_BUILD_GUIDE.md" "documentation\02-开发指南\" -Force

# 快速修复
Move-Item "QUICK_FIX_DATABASE.md" "documentation\guide\" -Force
Move-Item "QUICK_FIX_NACOS.md" "documentation\guide\" -Force
Move-Item "QUICK_PUSH.md" "documentation\guide\" -Force

# 脚本使用
Move-Item "SCRIPT_STATUS.md" "scripts\" -Force
Move-Item "SCRIPTS_USAGE_GUIDE.md" "scripts\" -Force
Move-Item "start-ps1-features.md" "scripts\" -Force

# 技术文档
Move-Item "DOCUMENTATION_CONSOLIDATION_AND_OPENSPEC_PROPOSAL.md" "documentation\technical\" -Force
Move-Item "TECHNICAL_DEBT_PREVENTION_GUIDE.md" "documentation\technical\" -Force

# 监控系统
Move-Item "MONITORING_ALERT_SYSTEM_DESIGN.md" "documentation\monitoring\" -Force
```

### 步骤3: 移动脚本文件

```powershell
Move-Item "build-local-ps1.ps1" "scripts\" -Force
Move-Item "start.ps1" "scripts\" -Force
Move-Item "fix-vue-encoding-simple.py" "scripts\" -Force
Move-Item "fix-vue-encoding.py" "scripts\" -Force
```

### 步骤4: 删除临时文件

```powershell
Remove-Item "*.log" -Force
Remove-Item "error.txt" -Force
Remove-Item "common-service-logs.txt" -Force
Remove-Item "views_tree.txt" -Force
Remove-Item "MANUAL_COMMANDS.txt" -Force
Remove-Item "index.html" -Force
Remove-Item "styles.css" -Force
Remove-Item "script.js" -Force
Remove-Item "nul" -Force -ErrorAction SilentlyContinue
```

---

## 📊 预期结果

| 清理项 | 清理前 | 清理后 | 减少 |
|--------|--------|--------|------|
| 根目录MD文件 | 90+ | 2 | 98% |
| 根目录脚本 | 4 | 0 | 100% |
| 根目录临时文件 | 10+ | 0 | 100% |

---

## ✅ 执行检查清单

- [ ] 备份重要文件（可选）
- [ ] 创建归档目录
- [ ] 批量归档报告文件
- [ ] 分类移动指南文件
- [ ] 移动脚本文件
- [ ] 删除临时文件
- [ ] 验证清理结果
- [ ] 生成清理报告

---

**注意**: 执行前请确认所有文件都已备份或可以安全移动/删除。

