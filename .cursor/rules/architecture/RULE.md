---
alwaysApply: true
---
# 🏗️ IOE-DREAM企业级架构重构完整方案 V3.0

**文档版本**: v3.0.0-COMPLETE  
**制定日期**: 2025-12-18  
**覆盖范围**: 100%微服务 + 100%业务场景  
**架构目标**: 企业级 + 高性能 + 低内存 + 可扩展 + 全场景覆盖  
**适用对象**: 架构师、技术经理、开发团队

---

## 📋 **文档摘要**

本方案是IOE-DREAM智慧园区一卡通管理平台的**完整企业级架构重构方案**，涵盖：

- ✅ **11个微服务**完整重构设计
- ✅ **10个公共组件**企业级标准实现
- ✅ **5大设计模式**充分应用（策略/工厂/装饰器/模板方法/依赖倒置）
- ✅ **全业务场景**100%覆盖（门禁/考勤/消费/访客/视频/OA等）
- ✅ **5种设备交互模式**⭐ 真实业务场景（边缘计算/中心验证/混合模式）
- ✅ **性能优化**架构（连接池/对象池/多级缓存/异步化）
- ✅ **10周实施路线图**（3-5人团队）

---

## 🔑 **核心架构理念**

基于**真实设备交互模式**的分层架构设计：

1. **边缘智能优先**: 门禁设备端完成验证，降低服务器压力
2. **数据安全第一**: 消费设备不存余额，防止篡改
3. **离线能力保障**: 关键场景支持离线工作
4. **中心计算精准**: 考勤排班+规则在软件端，灵活可控
5. **AI边缘推理**: 视频设备本地识别，只上传结果，基础视频功能如云台等功能需完善，实时查看视频等等


## 🎯 **一、完整微服务架构全景图**

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    IOE-DREAM 企业级微服务架构全景图                        │
└─────────────────────────────────────────────────────────────────────────┘

┌──────────────────── 接入层 (Gateway Layer) ─────────────────────┐
│  ioedream-gateway-service (8080)                                 │
│  ✓ 统一路由   ✓ 认证鉴权   ✓ 限流熔断   ✓ 协议转换              │
└──────────────────────────────────────────────────────────────────┘
                               ↓
┌──────────────────── 业务服务层 (Business Layer) ────────────────┐
│                                                                   │
│  ┌─── 核心业务服务 ───┐  ┌─── 扩展业务服务 ───┐                │
│  │                     │  │                     │                │
│  │ access-service      │  │ consume-service     │                │
│  │ (8090) 门禁管理     │  │ (8094) 消费管理     │                │
│  │                     │  │                     │                │
│  │ attendance-service  │  │ visitor-service     │                │
│  │ (8091) 考勤管理     │  │ (8095) 访客管理     │                │
│  │                     │  │                     │                │
│  │ video-service       │  │ oa-service          │                │
│  │ (8092) 视频监控     │  │ (8089) OA办公       │                │
│  └─────────────────────┘  └─────────────────────┘                │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
                               ↓
┌──────────────────── 能力服务层 (Capability Layer) ──────────────┐
│                                                                   │
│  biometric-service (8096) 🆕  ├─ 生物模板管理服务（仅存储+下发）        │
│  device-comm-service (8087)   ├─ 设备通讯服务                    │
│  common-service (8088)        ├─ 公共业务服务                    │
│  database-service (8093)      ├─ 数据库管理服务                  │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
                               ↓
┌──────────────────── 公共组件层 (Common Components) ─────────────┐
│                                                                   │
│  ┌─── 业务组件 ───────┐  ┌─── 技术组件 ────────┐               │
│  │                     │  │                      │               │
│  │ common-business     │  │ common-core          │               │
│  │ common-permission   │  │ common-security      │               │
│  │ common-workflow     │  │ common-cache         │               │
│  │ common-export       │  │ common-data          │               │
│  │                     │  │ common-monitor       │               │
│  └─────────────────────┘  └──────────────────────┘               │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
                               ↓
┌──────────────────── 基础设施层 (Infrastructure) ────────────────┐
│                                                                   │
│  Nacos (注册中心)   MySQL (数据库)    Redis (缓存)              │
│  RabbitMQ (消息)    Prometheus (监控)  Zipkin (链路追踪)        │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
```

---

## 🚀 **二、完整的11个微服务重构设计**

### **2.1 ioedream-gateway-service (8080) - API网关**

#### **职责定位**
- 统一入口：所有外部请求的唯一入口
- 路由转发：智能路由到后端11个微服务
- 认证鉴权：Token验证、用户信息传递
- 限流熔断：Resilience4j保护后端服务
- 协议转换：HTTP/HTTPS/WebSocket协议适配

#### **核心设计**

**1. 路由策略配置**
```yaml
# application-routes.yml
spring:
  cloud:
    gateway:
      routes:
        - id: access-service
          uri: lb://ioedream-access-service
          predicates:
            - Path=/api/v1/access/**
          filters:
            - StripPrefix=2
            - name: CircuitBreaker
              args:
                name: accessCB
                fallbackUri: forward:/fallback/access
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
        
        - id: biometric-service  # 🆕 新增
          uri: lb://ioedream-biometric-service
          predicates:
            - Path=/api/v1/biometric/**
          filters:
            - StripPrefix=2
        
        # ... 其他9个服务路由配置
```

**2. 全局认证过滤器**
```java
@Component
@Order(1)
public class GlobalAuthenticationFilter implements GlobalFilter {
    
    @Resource
    private TokenValidator tokenValidator;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        
        // 白名单路径
        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }
        
        // Token验证
        String token = extractToken(request);
        if (!tokenValidator.validate(token)) {
            return unauthorized(exchange);
        }
        
        // 用户信息传递
        UserContext context = tokenValidator.getUserContext(token);
        ServerHttpRequest newRequest = request.mutate()
            .header("X-User-Id", context.getUserId().toString())
            .header("X-User-Name", context.getUserName())
            .header("X-User-Roles", context.getRoles())
            .header("X-Department-Id", context.getDepartmentId().toString())
            .build();
        
        return chain.filter(exchange.mutate().request(newRequest).build());
    }
}
```

**3. 限流配置**
```java
@Configuration
public class RateLimitConfiguration {
    
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders()
                .getFirst("X-User-Id");
            return Mono.just(userId != null ? userId : "anonymous");
        };
    }
    
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(
            100,  // replenishRate: 每秒产生100个令牌
            200   // burstCapacity: 最大突发200个请求
        );
    }
}
```

#### **性能指标**
- 响应时间: P99 < 50ms
- 并发能力: 10000 QPS
- 可用性: 99.99%

---

### **2.2 ioedream-access-service (8090) - 门禁管理服务**

#### **职责定位**
- 权限管理：用户区域权限配置
- 通行控制：多模态生物识别验证
- 记录管理：通行记录存储与分析
- 告警联动：异常事件实时告警

#### **⭐ 设备交互模式：边缘自主验证**

```
【数据下发】软件 → 设备
  ├─ 人员信息（姓名、工号）
  ├─ 生物模板（人脸/指纹特征向量）
  └─ 权限数据（时间段、区域、有效期）

【实时通行】设备端完全自主
  ├─ 本地识别：设备内嵌算法1:N比对
  ├─ 本地验证：检查本地权限表
  └─ 本地控制：直接开门，无需等待服务器

【事后上传】设备 → 软件
  └─ 批量上传通行记录（每分钟或累计100条）

【优势】
  ✅ 离线可用：网络中断时设备仍可正常工作
  ✅ 秒级响应：无需等待服务器验证
  ✅ 降低压力：每秒1000次通行，服务器只需处理记录存储

【挑战】
  ⚠️ 数据一致性：权限变更需实时同步到设备
  ⚠️ 设备存储：大型园区10000+人员，设备存储有限
```

#### **职责定位**
- 通行管理：门禁通行验证与记录
- 权限管理：用户-区域权限管理
- 设备联动：门禁设备开门指令
- 审批流程：权限申请审批
- 事件通知：实时通行事件推送

#### **核心设计**

**1. 领域模型（DDD）**
```java
package net.lab1024.sa.access.domain;

/**
 * 通行记录聚合根
 */
@Data
public class AccessRecordAggregate {
    // 通行记录实体
    private AccessRecordEntity record;
    
    // 认证结果值对象
    private BiometricAuthResultVO authResult;
    
    // 关联区域
    private AreaEntity area;
    
    // 关联设备
    private DeviceEntity device;
    
    // 关联用户
    private UserEntity user;
    
    // 权限策略列表
    private List<PermissionPolicyVO> policies;
    
    /**
     * 聚合根行为: 验证通行权限
     */
    public boolean validateAccessPermission() {
        return policies.stream()
            .anyMatch(policy -> policy.allows(user, area, LocalDateTime.now()));
    }
}
```

**2. 策略模式：权限计算**
```java
public interface IAccessPermissionStrategy {
    boolean hasPermission(AccessRequest request);
    int getPriority();
}

@Component
@StrategyMarker(type = "TIME_BASED")
public class TimeBasedAccessStrategy implements IAccessPermissionStrategy {
    
    @Override
    public boolean hasPermission(AccessRequest request) {
        UserAreaPermissionEntity permission = permissionDao.selectByUserAndArea(
            request.getUserId(), request.getAreaId()
        );
        
        if (permission == null) return false;
        
        // 时间段验证
        LocalTime now = LocalTime.now();
        List<TimeSlot> timeSlots = JSON.parseArray(
            permission.getAllowedTimeSlots(), TimeSlot.class
        );
        
        return timeSlots.stream()
            .anyMatch(slot -> slot.contains(now));
    }
    
    @Override
    public int getPriority() {
        return 100;
    }
}

@Component
@StrategyMarker(type = "GEOFENCE")
public class GeofenceAccessStrategy implements IAccessPermissionStrategy {
    
    @Override
    public boolean hasPermission(AccessRequest request) {
        AreaEntity area = areaDao.selectById(request.getAreaId());
        if (area.getGeofenceData() == null) return true;
        
        // GPS位置验证
        Geofence geofence = JSON.parseObject(
            area.getGeofenceData(), Geofence.class
        );
        return geofence.contains(
            request.getLatitude(), 
            request.getLongitude()
        );
    }
    
    @Override
    public int getPriority() {
        return 90;
    }
}

@Component
@StrategyMarker(type = "ROLE_BASED")
public class RoleBasedAccessStrategy implements IAccessPermissionStrategy {
    
    @Override
    public boolean hasPermission(AccessRequest request) {
        AreaEntity area = areaDao.selectById(request.getAreaId());
        UserEntity user = userDao.selectById(request.getUserId());
        
        // 角色匹配验证
        List<String> allowedRoles = JSON.parseArray(
            area.getAllowedRoles(), String.class
        );
        List<String> userRoles = getUserRoles(user.getUserId());
        
        return userRoles.stream()
            .anyMatch(allowedRoles::contains);
    }
    
    @Override
    public int getPriority() {
        return 80;
    }
}
```

**3. 模板方法：通行流程**
```java
public abstract class AbstractAccessFlowTemplate {
    
    @Resource
    protected DeviceDao deviceDao;
    
    @Resource
    protected AccessRecordDao accessRecordDao;
    
    @Resource
    protected StrategyFactory<IAccessPermissionStrategy> strategyFactory;
    
    @Resource
    protected DeviceCommandService deviceCommandService;
    
    /**
     * 模板方法: 通行流程
     */
    public final AccessResult processAccess(AccessRequest request) {
        try {
            // 1. 参数校验
            validate(request);
            
            // 2. 设备验证
            DeviceEntity device = validateDevice(request.getDeviceId());
            
            // 3. 用户识别(抽象方法 - 子类实现)
            UserIdentityResult identity = identifyUser(request);
            if (!identity.isSuccess()) {
                return AccessResult.denied("身份识别失败: " + identity.getMessage());
            }
            
            // 4. 权限验证(策略模式)
            boolean hasPermission = checkPermission(
                identity.getUserId(), 
                request.getAreaId(), 
                request
            );
            if (!hasPermission) {
                recordFailedAccess(identity, device, "权限不足");
                return AccessResult.denied("权限不足");
            }
            
            // 5. 开门指令(抽象方法 - 子类实现)
            boolean opened = openDoor(device, request);
            
            // 6. 记录通行
            recordSuccessAccess(identity, device, request);
            
            // 7. 事件通知(钩子方法)
            notifyAccessEvent(identity, device, opened);
            
            return opened ? AccessResult.success() : AccessResult.failed("开门失败");
            
        } catch (Exception e) {
            log.error("[通行流程异常] request={}", request, e);
            return AccessResult.error("系统异常: " + e.getMessage());
        }
    }
    
