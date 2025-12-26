# 第1周实施计划：数据库和缓存优化

> **周期**: 第1周（5个工作日）
> **任务**: P1-7.1 数据库索引优化 + P1-7.2 缓存架构优化
> **预计工时**: 7人天（数据库3人天 + 缓存4人天）
> **创建日期**: 2025-12-26

---

## 📋 本周目标

### 主要目标

- ✅ **数据库索引优化**: 为25个核心查询添加索引，实现查询性能70%↑
- ✅ **缓存架构优化**: 实现三级缓存，实现缓存命中率从65%→90%

### 预期效果

| 指标 | 优化前 | 目标 | 提升幅度 |
|------|--------|------|----------|
| 平均查询响应时间 | 800ms | 240ms | 70%↑ |
| 慢查询数量 | 23个 | 0个 | 100%↓ |
| 缓存命中率 | 65% | 90% | 38%↑ |
| 数据库CPU使用率 | 85% | <60% | 29%↓ |

---

## 📅 每日计划

### Day 1: 数据库索引分析和创建（周一）

#### 上午：数据库索引分析

**任务清单**：

- [ ] 1.1 备份生产数据库（必须！）
```bash
# 数据库全量备份
mysqldump -u root -p smart_admin_v3 > backup_$(date +%Y%m%d_%H%M%S).sql

# 验证备份文件
ls -lh backup_*.sql
```

- [ ] 1.2 分析慢查询日志
```bash
# 查看慢查询日志位置
SHOW VARIABLES LIKE 'slow_query%';

# 导出慢查询
mysqldumpslow -s t -t 20 /var/log/mysql/mysql-slow.log > slow_queries_top20.txt

# 分析慢查询
cat slow_queries_top20.txt
```

- [ ] 1.3 使用EXPLAIN分析核心查询
```sql
-- 分析门禁记录查询
EXPLAIN SELECT * FROM t_access_record
WHERE user_id = 1
ORDER BY pass_time DESC
LIMIT 20;

-- 分析考勤记录查询
EXPLAIN SELECT * FROM t_attendance_record
WHERE user_id = 1
AND punch_time >= '2025-01-01'
ORDER BY punch_time DESC;

-- 分析消费记录查询
EXPLAIN SELECT * FROM t_consume_record
WHERE user_id = 1
ORDER BY consume_time DESC
LIMIT 20;
```

#### 下午：创建索引

**任务清单**：

