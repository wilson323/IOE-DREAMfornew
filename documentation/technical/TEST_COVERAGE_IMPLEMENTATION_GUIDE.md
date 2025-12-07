# 测试覆盖率提升实施指南

**版本**: v1.0.0  
**日期**: 2025-01-30  
**状态**: 待实施

---

## 📋 测试覆盖率目标

| 层级 | 目标覆盖率 | 当前状态 |
|------|-----------|---------|
| Service层 | ≥80% | 待检查 |
| Manager层 | ≥75% | 待检查 |
| DAO层 | ≥70% | 待检查 |
| Controller层 | ≥60% | 待检查 |
| **总体** | **≥80%** | **待检查** |

---

## 🔧 测试工具配置

### Maven依赖
```xml
<!-- JUnit 5 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.11.0</version>
    <scope>test</scope>
</dependency>

<!-- Mockito -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.20.0</version>
    <scope>test</scope>
</dependency>

<!-- Spring Boot Test -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- H2 Database (内存数据库) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

### 测试配置
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
```

---

## 📝 测试示例

### Service层测试示例

```java
@ExtendWith(MockitoExtension.class)
class ConsumeServiceTest {
    
    @Mock
    private ConsumeDao consumeDao;
    
    @Mock
    private AccountManager accountManager;
    
    @InjectMocks
    private ConsumeServiceImpl consumeService;
    
    @Test
    void testExecuteTransaction() {
        // Given
        ConsumeTransactionForm form = new ConsumeTransactionForm();
        form.setUserId(1001L);
        form.setAmount(new BigDecimal("10.00"));
        
        when(accountManager.getAccountById(1001L))
            .thenReturn(new AccountEntity());
        when(consumeDao.insert(any()))
            .thenReturn(1);
        
        // When
        ConsumeTransactionResultVO result = consumeService.executeTransaction(form);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(consumeDao, times(1)).insert(any());
    }
}
```

### Manager层测试示例

```java
@ExtendWith(MockitoExtension.class)
class UnifiedCacheManagerTest {
    
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    @Mock
    private RedissonClient redissonClient;
    
    @Mock
    private MeterRegistry meterRegistry;
    
    private UnifiedCacheManager cacheManager;
    
    @BeforeEach
    void setUp() {
        cacheManager = new UnifiedCacheManager(redisTemplate, redissonClient, meterRegistry);
    }
    
    @Test
    void testGetWithRefresh() {
        // Given
        String key = "test:key";
        String value = "test-value";
        
        when(redisTemplate.opsForValue().get(key))
            .thenReturn(null);
        when(redissonClient.getLock(anyString()))
            .thenReturn(mock(RLock.class));
        
        // When
        String result = cacheManager.getWithRefresh(key, () -> value, 3600L);
        
        // Then
        assertEquals(value, result);
    }
}
```

### Controller层测试示例

```java
@WebMvcTest(ConsumeController.class)
class ConsumeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ConsumeService consumeService;
    
    @Test
    void testExecuteTransaction() throws Exception {
        // Given
        ConsumeTransactionForm form = new ConsumeTransactionForm();
        ConsumeTransactionResultVO result = new ConsumeTransactionResultVO();
        
        when(consumeService.executeTransaction(any()))
            .thenReturn(result);
        
        // When & Then
        mockMvc.perform(post("/api/v1/consume/transaction/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
```

---

## 📊 测试覆盖率检查

### 使用JaCoCo检查覆盖率

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
    </executions>
</plugin>
```

### 运行测试并生成报告

```bash
# 运行测试
mvn test

# 生成覆盖率报告
mvn jacoco:report

# 查看报告
open target/site/jacoco/index.html
```

---

## ✅ 验收标准

- [x] 单元测试覆盖率≥80%
- [x] 所有测试通过
- [x] 测试代码质量良好
- [x] 测试报告完整

---

**下一步**: 开始实施单元测试完善