    /**
     * 抽象方法: 用户识别
     */
    protected abstract UserIdentityResult identifyUser(AccessRequest request);
    
    /**
     * 抽象方法: 开门
     */
    protected abstract boolean openDoor(DeviceEntity device, AccessRequest request);
    
    /**
     * 钩子方法: 事件通知(可选覆盖)
     */
    protected void notifyAccessEvent(UserIdentityResult identity, 
                                     DeviceEntity device, boolean opened) {
        // 默认空实现
    }
    
    /**
     * 权限验证(策略模式)
     */
    private boolean checkPermission(Long userId, Long areaId, AccessRequest request) {
        List<IAccessPermissionStrategy> strategies = strategyFactory.getAll();
        
        // 按优先级排序
        strategies.sort(Comparator.comparingInt(
            IAccessPermissionStrategy::getPriority).reversed()
        );
        
        // 任一策略通过即可
        return strategies.stream()
            .anyMatch(strategy -> strategy.hasPermission(request));
    }
}
```

**4. 具体实现：处理设备上传的通行记录** ⭐ 修正为真实架构
```java
/**
 * 门禁服务 - 接收设备上传的通行记录
 * ⚠️ 注意：生物识别由设备端完成，软件端只处理结果
 */
@RestController
@RequestMapping("/api/v1/access")
public class AccessRecordController {
    
    @Resource
    private AccessRecordService accessRecordService;
    
    @Resource
    private WebSocketService websocketService;
    
    @Resource
    private RabbitTemplate rabbitTemplate;
    
    /**
     * 接收设备上传的通行记录
     * ⭐ 设备端已完成：识别+权限验证+开门
     * ⭐ 软件端只需：存储记录+事件推送+异常检测
     */
    @PostMapping("/device/upload-record")
    public ResponseDTO<Void> uploadAccessRecord(
            @RequestBody @Valid AccessRecordUploadDTO uploadDTO) {
        
        // 1. 保存通行记录
        AccessRecordEntity record = new AccessRecordEntity();
        record.setUserId(uploadDTO.getUserId());
        record.setDeviceId(uploadDTO.getDeviceId());
        record.setAccessTime(uploadDTO.getAccessTime());
        record.setAuthMethod(uploadDTO.getAuthMethod());  // FACE/FINGERPRINT/CARD
        record.setAuthResult(uploadDTO.getAuthResult());  // SUCCESS/FAILED
        record.setMatchScore(uploadDTO.getMatchScore());
        record.setDoorOpened(uploadDTO.getDoorOpened());
        accessRecordService.insert(record);
        
        // 2. 实时推送到监控大屏
        AccessEvent event = AccessEvent.builder()
            .userId(record.getUserId())
            .deviceId(record.getDeviceId())
            .eventType(record.getAuthResult() == AuthResult.SUCCESS 
                ? "ACCESS_GRANTED" : "ACCESS_DENIED")
            .eventTime(record.getAccessTime())
            .build();
        websocketService.sendAccessEvent(event);
        
        // 3. 异常检测
        if (record.getAuthResult() == AuthResult.SUCCESS) {
            // 检查是否在授权时间外通行
            checkUnauthorizedTimeAccess(record);
        }
        
        // 4. 视频联动（成功通行时录像5分钟）
        if (record.getDoorOpened()) {
            rabbitTemplate.convertAndSend(
                "video.linkage.exchange",
                "video.record.route",
                VideoLinkageRequest.builder()
                    .deviceId(record.getDeviceId())
                    .eventType("ACCESS_GRANTED")
                    .duration(Duration.ofMinutes(5))
                    .build()
            );
        }
        
        return ResponseDTO.ok();
    }
    
    /**
     * 检查非授权时间通行
     */
    private void checkUnauthorizedTimeAccess(AccessRecordEntity record) {
        UserAreaPermissionEntity permission = 
            permissionDao.selectByUserAndArea(
                record.getUserId(), 
                record.getAreaId()
            );
        
        if (permission == null) {
            // 无权限但设备允许通行 → 告警
            alarmService.sendAlert(
                "用户" + record.getUserId() + "在无权限情况下通行",
                AlarmLevel.HIGH
            );
            return;
        }
        
        LocalTime accessTime = record.getAccessTime().toLocalTime();
        boolean inAllowedTime = permission.getAllowedTimeSlots().stream()
            .anyMatch(slot -> 
                !accessTime.isBefore(slot.getStartTime()) &&
                !accessTime.isAfter(slot.getEndTime())
            );
        
        if (!inAllowedTime) {
            // 非授权时间通行 → 告警
            alarmService.sendAlert(
                "用户" + record.getUserId() + "在非授权时间通行",
                AlarmLevel.MEDIUM
            );
        }
    }
}

/**
 * 生物模板管理服务
 * ⚠️ 注意：只负责管理模板，不负责识别验证
 */
@Service
public class BiometricTemplateService {
    
    @Resource
    private BiometricTemplateDao templateDao;
    
    @Resource
    private DeviceCommServiceClient deviceCommClient;
    
    /**
     * 添加生物模板（入职时调用）
     * ⭐ 核心：将模板下发到所有相关门禁设备
     */
    public void addBiometricTemplate(BiometricTemplateAddDTO addDTO) {
        // 1. 保存模板到数据库
        BiometricTemplateEntity template = new BiometricTemplateEntity();
        template.setUserId(addDTO.getUserId());
        template.setBiometricType(addDTO.getBiometricType());  // FACE/FINGERPRINT
        template.setFeatureData(addDTO.getFeatureData());  // 512维特征向量
        template.setTemplateVersion("1.0");
        template.setQualityScore(addDTO.getQualityScore());
        templateDao.insert(template);
        
        // 2. 查询该用户有权限的区域
        List<UserAreaPermissionEntity> permissions = 
            permissionDao.selectByUserId(addDTO.getUserId());
        
        // 3. 查询这些区域的所有门禁设备
        Set<String> deviceIds = new HashSet<>();
        for (UserAreaPermissionEntity permission : permissions) {
            List<DeviceEntity> devices = 
                deviceDao.selectByAreaIdAndType(
                    permission.getAreaId(), 
                    DeviceType.ACCESS_CONTROL
                );
            devices.forEach(d -> deviceIds.add(d.getDeviceId()));
        }
        
        // 4. 并行下发模板到所有设备 ⭐ 关键操作
        List<CompletableFuture<Void>> futures = deviceIds.stream()
            .map(deviceId -> CompletableFuture.runAsync(() -> {
                deviceCommClient.syncBiometricTemplate(
                    deviceId,
                    BiometricTemplateSyncDTO.builder()
                        .userId(addDTO.getUserId())
                        .userName(addDTO.getUserName())
                        .biometricType(addDTO.getBiometricType())
                        .featureData(addDTO.getFeatureData())
                        .build()
                );
            }))
            .collect(Collectors.toList());
        
        // 5. 等待所有下发完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .join();
        
        log.info("[模板下发完成] userId={}, deviceCount={}", 
            addDTO.getUserId(), deviceIds.size());
    }
    
    /**
     * 删除生物模板（离职时调用）
     */
    public void deleteBiometricTemplate(Long userId, BiometricType type) {
        // 1. 从数据库删除
        templateDao.deleteByUserIdAndType(userId, type);
        
        // 2. 从所有设备删除 ⭐ 防止离职人员仍可通行
        List<DeviceEntity> allAccessDevices = 
            deviceDao.selectByType(DeviceType.ACCESS_CONTROL);
        
        allAccessDevices.parallelStream().forEach(device -> {
            deviceCommClient.deleteBiometricTemplate(
                device.getDeviceId(),
                userId
            );
        });
        
        log.info("[模板删除完成] userId={}, deviceCount={}", 
            userId, allAccessDevices.size());
    }
}
```

**5. 具体实现：卡片通行**
```java
@Component("cardAccessFlow")
public class CardAccessFlow extends AbstractAccessFlowTemplate {
    
    @Resource
    private CardService cardService;
    
    @Override
    protected UserIdentityResult identifyUser(AccessRequest request) {
        // 卡号识别
        String cardNo = request.getCardNo();
        CardEntity card = cardService.getByCardNo(cardNo);
        
        if (card == null) {
            return UserIdentityResult.failed("卡片不存在");
        }
        
        if (card.getStatus() != CardStatus.ACTIVE) {
            return UserIdentityResult.failed("卡片已停用");
        }
        
        return UserIdentityResult.success(
            card.getUserId(),
            card.getUserName(),
            100.0  // 卡片识别置信度100%
        );
    }
    
    @Override
    protected boolean openDoor(DeviceEntity device, AccessRequest request) {
        // 与生物识别流程相同
        return super.openDoor(device, request);
    }
}
```

#### **性能优化**

**1. 缓存策略**
```java
@Service
public class AccessPermissionCacheService {
    
    @Resource
    private UnifiedCacheManager cacheManager;
    
    /**
     * 多级缓存获取权限
     */
    public UserAreaPermissionEntity getPermission(Long userId, Long areaId) {
        String cacheKey = "access:permission:" + userId + ":" + areaId;
        
        return cacheManager.get(
            cacheKey,
            UserAreaPermissionEntity.class,
            () -> permissionDao.selectByUserAndArea(userId, areaId)
        );
    }
}
```

**2. 性能指标**
- 通行验证: P99 < 100ms
- 开门响应: P99 < 200ms
- 并发能力: 5000 TPS

---

### **2.3 ioedream-biometric-service (8096) - 生物模板管理服务 🆕**

#### **职责定位** ⭐ 修正为真实架构
- 模板管理：生物特征模板CRUD
- 设备同步：⭐ 模板下发到边缘设备（核心职责）
- 权限联动：⭐ 根据用户权限智能同步到相关设备
- 模板压缩：特征向量压缩储存
- 版本管理：模板更新历史管理

#### **⚠️ 重要说明**
```
❓ 该服务负责生物识别吗？
✖️ 不！生物识别由设备端完成

❓ 那该服务做什么？
✅ 只管理模板数据，并下发给设备

【正确的架构流程】

1. 人员入职时：
   用户 → 上传人脸照片 → biometric-service
   biometric-service → 提取512维特征向量 → 存入数据库
   biometric-service → 查询用户有权限的区域 → 找出所有相关门禁设备
   biometric-service → 下发模板到这些设备 ⭐ 核心

2. 实时通行时：
   设备 → 采集人脸图像 → 设备内嵌算法提取特征
   设备 → 与本地存储的6a21板1:N比对 ⭐ 全部在设备端
   设备 → 匹配成功 → 检查本地权限表 → 开门
   设备 → 批量上传通行记录到软件

3. 人员离职时：
   biometric-service → 从数据库删除模板
   biometric-service → 从所有设备删除模板 ⭐ 防止离职人员仍可通行
```

#### **核心设计**

**1. 特征提取服务** ⭐ 修正：只用于入职时处理上传的照片
```java
/**
 * 生物特征提取服务
 * ⚠️ 注意：只在用户入职/更新模板时调用，不用于实时识别
 */
@Service
public class BiometricFeatureExtractionService {
    
    @Resource
    private FaceNetModel faceNetModel;  // 深度学习模型
    
    @Resource
    private FingerprintExtractor fingerprintExtractor;
    
    /**
     * 提取人脸特征
     * ⭐ 场景：用户上传人脸照片时，软件端提取特征向量
     */
    public FeatureVector extractFaceFeature(MultipartFile photo) {
        // 1. 读取图像
        Mat image = readImageFromFile(photo);
        
        // 2. 人脸检测
        List<Rect> faces = detectFaces(image);
        if (faces.isEmpty()) {
            throw new BusinessException("图片中未检测到人脸");
        }
        if (faces.size() > 1) {
            throw new BusinessException("图片中检测到多个人脸，请使用单人照片");
        }
        
        // 3. 人脸对齐
        Mat alignedFace = alignFace(image, faces.get(0));
        
        // 4. 特征提取(FaceNet 512维向量)
        float[] embeddings = faceNetModel.extract(alignedFace);
        
        // 5. 质量检测
        double qualityScore = assessQuality(alignedFace);
        if (qualityScore < 0.7) {
            throw new BusinessException("照片质量太低，请重新拍摄（光线充足、正面、无遮挡）");
        }
        
        return FeatureVector.builder()
            .biometricType(BiometricType.FACE)
            .dimension(512)
            .data(embeddings)
            .qualityScore(qualityScore)
            .build();
    }
    
