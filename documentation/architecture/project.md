# IOE-DREAM Project Context

## Purpose
IOE-DREAM 智慧园区综合管理平台，基于SmartAdmin v3框架构建的企业级快速开发平台。专为企业信息化管理提供完整的解决方案，集成了门禁系统、考勤系统、消费系统、智能视频监控、访客管理等核心业务模块。项目目标是提供一个高性能、安全可靠、易于扩展的管理系统开发框架，支持快速构建各类企业级应用。

## Tech Stack

### 后端技术栈
- **Java**: JDK 17+ (LTS版本)
- **框架**: Spring Boot 3.5.4, Spring Security 6.5.1
- **数据库**: MyBatis Plus 3.5.12, MySQL 9.3.0 (支持达梦、金仓等国产数据库)
- **连接池**: Druid 1.2.25 + P6spy 3.9.1 (SQL监控)
- **缓存**: Redis 7.0+, Redisson 3.50.0, Caffeine (L1+L2多级缓存)
- **认证**: Sa-Token 1.44.0 (权限认证)
- **文档**: Knife4j 4.6.0, SpringDoc OpenAPI 2.8.9
- **JSON处理**: FastJSON 2.0.57
- **工具库**: Hutool 5.8.39, Apache Commons, Google Guava 20.0
- **模板引擎**: Velocity 2.4.1, FreeMarker 2.3.34
- **加密**: BCrypt Provider 1.80, SM-Crypto 0.3.13 (国密支持)

### 前端技术栈
- **核心**: Vue 3.4.27 + Composition API
- **类型系统**: JavaScript (ES2022+)
- **构建工具**: Vite 5.2.12
- **UI框架**: Ant Design Vue 4.2.5
- **状态管理**: Pinia 2.1.7
- **路由**: Vue Router 4.3.2
- **国际化**: Vue i18n 9.13.1
- **样式处理**: Less 4.2.0
- **图表**: ECharts 5.4.3
- **富文本**: WangEditor 5.6.34
- **工具库**: Lodash 4.17.21, Day.js 1.11.13
- **移动端**: uni-app (Vue3版本)

### 开发工具和环境
- **Node.js**: >= 18 (ES2022支持)
- **Maven**: 3.9+ (Java项目构建)
- **IDE**: IntelliJ IDEA 2024.1+, VS Code
- **Git**: 2.40+
- **数据库管理**: DBeaver, Navicat
- **API测试**: Postman, Knife4j在线文档
- **代码质量**: ESLint, Prettier, StyleLint

## Project Conventions

### Development Standards

#### 🚨 **绝对禁止的开发行为（铁律）**
- **严禁** 通过脚本批量修改代码
- **严禁** 使用自动化脚本进行代码重构
- **严禁** 使用find+sed/awk等命令批量修改Java文件
- **严禁** 跳过人工审查直接应用修改结果
- **必须** 逐个文件手动修改，确保精确控制
- **必须** 每次修改后立即编译验证
- **必须** 所有代码变更必须经过人工审核

#### 🛠️ **强制的人工编辑流程**
1. **第一步**: 使用Read工具查看文件内容
2. **第二步**: 确认修改范围和影响
3. **第三步**: 使用Edit工具精确修改单个文件
4. **第四步**: 立即编译验证修改结果
5. **第五步**: 确认错误数量减少
6. **第六步**: 更新相关文档和测试

### Code Style

#### 命名规范
```java
// 包名: net.lab1024.sa.{module}.{layer}
// 类名: Controller→{Module}Controller, Service→{Module}Service
// 方法名: 查询→get/query/find/list, 新增→add/create/save/insert
// 变量名: Boolean→is/has/can开头, 集合→复数形式或List/Map后缀
```

#### 数据库规范
```sql
-- 表名: t_{business}_{entity} (t_sys_user, t_access_device)
-- 主键: {table}_id (user_id, device_id)
-- 必须字段: create_time, update_time, create_user_id, deleted_flag
-- 字符集: utf8mb4, 存储引擎: InnoDB
```

### Architecture Patterns

#### 四层架构设计
严格遵循四层架构调用链：
```
Controller层 (接口控制)
    ↓
Service层 (业务逻辑 + 事务管理)
    ↓
Manager层 (复杂业务 + 缓存管理)
    ↓
Repository层 (数据访问)
```

