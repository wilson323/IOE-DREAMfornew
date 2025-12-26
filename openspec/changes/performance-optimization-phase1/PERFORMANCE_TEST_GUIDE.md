# 性能测试实施指南 (P1-9.4)

> **项目**: IOE-DREAM智慧园区管理平台
> **任务**: P1-9.4 性能测试
> **完成日期**: 2025-12-26
> **实施周期**: 5人天
> **状态**: 📝 文档完成，待实施验证

---

## 📋 执行摘要

本指南提供了全面的性能测试方法论，确保系统满足性能要求。

### 核心目标

- ✅ **API响应时间**: P95 < 500ms
- ✅ **系统吞吐量**: ≥1000 TPS
- ✅ **并发用户支持**: ≥500并发
- ✅ **系统稳定性**: 7×24小时稳定运行

### 测试类型

| 测试类型 | 目标 | 优先级 | 预计工时 |
|---------|------|--------|----------|
| **负载测试** | 验证系统在预期负载下的性能 | P0 | 1.5人天 |
| **压力测试** | 找出系统性能瓶颈 | P0 | 1.5人天 |
| **稳定性测试** | 长时间运行稳定性验证 | P1 | 1人天 |
| **峰值测试** | 极限负载下的系统行为 | P1 | 1人天 |

---

## 🎯 性能测试策略

### 1. 测试金字塔

```
           /\
          /峰值\         峰值测试 (偶尔)
         /------\
        /  压力  \       压力测试 (找瓶颈)
       /----------\
      /   负载     \     负载测试 (常规) ← 本指南重点
     /--------------\
    /    稳定性       \   稳定性测试 (持续)
   /------------------\
```

### 2. 测试场景定义

**关键业务场景**：

| 场景名称 | API路径 | 目标TPS | 目标响应时间(P95) | 并发用户 |
|---------|---------|---------|------------------|----------|
| 用户登录 | POST /api/auth/login | 100 | 200ms | 50 |
| 门禁验证 | POST /api/access/verify | 200 | 300ms | 100 |
| 考勤打卡 | POST /api/attendance/punch | 150 | 250ms | 80 |
| 消费支付 | POST /api/consume/pay | 80 | 500ms | 40 |
| 查询记录 | GET /api/access/record/page | 300 | 400ms | 150 |
| 视频预览 | GET /api/video/preview | 50 | 1000ms | 20 |

**负载测试场景**：

```
场景1: 正常负载
  - 并发用户: 100
  - TPS: 200
  - 持续时间: 10分钟
  - 目标: P95 < 500ms, 错误率 < 0.1%

场景2: 高负载
  - 并发用户: 300
  - TPS: 500
  - 持续时间: 5分钟
  - 目标: P95 < 800ms, 错误率 < 0.5%

场景3: 峰值负载
  - 并发用户: 500
  - TPS: 1000
  - 持续时间: 2分钟
  - 目标: P95 < 1000ms, 错误率 < 1%
```

---

## 🛠️ 测试工具和环境

### 1. JMeter测试计划

**线程组配置**：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="IOE-DREAM性能测试">
      <elementProp name="TestPlan.user_defined_variables">
        <collectionProp name="Arguments.arguments">
          <elementProp name="BASE_URL" elementType="Argument">
            <stringProp name="Argument.name">BASE_URL</stringProp>
            <stringProp name="Argument.value">http://localhost:8080</stringProp>
          </elementProp>
          <elementProp name="USERS" elementType="Argument">
            <stringProp name="Argument.name">USERS</stringProp>
            <stringProp name="Argument.value">100</stringProp>
          </elementProp>
          <elementProp name="RAMP_UP" elementType="Argument">
            <stringProp name="Argument.name">RAMP_UP</stringProp>
            <stringProp name="Argument.value">10</stringProp>
          </elementProp>
        </collectionProp>
      </elementProp>
    </TestPlan>
    <hashTree>
      <!-- 线程组 -->
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="用户负载">
        <stringProp name="ThreadGroup.num_threads">${USERS}</stringProp>
        <stringProp name="ThreadGroup.ramp_time">${RAMP_UP}</stringProp>
        <boolProp name="ThreadGroup.scheduler">true</boolProp>
        <stringProp name="ThreadGroup.duration">600</stringProp>
        <elementProp name="ThreadGroup.main_controller">
          <stringProp name="LoopController.loops">-1</stringProp>
        </elementProp>
      </ThreadGroup>
      <hashTree>
        <!-- HTTP请求默认值 -->
        <ConfigTestElement guiclass="HttpDefaultsGui" testclass="ConfigTestElement" testname="HTTP请求默认值">
          <stringProp name="HTTPSampler.domain">${BASE_URL}</stringProp>
          <stringProp name="HTTPSampler.port">8080</stringProp>
          <stringProp name="HTTPSampler.protocol">http</stringProp>
          <stringProp name="HTTPSampler.contentEncoding">UTF-8</stringProp>
        </ConfigTestElement>

        <!-- JSON提取器 -->
        <JSONPostProcessor guiclass="JSONPostProcessorGui" testclass="JSONPostProcessor" testname="提取Token">
          <stringProp name="JSONPostProcessor.referenceNames">token</stringProp>
          <stringProp name="JSONPostProcessor.jsonPathExprs">$.data.token</stringProp>
          <stringProp name="JSONPostProcessor.match_numbers"></stringProp>
        </JSONPostProcessor>

        <!-- HTTP Cookie管理器 -->
        <CookieManager guiclass="CookiePanel" testclass="CookieManager" testname="HTTP Cookie管理器"/>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

