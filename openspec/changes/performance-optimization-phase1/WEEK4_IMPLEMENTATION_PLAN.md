# 第4周实施计划：内存优化和最终验证

**周期**: Week 4 (Day 16-20)
**负责人**: 后端团队 + 运维团队 + DBA团队
**预期目标**: 堆内存使用从2.5GB降至<1.8GB，Full GC频率降低80%
**涉及文档**: P1-7.9 MEMORY_OPTIMIZATION_GUIDE.md

---

## 📋 周目标概览

| 指标 | 当前值 | 目标值 | 提升幅度 |
|------|--------|--------|----------|
| **堆内存使用** | 2.5GB | <1.8GB | 28% ↓ |
| **Full GC频率** | 10次/小时 | <2次/小时 | 80% ↓ |
| **GC平均暂停时间** | 500ms | <100ms | 80% ↓ |
| **内存泄漏** | 存在泄漏 | 0泄漏 | 100% ↓ |

---

## 📅 Day 16: JVM堆内存调优

### 任务目标
优化JVM堆内存配置，调整垃圾回收器参数，减少GC压力。

### 16.1 上午：JVM参数配置

**步骤1**: 生产环境JVM参数配置

**文件位置**: `microservices/ioedream-gateway-service/src/main/resources/application.yml`

```yaml
# Spring Boot JVM参数配置
# 启动命令示例：
# java -jar -Xms1g -Xmx1g -XX:+UseG1GC ... gateway-service.jar

# 推荐JVM参数（生产环境）
JAVA_OPTS: >
  # 堆内存配置
  -Xms1g                    # 初始堆大小1GB
  -Xmx1g                    # 最大堆大小1GB
  -XX:NewRatio=1            # 新生代:老年代 = 1:1
  -XX:SurvivorRatio=8       # Eden:S0:S1 = 8:1:1

  # 元空间配置
  -XX:MetaspaceSize=256m    # 初始元空间256MB
  -XX:MaxMetaspaceSize=512m # 最大元空间512MB

  # G1GC配置
  -XX:+UseG1GC              # 使用G1垃圾回收器
  -XX:MaxGCPauseMillis=200  # 最大GC暂停时间200ms
  -XX:G1HeapRegionSize=16m  # G1区域大小16MB
  -XX:G1ReservePercent=10   # 保留10%内存
  -XX:InitiatingHeapOccupancyPercent=45  # 堆占用45%时触发并发标记

  # GC日志配置
  -Xlog:gc*:file=/var/log/gc-%t.log:time,tags:filecount=10,filesize=100m
  -XX:+PrintGCDetails       # 打印GC详情
  -XX:+PrintGCDateStamps    # 打印GC时间戳
  -XX:+PrintGCTimeStamps    # 打印GC耗时
  -XX:+PrintGCApplicationStoppedTime  # 打印应用暂停时间

  # OOM时自动dump
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/var/log/heapdump.hprof

  # 性能优化
  -XX:+UseStringDeduplication      # 字符串去重
  -XX:+OptimizeStringConcat       # 优化字符串拼接
  -XX:+UseCompressedOops          # 压缩普通对象指针（64位系统）
  -XX:+UseCompressedClassPointers # 压缩类指针
```

**微服务JVM参数差异配置**：

| 服务类型 | -Xms/-Xmx | NewRatio | SurvivorRatio | 说明 |
|---------|-----------|----------|---------------|------|
| **Gateway服务** | 1g | 1 | 8 | 高并发，中等对象 |
| **Access服务** | 1g | 1 | 8 | 短生命周期对象多 |
| **Attendance服务** | 1g | 2 | 8 | 长生命周期对象多 |
| **Consume服务** | 1g | 1 | 8 | 中等对象大小 |
| **Visitor服务** | 1g | 1 | 8 | 短生命周期对象 |
| **Video服务** | 2g | 1 | 8 | **大对象（视频流）** |
| **Device-Comm服务** | 1g | 1 | 8 | I/O密集型 |

**步骤2**: 创建JVM参数管理脚本

**文件位置**: `scripts/set-jvm-opts.sh`