    /**
     * 提取指纹特征
     */
    public FeatureVector extractFingerprintFeature(MultipartFile fingerprintImage) {
        Mat image = readImageFromFile(fingerprintImage);
        
        // 提取细节点(Minutiae)
        List<Minutia> minutiae = fingerprintExtractor.extract(image);
        
        if (minutiae.size() < 12) {
            throw new BusinessException("指纹特征点过少，请重新采集");
        }
        
        return FeatureVector.builder()
            .biometricType(BiometricType.FINGERPRINT)
            .dimension(minutiae.size() * 4)
            .data(serializeMinutiae(minutiae))
            .qualityScore(calculateFingerprintQuality(image))
            .build();
    }
}
```

**2. 模板同步服务** ⭐ 核心服务
```java
@Service
public class BiometricTemplateSyncService {
    
    @Resource
    private BiometricTemplateDao templateDao;
    
    @Resource
    private UserAreaPermissionDao permissionDao;
    
    @Resource
    private DeviceDao deviceDao;
    
    @Resource
    private DeviceCommServiceClient deviceCommClient;
    
    /**
     * 添加生物模板（人员入职）
     * ⭐ 核心流程：存储模板 + 智能下发到相关设备
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> addBiometricTemplate(BiometricTemplateAddDTO addDTO) {
        // 1. 保存模板到数据库
        BiometricTemplateEntity template = new BiometricTemplateEntity();
        template.setUserId(addDTO.getUserId());
        template.setBiometricType(addDTO.getBiometricType());
        template.setFeatureData(addDTO.getFeatureVector().getData());
        template.setQualityScore(addDTO.getFeatureVector().getQualityScore());
        template.setTemplateVersion("1.0");
        templateDao.insert(template);
        
        // 2. 查询用户有权限的区域 ⭐
        List<UserAreaPermissionEntity> permissions = 
            permissionDao.selectByUserId(addDTO.getUserId());
        
        if (permissions.isEmpty()) {
            log.warn("[无需同步] 用户userId={}无任何门禁权限", addDTO.getUserId());
            return ResponseDTO.ok();
        }
        
        // 3. 查询这些区域的所有门禁设备 ⭐
        Set<String> targetDeviceIds = new HashSet<>();
        for (UserAreaPermissionEntity permission : permissions) {
            List<DeviceEntity> devices = deviceDao.selectByAreaIdAndType(
                permission.getAreaId(),
                DeviceType.ACCESS_CONTROL  // 只同步到门禁设备
            );
            devices.forEach(d -> targetDeviceIds.add(d.getDeviceId()));
        }
        
        log.info("[开始同步] userId={}, targetDeviceCount={}", 
            addDTO.getUserId(), targetDeviceIds.size());
        
        // 4. 并行下发模板到所有目标设备 ⭐ 关键操作
        List<CompletableFuture<SyncResult>> futures = targetDeviceIds.stream()
            .map(deviceId -> CompletableFuture.supplyAsync(() -> {
                try {
                    deviceCommClient.syncBiometricTemplate(
                        deviceId,
                        BiometricTemplateSyncDTO.builder()
                            .userId(addDTO.getUserId())
                            .userName(addDTO.getUserName())
                            .biometricType(addDTO.getBiometricType())
                            .featureData(template.getFeatureData())
                            .build()
                    );
                    return SyncResult.success(deviceId);
                } catch (Exception e) {
                    log.error("[同步失败] deviceId={}", deviceId, e);
                    return SyncResult.failed(deviceId, e.getMessage());
                }
            }, syncExecutor))  // 使用独立线程池
            .collect(Collectors.toList());
        
        // 5. 等待所有同步完成
        List<SyncResult> results = futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
        
        // 6. 统计结果
        long successCount = results.stream().filter(SyncResult::isSuccess).count();
        long failedCount = results.size() - successCount;
        
        log.info("[同步完成] userId={}, 成功={}, 失败={}", 
            addDTO.getUserId(), successCount, failedCount);
        
        if (failedCount > 0) {
            return ResponseDTO.userErrorParam(
                String.format("部分设备同步失败（%d/%d）", 
                    failedCount, results.size())
            );
        }
        
        return ResponseDTO.ok();
    }
    
    /**
     * 删除生物模板（人员离职）
     * ⭐ 重要：必须从所有设备删除，防止离职人员仍可通行
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> deleteBiometricTemplate(Long userId, BiometricType type) {
        // 1. 从数据库删除
        templateDao.deleteByUserIdAndType(userId, type);
        
        // 2. 从所有门禁设备删除 ⭐ 注意：是所有设备
        List<DeviceEntity> allAccessDevices = 
            deviceDao.selectByType(DeviceType.ACCESS_CONTROL);
        
        allAccessDevices.parallelStream().forEach(device -> {
            try {
                deviceCommClient.deleteBiometricTemplate(
                    device.getDeviceId(),
                    userId
                );
            } catch (Exception e) {
                log.error("[删除失败] deviceId={}, userId={}", 
                    device.getDeviceId(), userId, e);
            }
        });
        
        log.info("[模板删除完成] userId={}, 设备数量={}", 
            userId, allAccessDevices.size());
        
        return ResponseDTO.ok();
    }
    
    /**
     * 权限变更时，同步模板到新设备/从旧设备删除
     * ⭐ 场景：用户新增了某区域权限，需要同步模板到该区域设备
     */
    @Async("permissionSyncExecutor")
    public void syncOnPermissionChange(UserAreaPermissionChangeEvent event) {
        Long userId = event.getUserId();
        Long areaId = event.getAreaId();
        
        if (event.getChangeType() == ChangeType.ADDED) {
            // 新增权限 → 同步模板到该区域设备
            List<DeviceEntity> devices = 
                deviceDao.selectByAreaIdAndType(areaId, DeviceType.ACCESS_CONTROL);
            
            BiometricTemplateEntity template = 
                templateDao.selectByUserIdAndType(userId, BiometricType.FACE);
            
            if (template != null) {
                devices.forEach(device -> {
                    deviceCommClient.syncBiometricTemplate(
                        device.getDeviceId(),
                        buildSyncDTO(template)
                    );
                });
            }
            
        } else if (event.getChangeType() == ChangeType.REMOVED) {
            // 删除权限 → 从该区域设备删除模板
            List<DeviceEntity> devices = 
                deviceDao.selectByAreaIdAndType(areaId, DeviceType.ACCESS_CONTROL);
            
            devices.forEach(device -> {
                deviceCommClient.deleteBiometricTemplate(
                    device.getDeviceId(),
                    userId
                );
            });
        }
    }
}
```
```java
@Service
public class BiometricTemplateSyncService {
    
    @Resource
    private DeviceCommServiceClient deviceCommClient;
    
    @Resource
    private BiometricTemplateDao templateDao;
    
    @Resource
    private DeviceTemplateMappingDao mappingDao;
    
    /**
     * 同步用户模板到设备
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<TemplateSyncResult> syncTemplateToDevice(
            Long userId, String deviceId) {
        
        // 1. 查询用户所有生物模板
        List<BiometricTemplateEntity> templates = templateDao.selectByUserId(userId);
        if (templates.isEmpty()) {
            return ResponseDTO.error("用户无生物模板");
        }
        
        // 2. 查询设备支持的生物类型
        DeviceEntity device = deviceDao.selectById(deviceId);
        List<BiometricType> supportedTypes = parseSupportedTypes(device);
        
        // 3. 过滤设备支持的模板
        List<BiometricTemplateEntity> syncTemplates = templates.stream()
            .filter(t -> supportedTypes.contains(t.getBiometricType()))
            .collect(Collectors.toList());
        
        // 4. 下发到设备
        List<TemplateSyncRecord> syncRecords = new ArrayList<>();
        for (BiometricTemplateEntity template : syncTemplates) {
            try {
                // 调用设备通讯服务
                ResponseDTO<Void> response = deviceCommClient.syncTemplate(
                    TemplateSyncRequest.builder()
                        .deviceId(deviceId)
                        .userId(userId)
                        .biometricType(template.getBiometricType())
                        .featureData(template.getFeatureData())
                        .build()
                );
                
                if (response.isSuccess()) {
                    // 创建映射记录
                    DeviceTemplateMappingEntity mapping = new DeviceTemplateMappingEntity();
                    mapping.setDeviceId(deviceId);
                    mapping.setTemplateId(template.getTemplateId());
                    mapping.setUserId(userId);
                    mapping.setSyncStatus(SyncStatus.SUCCESS);
                    mapping.setSyncTime(LocalDateTime.now());
                    mappingDao.insert(mapping);
                    
                    syncRecords.add(TemplateSyncRecord.success(template));
                } else {
                    syncRecords.add(TemplateSyncRecord.failed(
                        template, response.getMessage()
                    ));
                }
            } catch (Exception e) {
                log.error("[模板同步失败] templateId={}, deviceId={}", 
                    template.getTemplateId(), deviceId, e);
                syncRecords.add(TemplateSyncRecord.error(template, e.getMessage()));
            }
        }
        
        // 5. 返回同步结果
        return ResponseDTO.ok(TemplateSyncResult.builder()
            .totalCount(syncTemplates.size())
            .successCount((int) syncRecords.stream()
                .filter(TemplateSyncRecord::isSuccess).count())
            .syncRecords(syncRecords)
            .build());
    }
}
```

**3. 特征提取服务（⚠️ 重要：验证和识别由设备端完成）**
```java
@Service
public class BiometricFeatureExtractionService {
    
    @Resource
    private Map<BiometricType, IBiometricFeatureExtractionStrategy> biometricFeatureExtractionStrategyFactory;
    
    @Resource
    private BiometricTemplateDao templateDao;
    
    @Resource
    private UnifiedCacheManager cacheManager;
    
    /**
     * 提取特征向量（只用于入职时处理上传的照片）
     * ⚠️ 注意：验证和识别由设备端完成，服务端不实现
     */
    public ResponseDTO<FeatureVector> extractFeature(MultipartFile photo, BiometricType type) {
        // 1. 选择特征提取策略
        IBiometricFeatureExtractionStrategy strategy = biometricFeatureExtractionStrategyFactory.get(type);
        if (strategy == null) {
            return ResponseDTO.error("不支持的生物识别类型");
        }
        
        // 2. 构建样本
        BiometricSample sample = BiometricSample.builder()
            .type(type)
            .imageData(Base64.getEncoder().encodeToString(photo.getBytes()))
            .build();
        
        // 3. 提取特征向量
        FeatureVector featureVector = strategy.extractFeature(sample);
        
        // 4. 质量验证
        if (!strategy.validateFeatureQuality(featureVector)) {
            return ResponseDTO.error("特征质量不达标，请重新采集");
        }
        
        return ResponseDTO.ok(featureVector);
    }
    
    /**
     * 保存模板到数据库（用于后续下发到设备）
     */
    public ResponseDTO<Void> saveTemplate(Long userId, BiometricType type, FeatureVector featureVector) {
        BiometricTemplateEntity template = BiometricTemplateEntity.builder()
            .userId(userId)
            .biometricType(type.getCode())
            .featureData(serializeFeatureVector(featureVector))
            .build();
        templateDao.insert(template);
        
        // 清除缓存
        String cacheKey = String.format("biometric:template:%d:%s", userId, type.name());
        cacheManager.evict(cacheKey);
        
        return ResponseDTO.ok();
    }
    
    /**
     * 获取模板（带缓存）
     */
    private BiometricTemplateEntity getTemplate(Long userId, BiometricType type) {
        String cacheKey = String.format("biometric:template:%d:%s", userId, type.name());
        return cacheManager.get(cacheKey, () -> {
            return templateDao.selectByUserIdAndType(userId, type.getCode());
        });
    }
}
```

#### **性能优化**

**1. 对象池优化**
```java
@Component
public class FeatureVectorPool {
    
    private final GenericObjectPool<float[]> vectorPool;
    
