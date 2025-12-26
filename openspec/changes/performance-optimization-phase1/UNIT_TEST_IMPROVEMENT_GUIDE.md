# 单元测试完善实施指南 (P1-9.1)

> **项目**: IOE-DREAM智慧园区管理平台
> **任务**: P1-9.1 单元测试完善
> **完成日期**: 2025-12-26
> **实施周期**: 8人天
> **状态**: 📝 文档完成，待实施验证

---

## 📋 执行摘要

本指南提供了全面的单元测试完善方法论，确保企业级代码质量。

### 核心目标

- ✅ **核心业务测试覆盖率**: 100%
- ✅ **普通业务测试覆盖率**: ≥80%
- ✅ **测试代码质量**: 遵循企业级测试规范
- ✅ **CI/CD集成**: 自动化测试执行

### 覆盖范围

| 服务模块 | 目标覆盖率 | 优先级 | 预计工时 |
|---------|-----------|--------|----------|
| **考勤服务** | 100% (核心) / 80% (普通) | P0 | 2人天 |
| **门禁服务** | 100% (核心) / 80% (普通) | P0 | 1.5人天 |
| **消费服务** | 100% (核心) / 80% (普通) | P0 | 1.5人天 |
| **视频服务** | 100% (核心) / 80% (普通) | P0 | 1人天 |
| **访客服务** | 100% (核心) / 80% (普通) | P1 | 1人天 |
| **公共模块** | 90% | P1 | 1人天 |

---

## 🎯 测试覆盖率标准

### 1. 核心业务识别

**核心业务特征**：
- 涉及资金交易（消费支付）
- 涉及安全控制（门禁通行）
- 涉及合规性要求（考勤计算）
- 涉及用户数据（认证授权）

**核心业务模块清单**：

```
考勤服务 (100%覆盖):
├── 策略计算（标准/轮班/弹性）✅ 已完成
├── 打卡记录处理
├── 排班管理
├── 考勤汇总统计
└── 异常检测

门禁服务 (100%覆盖):
├── 通行权限验证 ⭐ 核心中的核心
├── 反潜回控制 ⭐ 核心中的核心
├── 生物识别验证 ⭐ 核心中的核心
├── 门禁控制指令
└── 移动端认证 ⭐ 已完成部分

消费服务 (100%覆盖):
├── 账户余额检查 ⭐ 核心中的核心
├── 支付扣款 ⭐ 核心中的核心
├── 补贴发放 ⭐ 核心中的核心
├── 充值退款
└── 离线同步 ⭐ 核心中的核心

视频服务 (100%覆盖):
├── 设备连接管理 ⭐ 核心中的核心
├── AI事件处理 ⭐ 核心中的核心
├── 录像回放控制
└── 实时预览流

访客服务 (100%覆盖):
├── 访客预约审批 ⭐ 核心中的核心
├── 访客码生成验证 ⭐ 核心中的核心
├── 生物特征采集 ⭐ 核心中的核心
└── 访客轨迹记录
```

### 2. 覆盖率计算方法

**使用JaCoCo插件统计**：

```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>jacoco-check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>PACKAGE</element>
                        <limits>
                            <!-- 核心业务100%覆盖 -->
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>1.00</minimum>
                            </limit>
                            <!-- 普通业务80%覆盖 -->
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 3. 覆盖率验证命令

```bash
# 运行测试并生成覆盖率报告
mvn clean test jacoco:report

# 查看覆盖率报告
open target/site/jacoco/index.html

# 验证覆盖率达标
mvn clean verify
```

---

## 🛠️ 测试框架和工具

### 1. 核心技术栈

```xml
<!-- 测试依赖 -->
<dependencies>
    <!-- JUnit 5 - 测试引擎 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.1</version>
        <scope>test</scope>
    </dependency>

    <!-- Mockito - Mock框架 -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>5.7.0</version>
        <scope>test</scope>
    </dependency>

    <!-- Mockito JUnit 5 扩展 -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-junit-jupiter</artifactId>
        <version>5.7.0</version>
        <scope>test</scope>
    </dependency>

    <!-- Spring Boot Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- AssertJ - 断言库 -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <version>3.24.2</version>
        <scope>test</scope>
    </dependency>

    <!-- H2数据库 - 测试数据库 -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 2. 测试基类配置

**Service层测试基类**：

