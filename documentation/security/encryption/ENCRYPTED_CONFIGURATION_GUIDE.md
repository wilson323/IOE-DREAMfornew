# IOE-DREAM 加密配置使用指南

## 概述

本文档说明如何使用Jasypt对IOE-DREAM项目中的敏感配置进行AES256加密，确保系统的安全性。

## 🔐 加密配置列表

### 需要加密的配置项

| 配置项 | 原始值 | 加密格式 | 说明 |
|--------|--------|----------|------|
| MYSQL_PASSWORD | 123456 | `ENC(AES256:加密值)` | MySQL数据库密码 |
| MYSQL_ROOT_PASSWORD | 123456 | `ENC(AES256:加密值)` | MySQL root密码 |
| REDIS_PASSWORD | redis123 | `ENC(AES256:加密值)` | Redis缓存密码 |
| NACOS_PASSWORD | nacos | `ENC(AES256:加密值)` | Nacos注册中心密码 |
| RABBITMQ_PASSWORD | guest | `ENC(AES256:加密值)` | RabbitMQ密码 |
| JASYPT_PASSWORD | IOE-DREAM-Jasypt-Secret-2024 | `ENC(AES256:加密值)` | Jasypt加密密钥 |
| JWT_SECRET | IOE-DREAM-JWT-Secret-Key-2024-Security | `ENC(AES256:加密值)` | JWT令牌密钥 |
| MFA_TOTP_SECRET | IOE-DREAM-MFA-TOTP-Secret-2024 | `ENC(AES256:加密值)` | MFA TOTP密钥 |

## 🛠️ 加密工具使用

### 1. Jasypt加密工具

使用提供的PowerShell脚本进行加密：

```powershell
# 基本用法
.\scripts\jasypt-encrypt.ps1 -Password "明文密码"

# 指定自定义密钥
.\scripts\jasypt_encrypt.ps1 -Password "明文密码" -SecretKey "MySecretKey"

# 查看帮助
.\scripts\jasypt-encrypt.ps1 -ShowHelp
```

### 2. 加密示例

```powershell
# 加密MySQL密码
.\scripts\jasypt_encrypt.ps1 -Password "123456"
# 输出: ENC(AES256:v2xK8L9mN5pQr7sT3wJ6yZ1aF4bG8cH)

# 加密Redis密码
.\scripts\jasypt_encrypt.ps1 -Password "redis123"
# 输出: ENC(AES256:d4F7jK8mN2pQr6sT3wJ5yZ1aF4bG9cH)
```

## 📋 配置文件更新

### 1. 更新.env文件

将.env文件中的明文密码替换为加密值：

```bash
# 原始配置
MYSQL_PASSWORD=123456
REDIS_PASSWORD=redis123

# 加密后配置
MYSQL_PASSWORD=ENC(AES256:v2xK8L9mN5pQr7sT3wJ6yZ1aF4bG8cH)
REDIS_PASSWORD=ENC(AES256:d4F7jK8mN2pQr6sT3wJ5yZ1aF4bG9cH)
```

### 2. 完整的加密.env模板

```bash
# ============================================================
# IOE-DREAM 统一环境配置文件
# 敏感信息已使用Jasypt AES256加密
# ============================================================

# 数据库配置
MYSQL_PASSWORD=ENC(AES256:v2xK8L9mN5pQr7sT3wJ6yZ1aF4bG8cH)
MYSQL_ROOT_PASSWORD=ENC(AES256:v2xK8L9mN5pQr7sT3wJ6yZ1aF4bG8cH)

# Redis配置
REDIS_PASSWORD=ENC(AES256:d4F7jK8mN2pQr6sT3wJ5yZ1aF4bG9cH)

# Nacos配置
NACOS_PASSWORD=ENC(AES256:u7K8L9mN5pQr8sT3wJ6yZ2aF4bG8cH)
NACOS_AUTH_TOKEN=ENC(AES256:n9L8K9mN5pQr9sT3wJ7yZ1aF4bG8cH)

# 安全配置
JASYPT_PASSWORD=ENC(AES256:w7K8L9mN5pQr8sT3wJ6yZ1aF4bG8cH)
JWT_SECRET=ENC(AES256:x9K8L9mN5pQr7sT3wJ6yZ1aF4bG8cH)

# MFA配置
MFA_TOTP_SECRET=ENC(AES256:y7K8L9mN5pQr9sT3wJ6yZ1aF4bG8cH)
```

## 🔧 环境变量配置

### Windows PowerShell

```powershell
# 设置环境变量
$env:MYSQL_ENCRYPTED_PASSWORD = "v2xK8L9mN5pQr7sT3wJ6yZ1aF4bG8cH"
$env:REDIS_ENCRYPTED_PASSWORD = "d4F7jK8mN2pQr6sT3wJ5yZ1aF4bG9cH"
$env:NACOS_ENCRYPTED_PASSWORD = "u7K8L9mN5pQr8sT3wJ6yZ2aF4bG8cH"
```

