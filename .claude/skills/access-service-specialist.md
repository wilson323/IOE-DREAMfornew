# 访问控制服务专家技能
## Access Service Specialist

**🎯 技能定位**: IOE-DREAM智慧园区门禁访问控制业务专家，精通门禁权限管理、实时监控、设备控制等核心业务

**⚡ 技能等级**: ★★★★★★ (顶级专家)
**🎯 适用场景**: 门禁服务开发、访问控制优化、生物识别集成、实时监控建设
**📊 技能覆盖**: 权限管理 | 生物识别 | 设备控制 | 实时监控 | 异常处理 | 跨服务调用
**🔧 技术栈**: Spring Boot 3.5.8 + Spring Security 6.4 + MyBatis-Plus + Redis

---

## 📋 技能概述

### **核心专长**
- **访问控制架构**: 深度理解基于角色的访问控制(RBAC)和基于属性的访问控制(ABAC)
- **生物识别技术**: 人脸识别、指纹识别、虹膜识别等多模态生物特征验证
- **设备协议集成**: 多种门禁设备协议适配和统一控制接口
- **实时监控系统**: 门禁事件实时监控、异常检测和智能告警
- **跨服务设备调用**: 接收访客服务调用，下发人员信息到门禁设备
- **高可用设计**: 访问控制服务的高可用、高性能架构设计

### **解决能力**
- **门禁服务开发**: 完整的门禁访问控制服务实现和优化
- **生物识别集成**: 多模态生物识别系统集成和优化
- **权限策略设计**: 灵活的权限策略引擎和动态权限分配
- **异常处理机制**: 门禁异常检测、安全告警和应急处理
- **性能优化**: 高并发门禁访问的性能优化和负载均衡

---

## 🎯 业务场景覆盖

### 🚪 门禁权限管理
```java
// 权限验证核心流程 (Jakarta EE 3.0+)
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.transaction.Transactional;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;

@Service
@Transactional(rollbackFor = Exception.class)
public class AccessControlServiceImpl implements AccessControlService {

    @Resource
    private AccessManager accessManager;

    @Override
    public AccessResult processAccess(AccessRequest request) {
        // 1. 参数验证
        validateAccessRequest(request);

        // 2. 调用Manager层处理复杂业务逻辑
        return accessManager.processAccess(request);
    }

    private void validateAccessRequest(AccessRequest request) {
        if (request.getUserId() == null) {
            throw new BusinessException("USER_ID_REQUIRED", "用户ID不能为空");
        }
        if (request.getDeviceId() == null) {
            throw new BusinessException("DEVICE_ID_REQUIRED", "设备ID不能为空");
        }
    }
}

// Manager层 - 复杂业务流程编排
public class AccessManagerImpl implements AccessManager {

    private final BiometricService biometricService;
    private final PermissionEngine permissionEngine;
    private final DeviceProtocolAdapter deviceAdapter;
    private final AccessRecordDao accessRecordDao;

    // 构造函数注入依赖
    public AccessManagerImpl(
            BiometricService biometricService,
            PermissionEngine permissionEngine,
            DeviceProtocolAdapter deviceAdapter,
            AccessRecordDao accessRecordDao) {
        this.biometricService = biometricService;
        this.permissionEngine = permissionEngine;
        this.deviceProtocolAdapter = deviceAdapter;
        this.accessRecordDao = accessRecordDao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccessResult processAccess(AccessRequest request) {
        // 1. 生物特征验证
        BiometricResult biometricResult = biometricService.verify(request.getBiometricData());

        // 2. 权限策略检查
        PermissionResult permissionResult = permissionEngine.checkPermission(
            request.getUserId(),
            request.getAreaId(),
            request.getDeviceId()
        );

        // 3. 记录访问日志
        AccessRecordEntity record = createAccessRecord(request, biometricResult, permissionResult);
        accessRecordDao.insert(record);

        // 4. 设备控制指令下发
        if (biometricResult.isValid() && permissionResult.isAllowed()) {
            AccessResult result = deviceAdapter.grantAccess(request.getDeviceId(), request);
            updateAccessRecordResult(record.getId(), result);
            return result;
        }

        return AccessResult.denied("验证失败或权限不足");
    }
}

// DAO层 - 数据访问
@Mapper
public interface AccessRecordDao extends BaseMapper<AccessRecordEntity> {

    @Transactional(readOnly = true)
    List<AccessRecordEntity> selectByUserId(Long userId);

    @Transactional(readOnly = true)
    List<AccessRecordEntity> selectByDeviceId(Long deviceId);

    @Transactional(rollbackFor = Exception.class)
    int updateAccessResult(@Param("id") Long id, @Param("result") String result, @Param("remark") String remark);
}

// 实体类 - 符合Jakarta EE规范
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_record")
public class AccessRecordEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("device_id")
    private Long deviceId;

    @TableField("area_id")
    private Long areaId;

    @TableField("access_type")
    private Integer accessType;

    @TableField("biometric_result")
    private String biometricResult;

    @TableField("permission_result")
    private String permissionResult;

    @TableField("access_result")
    private String accessResult;

    @TableField("access_time")
    private LocalDateTime accessTime;

    @TableField("remark")
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted_flag")
    private Integer deletedFlag;

    @Version
    private Integer version;
}
```