```bash
#!/bin/bash

# JVM参数管理脚本
# 用途：为不同服务设置最优JVM参数

SERVICE_NAME=$1
ENV=${2:-prod}

if [ -z "$SERVICE_NAME" ]; then
    echo "用法: $0 <服务名> [环境]"
    echo "示例: $0 gateway-service prod"
    exit 1
fi

echo "========================================="
echo "设置JVM参数: 服务=$SERVICE_NAME, 环境=$ENV"
echo "========================================="

case $SERVICE_NAME in
    gateway-service)
        JAVA_OPTS="-Xms1g -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
        ;;
    access-service)
        JAVA_OPTS="-Xms1g -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
        ;;
    attendance-service)
        JAVA_OPTS="-Xms1g -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:NewRatio=2"
        ;;
    consume-service)
        JAVA_OPTS="-Xms1g -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
        ;;
    visitor-service)
        JAVA_OPTS="-Xms1g -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
        ;;
    video-service)
        # Video服务需要更多内存（处理视频流）
        JAVA_OPTS="-Xms2g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=300 -XX:G1HeapRegionSize=32m"
        ;;
    device-comm-service)
        JAVA_OPTS="-Xms1g -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
        ;;
    *)
        echo "未知服务: $SERVICE_NAME"
        exit 1
        ;;
esac

# 添加通用GC日志
JAVA_OPTS="$JAVA_OPTS -Xlog:gc*:file=/var/log/$SERVICE_NAME-gc.log:time,tags:filecount=10,filesize=100m"

# 添加OOM dump
JAVA_OPTS="$JAVA_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/$SERVICE_NAME-heapdump.hprof"

# 环境特定配置
if [ "$ENV" = "prod" ]; then
    # 生产环境：字符串去重、压缩指针
    JAVA_OPTS="$JAVA_OPTS -XX:+UseStringDeduplication -XX:+UseCompressedOops -XX:+UseCompressedClassPointers"
elif [ "$ENV" = "dev" ]; then
    # 开发环境：保留调试信息
    JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails -XX:+PrintGCDateStamps"
fi

echo "JAVA_OPTS=$JAVA_OPTS"
echo "========================================="

# 导出环境变量
export JAVA_OPTS

echo "JVM参数设置完成！"
echo "启动服务命令："
echo "java \$JAVA_OPTS -jar $SERVICE_NAME.jar"
```

**使用示例**：

```bash
# 设置Gateway服务JVM参数（生产环境）
./scripts/set-jvm-opts.sh gateway-service prod

# 设置Video服务JVM参数（生产环境）
./scripts/set-jvm-opts.sh video-service prod

# 启动服务
java $JAVA_OPTS -jar gateway-service.jar
```

### 16.2 下午：Caffeine本地缓存优化

**步骤1**: 优化Caffeine缓存配置

**文件位置**: `microservices/microservices-common-cache/src/main/java/net/lab1024/sa/common/config/CaffeineCacheConfig.java`

```java
package net.lab1024.sa.common.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine本地缓存配置
 *
 * 优化点：
 * 1. 设置合理的initialCapacity（减少rehash）
 * 2. 使用weakKeys/weakValues（防止内存泄漏）
 * 3. 启用recordStats（监控缓存命中率）
 */
@Slf4j
@EnableCaching
@Configuration
public class CaffeineCacheConfig {

    /**
     * 用户基本信息缓存
     *
     * 配置说明：
     * - initialCapacity=1000: 预分配1000个槽位（避免rehash）
     * - maximumSize=10000: 最多缓存10000个用户
     * - expireAfterWrite=5min: 写入5分钟后过期
     * - weakKeys: 使用弱引用键（GC时自动回收）
     */
    @Bean("userBasicInfoCache")
    public Cache<String, UserBasicInfoVO> userBasicInfoCache() {
        log.info("[缓存配置] 初始化用户基本信息缓存");

        return Caffeine.newBuilder()
            .initialCapacity(1000)                   // 初始容量（关键优化）
            .maximumSize(10000)                      // 最大容量
            .expireAfterWrite(5, TimeUnit.MINUTES)   // 写入5分钟后过期
            .expireAfterAccess(3, TimeUnit.MINUTES)  // 访问3分钟后过期
            .weakKeys()                              // 弱引用键（防止内存泄漏）
            .recordStats()                           // 记录统计信息
            .build();
    }

    /**
     * 设备信息缓存
     *
     * 特点：
     * - 设备信息相对稳定（10分钟过期）
     * - 设备数量有限（maximumSize=5000）
     */
    @Bean("deviceInfoCache")
    public Cache<String, DeviceInfoVO> deviceInfoCache() {
        log.info("[缓存配置] 初始化设备信息缓存");

        return Caffeine.newBuilder()
            .initialCapacity(500)                    // 初始容量500
            .maximumSize(5000)                       // 最大容量5000
            .expireAfterWrite(10, TimeUnit.MINUTES)  // 写入10分钟后过期
            .expireAfterAccess(5, TimeUnit.MINUTES)  // 访问5分钟后过期
            .weakKeys()
            .recordStats()
            .build();
    }

    /**
     * 字典数据缓存
     *
     * 特点：
     * - 字典数据变化很少（30分钟过期）
     * - 数据量小（maximumSize=1000）
     */
    @Bean("dictDataCache")
    public Cache<String, DictDataVO> dictDataCache() {
        log.info("[缓存配置] 初始化字典数据缓存");

        return Caffeine.newBuilder()
            .initialCapacity(200)
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.MINUTES)  // 写入30分钟后过期
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .weakKeys()
            .recordStats()
            .build();
    }

    /**
     * 权限数据缓存
     *
     * 特点：
     * - 权限数据敏感（5分钟过期）
     * - 用户数量多（maximumSize=10000）
     */
    @Bean("permissionCache")
    public Cache<String, PermissionVO> permissionCache() {
        log.info("[缓存配置] 初始化权限数据缓存");

        return Caffeine.newBuilder()
            .initialCapacity(1000)
            .maximumSize(10000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .expireAfterAccess(3, TimeUnit.MINUTES)
            .weakKeys()
            .recordStats()
            .build();
    }

    /**
     * Token黑名单缓存
     *
     * 特点：
     * - 黑名单数据短期有效（15分钟过期）
     * - 数据量小（maximumSize=500）
     */
    @Bean("tokenBlacklistCache")
    public Cache<String, Boolean> tokenBlacklistCache() {
        log.info("[缓存配置] 初始化Token黑名单缓存");

        return Caffeine.newBuilder()
            .initialCapacity(100)
            .maximumSize(500)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .weakKeys()
            .recordStats()
            .build();
    }
}
```

