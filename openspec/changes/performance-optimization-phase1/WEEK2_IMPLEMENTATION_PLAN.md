# 第2周实施计划：并发和连接池优化

**周期**: Week 2 (Day 6-10)
**负责人**: 后端团队 + DBA团队 + 运维团队
**预期目标**: TPS从800提升到2000+，支持1000+并发用户
**涉及文档**: P1-7.3 CONNECTION_POOL_UNIFICATION_GUIDE.md, P1-7.8 CONCURRENCY_OPTIMIZATION_GUIDE.md

---

## 📋 周目标概览

| 指标 | 当前值 | 目标值 | 提升幅度 |
|------|--------|--------|----------|
| **TPS** | 800 | 2000+ | 150% ↑ |
| **并发用户** | 300 | 1000+ | 233% ↑ |
| **响应时间** | 800ms | <400ms | 50% ↓ |
| **连接池监控覆盖率** | 30% | 100% | 233% ↑ |

---

## 📅 Day 6: Druid连接池统一迁移

### 任务目标
将所有微服务的HikariCP连接池替换为Druid，建立统一的SQL监控体系。

### 6.1 上午：依赖管理和统一配置类

**步骤1**: 添加Druid依赖（parent pom.xml）

```xml
<properties>
    <druid.version>1.2.25</druid.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-3-starter</artifactId>
            <version>${druid.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

**步骤2**: 移除HikariCP依赖

```bash
# 在各个微服务的pom.xml中移除
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
</dependency>
```

**步骤3**: 创建统一Druid配置类

**文件位置**: `microservices/microservices-common-data/src/main/java/net/lab1024/sa/common/config/DruidDataSourceConfig.java`

```java
package net.lab1024.sa.common.config;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.filter.wall.WallConfig;
import com.alibaba.druid.filter.wall.WallFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.List;

/**
 * Druid数据源统一配置
 *
 * 核心特性：
 * 1. SQL监控：StatFilter记录所有SQL执行情况
 * 2. 防SQL注入：WallFilter防止SQL注入攻击
 * 3. 连接泄漏检测：自动检测未关闭的连接
 * 4. 慢SQL记录：超过1秒的SQL自动记录
 */
@Slf4j
@Configuration
public class DruidDataSourceConfig {