### 🔄 跨服务设备调用
```java
// 接收访客服务调用，下发人员信息到门禁设备 (Jakarta EE 3.0+)
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.transaction.Transactional;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/access/device")
@Tag(name = "门禁设备管理", description = "接收访客服务调用，管理门禁设备")
public class AccessDeviceController {

    @Resource
    private AccessDeviceService accessDeviceService;

    /**
     * 接收访客服务调用，下发访客信息到门禁设备
     */
    @PostMapping("/visitor/provision")
    @PreAuthorize("hasRole('VISITOR_SERVICE')")
    @RateLimiter(name = "visitor-provision", fallbackMethod = "provisionFallback")
    public ResponseDTO<Void> provisionVisitorToDevice(
            @Valid @RequestBody VisitorProvisionRequest request,
            HttpServletRequest httpRequest) {

        // 验证调用来源
        validateServiceCall(httpRequest, "visitor-service");

        return accessDeviceService.provisionVisitorToDevice(request);
    }

    /**
     * 回收访客权限
     */
    @DeleteMapping("/visitor/revoke")
    @PreAuthorize("hasRole('VISITOR_SERVICE')")
    @RateLimiter(name = "visitor-revoke", fallbackMethod = "revokeFallback")
    public ResponseDTO<Void> revokeVisitorAccess(
            @Valid @RequestBody VisitorRevokeRequest request,
            HttpServletRequest httpRequest) {

        // 验证调用来源
        validateServiceCall(httpRequest, "visitor-service");

        return accessDeviceService.revokeVisitorAccess(request);
    }

    // 服务降级处理
    public ResponseDTO<Void> provisionFallback(VisitorProvisionRequest request, Exception ex) {
        log.error("[访客权限下发] 服务降级, visitorId={}", request.getVisitorId(), ex);
        return ResponseDTO.error("SERVICE_DEGRADED", "系统繁忙，请稍后重试");
    }

    public ResponseDTO<Void> revokeFallback(VisitorRevokeRequest request, Exception ex) {
        log.error("[访客权限回收] 服务降级, visitorId={}", request.getVisitorId(), ex);
        return ResponseDTO.error("SERVICE_DEGRADED", "系统繁忙，请稍后重试");
    }
}

// Service层 - 业务逻辑实现
@Service
@Transactional(rollbackFor = Exception.class)
public class AccessDeviceServiceImpl implements AccessDeviceService {

    @Resource
    private AccessDeviceManager accessDeviceManager;

    @Override
    public ResponseDTO<Void> provisionVisitorToDevice(VisitorProvisionRequest request) {
        try {
            // 委托给Manager层处理复杂业务逻辑
            accessDeviceManager.provisionVisitorToDevice(request);
            return ResponseDTO.ok();
        } catch (BusinessException e) {
            log.warn("[访客权限下发] 业务异常, visitorId={}, error={}", request.getVisitorId(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[访客权限下发] 系统异常, visitorId={}", request.getVisitorId(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "系统内部错误");
        }
    }

    @Override
    public ResponseDTO<Void> revokeVisitorAccess(VisitorRevokeRequest request) {
        try {
            // 委托给Manager层处理复杂业务逻辑
            accessDeviceManager.revokeVisitorAccess(request);
            return ResponseDTO.ok();
        } catch (BusinessException e) {
            log.warn("[访客权限回收] 业务异常, visitorId={}, error={}", request.getVisitorId(), e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[访客权限回收] 系统异常, visitorId={}", request.getVisitorId(), e);
            return ResponseDTO.error("SYSTEM_ERROR", "系统内部错误");
        }
    }
}

// Manager层 - 复杂业务流程编排
public class AccessDeviceManagerImpl implements AccessDeviceManager {

    private final DeviceProtocolManager deviceProtocolManager;
    private final VisitorProvisionLogDao visitorProvisionLogDao;
    private final GatewayServiceClient gatewayServiceClient;

    // 构造函数注入依赖
    public AccessDeviceManagerImpl(
            DeviceProtocolManager deviceProtocolManager,
            VisitorProvisionLogDao visitorProvisionLogDao,
            GatewayServiceClient gatewayServiceClient) {
        this.deviceProtocolManager = deviceProtocolManager;
        this.visitorProvisionLogDao = visitorProvisionLogDao;
        this.gatewayServiceClient = gatewayServiceClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void provisionVisitorToDevice(VisitorProvisionRequest request) {
        // 1. 验证访客权限有效性
        validateVisitorPermission(request.getVisitorId(), request.getPermissionId());

        // 2. 获取设备信息
        DeviceEntity device = getDeviceInfo(request.getDeviceId());
        if (device == null || device.getStatus() != 1) {
            throw new BusinessException("DEVICE_NOT_FOUND", "设备不存在或已禁用");
        }

        // 3. 设备协议适配
        DeviceProtocolAdapter adapter = deviceProtocolManager.getAdapter(device.getDeviceType());

        // 4. 下发访客信息到门禁设备
        ProvisioningResult result = adapter.provisionVisitor(
            request.getDeviceId(),
            request.getVisitorInfo(),
            request.getAccessTimeWindow()
        );

        // 5. 记录下发日志
        logVisitorProvisioning(request, result);

        // 6. 如果下发失败，抛出业务异常
        if (!result.isSuccess()) {
            throw new BusinessException("DEVICE_PROVISIONING_FAILED", result.getErrorMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeVisitorAccess(VisitorRevokeRequest request) {
        // 1. 获取设备信息
        DeviceEntity device = getDeviceInfo(request.getDeviceId());
        if (device == null || device.getStatus() != 1) {
            throw new BusinessException("DEVICE_NOT_FOUND", "设备不存在或已禁用");
        }

        // 2. 设备协议适配
        DeviceProtocolAdapter adapter = deviceProtocolManager.getAdapter(device.getDeviceType());

        // 3. 从门禁设备删除访客权限
        RevocationResult result = adapter.revokeVisitorAccess(
            request.getDeviceId(),
            request.getVisitorId()
        );

        // 4. 记录回收日志
        logVisitorRevocation(request, result);

        // 5. 如果回收失败，抛出业务异常
        if (!result.isSuccess()) {
            throw new BusinessException("DEVICE_REVOCATION_FAILED", result.getErrorMessage());
        }
    }

    private void validateVisitorPermission(Long visitorId, Long permissionId) {
        // 通过网关调用访客服务验证权限
        ResponseDTO<Boolean> result = gatewayServiceClient.callVisitorService(
            "/api/v1/visitor/permission/validate",
            HttpMethod.POST,
            Map.of("visitorId", visitorId, "permissionId", permissionId),
            Boolean.class
        );

        if (result.getCode() != 200 || !result.getData()) {
            throw new BusinessException("VISITOR_PERMISSION_INVALID", "访客权限无效");
        }
    }

    private DeviceEntity getDeviceInfo(Long deviceId) {
        // 通过网关调用公共设备服务获取设备信息
        ResponseDTO<DeviceEntity> result = gatewayServiceClient.callCommonService(
            "/api/v1/device/" + deviceId,
            HttpMethod.GET,
            null,
            DeviceEntity.class
        );

        if (result.getCode() == 200) {
            return result.getData();
        }
        return null;
    }

    private void logVisitorProvisioning(VisitorProvisionRequest request, ProvisioningResult result) {
        VisitorProvisionLogEntity log = VisitorProvisionLogEntity.builder()
            .visitorId(request.getVisitorId())
            .deviceId(request.getDeviceId())
            .operationType("PROVISION")
            .operationResult(result.isSuccess() ? "SUCCESS" : "FAILED")
            .operationRemark(result.getErrorMessage())
            .build();

        visitorProvisionLogDao.insert(log);
    }

    private void logVisitorRevocation(VisitorRevokeRequest request, RevocationResult result) {
        VisitorProvisionLogEntity log = VisitorProvisionLogEntity.builder()
            .visitorId(request.getVisitorId())
            .deviceId(request.getDeviceId())
            .operationType("REVOKE")
            .operationResult(result.isSuccess() ? "SUCCESS" : "FAILED")
            .operationRemark(result.getErrorMessage())
            .build();

        visitorProvisionLogDao.insert(log);
    }
}

// DAO层 - 数据访问
@Mapper
public interface VisitorProvisionLogDao extends BaseMapper<VisitorProvisionLogEntity> {

    @Transactional(readOnly = true)
    List<VisitorProvisionLogEntity> selectByVisitorId(Long visitorId);

    @Transactional(readOnly = true)
    List<VisitorProvisionLogEntity> selectByDeviceId(Long deviceId);

    @Transactional(readOnly = true)
    List<VisitorProvisionLogEntity> selectByTimeRange(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
```

