# IOE-DREAM 环境变量配置文档

> **创建时间**: 2025-01-30  
> **用途**: 所有微服务必需的环境变量配置说明  
> **安全等级**: 生产环境必须使用环境变量，禁止硬编码

---

## 📋 必需环境变量清单

### 数据库配置

| 变量名 | 说明 | 默认值 | 必需 | 示例 |
|--------|------|--------|------|------|
| `DATABASE_URL` | 数据库连接URL | `jdbc:mysql://127.0.0.1:3306/ioedream` | ✅ | `jdbc:mysql://db.example.com:3306/ioedream` |
| `DATABASE_HOST` | 数据库主机 | `127.0.0.1` | ✅ | `db.example.com` |
| `DATABASE_PORT` | 数据库端口 | `3306` | ✅ | `3306` |
| `DATABASE_NAME` | 数据库名称 | `ioedream` | ✅ | `ioedream` |
| `DATABASE_USERNAME` | 数据库用户名 | `root` | ✅ | `ioedream_user` |
| `DATABASE_PASSWORD` | 数据库密码 | - | ✅ | `your_secure_password` |

### Redis配置

| 变量名 | 说明 | 默认值 | 必需 | 示例 |
|--------|------|--------|------|------|
| `REDIS_HOST` | Redis主机 | `127.0.0.1` | ✅ | `redis.example.com` |
| `REDIS_PORT` | Redis端口 | `6379` | ✅ | `6379` |
| `REDIS_PASSWORD` | Redis密码 | - | ⚠️ | `your_redis_password` |

### Nacos配置

| 变量名 | 说明 | 默认值 | 必需 | 示例 |
|--------|------|--------|------|------|
| `NACOS_SERVER_ADDR` | Nacos服务器地址 | `127.0.0.1:8848` | ✅ | `nacos.example.com:8848` |
| `NACOS_NAMESPACE` | Nacos命名空间 | `dev` | ✅ | `prod` |
| `NACOS_GROUP` | Nacos配置组 | `IOE-DREAM` | ✅ | `IOE-DREAM` |
| `NACOS_USERNAME` | Nacos用户名 | `nacos` | ✅ | `nacos` |
| `NACOS_PASSWORD` | Nacos密码 | `nacos` | ✅ | `your_nacos_password` |

### JWT配置

| 变量名 | 说明 | 默认值 | 必需 | 示例 |
|--------|------|--------|------|------|
| `JWT_SECRET` | JWT密钥 | - | ✅ | `your_jwt_secret_key_at_least_256_bits` |
| `JWT_EXPIRATION` | JWT过期时间（毫秒） | `7200000` | ⚠️ | `7200000` |
| `JWT_REFRESH_EXPIRATION` | 刷新令牌过期时间（毫秒） | `604800000` | ⚠️ | `604800000` |

### 加密配置

| 变量名 | 说明 | 默认值 | 必需 | 示例 |
|--------|------|--------|------|------|
| `ENCRYPTION_AES_KEY` | AES加密密钥 | - | ✅ | `your_aes_key_32_bytes` |
| `ENCRYPTION_AES_IV` | AES初始化向量 | - | ✅ | `your_aes_iv_16_bytes` |

---

## 🔒 安全配置说明

### 生产环境要求

1. **所有密码必须使用环境变量**
   - ❌ 禁止在配置文件中硬编码密码
   - ✅ 使用 `${VARIABLE_NAME:}` 格式（无默认值）

2. **敏感配置加密**
   - 使用Nacos加密配置功能
   - 格式：`ENC(encrypted_value)`

3. **环境变量管理**
   - 使用Kubernetes Secrets
   - 使用Docker Secrets
   - 使用配置中心加密

---

## 📝 配置示例

### Docker Compose

```yaml
services:
  ioedream-common-service:
    environment:
      - DATABASE_URL=jdbc:mysql://db:3306/ioedream
      - DATABASE_USERNAME=ioedream_user
      - DATABASE_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=redis
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - NACOS_SERVER_ADDR=nacos:8848
      - JWT_SECRET=${JWT_SECRET}
```

### Kubernetes

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: ioedream-secrets
type: Opaque
stringData:
  DATABASE_PASSWORD: "your_password"
  REDIS_PASSWORD: "your_redis_password"
  JWT_SECRET: "your_jwt_secret"
  ENCRYPTION_AES_KEY: "your_aes_key"
```

---

## ✅ 验证清单

- [ ] 所有密码使用环境变量
- [ ] 配置文件无明文密码
- [ ] 生产环境使用加密配置
- [ ] 环境变量文档完整
- [ ] 密钥管理安全

---

**文档维护**: IOE-DREAM架构团队  
**更新日期**: 2025-01-30
