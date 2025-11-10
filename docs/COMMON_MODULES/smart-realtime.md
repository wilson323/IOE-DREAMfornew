# 实时数据公共模块开发文档

> **📋 版本**: v1.0.0
> **📋 创建时间**: 2025-01-10
> **📋 适用范围**: 所有需要实时数据处理和推送的业务模块

---

## 📖 模块概述

### 模块简介
smart-realtime 是 SmartAdmin 项目的实时数据处理公共模块，提供统一的实时数据推送、缓存管理、数据同步等功能，支持WebSocket、SSE等多种实时通信方式。

### 核心特性
- **多协议支持**: WebSocket、Server-Sent Events、长轮询
- **高性能缓存**: 多级缓存策略，支持分布式缓存
- **实时推送**: 基于事件驱动的实时数据推送
- **数据同步**: 跨服务、跨节点的数据同步机制
- **连接管理**: 智能连接池管理和负载均衡
- **消息队列**: 支持多种消息队列中间件

---

## 🏗️ 架构设计

### 模块结构

```
smart-realtime/
├── controller/                    # 实时数据控制器
│   ├── RealtimeController.java           # 实时数据控制器
│   ├── WebSocketController.java         # WebSocket控制器
│   ├── SSEController.java              # SSE控制器
│   └── RealtimeSubscriptionController.java # 订阅管理控制器
├── service/                      # 实时数据服务层
│   ├── RealtimeService.java              # 实时数据服务
│   ├── WebSocketService.java            # WebSocket服务
│   ├── SSEService.java                   # SSE服务
│   └── SubscriptionService.java         # 订阅管理服务
├── manager/                      # 实时数据管理层
│   ├── RealtimeManager.java              # 实时数据管理器
│   ├── ConnectionManager.java            # 连接管理器
│   ├── MessageQueueManager.java          # 消息队列管理器
│   └── EventPublisherManager.java        # 事件发布管理器
├── dao/                          # 实时数据数据层
│   ├── RealtimeSubscriptionDao.java      # 订阅DAO
│   ├── RealtimeMessageDao.java           # 消息DAO
│   └── RealtimeConfigDao.java            # 配置DAO
├── entity/                       # 实时数据实体
│   ├── RealtimeSubscriptionEntity.java   # 订阅实体
│   ├── RealtimeMessageEntity.java        # 消息实体
│   ├── RealtimeConfigEntity.java         # 配置实体
│   └── ConnectionEntity.java            # 连接实体
├── handler/                      # 消息处理器
│   ├── MessageHandler.java                # 消息处理器接口
│   ├── DeviceStatusHandler.java          # 设备状态处理器
│   ├── AlarmHandler.java                 # 告警处理器
│   └── DataUpdateHandler.java            # 数据更新处理器
├── cache/                        # 缓存管理
│   ├── RealtimeCacheManager.java         # 实时缓存管理器
│   ├── LocalCacheManager.java            # 本地缓存管理器
│   └── DistributedCacheManager.java      # 分布式缓存管理器
└── queue/                        # 消息队列
    ├── RealMessageQueue.java              # 实时消息队列
    ├── EventQueue.java                    # 事件队列
    └── PriorityMessageQueue.java          # 优先级消息队列
```

### 核心设计模式

```java
// 观察者模式 - 事件发布订阅
@Component
public class RealtimeEventPublisher {

    private final Map<String, List<RealtimeEventListener>> listeners = new ConcurrentHashMap<>();

    /**
     * 注册事件监听器
     */
    public void registerListener(String eventType, RealtimeEventListener listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * 发布实时事件
     */
    public void publishEvent(RealtimeEvent event) {
        List<RealtimeEventListener> eventListeners = listeners.get(event.getEventType());
        if (eventListeners != null) {
            eventListeners.forEach(listener -> {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    log.error("处理实时事件失败: {}", event.getEventType(), e);
                }
            });
        }
    }

    /**
     * 异步发布事件
     */
    @Async("realtimeEventExecutor")
    public void publishEventAsync(RealtimeEvent event) {
        publishEvent(event);
    }
}

// 策略模式 - 消息处理器
@Component
public class MessageHandlerFactory {

    private final Map<String, MessageHandler> handlerMap = new ConcurrentHashMap<>();

    public MessageHandlerFactory(List<MessageHandler> handlers) {
        handlers.forEach(handler ->
            handlerMap.put(handler.getSupportedMessageType(), handler));
    }

    /**
     * 获取消息处理器
     */
    public MessageHandler getHandler(String messageType) {
        MessageHandler handler = handlerMap.get(messageType);
        if (handler == null) {
            throw new UnsupportedOperationException("不支持的消息类型: " + messageType);
        }
        return handler;
    }

    /**
     * 处理消息
     */
    public CompletableFuture<Void> handleMessage(RealtimeMessage message) {
        MessageHandler handler = getHandler(message.getMessageType());
        return handler.handle(message);
    }
}

// 工厂模式 - 连接管理器
@Component
public class ConnectionManagerFactory {

    private final Map<ConnectionType, ConnectionManager> managerMap = new ConcurrentHashMap<>();

    public ConnectionManagerFactory(List<ConnectionManager> managers) {
        managers.forEach(manager ->
            managerMap.put(manager.getSupportedConnectionType(), manager));
    }

    /**
     * 获取连接管理器
     */
    public ConnectionManager getManager(ConnectionType connectionType) {
        ConnectionManager manager = managerMap.get(connectionType);
        if (manager == null) {
            throw new UnsupportedOperationException("不支持的连接类型: " + connectionType);
        }
        return manager;
    }
}
```

---

## 🗄️ 数据库设计

### 实时订阅表 (t_realtime_subscription)

```sql
CREATE TABLE t_realtime_subscription (
    subscription_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订阅ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    session_id VARCHAR(100) COMMENT '会话ID',
    connection_type VARCHAR(20) NOT NULL COMMENT '连接类型',
    subscription_topic VARCHAR(200) NOT NULL COMMENT '订阅主题',
    subscription_filter JSON COMMENT '订阅过滤器JSON',
    subscription_params JSON COMMENT '订阅参数JSON',
    status TINYINT DEFAULT 1 COMMENT '状态：1-活跃，0-非活跃',
    last_activity_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后活动时间',
    expire_time DATETIME COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_connection_type (connection_type),
    INDEX idx_subscription_topic (subscription_topic),
    INDEX idx_status (status),
    INDEX idx_last_activity (last_activity_time),
    INDEX idx_expire_time (expire_time)
) COMMENT = '实时订阅表';

-- 连接类型枚举值
INSERT INTO t_sys_dict (dict_type, dict_key, dict_value, sort_order, remark) VALUES
('CONNECTION_TYPE', 'WEBSOCKET', 'WebSocket', 1, 'WebSocket连接'),
('CONNECTION_TYPE', 'SSE', 'Server-Sent Events', 2, 'SSE连接'),
('CONNECTION_TYPE', 'LONG_POLLING', '长轮询', 3, '长轮询连接');
```

### 实时消息表 (t_realtime_message)

```sql
CREATE TABLE t_realtime_message (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    message_type VARCHAR(50) NOT NULL COMMENT '消息类型',
    message_topic VARCHAR(200) NOT NULL COMMENT '消息主题',
    message_data JSON NOT NULL COMMENT '消息数据JSON',
    message_sender_id BIGINT COMMENT '发送者ID',
    message_sender_type VARCHAR(20) COMMENT '发送者类型',
    priority_level TINYINT DEFAULT 1 COMMENT '优先级：1-低，2-中，3-高，4-紧急',
    target_type VARCHAR(20) COMMENT '目标类型',
    target_id BIGINT COMMENT '目标ID',
    message_status TINYINT DEFAULT 0 COMMENT '消息状态：0-待发送，1-已发送，2-发送失败',
    send_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    expire_time DATETIME COMMENT '过期时间',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    max_retry INT DEFAULT 3 COMMENT '最大重试次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_message_type (message_type),
    INDEX idx_message_topic (message_topic),
    INDEX idx_sender_id (message_sender_id),
    INDEX idx_target_type (target_type),
    INDEX idx_target_id (target_id),
    INDEX idx_priority_level (priority_level),
    INDEX idx_message_status (message_status),
    INDEX idx_send_time (send_time),
    INDEX idx_expire_time (expire_time)
) COMMENT = '实时消息表';

-- 消息类型枚举值
INSERT INTO t_sys_dict (dict_type, dict_key, dict_value, sort_order, remark) VALUES
('MESSAGE_TYPE', 'DEVICE_STATUS', '设备状态', 1, '设备状态变更消息'),
('MESSAGE_TYPE', 'ALARM', '告警', 2, '告警消息'),
('MESSAGE_TYPE', 'DATA_UPDATE', '数据更新', 3, '数据更新消息'),
('MESSAGE_TYPE', 'SYSTEM_NOTIFICATION', '系统通知', 4, '系统通知消息'),
('MESSAGE_TYPE', 'USER_NOTIFICATION', '用户通知', 5, '用户通知消息');
```