#### 依赖注入规范
- **必须使用**: `@Resource` 注解
- **禁止使用**: `@Autowired` 注解

#### 缓存策略
- **L1缓存**: Caffeine (本地缓存，5分钟过期)
- **L2缓存**: Redis (分布式缓存，30分钟过期)
- **缓存模式**: 先更新数据库，再删除缓存

### Testing Strategy

#### 测试要求和覆盖率
- **单元测试覆盖率**: ≥ 80%（总体目标）
- **核心业务覆盖率**: 100%（关键业务逻辑）
- **集成测试**: Service层全覆盖，Manager层重点覆盖
- **接口测试**: Controller层全覆盖，包含异常场景
- **E2E测试**: 关键业务流程端到端测试
- **性能测试**: 接口响应时间和并发能力测试

#### 测试框架和工具
- **后端测试**:
  - JUnit 5: 单元测试框架
  - Mockito: Mock对象创建
  - Testcontainers: 集成测试容器化
  - Spring Boot Test: Spring生态测试支持
  - AssertJ: 断言库，提供流畅的断言API
- **前端测试**:
  - Vitest: 快速单元测试框架
  - Vue Test Utils: Vue组件测试工具
  - @testing-library/vue: 用户行为测试
  - MSW: API Mock Service Worker
- **API测试**:
  - Postman Collections: API集合测试
  - Newman: Postman命令行执行
  - Knife4j: 在线接口文档和调试
- **性能测试**:
  - JMeter: 压力测试和性能监控
  - Gatling: 高性能负载测试

#### 测试数据管理
- **测试数据库**: 使用Docker容器化MySQL测试环境
- **数据隔离**: 每个测试用例独立的数据环境
- **Mock策略**: 外部依赖使用Mock对象，保证测试稳定性
- **测试数据清理**: 测试后自动清理，避免数据污染

### Git Workflow

#### 分支策略
```
master: 主分支 (生产环境)
develop: 开发分支 (集成最新功能)
feature/xxx: 功能分支
hotfix/xxx: 热修复分支
release/xxx: 发布分支
```

#### 提交规范
```
<type>(<scope>): <subject>
feat(user): 添加用户头像上传功能
fix(device): 修复设备状态更新异常
docs(api): 更新接口文档
```

## Domain Context

### 核心业务模块
1. **系统管理**: 员工管理、部门管理、角色权限、菜单管理、数据字典
2. **统一区域管理**: 基础区域模块、人员区域关联、设备下发策略引擎
3. **统一生物特征管理**: 以人为中心的生物特征管理、统一下发引擎、多级缓存
4. **门禁系统**: 设备管理、门禁记录、权限控制、实时监控
5. **考勤系统**: 排班管理、打卡记录、异常处理、统计报表
6. **消费系统**: 消费终端、交易记录、账户管理、对账功能
7. **智能视频**: 视频设备、录像管理、智能分析、告警处理

### 新增核心架构组件 (2025-11-24 重构)
1. **统一生物特征下发引擎 (UnifiedBiometricDispatchEngine)**
   - 位置：`sa-base/src/main/java/net/lab1024/sa/base/module/biometric/engine/`
   - 功能：统一管理生物特征数据到各业务模块设备的下发工作
   - 特性：支持批量下发、异步处理、失败重试、性能监控

2. **分层式设备适配器架构**
   - 基础接口层：`sa-base/src/main/java/net/lab1024/sa/base/common/device/`
   - 业务实现层：各业务模块独立管理协议适配器
   - 原则：基础接口统一，协议实现独立，支持业务特化

3. **设备适配器注册表 (DeviceAdapterRegistry)**
   - 位置：`sa-base/src/main/java/net/lab1024/sa/base/module/biometric/engine/`
   - 功能：管理所有设备适配器的注册、查找和调用
   - 特性：自动注册Spring容器中的适配器、智能设备类型匹配

4. **生物特征缓存管理器 (BiometricCacheManager)**
   - 位置：`sa-base/src/main/java/net/lab1024/sa/base/module/biometric/manager/`
   - 策略：L1 Caffeine本地缓存 + L2 Redis分布式缓存
   - 配置：人员生物特征1000条/60分钟，模板5000条/30分钟