    /**
     * 配置Druid数据源
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    @Primary
    public DataSource druidDataSource() {
        log.info("[数据源配置] 初始化Druid连接池");

        DruidDataSource dataSource = new DruidDataSource();

        // 配置StatFilter（SQL监控）
        StatFilter statFilter = new StatFilter();
        statFilter.setLogSlowSql(true);           // 记录慢SQL
        statFilter.setSlowSqlMillis(1000);        // 慢SQL阈值：1秒
        statFilter.setMergeSql(true);             // 合并相同SQL
        log.info("[数据源配置] StatFilter配置完成：慢SQL阈值=1000ms");

        // 配置WallFilter（防SQL注入）
        WallFilter wallFilter = new WallFilter();
        wallFilter.setConfig(wallConfig());
        wallFilter.setLogViolation(true);         // 记录违反规则
        wallFilter.setThrowException(false);      // 不抛异常（仅记录）
        log.info("[数据源配置] WallFilter配置完成：防SQL注入启用");

        // 添加过滤器
        List<javax.servlet.Filter> filters = Lists.newArrayList();
        filters.add(statFilter);
        filters.add(wallFilter);
        dataSource.setProxyFilters(filters);

        // 连接泄漏检测
        dataSource.setRemoveAbandoned(true);              // 开启泄漏检测
        dataSource.setRemoveAbandonedTimeout(1800);       // 30分钟未归还视为泄漏
        dataSource.setLogAbandoned(true);                 // 记录泄漏堆栈

        log.info("[数据源配置] 连接泄漏检测启用：超时时间=1800秒");
        log.info("[数据源配置] Druid连接池初始化完成");

        return dataSource;
    }

    /**
     * WallFilter配置（防SQL注入规则）
     */
    @Bean
    public WallConfig wallConfig() {
        WallConfig config = new WallConfig();
        config.setMultiStatementAllow(true);     // 允许多条SQL
        config.setNoneBaseStatementAllow(true);  // 允许没有where条件的语句
        config.setStrictSyntaxCheck(false);      // 宽松语法检查
        return config;
    }
}
```

**步骤4**: 配置文件更新（application.yml）

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      # 基础配置
      url: jdbc:mysql://localhost:3306/ioedream?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
      username: root
      password: encrypted_password_here
      driver-class-name: com.mysql.cj.jdbc.Driver

      # 连接池大小配置
      initial-size: 10              # 初始连接数
      min-idle: 10                  # 最小空闲连接
      max-active: 100               # 最大活动连接（开发环境）
      max-wait: 60000               # 获取连接最大等待时间（60秒）

      # 连接健康检查
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false

      # 连接回收配置
      time-between-eviction-runs-millis: 60000    # 检查间隔（1分钟）
      min-evictable-idle-time-millis: 300000      # 最小生存时间（5分钟）
      max-evictable-idle-time-millis: 900000      # 最大生存时间（15分钟）

      # 监控统计配置
      filter:
        stat:
          enabled: true
          log-slow-sql: true
          slow-sql-millis: 1000
          merge-sql: true
        wall:
          enabled: true
          config:
            multi-statement-allow: true
        slf4j:
          enabled: true
          statement-log-enabled: true

      # Web监控配置（开发环境）
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        reset-enable: false
        login-username: admin
        login-password: admin123

      # Web-JDBC关联监控
      web-stat-filter:
        enabled: true
        url-pattern: /*
        exclusions: "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*"
```

### 6.2 下午：微服务迁移和验证

**迁移清单**（按优先级）：

```powershell
# 1. 核心服务优先
1. ioedream-common-service       # 公共业务服务
2. ioedream-gateway-service      # 网关服务
3. ioedream-access-service       # 门禁服务
4. ioedream-attendance-service   # 考勤服务
5. ioedream-consume-service      # 消费服务
6. ioedream-visitor-service      # 访客服务
7. ioedream-video-service        # 视频服务
```

**验证步骤**：

```bash
# 1. 编译验证
cd D:\IOE-DREAM
mvn clean compile -pl microservices/ioedream-common-service -am

# 2. 启动服务
cd microservices/ioedream-common-service
mvn spring-boot:run

# 3. 访问Druid监控页面
# 浏览器打开：http://localhost:8088/druid/index.html
# 用户名：admin
# 密码：admin123

# 4. 检查SQL监控
# 应该能看到：
# - SQL执行次数
# - SQL执行时间
# - 慢SQL列表
# - 连接池状态
```

**验收标准**：
- ✅ 所有服务编译成功
- ✅ Druid监控页面可访问
- ✅ SQL监控正常记录
- ✅ 无连接泄漏警告

---

## 📅 Day 7: 异步处理和线程池优化

### 任务目标
使用CompletableFuture实现异步处理，优化线程池配置，提升吞吐量。

### 7.1 上午：异步处理框架搭建

**步骤1**: 创建异步线程池配置

**文件位置**: `microservices/microservices-common-core/src/main/java/net/lab1024/sa/common/config/AsyncExecutorConfig.java`