### 连接信息表 (t_realtime_connection)

```sql
CREATE TABLE t_realtime_connection (
    connection_id VARCHAR(100) PRIMARY KEY COMMENT '连接ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    session_id VARCHAR(100) COMMENT '会话ID',
    connection_type VARCHAR(20) NOT NULL COMMENT '连接类型',
    client_info JSON COMMENT '客户端信息JSON',
    server_info JSON COMMENT '服务器信息JSON',
    connection_ip VARCHAR(50) COMMENT '连接IP',
    connection_port INT COMMENT '连接端口',
    connect_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '连接时间',
    last_heartbeat_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后心跳时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1-已连接，0-已断开',
    disconnect_time DATETIME COMMENT '断开时间',
    disconnect_reason VARCHAR(200) COMMENT '断开原因',
    total_messages_sent INT DEFAULT 0 COMMENT '总发送消息数',
    total_bytes_sent BIGINT DEFAULT 0 COMMENT '总发送字节数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_connection_type (connection_type),
    INDEX idx_status (status),
    INDEX idx_connect_time (connect_time),
    INDEX idx_last_heartbeat (last_heartbeat_time)
) COMMENT = '连接信息表';
```

### 实时配置表 (t_realtime_config)

```sql
CREATE TABLE t_realtime_config (
    config_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_type VARCHAR(50) NOT NULL COMMENT '配置类型',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_description TEXT COMMENT '配置描述',
    is_encrypted TINYINT DEFAULT 0 COMMENT '是否加密：1-是，0-否',
    config_version VARCHAR(20) DEFAULT '1.0' COMMENT '配置版本',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_config (config_type, config_key),
    INDEX idx_config_type (config_type),
    INDEX idx_status (status),
    INDEX idx_config_version (config_version)
) COMMENT = '实时配置表';

-- 默认实时配置
INSERT INTO t_realtime_config (config_type, config_key, config_value, config_description) VALUES
('WEBSOCKET', 'max_connections', '10000', 'WebSocket最大连接数'),
('WEBSOCKET', 'heartbeat_interval', '30', '心跳间隔(秒)'),
('WEBSOCKET', 'max_message_size', '1048576', '最大消息大小(字节)'),
('SSE', 'max_connections', '5000', 'SSE最大连接数'),
('SSE', 'heartbeat_interval', '60', '心跳间隔(秒)'),
('SSE', 'reconnect_timeout', '300', '重连超时(秒)'),
('CACHE', 'local_cache_size', '10000', '本地缓存大小'),
('CACHE', 'distributed_cache_expire', '300', '分布式缓存过期时间(秒)'),
('QUEUE', 'max_queue_size', '100000', '最大队列大小'),
('QUEUE', 'consumer_threads', '10', '消费者线程数');
```

---

## 🔧 后端实现

### 核心控制器 (RealtimeController)

```java
@RestController
@RequestMapping("/api/realtime")
@Tag(name = "实时数据", description = "实时数据相关接口")
public class RealtimeController {

    @Resource
    private RealtimeService realtimeService;

    @PostMapping("/subscribe")
    @Operation(summary = "订阅实时数据")
    @SaCheckLogin
    public ResponseDTO<RealtimeSubscriptionVO> subscribe(@Valid @RequestBody RealtimeSubscriptionDTO subscriptionDTO) {
        Long userId = SmartRequestUtil.getCurrentUserId();
        RealtimeSubscriptionVO subscription = realtimeService.subscribe(userId, subscriptionDTO);
        return ResponseDTO.ok(subscription);
    }

    @PostMapping("/unsubscribe")
    @Operation(summary = "取消订阅")
    @SaCheckLogin
    public ResponseDTO<String> unsubscribe(@RequestParam String subscriptionId) {
        Long userId = SmartRequestUtil.getCurrentUserId();
        realtimeService.unsubscribe(userId, subscriptionId);
        return ResponseDTO.ok();
    }

    @PostMapping("/publish")
    @Operation(summary = "发布实时消息")
    @SaCheckPermission("realtime:publish")
    public ResponseDTO<String> publish(@Valid @RequestBody RealtimeMessageDTO messageDTO) {
        Long userId = SmartRequestUtil.getCurrentUserId();
        realtimeService.publish(userId, messageDTO);
        return ResponseDTO.ok();
    }

    @GetMapping("/connections")
    @Operation(summary = "获取连接信息")
    @SaCheckPermission("realtime:connections")
    public ResponseDTO<List<RealtimeConnectionVO>> getConnections(@RequestParam(required = false) String connectionType) {
        List<RealtimeConnectionVO> connections = realtimeService.getConnections(connectionType);
        return ResponseDTO.ok(connections);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取实时统计")
    @SaCheckPermission("realtime:statistics")
    public ResponseDTO<RealtimeStatisticsVO> getStatistics() {
        RealtimeStatisticsVO statistics = realtimeService.getStatistics();
        return ResponseDTO.ok(statistics);
    }

    @PostMapping("/broadcast")
    @Operation(summary = "广播消息")
    @SaCheckPermission("realtime:broadcast")
    public ResponseDTO<String> broadcast(@Valid @RequestBody RealtimeBroadcastDTO broadcastDTO) {
        Long userId = SmartRequestUtil.getCurrentUserId();
        realtimeService.broadcast(userId, broadcastDTO);
        return ResponseDTO.ok();
    }

    @GetMapping("/subscriptions")
    @Operation(summary = "获取订阅信息")
    @SaCheckLogin
    public ResponseDTO<List<RealtimeSubscriptionVO>> getSubscriptions() {
        Long userId = SmartRequestUtil.getCurrentUserId();
        List<RealtimeSubscriptionVO> subscriptions = realtimeService.getUserSubscriptions(userId);
        return ResponseDTO.ok(subscriptions);
    }
}
```

### WebSocket控制器 (WebSocketController)

```java
@RestController
@RequestMapping("/ws/realtime")
@Tag(name = "WebSocket", description = "WebSocket实时通信")
public class WebSocketController {

    @Resource
    private WebSocketService webSocketService;

    @GetMapping("/device/{deviceId}")
    public String deviceWebSocket(@PathVariable Long deviceId,
                                 @RequestParam String token,
                                 HttpServletRequest request) {
        // 验证token
        if (!validateToken(token)) {
            return "redirect:/error/401";
        }

        // 建立WebSocket连接
        return webSocketService.connectDeviceWebSocket(deviceId, token, request);
    }

    @GetMapping("/user")
    public String userWebSocket(@RequestParam String token,
                                HttpServletRequest request) {
        // 验证token
        if (!validateToken(token)) {
            return "redirect:/error/401";
        }

        // 建立WebSocket连接
        return webSocketService.connectUserWebSocket(token, request);
    }

    @GetMapping("/system")
    public String systemWebSocket(@RequestParam String token,
                                 @RequestParam String channel,
                                 HttpServletRequest request) {
        // 验证token
        if (!validateToken(token)) {
            return "redirect:/error/401";
        }

        // 建立WebSocket连接
        return webSocketService.connectSystemWebSocket(token, channel, request);
    }

    private boolean validateToken(String token) {
        try {
            // 使用Sa-Token验证token
            return StpUtil.getLoginIdByToken(token) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 核心服务层 (RealtimeService)

```java
@Service
@Transactional(readOnly = true)
public class RealtimeService {