### 📹 实时监控
```java
// 门禁事件实时监控 (Jakarta EE 3.0+)
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AccessEventMonitor {

    @Resource
    private AccessEventManager accessEventManager;

    @Resource
    private AlertServiceClient alertServiceClient;

    @EventListener
    @Async("accessEventExecutor")  // 异步处理，不阻塞主流程
    public void handleAccessEvent(AccessEvent event) {
        log.info("[门禁事件] 接收到事件, eventId={}, userId={}, deviceId={}",
                event.getEventId(), event.getUserId(), event.getDeviceId());

        try {
            // 委托给Manager层处理复杂业务逻辑
            accessEventManager.processAccessEvent(event);
        } catch (Exception e) {
            log.error("[门禁事件] 处理异常, eventId={}", event.getEventId(), e);
            // 发送告警通知监控团队
            sendSystemAlert("ACCESS_EVENT_PROCESS_ERROR", "门禁事件处理异常", e);
        }
    }

    private void sendSystemAlert(String alertCode, String message, Exception e) {
        AlertRequest alert = AlertRequest.builder()
            .alertCode(alertCode)
            .alertLevel("HIGH")
            .message(message)
            .details(Map.of("exception", e.getMessage(), "timestamp", LocalDateTime.now()))
            .build();

        alertServiceClient.sendAlert(alert);
    }
}

// Manager层 - 门禁事件处理业务逻辑
public class AccessEventManagerImpl implements AccessEventManager {

    private final AccessEventDao accessEventDao;
    private final AnomalyDetectionService anomalyDetectionService;
    private final StatisticsService statisticsService;
    private final AlertServiceClient alertServiceClient;

    // 构造函数注入依赖
    public AccessEventManagerImpl(
            AccessEventDao accessEventDao,
            AnomalyDetectionService anomalyDetectionService,
            StatisticsService statisticsService,
            AlertServiceClient alertServiceClient) {
        this.accessEventDao = accessEventDao;
        this.anomalyDetectionService = anomalyDetectionService;
        this.statisticsService = statisticsService;
        this.alertServiceClient = alertServiceClient;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processAccessEvent(AccessEvent event) {
        // 1. 实时事件处理
        processRealTimeEvent(event);

        // 2. 异常检测
        AnomalyResult anomalyResult = detectAnomaly(event);
        if (anomalyResult.isAnomaly()) {
            triggerAlert(event, anomalyResult);
        }

        // 3. 数据统计更新
        updateStatistics(event);
    }

    private void processRealTimeEvent(AccessEvent event) {
        // 1. 持久化事件数据
        AccessEventEntity entity = convertToEntity(event);
        accessEventDao.insert(entity);

        // 2. 更新设备状态
        updateDeviceStatus(event.getDeviceId(), event.getAccessResult());

        // 3. 更新用户最后访问时间
        updateLastAccessTime(event.getUserId(), event.getAccessTime());
    }

    private AnomalyResult detectAnomaly(AccessEvent event) {
        return anomalyDetectionService.detectAnomaly(event);
    }

    private void triggerAlert(AccessEvent event, AnomalyResult anomalyResult) {
        AlertRequest alert = AlertRequest.builder()
            .alertCode("ACCESS_ANOMALY_DETECTED")
            .alertLevel(anomalyResult.getSeverity())
            .title("门禁异常检测")
            .message(String.format("检测到异常访问行为: %s", anomalyResult.getDescription()))
            .sourceDeviceId(event.getDeviceId())
            .sourceUserId(event.getUserId())
            .eventTime(event.getAccessTime())
            .details(Map.of(
                "eventId", event.getEventId(),
                "anomalyType", anomalyResult.getAnomalyType(),
                "confidence", anomalyResult.getConfidence(),
                "riskScore", anomalyResult.getRiskScore()
            ))
            .build();

        alertServiceClient.sendAlert(alert);

        // 记录异常日志
        log.warn("[门禁异常] 检测到异常访问, eventId={}, userId={}, anomalyType={}, riskScore={}",
                event.getEventId(), event.getUserId(), anomalyResult.getAnomalyType(), anomalyResult.getRiskScore());
    }

    private void updateStatistics(AccessEvent event) {
        // 异步更新统计数据，避免阻塞主流程
        statisticsService.updateAccessStatistics(event);
    }

    private void updateDeviceStatus(Long deviceId, String accessResult) {
        // 通过网关调用设备服务更新设备状态
        ResponseDTO<Void> result = gatewayServiceClient.callDeviceCommService(
            "/api/v1/device/" + deviceId + "/status",
            HttpMethod.PUT,
            Map.of("lastAccessTime", LocalDateTime.now(), "lastAccessResult", accessResult),
            Void.class
        );

        if (result.getCode() != 200) {
            log.error("[设备状态更新] 失败, deviceId={}, result={}", deviceId, result.getMessage());
        }
    }

    private void updateLastAccessTime(Long userId, LocalDateTime accessTime) {
        // 通过网关调用公共服务更新用户最后访问时间
        ResponseDTO<Void> result = gatewayServiceClient.callCommonService(
            "/api/v1/user/" + userId + "/last-access-time",
            HttpMethod.PUT,
            Map.of("lastAccessTime", accessTime),
            Void.class
        );

        if (result.getCode() != 200) {
            log.error("[用户访问时间更新] 失败, userId={}, result={}", userId, result.getMessage());
        }
    }

    private AccessEventEntity convertToEntity(AccessEvent event) {
        return AccessEventEntity.builder()
            .eventId(event.getEventId())
            .userId(event.getUserId())
            .deviceId(event.getDeviceId())
            .areaId(event.getAreaId())
            .accessType(event.getAccessType())
            .accessResult(event.getAccessResult())
            .accessTime(event.getAccessTime())
            .biometricData(event.getBiometricData())
            .deviceIp(event.getDeviceIp())
            .userAgent(event.getUserAgent())
            .build();
    }
}

// DAO层 - 门禁事件数据访问
@Mapper
public interface AccessEventDao extends BaseMapper<AccessEventEntity> {

    @Transactional(readOnly = true)
    List<AccessEventEntity> selectByUserIdAndTimeRange(
        @Param("userId") Long userId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @Transactional(readOnly = true)
    List<AccessEventEntity> selectByDeviceIdAndTimeRange(
        @Param("deviceId") Long deviceId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @Transactional(readOnly = true)
    Long countByDeviceIdAndResult(
        @Param("deviceId") Long deviceId,
        @Param("accessResult") String accessResult,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @Transactional(readOnly = true)
    List<AccessEventEntity> selectRecentFailedAttempts(
        @Param("deviceId") Long deviceId,
        @Param("minutes") Integer minutes
    );
}

// 实体类 - 门禁事件
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_access_event")
public class AccessEventEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private String eventId;

    @TableField("user_id")
    private Long userId;

    @TableField("device_id")
    private Long deviceId;

    @TableField("area_id")
    private Long areaId;

    @TableField("access_type")
    private Integer accessType;

    @TableField("access_result")
    private String accessResult;

    @TableField("access_time")
    private LocalDateTime accessTime;

    @TableField("biometric_data")
    private String biometricData;

    @TableField("device_ip")
    private String deviceIp;

    @TableField("user_agent")
    private String userAgent;

    @TableField("processing_time")
    private Long processingTime;  // 处理耗时(毫秒)

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted_flag")
    private Integer deletedFlag;
}

// 异步任务配置
@Configuration
@EnableAsync
public class AccessAsyncConfig {

    @Bean("accessEventExecutor")
    public TaskExecutor accessEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("AccessEvent-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

---

## 🔧 技术栈和工具

### 核心技术
- **Spring Boot 3.x**: 微服务框架
- **Spring Security**: 安全框架和认证授权
- **MyBatis-Plus**: 数据访问层
- **Redis**: 缓存和会话管理
- **RabbitMQ**: 异步消息处理

### 生物识别技术
- **人脸识别**: 基于深度学习的人脸检测和识别
- **指纹识别**: 指纹特征提取和匹配算法
- **虹膜识别**: 高精度虹膜识别技术
- **多模态融合**: 多种生物特征的融合验证

### 设备协议
- **TCP/IP协议**: 网络门禁设备通讯
- **HTTP/HTTPS协议**: Web门禁系统集成
- **串口协议**: RS485、RS232等传统门禁设备
- **WebSocket**: 实时双向通讯

---

## 📊 性能指标

### 响应时间要求
- **生物识别验证**: ≤ 500ms (95%分位)
- **权限策略检查**: ≤ 100ms (95%分位)
- **设备控制响应**: ≤ 200ms (95%分位)
- **跨服务调用响应**: ≤ 1s (95%分位)
- **访客权限下发**: ≤ 3s (95%分位)

### 并发处理能力
- **并发用户数**: ≥ 10,000
- **设备连接数**: ≥ 50,000
- **跨服务调用QPS**: ≥ 1,000
- **事件处理吞吐**: ≥ 100,000 events/minute
- **数据查询QPS**: ≥ 5,000
- **访客权限下发吞吐**: ≥ 200/分钟

### 可用性指标
- **服务可用性**: ≥ 99.9%
- **数据一致性**: 强一致性保证
- **故障恢复时间**: ≤ 30s
- **数据丢失率**: 0%

---

## 🛡️ 安全设计

### 数据安全
```java
// 敏感数据加密存储 (Jakarta EE 3.0+)
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.transaction.Transactional;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_biometric_template")
public class BiometricTemplateEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long templateId;

    @TableField("user_id")
    private Long userId;

    @TableField("biometric_type")
    private Integer biometricType;  // 1-人脸 2-指纹 3-虹膜 4-掌纹

    @TableField("template_data")
    @Lob
    @Convert(converter = EncryptedStringConverter.class)
    private String templateData;  // 生物特征数据加密存储

    @TableField("personal_info")
    @Convert(converter = EncryptedStringConverter.class)
    private String personalInfo;   // 个人信息加密存储

    @TableField("security_level")
    private Integer securityLevel;  // 安全等级 1-低 2-中 3-高

    @TableField("valid_until")
    private LocalDateTime validUntil;  // 有效期

    @TableField("status")
    private Integer status;  // 1-正常 2-过期 3-禁用

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted_flag")
    private Integer deletedFlag;

    @Version
    private Integer version;

    @PrePersist
    @PreUpdate
    protected void encryptSensitiveData() {
        // 自动加密敏感数据
        if (StringUtils.isNotEmpty(templateData) && !isEncrypted(templateData)) {
            this.templateData = encryptData(templateData);
        }
        if (StringUtils.isNotEmpty(personalInfo) && !isEncrypted(personalInfo)) {
            this.personalInfo = encryptData(personalInfo);
        }
    }
}