```java
package net.lab1024.sa.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步线程池配置
 *
 * 线程池参数说明：
 * - corePoolSize: 核心线程数（常驻线程）
 * - maxPoolSize: 最大线程数（高峰期扩展）
 * - queueCapacity: 队列容量（未执行任务缓冲）
 * - keepAliveSeconds: 空闲线程存活时间
 */
@Slf4j
@EnableAsync
@Configuration
public class AsyncExecutorConfig {

    /**
     * 业务异步线程池
     *
     * 适用场景：
     * - RPC调用（设备服务、用户服务）
     * - 数据库查询（异步DAO操作）
     * - 消息发送（通知、告警）
     */
    @Bean("asyncServiceExecutor")
    public Executor asyncServiceExecutor() {
        log.info("[线程池配置] 初始化异步业务线程池");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数 = CPU核心数 * 2
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors() * 2);
        log.info("[线程池配置] 核心线程数={}", executor.getCorePoolSize());

        // 最大线程数 = CPU核心数 * 4
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 4);
        log.info("[线程池配置] 最大线程数={}", executor.getMaxPoolSize());

        // 队列容量 = 500
        executor.setQueueCapacity(500);
        log.info("[线程池配置] 队列容量=500");

        // 空闲线程存活时间 = 60秒
        executor.setKeepAliveSeconds(60);

        // 线程名称前缀
        executor.setThreadNamePrefix("async-service-");

        // 拒绝策略：由调用线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.info("[线程池配置] 异步业务线程池初始化完成");
        return executor;
    }

    /**
     * 设备通信专用线程池
     *
     * 特点：
     * - 高并发I/O操作
     * - 较大队列容量
     * - 独立线程池避免阻塞业务线程
     */
    @Bean("deviceCommExecutor")
    public Executor deviceCommExecutor() {
        log.info("[线程池配置] 初始化设备通信线程池");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 设备通信需要更多线程处理I/O等待
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("device-comm-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.info("[线程池配置] 设备通信线程池初始化完成：core=20, max=50, queue=1000");
        return executor;
    }

    /**
     * 数据库操作专用线程池
     *
     * 特点：
     * - 专注于数据库查询/写入
     * - 中等队列容量
     * - 避免数据库连接池耗尽
     */
    @Bean("dbExecutor")
    public Executor dbExecutor() {
        log.info("[线程池配置] 初始化数据库操作线程池");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 数据库操作线程数适中
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("db-operation-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.info("[线程池配置] 数据库操作线程池初始化完成：core=10, max=20, queue=200");
        return executor;
    }
}
```

**步骤2**: 实现异步Service示例

**文件位置**: `microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/service/impl/AccessServiceImpl.java`