**步骤2**: 监控缓存命中率

**文件位置**: `microservices/ioedream-gateway-service/src/main/java/net/lab1024/sa/gateway/monitor/CacheStatsMonitor.java`

```java
package net.lab1024.sa.gateway.monitor;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.vo.UserBasicInfoVO;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 缓存统计监控
 *
 * 定时任务：每5分钟输出一次缓存统计
 */
@Slf4j
@Component
public class CacheStatsMonitor {

    @Resource(name = "userBasicInfoCache")
    private Cache<String, UserBasicInfoVO> userBasicInfoCache;

    @Resource
    private CacheManager cacheManager;

    /**
     * 缓存统计定时任务（每5分钟执行）
     */
    @Scheduled(fixedRate = 300000)
    public void printCacheStats() {
        log.info("=========================================");
        log.info("[缓存统计] 开始收集缓存统计信息");

        // 用户基本信息缓存统计
        var userStats = userBasicInfoCache.stats();
        if (userStats != null) {
            log.info("[缓存统计] 用户基本信息缓存:");
            log.info("  - 命中率: {:.2f}%", userStats.hitRate() * 100);
            log.info("  - 命中次数: {}", userStats.hitCount());
            log.info("  - 未命中次数: {}", userStats.missCount());
            log.info("  - 加载次数: {}", userStats.loadCount());
            log.info("  - 加载失败次数: {}", userStats.loadFailureCount());
            log.info("  - 总加载时间: {}ms", userStats.totalLoadTime() / 1_000_000);
            log.info("  - 平均加载时间: {:.2f}ms", userStats.averageLoadPenalty() / 1_000_000);
            log.info("  - 驱逐次数: {}", userStats.evictionCount());
        }

        log.info("[缓存统计] 缓存统计信息收集完成");
        log.info("=========================================");

        // 告警：如果命中率低于80%
        if (userStats != null && userStats.hitRate() < 0.8) {
            log.warn("[缓存告警] 用户基本信息缓存命中率低于80%: {:.2f}%",
                     userStats.hitRate() * 100);
        }
    }
}
```

**验收标准**：
- ✅ JVM参数配置完成
- ✅ Caffeine缓存优化完成
- ✅ 堆内存使用降低20%以上
- ✅ 缓存命中率>80%

---

## 📅 Day 17: 对象池化和复用

### 任务目标
实现对象池化，减少对象创建开销，降低GC压力。

### 17.1 上午：ThreadLocal复用

**步骤1**: SimpleDateFormat线程安全复用

**文件位置**: `microservices/microservices-common-core/src/main/java/net/lab1024/sa/common/util/DateUtil.java`

