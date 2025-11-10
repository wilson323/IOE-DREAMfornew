# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

**项目**: SmartAdmin v3 - 1024创新实验室开发的企业级快速开发平台
**技术栈**: Vue3 + SpringBoot3 + Sa-Token + MyBatis-Plus + Ant Design Vue
**语言环境**: 中文环境，请使用中文与用户交流

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

## 开发规范

### 📚 项目规范文档

本项目建立了完整的开发规范体系，所有开发工作必须严格遵循：

#### 核心规范文档
- **[综合开发规范文档](docs/DEV_STANDARDS.md)** - 详细的开发标准和规范（必读）
- **[项目开发指南](docs/PROJECT_GUIDE.md)** - 快速参考和最佳实践（开发时查阅）
- **[文档分析报告](docs/DOCUMENT_ANALYSIS_REPORT.md)** - 现有文档问题分析

#### 检查清单
- **[通用开发检查清单](docs/CHECKLISTS/通用开发检查清单.md)** - 适用于所有模块的通用标准
- **[门禁系统开发检查清单](docs/CHECKLISTS/门禁系统开发检查清单.md)** - 门禁系统专用标准
- **[消费系统开发检查清单](docs/CHECKLISTS/消费系统开发检查清单.md)** - 消费系统专用标准
- **[考勤系统开发检查清单](docs/CHECKLISTS/考勤系统开发检查清单.md)** - 考勤系统专用标准
- **[智能视频系统开发检查清单](docs/CHECKLISTS/智能视频系统开发检查清单.md)** - 智能视频系统专用标准

### 🔴 必须遵守的核心规范

#### 架构规范
- **四层架构**: 严格遵守 Controller → Service → Manager → DAO 调用链
- **依赖注入**: 使用 @Resource，禁止 @Autowired
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
- **日志记录**: 使用 SLF4J，禁止 System.out

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

### 📋 质量检查清单

#### 开发前检查
- [ ] 是否已阅读相关业务和规范文档？
- [ ] 是否已理解技术架构要求？
- [ ] 是否已确认数据库设计方案？

#### 提交前检查
- [ ] 代码是否遵循所有命名规范？
- [ ] 是否已添加必要的注释和文档？
- [ ] 是否已处理所有异常情况？
- [ ] 是否已添加权限控制？
- [ ] 单元测试覆盖率是否≥80%？
- [ ] 是否已通过相关检查清单验证？

### ⚠️ 常见错误提醒

#### 禁止的操作
- ❌ Controller层直接访问DAO层
- ❌ 在Controller层编写业务逻辑
- ❌ 在Manager层管理事务
- ❌ 使用@Autowired进行依赖注入
- ❌ 直接使用System.out.println输出日志
- ❌ 物理删除数据（必须使用软删除）

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

## 技术支持

**配置文件优先级**: 实际项目中的配置文件（如 sa-base.yaml）中的配置信息优先级高于本文档中提到的默认配置，请以实际配置文件为准。
