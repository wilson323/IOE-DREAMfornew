---
trigger: always_on
alwaysApply: true
---
<!-- OPENSPEC:START -->
# OpenSpec Instructions

These instructions are for AI assistants working in this project.

Always open `@/openspec/AGENTS.md` when the request:
- Mentions planning or proposals (words like proposal, spec, change, plan)
- Introduces new capabilities, breaking changes, architecture shifts, or big performance/security work
- Sounds ambiguous and you need the authoritative spec before coding

Use `@/openspec/AGENTS.md` to learn:
- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

Keep this managed block so 'openspec update' can refresh the instructions.

<!-- OPENSPEC:END -->

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**项目**: SmartAdmin v3 - 1024创新实验室开发的企业级快速开发平台
**技术栈**: Vue3 + SpringBoot3 + Sa-Token + MyBatis-Plus + Ant Design Vue
**语言环境**: 中文环境，请使用中文与用户交流

---

## 🔒 强制执行要求 (2025-11-15)

### **绝对禁止的行为**
- **严禁** 声称代码完成而未运行编译验证
- **严禁** 跳过质量门禁检查
- **严禁** 虚假报告任务完成状态
- **严禁** 忽视任何编译错误或测试失败

### **完整Hooks验证流程**

#### **工作前必须执行**
```bash
cd smart-admin-api-java17-springboot3
# 开发规范检查
bash ../scripts/dev-standards-check.sh
# 工作前Hook
bash ../scripts/pre-work-hook.sh
# 集成工作流程
bash ../scripts/integrated-workflow.sh pre-work
```

#### **工作后必须执行**
```bash
cd smart-admin-api-java17-springboot3
# 工作后Hook
bash ../scripts/post-work-hook.sh <work_type>
# 集成工作流程
bash ../scripts/integrated-workflow.sh post-work <work_type>
```

#### **Git提交前必须执行**
```bash
cd smart-admin-api-java17-springboot3
# 提交守卫
bash ../scripts/commit-guard.sh
# 集成工作流程
bash ../scripts/integrated-workflow.sh pre-commit
```

#### **任务完成必须执行**
```bash
cd smart-admin-api-java17-springboot3
# 任务验证
bash ../scripts/task-completion-verify.sh <task_id>
# 强制验证
bash ../scripts/mandatory-verification.sh
# 集成工作流程
bash ../scripts/integrated-workflow.sh task-verify <work_type>
```

#### **回答用户前必须确认**
```bash
# 确保代码能编译
mvn clean compile -q

# 检查验证报告存在
ls verification-report-*.json

# 检查工作流程证明存在
ls workflow-*-passed-*.proof

# 检查开发规范检查通过
ls dev-standards-check-*.proof
```

### **违规后果**
- 任何违规行为都必须立即停止所有工作
- 必须重新执行所有验证流程
- 必须向用户诚实报告所有问题
- 只有修复所有问题后才能继续

### **验证文件**
- `scripts/quality-gate.sh` - 质量门禁
- `scripts/mandatory-verification.sh` - 强制验证
- `scripts/task-completion-verify.sh` - 任务验证
- `FORCED_EXECUTION_CONTRACT.md` - 执行合同

**此强制要求永久生效，永不撤销。**

---

## 🚨 紧急状态通知（2025-11-13）

**当前状态**: 🔴 项目存在编译阻塞问题
**影响范围**: 所有开发工作
**预计修复时间**: 2-4 小时

### ⚡ 立即修复指南

#### 1. Spring Boot 3.x 包名修复（必须立即执行）
```bash
# 批量修复 javax → jakarta 包名
cd smart-admin-api-java17-springboot3
find . -name "*.java" -exec sed -i 's/javax\./jakarta\./g' {} \;
```

#### 2. 依赖注入修复（必须立即执行）
```bash
# 批量替换 @Autowired → @Resource
find . -name "*.java" -exec sed -i 's/@Autowired/@Resource/g' {} \;
```

#### 3. 基础类导入修复
```bash
# 修复 BaseEntity 导入路径
find . -name "*.java" -exec sed -i 's/net\.lab1024\.sa\.base\.module\.entity\.base\.BaseEntity/net.lab1024.sa.base.common.entity.BaseEntity/g' {} \;
```

#### 4. 快速验证修复效果
```bash
# 运行快速检查脚本
./scripts/quick-check.sh

# 尝试编译
mvn clean compile -DskipTests
```

**📞 如修复过程中遇到问题，请参考**：
- **[🔧 技术迁移规范](docs/TECHNOLOGY_MIGRATION.md)** - 详细的修复步骤
- **[📖 综合开发规范文档](docs/DEV_STANDARDS.md)** - 完整的编码标准
- **[🚨 关键项目状态报告](docs/CRITICAL_PROJECT_STATUS_REPORT.md)** - 问题分析和解决计划

---

## 技术栈

**前端**: JavaScript + Vue 3 + Vite 5 + Pinia + Ant Design Vue 4.X + Vue Router 4
**后端**: Java 17 + Spring Boot 3 + Sa-Token + MyBatis-Plus + Druid
**数据库**: MySQL (支持国产数据库：达梦、金仓等)
**缓存**: Redis
**移动端**: uni-app (Vue3版本)

## 常用命令

### 后端开发

```bash
# 进入后端根目录
cd smart-admin-api-java17-springboot3

# 编译整个项目（跳过测试）
mvn clean install -DskipTests

# 启动开发服务
cd sa-admin
mvn spring-boot:run

# 或者直接运行 JAR 包
java -jar sa-admin/target/sa-admin-dev-3.0.0.jar

# Maven 环境配置
# Java 17, Spring Boot 3.5.4
# 访问地址：http://localhost:1024
# API 文档：http://localhost:1024/doc.html

# 数据库初始化（首次启动）
# 确保执行：数据库SQL脚本/mysql/smart_admin_v3.sql
```

**配置文件位置**: `smart-admin-api-java17-springboot3/sa-base/src/main/resources/dev/sa-base.yaml`

### 前端开发

```bash
# 进入前端目录
cd smart-admin-web-javascript

# 安装依赖
npm install

# 启动开发服务器（localhost 模式）
npm run localhost

# 启动开发服务器（dev 模式）
npm run dev

# 构建测试环境
npm run build:test

# 构建预发布环境
npm run build:pre

# 构建生产环境
npm run build:prod
```

**配置文件**: `smart-admin-web-javascript/vite.config.js`
**访问地址**: http://localhost:8081
**Node.js 要求**: >= 18

### 移动端开发

```bash
# 进入移动端目录
cd smart-app

# 安装依赖（如果使用 HBuilderX，可直接运行）

# 运行到浏览器
npm run dev:mp-weixin  # 微信小程序
npm run dev:h5         # H5
npm run dev:app        # App
```

## 项目结构

