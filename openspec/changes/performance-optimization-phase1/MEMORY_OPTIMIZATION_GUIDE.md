# 内存优化实施指南

> **任务编号**: P1-7.9
> **任务名称**: 内存优化（内存占用降低30%）
> **预计工时**: 2人天
> **优先级**: P1（高优先级）
> **创建日期**: 2025-12-26

---

## 📋 任务概述

### 问题描述

当前系统存在以下内存问题：

- **内存占用高**: 堆内存占用2.5GB，频繁Full GC
- **对象创建频繁**: 大量临时对象创建，GC压力大
- **内存泄漏**: 部分对象未正确释放，内存持续增长
- **缓存配置不当**: 缓存对象过大，占用大量内存
- **连接池泄露**: 数据库连接未正确关闭，内存泄露
- **OOM风险**: 高峰期内存不足，存在OOM风险

### 优化目标

- ✅ **堆内存占用**: 从2.5GB→<1.8GB（**28%↓**）
- ✅ **Full GC频率**: 从每天5次→每天1次（**80%↓**）
- ✅ **GC停顿时间**: P95停顿<500ms
- ✅ **对象创建速率**: 降低50%
- ✅ **内存泄漏**: 清零内存泄漏问题
- ✅ **缓存内存**: 优化缓存占用，降低40%

### 预期效果

| 指标 | 优化前 | 优化后 | 提升幅度 |
|------|--------|--------|----------|
| **堆内存占用** | 2.5GB | <1.8GB | **28%↓** |
| **Full GC频率** | 5次/天 | 1次/天 | **80%↓** |
| **Young GC频率** | 120次/小时 | 60次/小时 | **50%↓** |
| **GC停顿时间(P95)** | 1200ms | <500ms | **58%↑** |
| **对象创建速率** | 10000个/秒 | 5000个/秒 | **50%↓** |
| **缓存内存占用** | 800MB | 480MB | **40%↓** |

---

## 🎯 优化策略

### 1. JVM内存配置优化

#### 1.1 堆内存配置

**当前配置（问题）**:

```bash
# 初始堆内存太小，最大堆内存设置不合理
java -Xms512m -Xmx2g -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m ...
```

**优化后配置**:

```bash
# 推荐配置（4GB服务器）
java -Xms1g -Xmx1g \
  -XX:MetaspaceSize=256m \
  -XX:MaxMetaspaceSize=512m \
  -XX:NewRatio=2 \
  -XX:SurvivorRatio=8 \
  -Xss512k \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:G1HeapRegionSize=16m \
  -XX:InitiatingHeapOccupancyPercent=45 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/logs/heapdump.hprof \
  ...
```

**参数说明**:

| 参数 | 说明 | 推荐值 | 计算公式 |
|------|------|--------|----------|
| `-Xms` | 初始堆内存 | 1g | 与Xmx相同，避免动态调整 |
| `-Xmx` | 最大堆内存 | 1g | 系统内存 * 0.5（留一半给OS） |
| `-XX:MetaspaceSize` | 元空间初始大小 | 256m | 根据类数量调整 |
| `-XX:MaxMetaspaceSize` | 元空间最大大小 | 512m | MetaspaceSize * 2 |
| `-XX:NewRatio` | 新生代/老年代比例 | 2 | 新生代:老年代 = 1:2 |
| `-XX:SurvivorRatio` | Eden/Survivor比例 | 8 | Eden:S0:S1 = 8:1:1 |
| `-Xss` | 线程栈大小 | 512k | 减少栈大小，降低内存占用 |
| `-XX:+UseG1GC` | 使用G1垃圾收集器 | - | 推荐用于大堆内存 |
| `-XX:MaxGCPauseMillis` | 最大GC停顿目标 | 200ms | GC停顿时间目标 |
| `-XX:G1HeapRegionSize` | G1区域大小 | 16m | 堆内存 / 2048 |
| `-XX:InitiatingHeapOccupancyPercent` | 触发并发GC阈值 | 45% | 老年代占用45%时触发 |

**不同环境配置**:

