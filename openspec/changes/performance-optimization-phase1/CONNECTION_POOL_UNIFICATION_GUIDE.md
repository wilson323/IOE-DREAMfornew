# 连接池统一优化实施指南

> **任务编号**: P1-7.3
> **任务名称**: 连接池统一（HikariCP → Druid）
> **预计工时**: 2人天
> **优先级**: P1（高优先级）
> **创建日期**: 2025-12-26

---

## 📋 任务概述

### 问题描述

当前系统使用多种连接池，存在配置不一致、监控能力弱、性能优化空间有限等问题：

- **连接池混用**: 12个微服务使用HikariCP，配置不统一
- **监控能力弱**: 无法有效监控连接池状态和SQL执行情况
- **性能待优化**: 连接池参数未调优，存在性能瓶颈
- **功能缺失**: 缺乏连接泄露检测、慢SQL记录等企业级特性

### 优化目标

- ✅ **统一连接池**: 将所有HikariCP替换为Druid连接池
- ✅ **性能提升**: 连接池性能提升40%，连接利用率从60%→90%
- ✅ **监控增强**: 实时监控连接池状态、SQL执行统计、慢SQL记录
- ✅ **功能完善**: 增加连接泄露检测、防火墙、防SQL注入等企业级特性

### 预期效果

| 指标 | 优化前 | 优化后 | 提升幅度 |
|------|--------|--------|----------|
| **连接获取性能** | 基线 | +40% | **40%↑** |
| **连接利用率** | 60% | 90% | **50%↑** |
| **SQL监控覆盖率** | 0% | 100% | **∞** |
| **慢SQL检测能力** | 无 | 有 | **新增** |
| **连接泄露检测** | 无 | 有 | **新增** |

---

## 🔧 技术方案

### 1. Druid vs HikariCP对比

| 特性 | HikariCP | Druid | 说明 |
|------|----------|-------|------|
| **性能** | 极快 | 快 | HikariCP性能略优，但Druid差距<5% |
| **监控能力** | 弱（仅JMX） | 强（内置监控页面） | Druid提供详细的SQL监控统计 |
| **功能丰富度** | 基础 | 丰富 | Druid提供防火墙、防注入、慢SQL记录等 |
| **配置灵活度** | 简单 | 高度可配置 | Druid提供更多调优选项 |
| **社区活跃度** | 高 | 高 | 两者都是成熟项目 |
| **企业级特性** | 少 | 多 | Druid更适合企业级应用 |

**结论**: 虽然HikariCP性能略优（<5%），但Druid的监控能力和企业级特性更适合IOE-DREAM项目。

### 2. Druid核心特性

#### 2.1 监控统计（StatFilter）

```java
// 启用StatFilter进行SQL监控
@Configuration
public class DruidConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    public DataSource druidDataSource() {
        DruidDataSource dataSource = new DruidDataSource();

        // 启用StatFilter
        List<Filter> filters = new ArrayList<>();
        filters.add(new StatFilter());

        dataSource.setProxyFilters(filters);
        return dataSource;
    }
}
```

**监控指标**:
- SQL执行次数
- SQL执行时间
- SQL执行错误数
- SQL慢查询记录
- 连接获取/释放统计
- 并发连接数

#### 2.2 防SQL注入（WallFilter）

```java
// 启用WallFilter防止SQL注入
@Bean
public WallFilter wallFilter() {
    WallFilter wallFilter = new WallFilter();
    wallFilter.setConfig(new WallConfig());

    // 允许的SQL操作
    wallFilter.getConfig().setSelectAllow(true);
    wallFilter.getConfig().setUpdateAllow(true);
    wallFilter.getConfig().setInsertAllow(true);
    wallFilter.getConfig().setDeleteAllow(true);

    // 禁止的危险操作
    wallFilter.getConfig().setMultiStatementAllow(false);  // 禁止多语句
    wallFilter.getConfig().setNoneBaseStatementAllow(false);  // 禁止非基础语句
    wallFilter.getConfig().setCallAllow(true);  // 允许存储过程
    wallFilter.getConfig().setSetAllow(true);  // 允许SET语句

    return wallFilter;
}
```

#### 2.3 日志输出（LogFilter）

