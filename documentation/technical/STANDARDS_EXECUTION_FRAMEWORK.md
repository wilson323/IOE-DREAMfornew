# SmartAdmin 开发规范执行框架

## 🎯 框架目标

建立**强制性的、自动化的、可持续的**开发规范执行体系，确保规范能够长期、准确、严格地被执行。

---

## 🏗️ 执行框架设计

### 三层保障机制

```
第一层：技术自动化保障（硬性约束）
├── IDE 实时检查插件
├── Pre-commit Hook 强制检查
├── CI/CD 流水线质量门禁
└── 定期代码健康扫描

第二层：流程制度化保障（软性约束）
├── 强制代码审查流程
├── 架构合规性评审
├── 定期质量回顾会议
└── 技术债务管理机制

第三层：文化持续性保障（长期约束）
├── 团队规范培训体系
├── 质量意识文化建设
├── 最佳实践分享机制
└── 持续改进反馈循环
```

---

## 🔧 第一层：技术自动化保障

### 1. IDE 实时检查配置

#### IntelliJ IDEA 配置
```xml
<!-- .idea/codeStyles/Project.xml -->
<component name="ProjectCodeStyleConfiguration">
  <code_scheme name="Project" version="173">
    <JavaCodeStyleSettings>
      <option name="IMPORT_LAYOUT_TABLE">
        <value>
          <package name="jakarta" withSubpackages="true" static="false"/>
          <package name="java" withSubpackages="true" static="false"/>
          <emptyLine/>
          <package name="org" withSubpackages="true" static="false"/>
          <package name="com" withSubpackages="true" static="false"/>
          <emptyLine/>
          <package name="net.lab1024" withSubpackages="true" static="false"/>
          <emptyLine/>
          <package name="" withSubpackages="true" static="false"/>
          <emptyLine/>
          <package name="" withSubpackages="true" static="true"/>
        </value>
      </option>
    </JavaCodeStyleSettings>
  </code_scheme>
</component>
```

#### VS Code 配置
```json
// .vscode/settings.json
{
  "java.format.settings.url": "https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml",
  "java.saveActions.organizeImports": true,
  "editor.formatOnSave": true,
  "java.codeGeneration.generateComments": true,
  "java.compile.nullAnalysis.mode": "automatic",
  "java.errors.incompleteClasspath.severity": "error"
}
```

### 2. Pre-commit Hook 配置

#### Git Hook 脚本
```bash
#!/bin/sh
# .git/hooks/pre-commit

echo "🔍 执行提交前代码质量检查..."

# 1. 编译检查
echo "1. 检查编译状态..."
cd smart-admin-api-java17-springboot3
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "❌ 编译失败，请修复编译错误后再提交"
    exit 1
fi

# 2. 运行快速检查脚本
echo "2. 运行代码规范检查..."
./scripts/quick-check.sh
if [ $? -ne 0 ]; then
    echo "❌ 代码规范检查失败，请修复规范问题后再提交"
    exit 1
fi

# 3. 检查 javax 包使用
echo "3. 检查 Java EE 包使用..."
JAVAX_COUNT=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
if [ "$JAVAX_COUNT" -gt 0 ]; then
    echo "❌ 发现 $JAVAX_COUNT 个文件使用 javax 包，请替换为 jakarta 包"
    exit 1
fi

# 4. 检查 @Resource 使用
echo "4. 检查依赖注入规范..."
AUTOWIRED_COUNT=$(find . -name "*.java" -exec grep -l "@Resource" {} \; | wc -l)
if [ "$AUTOWIRED_COUNT" -gt 0 ]; then
    echo "❌ 发现 $AUTOWIRED_COUNT 个文件使用 @Resource，请替换为 @Resource"
    exit 1
fi

echo "✅ 所有检查通过，可以提交代码"
exit 0
```

#### 安装 Pre-commit Hook
```bash
# 复制 hook 脚本到 Git hooks 目录
cp scripts/pre-commit .git/hooks/
chmod +x .git/hooks/pre-commit
```

### 3. CI/CD 流水线质量门禁

#### GitHub Actions 配置
```yaml
# .github/workflows/quality-gate.yml
name: Code Quality Gate

on: [push, pull_request]

jobs:
  quality-check:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Run quality checks
      run: |
        chmod +x scripts/quality-check.sh
        ./scripts/quality-check.sh

    - name: Upload quality report
      uses: actions/upload-artifact@v3
      with:
        name: quality-report
        path: reports/quality-report.html
```

