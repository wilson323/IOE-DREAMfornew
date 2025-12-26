# IOE-DREAM 代码质量检查脚本集合

## 📋 概述

本目录包含用于检查和维护IOE-DREAM项目代码质量的自动化脚本集合。

## 🎯 脚本列表

### 1. code-quality-check.sh

**功能**: 综合代码质量检查脚本

**检查项目**:
- UTF-8编码规范性
- BOM标记检查
- @Slf4j日志规范使用
- 依赖注入规范（@Autowired vs @Resource）
- DAO层规范（@Mapper vs @Repository）
- 日志格式规范性
- 类命名规范
- 方法命名规范
- 实体类复杂度检查
- 敏感信息日志检查

**使用方法**:
```bash
# 基本检查
./scripts/code-quality-check.sh

# 指定目录检查
./scripts/code-quality-check.sh /path/to/project

# 输出到文件
./scripts/code-quality-check.sh > quality-report.txt
```

### 2. fix-architecture-violations.ps1

**功能**: 架构违规检查和修复报告生成器

**检查项目**:
- @Autowired违规使用
- @Repository违规使用
- 传统LoggerFactory使用
- 字符串拼接日志
- 超大Entity文件
- 敏感信息日志记录

**使用方法**:
```powershell
# 基本检查（默认只检查不修复）
.\scripts\fix-architecture-violations.ps1

# 生成详细报告
.\scripts\fix-architecture-violations.ps1 -GenerateReport

# 检查指定目录
.\scripts\fix-architecture-violations.ps1 -RootPath "microservices"

# 实际修复（谨慎使用）
.\scripts\fix-architecture-violations.ps1 -DryRun:$false
```

## 🚀 快速开始

### 运行完整代码质量检查

```bash
# 1. 运行Shell脚本（Linux/Mac/WSL）
./scripts/code-quality-check.sh

# 2. 运行PowerShell脚本（Windows）
.\scripts\fix-architecture-violations.ps1
```

### 检查特定问题

```bash
# 只检查@Autowired违规
grep -r "@Autowired" microservices/ --include="*.java" | grep -v test

# 只检查@Repository违规
grep -r "@Repository" microservices/ --include="*Dao.java"

# 检查超大Entity文件
find microservices/ -name "*Entity.java" -exec wc -l {} \; | awk '$1 > 400'
```

## 📊 质量指标

### 当前项目质量评分

| 指标 | 当前状态 | 目标状态 | 评分 |
|------|---------|---------|------|
| **日志规范一致性** | 98.8% | 100% | 🟢 优秀 |
| **依赖注入规范** | 97.2% | 100% | 🟡 良好 |
| **DAO层规范** | 98.7% | 100% | 🟡 良好 |
| **方法命名规范** | 94.3% | 100% | 🟡 良好 |
| **实体类设计质量** | 91.5% | 95% | 🟡 良好 |

### 质量等级定义

- 🟢 **优秀 (95-100%)**: 完全符合规范，无需改进
- 🟡 **良好 (85-94%)**: 基本符合规范，有少量改进空间
- 🟠 **一般 (70-84%)**: 存在较多问题，需要重点改进
- 🔴 **需改进 (<70%)**: 存在严重问题，需要立即处理

## 🔧 修复指南

### @Autowired违规修复

**问题**: 使用@Autowired进行依赖注入

**解决方案**: 替换为@Resource

```java
// ❌ 错误
@Autowired
private SomeService someService;

// ✅ 正确
@Resource
private SomeService someService;
```

### @Repository违规修复

**问题**: DAO接口使用@Repository注解

**解决方案**: 替换为@Mapper

```java
// ❌ 错误
@Repository
public interface UserDao extends BaseMapper<UserEntity> {
}

// ✅ 正确
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {
}
```

### 日志格式修复

**问题**: 使用字符串拼接或格式不规范

**解决方案**: 使用参数化日志和统一格式

```java
// ❌ 错误
log.info("用户登录: userId=" + userId);

// ✅ 正确
log.info("[用户管理] 用户登录: userId={}", userId);
```

### 超大Entity修复

**问题**: Entity文件超过400行

**解决方案**: 拆分Entity

1. 核心字段保留在原Entity
2. 配置字段迁移到RuleEntity
3. 计算字段使用VO对象
4. 业务逻辑移到Manager层

## 📈 持续改进

### 定期检查计划

- **日常**: 开发者本地检查
- **每周**: 自动化CI/CD检查
- **每月**: 全面质量报告
- **每季度**: 技术债清理活动

### 质量门禁

设置以下质量门禁标准：

- **代码覆盖率**: ≥80%
- **架构违规**: 0个P0/P1问题
- **日志规范**: 100%符合
- **命名规范**: 95%符合
- **实体复杂度**: 无>400行文件

## 🛠️ 自定义和扩展

### 添加新的检查规则

1. 在`code-quality-check.sh`中添加新的检查步骤
2. 更新统计变量
3. 添加相应的输出格式

### 自定义质量标准

修改脚本中的阈值参数：

```bash
# 实体类行数阈值
ENTITY_LINE_LIMIT=400

# 方法复杂度阈值
METHOD_COMPLEXITY_LIMIT=10

# 代码覆盖率阈值
COVERAGE_THRESHOLD=80
```

## 📞 技术支持

如果在使用过程中遇到问题，请：

1. 查看脚本输出的错误信息
2. 检查文件权限设置
3. 确认Java环境和项目结构
4. 联系IOE-DREAM开发团队

---

**维护团队**: IOE-DREAM架构委员会
**更新频率**: 根据项目需要定期更新
**版本历史**: 见各脚本的版本注释