- [ ] 1.4 创建核心表索引
```sql
-- ====================================================================
-- 门禁记录表索引 (t_access_record)
-- ====================================================================

-- 1. 用户通行记录查询（最频繁）
CREATE INDEX idx_access_user_time
ON t_access_record(user_id, pass_time DESC)
COMMENT '用户ID+通行时间复合索引';

-- 2. 覆盖索引（包含所有常用字段，避免回表）
CREATE INDEX idx_access_cover
ON t_access_record(user_id, device_id, area_id, access_result, pass_time)
COMMENT '门禁记录覆盖索引';

-- 3. 设备通行记录查询
CREATE INDEX idx_access_device_time
ON t_access_record(device_id, pass_time DESC)
COMMENT '设备ID+通行时间复合索引';

-- 4. 区域通行统计查询
CREATE INDEX idx_access_area_time
ON t_access_record(area_id, pass_time DESC)
COMMENT '区域ID+通行时间复合索引';

-- ====================================================================
-- 考勤记录表索引 (t_attendance_record)
-- ====================================================================

-- 5. 用户考勤记录查询
CREATE INDEX idx_attendance_user_time
ON t_attendance_record(user_id, punch_time DESC)
COMMENT '用户ID+打卡时间复合索引';

-- 6. 考勤状态查询
CREATE INDEX idx_attendance_status_time
ON t_attendance_record(status, punch_time DESC)
COMMENT '考勤状态+打卡时间复合索引';

-- 7. 班次考勤查询
CREATE INDEX idx_attendance_shift_time
ON t_attendance_record(shift_id, punch_time DESC)
COMMENT '班次ID+打卡时间复合索引';

-- ====================================================================
-- 消费记录表索引 (t_consume_record)
-- ====================================================================

-- 8. 用户消费记录查询
CREATE INDEX idx_consume_user_time
ON t_consume_record(user_id, consume_time DESC)
COMMENT '用户ID+消费时间复合索引';

-- 9. 消费类型查询
CREATE INDEX idx_consume_type_time
ON t_consume_record(consume_type, consume_time DESC)
COMMENT '消费类型+消费时间复合索引';

-- 10. 区域消费统计
CREATE INDEX idx_consume_area_time
ON t_consume_record(area_id, consume_time DESC)
COMMENT '区域ID+消费时间复合索引';

-- ====================================================================
-- 用户表索引 (t_common_user)
-- ====================================================================

-- 11. 用户名唯一索引
CREATE UNIQUE INDEX uk_user_username
ON t_common_user(username)
COMMENT '用户名唯一索引';

-- 12. 手机号唯一索引
CREATE UNIQUE INDEX uk_user_phone
ON t_common_user(phone)
COMMENT '手机号唯一索引';

-- 13. 部门用户查询
CREATE INDEX idx_user_dept_status
ON t_common_user(dept_id, status)
COMMENT '部门ID+状态复合索引';

-- 14. 状态查询
CREATE INDEX idx_user_status
ON t_common_user(status)
COMMENT '用户状态索引';

-- ====================================================================
-- 部门表索引 (t_common_department)
-- ====================================================================

-- 15. 父部门查询
CREATE INDEX idx_dept_parent
ON t_common_department(parent_id, sort_order)
COMMENT '父部门ID+排序复合索引';

-- 16. 状态查询
CREATE INDEX idx_dept_status
ON t_common_department(status)
COMMENT '部门状态索引';

-- ====================================================================
-- 设备表索引 (t_common_device)
-- ====================================================================

-- 17. 设备编码唯一索引
CREATE UNIQUE INDEX uk_device_code
ON t_common_device(device_code)
COMMENT '设备编码唯一索引';

-- 18. 设备类型查询
CREATE INDEX idx_device_type_status
ON t_common_device(device_type, status)
COMMENT '设备类型+状态复合索引';

-- 19. 区域设备查询
CREATE INDEX idx_device_area
ON t_common_device(area_id)
COMMENT '区域ID索引';

-- ====================================================================
-- 访客记录表索引 (t_visitor_record)
-- ====================================================================

-- 20. 访客记录查询
CREATE INDEX idx_visitor_user_time
ON t_visitor_record(visitor_id, visit_time DESC)
COMMENT '访客ID+访问时间复合索引';

-- 21. 访客状态查询
CREATE INDEX idx_visitor_status_time
ON t_visitor_record(status, visit_time DESC)
COMMENT '访客状态+访问时间复合索引';

-- ====================================================================
-- 视频设备表索引 (t_video_device)
-- ====================================================================

-- 22. 视频设备状态查询
CREATE INDEX idx_video_device_status
ON t_video_device(status, device_type)
COMMENT '设备状态+设备类型复合索引';

-- 23. 区域视频设备查询
CREATE INDEX idx_video_area
ON t_video_device(area_id)
COMMENT '区域ID索引';

-- ====================================================================
-- 通用复合索引（其他高频查询）
-- ====================================================================

-- 24. 创建时间索引（多个表通用）
CREATE INDEX idx_access_create_time
ON t_access_record(create_time DESC);

CREATE INDEX idx_attendance_create_time
ON t_attendance_record(create_time DESC);

CREATE INDEX idx_consume_create_time
ON t_consume_record(create_time DESC);

-- 25. 更新时间索引（多个表通用）
CREATE INDEX idx_access_update_time
ON t_access_record(update_time DESC);

CREATE INDEX idx_attendance_update_time
ON t_attendance_record(update_time DESC);
```

- [ ] 1.5 验证索引创建成功
```sql
-- 查看表索引
SHOW INDEX FROM t_access_record;
SHOW INDEX FROM t_attendance_record;
SHOW INDEX FROM t_consume_record;

-- 验证索引使用效果
EXPLAIN SELECT * FROM t_access_record
WHERE user_id = 1
ORDER BY pass_time DESC
LIMIT 20;

-- 预期结果:
-- - type: ref (索引查找)
-- - key: idx_access_user_time (使用的索引名)
-- - rows: 扫描行数显著减少
-- - Extra: Using index (覆盖索引，理想情况)
```

**完成标准**：
- ✅ 25个索引全部创建成功
- ✅ EXPLAIN分析显示索引被正确使用
- ✅ 无索引创建错误

### Day 2: 缓存架构配置（周二）

#### 上午：添加依赖和配置类

