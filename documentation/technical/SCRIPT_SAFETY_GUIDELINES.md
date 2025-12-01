# 🔒 脚本安全使用清单

**创建日期**: 2025-11-22
**核心原则**: 严禁任何脚本修改、移动或删除项目代码文件
**特别保护**: SQL脚本文件（.sql）严禁任何形式的自动处理

---

## 🚨 绝对禁止的脚本操作

### ❌ 危险脚本特征（严禁使用）
- 包含 `rm -rf`、`find -exec rm` 的脚本
- 包含 `mv` 移动文件操作的脚本
- 包含 `sed -i` 批量修改文件内容的脚本
- 任何自动化编码修复脚本
- Docker 批量删除容器脚本
- **任何处理SQL脚本的自动化工具**

---

## ✅ 安全脚本推荐（每日开发使用）

### 🟢 检查验证类脚本（只读操作）
```bash
# 推荐安全脚本 - 仅检查和报告
./scripts/technology-migration-zero-tolerance-check.sh    # 技术迁移检查
./scripts/quick-tech-migration-check.sh                    # 快速检查
./scripts/quality-gate.sh                                 # 质量门禁
./scripts/mandatory-verification.sh                        # 强制验证
./scripts/dev-standards-check.sh                           # 开发规范检查
./scripts/commit-guard.sh                                 # 提交守卫
```

### 🟡 谨慎使用的脚本（需要人工确认）
```bash
# 谨慎使用 - 运行前必须检查内容
./scripts/pre-commit-technology-migration-check.sh         # Pre-commit检查
./scripts/integrated-workflow.sh                           # 集成工作流程
```

---

## 🔍 脚本安全检查清单

### 使用前必须检查
1. **阅读脚本内容**：完全理解脚本要做什么
2. **检查危险操作**：查找 `rm`、`mv`、`sed -i` 等关键词
3. **确认影响范围**：明确脚本会操作哪些文件
4. **备份重要数据**：如有不确定，先手动备份

### 安全验证步骤
```bash
# 1. 检查脚本内容（必须！）
grep -n "rm\|mv\|sed.*-i" script-name.sh

# 2. 模拟运行（使用 -n 或 --dry-run 参数）
./script-name.sh --dry-run  # 如果支持

# 3. 小范围测试
# 先在非关键文件上测试脚本效果

# 4. 完整运行（在确认安全后）
./script-name.sh
```

---

## 🚨 已识别的危险脚本（绝对禁止使用）

以下脚本包含危险操作，**严禁使用**：

### 高危脚本（包含删除操作）
- `fix-all-garbled-characters.py` - 大规模文件修改
- `fix-all-encoding-issues.py` - 批量编码修改
- `emergency-encoding-fix.sh` - 紧急修复脚本
- `ultimate-encoding-fix.sh` - 终极修复脚本
- `zero-garbage-encoding-fix.sh` - 零乱码修复
- 所有包含 `rm -rf` 的脚本

### 中危脚本（包含移动操作）
- `docker-cleanup.sh` - Docker清理
- `emergency-task-cleanup.sh` - 任务清理
- `pre-commit-technology-migration-check.sh` - 包含临时目录操作

---

## 🛡️ SQL脚本特别保护措施

### 📋 项目现有SQL脚本（5个文件，绝对保护）
```bash
# 数据库优化脚本 - 严禁自动执行
./attendance-database-index-optimization.sql          # 索引优化
./attendance-database-partitioning-optimization.sql   # 分区优化
./attendance-performance-optimization.sql            # 性能优化
./attendance-query-optimization-views.sql            # 查询优化视图
./attendance-stored-procedure-optimization.sql       # 存储过程优化
```

### 🔒 SQL脚本安全使用原则
- **严禁自动执行**：SQL脚本只能由数据库管理员手动执行
- **执行前备份**：执行前必须完整备份数据库
- **分步验证**：每个SQL语句都要单独验证
- **测试环境优先**：先在测试环境验证效果
- **回滚准备**：准备好回滚脚本