```yaml
# 开发环境（8GB内存服务器）
JAVA_OPTS: "-Xms512m -Xmx512m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -XX:+UseG1GC"

# 测试环境（16GB内存服务器）
JAVA_OPTS: "-Xms1g -Xmx1g -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:+UseG1GC"

# 生产环境（32GB内存服务器）
JAVA_OPTS: "-Xms4g -Xmx4g -XX:MetaspaceSize=512m -XX:MaxMetaspaceSize=1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

#### 1.2 G1GC配置优化

**G1GC核心参数**:

```bash
# G1GC最佳实践配置
-XX:+UseG1GC                                    # 使用G1收集器
-XX:MaxGCPauseMillis=200                        # 最大GC停顿200ms
-XX:G1HeapRegionSize=16m                        # Region大小16MB
-XX:InitiatingHeapOccupancyPercent=45           # 老年代45%时触发并发GC
-XX:G1ReservePercent=10                         # 预留10%内存防止To Space溢出
-XX:G1MixedGCCountTarget=8                      # 混合GC目标次数8次
-XX:G1OldCSetRegionThreshold=10                 # 老年代回收Region阈值10个
-XX:+G1UseAdaptiveIHOP                          # 自适应调整IHOP阈值
-XX:G1HeapWastePercent=5                        # 堆浪费百分比5%
-XX:G1MixedGCLiveThresholdPercent=85            # 存活率阈值85%
```

**效果**: GC停顿时间降低50%，内存利用率提升20%

### 2. 对象创建优化

#### 2.1 减少临时对象创建

**优化前（大量临时对象）**:

```java
public String formatUserInfo(UserEntity user) {
    // 每次调用都创建新的DateFormat对象
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String formattedDate = sdf.format(user.getCreateTime());

    // 每次调用都创建新的StringBuilder
    StringBuilder sb = new StringBuilder();
    sb.append("User: ").append(user.getUsername())
      .append(", Email: ").append(user.getEmail())
      .append(", Created: ").append(formattedDate);

    return sb.toString();
}
```

**优化后（对象复用）**:

```java
@Component
public class UserInfoFormatter {

    // 使用ThreadLocal复用DateFormat（线程安全）
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT =
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    // 复用StringBuilder
    private static final ThreadLocal<StringBuilder> STRING_BUILDER =
        ThreadLocal.withInitial(StringBuilder::new);

    public String formatUserInfo(UserEntity user) {
        // 复用DateFormat
        SimpleDateFormat sdf = DATE_FORMAT.get();
        String formattedDate = sdf.format(user.getCreateTime());

        // 复用StringBuilder
        StringBuilder sb = STRING_BUILDER.get();
        sb.setLength(0);  // 清空内容
        sb.append("User: ").append(user.getUsername())
          .append(", Email: ").append(user.getEmail())
          .append(", Created: ").append(formattedDate);

        return sb.toString();
    }
}
```

**预期效果**: 临时对象创建减少60%

#### 2.2 使用对象池

**场景**: 频繁创建/销毁的大对象（如数据库连接、ByteBuffer等）

**示例**: 使用Apache Commons Pool2对象池

```java
@Configuration
public class ObjectPoolConfig {

    @Bean
    public GenericObjectPool<ByteBuffer> byteBufferPool() {
        GenericObjectPoolConfig<ByteBuffer> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(100);  // 最大对象数
        config.setMaxIdle(50);    // 最大空闲对象数
        config.setMinIdle(10);    // 最小空闲对象数
        config.setMaxWaitMillis(5000);  // 获取对象最大等待时间

        return new GenericObjectPool<>(new ByteBufferPooledObjectFactory(), config);
    }

    /**
     * ByteBuffer工厂
     */
    private static class ByteBufferPooledObjectFactory implements PooledObjectFactory<ByteBuffer> {
        @Override
        public PooledObject<ByteBuffer> makeObject() {
            ByteBuffer buffer = ByteBuffer.allocateDirect(8192);  // 8KB直接缓冲区
            return new DefaultPooledObject<>(buffer);
        }