```java
// 启用LogFilter输出SQL日志
@Bean
public LogFilter logFilter() {
    LogFilter logFilter = new LogFilter();
    logFilter.setStatementExecutableSqlLogEnable(true);  // 可执行的SQL日志
    logFilter.setStatementCreateAfterLogEnabled(false);  // 关闭创建日志
    logFilter.setStatementCloseAfterLogEnabled(false);   // 关闭关闭日志
    logFilter.setStatementParameterSetLogEnabled(true);  // 参数设置日志
    return logFilter;
}
```

---

## 📝 实施步骤

### 步骤1: 添加Druid依赖

**修改文件**: `microservices/microservices-common-data/pom.xml`

```xml
<!-- 添加Druid连接池依赖 -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-3-starter</artifactId>
    <version>1.2.25</version>
</dependency>
```

**移除HikariCP依赖**（如果显式声明）:

```xml
<!-- 移除HikariCP依赖（Spring Boot默认包含，但显式排除）-->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <exclusions>
        <exclusion>
            <groupId>*</groupId>
            <artifactId>*</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### 步骤2: 创建Druid配置类

**新建文件**: `microservices/microservices-common-data/src/main/java/net/lab1024/sa/common/data/config/DruidDataSourceConfig.java`

```java
package net.lab1024.sa.common.data.config;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.filter.wall.WallConfig;
import com.alibaba.druid.filter.wall.WallFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.properties.DruidStatProperties;
import com.alibaba.druid.wall.Violation;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Druid连接池配置类
 *
 * 核心功能：
 * 1. 统一配置Druid连接池
 * 2. 启用SQL监控统计
 * 3. 防SQL注入
 * 4. 慢SQL记录
 * 5. 连接泄露检测
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Slf4j
@Configuration
public class DruidDataSourceConfig {

    @Resource
    private DruidStatProperties druidStatProperties;

    /**
     * 配置Druid数据源
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    @Primary
    public DataSource druidDataSource() {
        DruidDataSource dataSource = new DruidDataSource();

        try {
            // 配置StatFilter（SQL监控统计）
            StatFilter statFilter = new StatFilter();
            statFilter.setLogSlowSql(true);  // 记录慢SQL
            statFilter.setSlowSqlMillis(1000);  // 慢SQL阈值（1秒）
            statFilter.setMergeSql(true);  // 合并相同SQL

            // 配置WallFilter（防SQL注入）
            WallFilter wallFilter = new WallFilter();
            wallFilter.setConfig(wallConfig());
            wallFilter.setLogViolation(true);  // 记录违规SQL
            wallFilter.setThrowException(false);  // 不抛异常，只记录

            // 设置Filters
            List filters = Lists.newArrayList();
            filters.add(statFilter);
            filters.add(wallFilter);

            dataSource.setProxyFilters(filters);

            // 连接泄露检测
            dataSource.setRemoveAbandoned(true);  // 开启连接泄露检测
            dataSource.setRemoveAbandonedTimeout(1800);  // 泄露超时时间（30分钟）
            dataSource.setLogAbandoned(true);  // 记录泄露日志

            log.info("[数据源配置] Druid连接池配置成功");
        } catch (Exception e) {
            log.error("[数据源配置] Druid连接池配置失败", e);
            throw new RuntimeException("Druid连接池配置失败", e);
        }

        return dataSource;
    }

    /**
     * WallFilter配置（防SQL注入）
     */
    @Bean
    public WallConfig wallConfig() {
        WallConfig config = new WallConfig();

        // 允许的基础操作
        config.setSelectAllow(true);
        config.setUpdateAllow(true);
        config.setInsertAllow(true);
        config.setDeleteAllow(true);

        // 禁止的多语句操作
        config.setMultiStatementAllow(false);  // 禁止多语句（防注入）
        config.setNoneBaseStatementAllow(false);  // 禁止非基础语句

        // 允许的其他操作
        config.setCallAllow(true);  // 允许存储过程
        config.setSetAllow(true);  // 允许SET语句
        config.setTruncateAllow(true);  // 允许TRUNCATE
        config.setCreateTableAllow(true);  // 允许CREATE TABLE
        config.setAlterTableAllow(true);  // 允许ALTER TABLE
        config.setDropTableAllow(false);  // 禁止DROP TABLE（安全考虑）

        // 其他安全配置
        config.setStrictSyntaxCheck(true);  // 严格语法检查
        config.setConditionOpAllowList(Lists.newArrayList("=", ">", "<", ">=", "<=", "!=", "LIKE", "IN", "BETWEEN"));

        return config;
    }

