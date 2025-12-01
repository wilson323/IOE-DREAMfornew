# repowiki合规性增强和业务功能扩展 - 技术设计文档

## 架构决策记录 (ADR)

### ADR-001: Spring Boot 3.x迁移完整性

**决策**: 必须完成javax到jakarta包的完整迁移，确保Spring Boot 3.x编译通过

**理由**:
- 当前存在4个文件仍使用javax包，导致编译失败
- Spring Boot 3.x完全基于Jakarta EE，不支持javax包
- 这是一级规范违规，阻塞后续所有开发工作

**技术方案**:
```bash
# 批量修复策略
find . -name "*.java" -exec sed -i 's/javax\.crypto\./jakarta.crypto\./g' {} \;
find . -name "*.java" -exec sed -i 's/javax\.sql\.DataSource/jakarta.sql.DataSource/g' {} \;
find . -name "*.java" -exec sed -i 's/javax\.crypto\.spec\./jakarta.crypto.spec\./g' {} \;
```

**影响**: 加密工具类、数据源配置类需要重新测试

### ADR-002: 多级缓存架构设计

**决策**: 实现L1(Caffeine本地缓存) + L2(Redis分布式缓存)的多级缓存架构

**理由**:
- 当前CacheService.java实现过于简化，返回null占位符
- 业务层缓存使用不足，影响系统性能
- 需要满足repowiki性能规范要求（P95≤200ms）

**技术架构**:
```
Application Layer
    ↓
Service Layer (@Cacheable, @CacheEvict)
    ↓
Cache Manager
    ├── L1 Cache (Caffeine) - 5分钟过期
    ├── L2 Cache (Redis) - 30分钟过期
    └── Cache Aside + 双删策略
    ↓
Database Layer
```

**性能指标**:
- L1缓存命中率: ≥80%, 响应时间<10ms
- L2缓存命中率: ≥90%, 响应时间<50ms
- 缓存一致性: 版本号保证

### ADR-003: 智能视频监控系统架构

**决策**: 采用微服务架构，独立部署视频监控系统

**理由**:
- 视频监控功能复杂，需要独立扩展
- 视频流处理对实时性要求高
- 需要专门的硬件和存储支持

**技术栈选择**:
- **视频流**: RTSP接入，HLS/Web输出
- **AI分析**: OpenCV + TensorFlow Lite
- **存储**: 分布式文件系统 (MinIO)
- **流媒体**: FFmpeg + WebRTC

**系统架构**:
```
Frontend (Vue3)
    ↓
API Gateway
    ↓
Video Surveillance Service
    ├── Stream Management (RTSP/WebRTC)
    ├── AI Analytics (OpenCV/TensorFlow)
    ├── Storage Management (MinIO)
    └── Device Management (ONVIF)
    ↓
Video Database (MySQL + Redis + MinIO)
```

### ADR-004: 工作流引擎技术选型

**决策**: 基于Activiti 7实现工作流引擎

**理由**:
- Activiti 7支持BPMN 2.0标准，可视化程度高
- 与Spring Boot生态集成良好
- 支持分布式部署和高可用
- 社区活跃，文档完善

**实现方案**:
```java
// 工作流引擎核心配置
@Configuration
public class WorkflowConfig {

    @Bean
    public ProcessEngine processEngine() {
        return ProcessEngineConfiguration
            .createStandaloneProcessEngineConfiguration()
            .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
            .buildProcessEngine();
    }
}
```

### ADR-005: 数据库设计规范标准化

**决策**: 统一数据库设计规范，确保命名一致性和性能优化

**理由**:
- 当前部分表字段命名不统一（如record_id vs {table}_id）
- JSON字段过度使用，影响查询性能
- 需要符合repowiki数据库设计规范

**设计规范**:
```sql
-- 统一主键命名
CREATE TABLE t_example (
    example_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    -- 统一审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_user_id BIGINT,
    deleted_flag TINYINT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 1
);
```

## 技术风险分析

### 高风险项

1. **编译阻塞风险**
   - **风险**: javax包迁移可能导致其他依赖问题
   - **缓解**: 分批次修复，每次修复后验证编译
   - **应急**: 回退到修复前状态，逐个排查

2. **视频监控系统复杂性**
   - **风险**: 视频流处理技术门槛高
   - **缓解**: 采用成熟开源方案，分阶段实现
   - **应急**: 先实现基础功能，高级功能后期迭代

### 中等风险项

1. **缓存一致性**
   - **风险**: 多级缓存数据不一致
   - **缓解**: 版本号保证，双删策略
   - **应急**: 降级到单层缓存

2. **工作流引擎集成**
   - **风险**: 与现有系统集成复杂
   - **缓解**: 标准化接口设计，分步集成
   - **应急**: 简化工作流功能

## 性能设计方案

### 缓存性能优化

```java
// 智能缓存管理器
@Component
public class SmartCacheManager {

    // L1缓存 - Caffeine
    private final Cache<String, Object> localCache;

    // L2缓存 - Redis
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Cacheable(value = "users", key = "#userId", unless = "#result == null")
    public User getUserById(Long userId) {
        // 查询数据库逻辑
        return userMapper.selectById(userId);
    }

    @CacheEvict(value = "users", key = "#user.id")
    public void updateUser(User user) {
        // 双删策略保证一致性
        userMapper.updateById(user);
        // 延迟删除缓存
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(100);
                cacheManager.getCache("users").evict(user.getId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
```

