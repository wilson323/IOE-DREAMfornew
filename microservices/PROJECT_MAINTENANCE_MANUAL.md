# IOE-DREAM 项目维护手册

> **版本**: v1.0.0
> **更新日期**: 2025-12-20
> **适用范围**: IOE-DREAM 智慧园区一卡通管理平台

## 📋 概述

本手册为 IOE-DREAM 项目提供完整的维护指南，确保项目长期保持企业级代码质量标准。

### 🎯 维护目标

- **代码质量**: 保持企业级代码质量标准
- **架构合规**: 严格遵循四层架构规范
- **编译稳定**: 确保100%编译通过率
- **安全合规**: 符合国家三级等保要求

## 🛠️ 质量保障工具

### 1. 持续集成质量保障脚本

**脚本文件**: `ci-quality-gate.ps1`

**使用方法**:

```powershell
# 完整质量检查（推荐）
.\ci-quality-gate.ps1 -BuildType "full"

# 快速检查（仅编译）
.\ci-quality-gate.ps1 -BuildType "quick"

# 指定模块检查
.\ci-quality-gate.ps1 -BuildType "full" -TargetModule "microservices-common"

# 跳过测试
.\ci-quality-gate.ps1 -BuildType "quick" -SkipTests
```

**质量检查项目**:

| 检查类别 | 检查内容 | 通过标准 |
|---------|---------|---------|
| **环境检查** | Java/Maven/Git环境 | 环境正常 |
| **代码规范** | @Autowired、Repository、javax包名 | 零违规 |
| **编译检查** | Maven编译状态 | 95%+通过率 |
| **单元测试** | JUnit测试执行 | 100%通过 |
| **安全检查** | 硬编码密码、安全问题 | 零问题 |
| **性能检查** | String拼接、性能模式 | ≤5个问题 |

### 2. Git Pre-commit 钩子

**自动质量保障**: 每次提交代码前自动执行质量检查

**安装方法**:

```bash
# Windows环境
.\setup-git-hooks.bat

# 手动设置（Git Bash）
chmod +x .git/hooks/pre-commit
```

**使用说明**:

```bash
# 正常提交（会触发质量检查）
git commit -m "feat: 新增功能"

# 跳过质量检查（紧急情况）
git commit --no-verify -m "fix: 紧急修复"
```

## 📊 质量门禁标准

### 🔴 零容忍问题

以下问题必须为零，否则无法通过质量门禁：

1. **编译错误**: 任何模块编译失败
2. **架构违规**: @Autowired、@Repository、javax包名违规
3. **安全问题**: 硬编码密码、SQL注入等
4. **测试失败**: 单元测试执行失败

### 🟡 警告问题

以下问题建议修复，但不阻止提交：

1. **性能问题**: String拼接、循环优化
2. **代码复杂度**: 方法过长、圈复杂度过高
3. **测试覆盖率**: 低于80%的测试覆盖率

## 🚀 日常维护流程

### 代码开发流程

1. **开发前**:
   ```bash
   # 拉取最新代码
   git pull origin main

   # 切换到开发分支
   git checkout -b feature/新功能
   ```

2. **开发中**:
   ```bash
   # 定期检查代码质量
   .\ci-quality-gate.ps1 -BuildType "quick"
   ```

3. **提交前**:
   ```bash
   # Git钩子自动执行质量检查
   git add .
   git commit -m "feat: 实现新功能"
   ```

4. **合并前**:
   ```bash
   # 完整质量检查
   .\ci-quality-gate.ps1 -BuildType "full"

   # 推送到远程
   git push origin feature/新功能
   ```

### 发布流程

1. **发布前检查**:
   ```powershell
   # 完整质量检查
   .\ci-quality-gate.ps1 -BuildType "full" -GenerateReport

   # 验证所有模块编译
   mvn clean compile

   # 运行所有测试
   mvn test
   ```

2. **发布准备**:
   ```bash
   # 生成质量报告
   .\ci-quality-gate.ps1 -BuildType "full" -ReportPath "./release-reports"

   # 检查依赖更新
   mvn versions:display-dependency-updates
   ```