// API接口安全控制 (Jakarta EE 3.0+)
@RestController
@RequestMapping("/api/v1/access")
@Tag(name = "门禁验证", description = "门禁访问控制和安全验证")
@PreAuthorize("hasRole('ACCESS_CONTROL_ADMIN')")
public class AccessController {

    @Resource
    private AccessService accessService;

    @Resource
    private DataSecurityManager dataSecurityManager;

    /**
     * 门禁验证接口
     */
    @PostMapping("/verify")
    @RateLimiter(name = "access-verify", fallbackMethod = "verifyFallback")
    @ApiOperation(value = "门禁验证", notes = "生物识别和权限验证")
    public ResponseDTO<AccessResult> verifyAccess(@Valid @RequestBody AccessRequest request) {
        log.info("[门禁验证] 开始验证, userId={}, deviceId={}", request.getUserId(), request.getDeviceId());

        // 数据脱敏处理
        AccessRequest sanitizedRequest = sanitizeRequest(request);

        AccessResult result = accessService.verifyAccess(sanitizedRequest);

        // 返回结果脱敏
        AccessResult sanitizedResult = sanitizeResult(result);

        log.info("[门禁验证] 验证完成, userId={}, deviceId={}, result={}",
                request.getUserId(), request.getDeviceId(), result.getAccessStatus());

        return ResponseDTO.ok(sanitizedResult);
    }