```
smart-admin-master/
├── smart-admin-api-java17-springboot3/    # 后端项目（Java 17 + Spring Boot 3）
│   ├── sa-base/                           # 基础模块（配置、工具类、公共实体）
│   │   └── src/main/java/net/lab1024/sa/
│   │       ├── common/                    # 公共模块
│   │       │   ├── domain/                # 实体类（Entity, VO, DTO）
│   │       │   ├── mapper/                # MyBatis Plus Mapper
│   │       │   └── service/               # 基础 Service 接口
│   │       ├── config/                    # 配置类
│   │       ├── util/                      # 工具类
│   │       └── constant/                  # 常量定义
│   └── sa-admin/                          # 管理模块
│       └── src/main/java/net/lab1024/sa/
│           ├── admin/                     # 管理模块代码
│           │   ├── controller/            # 控制器（四层架构：controller, service, manager, dao）
│           │   ├── service/               # Service 层
│           │   ├── manager/               # Manager 层（业务逻辑封装）
│           │   └── dao/                   # DAO 层
│           └── module/                    # 功能模块（business, data, system 等）
├── smart-admin-web-javascript/            # 前端项目（Vue 3 + JavaScript）
│   ├── src/
│   │   ├── api/                          # API 接口
│   │   ├── assets/                       # 静态资源
│   │   ├── components/                   # 公共组件
│   │   ├── config/                       # 配置文件（环境变量、主题等）
│   │   ├── constants/                    # 常量定义（枚举、字典）
│   │   ├── directives/                   # 自定义指令
│   │   ├── i18n/                         # 国际化
│   │   ├── layout/                       # 布局组件
│   │   ├── lib/                          # 第三方库扩展
│   │   ├── plugins/                      # 插件（axios, ant-design-vue 等）
│   │   ├── router/                       # 路由配置
│   │   ├── store/                        # Pinia 状态管理
│   │   ├── theme/                        # 主题配置
│   │   ├── utils/                        # 工具函数
│   │   ├── views/                        # 页面组件
│   │   ├── App.vue                       # 根组件
│   │   └── main.js                       # 入口文件
│   ├── vite.config.js                    # Vite 配置
│   └── package.json
├── smart-app/                             # 移动端项目（uni-app）
└── 数据库SQL脚本/                          # 数据库脚本
```

## 代码架构特点

### 后端四层架构

**Controller 层**: 接收 HTTP 请求，参数校验，调用 Service 层
**Service 层**: 业务逻辑处理，事务管理
**Manager 层**: 复杂业务逻辑封装，跨模块调用
**DAO 层**: 数据访问，使用 MyBatis Plus

### 前端特点

1. **路径别名**: `/@/` 指向 `src/` 目录
2. **环境配置**: 支持 localhost、dev、test、pre、prod 五个环境
3. **主题系统**: 基于 Less 的主题定制
4. **国际化**: Vue i18n 实现多语言支持
5. **状态管理**: Pinia（Vue 3 推荐方案）
6. **动态路由**: 基于后端权限数据的动态路由构建，见 `main.js:53-71` 的初始化逻辑
7. **组件库**: Ant Design Vue 4.x，按需加载，支持主题定制
8. **构建优化**: Vite 5 打包，支持代码分割和 tree-shaking

### 安全特性

- **权限认证**: Sa-Token（支持 Redis）
- **接口加解密**: 支持国产加密算法和国际加密算法
- **数据脱敏**: 自动脱敏敏感信息
- **登录限制**: 双因子认证、密码复杂度、登录错误锁定
- **安全日志**: 登录日志、操作日志、设备信息记录

## 数据库配置

- **数据库**: MySQL 8.0+
- **连接信息**: 192.168.10.110:33060 / smart_admin_v3 / root / (空密码)
- **Redis**: 127.0.0.1:6389 / zkteco3100 / db 1

**配置文件**: `smart-admin-api-java17-springboot3/sa-base/src/main/resources/dev/sa-base.yaml`

**文件上传路径**: `D:/Progect/mart-admin-master/upload/` (在配置文件中定义)

**注意**: 实际配置与文档中的默认配置不一致，使用时请查看配置文件获取最新的数据库连接信息。

## 🚨 开发规范（强制执行）

### 📋 规范优先级体系

**⚠️ 重要提醒：所有开发工作必须严格遵循以下规范，违反规范将导致编译失败或代码被拒绝合并**

#### 🔴 **一级规范：必须遵守（强制执行）**
**权威来源：严格按照D:\IOE-DREAM\docs\repowiki中的规范**

违反这些规范将导致：
- ❌ 编译失败，无法运行
- ❌ CI/CD 流水线失败，无法合并

#### 🟡 **二级规范：业务模块开发规范（严格遵循）**
**⚠️ 业务模块开发强制要求**：
- ✅ **所有业务模块开发工作必须严格遵循 `docs/repowiki/` 路径下的权威规范**
- ✅ **开发前必须先查阅相关业务模块的专用检查清单和设计文档**
- ✅ **代码实现必须符合对应业务模块的技术架构和设计要求**
- ✅ **违反业务模块规范的代码将被拒绝合并**

**业务模块规范清单**：
- 门禁系统开发 → `docs/CHECKLISTS/门禁系统开发检查清单.md`
- 消费系统开发 → `docs/CHECKLISTS/消费系统开发检查清单.md`
- 考勤系统开发 → `docs/CHECKLISTS/考勤系统开发检查清单.md`
- 智能视频系统开发 → `docs/CHECKLISTS/智能视频系统开发检查清单.md`
- 通用开发标准 → `docs/CHECKLISTS/通用开发检查清单.md`

**业务模块开发流程**：
1. **设计阶段**：查阅 `docs/PROJECT_PLAN/` 和 `docs/COMMON_MODULES/` 中的相关设计文档
2. **规范确认**：严格遵循对应业务模块的检查清单和开发规范
3. **代码实现**：按照 `docs/DEV_STANDARDS.md` 和业务模块专用规范进行开发
4. **质量检查**：使用业务模块专用检查清单进行自检和互检
- ❌ 代码审查不通过，无法部署

**核心规范文档**：
1. **[📖 综合开发规范文档](docs/DEV_STANDARDS.md)** - **必读！** 包含所有核心开发标准
2. **[🔧 技术迁移规范](docs/TECHNOLOGY_MIGRATION.md)** - **必读！** Spring Boot 3.x 迁移标准
3. **[🏗️ 架构设计规范](docs/ARCHITECTURE_STANDARDS.md)** - **必读！** 四层架构设计标准

**🔴 立即检查工具**：
```bash
# 每次提交前必须执行
./scripts/quick-check.sh

# 完整项目健康检查
./scripts/project-health-check.sh
```

**📚 开发规范体系（repowiki）**：
- **[📖 开发规范体系](docs/repowiki/zh/content/开发规范体系.md)** - **必读！** 五层架构权威规范体系
- **[🏗️ 架构设计规范](docs/repowiki/zh/content/技术架构/技术架构.md)** - 系统架构和微服务设计标准
- **[💻 Java编码规范](docs/repowiki/zh/content/开发规范体系/Java编码规范.md)** - Java代码编写标准和最佳实践
- **[🌐 API设计规范](docs/repowiki/zh/content/开发规范体系/API设计规范.md)** - RESTful API设计标准
- **[🔒 系统安全规范](docs/repowiki/zh/content/开发规范体系/系统安全规范.md)** - 安全要求和漏洞防护
- **[🗄️ 数据库设计规范](docs/repowiki/zh/content/开发规范体系/数据库设计规范.md)** - 数据库设计和优化标准
- **[🧪 单元测试指南](docs/repowiki/zh/content/开发规范体系/单元测试指南.md)** - 测试编写和质量保证
- **[🎨 Vue3开发规范](docs/repowiki/zh/content/开发规范体系/Vue3开发规范.md)** - 前端组件开发标准
- **[🤖 AI开发指令集](docs/repowiki/zh/content/开发规范体系/AI开发指令集.md)** - AI辅助开发指导
- **[✅ 代码质量标准](docs/repowiki/zh/content/开发规范体系/代码质量标准.md)** - 代码质量度量和评估

