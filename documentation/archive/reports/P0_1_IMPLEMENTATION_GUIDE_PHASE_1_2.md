# P0-1 账户服务集成 - 阶段1.2实施指南

> **创建时间**: 2025-12-23
> **状态**: 阶段1.1已完成，阶段1.2进行中
> **目标**: 实现账户余额增加功能的完整集成

---

## ✅ 已完成工作总结

### 1. 核心代码实现（100%完成）

| 文件 | 状态 | 说明 |
|------|------|------|
| `client/AccountServiceClient.java` | ✅ 已完成 | Feign Client接口定义 |
| `client/dto/BalanceIncreaseRequest.java` | ✅ 已完成 | 余额增加请求DTO |
| `client/dto/BalanceDecreaseRequest.java` | ✅ 已完成 | 余额扣减请求DTO |
| `client/dto/BalanceCheckRequest.java` | ✅ 已完成 | 余额检查请求DTO |
| `client/dto/BalanceChangeResult.java` | ✅ 已完成 | 余额变更结果DTO |
| `client/dto/BalanceCheckResult.java` | ✅ 已完成 | 余额检查结果DTO |
| `client/fallback/AccountServiceClientFallback.java` | ✅ 已完成 | 降级策略实现 |
| `config/AccountServiceConfig.java` | ✅ 已完成 | 账户服务配置类 |
| `manager/SubsidyGrantManager.java` | ✅ 已更新 | 集成AccountServiceClient |
| `ConsumeServiceApplication.java` | ✅ 已更新 | 添加@EnableFeignClients注解 |

### 2. 核心功能实现

- ✅ **Feign Client接口**: 完整的账户服务调用接口
- ✅ **DTO类设计**: 请求/响应对象完整定义
- ✅ **降级策略**: 服务不可用时的降级处理
- ✅ **Manager集成**: SubsidyGrantManager已集成AccountServiceClient
- ✅ **Feign启用**: 主应用类已添加@EnableFeignClients注解
- ✅ **熔断器启用**: 主应用类已添加@EnableCircuitBreaker注解

---

## 📋 需要手动完成的配置

### 步骤1: 检查并添加Maven依赖

**文件**: `pom.xml`

**需要添加的依赖**:

```xml
<!-- ==================== Spring Cloud OpenFeign ==================== -->
<!-- OpenFeign（服务间调用） -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>

<!-- Spring Cloud LoadBalancer（负载均衡） -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>

<!-- Spring Cloud CircuitBreaker（熔断器，Resilience4j） -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker</artifactId>
</dependency>

<!-- ==================== Spring Retry（重试机制） ==================== -->
<dependency>
    <groupId>org.springframework.retry</groupId>
    <artifactId>spring-retry</artifactId>
</dependency>

<!-- ==================== Spring Boot Admin（监控） ==================== -->
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>spring-boot-admin-starter-client</artifactId>
    <version>${spring-boot-admin.version}</version>
</dependency>
```

**检查方法**:
```bash
# 在项目根目录执行
grep -n "spring-cloud-starter-openfeign" microservices/ioedream-consume-service/pom.xml
```

如果依赖不存在，需要添加上述依赖到pom.xml的`<dependencies>`节点中。

### 步骤2: 配置Feign客户端

**文件**: `src/main/resources/application.yml`

**需要添加的配置**:

```yaml
# ==================== Spring Cloud OpenFeign配置 ====================
feign:
  client:
    config:
      default:
        # 连接超时（毫秒）
        connectTimeout: 5000
        # 读取超时（毫秒）
        readTimeout: 10000
        # 日志级别
        loggerLevel: basic
  # 启用Hystrix熔断器
  hystrix:
    enabled: false
  # 启用压缩
  compression:
    request:
      enabled: true
      mime-types: text/xml,application/xml,application/json
      min-request-size: 2048
    response:
      enabled: true
  # 启用OkHttp作为HTTP客户端（可选，性能更好）
  okhttp:
    enabled: false

# ==================== Spring Cloud LoadBalancer配置 ====================
spring:
  cloud:
    loadbalancer:
      ribbon:
        enabled: false
      # 缓存配置
      cache:
        enabled: true
        ttl: 3600
      # 重试配置
      retry:
        enabled: true
        max-retries-on-same-service: 1
        max-retries-on-next-service: 1

# ==================== Resilience4j熔断器配置 ====================
resilience4j:
  circuitbreaker:
    configs:
      default:
        # 滑动窗口大小
        slidingWindowSize: 50
        # 最小调用次数
        minimumNumberOfCalls: 20
        # 失败率阈值（百分比）
        failureRateThreshold: 50
        # 慢调用阈值（毫秒）
        slowCallRateThreshold: 100
        slowCallDurationThreshold: 2000
        # 半开状态等待时间（毫秒）
        waitDurationInOpenState: 5000
        # 半开状态允许的调用次数
        permittedNumberOfCallsInHalfOpenState: 3
        # 自动从开启转换到半开的时间
        automaticTransitionFromOpenToHalfOpenEnabled: true
    instances:
      ioedream-account-service:
        baseConfig: default
  timelimiter:
    configs:
      default:
        # 超时时间（毫秒）
        timeoutDuration: 5000
    instances:
      ioedream-account-service:
        baseConfig: default
  retry:
    configs:
      default:
        # 最大重试次数
        maxAttempts: 3
        # 重试间隔（毫秒）
        waitDuration: 1000
        # 重试异常配置
        retryExceptions:
          - java.lang.IllegalArgumentException
          - java.util.concurrent.TimeoutException
          - java.net.UnknownHostException
        ignoreExceptions:
          - java.lang.IllegalStateException
    instances:
      ioedream-account-service:
        baseConfig: default
```