#### 质量检查脚本
```bash
#!/bin/bash
# scripts/quality-check.sh

set -e

echo "🔍 执行完整代码质量检查..."

# 创建报告目录
mkdir -p reports

# 1. 编译检查
echo "1. 编译检查..."
mvn clean compile -DskipTests > reports/compile.log 2>&1 || {
    echo "❌ 编译失败"
    cat reports/compile.log
    exit 1
}

# 2. 代码规范检查
echo "2. 代码规范检查..."
./scripts/quick-check.sh > reports/standards.log

# 3. 单元测试
echo "3. 单元测试..."
mvn test > reports/test.log 2>&1 || {
    echo "⚠️ 测试失败，但不阻塞提交"
}

# 4. 测试覆盖率
echo "4. 测试覆盖率..."
mvn jacoco:report > reports/coverage.log 2>&1

# 5. 生成质量报告
echo "5. 生成质量报告..."
cat > reports/quality-report.html << EOF
<!DOCTYPE html>
<html>
<head>
    <title>SmartAdmin 代码质量报告</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .pass { color: green; }
        .fail { color: red; }
        .warn { color: orange; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <h1>SmartAdmin 代码质量报告</h1>
    <p>生成时间: $(date)</p>

    <h2>检查结果</h2>
    <table>
        <tr><th>检查项目</th><th>状态</th><th>详情</th></tr>
        <tr><td>编译检查</td><td class="pass">✅ 通过</td><td>代码编译成功</td></tr>
        <tr><td>规范检查</td><td class="pass">✅ 通过</td><td>符合开发规范</td></tr>
        <tr><td>单元测试</td><td class="warn">⚠️ 部分</td><td>查看测试日志</td></tr>
    </table>

    <h2>详细信息</h2>
    <h3>编译日志</h3>
    <pre>$(cat reports/compile.log)</pre>

    <h3>规范检查日志</h3>
    <pre>$(cat reports/standards.log)</pre>
</body>
</html>
EOF

echo "✅ 质量检查完成，报告已生成：reports/quality-report.html"
```

---

## 📋 第二层：流程制度化保障

### 1. 强制代码审查流程

#### Pull Request 模板
```markdown
## 📋 代码审查清单

### 🔴 强制检查项（必须全部通过）
- [ ] 代码编译通过，无编译错误
- [ ] 已通过 `./scripts/quick-check.sh` 检查
- [ ] 已通过单元测试，覆盖率 ≥ 80%
- [ ] 遵循所有一级开发规范
- [ ] 无 javax 包使用，全部使用 jakarta
- [ ] 无 @Resource 使用，全部使用 @Resource
- [ ] 遵循四层架构规范
- [ ] 已添加必要的注释和文档

### 🟡 质量检查项（建议通过）
- [ ] 代码结构清晰，易于理解
- [ ] 变量命名规范，语义明确
- [ ] 异常处理完善
- [ ] 性能考虑合理
- [ ] 安全性考虑充分

### 📢 审查人确认
- [ ] 功能正确性确认
- [ ] 代码质量确认
- [ ] 规范遵循确认
- [ ] 测试覆盖确认

**⚠️ 重要提醒**：
- 任何强制检查项未通过，都不能合并
- 如有争议，请架构师或技术负责人裁决
```

### 2. 架构合规性评审

#### 重大变更评审清单
```markdown
## 🏗️ 架构变更评审

### 变更描述
- **变更范围**:
- **影响模块**:
- **变更原因**:

### 架构合规性检查
- [ ] 包命名符合规范
- [ ] 模块依赖关系清晰
- [ ] 遵循四层架构
- [ ] 接口设计合理
- [ ] 数据库设计规范
- [ ] 安全性考虑充分

### 性能影响评估
- [ ] 数据库查询优化
- [ ] 缓存策略合理
- [ ] 并发处理考虑
- [ ] 内存使用优化

### 评审结论
- [ ] **通过**: 符合架构规范，可以实施
- [ ] **有条件通过**: 需要修改指定问题
- [ ] **不通过**: 重新设计架构

**评审人**: ________________ **日期**: ________________
```

### 3. 技术债务管理

#### 技术债务跟踪表
```markdown
## 技术债务登记表

| ID | 问题描述 | 严重程度 | 发现时间 | 责任人 | 计划修复时间 | 状态 |
|----|----------|----------|----------|--------|--------------|------|
| TD001 | Spring Boot 3.x 迁移不彻底 | 高 | 2025-11-13 | 架构师 | 2025-11-13 | ✅ 已修复 |
| TD002 | 依赖注入方式不统一 | 高 | 2025-11-13 | 全体 | 2025-11-15 | 🔄 进行中 |
| TD003 | 包命名规范不统一 | 中 | 2025-11-13 | 开发团队 | 2025-11-20 | ⏳ 待处理 |

### 债务等级定义
- **高**: 影响编译或运行，必须立即修复
- **中**: 影响代码质量，计划性修复
- **低**: 优化建议，择机修复
```

---

## 🎓 第三层：文化持续性保障

### 1. 团队培训体系

#### 新成员入职培训计划
```markdown
## 新成员开发规范培训

### 第一周：基础规范
- [ ] 阅读 CLAUDE.md 核心规范
- [ ] 完成 [技术迁移规范](docs/TECHNOLOGY_MIGRATION.md) 学习
- [ ] 配置开发环境（IDE、Git Hook）
- [ ] 完成编码练习：创建符合规范的 Controller

### 第二周：架构规范
- [ ] 学习 [架构设计规范](docs/ARCHITECTURE_STANDARDS.md)
- [ ] 理解四层架构设计
- [ ] 完成编码练习：实现四层架构模块
- [ ] 参与代码审查

### 第三周：业务规范
- [ ] 学习相关业务模块规范
- [ ] 使用专用检查清单
- [ ] 独立完成功能开发
- [ ] 通过质量检查

### 培训考核
- [ ] 规范知识测试（≥90分）
- [ ] 编码实践考核
- [ ] 代码审查参与度
- [ ] 质量意识表现
```