    @Resource
    private RealtimeManager realtimeManager;
    @Resource
    private ConnectionManagerFactory connectionManagerFactory;
    @Resource
    private MessageHandlerFactory messageHandlerFactory;
    @Resource
    private RealtimeEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public RealtimeSubscriptionVO subscribe(Long userId, RealtimeSubscriptionDTO subscriptionDTO) {
        // 1. 验证订阅参数
        validateSubscription(subscriptionDTO);

        // 2. 创建订阅记录
        RealtimeSubscriptionEntity subscription = RealtimeSubscriptionEntity.builder()
            .userId(userId)
            .sessionId(generateSessionId())
            .connectionType(subscriptionDTO.getConnectionType())
            .subscriptionTopic(subscriptionDTO.getTopic())
            .subscriptionFilter(JsonUtils.toJsonString(subscriptionDTO.getFilter()))
            .subscriptionParams(JsonUtils.toJsonString(subscriptionDTO.getParams()))
            .status(1)
            .expireTime(calculateExpireTime(subscriptionDTO))
            .build();

        realtimeManager.saveSubscription(subscription);

        // 3. 注册事件监听器
        registerEventListeners(subscription);

        // 4. 发布订阅事件
        publishSubscriptionEvent(userId, subscription, "SUBSCRIBE");

        return convertToVO(subscription);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unsubscribe(Long userId, String subscriptionId) {
        // 1. 验证订阅存在性
        RealtimeSubscriptionEntity subscription = realtimeManager.getSubscription(subscriptionId);
        if (subscription == null || !subscription.getUserId().equals(userId)) {
            throw new SmartException("订阅不存在或无权限");
        }

        // 2. 更新订阅状态
        realtimeManager.updateSubscriptionStatus(subscriptionId, 0);

        // 3. 取消事件监听器
        unregisterEventListeners(subscription);

        // 4. 发布取消订阅事件
        publishSubscriptionEvent(userId, subscription, "UNSUBSCRIBE");
    }

    @Transactional(rollbackFor = Exception.class)
    public void publish(Long userId, RealtimeMessageDTO messageDTO) {
        // 1. 验证消息数据
        validateMessage(messageDTO);

        // 2. 创建消息实体
        RealtimeMessageEntity message = RealtimeMessageEntity.builder()
            .messageType(messageDTO.getMessageType())
            .messageTopic(messageDTO.getTopic())
            .messageData(JsonUtils.toJsonString(messageDTO.getData()))
            .messageSenderId(userId)
            .messageSenderType("USER")
            .priorityLevel(messageDTO.getPriorityLevel())
            .targetType(messageDTO.getTargetType())
            .targetId(messageDTO.getTargetId())
            .messageStatus(0)
            .expireTime(calculateMessageExpireTime(messageDTO))
            .maxRetry(messageDTO.getMaxRetry())
            .build();

        realtimeManager.saveMessage(message);

        // 3. 异步处理消息
        CompletableFuture.runAsync(() -> {
            try {
                processMessage(message);
            } catch (Exception e) {
                log.error("处理实时消息失败", e);
                updateMessageStatus(message.getMessageId(), 2); // 发送失败
            }
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void broadcast(Long userId, RealtimeBroadcastDTO broadcastDTO) {
        // 1. 验证广播权限
        validateBroadcastPermission(userId, broadcastDTO);

        // 2. 获取目标订阅者
        List<RealtimeSubscriptionEntity> targetSubscriptions = realtimeManager
            .getActiveSubscriptionsByTopic(broadcastDTO.getTopic());

        // 3. 批量创建消息
        List<RealtimeMessageEntity> messages = targetSubscriptions.stream()
            .map(subscription -> RealtimeMessageEntity.builder()
                .messageType(broadcastDTO.getMessageType())
                .messageTopic(broadcastDTO.getTopic())
                .messageData(JsonUtils.toJsonString(broadcastDTO.getData()))
                .messageSenderId(userId)
                .messageSenderType("USER")
                .priorityLevel(broadcastDTO.getPriorityLevel())
                .targetType("SUBSCRIPTION")
                .targetId(subscription.getSubscriptionId())
                .messageStatus(0)
                .expireTime(calculateMessageExpireTime(broadcastDTO))
                .build())
            .collect(Collectors.toList());

        realtimeManager.batchSaveMessages(messages);

        // 4. 异步批量处理消息
        CompletableFuture.runAsync(() -> {
            messages.forEach(message -> {
                try {
                    processMessage(message);
                } catch (Exception e) {
                    log.error("处理广播消息失败: {}", message.getMessageId(), e);
                    updateMessageStatus(message.getMessageId(), 2);
                }
            });
        });
    }

    public List<RealtimeConnectionVO> getConnections(String connectionType) {
        List<ConnectionEntity> connections = realtimeManager.getActiveConnections(connectionType);
        return connections.stream()
            .map(this::convertConnectionToVO)
            .collect(Collectors.toList());
    }

    public RealtimeStatisticsVO getStatistics() {
        // 获取连接统计
        Map<String, Long> connectionStats = realtimeManager.getConnectionStatistics();

        // 获取消息统计
        Map<String, Long> messageStats = realtimeManager.getMessageStatistics();

        // 获取订阅统计
        Map<String, Long> subscriptionStats = realtimeManager.getSubscriptionStatistics();

        return RealtimeStatisticsVO.builder()
            .totalConnections(connectionStats.getOrDefault("total", 0L))
            .websocketConnections(connectionStats.getOrDefault("WEBSOCKET", 0L))
            .sseConnections(connectionStats.getOrDefault("SSE", 0L))
            .totalMessages(messageStats.getOrDefault("total", 0L))
            .messagesSent(messageStats.getOrDefault("sent", 0L))
            .messagesFailed(messageStats.getOrDefault("failed", 0L))
            .totalSubscriptions(subscriptionStats.getOrDefault("total", 0L))
            .activeSubscriptions(subscriptionStats.getOrDefault("active", 0L))
            .statisticsTime(LocalDateTime.now())
            .build();
    }

    public List<RealtimeSubscriptionVO> getUserSubscriptions(Long userId) {
        List<RealtimeSubscriptionEntity> subscriptions = realtimeManager.getUserSubscriptions(userId);
        return subscriptions.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    private void processMessage(RealtimeMessageEntity message) {
        try {
            // 1. 更新消息状态为发送中
            updateMessageStatus(message.getMessageId(), 1);

            // 2. 获取消息处理器
            MessageHandler handler = messageHandlerFactory.getHandler(message.getMessageType());

            // 3. 处理消息
            CompletableFuture<Void> handleResult = handler.handle(convertToMessageDTO(message));

            // 4. 处理完成后的回调
            handleResult.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("消息处理失败: {}", message.getMessageId(), throwable);
                    handleFailedMessage(message);
                } else {
                    updateMessageStatus(message.getMessageId(), 1); // 发送成功
                }
            });

        } catch (Exception e) {
            log.error("处理消息异常: {}", message.getMessageId(), e);
            handleFailedMessage(message);
        }
    }

    private void handleFailedMessage(RealtimeMessageEntity message) {
        // 1. 增加重试次数
        message.setRetryCount(message.getRetryCount() + 1);

        // 2. 检查是否还能重试
        if (message.getRetryCount() < message.getMaxRetry()) {
            // 延迟重试
            long delay = calculateRetryDelay(message.getRetryCount());
            CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS).execute(() -> {
                processMessage(message);
            });
        } else {
            // 标记为发送失败
            updateMessageStatus(message.getMessageId(), 2);
        }
    }

    private void validateSubscription(RealtimeSubscriptionDTO subscriptionDTO) {
        if (StringUtils.isBlank(subscriptionDTO.getTopic())) {
            throw new SmartException("订阅主题不能为空");
        }

        if (StringUtils.isBlank(subscriptionDTO.getConnectionType())) {
            throw new SmartException("连接类型不能为空");
        }
    }

    private void validateMessage(RealtimeMessageDTO messageDTO) {
        if (StringUtils.isBlank(messageDTO.getMessageType())) {
            throw new SmartException("消息类型不能为空");
        }

        if (StringUtils.isBlank(messageDTO.getTopic())) {
            throw new SmartException("消息主题不能为空");
        }

        if (messageDTO.getData() == null) {
            throw new SmartException("消息数据不能为空");
        }
    }

    private void validateBroadcastPermission(Long userId, RealtimeBroadcastDTO broadcastDTO) {
        // 检查用户是否有广播权限
        boolean hasPermission = permissionService.hasPermission(userId, "realtime:broadcast");
        if (!hasPermission) {
            throw new SmartException("无广播权限");
        }

        // 检查主题权限
        if (!isTopicAllowed(userId, broadcastDTO.getTopic())) {
            throw new SmartException("无该主题的广播权限");
        }
    }

    private boolean isTopicAllowed(Long userId, String topic) {
        // 实现主题权限检查逻辑
        return true; // 简化实现
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private LocalDateTime calculateExpireTime(RealtimeSubscriptionDTO subscriptionDTO) {
        Integer expireMinutes = subscriptionDTO.getExpireMinutes();
        return expireMinutes != null && expireMinutes > 0
            ? LocalDateTime.now().plusMinutes(expireMinutes)
            : null;
    }

    private LocalDateTime calculateMessageExpireTime(RealtimeMessageDTO messageDTO) {
        Integer expireMinutes = messageDTO.getExpireMinutes();
        return expireMinutes != null && expireMinutes > 0
            ? LocalDateTime.now().plusMinutes(expireMinutes)
            : null;
    }

    private long calculateRetryDelay(int retryCount) {
        // 指数退避算法
        return Math.min(1000 * (1L << retryCount), 60000); // 最大1分钟
    }

    private void updateMessageStatus(Long messageId, int status) {
        RealtimeMessageEntity updateEntity = new RealtimeMessageEntity();
        updateEntity.setMessageId(messageId);
        updateEntity.setMessageStatus(status);
        updateEntity.setUpdateTime(LocalDateTime.now());

        realtimeManager.updateMessage(updateEntity);
    }

    private void registerEventListeners(RealtimeSubscriptionEntity subscription) {
        // 注册事件监听器
        RealtimeEventListener listener = new RealtimeEventListener() {
            @Override
            public void onEvent(RealtimeEvent event) {
                // 检查事件是否匹配订阅过滤器
                if (matchesSubscriptionFilter(subscription, event)) {
                    // 发送事件到订阅者
                    sendEventToSubscriber(subscription, event);
                }
            }
        };

        eventPublisher.registerListener(subscription.getSubscriptionTopic(), listener);
    }

    private void unregisterEventListeners(RealtimeSubscriptionEntity subscription) {
        // 取消事件监听器
        // 这里需要实现取消注册的逻辑
    }

    private boolean matchesSubscriptionFilter(RealtimeSubscriptionEntity subscription, RealtimeEvent event) {
        try {
            if (StringUtils.isBlank(subscription.getSubscriptionFilter())) {
                return true; // 无过滤器，匹配所有事件
            }

            // 解析过滤器并匹配
            Map<String, Object> filter = JsonUtils.parseObject(
                subscription.getSubscriptionFilter(), Map.class);

            return EventFilterUtils.matches(filter, event);
        } catch (Exception e) {
            log.error("匹配订阅过滤器失败", e);
            return true; // 出错时默认匹配
        }
    }

    private void sendEventToSubscriber(RealtimeSubscriptionEntity subscription, RealtimeEvent event) {
        // 获取连接管理器
        ConnectionManager manager = connectionManagerFactory.getManager(
            ConnectionType.valueOf(subscription.getConnectionType()));

        // 发送事件
        manager.sendToSubscription(subscription.getSubscriptionId(), event);
    }

    private void publishSubscriptionEvent(Long userId, RealtimeSubscriptionEntity subscription, String action) {
        RealtimeEvent event = RealtimeEvent.builder()
            .eventType("SUBSCRIPTION_" + action)
            .eventData(Map.of(
                "userId", userId,
                "subscriptionId", subscription.getSubscriptionId(),
                "topic", subscription.getSubscriptionTopic(),
                "connectionType", subscription.getConnectionType(),
                "action", action
            ))
            .eventTime(LocalDateTime.now())
            .build();

        eventPublisher.publishEvent(event);
    }

    private RealtimeSubscriptionVO convertToVO(RealtimeSubscriptionEntity entity) {
        RealtimeSubscriptionVO vo = new RealtimeSubscriptionVO();
        BeanUtil.copyProperties(entity, vo);
        return vo;
    }

    private RealtimeConnectionVO convertConnectionToVO(ConnectionEntity entity) {
        RealtimeConnectionVO vo = new RealtimeConnectionVO();
        BeanUtil.copyProperties(entity, vo);
        return vo;
    }

    private RealtimeMessageDTO convertToMessageDTO(RealtimeMessageEntity entity) {
        return RealtimeMessageDTO.builder()
            .messageId(entity.getMessageId())
            .messageType(entity.getMessageType())
            .topic(entity.getMessageTopic())
            .data(JsonUtils.parseObject(entity.getMessageData(), Map.class))
            .senderId(entity.getMessageSenderId())
            .senderType(entity.getMessageSenderType())
            .priorityLevel(entity.getPriorityLevel())
            .targetType(entity.getTargetType())
            .targetId(entity.getTargetId())
            .build();
    }
}
```

### WebSocket服务实现

```java
@Service
public class WebSocketService {

    @Resource
    private ConnectionManager connectionManager;
    @Resource
    private RealtimeSubscriptionDao subscriptionDao;
    @Resource
    private SaTokenConfig saTokenConfig;

    // WebSocket连接池
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 设备WebSocket连接
     */
    public String connectDeviceWebSocket(Long deviceId, String token, HttpServletRequest request) {
        try {
            // 1. 验证token并获取用户信息
            LoginUser loginUser = StpUtil.getLoginByToken(token);
            if (loginUser == null) {
                throw new SmartException("token无效");
            }

            // 2. 验证设备权限
            if (!validateDevicePermission(loginUser.getId(), deviceId)) {
                throw new SmartException("无设备访问权限");
            }

            // 3. 创建连接ID
            String connectionId = generateConnectionId();

            // 4. 保存连接信息
            ConnectionEntity connection = ConnectionEntity.builder()
                .connectionId(connectionId)
                .userId(loginUser.getId())
                .connectionType("WEBSOCKET")
                .clientInfo(buildClientInfo(request))
                .serverInfo(buildServerInfo())
                .connectionIp(getClientIpAddress(request))
                .connectTime(LocalDateTime.now())
                .lastHeartbeatTime(LocalDateTime.now())
                .status(1)
                .build();

            connectionManager.saveConnection(connection);

            // 5. 返回连接页面
            return forwardToWebSocketPage(connectionId, "DEVICE", deviceId);

        } catch (Exception e) {
            log.error("设备WebSocket连接失败", e);
            throw new SmartException("连接失败: " + e.getMessage());
        }
    }

    /**
     * 用户WebSocket连接
     */
    public String connectUserWebSocket(String token, HttpServletRequest request) {
        try {
            // 1. 验证token并获取用户信息
            LoginUser loginUser = StpUtil.getLoginByToken(token);
            if (loginUser == null) {
                throw new SmartException("token无效");
            }

            // 2. 创建连接ID
            String connectionId = generateConnectionId();

            // 3. 保存连接信息
            ConnectionEntity connection = ConnectionEntity.builder()
                .connectionId(connectionId)
                .userId(loginUser.getId())
                .connectionType("WEBSOCKET")
                .clientInfo(buildClientInfo(request))
                .serverInfo(buildServerInfo())
                .connectionIp(getClientIpAddress(request))
                .connectTime(LocalDateTime.now())
                .lastHeartbeatTime(LocalDateTime.now())
                .status(1)
                .build();

            connectionManager.saveConnection(connection);

            // 4. 返回连接页面
            return forwardToWebSocketPage(connectionId, "USER", loginUser.getId());

        } catch (Exception e) {
            log.error("用户WebSocket连接失败", e);
            throw new SmartException("连接失败: " + e.getMessage());
        }
    }

    /**
     * 系统WebSocket连接
     */
    public String connectSystemWebSocket(String token, String channel, HttpServletRequest request) {
        try {
            // 1. 验证token并获取用户信息
            LoginUser loginUser = StpUtil.getLoginByToken(token);
            if (loginUser == null) {
                throw new SmartException("token无效");
            }

            // 2. 验证系统权限
            if (!validateSystemPermission(loginUser.getId(), channel)) {
                throw new SmartException("无系统频道访问权限");
            }

            // 3. 创建连接ID
            String connectionId = generateConnectionId();

            // 4. 保存连接信息
            ConnectionEntity connection = ConnectionEntity.builder()
                .connectionId(connectionId)
                .userId(loginUser.getId())
                .connectionType("WEBSOCKET")
                .clientInfo(buildClientInfo(request))
                .serverInfo(buildServerInfo())
                .connectionIp(getClientIpAddress(request))
                .connectTime(LocalDateTime.now())
                .lastHeartbeatTime(LocalDateTime.now())
                .status(1)
                .build();

            connectionManager.saveConnection(connection);

            // 5. 返回连接页面
            return forwardToWebSocketPage(connectionId, "SYSTEM", channel);

        } catch (Exception e) {
            log.error("系统WebSocket连接失败", e);
            throw new SmartException("连接失败: " + e.getMessage());
        }
    }

    /**
     * 发送消息到WebSocket连接
     */
    public boolean sendToConnection(String connectionId, Object message) {
        WebSocketSession session = sessions.get(connectionId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(JsonUtils.toJsonString(message)));
                return true;
            } catch (Exception e) {
                log.error("发送WebSocket消息失败: {}", connectionId, e);
                return false;
            }
        }
        return false;
    }

