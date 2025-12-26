# IOE-DREAM SonarQube 代码质量分析配置指南

## 📋 概述

IOE-DREAM项目已配置企业级SonarQube代码质量分析，重点关注：
- **代码覆盖率 ≥ 80%**
- **重复代码率 ≤ 3%**
- **代码复杂度控制**
- **安全漏洞检测**
- **维护性评级**

## 🚀 快速开始

### 1. 本地代码分析

```bash
# 分析所有模块
.\scripts\sonar-analysis.ps1

# 分析特定模块
.\scripts\sonar-analysis.ps1 -Module microservices-common

# 跳过测试执行（更快）
.\scripts\sonar-analysis.ps1 -SkipTests
```

### 2. CI/CD模式分析

```bash
# 自动上传到SonarQube服务器
.\scripts\sonar-analysis.ps1 -CI -SonarUrl "http://your-sonar-server:9000"
```

### 3. 使用增强配置

```bash
# 使用增强版配置文件
mvn clean verify sonar:sonar -Dsonar.project.properties=sonar-project-enhanced.properties
```

## 📊 质量标准

### 覆盖率要求

| 层级 | 行覆盖率 | 分支覆盖率 | 说明 |
|------|---------|-----------|------|
| **总体** | ≥80% | ≥75% | 项目整体标准 |
| **Controller层** | ≥85% | ≥80% | 接口层要求高 |
| **Service层** | ≥90% | ≥85% | 核心业务逻辑要求最高 |
| **Manager层** | ≥85% | ≥80% | 业务编排层 |
| **DAO层** | ≥80% | ≥75% | 数据访问层 |
| **公共模块** | ≥90% | ≥85% | 基础组件要求最高 |

### 质量门禁

- ✅ **覆盖率**: ≥80%
- ✅ **重复代码**: ≤3%
- ✅ **可维护性**: B级或以上
- ✅ **可靠性**: B级或以上
- ✅ **安全性**: B级或以上
- ✅ **新增Bug**: ≤5个
- ✅ **安全漏洞**: 0个

## 🔧 配置文件说明

### 1. 主配置文件

- `sonar-project.properties` - 标准配置
- `sonar-project-enhanced.properties` - 增强版配置（推荐）

### 2. JaCoCo配置

- `jacoco-enhanced-config.xml` - 详细的JaCoCo配置
- 已集成到`pom.xml`的`jacoco-maven-plugin`

### 3. 质量门禁配置

- `sonar-quality-gate.json` - 标准质量门禁
- `sonar-quality-gate-enhanced.json` - 增强版质量门禁（推荐）

## 📈 报告查看

### 1. HTML报告

位置: `target/site/jacoco/index.html`

### 2. SonarQube仪表板

访问: http://localhost:9000/dashboard?id=ioedream-microservices

### 3. 覆盖率趋势

位置: `target/sonar-reports/analysis-summary-*.md`

## 🎯 模块特定标准

| 模块 | 覆盖率要求 | 特殊要求 |
|------|-----------|---------|
| `microservices-common` | 90% | 基础组件，要求最高 |
| `ioedream-common-service` | 85% | 核心服务 |
| `ioedream-access-service` | 85% | 安全敏感，0漏洞 |
| `ioedream-consume-service` | 85% | 涉及资金，A级可靠性 |
| `ioedream-attendance-service` | 85% | 业务核心 |
| `ioedream-video-service` | 80% | 设备接口复杂 |
| `ioedream-device-comm-service` | 80% | 协议适配 |
| `ioedream-oa-service` | 80% | 工作流复杂 |

## 🚨 常见问题

### 1. 覆盖率低怎么办？

- **检查排除配置**: 确保没有误排重要代码
- **增加单元测试**: 覆盖核心业务逻辑
- **使用JaCoCo报告**: 查看未覆盖的具体代码

### 2. 重复代码率高怎么处理？

- **提取公共方法**: 将重复逻辑抽取为公共方法
- **使用工具类**: 创建工具类处理通用功能
- **继承和组合**: 使用设计模式减少重复

### 3. 安全漏洞如何修复？

- **查看具体漏洞**: SonarQube会提供详细说明
- **遵循安全编码规范**: 使用安全API
- **使用依赖检查**: 更新有漏洞的依赖

## 📝 最佳实践

### 1. 开发阶段

```bash
# 快速检查本地代码质量
mvn clean compile jacoco:prepare-agent test jacoco:report

# 查看覆盖率报告
start target/site/jacoco/index.html
```

### 2. 提交前检查

```bash
# 完整质量检查
mvn clean verify

# 仅运行质量门禁检查
mvn sonar:sonar -Dsonar.qualitygate.wait=true
```

### 3. 持续集成

在CI/CD流水线中加入：

```yaml
# GitHub Actions示例
- name: Run SonarQube Analysis
  run: |
    mvn clean verify sonar:sonar \
      -Dsonar.projectKey=ioedream-microservices \
      -Dsonar.organization=your-org \
      -Dsonar.host.url=https://sonarcloud.io \
      -Dsonar.login=$SONAR_TOKEN
```

## 🔍 自定义规则

项目已配置IOE-DREAM特定规则：

### 架构合规性

- Controller必须使用@Resource注解
- DAO必须使用@Mapper注解
- 禁止跨层访问
- 遵循四层架构规范

### 安全检查

- 禁止硬编码密码
- SQL注入防护
- XSS攻击防护
- 关键操作日志记录

### 性能优化

- 避免数据库全表扫描
- 实施多级缓存策略
- 优化查询性能

## 📚 相关资源

- [SonarQube官方文档](https://docs.sonarqube.org/)
- [JaCoCo文档](https://www.eclemma.org/jacoco/)
- [Maven JaCoCo插件](https://www.mojohaus.org/jacoco/)
- [项目架构规范](./CLAUDE.md)

---

**维护团队**: IOE-DREAM架构委员会
**更新日期**: 2025-12-20
**配置版本**: v2.0.0