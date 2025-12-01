# Java导入批量优化 - 执行指南

## 📚 可用方案总览

| 方案 | 适用场景 | 优点 | 缺点 | 推荐度 |
|------|---------|------|------|--------|
| **方案一：IDEA批量优化** | 开发环境 | 最安全、最准确、支持撤销 | 需要IDEA | ⭐⭐⭐⭐⭐ |
| **方案二：Maven插件** | CI/CD | 自动化、可集成流水线 | 配置复杂 | ⭐⭐⭐⭐ |
| **方案三：PowerShell脚本** | 批量处理 | 快速、无需IDE | 可能误判 | ⭐⭐⭐ |

## 🚀 快速开始

### ✨ 推荐：使用IDEA批量优化（最安全）

```
📖 详细文档：scripts/idea-batch-optimize-imports.md

🎯 快速步骤：
1. 打开IntelliJ IDEA
2. 在Project面板中，右键点击项目根目录
3. 选择：Code → Optimize Imports
4. 点击 "Run" 开始批量优化
5. 运行编译测试：mvn clean compile -DskipTests
```

**⌨️ 快捷键**: `Ctrl + Alt + O` (单文件)

---

### 🔧 方案二：Maven插件检查（CI/CD集成）

#### 步骤1：配置Maven插件
```powershell
# 将以下插件配置添加到pom.xml
# 参考文件：scripts/maven-code-quality-check.xml
```

#### 步骤2：运行代码质量检查
```powershell
cd D:\IOE-DREAM\smart-admin-api-java17-springboot3

# 运行Checkstyle检查
mvn checkstyle:check

# 运行PMD代码分析
mvn pmd:check

# 运行SpotBugs缺陷检测
mvn spotbugs:check

# 生成完整质量报告
mvn clean verify site

# 查看报告
start target/site/checkstyle.html
```

---

### 💻 方案三：PowerShell自动化脚本

#### 步骤1：模拟运行（推荐先测试）
```powershell
cd D:\IOE-DREAM\scripts

# 模拟运行，不修改文件
.\clean-unused-imports.ps1 -DryRun

# 详细模式
.\clean-unused-imports.ps1 -DryRun -Verbose
```

#### 步骤2：查看模拟报告
```powershell
# 报告位置：scripts/reports/import-cleanup-report-yyyyMMdd_HHmmss.md
start scripts\reports\

# 使用默认编辑器打开最新报告
$latest = Get-ChildItem -Path ".\reports\" -Filter "import-cleanup-report-*.md" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
notepad $latest.FullName
```

#### 步骤3：实际执行清理
```powershell
# ⚠️ 注意：执行前请先提交当前代码到Git!
git add .
git commit -m "chore: 准备执行导入清理"

# 执行清理
.\clean-unused-imports.ps1

# 验证修改
git diff --stat

# 运行编译测试
cd ..\smart-admin-api-java17-springboot3
mvn clean compile -DskipTests
```

#### 步骤4：验证并提交
```powershell
# 如果编译通过，运行测试
mvn test

# 提交修改
git add .
git commit -m "chore: 清理未使用的Java导入语句"
```

---

## 📊 预期效果

### 优化前（当前状态）
```
⚠️ 未使用的导入:        约80处
⚠️ 类型安全警告:        约15处
⚠️ 未使用的字段/方法:   约40处
⚠️ 代码质量问题:        约150处
```

### 优化后（目标状态）
```
✅ 未使用的导入:        0处
✅ 导入排序规范:        100%符合
✅ 代码整洁度:          提升30%
✅ 编译警告:            减少50%+
```

---

## ⚙️ 配置建议

### 1. IDEA自动优化配置

```
File → Settings → Editor → General → Auto Import

✅ Optimize imports on the fly
✅ Add unambiguous imports on the fly
```

### 2. Git Pre-commit Hook配置

创建 `.git/hooks/pre-commit` 文件：
```bash
#!/bin/sh
# 提交前自动优化导入

echo "🔍 检查Java导入..."
cd smart-admin-api-java17-springboot3

# 使用Maven Checkstyle检查
mvn checkstyle:check -q

if [ $? -ne 0 ]; then
    echo "❌ 代码质量检查失败，请先优化导入!"
    echo "💡 运行: scripts\clean-unused-imports.ps1"
    exit 1
fi

echo "✅ 代码质量检查通过"
exit 0
```

---

## 🚨 注意事项

### ⚠️ 安全提醒
1. **备份代码**: 批量操作前务必提交代码到Git
2. **逐步验证**: 优化后立即运行编译和测试
3. **分模块处理**: 大项目建议分模块逐个优化
4. **检查删除**: 确认删除的导入确实未使用

### 💡 最佳实践
1. **优先使用IDEA**: 最安全最准确
2. **模拟运行优先**: PowerShell脚本先用-DryRun测试
3. **持续集成**: 在CI/CD中加入代码质量检查
4. **定期清理**: 养成每天优化一次的习惯

### 🐛 故障排除

#### 问题1：IDEA快捷键不生效
```
解决方案：
File → Settings → Keymap
搜索 "Optimize Imports"
检查快捷键绑定
```

#### 问题2：优化后编译失败
```
解决方案：
1. Ctrl + Z 撤销修改
2. 先确保项目能正常编译
3. 逐个模块优化并测试
```

#### 问题3：某些导入被误删
```
解决方案：
1. 检查是否通过反射使用
2. 添加 @SuppressWarnings("unused")
3. 在IDEA中排除特定包
```

---

## 📈 后续优化建议

### 1. 清理未使用的代码（code-quality-3）
```powershell
# 使用IDEA的Inspect Code功能
Analyze → Inspect Code → smart-admin-api-java17-springboot3
```

### 2. 修复类型安全警告
```powershell
# 使用@SuppressWarnings注解
@SuppressWarnings("unchecked")
```

### 3. 清理已弃用API
```powershell
# 搜索并替换已弃用方法
Find in Files: @Deprecated
```

### 4. 实现TODO标记的功能
```powershell
# 搜索TODO注释
Find in Files: // TODO
```

---

## 📞 技术支持

### 相关文档
- [Java编码规范](../docs/repowiki/zh/content/开发规范体系/核心规范/Java编码规范.md)
- [架构设计规范](../docs/ARCHITECTURE_STANDARDS.md)
- [项目开发指南](../docs/PROJECT_GUIDE.md)

### 工具文档
- [IDEA批量优化指南](idea-batch-optimize-imports.md)
- [Maven代码质量检查](maven-code-quality-check.xml)
- [PowerShell清理脚本](clean-unused-imports.ps1)

---

**文档版本**: v1.0
**更新时间**: 2025-11-16
**适用项目**: IOE-DREAM 智慧园区一卡通管理平台
**技术栈**: Java 17 + Spring Boot 3.5.4 + Maven 3.x

