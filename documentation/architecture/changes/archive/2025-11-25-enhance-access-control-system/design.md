# 门禁系统功能完善技术设计

## 架构设计

### 整体架构

严格遵循IOE-DREAM项目四层架构规范，实现门禁系统的完整技术架构：

```
┌─────────────────────────────────────────────────────────┐
│                    前端层 (Vue3)                        │
│  设备管理页面 | 区域管理页面 | 实时监控页面 | 事件查询页面   │
└─────────────────────────────────────────────────────────┘
                            │
                            ↓ HTTP/WebSocket
┌─────────────────────────────────────────────────────────┐
│                 Controller层 (接口控制)                    │
│  AccessDeviceController | AccessAreaController          │
│  AccessMonitorController | AccessReportController     │
└─────────────────────────────────────────────────────────┘
                            │
                            ↓ Service调用
┌─────────────────────────────────────────────────────────┐
│                  Service层 (业务逻辑)                      │
│  AccessDeviceService | AccessAreaService               │
│  AccessMonitorService | AccessReportService            │
└─────────────────────────────────────────────────────────┘
                            │
                            ↓ Manager调用
┌─────────────────────────────────────────────────────────┐
│                Manager层 (复杂业务+缓存)                   │
│  AccessDeviceManager | AccessAreaManager               │
│  AccessMonitorManager | ReportManager                  │
└─────────────────────────────────────────────────────────┘
                            │
                            ↓ DAO调用
┌─────────────────────────────────────────────────────────┐
│                   DAO层 (数据访问)                        │
│  AccessDeviceDao | AccessAreaDao                       │
│  AccessRecordDao | AccessReportDao                     │
└─────────────────────────────────────────────────────────┘
                            │
                            ↓ 数据库访问
┌─────────────────────────────────────────────────────────┐
│                数据存储层 (MySQL + Redis)                │
│  业务数据存储 | 缓存数据 | 会话数据 | 消息队列           │
└─────────────────────────────────────────────────────────┘
```

### 核心技术栈

**后端技术栈**：
- **框架**: Spring Boot 3.5.4 + Jakarta EE 9
- **数据库**: MySQL 9.3.0 + MyBatis Plus 3.5.12
- **缓存**: Redis 7.0 + Caffeine (L1+L2多级缓存)
- **实时通信**: WebSocket + Redis Pub/Sub
- **安全**: Sa-Token 1.44.0 + BCrypt
- **文档**: Knife4j 4.6.0 + SpringDoc OpenAPI

**前端技术栈**：
- **核心**: Vue 3.4.27 + Composition API
- **UI框架**: Ant Design Vue 4.2.5
- **状态管理**: Pinia 2.1.7
- **实时通信**: WebSocket + EventSource
- **图表**: ECharts 5.4.3
- **构建**: Vite 5.2.12

### 数据架构

#### 数据库设计（基于现有架构扩展）

**重要说明**：基于项目现有的统一基础架构，门禁模块采用扩展表机制，不重新设计基础表结构。

**现有基础模块利用**：
- **SmartDeviceEntity**：基础设备实体，提供设备通用字段
- **AreaEntity**：基础区域实体，提供区域通用字段
- **BiometricRecordEntity**：基础生物特征实体，提供生物特征数据存储
- **BiometricTemplateEntity**：基础生物特征模板实体，提供特征模板管理

**门禁模块扩展**：
- 通过扩展表增加门禁特有功能
- 通过关联表建立模块间关系
- 通过extensionConfig字段存储门禁专用配置