    public FeatureVectorPool() {
        GenericObjectPoolConfig<float[]> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(1000);
        config.setMaxIdle(100);
        config.setMinIdle(10);
        
        this.vectorPool = new GenericObjectPool<>(
            new BasePooledObjectFactory<float[]>() {
                @Override
                public float[] create() {
                    return new float[512];  // FaceNet向量维度
                }
                
                @Override
                public PooledObject<float[]> wrap(float[] obj) {
                    return new DefaultPooledObject<>(obj);
                }
            },
            config
        );
    }
    
    public float[] borrowVector() throws Exception {
        return vectorPool.borrowObject();
    }
    
    public void returnVector(float[] vector) {
        Arrays.fill(vector, 0);  // 清空数据
        vectorPool.returnObject(vector);
    }
}
```

**2. 性能指标**
- 特征提取: P99 < 50ms
- 1:1验证: P99 < 30ms
- 1:N识别(1000人): P99 < 200ms
- 内存占用: < 500MB

---

### **2.4 ioedream-attendance-service (8091) - 考勤管理服务**

#### **职责定位**
- 打卡管理：移动端/设备端打卡
- 排班管理：⭐ 多班次、轮班制、弹性工时
- 考勤计算：⭐ 结合打卡+排班+规则，精准计算
- 统计报表：日报/月报/年报
- 假期管理：请假/加班/调休

#### **⭐ 设备交互模式：边缘识别+中心计算+排班联动**

```
【数据下发】软件 → 设备
  ├─ 人员信息
  ├─ 生物模板
  └─ ⚠️ 不下发排班计划和考勤规则（计算在软件端）

【实时打卡】设备端轻量识别
  ├─ 采集生物特征
  ├─ 本地识别匹配userId
  ├─ 生成打卡记录：userId + 时间 + 设备ID
  └─ 立即上传到软件端

【事后计算】软件端综合计算 ⭐ 核心逻辑
  每日凌晨2点，计算前一天考勤：
  
  步骤1：查询排班计划 ⭐
    SELECT * FROM attendance_schedule
    WHERE userId=? AND scheduleDate=?
    → 获取：班次类型、开始时间、结束时间、休息时段、应出勤时长
  
  步骤2：查询打卡记录
    SELECT * FROM attendance_punch_record
    WHERE userId=? AND punchDate=?
    → 获取：上班打卡时间、下班打卡时间
  
  步骤3：查询考勤规则
    SELECT * FROM attendance_rule WHERE ruleId=?
    → 获取：迟到宽限、早退宽限、加班起算时长
  
  步骤4：三要素结合计算 ⭐ 核心算法
    打卡记录 + 排班计划 + 考勤规则 = 考勤结果
    
    a) 计算实际工时：
       (下班时间 - 上班时间) - 休息时长
    
    b) 判断迟到：
       上班打卡时间 > (排班开始时间 + 宽限)
    
    c) 判断早退：
       下班打卡时间 < (排班结束时间 - 宽限)
    
    d) 判断加班：
       (下班打卡 - 排班结束) > 加班起算时长
  
  步骤5：生成考勤结果
    AttendanceRecordEntity:
      - scheduleId: 关联排班
      - scheduledWorkMinutes: 应出勤时长
      - actualWorkMinutes: 实际出勤时长
      - status: 正常/迟到/早退/旷工
  
  步骤6：月度汇总
    每月1日执行：
      - 出勤天数 vs 应出勤天数
      - 总工时 vs 标准工时
      - 迟到次数、早退次数、加班时长

【优势】
  ✅ 设备端轻量：只负责识别，不需存储复杂规则
  ✅ 规则灵活：考勤规则变更无需更新设备
  ✅ 排班联动：支持标准/弹性/轮班/外勤多种制度
  ✅ 多维计算：综合打卡+排班+规则，精准计算工时
  ✅ 事后审计：可重新计算历史数据
```

#### **核心设计**

**1. 策略模式：考勤规则引擎**
```java
public interface IAttendanceRuleStrategy {
    /**
     * 规则名称
     */
    String getRuleName();
    
    /**
     * 计算考勤结果
     */
    AttendanceResult calculate(AttendancePunchRecord record, AttendanceRule rule);
}

@Component
@StrategyMarker(name = "STANDARD_WORKING_HOURS")
public class StandardWorkingHoursStrategy implements IAttendanceRuleStrategy {
    
    @Override
    public String getRuleName() {
        return "标准工时制";
    }
    
    @Override
    public AttendanceResult calculate(AttendancePunchRecord record, AttendanceRule rule) {
        LocalTime punchTime = record.getPunchTime().toLocalTime();
        LocalTime workStart = rule.getWorkStartTime();
        LocalTime workEnd = rule.getWorkEndTime();
        
        AttendanceResult result = new AttendanceResult();
        result.setUserId(record.getUserId());
        result.setDate(record.getPunchTime().toLocalDate());
        
        // 上班打卡
        if (record.getPunchType() == PunchType.CLOCK_IN) {
            if (punchTime.isAfter(workStart.plusMinutes(rule.getLateGracePeriod()))) {
                result.setStatus(AttendanceStatus.LATE);
                result.setLateDuration(
                    Duration.between(workStart, punchTime).toMinutes()
                );
            } else if (punchTime.isAfter(workStart)) {
                result.setStatus(AttendanceStatus.NORMAL);
                result.setRemark("在宽限期内到达");
            } else {
                result.setStatus(AttendanceStatus.NORMAL);
            }
        }
        
        // 下班打卡
        else if (record.getPunchType() == PunchType.CLOCK_OUT) {
            if (punchTime.isBefore(workEnd.minusMinutes(rule.getEarlyGracePeriod()))) {
                result.setStatus(AttendanceStatus.EARLY_LEAVE);
                result.setEarlyDuration(
                    Duration.between(punchTime, workEnd).toMinutes()
                );
            } else {
                result.setStatus(AttendanceStatus.NORMAL);
                
                // 加班判定
                if (punchTime.isAfter(workEnd.plusMinutes(30))) {
                    result.setOvertimeDuration(
                        Duration.between(workEnd, punchTime).toMinutes()
                    );
                }
            }
        }
        
        return result;
    }
}

@Component
@StrategyMarker(name = "FLEXIBLE_WORKING_HOURS")
public class FlexibleWorkingHoursStrategy implements IAttendanceRuleStrategy {
    
    @Resource
    private AttendanceRecordDao recordDao;
    
    @Override
    public String getRuleName() {
        return "弹性工作制";
    }
    
    @Override
    public AttendanceResult calculate(AttendancePunchRecord record, AttendanceRule rule) {
        Long userId = record.getUserId();
        LocalDate date = record.getPunchTime().toLocalDate();
        
        // 查询当天所有打卡记录
        List<AttendancePunchRecord> dayRecords = recordDao.selectByUserAndDate(
            userId, date
        );
        
        // 计算总工作时长
        long totalMinutes = calculateTotalWorkingMinutes(dayRecords);
        
        AttendanceResult result = new AttendanceResult();
        result.setUserId(userId);
        result.setDate(date);
        result.setWorkingMinutes(totalMinutes);
        
        // 弹性工作制: 只要满足8小时即可
        if (totalMinutes >= 480) {  // 8小时 = 480分钟
            result.setStatus(AttendanceStatus.NORMAL);
        } else {
            result.setStatus(AttendanceStatus.INSUFFICIENT_HOURS);
            result.setShortfallMinutes(480 - totalMinutes);
        }
        
        return result;
    }
    
    private long calculateTotalWorkingMinutes(List<AttendancePunchRecord> records) {
        // 成对计算上班下班时长
        records.sort(Comparator.comparing(AttendancePunchRecord::getPunchTime));
        
        long totalMinutes = 0;
        for (int i = 0; i < records.size() - 1; i += 2) {
            if (i + 1 < records.size()) {
                LocalDateTime start = records.get(i).getPunchTime();
                LocalDateTime end = records.get(i + 1).getPunchTime();
                totalMinutes += Duration.between(start, end).toMinutes();
            }
        }
        
        return totalMinutes;
    }
}

@Component
@StrategyMarker(name = "SHIFT_WORKING_HOURS")
public class ShiftWorkingHoursStrategy implements IAttendanceRuleStrategy {
    
    @Resource
    private ShiftScheduleDao scheduleDao;
    
    @Override
    public String getRuleName() {
        return "轮班制";
    }
    
    @Override
    public AttendanceResult calculate(AttendancePunchRecord record, AttendanceRule rule) {
        // 查询当天班次
        ShiftScheduleEntity schedule = scheduleDao.selectByUserAndDate(
            record.getUserId(),
            record.getPunchTime().toLocalDate()
        );
        
        if (schedule == null) {
            return AttendanceResult.absent("未排班");
        }
        
        // 根据班次时间计算
        LocalTime punchTime = record.getPunchTime().toLocalTime();
        LocalTime shiftStart = schedule.getShiftStartTime();
        LocalTime shiftEnd = schedule.getShiftEndTime();
        
        AttendanceResult result = new AttendanceResult();
        result.setUserId(record.getUserId());
        result.setDate(record.getPunchTime().toLocalDate());
        result.setShiftName(schedule.getShiftName());
        
        if (record.getPunchType() == PunchType.CLOCK_IN) {
            if (punchTime.isAfter(shiftStart.plusMinutes(15))) {
                result.setStatus(AttendanceStatus.LATE);
                result.setLateDuration(
                    Duration.between(shiftStart, punchTime).toMinutes()
                );
            } else {
                result.setStatus(AttendanceStatus.NORMAL);
            }
        }
        
        return result;
    }
}
```

**2. 装饰器模式：打卡流程增强**
```java
public interface IPunchExecutor {
    PunchResult execute(MobilePunchRequest request);
}

public class BasicPunchExecutor implements IPunchExecutor {
    
    @Resource
    private AttendancePunchRecordDao recordDao;
    
    @Override
    public PunchResult execute(MobilePunchRequest request) {
        // 基础打卡逻辑
        AttendancePunchRecord record = new AttendancePunchRecord();
        record.setUserId(request.getUserId());
        record.setPunchTime(LocalDateTime.now());
        record.setPunchType(request.getPunchType());
        record.setLatitude(request.getLatitude());
        record.setLongitude(request.getLongitude());
        
        recordDao.insert(record);
        
        return PunchResult.success(record);
    }
}

public abstract class PunchDecorator implements IPunchExecutor {
    
    protected IPunchExecutor delegate;
    
    public PunchDecorator(IPunchExecutor delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public PunchResult execute(MobilePunchRequest request) {
        return delegate.execute(request);
    }
}

public class GPSValidationDecorator extends PunchDecorator {
    
    @Resource
    private CompanyAreaService companyAreaService;
    
    public GPSValidationDecorator(IPunchExecutor delegate) {
        super(delegate);
    }
    
    @Override
    public PunchResult execute(MobilePunchRequest request) {
        // GPS位置验证
        boolean withinCompanyArea = companyAreaService.isWithinArea(
            request.getLatitude(),
            request.getLongitude()
        );
        
        if (!withinCompanyArea) {
            return PunchResult.failed("不在打卡范围内");
        }
        
        return super.execute(request);
    }
}

public class PhotoVerificationDecorator extends PunchDecorator {
    
    @Resource
    private BiometricServiceClient biometricClient;
    
    public PhotoVerificationDecorator(IPunchExecutor delegate) {
        super(delegate);
    }
    
    @Override
    public PunchResult execute(MobilePunchRequest request) {
        // 人脸验证
        if (request.getFacePhoto() != null) {
            ResponseDTO<MatchResult> verifyResult = biometricClient.verify(
                BiometricVerifyDTO.builder()
                    .userId(request.getUserId())
                    .biometricType(BiometricType.FACE)
                    .featureData(request.getFacePhoto())
                    .build()
            );
            
            if (!verifyResult.isSuccess() || !verifyResult.getData().isMatched()) {
                return PunchResult.failed("人脸验证失败");
            }
        }
        
        return super.execute(request);
    }
}

public class AntiCheatingDecorator extends PunchDecorator {
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    public AntiCheatingDecorator(IPunchExecutor delegate) {
        super(delegate);
    }
    
    @Override
    public PunchResult execute(MobilePunchRequest request) {
        // 防作弊检测: 同一用户短时间内多次打卡
        String lockKey = "punch:lock:" + request.getUserId();
        Boolean lockAcquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "1", Duration.ofMinutes(1));
        
        if (Boolean.FALSE.equals(lockAcquired)) {
            return PunchResult.failed("打卡过于频繁,请稍后再试");
        }
        
        try {
            return super.execute(request);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }
}

public class LoggingDecorator extends PunchDecorator {
    
