# IOE-DREAM 扩展表命名规范与性能优化指南

**基于现有项目实践的增强和完善**

**创建时间**: 2025-11-25
**版本**: v1.0.0
**基于项目**: IOE-DREAM智慧园区一卡通管理平台现有成功实践

---

## 📋 概述

本文档基于IOE-DREAM项目中已验证的命名规范和性能优化最佳实践，为扩展表机制提供标准化的指导原则。所有规范都基于现有成功案例的总结和完善，避免从零创建，确保与项目现有架构完全兼容。

## 🏷️ 命名规范

### 1. 数据库表命名规范（基于现有表结构分析）

#### 1.1 基础表命名模式
```sql
-- 基于现有成功实践的标准命名
t_{business_domain}              -- 基础业务表
t_{base_table}_{module}_ext      -- 扩展表模式
t_{module}_{specific}            -- 模块特定表
```

**现有成功案例**:
```sql
-- 基础表
t_area                          -- 区域基础表 ✅
t_device                        -- 设备基础表 ✅
t_account                       -- 账户基础表 ✅
t_biometric_record             -- 生物记录基础表 ✅

-- 扩展表
t_area_access_ext              -- 区域门禁扩展 ✅
t_area_attendance_ext          -- 区域考勤扩展 ✅
t_biometric_attendance_extension -- 生物考勤扩展 ✅

-- 继承表
t_access_device                -- 门禁设备表 ✅
t_consume_device               -- 消费设备表 ✅
t_attendance_schedule          -- 考勤排班表 ✅
```

#### 1.2 字段命名规范（基于现有字段分析）

**主键字段**:
```sql
-- 统一的主键命名模式
{table_name}_id                -- 例如：area_id, device_id, ext_id
```

**关联字段**:
```sql
-- 与关联表主键保持一致的命名
{referenced_table}_id         -- 例如：area_id, device_id, person_id
```

**业务字段**:
```sql
-- 基于现有成功实践的业务字段命名
{business_feature}_{type}      -- 例如：access_level, consume_limit
{config_field}                 -- 例如：time_config, alert_config
{status_field}                 -- 例如：device_status, account_status
```

**JSON配置字段**:
```sql
-- 基于现有JSON配置的成功模式
{feature}_config               -- 例如：access_config, time_config
{feature}_settings            -- 例如：biometric_settings
{feature}_rules               -- 例如：validation_rules
{feature}_restrictions        -- 例如：time_restrictions, location_restrictions
```

#### 1.3 索引命名规范（基于现有索引分析）

**现有成功索引模式**:
```sql
-- 基础索引
idx_{table}_{column}          -- 例如：idx_area_status

-- 复合索引
idx_{table}_{column1}_{column2} -- 例如：idx_area_parent_level

-- 业务索引
idx_{business}_{purpose}      -- 例如：idx_area_permission_check
idx_{module}_specific         -- 例如：idx_biometric_attendance_time
```

### 2. Java类命名规范（基于现有类结构分析）

#### 2.1 实体类命名模式

**基础实体**（基于AreaEntity, SmartDeviceEntity等成功实践）:
```java
{BaseDomain}Entity            -- 例如：AreaEntity, DeviceEntity, AccountEntity
```

**扩展实体**（基于AccessAreaExtEntity等成功实践）:
```java
{BaseDomain}{Module}ExtEntity -- 例如：AreaAccessExtEntity, DeviceConsumeExtEntity
```

**继承实体**（基于AccessDeviceEntity等成功实践）:
```java
{Module}{BaseType}Entity      -- 例如：AccessDeviceEntity, ConsumeDeviceEntity
```

#### 2.2 DAO接口命名模式

**现有成功模式**:
```java
{BaseDomain}Dao               -- 基础DAO（例如：AreaDao, DeviceDao）
{BaseDomain}{Module}ExtDao    -- 扩展DAO（例如：AreaAccessExtDao）
```

#### 2.3 Service类命名模式

**现有成功模式**:
```java
{BaseDomain}Service           -- 基础服务（例如：AreaService, DeviceService）
{BaseDomain}{Module}ExtService -- 扩展服务（例如：AreaAccessExtService）
```

