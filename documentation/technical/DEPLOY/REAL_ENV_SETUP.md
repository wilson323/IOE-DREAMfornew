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
1. 创建数据库:
   ```powershell
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
