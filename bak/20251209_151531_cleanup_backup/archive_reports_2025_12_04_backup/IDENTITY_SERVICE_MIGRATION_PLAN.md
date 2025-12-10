# Identity-Service 迁移计划

**迁移日期**: 2025-12-02  
**源服务**: `ioedream-identity-service`  
**目标模块**: `microservices-common` + `ioedream-common-service`  
**状态**: 🟡 进行中

---

## 📋 功能对比矩阵

### Controller层（4个）

| Controller | API端点数量 | 目标位置 | 状态 |
|-----------|------------|---------|------|
| **UserController** | 15个 | `ioedream-common-service/controller/UserController` | ⏳ 待迁移 |
| **AuthController** | 5个 | `ioedream-common-service/controller/AuthController` | ⏳ 待迁移 |
| **RoleController** | 8个 | `ioedream-common-service/controller/RoleController` | ⏳ 待迁移 |
| **PermissionController** | 9个 | `ioedream-common-service/controller/PermissionController` | ⏳ 待迁移 |

**总计**: 37个API端点

### Service层（4个）

| Service | 方法数量 | 目标位置 | 已有功能 | 缺失功能 | 状态 |
|---------|---------|---------|---------|---------|------|
| **UserService** | 12个 | `microservices-common/security/service/UserService` | ✅ UserEntity已存在 | ⏳ Service实现待迁移 | ⏳ 待迁移 |
| **AuthenticationService** | 8个 | `microservices-common/security/service/AuthenticationService` | ❌ 无 | ⏳ 全部待迁移 | ⏳ 待迁移 |
| **RoleService** | 8个 | `microservices-common/security/service/RoleService` | ✅ CommonRbacService部分功能 | ⏳ 完整Service待迁移 | ⏳ 待迁移 |
| **PermissionService** | 9个 | `microservices-common/security/service/PermissionService` | ✅ CommonRbacService部分功能 | ⏳ 完整Service待迁移 | ⏳ 待迁移 |

### DAO层（7个）

| DAO | 方法数量 | 目标位置 | 已有功能 | 状态 |
|-----|---------|---------|---------|------|
| **UserDao** | 基础CRUD | `microservices-common/security/dao/UserDao` | ✅ 已存在 | ✅ 无需迁移 |
| **RbacRoleDao** | 1个自定义方法 | `microservices-common/security/dao/RoleDao` | ✅ 已存在 | ⏳ 补充自定义方法 |
| **RbacUserRoleDao** | 2个自定义方法 | `microservices-common/security/dao/UserRoleDao` | ✅ 已存在 | ⏳ 补充自定义方法 |
| **RbacResourceDao** | 3个自定义方法 | `microservices-common/security/dao/PermissionDao` | ✅ 已存在 | ⏳ 补充自定义方法 |
| **RbacRoleResourceDao** | 多个自定义方法 | `microservices-common/security/dao/RolePermissionDao` | ✅ 已存在 | ⏳ 补充自定义方法 |
| **AreaPersonDao** | 1个自定义方法 | `microservices-common/organization/dao/AreaPersonDao` | ✅ 已存在 | ⏳ 补充自定义方法 |
| **EmployeeDeptDao** | 1个自定义方法 | `microservices-common/organization/dao/EmployeeDeptDao` | ✅ 已存在 | ⏳ 补充自定义方法 |

### Entity层

| Entity | 目标位置 | 状态 |
|--------|---------|------|
| **UserEntity** | `microservices-common/security/entity/UserEntity` | ✅ 已存在 |
| **RoleEntity** | `microservices-common/security/entity/RoleEntity` | ✅ 已存在 |
| **PermissionEntity** | `microservices-common/security/entity/PermissionEntity` | ✅ 已存在 |
| **UserRoleEntity** | `microservices-common/security/entity/UserRoleEntity` | ✅ 已存在 |
| **RolePermissionEntity** | `microservices-common/security/entity/RolePermissionEntity` | ✅ 已存在 |
| **AreaPersonEntity** | `microservices-common/organization/entity/AreaPersonEntity` | ✅ 已存在 |

### VO/DTO层