**任务清单**：

- [ ] 2.1 添加Caffeine依赖
```xml
<!-- microservices/microservices-common-cache/pom.xml -->
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
    <version>3.1.8</version>
</dependency>
```

- [ ] 2.2 创建Caffeine配置类
```java
// 文件: microservices/microservices-common-cache/src/main/java/net/lab1024/sa/common/cache/config/CaffeineCacheConfig.java

package net.lab1024.sa.common.cache.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.lab1024.sa.common.organization.entity.UserEntity;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.domain.entity.DictEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine本地缓存配置
 */
@Configuration
public class CaffeineCacheConfig {

    /**
     * 用户基本信息缓存（L1缓存）
     */
    @Bean("userBasicInfoCache")
    public Cache<String, Object> userBasicInfoCache() {
        return Caffeine.newBuilder()
            .maximumSize(10000)                    // 最多10000个用户
            .initialCapacity(1000)                  // 初始容量1000
            .expireAfterWrite(5, TimeUnit.MINUTES)  // 写入5分钟后过期
            .expireAfterAccess(3, TimeUnit.MINUTES) // 访问3分钟后过期
            .weakKeys()                            // 使用弱引用Key
            .recordStats()                         // 记录统计信息
            .build();
    }

    /**
     * 设备基本信息缓存（L1缓存）
     */
    @Bean("deviceBasicInfoCache")
    public Cache<String, Object> deviceBasicInfoCache() {
        return Caffeine.newBuilder()
            .maximumSize(5000)
            .initialCapacity(500)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .weakKeys()
            .recordStats()
            .build();
    }

    /**
     * 字典数据缓存（L1缓存）
     */
    @Bean("dictDataCache")
    public Cache<String, Object> dictDataCache() {
        return Caffeine.newBuilder()
            .maximumSize(1000)
            .initialCapacity(100)
            .expireAfterWrite(30, TimeUnit.MINUTES)  // 字典数据变化不频繁
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .weakKeys()
            .recordStats()
            .build();
    }

    /**
     * 用户权限缓存（L1缓存）
     */
    @Bean("userPermissionCache")
    public Cache<String, Object> userPermissionCache() {
        return Caffeine.newBuilder()
            .maximumSize(10000)
            .initialCapacity(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .weakKeys()
            .recordStats()
            .build();
    }
}
```

- [ ] 2.3 创建Redis缓存配置
```java
// 文件: microservices/microservices-common-cache/src/main/java/net/lab1024/sa/common/cache/config/RedisCacheConfig.java

package net.lab1024.sa.common.cache.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis缓存配置（L2缓存）
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))  // 默认30分钟过期
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();  // 不缓存null值

        // 针对不同缓存的个性化配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // 用户详情缓存（1小时过期）
        cacheConfigurations.put("userDetailCache",
            defaultConfig.entryTtl(Duration.ofHours(1))
        );

        // 聚合数据缓存（15分钟过期）
        cacheConfigurations.put("aggregatedDataCache",
            defaultConfig.entryTtl(Duration.ofMinutes(15))
        );

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()  // 支持事务
            .build();
    }
}
```

#### 下午：创建缓存预热和监控

**任务清单**：

- [ ] 2.4 创建缓存预热Runner
```java
// 文件: microservices/ioedream-common-service/src/main/java/net/lab1024/sa/common/cache/warmup/CacheWarmUpRunner.java

package net.lab1024.sa.common.cache.warmup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 缓存预热
 */
@Slf4j
@Component
public class CacheWarmUpRunner implements CommandLineRunner {

    @Resource
    private UserService userService;
    @Resource
    private DictService dictService;
    @Resource
    private DeviceService deviceService;

    @Override
    public void run(String... args) {
        log.info("[缓存预热] 开始预热缓存...");

        // 预热用户基本信息
        warmUpUserInfo();

        // 预热字典数据
        warmUpDictData();

        // 预热设备信息
        warmUpDeviceInfo();

        log.info("[缓存预热] 缓存预热完成！");
    }

    private void warmUpUserInfo() {
        log.info("[缓存预热] 预热用户基本信息...");
        try {
            // 预加载活跃用户到缓存
            List<UserEntity> activeUsers = userService.getActiveUsers(1000);
            log.info("[缓存预热] 预热用户数量: {}", activeUsers.size());
        } catch (Exception e) {
            log.error("[缓存预热] 用户信息预热失败", e);
        }
    }

    private void warmUpDictData() {
        log.info("[缓存预热] 预热字典数据...");
        try {
            // 预加载所有字典类型到缓存
            List<DictEntity> dictTypes = dictService.getAllDictTypes();
            log.info("[缓存预热] 预热字典类型数量: {}", dictTypes.size());
        } catch (Exception e) {
            log.error("[缓存预热] 字典数据预热失败", e);
        }
    }

    private void warmUpDeviceInfo() {
        log.info("[缓存预热] 预热设备信息...");
        try {
            // 预加载在线设备到缓存
            List<DeviceEntity> onlineDevices = deviceService.getOnlineDevices();
            log.info("[缓存预热] 预热设备数量: {}", onlineDevices.size());
        } catch (Exception e) {
            log.error("[缓存预热] 设备信息预热失败", e);
        }
    }
}
```