```java
package net.lab1024.sa.common.util;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期工具类 - 线程安全版本
 *
 * 优化点：
 * 1. ThreadLocal复用SimpleDateFormat（避免重复创建）
 * 2. 使用DateTimeFormatter（Java 8+，线程安全）
 */
@Slf4j
public class DateUtil {

    /**
     * ThreadLocal复用SimpleDateFormat
     *
     * 优势：
     * - 每个线程独享一个实例（线程安全）
     * - 避免重复创建对象（节省内存）
     * - 减少GC压力
     */
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT =
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT_SHORT =
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    /**
     * 格式化日期（线程安全）
     *
     * @param date 日期
     * @return 格式化字符串
     */
    public static String format(Date date) {
        return DATE_FORMAT.get().format(date);
    }

    /**
     * 格式化日期（短格式）
     *
     * @param date 日期
     * @return 格式化字符串
     */
    public static String formatShort(Date date) {
        return DATE_FORMAT_SHORT.get().format(date);
    }

    /**
     * 解析日期字符串（线程安全）
     *
     * @param dateStr 日期字符串
     * @return Date对象
     */
    public static Date parse(String dateStr) {
        try {
            return DATE_FORMAT.get().parse(dateStr);
        } catch (Exception e) {
            log.error("[日期工具] 解析日期失败: dateStr={}", dateStr, e);
            return null;
        }
    }

    /**
     * 清理ThreadLocal（防止内存泄漏）
     *
     * 注意：在Thread结束时调用
     */
    public static void cleanup() {
        DATE_FORMAT.remove();
        DATE_FORMAT_SHORT.remove();
    }

    // ==================== Java 8+ DateTimeFormatter（推荐） ====================

    /**
     * DateTimeFormatter常量（线程安全，无需ThreadLocal）
     */
    private static final DateTimeFormatter DTF =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter DTF_SHORT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 格式化LocalDateTime（推荐使用）
     *
     * @param dateTime 日期时间
     * @return 格式化字符串
     */
    public static String format(LocalDateTime dateTime) {
        return dateTime.format(DTF);
    }

    /**
     * 解析日期时间字符串
     *
     * @param dateTimeStr 日期时间字符串
     * @return LocalDateTime对象
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, DTF);
        } catch (Exception e) {
            log.error("[日期工具] 解析日期时间失败: dateTimeStr={}", dateTimeStr, e);
            return null;
        }
    }
}
```

**步骤2**: 字符串工具类ThreadLocal复用

**文件位置**: `microservices/microservices-common-core/src/main/java/net/lab1024/sa/common/util/StringBuilderUtil.java`

```java
package net.lab1024.sa.common.util;

import lombok.extern.slf4j.Slf4j;

/**
 * StringBuilder工具类 - ThreadLocal复用
 *
 * 优化点：
 * - 每个线程复用一个StringBuilder（避免重复创建）
 * - 减少对象创建和GC压力
 */
@Slf4j
public class StringBuilderUtil {

    /**
     * ThreadLocal复用StringBuilder（初始容量256）
     */
    private static final ThreadLocal<StringBuilder> STRING_BUILDER =
        ThreadLocal.withInitial(() -> new StringBuilder(256));

    /**
     * 获取StringBuilder实例
     *
     * @return StringBuilder实例
     */
    public static StringBuilder getStringBuilder() {
        StringBuilder sb = STRING_BUILDER.get();
        // 清空内容（复用）
        sb.setLength(0);
        return sb;
    }

    /**
     * 拼接字符串
     *
     * @param strings 字符串数组
     * @return 拼接后的字符串
     */
    public static String concat(String... strings) {
        StringBuilder sb = getStringBuilder();
        for (String str : strings) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * 清理ThreadLocal（防止内存泄漏）
     */
    public static void cleanup() {
        STRING_BUILDER.remove();
    }
}
```

### 17.2 下午：Commons Pool2对象池

**步骤1**: 添加依赖

```xml
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```

**步骤2**: 创建对象池工厂

**文件位置**: `microservices/microservices-common-core/src/main/java/net/lab1024/sa/common/pool/ObjectPoolFactory.java`