5. **设备下发策略引擎 (DeviceDispatchStrategyEngine)**
   - 位置：`sa-base/src/main/java/net/lab1024/sa/base/module/device/strategy/`
   - 功能：基于人员区域关系确定目标设备，执行智能下发策略
   - 特性：支持区域策略、业务类型策略、设备过滤规则

### 权限体系
- **认证**: Sa-Token + Redis
- **授权**: RBAC模型 + 数据权限
- **安全**: 双因子认证、密码复杂度、登录错误锁定

## Important Constraints

### 架构约束
- **严格遵循四层架构**: 禁止跨层访问
- **事务边界**: 必须在Service层
- **软删除**: 必须使用 deleted_flag 字段
- **审计字段**: 必须包含 create_time, update_time, create_user_id

### 性能要求
- **接口响应时间**: P95 ≤ 200ms, P99 ≤ 500ms
- **数据库查询**: 单次 ≤ 100ms, 批量 ≤ 500ms
- **缓存命中率**: L1 ≥ 80%, L2 ≥ 90%
- **并发处理**: 支持 1000+ QPS

### 安全约束
- **认证**: 所有接口必须使用 Sa-Token
- **权限**: 敏感接口必须使用 @SaCheckPermission
- **加密**: 密码使用 BCrypt，敏感数据使用 AES
- **防护**: SQL注入、XSS攻击、文件上传安全

## External Dependencies

### 数据库配置
- **生产数据库**: MySQL 9.3.0
  - 主库: 192.168.10.110:33060
  - 数据库: smart_admin_v3
  - 字符集: utf8mb4_unicode_ci
  - 连接池: Druid (初始2个连接，最大10个连接)
- **Redis缓存**: Redis 7.0+
  - 地址: 127.0.0.1:6389
  - 数据库: db1
  - 密码: zkteco3100
  - 连接池: Lettuce (最大5个连接)

### 文件存储配置
- **本地存储**: D:/Progect/mart-admin-master/upload/
- **OSS存储**: 阿里云OSS (备用方案)
  - 区域: oss-cn-hangzhou
  - Bucket: 1024lab-smart-admin
- **单文件限制**: 20MB
- **请求总大小限制**: 10MB

### 邮件服务
- **SMTP服务器**: smtp.163.com:465 (SSL)
- **认证账户**: lab1024@163.com
- **协议**: SMTP + SSL

### 第三方集成
- **数据库兼容**: 达梦、金仓、人大金仓等国产数据库
- **加密算法**: BCrypt (密码) + AES (数据) + 国密SM系列
- **定时任务**: SmartJob内置调度器
- **监控服务**: 自研心跳监控 + Spring Boot Actuator

### 环境配置
- **开发环境**:
  - 后端: http://localhost:1024
  - 前端: http://localhost:8081
  - API文档: http://localhost:1024/doc.html
- **测试环境**: 待配置
- **预发布环境**: 待配置
- **生产环境**: 待配置
- **容器化**: 支持Docker部署，多环境Maven Profile

## Deployment & Operations

### Docker 组件
- **核心服务**: Spring Boot 后端 (1024端口)、Vue 前端 (Nginx 8080/80端口)
- **依赖服务**: MySQL 8.0、Redis 7.2、Nginx 反向代理
- **网络**: 自定义 Docker 网络 `172.20.0.0/16`，所有容器在同一网段通信
- **存储**: 使用 Docker volumes 做数据和上传文件持久化（例如 `upload_data`）

### Compose Profiles
- `docker-compose.dev.yml`: 开发与本地联调环境，支持热重载，端口映射 1024/8080
- `docker-compose.yml`: 生产部署模板，包含前端、后端、Nginx、MySQL、Redis 等完整组件
- 启动命令示例: `docker-compose -f docker-compose.dev.yml up -d`（开发）、`docker-compose up -d --build`（生产）

### 服务端口与访问
- **前端**: http://localhost:8080 (开发) / http://localhost (生产，Nginx)
- **后端 API**: http://localhost:1024/api
- **API 文档**: http://localhost:1024/doc.html
- **MySQL**: 3306 端口 (`root/root1234` 可在 .env 中覆盖)
- **Redis**: 6379 端口 (`zkteco3100` 默认密码)

### 环境变量与配置
- 通过 Compose 文件注入 `SPRING_PROFILES_ACTIVE`、数据库、Redis 等配置
- 上传文件目录映射 `/app/upload`，与主机 `upload_data` 挂载目录保持一致
- MySQL 初始化脚本来自 `数据库SQL脚本/mysql/`，首次启动会自动执行