```java
package net.lab1024.sa.{service}.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Service层测试基类
 * 提供通用的Mock和测试工具方法
 */
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Service层单元测试基类")
public abstract class BaseServiceTest {

    /**
     * 初始化测试环境
     * 子类可以重写此方法进行特定配置
     */
    @BeforeEach
    void setUp() {
        // 初始化测试数据
        initTestData();
        // 配置反射工具
        configureReflectionTestUtils();
    }

    /**
     * 初始化测试数据
     */
    protected void initTestData() {
        // 子类实现
    }

    /**
     * 配置反射工具
     * 用于设置私有字段和调用私有方法
     */
    protected void configureReflectionTestUtils() {
        // 子类可以重写
    }

    /**
     * 创建测试数据构建器
     */
    protected <T> TestDataBuilder<T> createBuilder(Class<T> clazz) {
        return new TestDataBuilder<>(clazz);
    }
}
```

**Controller层测试基类**：

```java
package net.lab1024.sa.{service}.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller层测试基类
 * 提供MockMvc测试支持
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Controller层单元测试基类")
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        initTestData();
    }

    /**
     * 初始化测试数据
     */
    protected void initTestData() {
        // 子类实现
    }

    /**
     * 构建GET请求
     */
    protected ResultActions performGet(String url, Object... params) throws Exception {
        MockHttpServletRequestBuilder request = get(url, params);
        return mockMvc.perform(request);
    }

    /**
     * 构建POST请求
     */
    protected ResultActions performPost(String url, Object body) throws Exception {
        MockHttpServletRequestBuilder request = post(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body));
        return mockMvc.perform(request);
    }

    /**
     * 构建PUT请求
     */
    protected ResultActions performPut(String url, Object body) throws Exception {
        MockHttpServletRequestBuilder request = put(url)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body));
        return mockMvc.perform(request);
    }

    /**
     * 构建DELETE请求
     */
    protected ResultActions performDelete(String url) throws Exception {
        return mockMvc.perform(delete(url));
    }
}
```

---

## 📝 测试命名和组织规范

### 1. 测试类命名

| 被测类 | 测试类命名 | 示例 |
|--------|-----------|------|
| UserService | UserServiceTest | ✅ |
| AccessController | AccessControllerTest | ✅ |
| UserManager | UserManagerTest | ✅ |
| WorkShiftStrategy | WorkShiftStrategyTest | ✅ |

### 2. 测试方法命名

**标准命名模式**：

```java
// 模式: test_{方法名}_{场景}_{预期结果}
@Test
@DisplayName("测试用户登录 - 正常登录 - 返回用户信息")
void testLogin_success_normalLogin_returnUserInfo() {
    // given
    // when
    // then
}

// 模式: test_{方法名}_{异常场景}_{预期异常}
@Test
@DisplayName("测试用户登录 - 密码错误 - 抛出认证异常")
void testLogin_failure_invalidPassword_throwsAuthenticationException() {
    // given
    // when
    // then
}

// 模式: test_{方法名}_{边界条件}_{预期结果}
@Test
@DisplayName("测试分页查询 - 页码为0 - 自动修正为1")
void testQueryPage_pageNumZero_autoCorrectToOne() {
    // given
    // when
    // then
}
```

### 3. 测试目录结构

```
src/test/java/
├── net/lab1024/sa/{service}/
│   ├── controller/                    # Controller测试
│   │   ├── UserControllerTest.java
│   │   ├── AccessControllerTest.java
│   │   └── mobile/
│   │       └── AccessMobileControllerTest.java
│   ├── service/                       # Service测试
│   │   ├── impl/
│   │   │   ├── UserServiceImplTest.java
│   │   │   └── AccessServiceImplTest.java
│   │   └── UserServiceTest.java (接口测试)
│   ├── manager/                       # Manager测试
│   │   ├── UserManagerTest.java
│   │   └── AccessManagerTest.java
│   ├── dao/                           # DAO测试
│   │   ├── UserDaoTest.java
│   │   └── AccessRecordDaoTest.java
│   ├── strategy/                      # 策略测试
│   │   └── impl/
│   │       ├── StandardWorkingHoursStrategyTest.java
│   │       └── ShiftWorkingHoursStrategyTest.java
│   ├── domain/                        # 领域对象测试
│   │   ├── form/
│   │   │   └── UserAddFormTest.java
│   │   └── vo/
│   │       └── UserVOTest.java
│   ├── util/                          # 工具类测试
│   │   └── DateUtilsTest.java
│   ├── config/                        # 配置测试
│   │   └── SecurityConfigTest.java
│   └── test/                          # 测试基类和工具
│       ├── BaseServiceTest.java
│       ├── BaseControllerTest.java
│       ├── TestDataBuilder.java
│       └── MockDataFactory.java
```