### 2. JMeter测试脚本示例

**场景1: 用户登录负载测试**：

```java
import net.lab1024.sa.test.performance.BasePerformanceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 用户登录性能测试
 */
@DisplayName("用户登录性能测试")
class LoginPerformanceTest extends BasePerformanceTest {

    @Test
    @DisplayName("登录负载测试 - 100并发 - P95<200ms")
    void testLoginLoad_100Concurrent() {
        // given
        int threads = 100;
        int rampUp = 10;  // 10秒内启动所有线程
        int duration = 600;  // 持续10分钟

        // when
        PerformanceTestResult result = runLoadTest(() -> {
            // 执行登录请求
            loginUser("testuser", "password");
        }, threads, rampUp, duration);

        // then
        assertThat(result.getP95ResponseTime())
            .isLessThan(200);  // P95 < 200ms
        assertThat(result.getErrorRate())
            .isLessThan(0.01);  // 错误率 < 1%
        assertThat(result.getThroughput())
            .isGreaterThanOrEqualTo(100);  // TPS ≥ 100
    }

    @Test
    @DisplayName("登录压力测试 - 找出最大TPS")
    void testLoginStress_findMaxTPS() {
        // 从小到大逐步增加负载
        int[] userLevels = {50, 100, 200, 300, 400, 500};

        for (int users : userLevels) {
            System.out.println("测试并发用户数: " + users);

            PerformanceTestResult result = runLoadTest(() -> {
                loginUser("testuser", "password");
            }, users, 10, 120);  // 2分钟测试

            System.out.println("TPS: " + result.getThroughput());
            System.out.println("P95: " + result.getP95ResponseTime() + "ms");
            System.out.println("错误率: " + result.getErrorRate() + "%");

            // 如果错误率超过5%，停止测试
            if (result.getErrorRate() > 5.0) {
                System.out.println("达到性能瓶颈，最大TPS: " + result.getThroughput());
                break;
            }
        }
    }
}
```

**场景2: 门禁验证负载测试**：

```java
@DisplayName("门禁验证性能测试")
class AccessVerificationPerformanceTest extends BasePerformanceTest {

    @Test
    @DisplayName("门禁验证负载测试 - 200并发 - P95<300ms")
    void testAccessVerificationLoad() {
        // given
        int threads = 200;
        int rampUp = 20;
        int duration = 600;

        // when
        PerformanceTestResult result = runLoadTest(() -> {
            // 模拟门禁验证请求
            verifyAccess(1L, 100L);
        }, threads, rampUp, duration);

        // then
        assertThat(result.getP95ResponseTime())
            .isLessThan(300);
        assertThat(result.getErrorRate())
            .isLessThan(0.01);
        assertThat(result.getThroughput())
            .isGreaterThanOrEqualTo(200);
    }
}
```

### 3. 测试环境配置

**性能测试专用配置**：

```yaml
# application-performance.yml
spring:
  # 数据库连接池优化
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  # Redis缓存配置
  data:
    redis:
      host: localhost
      port: 6379
      lettuce:
        pool:
          max-active: 50
          max-idle: 20
          min-idle: 10

# Actuator监控端点
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

---

## 📊 性能测试执行

### 1. 命令行执行JMeter

```bash
# 1. 准备测试环境
# 启动MySQL、Redis等服务

