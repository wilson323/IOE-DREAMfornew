# IOE-DREAM 技术栈速查卡

**📋 版本**: v1.0.0
**📅 更新**: 2025-12-15
**🎯 目标**: 快速查阅IOE-DREAM项目技术栈标准

---

## 🔴 强制要求（违反将导致提交失败）

### 依赖注入规范
```java
// ✅ 强制使用
@Resource
private UserService userService;

// ❌ 严格禁止
@Autowired
private UserService userService;
```

### DAO层规范
```java
// ✅ 强制使用
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {
    // MyBatis-Plus方法
}

// ❌ 严格禁止
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // JPA方法
}
```

### 包名规范
```java
// ✅ 强制使用
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.persistence.Entity;
import jakarta.transaction.Transactional;

// ❌ 严格禁止
import javax.annotation.Resource;    // 禁止
import javax.validation.Valid;       // 禁止
import javax.persistence.Entity;     // 禁止
```

---

## 🟡 推荐实践（提高代码质量）

### 四层架构模式
```
Controller → Service → Manager → DAO
```

#### Controller层
- ✅ 使用@RestController
- ✅ 使用@Resource依赖注入
- ✅ 统一ResponseDTO响应格式
- ❌ 禁止直接调用Manager/DAO层

#### Service层
- ✅ 使用@Service注解
- ✅ 使用@Transactional事务管理
- ✅ 调用Manager层进行复杂编排
- ❌ 禁止跨过Manager层

#### Manager层
- ✅ 纯Java类，不使用Spring注解
- ✅ 构造函数注入依赖
- ✅ 复杂业务流程编排
- ❌ 禁止使用@Component/@Service

#### DAO层
- ✅ 使用@Mapper注解
- ✅ 继承BaseMapper<Entity>
- ✅ 使用Dao后缀命名
- ❌ 禁止包含业务逻辑

### 缓存策略
```java
// ✅ 推荐多级缓存
@Cacheable(value = "user", key = "#userId")
public UserEntity getUserById(Long userId) {
    return userDao.selectById(userId);
}

// L1: Caffeine本地缓存 (毫秒级)
// L2: Redis分布式缓存 (10ms级)
// L3: 网关调用缓存 (100ms级)
```

---

## 📊 版本要求

### 核心技术栈
| 技术 | 推荐版本 | 最低版本 | 说明 |
|------|----------|----------|------|
| **Spring Boot** | 3.5.8 | 3.5.8 | 微服务框架 |
| **Java** | 17 LTS | 17 | 运行环境 |
| **Jakarta EE** | 3.0+ | 3.0 | 包名规范 |
| **MyBatis-Plus** | 3.5.15 | 3.5.0 | ORM框架 |
| **Druid** | 1.2.20 | 1.2.0 | 数据库连接池 |
| **Redis** | 7.2.0 | 6.2.0 | 缓存数据库 |
| **Nacos** | 2.3.0 | 2.0.0 | 注册中心 |

### 构建工具
| 工具 | 推荐版本 | 用途 |
|------|----------|------|
| **Maven** | 3.9.0+ | 项目构建 |
| **PowerShell** | 7.3+ | 脚本执行 |
| **Docker** | 24.0+ | 容器化部署 |

---

## ⚙️ 配置标准

### application.yml 标准配置
```yaml
# ✅ 标准配置
spring:
  application:
    name: ${SERVICE_NAME:ioedream-xxx-service}

  datasource:
    type: com.alibaba.druid.pool.DruidDataSource  # 必须使用Druid
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: ${DB_URL:jdbc:mysql://localhost:3306/ioedream}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD}  # 必须加密配置

  redis:
    host: ${REDIS_HOST:127.0.0.1}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD}  # 必须加密配置
    database: 0  # 统一使用db=0

  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:nacos}
      config:
        server-addr: ${NACOS_SERVER_ADDR:127.0.0.1:8848}
        file-extension: yaml

# ❌ 禁止配置
# spring.datasource.type: com.zaxxer.hikari.HikariDataSource  # 禁止HikariCP
# spring.redis.database: 1  # 禁止使用非0数据库
```

### 依赖管理
```xml
<!-- ✅ 推荐依赖 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.8</version>
</parent>

<dependencies>
    <!-- 数据库相关 -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid-spring-boot-starter</artifactId>
        <version>1.2.20</version>
    </dependency>

    <!-- MyBatis-Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.5.15</version>
    </dependency>
</dependencies>
```

---

## 🚫 禁止事项清单

### ❌ 严格禁止
- **@Autowired**: 必须使用@Resource
- **@Repository**: 必须使用@Mapper
- **javax包名**: 必须使用jakarta
- **JPA/Hibernate**: 必须使用MyBatis-Plus
- **HikariCP**: 必须使用Druid
- **硬编码密码**: 必须使用加密配置
- **跨层访问**: 必须遵循四层架构

### ⚠️ 不推荐
- **构造函数注入**: 推荐字段注入@Resource
- **单一缓存**: 推荐多级缓存策略
- **同步调用**: 推荐异步处理耗时操作
- **硬编码配置**: 推荐使用配置中心

---

## 🔍 常见问题诊断

| 问题症状 | 可能原因 | 解决方案 |
|---------|----------|----------|
| 编译失败 | javax包名使用 | 检查import，替换为jakarta |
| 依赖注入失败 | @Autowired使用 | 查找@Autowired，替换为@Resource |
| JPA报错 | @Repository使用 | 查找@Repository，替换为@Mapper |
| 连接池报错 | HikariCP配置 | 检查配置文件，切换为Druid |
| 缓存问题 | 缓存策略不当 | 检查缓存配置，采用多级缓存 |

### 快速检查命令
```bash
# 检查@Autowired使用
grep -r "@Autowired" src/

# 检查@Repository使用
grep -r "@Repository" src/

# 检查javax包名使用
grep -r "javax\." src/

# 检查HikariCP使用
grep -r "HikariCP\|hikari" src/
```

---

## 📞 快速支持

### 技术栈问题
- **架构问题**: 联系架构委员会
- **技术栈规范**: 参考 CLAUDE.md
- **代码审查**: 提交PR进行审查
- **紧急问题**: 项目issue标记"P0"

### 有用链接
- **[完整技术栈报告](SKILLS_TECHNOLOGY_STANDARDIZATION_REPORT.md)**
- **[技能文档更新清单](SKILLS_DOCUMENTATION_UPDATE_CHECKLIST.md)**
- **[全局架构规范](CLAUDE.md)**

---

**💡 提示**: 将此速查卡保存为浏览器书签，随时查阅！

**🚀 让我们一起建设规范、高效的技术栈体系！**

---
**速查卡版本**: v1.0.0
**最后更新**: 2025-12-15
**维护团队**: IOE-DREAM架构委员会