```java
package net.lab1024.sa.common.pool;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * 对象池工厂
 *
 * 适用场景：
 * - 创建成本高的对象（数据库连接、Socket连接）
 * - 有限资源（连接数、文件句柄）
 * - 频繁创建销毁的对象
 */
@Slf4j
public class ObjectPoolFactory {

    /**
     * 创建对象池
     *
     * @param factory 对象工厂
     * @param config 对象池配置
     * @param <T> 对象类型
     * @return 对象池
     */
    public static <T> GenericObjectPool<T> createPool(
            PooledObjectFactory<T> factory,
            GenericObjectPoolConfig<T> config) {

        GenericObjectPool<T> pool = new GenericObjectPool<>(factory, config);

        log.info("[对象池] 创建对象池:");
        log.info("  - 最大对象数: {}", config.getMaxTotal());
        log.info("  - 最大空闲对象数: {}", config.getMaxIdle());
        log.info("  - 最小空闲对象数: {}", config.getMinIdle());
        log.info("  - 是否等待: {}", config.getBlockWhenExhausted());

        return pool;
    }

    /**
     * 默认对象池配置
     *
     * @param <T> 对象类型
     * @return 默认配置
     */
    public static <T> GenericObjectPoolConfig<T> createDefaultConfig() {
        GenericObjectPoolConfig<T> config = new GenericObjectPoolConfig<>();

        // 最大对象数
        config.setMaxTotal(20);

        // 最大空闲对象数
        config.setMaxIdle(10);

        // 最小空闲对象数
        config.setMinIdle(5);

        // 对象耗尽时是否等待
        config.setBlockWhenExhausted(true);

        // 获取对象最大等待时间（毫秒）
        config.setMaxWaitMillis(5000);

        // 获取对象时是否验证
        config.setTestOnBorrow(true);

        // 归还对象时是否验证
        config.setTestOnReturn(false);

        // 空闲时是否验证
        config.setTestWhileIdle(true);

        // 空闲对象驱逐线程运行间隔（毫秒）
        config.setTimeBetweenEvictionRunsMillis(30000);

        // 驱逐线程每次检查的最大对象数
        config.setNumTestsPerEvictionRun(3);

        // 对象最小空闲时间（毫秒）
        config.setMinEvictableIdleTimeMillis(60000);

        return config;
    }
}
```

**步骤3**: 实际使用示例

**文件位置**: `microservices/ioedream-device-comm-service/src/main/java/net/lab1024/sa/device/pool/DeviceConnectionPool.java`

```java
package net.lab1024.sa.device.pool;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 设备连接对象池
 *
 * 优势：
 * - 复用TCP连接（避免重复建立连接）
 * - 减少网络开销
 * - 提升设备通信性能
 */
@Slf4j
@Component
public class DeviceConnectionPool {

    private GenericObjectPool<DeviceConnection> pool;

    @PostConstruct
    public void init() {
        log.info("[设备连接池] 初始化设备连接对象池");

        // 创建对象池配置
        GenericObjectPoolConfig<DeviceConnection> config = ObjectPoolFactory.createDefaultConfig();

        // 设备连接池特定配置
        config.setMaxTotal(50);              // 最大连接数50
        config.setMaxIdle(20);               // 最大空闲连接20
        config.setMinIdle(10);               // 最小空闲连接10
        config.setMaxWaitMillis(3000);       // 获取连接最大等待3秒

        // 创建对象池
        pool = ObjectPoolFactory.createPool(new DeviceConnectionFactory(), config);

        log.info("[设备连接池] 设备连接对象池初始化完成");
    }

    /**
     * 获取设备连接
     *
     * @return 设备连接
     * @throws Exception 获取失败时抛出异常
     */
    public DeviceConnection borrowConnection() throws Exception {
        log.debug("[设备连接池] 获取设备连接");

        DeviceConnection connection = pool.borrowObject();

        log.debug("[设备连接池] 设备连接获取成功: active={}, idle={}",
                  pool.getNumActive(), pool.getNumIdle());

        return connection;
    }

    /**
     * 归还设备连接
     *
     * @param connection 设备连接
     */
    public void returnConnection(DeviceConnection connection) {
        log.debug("[设备连接池] 归还设备连接");

        pool.returnObject(connection);

        log.debug("[设备连接池] 设备连接归还成功: active={}, idle={}",
                  pool.getNumActive(), pool.getNumIdle());
    }

    @PreDestroy
    public void destroy() {
        log.info("[设备连接池] 关闭设备连接对象池");
        pool.close();
    }

    /**
     * 设备连接工厂
     */
    private static class DeviceConnectionFactory implements PooledObjectFactory<DeviceConnection> {

        @Override
        public PooledObject<DeviceConnection> makeObject() throws Exception {
            log.debug("[设备连接工厂] 创建新的设备连接");

            DeviceConnection connection = new DeviceConnection();
            connection.connect();

            return new DefaultPooledObject<>(connection);
        }

        @Override
        public void destroyObject(PooledObject<DeviceConnection> p) throws Exception {
            log.debug("[设备连接工厂] 销毁设备连接");

            DeviceConnection connection = p.getObject();
            connection.disconnect();
        }

        @Override
        public boolean validateObject(PooledObject<DeviceConnection> p) {
            DeviceConnection connection = p.getObject();
            return connection.isConnected();
        }

        @Override
        public void activateObject(PooledObject<DeviceConnection> p) throws Exception {
            log.debug("[设备连接工厂] 激活设备连接");
        }

        @Override
        public void passivateObject(PooledObject<DeviceConnection> p) throws Exception {
            log.debug("[设备连接工厂] 钝化设备连接");
        }
    }
}
```