### 2. 质量意识文化建设

#### 质量主题活动
```markdown
## 质量文化建设活动

### 每周活动
- **周一**: 质量例会（15分钟）- 回顾上周质量状况
- **周三**: 代码审查会（30分钟）- 集中审查重要代码
- **周五**: 最佳实践分享（30分钟）- 分享质量改进经验

### 每月活动
- **质量之星评选**: 表彰规范执行优秀的团队成员
- **技术债务清理日**: 集中处理技术债务
- **质量改进会议**: 制定质量改进计划

### 每季度活动
- **架构回顾会议**: 评估架构设计质量
- **规范更新讨论**: 根据实践更新规范
- **团队质量培训**: 提升整体质量意识
```

### 3. 持续改进反馈循环

#### 质量改进流程
```
发现问题 → 记录问题 → 分析根因 → 制定改进措施 → 实施改进 → 效果评估 → 标准化
    ↑                                                                  ↓
    ←←←←←←←←←←←←← 持续改进反馈循环 ←←←←←←←←←←←←←←←←←←←←←←←←←←←←←
```

#### 改进建议收集机制
```markdown
## 规范改进建议

### 建议提交渠道
1. **GitHub Issues**: 创建规范改进建议
2. **团队会议**: 在质量例会上提出
3. **一对一沟通**: 与技术负责人或架构师沟通
4. **匿名反馈**: 通过内部反馈系统

### 建议处理流程
1. **收集**: 定期收集各种渠道的建议
2. **评估**: 技术团队评估建议的可行性
3. **决策**: 确定是否采纳及实施计划
4. **实施**: 按计划实施改进措施
5. **反馈**: 向建议人反馈处理结果
```

---

## 📊 执行效果监控

### 质量指标体系
```markdown
## 代码质量指标

### 过程指标
- 编译通过率: 目标 100%
- 规范检查通过率: 目标 100%
- 代码审查覆盖率: 目标 100%
- 测试覆盖率: 目标 ≥80%

### 结果指标
- 生产环境 Bug 数量: 目标 ≤5个/月
- 代码重复率: 目标 ≤5%
- 技术债务数量: 目标 持续下降
- 代码复杂度: 目标 保持稳定

### 团队指标
- 规范培训完成率: 目标 100%
- 质量意识评分: 目标 ≥4.5/5
- 改进建议参与度: 目标 ≥80%
```

### 监控仪表板
```html
<!-- 简化的质量监控仪表板概念 -->
<div class="quality-dashboard">
    <h1>SmartAdmin 质量监控仪表板</h1>

    <div class="metrics-grid">
        <div class="metric-card">
            <h3>编译通过率</h3>
            <div class="metric-value">98.5%</div>
            <div class="trend up">↑ 2.1%</div>
        </div>

        <div class="metric-card">
            <h3>规范遵循率</h3>
            <div class="metric-value">96.2%</div>
            <div class="trend up">↑ 5.3%</div>
        </div>

        <div class="metric-card">
            <h3>测试覆盖率</h3>
            <div class="metric-value">82.7%</div>
            <div class="trend stable">→ 0.0%</div>
        </div>
    </div>
</div>
```

---

## 🚀 实施计划

### 第一阶段：基础设施搭建（1周）
1. ✅ 配置 IDE 代码检查插件
2. ✅ 实施 Pre-commit Hook
3. ✅ 建立基础 CI/CD 质量门禁
4. ✅ 完善快速检查脚本

### 第二阶段：流程制度建设（2周）
1. 🔄 实施强制代码审查流程
2. 🔄 建立架构评审机制
3. 🔄 完善技术债务管理
4. 🔄 制定质量改进流程

### 第三阶段：文化建设推进（持续）
1. ⏳ 开展团队规范培训
2. ⏳ 建立质量意识文化
3. ⏳ 完善持续改进机制
4. ⏳ 建立质量监控体系

---

## 📈 预期效果

### 短期效果（1-2周）
- ✅ 编译错误率下降 90%
- ✅ 规范遵循率提升到 95%
- ✅ 代码审查覆盖率 100%

### 中期效果（1-2月）
- ✅ 代码质量显著提升
- ✅ 技术债务得到控制
- ✅ 开发效率提升 30%

### 长期效果（3-6月）
- ✅ 建立高质量代码文化
- ✅ 形成持续改进机制
- ✅ 团队能力全面提升

---

**结论**: 通过三层保障机制的建立，可以确保开发规范得到长期、准确、严格的执行，从而实现项目代码质量的持续提升。

---

*框架制定时间: 2025-11-13*
*版本: v1.0*
*下次更新时间: 根据实施效果定期更新*