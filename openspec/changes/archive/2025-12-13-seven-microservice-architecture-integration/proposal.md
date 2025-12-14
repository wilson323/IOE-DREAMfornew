# Change: 七微服务架构重构与企业级整合实现

## Why
基于用户最新要求，IOE-DREAM项目需要从当前的多微服务架构严格重构为7微服务架构：公共模块微服务 + 设备通讯微服务 + OA微服务 + 考勤微服务 + 门禁微服务 + 消费微服务 + 访客微服务 + 视频微服务。当前架构存在13个分散服务需要整合，确保"绝不超限"的严格约束，并实现企业级高质量整合。

## What Changes
- **严格7微服务架构重构**：整合13个现有服务到7个核心微服务
- **公共模块微服务建设**：整合auth、identity、notification、audit、monitor、scheduler、system到公共模块微服务(8088)
- **设备通讯微服务重构**：整合device-service到设备通讯微服务(8087)，专注设备连接和协议处理
- **OA微服务新建**：整合enterprise、infrastructure到OA微服务(8089)，提供企业级办公自动化功能
- **业务微服务功能完善**：考勤(8091)、门禁(8090)、消费(8094)、访客(8095)、视频(8092)五大业务微服务完善
- **服务整合映射明确**：建立完整的13→7服务整合映射表，确保功能无遗漏
- **企业级架构标准**：统一技术栈、四层架构、SAGA分布式事务、监控告警体系

## Impact
- **Affected specs**:
  - microservice-architecture（新建微服务架构规格）
  - common-service（新建公共模块规格）
  - device-communication-service（新建设备通讯规格）
  - oa-service（新建OA服务规格）
  - access-control-system（修改门禁系统规格）
  - consume-module（修改消费模块规格）
  - attendance-system（修改考勤系统规格）
  - visitor-management（修改访客管理规格）
  - video-surveillance（修改视频监控规格）

- **Affected code**:
  - **新建微服务**：`ioedream-common-service`、`ioedream-device-comm-service`、`ioedream-oa-service`
  - **重构微服务**：所有现有微服务的架构调整和功能整合
  - **数据库迁移**：13个分散数据库到7个统一数据库的整合
  - **API网关配置**：7微服务的路由策略重新配置
  - **服务间调用**：所有微服务间通信适配到新架构

- **BREAKING CHANGES**:
  - 微服务数量从13个减少到7个
  - 数据库架构重新设计
  - API接口路径重新规划
  - 服务发现注册中心调整
  - 配置中心结构变更

## Implementation Scope
基于严格7微服务架构约束，确保：
- **绝不超限**：严格控制在7个核心微服务内
- **功能完整**：13个服务功能100%整合，无遗漏
- **架构统一**：四层架构、Spring Boot 3.x、Jakarta EE 100%合规
- **企业级质量**：SAGA事务、降级熔断、监控告警完整实现
- **文档同步**：所有相关文档同步更新，确保一致性

## Expected Outcomes
- 实现7微服务严格架构，符合用户"绝不超限"要求
- 13个分散服务功能完整整合到7个核心微服务
- 建立统一的技术栈和架构标准
- 提供企业级高可用性和扩展性
- 完整的监控、运维、部署体系
- 100%文档同步更新，确保项目一致性