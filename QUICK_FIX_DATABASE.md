# 数据库启动问题快速修复

## 问题原因

**nacos-schema.sql 文件错误** - 该文件包含的是Nacos启动日志，而不是SQL脚本！

错误内容示例：
```
Nacos is starting, you can docker logs your container
org.springframework.context.ApplicationContextException...
```

## 立即修复步骤

### 步骤1: 下载正确的Nacos Schema

访问以下任一链接，手动下载并保存到 `deployment/mysql/init/nacos-schema.sql`:

1. **GitHub** (可能需要梯子):
   https://raw.githubusercontent.com/alibaba/nacos/2.3.0/distribution/conf/mysql-schema.sql

2. **Gitee** (国内镜像):
   https://gitee.com/mirrors/nacos/raw/2.3.0/distribution/conf/mysql-schema.sql

### 步骤2: 合并业务SQL脚本

在PowerShell中执行:
```powershell
# 合并所有业务SQL
Get-Content database-scripts/common-service/*.sql | Out-File deployment/mysql/init/01-ioedream-init.sql -Encoding UTF8
```

### 步骤3: 启动MySQL并手动初始化

```powershell
# 只启动MySQL
docker-compose -f docker-compose-all.yml up -d mysql

# 等待30秒让MySQL完全启动
Start-Sleep -Seconds 30

# 执行Nacos Schema
Get-Content deployment/mysql/init/nacos-schema.sql | docker exec -i ioedream-mysql mysql -uroot -proot

# 执行业务SQL
Get-Content deployment/mysql/init/01-ioedream-init.sql | docker exec -i ioedream-mysql mysql -uroot -proot
```

### 步骤4: 启动所有服务

```powershell
# 启动所有服务
docker-compose -f docker-compose-all.yml up -d

# 查看Nacos日志
docker logs ioedream-nacos -f

# 查看服务状态
docker-compose -f docker-compose-all.yml ps
```

## 为什么MyBatis-Plus不自动建表？

**重要说明**: MyBatis-Plus **不支持**自动建表功能！

| 特性 | JPA/Hibernate | MyBatis-Plus |
|------|---------------|--------------|
| 自动建表 | ✅ 支持 `ddl-auto` | ❌ **不支持** |
| 表结构更新 | ✅ 自动检测 | ❌ 需要手动SQL |
| SQL控制 | ❌ 自动生成 | ✅ 完全控制 |
| 性能 | ⚠️ 较低 | ✅ 高性能 |

### IOE-DREAM为什么选择MyBatis-Plus？

1. **性能优先** - 生产环境需要高性能
2. **SQL可控** - 复杂业务查询需要精确控制
3. **架构规范** - 符合CLAUDE.md企业级标准
4. **灵活性** - 支持动态SQL、多表关联

### 正确的数据库管理方式

```
开发环境:
1. 手动执行SQL脚本 ✅
2. 使用Flyway自动迁移 ✅ (推荐)

生产环境:
1. Flyway版本化管理 ✅
2. 严格的变更审批 ✅
3. 数据备份 ✅
```

## 验证修复成功

### 检查Nacos
```bash
# Nacos应该显示类似：
# Nacos started successfully in stand alone mode
```

### 检查数据库表
```bash
docker exec -it ioedream-mysql mysql -uroot -proot -e "USE nacos; SHOW TABLES;"
docker exec -it ioedream-mysql mysql -uroot -proot -e "USE ioedream; SHOW TABLES;"
```

应该看到：
- **nacos库**: 15+张表 (config_info, users, roles等)
- **ioedream库**: 18+张表 (t_user, t_role等)

## 常见问题

### Q: 为什么nacos-schema.sql是日志文件？
A: 可能是之前错误地将Docker日志输出保存成了SQL文件

### Q: 能让MyBatis-Plus自动建表吗？
A: **不能**！这不是bug，MyBatis-Plus本来就不支持

### Q: 生产环境怎么管理数据库变更？
A: 使用Flyway或Liquibase进行版本化管理

## 下一步

修复完成后，建议：

1. ✅ 阅读 `STARTUP_ISSUE_ANALYSIS.md` 了解完整的问题分析
2. ✅ 考虑集成Flyway实现自动化数据库迁移
3. ✅ 建立数据库变更管理流程
