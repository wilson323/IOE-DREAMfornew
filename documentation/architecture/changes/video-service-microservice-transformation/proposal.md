# Change: video-service-microservice-transformation

## Why

为了实现IOE-DREAM项目从单体架构向微服务架构的转型，将视频监控模块从现有单体应用中独立出来，形成独立的视频微服务。根据改造计划，视频服务微服务化是Phase 3扩展业务微服务化的重要组成部分，需要7-9天完成独立化改造，包括视频流管理重构和AI分析与告警系统的独立化。

## What Changes

- **视频监控核心独立化**：将实时视频预览、历史录像回放功能独立为微服务
- **设备管理分离**：视频设备管理、状态监控功能从主应用分离
- **AI分析引擎独立**：智能视频分析、人脸识别、行为检测功能模块化
- **数据存储分离**：视频相关的数据库表和文件存储独立管理
- **API网关集成**：通过API网关统一暴露视频服务接口
- **配置中心集成**：使用Nacos配置中心管理视频服务配置
- **服务注册发现**：视频服务注册到Nacos注册中心
- **缓存独立化**：视频相关的Redis缓存策略独立部署

## Impact

- **Affected specs**: smart-access（可能影响与门禁的集成）
- **Affected code**:
  - `smart-admin-api-java17-springboot3/sa-admin/src/main/java/net/lab1024/sa/admin/module/smart/video/` (35个文件)
  - `smart-admin-api-java17-springboot3/sa-admin/src/main/resources/mapper/smart/video/` (MyBatis映射文件)
  - 相关前端Vue组件需要调整API调用地址
- **Breaking changes**: 视频相关的API接口将通过独立的微服务端口提供，前端调用地址需要更新
- **Dependencies**: 需要Nacos注册中心、配置中心、API网关基础设施支持