---

## 🔍 各层测试最佳实践

### 1. Controller层测试

**测试重点**：
- HTTP请求/响应正确性
- 参数验证
- 异常处理
- 状态码和响应格式

**完整示例**：

```java
package net.lab1024.sa.access.controller;

import net.lab1024.sa.access.test.BaseControllerTest;
import net.lab1024.sa.access.domain.form.AccessRecordQueryForm;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.domain.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 门禁记录控制器测试
 */
@DisplayName("门禁记录控制器测试")
class AccessRecordControllerTest extends BaseControllerTest {

    @Test
    @WithMockUser(username = "admin")
    @DisplayName("查询门禁记录 - 正常分页 - 返回分页数据")
    void testQueryPage_success_returnPageResult() throws Exception {
        // given
        AccessRecordQueryForm form = new AccessRecordQueryForm();
        form.setPageNum(1);
        form.setPageSize(20);

        // when
        performPost("/api/access/record/queryPage", form)

        // then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200))
        .andExpect(jsonPath("$.data.list").isArray())
        .andExpect(jsonPath("$.data.total").isNumber())
        .andExpect(jsonPath("$.data.pageNum").value(1))
        .andExpect(jsonPath("$.data.pageSize").value(20));
    }

    @Test
    @WithMockUser(username = "admin")
    @DisplayName("查询门禁记录 - 页码为0 - 自动修正为1")
    void testQueryPage_pageNumZero_autoCorrectToOne() throws Exception {
        // given
        AccessRecordQueryForm form = new AccessRecordQueryForm();
        form.setPageNum(0);  // 无效页码
        form.setPageSize(20);

        // when
        performPost("/api/access/record/queryPage", form)

        // then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.pageNum").value(1));  // 自动修正为1
    }

    @Test
    @DisplayName("查询门禁记录 - 未登录 - 返回401")
    void testQueryPage_notLoggedIn_return401() throws Exception {
        // given
        AccessRecordQueryForm form = new AccessRecordQueryForm();

        // when
        performPost("/api/access/record/queryPage", form)

        // then
        .andExpect(status().isUnauthorized());
    }
}
```

### 2. Service层测试

**测试重点**：
- 业务逻辑正确性
- 异常处理
- 数据转换
- 第三方调用

**完整示例**：

```java
package net.lab1024.sa.access.service.impl;

import net.lab1024.sa.access.manager.AccessManager;
import net.lab1024.sa.access.test.BaseServiceTest;
import net.lab1024.sa.common.domain.exception.BusinessException;
import net.lab1024.sa.common.organization.entity.AccessRecordEntity;
import net.lab1024.sa.common.organization.dao.AccessRecordDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 门禁服务实现测试
 */
@DisplayName("门禁服务实现测试")
class AccessServiceImplTest extends BaseServiceTest {

    @Mock
    private AccessRecordDao accessRecordDao;

    @Mock
    private AccessManager accessManager;

    @InjectMocks
    private AccessServiceImpl accessService;

    @Test
    @DisplayName("验证门禁通行 - 有权限 - 允许通行")
    void testVerifyAccess_hasPermission_allowAccess() {
        // given
        Long userId = 1L;
        Long deviceId = 100L;

        AccessRecordEntity mockRecord = new AccessRecordEntity();
        mockRecord.setUserId(userId);
        mockRecord.setDeviceId(deviceId);
        mockRecord.setAccessResult(1);  // 允许通行

        when(accessManager.validateAccess(userId, deviceId))
            .thenReturn(true);
        when(accessRecordDao.insert(any(AccessRecordEntity.class)))
            .thenReturn(1);

        // when
        AccessRecordEntity result = accessService.verifyAccess(userId, deviceId);

        // then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(deviceId, result.getDeviceId());
        assertEquals(1, result.getAccessResult());

        verify(accessManager, times(1)).validateAccess(userId, deviceId);
        verify(accessRecordDao, times(1)).insert(any(AccessRecordEntity.class));
    }

    @Test
    @DisplayName("验证门禁通行 - 无权限 - 拒绝通行")
    void testVerifyAccess_noPermission_denyAccess() {
        // given
        Long userId = 1L;
        Long deviceId = 100L;

        when(accessManager.validateAccess(userId, deviceId))
            .thenReturn(false);

        // when & then
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> accessService.verifyAccess(userId, deviceId)
        );

        assertEquals("ACCESS_DENIED", exception.getCode());
        assertEquals("无通行权限", exception.getMessage());

        verify(accessManager, times(1)).validateAccess(userId, deviceId);
        verify(accessRecordDao, never()).insert(any());
    }

    @Test
    @DisplayName("验证门禁通行 - 用户ID为null - 抛出异常")
    void testVerifyAccess_userIdNull_throwsException() {
        // given
        Long userId = null;
        Long deviceId = 100L;

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accessService.verifyAccess(userId, deviceId)
        );

        assertEquals("用户ID不能为空", exception.getMessage());

        verify(accessManager, never()).validateAccess(any(), any());
        verify(accessRecordDao, never()).insert(any());
    }
}
```

