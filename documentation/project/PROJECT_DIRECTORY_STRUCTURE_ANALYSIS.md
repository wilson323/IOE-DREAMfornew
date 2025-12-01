# IOE-DREAM 项目全局目录结构分析

> **文档版本**: v1.0
> **更新时间**: 2025-11-24
> **项目**: IOE-DREAM 智慧园区一卡通管理平台
> **技术栈**: Vue3 + Java 17 + Spring Boot 3.x + Sa-Token + MyBatis-Plus

## 📋 项目概览

IOE-DREAM 是一个基于 SmartAdmin v3 构建的智慧园区一卡通管理平台，采用前后端分离架构，遵循四层架构设计规范（Controller→Service→Manager→DAO），严格依赖 repowiki 规范体系。

### 核心技术架构
- **前端**: Vue3 + TypeScript + Pinia + Ant Design Vue 4.x
- **后端**: Java 17 + Spring Boot 3.5.4 + Sa-Token + MyBatis-Plus
- **数据库**: MySQL 8.0 + Redis
- **移动端**: uni-app (Vue3版本)
- **容器化**: Docker + Kubernetes

## 🏗️ 项目目录结构

### 根目录结构

```
IOE-DREAM/
├── 📁 smart-admin-api-java17-springboot3/  # 后端项目核心
├── 📁 smart-admin-web-javascript/          # 前端项目核心
├── 📁 smart-app/                           # 移动端项目
├── 📁 docs/                                # 项目文档中心
├── 📁 scripts/                             # 自动化脚本集合
├── 📁 docker/                              # Docker配置文件
├── 📁 deploy/                              # 部署配置
├── 📁 k8s/                                 # Kubernetes配置
├── 📁 templates/                           # 代码模板库
├── 📁 logs/                                # 日志文件存储
├── 📁 monitoring/                          # 监控配置
├── 📁 数据库SQL脚本/                        # 数据库初始化脚本
├── 📁 openspec/                            # OpenSpec规范管理
├── 📁 .claude/                             # Claude AI配置
├── 📁 .husky/                              # Git Hooks
├── 📁 archives/                            # 归档文件
├── 📁 reports/                             # 报告输出
└── 📄 根配置文件                            # 项目根级配置
```

---

## 📂 详细目录分析

### 🎯 核心应用目录

#### 1. `smart-admin-api-java17-springboot3/` - 后端项目
**作用**: Java后端服务，基于Spring Boot 3.x + Sa-Token
**负责人**: 后端开发团队

```
smart-admin-api-java17-springboot3/
├── 📁 sa-base/                    # 基础模块（公共组件、配置、工具类）
│   ├── src/main/java/net/lab1024/sa/base/
│   │   ├── common/               # 公共实体、工具类、常量
│   │   ├── config/               # 配置类（安全、缓存、数据库等）
│   │   ├── util/                 # 工具类集合
│   │   └── constant/             # 常量定义
├── 📁 sa-admin/                   # 管理模块
│   └── src/main/java/net/lab1024/sa/admin/
│       ├── controller/           # 控制器层（四层架构第一层）
│       ├── service/              # 服务层（四层架构第二层）
│       ├── manager/              # 管理层（四层架构第三层）
│       ├── dao/                  # 数据访问层（四层架构第四层）
│       └── module/               # 业务模块（门禁、消费、考勤等）
├── 📁 sa-support/                 # 支持模块
└── scripts/                      # 后端专用脚本
```

**❌ 不应放置的文件**:
- 前端资源文件（*.vue, *.js, *.css）
- 配置文件硬编码（应使用配置中心）
- 敏感信息明文（密码、密钥等）
- 物理删除操作的SQL脚本

#### 2. `smart-admin-web-javascript/` - 前端项目
**作用**: Vue3前端应用，管理界面
**负责人**: 前端开发团队

