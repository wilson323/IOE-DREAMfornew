# IOE-DREAM 项目 OpenSpec 配置

## 项目概述
IOE-DREAM是一个基于Spring Boot 3.5和Jakarta EE的企业级智能管理系统，采用微服务架构设计。项目专注于智能门禁、消费管理、考勤管理、视频监控等企业核心业务场景，支持多端统一（Web端、移动端、管理后台）的用户体验。

### 核心业务模块
- **智能门禁管理**: 多模态生物识别、访客管理、区域权限控制
- **消费管理系统**: 多种消费模式、账户管理、报表分析
- **考勤管理系统**: 智能排班、异常检测、统计分析
- **视频监控系统**: 实时监控、智能分析、录像管理

## 技术栈

### 后端技术栈
- **核心框架**: Spring Boot 3.5.7, Spring Cloud 2023.0.3
- **Java版本**: JDK 17 (LTS)
- **ORM框架**: MyBatis-Plus 3.5.7
- **数据库**: MySQL 8.0.33
- **缓存**: Redis 6.0+
- **连接池**: Druid 1.2.21
- **权限框架**: Sa-Token 1.37.0
- **消息队列**: RabbitMQ
- **API文档**: Swagger/OpenAPI 3

### 前端技术栈
- **框架**: Vue 3.x, JavaScript ES2022
- **构建工具**: Vite 5.x
- **状态管理**: Pinia 2.x
- **UI组件**: Ant Design Vue 4.x
- **HTTP客户端**: Axios 1.x
- **移动端**: uni-app

### 架构标准
- **包名规范**: 严格使用Jakarta EE (jakarta.*)
- **依赖注入**: 统一使用@Resource注解
- **架构模式**: 四层架构 (Controller → Service → Manager → DAO)
- **代码规范**: RepoWiki合规，UTF-8编码
- **缓存策略**: Redis集群，合理TTL设计
- **事务管理**: @Transactional注解，严格异常处理

## 微服务架构

### 已完成微服务 (90%完成度)
- **ioedream-auth-service**: 认证授权微服务 (100%完成)
- **ioedream-identity-service**: 身份权限微服务 (100%完成)
- **ioedream-device-service**: 设备管理微服务 (100%完成)
- **ioedream-access-service**: 门禁控制微服务 (100%完成)
- **ioedream-attendance-service**: 考勤管理微服务 (100%完成)
- **ioedream-consume-service**: 消费管理微服务 (100%完成)
- **ioedream-video-service**: 视频监控微服务 (100%完成)
- **ioedream-config-service**: 配置中心微服务 (企业级实现)
- **ioedream-audit-service**: 审计日志微服务 (合规安全管理)
- **visitor-service**: 访客管理微服务 (完整实现含92.5%测试覆盖)

### 基础设施服务
- **microservices-common**: 公共模块 (标准化完成)
- **smart-gateway**: 智能网关服务

### 待建设服务
- ioedream-notification-service: 通知服务
- ioedream-file-service: 文件服务
- ioedream-report-service: 报表服务

## 项目状态
- **Java文件数量**: 2,094+
- **Vue文件数量**: 418+
- **编译错误数量**: 0 (清洁状态)
- **Jakarta合规率**: 100%
- **架构违规数量**: 0 (完全规范)
- **微服务完成度**: 90%

## OpenSpec 变更管理
- **所有功能变更**必须通过OpenSpec流程管理
- **严格确保**架构一致性和质量标准
- **禁止跨层访问**，严格遵循四层架构
- **强制执行**RepoWiki编码规范
- **完整要求**单元测试覆盖率≥80%
- **必须包含**异常处理和日志记录

## 开发环境要求
- **JDK**: 17+ (必须17，不支持Java 8/11)
- **Node.js**: >= 18.0.0
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **IDE**: IntelliJ IDEA 2023+ (推荐)