### 3. Manager层测试

**测试重点**：
- 复杂业务编排
- DAO调用组合
- 业务规则验证
- 数据聚合和转换

**完整示例**：

```java
package net.lab1024.sa.access.manager;

import net.lab1024.sa.access.test.BaseServiceTest;
import net.lab1024.sa.common.organization.dao.AccessRecordDao;
import net.lab1024.sa.common.organization.dao.AreaDeviceDao;
import net.lab1024.sa.common.organization.entity.AccessRecordEntity;
import net.lab1024.sa.common.organization.entity.AreaDeviceEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 门禁管理器测试
 */
@DisplayName("门禁管理器测试")
class AccessManagerTest extends BaseServiceTest {

    @InjectMocks
    private AccessManager accessManager;

    @Mock
    private AccessRecordDao accessRecordDao;

    @Mock
    private AreaDeviceDao areaDeviceDao;

    @Test
    @DisplayName("验证通行权限 - 用户有区域权限 - 返回true")
    void testValidateAccess_userHasAreaPermission_returnTrue() {
        // given
        Long userId = 1L;
        Long deviceId = 100L;
        LocalDateTime now = LocalDateTime.now();

        // Mock设备关联区域
        AreaDeviceEntity areaDevice = new AreaDeviceEntity();
        areaDevice.setDeviceId(deviceId.toString());
        areaDevice.setAreaId(10L);
        areaDevice.setRelationStatus(1);  // 正常状态

        when(areaDeviceDao.selectByDeviceId(deviceId.toString()))
            .thenReturn(Arrays.asList(areaDevice));

        // Mock用户区域权限
        when(accessRecordDao.selectUserAreaPermission(userId, 10L, now))
            .thenReturn(1);  // 有权限

        // when
        boolean result = accessManager.validateAccess(userId, deviceId);

        // then
        assertTrue(result);

        verify(areaDeviceDao, times(1))
            .selectByDeviceId(deviceId.toString());
        verify(accessRecordDao, times(1))
            .selectUserAreaPermission(userId, 10L, now);
    }

    @Test
    @DisplayName("验证通行权限 - 设备未关联区域 - 返回false")
    void testValidateAccess_deviceNotLinkedArea_returnFalse() {
        // given
        Long userId = 1L;
        Long deviceId = 100L;

        when(areaDeviceDao.selectByDeviceId(deviceId.toString()))
            .thenReturn(Collections.emptyList());

        // when
        boolean result = accessManager.validateAccess(userId, deviceId);

        // then
        assertFalse(result);

        verify(areaDeviceDao, times(1))
            .selectByDeviceId(deviceId.toString());
        verify(accessRecordDao, never())
            .selectUserAreaPermission(anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("获取用户通行记录 - 正常查询 - 返回记录列表")
    void testGetUserAccessRecords_normalQuery_returnRecords() {
        // given
        Long userId = 1L;
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        List<AccessRecordEntity> mockRecords = Arrays.asList(
            createMockAccessRecord(1L, userId, 100L, 1, startTime),
            createMockAccessRecord(2L, userId, 101L, 1, startTime.plusDays(1))
        );

        when(accessRecordDao.selectUserRecords(userId, startTime, endTime))
            .thenReturn(mockRecords);

        // when
        List<AccessRecordEntity> result = accessManager
            .getUserAccessRecords(userId, startTime, endTime);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(userId, result.get(0).getUserId());

        verify(accessRecordDao, times(1))
            .selectUserRecords(userId, startTime, endTime);
    }

    // ==================== 辅助方法 ====================

    private AccessRecordEntity createMockAccessRecord(
        Long recordId, Long userId, Long deviceId,
        Integer result, LocalDateTime time
    ) {
        AccessRecordEntity record = new AccessRecordEntity();
        record.setRecordId(recordId);
        record.setUserId(userId);
        record.setDeviceId(deviceId);
        record.setAccessResult(result);
        record.setPassTime(time);
        return record;
    }
}
```

