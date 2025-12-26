# IOE-DREAM Nacos加密配置手动修复指南

## 📋 概述

本文档提供详细的手动修复步骤，将项目中的64个明文密码替换为Nacos加密配置格式。

**重要原则**：

- ✅ 所有修复必须手动完成
- ✅ 禁止使用脚本自动修改代码
- ✅ 确保代码质量和全局一致性

---

## 🔍 第一步：扫描明文密码

### 1.1 运行扫描脚本

```powershell
# 扫描所有配置文件中的明文密码
.\scripts\security\scan-plaintext-passwords.ps1

# 生成详细报告
.\scripts\security\scan-plaintext-passwords.ps1 -Detailed

# 生成JSON格式报告
.\scripts\security\scan-plaintext-passwords.ps1 -Json
```

### 1.2 查看扫描报告

扫描完成后，会生成 `plaintext-passwords-report.txt` 文件，包含：

- 发现的所有明文密码位置
- 文件路径和行号
- 密码类型（数据库、Redis、Nacos等）

---

## 🔐 第二步：生成加密值

### 2.1 使用加密工具

对每个明文密码，使用加密工具生成加密值：

```powershell
# 加密MySQL密码
.\scripts\security\nacos-encrypt-password.ps1 -Password "123456"

# 加密Redis密码
.\scripts\security\nacos-encrypt-password.ps1 -Password "redis123"

# 加密Nacos密码
.\scripts\security\nacos-encrypt-password.ps1 -Password "nacos"

# 使用自定义密钥
.\scripts\security\nacos-encrypt-password.ps1 -Password "your_password" -SecretKey "your_secret_key"
```

### 2.2 记录加密值

将生成的加密值记录下来，格式为：

```
ENC(AES256:加密后的值)
```

**示例输出**：

```
加密后的值: ENC(AES256:v2xK8L9mN5pQr7sT3wJ6yZ1aF4bG8cH)
```

---

## ✏️ 第三步：手动修复配置文件

### 3.1 修复数据库密码

**修复位置**：所有微服务的 `application.yml` 和 `bootstrap.yml`

**修复前**：

```yaml
spring:
  datasource:
    password: "123456"  # ❌ 明文密码
```

**修复后**：

```yaml
spring:
  datasource:
    password: ${MYSQL_PASSWORD:ENC(AES256:v2xK8L9mN5pQr7sT3wJ6yZ1aF4bG8cH)}  # ✅ 加密配置
```

### 3.2 修复Redis密码

**修复前**：

```yaml
spring:
  data:
    redis:
      password: "redis123"  # ❌ 明文密码
```

**修复后**：

```yaml
spring:
  data:
    redis:
      password: ${REDIS_PASSWORD:ENC(AES256:d4F7jK8mN2pQr6sT3wJ5yZ1aF4bG9cH)}  # ✅ 加密配置
```

### 3.3 修复Nacos密码

**修复前**：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        password: "nacos"  # ❌ 明文密码
      config:
        password: "nacos"  # ❌ 明文密码
```

**修复后**：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        password: ${NACOS_PASSWORD:ENC(AES256:u7K8L9mN5pQr8sT3wJ6yZ2aF4bG8cH)}  # ✅ 加密配置
      config:
        password: ${NACOS_PASSWORD:ENC(AES256:u7K8L9mN5pQr8sT3wJ6yZ2aF4bG8cH)}  # ✅ 加密配置
```

### 3.4 修复RabbitMQ密码

**修复前**：

```yaml
spring:
  rabbitmq:
    password: "guest"  # ❌ 明文密码
```

**修复后**：

```yaml
spring:
  rabbitmq:
    password: ${RABBITMQ_PASSWORD:ENC(AES256:j8K9L0M1N2O3P4Q5R6S7T8U9V0W1X2Y)}  # ✅ 加密配置
```

### 3.5 修复JWT密钥

**修复前**：

```yaml
security:
  jwt:
    secret: "ioedream-jwt-secret-key-2025"  # ❌ 明文密钥
```

**修复后**：

```yaml
security:
  jwt:
    secret: ${JWT_SECRET:ENC(AES256:x9K8L9mN5pQr7sT3wJ6yZ1aF4bG8cH)}  # ✅ 加密配置
```

---

## 📝 第四步：需要修复的文件清单

### 4.1 微服务配置文件

需要修复的微服务配置文件：

1. **ioedream-gateway-service**
   - `src/main/resources/application.yml`
   - `src/main/resources/bootstrap.yml`

2. **ioedream-common-service**
   - `src/main/resources/application.yml`
   - `src/main/resources/bootstrap.yml`

3. **ioedream-device-comm-service**
   - `src/main/resources/application.yml`
   - `src/main/resources/bootstrap.yml`

