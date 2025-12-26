# 集成测试完善实施指南 (P1-9.2)

> **项目**: IOE-DREAM智慧园区管理平台
> **任务**: P1-9.2 集成测试完善
> **完成日期**: 2025-12-26
> **实施周期**: 6人天
> **状态**: 📝 文档完成，待实施验证

---

## 📋 执行摘要

本指南提供了全面的集成测试完善方法论，确保系统各组件正确协作。

### 核心目标

- ✅ **API集成测试覆盖率**: ≥70%
- ✅ **数据库集成测试**: 完整CRUD和事务验证
- ✅ **服务间集成测试**: RPC调用和消息传递
- ✅ **端到端场景测试**: 完整业务流程验证

### 覆盖范围

| 测试类型 | 目标覆盖率 | 优先级 | 预计工时 |
|---------|-----------|--------|----------|
| **API集成测试** | ≥70% | P0 | 2人天 |
| **数据库集成测试** | 100% | P0 | 1.5人天 |
| **服务间调用测试** | ≥80% | P0 | 1.5人天 |
| **端到端流程测试** | 核心流程100% | P1 | 1人天 |

---

## 🎯 集成测试策略

### 1. 测试金字塔

```
        /\
       /E2E\          端到端测试 (少量)
      /------\
     /  集成  \        集成测试 (适量) ← 本指南重点
    /----------\
   /   单元测试   \     单元测试 (大量) ← P1-9.1
  /--------------\
```

**集成测试定位**：
- ✅ 验证组件间协作正确性
- ✅ 验证数据库集成和事务
- ✅ 验证API接口完整性
- ✅ 验证服务间调用
- ❌ 不替代单元测试（单元测试更快速、更精确）

### 2. 测试分层

```
┌─────────────────────────────────────┐
│  E2E测试: 完整业务流程               │  ← 最少、最慢
│  (用户登录 → 门禁通行 → 记录查询)   │
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│  API集成测试: HTTP接口测试           │  ← 本指南重点
│  (Controller → Service → DAO → DB)  │
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│  服务集成测试: 服务间调用            │  ← 本指南重点
│  (Access Service → Common Service)  │
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│  数据库集成测试: 数据持久化          │  ← 本指南重点
│  (DAO → MyBatis → MySQL)            │
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│  单元测试: 独立类测试                │  ← P1-9.1完成
│  (Service/Manager/DAO)             │
└─────────────────────────────────────┘
```

---

## 🛠️ 测试框架和工具

### 1. 核心技术栈

```xml
<!-- 集成测试依赖 -->
<dependencies>
    <!-- Spring Boot Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- TestRestTemplate - REST API测试 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- TestContainers - Docker集成测试 -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>testcontainers</artifactId>
        <version>1.19.1</version>
        <scope>test</scope>
    </dependency>

    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>mysql</artifactId>
        <version>1.19.1</version>
        <scope>test</scope>
    </dependency>

    <!-- H2数据库 - 内存数据库测试 -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- RestAssured - REST API测试DSL -->
    <dependency>
        <groupId>io.rest-assured</groupId>
        <artifactId>rest-assured</artifactId>
        <version>5.3.2</version>
        <scope>test</scope>
    </dependency>

    <!-- Awaitility - 异步等待 -->
    <dependency>
        <groupId>org.awaitility</groupId>
        <artifactId>awaitility</artifactId>
        <version>4.2.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 2. 测试配置

**application-test.yml**：

```yaml
# 测试环境配置
spring:
  # 使用H2内存数据库
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password: ""

  # JPA配置
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
        format_sql: false

  # Redis使用嵌入式
  data:
    redis:
      host: localhost
      port: 6370
      database: 15  # 使用独立的测试数据库

# 日志配置
logging:
  level:
    root: WARN
    net.lab1024.sa: INFO
    org.springframework.test: INFO
```

**测试基类**：

```java
package net.lab1024.sa.test;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

/**
 * 集成测试基类
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        initTestData();
    }

    /**
     * 初始化测试数据
     */
    protected void initTestData() {
        // 子类实现
    }
}
```

---

## 📝 API集成测试

### 1. Controller集成测试

**测试重点**：
- HTTP请求/响应完整性
- 参数验证
- 异常处理
- 安全认证
- 响应格式

**完整示例**：

```java
package net.lab1024.sa.access.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.lab1024.sa.test.BaseIntegrationTest;
import net.lab1024.sa.access.domain.form.AccessRecordQueryForm;
import net.lab1024.sa.common.domain.ResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 门禁记录Controller集成测试
 */