### 4. DAO层测试

**测试重点**：
- SQL正确性
- 参数绑定
- 结果映射
- 事务处理

**完整示例**：

```java
package net.lab1024.sa.common.organization.dao;

import net.lab1024.sa.test.BaseDaoTest;
import net.lab1024.sa.common.organization.entity.AccessRecordEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 门禁记录DAO测试
 */
@DisplayName("门禁记录DAO测试")
class AccessRecordDaoTest extends BaseDaoTest {

    @Autowired
    private AccessRecordDao accessRecordDao;

    @Test
    @DisplayName("插入门禁记录 - 正常插入 - 返回1")
    void testInsert_success_return1() {
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
        assertNotNull(record.getRecordId());  // 主键自动生成
    }

    @Test
    @DisplayName("根据ID查询 - 记录存在 - 返回记录")
    void testSelectById_recordExists_returnRecord() {
        // given
        Long recordId = 1L;

        // when
        AccessRecordEntity result = accessRecordDao.selectById(recordId);

        // then
        assertNotNull(result);
        assertEquals(recordId, result.getRecordId());
    }

    @Test
    @DisplayName("根据ID查询 - 记录不存在 - 返回null")
    void testSelectById_recordNotExists_returnNull() {
        // given
        Long recordId = 99999L;

        // when
        AccessRecordEntity result = accessRecordDao.selectById(recordId);

        // then
        assertNull(result);
    }

    @Test
    @DisplayName("分页查询 - 正常分页 - 返回分页数据")
    void testSelectPage_normalPage_returnPageData() {
        // given
        int pageNum = 1;
        int pageSize = 20;

        // when
        List<AccessRecordEntity> result = accessRecordDao
            .selectPage(pageNum, pageSize);

        // then
        assertNotNull(result);
        assertTrue(result.size() <= pageSize);
    }
}
```

### 5. 策略类测试

**测试重点**：
- 算法正确性
- 边界条件
- 规则验证
- 异常处理

**完整示例**：

```java
package net.lab1024.sa.attendance.strategy.impl;

import net.lab1024.sa.attendance.strategy.WorkingHoursStrategy;
import net.lab1024.sa.attendance.test.BaseServiceTest;
import net.lab1024.sa.attendance.domain.entity.WorkShiftRuleEntity;
import net.lab1024.sa.attendance.domain.vo.AttendanceResultVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 标准工时制策略测试
 */
@DisplayName("标准工时制策略测试")
class StandardWorkingHoursStrategyTest extends BaseServiceTest {

    private final WorkingHoursStrategy strategy = new StandardWorkingHoursStrategy();

    @Test
    @DisplayName("测试上班打卡 - 正常打卡（宽限期内） - 正常状态")
    void testCalculateCheckIn_normalWithinGracePeriod_normalStatus() {
        // given
        WorkShiftRuleEntity rule = createMockRule();
        rule.setWorkStartTime(LocalTime.of(9, 0));
        rule.setGracePeriod(10);  // 10分钟宽限期

        AttendanceResultVO result = new AttendanceResultVO();
        result.setPunchTime(LocalDate.now().atTime(9, 5));  // 宽限期内
        result.setPunchType(1);  // 上班打卡

        // when
        strategy.calculate(rule, result);

        // then
        assertEquals("NORMAL", result.getStatus());
        assertEquals(0L, result.getLateDuration());
        assertEquals("正常上班", result.getRemark());
    }

    @Test
    @DisplayName("测试上班打卡 - 迟到（超过宽限期） - 迟到状态")
    void testCalculateCheckIn_lateExceedGracePeriod_lateStatus() {
        // given
        WorkShiftRuleEntity rule = createMockRule();
        rule.setWorkStartTime(LocalTime.of(9, 0));
        rule.setGracePeriod(10);

        AttendanceResultVO result = new AttendanceResultVO();
        result.setPunchTime(LocalDate.now().atTime(9, 15));  // 迟到15分钟
        result.setPunchType(1);

        // when
        strategy.calculate(rule, result);

        // then
        assertEquals("LATE", result.getStatus());
        assertEquals(15L, result.getLateDuration());
        assertTrue(result.getRemark().contains("迟到"));
    }

    @Test
    @DisplayName("测试下班打卡 - 加班（满足最小时长） - 加班状态")
    void testCalculateCheckOut_overtimeMeetMinHours_overtimeStatus() {
        // given
        WorkShiftRuleEntity rule = createMockRule();
        rule.setWorkStartTime(LocalTime.of(9, 0));
        rule.setWorkEndTime(LocalTime.of(18, 0));
        rule.setMinWorkHours(8.0);  // 最少8小时

        AttendanceResultVO result = new AttendanceResultVO();
        result.setCheckInTime(LocalDate.now().atTime(9, 0));
        result.setPunchTime(LocalDate.now().atTime(19, 30));  // 加班1.5小时
        result.setPunchType(2);  // 下班打卡

        // when
        strategy.calculate(rule, result);

        // then
        assertEquals("OVERTIME", result.getStatus());
        assertEquals(90L, result.getOvertimeDuration());  // 90分钟加班
        assertTrue(result.getRemark().contains("加班"));
    }

    // ==================== 辅助方法 ====================

    private WorkShiftRuleEntity createMockRule() {
        WorkShiftRuleEntity rule = new WorkShiftRuleEntity();
        rule.setShiftId(1L);
        rule.setShiftName("正常班");
        rule.setRuleType(1);  // 标准工时制
        rule.setWorkStartTime(LocalTime.of(9, 0));
        rule.setWorkEndTime(LocalTime.of(18, 0));
        rule.setGracePeriod(10);
        rule.setMinWorkHours(8.0);
        return rule;
    }
}
```

