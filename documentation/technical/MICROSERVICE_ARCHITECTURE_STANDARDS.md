# IOE-DREAM 微服务架构规范

## 🏗️ 微服务命名规范

### 服务命名标准
- **统一前缀**: `ioedream-`
- **服务名称**: 小写字母，连字符分隔
- **目录结构**: `/ioedream-{service-name}/`

### 当前服务列表
```
ioedream-auth-service           # 认证服务
ioedream-identity-service      # 身份管理服务
ioedream-device-service        # 设备管理服务
ioedream-access-service        # 门禁管理服务
ioedream-consume-service       # 消费管理服务
ioedream-visitor-service       # 访客管理服务
ioedream-attendance-service    # 考勤管理服务
ioedream-video-service         # 视频监控服务
ioedream-notification-service  # 通知服务
ioedream-file-service          # 文件服务
ioedream-gateway              # 智能网关
microservices-common           # 公共模块
```

## 📁 标准目录结构

### 微服务标准结构
```
ioedream-{service-name}/
├── pom.xml
├── README.md
├── src/main/java/net/lab1024/sa/{service}/
│   ├── {ServiceName}Application.java          # 启动类
│   ├── config/                                 # 配置类
│   │   ├── SecurityConfig.java
│   │   ├── SwaggerConfig.java
│   │   └── RedisConfig.java
│   ├── controller/                              # 控制器层
│   ├── service/                                 # 服务层
│   │   ├── {Service}Service.java
│   │   └── impl/
│   │       └── {Service}ServiceImpl.java
│   ├── manager/                                 # 管理层
│   │   ├── {Service}Manager.java
│   │   └── impl/
│   │       └── {Service}ManagerImpl.java
│   ├── dao/                                    # 数据访问层
│   ├── domain/                                 # 领域模型
│   │   ├── entity/                            # 实体类
│   │   ├── vo/                                # 值对象
│   │   ├── dto/                               # 数据传输对象
│   │   ├── enums/                             # 枚举类
│   │   └── query/                             # 查询对象
│   └── feign/                                  # Feign客户端
│       ├── {Service}Client.java
│       └── fallback/
├── src/main/resources/
│   ├── application.yml                        # 主配置文件
│   ├── application-dev.yml                    # 开发环境配置
│   ├── application-prod.yml                   # 生产环境配置
│   ├── bootstrap.yml                          # 启动配置
│   ├── mapper/                               # MyBatis映射文件
│   └── static/                               # 静态资源
└── src/test/java/                           # 测试代码
```

## 🔧 技术栈规范

### 基础技术栈
- **框架**: Spring Boot 3.5.x
- **微服务**: Spring Cloud 2023.x
- **数据库**: MySQL 8.0
- **缓存**: Redis 6.0+
- **服务发现**: Consul
- **API网关**: Spring Cloud Gateway
- **配置中心**: Consul Config

### 依赖管理
- **Java版本**: JDK 17+
- **构建工具**: Maven 3.8+
- **包管理**: 统一父POM

## 📝 代码规范

### 包命名规范
- **基础包**: `net.lab1024.sa.{service}`
- **Controller**: `net.lab1024.sa.{service}.controller`
- **Service**: `net.lab1024.sa.{service}.service`
- **Manager**: `net.lab1024.sa.{service}.manager`
- **DAO**: `net.lab1024.sa.{service}.dao`
- **Entity**: `net.lab1024.sa.{service}.domain.entity`
- **VO**: `net.lab1024.sa.{service}.domain.vo`
- **DTO**: `net.lab1024.sa.{service}.domain.dto`
- **Config**: `net.lab1024.sa.{service}.config`

### 类命名规范
- **实体类**: `{ModuleName}Entity`
- **VO类**: `{ModuleName}VO`
- **DTO类**: `{ModuleName}DTO`
- **Service接口**: `{ModuleName}Service`
- **Service实现**: `{ModuleName}ServiceImpl`
- **Controller**: `{ModuleName}Controller`
- **DAO**: `{ModuleName}Dao`
- **Manager**: `{ModuleName}Manager`