| VO/DTO | 目标位置 | 状态 |
|--------|---------|------|
| **LoginRequest** | `microservices-common/security/domain/vo/LoginRequest` | ⏳ 待迁移 |
| **LoginResponse** | `microservices-common/security/domain/vo/LoginResponse` | ⏳ 待迁移 |
| **RefreshTokenRequest** | `microservices-common/security/domain/vo/RefreshTokenRequest` | ⏳ 待迁移 |

---

## 🎯 迁移步骤

### 阶段1: 补充DAO层自定义方法（预计30分钟）

**任务清单**:
1. ✅ 检查所有DAO接口
2. ⏳ 补充RbacRoleDao的`selectByRoleCode`方法到RoleDao
3. ⏳ 补充RbacUserRoleDao的`getRoleIdsByUserId`和`getRoleCodesByUserId`方法到UserRoleDao
4. ⏳ 补充RbacResourceDao的自定义方法到PermissionDao
5. ⏳ 补充RbacRoleResourceDao的自定义方法到RolePermissionDao
6. ⏳ 补充AreaPersonDao和EmployeeDeptDao的自定义方法

### 阶段2: 迁移Service层（预计2小时）

**任务清单**:
1. ⏳ 迁移UserService到`microservices-common/security/service/UserService`
2. ⏳ 迁移AuthenticationService到`microservices-common/security/service/AuthenticationService`
3. ⏳ 迁移RoleService到`microservices-common/security/service/RoleService`
4. ⏳ 迁移PermissionService到`microservices-common/security/service/PermissionService`
5. ⏳ 调整包名和依赖关系
6. ⏳ 确保使用CommonRbacService的已有功能

### 阶段3: 迁移VO/DTO（预计15分钟）

**任务清单**:
1. ⏳ 迁移LoginRequest到`microservices-common/security/domain/vo/`
2. ⏳ 迁移LoginResponse到`microservices-common/security/domain/vo/`
3. ⏳ 迁移RefreshTokenRequest到`microservices-common/security/domain/vo/`
4. ⏳ 调整包名

### 阶段4: 创建Controller（预计1.5小时）

**任务清单**:
1. ⏳ 创建UserController到`ioedream-common-service/controller/UserController`
2. ⏳ 创建AuthController到`ioedream-common-service/controller/AuthController`
3. ⏳ 创建RoleController到`ioedream-common-service/controller/RoleController`
4. ⏳ 创建PermissionController到`ioedream-common-service/controller/PermissionController`
5. ⏳ 确保所有37个API端点完整实现
6. ⏳ 添加Swagger文档注解

### 阶段5: 验证和测试（预计1小时）

**任务清单**:
1. ⏳ 编译验证
2. ⏳ 功能对比验证
3. ⏳ 创建单元测试
4. ⏳ 创建集成测试

---

## 📊 迁移进度

| 阶段 | 完成度 | 状态 |
|------|--------|------|
| 阶段1: DAO层补充 | 0% | ⏳ 待开始 |
| 阶段2: Service层迁移 | 0% | ⏳ 待开始 |
| 阶段3: VO/DTO迁移 | 0% | ⏳ 待开始 |
| 阶段4: Controller创建 | 0% | ⏳ 待开始 |
| 阶段5: 验证测试 | 0% | ⏳ 待开始 |

**总体进度**: 0%

---

## ⚠️ 注意事项

1. **避免功能重复**: 确保不重复实现CommonRbacService已有的功能
2. **统一使用Entity**: 使用microservices-common中的Entity，不要创建新的
3. **遵循CLAUDE.md规范**: 严格遵循四层架构、依赖注入、DAO命名等规范
4. **包名调整**: 所有迁移代码的包名需要调整为`net.lab1024.sa.common.*`
5. **依赖关系**: Service层依赖Manager层，Controller层依赖Service层

---

## 🔗 相关文件

- **源服务**: `microservices/ioedream-identity-service/`
- **目标公共模块**: `microservices/microservices-common/src/main/java/net/lab1024/sa/common/security/`
- **目标服务**: `microservices/ioedream-common-service/src/main/java/net/lab1024/sa/common/controller/`

---

**最后更新**: 2025-12-02

