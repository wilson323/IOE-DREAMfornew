# 最优七微服务架构重构 - 详细任务清单

## 📋 任务执行原则

**严格遵循 CLAUDE.md 规范**：
- 四层架构：Controller → Service → Manager → DAO
- 依赖注入：统一使用 @Resource（禁止 @Autowired）
- DAO命名：统一使用 @Mapper + Dao后缀（禁止 @Repository）
- 包名规范：统一使用 jakarta.*（禁止 javax.*）
- 连接池：统一使用 Druid（禁止 HikariCP）
- 服务调用：统一通过 GatewayServiceClient（禁止 FeignClient 直连）

---

## Phase 1: 准备阶段 (预计5个工作日)

### 1.1 功能扫描与对比矩阵
- [ ] 1.1.1 扫描 auth-service 所有 API 端点和功能
  - **注意**：记录所有 Controller 方法签名、Service 接口、DAO 方法
  - **验证**：生成 API 端点清单文档
- [ ] 1.1.2 扫描 identity-service 所有 API 端点和功能
  - **注意**：特别关注 RBAC 权限相关功能
  - **验证**：生成功能对比矩阵
- [ ] 1.1.3 扫描 notification-service 所有 API 端点和功能
  - **注意**：包括邮件、短信、站内消息模板
  - **验证**：确认通知渠道完整性
- [ ] 1.1.4 扫描 audit-service 所有 API 端点和功能
  - **注意**：审计日志记录和查询功能
  - **验证**：确认审计功能完整性
- [ ] 1.1.5 扫描 monitor-service 所有 API 端点和功能
  - **注意**：系统监控、健康检查、告警功能
  - **验证**：确认监控指标完整性
- [ ] 1.1.6 扫描 scheduler-service 所有 API 端点和功能
  - **注意**：定时任务、Cron 表达式、任务日志
  - **验证**：确认调度功能完整性
- [ ] 1.1.7 扫描 system-service 所有 API 端点和功能
  - **注意**：字典管理、配置管理、菜单管理
  - **验证**：确认系统配置完整性
- [ ] 1.1.8 扫描 config-service 所有 API 端点和功能
  - **注意**：Nacos 配置、环境管理
  - **验证**：确认配置中心功能
- [ ] 1.1.9 扫描 enterprise-service 所有 API 端点和功能
  - **注意**：企业信息、组织架构、文档管理
  - **验证**：确认企业功能完整性
- [ ] 1.1.10 扫描 infrastructure-service 所有 API 端点和功能
  - **注意**：基础设施、资产管理
  - **验证**：确认基础设施功能
- [ ] 1.1.11 扫描 device-service 所有 API 端点和功能
  - **注意**：设备协议、连接管理、数据采集
  - **验证**：确认设备通讯功能

### 1.2 创建功能对比矩阵文档
- [ ] 1.2.1 创建 `FUNCTION_COMPARISON_MATRIX.md`
  - **内容**：源服务 → 目标服务功能映射表
  - **格式**：表格形式，标记已迁移/待迁移/不迁移
- [ ] 1.2.2 识别重复功能并标记
  - **注意**：多个服务实现相同功能的情况
  - **决策**：选择最完善的实现作为迁移源
- [ ] 1.2.3 识别缺失功能并标记
  - **注意**：目标服务中尚未实现的功能
  - **决策**：确定功能实现优先级

### 1.3 测试基线建立
- [ ] 1.3.1 收集现有单元测试用例
  - **注意**：统计当前测试覆盖率
- [ ] 1.3.2 创建集成测试用例模板
  - **注意**：覆盖所有 API 端点
- [ ] 1.3.3 创建性能测试基线
  - **注意**：记录当前响应时间和吞吐量

---

## Phase 2: 公共模块整合 (预计10个工作日)

### 2.1 auth-service 整合 (Day 1-2)

#### 2.1.1 Entity/DAO 层迁移
- [ ] 2.1.1.1 迁移 AuthEntity 到 microservices-common
  - **目标路径**：`net.lab1024.sa.common.auth.entity`
  - **规范**：继承 BaseEntity，使用 @TableName
- [ ] 2.1.1.2 迁移 AuthDao 到 microservices-common
  - **目标路径**：`net.lab1024.sa.common.auth.dao`
  - **规范**：使用 @Mapper 注解，继承 BaseMapper
  - **禁止**：@Repository 注解

#### 2.1.2 Manager 层迁移
- [ ] 2.1.2.1 迁移 AuthManager 到 microservices-common
  - **目标路径**：`net.lab1024.sa.common.auth.manager`
  - **规范**：使用 @Component 注解
  - **职责**：JWT 生成、Token 验证、密码加密