    /**
     * 生物特征模板管理
     */
    @PostMapping("/biometric/template")
    @PreAuthorize("hasRole('BIOMETRIC_ADMIN')")
    @RateLimiter(name = "biometric-template", fallbackMethod = "templateFallback")
    public ResponseDTO<Void> createBiometricTemplate(@Valid @RequestBody BiometricTemplateRequest request) {
        log.info("[生物特征模板] 创建模板, userId={}, biometricType={}", request.getUserId(), request.getBiometricType());

        accessService.createBiometricTemplate(request);

        return ResponseDTO.ok();
    }

    /**
     * 服务降级处理
     */
    public ResponseDTO<AccessResult> verifyFallback(AccessRequest request, Exception ex) {
        log.error("[门禁验证] 服务降级, userId={}, deviceId={}", request.getUserId(), request.getDeviceId(), ex);

        // 安全优先降级策略：拒绝访问
        AccessResult fallbackResult = AccessResult.builder()
            .accessStatus("DENIED")
            .errorCode("SERVICE_DEGRADED")
            .errorMessage("系统繁忙，请稍后重试")
            .timestamp(LocalDateTime.now())
            .build();

        return ResponseDTO.error("SERVICE_DEGRADED", "系统繁忙，请稍后重试", fallbackResult);
    }

