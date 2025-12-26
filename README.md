# 🏢 IOE-DREAM 智慧园区一卡通管理平台

<div align="center">

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.8-brightgreen.svg)
![Vue](https://img.shields.io/badge/Vue-3.4-4FC08D.svg)
![Status](https://img.shields.io/badge/status-Enterprise%20Ready-success.svg)

**企业级智慧安防管理平台 | 基于设备端的多模态生物识别 + 一卡通 + 智能安防一体化解决方案**

[项目文档](documentation/) | [快速开始](#-快速开始) | [架构设计](documentation/architecture/) | [API文档](documentation/api/) | [CI/运维索引](#-ci运维索引)

</div>

---

## 📋 项目简介

**IOE-DREAM**（Intelligent Operations & Enterprise - Digital Resource & Enterprise Application Management）是基于 **SmartAdmin** 框架构建的新一代**智慧园区一卡通管理平台**。

该平台专注于园区一卡通和生物识别安全管理，采用**边缘计算架构**，生物识别在设备端完成，软件端负责模板管理和数据分析。是国内首个集成多种生物识别技术（人脸、指纹、掌纹、虹膜、声纹等）并满足《网络安全-三级等保》、《数据安全》功能要求的企业级解决方案。

### ✨ 核心特性

- 🔐 **多模态生物识别**: 支持人脸、指纹、掌纹、虹膜、声纹等多种识别方式,识别在设备端完成
- 🚪 **智能门禁控制**: 多层级权限管理，实时通行记录，异常行为检测,采用边缘自主验证模式
- 💳 **无感消费结算**: 刷脸/刷卡/手机NFC秒级支付，支持离线消费,采用中心实时验证模式
- ⏰ **自动考勤管理**: 生物识别打卡，灵活排班，自动统计,采用边缘识别+中心计算模式
- 👥 **智能访客管理**: 在线预约，审批流程，轨迹追踪,采用混合验证模式
- 📹 **视频监控联动**: AI智能分析，异常检测，多系统联动,采用边缘AI计算模式
- 🛡️ **三级等保合规**: 满足国家网络安全等级保护要求
- 🏗️ **微服务架构**: 11个核心微服务（1个API网关 + 10个业务服务），支持高并发、高可用、水平扩展
- 🔍 **全局代码分析**: 系统性代码质量诊断与修复体系，支持架构分析、编译错误修复、代码质量检测等8大核心功能

---

## 🏗️ 技术架构

### 技术栈

#### 后端技术栈
- **框架**: Spring Boot 3.5.8 + Spring Cloud 2025.0.0
- **语言**: Java 17 (LTS)
- **认证**: Spring Security 6.x（JWT Bearer）
- **ORM**: MyBatis-Plus 3.5.15
- **数据库**: MySQL 8.0+ / 国产数据库（达梦、金仓等）
- **缓存**: Spring Cache + Redis + Caffeine（多级缓存）
- **分布式事务**: Seata（AT/SAGA模式）
- **容错机制**: Resilience4j（重试、熔断、限流、隔离）
- **监控指标**: Micrometer + Prometheus + Grafana
- **消息队列**: RabbitMQ / RocketMQ
- **服务注册**: Nacos
- **API文档**: Swagger / Knife4j

#### 前端技术栈
- **框架**: Vue 3.4 + Vite 5
- **UI组件**: Ant Design Vue 4.2
- **状态管理**: Pinia 2.1
- **路由**: Vue Router 4.3
- **HTTP客户端**: Axios 1.6
- **图表**: ECharts 5.4

#### 移动端技术栈
- **框架**: uni-app 3.0 (Vue 3)
- **支持平台**: H5、微信小程序、支付宝小程序、iOS、Android

### 微服务架构

| 微服务 | 端口 | 职责 | 设备交互模式 | 状态 |
|--------|------|------|-------------|------|
| **ioedream-gateway-service** | 8080 | API网关，统一入口 | - | ✅ |
| **ioedream-common-service** | 8088 | 公共业务服务（用户、权限、字典等） | - | ✅ |
| **ioedream-device-comm-service** | 8087 | 设备通讯服务（协议适配、连接管理） | ⚠️ 模板下发,不做识别 | ✅ |
| **ioedream-oa-service** | 8089 | OA办公服务（工作流、通知等） | - | ✅ |
| **ioedream-access-service** | 8090 | 门禁管理服务 | Mode 1: 边缘自主验证 | ✅ |
| **ioedream-attendance-service** | 8091 | 考勤管理服务 | Mode 3: 边缘识别+中心计算 | ✅ |
| **ioedream-video-service** | 8092 | 视频监控服务 | Mode 5: 边缘AI计算 | ✅ |
| **ioedream-database-service** | 8093 | 数据库管理服务（备份恢复、性能监控） | - | ✅ |
| **ioedream-consume-service** | 8094 | 消费管理服务 | Mode 2: 中心实时验证 | ✅ |
| **ioedream-visitor-service** | 8095 | 访客管理服务 | Mode 4: 混合验证 | ✅ |
| **ioedream-biometric-service** | 8096 | 生物模板管理服务（模板存储、特征提取、设备下发） | ⚠️ 仅管理数据,不做识别 | ✅ |

#### 🔑 核心服务说明

**ioedream-biometric-service (8096) - 生物模板管理服务**

**职责定位**:
- ✅ **模板管理**: 生物特征模板CRUD（人脸、指纹、掌纹等）
- ✅ **特征提取**: 用户入职时从照片提取512维特征向量
- ✅ **设备同步**: ⭐ 核心职责 - 模板下发到边缘设备
- ✅ **权限联动**: 根据用户权限智能同步到相关设备
- ✅ **模板压缩**: 特征向量压缩存储
- ✅ **版本管理**: 模板更新历史管理

**⚠️ 重要说明**:
```
❓ 该服务负责生物识别吗？
✖️ 不！生物识别由设备端完成

❓ 那该服务做什么？
✅ 只管理模板数据，并下发给设备

【正确的架构流程】

1. 人员入职时：
   用户 → 上传人脸照片 → biometric-service
   biometric-service → 提取512维特征向量 → 存入数据库
   biometric-service → 查询用户有权限的区域 → 找出所有相关门禁设备
   biometric-service → 下发模板到这些设备 ⭐ 核心

2. 实时通行时：
   设备 → 采集人脸图像 → 设备内嵌算法提取特征
   设备 → 与本地存储的模板1:N比对 ⭐ 全部在设备端
   设备 → 匹配成功 → 检查本地权限表 → 开门
   设备 → 批量上传通行记录到软件

3. 人员离职时：
   biometric-service → 从数据库删除模板
   biometric-service → 从所有设备删除模板 ⭐ 防止离职人员仍可通行
```

**ioedream-database-service (8093) - 数据库管理服务**

**职责定位**:
- ✅ **数据备份**: 定时全量/增量备份
- ✅ **数据恢复**: 备份文件恢复
- ✅ **性能监控**: 慢查询/连接数监控
- ✅ **数据迁移**: 数据导入导出
- ✅ **健康检查**: 数据库连接状态监控
- ✅ **容量管理**: 数据库容量监控和告警

**核心功能**:
- 自动备份策略（全量备份 + 增量备份）
- 备份文件管理和清理
- 慢查询分析和优化建议
- 连接池监控和调优
- 数据迁移工具和脚本

### 项目结构

```
IOE-DREAM/
├── microservices/              # 微服务模块
│   ├── microservices-common-core/  # 公共库最小稳定内核（ResponseDTO/异常/常量等）
│   ├── microservices-common/       # 公共库聚合（逐步拆为 common-spring/common-starter-*/domain-api）
│   ├── ioedream-gateway-service/      # API网关
│   ├── ioedream-common-service/       # 公共业务服务
│   ├── ioedream-device-comm-service/  # 设备通讯服务
│   ├── ioedream-oa-service/           # OA办公服务
│   ├── ioedream-access-service/       # 门禁服务
│   ├── ioedream-attendance-service/   # 考勤服务
│   ├── ioedream-video-service/        # 视频服务
│   ├── ioedream-database-service/     # 数据库管理服务 🆕
│   ├── ioedream-consume-service/      # 消费服务
│   ├── ioedream-visitor-service/      # 访客服务
│   └── ioedream-biometric-service/   # 生物模板管理服务 🆕
├── smart-admin-web-javascript/ # 前端管理后台（Vue3）
├── smart-app/                  # 移动端应用（uni-app）
├── documentation/              # 项目文档
│   ├── architecture/          # 架构设计文档
│   ├── api/                   # API接口文档
│   ├── business/              # 业务需求文档
│   ├── technical/             # 技术文档
│   └── deployment/            # 部署文档
├── database-scripts/           # 数据库脚本
├── scripts/                    # 自动化脚本
├── docker/                     # Docker配置
└── deployment/                 # 部署配置
```

---

## 🧰 CI/运维索引

- 本地 Action 使用与运行环境清单：`documentation/technical/LOCAL_ACTIONS_ENV_CHECKLIST.md`
- Runner 预装脚本清单（Windows/Linux）：`documentation/technical/RUNNER_PREINSTALL_CHECKLIST.md`
- Runner 环境自检脚本：`scripts/runner-env-check.ps1`

---

## 📐 技术栈现代化升级（2025-01-30）

### ✨ Jakarta EE 9+ 迁移完成

**迁移状态**: ✅ 100%完成（2025-01-30）

IOE-DREAM项目已完成从Java EE到Jakarta EE的全面升级，所有代码已迁移至Jakarta EE 9+规范。

#### 迁移范围

| Java EE 包 | Jakarta EE 包 | 迁移状态 |
|-----------|--------------|---------|
| `javax.annotation.*` | `jakarta.annotation.*` | ✅ 100% |
| `javax.persistence.*` | `jakarta.persistence.*` | ✅ 100% |
| `javax.validation.*` | `jakarta.validation.*` | ✅ 100% |

#### 代码示例

```java
// ✅ 正确：使用 Jakarta EE
import jakarta.annotation.Resource;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;

// ❌ 禁止：使用 Java EE（已过时）
import javax.annotation.Resource;  // ❌ 已禁止
import javax.persistence.Entity;   // ❌ 已禁止
```

**自动化检查**: CI/CD流水线会自动检查所有代码，发现使用`javax.*`包将拒绝合并

### ✨ OpenAPI 3.0 统一完成

**迁移状态**: ✅ 100%完成（2025-01-30）

项目已统一使用OpenAPI 3.0规范，所有Swagger注解已更新。

#### 统一范围

| OpenAPI 3.1 API | OpenAPI 3.0 API | 修复数量 |
|----------------|----------------|---------|
| `requiredMode = Schema.RequiredMode.REQUIRED` | `required = true` | 11处 |

#### 代码示例

```java
// ✅ 正确：使用 OpenAPI 3.0
@Schema(description = "用户名", required = true, example = "admin")
private String username;

// ❌ 禁止：使用 OpenAPI 3.1（不兼容）
@Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
private String username;
```

**自动化检查**: CI/CD流水线会自动检查所有代码，发现使用`requiredMode`将拒绝合并

### 📊 升级成果

```
✅ Jakarta EE 迁移：100%完成
✅ OpenAPI 3.0 统一：100%完成
✅ 微服务编译：100%成功（5/5服务）
✅ CI/CD自动化检查：已启用
```

### 📚 相关文档

- **API版本规范**: [CLAUDE.md - API版本规范](CLAUDE.md#-api版本规范-2025-01-30新增)
- **GitHub Actions检查**: [`.github/workflows/api-version-check.yml`](.github/workflows/api-version-check.yml)
- **Jakarta EE规范**: [Jakarta EE 9 Documentation](https://jakarta.ee/specifications/)
- **OpenAPI规范**: [OpenAPI 3.0 Specification](https://swagger.io/specification/)

---

## 🚀 快速开始

> **💡 快速启动**: 
> - [开发环境快速启动](documentation/02-开发指南/DEVELOPMENT_QUICK_START.md) ⭐ **推荐阅读**
> - [3步快速启动](documentation/02-开发指南/QUICK_START.md)
> - [详细启动指南](documentation/technical/DEVELOPMENT_STARTUP_GUIDE.md)

### 环境要求

- **JDK**: 17+
- **Maven**: 3.8+
- **Node.js**: 18+
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Nacos**: 2.2.0+
- **Docker**: 20.10+
- **Docker Compose**: 2.0+

### API 基线与兼容窗口（30 天）

- **Canonical API 前缀**：统一使用 `/api/v1`
- **网关地址**：默认 `http://localhost:8080`
- **鉴权方式**：Spring Security（JWT Bearer），请求头 `Authorization: Bearer <token>`
- **内部同步调用策略**：默认经网关（`GatewayServiceClient`），同域高频热路径允许白名单直连（`DirectServiceClient`）
  - 详见：`documentation/architecture/INTERNAL_CALL_STRATEGY.md`
  - 公共库拆分与依赖方向：`documentation/architecture/COMMON_LIBRARY_SPLIT.md`
- **兼容窗口**：legacy 路由与 legacy 登录路径保留 30 天，窗口结束后将下线（建议尽快迁移到 canonical）
  - legacy 业务前缀（兼容）：`/access/**`、`/attendance/**`、`/consume/**`、`/visitor/**`、`/video/**`、`/device/**`
  - legacy 登录前缀（兼容）：`/login/**`

### 本地配置（必须）

`docker-compose-all.yml` 已移除默认口令占位，必须通过环境变量注入敏感配置：

- 复制模板：`.env.template` → `.env`（`.env` 已加入 `.gitignore`，不要提交到仓库）
- 至少配置：`MYSQL_ROOT_PASSWORD`、`REDIS_PASSWORD`、`NACOS_USERNAME`、`NACOS_PASSWORD`、`NACOS_AUTH_TOKEN`、`JWT_SECRET`

### 一键部署（自动检测并初始化数据库）

```powershell
# 方法1: 使用自动化部署脚本（推荐 - 自动检测并初始化数据库）
.\scripts\deploy.ps1

# 方法2: 使用专用自动化部署脚本
.\scripts\deploy-auto.ps1

# 方法3: 使用Docker Compose（需要手动初始化数据库）
docker-compose -f docker-compose-all.yml up -d
.\scripts\init-nacos-database.ps1  # 手动初始化数据库
```

**重要提示**: 
- ✅ `deploy.ps1` 和 `deploy-auto.ps1` **会自动检测并初始化nacos数据库**
- ⚠️ 如果使用 `docker-compose` 直接启动，需要先执行 `.\scripts\init-nacos-database.ps1`

### 1. 克隆项目

```bash
git clone https://github.com/wilson323/IOE-DREAMfornew.git
cd IOE-DREAM
```

### 2. 数据库初始化

```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE ioedream DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据库脚本
mysql -u root -p ioedream < database-scripts/common-service/00-database-init.sql
```

### 3. 配置Nacos

1. 启动Nacos服务
2. 导入配置到Nacos（配置文件在 `deployment/nacos/` 目录）

### 4. 启动后端服务

```powershell
# Windows PowerShell
cd microservices
mvn clean install -DskipTests

# 启动网关服务
cd ioedream-gateway-service
mvn spring-boot:run

# 启动其他微服务（按顺序）
# 1. ioedream-common-service
# 2. ioedream-device-comm-service
# 3. ioedream-oa-service
# 4. 业务服务（可并行启动）
```

### 5. 启动前端

```bash
cd smart-admin-web-javascript
npm install
npm run localhost  # 或 npm run dev
```

### 6. 启动移动端（可选）

```bash
cd smart-app
npm install
# 使用HBuilderX或uni-app CLI运行
```

### 一键启动脚本

```powershell
# 方式一：完整启动脚本（推荐，功能最全面）
# ✅ 自动检测并启动MySQL/Redis/Nacos（如果未运行）
.\scripts\start-all-complete.ps1                    # 启动所有服务
.\scripts\start-all-complete.ps1 -BackendOnly       # 仅启动后端
.\scripts\start-all-complete.ps1 -FrontendOnly      # 仅启动前端
.\scripts\start-all-complete.ps1 -MobileOnly        # 仅启动移动端
.\scripts\start-all-complete.ps1 -CheckOnly         # 仅检查服务状态

# 方式二：快速启动脚本（简化版）
.\scripts\quick-start.ps1                           # 启动所有服务
.\scripts\quick-start.ps1 -Backend                  # 仅启动后端
.\scripts\quick-start.ps1 -Frontend                 # 仅启动前端
.\scripts\quick-start.ps1 -Mobile                   # 仅启动移动端
```

**详细使用说明**:
- [启动脚本使用说明](scripts/README_START.md)
- [开发环境启动指南](documentation/technical/DEVELOPMENT_STARTUP_GUIDE.md) ⭐ **推荐阅读**

### 集成测试环境（Docker Compose）

对于集成测试和CI/CD环境，项目提供了独立的Docker Compose配置：

**环境配置** (`deployment/test-environment/.env.test`):
```bash
# 服务端口配置
MYSQL_PORT=3307
REDIS_PORT=6380
NACOS_PORT=8849

# 数据库配置
MYSQL_ROOT_PASSWORD=test_root_password
MYSQL_DATABASE=ioedream_test

# Redis配置
REDIS_PASSWORD=test_redis_password

# Nacos配置
NACOS_USERNAME=nacos
NACOS_PASSWORD=nacos
NACOS_AUTH_TOKEN=nacos_test_token
```

**快速启动测试环境**:
```bash
# 1. 进入测试环境目录
cd deployment/test-environment

# 2. 启动测试服务
docker-compose up -d

# 3. 验证服务状态
docker-compose ps

# 4. 查看服务日志
docker-compose logs -f

# 5. 停止测试环境
docker-compose down
```

**服务可用性验证**:
```bash
# MySQL服务
mysql -h 127.0.0.1 -P 3307 -u root -p

# Redis服务
redis-cli -h 127.0.0.1 -p 6380 -a test_redis_password ping

# Nacos控制台
http://localhost:8849/nacos
```

**故障排除**:
- 端口冲突：修改`.env.test`中的端口配置
- 容器启动失败：`docker-compose logs <service_name>`查看详细日志
- 数据库连接失败：确认MySQL容器已完全启动（约30秒）

**详细文档**: [测试环境完整指南](deployment/test-environment/README.md)

---

## 🔍 全局代码分析系统

IOE-DREAM 集成了系统性的代码质量诊断与修复体系，提供企业级代码质量保障：

### 核心功能模块

| 功能模块 | 主要职责 | 技术特点 |
|---------|---------|---------|
| **架构问题诊断** | 识别模块依赖关系、循环依赖检测、四层架构合规性验证 | 基于图论算法的依赖分析 |
| **编译错误修复** | 区分真实编译错误和IDE诊断、字符编码问题检测 | 智能错误分类与优先级排序 |
| **代码质量检测** | 注解违规检测、Lombok配置验证、代码规范检查 | 静态代码分析与质量评分 |
| **技术迁移支持** | Jakarta EE迁移检测、包名替换、依赖兼容性验证 | 自动化迁移工具与验证 |
| **模块拆分优化** | 公共模块职责边界分析、重构建议生成 | 模块化架构分析 |
| **自动化修复** | 批量注解替换、编码转换、包名替换 | 安全的自动修复与回滚机制 |
| **持续监控** | Git钩子集成、CI/CD质量检查、趋势分析 | 预防性质量保障 |
| **效果验证** | 修复前后对比、健康度评分、ROI分析 | 量化的质量改善报告 |

### CLI工具快速开始

**安装**:
```bash
# 使用 pip 安装（推荐）
pip install ioedream-code-analyzer

# 或从源码安装
cd ioedream-code-analyzer
poetry install
```

**基本使用**:
```bash
# 分析项目
ioedream-analyzer analyze /path/to/IOE-DREAM

# 修复问题（自动创建备份）
ioedream-analyzer fix /path/to/IOE-DREAM --auto-backup

# 生成HTML报告
ioedream-analyzer report /path/to/IOE-DREAM --format html

# 查看完整帮助
ioedream-analyzer --help
```

**开发环境设置**:
```bash
# 安装开发依赖
poetry install --with dev

# 安装 pre-commit 钩子
pre-commit install

# 运行测试套件
pytest

# 运行属性测试（验证15个核心正确性属性）
pytest tests/property/

# 代码格式化
black src tests
isort src tests

# 类型检查
mypy src
```

### 技术亮点

- ✅ **系统性诊断**: 覆盖架构、编译、质量、迁移、模块等8个维度
- ✅ **智能修复**: 支持自动化修复常见代码问题，提供安全回滚机制
- ✅ **持续监控**: 集成Git钩子和CI/CD流水线，预防质量问题
- ✅ **量化评估**: 提供架构健康度评分和质量改善ROI分析
- ✅ **企业级标准**: 符合Spring Boot 3.5.8 + Jakarta EE规范要求
- ✅ **正确性保证**: 基于15个核心正确性属性的属性测试验证
- ✅ **高性能**: 单个微服务分析<30秒，全项目<5分钟，内存占用<2GB
- ✅ **独立工具**: Python CLI工具，可独立安装和使用，无需Java环境

### 详细文档

完整的使用指南和技术文档请参考：
- **[CLI工具README](ioedream-code-analyzer/README.md)** - 安装、使用和开发指南
- **[系统设计文档](documentation/technical/GLOBAL_CODE_ANALYSIS_SYSTEM.md)** - 架构设计和技术规范
- **[需求规格](.kiro/specs/global-code-analysis/requirements.md)** - 详细需求和验收标准
- **[设计文档](.kiro/specs/global-code-analysis/design.md)** - 架构设计和实现方案
- **[任务清单](.kiro/specs/global-code-analysis/tasks.md)** - 实施任务和里程碑
- **[API文档](documentation/api/global-code-analysis-api.md)** - 接口规范和使用示例
- **[专家技能](.claude/skills/global-code-analysis-expert.md)** - AI专家技能定义

---

## 🤖 AI Skills System

IOE-DREAM 集成了专业的AI辅助开发技能系统，提供30个专业AI技能支持智能开发：

### 技能分类

**P0优先级守护技能**（架构质量保障）:
- `four-tier-architecture-guardian` - 四层架构守护专家
- `spring-boot-jakarta-guardian` - Spring Boot 3.5和Jakarta包名规范守护
- `access-control-business-specialist` - 门禁系统业务逻辑专家
- `code-quality-protector` - 代码质量和编码规范守护专家
- `openspec-compliance-specialist` - OpenSpec规范遵循专家
- `init-architect` - 自适应初始化专家

**核心开发技能**（22个）:
- 业务模块专家：门禁、考勤、消费、视频、访客、设备通讯
- 技术专项：数据库迁移、配置安全、异常处理、分布式追踪、Nacos服务发现
- 架构支持：RESTful API设计、生物识别架构、前端/移动端API开发

**扩展技能**（4个）:
- 工作流专家、日志规范守护、包结构守护、文档质量管理

### 技能使用方式

```bash
# 通过Claude Code CLI调用
claude-code --skill four-tier-architecture-guardian
claude-code --skill access-service-specialist

# 或通过项目.claude/skills/目录管理
.claude/skills/
├── P0-guardians/     # P0优先级守护技能
├── core/             # 核心开发技能
└── extended/         # 扩展技能
```

**详细文档**:
- **[技能系统总览](.claude/skills/README.md)** - 完整技能列表和使用指南
- **[技能使用培训](.claude/skills/deployment/training/P0_SKILLS_TRAINING_CURRICULUM.md)** - P0技能培训课程
- **[技能监控](.claude/skills/deployment/monitoring/SKILLS_USAGE_MONITORING.md)** - 技能使用监控

### 技术栈统一规范

所有AI技能严格遵循统一技术栈规范：
- **后端**: Spring Boot 3.5.8 + Spring Cloud 2025.0.0 + Java 17
- **前端**: Vue 3.4 + Ant Design Vue 4.2
- **移动端**: uni-app 3.0 (Vue 3)
- **数据库**: MySQL 8.0+ / MyBatis-Plus 3.5.15
- **规范**: Jakarta EE 9+ / OpenAPI 3.0

---

## 🔄 设备交互架构

IOE-DREAM采用**边缘计算优先**的架构设计，根据不同业务场景选择最优的设备交互模式：

### ⭐ 核心设计理念

```
设备端识别，软件端管理

1. 边缘智能优先: 门禁设备端完成验证，降低服务器压力
2. 数据安全第一: 消费设备不存余额，防止篡改
3. 离线能力保障: 关键场景支持离线工作
4. 中心计算精准: 考勤排班+规则在软件端，灵活可控
5. AI边缘推理: 视频设备本地识别，只上传结果
```

### 📊 5种设备交互模式

#### Mode 1: 边缘自主验证 (门禁系统)

**核心理念**: 设备端识别，软件端管理

```
【数据下发】软件 → 设备
  ├─ 生物模板（人脸/指纹特征向量）
  ├─ 权限数据（时间段/区域/有效期）
  └─ 人员信息（姓名/工号）

【实时通行】设备端完全自主 ⚠️ 无需联网
  ├─ 本地识别: 设备内嵌算法1:N比对
  ├─ 本地验证: 检查本地权限表
  └─ 本地控制: 直接开门（<1秒）

【事后上传】设备 → 软件
  └─ 批量上传通行记录（每分钟或100条）
```

**技术优势**:
- ✅ 离线可用: 网络中断不影响通行
- ✅ 秒级响应: 识别+验证+开门<1秒
- ✅ 降低压力: 1000次通行只需处理记录存储

#### Mode 2: 中心实时验证 (消费系统)

**核心理念**: 设备采集，服务器验证

```
【数据下发】软件 → 设备
  ├─ 生物模板（可选，部分设备不需要）
  └─ 设备配置（消费单价、限额等）

【实时消费】设备 ⇄ 软件（必须在线）
  设备端采集 → 上传biometricData/cardNo → 服务器验证
  服务器处理 → 识别用户 → 检查余额 → 执行扣款
  服务器返回 → 扣款结果 → 设备显示+语音提示

【离线降级】设备端处理
  ⚠️ 网络故障时: 支持有限次数的离线消费
  ├─ 白名单验证: 仅允许白名单用户
  ├─ 固定额度: 单次消费固定金额
  └─ 事后补录: 网络恢复后上传补录
```

**技术优势**:
- ✅ 数据安全: 余额存储在服务器，防止篡改
- ✅ 实时补贴: 可立即发放补贴到账户
- ✅ 灵活定价: 可根据时段/菜品动态定价

#### Mode 3: 边缘识别+中心计算 (考勤系统)

**核心理念**: 设备识别，服务器计算

```
【数据下发】软件 → 设备
  ├─ 生物模板
  ├─ 基础排班信息（仅当日）
  └─ 人员授权列表

【实时打卡】设备端识别
  ├─ 本地识别: 人脸/指纹1:N比对
  ├─ 上传打卡: 实时上传userId+time+location
  └─ 快速反馈: 设备端显示"打卡成功"

【服务器计算】软件端处理
  ├─ 排班匹配: 根据用户排班规则判断状态
  ├─ 考勤统计: 出勤/迟到/早退/旷工
  ├─ 异常检测: 跨设备打卡、频繁打卡告警
  └─ 数据推送: WebSocket推送实时考勤结果
```

**技术优势**:
- ✅ 识别速度快: 设备端识别<1秒
- ✅ 规则灵活: 排班规则在软件端，可随时调整
- ✅ 数据准确: 服务器计算，防止设备端篡改

#### Mode 4: 混合验证 (访客系统)

**核心理念**: 临时访客中心验证，常客边缘验证

```
【临时访客】中心实时验证
  预约申请 → 审批通过 → 生成访客码
  到访时扫码 → 服务器验证 → 现场采集人脸
  服务器生成临时模板 → 下发设备 → 设置有效期
  访客通行 → 实时上报 → 服务器记录轨迹
  访问结束 → 自动失效 → 从设备删除模板

【常客】边缘验证
  长期合作伙伴 → 申请常客权限 → 审批通过
  采集生物特征 → 下发所有授权设备
  日常通行 → 设备端验证 → 批量上传记录
  权限到期 → 自动失效 → 从设备删除
```

**技术优势**:
- ✅ 灵活控制: 根据安全等级选择验证模式
- ✅ 效率平衡: 常客快速通行，临时访客严格验证
- ✅ 轨迹追踪: 访客完整行为轨迹记录

#### Mode 5: 边缘AI计算 (视频监控)

**核心理念**: 设备端AI分析，服务器端管理

```
【模板下发】软件 → 设备
  ├─ 重点人员底库（黑名单/VIP/员工）
  ├─ AI模型更新（定期推送新版本）
  └─ 告警规则配置（区域入侵/徘徊检测）

【实时分析】设备端AI处理
  视频采集 → AI芯片分析 → 人脸检测+识别
            ↓
  行为分析 → 异常检测（徘徊/聚集/越界）
            ↓
  结构化数据 → 上传服务器

【服务器处理】软件端
  接收结构化数据 → 存储（人脸抓拍/行为事件）
  告警规则匹配 → 实时推送告警
  人脸检索 → 以图搜图/轨迹追踪
  视频联动 → 告警时调取原始视频

【原始视频】设备端存储
  ⚠️ 原始视频不上传，设备端录像7-30天
  ⚠️ 只有告警/案件时，才回调原始视频
```

**技术优势**:
- ✅ 带宽节省: 只上传结构化数据，节省>95%带宽
- ✅ 实时响应: 设备端AI分析，告警延迟<1秒
- ✅ 隐私保护: 原始视频不上传，符合隐私法规

### 📖 详细文档

- **[完整架构方案](documentation/architecture/ENTERPRISE_REFACTORING_COMPLETE_SOLUTION.md)** - 企业级架构重构完整方案
- **[系统架构设计](documentation/architecture/01-系统架构设计文档.md)** - 系统架构设计文档
- **[设备交互模式详解](documentation/GLOBAL_DOCUMENT_CONSISTENCY_ANALYSIS_REPORT.md)** - 全局文档一致性分析报告

---

## 📚 文档导航

### 🎯 快速导航

- **📖 [项目快速开始](documentation/technical/00-快速开始/)** - 新手入门指南
- **🏗️ [架构设计文档](documentation/architecture/)** - 系统架构详解
- **💻 [开发规范](documentation/technical/repowiki/zh/content/开发规范体系/)** - 编码规范和最佳实践
- **🔗 [API文档](documentation/api/)** - 接口文档和示例
- **🚀 [部署指南](documentation/deployment/)** - 部署和运维文档

### 📋 核心文档

- **[CLAUDE.md](CLAUDE.md)** - 项目核心指导文档（架构规范、开发规范）
- **[项目状态](documentation/project/README_PROJECT_STATUS.md)** - 项目完成度和状态
- **[开发规范](documentation/technical/UNIFIED_DEVELOPMENT_STANDARDS.md)** - 统一开发标准
- **🔍 [全局代码分析系统](.kiro/specs/global-code-analysis/)** - 代码质量诊断与修复体系
  - **[需求规格](.kiro/specs/global-code-analysis/requirements.md)** - 系统需求和验收标准
  - **[设计文档](.kiro/specs/global-code-analysis/design.md)** - 架构设计和实现方案
  - **[任务清单](.kiro/specs/global-code-analysis/tasks.md)** - 实施任务和里程碑
  - **[API文档](documentation/api/global-code-analysis-api.md)** - 接口规范和使用示例
  - **[专家技能](.claude/skills/global-code-analysis-expert.md)** - AI专家技能定义
  - **[CLI工具](ioedream-code-analyzer/README.md)** - 独立命令行工具使用指南

### 🔍 按模块查找

- **🚪 [门禁模块](documentation/03-业务模块/门禁/)** - 门禁系统文档
- **⏰ [考勤模块](documentation/business/)** - 考勤系统文档
- **💳 [消费模块](documentation/business/)** - 消费系统文档
- **👥 [访客模块](documentation/business/)** - 访客系统文档
- **📹 [视频模块](documentation/business/)** - 视频监控文档
- **🏢 [OA工作流](documentation/03-业务模块/OA工作流/)** - OA系统文档

---

## 🎯 核心功能

### 1. 智能门禁系统
- 多模态生物识别（人脸、指纹、卡片）
- 多层级权限管理
- 实时通行记录
- 异常行为检测和报警

### 2. 消费管理系统
- 无感支付（刷脸/刷卡/NFC）
- 离线消费支持
- 账户管理和充值
- 消费统计和报表

### 3. 考勤管理系统
- 生物识别打卡
- 灵活排班管理
- 自动考勤统计
- 异常考勤分析

### 4. 访客管理系统
- 在线预约和审批
- 人脸识别登记
- 临时权限发放
- 访客轨迹追踪

### 5. 视频监控系统
- AI智能分析
- 异常行为检测
- 多系统联动
- 录像回放和检索

### 6. OA办公系统
- 工作流引擎
- 通知公告
- 企业信息管理
- 审批流程

---

## 🏆 项目亮点

### 企业级特性

- ✅ **高可用架构**: 微服务架构，支持水平扩展
- ✅ **多级缓存**: L1本地缓存 + L2 Redis缓存 + L3网关缓存
- ✅ **分布式事务**: SAGA模式，确保数据一致性
- ✅ **服务降级熔断**: 完善的容错机制
- ✅ **监控告警**: 完整的监控和告警体系
- ✅ **安全合规**: 满足三级等保要求

### 代码质量

- ✅ **四层架构**: Controller → Service → Manager → DAO
- ✅ **统一规范**: 严格的编码规范和代码审查
- ✅ **高测试覆盖率**: 单元测试覆盖率 ≥ 80%
- ✅ **完整文档**: 详细的开发文档和API文档

### 开发体验

- ✅ **快速开发**: 基于SmartAdmin框架，开箱即用
- ✅ **代码生成**: 支持在线代码生成
- ✅ **多环境支持**: 开发、测试、预发布、生产环境
- ✅ **热更新**: 前端支持热更新，后端支持热部署

---

## 🔧 开发规范

### 架构规范

- **四层架构**: 严格遵循 Controller → Service → Manager → DAO
- **依赖注入**: 统一使用 `@Resource`，禁止使用 `@Autowired`
- **DAO命名**: 统一使用 `Dao` 后缀，禁止使用 `Repository`
- **事务管理**: 事务边界在Service层，使用 `@Transactional`

### 代码规范

- **Java编码**: 遵循PEP 8和项目规范
- **Vue3规范**: 使用Composition API，遵循Vue3最佳实践
- **命名规范**: 统一的命名约定和代码风格
- **注释规范**: 完整的函数级注释和文档

### 开发工作流

**OpenSpec规范驱动开发**（Doc First）:
- ✅ **文档优先**: 任何架构变更/模块拆分/依赖治理必须先更新文档
- ✅ **OpenSpec提案**: 必须先创建OpenSpec提案并通过评审，再进入代码实施
- ✅ **模板自定义**: 支持自定义OpenSpec模板覆盖默认配置

**模板加载优先级**:
```
用户自定义模板 (.spec-workflow/user-templates/)
    ↓ (未找到)
项目默认模板 (.spec-workflow/templates/)
    ↓ (未找到)
系统内置模板
```

**自定义模板示例**:
```yaml
# .spec-workflow/user-templates/custom-proposal.md
name: 自定义提案模板
description: 适用于特定类型的提案
sections:
  - name: 业务背景
    required: true
  - name: 技术方案
    required: true
  - name: 验收标准
    required: true
```

**详细文档**:
- **[OpenSpec工作流](.spec-workflow/README.md)** - 完整工作流指南
- **[模板自定义指南](.spec-workflow/user-templates/README.md)** - 模板配置说明
- **[OpenSpec Agents文档](.spec-workflow/AGENTS.md)** - Agent使用指南

详细规范请查看: [开发规范文档](documentation/technical/UNIFIED_DEVELOPMENT_STANDARDS.md)

---

## 🚀 部署

### Docker部署（自动数据库初始化）

```powershell
# 一键部署（推荐 - 自动检测并初始化数据库）
.\scripts\deploy.ps1

# 或使用专用自动化部署脚本
.\scripts\deploy-auto.ps1

# 手动部署（需要手动初始化数据库）
docker-compose -f docker-compose-all.yml build
docker-compose -f docker-compose-all.yml up -d mysql redis
.\scripts\init-nacos-database.ps1  # 手动初始化数据库
docker-compose -f docker-compose-all.yml up -d
```

**自动化功能**:
- ✅ 自动检测MySQL容器状态
- ✅ 自动检测nacos数据库是否存在
- ✅ 自动检测nacos数据库表结构是否已初始化
- ✅ 如果未初始化，自动执行初始化
- ✅ 验证初始化结果

### Kubernetes部署

```bash
# 部署到K8s
kubectl apply -f deployment/k8s/
```

详细部署文档: [部署指南](documentation/deployment/)

---

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码提交规范

- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建/工具相关

---

## 📄 许可证

本项目基于 [MIT License](LICENSE) 开源。

---

## 👥 团队

- **架构师团队**: 老王 + 技术专家团队
- **开发团队**: IOE-DREAM开发团队
- **基于**: [SmartAdmin](https://smartadmin.vip) by 1024创新实验室

---

## 📞 联系方式

- **项目地址**: https://github.com/wilson323/IOE-DREAMfornew
- **问题反馈**: [GitHub Issues](https://github.com/wilson323/IOE-DREAMfornew/issues)
- **文档中心**: [项目文档](documentation/)

---

## 🙏 致谢

- 感谢 [SmartAdmin](https://smartadmin.vip) 提供优秀的开发框架
- 感谢 [1024创新实验室](https://www.1024lab.net/) 的开源贡献
- 感谢所有贡献者的支持

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给一个Star支持一下！**

Made with ❤️ by IOE-DREAM Team

</div>