```java
package net.lab1024.sa.access.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.vo.AccessResultVO;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.gateway.client.GatewayServiceClient;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

/**
 * 门禁服务实现 - 异步处理版本
 */
@Slf4j
@Service
public class AccessServiceImpl {

    @Resource
    private DeviceDao deviceDao;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 异步门禁验证（核心优化方法）
     *
     * 优化点：
     * 1. 异步查询设备信息（不阻塞主线程）
     * 2. 异步调用设备服务（并行处理）
     * 3. 异步保存通行记录（非阻塞）
     * 4. 快速返回结果（提升响应速度）
     *
     * @param userId  用户ID
     * @param deviceId 设备ID
     * @return CompletableFuture包装的验证结果
     */
    public CompletableFuture<ResponseDTO<AccessResultVO>> verifyAccessAsync(Long userId, String deviceId) {
        log.info("[门禁服务] 开始异步验证: userId={}, deviceId={}", userId, deviceId);

        // 步骤1：异步查询设备信息（使用dbExecutor）
        CompletableFuture<DeviceEntity> deviceFuture = CompletableFuture.supplyAsync(() -> {
            log.debug("[门禁服务] 异步查询设备信息: deviceId={}", deviceId);
            DeviceEntity device = deviceDao.selectById(deviceId);
            log.debug("[门禁服务] 设备信息查询完成: deviceName={}", device.getDeviceName());
            return device;
        }, getDbExecutor());

        // 步骤2：异步调用设备服务验证（使用deviceCommExecutor）
        CompletableFuture<ResponseDTO<Boolean>> deviceVerifyFuture = deviceFuture.thenComposeAsync(device -> {
            log.debug("[门禁服务] 异步调用设备服务: deviceId={}, deviceName={}", deviceId, device.getDeviceName());

            // 构建验证请求
            VerifyRequest request = buildVerifyRequest(userId, device);

            // 异步调用设备服务
            return gatewayServiceClient.callDeviceServiceAsync(
                "/api/device/verify",
                HttpMethod.POST,
                request,
                Boolean.class
            );
        }, getDeviceCommExecutor());

        // 步骤3：异步保存通行记录（非阻塞，不等待完成）
        deviceVerifyFuture.thenAcceptAsync(deviceResponse -> {
            if (deviceResponse.getData() != null && deviceResponse.getData()) {
                log.debug("[门禁服务] 异步保存通行记录: userId={}, deviceId={}", userId, deviceId);

                AccessRecordEntity record = buildAccessRecord(userId, deviceId);
                accessRecordDao.insertAsync(record);

                log.debug("[门禁服务] 通行记录保存完成: recordId={}", record.getRecordId());
            }
        }, getDbExecutor());

        // 步骤4：快速返回结果（不等待保存完成）
        return deviceVerifyFuture.thenApply(deviceResponse -> {
            log.info("[门禁服务] 异步验证完成: userId={}, result={}", userId, deviceResponse.getData());

            AccessResultVO result = buildResult(deviceResponse.getData());
            return ResponseDTO.ok(result);
        });
    }

    /**
     * 批量异步验证（高并发场景）
     *
     * 适用场景：早高峰批量打卡验证
     *
     * @param verifyRequests 批量验证请求
     * @return CompletableFuture包装的批量验证结果
     */
    public CompletableFuture<List<ResponseDTO<AccessResultVO>>> batchVerifyAccessAsync(
            List<AccessVerifyRequest> verifyRequests) {

        log.info("[门禁服务] 批量异步验证开始: count={}", verifyRequests.size());

        // 并行处理所有验证请求
        List<CompletableFuture<ResponseDTO<AccessResultVO>>> futures = verifyRequests.stream()
            .map(request -> verifyAccessAsync(request.getUserId(), request.getDeviceId()))
            .collect(Collectors.toList());

        // 等待所有任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );

        // 收集结果
        return allFutures.thenApply(v -> {
            log.info("[门禁服务] 批量异步验证完成: count={}", verifyRequests.size());
            return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        });
    }

    // 辅助方法
    private VerifyRequest buildVerifyRequest(Long userId, DeviceEntity device) {
        VerifyRequest request = new VerifyRequest();
        request.setUserId(userId);
        request.setDeviceId(device.getDeviceId());
        request.setDeviceCode(device.getDeviceCode());
        return request;
    }

    private AccessRecordEntity buildAccessRecord(Long userId, String deviceId) {
        AccessRecordEntity record = new AccessRecordEntity();
        record.setUserId(userId);
        record.setDeviceId(deviceId);
        record.setAccessTime(LocalDateTime.now());
        return record;
    }

    private AccessResultVO buildResult(Boolean success) {
        AccessResultVO result = new AccessResultVO();
        result.setSuccess(success);
        result.setMessage(success ? "验证通过" : "验证失败");
        result.setAccessTime(LocalDateTime.now());
        return result;
    }

    @Resource(name = "dbExecutor")
    private Executor dbExecutor;

    @Resource(name = "deviceCommExecutor")
    private Executor deviceCommExecutor;

    private Executor getDbExecutor() {
        return dbExecutor;
    }

    private Executor getDeviceCommExecutor() {
        return deviceCommExecutor;
    }
}
```

### 7.2 下午：性能测试和调优

**性能测试脚本**（JMeter）