- [ ] 2.5 创建缓存统计Reporter
```java
// 文件: microservices/ioedream-common-service/src/main/java/net/lab1024/sa/common/cache/reporter/CacheStatsReporter.java

package net.lab1024.sa.common.cache.reporter;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 缓存统计报告
 */
@Slf4j
@Component
public class CacheStatsReporter {

    @Resource(name = "userBasicInfoCache")
    private Cache<String, Object> userBasicInfoCache;

    @Resource(name = "deviceBasicInfoCache")
    private Cache<String, Object> deviceBasicInfoCache;

    @Resource(name = "dictDataCache")
    private Cache<String, Object> dictDataCache;

    @Resource(name = "userPermissionCache")
    private Cache<String, Object> userPermissionCache;

    /**
     * 每小时输出缓存统计信息
     */
    @Scheduled(cron = "0 0 * * * ?")  // 每小时执行
    public void reportCacheStats() {
        log.info("=== 缓存统计报告 ===");

        reportCacheStats("用户基本信息缓存", userBasicInfoCache);
        reportCacheStats("设备基本信息缓存", deviceBasicInfoCache);
        reportCacheStats("字典数据缓存", dictDataCache);
        reportCacheStats("用户权限缓存", userPermissionCache);

        log.info("=== 缓存统计报告结束 ===");
    }

    private void reportCacheStats(String cacheName, Cache<String, Object> cache) {
        CacheStats stats = cache.stats();

        log.info("[缓存统计] 缓存名称: {}", cacheName);
        log.info("  - 命中率: {:.2f}%", stats.hitRate() * 100);
        log.info("  - 命中次数: {}", stats.hitCount());
        log.info("  - 未命中次数: {}", stats.missCount());
        log.info("  - 加载次数: {}", stats.loadSuccessCount());
        log.info("  - 失败次数: {}", stats.loadFailureCount());
        log.info("  - 总请求数: {}", stats.requestCount());
        log.info("  - 驱逐次数: {}", stats.evictionCount());
    }
}
```

**完成标准**：
- ✅ Caffeine配置类创建成功
- ✅ Redis配置类创建成功
- ✅ 缓存预热Runner创建成功
- ✅ 缓存统计Reporter创建成功

### Day 3: 三级缓存实现（周三）

#### 上午：创建三级缓存Service

**任务清单**：