#### 2.4 Manager类命名模式

**现有缓存管理器模式**:
```java
{BaseDomain}{Module}CacheManager -- 例如：AreaAccessCacheManager
```

### 3. 方法命名规范（基于现有方法分析）

#### 3.1 查询方法命名

**现有成功模式**:
```java
// 基础查询
get{BaseDomain}ById(Long id)           -- 根据ID查询
selectBy{BaseDomain}Ids(List<Long> ids) -- 批量查询
getAll{BaseDomain}s()                   -- 查询所有

// 条件查询
selectBy{FieldName}(Object value)       -- 根据字段查询
selectBy{FieldName}And{FieldName}(Object v1, Object v2) -- 多条件查询

// 扩展查询
get{BaseDomain}{Module}Info(Long id)    -- 获取扩展信息
select{BaseDomain}{Module}List(...)     -- 查询扩展列表
```

#### 3.2 业务方法命名

**现有成功模式**:
```java
// 判断方法
is{Feature}()                          -- 例如：isEnabled(), isRoot()
has{Feature}()                         -- 例如：hasChildren(), hasAccessMode()
supports{Feature}()                    -- 例如：supportsMode()

// 配置方法
set{Feature}Default()                  -- 设置默认值
get{Feature}Config()                   -- 获取配置
parse{Feature}Config()                 -- 解析配置

// 状态方法
enable{Feature}()                      -- 启用功能
disable{Feature}()                     -- 禁用功能
```

## 🚀 性能优化指南

### 1. 索引设计优化（基于现有索引分析）

#### 1.1 基础索引策略

**现有成功索引模式**:
```sql
-- 主键索引（自动创建）
PRIMARY KEY (ext_id)

-- 外键关联索引（高频查询优化）
KEY idx_{base_table}_{module}_id ({base_table}_id, deleted_flag)

-- 状态查询索引
KEY idx_{module}_status ({module}_status, deleted_flag)

-- 等级查询索引
KEY idx_{module}_level ({module}_level, deleted_flag)

-- 优先级排序索引
KEY idx_priority (priority, deleted_flag)
```

#### 1.2 复合索引策略（基于现有查询优化）

**现有成功复合索引**:
```sql
-- 基于高频查询模式的复合索引
KEY idx_{base_table}_level_status ({base_table}_id, {module}_level, {module}_status, deleted_flag)

-- 状态和优先级复合索引
KEY idx_status_priority ({module}_status, priority, deleted_flag)

-- 时间范围查询索引
KEY idx_time_range (create_time, {module}_status, deleted_flag)
```

#### 1.3 JSON索引策略（MySQL 5.7+）

**现有JSON字段索引**:
```sql
-- JSON配置字段索引（基于现有JSON使用）
KEY idx_time_restrictions ((CAST(time_restrictions AS CHAR(255))))
KEY idx_alert_config ((CAST(alert_config AS CHAR(255))))
KEY idx_device_linkage ((CAST(device_linkage_rules AS CHAR(255))))
```

### 2. 查询优化策略（基于现有查询分析）

#### 2.1 关联查询优化

**现有成功JOIN模式**:
```sql
-- 高效的关联查询（基于现有AreaPersonDao.xml优化）
SELECT
    base.area_id,
    base.area_code,
    base.area_name,
    ext.ext_id,
    ext.access_level,
    ext.time_restrictions
FROM t_area base
LEFT JOIN t_area_access_ext ext ON base.area_id = ext.area_id
WHERE base.deleted_flag = 0
AND (ext.access_level >= 2 OR ext.access_level IS NULL)
ORDER BY base.sort_order ASC, base.area_id ASC
```

#### 2.2 批量查询优化

**现有成功批量模式**:
```sql
-- 使用IN查询避免N+1问题（基于现有AreaPersonDao实践）
SELECT * FROM t_area_person
WHERE person_id IN (#{personIds})
AND status = 1
AND deleted_flag = 0
```

#### 2.3 分页查询优化

