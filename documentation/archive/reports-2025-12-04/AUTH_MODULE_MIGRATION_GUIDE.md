# Auth模块迁移指南

> **📋 迁移日期**: 2025-12-02  
> **📋 源服务**: ioedream-auth-service  
> **📋 目标服务**: ioedream-common-service/auth模块  
> **📋 迁移状态**: 🚀 执行中

---

## 🎯 迁移目标

将auth-service的所有功能完整迁移到common-service的auth模块，确保：
- ✅ 功能完整性
- ✅ 代码规范性（遵循CLAUDE.md）
- ✅ 技术栈统一（JPA→MyBatis-Plus）
- ✅ 配置统一

---

## 📋 迁移文件清单

### Controller层（1个文件）

| 源文件 | 目标位置 | 状态 |
|-------|---------|------|
| auth/controller/AuthController.java | common/auth/controller/AuthController.java | ⏳ 待迁移 |

### Service层（4个文件）

| 源文件 | 目标位置 | 状态 |
|-------|---------|------|
| auth/service/AuthService.java | common/auth/service/AuthService.java | ✅ 已创建 |
| auth/service/impl/AuthServiceImpl.java | common/auth/service/impl/AuthServiceImpl.java | ⏳ 待迁移 |
| auth/service/LoginService.java | common/auth/service/LoginService.java | ⏳ 待迁移 |
| auth/service/impl/LoginServiceImpl.java | common/auth/service/impl/LoginServiceImpl.java | ⏳ 待迁移 |
| auth/service/UserService.java | common/auth/service/UserService.java | ⏳ 待迁移 |

### Manager层（需要创建）

| 文件 | 目标位置 | 状态 |
|------|---------|------|
| AuthManager.java | common/auth/manager/AuthManager.java | ⏳ 待创建 |
| SessionManager.java | common/auth/manager/SessionManager.java | ⏳ 待创建 |

### DAO层（需要创建，转换JPA）

| 文件 | 目标位置 | 状态 | 说明 |
|------|---------|------|------|
| UserSessionDao.java | common/auth/dao/UserSessionDao.java | ⏳ 待创建 | JPA→MyBatis-Plus |

### Domain层（10+个文件）