```sql
-- 门禁设备扩展表（扩展现有SmartDeviceEntity）
CREATE TABLE `smart_device_access_extension` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `device_id` BIGINT(20) NOT NULL COMMENT '设备ID（关联t_smart_device.device_id）',
    `door_count` INT NOT NULL DEFAULT 0 COMMENT '门数量',
    `reader_count` INT NOT NULL DEFAULT 0 COMMENT '读头数量',
    `verify_modes` VARCHAR(200) COMMENT '支持的验证方式（JSON数组）',
    `access_direction` VARCHAR(20) NOT NULL DEFAULT 'BOTH' COMMENT '通行方向: IN-进入, OUT-退出, BOTH-双向',
    `open_duration` INT NOT NULL DEFAULT 30 COMMENT '开门持续时间（秒）',
    `anti_passback` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用反潜功能',
    `interlock_group_id` BIGINT(20) NULL COMMENT '互锁组ID',
    `door_config` JSON COMMENT '门配置信息（JSON格式）',
    `reader_config` JSON COMMENT '读头配置信息（JSON格式）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_id` (`device_id`, `deleted_flag`),
    KEY `idx_interlock_group` (`interlock_group_id`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁设备扩展表';

-- 门表（门禁模块特有，基于区域和设备）
CREATE TABLE `smart_access_door` (
    `door_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '门ID',
    `door_code` VARCHAR(50) NOT NULL COMMENT '门编码',
    `door_name` VARCHAR(100) NOT NULL COMMENT '门名称',
    `device_id` BIGINT(20) NOT NULL COMMENT '设备ID（关联t_smart_device.device_id）',
    `area_id` BIGINT(20) NOT NULL COMMENT '区域ID（关联t_area.area_id）',
    `door_index` INT NOT NULL DEFAULT 1 COMMENT '门在设备中的索引',
    `door_type` VARCHAR(20) NOT NULL DEFAULT 'SINGLE' COMMENT '门类型: SINGLE-单门, DOUBLE-双门, GLASS-玻璃门, TURNSTILE-转闸',
    `access_direction` VARCHAR(20) NOT NULL DEFAULT 'BOTH' COMMENT '通行方向: IN-进入, OUT-退出, BOTH-双向',
    `verify_mode` VARCHAR(100) NOT NULL DEFAULT 'CARD' COMMENT '验证方式: CARD-刷卡, FACE-人脸, FINGERPRINT-指纹, PASSWORD-密码, MULTI-多重验证',
    `open_time_rule` VARCHAR(200) COMMENT '开放时间规则',
    `max_open_duration` INT NOT NULL DEFAULT 30 COMMENT '最大开门时长（秒）',
    `interlock_group_id` BIGINT(20) NULL COMMENT '互锁组ID',
    `description` VARCHAR(500) COMMENT '门描述',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-启用, DISABLED-禁用, MAINTAIN-维护',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT(20) NULL COMMENT '创建人ID',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志',
    PRIMARY KEY (`door_id`),
    UNIQUE KEY `uk_door_code` (`door_code`, `deleted_flag`),
    KEY `idx_device_door` (`device_id`, `deleted_flag`),
    KEY `idx_area_door` (`area_id`, `deleted_flag`),
    KEY `idx_interlock_group` (`interlock_group_id`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁门表';

-- 区域门禁扩展表（扩展现有AreaEntity）
CREATE TABLE `smart_area_access_extension` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `area_id` BIGINT(20) NOT NULL COMMENT '区域ID（关联t_area.area_id）',
    `access_policy` VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT '区域访问策略: OPEN-开放, RESTRICTED-受限, PRIVATE-私有',
    `auto_assign_permission` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否自动分配权限',
    `max_concurrent_persons` INT NULL COMMENT '最大并发人数',
    `alert_capacity_threshold` DECIMAL(5,2) DEFAULT 0.85 COMMENT '容量告警阈值',
    `emergency_access_enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用紧急访问',
    `video_surveillance_enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用视频监控',
    `access_config` JSON COMMENT '门禁配置信息（JSON格式）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_area_id` (`area_id`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区域门禁扩展表';

-- 门禁生物特征关联表（关联现有BiometricRecordEntity）
CREATE TABLE `smart_access_biometric_relation` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `biometric_record_id` BIGINT(20) NOT NULL COMMENT '生物特征记录ID（关联t_biometric_record.record_id）',
    `person_id` BIGINT(20) NOT NULL COMMENT '人员ID',
    `area_id` BIGINT(20) NULL COMMENT '区域ID（关联t_area.area_id）',
    `device_id` BIGINT(20) NULL COMMENT '设备ID（关联t_smart_device.device_id）',
    `door_id` BIGINT(20) NULL COMMENT '门ID（关联smart_access_door.door_id）',
    `access_level` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '访问级别: NORMAL-普通, VIP-重要, ADMIN-管理员',
    `valid_start_time` DATETIME NOT NULL COMMENT '有效开始时间',
    `valid_end_time` DATETIME NOT NULL COMMENT '有效结束时间',
    `access_times` JSON COMMENT '通行时间限制（JSON格式）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-启用, DISABLED-禁用, EXPIRED-过期',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biometric_relation` (`biometric_record_id`, `area_id`, `door_id`, `deleted_flag`),
    KEY `idx_person_biometric` (`person_id`, `deleted_flag`),
    KEY `idx_area_biometric` (`area_id`, `deleted_flag`),
    KEY `idx_device_biometric` (`device_id`, `deleted_flag`),
    KEY `idx_door_biometric` (`door_id`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁生物特征关联表';

-- 读头表（门禁模块特有，基于设备和门）
CREATE TABLE `smart_access_reader` (
    `reader_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '读头ID',
    `reader_code` VARCHAR(50) NOT NULL COMMENT '读头编码',
    `reader_name` VARCHAR(100) NOT NULL COMMENT '读头名称',
    `device_id` BIGINT(20) NOT NULL COMMENT '设备ID（关联t_smart_device.device_id）',
    `door_id` BIGINT(20) NULL COMMENT '门ID（关联smart_access_door.door_id）',
    `reader_index` INT NOT NULL DEFAULT 1 COMMENT '读头在设备中的索引',
    `reader_type` VARCHAR(20) NOT NULL DEFAULT 'RFID' COMMENT '读头类型: RFID-射频, FACE-人脸, FINGERPRINT-指纹, PASSWORD-密码',
    `verify_modes` VARCHAR(200) NOT NULL DEFAULT 'CARD' COMMENT '支持的验证方式（JSON数组）',
    `install_location` VARCHAR(100) COMMENT '安装位置',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-启用, DISABLED-禁用, FAULT-故障',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志',
    PRIMARY KEY (`reader_id`),
    UNIQUE KEY `uk_reader_code` (`reader_code`, `deleted_flag`),
    KEY `idx_device_reader` (`device_id`, `deleted_flag`),
    KEY `idx_door_reader` (`door_id`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁读头表';

-- 告警记录表
CREATE TABLE `smart_access_alert` (
    `alert_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '告警ID',
    `alert_code` VARCHAR(50) NOT NULL COMMENT '告警编码',
    `alert_type` VARCHAR(20) NOT NULL COMMENT '告警类型: DEVICE_OFFLINE-设备离线, ILLEGAL_ACCESS-非法通行, DOOR_FORCED-强行开门, SYSTEM_ERROR-系统错误',
    `severity_level` VARCHAR(20) NOT NULL COMMENT '严重程度: LOW-低, MEDIUM-中, HIGH-高, CRITICAL-紧急',
    `device_id` BIGINT(20) NULL COMMENT '设备ID',
    `area_id` BIGINT(20) NULL COMMENT '区域ID',
    `person_id` BIGINT(20) NULL COMMENT '人员ID',
    `alert_title` VARCHAR(200) NOT NULL COMMENT '告警标题',
    `alert_content` TEXT COMMENT '告警内容',
    `alert_data` JSON COMMENT '告警详细数据',
    `alert_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '处理状态: PENDING-待处理, PROCESSING-处理中, RESOLVED-已解决, IGNORED-已忽略',
    `handle_user_id` BIGINT(20) NULL COMMENT '处理人ID',
    `handle_time` DATETIME NULL COMMENT '处理时间',
    `handle_note` VARCHAR(500) COMMENT '处理说明',
    `resolve_time` DATETIME NULL COMMENT '解决时间',
    `alert_time` DATETIME NOT NULL COMMENT '告警时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志',
    PRIMARY KEY (`alert_id`),
    KEY `idx_alert_type_status` (`alert_type`, `alert_status`, `deleted_flag`),
    KEY `idx_device_alert` (`device_id`, `deleted_flag`),
    KEY `idx_area_alert` (`area_id`, `deleted_flag`),
    KEY `idx_alert_time` (`alert_time`, `deleted_flag`),
    KEY `idx_severity_status` (`severity_level`, `alert_status`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁告警记录表';
```

#### 缓存架构

```java
/**
 * 门禁系统缓存架构
 *
 * L1缓存: Caffeine本地缓存 (5分钟过期)
 * L2缓存: Redis分布式缓存 (30分钟过期)
 */
@Component
public class AccessCacheManager extends BaseCacheManager {

    // 设备状态缓存 - L1+L2
    public static final String DEVICE_STATUS = "access:device:status:";

    // 区域权限缓存 - L2
    public static final String AREA_PERMISSION = "access:area:permission:";

    // 实时监控数据 - L1
    public static final String REALTIME_MONITOR = "access:monitor:realtime:";

    // 告警计数缓存 - L1
    public static final String ALERT_COUNT = "access:alert:count:";

    // 设备配置缓存 - L2
    public static final String DEVICE_CONFIG = "access:device:config:";

    // 区域人员缓存 - L2
    public static final String AREA_PERSONS = "access:area:persons:";
}
```

### 核心模块设计

#### 1. 设备管理模块

**Controller层设计**：
```java
@RestController
@RequestMapping("/api/access/device")
@Tag(name = "门禁设备管理", description = "门禁设备的增删改查和控制操作")
public class AccessDeviceController {

    @PostMapping("/add")
    @SaCheckPermission("access:device:add")
    @Operation(summary = "新增设备", description = "手动添加新的门禁设备")
    public ResponseDTO<String> addDevice(@Valid @RequestBody DeviceAddForm form);

    @PutMapping("/update")
    @SaCheckPermission("access:device:update")
    @Operation(summary = "更新设备", description = "更新设备基本信息和配置")
    public ResponseDTO<String> updateDevice(@Valid @RequestBody DeviceUpdateForm form);

    @DeleteMapping("/delete/{deviceId}")
    @SaCheckPermission("access:device:delete")
    @Operation(summary = "删除设备", description = "删除指定设备")
    public ResponseDTO<String> deleteDevice(@PathVariable Long deviceId);

    @PostMapping("/search")
    @SaCheckPermission("access:device:query")
    @Operation(summary = "搜索设备", description = "网络搜索门禁设备")
    public ResponseDTO<List<DeviceSearchResultVO>> searchDevices();

    @PostMapping("/control/{deviceId}")
    @SaCheckPermission("access:device:control")
    @Operation(summary = "设备控制", description = "远程控制设备操作")
    public ResponseDTO<String> controlDevice(@PathVariable Long deviceId, @Valid @RequestBody DeviceControlForm form);

    @PostMapping("/sync/{deviceId}")
    @SaCheckPermission("access:device:sync")
    @Operation(summary = "同步设备", description = "同步设备时间和数据")
    public ResponseDTO<String> syncDevice(@PathVariable Long deviceId, @RequestParam SyncType syncType);
}
```

**Service层设计**：
```java
@Service
@Transactional(rollbackFor = Exception.class)
public class AccessDeviceServiceImpl implements AccessDeviceService {

    @Resource
    private AccessDeviceDao accessDeviceDao;

    @Resource
    private AccessDeviceManager accessDeviceManager;

    @Resource
    private AccessDeviceAdapter accessDeviceAdapter;

    @Override
    public ResponseDTO<String> addDevice(DeviceAddForm form) {
        // 1. 设备参数验证
        validateDeviceConfig(form);

        // 2. 检查设备是否已存在
        checkDeviceExists(form.getDeviceNo());

        // 3. 设备连接测试
        testDeviceConnection(form);

        // 4. 保存设备信息
        AccessDeviceEntity device = convertToDeviceEntity(form);
        accessDeviceDao.insert(device);

        // 5. 自动同步设备信息
        syncDeviceInfo(device);

        // 6. 清除缓存
        accessDeviceManager.clearDeviceCache(device.getDeviceId());

        return ResponseDTO.ok("设备添加成功");
    }

    @Override
    public ResponseDTO<String> controlDevice(Long deviceId, DeviceControlForm form) {
        // 1. 权限验证
        validateDeviceControlPermission(deviceId);

        // 2. 设备状态检查
        checkDeviceOnlineStatus(deviceId);

        // 3. 执行控制命令
        String result = accessDeviceAdapter.executeControlCommand(deviceId, form);

        // 4. 记录操作日志
        recordDeviceOperation(deviceId, form, result);

        // 5. 实时推送状态更新
        pushDeviceStatusUpdate(deviceId);

        return ResponseDTO.ok(result);
    }
}
```

#### 2. 实时监控模块

**WebSocket设计**：
```java
@ServerEndpoint("/api/access/ws/monitor")
@Component
public class AccessMonitorWebSocket {

    private static final Logger log = LoggerFactory.getLogger(AccessMonitorWebSocket.class);

    // 线程安全的会话管理
    private static final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    // 用户权限缓存
    private static final ConcurrentHashMap<String, Set<String>> userPermissions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        String userId = getUserIdFromSession(session);
        String sessionId = session.getId();

        // 权限验证
        if (!hasMonitorPermission(userId)) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "无监控权限"));
                return;
            } catch (IOException e) {
                log.error("关闭无权限连接失败", e);
                return;
            }
        }

        sessions.put(sessionId, session);
        userPermissions.put(sessionId, getUserMonitorAreas(userId));

        log.info("用户{}连接实时监控, 当前连接数: {}", userId, sessions.size());

        // 发送初始数据
        sendInitialData(session, userId);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            MonitorMessage monitorMessage = JSON.parseObject(message, MonitorMessage.class);
            handleMessage(session, monitorMessage);
        } catch (Exception e) {
            log.error("处理WebSocket消息失败: {}", message, e);
            sendErrorResponse(session, "消息格式错误");
        }
    }

    @OnClose
    public void onClose(Session session) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        userPermissions.remove(sessionId);
        log.info("监控连接断开, 当前连接数: {}", sessions.size());
    }

    /**
     * 推送设备状态更新
     */
    public void pushDeviceStatusUpdate(DeviceStatusUpdateEvent event) {
        DeviceStatusMessage message = DeviceStatusMessage.builder()
            .deviceId(event.getDeviceId())
            .deviceName(event.getDeviceName())
            .status(event.getStatus())
            .timestamp(event.getTimestamp())
            .build();

        broadcastToAuthorizedUsers("device:status", message);
    }

    /**
     * 推送告警信息
     */
    public void pushAlert(AccessAlertEvent event) {
        AlertMessage message = AlertMessage.builder()
            .alertId(event.getAlertId())
            .alertType(event.getAlertType())
            .severity(event.getSeverity())
            .deviceId(event.getDeviceId())
            .areaId(event.getAreaId())
            .title(event.getTitle())
            .content(event.getContent())
            .timestamp(event.getTimestamp())
            .build();

        broadcastToAuthorizedUsers("alert", message);
    }
}
```

#### 3. 权限自动分配机制

```java
@Service
public class AccessPermissionAutoAssignService {

