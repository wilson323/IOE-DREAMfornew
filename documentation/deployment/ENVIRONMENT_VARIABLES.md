# IOE-DREAM 环境变量配置指南

**版本**: v1.0.0  
**更新日期**: 2025-12-03  
**适用范围**: 全部微服务

---

## 📋 必需环境变量清单

### 数据库配置

```bash
# MySQL数据库
DB_URL=jdbc:mysql://localhost:3306/ioedream_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
DB_USERNAME=root
DB_PASSWORD=your_secure_password_here  # 必须设置，无默认值
```

### Redis配置

```bash
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_PASSWORD=  # 可选，开发环境可为空
REDIS_DATABASE=0
```

### Nacos配置

```bash
NACOS_SERVER_ADDR=127.0.0.1:8848
NACOS_NAMESPACE=dev
NACOS_GROUP=IOE-DREAM
NACOS_USERNAME=nacos
NACOS_PASSWORD=  # 必须设置，已移除默认值
```

### JWT和认证配置

```bash
JWT_SECRET=your_jwt_secret_key_at_least_256_bits
JWT_ACCESS_EXPIRATION=86400
JWT_REFRESH_EXPIRATION=604800

SA_TOKEN_TIMEOUT=86400
SA_ACTIVE_TIMEOUT=1800
SA_JWT_SECRET=your_satoken_secret_key
```

### 服务端口配置

```bash
GATEWAY_PORT=8080
COMMON_SERVICE_PORT=8088
DEVICE_COMM_SERVICE_PORT=8087
OA_SERVICE_PORT=8089
ACCESS_SERVICE_PORT=8090
ATTENDANCE_SERVICE_PORT=8091
VIDEO_SERVICE_PORT=8092
CONSUME_SERVICE_PORT=8094
VISITOR_SERVICE_PORT=8095
```

### 分布式追踪配置

```bash
ZIPKIN_BASE_URL=http://localhost:9411
TRACING_SAMPLE_RATE=0.1
```

---

## ⚠️ 安全注意事项

1. **禁止明文存储**: 所有密码和密钥必须通过环境变量或配置中心管理
2. **生产环境**: 生产环境密码强度要求：至少16位，包含大小写字母、数字、特殊字符
3. **定期轮换**: 建议每3个月轮换一次敏感配置
4. **访问控制**: 限制配置文件和环境变量的访问权限

---

## 🔧 配置方法

### 方法1: 环境变量（开发环境）

```bash
# Windows PowerShell
$env:DB_PASSWORD="your_password"
$env:NACOS_PASSWORD="your_password"

# Linux/Mac
export DB_PASSWORD="your_password"
export NACOS_PASSWORD="your_password"
```

### 方法2: Nacos配置中心（生产环境）

1. 在Nacos配置中心创建加密配置
2. 使用AES-256加密密码
3. 配置文件中使用 `ENC(加密字符串)` 格式

---

**重要提示**: 修改配置后需要重启相应的微服务

