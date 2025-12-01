# IOE-DREAM 架构符合性检查指南

> **创建时间**: 2025-11-20  
> **脚本版本**: v1.0.0  
> **规范基准**: repowiki四层架构规范

---

## 📋 概述

架构符合性检查脚本用于自动化检查项目是否符合repowiki四层架构规范，确保代码质量和架构一致性。

---

## 🚀 快速开始

### Windows环境（推荐）

```powershell
# 检查整个项目
.\scripts\architecture-compliance-check.ps1

# 检查特定模块
.\scripts\architecture-compliance-check.ps1 -CheckPath "sa-admin\src\main\java\net\lab1024\sa\admin\module\consume"
```

### Linux/Mac环境

```bash
# 检查整个项目
./scripts/architecture-compliance-check.sh

# 检查特定模块
./scripts/architecture-compliance-check.sh sa-admin/src/main/java/net/lab1024/sa/admin/module/consume
```

---

## 🔍 检查项说明

### 1. Controller层DAO访问检查（禁止）

**检查内容**: Controller层是否直接注入或访问DAO

**规范要求**: 
- ❌ **禁止**: Controller直接访问DAO
- ✅ **必须**: Controller只能调用Service层

**违规示例**:
```java
@RestController
public class ConsumeController {
    @Resource
    private ConsumeRecordDao consumeRecordDao;  // ❌ 违规
}
```

**正确示例**:
```java
@RestController
public class ConsumeController {
    @Resource
    private ConsumeService consumeService;  // ✅ 正确
}
```

---

### 2. Service层DAO访问检查（建议优化）

**检查内容**: Service层是否直接注入DAO（允许，但建议通过Manager）

**规范要求**: 
- ⚠️ **允许**: Service层可以直接访问DAO
- ✅ **建议**: 复杂业务逻辑通过Manager层访问DAO

**注意**: 继承`ServiceImpl<Dao, Entity>`的情况是MyBatis-Plus标准用法，不视为违规。

---

### 3. Engine层DAO访问检查（禁止）

**检查内容**: Engine层是否直接注入或访问DAO

**规范要求**: 
- ❌ **禁止**: Engine直接访问DAO
- ✅ **必须**: Engine通过Service层访问数据

**违规示例**:
```java
@Component
public class OrderingMode {
    @Resource
    private ProductDao productDao;  // ❌ 违规
}
```

**正确示例**:
```java
@Component
public class OrderingMode {
    @Resource
    private ProductService productService;  // ✅ 正确
}
```

---

### 4. 依赖注入规范检查（必须）

**检查内容**: 是否使用@Autowired（禁止）

**规范要求**: 
- ❌ **禁止**: 使用@Autowired
- ✅ **必须**: 使用@Resource

**违规示例**:
```java
@Service
public class ConsumeService {
    @Autowired  // ❌ 违规
    private ConsumeManager consumeManager;
}
```

**正确示例**:
```java
@Service
public class ConsumeService {
    @Resource  // ✅ 正确
    private ConsumeManager consumeManager;
}
```

---

### 5. 命名规范检查

**检查内容**: 类名是否符合repowiki命名规范

**规范要求**:
- Controller: `{Module}Controller`
- Service: `{Module}Service` 或 `{Module}ServiceImpl`
- Manager: `{Module}Manager`
- DAO: `{Module}Dao`

---

### 6. 冗余文件检查

**检查内容**: 是否存在备份文件（.backup, .bak, .old, .tmp）

**规范要求**: 
- ❌ **禁止**: 提交备份文件到代码库
- ✅ **建议**: 定期清理临时文件

---

### 7. 重复类定义检查

**检查内容**: 是否存在重复的类定义

**规范要求**: 
- ❌ **禁止**: 同一类在多个位置定义
- ✅ **必须**: 统一使用一个实现

---

### 8. 编码问题检查

**检查内容**: 文件是否包含BOM字符

**规范要求**: 
- ❌ **禁止**: 文件包含BOM字符（\ufeff）
- ✅ **必须**: 使用UTF-8 without BOM编码

---

## 📊 检查报告

### 报告位置

检查完成后，会在`docs/`目录下生成报告文件：

```
docs/ARCHITECTURE_COMPLIANCE_REPORT_YYYYMMDD_HHMMSS.md
```

### 报告内容

报告包含以下内容：
1. **检查结果汇总**: 总体统计信息
2. **详细检查结果**: 每个检查项的详细结果
3. **检查项清单**: 所有检查项的通过状态
4. **修复建议**: 针对发现问题的修复建议

---

## 🔧 集成到CI/CD

### GitHub Actions示例

```yaml
name: Architecture Compliance Check

on: [push, pull_request]

jobs:
  architecture-check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Run Architecture Compliance Check
        run: |
          chmod +x scripts/architecture-compliance-check.sh
          ./scripts/architecture-compliance-check.sh
```

### GitLab CI示例

```yaml
architecture-check:
  stage: test
  script:
    - chmod +x scripts/architecture-compliance-check.sh
    - ./scripts/architecture-compliance-check.sh
  only:
    - merge_requests
    - main
```

---

## 📝 使用示例

### 示例1: 检查整个项目

```powershell
.\scripts\architecture-compliance-check.ps1
```

**输出**:
```
==========================================
  IOE-DREAM 架构符合性检查
==========================================

检查路径: smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module
报告文件: docs\ARCHITECTURE_COMPLIANCE_REPORT_20251120_131000.md

[INFO] 检查1: Controller层直接访问DAO...
[SUCCESS] Controller层无DAO访问违规

[INFO] 检查2: Service层直接访问DAO（建议通过Manager）...
[SUCCESS] Service层DAO访问符合最佳实践

...

检查文件数: 156
违规数量: 0
警告数量: 0

✅ 架构符合性检查通过！
```

### 示例2: 检查特定模块

```powershell
.\scripts\architecture-compliance-check.ps1 -CheckPath "sa-admin\src\main\java\net\lab1024\sa\admin\module\consume"
```

---

## 🎯 修复建议

### 修复Controller层DAO访问违规

1. 移除Controller中的DAO注入
2. 在Service层实现数据访问逻辑
3. Controller调用Service层方法

### 修复依赖注入违规

1. 将`@Autowired`改为`@Resource`
2. 确保导入正确的包：`jakarta.annotation.Resource`

### 修复编码问题

1. 使用编辑器重新保存文件为UTF-8 without BOM
2. 或使用编码转换工具

---

## 📚 相关文档

- [repowiki四层架构详解](.qoder/repowiki/zh/content/后端架构/四层架构详解/四层架构详解.md)
- [Service层规范](.qoder/repowiki/zh/content/后端架构/四层架构详解/Service层/Service层.md)
- [全局一致性深度分析报告](docs/GLOBAL_CONSISTENCY_DEEP_ANALYSIS.md)

---

## 🔄 持续改进

### 定期检查

建议在以下时机执行检查：
- 每次提交代码前
- 代码审查时
- 定期（每周/每月）全面检查

### 自动化集成

将检查脚本集成到：
- Pre-commit Hook
- CI/CD流水线
- 代码审查流程

---

**最后更新**: 2025-11-20  
**维护人员**: SmartAdmin规范治理委员会

