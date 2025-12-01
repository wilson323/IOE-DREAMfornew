# 开发规范文档

## 📋 核心开发标准

### 🔴 一级规范：必须遵守（强制执行）

#### 包名规范
- **禁止使用**: `javax.*` 包
- **必须使用**: `jakarta.*` 包
- **检查命令**: `find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l`

#### 依赖注入规范
- **禁止使用**: `@Autowired` 注解
- **必须使用**: `@Resource` 注解
- **检查命令**: `find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l`

#### 架构规范
- **严格遵循四层架构**: Controller → Service → Manager → DAO
- **禁止跨层访问**: Controller 不能直接访问 DAO
- **事务边界**: 事务必须在 Service 层管理

#### 实体类规范
- **必须继承**: `net.lab1024.sa.base.common.entity.BaseEntity`
- **审计字段**: 自动包含 create_time, update_time, create_user_id, deleted_flag
- **软删除**: 使用 deleted_flag 字段，禁止物理删除

### 🟡 二级规范：应该遵守（质量保障）

#### 代码质量
- **单元测试覆盖率**: ≥ 80%
- **代码复杂度**: 圈复杂度 ≤ 10
- **重复代码**: 重复率 ≤ 5%

#### 安全规范
- **权限验证**: 所有接口必须添加 `@SaCheckLogin` 和 `@SaCheckPermission`
- **参数校验**: 使用 `@Valid` 进行参数校验
- **异常处理**: 使用 `SmartException` 及其子类

#### 性能规范
- **数据库查询**: 禁止 N+1 查询问题
- **缓存使用**: 合理使用 Redis 缓存
- **分页查询**: 大数据量查询必须分页

### 🟢 三级规范：建议遵守（最佳实践）

#### 编码风格
- **命名规范**: 使用有意义的变量名和方法名
- **注释规范**: 复杂逻辑必须添加注释
- **代码格式**: 使用统一的代码格式化规则

#### 文档规范
- **API文档**: 重要接口需要编写API文档
- **变更记录**: 重要变更需要更新变更日志
- **架构文档**: 复杂模块需要编写架构文档

## 🔧 开发工具和命令

### 编译和测试
```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包项目
mvn clean package -DskipTests
```

### 质量检查
```bash
# 检查包名规范
find . -name "*.java" -exec grep -l "javax\." {} \;

# 检查依赖注入规范
find . -name "*.java" -exec grep -l "@Autowired" {} \;

# 运行完整质量检查
bash scripts/quality-gate.sh
```

### Git工作流
```bash
# 提交前检查
bash scripts/commit-guard.sh

# 任务完成后验证
bash scripts/mandatory-verification.sh
```

## 📊 违规后果

### 一级规范违规
- **立即后果**: 阻塞开发，无法提交代码
- **修复要求**: 必须立即修复所有违规问题
- **验证要求**: 通过完整的质量门禁检查

### 二级规范违规
- **技术债务**: 记录技术债务，限期修复
- **代码审查**: 必须通过代码审查
- **质量扣分**: 影响绩效评估

### 三级规范违规
- **建议记录**: 记录改进建议
- **择机优化**: 在合适时机进行优化
- **知识分享**: 团队内分享最佳实践

---

**重要提醒**: 违反任何一级规范都将导致工作被阻止！