# 测试完善能力规格

**能力ID**: testing-improvement
**优先级**: P0
**创建日期**: 2025-01-30
**状态**: 提案中

---

## ADDED Requirements

### REQ-TEST-001: 单元测试覆盖

**优先级**: P0
**需求描述**: 所有新增代码必须编写单元测试，达到指定的覆盖率要求。

**场景**:

#### Scenario: Manager层单元测试

**Given** 新增Manager类
**When** 编写单元测试
**Then** 应该满足：
- 测试覆盖率 > 80%
- 所有公共方法都有测试用例
- 包含正常场景和异常场景

**示例**:
```java
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AiModelManagerTest {

    @Autowired
    private AiModelManager aiModelManager;

    @MockBean
    private MinioClient minioClient;

    @MockBean
    private AiModelDao aiModelDao;

    @Test
    void uploadModel_shouldSaveModel() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("model.onnx");

        AiModelUploadForm form = new AiModelUploadForm();
        form.setModelName("Test Model");
        form.setModelVersion("1.0.0");

        // When
        AiModelEntity result = aiModelManager.uploadModel(file, form);

        // Then
        assertNotNull(result);
        assertEquals("Test Model", result.getModelName());
        verify(minioClient).uploadFile(any(), any());
        verify(aiModelDao).insert(any());
    }

    @Test
    void uploadModel_fileTooLarge_shouldThrowException() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(600L * 1024 * 1024); // 600MB

        // When & Then
        assertThrows(BusinessException.class, () -> {
            aiModelManager.uploadModel(file, new AiModelUploadForm());
        });
    }
}
```

**覆盖率目标**:
| 层级 | 目标覆盖率 | 工具 |
|------|-----------|------|
| Manager层 | > 80% | JaCoCo |
| Service层 | > 75% | JaCoCo |
| Controller层 | > 60% | JaCoCo |

---

### REQ-TEST-002: 集成测试

**优先级**: P0
**需求描述**: 所有新增API必须编写集成测试，验证端到端功能。

**场景**:

#### Scenario: API集成测试

**Given** 使用TestContainers启动测试环境
**When** 测试API端点
**Then** 应该：
- 验证请求-响应流程
- 验证数据库操作
- 验证事务回滚
- 验证异常处理

**示例**:
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class DeviceAIEventIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void receiveDeviceEvent_shouldCreateEvent() {
        // Given
        DeviceAIEventForm form = new DeviceAIEventForm();
        form.setDeviceId("CAM001");
        form.setEventType("FALL_DETECTION");
        form.setConfidence(new BigDecimal("0.95"));
        form.setEventTime(LocalDateTime.now());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DeviceAIEventForm> request = new HttpEntity<>(form, headers);

        // When
        ResponseEntity<ResponseDTO> response = restTemplate.postForEntity(
            "/api/v1/video/device/ai/event",
            request,
            ResponseDTO.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getData());

        // 验证数据库
        DeviceAIEventEntity event = deviceAIEventDao.selectOne(
            new LambdaQueryWrapper<DeviceAIEventEntity>()
                .eq(DeviceAIEventEntity::getDeviceId, "CAM001")
        );
        assertNotNull(event);
    }
}
```

---

### REQ-TEST-003: 性能测试

**优先级**: P0
**需求描述**: 系统必须通过性能测试，满足预定的性能指标。

**场景**:

#### Scenario: 并发事件接收测试

**Given** 使用JMeter创建100个并发线程
**When** 模拟设备上报事件
**Then** 系统应该：
- 支持并发数 >= 100
- 吞吐量 >= 1000事件/秒
- 响应时间 P95 < 100ms
- 错误率 < 0.1%

**JMeter测试计划**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan>
  <hashTree>
    <TestPlan>
      <ThreadGroup>
        <stringProp name="ThreadGroup.num_threads">100</stringProp>
        <stringProp name="ThreadGroup.ramp_time">10</stringProp>
        <LoopController>
          <intProp name="LoopController.loops">1000</intProp>
        </LoopController>
        <HTTPSamplerProxy>
          <stringProp name="HTTPSampler.domain">localhost</stringProp>
          <stringProp name="HTTPSampler.port">8092</stringProp>
          <stringProp name="HTTPSampler.path">/api/v1/video/device/ai/event</stringProp>
          <stringProp name="HTTPSampler.method">POST</stringProp>
        </HTTPSamplerProxy>
        <ResultCollector guiclass="="ViewResultsFullVisualizer"/>
        <DurationAssertion guiclassstyle="="DurationAssertion">
          <stringProp name="DurationAssertion.duration">100</stringProp>
        </DurationAssertion>
      </ThreadGroup>
    </TestPlan>
  </hashTree>
</jmeterTestPlan>
```