#### 2.1.3 Service 层迁移
- [ ] 2.1.3.1 迁移 AuthService 接口到 microservices-common
  - **目标路径**：`net.lab1024.sa.common.auth.service`
- [ ] 2.1.3.2 创建 AuthServiceImpl 到 ioedream-common-service
  - **目标路径**：`net.lab1024.sa.common.auth.service.impl`
  - **规范**：使用 @Service + @Transactional

#### 2.1.4 Controller 层创建
- [ ] 2.1.4.1 创建 AuthController 到 ioedream-common-service
  - **目标路径**：`net.lab1024.sa.common.auth.controller`
  - **API 路径**：保持原有路径 `/api/v1/auth/*`
  - **规范**：使用 @RestController + @Tag

#### 2.1.5 验证
- [ ] 2.1.5.1 编译验证：0 错误 0 警告
- [ ] 2.1.5.2 单元测试：覆盖率 ≥ 80%
- [ ] 2.1.5.3 API 兼容性验证

### 2.2 identity-service 整合 (Day 2-3)

#### 2.2.1 Entity/DAO 层迁移
- [ ] 2.2.1.1 迁移 UserEntity、RoleEntity、PermissionEntity
  - **注意**：检查是否已存在，避免重复
- [ ] 2.2.1.2 迁移 UserDao、RoleDao、PermissionDao
  - **规范**：@Mapper + Dao 后缀
  - **禁止**：@Repository + Repository 后缀

#### 2.2.2 Manager 层迁移
- [ ] 2.2.2.1 迁移 UserManager、RoleManager、PermissionManager
  - **职责**：用户管理、角色分配、权限验证

#### 2.2.3 Service 层迁移
- [ ] 2.2.3.1 迁移 UserService、RoleService、PermissionService 接口
- [ ] 2.2.3.2 创建 ServiceImpl 实现类

#### 2.2.4 Controller 层创建
- [ ] 2.2.4.1 创建 UserController
- [ ] 2.2.4.2 创建 RoleController
- [ ] 2.2.4.3 创建 PermissionController

#### 2.2.5 验证
- [ ] 2.2.5.1 编译验证
- [ ] 2.2.5.2 单元测试
- [ ] 2.2.5.3 RBAC 功能验证

### 2.3 notification-service 整合 (Day 3-4)

#### 2.3.1 Entity/DAO 层迁移
- [ ] 2.3.1.1 迁移 NotificationEntity、NotificationTemplateEntity
- [ ] 2.3.1.2 迁移 NotificationDao、NotificationTemplateDao

#### 2.3.2 Manager 层迁移
- [ ] 2.3.2.1 迁移 NotificationManager
  - **职责**：邮件发送、短信发送、站内消息

#### 2.3.3 Service 层迁移
- [ ] 2.3.3.1 迁移 NotificationService 接口
- [ ] 2.3.3.2 创建 NotificationServiceImpl

#### 2.3.4 Controller 层创建
- [ ] 2.3.4.1 创建 NotificationController

#### 2.3.5 验证
- [ ] 2.3.5.1 编译验证
- [ ] 2.3.5.2 通知发送功能测试

### 2.4 audit-service 整合 (Day 4-5)

#### 2.4.1 Entity/DAO 层迁移
- [ ] 2.4.1.1 验证 AuditLogEntity 已存在
- [ ] 2.4.1.2 验证 AuditLogDao 已存在

#### 2.4.2 Manager 层迁移
- [ ] 2.4.2.1 验证 AuditManager 已存在

#### 2.4.3 Service 层迁移
- [ ] 2.4.3.1 验证 AuditService 接口已存在
- [ ] 2.4.3.2 补充缺失的方法实现

#### 2.4.4 Controller 层验证
- [ ] 2.4.4.1 验证 AuditController 已存在
- [ ] 2.4.4.2 补充缺失的 API 端点

#### 2.4.5 验证
- [ ] 2.4.5.1 编译验证
- [ ] 2.4.5.2 审计日志记录测试

### 2.5 monitor-service 整合 (Day 5-6)

#### 2.5.1 Entity/DAO 层迁移
- [ ] 2.5.1.1 迁移 MonitorMetricEntity、AlertEntity
- [ ] 2.5.1.2 迁移 MonitorMetricDao、AlertDao

#### 2.5.2 Manager 层迁移
- [ ] 2.5.2.1 迁移 MonitorManager、AlertManager
  - **职责**：指标采集、告警触发

#### 2.5.3 Service 层迁移
- [ ] 2.5.3.1 迁移 MonitorService、AlertService 接口
- [ ] 2.5.3.2 创建 ServiceImpl 实现类