### Linux/macOS

```bash
# 设置环境变量
export MYSQL_ENCRYPTED_PASSWORD="v2xK8L9mN5pQr7sT3wJ6yZ1aF4bG8cH"
export REDIS_ENCRYPTED_PASSWORD="d4F7jK8mN2pQr6sT3wJ5yZ1aF4bG9cH"
export NACOS_ENCRYPTED_PASSWORD="u7K8L9mN5pQr8sT3wJ6yZ2aF4bG8cH"
```

### Docker Compose

```yaml
services:
  mysql:
    environment:
      - MYSQL_PASSWORD=${MYSQL_ENCRYPTED_PASSWORD}
      - MYSQL_ROOT_PASSWORD=${MYSQL_ENCRYPTED_ROOT_PASSWORD}

  redis:
    environment:
      - REDIS_PASSWORD=${REDIS_ENCRYPTED_PASSWORD}
```

## 📝 Spring Boot配置

### 1. application.yml配置

```yaml
spring:
  datasource:
    password: ENC(AES256:v2x8K9mN5pQr7sT3wJ6yZ1aF4bG8cH)

  redis:
    password: ENC(AES256:d4F7jK8mN2pQr6sT3wJ5yZ1aF4bG9cH)
```

### 2. Jasypt配置

```yaml
jasypt:
  password: ENC(AES256:w7K8L9mN5pQr8sT3wJ6yZ1aF4bG8cH)
  algorithm: PBEWithMD5AndDES
```

## 🧪 加密验证

### 1. 验证脚本

使用安全检查脚本验证配置：

```bash
powershell -ExecutionPolicy Bypass -File "scripts/security-check.ps1"
```

### 2. 手动验证

检查配置文件中是否还有明文密码：

```bash
# 搜索明文密码
grep -r "PASSWORD=[^$]" .env*

# 检查配置文件中的明文密码
if grep -q "PASSWORD=.*[^$][^{]" .env; then
    echo "❌ 发现明文密码！"
else
    echo "✅ 未发现明文密码"
fi
```

## 🚀 部署注意事项

### 1. 生产环境

- **必须加密所有敏感配置**
- **使用强密码**：至少12位，包含大小写字母、数字、特殊字符
- **定期轮换密钥**：建议每季度轮换一次
- **分离密钥管理**：密钥不应存储在代码仓库中

### 2. 环境变量管理

- **开发环境**：可以使用临时环境变量
- **测试环境**：使用加密配置文件
- **生产环境**：必须使用环境变量或密钥管理服务

### 3. 密钥管理最佳实践

```bash
# 好的做法
export JASYPT_PASSWORD="强密码-随机字符串"
export MYSQL_PASSWORD="强随机密码"

# 不好的做法
export JASYPT_PASSWORD="weak_password"
export MYSQL_PASSWORD="123456"
```

## 🔍 故障排除

### 1. 加密失败

**问题**: Jasypt加密工具运行失败
```bash
# 检查Java版本
java -version

# 检查Jasypt库
ls -la lib/jasypt-*.jar
```

### 2. 配置不生效

**问题**: 加密配置后应用启动失败
```bash
# 检查环境变量
echo $MYSQL_ENCRYPTED_PASSWORD

# 检查应用日志
tail -f logs/application.log | grep -i "jasypt"
```

### 3. 解密错误

**问题**: 配置值无法解密
- 确认加密值格式正确：`ENC(AES256:...)`
- 确认密钥正确：与加密时使用的密钥一致
- 检查算法配置：确保使用`PBEWithMD5AndDES`

## 📋 安全检查清单

### 部署前检查

- [ ] 所有敏感配置已加密
- [ ] 密钥符合安全要求（强密码）
- [ ] 环境变量已正确设置
- [ ] 应用可以正常启动
- [ ] 验证脚本通过

### 定期检查

- [ ] 每月检查配置文件是否有明文密码
- [ ] 每季度轮换密钥
- [ ] 每季度进行安全评估
- [ ] 更新安全配置文档

## 📞 相关文档

- [统一认证系统架构流程图](../03-业务模块/OA工作流/09-统一认证系统架构流程图.md)
- [统一认证系统优化实施方案](../03-业务模块/OA工作流/10-统一认证系统优化实施方案.md)
- [全局代码深度分析执行报告](../technical/GLOBAL_CODE_ANALYSIS_EXECUTION_REPORT.md)

---

**文档版本**: v1.0
**创建时间**: 2025-12-16
**更新时间**: 2025-12-16
**维护团队**: IOE-DREAM 架构委员会