3. **发布执行**:
   ```bash
   # 合并到主分支
   git checkout main
   git merge feature/新功能

   # 打包发布
   mvn clean package -DskipTests

   # 打标签
   git tag -a v1.0.1 -m "发布版本1.0.1"
   ```

## 🔧 常见问题解决

### Q1: 质量检查失败如何处理？

**A**: 查看质量报告，按优先级修复问题：

```powershell
# 查看详细错误信息
.\ci-quality-gate.ps1 -BuildType "full" -GenerateReport

# 常见问题修复：
# 1. @Autowired → @Resource
# 2. @Repository → @Mapper
# 3. javax.* → jakarta.*
# 4. 修复编译错误
```

### Q2: 如何跳过质量检查？

**A**: 紧急情况下可以跳过：

```bash
# 跳过pre-commit钩子
git commit --no-verify -m "紧急修复"

# 直接跳过质量脚本
# （不推荐，仅在CI/CD环境中使用）
```

### Q3: 如何添加自定义质量检查？

**A**: 扩展 `ci-quality-gate.ps1` 脚本：

```powershell
# 在脚本中添加新的检查函数
function Test-CustomRule {
    param()

    # 自定义检查逻辑
    Write-Host "执行自定义检查..." -ForegroundColor Yellow

    # 返回检查结果
    return $true
}

# 在主流程中调用
$customResult = Test-CustomRule
```

### Q4: 质量报告在哪里？

**A**: 质量报告默认生成在 `.ci-reports` 目录：

```bash
# 报告文件命名格式
quality-report-YYYYMMDD-HHMMSS.md

# 自定义报告路径
.\ci-quality-gate.ps1 -ReportPath "./custom-reports"
```

## 📈 质量监控

### 每日检查清单

- [ ] 代码编译无错误
- [ ] 无架构违规问题
- [ ] 测试通过率100%
- [ ] 无新增安全问题

### 每周检查清单

- [ ] 完整质量门禁检查
- [ ] 测试覆盖率≥80%
- [ ] 依赖更新检查
- [ ] 性能基准测试

### 每月检查清单

- [ ] 代码质量趋势分析
- [ ] 技术债务评估
- [ ] 架构优化建议
- [ ] 团队培训需求

## 🎯 最佳实践

### 开发规范

1. **提交信息规范**:
   ```
   feat: 新功能
   fix: 修复问题
   docs: 文档更新
   style: 代码格式调整
   refactor: 重构
   test: 测试相关
   chore: 构建/工具相关
   ```

2. **分支命名规范**:
   ```
   feature/功能名称
   fix/问题描述
   hotfix/紧急修复
   release/版本发布
   ```

3. **代码审查清单**:
   - 架构合规性检查
   - 性能影响评估
   - 安全风险评估
   - 测试覆盖率检查

### 质量保障

1. **自动化检查**:
   - Git pre-commit钩子
   - CI/CD流水线质量门禁
   - 定期质量扫描

2. **人工审查**:
   - 代码审查制度
   - 架构设计评审
   - 安全渗透测试

3. **持续改进**:
   - 质量指标监控
   - 技术债务管理
   - 团队能力提升

## 📞 支持与反馈

### 问题报告

如遇到质量问题，请提供以下信息：

1. **环境信息**: OS、Java、Maven版本
2. **复现步骤**: 详细的操作步骤
3. **错误信息**: 完整的错误日志
4. **质量报告**: 质量检查生成的报告文件

### 联系方式

- **技术支持**: 架构委员会
- **文档维护**: 开发团队
- **工具改进**: DevOps团队

---

**📋 更新记录**:

| 版本 | 日期 | 更新内容 | 维护人 |
|------|------|---------|--------|
| v1.0.0 | 2025-12-20 | 初始版本，建立完整的质量保障体系 | IOE-DREAM团队 |

---

**💡 重要提醒**: 严格遵守质量保障流程是项目长期成功的关键。每个团队成员都有责任维护代码质量，确保项目始终符合企业级标准。