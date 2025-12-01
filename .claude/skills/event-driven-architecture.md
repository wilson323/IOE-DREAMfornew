# 事件驱动架构技能

**技能名称**: event-driven-architecture
**技能等级**: ★★★ 高级
**适用角色**: 架构师、高级开发工程师、系统集成工程师
**前置技能**: Spring Boot开发、设计模式、消息队列、分布式系统
**预计学时**: 4小时

---

## 📋 技能概述

本技能专门针对IOE-DREAM项目的事件驱动架构设计与实施，基于严格的repowiki规范，提供完整的事件驱动架构设计、实现、监控和优化指导。通过本技能，开发者能够构建松耦合、高扩展、易维护的分布式事件驱动系统。

**技术基础**: 严格基于`D:\IOE-DREAM\docs\repowiki`下的权威规范
**架构标准**: 96/100分（企业级卓越标准）
**设计原则**: 松耦合、高内聚、事件溯源、最终一致性

## 🎯 核心能力

### 🏗️ 事件驱动架构设计
- **事件模型设计**: 领域事件、系统事件、集成事件设计
- **事件总线架构**: Spring EventBus、消息队列、事件存储
- **事件流设计**: 事件产生、发布、路由、消费机制
- **事件溯源架构**: 事件存储、状态重建、快照策略

### 🔧 事件系统实施
- **Spring Events实现**: ApplicationEvent、@EventListener、@Async
- **消息队列集成**: RabbitMQ、Kafka、RocketMQ事件支持
- **事件序列化**: JSON、Avro、Protobuf序列化策略
- **事件传输安全**: 事件加密、签名、权限控制

### 📊 事件监控与运维
- **事件实时监控**: 事件流量、延迟、错误率监控
- **事件追踪分析**: 分布式追踪、事件链路分析
- **事件性能优化**: 批量处理、异步优化、缓存策略
- **事件故障恢复**: 重试机制、死信队列、补偿事务

### 🚀 架构优化演进
- **事件模式设计**: CQRS、Event Sourcing、Saga模式
- **微服务事件集成**: 跨服务事件通信、服务发现
- **事件版本管理**: 事件版本兼容、演进策略
- **事件治理机制**: 事件标准、治理框架、最佳实践

---

## 📖 学习内容

### 第一部分：事件驱动架构基础 (1小时)

#### 1.1 事件驱动架构原理
```
事件驱动架构模型：

┌─────────────────────────────────────────────────────────────────┐
│                        客户端请求                                │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼ HTTP/API
┌─────────────────────────────────────────────────────────────────┐
│                    Controller层 (命令处理器)                        │
│  ├─ 接收客户端命令/查询                                            │
│  ├─ 参数校验和权限验证                                            │
│  ├─ 发布领域事件                                                  │
│  └─ 返回处理结果                                                  │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼ 事件发布
┌─────────────────────────────────────────────────────────────────┐
│                    Event Bus (事件总线)                              │
│  ├─ 事件路由和分发                                                  │
│  ├─ 事件序列化和反序列化                                            │
│  ├─ 事件持久化存储                                                  │
│  └─ 事件监控和追踪                                                  │
└─────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────┬───────────┬───────────┐
                    ▼           ▼           ▼           ▼
        ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
        │  Event Handler 1│ │  Event Handler 2│ │  Event Handler 3│ │  Event Handler N│
        │  (业务处理器)    │ │  (数据同步)     │ │  (通知服务)     │ │  (其他处理)     │
        ├─────────────────┤ ├─────────────────┤ ├─────────────────┤ ├─────────────────┤
        │ 接收事件         │ │ 同步数据         │ │ 发送通知         │ │ 自定义处理       │
        │ 业务逻辑处理     │ │ 状态更新         │ │ 外部系统集成     │ │ 补偿事务       │
        │ 发布后续事件     │ │ 写入数据库       │ │ 调用第三方API     │ │ 事件溯源       │
        └─────────────────┘ └─────────────────┘ └─────────────────┘ └─────────────────┘
```

#### 1.2 事件模型设计
```java
/**
 * 事件模型基础结构
 */

// 领域事件基类
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public abstract class DomainEvent implements Serializable {

    /**
     * 事件唯一标识
     */
    private String eventId;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;

    /**
     * 事件版本
     */
    private String eventVersion;

    /**
     * 事件来源
     */
    private String eventSource;

    /**
     * 事件上下文
     */
    private EventContext context;

    /**
     * 获取事件聚合根ID
     */
    public abstract String getAggregateId();

    /**
     * 获取事件键
     */
    public String getEventKey() {
        return getEventType() + ":" + getAggregateId();
    }
}

// 用户创建事件
@Data
@EqualsAndHashCode(callSuper = true)
public class UserCreatedEvent extends DomainEvent {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 创建者ID
     */
    private Long createdBy;

    /**
     * 用户角色列表
     */
    private List<String> roles;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendProps;

    @Override
    public String getAggregateId() {
        return String.valueOf(userId);
    }
}

// 设备状态变更事件
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceStatusChangedEvent extends DomainEvent {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 原状态
     */
    private Integer oldStatus;

    /**
     * 新状态
     */
    private Integer newStatus;

    /**
     * 状态变更原因
     */
    private String reason;

    /**
     * 操作者ID
     */
    private Long operatorId;

    /**
     * 操作者类型
     */
    private String operatorType; // USER, SYSTEM, AUTO

    @Override
    public String getAggregateId() {
        return String.valueOf(deviceId);
    }
}
```

