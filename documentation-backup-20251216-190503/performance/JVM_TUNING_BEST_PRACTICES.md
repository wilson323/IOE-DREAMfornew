# JVM性能调优最佳实践指南

## 📋 概述

本文档提供了IOE-DREAM智慧园区一卡通管理平台的JVM性能调优最佳实践，涵盖内存管理、垃圾回收、并发优化等关键方面。

## 🎯 调优目标

- **高并发支持**: 支持1000+ TPS的并发访问
- **低延迟**: 响应时间控制在100ms以内
- **高可用**: 99.9%的服务可用性
- **资源高效**: 合理的内存和CPU使用率

---

## 💾 内存管理优化

### 1. 堆内存配置原则

#### **Xms = Xmx (避免内存抖动)**
```bash
# ✅ 推荐配置
-Xms4g -Xmx4g

# ❌ 避免配置
-Xms2g -Xmx4g  # 容易导致内存抖动
```

#### **年轻代比例 (Xmn = Xmx × 0.25-0.3)**
```bash
# 对于8GB堆内存
-Xms8g -Xmx8g -Xmn2g  # 25% 年轻代

# 对于4GB堆内存
-Xms4g -Xmx4g -Xmn1g  # 25% 年轻代
```

#### **元空间配置**
```bash
# 根据类数量调整
-XX:MetaspaceSize=512m   # 中小型应用
-XX:MaxMetaspaceSize=1g   # 大型应用
```

### 2. 内存使用监控

#### **关键监控指标**
- **堆使用率**: < 85% (告警阈值)
- **元空间使用率**: < 90% (告警阈值)
- **直接内存使用率**: < 80%
- **内存碎片率**: < 10%

#### **内存分析工具**
```bash
# jstat -gcutil <pid> 5s
# jmap -histo:live <pid>
# jcmd <pid> GC.heap_info
```

---

## 🗑️ 垃圾回收优化

### 1. GC选择策略

#### **生产环境推荐: G1GC**
```bash
# G1GC配置
-XX:+UseG1GC
-XX:MaxGCPauseMillis=100     # 目标暂停时间
-XX:G1HeapRegionSize=16m       # 区域大小
-XX:+UnlockExperimentalVMOptions -XX:+UseStringDeduplication
```

#### **G1GC关键参数**
```bash
-XX:MaxGCPauseMillis=200      # 最大GC暂停时间
-XX:G1MixedGCCountTarget=8      # 混合GC目标次数
-XX:G1OldGCCountTarget=4       # 老年代GC目标次数
-XX:G1NewSizePercent=30        # 年轻代比例
-XX:G1MaxNewSizePercent=40      # 最大年轻代比例
```

### 2. GC性能监控

#### **关键指标**
- **GC暂停时间**: < 200ms (95%分位)
- **GC频率**: < 5次/分钟
- **GC吞吐量**: > 99%

#### **GC日志分析**
```bash
# GC日志配置
-Xloggc:/var/log/app/gc.log
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+PrintGCDateStamps
-XX:+PrintGCCause
-XX:+UseGCLogFileRotation
-XX:NumberOfGCLogFiles=10
-XX:GCLogFileSize=10M
```

---

## 🧵 并发性能优化

### 1. 线程池配置

#### **业务线程池**
```yaml
spring:
  task:
    execution:
      pool:
        core-size: 50
        max-size: 200
        queue-capacity: 1000
        keep-alive: 60s
```

#### **异步线程池**
```yaml
spring:
  async:
    executor:
      pool:
        core-size: 20
        max-size: 100
        queue-capacity: 500
        keep-alive: 30s
```

### 2. 锁优化策略

#### **锁粒度优化**
```java
// ✅ 细粒度锁
private final Object lock = new Object();

// ❌ 粗粒度锁
private final ReentrantLock lock = new ReentrantLock();
```

#### **锁超时配置**
```java
// 使用tryLock避免死锁
if (lock.tryLock(5, TimeUnit.SECONDS)) {
    try {
        // 业务逻辑
    } finally {
        lock.unlock();
    }
}
```

---

## ⚡ JIT编译优化

### 1. 编译器配置

#### **分层编译**
```bash
-XX:+TieredCompilation
-XX:TieredStopAtLevel=1  # 快速启动
-XX:CompileThreshold=1500
```

#### **热点代码编译**
```bash
-XX:CompileCommand=exclude,java/lang/String
-XX:CompileCommand=exclude,java/util/ArrayList
```

### 2. 代码优化技巧

#### **减少对象创建**
```java
// ✅ 对象池
private static final ObjectPool<MyObject> pool = new ObjectPool<>();

// ❌ 频繁创建对象
public void process() {
    MyObject obj = new MyObject();  // 频繁创建
}
```