#### 2.5.4 Controller 层创建
- [ ] 2.5.4.1 创建 MonitorController
- [ ] 2.5.4.2 创建 AlertController

#### 2.5.5 验证
- [ ] 2.5.5.1 编译验证
- [ ] 2.5.5.2 监控指标采集测试

### 2.6 scheduler-service 整合 (Day 6-7)

#### 2.6.1 Entity/DAO 层迁移
- [ ] 2.6.1.1 迁移 ScheduledTaskEntity、TaskLogEntity
- [ ] 2.6.1.2 迁移 ScheduledTaskDao、TaskLogDao

#### 2.6.2 Manager 层迁移
- [ ] 2.6.2.1 迁移 SchedulerManager
  - **职责**：任务调度、Cron 解析

#### 2.6.3 Service 层迁移
- [ ] 2.6.3.1 迁移 SchedulerService 接口
- [ ] 2.6.3.2 创建 SchedulerServiceImpl

#### 2.6.4 Controller 层创建
- [ ] 2.6.4.1 创建 SchedulerController

#### 2.6.5 验证
- [ ] 2.6.5.1 编译验证
- [ ] 2.6.5.2 定时任务执行测试

### 2.7 system-service 整合 (Day 7-8)

#### 2.7.1 Dict 模块验证
- [ ] 2.7.1.1 验证 DictTypeEntity、DictDataEntity 已存在
- [ ] 2.7.1.2 验证 DictTypeDao、DictDataDao 已存在
- [ ] 2.7.1.3 验证 DictTypeService、DictDataService 已存在
- [ ] 2.7.1.4 验证 DictController 已存在

#### 2.7.2 Config 模块验证
- [ ] 2.7.2.1 验证 ConfigEntity 已存在
- [ ] 2.7.2.2 验证 ConfigDao 已存在
- [ ] 2.7.2.3 验证 ConfigService 已存在
- [ ] 2.7.2.4 验证 ConfigController 已存在

#### 2.7.3 Menu 模块验证
- [ ] 2.7.3.1 验证 MenuEntity 已存在
- [ ] 2.7.3.2 验证 MenuDao 已存在
- [ ] 2.7.3.3 验证 MenuService 已存在
- [ ] 2.7.3.4 验证 MenuController 已存在

#### 2.7.4 Department 模块验证
- [ ] 2.7.4.1 验证 DepartmentEntity 已存在
- [ ] 2.7.4.2 验证 DepartmentDao 已存在
- [ ] 2.7.4.3 验证 DepartmentService 已存在
- [ ] 2.7.4.4 验证 DepartmentController 已存在

#### 2.7.5 验证
- [ ] 2.7.5.1 编译验证
- [ ] 2.7.5.2 系统配置功能测试

### 2.8 config-service 整合 (Day 8)

#### 2.8.1 验证 Nacos 配置功能
- [ ] 2.8.1.1 验证配置中心连接
- [ ] 2.8.1.2 验证配置热更新

### 2.9 enterprise-service 整合 (Day 8-9)

#### 2.9.1 File 模块验证
- [ ] 2.9.1.1 验证 FileEntity 已存在
- [ ] 2.9.1.2 验证 FileDao 已存在
- [ ] 2.9.1.3 验证 FileService 已存在
- [ ] 2.9.1.4 验证 FileController 已存在

#### 2.9.2 Workflow 模块验证
- [ ] 2.9.2.1 验证 WorkflowDefinitionEntity 已存在
- [ ] 2.9.2.2 验证 WorkflowInstanceEntity 已存在
- [ ] 2.9.2.3 验证 WorkflowTaskEntity 已存在
- [ ] 2.9.2.4 验证 WorkflowEngineService 已存在
- [ ] 2.9.2.5 验证 WorkflowEngineController 已存在

#### 2.9.3 验证
- [ ] 2.9.3.1 编译验证
- [ ] 2.9.3.2 文件上传下载测试
- [ ] 2.9.3.3 工作流执行测试

### 2.10 infrastructure-service 整合 (Day 9-10)

#### 2.10.1 迁移基础设施功能
- [ ] 2.10.1.1 迁移资产管理功能
- [ ] 2.10.1.2 迁移运维工具功能

#### 2.10.2 验证
- [ ] 2.10.2.1 编译验证
- [ ] 2.10.2.2 功能测试

---

## Phase 3: 设备服务整合 (预计5个工作日)

### 3.1 device-service → device-comm-service 迁移