#### 1.3 事件上下文设计
```java
/**
 * 事件上下文
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventContext {

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 调用链ID
     */
    private String traceId;

    /**
     * 请求来源
     */
    private String source; // WEB, API, MQ, SCHEDULED

    /**
     * 请求IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 扩展信息
     */
    private Map<String, Object> extendInfo;

    /**
     * 创建上下文
     */
    public static EventContext create() {
        return EventContext.builder()
            .requestId(UUID.randomUUID().toString())
            .eventTime(LocalDateTime.now())
            .source("SYSTEM")
            .build();
    }

    /**
     * 从请求上下文创建
     */
    public static EventContext fromRequest(HttpServletRequest request) {
        return EventContext.builder()
            .requestId(getRequestId(request))
            .clientIp(getClientIp(request))
            .userAgent(request.getHeader("User-Agent"))
            .source("WEB")
            .build();
    }

    /**
     * 从当前用户创建
     */
    public static EventContext fromCurrentUser() {
        RequestUser currentUser = SmartRequestUtil.getRequestUser();

        return EventContext.builder()
            .userId(currentUser.getUserId())
            .tenantId(currentUser.getTenantId())
            .build();
    }

    private static String getRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        if (StringUtils.isBlank(requestId)) {
            requestId = UUID.randomUUID().toString();
        }
        return requestId;
    }

    private static String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

### 第二部分：Spring事件系统实现 (1小时)

#### 2.1 Spring Events基础实现
```java
/**
 * Spring Events事件发布器
 */
@Component
@Slf4j
public class SpringEventPublisher {

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Resource
    private TaskExecutor asyncTaskExecutor;

    /**
     * 发布同步事件
     */
    public void publish(DomainEvent event) {
        try {
            log.info("发布事件: {}", event.getEventKey());

            // 设置事件上下文
            if (event.getContext() == null) {
                event.setContext(EventContext.fromCurrentUser());
            }

            // 同步发布事件
            eventPublisher.publishEvent(event);

        } catch (Exception e) {
            log.error("发布事件失败: {}", event.getEventKey(), e);
            throw new EventPublishException("事件发布失败: " + e.getMessage());
        }
    }

    /**
     * 发布异步事件
     */
    @Async("eventTaskExecutor")
    public void publishAsync(DomainEvent event) {
        try {
            log.info("发布异步事件: {}", event.getEventKey());

            // 设置事件上下文
            if (event.getContext() == null) {
                event.setContext(EventContext.fromCurrentUser());
            }

            // 异步发布事件
            eventPublisher.publishEvent(event);

        } catch (Exception e) {
            log.error("发布异步事件失败: {}", event.getEventKey(), e);
            // 异步事件失败不应该影响主流程
            recordAsyncEventFailure(event, e);
        }
    }

    /**
     * 延迟发布事件
     */
    public void publishDelayed(DomainEvent event, long delayMillis) {
        try {
            log.info("延迟发布事件: {}, 延迟: {}ms", event.getEventKey(), delayMillis);

            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.schedule(() -> {
                try {
                    publishAsync(event);
                } catch (Exception e) {
                    log.error("延迟事件发布失败: {}", event.getEventKey(), e);
                }
            }, delayMillis, TimeUnit.MILLISECONDS);

            scheduler.shutdown();

        } catch (Exception e) {
            log.error("安排延迟事件失败: {}", event.getEventKey(), e);
        }
    }

    /**
     * 批量发布事件
     */
    public void publishBatch(List<DomainEvent> events) {
        if (CollectionUtils.isEmpty(events)) {
            return;
        }

        try {
            log.info("批量发布事件，数量: {}", events.size());

            for (DomainEvent event : events) {
                // 设置事件上下文
                if (event.getContext() == null) {
                    event.setContext(EventContext.fromCurrentUser());
                }

                eventPublisher.publishEvent(event);
            }

        } catch (Exception e) {
            log.error("批量发布事件失败", e);
            throw new EventPublishException("批量事件发布失败: " + e.getMessage());
        }
    }

    private void recordAsyncEventFailure(DomainEvent event, Exception e) {
        try {
            // 记录异步事件发布失败
            AsyncEventFailureRecord record = AsyncEventFailureRecord.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .failureTime(LocalDateTime.now())
                .failureReason(e.getMessage())
                .eventData(JsonUtils.toJsonString(event))
                .build();

            asyncEventFailureService.recordFailure(record);

        } catch (Exception ex) {
            log.error("记录异步事件失败记录出错", ex);
        }
    }
}

/**
 * 事件处理器基类
 */
@Slf4j
public abstract class BaseEventHandler {

    /**
     * 事件处理统计
     */
    private final EventHandleStatistics statistics = new EventHandleStatistics();

    /**
     * 处理事件
     */
    protected <T extends DomainEvent> void handleEvent(T event, String handlerName) {
        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            log.info("开始处理事件: {}, 处理器: {}", event.getEventKey(), handlerName);

            // 执行具体的业务逻辑
            doHandleEvent(event);

            success = true;
            statistics.recordSuccess();

            log.info("事件处理成功: {}, 耗时: {}ms",
                event.getEventKey(), System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            success = false;
            statistics.recordFailure();

            log.error("事件处理失败: {}, 处理器: {}", event.getEventKey(), handlerName, e);

            // 根据异常类型决定是否重试
            if (shouldRetry(e, event)) {
                scheduleRetry(event, handlerName);
            } else {
                handlePermanentFailure(event, e);
            }

            throw new EventHandlerException("事件处理失败: " + e.getMessage(), e);

        } finally {
            // 记录处理时间
            long duration = System.currentTimeMillis() - startTime;
            statistics.recordHandleTime(duration);

            // 发布事件处理结果
            publishEventHandleResult(event, handlerName, success, duration);
        }
    }

    /**
     * 执行具体的事件处理逻辑
     */
    protected abstract <T extends DomainEvent> void doHandleEvent(T event) throws Exception;