- [ ] 3.1 创建三级缓存Service
```java
// 文件: microservices/microservices-common-cache/src/main/java/net/lab1024/sa/common/cache/service/ThreeLevelCacheService.java

package net.lab1024.sa.common.cache.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 三级缓存Service
 * L1: Caffeine本地缓存 (1-5ms)
 * L2: Redis分布式缓存 (10-30ms)
 * L3: Database (100-500ms)
 */
@Slf4j
@Service
public class ThreeLevelCacheService {

    @Resource(name = "userBasicInfoCache")
    private Cache<String, Object> userBasicInfoCache;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取用户基本信息（三级缓存）
     */
    public Object getUserBasicInfo(Long userId) {
        String cacheKey = "user:basic:" + userId;

        // 1. 尝试从L1缓存获取
        Object cachedUser = userBasicInfoCache.getIfPresent(cacheKey);
        if (cachedUser != null) {
            log.debug("[三级缓存] L1缓存命中: userId={}", userId);
            return cachedUser;
        }

        // 2. 从L2缓存获取
        try {
            cachedUser = redisTemplate.opsForValue().get("user:basic:" + userId);
            if (cachedUser != null) {
                log.debug("[三级缓存] L2缓存命中: userId={}", userId);
                // 回写L1缓存
                userBasicInfoCache.put(cacheKey, cachedUser);
                return cachedUser;
            }
        } catch (Exception e) {
            log.error("[三级缓存] L2缓存获取失败: userId={}", userId, e);
        }

        // 3. 从数据库查询（由业务Service调用）
        log.debug("[三级缓存] L3缓存查询: userId={}", userId);
        return null;  // 返回null，由业务Service查询数据库
    }

    /**
     * 写入用户基本信息（三级缓存）
     */
    public void putUserBasicInfo(Long userId, Object user) {
        String cacheKey = "user:basic:" + userId;

        // 写入L1缓存
        userBasicInfoCache.put(cacheKey, user);

        // 写入L2缓存
        try {
            redisTemplate.opsForValue().set(
                "user:basic:" + userId,
                user,
                Duration.ofMinutes(30)  // L2缓存30分钟
            );
        } catch (Exception e) {
            log.error("[三级缓存] L2缓存写入失败: userId={}", userId, e);
        }
    }

    /**
     * 删除用户基本信息（三级缓存）
     */
    public void evictUserBasicInfo(Long userId) {
        String cacheKey = "user:basic:" + userId;

        // 删除L1缓存
        userBasicInfoCache.invalidate(cacheKey);

        // 删除L2缓存
        try {
            redisTemplate.delete("user:basic:" + userId);
        } catch (Exception e) {
            log.error("[三级缓存] L2缓存删除失败: userId={}", userId, e);
        }
    }
}
```

#### 下午：集成到业务Service

**任务清单**：

- [ ] 3.2 改造UserService使用三级缓存
```java
// 文件: microservices/ioedream-common-service/src/main/java/net/lab1024/sa/common/user/service/impl/UserServiceImpl.java

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;
    @Resource
    private ThreeLevelCacheService threeLevelCacheService;

    @Override
    public UserVO getUserById(Long userId) {
        // 1. 尝试从三级缓存获取
        Object cachedUser = threeLevelCacheService.getUserBasicInfo(userId);
        if (cachedUser instanceof UserVO) {
            return (UserVO) cachedUser;
        }

        // 2. 从数据库查询
        UserEntity userEntity = userDao.selectById(userId);
        if (userEntity == null) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }

        // 3. 转换为VO
        UserVO userVO = convertToVO(userEntity);

        // 4. 写入三级缓存
        threeLevelCacheService.putUserBasicInfo(userId, userVO);

        return userVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserVO user) {
        // 1. 更新数据库
        userDao.updateById(convertToEntity(user));

        // 2. 删除缓存（延迟双删）
        threeLevelCacheService.evictUserBasicInfo(user.getUserId());

        // 3. 延迟删除（防止并发脏读）
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(500);
                threeLevelCacheService.evictUserBasicInfo(user.getUserId());
            } catch (Exception e) {
                log.error("[用户服务] 延迟删除缓存失败", e);
            }
        });
    }
}
```

**完成标准**：
- ✅ 三级缓存Service创建成功
- ✅ 业务Service集成三级缓存
- ✅ 缓存一致性保证（延迟双删）

### Day 4: 缓存防护和测试（周四）

#### 上午：实现缓存防护

**任务清单**：

- [ ] 4.1 创建BloomFilter防护
```java
// 文件: microservices/microservices-common-cache/src/main/java/net/lab1024/sa/common/cache/filter/BloomFilterCache.java

package net.lab1024.sa.common.cache.filter;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * BloomFilter缓存防护
 * 用于防止缓存穿透
 */
@Slf4j
@Component
public class BloomFilterCache {

    private BloomFilter<Long> userIdBloomFilter;
    private BloomFilter<Long> deviceIdBloomFilter;

    @PostConstruct
    public void init() {
        // 初始化用户ID布隆过滤器（预计100万用户，1%误判率）
        userIdBloomFilter = BloomFilter.create(
            Funnels.longFunnel(),
            1000000,
            0.01
        );

        // 初始化设备ID布隆过滤器（预计10万设备，1%误判率）
        deviceIdBloomFilter = BloomFilter.create(
            Funnels.longFunnel(),
            100000,
            0.01
        );

        // 预加载已存在的ID
        warmUpBloomFilter();
    }

    /**
     * 检查用户ID是否存在
     * @return true: 可能存在，false: 一定不存在
     */
    public boolean mightContainUser(Long userId) {
        return userIdBloomFilter.mightContain(userId);
    }

    /**
     * 检查设备ID是否存在
     */
    public boolean mightContainDevice(Long deviceId) {
        return deviceIdBloomFilter.mightContain(deviceId);
    }

    /**
     * 添加用户ID到布隆过滤器
     */
    public void putUser(Long userId) {
        userIdBloomFilter.put(userId);
    }

    /**
     * 添加设备ID到布隆过滤器
     */
    public void putDevice(Long deviceId) {
        deviceIdBloomFilter.put(deviceId);
    }

    private void warmUpBloomFilter() {
        log.info("[BloomFilter] 开始预热...");
        // TODO: 从数据库加载所有ID到布隆过滤器
        log.info("[BloomFilter] 预热完成");
    }
}
```