---

## 🎭 Mock和测试数据管理

### 1. 测试数据工厂

```java
package net.lab1024.sa.test;

import net.lab1024.sa.common.organization.entity.UserEntity;
import net.lab1024.sa.common.organization.entity.DepartmentEntity;
import net.lab1024.sa.common.organization.entity.AccessRecordEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mock数据工厂
 * 提供各种实体的Mock数据生成
 */
public class MockDataFactory {

    private static final AtomicLong idGenerator = new AtomicLong(1);

    // ==================== 用户实体 ====================

    /**
     * 创建默认用户
     */
    public static UserEntity createDefaultUser() {
        return createUser("测试用户", "test@example.com");
    }

    /**
     * 创建用户
     */
    public static UserEntity createUser(String username, String email) {
        UserEntity user = new UserEntity();
        user.setUserId(idGenerator.getAndIncrement());
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone("13800138000");
        user.setStatus(1);  // 启用
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return user;
    }

    /**
     * 创建管理员用户
     */
    public static UserEntity createAdminUser() {
        UserEntity admin = createUser("管理员", "admin@example.com");
        admin.setIsAdmin(1);
        return admin;
    }

    // ==================== 部门实体 ====================

    /**
     * 创建默认部门
     */
    public static DepartmentEntity createDefaultDepartment() {
        return createDepartment("测试部门", 1L);
    }

    /**
     * 创建部门
     */
    public static DepartmentEntity createDepartment(String deptName, Long parentId) {
        DepartmentEntity dept = new DepartmentEntity();
        dept.setDeptId(idGenerator.getAndIncrement());
        dept.setDeptName(deptName);
        dept.setParentId(parentId);
        dept.setStatus(1);  // 启用
        dept.setCreateTime(LocalDateTime.now());
        dept.setUpdateTime(LocalDateTime.now());
        return dept;
    }

    // ==================== 门禁记录实体 ====================

    /**
     * 创建默认门禁记录
     */
    public static AccessRecordEntity createDefaultAccessRecord() {
        return createAccessRecord(1L, 100L, 1, LocalDateTime.now());
    }

    /**
     * 创建门禁记录
     */
    public static AccessRecordEntity createAccessRecord(
        Long userId, Long deviceId, Integer result, LocalDateTime passTime
    ) {
        AccessRecordEntity record = new AccessRecordEntity();
        record.setRecordId(idGenerator.getAndIncrement());
        record.setUserId(userId);
        record.setDeviceId(deviceId);
        record.setAccessResult(result);  // 1-允许 2-拒绝
        record.setPassTime(passTime);
        record.setCreateTime(LocalDateTime.now());
        return record;
    }

    /**
     * 批量创建门禁记录
     */
    public static List<AccessRecordEntity> createAccessRecords(
        Long userId, Long deviceId, int count
    ) {
        List<AccessRecordEntity> records = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now();

        for (int i = 0; i < count; i++) {
            records.add(createAccessRecord(
                userId,
                deviceId,
                1,  // 允许通行
                baseTime.minusHours(i)
            ));
        }

        return records;
    }
}
```

### 2. 测试数据构建器

