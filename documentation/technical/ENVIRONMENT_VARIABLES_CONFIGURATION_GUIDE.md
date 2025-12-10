# IOE-DREAM 环境变量配置指南

> **更新日期**: 2025-01-30  
> **配置阶段**: 阶段0 - 配置安全加固  
> **重要性**: P0 - 必需配置

---

## 概述

为了确保系统安全性，IOE-DREAM项目已移除所有配置文件中明文密码的默认值。所有敏感信息（密码、密钥等）必须通过环境变量提供。

---

## 快速开始

### 1. 复制环境变量模板

```bash
# 复制模板文件
cp .env.example .env

# 编辑环境变量
# Windows (PowerShell)
notepad .env

# Linux/Mac
vim .env
```

### 2. 设置必需的环境变量

#### 数据库配置（必需）
```bash
export MYSQL_HOST=127.0.0.1
export MYSQL_PORT=3306
export MYSQL_DATABASE=ioedream
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=your_actual_mysql_password
```

#### Redis配置（必需）
```bash
export REDIS_HOST=127.0.0.1
export REDIS_PORT=6379
export REDIS_PASSWORD=your_actual_redis_password
export REDIS_DATABASE=0
```

#### Nacos配置（必需）
```bash
export NACOS_SERVER_ADDR=127.0.0.1:8848
export NACOS_NAMESPACE=dev
export NACOS_GROUP=IOE-DREAM
export NACOS_USERNAME=nacos
export NACOS_PASSWORD=your_actual_nacos_password
```

### 3. 验证配置

启动服务前，确保所有必需的环境变量已设置：

```bash
# 检查环境变量（示例）
echo $MYSQL_PASSWORD
echo $REDIS_PASSWORD
echo $NACOS_PASSWORD
```

---

## 环境变量清单

### 必需环境变量（P0）

这些环境变量必须在启动服务前配置，否则服务将无法正常启动。

| 变量名 | 说明 | 默认值 | 必需 |
|--------|------|--------|------|
| `MYSQL_PASSWORD` | MySQL数据库密码 | 无（必须设置） | ✅ 是 |
| `REDIS_PASSWORD` | Redis密码 | 无（必须设置） | ✅ 是 |
| `NACOS_PASSWORD` | Nacos认证密码 | 无（必须设置） | ✅ 是 |

### 可选环境变量（P1）

这些环境变量有合理的默认值，可以根据实际情况调整。

| 变量名 | 说明 | 默认值 | 必需 |
|--------|------|--------|------|
| `MYSQL_HOST` | MySQL主机地址 | `127.0.0.1` | ❌ 否 |
| `MYSQL_PORT` | MySQL端口 | `3306` | ❌ 否 |
| `REDIS_HOST` | Redis主机地址 | `127.0.0.1` | ❌ 否 |
| `REDIS_PORT` | Redis端口 | `6379` | ❌ 否 |
| `NACOS_SERVER_ADDR` | Nacos服务器地址 | `127.0.0.1:8848` | ❌ 否 |

---

## 不同环境的配置方式

### 开发环境

#### Windows PowerShell
```powershell
# 方式1: 临时设置（当前会话有效）
$env:MYSQL_PASSWORD = "your_password"
$env:REDIS_PASSWORD = "your_password"
$env:NACOS_PASSWORD = "nacos"

# 方式2: 使用.env文件（需要加载脚本）
# 创建 load-env.ps1
Get-Content .env | ForEach-Object {
    if ($_ -match '^([^#][^=]+)=(.*)$') {
        [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2], "Process")
    }
}
```

#### Linux/Mac
```bash
# 方式1: 临时设置（当前会话有效）
export MYSQL_PASSWORD=your_password
export REDIS_PASSWORD=your_password
export NACOS_PASSWORD=nacos

# 方式2: 使用.env文件
source .env

# 方式3: 添加到 ~/.bashrc 或 ~/.zshrc
echo 'export MYSQL_PASSWORD=your_password' >> ~/.bashrc
source ~/.bashrc
```

### Docker环境

#### docker-compose.yml
```yaml
services:
  mysql:
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_PASSWORD}
  
  redis:
    environment:
      - REDIS_PASSWORD=${REDIS_PASSWORD}
  
  common-service:
    environment:
      - MYSQL_PASSWORD=${MYSQL_PASSWORD}
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - NACOS_PASSWORD=${NACOS_PASSWORD}
    env_file:
      - .env
```

#### 启动命令
```bash
# 使用环境变量文件
docker-compose --env-file .env up -d
```

### Kubernetes环境

使用Kubernetes Secrets管理敏感信息：

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: ioedream-secrets
type: Opaque
data:
  mysql-password: <base64-encoded-password>
  redis-password: <base64-encoded-password>
  nacos-password: <base64-encoded-password>
```

在Deployment中引用：
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ioedream-common-service
spec:
  template:
    spec:
      containers:
      - name: common-service
        env:
        - name: MYSQL_PASSWORD
          valueFrom:
            secretKeyRef:
              name: ioedream-secrets
              key: mysql-password
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: ioedream-secrets
              key: redis-password
        - name: NACOS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: ioedream-secrets
              key: nacos-password
```

### 生产环境

#### 使用Nacos配置中心（推荐）

1. 登录Nacos控制台
2. 创建配置（命名空间：prod）
3. 使用加密配置格式存储敏感信息

#### 使用密钥管理服务

- AWS Secrets Manager
- Azure Key Vault
- HashiCorp Vault
- Alibaba Cloud KMS

---

## 安全最佳实践

### 1. 密码强度要求

- **长度**: 至少16个字符
- **复杂度**: 包含大小写字母、数字、特殊字符
- **唯一性**: 每个环境使用不同的密码

### 2. 密码轮换策略

- **开发环境**: 每季度轮换一次
- **测试环境**: 每季度轮换一次
- **生产环境**: 每半年轮换一次

### 3. 访问控制

- 限制`.env`文件的访问权限（600）
- 不要将`.env`文件提交到代码仓库
- 使用`.gitignore`排除`.env`文件

### 4. 审计和监控

- 定期审计环境变量使用情况
- 监控环境变量的访问日志
- 建立异常访问告警机制

---

## 故障排查

### 问题1: 服务启动失败，提示缺少环境变量

**错误信息**:
```
Could not resolve placeholder 'MYSQL_PASSWORD' in value "${MYSQL_PASSWORD}"
```

**解决方法**:
1. 检查环境变量是否已设置：`echo $MYSQL_PASSWORD`
2. 确认`.env`文件已正确加载
3. 验证环境变量名称拼写正确

### 问题2: 数据库连接失败

**错误信息**:
```
Access denied for user 'root'@'localhost' (using password: YES)
```

**解决方法**:
1. 验证`MYSQL_PASSWORD`环境变量值正确
2. 检查MySQL用户权限
3. 确认MySQL服务正常运行

### 问题3: Redis连接失败

**错误信息**:
```
NOAUTH Authentication required
```

**解决方法**:
1. 验证`REDIS_PASSWORD`环境变量值正确
2. 检查Redis配置中的`requirepass`设置
3. 确认Redis服务正常运行

---

## 相关文档

- [配置安全加固实施文档](./CONFIGURATION_SECURITY_HARDENING_IMPLEMENTATION.md)
- [Jasypt加密配置指南](./JASYPT_ENCRYPTION_GUIDE.md)
- [部署指南](../deployment/)

---

## 更新日志

- **2025-01-30**: 初始版本，移除所有明文密码默认值