```
smart-admin-web-javascript/
├── 📁 src/
│   ├── api/                      # API接口封装
│   ├── assets/                   # 静态资源（图片、字体）
│   ├── components/               # 公共组件
│   ├── config/                   # 配置文件（环境变量、主题）
│   ├── constants/                # 常量定义（枚举、字典）
│   ├── directives/               # 自定义指令
│   ├── i18n/                     # 国际化文件
│   ├── layout/                   # 布局组件
│   ├── lib/                      # 第三方库扩展
│   ├── plugins/                  # 插件配置
│   ├── router/                   # 路由配置
│   ├── stores/                   # Pinia状态管理
│   ├── theme/                    # 主题配置
│   ├── utils/                    # 工具函数
│   ├── views/                    # 页面组件
│   ├── App.vue                   # 根组件
│   └── main.js                   # 入口文件
├── 📁 public/                    # 公共静态文件
├── 📁 tests/                     # 测试文件
├── vite.config.js               # Vite构建配置
└── package.json                 # 依赖配置
```

**❌ 不应放置的文件**:
- 后端Java代码
- 配置文件中的敏感信息
- 大型媒体文件（应放CDN）
- 浏览器缓存文件

#### 3. `smart-app/` - 移动端项目
**作用**: uni-app移动端应用
**负责人**: 移动端开发团队

```
smart-app/
├── 📁 pages/                     # 页面文件
├── 📁 src/                       # 源代码
├── 📁 api/                       # API接口
├── 📁 utils/                     # 工具函数
└── manifest.json                # uni-app配置
```

**❌ 不应放置的文件**:
- PC端专用组件
- 复杂的第三方库（影响性能）
- 大量静态资源

---

### 📚 文档与规范目录

#### 4. `docs/` - 项目文档中心
**作用**: 项目文档、规范、指南的统一管理
**负责人**: 技术文档团队

```
docs/
├── 📁 repowiki/                   # repowiki权威规范体系 ⭐
│   └── zh/content/               # 中文规范文档
├── 📁 00-快速开始/                # 快速开始指南
├── 📁 ADR/                        # 架构决策记录
├── 📁 API文档/                     # API接口文档
├── 📁 ARCHITECTURE_STANDARDS/     # 架构设计规范
├── 📁 business-rules/             # 业务规则文档
├── 📁 CHECKLISTS/                 # 开发检查清单
├── 📁 COMMON_MODULES/             # 公共模块设计
├── 📁 DEPLOY/                     # 部署指南
├── 📁 DEVICE_MANAGEMENT/          # 设备管理文档
├── 📁 PROJECT_PLAN/               # 项目规划
├── 📁 SECURITY/                   # 安全文档
├── 📁 STANDARDS/                  # 技术标准
├── 📁 templates/                  # 文档模板
├── 📁 TESTING/                    # 测试文档
├── 📁 training/                   # 培训材料
├── 📁 各业务模块文档/               # 业务模块文档
├── 📁 管理员指南/                   # 管理员操作指南
└── 📁 用户手册/                    # 用户操作手册
```

**❌ 不应放置的文件**:
- 过时的文档（应归档到archives/）
- 代码文件（应放在项目目录）
- 个人笔记（应放在个人目录）
- 临时文件

#### 5. `openspec/` - OpenSpec规范管理
**作用**: 变更提案、规格管理、审批流程
**负责人**: 架构师团队

```
openspec/
├── 📁 changes/                    # 变更提案
├── 📁 specs/                      # 技术规格
├── 📁 archive/                    # 归档规格
├── project.md                     # 项目规格
└── AGENTS.md                      # AI助手指南
```

**❌ 不应放置的文件**:
- 代码实现文件
- 临时讨论记录
- 个人意见文档
- 非标准化文档

---

### 🛠️ 工具与脚本目录

#### 6. `scripts/` - 自动化脚本集合
**作用**: 项目自动化、质量检查、部署脚本
**负责人**: DevOps团队

```
scripts/
├── 📁 git-hooks/                  # Git钩子脚本
├── 📁 quarantined-scripts/        # 隔离脚本（存在风险）
├── 📁 safe-scripts/               # 安全脚本
├── 🐧 编译修复脚本/                 # Linux编译修复
├── 🪟 编译修复脚本/                 # Windows编译修复
├── 🔧 质量检查脚本/                 # 代码质量检查
├── 🐳 部署脚本/                    # Docker部署
├── 🔒 安全检查脚本/                 # 安全扫描
└── 📊 监控脚本/                    # 性能监控
```