    /**
     * 广播消息到所有连接
     */
    public void broadcast(Object message) {
        String messageJson = JsonUtils.toJsonString(message);
        sessions.values().parallelStream().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(messageJson));
                } catch (Exception e) {
                    log.error("广播消息失败", e);
                }
            }
        });
    }

    /**
     * 处理WebSocket连接建立
     */
    @EventListener
    public void handleWebSocketConnect(WebSocketConnectEvent event) {
        String connectionId = event.getConnectionId();
        WebSocketSession session = event.getSession();

        // 保存会话
        sessions.put(connectionId, session);

        // 更新连接状态
        connectionManager.updateConnectionStatus(connectionId, 1);

        log.info("WebSocket连接建立: {}", connectionId);
    }

    /**
     * 处理WebSocket连接断开
     */
    @EventListener
    public void handleWebSocketDisconnect(WebSocketDisconnectEvent event) {
        String connectionId = event.getConnectionId();

        // 移除会话
        sessions.remove(connectionId);

        // 更新连接状态
        connectionManager.updateConnectionStatus(connectionId, 0);

        log.info("WebSocket连接断开: {}", connectionId);
    }

    /**
     * 处理WebSocket消息
     */
    @EventListener
    public void handleWebSocketMessage(WebSocketMessageEvent event) {
        String connectionId = event.getConnectionId();
        String message = event.getMessage();

        try {
            // 解析消息
            WebSocketMessage wsMessage = JsonUtils.parseObject(message, WebSocketMessage.class);

            // 处理不同类型的消息
            switch (wsMessage.getType()) {
                case "HEARTBEAT":
                    handleHeartbeat(connectionId);
                    break;
                case "SUBSCRIBE":
                    handleSubscribe(connectionId, wsMessage.getData());
                    break;
                case "UNSUBSCRIBE":
                    handleUnsubscribe(connectionId, wsMessage.getData());
                    break;
                default:
                    log.warn("未知的WebSocket消息类型: {}", wsMessage.getType());
            }

        } catch (Exception e) {
            log.error("处理WebSocket消息失败: {}", connectionId, e);
        }
    }

    private void handleHeartbeat(String connectionId) {
        // 更新心跳时间
        connectionManager.updateHeartbeatTime(connectionId);

        // 发送心跳响应
        WebSocketSession session = sessions.get(connectionId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(JsonUtils.toJsonString(
                    WebSocketMessage.builder()
                        .type("HEARTBEAT_RESPONSE")
                        .timestamp(System.currentTimeMillis())
                        .build()
                )));
            } catch (Exception e) {
                log.error("发送心跳响应失败", e);
            }
        }
    }

    private void handleSubscribe(String connectionId, Object data) {
        try {
            // 解析订阅数据
            Map<String, Object> subscribeData = (Map<String, Object>) data;
            String topic = (String) subscribeData.get("topic");
            Map<String, Object> filter = (Map<String, Object>) subscribeData.get("filter");

            // 获取连接信息
            ConnectionEntity connection = connectionManager.getConnection(connectionId);
            if (connection != null) {
                // 创建订阅
                RealtimeSubscriptionEntity subscription = RealtimeSubscriptionEntity.builder()
                    .userId(connection.getUserId())
                    .sessionId(connection.getSessionId())
                    .connectionType("WEBSOCKET")
                    .subscriptionTopic(topic)
                    .subscriptionFilter(JsonUtils.toJsonString(filter))
                    .status(1)
                    .build();

                subscriptionDao.insert(subscription);

                log.info("WebSocket订阅成功: {} -> {}", connectionId, topic);
            }

        } catch (Exception e) {
            log.error("处理WebSocket订阅失败: {}", connectionId, e);
        }
    }

    private void handleUnsubscribe(String connectionId, Object data) {
        try {
            // 解析取消订阅数据
            Map<String, Object> unsubscribeData = (Map<String, Object>) data;
            String topic = (String) unsubscribeData.get("topic");

            // 获取连接信息
            ConnectionEntity connection = connectionManager.getConnection(connectionId);
            if (connection != null) {
                // 更新订阅状态
                subscriptionDao.update(null,
                    new UpdateWrapper<RealtimeSubscriptionEntity>()
                        .eq("user_id", connection.getUserId())
                        .eq("subscription_topic", topic)
                        .eq("status", 1)
                        .set("status", 0)
                        .set("update_time", LocalDateTime.now())
                );

                log.info("WebSocket取消订阅成功: {} -> {}", connectionId, topic);
            }

        } catch (Exception e) {
            log.error("处理WebSocket取消订阅失败: {}", connectionId, e);
        }
    }

    private boolean validateDevicePermission(Long userId, Long deviceId) {
        // 实现设备权限验证逻辑
        return deviceService.hasDeviceAccess(userId, deviceId);
    }

    private boolean validateSystemPermission(Long userId, String channel) {
        // 实现系统权限验证逻辑
        return permissionService.hasPermission(userId, "system:channel:" + channel);
    }

    private String generateConnectionId() {
        return "ws_" + System.currentTimeMillis() + "_" + ThreadLocalRandom.current().nextInt(1000);
    }

    private String buildClientInfo(HttpServletRequest request) {
        Map<String, Object> clientInfo = new HashMap<>();
        clientInfo.put("userAgent", request.getHeader("User-Agent"));
        clientInfo.put("origin", request.getHeader("Origin"));
        clientInfo.put("referer", request.getHeader("Referer"));
        return JsonUtils.toJsonString(clientInfo);
    }

    private String buildServerInfo() {
        Map<String, Object> serverInfo = new HashMap<>();
        serverInfo.put("serverName", getServerName());
        serverInfo.put("serverIp", getServerIp());
        serverInfo.put("serverTime", LocalDateTime.now().toString());
        return JsonUtils.toJsonString(serverInfo);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String forwardToWebSocketPage(String connectionId, String type, Object target) {
        // 返回WebSocket连接页面的路径
        return String.format("/forward:/websocket/connect?connectionId=%s&type=%s&target=%s",
            connectionId, type, target);
    }
}
```

---

## 🎨 前端实现

### WebSocket Hook (useWebSocket)

```javascript
// /composables/useWebSocket.js
import { ref, onMounted, onUnmounted } from 'vue'
import { useLocationStore } from '/@/store/location'
import { notification } from 'ant-design-vue'

