# 规范遵循检查清单

> **创建时间**: 2025-11-20  
> **规范基准**: repowiki规范体系  
> **适用范围**: IOE-DREAM项目所有开发工作

---

## 📋 检查清单说明

本检查清单基于 `D:\IOE-DREAM\docs\repowiki` 下的权威规范文档制定，确保所有开发工作100%符合企业级开发标准。

---

## 🔴 一级规范：必须遵守（强制执行）

### 编码规范
- [ ] **UTF-8编码**: 所有文件必须使用UTF-8编码（无BOM）
- [ ] **无乱码字符**: 禁止出现 `????`、`涓`、`鏂`、`锟斤拷` 等乱码
- [ ] **包名规范**: 禁止使用 `javax.*`（EE命名空间），必须使用 `jakarta.*`
- [ ] **依赖注入**: 禁止使用 `@Autowired`，必须使用 `@Resource`

### 架构规范
- [ ] **四层架构**: 严格遵守 Controller → Service → Manager → DAO 调用链
- [ ] **Controller层**: 禁止直接访问DAO层
- [ ] **Engine层**: 禁止直接访问DAO层
- [ ] **事务管理**: 事务边界必须在Service层

### 编译规范
- [ ] **编译成功**: 所有代码必须能够成功编译
- [ ] **无编译错误**: 编译错误数量必须为0
- [ ] **类型定义**: 所有类型必须正确定义
- [ ] **方法签名**: 所有方法签名必须正确

---

## 🟡 二级规范：应该遵守（质量保障）

### 代码质量
- [ ] **日志规范**: 禁止使用 `System.out.println`，必须使用SLF4J
- [ ] **异常处理**: 必须使用统一异常处理机制
- [ ] **参数验证**: 必须使用 `@Valid` 进行参数校验
- [ ] **权限控制**: 所有接口必须使用 `@SaCheckPermission`

### 数据库规范
- [ ] **表命名**: 使用 `t_{business}_{entity}` 格式
- [ ] **主键命名**: 使用 `{table}_id` 格式
- [ ] **审计字段**: 必须包含 `create_time`, `update_time`, `create_user_id`, `deleted_flag`
- [ ] **软删除**: 使用 `deleted_flag` 字段，禁止物理删除

### 命名规范
- [ ] **包名**: `net.lab1024.sa.{module}.{layer}`
- [ ] **类名**: Controller→{Module}Controller, Service→{Module}Service
- [ ] **方法名**: 查询→get/query/find/list, 新增→add/create/save
- [ ] **变量名**: Boolean→is/has/can开头，集合→复数形式

---

## 🟢 三级规范：建议遵守（最佳实践）

### 代码组织
- [ ] **单一职责**: 每个类和方法只负责一个功能
- [ ] **代码复用**: 避免重复代码，提取公共方法
- [ ] **注释完整**: 重要方法必须有JavaDoc注释
- [ ] **单元测试**: 核心业务逻辑必须有单元测试

### 性能优化
- [ ] **缓存使用**: 合理使用缓存，避免重复查询
- [ ] **批量操作**: 批量操作使用批量方法
- [ ] **索引优化**: 数据库查询使用合适的索引

---

## 🔧 自动化检查工具

### 编译错误检查
```bash
# 执行编译错误检查
./scripts/compile-error-check.sh

# 查看检查报告
cat docs/COMPILE_ERROR_REPORT_*.md
```

### 架构合规检查
```bash
# 执行架构合规检查
./scripts/architecture-compliance-check.sh

# PowerShell版本
.\scripts\architecture-compliance-check.ps1
```

### 规范检查
```bash
# 执行完整规范检查
./scripts/enforce-standards.sh

# 快速检查
./scripts/quick-check.sh
```

---

## 📊 检查结果处理

### 一级规范违规
- **处理方式**: 立即停止工作，强制修复
- **修复工具**: 
  - 包名修复: `find . -name "*.java" -exec sed -i 's/javax\./jakarta\./g' {} \;`
  - 依赖注入修复: `find . -name "*.java" -exec sed -i 's/@Autowired/@Resource/g' {} \;`
  - 编码修复: `./scripts/ultimate-encoding-fix-fixed.sh`

### 二级规范违规
- **处理方式**: 记录技术债务，限期修复
- **修复工具**: 根据具体问题使用相应修复脚本

### 三级规范违规
- **处理方式**: 记录建议，择机优化
- **修复工具**: 代码审查时提出改进建议

---

## ✅ 检查流程

### 开发前检查
1. 运行 `./scripts/dev-standards-check.sh`
2. 运行 `./scripts/pre-work-hook.sh`
3. 确认所有一级规范检查通过

### 开发中检查
1. 实时监控编译状态
2. 使用IDE实时检查
3. 定期运行快速检查脚本

### 开发后检查
1. 运行 `./scripts/post-work-hook.sh`
2. 运行 `./scripts/compile-error-check.sh`
3. 运行 `./scripts/architecture-compliance-check.sh`
4. 确认所有检查通过

### 提交前检查
1. 运行 `./scripts/commit-guard.sh`
2. 运行 `./scripts/enforce-standards.sh`
3. 确认所有检查通过

---

## 📚 相关文档

- **[综合开发规范文档](docs/DEV_STANDARDS.md)** - 完整编码标准
- **[架构设计规范](docs/ARCHITECTURE_STANDARDS.md)** - 四层架构设计标准
- **[技术迁移规范](docs/TECHNOLOGY_MIGRATION.md)** - Spring Boot 3.x 迁移标准
- **[repowiki规范体系](docs/repowiki/)** - 权威规范文档

---

**最后更新**: 2025-11-20  
**维护者**: SmartAdmin规范治理委员会

