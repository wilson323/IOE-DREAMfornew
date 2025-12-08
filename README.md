# 🏢 IOE-DREAM 智慧园区一卡通管理平台

<div align="center">

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.8-brightgreen.svg)
![Vue](https://img.shields.io/badge/Vue-3.4-4FC08D.svg)
![Status](https://img.shields.io/badge/status-Enterprise%20Ready-success.svg)

**企业级智慧安防管理平台 | 多模态生物识别 + 一卡通 + 智能安防一体化解决方案**

[项目文档](documentation/) | [快速开始](#-快速开始) | [架构设计](documentation/architecture/) | [API文档](documentation/api/)

</div>

---

## 📋 项目简介

**IOE-DREAM**（Intelligent Operations & Enterprise - Digital Resource & Enterprise Application Management）是基于 **SmartAdmin** 框架构建的新一代**智慧园区一卡通管理平台**。

该平台专注于园区一卡通和生物识别安全管理，是国内首个集成多种生物识别技术（人脸、指纹、掌纹、虹膜、声纹等）并满足《网络安全-三级等保》、《数据安全》功能要求的企业级解决方案。

### ✨ 核心特性

- 🔐 **多模态生物识别**: 支持人脸、指纹、掌纹、虹膜、声纹等多种识别方式
- 🚪 **智能门禁控制**: 多层级权限管理，实时通行记录，异常行为检测
- 💳 **无感消费结算**: 刷脸/刷卡/手机NFC秒级支付，支持离线消费
- ⏰ **自动考勤管理**: 生物识别打卡，灵活排班，自动统计
- 👥 **智能访客管理**: 在线预约，审批流程，轨迹追踪
- 📹 **视频监控联动**: AI智能分析，异常检测，多系统联动
- 🛡️ **三级等保合规**: 满足国家网络安全等级保护要求
- 🏗️ **微服务架构**: 7个核心微服务，支持高并发、高可用、水平扩展

---

## 🏗️ 技术架构

### 技术栈

#### 后端技术栈
- **框架**: Spring Boot 3.5.8 + Spring Cloud 2025.0.0
- **语言**: Java 17 (LTS)
- **认证**: Sa-Token 1.44.0
- **ORM**: MyBatis-Plus 3.5.15
- **数据库**: MySQL 8.0+ / 国产数据库（达梦、金仓等）
- **缓存**: Redis + Caffeine（多级缓存）
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

| 微服务 | 端口 | 职责 | 状态 |
|--------|------|------|------|
| **ioedream-gateway-service** | 8080 | API网关，统一入口 | ✅ |
| **ioedream-common-service** | 8088 | 公共业务服务（用户、权限、字典等） | ✅ |
| **ioedream-device-comm-service** | 8087 | 设备通讯服务（协议适配、连接管理） | ✅ |
| **ioedream-oa-service** | 8089 | OA办公服务（工作流、通知等） | ✅ |
| **ioedream-access-service** | 8090 | 门禁管理服务 | ✅ |
| **ioedream-attendance-service** | 8091 | 考勤管理服务 | ✅ |
| **ioedream-video-service** | 8092 | 视频监控服务 | ✅ |
| **ioedream-consume-service** | 8094 | 消费管理服务 | ✅ |
| **ioedream-visitor-service** | 8095 | 访客管理服务 | ✅ |

### 项目结构

```
IOE-DREAM/
├── microservices/              # 微服务模块
│   ├── microservices-common/  # 公共JAR库（Entity、DAO、Manager等）
│   ├── ioedream-gateway-service/      # API网关
│   ├── ioedream-common-service/       # 公共业务服务
│   ├── ioedream-device-comm-service/  # 设备通讯服务
│   ├── ioedream-oa-service/           # OA办公服务
│   ├── ioedream-access-service/       # 门禁服务
│   ├── ioedream-attendance-service/   # 考勤服务
│   ├── ioedream-video-service/        # 视频服务
│   ├── ioedream-consume-service/      # 消费服务
│   └── ioedream-visitor-service/      # 访客服务
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

## 🚀 快速开始

> **💡 快速启动**: 
> - [开发环境快速启动](DEVELOPMENT_QUICK_START.md) ⭐ **推荐阅读**
> - [3步快速启动](QUICK_START.md)
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