export function useWebSocket(url, options = {}) {
  const {
    reconnect = true,
    reconnectInterval = 3000,
    maxReconnectAttempts = 5,
    heartbeatInterval = 30000,
    onMessage,
    onConnect,
    onDisconnect,
    onError
  } = options

  const isConnected = ref(false)
  const isReconnecting = ref(false)
  const reconnectAttempts = ref(0)
  const ws = ref(null)
  const heartbeatTimer = ref(null)

  // 连接WebSocket
  const connect = () => {
    try {
      // 构建WebSocket URL
      const wsUrl = buildWebSocketUrl(url)

      // 创建WebSocket连接
      ws.value = new WebSocket(wsUrl)

      // 设置事件处理器
      ws.value.onopen = handleOpen
      ws.value.onmessage = handleMessage
      ws.value.onclose = handleClose
      ws.value.onerror = handleError

    } catch (error) {
      console.error('WebSocket连接失败:', error)
      handleConnectionError(error)
    }
  }

  // 断开连接
  const disconnect = () => {
    if (ws.value) {
      ws.value.close()
      ws.value = null
    }
    clearHeartbeat()
  }

  // 发送消息
  const send = (message) => {
    if (ws.value && ws.value.readyState === WebSocket.OPEN) {
      try {
        const messageStr = typeof message === 'string' ? message : JSON.stringify(message)
        ws.value.send(messageStr)
        return true
      } catch (error) {
        console.error('发送WebSocket消息失败:', error)
        return false
      }
    }
    return false
  }

  // 订阅主题
  const subscribe = (topic, filter = {}) => {
    const message = {
      type: 'SUBSCRIBE',
      data: {
        topic,
        filter,
        timestamp: Date.now()
      }
    }
    return send(message)
  }

  // 取消订阅
  const unsubscribe = (topic) => {
    const message = {
      type: 'UNSUBSCRIBE',
      data: {
        topic,
        timestamp: Date.now()
      }
    }
    return send(message)
  }

  // 事件处理器
  const handleOpen = (event) => {
    console.log('WebSocket连接已建立')
    isConnected.value = true
    isReconnecting.value = false
    reconnectAttempts.value = 0

    // 开始心跳
    startHeartbeat()

    // 调用连接回调
    if (onConnect) {
      onConnect(event)
    }
  }

  const handleMessage = (event) => {
    try {
      const message = JSON.parse(event.data)

      // 处理心跳响应
      if (message.type === 'HEARTBEAT_RESPONSE') {
        return
      }

      // 调用消息回调
      if (onMessage) {
        onMessage(message)
      }

      // 处理特定类型的消息
      handleSpecialMessage(message)

    } catch (error) {
      console.error('解析WebSocket消息失败:', error)
    }
  }

  const handleClose = (event) => {
    console.log('WebSocket连接已关闭')
    isConnected.value = false
    clearHeartbeat()

    // 调用断开回调
    if (onDisconnect) {
      onDisconnect(event)
    }

    // 自动重连
    if (reconnect && !isReconnecting.value && reconnectAttempts.value < maxReconnectAttempts) {
      scheduleReconnect()
    }
  }

  const handleError = (error) => {
    console.error('WebSocket连接错误:', error)
    isConnected.value = false

    // 调用错误回调
    if (onError) {
      onError(error)
    }
  }

  const handleConnectionError = (error) => {
    console.error('WebSocket连接异常:', error)

    // 显示错误通知
    notification.error({
      message: 'WebSocket连接失败',
      description: error.message || '网络连接异常',
      duration: 0
    })
  }

  const handleSpecialMessage = (message) => {
    switch (message.type) {
      case 'DEVICE_STATUS':
        handleDeviceStatusMessage(message)
        break
      case 'ALARM':
        handleAlarmMessage(message)
        break
      case 'LOCATION_UPDATE':
        handleLocationUpdateMessage(message)
        break
      case 'SYSTEM_NOTIFICATION':
        handleSystemNotificationMessage(message)
        break
      default:
        // 未知消息类型，记录日志
        console.log('未处理的WebSocket消息:', message)
    }
  }

  const handleDeviceStatusMessage = (message) => {
    const locationStore = useLocationStore()
    locationStore.updateDeviceStatus(message.data.deviceId, {
      status: message.data.status,
      isOnline: message.data.isOnline,
      lastHeartbeatTime: new Date(message.data.timestamp)
    })

    // 显示设备状态变更通知
    notification.info({
      message: '设备状态变更',
      description: `设备${message.data.deviceName}状态变更为${message.data.isOnline ? '在线' : '离线'}`,
      duration: 3
    })
  }

  const handleAlarmMessage = (message) => {
    // 显示告警通知
    const alarmData = message.data
    const notificationType = alarmData.level === 3 ? 'error' : 'warning'

    notification[notificationType]({
      message: alarmData.alarmType,
      description: alarmData.alarmMessage,
      duration: 0,
      onClick: () => {
        // 跳转到告警详情页面
        router.push(`/alarm/detail/${alarmData.alarmId}`)
      }
    })
  }

  const handleLocationUpdateMessage = (message) => {
    const locationStore = useLocationStore()
    locationStore.updateDeviceLocation(message.data.deviceId, {
      latitude: message.data.latitude,
      longitude: message.data.longitude,
      timestamp: new Date(message.data.timestamp)
    })
  }

  const handleSystemNotificationMessage = (message) => {
    const notificationData = message.data
    const notificationType = notificationData.type === 'SUCCESS' ? 'success' :
                           notificationData.type === 'WARNING' ? 'warning' :
                           notificationData.type === 'ERROR' ? 'error' : 'info'

    notification[notificationType]({
      message: notificationData.title,
      description: notificationData.content,
      duration: notificationData.duration || 4.5
    })
  }

  // 心跳相关方法
  const startHeartbeat = () => {
    heartbeatTimer.value = setInterval(() => {
      if (ws.value && ws.value.readyState === WebSocket.OPEN) {
        send({
          type: 'HEARTBEAT',
          timestamp: Date.now()
        })
      }
    }, heartbeatInterval)
  }

  const clearHeartbeat = () => {
    if (heartbeatTimer.value) {
      clearInterval(heartbeatTimer.value)
      heartbeatTimer.value = null
    }
  }

  // 重连相关方法
  const scheduleReconnect = () => {
    isReconnecting.value = true
    reconnectAttempts.value++

    console.log(`WebSocket重连中... (${reconnectAttempts.value}/${maxReconnectAttempts})`)

    setTimeout(() => {
      connect()
    }, reconnectInterval)
  }

  const buildWebSocketUrl = (wsUrl) => {
    // 如果是相对路径，构建完整的WebSocket URL
    if (wsUrl.startsWith('/')) {
      const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      const host = window.location.host
      return `${protocol}//${host}${wsUrl}`
    }
    return wsUrl
  }

  // 生命周期
  onMounted(() => {
    connect()
  })

  onUnmounted(() => {
    disconnect()
  })

  return {
    isConnected,
    isReconnecting,
    reconnectAttempts,
    connect,
    disconnect,
    send,
    subscribe,
    unsubscribe
  }
}
```

### 实时数据组件 (RealtimeDataCard)

```vue
<template>
  <a-card
    class="realtime-data-card"
    :title="title"
    :bordered="false"
    :loading="loading"
  >
    <template #extra>
      <a-space>
        <a-tag :color="connectionStatus.color">
          <template #icon>
            <component :is="connectionStatus.icon" />
          </template>
          {{ connectionStatus.text }}
        </a-tag>
        <a-button
          size="small"
          :type="isConnected ? 'default' : 'primary'"
          @click="toggleConnection"
        >
          {{ isConnected ? '断开' : '连接' }}
        </a-button>
      </a-space>
    </template>

    <div class="realtime-content">
      <!-- 数据统计 -->
      <div class="data-statistics" v-if="statistics">
        <a-row :gutter="16">
          <a-col
            v-for="stat in statistics"
            :key="stat.key"
            :span="6"
          >
            <a-statistic
              :title="stat.title"
              :value="stat.value"
              :suffix="stat.suffix"
              :value-style="{ color: stat.color }"
            />
          </a-col>
        </a-row>
      </div>

      <!-- 实时图表 -->
      <div class="realtime-chart" v-if="chartData">
        <div ref="chartContainer" class="chart-container"></div>
      </div>

      <!-- 实时消息 -->
      <div class="realtime-messages" v-if="messages.length > 0">
        <a-timeline>
          <a-timeline-item
            v-for="message in messages.slice(0, 5)"
            :key="message.id"
            :color="getMessageColor(message)"
          >
            <template #dot>
                <component :is="getMessageIcon(message)" />
            </template>
            <div class="message-content">
              <div class="message-title">{{ message.title }}</div>
              <div class="message-desc">{{ message.description }}</div>
              <div class="message-time">{{ formatTime(message.timestamp) }}</div>
            </div>
          </a-timeline-item>
        </a-timeline>
      </div>

      <!-- 空状态 -->
      <div class="empty-state" v-if="!loading && !hasData">
        <a-empty description="暂无实时数据" />
      </div>
    </div>
  </a-card>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useWebSocket } from '/@/composables/useWebSocket'