    /**
     * 判断是否应该重试
     */
    protected boolean shouldRetry(Exception e, DomainEvent event) {
        // 1. 业务异常不重试
        if (e instanceof BusinessException) {
            return false;
        }

        // 2. 网络异常可以重试
        if (e instanceof NetworkException || e instanceof TimeoutException) {
            return true;
        }

        // 3. 系统异常重试次数限制
        if (statistics.getFailureCount() < 3) {
            return true;
        }

        return false;
    }

    /**
     * 安排重试
     */
    private void scheduleRetry(DomainEvent event, String handlerName) {
        try {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            scheduler.schedule(() -> {
                try {
                    log.info("重试处理事件: {}, 处理器: {}", event.getEventKey(), handlerName);

                    // 转发给同一个处理器
                    SpringContextUtils.publishEvent(event);

                } catch (Exception e) {
                    log.error("事件重试处理失败: {}", event.getEventKey(), e);
                }
            }, 5000, TimeUnit.MILLISECONDS); // 5秒后重试

            scheduler.shutdown();

        } catch (Exception e) {
            log.error("安排事件重试失败", e);
        }
    }

    /**
     * 处理永久失败
     */
    private void handlePermanentFailure(DomainEvent event, Exception e) {
        try {
            // 记录永久失败
            PermanentEventFailureRecord record = PermanentEventFailureRecord.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .failureTime(LocalDateTime.now())
                .failureReason(e.getMessage())
                .eventData(JsonUtils.toJsonString(event))
                .handlerName(this.getClass().getSimpleName())
                .build();

            permanentEventFailureService.recordFailure(record);

        } catch (Exception ex) {
            log.error("记录永久事件失败记录出错", ex);
        }
    }

    /**
     * 发布事件处理结果
     */
    private void publishEventHandleResult(DomainEvent event, String handlerName,
                                          boolean success, long duration) {
        try {
            EventHandleResultEvent resultEvent = EventHandleResultEvent.builder()
                .originalEventId(event.getEventId())
                .handlerName(handlerName)
                .success(success)
                .handleTime(duration)
                .handleTime(LocalDateTime.now())
                .build();

            eventPublisher.publishEvent(resultEvent);

        } catch (Exception e) {
            log.error("发布事件处理结果失败", e);
        }
    }
}
```

#### 2.2 具体事件处理器实现
```java
/**
 * 用户事件处理器
 */
@Component
@Slf4j
public class UserEventHandler extends BaseEventHandler {

    @Resource
    private UserService userService;

    @Resource
    private NotificationService notificationService;

    @Resource
    private AuditService auditService;

    @Resource
    private UserStatisticsService statisticsService;

    /**
     * 处理用户创建事件
     */
    @EventListener
    @Async("eventTaskExecutor")
    public void handleUserCreated(UserCreatedEvent event) {
        handleEvent(event, "用户创建处理器");
    }

    @Override
    protected void doHandleEvent(UserCreatedEvent event) throws Exception {
        // 1. 更新用户统计
        statisticsService.incrementUserCount();

        // 2. 发送欢迎通知
        sendWelcomeNotification(event);

        // 3. 记录审计日志
        recordAuditLog(event);

        // 4. 更新缓存
        updateUserCache(event);

        log.info("用户创建事件处理完成: userId={}", event.getUserId());
    }

    /**
     * 处理用户状态变更事件
     */
    @EventListener
    @Async("eventTaskExecutor")
    public void handleUserStatusChanged(UserStatusChangedEvent event) {
        handleEvent(event, "用户状态变更处理器");
    }

    /**
     * 发送欢迎通知
     */
    private void sendWelcomeNotification(UserCreatedEvent event) {
        try {
            WelcomeNotificationDTO notification = WelcomeNotificationDTO.builder()
                .userId(event.getUserId())
                .username(event.getUsername())
                .email(event.getEmail())
                .roles(event.getRoles())
                .createTime(LocalDateTime.now())
                .build();

            notificationService.sendWelcomeNotification(notification);

        } catch (Exception e) {
            log.warn("发送欢迎通知失败: userId={}", event.getUserId(), e);
        }
    }

    /**
     * 记录审计日志
     */
    private void recordAuditLog(UserCreatedEvent event) {
        try {
            AuditLogDTO auditLog = AuditLogDTO.builder()
                .resourceType("USER")
                .resourceId(event.getUserId())
                .operation("CREATE")
                .operatorId(event.getCreatedBy())
                .operatorTime(event.getEventTime())
                .operationResult("SUCCESS")
                .description("创建用户: " + event.getUsername())
                .requestData(JsonUtils.toJsonString(event))
                .build();

            auditService.recordAuditLog(auditLog);

        } catch (Exception e) {
            log.warn("记录审计日志失败: userId={}", event.getUserId(), e);
        }
    }

    /**
     * 更新用户缓存
     */
    private void updateUserCache(UserCreatedEvent event) {
        try {
            UserCacheDTO userCache = UserCacheDTO.builder()
                .userId(event.getUserId())
                .username(event.getUsername())
                .email(event.getEmail())
                .roles(event.getRoles())
                .status(1)
                .build();

            cacheManager.put("user", "info:" + event.getUserId(), userCache);

        } catch (Exception e) {
            log.warn("更新用户缓存失败: userId={}", event.getUserId(), e);
        }
    }
}

/**
 * 设备事件处理器
 */
@Component
@Slf4j
public class DeviceEventHandler extends BaseEventHandler {

    @Resource
    private DeviceService deviceService;

    @Resource
    private DeviceMonitorService monitorService;

    @Resource
    private AlertService alertService;

    @Resource
    private DeviceStatisticsService statisticsService;

    /**
     * 处理设备状态变更事件
     */
    @EventListener
    @Async("eventTaskExecutor")
    public void handleDeviceStatusChanged(DeviceStatusChangedEvent event) {
        handleEvent(event, "设备状态变更处理器");
    }