**文件位置**: `openspec/changes/performance-optimization-phase1/tests/async-access-test.jmx`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="异步门禁验证性能测试">
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments">
        <collectionProp name="Arguments.arguments">
          <elementProp name="BASE_URL" elementType="Argument">
            <stringProp name="Argument.name">BASE_URL</stringProp>
            <stringProp name="Argument.value">http://localhost:8090</stringProp>
          </elementProp>
          <elementProp name="THREAD_COUNT" elementType="Argument">
            <stringProp name="Argument.name">THREAD_COUNT</stringProp>
            <stringProp name="Argument.value">500</stringProp>
          </elementProp>
        </collectionProp>
      </elementProp>
    </TestPlan>
    <hashTree>
      <!-- 线程组：模拟500并发用户 -->
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="并发用户组">
        <stringProp name="ThreadGroup.num_threads">${THREAD_COUNT}</stringProp>
        <stringProp name="ThreadGroup.ramp_time">60</stringProp>
        <longProp name="ThreadGroup.duration">300</longProp>
      </ThreadGroup>
      <hashTree>
        <!-- HTTP请求：异步门禁验证 -->
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="异步门禁验证">
          <stringProp name="HTTPSampler.domain">${BASE_URL}</stringProp>
          <stringProp name="HTTPSampler.port">8090</stringProp>
          <stringProp name="HTTPSampler.path">/api/v1/access/verify/async</stringProp>
          <stringProp name="HTTPSampler.method">POST</stringProp>
          <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
        </HTTPSamplerProxy>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

**性能测试执行**：

```bash
# 1. 启动服务
cd D:\IOE-DREAM\microservices\ioedream-access-service
mvn spring-boot:run

# 2. 执行JMeter测试
cd D:\IOE-DREAM\openspec\changes\performance-optimization-phase1\tests
jmeter -n -t async-access-test.jmx -l result.jtl -e -o report/

# 3. 查看测试报告
# 浏览器打开：report/index.html
```

**验收标准**：
- ✅ TPS ≥ 1500（目标2000）
- ✅ 平均响应时间 < 500ms（目标<400ms）
- ✅ 99%请求响应时间 < 1000ms
- ✅ 无线程池拒绝错误
- ✅ 无内存泄漏

---

## 📅 Day 8: Tomcat线程池优化

### 任务目标
优化Tomcat线程池配置，提升HTTP请求处理能力。

### 8.1 上午：Tomcat线程池配置

**配置文件更新**（application.yml）

```yaml
server:
  port: 8090
  tomcat:
    # 线程池配置
    threads:
      max: 800              # 最大工作线程数（从200增加到800）
      min-spare: 100        # 最小空闲线程数（从10增加到100）

    # 连接配置
    accept-count: 1000      # 等待队列长度（从100增加到1000）
    max-connections: 10000  # 最大连接数（从8192增加到10000）

    # 连接超时配置
    connection-timeout: 20000  # 连接超时：20秒
    keep-alive-timeout: 60000  # Keep-Alive超时：60秒

    # 优化配置
    max-http-form-post-size: 10MB  # POST请求大小限制
    additional-tld-skip-patterns: '*.jar'  # 跳过JAR扫描

  # 压缩配置
  compression:
    enabled: true
    mime-types: text/html,text/xml,text/plain,text/css,application/javascript,application/json
    min-response-size: 1024  # 最小压缩大小：1KB
```

**性能调优说明**：

| 参数 | 默认值 | 优化值 | 说明 |
|------|--------|--------|------|
| `threads.max` | 200 | 800 | 最大工作线程数，支持更多并发请求 |
| `threads.min-spare` | 10 | 100 | 最小空闲线程，减少线程创建开销 |
| `accept-count` | 100 | 1000 | 等待队列长度，避免拒绝连接 |
| `max-connections` | 8192 | 10000 | 最大连接数，提升并发处理能力 |

### 8.2 下午：压力测试和验证

**压力测试命令**：

```bash
# 使用wrk进行压力测试
wrk -t12 -c400 -d30s --latency http://localhost:8090/api/v1/access/verify/async

# 参数说明：
# -t12: 12个线程
# -c400: 400个并发连接
# -d30s: 持续30秒
# --latency: 显示延迟统计
```

