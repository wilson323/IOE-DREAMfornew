# IOE-DREAM 项目 OpenSpec 配置

## 项目概述

**IOE-DREAM**（Intelligent Operations & Enterprise - Digital Resource & Enterprise Application Management）是新一代**智慧园区一卡通管理平台**，基于Spring Boot 3.5.8 + Spring Cloud 2025.0.0 + Spring Cloud Alibaba 2025.0.0.0构建的企业级微服务架构系统。

### 项目定位
- **业务定位**: 智慧园区一卡通管理平台，提供完整的一卡通和生物识别安防解决方案
- **技术定位**: 企业级微服务架构，支持高并发、高可用、水平扩展
- **安全定位**: 满足国家三级等保要求，具备金融级安全防护体系
- **集成定位**: 标准化API接口，支持与各类第三方系统无缝集成

### 核心业务场景
- **智慧园区综合管理**: 一卡通统一管理、多系统数据融合、智能访客系统、数据驱动决策
- **企业安全防护**: 多模态生物识别、活体检测技术、视频监控联动、异常行为检测
- **无感消费结算**: 刷脸/刷卡/手机NFC、离线消费、实时统计、智能补贴
- **智能考勤管理**: 生物识别打卡、灵活排班管理、自动考勤统计、多系统数据融合
- **访客智能管理**: 在线预约、身份验证、临时授权、轨迹追踪
- **视频监控智能分析**: AI智能分析、目标搜索、实时告警、多系统联动

## 技术栈

### 后端技术栈
- **核心框架**: Spring Boot 3.5.8, Spring Cloud 2025.0.0, Spring Cloud Alibaba 2025.0.0.0
- **Java版本**: JDK 17 (LTS)
- **ORM框架**: MyBatis-Plus 3.5.7
- **数据库**: MySQL 8.0.33
- **缓存**: Redis 6.0+ (统一使用db=0)
- **连接池**: Druid 1.2.21 (强制统一，禁止HikariCP)
- **鉴权/权限**: Spring Security 6.x（JWT Bearer）
- **消息队列**: RabbitMQ
- **服务注册**: Nacos (强制统一)
- **分布式事务**: Seata
- **容错机制**: Resilience4j
- **监控指标**: Micrometer + Prometheus
- **API文档**: Swagger/OpenAPI 3 (Knife4j)

### 前端技术栈
- **框架**: Vue 3.4.x, JavaScript ES2022
- **构建工具**: Vite 5.x
- **状态管理**: Pinia 2.x
- **UI组件**: Ant Design Vue 4.x
- **HTTP客户端**: Axios 1.6.x
- **图表**: ECharts 5.4.x
- **国际化**: Vue I18n 9.x
- **移动端**: uni-app 3.0.x
- **微前端**: qiankun 2.10 (管理后台)

### 架构标准
- **包名规范**: 强制使用Jakarta EE 3.0+ (jakarta.*)
- **依赖注入**: 统一使用@Resource注解（禁止@Autowired）
- **架构模式**: 四层架构 (Controller → Service → Manager → DAO)
- **数据访问层**: 统一使用DAO后缀 + @Mapper注解（禁止Repository）
- **代码规范**: UTF-8编码，统一格式化规则
- **缓存策略**: 多级缓存架构 (L1本地 + L2 Redis + L3网关)
- **事务管理**: SAGA分布式事务模式，@Transactional注解
- **安全体系**: 接口加解密、数据脱敏、操作审计
- **性能优化**: 数据库索引优化、连接池调优、JVM参数优化

## 微服务架构

### 七微服务架构 (强制执行)

| 服务名称 | 端口 | 类型 | 说明 | 状态 |
|---------|------|------|------|------|
| **ioedream-gateway-service** | 8080 | 基础设施 | API网关，统一入口 | ✅ 完成 |
| **ioedream-common-service** | 8088 | 核心 | 公共模块微服务 | ✅ 完成 |
| **ioedream-device-comm-service** | 8087 | 核心 | 设备通讯微服务 | ✅ 完成 |
| **ioedream-oa-service** | 8089 | 核心 | OA微服务 | ✅ 完成 |
| **ioedream-access-service** | 8090 | 核心 | 门禁管理服务 | ✅ 完成 |
| **ioedream-attendance-service** | 8091 | 核心 | 考勤管理服务 | ✅ 完成 |
| **ioedream-video-service** | 8092 | 核心 | 视频监控服务 | ✅ 完成 |
| **ioedream-consume-service** | 8094 | 核心 | 消费管理服务 | ✅ 完成 |
| **ioedream-visitor-service** | 8095 | 核心 | 访客管理服务 | ✅ 完成 |

