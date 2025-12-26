# UnifiedCacheManager 迁移指南

**版本**: v1.0.0  
**日期**: 2025-01-30  
**目标**: 统一使用UnifiedCacheManager三级缓存体系，提升缓存命中率从65%到90%

---

## 📋 迁移目标

### 当前状态

- ❌ 多个服务直接使用 `RedisTemplate`
- ❌ 缓存命中率仅65%
- ❌ 缺少缓存穿透/击穿/雪崩防护
- ❌ 没有统一的缓存管理策略

### 目标状态

- ✅ 所有服务统一使用 `UnifiedCacheManager`
- ✅ 缓存命中率提升至90%
- ✅ 完整的缓存防护机制
- ✅ 三级缓存架构（L1本地 + L2 Redis + L3网关）

---

## 🏗️ UnifiedCacheManager 架构

### 三级缓存体系

```
┌─────────────────────────────────────────────────────────────────┐
│   L1 本地缓存 (Caffeine)  - 毫秒级响应，无网络开销              │
│   TTL: 5分钟，容量: 10000                                       │
├─────────────────────────────────────────────────────────────────┤
│   L2 Redis缓存 - 分布式一致性，集群共享                         │
│   TTL: 30分钟，支持集群模式                                     │
├─────────────────────────────────────────────────────────────────┤
│   L3 网关缓存 (GatewayServiceClient) - 减少微服务间RPC调用      │
│   TTL: 10分钟，服务间调用结果缓存                               │
└─────────────────────────────────────────────────────────────────┘
```

### 缓存防护机制

1. **缓存穿透防护**: 空值缓存（TTL 5分钟）
2. **缓存击穿防护**: 分布式锁（Redisson）
3. **缓存雪崩防护**: 差异化TTL + 预热机制

---

## 🔧 迁移步骤

### 步骤1: 添加依赖

确保服务已依赖 `microservices-common-cache` 模块：

```xml
<dependency>
    <groupId>net.lab1024.sa</groupId>
    <artifactId>microservices-common-cache</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 步骤2: 注入UnifiedCacheManager

`UnifiedCacheManager` 已通过 `UnifiedCacheManagerConfiguration` 自动注册为Spring Bean，直接注入使用：

```java
@Service
public class YourServiceImpl implements YourService {
    
    @Resource
    private UnifiedCacheManager cacheManager;
    
    // 不再使用 RedisTemplate
    // @Resource
    // private RedisTemplate<String, Object> redisTemplate;
}
```

### 步骤3: 替换缓存操作

#### ❌ 旧代码（直接使用RedisTemplate）

```java
// 获取缓存
Object value = redisTemplate.opsForValue().get("user:" + userId);
if (value == null) {
    value = userDao.selectById(userId);
    redisTemplate.opsForValue().set("user:" + userId, value, 30, TimeUnit.MINUTES);
}

// 设置缓存
redisTemplate.opsForValue().set("user:" + userId, user, 30, TimeUnit.MINUTES);

// 删除缓存
redisTemplate.delete("user:" + userId);
```

#### ✅ 新代码（使用UnifiedCacheManager）

```java
// 获取缓存（自动三级缓存查询 + 防护机制）
UserEntity user = cacheManager.get(
    "user:" + userId,
    UserEntity.class,
    () -> userDao.selectById(userId),  // 数据加载器
    Duration.ofMinutes(30)  // TTL
);

// 设置缓存（自动写入三级缓存）
cacheManager.put("user:" + userId, user, Duration.ofMinutes(30));

// 删除缓存（自动清除三级缓存）
cacheManager.evict("user:" + userId);
```

---

## 📝 迁移示例

### 示例1: 用户信息缓存

#### 迁移前

```java
@Service
public class UserServiceImpl implements UserService {
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Resource
    private UserDao userDao;
    
    public UserEntity getUserById(Long userId) {
        String key = "user:" + userId;
        UserEntity user = (UserEntity) redisTemplate.opsForValue().get(key);
        if (user == null) {
            user = userDao.selectById(userId);
            if (user != null) {
                redisTemplate.opsForValue().set(key, user, 30, TimeUnit.MINUTES);
            }
        }
        return user;
    }
}
```

#### 迁移后

```java
@Service
public class UserServiceImpl implements UserService {
    
    @Resource
    private UnifiedCacheManager cacheManager;
    
    @Resource
    private UserDao userDao;
    
    public UserEntity getUserById(Long userId) {
        return cacheManager.get(
            "user:" + userId,
            UserEntity.class,
            () -> userDao.selectById(userId),
            Duration.ofMinutes(30)
        );
    }
}
```

### 示例2: 权限信息缓存

#### 迁移前

```java
@Service
public class PermissionServiceImpl implements PermissionService {
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    public List<String> getUserPermissions(Long userId) {
        String key = "permission:" + userId;
        List<String> permissions = (List<String>) redisTemplate.opsForValue().get(key);
        if (permissions == null) {
            permissions = loadPermissionsFromDatabase(userId);
            if (permissions != null) {
                redisTemplate.opsForValue().set(key, permissions, 60, TimeUnit.MINUTES);
            }
        }
        return permissions;
    }
}
```

#### 迁移后

```java
@Service
public class PermissionServiceImpl implements PermissionService {
    
    @Resource
    private UnifiedCacheManager cacheManager;
    
