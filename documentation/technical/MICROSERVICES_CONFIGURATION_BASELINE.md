# 微服务配置基准文档

**文档版本**: v1.0.0
**生成时间**: 2025-12-23
**适用范围**: IOE-DREAM微服务项目

---

## 1. 配置文件组织结构

### 1.1 标准目录结构

```
microservices/
├── common-config/                          # 公共配置模板
│   ├── application-common-base.yml        # 基础配置模板
│   ├── rabbitmq-application.yml          # RabbitMQ配置
│   ├── redis-application.yml              # Redis配置
│   ├── resilience4j-application.yml       # Resilience4j配置
│   ├── tracing/application-tracing.yml    # 分布式追踪配置
│   └── springdoc-application.yml          # API文档配置
│
├── {service-name}/                        # 业务服务
│   └── src/main/resources/
│       ├── application.yml                # 主配置文件（必需）
│       ├── application-dev.yml            # 开发环境配置
│       ├── application-test.yml           # 测试环境配置
│       ├── application-prod.yml           # 生产环境配置
│       ├── bootstrap.yml                  # 引导配置（可选）
│       ├── logback-spring.xml             # 日志配置
│       └── ...                            # 其他配置文件
│
└── pom.xml                                # Maven父POM（版本管理）
```

### 1.2 配置文件命名规范

**必需配置**：
- `application.yml` - 主配置文件（必需）

**环境配置**（推荐）：
- `application-dev.yml` - 开发环境
- `application-test.yml` - 测试环境
- `application-prod.yml` - 生产环境

**专项配置**（可选）：
- `bootstrap.yml` - 引导配置（服务发现、配置中心）
- `logback-spring.xml` - 日志配置

---

## 2. 公共配置模板

### 2.1 基础配置模板

**文件**: `common-config/application-common-base.yml`

**包含配置**：
- Spring Boot基础配置
- Actuator监控端点
- 管理端点配置
- 健康检查配置

**使用方式**：通过Spring Cloud Config或Nacos配置中心统一管理

### 2.2 中间件配置模板

**RabbitMQ配置**：`rabbitmq-application.yml`
**Redis配置**：`redis-application.yml`
**Resilience4j配置**：`resilience4j-application.yml`
**分布式追踪配置**：`tracing/application-tracing.yml`
**API文档配置**：`springdoc-application.yml`

---

## 3. 服务配置标准

### 3.1 必需配置项

每个服务的`application.yml`必须包含：

```yaml
spring:
  application:
    name: {service-name}    # 服务名称（必需）

server:
  port: {port}              # 服务端口（可选，可通过环境变量覆盖）

# 其他配置项...
```

### 3.2 配置管理原则

**原则1：配置外置**
- 敏感信息（密码、密钥）通过环境变量或配置中心管理
- 避免将敏感信息写入代码仓库

**原则2：环境隔离**
- 不同环境使用不同的配置文件
- 通过Spring Profile切换环境

**原则3：配置最小化**
- 只配置服务特定参数
- 公共配置通过配置中心统一管理

**原则4：配置版本化**
- 配置变更纳入版本管理
- 记录配置变更历史和原因

---

## 4. 各服务配置清单（2025-12-23）

### 4.1 网关服务

**服务名**: `ioedream-gateway-service`
**配置文件**: 8个
**特点**: 使用公共配置模板

### 4.2 公共业务服务

**服务名**: `ioedream-common-service`
**配置文件**: 8个
**特点**: 用户、组织、权限等公共业务

### 4.3 门禁服务

**服务名**: `ioedream-access-service`
**配置文件**: 0个额外配置
**特点**: 使用公共配置模板

### 4.4 考勤服务

**服务名**: `ioedream-attendance-service`
**配置文件**: 3个额外配置
**特点**: 已配置spring.application.name

### 4.5 消费服务

**服务名**: `ioedream-consume-service`
**配置文件**: 4个额外配置
**特点**: 已配置Nacos服务发现