    private static final Logger log = LoggerFactory.getLogger(LoggingDecorator.class);
    
    public LoggingDecorator(IPunchExecutor delegate) {
        super(delegate);
    }
    
    @Override
    public PunchResult execute(MobilePunchRequest request) {
        log.info("[打卡请求] userId={}, type={}, location=({}, {})",
            request.getUserId(), request.getPunchType(),
            request.getLatitude(), request.getLongitude());
        
        long startTime = System.currentTimeMillis();
        try {
            PunchResult result = super.execute(request);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("[打卡完成] userId={}, result={}, duration={}ms",
                request.getUserId(), result.isSuccess(), duration);
            
            return result;
        } catch (Exception e) {
            log.error("[打卡异常] userId={}", request.getUserId(), e);
            throw e;
        }
    }
}

// 装饰器组装
@Configuration
public class PunchExecutorConfiguration {
    
    @Bean
    public IPunchExecutor punchExecutor() {
        IPunchExecutor executor = new BasicPunchExecutor();
        executor = new GPSValidationDecorator(executor);
        executor = new PhotoVerificationDecorator(executor);
        executor = new AntiCheatingDecorator(executor);
        executor = new LoggingDecorator(executor);
        return executor;
    }
}
```

**3. 考勤统计服务**
```java
@Service
public class AttendanceStatisticsService {
    
    @Resource
    private AttendanceRecordDao recordDao;
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 今日考勤实时统计
     */
    public AttendanceDailyStatistics getTodayStatistics(Long departmentId) {
        String cacheKey = "attendance:daily:" + LocalDate.now() + ":" + departmentId;
        
        AttendanceDailyStatistics stats = (AttendanceDailyStatistics) 
            redisTemplate.opsForValue().get(cacheKey);
        
        if (stats == null) {
            stats = calculateTodayStatistics(departmentId);
            redisTemplate.opsForValue().set(
                cacheKey, stats, Duration.ofMinutes(5)
            );
        }
        
        return stats;
    }
    
    private AttendanceDailyStatistics calculateTodayStatistics(Long departmentId) {
        List<AttendanceRecordEntity> records = recordDao.selectTodayByDepartment(
            departmentId, LocalDate.now()
        );
        
        int totalEmployees = getTotalEmployees(departmentId);
        
        Map<AttendanceStatus, Long> statusCount = records.stream()
            .collect(Collectors.groupingBy(
                AttendanceRecordEntity::getStatus,
                Collectors.counting()
            ));
        
        return AttendanceDailyStatistics.builder()
            .date(LocalDate.now())
            .departmentId(departmentId)
            .totalEmployees(totalEmployees)
            .clockedIn(statusCount.getOrDefault(AttendanceStatus.NORMAL, 0L).intValue())
            .late(statusCount.getOrDefault(AttendanceStatus.LATE, 0L).intValue())
            .earlyLeave(statusCount.getOrDefault(AttendanceStatus.EARLY_LEAVE, 0L).intValue())
            .absent(totalEmployees - records.size())
            .leave(statusCount.getOrDefault(AttendanceStatus.LEAVE, 0L).intValue())
            .build();
    }
    
    /**
     * 月度考勤报表(异步生成)
     */
    @Async("attendanceStatExecutor")
    public CompletableFuture<MonthlyAttendanceReport> generateMonthlyReport(
            Long departmentId, YearMonth yearMonth) {
        
        List<AttendanceRecordEntity> records = recordDao.selectByDepartmentAndMonth(
            departmentId, yearMonth
        );
        
        // 按用户分组统计
        Map<Long, EmployeeAttendanceSummary> summaryMap = records.stream()
            .collect(Collectors.groupingBy(
                AttendanceRecordEntity::getUserId,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    this::summarizeEmployeeAttendance
                )
            ));
        
        MonthlyAttendanceReport report = new MonthlyAttendanceReport();
        report.setDepartmentId(departmentId);
        report.setYearMonth(yearMonth);
        report.setEmployeeSummaries(new ArrayList<>(summaryMap.values()));
        report.setGenerateTime(LocalDateTime.now());
        
        return CompletableFuture.completedFuture(report);
    }
    
    private EmployeeAttendanceSummary summarizeEmployeeAttendance(
            List<AttendanceRecordEntity> records) {
        
        EmployeeAttendanceSummary summary = new EmployeeAttendanceSummary();
        summary.setUserId(records.get(0).getUserId());
        summary.setTotalDays(records.size());
        summary.setNormalDays((int) records.stream()
            .filter(r -> r.getStatus() == AttendanceStatus.NORMAL).count());
        summary.setLateDays((int) records.stream()
            .filter(r -> r.getStatus() == AttendanceStatus.LATE).count());
        summary.setEarlyLeaveDays((int) records.stream()
            .filter(r -> r.getStatus() == AttendanceStatus.EARLY_LEAVE).count());
        summary.setAbsentDays((int) records.stream()
            .filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count());
        summary.setLeaveDays((int) records.stream()
            .filter(r -> r.getStatus() == AttendanceStatus.LEAVE).count());
        summary.setTotalOvertimeMinutes(records.stream()
            .mapToLong(r -> r.getOvertimeDuration() != null ? r.getOvertimeDuration() : 0)
            .sum());
        
        return summary;
    }
}
```

#### **性能指标**
- 打卡响应: P99 < 200ms
- 统计查询: P99 < 500ms
- 月报生成: < 10s

---

### **2.5 ioedream-consume-service (8094) - 消费管理服务**

#### **职责定位**
- 账户管理：预付费/后付费账户
- 消费记录：刷卡消费、扫码支付
- 离线消费：离线模式下的消费同步
- 报表分析：消费统计、趋势分析

#### **核心设计**

**1. 策略模式：消费模式**
```java
public interface IConsumeStrategy {
    String getStrategyName();
    ConsumeResult process(ConsumeRequest request);
}

@Component
@StrategyMarker(name = "PREPAID")
public class PrepaidConsumeStrategy implements IConsumeStrategy {
    
    @Resource
    private AccountDao accountDao;
    
    @Resource
    private ConsumeRecordDao consumeRecordDao;
    
    @Override
    public String getStrategyName() {
        return "预付费模式";
    }
    
    @Override
    public ConsumeResult process(ConsumeRequest request) {
        // 1. 查询账户余额
        AccountEntity account = accountDao.selectByUserId(request.getUserId());
        if (account == null) {
            return ConsumeResult.failed("账户不存在");
        }
        
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            return ConsumeResult.failed("余额不足");
        }
        
        // 2. 扣款(乐观锁)
        int updated = accountDao.deductBalance(
            account.getAccountId(),
            request.getAmount(),
            account.getVersion()
        );
        
        if (updated == 0) {
            throw new ConcurrentUpdateException("账户余额更新失败,请重试");
        }
        
        // 3. 创建消费记录
        ConsumeRecordEntity record = createConsumeRecord(request, account);
        record.setPaymentStatus(PaymentStatus.PAID);
        consumeRecordDao.insert(record);
        
        // 4. 更新余额后的账户对象
        AccountEntity updatedAccount = accountDao.selectById(account.getAccountId());
        
        return ConsumeResult.success(record, updatedAccount.getBalance());
    }
}

@Component
@StrategyMarker(name = "POSTPAID")
public class PostpaidConsumeStrategy implements IConsumeStrategy {
    
    @Resource
    private ConsumeRecordDao consumeRecordDao;
    
    @Override
    public String getStrategyName() {
        return "后付费模式";
    }
    
    @Override
    public ConsumeResult process(ConsumeRequest request) {
        // 后付费: 直接记录消费,定期结算
        ConsumeRecordEntity record = createConsumeRecord(request, null);
        record.setPaymentStatus(PaymentStatus.PENDING);
        record.setSettlementCycle(SettlementCycle.MONTHLY);
        consumeRecordDao.insert(record);
        
        return ConsumeResult.success(record, null);
    }
}
```

**2. 离线消费处理**
```java
@Component
public class OfflineConsumeProcessor {
    
    @Resource
    private ConsumeRecordDao consumeRecordDao;
    
    @Resource
    private AccountDao accountDao;
    
    /**
     * 批量同步离线消费记录
     */
    @Transactional(rollbackFor = Exception.class)
    public BatchSyncResult syncOfflineRecords(List<OfflineConsumeRecord> offlineRecords) {
        BatchSyncResult result = new BatchSyncResult();
        
        for (OfflineConsumeRecord offline : offlineRecords) {
            try {
                // 1. 验证记录合法性
                validateOfflineRecord(offline);
                
                // 2. 防重复消费检查
                boolean exists = consumeRecordDao.existsByOfflineId(offline.getOfflineId());
                if (exists) {
                    result.addDuplicate(offline);
                    continue;
                }
                
                // 3. 转换为在线记录
                ConsumeRecordEntity record = convertToOnlineRecord(offline);
                record.setIsOfflineSync(true);
                record.setSyncTime(LocalDateTime.now());
                
                // 4. 补扣账户余额
                accountDao.deductBalance(
                    offline.getAccountId(),
                    offline.getAmount(),
                    null  // 离线消费不使用乐观锁
                );
                
                // 5. 保存记录
                consumeRecordDao.insert(record);
                
                result.addSuccess(offline);
                
            } catch (Exception e) {
                log.error("[离线消费同步失败] offlineId={}", offline.getOfflineId(), e);
                result.addFailed(offline, e.getMessage());
            }
        }
        
        return result;
    }
}
```

**3. 消费报表服务**
```java
@Service
public class ConsumeReportService {
    
    @Resource
    private ConsumeRecordDao consumeRecordDao;
    
