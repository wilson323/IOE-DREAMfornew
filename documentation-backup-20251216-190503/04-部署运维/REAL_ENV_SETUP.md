# SmartAdmin Windows 实战部署手册

> 版本: v1.0.0  
> 责任人: 运维支持组  
> 最后更新: 2025-11-12

## 1. 环境准备
- 操作系统: Windows 10/11 专业版, PowerShell 5.1+
- JDK: Temurin OpenJDK 17 (设置 `JAVA_HOME`)
- Maven: 3.9+, 配置镜像指向公司仓库
- Node.js: 18 LTS, npm 9+
- 数据库: MySQL 8.0+, Redis 7.0+, 本地或内网实例
- 必要工具: Git 2.40+, 7-Zip, Visual C++ 运行库, `netsh` 权限

## 2. 目录约定
```
D:\IOE-DREAM\
  ├── smart-admin-api-java17-springboot3\
  ├── smart-admin-web-javascript\
  ├── docs\
  └── scripts\
```

## 3. 数据库初始化
## 📋 IOE-DREAM七微服务架构

**核心架构组成**:
- **Gateway Service (8080)**: API网关
- **Common Service (8088)**: 公共模块微服务
- **DeviceComm Service (8087)**: 设备通讯微服务
- **OA Service (8089)**: OA微服务
- **Access Service (8090)**: 门禁服务
- **Attendance Service (8091)**: 考勤服务
- **Video Service (8092)**: 视频服务
- **Consume Service (8094)**: 消费服务
- **Visitor Service (8095)**: 访客服务

**架构特点**:
- 基于Spring Boot 3.5.8 + Java 17
- 严格遵循企业级微服务规范
- 支持高并发、高可用、水平扩展

**技术栈标准**:
- **数据库**: MySQL 8.0 + Druid连接池
- **缓存**: Redis + Caffeine多级缓存
- **注册中心**: Nacos
- **配置中心**: Nacos Config
- **认证授权**: Sa-Token
1. 创建数据库:
## 🏗️ 四层架构规范

**标准架构模式**:
```
Controller (接口控制层)
    ↓
Service (核心业务层)
    ↓
Manager (流程管理层)
    ↓
DAO (数据访问层)
```

**层级职责**:
- **Controller层**: HTTP请求处理、参数验证、权限控制
- **Service层**: 核心业务逻辑、事务管理、业务规则验证
- **Manager层**: 复杂流程编排、多数据组装、第三方服务集成
- **DAO层**: 数据库CRUD操作、SQL查询实现、数据访问边界

**严格禁止跨层访问**: Controller不能直接调用Manager/DAO！
   ```powershell
## ⚠️ IOE-DREAM零容忍规则（强制执行）

**必须遵守的架构规则**:
- ✅ **必须使用 @Resource 注入依赖**
- ✅ **必须使用 @Mapper 注解** (禁止@Repository)
- ✅ **必须使用 Dao 后缀** (禁止Repository)
- ✅ **必须使用 @RestController 注解**
- ✅ **必须使用 @Valid 参数校验**
- ✅ **必须返回统一ResponseDTO格式**
- ✅ **必须遵循四层架构边界**

**严格禁止事项**:
- ❌ **禁止使用 @Autowired 注入**
- ❌ **禁止使用 @Repository 注解**
- ❌ **禁止使用 Repository 后缀命名**
- ❌ **禁止跨层访问**
- ❌ **禁止在Controller中包含业务逻辑**
- ❌ **禁止直接访问数据库**

**违规后果**: P0级问题，立即修复，禁止合并！
   mysql -h 127.0.0.1 -P 3306 -u root -p "CREATE DATABASE smart_admin_v3 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
   ```
2. 导入脚本:
   ```powershell
   mysql -h 127.0.0.1 -P 3306 -u root smart_admin_v3 < "数据库SQL脚本/mysql/smart_admin_v3.sql"
   ```
3. Redis 配置: 确保 `redis.windows.conf` 中 `requirepass` 与项目配置一致。

## 4. 后端部署
1. 切换目录:
   ```powershell
   Set-Location D:\IOE-DREAM\smart-admin-api-java17-springboot3
   ```
2. 安装依赖:
   ```powershell
   mvn clean install -DskipTests
   ```
3. 配置文件: 编辑 `sa-base/src/main/resources/dev/sa-base.yaml`, 检查数据库、Redis、文件存储路径。
4. 启动服务:
   ```powershell
   Set-Location .\sa-admin
   mvn spring-boot:run
   ```
5. 验证: 访问 `http://localhost:1024/doc.html` 确认接口文档加载正常。

## 5. 前端部署
1. 切换目录并安装依赖:
   ```powershell
   Set-Location D:\IOE-DREAM\smart-admin-web-javascript
   npm install
   ```
2. 启动开发服务器:
   ```powershell
   npm run localhost
   ```
3. 访问 `http://localhost:8081` 验证登录页面加载。

## 6. 运行验证
- 登录系统并确认菜单与权限加载;
- 检查控制台无严重错误, 后端日志无异常堆栈;
- 执行一次设备注册、权限查询, 确认数据库写入成功;
- 执行 `npm run build:test` 验证前端打包。

## 7. 常见问题排查
- 端口冲突: 使用 `netstat -ano | findstr :1024` 定位进程;
- JDK 未配置: PowerShell 执行 `java -version` 确认;
- 数据库连接失败: 检查 `sa-base.yaml` 中的 `jdbcUrl` 与防火墙策略;
- Redis 鉴权失败: 确认密码一致并重启服务。

## 8. 变更与维护
- 环境变动须更新本手册并记录于 `CHANGELOG.md`;
- 建议使用 Windows 任务计划定时备份数据库与 Redis;
- 新增节点部署时同步执行上述步骤并记录服务器清单。