    @Resource
    private AccessAreaService accessAreaService;

    @Resource
    private AccessDeviceService accessDeviceService;

    @EventListener
    public void handleNewPersonEvent(NewPersonEvent event) {
        // 新增人员时自动分配权限
        Long personId = event.getPersonId();
        List<Long> areaIds = event.getAreaIds();

        for (Long areaId : areaIds) {
            autoAssignPersonToAreaPermissions(personId, areaId);
        }
    }

    @EventListener
    public void handleNewDoorEvent(NewDoorEvent event) {
        // 新增门时自动分配权限
        Long doorId = event.getDoorId();
        Long areaId = event.getAreaId();

        autoAssignDoorToAreaPersons(doorId, areaId);
    }

    private void autoAssignPersonToAreaPermissions(Long personId, Long areaId) {
        // 获取区域内所有门
        List<Long> doorIds = accessAreaService.getAreaDoors(areaId);

        // 为人员分配所有门的权限
        for (Long doorId : doorIds) {
            AccessPermissionEntity permission = AccessPermissionEntity.builder()
                .personId(personId)
                .doorId(doorId)
                .areaId(areaId)
                .permissionType(PermissionType.AUTO)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusYears(10))
                .status(PermissionStatus.ACTIVE)
                .build();

            accessPermissionDao.insert(permission);
        }
    }
}
```

### 性能优化设计

#### 1. 多级缓存策略

```java
@Configuration
public class AccessCacheConfig {

