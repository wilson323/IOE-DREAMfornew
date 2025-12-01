# 考勤模块微服务架构规范

> **规范类型**: 微服务架构设计
> **适用范围**: 考勤模块微服务化改造
> **创建时间**: 2025-11-27
> **版本**: v1.0

---

## 📋 规范概述

### 🎯 规范目标
定义考勤模块微服务化的架构设计规范，确保微服务架构的一致性、可维护性和可扩展性，为考勤模块的微服务化改造提供技术指导。

### 🎯 适用范围
本规范适用于IOE-DREAM项目中考勤模块(attendance)的微服务化改造，包括：
- 微服务架构设计原则
- 服务拆分策略和边界定义
- 技术栈选型和配置规范
- 数据架构和分布式事务设计
- API设计和服务间通信规范

---

## 🏗️ 架构设计原则

### 1. 单一职责原则
每个微服务应该专注于单一的业务领域，具有明确的业务边界和职责范围。

**考勤微服务职责**:
- 考勤记录的创建、查询、更新和删除
- 排班管理和班次配置
- 考勤规则的配置和执行
- 考勤统计分析和报表生成
- 考勤异常的检测和处理

### 2. 高内聚低耦合原则
- **高内聚**: 相关的业务功能应该聚合在同一个微服务内
- **低耦合**: 微服务之间通过定义良好的接口进行通信，减少直接依赖

### 3. 自治性原则
每个微服务应该具备：
- **技术自治**: 可以独立选择适合的技术栈
- **部署自治**: 可以独立部署和升级
- **数据自治**: 拥有自己的数据库和数据存储
- **扩展自治**: 可以根据业务需求独立扩展

### 4. 容错设计原则
- **故障隔离**: 单个微服务的故障不应影响其他微服务
- **降级策略**: 在依赖服务不可用时提供备用方案
- **熔断机制**: 防止级联故障的发生

---

## 🔧 服务拆分策略

### 业务边界定义

#### 核心业务能力
```
考勤微服务 (Attendance Service)
├── 考勤记录管理 (Attendance Record Management)
│   ├── 打卡记录处理 (Clock-in/out Processing)
│   ├── 考勤数据采集 (Attendance Data Collection)
│   ├── 记录验证和校准 (Record Validation & Calibration)
│   └── 历史记录查询 (Historical Record Query)
├── 排班管理 (Schedule Management)
│   ├── 班次定义和管理 (Shift Definition & Management)
│   ├── 排班计划制定 (Schedule Planning)
│   ├── 班次调整和变更 (Shift Adjustment)
│   └── 排班冲突检测 (Schedule Conflict Detection)
├── 考勤规则引擎 (Attendance Rule Engine)
│   ├── 规则定义和配置 (Rule Definition & Configuration)
│   ├── 规则执行和评估 (Rule Execution & Evaluation)
│   ├── 异常检测和处理 (Exception Detection & Handling)
│   └── 规则版本管理 (Rule Version Management)
├── 统计分析服务 (Analytics Service)
│   ├── 考勤统计计算 (Attendance Statistics Calculation)
│   ├── 绩效指标计算 (Performance Metrics Calculation)
│   ├── 报表生成和管理 (Report Generation & Management)
│   └── 数据可视化支持 (Data Visualization Support)
└── 异常处理服务 (Exception Handling Service)
    ├── 异常申请处理 (Exception Application Processing)
    ├── 异常审批流程 (Exception Approval Workflow)
    ├── 异常数据统计 (Exception Data Statistics)
    └── 异常趋势分析 (Exception Trend Analysis)
```

#### 服务边界清晰化
```
内部职责 (Within Attendance Service):
✅ 考勤记录的CRUD操作
✅ 排班计划的制定和管理
✅ 考勤规则的配置和执行
✅ 考勤数据的统计和分析
✅ 异常申请和处理流程

外部依赖 (External Dependencies):
❌ 用户信息管理 → 依赖用户服务
❌ 设备信息管理 → 依赖设备服务
❌ 组织架构管理 → 依赖组织服务
❌ 权限控制 → 依赖权限服务
❌ 通知服务 → 依赖通知服务
```

### 数据边界定义

#### 数据所有权原则
- 考勤微服务拥有考勤相关的所有数据
- 通过API接口提供数据的查询和更新
- 避免跨服务的直接数据库访问