# 2. 执行负载测试
jmeter -n -t test-plans/access-verification.jmx \
  -JBASE_URL=http://localhost:8080 \
  -JUSERS=200 \
  -JRAMP_UP=20 \
  -l results/access-verification-20250126.jtl \
  -e -o reports/access-verification-20250126

# 3. 查看测试报告
open reports/access-verification-20250126/index.html

# 4. 解析JTL结果
jmeter -g results/access-verification-20250126.jtl \
  -o reports/access-verification-20250126
```

### 2. 性能基线建立

**建立性能基线**：

```bash
#!/bin/bash
# scripts/performance-baseline.sh

echo "建立性能基线..."

# 测试场景1: 用户登录 (目标: 100 TPS, P95<200ms)
jmeter -n -t test-plans/login-load.jmx \
  -JUSERS=50 \
  -JRAMP_UP=5 \
  -l results/baseline-login-100tps.jtl \
  -e -o reports/baseline-login-100tps

# 测试场景2: 门禁验证 (目标: 200 TPS, P95<300ms)
jmeter -n -t test-plans/access-verification.jmx \
  -JUSERS=100 \
  -JRAMP_UP=10 \
  -l results/baseline-access-200tps.jtl \
  -e -o reports/baseline-access-200tps

# 测试场景3: 考勤打卡 (目标: 150 TPS, P95<250ms)
jmeter -n -t test-plans/attendance-punch.jmx \
  -JUSERS=80 \
  -JRAMP_UP=8 \
  -l results/baseline-attendance-150tps.jtl \
  -e -o reports/baseline-attendance-150tps

echo "性能基线建立完成！"
```

### 3. 性能瓶颈分析

**瓶颈分析方法**：

```java
package net.lab1024.sa.test.performance;

import java.util.concurrent.TimeUnit;

/**
 * 性能瓶颈分析器
 */
public class PerformanceBottleneckAnalyzer {

    /**
     * 分析性能瓶颈
     */
    public void analyzeBottleneck(PerformanceTestResult result) {
        System.out.println("=== 性能瓶颈分析报告 ===");
        System.out.println("TPS: " + result.getThroughput());
        System.out.println("P50: " + result.getP50ResponseTime() + "ms");
        System.out.println("P95: " + result.getP95ResponseTime() + "ms");
        System.out.println("P99: " + result.getP99ResponseTime() + "ms");
        System.out.println("错误率: " + result.getErrorRate() + "%");

        // 分析响应时间
        if (result.getP95ResponseTime() > 500) {
            System.out.println("⚠️  响应时间过长 (P95 > 500ms)");
            System.out.println("可能原因:");
            System.out.println("  1. 数据库慢查询");
            System.out.println("  2. 缓存未命中");
            System.out.println("  3. 网络延迟");
            System.out.println("  4. JVM GC暂停");
        }

        // 分析错误率
        if (result.getErrorRate() > 1.0) {
            System.out.println("⚠️  错误率过高 (" + result.getErrorRate() + "%)");
            System.out.println("可能原因:");
            System.out.println("  1. 超时");
            System.out.println("  2. 资源耗尽");
            System.out.println("  3. 并发冲突");
            System.out.println("  4. 内存溢出");
        }

        // 分析吞吐量
        if (result.getThroughput() < 100) {
            System.out.println("⚠️  吞吐量过低 (TPS < 100)");
            System.out.println("可能原因:");
            System.out.println("  1. 连接池不足");
            System.out.println("  2. 线程池不足");
            System.out.println("  3. 数据库锁等待");
            System.out.println("  4. 慢查询阻塞");
        }

        System.out.println("========================");
    }

    /**
     * 生成优化建议
     */
    public void generateOptimizationSuggestions(PerformanceTestResult result) {
        System.out.println("=== 性能优化建议 ===");

        if (result.getP95ResponseTime() > 500) {
            System.out.println("1. 响应时间优化:");
            System.out.println("   - 添加数据库索引");
            System.out.println("   - 启用查询缓存");
            System.out.println("   - 优化SQL语句");
            System.out.println("   - 使用批量操作");
        }

        if (result.getErrorRate() > 1.0) {
            System.out.println("2. 错误率优化:");
            System.out.println("   - 增加连接池大小");
            System.out.println("   - 增加超时时间");
            System.out.println("   - 优化并发控制");
            System.out.println("   - 增加重试机制");
        }

        if (result.getThroughput() < 100) {
            System.out.println("3. 吞吐量优化:");
            System.out.println("   - 增加线程池大小");
            System.out.println("   - 优化数据库连接池");
            System.out.println("   - 使用异步处理");
            System.out.println("   - 启用HTTP/2");
        }

        System.out.println("====================");
    }
}
```

---

## 🔍 监控和分析

### 1. JVM监控

**使用JConsole/VisualVM**：

```bash
# 启动应用时添加JMX监控参数
java -Dcom.sun.management.jmxremote \
     -Dcom.sun.management.jmxremote.port=9010 \
     -Dcom.sun.management.jmxremote.authenticate=false \
     -Dcom.sun.management.jmxremote.ssl=false \
     -jar ioedream-access-service.jar