import { formatDateTime } from '/@/utils/format'
import * as echarts from 'echarts'
import {
  WifiOutlined,
  DisconnectOutlined,
  ExclamationCircleOutlined,
  InfoCircleOutlined,
  CheckCircleOutlined
} from '@ant-design/icons-vue'

const props = defineProps({
  title: {
    type: String,
    default: '实时数据'
  },
  wsUrl: {
    type: String,
    required: true
  },
  topic: {
    type: String,
    default: ''
  },
  chartType: {
    type: String,
    default: 'line' // line, bar, pie
  },
  maxMessages: {
    type: Number,
    default: 50
  }
})

const emit = defineEmits(['dataUpdate', 'messageReceive'])

const loading = ref(false)
const messages = ref([])
const statistics = ref(null)
const chartData = ref([])
const chartContainer = ref(null)
let chart = null

// WebSocket Hook
const {
  isConnected,
  connect,
  disconnect,
  subscribe,
  unsubscribe
} = useWebSocket(props.wsUrl, {
  onConnect: () => {
    console.log('实时数据连接已建立')
    if (props.topic) {
      subscribe(props.topic)
    }
  },
  onDisconnect: () => {
    console.log('实时数据连接已断开')
  },
  onMessage: (message) => {
    handleWebSocketMessage(message)
  },
  onError: (error) => {
    console.error('实时数据连接错误:', error)
    loading.value = false
  }
})