### 运维与安全
- 常用命令: `docker-compose logs -f <service>`、`docker-compose restart <service>`
- 日志目录: `logs/backend/`、`logs/nginx/` 用于排查生产问题
- 备份策略: `mysqldump` 导出数据库、`redis-cli --rdb` 导出 Redis
- 安全建议: 启动后立即修改数据库与 Redis 默认密码，Nginx 上启用自签或正式 HTTPS 证书，服务器仅开放 80/443/1024/8080 等必要端口

## Project Structure

```
IOE-DREAM/
├── smart-admin-api-java17-springboot3/    # 后端项目 (Spring Boot 3)
│   ├── sa-base/                           # 基础模块 (公共组件、工具、配置)
│   │   ├── src/main/java/net/lab1024/sa/base/
│   │   │   ├── common/                    # 公共模块 (Entity, VO, DTO)
│   │   │   ├── config/                    # 配置类 (Sa-Token、Swagger等)
│   │   │   ├── util/                      # 工具类
│   │   │   ├── constant/                  # 常量定义
│   │   │   ├── exception/                 # 异常处理
│   │   │   └── module/                    # 核心业务模块
│   │   │       ├── access/                # 门禁系统模块
│   │   │       ├── device/                # 设备管理模块
│   │   │       ├── person/                # 人员管理模块
│   │   │       ├── permission/            # 权限管理模块
│   │   │       ├── area/                  # 统一区域管理模块
│   │   │       ├── biometric/             # 统一生物特征管理模块
│   │   │       └── common/device/         # 统一设备适配器接口
│   │   └── src/main/resources/
│   │       ├── dev/sa-base.yaml           # 开发环境配置
│   │       ├── mapper/                    # MyBatis映射文件
│   │       └── db/                        # 数据库脚本
│   └── sa-admin/                          # 管理模块 (业务逻辑)
│       └── src/main/java/net/lab1024/sa/admin/
│           ├── module/                    # 业务模块
│           │   ├── system/                # 系统管理
│           │   ├── business/              # 业务功能
│           │   └── data/                  # 数据管理
│           └── controller/                # 控制器层
├── smart-admin-web-javascript/            # 前端项目 (Vue 3)
│   ├── src/
│   │   ├── api/                          # API接口封装
│   │   ├── assets/                       # 静态资源
│   │   ├── components/                   # 公共组件
│   │   ├── config/                       # 配置文件 (环境、主题)
│   │   ├── constants/                    # 常量定义
│   │   ├── router/                       # 路由配置
│   │   ├── store/                        # Pinia状态管理
│   │   ├── utils/                        # 工具函数
│   │   ├── views/                        # 页面组件
│   │   │   ├── system/                   # 系统管理页面
│   │   │   ├── business/                 # 业务功能页面
│   │   │   └── common/                   # 通用页面
│   │   ├── App.vue                       # 根组件
│   │   └── main.js                       # 入口文件
│   ├── vite.config.js                    # Vite构建配置
│   ├── package.json                      # 依赖配置
│   └── tests/                            # 测试文件
├── smart-app/                             # 移动端项目 (uni-app)
│   ├── pages/                            # 页面文件
│   ├── api/                              # 接口封装
│   ├── utils/                            # 工具函数
│   ├── static/                           # 静态资源
│   └── manifest.json                     # 应用配置
├── docs/                                  # 项目文档
│   ├── DEV_STANDARDS.md                  # 开发规范
│   ├── PROJECT_GUIDE.md                  # 项目指南
│   ├── SmartAdmin规范体系_v4/            # 完整规范体系
│   │   ├── 01-核心规范层/                # 编码、架构、数据规范
│   │   ├── 02-实施指南层/                # 开发指南、模板库
│   │   ├── 03-AI专用层/                  # AI开发指令、学习路径
│   │   ├── 04-质量保障层/                # 测试规范、评审规范
│   │   └── 05-治理运营层/                # 持续改进、团队培训
│   ├── 各业务模块文档/                    # 业务模块详细设计
│   │   ├── 门禁/                         # 门禁系统文档
│   │   ├── 考勤/                         # 考勤系统文档
│   │   ├── 消费/                         # 消费系统文档
│   │   ├── 智能视频/                     # 视频监控文档
│   │   └── 访客/                         # 访客管理文档
│   └── CHECKLISTS/                       # 开发检查清单
├── openspec/                              # OpenSpec 规格管理
│   ├── project.md                        # 项目上下文
│   ├── AGENTS.md                         # AI代理指南
│   └── specs/                            # 规格文档
├── 数据库SQL脚本/                          # 数据库初始化脚本
│   ├── mysql/                            # MySQL脚本
│   └── smart_device_tables.sql           # 设备相关表结构
├── scripts/                              # 构建和部署脚本
└── CLAUDE.md                             # AI开发指导文档
```