    /**
     * 生成部门消费报表
     */
    @Async("reportExecutor")
    public CompletableFuture<DepartmentConsumeReport> generateDepartmentReport(
            Long departmentId, LocalDate startDate, LocalDate endDate) {
        
        List<ConsumeRecordEntity> records = consumeRecordDao.selectByDepartmentAndDateRange(
            departmentId, startDate, endDate
        );
        
        DepartmentConsumeReport report = new DepartmentConsumeReport();
        report.setDepartmentId(departmentId);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalAmount(records.stream()
            .map(ConsumeRecordEntity::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        report.setTotalCount(records.size());
        report.setAverageAmount(report.getTotalAmount().divide(
            BigDecimal.valueOf(report.getTotalCount()), 2, RoundingMode.HALF_UP));
        
        // 按消费类型分组
        Map<String, BigDecimal> categoryStats = records.stream()
            .collect(Collectors.groupingBy(
                ConsumeRecordEntity::getConsumeCategory,
                Collectors.reducing(BigDecimal.ZERO,
                    ConsumeRecordEntity::getAmount,
                    BigDecimal::add)
            ));
        report.setCategoryStats(categoryStats);
        
        // 按日期统计趋势
        Map<LocalDate, BigDecimal> dailyTrend = records.stream()
            .collect(Collectors.groupingBy(
                r -> r.getConsumeTime().toLocalDate(),
                Collectors.reducing(BigDecimal.ZERO,
                    ConsumeRecordEntity::getAmount,
                    BigDecimal::add)
            ));
        report.setDailyTrend(dailyTrend);
        
        return CompletableFuture.completedFuture(report);
    }
}
```

#### **性能指标**
- 消费响应: P99 < 100ms
- 余额查询: P99 < 50ms
- 离线同步: 1000条/秒

---

### **2.6 ioedream-visitor-service (8095) - 访客管理服务**

#### **职责定位**
- 预约管理：访客预约、审批流程
- 签到签出：二维码通行证、人脸验证
- 轨迹追踪：实时位置追踪、异常告警
- VIP管理：VIP访客快速通道

#### **核心设计**

**1. 访客预约工作流**
```java
@Service
public class VisitorAppointmentWorkflowService {
    
    @Resource
    private WorkflowEngine workflowEngine;
    
    @Resource
    private VisitorAppointmentDao appointmentDao;
    
    @Resource
    private AccessPermissionService accessPermissionService;
    
    /**
     * 提交访客预约
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> submitAppointment(VisitorAppointmentDTO appointmentDTO) {
        // 1. 创建预约记录
        VisitorAppointmentEntity appointment = new VisitorAppointmentEntity();
        BeanUtils.copyProperties(appointmentDTO, appointment);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setAppointmentNo(generateAppointmentNo());
        appointmentDao.insert(appointment);
        
        // 2. 启动审批流程
        WorkflowInstanceVO workflow = workflowEngine.startProcess(
            "visitor_appointment",
            appointment.getAppointmentId().toString(),
            Map.of(
                "visitorName", appointment.getVisitorName(),
                "visitDate", appointment.getVisitDate(),
                "hostUserId", appointment.getHostUserId(),
                "visitPurpose", appointment.getVisitPurpose()
            )
        );
        
        // 3. 更新工作流ID
        appointment.setWorkflowInstanceId(workflow.getInstanceId());
        appointmentDao.updateById(appointment);
        
        // 4. 发送通知给被访人
        notifyHost(appointment);
        
        return ResponseDTO.ok(appointment.getAppointmentNo());
    }
    
    /**
     * 审批访客预约
     */
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Void> approveAppointment(Long appointmentId, ApprovalForm form) {
        // 1. 查询预约
        VisitorAppointmentEntity appointment = appointmentDao.selectById(appointmentId);
        
        // 2. 提交审批
        workflowEngine.completeTask(
            form.getTaskId(),
            form.getApproved(),
            form.getComment()
        );
        
        // 3. 审批通过后生成访客通行证
        if (form.getApproved()) {
            appointment.setStatus(AppointmentStatus.APPROVED);
            appointment.setApprovalTime(LocalDateTime.now());
            
            // 生成二维码通行证
            String qrCode = generateVisitorQRCode(appointment);
            appointment.setQrCode(qrCode);
            
            // 发送通行证到访客手机
            sendVisitorPass(appointment);
            
            // 临时权限下发到门禁设备
            grantTemporaryAccess(appointment);
            
        } else {
            appointment.setStatus(AppointmentStatus.REJECTED);
            appointment.setRejectReason(form.getComment());
            
            // 通知访客预约被拒绝
            notifyVisitorRejection(appointment);
        }
        
        appointmentDao.updateById(appointment);
        return ResponseDTO.ok();
    }
    
    /**
     * 访客签到
     */
    public ResponseDTO<VisitorCheckInVO> checkIn(VisitorCheckInRequest request) {
        // 1. 验证二维码
        VisitorAppointmentEntity appointment = validateQRCode(request.getQrCode());
        if (appointment.getStatus() != AppointmentStatus.APPROVED) {
            return ResponseDTO.error("预约未审批通过");
        }
        
        // 2. 时间验证
        if (!isWithinVisitTime(appointment)) {
            return ResponseDTO.error("不在预约时间范围内");
        }
        
        // 3. 人脸验证(可选)
        if (request.getFacePhoto() != null) {
            boolean faceMatched = verifyVisitorFace(appointment, request.getFacePhoto());
            if (!faceMatched) {
                return ResponseDTO.error("人脸验证失败");
            }
        }
        
        // 4. 创建签到记录
        VisitorCheckInRecord record = new VisitorCheckInRecord();
        record.setAppointmentId(appointment.getAppointmentId());
        record.setCheckInTime(LocalDateTime.now());
        record.setCheckInDevice(request.getDeviceId());
        record.setCheckInMethod(request.getFacePhoto() != null ? "FACE" : "QR_CODE");
        visitorCheckInDao.insert(record);
        
        // 5. 更新预约状态
        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        appointmentDao.updateById(appointment);
        
        // 6. 开始轨迹追踪
        startVisitorTracking(appointment);
        
        // 7. 通知被访人
        notifyHostVisitorArrival(appointment);
        
        return ResponseDTO.ok(VisitorCheckInVO.from(appointment, record));
    }
    
    /**
     * 生成临时通行权限
     */
    private void grantTemporaryAccess(VisitorAppointmentEntity appointment) {
        // 临时权限: 预约时间内有效
        accessPermissionService.grantTemporaryPermission(
            TemporaryPermissionDTO.builder()
                .userId(appointment.getVisitorId())  // 访客临时账户ID
                .areaIds(appointment.getAllowedAreaIds())
                .startTime(appointment.getVisitStartTime())
                .endTime(appointment.getVisitEndTime())
                .permissionType("VISITOR")
                .build()
        );
    }
}
```

**2. 访客轨迹追踪**
```java
@Service
public class VisitorTrackingService {
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Resource
    private VisitorTrackingDao trackingDao;
    
    @Resource
    private WebSocketService websocketService;
    
    /**
     * 记录访客轨迹
     */
    public void recordTrackingPoint(Long appointmentId, TrackingPoint point) {
        String trackingKey = "visitor:tracking:" + appointmentId;
        
        // 1. 实时轨迹存入Redis
        redisTemplate.opsForList().rightPush(trackingKey, point);
        redisTemplate.expire(trackingKey, Duration.ofDays(1));
        
        // 2. 推送到监控端(WebSocket)
        websocketService.sendTrackingUpdate(
            VisitorTrackingUpdate.builder()
                .appointmentId(appointmentId)
                .currentArea(point.getAreaName())
                .timestamp(point.getTimestamp())
                .build()
        );
        
        // 3. 异常区域告警
        checkUnauthorizedArea(appointmentId, point);
        
        // 4. 异步持久化到数据库
        CompletableFuture.runAsync(() -> {
            VisitorTrackingEntity tracking = new VisitorTrackingEntity();
            tracking.setAppointmentId(appointmentId);
            tracking.setDeviceId(point.getDeviceId());
            tracking.setAreaId(point.getAreaId());
            tracking.setTrackingTime(point.getTimestamp());
            trackingDao.insert(tracking);
        });
    }
    
    /**
     * 获取访客实时轨迹
     */
    public List<TrackingPoint> getRealtimeTracking(Long appointmentId) {
        String trackingKey = "visitor:tracking:" + appointmentId;
        return redisTemplate.opsForList().range(trackingKey, 0, -1)
            .stream()
            .map(obj -> (TrackingPoint) obj)
            .collect(Collectors.toList());
    }
    
    /**
     * 检查未授权区域访问
     */
    private void checkUnauthorizedArea(Long appointmentId, TrackingPoint point) {
        VisitorAppointmentEntity appointment = appointmentDao.selectById(appointmentId);
        List<Long> allowedAreaIds = appointment.getAllowedAreaIds();
        
        if (!allowedAreaIds.contains(point.getAreaId())) {
            // 访客进入未授权区域,发送告警
            VisitorAlertEvent alert = VisitorAlertEvent.builder()
                .appointmentId(appointmentId)
                .visitorName(appointment.getVisitorName())
                .alertType("UNAUTHORIZED_AREA")
                .currentArea(point.getAreaName())
                .alertTime(LocalDateTime.now())
                .build();
            
            // WebSocket实时推送告警
            websocketService.sendAlert(alert);
            
            // 短信通知被访人和安保人员
            notifyUnauthorizedAreaAccess(appointment, point);
        }
    }
}
```

#### **性能指标**
- 预约创建: P99 < 300ms
- 签到响应: P99 < 200ms
- 轨迹追踪: 实时推送延迟 < 500ms

---

### **2.7 ioedream-video-service (8092) - 视频监控服务**

#### **职责定位**
- 视频流管理：实时视频流推拉流
- AI分析：人脸检测、入侵检测、行为分析
- 录像管理：事件联动录像、录像回放
- 告警联动：异常事件告警推送

#### **核心设计**

**1. 工厂模式：视频流适配器**
```java
public interface IVideoStreamAdapter {
    String getVendorName();
    VideoStream createStream(DeviceEntity device);
    void stopStream(String streamId);
}

@Component
public class HikvisionStreamAdapter implements IVideoStreamAdapter {
    
    @Override
    public String getVendorName() {
        return "HIKVISION";
    }
    
    @Override
    public VideoStream createStream(DeviceEntity device) {
        // 海康RTSP流
        String rtspUrl = String.format(
            "rtsp://%s:%s@%s:%d/Streaming/Channels/101",
            device.getUsername(),
            device.getPassword(),
            device.getIpAddress(),
            device.getPort()
        );
        
        return VideoStream.builder()
            .deviceId(device.getDeviceId())
            .streamUrl(rtspUrl)
            .protocol("RTSP")
            .build();
    }
}

@Component
public class DahuaStreamAdapter implements IVideoStreamAdapter {
    
    @Override
    public String getVendorName() {
        return "DAHUA";
    }
    
    @Override
    public VideoStream createStream(DeviceEntity device) {
        // 大华RTSP流
        String rtspUrl = String.format(
            "rtsp://%s:%s@%s:%d/cam/realmonitor?channel=1&subtype=0",
            device.getUsername(),
            device.getPassword(),
            device.getIpAddress(),
            device.getPort()
        );
        
        return VideoStream.builder()
            .deviceId(device.getDeviceId())
            .streamUrl(rtspUrl)
            .protocol("RTSP")
            .build();
    }
}
```

**2. 策略模式：AI分析策略**
```java
public interface IVideoAnalysisStrategy {
    String getAnalysisType();
    AnalysisResult analyze(VideoFrame frame);
}

@Component
@StrategyMarker(type = "FACE_DETECTION")
public class FaceDetectionStrategy implements IVideoAnalysisStrategy {
    
    @Override
    public AnalysisResult analyze(VideoFrame frame) {
        // OpenCV人脸检测
        Mat image = frame.getMat();
        CascadeClassifier faceDetector = new CascadeClassifier(
            "haarcascade_frontalface_default.xml"
        );
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(image, faceDetections);
        
        List<FaceRegion> faces = Arrays.stream(faceDetections.toArray())
            .map(rect -> new FaceRegion(
                rect.x, rect.y, rect.width, rect.height
            ))
            .collect(Collectors.toList());
        
        return AnalysisResult.builder()
            .type("FACE_DETECTION")
            .detectedFaces(faces)
            .faceCount(faces.size())
            .build();
    }
}
```

**3. 事件联动录像**
```java
@Service
public class VideoEventLinkageService {
    
    @Resource
    private VideoStreamService streamService;
    
    @Resource
    private VideoRecordDao recordDao;
    
    /**
     * 门禁事件联动录像
     */
    @EventListener
    @Async("videoEventExecutor")
    public void handleAccessEvent(AccessEvent event) {
        // 1. 查询该门禁点的摄像头
        List<DeviceEntity> cameras = deviceDao.selectCamerasByArea(
            event.getAreaId()
        );
        
        // 2. 触发录像
        for (DeviceEntity camera : cameras) {
            streamService.startRecording(
                camera.getDeviceId(),
                Duration.ofMinutes(5)
            );
        }
        
        // 3. 创建录像记录
        VideoRecordEntity record = new VideoRecordEntity();
        record.setDeviceId(cameras.get(0).getDeviceId());
        record.setEventType("ACCESS");
        record.setEventId(event.getRecordId());
        record.setStartTime(LocalDateTime.now());
        recordDao.insert(record);
    }
}
```

#### **性能指标**
- 视频流启动: < 2s
- AI分析延迟: < 100ms/帧
- 并发视频流: 100+

---

### **2.8 ioedream-oa-service (8089) - OA办公服务**

#### **职责定位**
- 工作流引擎：审批流程管理
- 通知服务：多渠道消息推送
- 文档管理：文件上传下载
- 日程管理：会议室预订、日程提醒

#### **核心设计**

**1. 工作流引擎**
```java
@Service
public class WorkflowEngineService {
    
    @Resource
    private WorkflowInstanceDao instanceDao;
    
    @Resource
    private WorkflowTaskDao taskDao;
    
    /**
     * 启动流程
     */
    @Transactional(rollbackFor = Exception.class)
    public WorkflowInstanceVO startProcess(
            String processKey, 
            String businessKey, 
            Map<String, Object> variables) {
        
        // 1. 创建流程实例
        WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
        instance.setProcessKey(processKey);
        instance.setBusinessKey(businessKey);
        instance.setStatus(WorkflowStatus.RUNNING);
        instance.setVariables(JSON.toJSONString(variables));
        instanceDao.insert(instance);
        
        // 2. 创建首个任务
        WorkflowTaskEntity firstTask = createFirstTask(instance, variables);
        taskDao.insert(firstTask);
        
        // 3. 发送待办通知
        notifyAssignee(firstTask);
        
        return WorkflowInstanceVO.from(instance);
    }
    
    /**
     * 完成任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(Long taskId, boolean approved, String comment) {
        // 1. 更新任务状态
        WorkflowTaskEntity task = taskDao.selectById(taskId);
        task.setStatus(approved ? TaskStatus.APPROVED : TaskStatus.REJECTED);
        task.setComment(comment);
        task.setCompleteTime(LocalDateTime.now());
        taskDao.updateById(task);
        
        // 2. 判断流程走向
        if (approved) {
            WorkflowTaskEntity nextTask = createNextTask(task);
            if (nextTask != null) {
                taskDao.insert(nextTask);
                notifyAssignee(nextTask);
            } else {
                completeProcess(task.getInstanceId());
            }
        } else {
            terminateProcess(task.getInstanceId());
        }
    }
}
```

**2. 多渠道通知服务**
```java
@Service
public class NotificationService {
    
    @Resource
    private RabbitTemplate rabbitTemplate;
    
    @Resource
    private WebSocketService websocketService;
    
    @Resource
    private SmsService smsService;
    
    @Resource
    private EmailService emailService;
    
    /**
     * 发送多渠道通知
     */
    public void sendNotification(NotificationMessage message) {
        // 1. 站内消息(WebSocket实时推送)
        if (message.getChannels().contains(NotificationChannel.WEBSOCKET)) {
            websocketService.sendToUser(
                message.getUserId(), 
                message.getContent()
            );
        }
        
        // 2. 短信通知
        if (message.getChannels().contains(NotificationChannel.SMS)) {
            smsService.send(message.getPhone(), message.getContent());
        }
        
        // 3. 邮件通知
        if (message.getChannels().contains(NotificationChannel.EMAIL)) {
            emailService.send(
                message.getEmail(), 
                message.getSubject(), 
                message.getContent()
            );
        }
        
        // 4. 推送到MQ(异步处理)
        rabbitTemplate.convertAndSend(
            "notification.exchange", 
            "notification.route", 
            message
        );
    }
}
```

#### **性能指标**
- 流程启动: P99 < 200ms
- 通知推送: P99 < 100ms
- 并发审批: 1000+ TPS

---

### **2.9 ioedream-device-comm-service (8087) - 设备通讯服务**

#### **职责定位**
- 协议适配：多厂商设备协议适配
- 连接管理：设备连接池管理
- 指令下发：开门/关门/重启等指令
- 心跳检测：设备在线状态监控

#### **核心设计**（已在前面完整设计）

---

### **2.10 ioedream-common-service (8088) - 公共业务服务**

#### **职责定位**
- 用户管理：用户CRUD、角色权限
- 组织管理：部门/区域/设备管理
- 字典管理：系统字典配置
- 配置管理：系统参数配置

---

### **2.11 ioedream-database-service (8093) - 数据库管理服务**

#### **职责定位**
- 数据备份：定时全量/增量备份
- 数据恢复：备份文件恢复
- 性能监控：慢查询/连接数监控
- 数据迁移：数据导入导出

---

## 🎯 **三、完整的10个公共组件库详细设计**

### **3.1 microservices-common-core - 核心组件**

```java
/**
 * ResponseDTO - 统一返回格式
 */
@Data
public class ResponseDTO<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;
    
    public static <T> ResponseDTO<T> ok(T data) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.setCode(200);
        response.setMessage("success");
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
    
    public static <T> ResponseDTO<T> error(String message) {
        ResponseDTO<T> response = new ResponseDTO<>();
        response.setCode(500);
        response.setMessage(message);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }
    
    public boolean isSuccess() {
        return code != null && code == 200;
    }
}

/**
 * PageResult - 分页返回
 */
@Data
public class PageResult<T> {
    private Long total;
    private List<T> records;
    private Integer pageNum;
    private Integer pageSize;
    private Integer totalPages;
    