#### 数据划分策略
```sql
-- 考勤微服务独有数据表
t_attendance_record          -- 考勤记录表
t_attendance_schedule        -- 排班表
t_attendance_shift          -- 班次表
t_attendance_rule           -- 考勤规则表
t_attendance_exception      -- 考勤异常表
t_attendance_report         -- 考勤报表表
t_attendance_statistics     -- 考勤统计表

-- 外部服务数据(通过API访问)
t_user_info                 -- 用户信息(用户服务)
t_department_info          -- 部门信息(组织服务)
t_device_info              -- 设备信息(设备服务)
t_permission_info          -- 权限信息(权限服务)
```

---

## 🔌 API设计规范

### RESTful API设计

#### API版本管理
```
基础路径: /api/attendance/v1
版本策略: URL路径版本控制
向后兼容: 保持至少两个版本的兼容性
弃用通知: 提前3个月通知API弃用
```

#### 资源命名规范
```
# 使用复数形式表示资源集合
/api/attendance/v1/records         # 考勤记录集合
/api/attendance/v1/schedules       # 排班计划集合
/api/attendance/v1/shifts          # 班次集合
/api/attendance/v1/rules           # 规则集合
/api/attendance/v1/reports         # 报表集合

# 使用具体ID标识单个资源
/api/attendance/v1/records/{id}    # 特定考勤记录
/api/attendance/v1/schedules/{id}  # 特定排班计划
```

#### HTTP方法使用规范
```
GET    /records                    # 获取考勤记录列表
GET    /records/{id}               # 获取特定考勤记录
POST   /records                    # 创建考勤记录
PUT    /records/{id}               # 更新考勤记录
DELETE /records/{id}               # 删除考勤记录
PATCH  /records/{id}/status        # 更新考勤记录状态

# 复杂查询使用Query参数
GET    /records?userId=123&startDate=2024-01-01&endDate=2024-01-31
GET    /records?page=1&size=20&sort=createTime,desc
```

### 请求响应规范

#### 统一响应格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    // 具体业务数据
  },
  "timestamp": "2025-11-27T10:30:00Z",
  "traceId": "abc123def456",
  "requestId": "req-789xyz"
}
```

#### 错误响应格式
```json
{
  "code": 400,
  "message": "请求参数错误",
  "error": {
    "type": "VALIDATION_ERROR",
    "details": [
      {
        "field": "clockTime",
        "message": "打卡时间不能为空"
      }
    ]
  },
  "timestamp": "2025-11-27T10:30:00Z",
  "traceId": "abc123def456",
  "requestId": "req-789xyz"
}
```

#### 分页响应格式
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "content": [
      // 数据列表
    ],
    "page": {
      "number": 1,
      "size": 20,
      "totalElements": 100,
      "totalPages": 5,
      "first": true,
      "last": false
    }
  },
  "timestamp": "2025-11-27T10:30:00Z"
}
```

---

## 🔄 服务间通信规范

### Feign客户端规范

#### 客户端接口定义
```java
@FeignClient(
    name = "user-service",
    url = "${services.user-service.url}",
    configuration = FeignConfiguration.class,
    fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {

    @GetMapping("/api/user/v1/users/{userId}")
    ResponseDTO<UserVO> getUserById(@PathVariable("userId") Long userId);

    @GetMapping("/api/user/v1/users/batch")
    ResponseDTO<List<UserVO>> getUsersByIds(@RequestParam("userIds") List<Long> userIds);

    @GetMapping("/api/user/v1/departments/{deptId}")
    ResponseDTO<DepartmentVO> getDepartmentById(@PathVariable("deptId") Long deptId);
}
```

#### 容错处理规范
```java
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public ResponseDTO<UserVO> getUserById(Long userId) {
        log.warn("User service unavailable, using fallback for userId: {}", userId);
        return ResponseDTO.error(UserErrorCode.SERVICE_UNAVAILABLE);
    }

    @Override
    public ResponseDTO<List<UserVO>> getUsersByIds(List<Long> userIds) {
        log.warn("User service unavailable, using fallback for userIds: {}", userIds);
        return ResponseDTO.error(UserErrorCode.SERVICE_UNAVAILABLE);
    }

    @Override
    public ResponseDTO<DepartmentVO> getDepartmentById(Long deptId) {
        log.warn("User service unavailable, using fallback for deptId: {}", deptId);
        return ResponseDTO.error(UserErrorCode.SERVICE_UNAVAILABLE);
    }
}
```

### 事件驱动规范