### ✅ SQL脚本安全操作流程
```bash
# 1. 检查SQL脚本内容（必须！）
cat attendance-database-index-optimization.sql

# 2. 备份数据库
mysqldump -u root -p smart_admin_v3 > backup_before_optimization.sql

# 3. 在测试环境验证
# 4. 逐条执行SQL语句
# 5. 验证执行结果
# 6. 准备回滚方案
```

---

## 📋 替代的安全操作方案

### 编码问题修复
**❌ 危险方式**：使用自动化修复脚本
**✅ 安全方式**：
```bash
# 1. 手动识别问题文件
find . -name "*.java" -exec file {} \; | grep -v "UTF-8"

# 2. 手动修复单个文件
iconv -f GBK -t UTF-8 problematic-file.java > temp-file.java
mv temp-file.java problematic-file.java

# 3. 验证修复结果
file problematic-file.java
```

### 依赖注入修复
**❌ 危险方式**：使用批量替换脚本
**✅ 安全方式**：
```bash
# 1. 识别问题文件
grep -r "@Autowired" . --include="*.java"

# 2. IDE手动替换或逐个文件修复
# 3. 验证每个修改的文件
```

### 包名修复
**❌ 危险方式**：使用全局替换脚本
**✅ 安全方式**：
```bash
# 1. 识别问题文件
grep -r "javax\." . --include="*.java"

# 2. IDE重构功能或逐个文件手动修复
# 3. 编译验证每个修改
```

---

## 🎯 脚本使用最佳实践

### 日常开发推荐流程
```bash
# 每日开发安全检查流程
./scripts/quick-tech-migration-check.sh        # 快速检查
./scripts/quality-gate.sh                     # 质量门禁

# Git提交前安全检查
./scripts/commit-guard.sh                     # 提交守卫

# 任务完成后验证
./scripts/mandatory-verification.sh           # 强制验证
```

### 禁止的自动化操作
- ❌ 任何形式的批量文件修改
- ❌ 任何形式的自动文件删除
- ❌ 任何形式的自动文件移动
- ❌ 任何形式的编码自动修复

### 推荐的手动操作
- ✅ IDE内置重构工具
- ✅ 逐个文件手动修复
- ✅ 小范围测试验证
- ✅ 完整的备份策略

---

## 📞 紧急情况处理

### 如果脚本意外执行
1. **立即停止**：Ctrl+C 中断执行
2. **检查修改**：`git status` 查看文件变化
3. **立即撤销**：`git checkout -- .` 撤销所有修改
4. **验证恢复**：确认文件已恢复原状

### 脚本问题报告
如发现危险脚本或有安全问题，立即：
1. 停止使用该脚本
2. 记录脚本名称和问题
3. 向团队报告安全风险

---

---

## 📞 紧急情况处理

### 如果脚本意外执行
1. **立即停止**：Ctrl+C 中断执行
2. **检查修改**：`git status` 查看文件变化
3. **立即撤销**：`git checkout -- .` 撤销所有修改
4. **验证恢复**：确认文件已恢复原状
5. **特别检查**：确认所有SQL脚本文件未被修改

### SQL脚本误操作处理
```bash
# 检查SQL脚本是否被修改
git status | grep "\.sql$"

# 如果被修改，立即恢复
git checkout -- *.sql

# 验证SQL文件完整性
md5sum *.sql  # 记录MD5值用于对比
```

### 脚本问题报告
如发现危险脚本或有安全问题，立即：
1. 停止使用该脚本
2. 记录脚本名称和问题
3. 向团队报告安全风险
4. 保护相关文件不被进一步影响

---

## 🎯 最终安全承诺

**⚠️ 最终警告**:

**任何情况下都严禁运行可能修改、删除或移动项目代码的脚本！**
**所有代码修改必须通过IDE手动完成或经过严格审查！**
**SQL脚本文件绝对保护，严禁任何自动化处理！**
**数据库脚本只能由数据库管理员在完整备份后手动执行！**

**安全第一，预防为主！**
**数据安全，重于泰山！**