**验收标准**：
- ✅ ThreadLocal复用实现完成
- ✅ 对象池配置完成
- ✅ 对象创建数量减少50%以上
- ✅ GC频率降低30%以上

---

## 📅 Day 18: 内存泄漏检测

### 任务目标
使用VisualVM、MAT检测内存泄漏，修复发现的问题。

### 18.1 上午：VisualVM监控

**步骤1**: 启动应用并附加VisualVM

```bash
# 1. 启动应用（添加JMX参数）
java -Dcom.sun.management.jmxremote \
     -Dcom.sun.management.jmxremote.port=9010 \
     -Dcom.sun.management.jmxremote.authenticate=false \
     -Dcom.sun.management.jmxremote.ssl=false \
     -jar ioedream-gateway-service.jar

# 2. 启动VisualVM
# Windows: visualvm.exe
# Linux: visualvm
# Mac: visualvm

# 3. 连接到应用
# 添加JMX连接：localhost:9010
```

**步骤2**: 监控堆内存使用

**VisualVM操作步骤**：

1. 切换到"Monitor"标签页
2. 观察"Heap"堆内存使用情况
3. 点击"Perform GC"手动触发GC
4. 观察GC后内存是否释放

**正常情况**：
- GC后堆内存应该明显下降
- 内存使用曲线应该有波动（不应持续上升）

**内存泄漏迹象**：
- GC后内存不下降
- 内存使用曲线持续上升
- Full GC频繁触发

### 18.2 下午：MAT分析Heap Dump

**步骤1**: 生成Heap Dump

```bash
# 方法1：使用jmap命令
jmap -dump:live,format=b,file=heapdump.hprof <PID>

# 方法2：使用JMX（VisualVM）
# 在VisualVM中点击"Heap Dump"按钮

# 方法3：OOM自动dump（已配置）
# 当OOM时自动生成：/var/log/heapdump.hprof
```

**步骤2**: 使用MAT分析

**打开MAT**：
```bash
# 启动Memory Analyzer Tool
# Windows: MemoryAnalyzer.exe
# Linux: ./MemoryAnalyzer
# Mac: ./MemoryAnalyzer.app
```

**分析步骤**：

1. **打开Heap Dump文件**：File → Open Heap Dump → 选择heapdump.hprof

2. **查看 Leak Suspects**：
   - 点击"Leak Suspects"报告
   - 查看自动检测的内存泄漏嫌疑
   - 分析可疑对象

3. **Dominator Tree**：
   - 切换到"Dominator Tree"视图
   - 按对象大小排序
   - 查找最大的对象

4. **Histogram**：
   - 切换到"Histogram"视图
   - 按类名分组统计
   - 查找对象数量异常的类

**常见内存泄漏模式**：

```java
// ❌ 错误示例1：静态集合持有对象引用
public class CacheManager {
    private static final Map<String, Object> CACHE = new HashMap<>();

    public void addToCache(String key, Object value) {
        CACHE.put(key, value);  // 永不释放，内存泄漏
    }
}

// ✅ 正确示例1：使用Caffeine缓存
public class CacheManager {
    private final Cache<String, Object> cache = Caffeine.newBuilder()
        .maximumSize(10000)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build();

    public void addToCache(String key, Object value) {
        cache.put(key, value);  // 自动过期，不会泄漏
    }
}

// ❌ 错误示例2：ThreadLocal未清理
public class ThreadLocalExample {
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT =
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    // 线程结束时未清理，内存泄漏
}

// ✅ 正确示例2：ThreadLocal及时清理
public class ThreadLocalExample {
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT =
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    public void cleanup() {
        DATE_FORMAT.remove();  // 线程结束时清理
    }
}

// ❌ 错误示例3：未关闭的资源
public class FileReader {
    public void readFile(String path) {
        FileInputStream fis = new FileInputStream(path);
        // 未关闭流，资源泄漏
    }
}

// ✅ 正确示例3：使用try-with-resources
public class FileReader {
    public void readFile(String path) {
        try (FileInputStream fis = new FileInputStream(path)) {
            // 自动关闭
        } catch (IOException e) {
            log.error("读取文件失败", e);
        }
    }
}
```

**验收标准**：
- ✅ 无内存泄漏
- ✅ ThreadLocal正确清理
- ✅ 资源正确关闭
- ✅ 静态集合使用缓存替代