// 计算属性
const connectionStatus = computed(() => {
  if (isConnected.value) {
    return {
      text: '已连接',
      color: 'green',
      icon: WifiOutlined
    }
  } else {
    return {
      text: '未连接',
      color: 'red',
      icon: DisconnectOutlined
    }
  }
})

const hasData = computed(() => {
  return messages.value.length > 0 || statistics.value || chartData.value.length > 0
})

// 方法
const toggleConnection = () => {
  if (isConnected.value) {
    disconnect()
  } else {
    connect()
  }
}

const handleWebSocketMessage = (message) => {
  switch (message.type) {
    case 'STATISTICS_UPDATE':
      handleStatisticsUpdate(message.data)
      break
    case 'CHART_DATA_UPDATE':
      handleChartDataUpdate(message.data)
      break
    case 'MESSAGE':
      handleMessageReceive(message.data)
      break
    default:
      console.log('未处理的消息类型:', message.type)
  }

  emit('messageReceive', message)
}

const handleStatisticsUpdate = (data) => {
  statistics.value = data
  emit('dataUpdate', { type: 'statistics', data })
}

const handleChartDataUpdate = (data) => {
  chartData.value = data
  updateChart()
  emit('dataUpdate', { type: 'chart', data })
}

const handleMessageReceive = (data) => {
  const message = {
    id: Date.now() + Math.random(),
    ...data,
    timestamp: new Date()
  }

  messages.value.unshift(message)

  // 限制消息数量
  if (messages.value.length > props.maxMessages) {
    messages.value = messages.value.slice(0, props.maxMessages)
  }

  emit('dataUpdate', { type: 'message', data: message })
}

const initChart = () => {
  if (!chartContainer.value) return

  chart = echarts.init(chartContainer.value)

  const option = {
    title: {
      text: '实时数据趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['数值'],
      bottom: 0
    },
    xAxis: {
      type: 'time',
      splitLine: {
        show: false
      }
    },
    yAxis: {
      type: 'value',
      splitLine: {
        show: true
      }
    },
    series: [{
      name: '数值',
      type: props.chartType,
      data: [],
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: {
        width: 2
      },
      areaStyle: {
        opacity: 0.3
      }
    }]
  }

  chart.setOption(option)
}

const updateChart = () => {
  if (!chart || !chartData.value.length) return

  const option = {
    series: [{
      data: chartData.value.map(item => [item.timestamp, item.value])
    }]
  }

  chart.setOption(option)
}

const getMessageColor = (message) => {
  const colorMap = {
    'success': 'green',
    'warning': 'orange',
    'error': 'red',
    'info': 'blue'
  }
  return colorMap[message.level] || 'blue'
}

const getMessageIcon = (message) => {
  const iconMap = {
    'success': CheckCircleOutlined,
    'warning': ExclamationCircleOutlined,
    'error': ExclamationCircleOutlined,
    'info': InfoCircleOutlined
  }
  return iconMap[message.level] || InfoCircleOutlined
}

const formatTime = (timestamp) => {
  return formatDateTime(timestamp)
}

// 监听连接状态变化
watch(isConnected, (connected) => {
  loading.value = !connected
})

// 监听图表数据变化
watch(chartData, () => {
  nextTick(() => {
    if (!chart) {
      initChart()
    } else {
      updateChart()
    }
  })
}, { deep: true })

// 生命周期
onMounted(() => {
  if (props.chartType !== 'none') {
    nextTick(() => {
      initChart()
    })
  }
})

onUnmounted(() => {
  if (chart) {
    chart.dispose()
    chart = null
  }
})
</script>

<style lang="less" scoped>
.realtime-data-card {
  .realtime-content {
    .data-statistics {
      margin-bottom: 16px;
    }

    .realtime-chart {
      margin-bottom: 16px;

      .chart-container {
        width: 100%;
        height: 300px;
      }
    }

    .realtime-messages {
      max-height: 300px;
      overflow-y: auto;

      .message-content {
        .message-title {
          font-weight: 500;
          margin-bottom: 4px;
        }

        .message-desc {
          color: #666;
          font-size: 12px;
          margin-bottom: 4px;
        }

        .message-time {
          color: #999;
          font-size: 11px;
        }
      }
    }

    .empty-state {
      padding: 40px 0;
      text-align: center;
    }
  }
}
</style>
```

---

## 🧪 测试策略

### 1. 单元测试

```java
@SpringBootTest
class RealtimeServiceTest {

    @Resource
    private RealtimeService realtimeService;

    @Resource
    private RealtimeSubscriptionDao subscriptionDao;

    @MockBean
    private ConnectionManagerFactory connectionManagerFactory;

    @MockBean
    private MessageHandlerFactory messageHandlerFactory;