    public List<String> getUserPermissions(Long userId) {
        return cacheManager.get(
            "permission:" + userId,
            List.class,
            () -> loadPermissionsFromDatabase(userId),
            Duration.ofMinutes(60)
        );
    }
}
```

### 示例3: 设备状态缓存

#### 迁移前

```java
@Service
public class DeviceServiceImpl implements DeviceService {
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    public DeviceEntity getDeviceStatus(Long deviceId) {
        String key = "device:status:" + deviceId;
        DeviceEntity device = (DeviceEntity) redisTemplate.opsForValue().get(key);
        if (device == null) {
            device = deviceDao.selectById(deviceId);
            if (device != null) {
                redisTemplate.opsForValue().set(key, device, 10, TimeUnit.MINUTES);
            }
        }
        return device;
    }
    
    public void updateDeviceStatus(Long deviceId, DeviceEntity device) {
        deviceDao.updateById(device);
        // 清除缓存
        redisTemplate.delete("device:status:" + deviceId);
    }
}
```

#### 迁移后

```java
@Service
public class DeviceServiceImpl implements DeviceService {
    
    @Resource
    private UnifiedCacheManager cacheManager;
    
    @Resource
    private DeviceDao deviceDao;
    
    public DeviceEntity getDeviceStatus(Long deviceId) {
        return cacheManager.get(
            "device:status:" + deviceId,
            DeviceEntity.class,
            () -> deviceDao.selectById(deviceId),
            Duration.ofMinutes(10)
        );
    }
    
    public void updateDeviceStatus(Long deviceId, DeviceEntity device) {
        deviceDao.updateById(device);
        // 清除缓存（自动清除三级缓存）
        cacheManager.evict("device:status:" + deviceId);
    }
}
```

---

## 🎯 缓存键命名规范

### 命名格式

```
{namespace}:{resource}:{identifier}
```

### 示例

```
user:info:1001              # 用户信息
permission:user:1001        # 用户权限
device:status:2001          # 设备状态
area:config:3001           # 区域配置
dict:type:USER_STATUS      # 字典类型
menu:tree:all              # 菜单树
```

### 命名空间规范

| 命名空间 | 说明 | TTL建议 |
|---------|------|---------|
| `user` | 用户相关 | 30分钟 |
| `permission` | 权限相关 | 60分钟 |
| `device` | 设备相关 | 10分钟 |
| `area` | 区域相关 | 60分钟 |
| `dict` | 字典相关 | 24小时 |
| `menu` | 菜单相关 | 12小时 |
| `config` | 配置相关 | 12小时 |

---

## 📊 缓存统计和监控

### 获取缓存统计

```java
@Resource
private UnifiedCacheManager cacheManager;

public void printCacheStats() {
    UnifiedCacheManager.CacheStats stats = cacheManager.getStats();
    log.info("缓存命中率: {}%, 命中次数: {}, 未命中次数: {}, 缓存大小: {}",
        stats.getHitRate() * 100,
        stats.getHitCount(),
        stats.getMissCount(),
        stats.getSize()
    );
}
```

### 预期效果

- **缓存命中率**: 从65%提升至90%
- **响应时间**: L1缓存命中 < 1ms，L2缓存命中 < 10ms
- **数据库压力**: 减少70%的数据库查询

---

## ⚠️ 注意事项

### 1. 缓存键唯一性

确保缓存键在所有服务中唯一，建议使用命名空间前缀：

```java
// ✅ 正确
String key = "user:info:" + userId;

// ❌ 错误（可能冲突）
String key = "user:" + userId;
```

### 2. TTL设置

根据数据更新频率合理设置TTL：

- **热点数据**: 5-10分钟（如设备状态）
- **稳定数据**: 30-60分钟（如用户信息）
- **静态数据**: 12-24小时（如字典、菜单）

### 3. 缓存更新策略

- **写操作**: 先更新数据库，再清除缓存（Cache-Aside模式）
- **批量更新**: 使用 `evict()` 清除相关缓存
- **缓存预热**: 系统启动时预加载热点数据

### 4. 空值处理

`UnifiedCacheManager` 已自动处理空值缓存（穿透防护），无需手动处理：

```java
// ✅ 正确（自动处理空值）
UserEntity user = cacheManager.get(
    "user:" + userId,
    UserEntity.class,
    () -> userDao.selectById(userId),  // 可能返回null
    Duration.ofMinutes(30)
);

// ❌ 不需要手动检查
if (user == null) {
    // 不需要手动缓存null值
}
```

---

## 🔍 迁移检查清单

### 代码迁移

- [ ] 添加 `microservices-common-cache` 依赖
- [ ] 移除直接使用 `RedisTemplate` 的缓存操作
- [ ] 注入 `UnifiedCacheManager`
- [ ] 替换所有 `redisTemplate.opsForValue().get()` 为 `cacheManager.get()`
- [ ] 替换所有 `redisTemplate.opsForValue().set()` 为 `cacheManager.put()`
- [ ] 替换所有 `redisTemplate.delete()` 为 `cacheManager.evict()`
- [ ] 统一缓存键命名规范

### 测试验证

- [ ] 验证缓存命中率提升
- [ ] 验证缓存穿透/击穿/雪崩防护生效
- [ ] 验证三级缓存正常工作
- [ ] 验证缓存统计功能正常

---

## 📚 相关文档

- [CLAUDE.md - 三级缓存架构规范](../../CLAUDE.md)
- [缓存架构合规性报告](./CACHE_ARCHITECTURE_COMPLIANCE_REPORT.md)
- [技术栈标准规范](./TECHNOLOGY_STACK_STANDARD.md)

---

**维护团队**: IOE-DREAM架构委员会