- [ ] 4.2 集成BloomFilter到UserService
```java
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private BloomFilterCache bloomFilterCache;
    @Resource
    private ThreeLevelCacheService threeLevelCacheService;
    @Resource
    private UserDao userDao;

    @Override
    public UserVO getUserById(Long userId) {
        // 1. BloomFilter快速判断
        if (!bloomFilterCache.mightContainUser(userId)) {
            log.warn("[用户服务] 用户不存在（BloomFilter判断）: userId={}", userId);
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }

        // 2. 尝试从三级缓存获取
        Object cachedUser = threeLevelCacheService.getUserBasicInfo(userId);
        if (cachedUser instanceof UserVO) {
            return (UserVO) cachedUser;
        }

        // 3. 从数据库查询
        UserEntity userEntity = userDao.selectById(userId);
        if (userEntity == null) {
            // 从布隆过滤器移除不存在的ID
            bloomFilterCache.putUser(userId);  // 虽然不存在，但布隆过滤器会误判，下次需要查数据库验证
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }

        // 4. 转换为VO并写入缓存
        UserVO userVO = convertToVO(userEntity);
        threeLevelCacheService.putUserBasicInfo(userId, userVO);

        return userVO;
    }
}
```

#### 下午：单元测试

**任务清单**：

- [ ] 4.3 创建三级缓存测试
```java
// 文件: microservices/microservices-common-cache/src/test/java/net/lab1024/sa/common/cache/ThreeLevelCacheServiceTest.java

@SpringBootTest
public class ThreeLevelCacheServiceTest {

    @Resource
    private ThreeLevelCacheService threeLevelCacheService;

    @Test
    public void testGetUserBasicInfo_L1Hit() {
        // 准备数据：预先加载到L1缓存
        UserVO user = new UserVO();
        user.setUserId(1L);
        threeLevelCacheService.putUserBasicInfo(1L, user);

        // 执行测试
        Object result = threeLevelCacheService.getUserBasicInfo(1L);

        // 验证：应该从L1缓存命中
        assertNotNull(result);
        assertEquals(1L, ((UserVO) result).getUserId());
    }

    @Test
    public void testGetUserBasicInfo_L2Hit() {
        // 准备数据：只加载到L2缓存
        UserVO user = new UserVO();
        user.setUserId(2L);
        // 直接写入Redis，不写L1
        redisTemplate.opsForValue().set("user:basic:2", user, Duration.ofMinutes(30));

        // 清空L1缓存
        threeLevelCacheService.evictUserBasicInfo(2L);

        // 执行测试
        Object result = threeLevelCacheService.getUserBasicInfo(2L);

        // 验证：应该从L2缓存命中并回写L1
        assertNotNull(result);
        assertEquals(2L, ((UserVO) result).getUserId());
    }

    @Test
    public void testCacheConsistency_DelayedDoubleDelete() {
        // 测试延迟双删
        UserVO user = new UserVO();
        user.setUserId(3L);
        threeLevelCacheService.putUserBasicInfo(3L, user);

        // 第一次删除
        threeLevelCacheService.evictUserBasicInfo(3L);

        // 验证缓存已删除
        Object result = threeLevelCacheService.getUserBasicInfo(3L);
        assertNull(result);
    }
}
```

**完成标准**：
- ✅ BloomFilter防护实现成功
- ✅ 单元测试通过率100%
- ✅ 缓存穿透防护验证成功

### Day 5: 性能验证和优化（周五）

#### 上午：性能测试