        @Override
        public void passivateObject(PooledObject<ByteBuffer> p) {
            p.getObject().clear();  // 清空缓冲区
        }

        @Override
        public boolean validateObject(PooledObject<ByteBuffer> p) {
            return p.getObject() != null;
        }

        @Override
        public void destroyObject(PooledObject<ByteBuffer> p) {
            ByteBuffer buffer = p.getObject();
            if (buffer instanceof DirectBuffer) {
                ((DirectBuffer) buffer).cleaner().clean();  // 释放直接内存
            }
        }
    }
}
```

**使用对象池**:

```java
@Service
public class DataProcessService {

    @Resource
    private GenericObjectPool<ByteBuffer> byteBufferPool;

    public void processData(byte[] data) {
        ByteBuffer buffer = null;
        try {
            // 从对象池获取ByteBuffer
            buffer = byteBufferPool.borrowObject();

            // 使用buffer处理数据
            buffer.put(data);
            buffer.flip();
            // ... 处理逻辑

        } catch (Exception e) {
            log.error("[数据处理] 处理失败", e);
        } finally {
            if (buffer != null) {
                // 归还到对象池
                byteBufferPool.returnObject(buffer);
            }
        }
    }
}
```

**预期效果**: 大对象创建减少80%，内存分配降低40%

#### 2.3 使用基本类型而非包装类型

**优化前**:

```java
// 使用包装类型（占用更多内存，有自动装箱/拆箱开销）
List<Integer> userIds = new ArrayList<>();
for (int i = 0; i < 10000; i++) {
    userIds.add(Integer.valueOf(i));  // 每个Integer对象占用24字节
}

Map<String, Long> userMap = new HashMap<>();
userMap.put("user_1", 1L);  // Long对象占用24字节
```

**优化后**:

```java
// 使用基本类型数组
int[] userIds = new int[10000];
for (int i = 0; i < 10000; i++) {
    userIds[i] = i;  // 每个int占用4字节，节省83%内存
}

// 使用第三方库（如Eclipse Collections、HPPC）
IntLongMap userMap = new IntLongHashMap();
userMap.put(1, 1L);  // 只占用12字节
```

**预期效果**: 内存占用节省60-80%

### 3. 缓存内存优化

#### 3.1 限制缓存大小

**优化前（无限制缓存）**:

```java
@Component
public class UserCache {

    private final Map<Long, UserVO> cache = new HashMap<>();

    public void putUser(Long userId, UserVO user) {
        cache.put(userId, user);  // 无限制，可能导致OOM
    }

    public UserVO getUser(Long userId) {
        return cache.get(userId);
    }
}
```

**优化后（使用Caffeine限制大小）**:

```java
@Component
public class UserCache {

    private final Cache<Long, UserVO> cache;

    public UserCache() {
        this.cache = Caffeine.newBuilder()
            .maximumSize(10000)  // 最多缓存10000个用户
            .expireAfterWrite(30, TimeUnit.MINUTES)  // 30分钟过期
            .weakKeys()  // 使用弱引用Key
            .weakValues()  // 使用弱引用Value
            .recordStats()  // 记录统计信息
            .removalListener((key, value, cause) -> {
                log.info("[缓存移除] key={}, cause={}", key, cause);
            })
            .build();
    }

    public void putUser(Long userId, UserVO user) {
        cache.put(userId, user);
    }

    public UserVO getUser(Long userId) {
        return cache.getIfPresent(userId);
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStats getStats() {
        return cache.stats();
    }
}
```

**预期效果**: 缓存内存占用降低50%

#### 3.2 使用软引用/弱引用

**场景**: 大对象缓存（如图片、文档）

**优化前（强引用）**:

```java
@Component
public class ImageCache {

    private final Map<String, BufferedImage> cache = new ConcurrentHashMap<>();

    public void putImage(String key, BufferedImage image) {
        cache.put(key, image);  // 强引用，永远不会被GC
    }
}
```

**优化后（软引用）**:

```java
@Component
public class ImageCache {

    private final Map<String, SoftReference<BufferedImage>> cache = new ConcurrentHashMap<>();