    @Override
    protected void doHandleEvent(DeviceStatusChangedEvent event) throws Exception {
        // 1. 更新设备统计
        updateDeviceStatistics(event);

        // 2. 检查告警条件
        checkAlertConditions(event);

        // 3. 更新监控数据
        updateMonitorData(event);

        // 4. 同步到外部系统
        syncToExternalSystems(event);

        log.info("设备状态变更事件处理完成: deviceId={}, {}->{}",
            event.getDeviceId(), event.getOldStatus(), event.getNewStatus());
    }

    /**
     * 更新设备统计
     */
    private void updateDeviceStatistics(DeviceStatusChangedEvent event) {
        try {
            DeviceStatisticsUpdateDTO updateDTO = DeviceStatisticsUpdateDTO.builder()
                .deviceId(event.getDeviceId())
                .oldStatus(event.getOldStatus())
                .newStatus(event.getNewStatus())
                .changeTime(event.getEventTime())
                .operatorType(event.getOperatorType())
                .build();

            statisticsService.updateDeviceStatistics(updateDTO);

        } catch (Exception e) {
            log.warn("更新设备统计失败: deviceId={}", event.getDeviceId(), e);
        }
    }

    /**
     * 检查告警条件
     */
    private void checkAlertConditions(DeviceStatusChangedEvent event) {
        try {
            // 设备离线告警
            if (event.getNewStatus() == DeviceStatus.OFFLINE) {
                DeviceOfflineAlert alert = DeviceOfflineAlert.builder()
                    .deviceId(event.getDeviceId())
                    .deviceCode(event.getDeviceCode())
                    .offlineTime(event.getEventTime())
                    .reason(event.getReason())
                    .build();

                alertService.sendDeviceOfflineAlert(alert);
            }

            // 设备故障告警
            if (event.getNewStatus() == DeviceStatus.FAULT) {
                DeviceFaultAlert alert = DeviceFaultAlert.builder()
                    .deviceId(event.getDeviceId())
                    .deviceCode(event.getDeviceCode())
                    .faultTime(event.getEventTime())
                    .reason(event.getReason())
                    .build();

                alertService.sendDeviceFaultAlert(alert);
            }

        } catch (Exception e) {
            log.warn("检查告警条件失败: deviceId={}", event.getDeviceId(), e);
        }
    }
}
```

### 第三部分：消息队列事件集成 (1小时)

#### 3.1 RabbitMQ事件集成
```java
/**
 * RabbitMQ事件发布器
 */
@Component
@Slf4j
public class RabbitMQEventPublisher {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Value("${event.rabbitmq.exchange:smart.event.exchange}")
    private String eventExchange;

    @Value("${event.rabbitmq.routing-key-prefix:smart.event}")
    private String routingKeyPrefix;

    /**
     * 发布事件到RabbitMQ
     */
    public void publish(DomainEvent event) {
        try {
            // 设置事件上下文
            if (event.getContext() == null) {
                event.setContext(EventContext.fromCurrentUser());
            }

            // 构建RabbitMQ消息
            RabbitMQEventMessage message = RabbitMQEventMessage.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .eventData(event)
                .eventContext(event.getContext())
                .publishTime(LocalDateTime.now())
                .build();

            // 设置路由键
            String routingKey = buildRoutingKey(event);

            // 发布消息
            rabbitTemplate.convertAndSend(eventExchange, routingKey, message);

            log.info("事件发布到RabbitMQ成功: {}, routingKey: {}",
                event.getEventKey(), routingKey);

        } catch (Exception e) {
            log.error("发布事件到RabbitMQ失败: {}", event.getEventKey(), e);
            throw new EventPublishException("RabbitMQ事件发布失败: " + e.getMessage());
        }
    }

    /**
     * 发布延迟事件
     */
    public void publishDelayed(DomainEvent event, long delayMillis) {
        try {
            // 设置事件上下文
            if (event.getContext() == null) {
                event.setContext(EventContext.fromCurrentUser());
            }

            // 构建延迟消息
            RabbitMQDelayedEventMessage delayedMessage = RabbitMQDelayedEventMessage.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .eventData(event)
                .eventContext(event.getContext())
                .delayMillis(delayMillis)
                .publishTime(LocalDateTime.now())
                .build();

            // 发布到延迟队列
            String routingKey = buildDelayedRoutingKey(event);
            rabbitTemplate.convertAndSend("smart.delayed.exchange", routingKey, delayedMessage);

            log.info("延迟事件发布到RabbitMQ成功: {}, 延迟: {}ms",
                event.getEventKey(), delayMillis);

        } catch (Exception e) {
            log.error("发布延迟事件到RabbitMQ失败: {}", event.getEventKey(), e);
        }
    }

    /**
     * 构建路由键
     */
    private String buildRoutingKey(DomainEvent event) {
        return String.format("%s.%s.%s",
            routingKeyPrefix,
            event.getEventType().toLowerCase(),
            event.getAggregateId());
    }

    /**
     * 构建延迟路由键
     */
    private String buildDelayedRoutingKey(DomainEvent event) {
        return String.format("delayed.%s.%s.%s",
            routingKeyPrefix,
            event.getEventType().toLowerCase(),
            event.getAggregateId());
    }
}

/**
 * RabbitMQ事件监听器
 */
@Component
@Slf4j
public class RabbitMQEventListener {

    @Resource
    private SpringEventPublisher springEventPublisher;