```java
package net.lab1024.sa.test;

import net.lab1024.sa.common.organization.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * 测试数据构建器
 * 使用Builder模式构建测试数据
 */
public class TestDataBuilder<T> {

    private final Class<T> clazz;
    private T instance;

    public TestDataBuilder(Class<T> clazz) {
        this.clazz = clazz;
        try {
            this.instance = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("无法创建实例: " + clazz.getName(), e);
        }
    }

    /**
     * 设置属性值
     */
    public TestDataBuilder<T> set(String fieldName, Object value) {
        try {
            var field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException("设置字段失败: " + fieldName, e);
        }
        return this;
    }

    /**
     * 使用Consumer设置多个属性
     */
    public TestDataBuilder<T> with(Consumer<T> consumer) {
        consumer.accept(instance);
        return this;
    }

    /**
     * 构建实例
     */
    public T build() {
        return instance;
    }

    // ==================== 便捷方法 ====================

    /**
     * 创建用户构建器
     */
    public static TestDataBuilder<UserEntity> userBuilder() {
        return new TestDataBuilder<>(UserEntity.class)
            .set("userId", 1L)
            .set("username", "测试用户")
            .set("email", "test@example.com")
            .set("status", 1)
            .set("createTime", LocalDateTime.now())
            .set("updateTime", LocalDateTime.now());
    }
}
```

### 3. Mock工具类

```java
package net.lab1024.sa.test;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Mock工具类
 * 提供常用的Mock验证方法
 */
public class MockUtil {

    /**
     * 验证方法只调用一次
     */
    public static <T> void verifyCalledOnce(T mock) {
        verify(mock, times(1));
    }

    /**
     * 验证方法从未调用
     */
    public static <T> void verifyNeverCalled(T mock) {
        verify(mock, never());
    }

    /**
     * 验证方法调用指定次数
     */
    public static <T> void verifyCalledTimes(T mock, int times) {
        verify(mock, times(times));
    }

    /**
     * 捕获方法调用参数
     */
    public static <T> T captureArgument(ArgumentCaptor<T> captor) {
        return captor.getValue();
    }

    /**
     * 捕获方法调用所有参数
     */
    public static <T> List<T> captureAllArguments(ArgumentCaptor<T> captor) {
        return captor.getAllValues();
    }

    /**
     * 重置Mock
     */
    public static void resetMocks(Object... mocks) {
        Mockito.reset(mocks);
    }
}
```

---

## ✅ 测试检查清单

### 单个测试用例检查清单

```markdown
## 测试用例检查清单

### 命名和结构
- [ ] 测试类命名: XxxTest
- [ ] 测试方法命名: test_{方法名}_{场景}_{预期结果}
- [ ] 使用@DisplayName描述测试意图
- [ ] 遵循AAA模式（given-when-then）

### 测试覆盖
- [ ] 正常路径测试
- [ ] 边界条件测试
- [ ] 异常场景测试
- [ ] 空值/null值测试

### 断言完整性
- [ ] 验证返回值
- [ ] 验证状态变化
- [ ] 验证异常（如适用）
- [ ] 验证Mock调用次数

### 代码质量
- [ ] 无硬编码数据
- [ ] 使用测试数据工厂
- [ ] 测试独立（不依赖执行顺序）
- [ ] 测试可重复执行

### 日志和注释
- [ ] 关键步骤有注释
- [ ] 测试意图清晰
- [ ] 测试数据有意义
```

### 测试类检查清单

```markdown
## 测试类检查清单

### 类级别
- [ ] 测试基类继承（如适用）
- [ ] @DisplayName类描述
- [ ] 类JavaDoc注释
- [ ] 测试覆盖所有public方法

### 测试方法覆盖
- [ ] 正常流程测试
- [ ] 异常流程测试
- [ ] 边界条件测试
- [ ] 并发测试（如适用）

### Mock使用
- [ ] 合理使用Mock隔离依赖
- [ ] 验证Mock调用
- [ ] 不Mock被测类
- [ ] Mock返回值设置完整

### 测试数据
- [ ] 使用测试数据工厂
- [ ] 测试数据独立
- [ ] 清理测试数据
- [ ] 使用事务回滚（DAO测试）
```

---

## 🚀 CI/CD集成

### 1. Maven配置