**现有成功分页模式**:
```sql
-- 高效分页查询（基于现有标准分页实现）
SELECT base.area_id, base.area_name, ext.access_level
FROM t_area base
LEFT JOIN t_area_access_ext ext ON base.area_id = ext.area_id
WHERE base.deleted_flag = 0
<if test='areaName != null and areaName != ""'>
AND base.area_name LIKE CONCAT('%', #{areaName}, '%')
</if>
<if test='accessLevel != null'>
AND ext.access_level >= #{accessLevel}
</if>
ORDER BY base.sort_order ASC, base.area_id ASC
LIMIT #{offset}, #{pageSize}
```

### 3. 缓存优化策略（基于现有缓存模式）

#### 3.1 分层缓存架构

**现有缓存分层模式**:
```java
// L1缓存：本地Caffeine缓存（高频访问）
// L2缓存：分布式Redis缓存（共享缓存）
// 缓存键设计：area:access:info:{areaId}

// 基于现有AreaAccessCacheManager的缓存策略
@Component
public class AreaAccessCacheManager extends BaseCacheManager {

    // L1本地缓存
    private final Cache<String, AreaAccessVO> localCache;

    // L2分布式缓存
    private final RedisTemplate<String, Object> redisTemplate;

    // 缓存键模式
    private static final String CACHE_KEY_PREFIX = "area:access";

    public AreaAccessVO getInfo(Long areaId) {
        String cacheKey = CACHE_KEY_PREFIX + ":info:" + areaId;

        // 1. 检查L1缓存
        AreaAccessVO result = localCache.getIfPresent(cacheKey);
        if (result != null) {
            return result;
        }

        // 2. 检查L2缓存
        result = (AreaAccessVO) redisTemplate.opsForValue().get(cacheKey);
        if (result != null) {
            localCache.put(cacheKey, result);
            return result;
        }

        // 3. 数据库查询
        result = queryFromDatabase(areaId);
        if (result != null) {
            // 4. 写入缓存
            localCache.put(cacheKey, result);
            redisTemplate.opsForValue().set(cacheKey, result, 30, TimeUnit.MINUTES);
        }

        return result;
    }
}
```

#### 3.2 缓存失效策略

**现有成功失效模式**:
```java
// 基于现有缓存失效策略的增强版本
public void evict(Long areaId) {
    String cacheKey = CACHE_KEY_PREFIX + ":info:" + areaId;

    // 1. 立即失效L1缓存
    localCache.invalidate(cacheKey);

    // 2. 立即失效L2缓存
    redisTemplate.delete(cacheKey);

    // 3. 发布缓存失效事件（用于集群环境）
    cacheEventPublisher.publishCacheInvalidationEvent(cacheKey);
}

// 批量失效
public void batchEvict(List<Long> areaIds) {
    List<String> cacheKeys = areaIds.stream()
        .map(id -> CACHE_KEY_PREFIX + ":info:" + id)
        .collect(Collectors.toList());

    // 批量失效L1缓存
    cacheKeys.forEach(localCache::invalidate);

    // 批量失效L2缓存
    redisTemplate.delete(cacheKeys);

    // 批量发布失效事件
    cacheEventPublisher.publishBatchCacheInvalidationEvent(cacheKeys);
}
```

### 4. JSON配置优化（基于现有JSON处理模式）

#### 4.1 JSON配置设计原则

**现有成功JSON模式**:
```java
// 避免过度嵌套，保持扁平化结构
public class TimeRestrictions {
    private List<String> workdays;      // 工作日时间段
    private List<String> weekends;      // 周末时间段
    private List<String> holidays;      // 节假日时间段
    private Boolean enabled;           // 是否启用
    private String timezone;           // 时区
}

// 配置示例
{
    "workdays": ["07:00-09:00", "17:00-19:00"],
    "weekends": ["09:00-21:00"],
    "holidays": ["全天候"],
    "enabled": true,
    "timezone": "Asia/Shanghai"
}
```

#### 4.2 JSON解析优化