@AutoConfigureMockMvc
@DisplayName("门禁记录Controller集成测试")
class AccessRecordControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("查询门禁记录 - 正常分页 - 返回分页数据")
    void testQueryPage_success_returnPageResult() throws Exception {
        // given
        AccessRecordQueryForm form = new AccessRecordQueryForm();
        form.setPageNum(1);
        form.setPageSize(20);
        form.setStartTime(LocalDateTime.now().minusDays(7));
        form.setEndTime(LocalDateTime.now());

        // when
        mockMvc.perform(post("/api/access/record/queryPage")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form)))

        // then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.message").value("success"))
        .andExpect(jsonPath("$.data").exists())
        .andExpect(jsonPath("$.data.list").isArray())
        .andExpect(jsonPath("$.data.total").isNumber())
        .andExpect(jsonPath("$.data.pageNum").value(1))
        .andExpect(jsonPath("$.data.pageSize").value(20))
        .andExpect(jsonPath("$.data.pages").isNumber());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("查询单条门禁记录 - 记录存在 - 返回记录详情")
    void testGetById_recordExists_returnRecord() throws Exception {
        // given
        Long recordId = 1L;

        // when
        mockMvc.perform(get("/api/access/record/" + recordId))

        // then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.recordId").value(recordId))
        .andExpect(jsonPath("$.data.userId").isNumber())
        .andExpect(jsonPath("$.data.deviceId").isNumber())
        .andExpect(jsonPath("$.data.accessResult").isNumber());
    }

    @Test
    @DisplayName("查询门禁记录 - 未登录 - 返回401")
    void testQueryPage_notLoggedIn_return401() throws Exception {
        // given
        AccessRecordQueryForm form = new AccessRecordQueryForm();

        // when
        mockMvc.perform(post("/api/access/record/queryPage")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form)))

        // then
        .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("查询门禁记录 - 权限不足 - 返回403")
    void testQueryPage_insufficientPermission_return403() throws Exception {
        // given
        AccessRecordQueryForm form = new AccessRecordQueryForm();

        // when
        mockMvc.perform(post("/api/access/record/queryPage")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form)))

        // then
        .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("查询门禁记录 - 参数验证失败 - 返回400")
    void testQueryPage_validationFailed_return400() throws Exception {
        // given
        AccessRecordQueryForm form = new AccessRecordQueryForm();
        form.setPageNum(0);  // 无效页码
        form.setPageSize(1000);  // 超出限制

        // when
        mockMvc.perform(post("/api/access/record/queryPage")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form)))

        // then
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(400))
        .andExpect(jsonPath("$.message").value(containsString("参数验证失败")));
    }
}
```

### 2. 使用RestAssured的API测试

**优势**: 更简洁的DSL，更适合REST API测试

```java
package net.lab1024.sa.access.controller;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import net.lab1024.sa.test.BaseIntegrationTest;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * 门禁API集成测试 - RestAssured版本
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("门禁API集成测试 (RestAssured)")
class AccessApiIntegrationTest extends BaseIntegrationTest {

    @BeforeAll
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/api";
    }

    @Test
    @DisplayName("GET /access/record/{id} - 记录存在 - 返回200")
    void testGetRecordById_exists() {
        // when & then
        given()
            .auth().basic("admin", "admin")
            .pathParam("id", 1)
        .when()
            .get("/access/record/{id}")
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data.recordId", equalTo(1))
            .body("data.userId", notNullValue())
            .body("data.deviceId", notNullValue());
    }

    @Test
    @DisplayName("GET /access/record/{id} - 记录不存在 - 返回404")
    void testGetRecordById_notExists() {
        // when & then
        given()
            .auth().basic("admin", "admin")
            .pathParam("id", 99999)
        .when()
            .get("/access/record/{id}")
        .then()
            .statusCode(200)
            .body("code", equalTo(404))
            .body("message", equalTo("记录不存在"));
    }

    @Test
    @DisplayName("POST /access/record/queryPage - 正常分页 - 返回分页数据")
    void testQueryRecordsPage_success() {
        // when & then
        given()
            .auth().basic("admin", "admin")
            .contentType("application/json")
            .body("{\"pageNum\":1,\"pageSize\":20}")
        .when()
            .post("/access/record/queryPage")
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data.list", notNullValue())
            .body("data.total", notNullValue())
            .body("data.pageNum", equalTo(1))
            .body("data.pageSize", equalTo(20));
    }

    @Test
    @DisplayName("POST /access/verify - 验证通行 - 允许通行")
    void testVerifyAccess_allowed() {
        // when & then
        given()
            .auth().basic("admin", "admin")
            .contentType("application/json")
            .body("{\"userId\":1,\"deviceId\":100}")
        .when()
            .post("/access/verify")
        .then()
            .statusCode(200)
            .body("code", equalTo(200))
            .body("data.accessResult", equalTo(1))
            .body("data.message", equalTo("允许通行"));
    }
}
```

---

## 🗄️ 数据库集成测试

### 1. DAO集成测试

**测试重点**：
- SQL正确性
- MyBatis映射
- 事务管理
- 数据库约束

**完整示例**：

```java
package net.lab1024.sa.common.organization.dao;