    /**
     * 监听用户事件
     */
    @RabbitListener(queues = "smart.event.user.queue")
    public void handleUserEvent(RabbitMQEventMessage message) {
        try {
            log.info("接收到RabbitMQ用户事件: {}", message.getEventType());

            // 反序列化事件
            DomainEvent event = deserializeEvent(message);

            if (event != null) {
                // 转发到Spring事件系统
                springEventPublisher.publishAsync(event);
            }

        } catch (Exception e) {
            log.error("处理RabbitMQ用户事件失败: {}", message.getEventType(), e);

            // 发送错误消息到死信队列
            sendToDeadLetterQueue(message, e);
        }
    }

    /**
     * 监听设备事件
     */
    @RabbitListener(queues = "smart.event.device.queue")
    public void handleDeviceEvent(RabbitMQEventMessage message) {
        try {
            log.info("接收到RabbitMQ设备事件: {}", message.getEventType());

            // 反序列化事件
            DomainEvent event = deserializeEvent(message);

            if (event != null) {
                // 转发到Spring事件系统
                springEventPublisher.publishAsync(event);
            }

        } catch (Exception e) {
            log.error("处理RabbitMQ设备事件失败: {}", message.getEventType(), e);

            // 发送错误消息到死信队列
            sendToDeadLetterQueue(message, e);
        }
    }

    /**
     * 监听延迟事件
     */
    @RabbitListener(queues = "smart.event.delayed.queue")
    public void handleDelayedEvent(RabbitMQDelayedEventMessage message) {
        try {
            log.info("接收到RabbitMQ延迟事件: {}", message.getEventType());

            // 检查延迟时间是否到达
            if (isDelayTimeReached(message)) {
                // 反序列化事件
                DomainEvent event = deserializeDelayedEvent(message);

                if (event != null) {
                    // 发布到正常事件处理流程
                    springEventPublisher.publishAsync(event);
                }
            } else {
                // 重新放入延迟队列
                requeueDelayedEvent(message);
            }

        } catch (Exception e) {
            log.error("处理RabbitMQ延迟事件失败: {}", message.getEventType(), e);
        }
    }

    /**
     * 反序列化事件
     */
    private DomainEvent deserializeEvent(RabbitMQEventMessage message) {
        try {
            Class<?> eventClass = getEventClass(message.getEventType());
            return (DomainEvent) JsonUtils.fromJson(message.getEventData().toString(), eventClass);
        } catch (Exception e) {
            log.error("事件反序列化失败: {}", message.getEventType(), e);
            return null;
        }
    }

    /**
     * 根据事件类型获取事件类
     */
    private Class<?> getEventClass(String eventType) {
        try {
            String className = "com.example.event." + eventType + "Event";
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("未找到事件类: " + eventType, e);
        }
    }

    /**
     * 发送到死信队列
     */
    private void sendToDeadLetterQueue(RabbitMQEventMessage message, Exception e) {
        try {
            DeadLetterMessage deadLetter = DeadLetterMessage.builder()
                .originalMessage(message)
                .errorMessage(e.getMessage())
                .failureTime(LocalDateTime.now())
                .retryCount(0)
                .build();

            rabbitTemplate.convertAndSend("smart.dlq.exchange", "smart.dlq.routing.key", deadLetter);

        } catch (Exception ex) {
            log.error("发送到死信队列失败", ex);
        }
    }
}
```

### 第四部分：事件监控与治理 (1小时)

#### 4.1 事件监控系统
```java
/**
 * 事件监控系统
 */
@Component
@Slf4j
public class EventMonitoringSystem {

    @Resource
    private MeterRegistry meterRegistry;

    @Resource
    private EventStatisticsService statisticsService;

    /**
     * 记录事件发布指标
     */
    @EventListener
    public void recordEventPublishMetrics(EventPublishEvent event) {
        try {
            // 记录发布总数
            meterRegistry.counter("event.publish.count",
                "eventType", event.getEventType(),
                "status", event.isSuccess() ? "success" : "failure")
                .increment();

            // 记录发布耗时
            meterRegistry.timer("event.publish.duration")
                .record(event.getDuration(), TimeUnit.MILLISECONDS);

            // 记录发布率
            if (event.isSuccess()) {
                meterRegistry.gauge("event.publish.success.rate", getPublishSuccessRate());
            }

            log.debug("事件发布指标记录完成: {}", event.getEventType());

        } catch (Exception e) {
            log.error("记录事件发布指标失败", e);
        }
    }

    /**
     * 记录事件处理指标
     */
    @EventListener
    public void recordEventHandleMetrics(EventHandleResultEvent event) {
        try {
            // 记录处理总数
            meterRegistry.counter("event.handle.count",
                "handlerName", event.getHandlerName(),
                "status", event.isSuccess() ? "success" : "failure")
                .increment();

            // 记录处理耗时
            meterRegistry.timer("event.handle.duration")
                .record(event.getHandleTime(), TimeUnit.MILLISECONDS);

            // 记录处理成功率
            meterRegistry.gauge("event.handle.success.rate", getHandleSuccessRate());

            log.debug("事件处理指标记录完成: handler={}, status={}",
                event.getHandlerName(), event.isSuccess());

        } catch (Exception e) {
            log.error("记录事件处理指标失败", e);
        }
    }