    public ResponseDTO<Void> templateFallback(BiometricTemplateRequest request, Exception ex) {
        log.error("[生物特征模板] 服务降级, userId={}", request.getUserId(), ex);
        return ResponseDTO.error("SERVICE_DEGRADED", "系统繁忙，请稍后重试");
    }

    /**
     * 请求数据脱敏
     */
    private AccessRequest sanitizeRequest(AccessRequest request) {
        AccessRequest sanitized = AccessRequest.builder()
            .userId(request.getUserId())
            .deviceId(request.getDeviceId())
            .areaId(request.getAreaId())
            .accessType(request.getAccessType())
            .build();

        // 生物特征数据脱敏，只保留必要信息用于验证
        if (StringUtils.isNotEmpty(request.getBiometricData())) {
            sanitized.setBiometricData(maskBiometricData(request.getBiometricData()));
        }

        return sanitized;
    }

    /**
     * 返回结果脱敏
     */
    private AccessResult sanitizeResult(AccessResult result) {
        return AccessResult.builder()
            .accessStatus(result.getAccessStatus())
            .userId(maskUserId(result.getUserId()))
            .deviceId(result.getDeviceId())
            .accessTime(result.getAccessTime())
            .timestamp(result.getTimestamp())
            .build();
    }

    /**
     * 生物特征数据脱敏
     */
    private String maskBiometricData(String biometricData) {
        if (StringUtils.isEmpty(biometricData)) {
            return null;
        }
        // 保留前8位和后8位，中间用*替代
        int length = biometricData.length();
        if (length <= 16) {
            return "********";
        }
        return biometricData.substring(0, 8) + "****" + biometricData.substring(length - 8);
    }