## AI Assistant Guidelines

### 开发优先级
1. **严格遵循项目规范**: 参考 docs/DEV_STANDARDS.md 和 SmartAdmin规范体系_v4
2. **使用检查清单**: 开发前、中、后都要检查对应清单（通用+业务专用）
3. **优先使用模板**: 从 PROJECT_GUIDE.md 中复制对应模板和代码示例
4. **保持代码质量**: 单元测试覆盖率≥80%，代码审查必须通过

### 开发约束和最佳实践

#### 架构约束
- **严格四层架构**: Controller → Service → Manager → DAO，禁止跨层访问
- **事务边界**: 事务管理必须在Service层，Manager层不得管理事务
- **依赖注入**: 强制使用 @Resource 注解，禁止使用 @Autowired
- **异常处理**: 使用 SmartException 及其子类，统一异常响应格式

#### 代码质量约束
- **参数验证**: Controller层必须使用 @Valid 进行参数校验
- **权限控制**: 所有接口必须使用 @SaCheckLogin 和 @SaCheckPermission
- **响应格式**: 统一使用 ResponseDTO 包装返回结果
- **日志记录**: 使用@Slf4j (Logger)，禁止 System.out.println
- **代码注释**: 所有public方法必须有完整的JavaDoc注释

#### 数据库约束
- **表命名**: 严格遵循 t_{business}_{entity} 格式
- **主键设计**: {table}_id 格式，BIGINT AUTO_INCREMENT
- **审计字段**: 必须包含 create_time, update_time, create_user_id, deleted_flag
- **软删除**: 使用 deleted_flag 字段，禁止物理删除
- **字符集**: 统一使用 utf8mb4，存储引擎 InnoDB

#### 前端约束
- **技术栈**: 必须使用 Vue 3 + Composition API + TypeScript
- **组件规范**: 遵循单一职责原则，使用 setup 语法糖
- **API封装**: 统一在 /@/api/ 目录下封装，使用 async/await
- **状态管理**: 使用 Pinia store，模块化管理
- **样式规范**: 使用 Less 预处理器，遵循主题系统

#### 缓存策略约束
- **L1缓存**: Caffeine本地缓存，默认5分钟过期
- **L2缓存**: Redis分布式缓存，默认30分钟过期
- **缓存模式**: 先更新数据库，再删除缓存（Cache Aside模式）
- **缓存键命名**: 使用统一的命名规范

#### 安全约束
- **认证**: 所有接口必须进行 Sa-Token 认证
- **权限**: 敏感操作必须进行细粒度权限验证
- **加密**: 用户密码使用 BCrypt，敏感数据使用 AES 加密
- **SQL安全**: 使用 MyBatis 参数绑定，防止SQL注入
- **XSS防护**: 前后端输入输出都必须进行XSS过滤

### 禁止操作
- ❌ 使用 @Autowired 进行依赖注入
- ❌ 在Controller层编写业务逻辑
- ❌ 物理删除数据
- ❌ 使用 System.out.println 输出日志
- ❌ 跨层直接访问 (如 Service → DAO)
- ❌ 在Manager层管理事务
- ❌ 硬编码配置信息
- ❌ 提交包含敏感信息的代码

### 必须操作
- ✅ 所有接口使用 @SaCheckPermission 权限控制
- ✅ 参数验证使用 @Valid 注解
- ✅ 统一使用 ResponseDTO 返回格式
- ✅ 异常处理使用 SmartException
- ✅ 日志记录使用@Slf4j
- ✅ 编写完整的单元测试
- ✅ 代码提交前运行检查清单
- ✅ 重要功能添加接口文档注释