import net.lab1024.sa.test.BaseIntegrationTest;
import net.lab1024.sa.common.organization.entity.AccessRecordEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 门禁记录DAO集成测试
 */
@DisplayName("门禁记录DAO集成测试")
class AccessRecordDaoIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AccessRecordDao accessRecordDao;

    @Test
    @DisplayName("插入门禁记录 - 正常插入 - 返回1")
    @Sql(scripts = "/sql/clean-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testInsert_success() {
        // given
        AccessRecordEntity record = new AccessRecordEntity();
        record.setUserId(1L);
        record.setDeviceId(100L);
        record.setAccessResult(1);
        record.setPassTime(LocalDateTime.now());

        // when
        int result = accessRecordDao.insert(record);

        // then
        assertEquals(1, result);
        assertNotNull(record.getRecordId());

        // 验证数据库中存在
        AccessRecordEntity inserted = accessRecordDao.selectById(record.getRecordId());
        assertNotNull(inserted);
        assertEquals(1L, inserted.getUserId());
        assertEquals(100L, inserted.getDeviceId());
    }

    @Test
    @DisplayName("更新门禁记录 - 正常更新 - 返回1")
    @Sql(scripts = {
        "/sql/clean-test-data.sql",
        "/sql/insert-test-record.sql"
    })
    void testUpdate_success() {
        // given
        AccessRecordEntity record = accessRecordDao.selectById(1L);
        assertNotNull(record);
        record.setAccessResult(2);  // 修改为拒绝通行

        // when
        int result = accessRecordDao.updateById(record);

        // then
        assertEquals(1, result);

        // 验证数据库已更新
        AccessRecordEntity updated = accessRecordDao.selectById(1L);
        assertEquals(2, updated.getAccessResult());
    }

    @Test
    @DisplayName("删除门禁记录 - 逻辑删除 - 返回1")
    @Sql(scripts = {
        "/sql/clean-test-data.sql",
        "/sql/insert-test-record.sql"
    })
    void testDeleteById_logicDelete_success() {
        // given
        Long recordId = 1L;

        // when
        int result = accessRecordDao.deleteById(recordId);

        // then
        assertEquals(1, result);

        // 验证逻辑删除（deletedFlag=1）
        AccessRecordEntity deleted = accessRecordDao.selectById(recordId);
        assertNull(deleted);  // 逻辑删除后查询不到
    }

    @Test
    @DisplayName("批量查询 - 用户ID查询 - 返回记录列表")
    @Sql(scripts = {
        "/sql/clean-test-data.sql",
        "/sql/insert-test-records.sql"
    })
    void testSelectBatchIds_success() {
        // given
        List<Long> recordIds = List.of(1L, 2L, 3L);

        // when
        List<AccessRecordEntity> records = accessRecordDao.selectBatchIds(recordIds);

        // then
        assertNotNull(records);
        assertEquals(3, records.size());
    }

    @Test
    @DisplayName("分页查询 - 正常分页 - 返回分页数据")
    @Sql(scripts = {
        "/sql/clean-test-data.sql",
        "/sql/insert-test-records.sql"
    })
    void testSelectPage_success() {
        // given
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AccessRecordEntity> page =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 20);

        // when
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AccessRecordEntity> result =
            accessRecordDao.selectPage(page, null);

        // then
        assertNotNull(result);
        assertNotNull(result.getRecords());
        assertTrue(result.getTotal() > 0);
        assertEquals(1, result.getCurrent());
        assertEquals(20, result.getSize());
    }

    @Test
    @DisplayName("事务测试 - 插入失败回滚 - 数据库无变化")
    @Sql(scripts = "/sql/clean-test-data.sql")
    void testTransaction_insertFailed_rollback() {
        // given
        AccessRecordEntity record = new AccessRecordEntity();
        record.setUserId(1L);
        record.setDeviceId(100L);
        record.setAccessResult(1);
        record.setPassTime(LocalDateTime.now());

        // when & then
        assertThrows(Exception.class, () -> {
            accessRecordDao.insert(record);
            // 模拟异常
            throw new RuntimeException("模拟异常");
        });

        // 验证回滚（数据库中没有记录）
        List<AccessRecordEntity> records = accessRecordDao.selectList(null);
        assertTrue(records.isEmpty());
    }
}
```

### 2. TestContainers集成测试

**优势**: 使用真实MySQL数据库，更接近生产环境

```java
package net.lab1024.sa.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * TestContainers集成测试基类
 * 使用真实MySQL数据库进行测试
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
public abstract class BaseTestContainersTest {