---

## 📅 Day 19: GC日志分析和优化

### 任务目标
分析GC日志，优化GC参数，减少GC暂停时间。

### 19.1 上午：GC日志分析

**步骤1**: 使用GCViewer分析GC日志

```bash
# 1. 下载GCViewer
# https://github.com/chewiebug/GCViewer/releases

# 2. 打开GC日志文件
# File → Open File → 选择 /var/log/gc-*.log

# 3. 查看GC统计信息
# - 总GC次数
# - 总GC时间
# - 平均GC暂停时间
# - 最大GC暂停时间
# - 吞吐量（非GC时间占比）
```

**关键指标解读**：

| 指标 | 合理范围 | 说明 |
|------|---------|------|
| **吞吐量** | >98% | 非GC时间占比，越高越好 |
| **Full GC频率** | <1次/小时 | Full GC对性能影响大 |
| **平均GC暂停时间** | <100ms | Young GC暂停时间 |
| **最大GC暂停时间** | <500ms | 最坏情况下的暂停时间 |

**步骤2**: 使用GCEasy分析GC日志

```bash
# 在线工具：https://gceasy.io/
# 上传GC日志文件：/var/log/gc-*.log
# 查看分析报告
```

**GCEasy报告关键信息**：

1. **GC概览**：
   - GC次数和频率
   - GC暂停时间分布
   - 堆内存使用趋势

2. **问题诊断**：
   - GC性能瓶颈
   - 内存泄漏嫌疑
   - 优化建议

### 19.2 下午：GC参数调优

**调优前GC参数**：

```bash
# 初始配置
-Xms1g -Xmx1g -XX:+UseG1GC
```

**调优后GC参数**：

```bash
# 优化配置
-Xms1g -Xmx1g \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=200 \
-XX:G1HeapRegionSize=16m \
-XX:G1ReservePercent=10 \
-XX:InitiatingHeapOccupancyPercent=45 \
-XX:+UseStringDeduplication \
-XX:+PrintGCDetails \
-XX:+PrintGCDateStamps \
-Xlog:gc*:file=/var/log/gc-%t.log:time,tags:filecount=10,filesize=100m
```

**调优说明**：

| 参数 | 默认值 | 优化值 | 说明 |
|------|--------|--------|------|
| `MaxGCPauseMillis` | 200ms | 200ms | 目标最大GC暂停时间 |
| `G1HeapRegionSize` | 自动 | 16m | G1区域大小（堆大小/2048） |
| `InitiatingHeapOccupancyPercent` | 45 | 45 | 触发并发标记的堆占用率 |
| `UseStringDeduplication` | 关闭 | 开启 | 字符串去重（节省内存） |

**对比测试**：

```bash
# 1. 启动应用（使用优化前参数）
java -Xms1g -Xmx1g -XX:+UseG1GC -jar app.jar
# 运行压测：wrk -t12 -c400 -d300s ...
# 记录GC日志：gc-before.log

# 2. 启动应用（使用优化后参数）
java -Xms1g -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -jar app.jar
# 运行压测：wrk -t12 -c400 -d300s ...
# 记录GC日志：gc-after.log

# 3. 对比分析
# 使用GCViewer/GCEasy对比gc-before.log和gc-after.log
```

**验收标准**：
- ✅ 吞吐量>98%
- ✅ Full GC频率<1次/小时
- ✅ 平均GC暂停时间<100ms
- ✅ GC日志分析无异常

---

## 📅 Day 20: 最终性能验证

### 任务目标
执行全面的性能测试，验证所有P1指标达成。

### 20.1 上午：综合性能测试

**测试场景**：

**场景1：基础接口性能测试**

```bash
# 测试目标：验证接口响应时间<400ms
wrk -t12 -c400 -d60s --latency http://localhost:8090/api/v1/access/devices

# 预期结果：
# - Latency平均<400ms
# - P99<1000ms
```

**场景2：并发用户测试**

```bash
# 测试目标：验证支持1000+并发用户
wrk -t12 -c1000 -d300s --latency http://localhost:8090/api/v1/access/verify/async

# 预期结果：
# - 无连接拒绝
# - 无请求超时
# - 错误率<1%
```

**场景3：缓存命中率测试**

```bash
# 测试目标：验证缓存命中率>90%
# 使用Druid监控查看SQL执行次数
# 计算公式：缓存命中率 = 1 - (SQL执行次数 / 总请求数)

# 预期结果：
# - 缓存命中率>90%
# - 数据库CPU使用率<50%
```

**场景4：内存使用测试**