    @Test
    void testSubscribe() {
        // 准备测试数据
        Long userId = 1L;
        RealtimeSubscriptionDTO subscriptionDTO = new RealtimeSubscriptionDTO();
        subscriptionDTO.setConnectionType("WEBSOCKET");
        subscriptionDTO.setTopic("device:status");
        subscriptionDTO.setFilter(Map.of("deviceId", 123));
        subscriptionDTO.setParams(Map.of("expireMinutes", 60));

        // 执行测试
        RealtimeSubscriptionVO result = realtimeService.subscribe(userId, subscriptionDTO);

        // 验证结果
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("WEBSOCKET", result.getConnectionType());
        assertEquals("device:status", result.getSubscriptionTopic());
        assertEquals(1, result.getStatus());

        // 验证数据库中的记录
        RealtimeSubscriptionEntity savedSubscription = subscriptionDao.selectById(result.getSubscriptionId());
        assertNotNull(savedSubscription);
        assertEquals(userId, savedSubscription.getUserId());
        assertEquals("device:status", savedSubscription.getSubscriptionTopic());
    }

    @Test
    void testPublishMessage() {
        // 准备测试数据
        Long userId = 1L;
        RealtimeMessageDTO messageDTO = new RealtimeMessageDTO();
        messageDTO.setMessageType("DEVICE_STATUS");
        messageDTO.setTopic("device:123:status");
        messageDTO.setData(Map.of(
            "deviceId", 123,
            "status", "online",
            "timestamp", System.currentTimeMillis()
        ));
        messageDTO.setPriorityLevel(2);

        // Mock消息处理器
        MessageHandler mockHandler = mock(MessageHandler.class);
        when(messageHandlerFactory.getHandler("DEVICE_STATUS")).thenReturn(mockHandler);
        when(mockHandler.handle(any())).thenReturn(CompletableFuture.completedFuture(null));

        // 执行测试
        assertDoesNotThrow(() -> realtimeService.publish(userId, messageDTO));

        // 验证消息已保存到数据库
        List<RealtimeMessageEntity> messages = realtimeMessageDao.selectList(
            new QueryWrapper<RealtimeMessageEntity>()
                .eq("message_type", "DEVICE_STATUS")
                .eq("message_topic", "device:123:status")
                .orderByDesc("create_time")
                .last("LIMIT 1")
        );

        assertFalse(messages.isEmpty());
        RealtimeMessageEntity savedMessage = messages.get(0);
        assertEquals(userId, savedMessage.getMessageSenderId());
        assertEquals("DEVICE_STATUS", savedMessage.getMessageType());
        assertEquals(2, savedMessage.getPriorityLevel());
    }

    @Test
    void testBroadcastMessage() {
        // 准备测试数据
        Long userId = 1L;
        RealtimeBroadcastDTO broadcastDTO = new RealtimeBroadcastDTO();
        broadcastDTO.setMessageType("SYSTEM_NOTIFICATION");
        broadcastDTO.setTopic("system:announcement");
        broadcastDTO.setData(Map.of(
            "title", "系统维护通知",
            "content", "系统将于今晚进行维护",
            "level", "warning"
        ));

        // Mock权限验证
        when(permissionService.hasPermission(userId, "realtime:broadcast")).thenReturn(true);

        // Mock消息处理器
        MessageHandler mockHandler = mock(MessageHandler.class);
        when(messageHandlerFactory.getHandler("SYSTEM_NOTIFICATION")).thenReturn(mockHandler);
        when(mockHandler.handle(any())).thenReturn(CompletableFuture.completedFuture(null));

        // 执行测试
        assertDoesNotThrow(() -> realtimeService.broadcast(userId, broadcastDTO));

        // 验证广播消息已创建
        List<RealtimeMessageEntity> messages = realtimeMessageDao.selectList(
            new QueryWrapper<RealtimeMessageEntity>()
                .eq("message_type", "SYSTEM_NOTIFICATION")
                .eq("message_topic", "system:announcement")
                .eq("message_sender_id", userId)
        );

        assertFalse(messages.isEmpty());
    }
}
```

### 2. 集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RealtimeIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testRealtimeWorkflow() throws InterruptedException {
        String token = authenticate("admin", "123456");

        // 1. 订阅实时数据
        RealtimeSubscriptionDTO subscriptionDTO = new RealtimeSubscriptionDTO();
        subscriptionDTO.setConnectionType("WEBSOCKET");
        subscriptionDTO.setTopic("device:status");
        subscriptionDTO.setFilter(Map.of("deviceId", 123));

        ResponseEntity<ResponseDTO<RealtimeSubscriptionVO>> subscribeResponse = restTemplate.exchange(
            "/api/realtime/subscribe",
            HttpMethod.POST,
            createEntityWithToken(token, subscriptionDTO),
            new ParameterizedTypeReference<ResponseDTO<RealtimeSubscriptionVO>>() {}
        );

        assertEquals(200, subscribeResponse.getStatusCodeValue());
        assertNotNull(subscribeResponse.getBody().getData().getSubscriptionId());

        // 2. 发布消息
        RealtimeMessageDTO messageDTO = new RealtimeMessageDTO();
        messageDTO.setMessageType("DEVICE_STATUS");
        messageDTO.setTopic("device:123:status");
        messageDTO.setData(Map.of(
            "deviceId", 123,
            "status", "online",
            "timestamp", System.currentTimeMillis()
        ));

        ResponseEntity<ResponseDTO<String>> publishResponse = restTemplate.exchange(
            "/api/realtime/publish",
            HttpMethod.POST,
            createEntityWithToken(token, messageDTO),
            new ParameterizedTypeReference<ResponseDTO<String>>() {}
        );

        assertEquals(200, publishResponse.getStatusCodeValue());

        // 3. 等待消息处理完成
        Thread.sleep(1000);

        // 4. 验证统计信息
        ResponseEntity<ResponseDTO<RealtimeStatisticsVO>> statsResponse = restTemplate.exchange(
            "/api/realtime/statistics",
            HttpMethod.GET,
            createEntityWithToken(token, null),
            new ParameterizedTypeReference<ResponseDTO<RealtimeStatisticsVO>>() {}
        );

        assertEquals(200, statsResponse.getStatusCodeValue());
        RealtimeStatisticsVO stats = statsResponse.getBody().getData();
        assertTrue(stats.getTotalMessages() > 0);
    }

    @Test
    void testWebSocketConnection() throws Exception {
        // 这里需要实际的WebSocket客户端来测试
        // 可以使用Spring WebSocket Test或第三方WebSocket客户端库

        String token = authenticate("admin", "123456");
        String wsUrl = "ws://localhost:" + port + "/ws/realtime/user?token=" + token;

        // 创建WebSocket客户端连接
        WebSocketClient client = new StandardWebSocketClient();
        Session session = client.doConnect(wsUrl, new WebSocketHandler() {
            @Override
            public void afterConnectionEstablished(Session session) throws Exception {
                // 连接建立后发送订阅消息
                session.sendMessage(new TextMessage("{\"type\":\"SUBSCRIBE\",\"data\":{\"topic\":\"test\"}}"));
            }

            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                // 处理接收到的消息
                String payload = message.getPayload();
                System.out.println("Received message: " + payload);
            }
        });

        // 等待连接建立和消息处理
        Thread.sleep(2000);

        // 关闭连接
        session.close();
    }
}
```

---

## 📋 检查清单

### 开发前检查

- [ ] 是否已明确支持的实时通信协议？
- [ ] 是否已确认消息优先级要求？
- [ ] 是否已了解连接数限制？
- [ ] 是否已确认消息持久化需求？

### 开发中检查

- [ ] 是否实现了多协议支持？
- [ ] 是否添加了消息队列机制？
- [ ] 是否实现了连接池管理？
- [ ] 是否添加了心跳检测机制？
- [ ] 是否实现了消息重试机制？

### 部署前检查

- [ ] WebSocket服务是否正常运行？
- [ ] 消息队列配置是否正确？
- [ ] 连接数限制是否合理？
- [ ] 心跳检测是否生效？
- [ ] 消息处理性能是否达标？

---

**📞 技术支持**：架构师团队

**📚 相关文档**：
- [设备管理公共模块](./smart-device.md)
- [权限管理公共模块](./smart-permission.md)
- [告警管理公共模块](./smart-alarm.md)
- [综合开发规范文档](../DEV_STANDARDS.md)

---

*最后更新：2025-01-10*
*维护者：SmartAdmin开发团队*