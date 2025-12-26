# IOE-DREAM 环境变量配置指南

**创建时间**: 2025-12-23
**目的**: 确保敏感信息通过环境变量配置，不使用明文默认值
**优先级**: P0 - 安全配置

---

## 🔐 必需的环境变量

### 数据库配置（所有服务）

```bash
# MySQL数据库配置
export MYSQL_HOST=127.0.0.1
export MYSQL_PORT=3306
export MYSQL_DATABASE=ioedream
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=your_secure_password_here

# 测试环境数据库
export MYSQL_TEST_HOST=127.0.0.1
export MYSQL_TEST_PORT=3306
export MYSQL_TEST_DATABASE=ioedream_test
export MYSQL_TEST_USERNAME=root
export MYSQL_TEST_PASSWORD=your_test_password_here
```

### Redis配置（所有服务）

```bash
# Redis连接配置
export REDIS_HOST=127.0.0.1
export REDIS_PORT=6379
export REDIS_PASSWORD=your_redis_password_here
export REDIS_DATABASE=0
```

### Nacos配置（所有微服务）

```bash
# Nacos服务发现和配置中心
export NACOS_SERVER_ADDR=127.0.0.1:8848
export NACOS_NAMESPACE=dev
export NACOS_GROUP=IOE-DREAM
export NACOS_USERNAME=nacos
export NACOS_PASSWORD=your_nacos_password_here
```

### Druid监控配置

```bash
# Druid监控后台访问
export DRUID_ADMIN_USERNAME=admin
export DRUID_ADMIN_PASSWORD=your_druid_admin_password_here
```

---

## 📋 配置优先级

配置加载优先级（从高到低）：
1. **环境变量** (推荐) - 最安全，优先级最高
2. **启动参数** - `--spring.datasource.password=xxx`
3. **配置文件** - `application-{profile}.yml`
4. **默认值** - 已全部移除（不再安全）

---

## 🚀 快速启动配置

### 方式1: 使用.env文件（开发环境）

创建`.env`文件在项目根目录：

```bash
# .env文件内容
MYSQL_PASSWORD=mysql_secure_pass_2025
REDIS_PASSWORD=redis_secure_pass_2025
NACOS_PASSWORD=nacos_secure_pass_2025
DRUID_ADMIN_PASSWORD=druid_secure_pass_2025
```

加载环境变量：
```bash
# Windows PowerShell
Get-Content .env | ForEach-Object {
    $var = $_.Split('=')
    [Environment]::SetEnvironmentVariable($var[0], $var[1])
}

# Linux/Mac
export $(cat .env | xargs)
```

### 方式2: 使用启动脚本（推荐）

**Windows**: `scripts/set-env.bat`
```batch
@echo off
set MYSQL_PASSWORD=mysql_secure_pass_2025
set REDIS_PASSWORD=redis_secure_pass_2025
set NACOS_PASSWORD=nacos_secure_pass_2025
set DRUID_ADMIN_PASSWORD=druid_secure_pass_2025

echo 环境变量已设置
```

**Linux/Mac**: `scripts/set-env.sh`
```bash
#!/bin/bash
export MYSQL_PASSWORD=mysql_secure_pass_2025
export REDIS_PASSWORD=redis_secure_pass_2025
export NACOS_PASSWORD=nacos_secure_pass_2025
export DRUID_ADMIN_PASSWORD=druid_secure_pass_2025

echo "环境变量已设置"
```

### 方式3: Docker环境（生产环境）

在`docker-compose.yml`中配置：

```yaml
services:
  mysql:
    environment:
      - MYSQL_ROOT_PASSWORD=mysql_secure_pass_2025
      - MYSQL_DATABASE=ioedream
      - MYSQL_USER=ioedream
      - MYSQL_PASSWORD=mysql_secure_pass_2025

  redis:
    command: redis-server --requirepass redis_secure_pass_2025

  nacos:
    environment:
      - NACOS_AUTH_TOKEN=SecureToken_nacos_2025
      - MYSQL_SERVICE_HOST=mysql
      - MYSQL_SERVICE_DB_NAME=ioedream
      - MYSQL_SERVICE_USER=root
      - MYSQL_SERVICE_PASSWORD=mysql_secure_pass_2025

  ioedream-access-service:
    env_file:
      - .env.production
    environment:
      - MYSQL_PASSWORD=${MYSQL_PASSWORD}
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - NACOS_PASSWORD=${NACOS_PASSWORD}
      - DRUID_ADMIN_PASSWORD=${DRUID_ADMIN_PASSWORD}
```

---

## ⚠️ 安全提示

### 密码安全要求

1. **长度要求**: 至少12个字符
2. **复杂度要求**: 包含大小写字母、数字、特殊字符
3. **定期更换**: 每3-6个月更换一次
4. **禁止使用**:
   - ❌ 常见密码（admin123, 123456, password等）
   - ❌ 字典单词
   - ❌ 个人信息（生日、姓名等）

### 密码示例（安全）

✅ **推荐**:
- `Mysql@2025#Secure!Pass`
- `Redis*Production$789`
- `Nacos&Config^Admin%123`

❌ **禁止**:
- `admin123`
- `123456`
- `password`
- `nacos`
- `root`

---

## 🔍 验证配置

### 检查环境变量是否设置

**Windows PowerShell**:
```powershell
echo $env:MYSQL_PASSWORD
echo $env:REDIS_PASSWORD
echo $env:NACOS_PASSWORD
echo $env:DRUID_ADMIN_PASSWORD
```

**Linux/Mac**:
```bash
echo $MYSQL_PASSWORD
echo $REDIS_PASSWORD
echo $NACOS_PASSWORD
echo $DRUID_ADMIN_PASSWORD
```

### 验证应用启动

启动应用后检查日志：

```
✅ 成功: Database connected successfully
✅ 成功: Redis connected successfully
✅ 成功: Nacos registry initialized
❌ 失败: Access denied for user ''@'localhost' → 密码未设置
```

---

## 🛠️ 故障排查

### 问题1: 数据库连接失败

**错误信息**:
```
Access denied for user 'root'@'localhost'
```

**解决方案**:
1. 检查环境变量是否设置: `echo $MYSQL_PASSWORD`
2. 验证密码是否正确
3. 确认MySQL服务运行中: `mysql -u root -p`

### 问题2: Redis连接失败

**错误信息**:
```
NOAUTH Authentication required
```

**解决方案**:
1. 检查Redis是否设置了密码: `redis-cli CONFIG GET requirepass`
2. 设置环境变量: `export REDIS_PASSWORD=your_password`
3. 重启应用

### 问题3: Nacos注册失败

**错误信息**:
```
nacos client failed to authenticate
```

**解决方案**:
1. 检查Nacos是否启用认证: 访问 http://localhost:8848/nacos
2. 确认用户名密码: 默认 nacos/nacos
3. 设置环境变量: `export NACOS_USERNAME=nacos` 和 `export NACOS_PASSWORD=nacos`

---

## 📚 参考文档

- **Spring Boot配置**: https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html
- **环境变量**: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config
- **Druid监控**: https://github.com/alibaba/druid/wiki

---

**维护人**: IOE-DREAM 架构委员会
**更新时间**: 2025-12-23
**下次审查**: 2025-03-23