    @Container
    private static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .withInitScript("sql/init-test-db.sql");

    /**
     * 获取测试数据库URL
     */
    protected static String getJdbcUrl() {
        return mysqlContainer.getJdbcUrl();
    }

    @Test
    void testContainerIsRunning() {
        assertTrue(mysqlContainer.isRunning());
    }
}
```

**application-testcontainers.yml**：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: ${TEST_DB_URL}
    username: ${TEST_DB_USER}
    password: ${TEST_DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
```

---

## 🔄 服务间集成测试

### 1. RPC调用测试

**测试重点**：
- 服务间通信正确性
- 参数序列化/反序列化
- 超时处理
- 异常处理

**完整示例**：

```java
package net.lab1024.sa.access.integration;

import net.lab1024.sa.test.BaseIntegrationTest;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.organization.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 门禁服务集成测试 - 服务间调用
 */
@DisplayName("门禁服务集成测试 - 服务间调用")
class AccessServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private GatewayServiceClient gatewayServiceClient;

    @Test
    @DisplayName("调用用户服务 - 查询用户信息 - 返回用户")
    void testCallUserService_returnUser() {
        // when
        ResponseDTO<UserEntity> response = gatewayServiceClient.callCommonService(
            "/api/user/getById",
            HttpMethod.GET,
            null,
            new com.fasterxml.jackson.core.type.TypeReference<ResponseDTO<UserEntity>>() {}
        );

        // then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertNotNull(response.getData().getUserId());
    }

    @Test
    @DisplayName("调用设备服务 - 查询设备信息 - 返回设备")
    void testCallDeviceService_returnDevice() {
        // when
        ResponseDTO<DeviceEntity> response = gatewayServiceClient.callDeviceService(
            "/api/device/getInfo",
            HttpMethod.GET,
            null,
            new com.fasterxml.jackson.core.type.TypeReference<ResponseDTO<DeviceEntity>>() {}
        );

        // then
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
    }

    @Test
    @DisplayName("调用用户服务 - 用户不存在 - 返回404")
    void testCallUserService_userNotExists_return404() {
        // when
        ResponseDTO<UserEntity> response = gatewayServiceClient.callCommonService(
            "/api/user/getById",
            HttpMethod.GET,
            null,
            new com.fasterxml.jackson.core.type.TypeReference<ResponseDTO<UserEntity>>() {}
        );

        // then
        assertNotNull(response);
        assertEquals(404, response.getCode());
    }
}
```

### 2. 消息队列集成测试

**测试重点**：
- 消息发送和接收
- 消息格式正确性
- 消息重试机制
- 死信队列处理

**完整示例**：

```java
package net.lab1024.sa.access.integration;

import net.lab1024.sa.test.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 消息队列集成测试
 */
@DisplayName("消息队列集成测试")
class MessageQueueIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    @DisplayName("发送门禁通行消息 - 消息发送成功")
    void testSendAccessMessage_success() {
        // given
        String message = "{\"userId\":1,\"deviceId\":100,\"result\":1}";

        // when
        rabbitTemplate.convertAndSend("access.exchange", "access.record", message);

        // then
        // 验证消息发送（无异常）
        assertTrue(true);
    }

    @Test
    @DisplayName("消费门禁通行消息 - 消息处理成功")
    @DisplayName("等待消息消费完成")
    void testConsumeAccessMessage_processed() {
        // given
        String message = "{\"userId\":1,\"deviceId\":100,\"result\":1}";

        // when
        rabbitTemplate.convertAndSend("access.exchange", "access.record", message);

        // then - 等待消息被消费（最多5秒）
        await().atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                // 验证消息处理结果
                // 例如：检查数据库中的记录
                AccessRecordEntity record = accessRecordDao.selectLatestRecord(1L);
                assertNotNull(record);
                assertEquals(1, record.getAccessResult());
            });
    }
}
```