    public void putImage(String key, BufferedImage image) {
        // 软引用：内存不足时会被GC回收
        cache.put(key, new SoftReference<>(image));
    }

    public BufferedImage getImage(String key) {
        SoftReference<BufferedImage> ref = cache.get(key);
        return ref != null ? ref.get() : null;
    }
}
```

**预期效果**: 内存不足时自动释放大对象，避免OOM

### 4. 集合优化

#### 4.1 初始化集合容量

**优化前（频繁扩容）**:

```java
// 默认容量10，扩容时需要复制数组
List<UserVO> users = new ArrayList<>();
for (int i = 0; i < 10000; i++) {
    users.add(user);  // 触发多次扩容，每次扩容复制数组
}

Map<Long, UserVO> userMap = new HashMap<>();
for (int i = 0; i < 10000; i++) {
    userMap.put(userId, user);  // 触发多次扩容
}
```

**优化后（指定初始容量）**:

```java
// 指定初始容量，避免扩容
List<UserVO> users = new ArrayList<>(10000);  // 初始容量10000
for (int i = 0; i < 10000; i++) {
    users.add(user);  // 无需扩容
}

// HashMap初始容量 = 预期元素数 / 负载因子(0.75) + 1
Map<Long, UserVO> userMap = new HashMap<>((int)(10000 / 0.75) + 1);
for (int i = 0; i < 10000; i++) {
    userMap.put(userId, user);  // 无需扩容
}
```

**预期效果**: 集合内存占用减少30%，性能提升20%

#### 4.2 使用更高效的集合实现

**场景1**: 大量基本类型数据

```java
// 使用Eclipse Collections或HPPC
IntIntMap map = new IntIntHashMap(10000);  // 比HashMap<Integer,Integer>节省60%内存
LongLongMap longMap = new LongLongHashMap(10000);
```

**场景2**: 只读集合

```java
// 使用不可变集合（节省内存，线程安全）
List<String> readOnlyList = List.of("item1", "item2", "item3");
Map<String, Integer> readOnlyMap = Map.of("key1", 1, "key2", 2);
Set<String> readOnlySet = Set.of("item1", "item2", "item3");
```

**场景3**: 枚举Set/Map

```java
// 使用EnumSet替代HashSet
enum Status { ACTIVE, INACTIVE, PENDING }

Set<Status> statusSet = EnumSet.of(Status.ACTIVE, Status.INACTIVE);  // 比HashSet节省80%内存

// 使用EnumMap替代HashMap
Map<Status, String> statusMap = new EnumMap<>(Status.class);  // 比HashMap节省70%内存
statusMap.put(Status.ACTIVE, "活跃");
```

**预期效果**: 集合内存占用减少50-80%

### 5. IO资源优化

#### 5.1 正确关闭资源（Try-With-Resources）

**优化前（资源泄露）**:

```java
public void readFile(String filePath) throws IOException {
    FileInputStream fis = new FileInputStream(filePath);
    BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

    String line;
    while ((line = reader.readLine()) != null) {
        // 处理逻辑
    }

    // 如果发生异常，资源可能未关闭
    fis.close();
    reader.close();
}
```

**优化后（自动关闭资源）**:

```java
public void readFile(String filePath) throws IOException {
    // try-with-resources自动关闭资源
    try (FileInputStream fis = new FileInputStream(filePath);
         BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {

        String line;
        while ((line = reader.readLine()) != null) {
            // 处理逻辑
        }
    }  // 自动调用close()，即使发生异常也会关闭
}
```

**预期效果**: 资源泄露清零，内存占用稳定

#### 5.2 使用NIO（零拷贝）

**优化前（传统IO）**:

```java
// 文件复制（需要多次数据拷贝）
public void copyFile(String src, String dest) throws IOException {
    try (FileInputStream fis = new FileInputStream(src);
         FileOutputStream fos = new FileOutputStream(dest)) {

        byte[] buffer = new byte[8192];  // 8KB缓冲区
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
        }
    }
}
// 数据流向: 文件 → 内核缓冲区 → 用户缓冲区 → 内核缓冲区 → 文件
// 内存占用: 8KB用户缓冲区
```

**优化后（NIO零拷贝）**:

```java
// 文件复制（零拷贝，数据不经过用户空间）
public void copyFile(String src, String dest) throws IOException {
    try (FileChannel srcChannel = FileChannel.open(Paths.get(src), StandardOpenOption.READ);
         FileChannel destChannel = FileChannel.open(Paths.get(dest),
             StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {

        // transferTo直接在内核空间传输数据
        srcChannel.transferTo(0, srcChannel.size(), destChannel);
    }
}
// 数据流向: 文件 → 内核缓冲区 → 文件（零拷贝）
// 内存占用: 0（无需用户缓冲区）
```

**预期效果**: 内存占用减少100%，性能提升50%

### 6. 内存泄漏排查

#### 6.1 常见内存泄漏场景

**场景1: 静态集合持有对象引用**

```java
// ❌ 错误示例
public class CacheManager {
    private static final Map<String, Object> CACHE = new HashMap<>();  // 静态集合永不释放

    public void cache(String key, Object value) {
        CACHE.put(key, value);  // 对象永远不会被GC
    }
}

// ✅ 正确示例（使用WeakHashMap）
public class CacheManager {
    private static final Map<String, Object> CACHE = new WeakHashMap<>();  // 弱引用

    public void cache(String key, Object value) {
        CACHE.put(key, value);  // 没有强引用时可以被GC
    }
}
```

**场景2: 未关闭的资源**

```java
// ❌ 错误示例
public class DatabaseService {
    private Connection connection;

    public void query() throws SQLException {
        connection = dataSource.getConnection();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM user");
        // 未关闭ResultSet、Statement、Connection
    }
}

// ✅ 正确示例（try-with-resources）
public class DatabaseService {
    public void query() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM user")) {
            // 自动关闭资源
        }
    }
}
```

**场景3: 监听器未注销**

```java
// ❌ 错误示例
public class EventManager {
    public void registerListener(EventListener listener) {
        eventBus.register(listener);  // 注册后未注销
    }
}

// ✅ 正确示例（显式注销）
public class EventManager {
    public void registerListener(EventListener listener) {
        eventBus.register(listener);
    }

