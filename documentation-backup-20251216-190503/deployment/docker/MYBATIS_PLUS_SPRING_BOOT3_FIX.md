# MyBatis-Plus Spring Boot 3.x 兼容性修复报告

> **修复日期**: 2025-12-08  
> **问题严重程度**: P0 (阻塞所有微服务启动)  
> **状态**: ✅ 已修复

---

## 📋 问题描述

### 错误信息

```
org.springframework.beans.factory.BeanDefinitionStoreException: 
Invalid bean definition with name 'userDao' defined in URL 
[jar:nested:/app/app.jar/!BOOT-INF/lib/microservices-common-1.0.0.jar!/net/lab1024/sa/common/auth/dao/UserDao.class]: 
Invalid value type for attribute 'factoryBeanObjectType': java.lang.String
```

### 影响范围

所有9个微服务启动失败：
- `ioedream-gateway-service`
- `ioedream-common-service`
- `ioedream-device-comm-service`
- `ioedream-oa-service`
- `ioedream-access-service`
- `ioedream-attendance-service`
- `ioedream-visitor-service`
- `ioedream-video-service`
- `ioedream-consume-service`

---

## 🔍 根本原因

### 直接原因

**MyBatis-Plus依赖使用了错误的starter**：
- ❌ 使用了 `mybatis-plus-boot-starter`（适用于Spring Boot 2.x）
- ✅ 应该使用 `mybatis-plus-spring-boot3-starter`（适用于Spring Boot 3.x）

### 技术背景

- **Spring Boot 3.x**: 引入了新的Bean定义机制，改变了`factoryBeanObjectType`的处理方式
- **MyBatis-Plus 3.5.15**: 提供了专门的Spring Boot 3.x starter
- **兼容性问题**: `mybatis-plus-boot-starter`在Spring Boot 3.x中会导致Bean定义错误

---

## ✅ 修复方案

### 修复内容

将所有微服务的MyBatis-Plus依赖从：
```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>${mybatis-plus.version}</version>
</dependency>
```

替换为：
```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>${mybatis-plus.version}</version>
</dependency>
```

### 修复的文件

| 模块 | 文件路径 | 状态 |
|------|---------|------|
| **microservices-common** | `microservices/microservices-common/pom.xml` | ✅ 已修复 |
| **gateway-service** | `microservices/ioedream-gateway-service/pom.xml` | ✅ 已修复 |
| **common-service** | `microservices/ioedream-common-service/pom.xml` | ✅ 已修复 |
| **device-comm-service** | `microservices/ioedream-device-comm-service/pom.xml` | ✅ 已修复 |
| **oa-service** | `microservices/ioedream-oa-service/pom.xml` | ✅ 已修复 |
| **access-service** | `microservices/ioedream-access-service/pom.xml` | ✅ 已修复 |
| **attendance-service** | `microservices/ioedream-attendance-service/pom.xml` | ✅ 已修复 |
| **consume-service** | `microservices/ioedream-consume-service/pom.xml` | ✅ 已修复 |
| **visitor-service** | `microservices/ioedream-visitor-service/pom.xml` | ✅ 已修复 |
| **video-service** | `microservices/ioedream-video-service/pom.xml` | ✅ 已修复 |

**修复完成率**: 100% ✅

---

## 🔧 技术细节

### MyBatis-Plus版本兼容性

| Spring Boot版本 | MyBatis-Plus Starter | 状态 |
|----------------|---------------------|------|
| Spring Boot 2.x | `mybatis-plus-boot-starter` | ✅ 正确 |
| Spring Boot 3.x | `mybatis-plus-spring-boot3-starter` | ✅ 正确 |
| Spring Boot 3.x | `mybatis-plus-boot-starter` | ❌ 不兼容 |

### 关键差异

`mybatis-plus-spring-boot3-starter` 针对Spring Boot 3.x进行了以下优化：
1. 适配Spring Boot 3.x的Bean定义机制
2. 修复`factoryBeanObjectType`类型处理
3. 兼容Jakarta EE包名（而非javax）
4. 支持Spring Boot 3.x的自动配置

---

## 📝 验证步骤

### 1. 重新构建项目

```powershell
# 清理并重新构建microservices-common
cd microservices/microservices-common
mvn clean install -DskipTests

# 重新构建所有微服务
cd ../..
mvn clean install -DskipTests
```

### 2. 重新构建Docker镜像

```powershell
# 重新构建所有Docker镜像
docker-compose -f docker-compose-all.yml build --no-cache
```

### 3. 启动服务并验证

```powershell
# 启动所有服务
docker-compose -f docker-compose-all.yml up -d

# 检查服务状态
docker-compose -f docker-compose-all.yml ps

# 查看日志确认无错误
docker logs ioedream-attendance-service --tail 50
```

### 预期结果

- ✅ 所有微服务成功启动
- ✅ 无`Invalid bean definition`错误
- ✅ `UserDao`等DAO接口正常注册为Spring Bean
- ✅ 数据库连接正常

---

## 🚨 重要提醒

### 必须重新构建

**⚠️ 此修复需要重新构建项目才能生效**：
1. 必须重新构建`microservices-common`（因为所有服务依赖它）
2. 必须重新构建所有微服务（更新依赖）
3. 必须重新构建Docker镜像（包含新的JAR包）

### 构建顺序

```
1. microservices-common ← 必须先构建
 ↓
2. 所有业务微服务（可并行构建）
```

---

## 📚 参考资料

- [MyBatis-Plus官方文档 - Spring Boot 3.x支持](https://baomidou.com/pages/97710a/)
- [Spring Boot 3.x迁移指南](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [GitHub Issue - factoryBeanObjectType错误](https://github.com/mybatis/spring/issues/881)

---

**修复完成时间**: 2025-12-08  
**修复人员**: IOE-DREAM架构团队  
**验证状态**: 待验证（需要重新构建项目）