#### 事件定义标准
```java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = AttendanceRecordCreatedEvent.class, name = "RECORD_CREATED"),
    @JsonSubTypes.Type(value = AttendanceRecordUpdatedEvent.class, name = "RECORD_UPDATED"),
    @JsonSubTypes.Type(value = AttendanceExceptionEvent.class, name = "EXCEPTION_OCCURRED")
})
public abstract class AttendanceEvent {
    private String eventId;
    private String eventType;
    private Long timestamp;
    private String source;
    private Map<String, Object> metadata;

    // 构造函数、getter、setter方法
}
```

#### 事件发布规范
```java
@Service
public class AttendanceEventPublisher {

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void publishAttendanceRecordCreated(AttendanceRecordEntity record) {
        // 发布本地事件
        AttendanceRecordCreatedEvent localEvent = new AttendanceRecordCreatedEvent(record);
        eventPublisher.publishEvent(localEvent);

        // 发布远程事件
        AttendanceEventMessage remoteEvent = AttendanceEventMessage.builder()
            .eventId(UUID.randomUUID().toString())
            .eventType("RECORD_CREATED")
            .payload(record)
            .timestamp(System.currentTimeMillis())
            .build();

        rabbitTemplate.convertAndSend("attendance.exchange", "attendance.record.created", remoteEvent);
    }
}
```

---

## 🗄️ 数据架构规范

### 数据库设计规范

#### 分库分表策略
```sql
-- 按业务域分库
主库: smart_attendance_v3 (考勤业务数据库)

-- 按时间分表(考勤记录)
t_attendance_record_2024
t_attendance_record_2025
t_attendance_record_2026

-- 按维度分表(统计数据)
t_attendance_daily_stats    -- 按天统计
t_attendance_monthly_stats  -- 按月统计
t_attendance_yearly_stats   -- 按年统计
```

#### 数据一致性规范
```java
@Service
@Transactional
public class AttendanceRecordService {

    @Resource
    private AttendanceRecordRepository recordRepository;

    @Resource
    private UserServiceClient userServiceClient;

    @GlobalTransactional
    public void createAttendanceRecord(AttendanceRecordCreateDTO dto) {
        // 1. 验证用户信息
        ResponseDTO<UserVO> userResponse = userServiceClient.getUserById(dto.getUserId());
        if (!userResponse.isSuccess()) {
            throw new SmartException(UserErrorCode.USER_NOT_FOUND);
        }

        // 2. 创建考勤记录
        AttendanceRecordEntity record = AttendanceRecordEntity.builder()
            .userId(dto.getUserId())
            .clockTime(dto.getClockTime())
            .clockType(dto.getClockType())
            .deviceId(dto.getDeviceId())
            .build();

        recordRepository.save(record);

        // 3. 发布事件
        eventPublisher.publishAttendanceRecordCreated(record);
    }
}
```

### 缓存策略规范

#### 多级缓存架构
```java
@Component
public class AttendanceCacheManager {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private Cache<String, Object> caffeineCache;

    public <T> T get(String key, Class<T> clazz) {
        // 1. 先查L1缓存(Caffeine)
        T value = (T) caffeineCache.getIfPresent(key);
        if (value != null) {
            return value;
        }

        // 2. 查L2缓存(Redis)
        RBucket<T> bucket = redissonClient.getBucket(key);
        value = bucket.get();
        if (value != null) {
            caffeineCache.put(key, value);
            return value;
        }

        return null;
    }

    public <T> void set(String key, T value, Duration ttl) {
        // 1. 设置L1缓存
        caffeineCache.put(key, value);

        // 2. 设置L2缓存
        RBucket<T> bucket = redissonClient.getBucket(key);
        bucket.set(value, ttl);
    }
}
```

---

## 🛡️ 安全规范

### API安全规范

#### 认证授权
```java
@RestController
@RequestMapping("/api/attendance/v1")
@SaCheckLogin
public class AttendanceController {

    @PostMapping("/records")
    @SaCheckPermission("attendance:record:create")
    @SaCheckRole("admin")
    public ResponseDTO<Long> createRecord(@Valid @RequestBody AttendanceRecordCreateDTO dto) {
        // 业务逻辑
    }

    @GetMapping("/records/{id}")
    @SaCheckPermission("attendance:record:query")
    public ResponseDTO<AttendanceRecordVO> getRecord(@PathVariable Long id) {
        // 业务逻辑
    }
}
```