---

## 🎭 端到端流程测试

### 1. 完整业务流程测试

**测试重点**：
- 多个服务协作
- 完整业务场景
- 数据一致性
- 异常恢复

**示例：门禁通行完整流程**

```java
package net.lab1024.sa.access.e2e;

import net.lab1024.sa.test.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 门禁通行端到端测试
 * 完整流程：用户识别 → 权限验证 → 门禁控制 → 记录存储 → 通知推送
 */
@DisplayName("门禁通行端到端测试")
class AccessPassE2ETest extends BaseIntegrationTest {

    @Test
    @DisplayName("完整通行流程 - 有权限用户 - 成功通行")
    void testCompleteAccessFlow_userHasPermission_success() {
        // 1. 用户识别（设备端完成）
        Long userId = identifyUser("FACE_001");
        assertNotNull(userId);
        assertEquals(1L, userId);

        // 2. 权限验证
        boolean hasPermission = validateAccessPermission(userId, 100L);
        assertTrue(hasPermission);

        // 3. 门禁控制
        boolean doorOpened = openDoor(100L);
        assertTrue(doorOpened);

        // 4. 记录存储
        AccessRecordEntity record = saveAccessRecord(userId, 100L, 1);
        assertNotNull(record);
        assertNotNull(record.getRecordId());
        assertEquals(1, record.getAccessResult());

        // 5. 通知推送
        boolean notificationSent = sendNotification(record);
        assertTrue(notificationSent);

        // 6. 验证完整流程结果
        AccessRecordEntity finalRecord = accessRecordDao.selectById(record.getRecordId());
        assertNotNull(finalRecord);
        assertEquals(1, finalRecord.getAccessResult());
    }

    @Test
    @DisplayName("完整通行流程 - 无权限用户 - 拒绝通行")
    void testCompleteAccessFlow_userNoPermission_denied() {
        // 1. 用户识别
        Long userId = identifyUser("FACE_002");
        assertNotNull(userId);
        assertEquals(2L, userId);

        // 2. 权限验证
        boolean hasPermission = validateAccessPermission(userId, 100L);
        assertFalse(hasPermission);

        // 3. 门禁不打开
        boolean doorOpened = openDoor(100L);
        assertFalse(doorOpened);

        // 4. 记录拒绝通行
        AccessRecordEntity record = saveAccessRecord(userId, 100L, 2);
        assertNotNull(record);
        assertEquals(2, record.getAccessResult());

        // 5. 验证记录
        AccessRecordEntity finalRecord = accessRecordDao.selectById(record.getRecordId());
        assertNotNull(finalRecord);
        assertEquals(2, finalRecord.getAccessResult());
    }

    // ==================== 辅助方法 ====================

    private Long identifyUser(String biometricId) {
        // 模拟设备端识别
        if ("FACE_001".equals(biometricId)) {
            return 1L;
        } else if ("FACE_002".equals(biometricId)) {
            return 2L;
        }
        return null;
    }

    private boolean validateAccessPermission(Long userId, Long deviceId) {
        // 调用权限验证服务
        return accessManager.validateAccess(userId, deviceId);
    }

    private boolean openDoor(Long deviceId) {
        // 模拟开门控制
        return true;
    }

    private AccessRecordEntity saveAccessRecord(Long userId, Long deviceId, int result) {
        AccessRecordEntity record = new AccessRecordEntity();
        record.setUserId(userId);
        record.setDeviceId(deviceId);
        record.setAccessResult(result);
        record.setPassTime(LocalDateTime.now());
        accessRecordDao.insert(record);
        return record;
    }

    private boolean sendNotification(AccessRecordEntity record) {
        // 模拟通知推送
        return true;
    }
}
```

---

## ✅ 集成测试检查清单

### API集成测试检查清单