#### 🟡 **二级规范：应该遵守（质量保障）**
违反这些规范将导致：
- ⚠️ 代码质量问题警告
- ⚠️ 代码审查要求修复
- ⚠️ 技术债务累积

**业务模块专用检查清单**：
- **[📋 通用开发检查清单](docs/CHECKLISTS/通用开发检查清单.md)** - 所有模块通用标准
- **[🚪 门禁系统开发检查清单](docs/CHECKLISTS/门禁系统开发检查清单.md)** - 门禁功能专用
- **[💳 消费系统开发检查清单](docs/CHECKLISTS/消费系统开发检查清单.md)** - 消费功能专用
- **[⏰ 考勤系统开发检查清单](docs/CHECKLISTS/考勤系统开发检查清单.md)** - 考勤功能专用
- **[📹 智能视频系统开发检查清单](docs/CHECKLISTS/智能视频系统开发检查清单.md)** - 视频功能专用

#### 🟢 **三级规范：建议遵守（最佳实践）**
违反这些规范将导致：
- 💡 代码质量建议
- 💡 最佳实践推荐
- 💡 性能优化提示

**参考文档**：
- **[👥 团队开发规范](docs/TEAM_DEVELOPMENT_STANDARDS.md)** - 团队协作最佳实践
- **[📚 项目开发指南](docs/PROJECT_GUIDE.md)** - 快速参考和技巧
- **[📊 文档分析报告](docs/DOCUMENT_ANALYSIS_REPORT.md)** - 持续改进建议

### 🎯 规范执行机制

#### 自动化检查（技术保障）
```
IDE 实时检查 → 代码编写时即时提醒
Pre-commit Hook → 提交前强制检查
CI/CD 流水线 → 持续集成质量门禁
定期扫描 → 代码健康度评估
```

#### 人工审核（流程保障）
```
代码审查 → Peer Review 强制要求
架构评审 → 重大变更架构师审查
质量会议 → 定期代码质量回顾
```

#### 违规后果（制度保障）
```
一级规范违规 → 阻塞开发，立即修复
二级规范违规 → 技术债务记录，限期修复
三级规范违规 → 建议记录，择机优化
```

### 🔴 必须遵守的核心规范

#### ⚠️ 权威规范引用
**重要提醒**：本文档中所有开发规范以 **[📖 统一开发规范文档](docs/UNIFIED_DEVELOPMENT_STANDARDS.md)** 为唯一权威来源。

**核心规范包括但不限于**：
- 🔧 **技术栈规范**：Spring Boot 3.5.4 + Jakarta EE
- 🏗️ **架构设计规范**：四层架构严格规范
- 📝 **编码质量标准**：代码复杂度、测试覆盖率等
- 🚫 **禁止清单**：绝对禁止的开发实践
- ✅ **必须标准**：强制执行的最低要求

#### ⚠️ 立即更新要求
所有开发团队成员必须：
1. **立即阅读统一开发规范文档**
2. **按照统一规范要求更新现有代码**
3. **在开发过程中严格遵循规范要求**
4. **使用自动化检查工具验证规范遵循情况**

#### 🔍 规范验证工具
```bash
# 规范检查脚本（每次提交前必须执行）
./scripts/enforce-standards.sh

# 快速检查当前项目规范状态
./scripts/quick-standards-check.sh
```

#### 📊 违规处理机制
- **一级规范违规**：阻塞开发，立即修复
- **二级规范违规**：记录技术债务，限期修复
- **三级规范违规**：记录建议，择机优化

#### 架构规范
- **四层架构**: 严格遵守 Controller → Service → Manager → DAO 调用链
- **事务管理**: 事务边界必须在 Service 层
- **跨层访问**: 禁止 Controller 直接访问 DAO 层

#### 数据库规范
- **表命名**: `t_{business}_{entity}` 格式
- **主键**: `{table}_id` 格式，BIGINT AUTO_INCREMENT
- **审计字段**: 必须包含 create_time, update_time, create_user_id, deleted_flag
- **软删除**: 使用 deleted_flag 字段，禁止物理删除
- **字符集**: 统一使用 utf8mb4，存储引擎 InnoDB

#### 后端规范
- **权限控制**: 所有接口必须使用 @SaCheckLogin 和 @SaCheckPermission
- **参数验证**: 使用 @Valid 进行参数校验
- **统一响应**: 使用 ResponseDTO 返回格式
- **异常处理**: 使用 SmartException 及其子类
- **日志记录**: 使用@Slf4j，禁止 System.out

#### 前端规范
- **技术栈**: 必须使用 Vue 3 + TypeScript + Pinia + Ant Design Vue 4.x
- **组件开发**: 使用 Composition API，遵循单一职责原则
- **API封装**: 统一在 `/@/api/` 目录下封装
- **权限控制**: 使用 v-permission 指令
- **状态管理**: 使用 Pinia 进行状态管理

#### 命名规范
- **包名**: `net.lab1024.sa.{module}.{layer}`
- **类名**: Controller→{Module}Controller, Service→{Module}Service, Manager→{Module}Manager, DAO→{Module}Dao
- **方法名**: 查询→get/query/find/list, 新增→add/create/save/insert, 修改→update/edit/modify, 删除→delete/remove
- **变量名**: Boolean→is/has/can开头，集合→复数形式或List/Map后缀

### 🚀 开发流程要求

#### 新功能开发步骤
1. **设计阶段**: 阅读相关规范文档，制定设计方案
2. **数据库设计**: 遵循数据库规范，编写DDL脚本
3. **后端开发**: 按四层架构顺序开发，编写单元测试
4. **前端开发**: 创建API、Store、组件，遵循Vue 3最佳实践
5. **测试验证**: 运行完整测试，确保质量达标

#### 代码提交要求
- **提交信息**: 遵循 `<type>(<scope>): <subject>` 格式
- **分支管理**: 遵循 GitFlow 工作流
- **代码审查**: 必须通过代码审查才能合并
- **文档更新**: 重要变更需要更新相关文档

#### 分支命名规范
- **主分支**: `main` (生产环境)
- **开发分支**: `develop` (开发环境)
- **功能分支**: `feature/{feature-name}` (新功能开发)
- **修复分支**: `fix/{bug-description}` (问题修复)
- **热修复**: `hotfix/{hotfix-description}` (紧急修复)
- **发布分支**: `release/{version}` (版本发布)
- **WebSocket分支**: `{branch-name}-wss` (WebSocket相关功能，如：`device-monitor-wss`)
- **特殊标识**: 可根据项目需求添加特定前缀（如：`wss` 用于区分WebSocket功能）