# 连接JConsole
jconsole localhost:9010

# 关键监控指标:
# - 堆内存使用
# - GC次数和时间
# - 线程数
# - CPU使用率
```

### 2. 数据库监控

**MySQL慢查询监控**：

```sql
-- 启用慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 0.5;
SET GLOBAL log_queries_not_using_indexes = 'ON';

-- 查看慢查询统计
SELECT
    COUNT(*) as '慢查询数量',
    ROUND(AVG(query_time), 2) as '平均耗时(秒)',
    ROUND(MAX(query_time), 2) as '最大耗时(秒)'
FROM mysql.slow_log
WHERE start_time > DATE_SUB(NOW(), INTERVAL 1 HOUR);

-- 查看Top 10慢查询
SELECT
    ROUND(query_time, 2) as '耗时(秒)',
    SUBSTRING(sql_text, 1, 100) as 'SQL语句'
FROM mysql.slow_log
WHERE start_time > DATE_SUB(NOW(), INTERVAL 1 HOUR)
ORDER BY query_time DESC
LIMIT 10;
```

### 3. 应用监控

**Spring Boot Actuator**：

```bash
# 查看应用健康状态
curl http://localhost:8080/actuator/health

# 查看应用指标
curl http://localhost:8080/actuator/metrics

# 查看JVM内存
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# 查看HTTP请求统计
curl http://localhost:8080/actuator/metrics/http.server.requests

# 导出Prometheus格式
curl http://localhost:8080/actuator/prometheus
```

---

## 📈 性能测试报告

### 1. 报告模板

```markdown
# 性能测试报告

## 测试概述

- **测试日期**: 2025-12-26
- **测试环境**: 测试环境
- **测试工具**: JMeter 5.6
- **测试场景**: 门禁验证负载测试

## 测试结果

### 负载测试结果

| 指标 | 目标值 | 实际值 | 状态 |
|------|--------|--------|------|
| TPS | ≥200 | 235 | ✅ 达标 |
| P95响应时间 | <300ms | 256ms | ✅ 达标 |
| P99响应时间 | <500ms | 412ms | ✅ 达标 |
| 错误率 | <0.1% | 0.05% | ✅ 达标 |
| CPU使用率 | <80% | 65% | ✅ 达标 |
| 内存使用率 | <80% | 72% | ✅ 达标 |

### 性能瓶颈分析

1. **响应时间分析**:
   - P50: 145ms
   - P95: 256ms
   - P99: 412ms
   - 最大值: 687ms

2. **吞吐量分析**:
   - 平均TPS: 235
   - 峰值TPS: 312
   - 最低TPS: 198

3. **错误分析**:
   - 总请求数: 141,000
   - 错误请求数: 71
   - 错误率: 0.05%

### 优化建议

1. **数据库优化**:
   - [ ] 添加复合索引: idx_user_device_time
   - [ ] 优化慢查询: SELECT * FROM t_access_record
   - [ ] 启用查询缓存

2. **缓存优化**:
   - [ ] 增加Redis缓存命中率
   - [ ] 使用本地缓存(Caffeine)
   - [ ] 实现缓存预热

3. **连接池优化**:
   - [ ] 增加HikariCP最大连接数: 50 → 80
   - [ ] 优化连接超时时间

## 结论

✅ **性能测试通过** - 所有指标均达到预期目标

## 附录

- [详细测试报告](reports/access-verification-20250126/index.html)
- [JMeter测试脚本](test-plans/access-verification.jmx)
- [监控数据](monitoring/access-verification-20250126/)
```

---

## ✅ 性能测试检查清单

### 测试前检查

```markdown
## 测试前准备检查清单

### 环境准备
- [ ] 测试环境搭建完成
- [ ] 数据库数据准备
- [ ] Redis缓存预热
- [ ] 应用服务启动

### 测试工具
- [ ] JMeter安装配置
- [ ] 测试脚本准备
- [ ] 监控工具准备
- [ ] 报告生成工具配置