**现有成功解析模式**:
```java
// 基于现有JSON解析的优化版本
public Map<String, Object> parseJsonConfig(String jsonConfig) {
    if (StringUtils.isBlank(jsonConfig)) {
        return Collections.emptyMap();
    }

    try {
        // 使用FastJSON解析（项目统一JSON库）
        Map<String, Object> config = JSON.parseObject(jsonConfig, Map.class);

        // 预处理常用字段，避免重复解析
        preprocessConfigFields(config);

        return config;
    } catch (Exception e) {
        log.warn("JSON配置解析失败: {}, 使用默认配置", jsonConfig, e);
        return getDefaultConfig();
    }
}

private void preprocessConfigFields(Map<String, Object> config) {
    // 预处理时间限制配置
    Object timeRestrictions = config.get("time_restrictions");
    if (timeRestrictions instanceof String) {
        config.put("time_restrictions", parseTimeRestrictions((String) timeRestrictions));
    }

    // 预处理位置配置
    Object locationRules = config.get("location_rules");
    if (locationRules instanceof String) {
        config.put("location_rules", parseLocationRules((String) locationRules));
    }
}
```

### 5. 批量操作优化（基于现有批量处理模式）

#### 5.1 批量插入优化

**现有成功批量模式**:
```java
// 基于现有MyBatis批量插入的优化版本
public int batchInsert(List<AreaAccessExtEntity> extensions) {
    if (CollectionUtils.isEmpty(extensions)) {
        return 0;
    }

    // 分批处理，避免单次批量过大
    int batchSize = 1000;
    int totalInserted = 0;

    for (int i = 0; i < extensions.size(); i += batchSize) {
        int endIndex = Math.min(i + batchSize, extensions.size());
        List<AreaAccessExtEntity> batch = extensions.subList(i, endIndex);

        // 使用MyBatis批量插入
        int inserted = areaAccessExtDao.batchInsert(batch);
        totalInserted += inserted;

        log.debug("批量插入扩展信息: {}/{} 条", inserted, batch.size());
    }

    return totalInserted;
}
```

#### 5.2 批量更新优化

**现有成功更新模式**:
```java
// 基于现有批量更新的优化版本
public void batchUpdateStatus(List<Long> areaIds, Integer status, Long userId) {
    if (CollectionUtils.isEmpty(areaIds)) {
        return;
    }

    // 分批更新，避免锁表时间过长
    int batchSize = 500;

    for (int i = 0; i < areaIds.size(); i += batchSize) {
        int endIndex = Math.min(i + batchSize, areaIds.size());
        List<Long> batchIds = areaIds.subList(i, endIndex);

        // 批量更新
        areaAccessExtDao.batchUpdateStatus(batchIds, status, userId);

        // 批量失效缓存
        areaAccessCacheManager.batchEvict(batchIds);

        log.debug("批量更新状态: {} 条", batchIds.size());
    }
}
```

### 6. 内存优化策略

#### 6.1 对象池化

**现有成功对象池模式**:
```java
// 基于现有对象池化的JSON解析优化
@Component
public class ConfigObjectPool {

    private final ObjectPool<Map<String, Object>> configMapPool;

    public ConfigObjectPool() {
        this.configMapPool = new GenericObjectPool<>(
            new BasePooledObjectFactory<Map<String, Object>>() {
                @Override
                public Map<String, Object> create() {
                    return new HashMap<>(16); // 预分配容量
                }

                @Override
                public void passivateObject(PooledObject<Map<String, Object>> p) {
                    p.getObject().clear(); // 清空对象内容
                }
            });
    }

    public Map<String, Object> borrowObject() throws Exception {
        return configMapPool.borrowObject();
    }

    public void returnObject(Map<String, Object> object) {
        try {
            configMapPool.returnObject(object);
        } catch (Exception e) {
            log.warn("对象归还失败", e);
        }
    }
}
```

#### 6.2 内存监控