**验收标准**：
- ✅ 支持400并发连接
- ✅ 平均响应时间 < 300ms
- ✅ 无连接拒绝错误
- ✅ CPU使用率 < 80%

---

## 📅 Day 9: 限流和熔断机制

### 任务目标
集成Sentinel实现限流和熔断，保护系统稳定性。

### 9.1 上午：Sentinel集成

**步骤1**: 添加Sentinel依赖

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-sentinel-datasource-nacos</artifactId>
</dependency>
```

**步骤2**: Sentinel配置（application.yml）

```yaml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8080  # Sentinel控制台地址
        port: 8719                # 客户端监控API端口
      datasource:
        flow:
          nacos:
            server-addr: localhost:8848
            data-id: ${spring.application.name}-flow-rules
            group-id: SENTINEL_GROUP
            rule-type: flow

      # 限流配置
      eager: true                 # 启动时立即初始化
      filter:
        url-patterns: /**         # 对所有URL生效

      # 熔断配置
      block-page: /blocked        # 被拦截时的跳转页面
```

**步骤3**: 限流规则配置（Nacos）

**Data ID**: `ioedream-access-service-flow-rules`

```json
[
  {
    "resource": "/api/v1/access/verify",
    "limitApp": "default",
    "grade": 1,
    "count": 1000,
    "strategy": 0,
    "controlBehavior": 0,
    "clusterMode": false
  },
  {
    "resource": "/api/v1/access/verify/async",
    "limitApp": "default",
    "grade": 1,
    "count": 2000,
    "strategy": 0,
    "controlBehavior": 0,
    "clusterMode": false
  }
]
```

**规则说明**：
- `grade=1`: QPS限流
- `count`: 阈值（同步接口1000 QPS，异步接口2000 QPS）
- `strategy=0`: 直接拒绝
- `controlBehavior=0`: 快速失败

**步骤4**: 熔断规则配置

**Data ID**: `ioedream-access-service-degrade-rules`

```json
[
  {
    "resource": "/api/v1/access/verify/async",
    "grade": 0,
    "count": 50,
    "timeWindow": 10,
    "minRequestAmount": 10,
    "statIntervalMs": 1000,
    "slowRatioThreshold": 0.5
  }
]
```

**规则说明**：
- `grade=0`: 慢调用比例熔断
- `count=50`: 慢调用阈值（50ms）
- `slowRatioThreshold=0.5`: 慢调用比例50%时熔断
- `timeWindow=10`: 熔断时长10秒

### 9.2 下午：自定义异常处理

**文件位置**: `microservices/ioedream-access-service/src/main/java/net/lab1024/sa/access/config/SentinelExceptionHandler.java`

```java
package net.lab1024.sa.access.config;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Sentinel异常统一处理
 */
@Slf4j
@RestControllerAdvice
public class SentinelExceptionHandler {

    /**
     * 限流异常处理
     */
    @ExceptionHandler(FlowException.class)
    public ResponseDTO<Void> handleFlowException(FlowException e) {
        log.warn("[限流拦截] 接口限流触发: rule={}", e.getRule());
        return ResponseDTO.userError("429", "系统繁忙，请稍后再试");
    }

    /**
     * 熔断降级异常处理
     */
    @ExceptionHandler(DegradeException.class)
    public ResponseDTO<Void> handleDegradeException(DegradeException e) {
        log.warn("[熔断降级] 服务降级触发: rule={}", e.getRule());
        return ResponseDTO.userError("503", "服务暂时不可用，请稍后重试");
    }

    /**
     * 权限控制异常处理
     */
    @ExceptionHandler(AuthorityException.class)
    public ResponseDTO<Void> handleAuthorityException(AuthorityException e) {
        log.warn("[权限控制] 权限校验失败: rule={}", e.getRule());
        return ResponseDTO.userError("403", "无权访问");
    }