    @Bean
    public Cache<String, Object> caffeineCache() {
        return Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()
            .build();
    }

    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .transactionAware()
            .build();
    }
}
```

#### 2. 数据库优化

```sql
-- 核心查询索引优化
CREATE INDEX idx_device_status_time ON smart_access_device(device_status, update_time);
CREATE INDEX idx_record_device_time ON smart_access_record(device_id, access_time);
CREATE INDEX idx_alert_type_status ON smart_access_alert(alert_type, alert_status);
CREATE INDEX idx_permission_person_door ON smart_access_permission(person_id, door_id);

-- 分区表优化 (按月分区)
ALTER TABLE smart_access_record PARTITION BY RANGE (MONTH(access_time)) (
    PARTITION p202401 VALUES LESS THAN (2),
    PARTITION p202402 VALUES LESS THAN (3),
    -- ... 其他分区
    PARTITION pmax VALUES LESS THAN MAXVALUE
);
```

#### 3. 异步处理机制

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "accessTaskExecutor")
    public Executor accessTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("AccessTask-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}

@Service
public class AsyncAccessService {

    @Async("accessTaskExecutor")
    public CompletableFuture<Void> processAccessEventAsync(AccessEvent event) {
        // 异步处理门禁事件
        processAccessEvent(event);
        return CompletableFuture.completedFuture(null);
    }

    @Async("accessTaskExecutor")
    public CompletableFuture<Void> syncDeviceDataAsync(Long deviceId) {
        // 异步同步设备数据
        syncDeviceData(deviceId);
        return CompletableFuture.completedFuture(null);
    }
}
```