### 注解规范
- **依赖注入**: `@Resource`
- **包名**: `jakarta.*`
- **日志**: `@Slf4j` + SLF4J
- **异常处理**: 统一异常处理器
- **API文档**: `@Tag`, `@Operation`

## 🔌 API接口规范

### RESTful API设计
- **命名**: 小写字母+连字符
- **HTTP方法**: 严格遵循RESTful规范
- **响应格式**: 统一ResponseDTO
- **状态码**: 标准HTTP状态码

### 接口路径规范
```
/api/{service}/{resource}          # 资源列表
/api/{service}/{resource}/{id}     # 具体资源
/api/{service}/{resource}/search   # 搜索接口
/api/{service}/{resource}/export   # 导出接口
/api/{service}/{resource}/import   # 导入接口
```

### 响应格式规范
```json
{
  "code": 200,
  "success": true,
  "message": "操作成功",
  "data": {
    // 响应数据
  },
  "timestamp": 1704403200000
}
```

## 🗄️ 数据库规范

### 表命名规范
- **前缀**: `t_`
- **格式**: `t_{business}_{entity}`
- **示例**: `t_user_info`, `t_device_record`

### 字段命名规范
- **主键**: `{table}_id` (BIGINT AUTO_INCREMENT)
- **外键**: `{related_table}_id`
- **审计字段**:
  - `create_time` (DATETIME)
  - `update_time` (DATETIME)
  - `create_user_id` (BIGINT)
  - `update_user_id` (BIGINT)
  - `deleted_flag` (TINYINT, 0-未删除, 1-已删除)
- **业务字段**: 小写+下划线

### 索引规范
- **主键索引**: `pk_{table}`
- **唯一索引**: `uk_{table}_{column}`
- **普通索引**: `idx_{table}_{column}`

## 🔒 安全规范

### 认证授权
- **认证方式**: JWT Token
- **权限控制**: RBAC
- **接口鉴权**: @SaCheckPermission
- **数据权限**: @DataScope

### 安全配置
- **密码加密**: BCrypt
- **SQL注入防护**: MyBatis预编译
- **XSS防护**: 输入验证和输出编码
- **CSRF防护**: Token验证

## 📊 监控规范

### 应用监控
- **健康检查**: `/actuator/health`
- **指标收集**: `/actuator/metrics`
- **日志记录**: 统一日志格式
- **性能监控**: APM集成

### 业务监控
- **接口调用量**: 响应时间、错误率
- **业务指标**: 用户活跃度、交易量
- **系统指标**: CPU、内存、磁盘、网络

## 🚀 部署规范

### 环境配置
- **开发环境**: application-dev.yml
- **测试环境**: application-test.yml
- **生产环境**: application-prod.yml

### 容器化规范
- **基础镜像**: openjdk:17-jre-slim
- **端口配置**: 每个服务独立端口
- **健康检查**: 内置健康检查端点
- **资源限制**: CPU、内存限制

### 配置管理
- **敏感配置**: 环境变量
- **业务配置**: 配置中心
- **外部配置**: ConfigMap

## 🔄 CI/CD规范

### 分支管理
- **主分支**: main (生产环境)
- **开发分支**: develop (开发环境)
- **功能分支**: feature/{功能名}

### 构建规范
- **构建命令**: `mvn clean package`
- **测试命令**: `mvn test`
- **代码检查**: `mvn checkstyle:check`

### 部署规范
- **灰度发布**: 蓝绿部署
- **回滚机制**: 版本回退
- **监控告警**: 部署状态监控

---

## 📋 检查清单

### 代码质量
- [ ] 符合命名规范
- [ ] 遵循架构分层
- [ ] 异常处理完善
- [ ] 日志记录规范
- [ ] 单元测试覆盖

### 性能要求
- [ ] 数据库查询优化
- [ ] 缓存策略合理
- [ ] 接口响应时间
- [ ] 并发处理能力

### 安全要求
- [ ] 认证授权完整
- [ ] 数据加密保护
- [ ] 输入验证充分
- [ ] 安全配置正确

### 可维护性
- [ ] 代码注释完整
- [ ] 文档更新及时
- [ ] 版本管理规范
- [ ] 监控告警完善