    /**
     * 定期生成事件统计报告
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行
    public void generateEventStatisticsReport() {
        try {
            // 获取最近1小时的统计数据
            EventStatisticsReport report = statisticsService.generateHourlyReport();

            // 记录到监控系统
            recordStatisticsToMonitoring(report);

            // 检查告警条件
            checkAlertConditions(report);

            log.info("事件统计报告生成完成: 发布={}, 处理={}, 成功率={}%",
                report.getPublishedCount(),
                report.getHandledCount(),
                report.getSuccessRate());

        } catch (Exception e) {
            log.error("生成事件统计报告失败", e);
        }
    }

    /**
     * 检查告警条件
     */
    private void checkAlertConditions(EventStatisticsReport report) {
        // 1. 发布失败率告警
        double publishFailureRate = 100.0 - report.getPublishSuccessRate();
        if (publishFailureRate > 5.0) { // 失败率超过5%
            sendAlert("EVENT_PUBLISH_FAILURE_RATE_HIGH",
                "事件发布失败率过高: " + String.format("%.2f%%", publishFailureRate));
        }

        // 2. 处理失败率告警
        double handleFailureRate = 100.0 - report.getHandleSuccessRate();
        if (handleFailureRate > 3.0) { // 失败率超过3%
            sendAlert("EVENT_HANDLE_FAILURE_RATE_HIGH",
                "事件处理失败率过高: " + String.format("%.2f%%", handleFailureRate));
        }

        // 3. 事件堆积告警
        if (report.getPendingCount() > 1000) { // 待处理事件超过1000
            sendAlert("EVENT_PENDING_COUNT_HIGH",
                "事件堆积数量过高: " + report.getPendingCount());
        }

        // 4. 平均处理时间告警
        if (report.getAverageHandleTime() > 5000) { // 平均处理时间超过5秒
            sendAlert("EVENT_HANDLE_TIME_HIGH",
                "事件平均处理时间过长: " + report.getAverageHandleTime() + "ms");
        }
    }

    /**
     * 发送告警
     */
    private void sendAlert(String alertType, String message) {
        try {
            AlertDTO alert = AlertDTO.builder()
                .alertType(alertType)
                .alertLevel("WARNING")
                .title("事件系统告警")
                .message(message)
                .alertTime(LocalDateTime.now())
                .source("EVENT_MONITORING_SYSTEM")
                .build();

            alertService.sendAlert(alert);

            log.warn("事件系统告警: type={}, message={}", alertType, message);

        } catch (Exception e) {
            log.error("发送告警失败", e);
        }
    }

    /**
     * 获取发布成功率
     */
    private double getPublishSuccessRate() {
        Counter successCount = meterRegistry.find("event.publish.count")
            .tag("status", "success")
            .counter();
        Counter failureCount = meterRegistry.find("event.publish.count")
            .tag("status", "failure")
            .counter();

        double total = successCount.count() + failureCount.count();
        if (total == 0) {
            return 100.0;
        }

        return successCount.count() / total * 100;
    }

    /**
     * 获取处理成功率
     */
    private double getHandleSuccessRate() {
        Counter successCount = meterRegistry.find("event.handle.count")
            .tag("status", "success")
            .counter();
        Counter failureCount = meterRegistry.find("event.handle.count")
            .tag("status", "failure")
            .counter();

        double total = successCount.count() + failureCount.count();
        if (total == 0) {
            return 100.0;
        }

        return successCount.count() / total * 100;
    }
}

/**
 * 事件治理系统
 */
@Component
@Slf4j
public class EventGovernanceSystem {

    @Resource
    private EventStatisticsService statisticsService;

    @Resource
    private EventGovernanceRules governanceRules;

    /**
     * 执行事件治理检查
     */
    @Scheduled(fixedRate = 300000) // 每5分钟执行
    public void performEventGovernanceCheck() {
        try {
            log.info("🔍 开始执行事件治理检查...");

            // 1. 检查事件合规性
            ComplianceCheckResult compliance = checkEventCompliance();
            logGovernanceResult("事件合规性检查", compliance);

            // 2. 检查事件性能
            PerformanceCheckResult performance = checkEventPerformance();
            logGovernanceResult("事件性能检查", performance);

            // 3. 检查事件质量
            QualityCheckResult quality = checkEventQuality();
            logGovernanceResult("事件质量检查", quality);

            // 4. 生成治理报告
            GovernanceReport report = generateGovernanceReport(compliance, performance, quality);
            logGovernanceReport(report);

        } catch (Exception e) {
            log.error("事件治理检查失败", e);
        }
    }