4. **ioedream-oa-service**
   - `src/main/resources/application.yml`
   - `src/main/resources/bootstrap.yml`

5. **ioedream-access-service**
   - `src/main/resources/application.yml`

6. **ioedream-attendance-service**
   - `src/main/resources/application.yml`
   - `src/main/resources/bootstrap.yml`

7. **ioedream-video-service**
   - `src/main/resources/application.yml`
   - `src/main/resources/bootstrap.yml`

8. **ioedream-consume-service**
   - `src/main/resources/application.yml`
   - `src/main/resources/bootstrap.yml`

9. **ioedream-visitor-service**
   - `src/main/resources/application.yml`
   - `src/main/resources/bootstrap.yml`

10. **ioedream-biometric-service**
    - `src/main/resources/application.yml`

11. **ioedream-database-service**
    - `src/main/resources/application.yml`

### 4.2 公共配置文件

- `microservices/common-config/nacos/common-database.yaml`
- `microservices/common-config/nacos/common-security.yaml`
- `microservices/common-config/application-common-base.yml`

---

## ✅ 第五步：验证修复结果

### 5.1 运行验证脚本

```powershell
# 验证所有配置文件的加密状态
.\scripts\security\verify-encrypted-config.ps1

# 生成详细验证报告
.\scripts\security\verify-encrypted-config.ps1 -Detailed
```

### 5.2 检查验证报告

验证脚本会生成 `encryption-verification-report.txt`，包含：

- 加密覆盖率统计
- 每个文件的加密状态
- 剩余的明文密码位置

**目标**：加密覆盖率达到 100%

---

## 🔧 第六步：配置Nacos配置中心

### 6.1 在Nacos中创建加密配置

1. 登录Nacos控制台
2. 进入"配置管理" → "配置列表"
3. 创建新的配置项，Data ID格式：`{service-name}-encrypted.yaml`
4. 配置内容示例：

```yaml
# 数据库密码（已加密）
MYSQL_PASSWORD: ENC(AES256:v2xK8L9mN5pQr7sT3wJ6yZ1aF4bG8cH)

# Redis密码（已加密）
REDIS_PASSWORD: ENC(AES256:d4F7jK8mN2pQr6sT3wJ5yZ1aF4bG9cH)

# Nacos密码（已加密）
NACOS_PASSWORD: ENC(AES256:u7K8L9mN5pQr8sT3wJ6yZ2aF4bG8cH)
```

### 6.2 更新bootstrap.yml引用Nacos配置

```yaml
spring:
  cloud:
    nacos:
      config:
        enabled: true
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
        namespace: ${NACOS_NAMESPACE:dev}
        group: ${NACOS_GROUP:IOE-DREAM}
        file-extension: yaml
        # 引用加密配置
        extension-configs:
          - data-id: ${spring.application.name}-encrypted.yaml
            group: IOE-DREAM
            refresh: true
```

---

## 📋 修复检查清单

在完成修复后，请确认：

- [ ] 所有数据库密码已加密
- [ ] 所有Redis密码已加密
- [ ] 所有Nacos密码已加密
- [ ] 所有RabbitMQ密码已加密
- [ ] 所有JWT密钥已加密
- [ ] 所有API密钥已加密
- [ ] 验证脚本显示100%加密覆盖率
- [ ] 所有服务可以正常启动
- [ ] Nacos配置中心已配置加密值

---

## 🚨 注意事项

### 安全建议

1. **密钥管理**
   - 加密密钥必须安全存储
   - 不要将密钥提交到代码仓库
   - 使用环境变量或密钥管理服务

2. **环境隔离**
   - 开发、测试、生产环境使用不同的加密密钥
   - 不同环境的加密值不能混用

3. **备份恢复**
   - 修复前备份所有配置文件
   - 记录所有原始明文密码（安全存储）
   - 确保可以回滚到修复前状态

### 常见问题

**Q: 加密后服务无法启动？**
A: 检查Nacos配置中心是否已配置加密值，或使用环境变量提供加密值。

**Q: 如何验证加密值是否正确？**
A: 使用加密工具解密功能验证，或查看服务启动日志。

**Q: 可以混合使用加密和环境变量吗？**
A: 可以，使用 `${ENV_VAR:ENC(...)}` 格式，优先使用环境变量，环境变量不存在时使用加密默认值。

---

## 📞 支持

如有问题，请参考：

- [Nacos加密配置文档](documentation/security/encryption/ENCRYPTED_CONFIGURATION_GUIDE.md)
- [配置安全规范](.claude/skills/configuration-security-specialist.md)

---

**最后更新**: 2025-01-30  
**版本**: v1.0.0