    /**
     * 通用BlockException处理
     */
    @ExceptionHandler(BlockException.class)
    public ResponseDTO<Void> handleBlockException(BlockException e) {
        log.error("[Sentinel异常] 未知拦截异常: {}", e.getMessage());
        return ResponseDTO.userError("429", "系统繁忙，请稍后再试");
    }
}
```

**验收标准**：
- ✅ Sentinel控制台可访问
- ✅ 限流规则生效
- ✅ 熔断规则生效
- ✅ 自定义异常处理正常

---

## 📅 Day 10: 分布式锁和综合测试

### 任务目标
集成Redisson分布式锁，执行综合性能测试。

### 10.1 上午：Redisson分布式锁

**步骤1**: 添加Redisson依赖

```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.27.2</version>
</dependency>
```

**步骤2**: Redisson配置

**文件位置**: `microservices/microservices-common-cache/src/main/java/net/lab1024/sa/common/config/RedissonConfig.java`

```java
package net.lab1024.sa.common.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson分布式锁配置
 */
@Slf4j
@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        log.info("[Redisson配置] 初始化Redisson客户端");

        String address = "redis://" + redisHost + ":" + redisPort;
        log.info("[Redisson配置] Redis地址: {}", address);

        Config config = new Config();
        config.useSingleServer()
              .setAddress(address)
              .setPassword(redisPassword.isEmpty() ? null : redisPassword)
              .setDatabase(0)
              .setConnectionPoolSize(64)
              .setConnectionMinimumIdleSize(10)
              .setIdleConnectionTimeout(10000)
              .setConnectTimeout(10000)
              .setTimeout(3000)
              .setRetryAttempts(3)
              .setRetryInterval(1500);

        RedissonClient redissonClient = Redisson.create(config);
        log.info("[Redisson配置] Redisson客户端初始化完成");

        return redissonClient;
    }
}
```

**步骤3**: 分布式锁工具类

**文件位置**: `microservices/microservices-common-cache/src/main/java/net/lab1024/sa/common/util/DistributedLockUtil.java`

```java
package net.lab1024.sa.common.util;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁工具类
 */
@Slf4j
@Component
public class DistributedLockUtil {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 尝试获取锁
     *
     * @param lockKey 锁的键
     * @param waitTime 等待时间（秒）
     * @param leaseTime 自动释放时间（秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime) {
        log.debug("[分布式锁] 尝试获取锁: lockKey={}, waitTime={}s, leaseTime={}s",
                  lockKey, waitTime, leaseTime);

        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (acquired) {
                log.info("[分布式锁] 锁获取成功: lockKey={}", lockKey);
            } else {
                log.warn("[分布式锁] 锁获取失败: lockKey={}", lockKey);
            }
            return acquired;
        } catch (InterruptedException e) {
            log.error("[分布式锁] 获取锁异常: lockKey={}, error={}", lockKey, e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 释放锁
     *
     * @param lockKey 锁的键
     */
    public void unlock(String lockKey) {
        log.debug("[分布式锁] 释放锁: lockKey={}", lockKey);

        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.info("[分布式锁] 锁释放成功: lockKey={}", lockKey);
        } else {
            log.warn("[分布式锁] 当前线程未持有锁: lockKey={}", lockKey);
        }
    }

    /**
     * 执行带锁的业务
     *
     * @param lockKey 锁的键
     * @param waitTime 等待时间（秒）
     * @param leaseTime 自动释放时间（秒）
     * @param业务逻辑
     * @return 执行结果
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime,
                                  LockedBusiness<T> business) {
        boolean locked = false;
        try {
            locked = tryLock(lockKey, waitTime, leaseTime);
            if (!locked) {
                throw new RuntimeException("获取锁失败: " + lockKey);
            }
            return business.execute();
        } finally {
            if (locked) {
                unlock(lockKey);
            }
        }
    }

    @FunctionalInterface
    public interface LockedBusiness<T> {
        T execute();
    }
}
```

**步骤4**: 业务代码中使用分布式锁

```java
@Service
public class AccessServiceImpl {

    @Resource
    private DistributedLockUtil lockUtil;

    /**
     * 防重复门禁验证（分布式锁示例）
     */
    public ResponseDTO<AccessResultVO> verifyAccessWithLock(Long userId, String deviceId) {
        String lockKey = String.format("access:verify:%d:%s", userId, deviceId);

        return lockUtil.executeWithLock(lockKey, 3, 10, () -> {
            // 业务逻辑：查询是否已验证
            AccessRecordEntity exists = accessRecordDao.selectTodayRecord(userId, deviceId);
            if (exists != null) {
                return ResponseDTO.userError("DUPLICATE", "今日已验证");
            }

            // 执行验证
            return verifyAccessAsync(userId, deviceId).join();
        });
    }
}
```

### 10.2 下午：综合性能测试

**综合测试场景**：

```bash
# 测试场景1：500并发用户连续验证
wrk -t12 -c500 -d300s --latency http://localhost:8090/api/v1/access/verify/async