### 服务整合说明

**已整合到核心微服务的功能模块**：
- `ioedream-auth-service` → 整合到 `ioedream-common-service`
- `ioedream-identity-service` → 整合到 `ioedream-common-service`
- `ioedream-device-service` → 整合到 `ioedream-device-comm-service`
- `ioedream-enterprise-service` → 整合到 `ioedream-oa-service`
- `ioedream-notification-service` → 整合到 `ioedream-common-service`
- `ioedream-audit-service` → 整合到 `ioedream-common-service`
- `ioedream-monitor-service` → 整合到 `ioedream-common-service`
- `ioedream-integration-service` → 拆分到各业务服务
- `ioedream-system-service` → 整合到 `ioedream-common-service`
- `ioedream-report-service` → 拆分到各业务服务
- `ioedream-scheduler-service` → 整合到 `ioedream-common-service`
- `ioedream-infrastructure-service` → 整合到 `ioedream-oa-service`

### 基础设施服务
- **microservices-common**: 公共JAR库，被所有微服务依赖
- **Nacos Config Server**: 配置中心 (端口8888)

## 项目状态

### 代码统计
- **Java文件数量**: 2,094+
- **Vue文件数量**: 418+
- **微服务数量**: 9个 (7个核心微服务 + 1个网关 + 1个公共JAR库)
- **前端项目**: 3个 (管理后台、微前端主应用、移动端)

### 质量指标
- **编译错误数量**: 0 (清洁状态)
- **Jakarta合规率**: 100%
- **架构违规数量**: 0 (完全规范)
- **微服务完成度**: 100%
- **测试覆盖率**: ≥80% (核心模块100%)
- **代码重复率**: ≤3%

### 架构评分
- **整体架构**: 83/100 (基于2025-12-01全局深度分析)
- **安全性**: 76/100 (存在64个明文密码需整改)
- **性能**: 64/100 (存在65%查询缺少索引问题)
- **监控**: 52/100 (缺少分布式追踪)
- **API设计**: 72/100 (65%接口滥用POST方法)
- **合规性**: 81/100 (存在96个Repository违规)

## OpenSpec 变更管理
- **所有功能变更**必须通过OpenSpec流程管理
- **严格确保**架构一致性和质量标准
- **禁止跨层访问**，严格遵循四层架构 (Controller → Service → Manager → DAO)
- **强制执行**全局统一架构规范 (CLAUDE.md)
- **完整要求**单元测试覆盖率≥80% (核心业务100%)
- **必须包含**异常处理和日志记录
- **技术栈统一**：Seata、Spring Cache、Resilience4j、Micrometer

## 开发环境要求
- **JDK**: 17+ (必须17，不支持Java 8/11)
- **Node.js**: >= 18.0.0
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **IDE**: IntelliJ IDEA 2023+ (推荐)

## 构建规范
- **构建顺序**: microservices-common 必须优先构建
- **Maven命令**: `mvn clean install -pl microservices/microservices-common -am`
- **统一脚本**: `.\scripts\build-all.ps1`

## 企业级特性
- **多级缓存**: L1本地缓存 + L2 Redis缓存 + L3网关缓存
- **分布式事务**: SAGA模式，最终一致性
- **服务降级**: Resilience4j熔断限流
- **监控告警**: Micrometer + Prometheus + Grafana
- **安全防护**: 接口加解密、数据脱敏、操作审计
- **无状态设计**: 支持水平扩展
- **配置管理**: Nacos配置中心，支持环境隔离

## 项目联系信息
- **项目架构师**: 老王 (企业级架构分析专家团队)
- **技术文档**: [CLAUDE.md](../CLAUDE.md) - 项目唯一架构规范
- **开发指南**: [documentation/technical/](../documentation/technical/)
- **问题反馈**: GitHub Issues