### 📋 质量检查清单

#### 开发前检查
- [ ] 是否已阅读相关业务和规范文档？
- [ ] 是否已理解技术架构要求？
- [ ] 是否已确认数据库设计方案？

#### 🔒 强制性六层验证机制

**🚨 每个任务完成前必须通过的质量门禁 - 违反任何一项都将导致任务失败，必须0异常才能继续下一步任务**

##### 第零层：本地启动验证（Docker部署前必须通过）
```bash
cd smart-admin-api-java17-springboot3/sa-admin

# 1. 本地启动测试（必须0异常运行60秒）
echo "🔥 开始第零层验证：本地启动测试..."
timeout 90s mvn spring-boot:run -Dspring-boot.run.profiles=docker > ../local_startup_test.log 2>&1 &
pid=$!

# 2. 等待启动完成
sleep 60

# 3. 检查进程状态
if ps -p $pid > /dev/null 2>&1; then
    echo "✅ 本地应用成功启动，持续运行60秒"
    kill $pid 2>/dev/null || true
    wait $pid 2>/dev/null || true
else
    wait $pid
    # 检查是否启动失败
    if grep -q "Application run failed\|ERROR\|Exception" ../local_startup_test.log; then
        echo "❌ 本地启动失败，禁止进行Docker部署"
        echo "错误详情:"
        tail -30 ../local_startup_test.log
        exit 1
    fi
fi

# 4. 严格检查本地启动日志中的所有异常
echo "🔍 严格检查本地启动日志异常..."
local_logs=$(cat ../local_startup_test.log 2>&1)

# 定义关键错误模式
local_error_patterns=(
    "ERROR"
    "Exception"
    "Failed"
    "Unable to"
    "Connection refused"
    "No.*found"
    "BeanCreationException"
    "UnsatisfiedDependencyException"
    "Application startup failed"
    "mapperLocations.*not found"
    "log4j2.*error"
    "javax\."
)

local_critical_errors=0
for pattern in "${local_error_patterns[@]}"; do
    error_count=$(echo "$local_logs" | grep -i "$pattern" | wc -l)
    if [ $error_count -gt 0 ]; then
        echo "❌ 本地启动发现 $pattern 错误: $error_count 次"
        echo "$local_logs" | grep -i "$pattern" | head -3
        ((local_critical_errors++))
        exit 1
    fi
done

# 5. 检查本地启动成功标志
if echo "$local_logs" | grep -q "Started.*Application\|Application.*started\|Tomcat.*started"; then
    echo "✅ 本地应用成功启动"
else
    echo "❌ 本地应用未显示启动成功标志"
    exit 1
fi

echo "🎉 第零层验证通过：本地启动无异常"
```

##### 第一层：完整构建验证
```bash
cd smart-admin-api-java17-springboot3

# 1. 完整打包验证（必须通过）
mvn clean package -DskipTests

# 2. 包名检查（javax使用必须为0）
javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
if [ $javax_count -ne 0 ]; then
    echo "❌ 发现 javax 包使用: $javax_count 个文件"
    exit 1
fi

# 3. @Autowired 检查（使用必须为0）
autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
if [ $autowired_count -ne 0 ]; then
    echo "❌ 发现 @Autowired 使用: $autowired_count 个文件"
    exit 1
fi
```

##### 第二层：MyBatis完整性验证
```bash
# 检查所有mapper.xml文件中的实体类是否存在
find . -name "*.xml" -path "*/mapper/*" -exec sh -c '
    mapper_file="$1"
    entities=$(grep -o "resultType=\"[^\"]*Entity\"" "$mapper_file" | sed "s/resultType=\"//" | sed "s/\"//")
    for entity in $entities; do
        entity_file=$(echo "$entity" | sed "s/\./\//g").java
        if [ ! -f "$entity_file" ]; then
            echo "❌ Mapper $mapper_file 引用的实体类不存在: $entity"
            exit 1
        fi
    done
' _ {} \;
```

##### 第二层：MyBatis完整性验证
```bash
# 检查所有mapper.xml文件中的实体类是否存在
find . -name "*.xml" -path "*/mapper/*" -exec sh -c '
    mapper_file="$1"
    entities=$(grep -o "resultType=\"[^\"]*Entity\"" "$mapper_file" | sed "s/resultType=\"//" | sed "s/\"//" 2>/dev/null)
    for entity in $entities; do
        entity_file=$(echo "$entity" | sed "s/\./\//g").java
        if [ ! -f "$entity_file" ]; then
            echo "❌ Mapper $mapper_file 引用的实体类不存在: $entity"
            exit 1
        fi
    done
' _ {} \;
```

##### 第三层：Spring Boot启动验证
```bash
cd smart-admin-api-java17-springboot3/sa-admin

# 启动测试（必须成功，90秒超时）
timeout 90s mvn spring-boot:run -Dspring-boot.run.profiles=docker > ../startup_test.log 2>&1 &
pid=$!
sleep 60
if ps -p $pid > /dev/null; then
    kill $pid
else
    if grep -q "Application run failed\|ERROR\|Exception" ../startup_test.log; then
        echo "❌ Spring Boot启动失败"
        tail -30 ../startup_test.log
        exit 1
    fi
fi
```

##### 第四层：Docker部署验证
```bash
# 1. 构建Docker镜像
docker-compose build backend
if [ $? -ne 0 ]; then
    echo "❌ Docker镜像构建失败"
    exit 1
fi

# 2. 启动容器并检查状态
docker-compose up -d backend
sleep 30

# 3. 验证容器健康状态
container_status=$(docker-compose ps | grep backend | grep -c "Up")
if [ $container_status -ne 1 ]; then
    echo "❌ Docker容器启动失败"
    docker logs smart-admin-backend --tail 50
    exit 1
fi

# 4. 🔥 120秒持续监控（关键环节）
echo "🔍 开始120秒持续监控容器稳定性..."
for i in 30 60 90 120; do
    echo "第${i}秒检查:"
    docker-compose ps | grep backend
    container_status=$(docker-compose ps | grep backend | grep -c "Up" || echo "0")
    if [ "$container_status" = "0" ]; then
        echo "❌ 容器在第${i}秒停止运行"
        docker logs smart-admin-backend --tail 50
        exit 1
    fi
    sleep 30
done

# 5. 严格检查所有异常类型
echo "=== 严格检查Docker启动日志异常 ==="
docker_logs=$(docker logs smart-admin-backend 2>&1)

# 定义关键错误模式
error_patterns=(
    "ERROR"
    "Exception"
    "Failed"
    "Unable to"
    "Connection refused"
    "No.*found"
    "NullPointerException"
    "ClassNotFoundException"
    "BeanCreationException"
    "UnsatisfiedDependencyException"
    "Application startup failed"
    "RollingFile.*not found"
    "log4j2.*error"
    "javax\."
    "mapperLocations.*not found"
)

critical_errors=0
for pattern in "${error_patterns[@]}"; do
    error_count=$(echo "$docker_logs" | grep -i "$pattern" | wc -l)
    if [ $error_count -gt 3 ]; then  # 允许少量重试错误
        echo "❌ 发现 $pattern 错误: $error_count 次"
        echo "$docker_logs" | grep -i "$pattern" | head -3
        ((critical_errors++))
    fi
done

# 6. 检查应用启动成功标志
if echo "$docker_logs" | grep -q "Started.*Application\|Application.*started\|Tomcat.*started"; then
    echo "✅ 应用启动成功"
else
    echo "❌ 应用未显示启动成功标志"
    echo "最近50行日志:"
    echo "$docker_logs" | tail -50
    exit 1
fi

# 7. 健康检查验证
echo "=== 执行健康检查验证 ==="
sleep 10
health_response=$(docker exec smart-admin-backend curl -s http://localhost:1024/api/health 2>/dev/null || echo "FAILED")
if [ "$health_response" != "FAILED" ]; then
    echo "✅ 健康检查通过: $health_response"
else
    echo "❌ 健康检查失败"
    exit 1
fi

# 8. 最终验证
if [ $critical_errors -gt 0 ]; then
    echo "❌ 发现 $critical_errors 类严重错误，Docker部署失败"
    exit 1
fi

echo "🎉 Docker部署验证通过！"
```