```markdown
## API集成测试检查清单

### 请求测试
- [ ] GET请求正常返回
- [ ] POST请求正常创建
- [ ] PUT请求正常更新
- [ ] DELETE请求正常删除
- [ ] 请求参数验证

### 响应验证
- [ ] HTTP状态码正确
- [ ] 响应格式符合契约
- [ ] 响应数据完整
- [ ] 错误信息清晰

### 安全测试
- [ ] 认证测试
- [ ] 授权测试
- [ ] 权限测试
- [ ] SQL注入测试

### 异常测试
- [ ] 404 Not Found
- [ ] 400 Bad Request
- [ ] 401 Unauthorized
- [ ] 403 Forbidden
- [ ] 500 Internal Server Error
```

### 数据库集成测试检查清单

```markdown
## 数据库集成测试检查清单

### CRUD测试
- [ ] Create: 插入记录
- [ ] Read: 查询记录
- [ ] Update: 更新记录
- [ ] Delete: 删除记录

### 事务测试
- [ ] 正常提交
- [ ] 异常回滚
- [ ] 并发更新
- [ ] 死锁处理

### 约束测试
- [ ] 主键约束
- [ ] 外键约束
- [ ] 唯一约束
- [ ] 非空约束

### 性能测试
- [ ] 批量操作性能
- [ ] 索引使用验证
- [ ] 慢查询检测
```

---

## 🚀 CI/CD集成

### 1. Maven配置

```xml
<!-- pom.xml -->
<build>
    <plugins>
        <!-- Failsafe插件 - 集成测试 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-failsafe-plugin</artifactId>
            <version>3.0.0-M9</version>
            <executions>
                <execution>
                    <goals>
                        <goal>integration-test</goal>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <includes>
                    <include>**/*IntegrationTest.java</include>
                    <include>**/*IT.java</include>
                </includes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 2. GitHub Actions配置

```yaml
# .github/workflows/integration-test.yml
name: 集成测试

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  integration-test:
    runs-on: ubuntu-latest

    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: test
          MYSQL_DATABASE: testdb
        ports:
          - 3306:3306
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3

      redis:
        image: redis:7
        ports:
          - 6379:6379
        options: >-
          --health-cmd="redis-cli ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3

    steps:
    - uses: actions/checkout@v3

    - name: 设置JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: 'maven'

    - name: 等待服务就绪
      run: |
        while ! mysqladmin ping -h 127.0.0.1 -u root -ptest --silent; do
          echo "等待MySQL就绪..."
          sleep 2
        done
        while ! redis-cli ping; do
          echo "等待Redis就绪..."
          sleep 2
        done

    - name: 运行集成测试
      run: mvn clean verify
      env:
        TEST_DB_URL: jdbc:mysql://localhost:3306/testdb
        TEST_DB_USER: root
        TEST_DB_PASSWORD: test

    - name: 发布测试报告
      if: always()
      uses: actions/upload-artifact@v3
      with:
        name: integration-test-report
        path: target/failsafe-reports/
```

---

## 📚 相关文档

- **单元测试完善指南**: [UNIT_TEST_IMPROVEMENT_GUIDE.md](./UNIT_TEST_IMPROVEMENT_GUIDE.md)
- **P0集成测试报告**: [P1_INTEGRATION_TEST_COMPLETE_REPORT.md](../../documentation/archive/reports/P1_INTEGRATION_TEST_COMPLETE_REPORT.md)
- **Spring Boot Test文档**: [Spring Boot Test Features](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- **RestAssured文档**: [RestAssured Getting Started](https://rest-assured.io/)
- **TestContainers文档**: [TestContainers Introduction](https://www.testcontainers.org/)

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
- **实施周期**: 6人天
- **技术栈**: Spring Boot Test + TestContainers + RestAssured

---

## 🎯 总结

本指南提供了全面的集成测试完善方法论，涵盖：

- ✅ **API集成测试**: HTTP接口完整性和契约验证
- ✅ **数据库集成测试**: SQL正确性、事务、约束验证
- ✅ **服务间集成测试**: RPC调用、消息队列
- ✅ **端到端流程测试**: 完整业务场景验证
- ✅ **测试框架配置**: Spring Boot Test + TestContainers
- ✅ **CI/CD集成**: Maven Failsafe + GitHub Actions

**下一步**: 继续P1-9.4性能测试实施指南。

---

**报告生成时间**: 2025-12-26
**报告状态**: ✅ 文档完成，待实际验证