    /**
     * 用户ID脱敏
     */
    private Long maskUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        // 只保留用户ID后4位，其他位用0替代
        return userId % 10000;
    }
}

// 数据安全管理器
@Component
public class DataSecurityManager {

    @Resource
    private AESUtil aesUtil;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ENCRYPTION_PREFIX = "ENC:";
    private static final int MAX_ENCRYPTION_ATTEMPTS = 3;

    /**
     * 加密数据
     */
    public String encryptData(String plainText) {
        if (StringUtils.isEmpty(plainText)) {
            return plainText;
        }

        try {
            String encrypted = aesUtil.encrypt(plainText);
            return ENCRYPTION_PREFIX + encrypted;
        } catch (Exception e) {
            log.error("[数据加密] 加密失败", e);
            throw new BusinessException("ENCRYPTION_FAILED", "数据加密失败");
        }
    }

    /**
     * 解密数据
     */
    public String decryptData(String encryptedText) {
        if (StringUtils.isEmpty(encryptedText)) {
            return encryptedText;
        }

        if (!isEncrypted(encryptedText)) {
            return encryptedText;
        }

        // 限制解密尝试次数
        String attemptKey = "decrypt:attempt:" + encryptedText.hashCode();
        Integer attempts = (Integer) redisTemplate.opsForValue().get(attemptKey);
        if (attempts != null && attempts >= MAX_ENCRYPTION_ATTEMPTS) {
            throw new BusinessException("DECRYPTION_BLOCKED", "解密尝试次数过多，已被阻止");
        }

        try {
            String dataPart = encryptedText.substring(ENCRYPTION_PREFIX.length());
            String decrypted = aesUtil.decrypt(dataPart);

            // 清除尝试计数
            redisTemplate.delete(attemptKey);

            return decrypted;
        } catch (Exception e) {
            log.error("[数据解密] 解密失败", e);

            // 增加尝试计数
            redisTemplate.opsForValue().increment(attemptKey);
            redisTemplate.expire(attemptKey, Duration.ofMinutes(5));

            throw new BusinessException("DECRYPTION_FAILED", "数据解密失败");
        }
    }