# 测试场景2：慢SQL模拟（验证Druid监控）
# 在Druid监控页面查看慢SQL记录

# 测试场景3：限流测试（QPS超过2000）
# 应该看到限流生效，返回429错误

# 测试场景4：熔断测试（模拟服务降级）
# 关闭设备服务，观察熔断是否生效

# 测试场景5：分布式锁并发测试
# 使用JMeter模拟100个并发用户同时请求同一资源
```

**验收标准**：
- ✅ TPS ≥ 2000
- ✅ 平均响应时间 < 400ms
- ✅ 限流功能正常
- ✅ 熔断功能正常
- ✅ 分布式锁防止并发冲突
- ✅ Druid监控正常记录

---

## 📊 周总结和验证

### 周末验收清单

**Day 6-7: 连接池和异步处理**
- [ ] Druid替换HikariCP完成
- [ ] Druid监控页面可访问
- [ ] 异步线程池配置完成
- [ ] 异步Service实现验证通过

**Day 8-9: 线程池和限流熔断**
- [ ] Tomcat线程池优化完成
- [ ] Sentinel集成完成
- [ ] 限流规则验证通过
- [ ] 熔断规则验证通过

**Day 10: 分布式锁和综合测试**
- [ ] Redisson分布式锁集成完成
- [ ] 分布式锁功能验证通过
- [ ] 综合性能测试通过
- [ ] 所有验收指标达成

### 最终验收指标

| 指标 | 目标值 | 实际值 | 达成率 |
|------|--------|--------|--------|
| **TPS** | ≥2000 | ___ | ___% |
| **并发用户** | ≥1000 | ___ | ___% |
| **响应时间** | <400ms | ___ | ___% |
| **连接池监控覆盖率** | 100% | ___% | ___% |
| **限流准确率** | 100% | ___% | ___% |
| **熔断响应时间** | <10s | ___ | ___% |

### 回滚准备

```bash
# 创建Git标签（Day 10执行）
git tag -a v2.0.0-week2-concurrency-optimization -m "第2周：并发和连接池优化完成"

# 推送到远程仓库
git push origin v2.0.0-week2-concurrency-optimization

# 如果需要回滚
git checkout v2.0.0-week1-database-cache-optimization
```

---

## 🎯 下周预告（Week 3）

**下周任务**: 前端和网络优化
- Day 11-12: Vite配置和代码分割
- Day 13: HTTP/2和CDN迁移
- Day 14-15: 性能测试和优化验证

**预期成果**:
- 首屏加载时间: 3.5s → <2s
- Bundle大小: 5.2MB → <2MB
- 页面加载时间: 3.5s → <1.5s

---

**文档版本**: v1.0
**创建时间**: 2025-01-XX
**负责人**: 性能优化小组
**审核人**: 架构委员会
