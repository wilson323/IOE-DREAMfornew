# ioedream-common-service 模块结构文档

## 服务定位

**服务名称**: ioedream-common-service  
**端口**: 8088  
**服务类型**: 核心公共业务服务

## 核心职责

提供企业级公共业务功能，包括用户认证、组织架构、权限管理、字典配置、系统监控等横切关注点功能。

## 模块划分

### 1. 用户管理模块 (user)

**职责**:
- 用户基本信息管理
- 用户状态管理
- 用户查询和统计

**禁止职责**:
- ❌ 不处理业务特定用户逻辑（如考勤用户、消费用户）
- ❌ 不处理用户业务数据（如考勤记录、消费记录）

**核心实体**: `UserEntity`  
**核心DAO**: `UserDao`  
**核心Service**: `UserService`

### 2. 认证授权模块 (auth)

**职责**:
- 用户登录认证
- Token生成和验证
- 密码加密和验证
- 会话管理

**禁止职责**:
- ❌ 不处理业务权限验证（由业务服务处理）
- ❌ 不处理业务特定认证逻辑

**核心实体**: `AuthTokenEntity`  
**核心DAO**: `AuthTokenDao`  
**核心Service**: `AuthService`

### 3. 权限管理模块 (permission)

**职责**:
- 角色管理
- 权限管理
- 角色权限关联
- 用户角色关联

**禁止职责**:
- ❌ 不处理业务特定权限（如门禁权限、考勤权限）
- ❌ 不处理业务数据权限

**核心实体**: `RoleEntity`, `PermissionEntity`  
**核心DAO**: `RoleDao`, `PermissionDao`  
**核心Service**: `RoleService`, `PermissionService`

### 4. 组织架构模块 (organization)

**职责**:
- 部门管理
- 区域管理
- 设备管理（统一设备实体）
- 组织架构树管理

**禁止职责**:
- ❌ 不处理业务特定设备逻辑（如门禁设备控制、考勤设备配置）
- ❌ 不处理设备协议通信（由device-comm-service处理）

**核心实体**: `DepartmentEntity`, `AreaEntity`, `DeviceEntity`  
**核心DAO**: `DepartmentDao`, `AreaDao`, `DeviceDao`  
**核心Service**: `DepartmentService`, `AreaService`, `CommonDeviceService`

### 5. 字典管理模块 (dict)

**职责**:
- 字典类型管理
- 字典数据管理
- 字典缓存管理

**禁止职责**:
- ❌ 不处理业务特定字典（如考勤状态字典、消费类型字典）

**核心实体**: `DictTypeEntity`, `DictDataEntity`  
**核心DAO**: `DictTypeDao`, `DictDataDao`  
**核心Service**: `DictService`

### 6. 菜单管理模块 (menu)

**职责**:
- 菜单管理
- 菜单权限关联
- 菜单树构建

**禁止职责**:
- ❌ 不处理业务特定菜单逻辑

**核心实体**: `MenuEntity`  
**核心DAO**: `MenuDao`  
**核心Service**: `MenuService`

### 7. 审计日志模块 (audit)

**职责**:
- 操作日志记录
- 登录日志记录
- 日志查询和统计

**禁止职责**:
- ❌ 不处理业务特定审计逻辑（如考勤审计、消费审计）

**核心实体**: `AuditLogEntity`  
**核心DAO**: `AuditLogDao`  
**核心Service**: `AuditLogService`

### 8. 系统配置模块 (config)

**职责**:
- 系统参数配置
- 配置缓存管理
- 配置热更新

**禁止职责**:
- ❌ 不处理业务特定配置（如考勤规则配置、消费规则配置）

**核心实体**: `ConfigEntity`  
**核心DAO**: `ConfigDao`  
**核心Service**: `ConfigService`

### 9. 通知管理模块 (notification)

**职责**:
- 通知发送
- 通知模板管理
- 通知记录管理

**禁止职责**:
- ❌ 不处理业务特定通知逻辑（如考勤提醒、消费通知）

**核心实体**: `NotificationEntity`  
**核心DAO**: `NotificationDao`  
**核心Service**: `NotificationService`

### 10. 定时任务模块 (scheduler)

**职责**:
- 定时任务管理
- 任务调度
- 任务执行记录

**禁止职责**:
- ❌ 不处理业务特定任务逻辑（如考勤统计任务、消费结算任务）

**核心实体**: `SchedulerJobEntity`  
**核心DAO**: `SchedulerJobDao`  
**核心Service**: `SchedulerJobService`

### 11. 监控告警模块 (monitor)

**职责**:
- 系统监控指标收集
- 告警规则管理
- 告警通知

**禁止职责**:
- ❌ 不处理业务特定监控（如考勤异常监控、消费异常监控）

**核心实体**: `MonitorMetricEntity`  
**核心DAO**: `MonitorMetricDao`  
**核心Service**: `MonitorService`

## 模块依赖关系

```
user ← auth (用户认证依赖用户信息)
permission ← user (权限管理依赖用户)
organization ← user (组织架构依赖用户)
audit ← user (审计日志依赖用户)
notification ← user (通知管理依赖用户)
scheduler ← config (定时任务依赖配置)
monitor ← config (监控告警依赖配置)
```

## 与其他服务的关系

### 与业务服务的关系

- **提供公共服务**: 所有业务服务依赖common-service获取用户、组织、权限等基础信息
- **不处理业务逻辑**: common-service不处理业务特定逻辑，只提供通用功能

### 与device-comm-service的关系

- **设备实体管理**: common-service管理统一设备实体（DeviceEntity）
- **设备协议通信**: device-comm-service处理设备协议适配和通信
- **职责分离**: common-service负责设备CRUD，device-comm-service负责设备通信

### 与biometric-service的关系

- **用户信息提供**: common-service提供用户和组织信息
- **模板管理**: biometric-service管理生物模板，依赖common-service的用户信息

## 数据访问规范

### DAO层规范

- ✅ 统一使用`@Mapper`注解
- ✅ 统一使用`Dao`后缀命名
- ✅ 继承`BaseMapper<Entity>`
- ✅ 查询方法使用`@Transactional(readOnly = true)`
- ❌ 禁止使用`@Repository`注解
- ❌ 禁止使用`Repository`后缀

### 依赖注入规范

- ✅ 统一使用`@Resource`注解
- ❌ 禁止使用`@Autowired`注解

## 缓存策略

### 多级缓存架构

- **L1本地缓存**: Caffeine本地缓存（5分钟TTL）
- **L2 Redis缓存**: 分布式缓存（30分钟TTL）
- **L3网关缓存**: 服务间调用缓存（10分钟TTL）

### 缓存域配置

- **用户信息**: L1=1000, L2=1h
- **权限信息**: L1=2000, L2=30m
- **字典数据**: L1=500, L2=2h
- **配置信息**: L1=500, L2=2h

## 版本信息

- **文档版本**: 1.0.0
- **创建日期**: 2025-01-30
- **最后更新**: 2025-01-30
- **维护团队**: IOE-DREAM架构团队