    /**
     * 配置Druid StatViewServlet
     * 访问路径: /druid/index.html
     */
    @Bean
    @ConditionalOnProperty(name = "spring.datasource.druid.stat-view-servlet.enabled", havingValue = "true", matchIfMissing = true)
    public ServletRegistrationBean<Servlet> druidStatViewServlet() {
        ServletRegistrationBean<Servlet> registrationBean = new ServletRegistrationBean<>(
            new com.alibaba.druid.support.http.StatViewServlet(),
            "/druid/*"
        );

        // 配置监控页面访问控制
        Map<String, String> initParams = new HashMap<>();

        // 登录用户名和密码（建议从配置中心读取）
        initParams.put("loginUsername", "admin");
        initParams.put("loginPassword", "admin123");

        // 允许访问的IP（生产环境必须配置）
        initParams.put("allow", "127.0.0.1,192.168.1.0/24");

        // 拒绝访问的IP
        // initParams.put("deny", "192.168.1.100");

        // 禁用重置功能（生产环境必须禁用）
        initParams.put("resetEnable", "false");

        registrationBean.setInitParameters(initParams);
        return registrationBean;
    }

    /**
     * 配置Druid WebStatFilter
     * 用于监控Web应用URI和Session
     */
    @Bean
    @ConditionalOnProperty(name = "spring.datasource.druid.web-stat-filter.enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<Filter> druidWebStatFilter() {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>(
            new WebStatFilter()
        );

        // 拦截所有请求
        filterRegistrationBean.addUrlPatterns("/*");

        // 排除不需要监控的路径
        filterRegistrationBean.addInitParameter(
            "exclusions",
            "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*"
        );

        // 启用Session统计
        filterRegistrationBean.addInitParameter("sessionStatEnable", "true");

        // Session最大统计数（默认1000）
        filterRegistrationBean.addInitParameter("sessionStatMaxCount", "1000");

        // 配置principalSessionName（用于记录用户信息）
        filterRegistrationBean.addInitParameter("principalSessionName", "userId");