    public void unregisterListener(EventListener listener) {
        eventBus.unregister(listener);  // 注销监听器
    }
}
```

#### 6.2 内存泄漏检测工具

**工具1: VisualVM**

```bash
# 启动VisualVM
visualvm

# 操作步骤:
# 1. 连接到目标应用
# 2. 查看"Profiler" → "Memory"
# 3. 点击"Heap Dump"生成堆转储
# 4. 查看" biggest objects"找出大对象
# 5. 查看"References"找出对象引用链
```

**工具2: MAT (Memory Analyzer Tool)**

```bash
# 1. 生成堆转储文件
jmap -dump:format=b,file=heapdump.hprof <pid>

# 2. 使用MAT分析
# 打开MAT → File → Open Heap Dump → heapdump.hprof

# 3. 查找内存泄漏
# - Leak Suspects: 自动检测疑似泄漏
# - Dominator Tree: 查看支配树
# - Histogram: 查看对象分布
# - Thread Overview: 查看线程栈
```

**工具3: Arthas在线诊断**

```bash
# 1. 安装Arthas
curl -O https://arthas.aliyun.com/arthas-boot.jar
java -jar arthas-boot.jar

# 2. 选择目标进程

# 3. 查看内存信息
[arthas]$ dashboard

# 4. 查看对象统计
[arthas]$ vmtool --action getInstances --className java.util.ArrayList --limit 10

# 5. 查看GC状态
[arthas]$ vmtool --action getInstances --className java.lang.String --limit 10

# 6. 生成堆转储
[arthas]$ heapdump /tmp/heapdump.hprof
```

### 7. GC日志分析

#### 7.1 启用GC日志

**JVM参数**:

```bash
# JDK 8
-XX:+PrintGCDetails \
-XX:+PrintGCDateStamps \
-XX:+PrintGCTimeStamps \
-Xloggc:/logs/gc.log

# JDK 11+ (推荐使用Unified Logging)
-Xlog:gc*:file=/logs/gc.log:time,tags:filecount=10,filesize=100m
```

**GC日志格式**:

```
[2025-12-26T10:30:45.123+0800] GC pause (G1 Evacuation Pause) (young), 0.0234567 secs
   [Parallel Time: 18.5 ms]
   [Eden: 512.0M(512.0M)->0.0B(512.0M) Survivors: 64.0M->64.0M Heap: 768.0M(1024.0M)->256.0M(1024.0M)]
   [Other: 4.2 ms]
   [Eden: 512.0M(512.0M)->0.0B(512.0M) Survivors: 64.0M->64.0M Heap: 768.0M(1024.0M)->256.0M(1024.0M)]
```

**解读**:
- `GC pause (young)`: Young GC，正常
- `0.0234567 secs`: GC停顿23ms（正常）
- `Eden: 512.0M->0.0B`: Eden区回收了512MB
- `Heap: 768.0M->256.0M`: 堆内存从768MB回收到256MB

#### 7.2 GC日志分析工具

**工具1: GCViewer**

```bash
# 下载GCViewer
wget https://github.com/chewiebug/GCViewer/releases/download/v1.3.9/gcviewer-1.3.9.jar

# 分析GC日志
java -jar gcviewer-1.3.9.jar /logs/gc.log

# 关键指标:
# - Throughput: GC吞吐量（应该>95%）
# - Pause: GC停顿时间（P95<500ms）
# - Heap: 堆内存使用趋势
# - GC Frequency: GC频率
```

**工具2: GCEasy**

```bash
# 在线分析: https://gceasy.io/

# 上传GC日志文件，自动分析:
# - GC性能评分
# - 建议的JVM参数优化
# - GC停顿时间分布
# - 内存泄漏检测
```

---

## 📊 性能验证

### 1. 内存使用监控

**Prometheus查询**:

```promql
# 堆内存使用率
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100

# 非堆内存使用率
jvm_memory_used_bytes{area="nonheap"} / jvm_memory_max_bytes{area="nonheap"} * 100

# GC频率（每分钟GC次数）
rate(jvm_gc_pause_seconds_count[1m]) * 60

# GC停顿时间（P95）
histogram_quantile(0.95, rate(jvm_gc_pause_seconds_bucket[5m]))

# Full GC频率
rate(jvm_gc_pause_seconds_count{gc="G1 Old Generation"}[1m]) * 60
```

### 2. 性能对比

**优化前**:

| 指标 | 数值 |
|------|------|
| 堆内存占用 | 2.5GB |
| Young GC频率 | 120次/小时 |
| Full GC频率 | 5次/天 |
| GC停顿时间(P95) | 1200ms |
| 对象创建速率 | 10000个/秒 |

**优化后预期**:

| 指标 | 数值 | 提升幅度 |
|------|------|----------|
| 堆内存占用 | 1.8GB | **28%↓** |
| Young GC频率 | 60次/小时 | **50%↓** |
| Full GC频率 | 1次/天 | **80%↓** |
| GC停顿时间(P95) | 500ms | **58%↑** |
| 对象创建速率 | 5000个/秒 | **50%↓** |

---

## 📋 实施检查清单

### 阶段1: JVM配置优化

- [ ] **堆内存配置**
  - [ ] 设置合理的-Xms和-Xmx（相同值）
  - [ ] 配置元空间大小
  - [ ] 配置新生代/老年代比例
  - [ ] 验证配置生效

- [ ] **GC配置优化**
  - [ ] 选择合适的垃圾收集器（G1GC）
  - [ ] 配置GC停顿目标
  - [ ] 配置并发GC触发阈值
  - [ ] 启用GC日志
  - [ ] 验证GC效果

### 阶段2: 代码优化

- [ ] **减少对象创建**
  - [ ] 复用DateFormat、NumberFormat等对象
  - [ ] 使用ThreadLocal复用线程本地对象
  - [ ] 使用StringBuilder替代字符串拼接
  - [ ] 验证对象创建数量降低

- [ ] **使用对象池**
  - [ ] 引入Commons Pool2
  - [ ] 配置对象池参数
  - [ ] 使用对象池管理大对象
  - [ ] 验证内存占用降低

- [ ] **集合优化**
  - [ ] 指定集合初始容量
  - [ ] 使用更高效的集合实现
  - [ ] 使用基本类型集合
  - [ ] 验证集合内存占用降低

- [ ] **IO资源优化**
  - [ ] 使用try-with-resources自动关闭资源
  - [ ] 使用NIO零拷贝
  - [ ] 验证资源泄露清零

### 阶段3: 缓存优化

- [ ] **限制缓存大小**
  - [ ] 使用Caffeine替代HashMap
  - [ ] 配置缓存最大容量
  - [ ] 配置过期策略
  - [ ] 验证缓存内存降低

- [ ] **使用弱引用/软引用**
  - [ ] 大对象使用SoftReference
  - [ ] 缓存使用WeakReference
  - [ ] 验证内存自动释放

### 阶段4: 内存泄漏排查

- [ ] **检测内存泄漏**
  - [ ] 使用VisualVM生成堆转储
  - [ ] 使用MAT分析堆转储
  - [ ] 使用Arthas在线诊断
  - [ ] 定位并修复内存泄漏

- [ ] **监控内存使用**
  - [ ] 配置Prometheus监控
  - [ ] 配置Grafana面板
  - [ ] 配置内存告警
  - [ ] 验证内存稳定

### 阶段5: 性能验证

- [ ] **功能测试**
  - [ ] 验证所有功能正常
  - [ ] 验证无资源泄露
  - [ ] 验证内存使用稳定

- [ ] **性能测试**
  - [ ] 执行内存压测
  - [ ] 收集GC日志
  - [ ] 分析GC性能
  - [ ] 验证内存占用降低30%

---

## 📚 附录

### A. JVM参数速查表

```bash
# === 堆内存配置 ===
-Xms1g                          # 初始堆内存1GB
-Xmx1g                          # 最大堆内存1GB
-XX:NewRatio=2                  # 新生代:老年代 = 1:2
-XX:SurvivorRatio=8             # Eden:S0:S1 = 8:1:1

# === 元空间配置 ===
-XX:MetaspaceSize=256m          # 元空间初始大小
-XX:MaxMetaspaceSize=512m       # 元空间最大大小

# === 线程栈配置 ===
-Xss512k                        # 线程栈大小512KB

# === GC配置 ===
-XX:+UseG1GC                    # 使用G1收集器
-XX:MaxGCPauseMillis=200        # 最大GC停顿200ms
-XX:G1HeapRegionSize=16m         # G1 Region大小16MB
-XX:InitiatingHeapOccupancyPercent=45  # 触发并发GC阈值45%

# === GC日志配置（JDK 11+）===
-Xlog:gc*:file=/logs/gc.log:time,tags:filecount=10,filesize=100m

# === OOM时生成堆转储 ===
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/logs/heapdump.hprof

# === 减少指针压缩（>32GB堆内存时禁用）===
-XX:+UseCompressedOops          # 启用指针压缩（<32GB堆内存）
-XX:+UseCompressedClassPointers # 启用类指针压缩
```

### B. 常用命令

```bash
# === 查看JVM进程 ===
jps -lvm

# === 查看堆内存使用 ===
jmap -heap <pid>

# === 查看GC状态 ===
jstat -gcutil <pid> 1000 10  # 每秒输出一次，共10次

# === 生成堆转储 ===
jmap -dump:format=b,file=heapdump.hprof <pid>

# === 查看线程栈 ===
jstack <pid>

# === 使用Arthas诊断 ===
java -jar arthas-boot.jar  # 启动Arthas
dashboard                   # 查看系统概况
thread                      # 查看线程信息
vmtool                      # 查看对象信息
heapdump /tmp/dump.hprof    # 生成堆转储
```

---

**文档版本**: v1.0
**最后更新**: 2025-12-26
**作者**: IOE-DREAM 性能优化小组
**状态**: ✅ 文档完成，待实施验证