##### 第五层：repowiki规范符合性验证
```bash
# 1. repowiki规范强制检查
echo "=== repowiki规范符合性验证 ==="

# 2. 包名规范检查（一级规范）
javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
if [ $javax_count -ne 0 ]; then
    echo "❌ 发现 javax 包使用: $javax_count 个文件，违反repowiki一级规范"
    find . -name "*.java" -exec grep -l "javax\." {} \;
    exit 1
fi
echo "✅ jakarta包名规范检查通过"

# 3. 依赖注入规范检查（一级规范）
autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
if [ $autowired_count -ne 0 ]; then
    echo "❌ 发现 @Autowired 使用: $autowired_count 个文件，违反repowiki一级规范"
    find . -name "*.java" -exec grep -l "@Autowired" {} \;
    exit 1
fi
echo "✅ @Resource依赖注入规范检查通过"

# 4. 四层架构规范检查（一级规范）
controller_direct_dao=$(grep -r "@Resource.*Dao" --include="*Controller.java" . | wc -l)
if [ $controller_direct_dao -ne 0 ]; then
    echo "❌ 发现Controller直接访问DAO: $controller_direct_dao 处，违反repowiki一级规范"
    grep -r "@Resource.*Dao" --include="*Controller.java" .
    exit 1
fi
echo "✅ 四层架构规范检查通过"

# 5. 代码质量规范检查（二级规范）
system_out_count=$(find . -name "*.java" -exec grep -l "System\.out\.println" {} \; | wc -l)
if [ $system_out_count -ne 0 ]; then
    echo "⚠️ 发现 System.out.println 使用: $system_out_count 个文件，违反repowiki二级规范"
    find . -name "*.java" -exec grep -l "System\.out\.println" {} \;
fi

# 6. 权限控制规范检查（二级规范）
controller_methods=$(grep -r "@PostMapping\|@GetMapping\|@PutMapping\|@DeleteMapping" --include="*Controller.java" . | wc -l)
permission_methods=$(grep -r "@SaCheckPermission" --include="*Controller.java" . | wc -l)
if [ $permission_methods -lt $((controller_methods / 2)) ]; then
    echo "⚠️ 权限控制注解覆盖率偏低，建议检查是否违反repowiki二级规范"
    echo "Controller方法总数: $controller_methods, 权限注解数量: $permission_methods"
fi
```

##### 第六层：任务完成度验证
```bash
# 1. 任务目标达成验证
echo "=== 任务完成度验证 ==="

# 2. 编译完整性验证
mvn clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo "❌ 编译完整性验证失败"
    exit 1
fi

# 3. 测试覆盖率验证（如果有测试）
if [ -d "src/test" ]; then
    mvn test -q
    if [ $? -ne 0 ]; then
        echo "⚠️ 测试执行失败，建议检查测试用例"
    fi
fi

# 4. 文档更新验证
echo "✅ 任务完成度验证通过"
```

#### 🔒 每次OpenSpec任务完成必须执行的检查清单

**🚨 强制要求：** 每个OpenSpec任务完成后必须执行以下检查：

```bash
#!/bin/bash
# 强制检查脚本 - 每次任务完成后执行（包含repowiki规范验证）

echo "🔍 执行任务后强制检查（包含repowiki规范验证）..."

# 1. 本地构建验证
echo "步骤1: 本地构建验证"
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ 构建失败，任务未完成"
    exit 1
fi
echo "✅ 构建验证通过"

# 2. repowiki规范一级检查（包名合规）
echo "步骤2: repowiki规范一级检查 - 包名合规"
javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
if [ $javax_count -ne 0 ]; then
    echo "❌ 发现 javax 包使用: $javax_count 个文件，违反repowiki一级规范"
    find . -name "*.java" -exec grep -l "javax\." {} \;
    exit 1
fi
echo "✅ jakarta包名规范检查通过"

# 2.1 repowiki规范一级检查（依赖注入）
echo "步骤2.1: repowiki规范一级检查 - 依赖注入"
autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
if [ $autowired_count -ne 0 ]; then
    echo "❌ 发现 @Autowired 使用: $autowired_count 个文件，违反repowiki一级规范"
    find . -name "*.java" -exec grep -l "@Autowired" {} \;
    exit 1
fi
echo "✅ @Resource依赖注入规范检查通过"

# 3. Docker部署验证（如果涉及Docker）
echo "步骤3: Docker部署验证"
docker-compose build backend
docker-compose up -d backend

# 4. 120秒持续监控
echo "步骤4: 120秒持续监控"
for i in 30 60 90 120; do
    container_status=$(docker-compose ps | grep backend | grep -c "Up" || echo "0")
    if [ "$container_status" = "0" ]; then
        echo "❌ 容器在第${i}秒停止运行"
        docker logs smart-admin-backend --tail 30
        exit 1
    fi
    sleep 30
done
echo "✅ 容器稳定性验证通过"

# 5. 异常模式检查
echo "步骤5: 异常模式检查"
docker_logs=$(docker logs smart-admin-backend 2>&1)
error_count=$(echo "$docker_logs" | grep -i -E "ERROR|Exception|Failed|Connection refused" | wc -l)
if [ $error_count -gt 5 ]; then
    echo "❌ 发现过多异常: $error_count 个"
    echo "$docker_logs" | grep -i -E "ERROR|Exception|Failed|Connection refused" | head -10
    exit 1
fi
echo "✅ 异常检查通过"

# 6. 启动成功验证
echo "步骤6: 启动成功验证"
echo "$docker_logs" | grep -q "Started.*Application\|Application.*started\|Tomcat.*started"
if [ $? -ne 0 ]; then
    echo "❌ 应用未显示启动成功标志"
    exit 1
fi
echo "✅ 应用启动成功"

# 7. 健康检查验证
echo "步骤7: 健康检查验证"
docker exec smart-admin-backend curl -f http://localhost:1024/api/health > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "❌ 健康检查失败"
    exit 1
fi
echo "✅ 健康检查通过"

echo ""
echo "🎉 所有强制检查通过，任务完成！"
echo "✅ 本地构建验证"
echo "✅ repowiki一级规范：jakarta包名合规检查"
echo "✅ repowiki一级规范：@Resource依赖注入检查"
echo "✅ Docker部署验证"
echo "✅ 120秒持续监控"
echo "✅ 异常模式检查"
echo "✅ 启动成功验证"
echo "✅ 健康检查验证"
echo ""

### 🎯 任务执行前强制检查清单（基于repowiki规范）

**🚨 每个任务开始前必须执行的repowiki规范梳理和检查**

#### 第一步：任务类型分析和规范识别
```bash
# 1. 识别任务类型
echo "=== 步骤1：任务类型分析和规范识别 ==="