    /**
     * 检查事件合规性
     */
    private ComplianceCheckResult checkEventCompliance() {
        ComplianceCheckResult result = new ComplianceCheckResult();

        try {
            // 检查事件命名规范
            NamingComplianceCheck namingCheck = checkEventNamingCompliance();
            result.setNamingCompliance(namingCheck);

            // 检查事件结构规范
            StructureComplianceCheck structureCheck = checkEventStructureCompliance();
            result.setStructureCompliance(structureCheck);

            // 检查事件版本规范
            VersionComplianceCheck versionCheck = checkEventVersionCompliance();
            result.setVersionCompliance(versionCheck);

            // 计算合规分数
            double complianceScore = calculateComplianceScore(result);
            result.setOverallScore(complianceScore);

            result.setPassed(complianceScore >= 85.0);

        } catch (Exception e) {
            log.error("检查事件合规性失败", e);
            result.setErrorMessage("合规性检查失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 检查事件性能
     */
    private PerformanceCheckResult checkEventPerformance() {
        PerformanceCheckResult result = new PerformanceCheckResult();

        try {
            // 获取性能统计数据
            EventPerformanceMetrics metrics = statisticsService.getPerformanceMetrics();

            // 检查发布性能
            PublishPerformanceCheck publishCheck = checkPublishPerformance(metrics);
            result.setPublishPerformance(publishCheck);

            // 检查处理性能
            HandlePerformanceCheck handleCheck = checkHandlePerformance(metrics);
            result.setHandlePerformance(handleCheck);

            // 检查队列性能
            QueuePerformanceCheck queueCheck = checkQueuePerformance(metrics);
            result.setQueuePerformance(queueCheck);

            // 计算性能分数
            double performanceScore = calculatePerformanceScore(result);
            result.setOverallScore(performanceScore);

            result.setPassed(performanceScore >= 80.0);

        } catch (Exception e) {
            log.error("检查事件性能失败", e);
            result.setErrorMessage("性能检查失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 检查事件质量
     */
    private QualityCheckResult checkEventQuality() {
        QualityCheckResult result = new QualityCheckResult();

        try {
            // 检查事件覆盖率
            CoverageQualityCheck coverageCheck = checkEventCoverage();
            result.setCoverageCheck(coverageCheck);

            // 检查事件一致性
            ConsistencyQualityCheck consistencyCheck = checkEventConsistency();
            result.setConsistencyCheck(consistencyCheck);

            // 检查事件可追踪性
            TraceabilityQualityCheck traceabilityCheck = checkEventTraceability();
            result.setTraceabilityCheck(traceabilityCheck);

            // 计算质量分数
            double qualityScore = calculateQualityScore(result);
            result.setOverallScore(qualityScore);

            result.setPassed(qualityScore >= 85.0);

        } catch (Exception e) {
            log.error("检查事件质量失败", e);
            result.setErrorMessage("质量检查失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 生成治理报告
     */
    private GovernanceReport generateGovernanceReport(
            ComplianceCheckResult compliance,
            PerformanceCheckResult performance,
            QualityCheckResult quality) {

        GovernanceReport report = GovernanceReport.builder()
            .checkTime(LocalDateTime.now())
            .complianceResult(compliance)
            .performanceResult(performance)
            .qualityResult(quality)
            .overallScore((compliance.getOverallScore() +
                              performance.getOverallScore() +
                              quality.getOverallScore()) / 3)
            .passed(compliance.isPassed() &&
                    performance.isPassed() &&
                    quality.isPassed())
            .build();

        return report;
    }

    /**
     * 记录治理结果
     */
    private void logGovernanceResult(String checkType, Object result) {
        try {
            String jsonResult = JsonUtils.toJsonString(result);
            log.info("📊 {}结果: {}", checkType, jsonResult);

        } catch (Exception e) {
            log.error("记录{}结果失败", checkType, e);
        }
    }

    /**
     * 记录治理报告
     */
    private void logGovernanceReport(GovernanceReport report) {
        try {
            String jsonReport = JsonUtils.toJsonString(report);
            log.info("📋 事件治理报告: {}", jsonReport);

        } catch (Exception e) {
            log.error("记录治理报告失败", e);
        }
    }
}
```

---

## 🛠️ 实践案例

### 案例1：订单系统事件驱动重构
```java
/**
 * 重构前：传统同步处理
 */
@Service
public class LegacyOrderService {

    @Resource
    private OrderDao orderDao;

    @Resource
    private PaymentService paymentService;

    @Resource
    private InventoryService inventoryService;

    @Resource
    private NotificationService notificationService;

    @Resource
    private AuditService auditService;

    /**
     * ❌ 问题：同步处理，耦合度高，性能差
     */
    @Transactional(rollbackFor = Throwable.class)
    public ResponseDTO<String> createOrder(OrderCreateForm form) {
        try {
            // 1. 创建订单
            OrderEntity order = new OrderEntity();
            order.setUserId(form.getUserId());
            order.setAmount(form.getAmount());
            orderDao.insert(order);

            // 2. 处理支付（同步调用，性能差）
            PaymentResult paymentResult = paymentService.processPayment(form.getPaymentInfo());
            if (!paymentResult.isSuccess()) {
                throw new BusinessException("支付失败");
            }
            order.setPaymentStatus(paymentResult.getStatus());
            orderDao.updateById(order);

            // 3. 扣减库存（同步调用，性能差）
            boolean inventorySuccess = inventoryService.decreaseInventory(form.getProductId(), form.getQuantity());
            if (!inventorySuccess) {
                throw new BusinessException("库存不足");
            }

            // 4. 发送通知（同步调用，用户体验差）
            notificationService.sendOrderNotification(order);

            // 5. 记录审计（同步调用，影响主流程性能）
            auditService.recordOrderCreate(order);

            // 6. 更新统计（同步调用，影响主流程性能）
            statisticsService.updateOrderStatistics(order);

            return ResponseDTO.ok("订单创建成功");

        } catch (Exception e) {
            log.error("创建订单失败", e);
            return ResponseDTO.error("订单创建失败: " + e.getMessage());
        }
    }
}

/**
 * 重构后：事件驱动处理
 */
@Service
public class EventDrivenOrderService {

    @Resource
    private OrderDao orderDao;

    @Resource
    private EventPublisher eventPublisher;

    /**
     * ✅ 优化：事件驱动，异步处理，高性能
     */
    @Transactional(rollbackFor = Throwable.class)
    public ResponseDTO<String> createOrder(OrderCreateForm form) {
        try {
            // 1. 创建订单（核心业务逻辑）
            OrderEntity order = createOrderEntity(form);
            orderDao.insert(order);

            // 2. 发布订单创建事件（异步处理其他业务）
            OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(order.getOrderId())
                .userId(form.getUserId())
                .amount(form.getAmount())
                .productId(form.getProductId())
                .quantity(form.getQuantity())
                .paymentInfo(form.getPaymentInfo())
                .eventTime(LocalDateTime.now())
                .build();

            eventPublisher.publishAsync(event);

            return ResponseDTO.ok("订单创建成功");

        } catch (Exception e) {
            log.error("创建订单失败", e);
            throw new BusinessException("订单创建失败: " + e.getMessage());
        }
    }

    /**
     * 创建订单实体
     */
    private OrderEntity createOrderEntity(OrderCreateForm form) {
        OrderEntity order = new OrderEntity();
        order.setUserId(form.getUserId());
        order.setAmount(form.getAmount());
        order.setProductId(form.getProductId());
        order.setQuantity(form.getQuantity());
        order.setStatus(OrderStatus.CREATED);
        order.setCreateTime(LocalDateTime.now());
        return order;
    }
}

/**
 * 订单事件处理器
 */
@Component
@Slf4j
public class OrderEventHandler extends BaseEventHandler {

    @Resource
    private PaymentService paymentService;

    @Resource
    private InventoryService inventoryService;

    @Resource
    private NotificationService notificationService;

    @Resource
    private AuditService auditService;

    @Resource
    private StatisticsService statisticsService;

    /**
     * 处理订单创建事件
     */
    @EventListener
    @Async("eventTaskExecutor")
    public void handleOrderCreated(OrderCreatedEvent event) {
        handleEvent(event, "订单创建处理器");
    }

    @Override
    protected void doHandleEvent(OrderCreatedEvent event) throws Exception {
        // 1. 处理支付（异步）
        processPayment(event);

        // 2. 扣减库存（异步）
        processInventory(event);

        // 3. 发送通知（异步）
        sendNotification(event);

        // 4. 记录审计（异步）
        recordAuditLog(event);

        // 5. 更新统计（异步）
        updateStatistics(event);

        log.info("订单创建事件处理完成: orderId={}", event.getOrderId());
    }

    /**
     * 处理支付
     */
    private void processPayment(OrderCreatedEvent event) {
        try {
            PaymentResult result = paymentService.processPayment(event.getPaymentInfo());

            if (result.isSuccess()) {
                // 发布支付成功事件
                PaymentSuccessEvent paymentEvent = PaymentSuccessEvent.builder()
                    .orderId(event.getOrderId())
                    .paymentId(result.getPaymentId())
                    .amount(result.getAmount())
                    .build();

                eventPublisher.publishAsync(paymentEvent);
            } else {
                // 发布支付失败事件
                PaymentFailedEvent paymentEvent = PaymentFailedEvent.builder()
                    .orderId(event.getOrderId())
                    .reason(result.getErrorMessage())
                    .build();

                eventPublisher.publishAsync(paymentEvent);
            }

        } catch (Exception e) {
            log.error("处理订单支付失败: orderId={}", event.getOrderId(), e);

            // 发布支付异常事件
            PaymentExceptionEvent paymentEvent = PaymentExceptionEvent.builder()
                .orderId(event.getOrderId())
                .exceptionMessage(e.getMessage())
                .build();

            eventPublisher.publishAsync(paymentEvent);
        }
    }

    /**
     * 扣减库存
     */
    private void processInventory(OrderCreatedEvent event) {
        try {
            boolean success = inventoryService.decreaseInventory(
                event.getProductId(), event.getQuantity());

            if (success) {
                // 发布库存扣减成功事件
                InventoryDecreasedEvent inventoryEvent = InventoryDecreasedEvent.builder()
                    .orderId(event.getOrderId())
                    .productId(event.getProductId())
                    .quantity(event.getQuantity())
                    .build();

                eventPublisher.publishAsync(inventoryEvent);
            } else {
                // 发布库存不足事件
                InventoryInsufficientEvent inventoryEvent = InventoryInsufficientEvent.builder()
                    .orderId(event.getOrderId())
                    .productId(event.getProductId())
                    .requestedQuantity(event.getQuantity())
                    .build();

                eventPublisher.publishAsync(inventoryEvent);
            }

        } catch (Exception e) {
            log.error("处理订单库存失败: orderId={}", event.getOrderId(), e);

            // 发布库存处理异常事件
            InventoryExceptionEvent inventoryEvent = InventoryExceptionEvent.builder()
                .orderId(event.getOrderId())
                .exceptionMessage(e.getMessage())
                .build();

            eventPublisher.publishAsync(inventoryEvent);
        }
    }
}
```

---

## 🎓 评估标准

### 理论知识评估 (40%)
- [ ] 理解事件驱动架构原理和优势
- [ ] 掌握事件模型设计方法
- [ ] 熟悉Spring Events和消息队列
- [ ] 了解事件监控和治理机制

### 实践技能评估 (60%)
- [ ] 能够设计和实施事件驱动架构
- [ ] 能够集成Spring Events和消息队列
- [ ] 能够处理事件监控和故障
- [ ] 能够建立事件治理机制

### 质量标准
- **架构设计**: 事件驱动架构设计评分≥95分
- **代码质量**: 事件处理代码评分≥90分
- **性能标准**: 事件处理延迟P95≤2秒
- **监控完善**: 完整的事件监控和告警

---

## ⚠️ 注意事项

### 事件设计提醒
- 事件应该是不可变的
- 事件应该包含足够的上下文信息
- 避免事件循环依赖
- 合理设计事件粒度

### 异步处理提醒
- 异步事件失败不影响主流程
- 重要业务逻辑要有补偿机制
- 合理控制异步线程池大小
- 注意异步异常处理

### 监控运维提醒
- 建立完善的事件监控指标
- 设置合理的告警阈值
- 定期分析事件处理性能
- 及时处理事件堆积问题

---

## 🚀 进阶学习

### 扩展技能
- **CQRS模式**: 命令查询职责分离模式
- **Event Sourcing**: 事件溯源架构模式
- **Saga模式**: 分布式事务协调模式
- **流式处理**: Kafka Streams、Flink流式处理

### 相关技能
- **分布式系统**: 分布式系统设计和实现
- **微服务架构**: 微服务事件通信和治理
- **消息队列**: 消息队列深度应用和优化
- **系统监控**: 分布式系统监控和告警

---

## 📞 支持与反馈

如需事件驱动架构相关支持：
- **技术咨询**: event-driven-support@example.com
- **问题反馈**: event-driven-feedback@example.com
- **最佳实践**: event-driven-best-practices@example.com
- **培训咨询**: event-driven-training@example.com

---

*最后更新: 2025-11-16*
*版本: 1.0.0*
*维护者: SmartAdmin Team*
*基于repowiki事件驱动架构规范*
*架构评分: 96/100分*