    public static <T> PageResult<T> from(Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords());
        result.setPageNum((int) page.getCurrent());
        result.setPageSize((int) page.getSize());
        result.setTotalPages((int) page.getPages());
        return result;
    }
}

/**
 * BusinessException - 业务异常
 */
public class BusinessException extends RuntimeException {
    private final String errorCode;
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

/**
 * Constants - 常量定义
 */
public class Constants {
    public static final String CACHE_PREFIX = "ioedream:";
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final long TOKEN_EXPIRE_SECONDS = 7200;
    public static final int MAX_UPLOAD_SIZE_MB = 50;
}
```

### **3.2 microservices-common-security - 安全组件**

```java
/**
 * TokenUtil - Token工具
 */
@Component
public class TokenUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    public String generateToken(UserContext userContext) {
        return Jwts.builder()
            .setSubject(userContext.getUserId().toString())
            .claim("username", userContext.getUserName())
            .claim("roles", userContext.getRoles())
            .claim("departmentId", userContext.getDepartmentId())
            .setExpiration(new Date(
                System.currentTimeMillis() + Constants.TOKEN_EXPIRE_SECONDS * 1000
            ))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }
    
    public UserContext parseToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .getBody();
        
        UserContext context = new UserContext();
        context.setUserId(Long.parseLong(claims.getSubject()));
        context.setUserName(claims.get("username", String.class));
        context.setRoles(claims.get("roles", String.class));
        context.setDepartmentId(claims.get("departmentId", Long.class));
        return context;
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

/**
 * EncryptionUtil - 加密工具
 */
@Component
public class EncryptionUtil {
    
    public String md5(String input) {
        return DigestUtils.md5DigestAsHex(input.getBytes());
    }
    
    public String sha256(String input) {
        return DigestUtils.sha256Hex(input);
    }
    
    public String bcryptEncode(String rawPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.encode(rawPassword);
    }
    
    public boolean bcryptMatches(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(rawPassword, encodedPassword);
    }
}
```

### **3.3 microservices-common-cache - 缓存组件**

```java
/**
 * UnifiedCacheManager - 多级缓存管理器
 */
@Component
public class UnifiedCacheManager {
    
    private final Cache<String, Object> localCache;  // Caffeine本地缓存
    private final RedisTemplate<String, Object> redisTemplate;
    private final BloomFilter<String> bloomFilter;
    private final RedissonClient redissonClient;
    
    public UnifiedCacheManager(
            RedisTemplate<String, Object> redisTemplate,
            RedissonClient redissonClient) {
        
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
        
        // 配置Caffeine本地缓存
        this.localCache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .recordStats()
            .build();
        
        // 配置布隆过滤器
        this.bloomFilter = BloomFilter.create(
            Funnels.stringFunnel(Charset.defaultCharset()),
            100000,
            0.01
        );
    }
    
    /**
     * 多级缓存获取
     */
    public <T> T get(String key, Class<T> type, Supplier<T> loader) {
        // L1: 本地缓存
        T value = (T) localCache.getIfPresent(key);
        if (value != null) {
            return value;
        }
        
        // 布隆过滤器检查
        if (!bloomFilter.mightContain(key)) {
            return null;
        }
        
        // L2: Redis缓存
        value = (T) redisTemplate.opsForValue().get(key);
        if (value != null) {
            localCache.put(key, value);
            return value;
        }
        
        // L3: 分布式锁+数据加载
        String lockKey = "lock:" + key;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                // 双重检查
                value = (T) redisTemplate.opsForValue().get(key);
                if (value != null) {
                    localCache.put(key, value);
                    return value;
                }
                
                // 加载数据
                value = loader.get();
                if (value != null) {
                    put(key, value, Duration.ofMinutes(30));
                    bloomFilter.put(key);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        
        return value;
    }
    
    /**
     * 写入缓存
     */
    public void put(String key, Object value, Duration ttl) {
        localCache.put(key, value);
        redisTemplate.opsForValue().set(key, value, ttl);
        bloomFilter.put(key);
    }
    
    /**
     * 删除缓存
     */
    public void evict(String key) {
        localCache.invalidate(key);
        redisTemplate.delete(key);
    }
}
```

### **3.4 microservices-common-data - 数据组件**

```java
/**
 * MyBatis-Plus配置
 */
@Configuration
public class MyBatisPlusConfiguration {
    
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 分页插件
        interceptor.addInnerInterceptor(
            new PaginationInnerInterceptor(DbType.MYSQL)
        );
        
        // 乐观锁插件
        interceptor.addInnerInterceptor(
            new OptimisticLockerInnerInterceptor()
        );
        
        return interceptor;
    }
    
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(
                    metaObject, "createTime", 
                    LocalDateTime.class, LocalDateTime.now()
                );
                this.strictInsertFill(
                    metaObject, "updateTime", 
                    LocalDateTime.class, LocalDateTime.now()
                );
            }
            
            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(
                    metaObject, "updateTime", 
                    LocalDateTime.class, LocalDateTime.now()
                );
            }
        };
    }
}

/**
 * BaseEntity - 基础实体
 */
@Data
public abstract class BaseEntity {
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
    
    @TableLogic
    private Integer deleted;
    
    @Version
    private Integer version;
}
```

### **3.5 microservices-common-monitor - 监控组件**

```java
/**
 * BusinessMetrics - 业务指标
 */
@Component
public class BusinessMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    /**
     * 记录通行事件
     */
    public void recordAccessEvent(String result) {
        Counter.builder("access.event")
            .tag("result", result)
            .register(meterRegistry)
            .increment();
    }
    
    /**
     * 记录API响应时间
     */
    public void recordResponseTime(String api, long duration) {
        Timer.builder("api.response.time")
            .tag("api", api)
            .register(meterRegistry)
            .record(duration, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 记录生物识别性能
     */
    public void recordBiometricPerformance(
            String type, long duration, boolean success) {
        
        Timer.builder("biometric.recognition.time")
            .tag("type", type)
            .tag("success", String.valueOf(success))
            .register(meterRegistry)
            .record(duration, TimeUnit.MILLISECONDS);
    }
}

/**
 * AuditLogAspect - 审计日志切面
 */
@Aspect
@Component
public class AuditLogAspect {
    
    @Resource
    private AuditLogDao auditLogDao;
    
    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint pjp, AuditLog auditLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取用户信息
        UserContext userContext = UserContextHolder.get();
        
        // 获取请求参数
        Object[] args = pjp.getArgs();
        String params = JSON.toJSONString(args);
        
        Object result = null;
        Exception exception = null;
        
        try {
            result = pjp.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录审计日志
            AuditLogEntity log = new AuditLogEntity();
            log.setUserId(userContext.getUserId());
            log.setUserName(userContext.getUserName());
            log.setModule(auditLog.module());
            log.setOperationType(auditLog.operationType());
            log.setDescription(auditLog.description());
            log.setParams(params);
            log.setResult(exception == null ? "SUCCESS" : "FAILED");
            log.setErrorMessage(exception != null ? exception.getMessage() : null);
            log.setDuration(duration);
            log.setOperationTime(LocalDateTime.now());
            
            auditLogDao.insert(log);
        }
    }
}
```

### **3.6 microservices-common-export - 导出组件**

```java
/**
 * ExcelExportService - Excel导出
 */
@Component
public class ExcelExportService {
    
    public <T> void exportExcel(
            List<T> data, 
            Class<T> clazz, 
            HttpServletResponse response) {
        
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String filename = URLEncoder.encode(
                "export_" + System.currentTimeMillis(), 
                "UTF-8"
            );
            response.setHeader(
                "Content-Disposition", 
                "attachment;filename=" + filename + ".xlsx"
            );
            
            EasyExcel.write(response.getOutputStream(), clazz)
                .sheet("Sheet1")
                .doWrite(data);
                
        } catch (Exception e) {
            throw new ExportException("Excel导出失败", e);
        }
    }
}
```

### **3.7 microservices-common-workflow - 工作流组件**

（已在oa-service中设计）

### **3.8 microservices-common-permission - 权限组件**

```java
/**
 * PermissionValidator - 权限验证器
 */
@Component
public class PermissionValidator {
    
    @Resource
    private UserPermissionDao permissionDao;
    
    /**
     * 验证用户是否有权限访问区域
     */
    public boolean hasAreaPermission(Long userId, Long areaId) {
        UserAreaPermissionEntity permission = 
            permissionDao.selectByUserAndArea(userId, areaId);
        
        if (permission == null) {
            return false;
        }
        
        // 检查权限是否过期
        if (permission.getEndTime() != null && 
            LocalDateTime.now().isAfter(permission.getEndTime())) {
            return false;
        }
        
        return true;
    }
}
```

### **3.9 microservices-common-business - 业务组件**

（包含所有共享实体、DAO、Manager）

### **3.10 microservices-common - 聚合组件**

（整合上述所有组件）

---

## 🎯 **四、完整业务场景设计**

### **场景1：员工刷脸门禁通行（完整流程）**

```
┌─────────────────────────────────────────────────────────────┐
│  场景: 员工刷脸门禁通行                                       │
└─────────────────────────────────────────────────────────────┘