### 安全设计

#### 1. 权限控制

```java
@Component
public class AccessPermissionEvaluator {

    public boolean hasDevicePermission(Long userId, Long deviceId, String permission) {
        // 检查用户设备权限
        UserEntity user = userService.getById(userId);
        AccessDeviceEntity device = deviceService.getById(deviceId);

        // 检查区域权限
        if (!hasAreaPermission(userId, device.getAreaId(), permission)) {
            return false;
        }

        // 检查角色权限
        return hasRolePermission(user.getRoleIds(), permission);
    }

    public boolean hasAreaPermission(Long userId, Long areaId, String permission) {
        // 检查用户区域权限
        List<Long> userAreaIds = getUserAreaIds(userId);
        return userAreaIds.contains(areaId) || isSuperAdmin(userId);
    }
}
```

#### 2. 数据加密

```java
@Component
public class AccessDataEncryption {

    @Value("${app.encryption.key}")
    private String encryptionKey;

    public String encryptSensitiveData(String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);

            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new AccessSecurityException("数据加密失败", e);
        }
    }

    public String decryptSensitiveData(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            throw new AccessSecurityException("数据解密失败", e);
        }
    }
}
```

---

**本技术设计文档提供了门禁系统完善的详细技术架构和实现方案，确保系统的高性能、高可用性和高安全性。**