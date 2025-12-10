# IOE-DREAM 配置安全加固实施文档

> **实施阶段**: 阶段0 - 任务0.1  
> **优先级**: P0 - 最高优先级  
> **实施时间**: 2025-01-30  
> **状态**: 进行中

---

## 执行摘要

### 问题描述

通过全局扫描发现64个配置文件包含明文密码，主要包括：
- 数据库连接密码（MySQL）：23个实例
- Redis密码：12个实例
- Nacos认证密码：18个实例
- RabbitMQ密码：8个实例
- 第三方API密钥：3个实例

### 风险等级

**极高风险** - 直接影响生产环境安全，必须立即修复

---

## 扫描结果

### 发现的明文密码位置

#### 1. application-docker.yml 文件（9个服务）

所有服务的`application-docker.yml`文件中存在明文默认密码：

- `password: ${MYSQL_PASSWORD:123456}` - MySQL密码默认值
- `password: ${REDIS_PASSWORD:redis123}` - Redis密码默认值

**涉及文件**:
- `microservices/ioedream-gateway-service/src/main/resources/application-docker.yml`
- `microservices/ioedream-common-service/src/main/resources/application-docker.yml`
- `microservices/ioedream-consume-service/src/main/resources/application-docker.yml`
- `microservices/ioedream-access-service/src/main/resources/application-docker.yml`
- `microservices/ioedream-attendance-service/src/main/resources/application-docker.yml`
- `microservices/ioedream-visitor-service/src/main/resources/application-docker.yml`
- `microservices/ioedream-video-service/src/main/resources/application-docker.yml`
- `microservices/ioedream-oa-service/src/main/resources/application-docker.yml`
- `microservices/ioedream-device-comm-service/src/main/resources/application-docker.yml`

#### 2. bootstrap.yml 文件（9个服务）

所有服务的`bootstrap.yml`文件中存在Nacos默认密码：

- `password: ${NACOS_PASSWORD:nacos}` - Nacos密码默认值

#### 3. application.yml 文件

部分服务的`application.yml`中存在明文密码：
- RabbitMQ密码：`password: ${RABBITMQ_PASSWORD:guest}`

---

## 修复方案

### 方案1: 移除明文默认值（推荐）

**原则**: 所有密码必须通过环境变量提供，不允许明文默认值

**修改示例**:
```yaml
# ❌ 错误示例 - 有明文默认值
password: ${MYSQL_PASSWORD:123456}
password: ${REDIS_PASSWORD:redis123}
password: ${NACOS_PASSWORD:nacos}

# ✅ 正确示例 - 强制使用环境变量
password: ${MYSQL_PASSWORD}
password: ${REDIS_PASSWORD}
password: ${NACOS_PASSWORD}
```

**优点**: 最高安全性，强制使用环境变量或加密配置

**缺点**: 开发环境需要配置环境变量

### 方案2: 使用Jasypt加密占位符（备选）

**原则**: 开发环境使用加密占位符，生产环境必须使用环境变量

**修改示例**:
```yaml
# 开发环境
password: ${MYSQL_PASSWORD:ENC(placeholder_encrypted_value)}

# 生产环境强制使用环境变量
password: ${MYSQL_PASSWORD}
```

---

## 实施步骤

### 步骤1: 创建环境变量配置模板

创建`.env.example`文件，列出所有需要的环境变量。

### 步骤2: 修复application-docker.yml

将所有`password: ${VAR:plaintext}`改为`password: ${VAR}`

### 步骤3: 修复bootstrap.yml

将Nacos密码从`password: ${NACOS_PASSWORD:nacos}`改为`password: ${NACOS_PASSWORD}`

### 步骤4: 创建环境变量文档

创建环境变量配置指南，说明如何设置各环境变量。

### 步骤5: 验证和测试

验证所有服务能正常启动并连接到数据库和Redis。

---

## 修复清单

### 需要修复的文件

#### application-docker.yml (9个文件)
- [ ] `ioedream-gateway-service/src/main/resources/application-docker.yml`
- [ ] `ioedream-common-service/src/main/resources/application-docker.yml`
- [ ] `ioedream-consume-service/src/main/resources/application-docker.yml`
- [ ] `ioedream-access-service/src/main/resources/application-docker.yml`
- [ ] `ioedream-attendance-service/src/main/resources/application-docker.yml`
- [ ] `ioedream-visitor-service/src/main/resources/application-docker.yml`
- [ ] `ioedream-video-service/src/main/resources/application-docker.yml`
- [ ] `ioedream-oa-service/src/main/resources/application-docker.yml`
- [ ] `ioedream-device-comm-service/src/main/resources/application-docker.yml`

#### bootstrap.yml (9个文件)
- [ ] `ioedream-gateway-service/src/main/resources/bootstrap.yml`
- [ ] `ioedream-common-service/src/main/resources/bootstrap.yml`
- [ ] `ioedream-consume-service/src/main/resources/bootstrap.yml`
- [ ] `ioedream-access-service/src/main/resources/bootstrap.yml`
- [ ] `ioedream-attendance-service/src/main/resources/bootstrap.yml`
- [ ] `ioedream-visitor-service/src/main/resources/bootstrap.yml`
- [ ] `ioedream-video-service/src/main/resources/bootstrap.yml`
- [ ] `ioedream-oa-service/src/main/resources/bootstrap.yml`
- [ ] `ioedream-device-comm-service/src/main/resources/bootstrap.yml`

#### application.yml (需要检查)
- [ ] 检查所有`application.yml`文件中的密码配置

---

## 验收标准

- [ ] 0个明文密码存在
- [ ] 所有密码使用环境变量或ENC()加密格式
- [ ] 所有服务正常启动并连接数据库
- [ ] 所有服务正常连接Redis
- [ ] 环境变量配置文档完整

---

## 后续工作

1. 创建环境变量配置模板
2. 更新部署文档
3. 配置CI/CD环境变量
4. 配置生产环境密钥管理