**主要脚本类别**:
- **编译修复**: fix-*.sh, compile-*.sh
- **质量检查**: quality-*.sh, check-*.sh
- **部署运维**: deploy-*.sh, docker-*.sh
- **编码规范**: encoding-*.sh, standards-*.sh

**❌ 不应放置的文件**:
- 破坏性脚本（未经安全审查）
- 个人临时脚本
- 硬编码的配置脚本
- 未测试的脚本

---

### 🔧 配置与部署目录

#### 7. `docker/` - Docker配置
**作用**: 容器化配置
**负责人**: 运维团队

```
docker/
├── 📁 mysql/                      # MySQL配置
├── 📁 nginx/                      # Nginx配置
├── 📁 redis/                      # Redis配置
└── docker-compose.yml             # 容器编排
```

**❌ 不应放置的文件**:
- 应用代码
- 敏感配置（应使用环境变量）
- 日志文件
- 临时文件

#### 8. `deploy/` - 部署配置
**作用**: 部署脚本和配置
**负责人**: 运维团队

```
deploy/
└── 📁 kubernetes/                 # K8s部署配置
```

**❌ 不应放置的文件**:
- 生产环境密钥
- 个人配置文件
- 临时部署脚本

#### 9. `k8s/` - Kubernetes配置
**作用**: K8s集群配置
**负责人**: 运维团队

```
k8s/
├── namespace.yaml                # 命名空间
├── configmap.yaml                # 配置映射
├── secret.yaml                   # 密钥配置
├── deployment.yaml               # 部署配置
└── service.yaml                  # 服务配置
```

**❌ 不应放置的文件**:
- 明文密码
- 开发环境配置
- 个人调试配置

---

### 📊 其他重要目录

#### 10. `templates/` - 代码模板库
**作用**: 标准化代码模板
**负责人**: 架构师团队

```
templates/
├── 📁 config/                     # 配置文件模板
├── 📁 controller/                 # Controller模板
├── 📁 entity/                     # 实体类模板
├── 📁 service/                    # Service模板
└── 📁 test/                       # 测试模板
```

**❌ 不应放置的文件**:
- 项目特定代码
- 不符合规范的模板
- 过时的模板

#### 11. `logs/` - 日志存储
**作用**: 应用日志存储
**负责人**: 运维团队

```
logs/
├── 📁 backend/                    # 后端日志
├── 📁 nginx/                      # Nginx日志
└── backend;D/                     # 异常日志目录（命名不规范）
```

**❌ 不应放置的文件**:
- 应用代码
- 配置文件
- 临时文件
- 大型二进制文件

#### 12. `monitoring/` - 监控配置
**作用**: 系统监控配置
**负责人**: 运维团队

```
monitoring/
├── prometheus.yml                # Prometheus配置
├── grafana/                      # Grafana配置
└── alerts/                       # 告警配置
```

**❌ 不应放置的文件**:
- 敏感监控数据
- 个人监控配置
- 临时监控脚本

#### 13. `数据库SQL脚本/` - 数据库脚本
**作用**: 数据库初始化和迁移脚本
**负责人**: DBA团队

```
数据库SQL脚本/
└── 📁 mysql/                      # MySQL脚本
    ├── smart_admin_v3.sql        # 初始化脚本
    └── migration/                # 迁移脚本
```

**❌ 不应放置的文件**:
- 生产数据脚本
- 个人测试脚本
- 未审核的SQL脚本

---

### 🤖 AI辅助开发目录

#### 14. `.claude/` - Claude AI配置
**作用**: Claude AI助手配置和技能
**负责人**: AI开发团队

```
.claude/
├── 📁 commands/                   # 自定义命令
│   └── openspec/                  # OpenSpec命令
└── 📁 skills/                     # AI技能库
    ├── business-operations/       # 业务操作技能
    ├── device-management/         # 设备管理技能
    └── technical-skills/          # 技术开发技能
```

**❌ 不应放置的文件**:
- 项目代码
- 个人配置
- 敏感信息