```bash
# 测试目标：验证堆内存使用<1.8GB
# 使用VisualVM监控堆内存

# 预期结果：
# - 堆内存使用<1.8GB
# - Full GC频率<2次/小时
```

**场景5：网络性能测试**

```bash
# 测试目标：验证页面加载时间<1.5s
# 使用Lighthouse测试

lighthouse https://cdn.example.com --output html --output-path report.html

# 预期结果：
# - Performance评分≥85
# - LCP（最大内容绘制）<2.5s
# - FID（首次输入延迟）<100ms
```

### 20.2 下午：最终验收报告

**P1性能优化成果汇总表**：

| 维度 | 指标 | 优化前 | 优化后 | 提升幅度 | 状态 |
|------|------|--------|--------|----------|------|
| **数据库性能** | 查询响应时间 | 800ms | <200ms | 75% ↑ | ✅ |
| **缓存效率** | 缓存命中率 | 65% | >90% | 38% ↑ | ✅ |
| **连接池** | 连接池性能 | 基线 | 40% ↑ | 40% ↑ | ✅ |
| **并发能力** | TPS | 800 | >2000 | 150% ↑ | ✅ |
| **并发用户** | 并发用户数 | 300 | >1000 | 233% ↑ | ✅ |
| **内存使用** | 堆内存 | 2.5GB | <1.8GB | 28% ↓ | ✅ |
| **GC性能** | Full GC频率 | 10次/小时 | <2次/小时 | 80% ↓ | ✅ |
| **前端性能** | 首屏加载 | 3.5s | <2s | 43% ↑ | ✅ |
| **网络性能** | 页面加载 | 3.5s | <1.5s | 57% ↑ | ✅ |
| **Bundle大小** | Bundle体积 | 5.2MB | <2MB | 62% ↓ | ✅ |

**文档清单**：

**已完成的实施文档**：
1. ✅ WEEK1_IMPLEMENTATION_PLAN.md（数据库和缓存优化）
2. ✅ WEEK2_IMPLEMENTATION_PLAN.md（并发和连接池优化）
3. ✅ WEEK3_IMPLEMENTATION_PLAN.md（前端和网络优化）
4. ✅ WEEK4_IMPLEMENTATION_PLAN.md（内存优化和最终验证）

**技术指南文档**：
1. ✅ DATABASE_INDEX_OPTIMIZATION_GUIDE.md
2. ✅ CACHE_ARCHITECTURE_OPTIMIZATION_GUIDE.md
3. ✅ CONNECTION_POOL_UNIFICATION_GUIDE.md
4. ✅ CONCURRENCY_OPTIMIZATION_GUIDE.md
5. ✅ FRONTEND_PERFORMANCE_OPTIMIZATION_GUIDE.md
6. ✅ NETWORK_OPTIMIZATION_GUIDE.md
7. ✅ MEMORY_OPTIMIZATION_GUIDE.md

**最终Git标签**：

```bash
# 创建最终Git标签
git tag -a v4.0.0-p1-performance-optimization-complete -m "P1性能优化完成：所有10个指标达成"

# 推送到远程仓库
git push origin v4.0.0-p1-performance-optimization-complete
```

**验收签字确认**：

- [ ] 后端团队负责人：_________
- [ ] 前端团队负责人：_________
- [ ] DBA团队负责人：_________
- [ ] 运维团队负责人：_________
- [ ] 架构委员会：_________
- [ ] 项目经理：_________

---

## 📊 最终总结

### 成果亮点

**1. 性能提升显著**
- TPS提升150%（800→2000+）
- 并发用户提升233%（300→1000+）
- 响应时间降低75%（800ms→<200ms）

**2. 资源利用率优化**
- 内存使用降低28%（2.5GB→<1.8GB）
- 数据库负载降低60%（缓存命中率65%→90%）
- GC频率降低80%（10次/小时→<2次/小时）

**3. 用户体验改善**
- 首屏加载时间降低43%（3.5s→<2s）
- 页面加载时间降低57%（3.5s→<1.5s）
- Bundle体积降低62%（5.2MB→<2MB）

### 后续持续优化

**P2级优化任务**（下个阶段）：
1. 微服务边界优化
2. 配置管理统一
3. 日志标准化
4. 监控体系完善

**性能监控持续化**：
- 部署Prometheus+Grafana监控
- 设置性能告警阈值
- 定期性能测试（每月一次）
- 性能指标持续跟踪

---

**文档版本**: v1.0
**创建时间**: 2025-01-XX
**负责人**: 性能优化小组
**审核人**: 架构委员会
**验收时间**: 2025-01-XX