#### Scenario: WebSocket并发连接测试

**Given** 使用JMeter创建500个并发连接
**When** 连接到WebSocket端点
**Then** 系统应该：
- 支持并发连接数 >= 500
- 连接成功率 > 99%
- 消息推送延迟 P95 < 500ms
- 无内存泄漏

---

### REQ-TEST-004: 压力测试和稳定性

**优先级**: P0
**需求描述**: 系统必须通过72小时稳定性测试，验证系统长期运行的可靠性。

**场景**:

#### Scenario: 长时间稳定性测试

**Given** 系统部署到测试环境
**When** 运行72小时压测
**Then** 系统应该：
- 无服务崩溃
- 无内存泄漏
- CPU使用率 < 80%
- 响应时间无明显下降

**Gatling测试脚本**:
```scala
class StabilitySimulation extends Simulation {

  val scn = scenario("Stability Test")
    .exec(http("Home Page")
      .get("/")
      .check(status.in(200))
    )
    .exec(http("Device Events")
      .get("/api/v1/video/device/ai/events")
      .check(status.in(200))
    )
    .exec(ws("WebSocket Connect")
      .connect("/ws/video")
      .subscribe("/topic/device-events")
      .await(1)(ws.receiveText("message"))
    )

  setUp(
    scn.inject(
      constantUsersPerSec(10) during (1 minutes),
      rampUsersPerSec(10) to (100) during (5 minutes),
      constantUsersPerSec(100) during (60 minutes)
    )
  ).protocols(http.ws)
}
```

#### Scenario: 峰值流量测试

**Given** 系统正常运行
**When** 突发2倍峰值流量
**Then** 系统应该：
- 不崩溃
- 优雅降级（限流、熔断）
- 恢复后正常服务

---

## MODIFIED Requirements

*（本能力为新增，无修改的需求）*

---

## REMOVED Requirements

*（本能力为新增，无删除的需求）*

---

## 附录

### A. 测试工具

| 工具 | 用途 | 版本 |
|------|------|------|
| JUnit 5 | 单元测试框架 | 5.9+ |
| Mockito | Mock框架 | 5.3+ |
| JaCoCo | 测试覆盖率 | 0.8.12 |
| TestContainers | 集成测试容器 | 1.19+ |
| JMeter | 性能测试 | 5.6+ |
| Gatling | 负载测试 | 3.9+ |
| Cypress | E2E测试 | 13.6+ |

### B. 测试环境配置

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: create-drop

  redis:
    host: localhost
    port: 6379
    database: 0  # 使用独立的数据库

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: test-ai-models

websocket:
  enabled: true
  endpoint: /ws/test
```

### C. CI/CD集成

```yaml
# .github/workflows/test.yml
name: Test Pipeline

on:
  pull_request:
    branches: [main]
  push:
    branches: [main]

jobs:
  unit-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run unit tests
        run: mvn test
      - name: Check coverage
        run: |
          mvn jacoco:report
          echo "Coverage threshold: 80%"
          # 检查覆盖率是否达标

  integration-test:
    runs-on: ubuntu-latest
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: test
          MYSQL_DATABASE: test
      redis:
        image: redis:7-alpine
    steps:
      - uses: actions/checkout@v3
      - name: Run integration tests
        run: mvn verify -Dspring.profiles.active=integration

  performance-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run JMeter tests
        run: |
          mvn verify -Pperformance-test
          # 检查性能指标是否达标
```

### D. 测试报告

**单元测试报告**:
- JaCoCo HTML报告：`target/site/jacoco/index.html`
- 覆盖率按包、类、方法展示
- 目标：整体覆盖率 > 80%

**集成测试报告**:
- Surefire报告：`target/surefire-reports/`
- 包含测试通过率、执行时间

**性能测试报告**:
- JMeter HTML报告：`target/jmeter/report/index.html`
- 包含响应时间图、吞吐量图、错误率统计

---

**规格编写人**: IOE-DREAM 架构委员会
**创建日期**: 2025-01-30
**版本**: 1.0.0