#### 数据脱敏规范
```java
@Data
public class AttendanceRecordVO {

    private Long recordId;

    private Long userId;

    @JsonSerialize(using = SensitiveDataSerializer.class)
    private String userName;          // 脱敏处理

    @JsonSerialize(using = SensitiveDataSerializer.class)
    private String idCardNumber;      // 脱敏处理

    private LocalDateTime clockTime;

    private Integer clockType;
}
```

### 数据安全规范

#### 敏感数据加密
```java
@Component
public class SensitiveDataEncryptor {

    @Value("${app.security.encrypt.key}")
    private String encryptKey;

    public String encrypt(String plainText) {
        // 使用AES加密
        return AESUtils.encrypt(plainText, encryptKey);
    }

    public String decrypt(String encryptedText) {
        // 使用AES解密
        return AESUtils.decrypt(encryptedText, encryptKey);
    }
}
```

---

## 📊 监控和日志规范

### 监控指标规范

#### 业务指标监控
```java
@Component
public class AttendanceMetrics {

    private final Counter recordCreateCounter;
    private final Timer recordProcessTimer;
    private final Gauge activeUsersGauge;

    public AttendanceMetrics(MeterRegistry meterRegistry) {
        this.recordCreateCounter = Counter.builder("attendance.record.created")
            .description("Number of attendance records created")
            .register(meterRegistry);

        this.recordProcessTimer = Timer.builder("attendance.record.process.time")
            .description("Time taken to process attendance record")
            .register(meterRegistry);

        this.activeUsersGauge = Gauge.builder("attendance.active.users")
            .description("Number of active users")
            .register(meterRegistry);
    }

    public void incrementRecordCreate() {
        recordCreateCounter.increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start();
    }

    public void recordTime(Timer.Sample sample) {
        sample.stop(recordProcessTimer);
    }
}
```

### 日志规范

#### 结构化日志
```java
@Slf4j
@Service
public class AttendanceRecordService {

    public void createRecord(AttendanceRecordCreateDTO dto) {
        log.info("开始创建考勤记录, userId: {}, clockTime: {}", dto.getUserId(), dto.getClockTime());

        try {
            // 业务逻辑
            log.info("考勤记录创建成功, recordId: {}, userId: {}", recordId, dto.getUserId());

        } catch (Exception e) {
            log.error("考勤记录创建失败, userId: {}, error: {}", dto.getUserId(), e.getMessage(), e);
            throw e;
        }
    }
}
```

---

## 🚀 部署规范

### 容器化规范

#### Dockerfile规范
```dockerfile
FROM openjdk:17-jdk-slim

LABEL maintainer="IOE-DREAM Team"
LABEL version="1.0.0"
LABEL description="Attendance Microservice"

# 设置工作目录
WORKDIR /app

# 复制jar文件
COPY target/attendance-service-*.jar app.jar

# 创建非root用户
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 设置文件权限
RUN chown -R appuser:appuser /app

# 切换用户
USER appuser

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Kubernetes部署规范

#### 部署配置
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: attendance-service
  labels:
    app: attendance-service
    version: v1
spec:
  replicas: 3
  selector:
    matchLabels:
      app: attendance-service
  template:
    metadata:
      labels:
        app: attendance-service
        version: v1
    spec:
      containers:
      - name: attendance-service
        image: attendance-service:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: NACOS_SERVER_ADDR
          value: "nacos-server:8848"
        resources:
          requests:
            memory: "512Mi"
            cpu: "200m"
          limits:
            memory: "1024Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

---

## ✅ 合规性检查

### 强制性规范检查清单

#### 架构设计检查
- [ ] 服务边界清晰，单一职责原则
- [ ] 数据所有权明确，无跨库访问
- [ ] API设计符合RESTful规范
- [ ] 服务间通信采用标准协议
- [ ] 容错机制完善，故障隔离有效

#### 技术实现检查
- [ ] 使用Spring Boot 3.x和Java 17
- [ ] 统一使用jakarta包名
- [ ] 使用@Resource依赖注入
- [ ] 实现分布式事务管理
- [ ] 配置多级缓存策略

#### 安全合规检查
- [ ] API接口权限控制完整
- [ ] 敏感数据加密脱敏
- [ ] 日志记录规范完整
- [ ] 监控指标覆盖全面
- [ ] 部署配置安全可靠

### 违规处理机制
- **严重违规**: 立即停止实施，重新设计
- **一般违规**: 修正后继续实施
- **轻微违规**: 记录问题，后续优化

---

**最后更新时间**: 2025-11-27
**规范版本**: v1.0
**下次更新时间**: 根据实施情况适时更新