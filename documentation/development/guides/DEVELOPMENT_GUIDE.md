# 开发指南

**版本**: v1.0.0  
**日期**: 2025-01-30  
**状态**: 进行中

---

## 📋 目录

1. [开发环境搭建](#开发环境搭建)
2. [代码规范](#代码规范)
3. [常见问题解答](#常见问题解答)
4. [性能优化指南](#性能优化指南)
5. [缓存使用指南](#缓存使用指南)
6. [数据库优化指南](#数据库优化指南)

---

## 🔧 开发环境搭建

### 1. 环境要求

- **JDK**: OpenJDK 17+
- **Maven**: 3.8+
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Nacos**: 2.0+
- **Node.js**: 18+ (前端开发)

### 2. 项目结构

```
IOE-DREAM/
├── microservices/          # 微服务模块
│   ├── microservices-common/    # 公共模块
│   ├── ioedream-common-service/ # 公共业务服务
│   ├── ioedream-consume-service/# 消费服务
│   └── ...
├── smart-admin-web-javascript/  # 前端项目
├── smart-app/              # 移动端项目
└── documentation/          # 文档
```

### 3. 本地开发配置

#### 3.1 数据库配置

在Nacos配置中心配置数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ioedream?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    type: com.alibaba.druid.pool.DruidDataSource
```

#### 3.2 Redis配置

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: 
      database: 0
```

---

## 📝 代码规范

### 1. 命名规范

- **类名**: 大驼峰命名（PascalCase）
- **方法名**: 小驼峰命名（camelCase）
- **常量**: 全大写下划线分隔（UPPER_SNAKE_CASE）
- **包名**: 全小写，点分隔（net.lab1024.sa）

### 2. 注释规范

```java
/**
 * 类/方法说明
 * <p>
 * 详细描述
 * </p>
 *
 * @param param1 参数说明
 * @return 返回值说明
 * @throws Exception 异常说明
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
```

### 3. 架构规范

严格遵循CLAUDE.md规范：
- **四层架构**: Controller → Service → Manager → DAO
- **依赖注入**: 使用 `@Resource` 注解
- **DAO层**: 使用 `@Mapper` 注解
- **Manager层**: 在microservices-common中是纯Java类
- **禁止兼容层**: 不保留旧包名兼容类、重复接口、同包名重复类
- **ResponseDTO归属**: 必须定义在 `microservices-common-core`
- **接口唯一来源**: 共享接口只允许在对应公共模块中存在一份

---

## ❓ 常见问题解答

### Q1: 如何添加新的业务模块？

1. 在 `microservices` 目录下创建新的服务模块
2. 参考现有服务模块的结构
3. 在父POM中注册新模块
4. 在Nacos中配置服务信息

### Q2: 如何使用缓存？

```java
@Resource
private UnifiedCacheManager cacheManager;

// 获取缓存
CacheResult<String> result = cacheManager.get(
    CacheNamespace.USER, "user:1001", String.class);

// 设置缓存
cacheManager.set(CacheNamespace.USER, "user:1001", userInfo);

// 获取并刷新缓存
String value = cacheManager.getWithRefresh(
    "key", () -> loadFromDatabase(), 3600L);
```

### Q3: 如何处理分页查询？

**传统分页**（不推荐深度分页）:
```java
PageParam pageParam = PageParam.of(pageNum, pageSize);
List<Entity> list = dao.selectByPage(offset, limit);
```

**游标分页**（推荐）:
```java
CursorPageParam cursorParam = CursorPageParam.of(
    pageSize, lastCreateTime, lastId);
List<Entity> list = dao.selectByCursor(cursorParam);
CursorPageResult<Entity> result = CursorPageResult.of(
    list, pageSize, nextCreateTime, nextId);
```

---

## 🚀 性能优化指南

### 1. 缓存优化

- **热点数据**: 使用L1本地缓存（Caffeine）
- **普通数据**: 使用L2 Redis缓存
- **缓存过期**: 根据数据更新频率设置过期时间

### 2. 数据库优化

- **索引优化**: 为常用查询字段添加索引
- **分页优化**: 使用游标分页替代深度分页
- **慢查询监控**: 定期检查慢查询日志

### 3. 连接池优化

- **连接数配置**: 根据并发量调整 `max-active`
- **连接监控**: 使用Druid监控页面查看连接池状态

---

## 💾 缓存使用指南

### 1. 缓存命名空间

```java
// 使用预定义的命名空间
CacheNamespace.USER      // 用户缓存（30分钟）
CacheNamespace.CONSUME   // 消费缓存（10分钟）
CacheNamespace.DICT      // 字典缓存（1小时）
```

### 2. 缓存操作

```java
// 获取缓存
CacheResult<UserVO> result = cacheManager.get(
    CacheNamespace.USER, "user:" + userId, UserVO.class);

if (result.isSuccess()) {
    UserVO user = result.getData();
    // 使用缓存数据
} else {
    // 缓存未命中，从数据库加载
    UserVO user = loadFromDatabase();
    // 写入缓存
    cacheManager.set(CacheNamespace.USER, "user:" + userId, user);
}
```

### 3. 缓存击穿防护

```java
// 自动处理缓存击穿（使用分布式锁）
String value = cacheManager.getWithRefresh(
    "key", 
    () -> loadFromDatabase(),  // 数据加载器
    3600L                       // 过期时间（秒）
);
```

---

## 🗄️ 数据库优化指南

### 1. 索引优化

- **组合索引**: 为多条件查询创建组合索引
- **覆盖索引**: 包含查询所需的所有字段
- **索引维护**: 定期分析索引使用情况

### 2. 查询优化

- **避免SELECT ***: 只查询需要的字段
- **使用LIMIT**: 限制返回结果数量
- **使用EXPLAIN**: 分析查询计划

### 3. 分页优化

- **游标分页**: 使用 `CursorPageParam` 替代传统分页
- **避免深度分页**: offset > 1000时使用游标分页

---

**更多详细信息请参考各模块的README文档**