### 步骤3: 配置Seata分布式事务

**文件**: `src/main/resources/application.yml` 或 `application-seata.yml`

**需要添加的配置**:

```yaml
# ==================== Seata分布式事务配置 ====================
seata:
  # 是否启用Seata
  enabled: ${SEATA_ENABLED:true}
  # 应用ID
  application-id: ioedream-consume-service
  # 事务组
  tx-service-group: ioedream-tx-group
  # 服务配置
  service:
    # 事务分组映射
    vgroup-mapping:
      ioedream-tx-group: default
    # 分组列表
    grouplist:
      - ${SEATA_SERVER_ADDR:127.0.0.1:8091}
    # 关闭全局事务
    disable-global-transaction: false
    # 数据源代理模式
    enable-autoDataSourceProxy: true
    # 数据源代理类型
    data-source-proxy-mode: AT
  # 配置中心配置
  config:
    type: nacos
    nacos:
      server-addr: ${spring.cloud.nacos.discovery.server-addr}
      namespace: ${NACOS_NAMESPACE:dev}
      group: SEATA_GROUP
      username: ${NACOS_USERNAME:nacos}
      password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vPrYnCJNA==)}
      data-id: seata-server.properties
  # 注册中心配置
  registry:
    type: nacos
    nacos:
      server-addr: ${spring.cloud.nacos.discovery.server-addr}
      namespace: ${NACOS_NAMESPACE:dev}
      group: SEATA_GROUP
      username: ${NACOS_USERNAME:nacos}
      password: ${NACOS_PASSWORD:ENC(I2//JcHFpBc7vPrYnCJNA==)}
      application: seata-server
  # undo_log表配置
  undo:
    log-validation: true
    log-serialization: jackson
    # 只有在AT模式下才需要
    data:
      # 每个分支事务的缓存大小
      cache-size: 1000
      # undo日志表的表名
      table: undo_log
  # 客户端配置
  client:
    rm:
      asyncCommitBufferLimit: 10000
      lock:
        retry-interval: 10
        retry-times: 30
        retry-branch-rollback-on: true
      report:
        retry-count: 5
        report-success-count: 3
    tm:
      commit-retry-count: 5
      rollback-retry-count: 5
    undo:
      data-validation: true
      log-serialization: jackson
```

### 步骤4: 创建undo_log表（AT模式需要）

**文件**: `src/main/resources/db/migration/V1.0.1__create_seata_undo_log.sql`

```sql
-- ==================== Seata Undo Log表 ====================
-- 用于Seata AT模式的回滚日志
-- 分布式事务回滚时使用

CREATE TABLE IF NOT EXISTS `undo_log` (
  `branch_id` BIGINT NOT NULL COMMENT '分支事务ID',
  `xid` VARCHAR(128) NOT NULL COMMENT '全局事务ID',
  `context` VARCHAR(128) NOT NULL COMMENT '上下文',
  `rollback_info` LONGBLOB NOT NULL COMMENT '回滚数据',
  `log_status` INT NOT NULL COMMENT '状态（0-正常，1-已完成回滚）',
  `log_created` DATETIME NOT NULL COMMENT '创建时间',
  `log_modified` DATETIME NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`branch_id`),
  KEY `idx_xid` (`xid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT模式回滚日志表';

-- 添加索引优化查询性能
CREATE INDEX idx_log_status ON undo_log(log_status);
CREATE INDEX idx_log_created ON undo_log(log_created);
```

### 步骤5: 配置Spring Retry（重试机制）

**文件**: `config/RetryConfig.java`

```java
package net.lab1024.sa.consume.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * 重试配置
 * <p>
 * 配置账户服务调用的重试策略
 * </p>
 */
@Configuration
@EnableRetry
public class RetryConfig {

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 固定间隔重试策略
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(1000L); // 1秒

        // 简单重试策略
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3); // 最多重试3次

        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}