#### 3.1.1 协议适配器迁移
- [ ] 3.1.1.1 验证 DeviceProtocolAdapter 接口已存在
- [ ] 3.1.1.2 验证 TcpProtocolAdapter 已存在
- [ ] 3.1.1.3 验证 HttpProtocolAdapter 已存在
- [ ] 3.1.1.4 补充其他协议适配器（MQTT、UDP）

#### 3.1.2 连接管理迁移
- [ ] 3.1.2.1 迁移 DeviceConnectionManager
- [ ] 3.1.2.2 实现连接池管理
- [ ] 3.1.2.3 实现心跳检测

#### 3.1.3 数据采集迁移
- [ ] 3.1.3.1 迁移数据采集服务
- [ ] 3.1.3.2 实现数据缓存

#### 3.1.4 Controller 创建
- [ ] 3.1.4.1 验证 DeviceCommunicationController 已存在

#### 3.1.5 验证
- [ ] 3.1.5.1 编译验证
- [ ] 3.1.5.2 设备连接测试
- [ ] 3.1.5.3 数据采集测试

---

## Phase 4: 测试验证 (预计5个工作日)

### 4.1 单元测试
- [ ] 4.1.1 auth 模块单元测试（覆盖率 ≥ 80%）
- [ ] 4.1.2 identity 模块单元测试（覆盖率 ≥ 80%）
- [ ] 4.1.3 notification 模块单元测试（覆盖率 ≥ 80%）
- [ ] 4.1.4 audit 模块单元测试（覆盖率 ≥ 80%）
- [ ] 4.1.5 monitor 模块单元测试（覆盖率 ≥ 80%）
- [ ] 4.1.6 scheduler 模块单元测试（覆盖率 ≥ 80%）
- [ ] 4.1.7 system 模块单元测试（覆盖率 ≥ 80%）
- [ ] 4.1.8 file/workflow 模块单元测试（覆盖率 ≥ 80%）
- [ ] 4.1.9 device-comm 模块单元测试（覆盖率 ≥ 80%）

### 4.2 集成测试
- [ ] 4.2.1 API 端点集成测试
- [ ] 4.2.2 服务间调用集成测试
- [ ] 4.2.3 数据库事务集成测试

### 4.3 性能测试
- [ ] 4.3.1 API 响应时间测试（目标 < 200ms）
- [ ] 4.3.2 并发压力测试（目标 1000 TPS）
- [ ] 4.3.3 内存泄漏测试

### 4.4 兼容性测试
- [ ] 4.4.1 前端 API 兼容性测试
- [ ] 4.4.2 移动端 API 兼容性测试

---

## Phase 5: 归档清理 (预计2个工作日)

### 5.1 服务归档
- [ ] 5.1.1 创建 Git 标签备份：`backup/pre-consolidation-20251202`
- [ ] 5.1.2 移动 auth-service → archive/deprecated-services/
- [ ] 5.1.3 移动 identity-service → archive/deprecated-services/
- [ ] 5.1.4 移动 notification-service → archive/deprecated-services/
- [ ] 5.1.5 移动 audit-service → archive/deprecated-services/
- [ ] 5.1.6 移动 monitor-service → archive/deprecated-services/
- [ ] 5.1.7 移动 scheduler-service → archive/deprecated-services/
- [ ] 5.1.8 移动 system-service → archive/deprecated-services/
- [ ] 5.1.9 移动 config-service → archive/deprecated-services/
- [ ] 5.1.10 移动 enterprise-service → archive/deprecated-services/
- [ ] 5.1.11 移动 infrastructure-service → archive/deprecated-services/
- [ ] 5.1.12 移动 device-service → archive/deprecated-services/

### 5.2 文档更新
- [ ] 5.2.1 更新 CLAUDE.md 微服务列表
- [ ] 5.2.2 更新 API 文档
- [ ] 5.2.3 更新部署文档
- [ ] 5.2.4 创建迁移完成报告

### 5.3 CI/CD 更新
- [ ] 5.3.1 更新 Maven 父 POM
- [ ] 5.3.2 更新 Docker Compose 配置
- [ ] 5.3.3 更新 Kubernetes 部署配置

---

## 质量检查清单

### 每个迁移任务完成后必须验证
- [ ] 编译 0 错误 0 警告
- [ ] 无 @Autowired 使用
- [ ] 无 @Repository 使用
- [ ] 无 javax.* 包引用
- [ ] 四层架构正确
- [ ] API 路径保持兼容
- [ ] 单元测试覆盖率 ≥ 80%

---

**总计任务数量**: 150+ 个详细任务
**预计完成时间**: 27 个工作日（约 5-6 周）
**质量标准**: 企业级高质量实现，严格遵循 CLAUDE.md 规范