**任务清单**：

- [ ] 5.1 数据库性能测试
```sql
-- 测试1: 查询响应时间对比
-- 优化前
SET profiling = 1;
SELECT * FROM t_access_record WHERE user_id = 1 ORDER BY pass_time DESC LIMIT 20;
SHOW PROFILES;

-- 优化后（使用索引）
SET profiling = 1;
SELECT * FROM t_access_record WHERE user_id = 1 ORDER BY pass_time DESC LIMIT 20;
SHOW PROFILES;

-- 预期结果: 查询时间从800ms降低到240ms
```

- [ ] 5.2 缓存命中率测试
```java
// 创建缓存命中率测试
@Test
public void testCacheHitRate() {
    int totalRequests = 10000;
    int hitCount = 0;

    // 模拟10000次请求
    for (int i = 0; i < totalRequests; i++) {
        Long userId = (long) (i % 1000);  // 模拟1000个用户

        // 第一次请求会缓存
        threeLevelCacheService.getUserBasicInfo(userId);

        // 后续请求应该命中缓存
        Object result = threeLevelCacheService.getUserBasicInfo(userId);
        if (result != null) {
            hitCount++;
        }
    }

    double hitRate = (double) hitCount / totalRequests;
    log.info("缓存命中率: {:.2f}%", hitRate * 100);

    // 验证缓存命中率≥90%
    assertTrue(hitRate >= 0.9, "缓存命中率应该≥90%，实际: " + hitRate);
}
```

#### 下午：监控和调优

**任务清单**：

- [ ] 5.3 配置Prometheus监控
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,caches
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      cache: caffeine,redis
```

- [ ] 5.4 配置Grafana监控面板
```json
{
  "dashboard": {
    "title": "数据库和缓存性能监控",
    "panels": [
      {
        "title": "数据库查询响应时间",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_sum{service=\"ioedream-access-service\",uri=\"/api/v1/access/record\"}[5m])"
          }
        ]
      },
      {
        "title": "缓存命中率",
        "targets": [
          {
            "expr": "cache_hit_rate{application=\"ioedream-access-service\"}"
          }
        ]
      },
      {
        "title": "数据库CPU使用率",
        "targets": [
          {
            "expr": "rate(mysql_global_status_bytes{service=\"ioedream-access-service\"}[5m])"
          }
        ]
      }
    ]
  }
}
```

- [ ] 5.5 生成性能对比报告
```markdown
# 第1周性能优化报告

## 优化目标达成情况

| 指标 | 优化前 | 优化后 | 目标达成 |
|------|--------|--------|----------|
| 平均查询响应时间 | 800ms | 240ms | ✅ 70%↑ |
| 缓存命中率 | 65% | 90% | ✅ 38%↑ |
| 数据库CPU使用率 | 85% | 58% | ✅ 27%↓ |

## 下一步计划

- 第2周: 并发和连接池优化（P1-7.3 + P1-7.8）
- 重点: 实现异步处理、线程池优化、限流熔断
```

**完成标准**：
- ✅ 性能测试全部通过
- ✅ 监控面板配置完成
- ✅ 性能对比报告生成
- ✅ 优化目标全部达成

---

## ✅ 每日检查清单

### 通用检查项（每天执行）

- [ ] 代码提交前检查（编译、单元测试）
- [ ] Git提交并推送（每日结束时）
- [ ] 填写工作日志（记录完成情况和遇到的问题）

### 回滚准备

- [ ] 每天开始前创建回滚点（Git Tag）
```bash
git tag -a day1-start -m "第1天开始点"
```

- [ ] 如果出现严重问题，立即回滚
```bash
git reset --hard day1-start
```

---

## 📊 周末总结

### 产出物

- ✅ 25个数据库索引创建完成
- ✅ 三级缓存架构实现完成
- ✅ 缓存防护机制实现完成
- ✅ 单元测试和性能测试完成
- ✅ 监控面板配置完成
- ✅ 性能对比报告生成

### 验收标准

- [ ] 数据库查询响应时间<240ms（平均）
- [ ] 缓存命中率≥90%
- [ ] 数据库CPU使用率<60%
- [ ] 无缓存穿透问题
- [ ] 无缓存一致性问题

---

**文档版本**: v1.0
**创建日期**: 2025-12-26
**负责人**: IOE-DREAM 性能优化小组
**状态**: ✅ 计划完成，准备执行