### 测试数据
- [ ] 测试账号准备
- [ ] 测试设备数据
- [ ] 测试场景数据
- [ ] 基线数据准备
```

### 测试执行检查

```markdown
## 测试执行检查清单

### 负载测试
- [ ] 正常负载测试 (100用户)
- [ ] 高负载测试 (300用户)
- [ ] 峰值负载测试 (500用户)
- [ ] 测试数据收集

### 压力测试
- [ ] 逐步增加负载
- [ ] 找出性能瓶颈
- [ ] 记录瓶颈点
- [ ] 分析瓶颈原因

### 稳定性测试
- [ ] 长时间运行 (7×24h)
- [ ] 内存泄漏检查
- [ ] 连接泄漏检查
- [ ] 性能衰减分析
```

---

## 🚀 CI/CD集成

### 1. Maven配置

```xml
<!-- pom.xml -->
<build>
    <plugins>
        <!-- JMeter Maven插件 -->
        <plugin>
            <groupId>com.lazerycode.jmeter</groupId>
            <artifactId>jmeter-maven-plugin</artifactId>
            <version>3.5.0</version>
            <executions>
                <execution>
                    <id>jmeter-tests</id>
                    <goals>
                        <goal>jmeter</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <testFilesIncluded>
                    <jMeterTestFile>**/*.jmx</jMeterTestFile>
                </testFilesIncluded>
                <jMeterVersion>5.6</jMeterVersion>
                <jmeterPlugins>
                    <plugin>
                        <groupId>kg.apc</groupId>
                        <artifactId>jmeter-plugins-standard</artifactId>
                    </plugin>
                </jmeterPlugins>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 2. GitHub Actions配置

```yaml
# .github/workflows/performance-test.yml
name: 性能测试

on:
  push:
    branches: [ main, develop ]
  schedule:
    - cron: '0 2 * * *'  # 每天凌晨2点执行

jobs:
  performance-test:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: 设置JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: 'maven'

    - name: 启动测试环境
      run: |
        docker-compose -f docker-compose.test.yml up -d
        sleep 30  # 等待服务就绪

    - name: 运行性能测试
      run: mvn verify -Pperformance-test

    - name: 收集测试结果
      if: always()
      uses: actions/upload-artifact@v3
      with:
        name: performance-test-report
        path: target/jmeter/results/

    - name: 发布测试报告
      if: always()
      uses: peaceiris/actions-gh-pages@v3
      with:
        github_token: ${{ secrets.GITHUB_TOKEN }}
        publish_dir: target/jmeter/report/
```

---

## 📚 相关文档

- **单元测试完善指南**: [UNIT_TEST_IMPROVEMENT_GUIDE.md](./UNIT_TEST_IMPROVEMENT_GUIDE.md)
- **集成测试完善指南**: [INTEGRATION_TEST_IMPROVEMENT_GUIDE.md](./INTEGRATION_TEST_IMPROVEMENT_GUIDE.md)
- **SQL优化指南**: [SQL_OPTIMIZATION_IMPLEMENTATION_GUIDE.md](./SQL_OPTIMIZATION_IMPLEMENTATION_GUIDE.md)
- **JMeter用户手册**: [JMeter User Manual](https://jmeter.apache.org/usermanual/index.html)
- **性能测试最佳实践**: [Performance Testing Best Practices](https://www.performancetesting.co.uk/)

---

## 👥 实施团队

- **文档编写**: AI编程助手 (Claude Code)
- **方案设计**: IOE-DREAM架构团队
- **技术审核**: 待审核
- **实施验证**: 待验证

---

## 📅 版本信息

- **文档版本**: v1.0.0
- **完成日期**: 2025-12-26
- **实施周期**: 5人天
- **技术栈**: JMeter + Spring Boot Actuator + Prometheus

---

## 🎯 总结

本指南提供了全面的性能测试方法论，涵盖：

- ✅ **负载测试**: 验证系统在预期负载下的性能表现
- ✅ **压力测试**: 找出系统性能瓶颈和极限
- ✅ **稳定性测试**: 验证系统长时间运行的稳定性
- ✅ **峰值测试**: 验证系统在极限负载下的行为
- ✅ **性能监控**: JVM、数据库、应用全方位监控
- ✅ **瓶颈分析**: 系统化的性能瓶颈分析方法
- ✅ **优化建议**: 基于测试结果的针对性优化方案

**下一步**: 生成Phase 1完成报告，总结所有优化工作。

---

**报告生成时间**: 2025-12-26
**报告状态**: ✅ 文档完成，待实际验证