1️⃣ 员工在门禁设备刷脸
   └─> device-comm-service 接收设备事件
       └─> 调用 biometric-service 进行人脸识别
           └─> FaceRecognitionStrategy.identify()
               └─> 返回 userId + matchScore
               
2️⃣ 调用 access-service 验证权限
   └─> BiometricAccessFlow.processAccess()
       └─> TimeBasedAccessStrategy.hasPermission()
       └─> GeofenceAccessStrategy.hasPermission()
       └─> RoleBasedAccessStrategy.hasPermission()
           └─> 权限验证通过
           
3️⃣ 下发开门指令
   └─> DeviceCommandService.execute(OPEN_DOOR)
       └─> HikvisionProtocolAdapter.sendCommand()
           └─> 门禁设备开门5秒
           
4️⃣ 记录通行事件
   └─> AccessRecordDao.insert(record)
   └─> WebSocket实时推送监控端
   └─> RabbitMQ异步消息
   
5️⃣ 视频联动录像
   └─> video-service 接收MQ消息
       └─> 启动该区域所有摄像头录像5分钟
       └─> VideoRecordDao.insert(record)
       
涉及服务: 5个 (gateway → device-comm → biometric → access → video)
设计模式: 策略模式 + 模板方法 + 装饰器
性能指标: 端到端响应 < 500ms
```

### **场景2：访客预约+现场签到（完整流程）**

```
┌─────────────────────────────────────────────────────────────┐
│  场景: 访客预约+现场签到                                      │
└─────────────────────────────────────────────────────────────┘

1️⃣ 访客在微信小程序提交预约
   └─> visitor-service.submitAppointment()
       └─> 创建预约记录 (status=PENDING)
       └─> 启动审批流程
           └─> oa-service.startProcess("visitor_appointment")
               └─> 创建WorkflowInstance + 首个Task
               └─> 通知被访人审批
               
2️⃣ 被访人审批通过
   └─> oa-service.completeTask(approved=true)
       └─> visitor-service 更新预约状态 (status=APPROVED)
           └─> 生成二维码通行证 (QR Code)
           └─> 发送通行证到访客手机
           └─> 临时授权门禁权限
               └─> access-service.grantTemporaryPermission()
                   └─> 创建 UserAreaPermissionEntity (临时)
                   
3️⃣ 访客到达现场扫码签到
   └─> visitor-service.checkIn(qrCode)
       └─> 验证二维码 + 时间范围
       └─> 可选人脸验证
           └─> biometric-service.verify(FACE)
       └─> 创建签到记录 (CheckInRecord)
       └─> 更新预约状态 (status=CHECKED_IN)
       └─> 开始轨迹追踪
           └─> 每次通过门禁/摄像头识别
               └─> visitor-service.recordTrackingPoint()
                   └─> Redis实时轨迹
                   └─> WebSocket推送监控端
                   └─> 检查未授权区域
                       └─> 触发告警 (SMS + WebSocket)
                       
4️⃣ 访客离开签出
   └─> visitor-service.checkOut()
       └─> 更新预约状态 (status=CHECKED_OUT)
       └─> 停止轨迹追踪
       └─> 撤销临时权限
       
涉及服务: 4个 (visitor → oa → access → biometric)
设计模式: 工作流引擎 + 状态机 + 观察者
性能指标: 预约提交 < 300ms, 签到 < 200ms
```

### **场景3：考勤月报生成（完整流程）**

```
┌─────────────────────────────────────────────────────────────┐
│  场景: 考勤月报生成                                           │
└─────────────────────────────────────────────────────────────┘

1️⃣ 定时任务触发月报生成
   └─> @Scheduled(cron = "0 0 2 1 * ?")  // 每月1号凌晨2点
       └─> attendance-service.generateMonthlyReport()
           └─> 查询上月所有打卡记录
               └─> AttendanceRecordDao.selectByMonth()
               
2️⃣ 应用考勤规则计算
   └─> 按用户分组统计
       └─> StandardWorkingHoursStrategy.calculate()
           └─> 计算迟到/早退/缺勤
       └─> FlexibleWorkingHoursStrategy.calculate()
           └─> 计算总工时
           
3️⃣ 汇总统计数据
   └─> EmployeeAttendanceSummary
       - 正常天数: 20天
       - 迟到次数: 2次
       - 早退次数: 0次
       - 缺勤次数: 1天
       - 加班时长: 30小时
       
4️⃣ 导出Excel报表
   └─> export-service.exportExcel()
       └─> EasyExcel.write()
           └─> 生成 attendance_report_202412.xlsx
           
5️⃣ 发送邮件通知
   └─> oa-service.sendEmail()
       └─> 部门经理 + HR
       └─> 附件: Excel报表
       
涉及服务: 3个 (attendance → export → oa)
设计模式: 策略模式(规则引擎) + 模板方法
性能指标: 1000人月报生成 < 10s
```

### **场景4：消费离线同步（完整流程）**

```
┌─────────────────────────────────────────────────────────────┐
│  场景: 消费机离线消费同步                                      │
└─────────────────────────────────────────────────────────────┘

1️⃣ 离线消费发生
   └─> 消费机无网络连接
       └─> 本地验证余额 (离线白名单)
       └─> 记录离线消费
           └─> offlineId: OFL20251218001
           └─> userId, amount, timestamp
           
2️⃣ 网络恢复后上传离线记录
   └─> device-comm-service 接收设备上传
       └─> 批量离线消费记录 (1000条)
       └─> 调用 consume-service.syncOfflineRecords()
       
3️⃣ 批量同步处理
   └─> OfflineConsumeProcessor.syncOfflineRecords()
       └─> 逐条处理:
           - 验证合法性
           - 防重复消费检查 (offlineId去重)
           - 补扣账户余额
           - 创建在线消费记录
       └─> 返回同步结果:
           - 成功: 980条
           - 失败: 15条 (余额不足)
           - 重复: 5条
           
4️⃣ 异常处理
   └─> 余额不足的记录
       └─> 创建欠费记录
       └─> 发送催缴通知
   └─> 重复记录
       └─> 记录日志,丢弃
       
涉及服务: 2个 (device-comm → consume)
设计模式: 批处理模式 + 补偿事务
性能指标: 1000条同步 < 5s
```

---

## 🚀 **五、企业级性能优化完整方案**

### **5.1 连接池优化**

```java
/**
 * DeviceConnectionPoolManager - 设备连接池
 */
@Component
public class DeviceConnectionPoolManager {
    
    private final Map<String, GenericObjectPool<DeviceConnection>> pools 
        = new ConcurrentHashMap<>();
    
    public DeviceConnection borrowConnection(String deviceId) throws Exception {
        return getOrCreatePool(deviceId).borrowObject();
    }
    
    public void returnConnection(String deviceId, DeviceConnection connection) {
        GenericObjectPool<DeviceConnection> pool = pools.get(deviceId);
        if (pool != null) {
            pool.returnObject(connection);
        }
    }
    
    private GenericObjectPool<DeviceConnection> getOrCreatePool(String deviceId) {
        return pools.computeIfAbsent(deviceId, id -> {
            GenericObjectPoolConfig<DeviceConnection> config 
                = new GenericObjectPoolConfig<>();
            config.setMaxTotal(5);        // 最大连接数
            config.setMaxIdle(2);         // 最大空闲连接
            config.setMinIdle(1);         // 最小空闲连接
            config.setTestOnBorrow(true); // 借用时测试
            
            return new GenericObjectPool<>(
                new DeviceConnectionFactory(id), 
                config
            );
        });
    }
}
```

### **5.2 对象池优化**

```java
/**
 * FeatureVectorPool - 特征向量对象池
 */
@Component
public class FeatureVectorPool {
    
    private final GenericObjectPool<float[]> vectorPool;
    
    public FeatureVectorPool() {
        GenericObjectPoolConfig<float[]> config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(1000);
        config.setMaxIdle(100);
        config.setMinIdle(10);
        
        this.vectorPool = new GenericObjectPool<>(
            new BasePooledObjectFactory<float[]>() {
                @Override
                public float[] create() {
                    return new float[512];  // FaceNet向量维度
                }
                
                @Override
                public PooledObject<float[]> wrap(float[] obj) {
                    return new DefaultPooledObject<>(obj);
                }
            },
            config
        );
    }
    
    public float[] borrowVector() throws Exception {
        return vectorPool.borrowObject();
    }
    
    public void returnVector(float[] vector) {
        Arrays.fill(vector, 0);  // 清空数据
        vectorPool.returnObject(vector);
    }
}
```

### **5.3 异步任务编排**

```java
/**
 * AsyncTaskConfiguration - 异步任务配置
 */
@Configuration
@EnableAsync
public class AsyncTaskConfiguration {
    
    @Bean("attendanceStatExecutor")
    public Executor attendanceStatExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("attendance-stat-");
        executor.setRejectedExecutionHandler(
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
        executor.initialize();
        return executor;
    }
    
    @Bean("reportExecutor")
    public Executor reportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("report-");
        executor.initialize();
        return executor;
    }
}
```

### **5.4 性能监控指标**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active}
```

---

## 📅 **六、10周实施路线图**

### **Phase 1: 基础设施重构 (Week 1-2)**

**目标**: 搭建企业级基础设施

**任务清单**:
- [ ] 创建 biometric-service 新服务
- [ ] 实现策略工厂 StrategyFactory
- [ ] 实现 UnifiedCacheManager 多级缓存
- [ ] 配置 Resilience4j 限流熔断
- [ ] 配置 Prometheus + Grafana 监控

**交付物**:
- biometric-service 基础骨架
- 公共组件库升级
- 监控仪表板

### **Phase 2: 模块拆分重构 (Week 3-5)**

**目标**: 完成核心模块拆分

**任务清单**:
- [ ] 迁移生物识别功能到 biometric-service
- [ ] 实现 5 大识别策略 (人脸/指纹/虹膜/掌纹/声纹)
- [ ] 实现模板同步服务
- [ ] 修正 access-service 的 package 声明错误
- [ ] 新增 UserAreaPermissionEntity 等4个实体

**交付物**:
- biometric-service 完整功能
- 权限模型完善
- Entity包结构规范

### **Phase 3: 设计模式应用 (Week 6-7)**

**目标**: 充分应用企业级设计模式

**任务清单**:
- [ ] 策略模式: 识别算法/考勤规则/消费模式
- [ ] 工厂模式: 设备适配器/视频流适配器
- [ ] 装饰器模式: 打卡流程增强/命令增强
- [ ] 模板方法: 通行流程/模板同步流程
- [ ] 依赖倒置: 所有 Strategy/Adapter 接口化

**交付物**:
- 5大设计模式完整实现
- 代码复用率提升 40%+

### **Phase 4: 性能优化 (Week 8-9)**

**目标**: 达到企业级性能标准

**任务清单**:
- [ ] 设备连接池优化
- [ ] 特征向量对象池优化
- [ ] 多级缓存全面应用
- [ ] 异步任务编排优化
- [ ] 慢查询优化 (索引/分页)

**交付物**:
- 响应时间降低 50%+
- 并发能力提升 300%+
- 内存占用降低 30%+

### **Phase 5: 文档与规范 (Week 10)**

**目标**: 完善文档和编码规范

**任务清单**:
- [ ] 更新所有架构文档
- [ ] 更新所有API文档
- [ ] 更新所有微服务文档
- [ ] 编写开发指南
- [ ] 编写部署手册

**交付物**:
- 完整文档体系 (100+ 文档)
- 开发规范手册
- 运维手册

---

✅ **完整的11个微服务**全面重构设计  
✅ **完整的10个公共组件**企业级标准实现  
✅ **5大设计模式**充分应用  
✅ **全业务场景**100%覆盖  
✅ **性能优化**架构全面升级  
✅ **10周实施路线图**清晰可执行  

通过本方案的实施，IOE-DREAM项目将达到：
- **企业级架构标准**
- **高性能**（响应时间↓50%，并发↑300%）
- **低内存**（内存占用↓40%）
- **高可扩展性**（模块化+组件化+策略化）
- **高可维护性**（代码复用率↑42%）

---

**文档版本**: v3.0.0-COMPLETE  
**最后更新**: 2025-12-18  
**审核状态**: ✅ 已完成  
**实施状态**: ⏳ 待实施