#### **字符串优化**
```java
// ✅ 使用StringBuilder
StringBuilder sb = new StringBuilder();

// ❌ 字符串拼接
String result = "hello" + "world";
```

---

## 🔧 环境特定配置

### 1. 开发环境

```bash
# 快速启动，较小内存
-Xms512m -Xmx1024m -Xmn256m
-XX:+TieredCompilation
-XX:CompileThreshold=1000
```

### 2. 测试环境

```bash
# 中等配置，兼顾性能和资源
-Xms2048m -Xmx4096m -Xmn1024m
-XX:+UseG1GC
-XX:MaxGCPauseMillis=150
```

### 3. 生产环境

```bash
# 高性能配置，优化参数
-Xms4096m -Xmx8192m -Xmn2048m
-XX:+UseG1GC
-XX:MaxGCPauseMillis=100
-XX:+UnlockExperimentalVMOptions -XX:+UseStringDeduplication
-XX:+PrintGCDetails -Xloggc:/var/log/app/gc.log
```

---

## 📊 性能监控和分析

### 1. 关键指标监控

#### **内存指标**
```java
// 堆内存使用率
MemoryUsage heapUsage = MemoryMXBean.getMemoryMXBean().getHeapMemoryUsage();
double heapUsagePercent = (double) heapUsage.getUsed() / heapUsage.getMax() * 100;

// GC统计
List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
long totalGcTime = gcBeans.stream().mapToLong(GarbageCollectorMXBean::getCollectionTime).sum();
```

#### **线程指标**
```java
// 线程状态统计
ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
ThreadInfo[] threads = threadBean.getAllThreadIds();
Map<Thread.State, Integer> stateCount = new HashMap<>();
```

### 2. 性能分析工具

#### **JFR (Java Flight Recorder)**
```bash
# 启用JFR
-XX:+StartFlightRecording
-XX:FlightRecorderOptions=settings=profile
-XX:UnlockExperimentalVMOptions -XX:+FlightRecorder
```

#### **异步性能分析**
```java
// 使用CompletableFuture进行异步编程
CompletableFuture.supplyAsync(() -> {
    // 异步任务
    return heavyOperation();
}).thenAccept(result -> {
    // 处理结果
});
```

---

## 🚨 常见问题和解决方案

### 1. 内存溢出 (OutOfMemoryError)

#### **问题分析**
- **堆内存溢出**: 对象创建过多或引用未释放
- **元空间溢出**: 类加载过多或动态代理过多
- **直接内存溢出**: NIO缓冲区分配过多

#### **解决方案**
```bash
# 增加堆内存
-Xmx8192m

# 分析内存泄漏
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/log/app/dumps/

# 元空间调优
-XX:MetaspaceSize=1024m
-XX:MaxMetaspaceSize=2048m
```

### 2. GC暂停时间过长

#### **问题分析**
- **大对象**: 单个对象占用内存过大
- **内存碎片**: 碎片过多导致回收困难
- **GC参数不当**: 不适合当前场景的GC配置

#### **解决方案**
```bash
# 调整GC参数
-XX:MaxGCPauseMillis=100
-XX:G1HeapRegionSize=32m
-XX:G1MixedGCCountTarget=4
```

### 3. 线程阻塞和死锁

#### **问题分析**
- **锁竞争**: 多个线程竞争同一个锁
- **死锁**: 线程相互等待对方释放资源
- **资源不足**: 线程数量过多

#### **解决方案**
```bash
# 线程栈大小调整
-Xss1m

# 启用死锁检测
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+PrintGCApplicationStoppedTime
```

---

## 📈 持续优化建议

### 1. 定期性能评估

- **每周**: 性能指标回顾
- **每月**: 性能趋势分析
- **每季度**: JVM参数调优

### 2. 压力测试策略

- **负载测试**: 模拟高并发场景
- **稳定性测试**: 长时间运行测试
- **容量规划**: 基于历史数据规划资源

### 3. 性能基准建立

- **建立基准**: 确定性能基线
- **监控告警**: 设置合理的告警阈值
- **自动化监控**: 集成到CI/CD流程

---

## 📚 参考资源

- [Oracle官方JVM调优指南](https://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/)
- [G1垃圾收集器文档](https://docs.oracle.com/javase/9/gctuning/g1-gc.htm)
- [Java性能调优实践](https://www.oracle.com/technetwork/java/javase/performance-tuning-139431.html)

---

**💡 核心原则**: 性能调优是一个持续的过程，需要根据实际业务需求和运行环境不断调整和优化。建议在调优过程中遵循"测试-调优-验证"的循环方法。