    /**
     * 检查是否为加密数据
     */
    public boolean isEncrypted(String data) {
        return StringUtils.isNotEmpty(data) && data.startsWith(ENCRYPTION_PREFIX);
    }

    /**
     * 验证生物特征数据格式
     */
    public boolean validateBiometricDataFormat(String data, Integer biometricType) {
        if (StringUtils.isEmpty(data)) {
            return false;
        }

        switch (biometricType) {
            case 1: // 人脸
                return validateFaceDataFormat(data);
            case 2: // 指纹
                return validateFingerprintDataFormat(data);
            case 3: // 虹膜
                return validateIrisDataFormat(data);
            case 4: // 掌纹
                return validatePalmDataFormat(data);
            default:
                return false;
        }
    }

    private boolean validateFaceDataFormat(String data) {
        // 人脸特征向量格式验证
        return data.matches("^[A-Za-z0-9+/=]+$") && data.length() >= 128;
    }

    private boolean validateFingerprintDataFormat(String data) {
        // 指纹特征数据格式验证
        return data.matches("^[A-Za-z0-9+/=]+$") && data.length() >= 256;
    }

    private boolean validateIrisDataFormat(String data) {
        // 虹膜特征数据格式验证
        return data.matches("^[A-Za-z0-9+/=]+$") && data.length() >= 512;
    }

    private boolean validatePalmDataFormat(String data) {
        // 掌纹特征数据格式验证
        return data.matches("^[A-Za-z0-9+/=]+$") && data.length() >= 384;
    }
}

// AES加密工具类
@Component
public class AESUtil {

    @Value("${app.security.aes.key:defaultKey123456789}")
    private String secretKey;

    @Value("${app.security.aes.iv:defaultIV123456789}")
    private String ivParameter;

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    /**
     * AES加密
     */
    public String encrypt(String plainText) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivParameterSpec.getBytes());

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

        byte[] encrypted = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * AES解密
     */
    public String decrypt(String encryptedText) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivParameterSpec.getBytes());

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

        byte[] decoded = Base64.getDecoder().decode(encryptedText);
        byte[] decrypted = cipher.doFinal(decoded);

        return new String(decrypted);
    }
}
```

### 访问控制
- **角色基础访问控制(RBAC)**: 用户-角色-权限三层模型
- **基于时间的访问控制**: 支持时间段限制访问
- **基于位置的访问控制**: 支持地理位置限制
- **动态权限策略**: 支持运行时权限策略调整

---

## 📋 开发检查清单

### 功能开发检查
- [ ] 生物识别算法集成和测试
- [ ] 权限策略引擎实现
- [ ] 设备协议适配器开发
- [ ] 实时监控系统集成
- [ ] 跨服务调用接口开发
- [ ] 访客权限下发接口实现

### 安全检查
- [ ] 敏感数据加密存储
- [ ] API接口权限控制
- [ ] 生物特征数据脱敏
- [ ] 跨服务调用安全验证
- [ ] 访问日志记录和审计
- [ ] 异常行为检测

### 性能检查
- [ ] 高并发场景测试
- [ ] 响应时间优化
- [ ] 缓存策略实现
- [ ] 数据库索引优化
- [ ] 负载均衡配置

---

## 🔗 相关技能文档

- **visitor-service-specialist**: 访客服务专家（调用方）
- **biometric-architecture-specialist**: 生物识别架构专家
- **device-protocol-specialist**: 设备协议专家
- **security-protection-specialist**: 安全防护专家
- **performance-optimization-specialist**: 性能优化专家
- **real-time-monitoring-specialist**: 实时监控专家
- **gateway-service-specialist**: 网关服务专家（服务间调用）

---

## 📞 联系和支持

**技能负责人**: 访问控制服务开发团队
**技术支持**: 架构师团队 + 安全团队
**问题反馈**: 通过项目管理系统提交

**版本信息**:
- **创建时间**: 2025-12-02
- **最后更新**: 2025-12-02
- **版本**: v1.0.0

---

**💡 重要提醒**: 本技能专注于门禁访问控制的核心业务，特别是作为被调用方接收访客服务的权限下发请求。需要结合访客服务、生物识别、设备控制、安全防护等相关技能一起使用，确保系统的完整性和安全性。注意：门禁模块不包含访客管理功能，访客功能由独立的访客服务提供。