### 4.6 视频服务

**服务名**: `ioedream-video-service`
**配置文件**: 3个额外配置
**特点**: 使用公共配置模板

### 4.7 访客服务

**服务名**: `ioedream-visitor-service`
**配置文件**: 3个额外配置
**特点**: 使用公共配置模板

### 4.8 设备通信服务

**服务名**: `ioedream-device-comm-service`
**配置文件**: 3个额外配置
**特点**: 设备协议适配

### 4.9 OA服务

**服务名**: `ioedream-oa-service`
**配置文件**: 4个额外配置
**特点**: 办公自动化

---

## 5. 配置一致性检查结果（2025-12-23）

### 5.1 检查统计

- 总检查项: 54
- 通过: 21 ✅
- 警告: 19 ⚠️
- 错误: 0 ✅

### 5.2 主要发现

**✅ 符合规范**：
- 所有服务都有`application.yml`
- 大部分服务配置了`spring.application.name`
- 所有服务使用配置模板，无硬编码

**⚠️ 需要改进**：
- `server.port`配置不统一（部分服务使用默认值）
- Nacos配置不统一（仅consume-service配置）
- 部分服务配置项缺失

### 5.3 改进建议

1. **统一端口配置**
   - 在父POM中定义各服务默认端口
   - 通过环境变量覆盖，便于容器化部署

2. **统一服务发现配置**
   - 所有服务统一配置Nacos或其他服务发现
   - 制定服务发现配置标准

3. **配置模板完善**
   - 补充缺失的配置项
   - 提供配置模板使用指南

---

## 6. 配置管理最佳实践

### 6.1 配置分层

**第1层：框架配置**
- Spring Boot默认配置
- 自动装配配置

**第2层：公共配置**
- 通过配置中心统一管理
- 所有服务共享

**第3层：服务配置**
- 服务特定配置
- 在`application.yml`中定义

**第4层：环境配置**
- 环境特定配置
- 通过Profile切换

**第5层：运行时配置**
- 环境变量
- 命令行参数

### 6.2 敏感信息管理

**禁止**：
- ❌ 将密码写入配置文件
- ❌ 将密钥提交到代码仓库
- ❌ 在日志中打印敏感信息

**推荐**：
- ✅ 使用环境变量存储敏感信息
- ✅ 使用配置中心加密存储
- ✅ 使用密钥管理服务（如HashiCorp Vault）

### 6.3 配置变更流程

**变更步骤**：
1. 评估配置影响范围
2. 在测试环境验证
3. 更新配置文档
4. 提交变更请求
5. 在灰度环境验证
6. 全量发布
7. 监控配置效果

**回滚机制**：
- 保留配置历史版本
- 制定回滚决策标准
- 准备快速回滚方案

---

## 7. 配置检查工具

### 7.1 配置一致性检查脚本

```bash
# 运行配置检查
cd microservices
bash scripts/check-module-configuration.sh
```

**检查项**：
- 配置文件完整性
- 必需配置项检查
- 配置模板使用情况
- 配置规范符合性

### 7.2 Maven配置检查

```bash
# 验证Maven配置
mvn validate

# 查看有效配置
mvn help:effective-pom
```

---

## 8. 配置文档维护

### 8.1 更新频率

- 配置模板变更：立即更新
- 新服务接入：更新服务配置清单
- 配置标准变更：季度审查更新

### 8.2 责任人

- **配置管理**: 架构委员会
- **配置维护**: 各服务负责人
- **配置审查**: DevOps团队

---

## 9. 参考文档

- [Spring Boot配置文档](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)
- [Spring Cloud Config文档](https://cloud.spring.io/spring-cloud-config/)
- [Nacos配置管理文档](https://nacos.io/zh-cn/docs/configuration.html)

---

**文档维护者**: IOE-DREAM架构委员会
**最后更新**: 2025-12-23
**下次审查**: 2026-03-23