#### 15. `.husky/` - Git Hooks
**作用**: Git提交钩子
**负责人**: 开发团队

```
.husky/
├── 📁 _/                          # Husky内置脚本
├── pre-commit                     # 提交前检查
├── pre-push                       # 推送前检查
└── commit-msg                     # 提交信息检查
```

**❌ 不应放置的文件**:
- 破坏性脚本
- 个人钩子配置
- 未经测试的钩子

---

### 📋 其他辅助目录

#### 16. `archives/` - 归档文件
**作用**: 历史文件归档
**负责人**: 文档管理团队

```
archives/
└── 📁 backups/                    # 备份文件
```

**❌ 不应放置的文件**:
- 活跃项目文件
- 敏感信息
- 临时文件

#### 17. `reports/` - 报告输出
**作用**: 各类报告输出
**负责人**: 质量保证团队

```
reports/
├── compliance-reports/            # 合规报告
└── security-analysis-reports/     # 安全分析报告
```

**❌ 不应放置的文件**:
- 个人报告
- 未审核的报告
- 临时报告

#### 18. `node_modules/` - 依赖包
**作用**: 前端依赖包
**负责人**: 前端开发团队

**❌ 不应放置的文件**:
- 源代码
- 配置文件
- 个人文件

#### 19. 数据库配置文件
**作用**: 数据库连接配置
**位置**: `smart-admin-api-java17-springboot3/sa-base/src/main/resources/dev/`

**重要配置**:
- 数据库连接: `192.168.10.110:33060/smart_admin_v3`
- Redis连接: `127.0.0.1:6389`
- 文件上传路径: `D:/Progect/mart-admin-master/upload/`

---

## 🔒 目录权限与责任矩阵

| 目录 | 负责人 | 访问权限 | 变更审批 |
|------|--------|----------|----------|
| `smart-admin-api-java17-springboot3/` | 后端团队 | 读写 | 代码审查 |
| `smart-admin-web-javascript/` | 前端团队 | 读写 | 代码审查 |
| `docs/repowiki/` | 架构师 | 读写 | 架构师审批 |
| `openspec/` | 架构师 | 读写 | 架构师审批 |
| `scripts/` | DevOps | 读写 | 运维审批 |
| `docker/`, `k8s/`, `deploy/` | 运维团队 | 读写 | 运维审批 |
| `templates/` | 架构师 | 读写 | 架构师审批 |
| `数据库SQL脚本/` | DBA | 读写 | DBA审批 |

---

## 🚨 目录使用规范与最佳实践

### ✅ 推荐实践

1. **严格按照四层架构**: Controller→Service→Manager→DAO
2. **统一使用repowiki规范**: 所有开发工作严格遵循 `docs/repowiki/` 下的规范
3. **编码标准零容忍**: UTF-8编码、jakarta包名、@Resource注入
4. **安全第一原则**: 敏感信息使用环境变量，禁止硬编码
5. **文档同步更新**: 代码变更必须同步更新相关文档

### ❌ 禁止行为

1. **禁止跨架构层级调用**: Controller不能直接访问DAO
2. **禁止硬编码配置**: 配置信息必须外部化
3. **禁止物理删除**: 必须使用软删除机制
4. **禁止提交敏感信息**: 密码、密钥、个人信息等
5. **禁止绕过质量门禁**: 所有提交必须通过自动检查

### 🔄 目录维护机制

1. **定期清理**: 临时文件、过期文档及时归档
2. **权限审计**: 定期检查目录访问权限
3. **规范更新**: 根据项目发展更新目录结构
4. **自动化检查**: 使用脚本验证目录结构合规性

---

## 📝 目录变更记录

| 版本 | 日期 | 变更内容 | 变更人 |
|------|------|----------|--------|
| v1.0 | 2025-11-24 | 初始版本创建，完整梳理项目目录结构 | AI助手 |

---

**💡 核心提示**:
- 本文档必须与实际项目结构保持同步
- 任何目录结构变更都需要更新此文档
- 严格按照repowiki规范执行所有开发工作
- 定期执行 `scripts/quality-check.sh` 验证项目结构健康度