```

### 步骤6: 编译和验证

**命令**:

```bash
# 1. 清理并编译项目
cd D:\IOE-DREAM\microservices\ioedream-consume-service
mvn clean compile

# 2. 检查编译结果
echo $?  # 0表示成功

# 3. 运行测试（如果有）
mvn test -Dtest=SubsidyGrantManagerTest

# 4. 打包
mvn package -DskipTests
```

---

## 🔄 下一步实施计划

### 阶段1.2剩余工作（继续实施中）

#### Task 2.6: 添加@GlobalTransactional注解

**文件**: `manager/SubsidyGrantManager.java`

**需要修改的方法**:
- `grantMonthlyMealSubsidy()`
- `grantFestivalSubsidy()`
- `batchGrantSubsidy()`
- `grantOvertimeMealSubsidy()`
- `grantNightShiftSubsidy()`
- `grantRefund()`
- `revokeSubsidy()`

**示例修改**:

```java
@GlobalTransactional(name = "grant-monthly-meal-subsidy", rollbackFor = Exception.class)
public String grantMonthlyMealSubsidy(Long userId, int year, int month,
                                      BigDecimal amount, Long operatorId) {
    // ... 现有代码 ...
}
```

### 阶段1.3: 异常处理和降级策略（2天）

#### Task 3.2: 创建本地补偿表

**文件**: `entity/AccountCompensationEntity.java`

**需要创建的表**: `t_account_compensation`

**字段**:
- `id` - 主键
- `user_id` - 用户ID
- `operation` - 操作类型（INCREASE/DECREASE）
- `amount` - 金额
- `business_type` - 业务类型
- `business_no` - 业务编号
- `status` - 状态（PENDING/SUCCESS/FAILED）
- `retry_count` - 重试次数
- `error_message` - 错误信息
- `create_time` - 创建时间
- `update_time` - 更新时间

#### Task 3.3: 实现补偿任务调度

**文件**: `scheduler/AccountCompensationScheduler.java`

**功能**:
- 定时扫描补偿表
- 重新调用账户服务
- 更新补偿状态
- 达到最大重试次数后标记失败

### 阶段1.4: 测试验证（3天）

#### Task 4.1-4.6: 测试

**测试文件**: `test/.../SubsidyGrantManagerIntegrationTest.java`

**测试场景**:
- 正常发放流程
- 重复发放幂等性
- 余额不足扣减
- 服务降级
- 分布式事务回滚

---

## 📊 实施检查清单

### 必须完成（P0级）

- [ ] 步骤1: 检查并添加Maven依赖到pom.xml
- [ ] 步骤2: 在application.yml中配置Feign
- [ ] 步骤3: 在application.yml中配置Seata
- [ ] 步骤4: 创建undo_log表（Flyway迁移脚本）
- [ ] 步骤5: 创建RetryConfig配置类
- [ ] 步骤6: 编译和验证
- [ ] Task 2.6: 在补贴发放方法上添加@GlobalTransactional注解

### 建议完成（P1级）

- [ ] 创建本地补偿表和DAO
- [ ] 实现补偿任务调度器
- [ ] 编写单元测试和集成测试
- [ ] 编写性能测试

### 可选完成（P2级）

- [ ] 配置Spring Boot Admin监控
- [ ] 配置Zipkin分布式追踪
- [ ] 编写运维文档

---

## ⚠️ 重要注意事项

### 1. 账户服务依赖

**当前状态**: ⚠️ 未确认

**问题**: 账户服务（ioedream-account-service）是否已经实现？
- ✅ 如果已实现：可以直接调用
- ❌ 如果未实现：需要先实现账户服务，或使用Mock测试

**建议**:
1. 先检查账户服务是否已部署
2. 如果未部署，创建Mock服务用于测试
3. 确保Nacos服务发现可以找到账户服务

### 2. Seata Server

**当前状态**: ⚠️ 未确认

**问题**: Seata Server是否已经启动？

**建议**:
1. 下载Seata Server: https://seata.io/
2. 配置file.conf和registry.conf
3. 启动Seata Server
4. 在Nacos上配置seata-server.properties

### 3. Nacos服务发现

**当前状态**: ✅ 已配置

**建议**:
1. 确保Nacos Server已启动
2. 确保consume-service和account-service都已注册到Nacos
3. 测试服务间发现是否正常

---

## 📞 需要帮助？

如果遇到以下问题，请联系架构团队：

1. **依赖冲突**: Maven依赖版本冲突无法解决
2. **Seata配置**: Seata Server配置不正确
3. **服务发现**: Nacos服务发现不正常
4. **测试失败**: 单元测试或集成测试失败

**联系方式**:
- **架构委员会**: IOE-DREAM架构委员会
- **技术支持**: 企业内部工单系统

---

**文档创建**: 2025-12-23
**最后更新**: 2025-12-23
**版本**: v1.0.0