| 源文件 | 目标位置 | 状态 |
|-------|---------|------|
| domain/entity/UserSessionEntity.java | common/auth/domain/entity/UserSessionEntity.java | ⏳ |
| domain/request/LoginRequest.java | common/auth/domain/dto/LoginRequestDTO.java | ⏳ |
| domain/request/RefreshTokenRequest.java | common/auth/domain/dto/RefreshTokenRequestDTO.java | ⏳ |
| domain/response/LoginResponse.java | common/auth/domain/vo/LoginResponseVO.java | ⏳ |
| domain/response/UserInfoResponse.java | common/auth/domain/vo/UserInfoVO.java | ⏳ |
| domain/vo/* | common/auth/domain/vo/ | ⏳ |

### Util层（1个文件）

| 源文件 | 目标位置 | 状态 |
|-------|---------|------|
| util/JwtTokenUtil.java | common/auth/util/JwtTokenUtil.java | ⏳ 待迁移 |

---

## 🔧 技术栈转换

### 转换1: JPA → MyBatis-Plus

**auth-service使用JPA（违规）**:
```xml
<!-- 违规依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

**转换为MyBatis-Plus**:
```xml
<!-- 符合规范 -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
</dependency>
```

**代码转换示例**:
```java
// ❌ 原代码（JPA）
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}

// ✅ 新代码（MyBatis-Plus）
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {
    @Select("SELECT * FROM t_user WHERE username = #{username} AND deleted_flag = 0")
    @Transactional(readOnly = true)
    UserEntity selectByUsername(@Param("username") String username);
}
```

### 转换2: @Repository → @Mapper

**批量替换**:
```java
// 所有DAO文件
@Repository  → @Mapper
```

### 转换3: @Autowired → @Resource

**批量替换**:
```java
// 所有依赖注入
@Autowired  → @Resource
```

---

## 📦 包名更新规则

### 统一包名规范

**原包名**:
```
net.lab1024.sa.auth.*
```

**新包名**:
```
net.lab1024.sa.common.auth.*
```

### 具体映射

| 原包名 | 新包名 |
|-------|-------|
| net.lab1024.sa.auth.controller | net.lab1024.sa.common.auth.controller |
| net.lab1024.sa.auth.service | net.lab1024.sa.common.auth.service |
| net.lab1024.sa.auth.service.impl | net.lab1024.sa.common.auth.service.impl |
| net.lab1024.sa.auth.manager | net.lab1024.sa.common.auth.manager |
| net.lab1024.sa.auth.dao | net.lab1024.sa.common.auth.dao |
| net.lab1024.sa.auth.domain.entity | net.lab1024.sa.common.auth.domain.entity |
| net.lab1024.sa.auth.domain.dto | net.lab1024.sa.common.auth.domain.dto |
| net.lab1024.sa.auth.domain.vo | net.lab1024.sa.common.auth.domain.vo |
| net.lab1024.sa.auth.util | net.lab1024.sa.common.auth.util |

---

## 🔄 配置整合

### auth-service配置整合到common-service

**原配置** (auth-service/application.yml):
```yaml
server:
  port: 8081

spring:
  application:
    name: ioedream-auth-service
```

**整合后** (common-service/bootstrap.yml):
```yaml
server:
  port: 8088

spring:
  application:
    name: ioedream-common-service

# Auth模块配置
auth:
  jwt:
    secret: ${JWT_SECRET:ioedream-jwt-secret-key-2025}
    access-token-expiration: 86400
    refresh-token-expiration: 604800
  session:
    max-sessions: 3
    timeout: 3600
  login:
    max-retry: 5
    lock-duration: 1800
```

---

## ✅ 迁移验证清单

### 代码验证
- [ ] 所有类文件已迁移
- [ ] 包名已更新
- [ ] Import语句已更新
- [ ] 依赖注入已更新（@Resource）
- [ ] DAO层已转换（@Mapper）

### 功能验证
- [ ] 用户登录功能正常
- [ ] 令牌刷新功能正常
- [ ] 用户登出功能正常
- [ ] 权限验证功能正常
- [ ] 会话管理功能正常

### 技术栈验证
- [ ] 无JPA依赖
- [ ] 使用MyBatis-Plus
- [ ] 使用Druid连接池
- [ ] 使用@Mapper注解
- [ ] 使用@Resource注入

### 配置验证
- [ ] 配置已整合到common-service
- [ ] 端口配置正确（8088）
- [ ] Nacos注册正常
- [ ] Redis连接正常
- [ ] 数据库连接正常

---

## 📊 迁移进度跟踪

### 当前进度

| 模块 | 文件总数 | 已迁移 | 待迁移 | 完成度 |
|------|---------|-------|-------|--------|
| Controller | 1 | 0 | 1 | 0% |
| Service | 4 | 1 | 3 | 25% |
| Manager | 2 | 0 | 2 | 0% |
| DAO | 1 | 0 | 1 | 0% |
| Domain | 10 | 0 | 10 | 0% |
| Util | 1 | 0 | 1 | 0% |
| **总计** | **19** | **1** | **18** | **5%** |

---

## 🚀 下一步行动

### 立即执行

1. **迁移AuthServiceImpl.java**
   - 更新包名
   - 更新import
   - 转换依赖注入

2. **迁移JwtTokenUtil.java**
   - 工具类，无依赖
   - 直接迁移

3. **迁移Domain类**
   - 重命名：Request→DTO，Response→VO
   - 更新包名

4. **创建Manager层**
   - AuthManager：复杂业务逻辑
   - SessionManager：会话管理

5. **创建DAO层**
   - 转换JPA为MyBatis-Plus
   - 使用@Mapper注解

---

**👥 迁移执行**: IOE-DREAM 开发团队  
**📅 开始日期**: 2025-12-02  
**⏰ 预计完成**: 2025-12-02（今日完成auth模块）  
**✅ 执行状态**: 进行中