**现有内存监控模式**:
```java
// 基于现有内存监控的增强版本
@Scheduled(fixedRate = 60000) // 每分钟检查一次
public void monitorMemoryUsage() {
    MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();

    long usedMemory = heapUsage.getUsed();
    long maxMemory = heapUsage.getMax();
    double usagePercent = (double) usedMemory / maxMemory * 100;

    // 内存使用率告警
    if (usagePercent > 80) {
        log.warn("内存使用率过高: {:.2f}%, 开始清理缓存", usagePercent);

        // 清理L1缓存
        localCache.cleanUp();

        // 清理L2缓存中的非热点数据
        evictColdCacheData();

        // 触发GC
        System.gc();
    }

    // 记录内存使用情况
    memoryMetrics.recordMemoryUsage(usagePercent);
}
```

## 📊 性能监控和调优

### 1. 关键性能指标（基于现有监控实践）

#### 1.1 数据库性能指标

**现有监控指标**:
```sql
-- 查询响应时间监控（期望 < 5ms）
SELECT AVG(timer_wait/1000000000) as avg_seconds
FROM performance_schema.events_statements_summary_by_digest
WHERE DIGEST_TEXT LIKE '%t_area_access_ext%'
AND COUNT_STAR > 100;

-- 索引使用率监控
SELECT
    TABLE_NAME,
    INDEX_NAME,
    CARDINALITY,
    SUB_PART,
    NULLABLE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME = 't_area_access_ext'
ORDER BY CARDINALITY DESC;

-- 表空间使用监控
SELECT
    table_name,
    ROUND(data_length/1024/1024, 2) as data_size_mb,
    ROUND(index_length/1024/1024, 2) as index_size_mb,
    ROUND((data_length + index_length)/1024/1024, 2) as total_size_mb
FROM information_schema.TABLES
WHERE table_schema = DATABASE()
AND table_name = 't_area_access_ext';
```

#### 1.2 缓存性能指标

**现有缓存监控**:
```java
// 基于现有缓存监控的增强版本
@Component
public class CacheMetrics {

    private final MeterRegistry meterRegistry;

    public CacheMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordCacheHit(String cacheName) {
        Counter.builder("cache.hits")
            .tag("cache", cacheName)
            .register(meterRegistry)
            .increment();
    }

    public void recordCacheMiss(String cacheName) {
        Counter.builder("cache.misses")
            .tag("cache", cacheName)
            .register(meterRegistry)
            .increment();
    }

    public void recordCacheLoadTime(String cacheName, long duration) {
        Timer.builder("cache.load.time")
            .tag("cache", cacheName)
            .register(meterRegistry)
            .record(duration, TimeUnit.MILLISECONDS);
    }

    public double getCacheHitRate(String cacheName) {
        double hits = meterRegistry.get("cache.hits")
            .tag("cache", cacheName)
            .counter()
            .count();

        double misses = meterRegistry.get("cache.misses")
            .tag("cache", cacheName)
            .counter()
            .count();

        return hits / (hits + misses);
    }
}
```

### 2. 调优建议（基于现有调优经验）

#### 2.1 数据库调优

**现有成功调优经验**:
```sql
-- 1. 优化查询执行计划
ANALYZE TABLE t_area_access_ext;

-- 2. 重建碎片化索引
ALTER TABLE t_area_access_ext ENGINE=InnoDB;

-- 3. 优化表结构
OPTIMIZE TABLE t_area_access_ext;

-- 4. 调整缓存参数
SET GLOBAL innodb_buffer_pool_size = 1073741824; -- 1GB
SET GLOBAL query_cache_size = 67108864; -- 64MB
```

#### 2.2 应用调优

**现有应用调优经验**:
```java
// JVM调优参数（基于现有生产环境经验）
-Xms2g -Xmx4g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps

// 连接池调优（基于现有Druid配置）
spring.datasource.druid.initial-size=10
spring.datasource.druid.max-active=50
spring.datasource.druid.min-idle=10
spring.datasource.druid.max-wait=60000
spring.datasource.druid.validation-query=SELECT 1
```

---

**文档维护**: 本指南将基于项目实践持续更新和完善
**执行要求**: 所有扩展表开发必须严格遵循本指南
**性能目标**: 查询响应时间 < 5ms，缓存命中率 > 80%