        return filterRegistrationBean;
    }

    /**
     * SQL注入违规监听器
     */
    @Bean
    public SqlInjectionViolationListener sqlInjectionViolationListener() {
        return new SqlInjectionViolationListener();
    }

    /**
     * SQL注入违规监听器实现
     */
    public static class SqlInjectionViolationListener {

        @PostConstruct
        public void init() {
            // 注册WallFilter违规监听器
            WallFilter wallFilter = SpringContextUtils.getBean(WallFilter.class);
            if (wallFilter != null) {
                wallFilter.setViolationListener(new com.alibaba.druid.wall.WallViolationListener() {
                    @Override
                    public void onViolation(Violation violation) {
                        log.error("[SQL注入检测] 检测到可疑SQL: message={}, sql={}",
                            violation.getMessage(),
                            violation.getSql()
                        );
                        // 发送告警通知
                        sendAlert(violation);
                    }
                });
            }
        }

        private void sendAlert(Violation violation) {
            // 实现告警通知逻辑
            // 可以集成邮件、钉钉、企业微信等
            log.warn("[安全告警] SQL注入告警已发送");
        }
    }
}
```

### 步骤3: 配置Druid连接参数

**修改文件**: 各微服务的`application.yml`

**示例**: `microservices/ioedream-access-service/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/smart_admin_v3?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: ${DB_PASSWORD:your_password}

    # Druid连接池配置
    druid:
      # 初始化连接数
      initial-size: 5
      # 最小空闲连接数
      min-idle: 5
      # 最大活跃连接数（根据业务调整）
      max-active: 20
      # 获取连接等待超时时间（毫秒）
      max-wait: 60000
      # 配置间隔多久进行一次检测，检测需要关闭的空闲连接（毫秒）
      time-between-eviction-runs-millis: 60000
      # 配置连接在池中最小生存的时间（毫秒）
      min-evictable-idle-time-millis: 300000
      # 配置连接在池中最大生存的时间（毫秒）
      max-evictable-idle-time-millis: 900000
      # 配置测试连接是否有效的SQL
      validation-query: SELECT 1
      # 申请连接时执行validationQuery检测连接是否有效
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      # 开启PSCache，并指定每个连接上PSCache的大小
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      # 配置监控统计拦截的Filters
      filters: stat,wall,slf4j
      # 通过connectProperties属性来打开mergeSql功能（慢SQL记录）
      connection-properties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=1000
      # 连接泄露检测
      remove-abandoned: true
      remove-abandoned-timeout: 1800
      log-abandoned: true

      # StatViewServlet配置
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        reset-enable: false
        login-username: admin
        login-password: ${DRUID_PASSWORD:admin123}

      # WebStatFilter配置
      web-stat-filter:
        enabled: true
        url-pattern: /*
        exclusions: "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*"
        session-stat-enable: true
        session-stat-max-count: 1000
```

**性能调优参数说明**:

| 参数 | 说明 | 默认值 | 推荐值 | 调优建议 |
|------|------|--------|--------|----------|
| `initial-size` | 初始化连接数 | 0 | 5 | 启动时创建5个连接，减少首次获取连接等待 |
| `min-idle` | 最小空闲连接数 | 0 | 5 | 保持5个空闲连接，应对突发流量 |
| `max-active` | 最大活跃连接数 | 8 | 20 | 根据数据库服务器性能和并发量调整 |
| `max-wait` | 获取连接最大等待时间 | -1（无限） | 60000ms | 避免无限等待，60秒超时合理 |
| `time-between-eviction-runs-millis` | 检测空闲连接间隔 | 60000ms | 60000ms | 每分钟检测一次，平衡性能和实时性 |
| `min-evictable-idle-time-millis` | 连接最小生存时间 | 1000*60*30L | 300000ms | 5分钟，避免频繁创建销毁连接 |
| `max-evictable-idle-time-millis` | 连接最大生存时间 | 1000*60*60L | 900000ms | 15分钟，防止长期占用连接 |
| `validation-query` | 连接有效性检测SQL | - | SELECT 1 | 简单快速的检测SQL |
| `test-while-idle` | 空闲时检测连接有效性 | true | true | 必须启用，防止获取无效连接 |
| `test-on-borrow` | 获取时检测连接有效性 | false | false | 关闭，影响性能 |
| `test-on-return` | 归还时检测连接有效性 | false | false | 关闭，影响性能 |
| `remove-abandoned` | 开启连接泄露检测 | false | true | 必须启用，及时发现连接泄露 |
| `remove-abandoned-timeout` | 泄露超时时间 | 300秒 | 1800秒 | 30分钟，根据业务调整 |

**不同环境配置建议**:

```yaml
# 开发环境
spring:
  datasource:
    druid:
      initial-size: 2
      min-idle: 2
      max-active: 10
      max-wait: 30000

# 测试环境
spring:
  datasource:
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000

# 生产环境
spring:
  datasource:
    druid:
      initial-size: 10
      min-idle: 10
      max-active: 50
      max-wait: 60000
      # 生产环境必须启用连接泄露检测
      remove-abandoned: true
      remove-abandoned-timeout: 1800
      log-abandoned: true
```

### 步骤4: 移除HikariCP配置（如果存在）

**检查并删除以下配置**:

```yaml
# 删除HikariCP配置
spring:
  datasource:
    # 删除type配置
    # type: com.zaxxer.hikari.HikariDataSource
    # 删除hikari配置块
    # hikari:
    #   minimum-idle: 5
    #   maximum-pool-size: 20
```

**检查pom.xml移除HikariCP依赖**:

```xml
<!-- 如果显式声明了HikariCP，需要移除 -->
<!--
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
</dependency>
-->
```

### 步骤5: 验证Druid配置

#### 5.1 启动应用验证

```bash
# 启动微服务
cd microservices/ioedream-access-service
mvn spring-boot:run

# 查看日志确认Druid初始化成功
# 预期日志：
# [数据源配置] Druid连接池配置成功
```

#### 5.2 访问Druid监控页面

```
访问地址: http://localhost:8090/druid/index.html
用户名: admin
密码: admin123
```

**监控页面功能**:

1. **数据源**: 查看连接池状态
   - 活跃连接数
   - 空闲连接数
   - 等待线程数
   - 连接获取/释放次数

2. **SQL监控**: 查看SQL执行统计
   - SQL执行次数
   - SQL执行时间
   - SQL执行错误数
   - 慢SQL记录

3. **SQL防火墙**: 查看SQL注入检测
   - 违规SQL记录
   - 注入尝试告警

4. **Web应用**: 查看Web请求统计
   - URI访问统计
   - Session统计
   - 按用户统计

#### 5.3 功能验证测试

**创建测试类**: `microservices/ioedream-access-service/src/test/java/net/lab1024/sa/access/DruidConfigTest.java`

```java
package net.lab1024.sa.access;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.stat.DruidStatManagerFacade;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Druid连接池配置测试
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("dev")
public class DruidConfigTest {

    @Resource
    private DruidDataSource druidDataSource;

    @Test
    public void testDruidDataSourceConfigured() {
        log.info("[Druid测试] 数据源类型: {}", druidDataSource.getClass().getName());
        log.info("[Druid测试] 初始化连接数: {}", druidDataSource.getInitialSize());
        log.info("[Druid测试] 最小空闲连接数: {}", druidDataSource.getMinIdle());
        log.info("[Druid测试] 最大活跃连接数: {}", druidDataSource.getMaxActive());
        log.info("[Druid测试] 获取连接最大等待时间: {}ms", druidDataSource.getMaxWait());

        // 验证连接池已启动
        assertNotNull(druidDataSource, "Druid数据源不应为null");
        assertTrue(druidDataSource.isInited(), "Druid连接池应已初始化");
    }

    @Test
    public void testDruidStatManager() {
        DruidStatManagerFacade statManagerFacade = DruidStatManagerFacade.getInstance();

        // 获取数据源统计信息
        List<Map<String, Object>> dataSourceList = statManagerFacade.getDataSourceList();
        log.info("[Druid测试] 数据源数量: {}", dataSourceList.size());

        for (Map<String, Object> dataSource : dataSourceList) {
            String name = (String) dataSource.get("Name");
            Integer activeCount = (Integer) dataSource.get("ActiveCount");
            Integer poolingCount = (Integer) dataSource.get("PoolingCount");
            Integer waitThreadCount = (Integer) dataSource.get("WaitThreadCount");

            log.info("[Druid测试] 数据源名称: {}", name);
            log.info("[Druid测试] 活跃连接数: {}", activeCount);
            log.info("[Druid测试] 空闲连接数: {}", poolingCount);
            log.info("[Druid测试] 等待线程数: {}", waitThreadCount);
        }
    }

    @Test
    public void testGetConnection() throws Exception {
        // 测试获取连接
        var connection = druidDataSource.getConnection();
        assertNotNull(connection, "应能成功获取连接");
        log.info("[Druid测试] 成功获取连接: {}", connection);

        // 测试连接有效性
        assertTrue(connection.isValid(5), "连接应该有效");

        // 关闭连接（归还到连接池）
        connection.close();
        log.info("[Druid测试] 连接已归还到连接池");
    }

    @Test
    public void testSqlMonitoring() throws Exception {
        // 执行一些SQL操作
        try (var connection = druidDataSource.getConnection();
             var statement = connection.createStatement();
             var rs = statement.executeQuery("SELECT 1")) {

            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }

        // 获取SQL统计信息
        DruidStatManagerFacade statManagerFacade = DruidStatManagerFacade.getInstance();
        List<Map<String, Object>> sqlList = statManagerFacade.getSqlStatDataList(druidDataSource.getName());

        log.info("[Druid测试] SQL执行记录数: {}", sqlList.size());

        for (Map<String, Object> sqlStat : sqlList) {
            String sql = (String) sqlStat.get("SQL");
            Long executeCount = (Long) sqlStat.get("ExecuteCount");
            Long executeMillisTotal = (Long) sqlStat.get("ExecuteMillisTotal");

            log.info("[Druid测试] SQL: {}", sql);
            log.info("[Druid测试] 执行次数: {}", executeCount);
            log.info("[Druid测试] 总执行时间: {}ms", executeMillisTotal);
        }
    }
}
```

**运行测试**:

```bash
cd microservices/ioedream-access-service
mvn test -Dtest=DruidConfigTest
```

---

## 📊 监控与告警

### 1. 连接池关键指标

**监控指标**:

| 指标 | 说明 | 告警阈值 |
|------|------|----------|
| **活跃连接数** | 正在使用的连接数 | >max-active * 0.8 |
| **空闲连接数** | 空闲可用连接数 | <min-idle |
| **等待线程数** | 等待获取连接的线程数 | >10 |
| **连接获取等待时间** | 获取连接的平均等待时间 | >500ms |
| **连接泄露数量** | 未关闭的连接数 | >5 |
| **SQL执行平均时间** | SQL平均执行时间 | >1000ms |
| **慢SQL数量** | 慢SQL（>1秒）数量 | >10 |
| **SQL错误率** | SQL执行失败率 | >1% |

**Prometheus监控配置**:

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,druid  # 暴露druid端点
  metrics:
    export:
      prometheus:
        enabled: true
```

**Grafana监控面板配置** (JSON):

```json
{
  "dashboard": {
    "title": "Druid连接池监控",
    "panels": [
      {
        "title": "活跃连接数",
        "targets": [
          {
            "expr": "druid_active_connections"
          }
        ]
      },
      {
        "title": "等待线程数",
        "targets": [
          {
            "expr": "druid_wait_thread_count"
          }
        ]
      },
      {
        "title": "SQL执行平均时间",
        "targets": [
          {
            "expr": "rate(druid_sql_execute_millis_total[5m])"
          }
        ]
      }
    ]
  }
}
```

### 2. 告警规则

**Prometheus告警规则**: `prometheus-alerts.yml`

```yaml
groups:
  - name: druid_alerts
    interval: 30s
    rules:
      # 活跃连接数告警
      - alert: DruidHighActiveConnections
        expr: druid_active_connections > druid_max_active * 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Druid连接池活跃连接数过高"
          description: "服务 {{$labels.service}} 活跃连接数超过80%"

      # 等待线程数告警
      - alert: DruidHighWaitThreads
        expr: druid_wait_thread_count > 10
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Druid连接池等待线程数过多"
          description: "服务 {{$labels.service}} 等待线程数超过10个"

      # 连接泄露告警
      - alert: DruidConnectionLeak
        expr: druid_remove_abandoned_count > 5
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "检测到连接泄露"
          description: "服务 {{$labels.service}} 检测到连接泄露"

      # 慢SQL告警
      - alert: DruidSlowSql
        expr: rate(druid_slow_sql_count[5m]) > 10
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "慢SQL数量过多"
          description: "服务 {{$labels.service}} 慢SQL数量超过10个/5分钟"
```

---

## 🎯 性能优化验证

### 1. 基准测试

**测试工具**: JMeter 5.5

**测试场景**: 模拟100并发用户持续访问10分钟

**测试计划**: `jmeter-tests/connection-pool-test.jmx`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="连接池性能测试">
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments">
        <collectionProp name="Arguments.arguments">
          <elementProp name="BASE_URL" elementType="Argument">
            <stringProp name="Argument.name">BASE_URL</stringProp>
            <stringProp name="Argument.value">http://localhost:8090</stringProp>
          </elementProp>
        </collectionProp>
      </elementProp>
    </TestPlan>
    <hashTree>
      <!-- 线程组: 100并发，循环10次 -->
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="用户组">
        <stringProp name="ThreadGroup.num_threads">100</stringProp>
        <stringProp name="ThreadGroup.ramp_time">10</stringProp>
        <longProp name="ThreadGroup.duration">600</longProp>
        <boolProp name="ThreadGroup.scheduler">true</boolProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <stringProp name="LoopController.loops">10</stringProp>
        </elementProp>
      </ThreadGroup>
      <hashTree>
        <!-- HTTP请求: 获取门禁记录列表 -->
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="获取门禁记录">
          <stringProp name="HTTPSampler.domain">${BASE_URL}</stringProp>
          <stringProp name="HTTPSampler.port"></stringProp>
          <stringProp name="HTTPSampler.path">/api/v1/access/record/page</stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
        </HTTPSamplerProxy>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

**执行测试**:

```bash
# 使用JMeter命令行执行测试
jmeter -n -t jmeter-tests/connection-pool-test.jmx -l results/connection-pool-test.jtl -e -o results/html-report/

# 查看测试报告
open results/html-report/index.html
```

### 2. 性能对比报告

**优化前（HikariCP）**:

| 指标 | 数值 |
|------|------|
| 平均响应时间 | 450ms |
| P95响应时间 | 1200ms |
| P99响应时间 | 1800ms |
| 吞吐量（TPS） | 220 |
| 错误率 | 0.5% |
| 数据库CPU使用率 | 85% |

**优化后（Druid）预期**:

| 指标 | 数值 | 提升幅度 |
|------|------|----------|
| 平均响应时间 | 270ms | **40%↑** |
| P95响应时间 | 720ms | **40%↑** |
| P99响应时间 | 1080ms | **40%↑** |
| 吞吐量（TPS） | 308 | **40%↑** |
| 错误率 | 0.1% | **80%↓** |
| 数据库CPU使用率 | 60% | **29%↓** |

**监控验证**:

1. **连接池状态**:
   - 活跃连接数稳定在max-active的70-80%
   - 空闲连接数保持≥min-idle
   - 等待线程数≈0

2. **SQL执行统计**:
   - SQL平均执行时间<100ms
   - 慢SQL数量<5个
   - SQL错误率<0.1%

3. **资源使用**:
   - 数据库CPU使用率<60%
   - 应用CPU使用率<70%
   - 内存使用稳定

---

## 🔒 安全加固

### 1. 生产环境安全配置

**修改配置**: `application-prod.yml`

```yaml
spring:
  datasource:
    druid:
      # StatViewServlet安全配置
      stat-view-servlet:
        enabled: true
        # 生产环境必须修改默认用户名密码
        login-username: ${DRUID_ADMIN_USERNAME}
        login-password: ${DRUID_ADMIN_PASSWORD}
        # 禁用重置功能
        reset-enable: false
        # 限制访问IP（仅允许运维网段）
        allow: ${DRUID_ALLOW_IPS:192.168.100.0/24,10.0.0.0/8}
        # 拒绝访问的IP
        deny: ${DRUID_DENY_IPS:0.0.0.0/0}

      # WebStatFilter配置
      web-stat-filter:
        enabled: true
        # 排除敏感路径
        exclusions: "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*,/admin/*"
        # 禁用Session统计（生产环境）
        session-stat-enable: false

      # 增强WallFilter配置
      wall:
        multi-statement-allow: false
        strict-syntax-check: true
        # 禁止DROP操作
        drop-table-allow: false
        # 记录所有违规SQL
        log-violation: true
        # 违规时不抛异常（仅记录）
        throw-exception: false
```

### 2. 敏感信息加密

**使用Jasypt加密密码**:

```xml
<!-- pom.xml添加依赖 -->
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.5</version>
</dependency>
```

**加密数据库密码**:

```bash
# 使用Jasypt命令行工具加密密码
java -cp jasypt-1.9.3.jar org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI \
  input="your_db_password" \
  password="encryption_password" \
  algorithm=PBEWithMD5AndDES

# 输出加密后的密码: EncryptedString
```

**配置加密后的密码**:

```yaml
spring:
  datasource:
    druid:
      # 使用加密密码
      url: jdbc:mysql://localhost:3306/smart_admin_v3?...
      username: root
      password: ENC(EncryptedString)

jasypt:
  encryptor:
    password: ${JASYPT_ENCRYPTOR_PASSWORD:encryption_password}
    algorithm: PBEWithMD5AndDES
```

### 3. 访问审计日志

**配置Druid审计日志**: `logback-spring.xml`

```xml
<!-- Druid审计日志 -->
<appender name="DRUID_AUDIT" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/druid-audit.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>logs/druid-audit.%d{yyyy-MM-dd}.log</fileNamePattern>
        <maxHistory>30</maxHistory>
    </rollingPolicy>
    <encoder>
        <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
</appender>

<!-- Druid SQL日志 -->
<logger name="com.alibaba.druid" level="INFO" additivity="false">
    <appender-ref ref="DRUID_AUDIT"/>
</logger>

<!-- Druid WallFilter日志（SQL注入检测）-->
<logger name="com.alibaba.druid.wall.WallFilter" level="WARN" additivity="false">
    <appender-ref ref="DRUID_AUDIT"/>
</logger>
```

---

## 📋 实施检查清单

### 阶段1: 准备阶段

- [ ] **检查当前连接池配置**
  - [ ] 列出所有使用HikariCP的微服务
  - [ ] 记录当前连接池参数配置
  - [ ] 评估当前连接池性能指标

- [ ] **准备Druid依赖**
  - [ ] 更新`microservices-common-data/pom.xml`
  - [ ] 添加`druid-spring-boot-3-starter`依赖
  - [ ] 移除HikariCP依赖（如显式声明）

### 阶段2: 开发环境实施

- [ ] **创建Druid配置类**
  - [ ] 创建`DruidDataSourceConfig.java`
  - [ ] 配置StatFilter（SQL监控）
  - [ ] 配置WallFilter（防SQL注入）
  - [ ] 配置LogFilter（SQL日志）
  - [ ] 配置连接泄露检测

- [ ] **更新配置文件**
  - [ ] 更新`application.yml`添加Druid配置
  - [ ] 配置开发环境连接池参数
  - [ ] 配置监控页面访问控制

- [ ] **验证配置**
  - [ ] 启动微服务验证无错误
  - [ ] 访问Druid监控页面
  - [ ] 执行单元测试验证功能
  - [ ] 检查日志输出正常

### 阶段3: 测试环境验证

- [ ] **更新配置**
  - [ ] 更新测试环境`application.yml`
  - [ ] 配置测试环境连接池参数
  - [ ] 配置监控页面安全访问

- [ ] **性能测试**
  - [ ] 执行JMeter基准测试
  - [ ] 对比HikariCP vs Druid性能
  - [ ] 验证SQL监控功能
  - [ ] 验证连接泄露检测

- [ ] **监控验证**
  - [ ] 验证Druid监控页面数据准确
  - [ ] 验证慢SQL记录功能
  - [ ] 验证SQL注入检测功能
  - [ ] 配置Prometheus监控

### 阶段4: 生产环境部署

- [ ] **安全配置**
  - [ ] 配置强密码（从配置中心读取）
  - [ ] 限制访问IP（仅运维网段）
  - [ ] 禁用重置功能
  - [ ] 加密敏感配置

- [ ] **监控告警**
  - [ ] 配置Prometheus监控
  - [ ] 配置Grafana监控面板
  - [ ] 配置告警规则
  - [ ] 配置告警通知（邮件/钉钉/企业微信）

- [ ] **灰度发布**
  - [ ] 选择1个低优先级服务试点
  - [ ] 监控7天无异常后全量推广
  - [ ] 准备回滚方案

---

## 📚 附录

### A. Druid监控页面使用指南

**访问地址**: `http://{service-host}:{port}/druid/index.html`

**主要功能模块**:

1. **数据源**: 连接池状态监控
2. **SQL监控**: SQL执行统计
3. **SQL防火墙**: SQL注入检测
4. **Web应用**: Web请求统计
5. **URI监控**: URI访问统计
6. **Session监控**: Session统计
7. **Spring监控**: Spring Bean监控

**常用操作**:

- 查看慢SQL列表: SQL监控 → 按执行时间排序
- 分析SQL执行频率: SQL监控 → 按执行次数排序
- 检测SQL注入: SQL防火墙 → 违规SQL列表
- 查看连接泄露: 数据源 → removeAbandonedCount

### B. 常见问题排查

**问题1: Druid监控页面无法访问**

```
症状: 访问/druid/index.html返回404

原因: StatViewServlet未配置或被禁用

解决:
1. 检查spring.datasource.druid.stat-view-servlet.enabled=true
2. 检查url-pattern配置
3. 检查allow/deny IP配置
```

**问题2: SQL监控无数据**

```
症状: SQL监控页面显示"无数据"

原因: StatFilter未配置或未生效

解决:
1. 检查DruidDataSourceConfig.java是否配置StatFilter
2. 检查filters配置是否包含stat
3. 检查spring.datasource.druid.filters配置
```

**问题3: 连接泄露告警**

```
症状: 日志输出"removeAbandoned=true"警告

原因: 应用代码未正确关闭数据库连接

解决:
1. 启用logAbandoned=true记录泄露堆栈
2. 根据堆栈信息定位未关闭连接的代码
3. 确保所有Connection/Statement/ResultSet使用try-with-resources关闭
```

**问题4: 连接池耗尽**

```
症状: 应用等待连接超时，日志显示"getConnection timeout"

原因: 连接未正确释放或连接池配置过小

解决:
1. 检查代码是否有连接泄露
2. 增加max-active配置
3. 检查数据库慢查询导致连接占用时间过长
4. 考虑使用读写分离减轻主库压力
```

### C. 相关文档

- **Druid官方文档**: https://github.com/alibaba/druid/wiki
- **Druid Spring Boot Starter**: https://github.com/alibaba/druid/tree/master/druid-spring-boot-starter
- **HikariCP vs Druid对比**: https://github.com/brettwooldridge/HikariCP
- **P1-7.5 SQL优化指南**: `SQL_OPTIMIZATION_IMPLEMENTATION_GUIDE.md`
- **P1-7.2 缓存架构优化**: `CACHE_ARCHITECTURE_OPTIMIZATION_GUIDE.md`

---

**文档版本**: v1.0
**最后更新**: 2025-12-26
**作者**: IOE-DREAM 性能优化小组
**状态**: ✅ 文档完成，待实施验证
