# Change: 访客管理模块微服务化改造

## Why

基于IOE-DREAM项目微服务化改造计划，将访客管理模块从现有的单体架构中独立出来，创建专门的visitor-service微服务。当前访客模块功能完整且设计规范，但与其他业务模块耦合在同一应用中，限制了独立部署和扩展能力。通过微服务化改造，可以实现：

- 提升访客服务的可扩展性和可用性
- 支持独立部署和版本管理
- 降低与其他业务模块的耦合度
- 优化访客管理专项性能

## What Changes

- **创建独立的visitor-service微服务**：包含完整的访客管理功能
- **数据库表迁移和优化**：将访客相关表结构迁移到专用数据库
- **API接口重构**：设计微服务化的RESTful API接口
- **服务间通信机制**：建立与access-service、user-service等的通信协议
- **事件驱动架构**：实现基于事件的异步通信
- **配置和监控**：独立的配置管理和监控体系

## Impact

- **Affected specs**:
  - `smart-access` (访客权限集成)
  - `rbac` (访客权限管理)
  - `area` (区域访问权限)
  - `people` (访客信息管理)
- **Affected code**:
  - `smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/` (移除访客相关代码)
  - `smart-admin-api-java17-springboot3/sa-base/src/main/java/net/lab1024/sa/base/module/support/rbac/dao/AreaPersonDao.java` (区域人员关系)
  - `microservices/access-service/` (API接口调整)
- **Breaking changes**:
  - 访客相关API将从主单体应用迁移到独立微服务
  - 数据库访问方式变更（需要配置独立数据源）
  - 内部方法调用改为微服务间API调用