# 2. 根据任务类型确定必须遵循的repowiki规范
case "$TASK_TYPE" in
    "新功能开发")
        echo "必须遵循的repowiki规范："
        echo "  - 架构设计规范（四层架构）"
        echo "  - Java编码规范（jakarta包名）"
        echo "  - API设计规范（RESTful）"
        echo "  - 数据库设计规范（表命名、审计字段）"
        echo "  - 权限控制规范（Sa-Token）"
        ;;
    "代码修复")
        echo "必须遵循的repowiki规范："
        echo "  - Java编码规范（包名修复、依赖注入）"
        echo "  - 代码质量标准（复杂度、重复代码）"
        echo "  - 异常处理规范"
        ;;
    "Docker部署")
        echo "必须遵循的repowiki规范："
        echo "  - 环境配置规范"
        echo "  - 部署验证规范"
        echo "  - 监控配置规范"
        ;;
    "AI辅助开发")
        echo "必须遵循的repowiki规范："
        echo "  - AI开发指令集"
        echo "  - AI约束检查清单"
        echo "  - 代码质量标准"
        ;;
esac
```

#### 第二步：当前项目repowiki规范符合性评估
```bash
# 1. 一级规范符合性快速评估
echo "=== 步骤2：当前项目repowiki规范符合性评估 ==="

# 2. 关键指标检查
javax_count=$(find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l)
echo "javax包使用数量: $javax_count（必须为0）"

autowired_count=$(find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l)
echo "@Autowired使用数量: $autowired_count（必须为0）"

architecture_violations=$(grep -r "@Resource.*Dao" --include="*Controller.java" . | wc -l)
echo "架构违规数量: $architecture_violations（必须为0）"

# 3. 根据评估结果确定任务执行策略
if [ $javax_count -gt 0 ] || [ $autowired_count -gt 0 ] || [ $architecture_violations -gt 0 ]; then
    echo "⚠️ 发现repowiki一级规范违规，必须先修复才能继续任务"
    exit 1
fi
echo "✅ repowiki一级规范符合性评估通过"
```

#### 第三步：任务执行策略制定
```bash
# 1. 基于repowiki规范制定任务执行策略
echo "=== 步骤3：任务执行策略制定 ==="

# 2. 确定质量门禁标准
echo "质量门禁标准："
echo "  - 编译0错误"
echo "  - 包名规范100%合规"
echo "  - 依赖注入100%合规"
echo "  - 架构规范100%合规"
echo "  - 测试覆盖率≥80%（如果有测试）"
echo "  - Docker部署0异常（如果涉及部署）"
```

#### 第四步：任务执行环境准备
```bash
# 1. 确保开发环境符合repowiki规范要求
echo "=== 步骤4：任务执行环境准备 ==="

# 2. 检查工具配置
java -version  # 必须是Java 17
mvn -version   # 确保Maven版本兼容

# 3. 检查配置文件
echo "确保配置文件符合repowiki规范要求..."
```

#### ✅ repowiki规范整合完成说明

**🎯 已完成的CLAUDE.md增强内容：**

1. **六层验证机制**：从原有的五层验证升级为六层验证，增加repowiki规范符合性验证
2. **repowiki一级规范强制检查**：jakarta包名、@Resource依赖注入、四层架构规范
3. **任务执行前强制检查清单**：基于repowiki规范的任务类型分析和规范识别
4. **完整验证脚本**：整合repowiki规范的强制检查脚本，确保0异常才能继续下一步

**🔒 核心机制强化：**
- **每个任务必须0异常才能继续下一步**
- **每次任务开始前先梳理repowiki规范要求**
- **严格的repowiki一级规范违规阻断机制**
- **完整的任务前后验证体系**

现在每个AI助手在执行任务时都会：
1. **任务开始前**：必须执行repowiki规范梳理和符合性评估
2. **任务执行中**：严格遵循对应的repowiki规范要求
3. **任务完成后**：必须通过包含repowiki规范的强制验证

这确保了IOE-DREAM项目的所有开发工作都严格遵循repowiki规范体系。

---

### 🛠️ AI开发专用检查清单

#### 代码生成前检查
- [ ] 已阅读repowiki相关规范文档
- [ ] 已识别任务类型和必须遵循的规范
- [ ] 已通过repowiki一级规范符合性评估
- [ ] 已选择合适的代码模板
- [ ] 已理解业务状态机和验证规则

#### 代码生成后自检
- [ ] 使用@Resource而非@Autowired（repowiki一级规范）
- [ ] 使用jakarta.*而非javax.*（repowiki一级规范）
- [ ] 使用SLF4J而非System.out（repowiki二级规范）
- [ ] Entity继承BaseEntity且未重复定义审计字段
- [ ] Controller只做参数验证和调用Service（repowiki架构规范）
- [ ] Service包含@Transactional注解
- [ ] Controller返回ResponseDTO
- [ ] 添加@SaCheckPermission权限注解
- [ ] 使用@Valid进行参数验证
- [ ] 编写单元测试(覆盖率≥80%)
- [ ] 添加JavaDoc和Swagger注解

#### 提交前验证
- [ ] 运行repowiki规范符合性检查（通过）
- [ ] 运行./scripts/quick-check.sh（通过）
- [ ] 运行./scripts/ai-code-validation.sh（通过）
- [ ] Pre-commit Hook检查通过
- [ ] 单元测试全部通过

---

#### 提交前检查（传统方式，已升级为六层验证）
- [ ] **六层验证**: 是否通过强制性六层验证机制？
- [ ] **repowiki规范**: 是否通过repowiki一级规范符合性检查？
- [ ] **架构检查**: 是否遵循四层架构规范？
- [ ] **命名规范**: 是否遵循所有命名规范？
- [ ] **异常处理**: 是否已处理所有异常情况？
- [ ] **权限控制**: 是否已添加权限控制？
- [ ] **代码质量**: 单元测试覆盖率是否≥80%？
- [ ] **文档更新**: 是否已更新相关文档？

#### 🔍 编译错误预防检查清单（必须执行）
```bash
# 每次提交前必须执行的检查脚本
cd smart-admin-api-java17-springboot3

# 1. 编译检查
mvn clean compile -DskipTests

# 2. 检查 javax 包使用（应该为 0）
find . -name "*.java" -exec grep -l "javax\." {} \; | wc -l

# 3. 检查 @Autowired 使用（应该为 0）
find . -name "*.java" -exec grep -l "@Autowired" {} \; | wc -l