### 数据库性能优化

```sql
-- 分区表设计（大数据量表）
CREATE TABLE t_video_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id BIGINT NOT NULL,
    record_time DATETIME NOT NULL,
    file_path VARCHAR(500),
    INDEX idx_device_time (device_id, record_time)
) PARTITION BY RANGE (YEAR(record_time)) (
    PARTITION p2023 VALUES LESS THAN (2024),
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026)
);
```

## 安全设计方案

### 缓存数据安全

```java
// 敏感数据加密缓存
@Component
public class SecureCacheService {

    @Value("${cache.encryption.key}")
    private String encryptionKey;

    public void putSecure(String key, Object value) {
        String encryptedValue = AESUtil.encrypt(JSON.toJSONString(value), encryptionKey);
        redisTemplate.opsForValue().set(key, encryptedValue);
    }

    public <T> T getSecure(String key, Class<T> clazz) {
        String encryptedValue = (String) redisTemplate.opsForValue().get(key);
        if (encryptedValue != null) {
            String decryptedValue = AESUtil.decrypt(encryptedValue, encryptionKey);
            return JSON.parseObject(decryptedValue, clazz);
        }
        return null;
    }
}
```

### API安全控制

```java
// 统一权限控制
@RestController
@RequestMapping("/api/video-surveillance")
public class VideoSurveillanceController {

    @GetMapping("/preview")
    @SaCheckPermission("video:preview")
    @RequireResource(value = DataScope.DEPT)
    public ResponseDTO<List<VideoStream>> getPreviewStreams() {
        // 视频预览逻辑
        return ResponseDTO.ok(videoService.getPreviewStreams());
    }
}
```

## 部署架构设计

### Docker容器化部署

```yaml
# docker-compose.yml
version: '3.8'
services:
  # 后端服务
  backend:
    build: ./smart-admin-api-java17-springboot3
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - REDIS_HOST=redis
      - MYSQL_HOST=mysql
    depends_on:
      - redis
      - mysql

  # 视频监控服务
  video-surveillance:
    build: ./video-surveillance-service
    environment:
      - REDIS_HOST=redis
      - MINIO_HOST=minio
    depends_on:
      - redis
      - minio

  # Redis缓存
  redis:
    image: redis:7-alpine
    volumes:
      - redis_data:/data

  # 对象存储
  minio:
    image: minio/minio
    volumes:
      - minio_data:/data
```

### Kubernetes部署

```yaml
# k8s-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: smart-admin-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: smart-admin-backend
  template:
    metadata:
      labels:
        app: smart-admin-backend
    spec:
      containers:
      - name: backend
        image: smart-admin-backend:latest
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "k8s"
```

## 监控和运维设计

### 应用性能监控 (APM)

```java
// 性能监控切面
@Aspect
@Component
public class PerformanceMonitorAspect {

    @Around("@annotation(Cacheable)")
    public Object monitorCachePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - startTime;

        // 记录缓存操作耗时
        meterRegistry.timer("cache.operation",
            "method", joinPoint.getSignature().getName())
            .record(duration, TimeUnit.MILLISECONDS);

        return result;
    }
}
```

### 健康检查设计

```java
// 健康检查端点
@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        boolean cacheHealthy = checkCacheHealth();
        boolean dbHealthy = checkDatabaseHealth();
        boolean videoServiceHealthy = checkVideoServiceHealth();

        if (cacheHealthy && dbHealthy && videoServiceHealthy) {
            return Health.up()
                .withDetail("cache", "正常")
                .withDetail("database", "正常")
                .withDetail("video-service", "正常")
                .build();
        } else {
            return Health.down()
                .withDetail("cache", cacheHealthy ? "正常" : "异常")
                .withDetail("database", dbHealthy ? "正常" : "异常")
                .withDetail("video-service", videoServiceHealthy ? "正常" : "异常")
                .build();
        }
    }
}
```

## 测试策略

### 单元测试设计

```java
// 缓存服务测试
@SpringBootTest
class CacheServiceTest {

    @Test
    void testCacheHitPerformance() {
        // 第一次查询（缓存未命中）
        long startTime = System.currentTimeMillis();
        cacheService.get("test-key");
        long firstQueryTime = System.currentTimeMillis() - startTime;

        // 第二次查询（缓存命中）
        startTime = System.currentTimeMillis();
        cacheService.get("test-key");
        long secondQueryTime = System.currentTimeMillis() - startTime;

        // 缓存命中应显著快于首次查询
        assertThat(secondQueryTime).isLessThan(firstQueryTime / 10);
    }
}
```

### 集成测试设计

```java
// 视频监控系统集成测试
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
class VideoSurveillanceIntegrationTest {

    @Test
    void testVideoStreamIntegration() {
        // 测试视频流从接入到输出的完整流程
        VideoStream stream = videoService.startStream(deviceId);
        assertNotNull(stream.getStreamUrl());

        // 验证流可访问
        ResponseEntity<String> response = restTemplate.getForEntity(
            stream.getStreamUrl(), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
```

这个设计文档为repowiki合规性增强和业务功能扩展提供了完整的技术架构指导，确保实现方案的技术可行性和系统可靠性。