```xml
<!-- pom.xml -->
<build>
    <plugins>
        <!-- JaCoCo覆盖率插件 -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <executions>
                <execution>
                    <id>prepare-agent</id>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
                <execution>
                    <id>jacoco-check</id>
                    <goals>
                        <goal>check</goal>
                    </goals>
                    <configuration>
                        <rules>
                            <rule>
                                <element>PACKAGE</element>
                                <limits>
                                    <!-- 核心业务100%覆盖 -->
                                    <limit>
                                        <counter>LINE</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>1.00</minimum>
                                    </limit>
                                    <!-- 普通业务80%覆盖 -->
                                    <limit>
                                        <counter>LINE</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>0.80</minimum>
                                    </limit>
                                </limits>
                            </rule>
                        </rules>
                    </configuration>
                </execution>
            </executions>
        </plugin>

        <!-- Surefire测试插件 -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.0.0-M9</version>
            <configuration>
                <includes>
                    <include>**/*Test.java</include>
                    <include>**/*Tests.java</include>
                </includes>
                <excludes>
                    <exclude>**/*IntegrationTest.java</exclude>
                    <exclude>**/*IT.java</exclude>
                </excludes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### 2. GitHub Actions配置

```yaml
# .github/workflows/unit-test.yml
name: 单元测试

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: 设置JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: 'maven'

    - name: 运行单元测试
      run: mvn clean test

    - name: 生成覆盖率报告
      run: mvn jacoco:report

    - name: 上传覆盖率报告
      uses: codecov/codecov-action@v3
      with:
        files: target/site/jacoco/jacoco.xml
        flags: unittests
        name: codecov-umbrella

    - name: 检查覆盖率达标
      run: mvn jacoco:check

    - name: 发布测试报告
      uses: actions/upload-artifact@v3
      with:
        name: test-report
        path: target/surefire-reports/
```

---

## 📊 测试执行和报告

### 1. 本地测试执行

```bash
# 运行所有测试
mvn clean test

# 运行指定测试类
mvn test -Dtest=AccessServiceImplTest

# 运行指定测试方法
mvn test -Dtest=AccessServiceImplTest#testVerifyAccess

# 跳过测试
mvn clean install -DskipTests

# 生成测试报告
mvn clean test surefire-report:report
```

### 2. 覆盖率报告查看

```bash
# 生成覆盖率报告
mvn clean test jacoco:report

# 查看HTML报告
open target/site/jacoco/index.html

# 查看覆盖率统计
mvn jacoco:check
```

### 3. 覆盖率报告解读

**JaCoCo报告指标**：

- **Line Coverage**: 行覆盖率（代码行执行比例）
- **Branch Coverage**: 分支覆盖率（if/switch分支执行比例）
- **Method Coverage**: 方法覆盖率（方法执行比例）
- **Class Coverage**: 类覆盖率（类加载比例）

**目标标准**：
- 核心业务: Line Coverage ≥ 100%, Branch Coverage ≥ 95%
- 普通业务: Line Coverage ≥ 80%, Branch Coverage ≥ 70%

---

## 📚 相关文档

- **P0单元测试完成报告**: [P0_UNIT_TEST_COMPLETE_REPORT.md](../../documentation/archive/reports/P0_UNIT_TEST_COMPLETE_REPORT.md)
- **考勤服务测试实施**: [ATTENDANCE_SERVICE_TEST_OPTIMIZATION_REPORT.md](../../microservices/ioedream-attendance-service/SERVICE_TEST_OPTIMIZATION_REPORT.md)
- **Maven测试验证指南**: [maven-test-verify-guide.md](../../openspec/changes/archive/2025-12-13-refactor-platform-hardening/artifacts/maven-test-verify-guide.md)
- **JUnit 5用户指南**: [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- **Mockito文档**: [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

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
- **实施周期**: 8人天
- **技术栈**: JUnit 5 + Mockito + Spring Boot Test + JaCoCo

---

## 🎯 总结

本指南提供了全面的单元测试完善方法论，涵盖：

- ✅ **测试覆盖率标准**: 核心业务100%、普通业务≥80%
- ✅ **测试框架配置**: JUnit 5 + Mockito + Spring Boot Test
- ✅ **各层测试最佳实践**: Controller、Service、Manager、DAO、Strategy
- ✅ **测试数据管理**: Mock工厂、数据构建器、Mock工具
- ✅ **CI/CD集成**: Maven + JaCoCo + GitHub Actions
- ✅ **测试检查清单**: 确保测试质量和完整性

**下一步**: 继续P1-9.2集成测试完善，然后P1-9.4性能测试。

---

**报告生成时间**: 2025-12-26
**报告状态**: ✅ 文档完成，待实际验证