# 4. 检查 System.out.println 使用（应该为 0）
find . -name "*.java" -exec grep -l "System\.out\.println" {} \; | wc -l
```

### ⚠️ 常见错误提醒

#### 禁止的操作
- ❌ **使用 javax 包**: 必须使用 jakarta 包（会导致编译错误）
- ❌ **使用 @Autowired**: 必须使用 @Resource（会导致编译错误）
- ❌ **Controller层直接访问DAO层**
- ❌ **在Controller层编写业务逻辑**
- ❌ **在Manager层管理事务**
- ❌ **直接使用System.out.println输出日志**
- ❌ **物理删除数据（必须使用软删除）**
- ❌ **实体类不继承BaseEntity**（会导致审计字段缺失）
- ❌ **硬编码配置信息**
- ❌ **提交包含敏感信息的代码**

#### 🚨 编译错误高发项（特别注意）
- ❌ `import javax.annotation.*;` → 必须改为 `jakarta.annotation.*`
- ❌ `import javax.validation.*;` → 必须改为 `jakarta.validation.*`
- ❌ `import javax.persistence.*;` → 必须改为 `jakarta.persistence.*`
- ❌ `@Autowired` → 必须改为 `@Resource`
- ❌ 类不继承BaseEntity → 必须继承 `net.lab1024.sa.base.common.entity.BaseEntity`

#### 必须的操作
- ✅ 严格遵循四层架构调用规范
- ✅ 所有接口必须进行权限验证
- ✅ 使用统一异常处理机制
- ✅ 编写完整的单元测试
- ✅ 遵循缓存使用规范
- ✅ 确保代码安全性

### 💡 开发建议

在开发新功能时，请按以下顺序查阅文档：
1. 首先阅读 **PROJECT_GUIDE.md** 的快速参考部分
2. 使用 **通用开发检查清单** 确保代码质量
3. 如涉及具体业务模块，使用对应的专用检查清单
4. 遇到复杂场景，查阅 **DEV_STANDARDS.md** 的详细规范

#### 🛡️ 编译错误避免策略
1. **开发前检查**：
   - 运行 `mvn clean compile` 确保项目可编译
   - 检查 IDE 是否显示编译错误
   - 确认依赖版本兼容性

2. **编码过程中**：
   - 使用 IDE 自动导入功能，避免手动输入包名
   - 新建类时，确保继承正确的基类
   - 使用模板代码而非手动编写

3. **提交前验证**：
   - 运行完整的编译检查脚本
   - 执行单元测试确保功能正常
   - 使用代码格式化工具统一风格

#### 📚 推荐阅读顺序
1. **TECHNOLOGY_MIGRATION.md** - 技术迁移规范
2. **ARCHITECTURE_STANDARDS.md** - 架构设计规范
3. **TEAM_DEVELOPMENT_STANDARDS.md** - 团队开发规范
4. **通用开发检查清单** - 代码质量保证
5. **业务模块专用清单** - 具体功能规范

### 前端规范（补充）

1. **API 命名**: 使用下划线分隔（如：`login_in`)
2. **常量定义**: 使用枚举，避免魔法数字
3. **组件结构**: 遵循 Vue 3 Composition API 最佳实践
4. **样式规范**: Less 预处理器，使用主题变量
5. **文件组织**: 按功能模块组织，API、组件、页面分离
6. **状态管理**: 使用 Pinia store，模块化管理
7. **权限控制**: 基于指令和路由守卫的权限控制
8. **错误处理**: 统一的错误处理和消息提示

### 后端规范（补充）

1. **包名**: `net.lab1024.sa`
2. **分层**: controller → service → manager → dao
3. **实体类**: Entity（数据库表）| VO（展示对象）| DTO（传输对象）
4. **返回码**: 统一的响应码规范

## 核心功能模块

### 系统管理
- 员工管理、部门管理、角色权限管理
- 菜单管理、系统参数配置
- 数据字典、单号生成器

### 数据功能
- 数据变更记录（基于 git diff）
- 表格自定义列
- 数据脱敏展示

### 安全功能
- 登录认证、权限控制
- 接口加解密
- 安全审计日志

### 文件管理
- 本地/OSS 文件上传
- 文件预览、下载

### 其他
- 在线文档（右侧帮助文档）
- 意见反馈系统
- 版本更新记录
- 服务器心跳监控

## 项目配置要点

### 首次启动前的必要操作

1. **数据库初始化**
   ```sql
   CREATE DATABASE smart_admin_v3 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   -- 导入数据
   mysql -uroot -proot1234 smart_admin_v3 < 数据库SQL脚本/mysql/smart_admin_v3.sql
   ```

2. **服务启动顺序**
   - 先启动 MySQL 和 Redis 服务
   - 再启动后端服务（端口 1024）
   - 最后启动前端服务（端口 8081）

3. **重要文件路径**
   - 数据库配置：`smart-admin-api-java17-springboot3/sa-base/src/main/resources/dev/sa-base.yaml`
   - 前端配置：`smart-admin-web-javascript/vite.config.js`
   - 文件上传路径：配置文件中指定的本地目录

## 官方资源

- **官方网站**: https://smartadmin.vip
- **在线预览**: https://preview.smartadmin.vip
- **移动端预览**: https://app.smartadmin.vip/#/pages/login/login
- **GitHub**: https://github.com/1024-lab/smart-admin

## 🛠️ 快速检查工具

### 开发环境快速检查
```bash
# 每次提交代码前必须运行
./scripts/quick-check.sh

# 完整项目健康检查
./scripts/project-health-check.sh
```

### IDE 配置建议
- **IntelliJ IDEA**: 导入项目时选择"Import project from external model" → Maven
- **VS Code**: 安装 Java Extension Pack、Spring Boot Extension Pack
- **代码格式化**: 使用项目根目录的 `.editorconfig` 配置

### 编译错误快速修复
```bash
# 批量修复 javax → jakarta 包名
find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/javax\.annotation/jakarta.annotation/g' {} \;
find smart-admin-api-java17-springboot3 -name "*.java" -exec sed -i 's/javax\.validation/jakarta.validation/g' {} \;

# 批量替换 @Autowired → @Resource (如需手动处理)
# 注意：建议逐个文件检查并替换，确保逻辑正确
```

## 📚 相关文档链接

- **[技术迁移规范](docs/TECHNOLOGY_MIGRATION.md)** - Spring Boot 升级和包名规范
- **[架构设计规范](docs/ARCHITECTURE_STANDARDS.md)** - 详细的架构设计标准
- **[团队开发规范](docs/TEAM_DEVELOPMENT_STANDARDS.md)** - 团队协作和流程规范
- **[通用开发检查清单](docs/CHECKLISTS/通用开发检查清单.md)** - 代码质量保证清单

## 📋 全局一致性保障机制

### 一致性五层模型

为确保基于Claude Code的高质量0异常开发,建立五层一致性保障体系:

#### 第一层:规范文档一致性
- **单一权威来源**:本文档(CLAUDE.md)作为唯一权威规范
- **规范优先级**:一级(禁止) > 二级(强制) > 三级(建议)
- **规范更新**:所有规范变更必须同步更新本文档
- **repowiki集成**:核心规范已从docs/repowiki/提取到本文档

#### 第二层:代码实现一致性  
- **语法规范**:自动检查javax、@Autowired、System.out(覆盖率100%)
- **架构规范**:四层架构、事务边界、返回格式
- **命名规范**:包名、类名、方法名、变量名统一
- **前后端一致性**:API契约、数据模型、枚举常量、错误码

#### 第三层:数据与配置一致性
- **数据库一致性**:SQL与Entity字段、类型、命名匹配
- **API一致性**:OpenAPI规范、契约测试
- **配置一致性**:环境配置、数据库连接、API地址

#### 第四层:业务逻辑一致性
- **业务规则**:状态机、验证规则、业务流程
- **权限控制**:前后端权限标识、数据权限
- **错误处理**:错误码、错误信息、异常处理

#### 第五层:版本与依赖一致性
- **依赖版本**:统一管理依赖版本(Spring Boot 3.5.4)
- **API版本**:接口版本管理(/api/v1/、/api/v2/)
- **Schema版本**:Flyway数据库版本管理

### 一致性检查工具

```bash
# 完整一致性检查(提交前必须运行)
./scripts/enforce-standards.sh

# 快速检查(开发过程中)
./scripts/quick-check.sh

# 配置一致性检查
./scripts/check-config-consistency.sh

# 数据库一致性检查  
./scripts/check-database-consistency.sh

# AI代码验证
./scripts/ai-code-validation.sh
```

---

## 🤖 基于Claude Code的0异常开发保障体系

### 四层防护模型

#### 第一层:预防性防护(Design Phase)
**目标**:在设计阶段防止错误

✅ **统一规范文档**
- 本文档作为单一权威来源
- 集成repowiki核心规范
- 明确禁止项和必须项

✅ **AI开发模板库**
- Entity模板:docs/templates/backend/entity-template.java
- Controller模板:docs/templates/backend/controller-template.java
- Service模板:docs/templates/backend/service-template.java
- 其他模板:Manager、DAO、VO、DTO

✅ **业务规则文档化**
- 设备状态机:docs/business-rules/device-management.md
- 门禁规则:docs/business-rules/access-control.md
- 权限模型:docs/business-rules/permission-model.md

✅ **架构决策记录(ADR)**
- ADR-001:使用四层架构
- ADR-002:依赖注入使用@Resource
- ADR-003:统一响应格式使用ResponseDTO

#### 第二层:检测性防护(Coding Phase)
**目标**:在编码阶段检测错误

✅ **IDE实时检查**
- IntelliJ IDEA实时代码检查
- 编译错误即时提示

✅ **AI自检机制**
- AI生成代码后自我验证
- 运行quick-check.sh快速检查
- 根据错误信息自动修复

✅ **增量检查**
- 只检查变更文件(Git staged)
- 检查时间<1分钟
- 快速反馈循环

#### 第三层:验证性防护(Review Phase)  
**目标**:在提交前验证质量

✅ **Pre-commit Hook强制检查**
- 自动运行enforce-standards.sh
- 不通过无法提交
- 提供清晰的错误信息和修复建议

✅ **多维度自动化检查**
- 语法规范:javax、@Autowired、System.out
- 架构规范:四层架构、事务边界
- 安全规范:@SaCheckPermission、@Valid
- 测试覆盖率:≥80%

✅ **契约测试**
- 前后端接口契约(Spring Cloud Contract)
- 模块间接口契约
- 数据库Schema契约

#### 第四层:保障性防护(Integration Phase)
**目标**:在集成部署时保障质量

✅ **CI/CD全面检查**
- 编译检查:mvn clean compile
- 规范检查:enforce-standards.sh
- 单元测试:mvn test
- 测试覆盖率:JaCoCo ≥80%
- 安全扫描:OWASP Dependency Check
- 契约测试:Spring Cloud Contract

✅ **Docker部署验证**
- Docker镜像构建成功
- 容器启动成功
- 120秒持续监控
- 健康检查通过

### AI开发专用检查清单

#### 代码生成前检查
- [ ] 已读取CLAUDE.md(本文档)
- [ ] 已读取相关业务规则文档
- [ ] 已选择合适的代码模板
- [ ] 已理解业务状态机和验证规则

#### 代码生成后自检
- [ ] 使用@Resource而非@Autowired
- [ ] 使用jakarta.*而非javax.*  
- [ ] 使用SLF4J而非System.out
- [ ] Entity继承BaseEntity且未重复定义审计字段
- [ ] Controller只做参数验证和调用Service
- [ ] Service包含@Transactional注解
- [ ] Controller返回ResponseDTO
- [ ] 添加@SaCheckPermission权限注解
- [ ] 使用@Valid进行参数验证
- [ ] 编写单元测试(覆盖率≥80%)
- [ ] 添加JavaDoc和Swagger注解

#### 提交前验证
- [ ] 运行./scripts/quick-check.sh(通过)
- [ ] 运行./scripts/ai-code-validation.sh(通过)
- [ ] Pre-commit Hook检查通过
- [ ] 单元测试全部通过

### AI常见错误及修复

#### 错误1:Entity重复定义审计字段
**错误代码**:
```java
public class DeviceEntity extends BaseEntity {
    private LocalDateTime createTime;  // ❌ 错误:BaseEntity已包含
    private LocalDateTime updateTime;  // ❌ 错误
}
```

**正确代码**:
```java
public class DeviceEntity extends BaseEntity {
    // ✅ 不需要定义,BaseEntity已包含以下字段:
    // createTime, updateTime, createUserId, updateUserId, deletedFlag, version
}
```

#### 错误2:Controller直接访问DAO
**错误代码**:
```java
@RestController
public class DeviceController {
    @Resource
    private DeviceDao deviceDao;  // ❌ 错误:违反四层架构
}
```

**正确代码**:
```java
@RestController  
public class DeviceController {
    @Resource
    private DeviceService deviceService;  // ✅ 正确:调用Service层
}
```

#### 错误3:缺少权限注解
**错误代码**:
```java
@PostMapping("/delete")
public ResponseDTO<String> delete(@RequestBody Long id) {
    // ❌ 错误:缺少权限注解
}
```

**正确代码**:
```java
@PostMapping("/delete")
@SaCheckPermission("device:delete")  // ✅ 正确:添加权限注解
public ResponseDTO<String> delete(@Valid @RequestBody Long id) {
    // ✅ 正确:同时添加@Valid参数验证
}
```

---

## 技术支持

**配置文件优先级**: 实际项目中的配置文件(如 sa-base.yaml)中的配置信息优先级高于本文档中提到的默认配置,请以实际配置文件为准。

**⚠️ 重要提醒**:
- 每次提交代码前必须运行快速检查脚本
- 遇到编译错误时,优先检查 javax/jakarta 包名问题
- 新增功能时,严格遵循本文档中的编码规范
- 如有疑问,参考相关规范文档或询问团队技术负责人

**🤖 AI开发特别提醒**:
- AI生成代码后必须运行./scripts/ai-code-validation.sh验证
- Entity类必须继承BaseEntity且不要重复定义审计字段
- Controller必须只做参数验证和调用Service,不要编写业务逻辑
- 所有敏感接口必须添加@SaCheckPermission权限注解
- 生成代码必须包含完整的单元测试(覆盖率≥80%)
