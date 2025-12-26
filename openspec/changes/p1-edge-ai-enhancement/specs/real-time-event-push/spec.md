# 实时事件推送能力规格

**能力ID**: real-time-event-push
**优先级**: P0
**创建日期**: 2025-01-30
**状态**: 提案中

---

## ADDED Requirements

### REQ-REALTIME-PUSH-001: WebSocket连接管理

**优先级**: P0
**需求描述**: 系统必须支持WebSocket连接管理，包括连接建立、认证、心跳检测和断线重连。

**场景**:

#### Scenario: 建立WebSocket连接

**Given** 用户已登录并持有有效token
**When** 前端建立WebSocket连接
**Then** 系统应该：
- 验证JWT token
- 记录连接信息（userId, sessionId）
- 发送连接成功消息
- 启动心跳检测

**验证标准**:
```javascript
// 前端代码
const socket = new SockJS('http://localhost:8092/ws/video')
const stompClient = Stomp.over(socket)

const headers = {
  'Authorization': 'Bearer ' + getToken()
}

stompClient.connect(headers, () => {
  console.log('连接成功')
  // 订阅设备事件
  stompClient.subscribe('/topic/device-events', (message) => {
    const event = JSON.parse(message.body)
    console.log('收到设备事件:', event)
  })
})
```

#### Scenario: 心跳检测

**Given** WebSocket连接已建立
**When** 每30秒发送心跳
**Then** 系统应该：
- 前端发送心跳消息
- 后端响应心跳消息
- 连接超时自动断开

**心跳消息**:
```javascript
// 前端：每30秒发送一次
stompClient.send('/app/heartbeat', {}, JSON.stringify({
  timestamp: Date.now()
}))

// 后端：响应心跳
@MessageMapping("/heartbeat")
public void handleHeartbeat(StompHeaderAccessor accessor) {
  // 更新最后活跃时间
  connectionManager.updateLastActivity(sessionId)
}
```

#### Scenario: 断线重连

**Given** WebSocket连接意外断开
**When** 检测到连接断开
**Then** 前端应该：
- 自动尝试重连（指数退避：1s, 2s, 4s, 8s, ...）
- 重连成功后重新订阅频道
- 显示连接状态

**重连逻辑**:
```javascript
let reconnectAttempts = 0
const maxReconnectAttempts = 5

function connect() {
  socket = new SockJS('http://localhost:8092/ws/video')
  stompClient = Stomp.over(socket)

  stompClient.connect(headers, () => {
    reconnectAttempts = 0
    console.log('重连成功')
    subscribe()
  }, (error) => {
    if (reconnectAttempts < maxReconnectAttempts) {
      setTimeout(() => {
        reconnectAttempts++
        console.log(`重连尝试 ${reconnectAttempts}/${maxReconnectAttempts}`)
        connect()
      }, 1000 * Math.pow(2, reconnectAttempts))  // 指数退避
    }
  })
}
```

---

### REQ-REALTIME-PUSH-002: 设备AI事件推送

**优先级**: P0
**需求描述**: 系统必须实时推送设备AI事件到前端，支持事件过滤和批量推送优化。

**场景**:

#### Scenario: 推送单个设备事件

**Given** 设备上报AI事件
**And** 事件已存储到数据库
**When** 事件创建成功
**Then** 系统应该：
- 构造WebSocket消息
- 推送到所有订阅者（/topic/device-events）
- 记录推送日志

**推送逻辑**:
```java
@Service
public class EventPushService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void pushDeviceEvent(DeviceAIEventEntity event) {
        DeviceEventMessage message = DeviceEventMessage.builder()
            .eventId(event.getEventId())
            .deviceId(event.getDeviceId())
            .eventType(event.getEventType())
            .confidence(event.getConfidence())
            .eventTime(event.getEventTime())
            .build();

        // 广播到所有订阅者
        messagingTemplate.convertAndSend("/topic/device-events", message);

        log.info("[WebSocket推送] 设备事件: eventId={}, eventType={}",
            event.getEventId(), event.getEventType());
    }
}
```

#### Scenario: 推送告警事件

**Given** 告警规则匹配成功
**And** 告警记录已创建
**When** 告警创建成功
**Then** 系统应该：
- 推送到所有订阅者（/topic/alarms）
- 如果告警已分配，额外推送到处理人（/queue/my-alarms）
- 根据告警级别播放不同的音效

**推送逻辑**:
```java
public void pushAlarmEvent(AlarmRecordEntity alarm) {
    AlarmEventMessage message = AlarmEventMessage.builder()
        .alarmId(alarm.getAlarmId())
        .alarmLevel(alarm.getAlarmLevel())
        .alarmMessage(alarm.getAlarmMessage())
        .alarmTime(alarm.getAlarmTime())
        .build();

    // 广播到所有订阅者
    messagingTemplate.convertAndSend("/topic/alarms", message);

    // 推送到处理人（如果有）
    if (alarm.getHandlerId() != null) {
        messagingTemplate.convertAndSendToUser(
            String.valueOf(alarm.getHandlerId()),
            "/queue/my-alarms",
            message
        );
    }
}
```

---

### REQ-REALTIME-PUSH-003: 消息队列和优化

**优先级**: P0
**需求描述**: 系统必须使用Redis Pub/Sub实现消息转发，支持消息持久化和批量推送优化。

**场景**:

#### Scenario: Redis Pub/Sub消息转发

**Given** 设备AI事件已创建
**When** 事件发布到Redis
**Then** 系统应该：
- 监听Redis频道
- 转发消息到WebSocket
- 支持多实例部署（负载均衡）

**配置**:
```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: 6379
    pub-sub:
      device-events-topic: device-ai-events
      alarm-events-topic: alarm-events
```

**监听器**:
```java
@Component
public class RedisMessageListener {

    @Autowired
    private EventPushService eventPushService;

    @RedisListener(topic = "${spring.redis.pub-sub.device-events-topic}")
    public void onDeviceEvent(String message) {
        DeviceAIEventEntity event = JSON.parseObject(message, DeviceAIEventEntity.class);
        eventPushService.pushDeviceEvent(event);
    }

    @RedisListener(topic = "${spring.redis.pub-sub.alarm-events-topic}")
    public void onAlarmEvent(String message) {
        AlarmRecordEntity alarm = JSON.parseObject(message, AlarmRecordEntity.class);
        eventPushService.pushAlarmEvent(alarm);
    }
}
```

#### Scenario: 批量推送优化

**Given** 短时间内有大量事件（如 > 100事件/秒）
**When** 触发推送
**Then** 系统应该：
- 批量聚合消息（100ms窗口）
- 一次性推送聚合消息
- 减少推送频率

**优化逻辑**:
```java
@Component
public class BatchEventPushService {

    private final List<DeviceAIEventEntity> eventBuffer = new ArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        // 每100ms批量推送一次
        scheduler.scheduleAtFixedRate(() -> {
            if (!eventBuffer.isEmpty()) {
                flushEvents();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void addEvent(DeviceAIEventEntity event) {
        synchronized (eventBuffer) {
            eventBuffer.add(event);
        }
    }

    private void flushEvents() {
        List<DeviceAIEventEntity> events;
        synchronized (eventBuffer) {
            events = new ArrayList<>(eventBuffer);
            eventBuffer.clear();
        }

        if (!events.isEmpty()) {
            messagingTemplate.convertAndSend("/topic/device-events-batch", events);
            log.info("[WebSocket批量推送] 推送{}个事件", events.size());
        }
    }
}
```

---

### REQ-REALTIME-PUSH-004: 推送权限和过滤

**优先级**: P0
**需求描述**: 系统必须根据用户权限过滤推送的消息，确保用户只能看到其有权限的设备和事件。

**场景**:

#### Scenario: 用户权限验证

**Given** 用户订阅设备事件
**And** 用户只有部分设备的访问权限
**When** 设备事件发生
**Then** 系统应该：
- 检查用户是否有权限访问该设备
- 有权限则推送，无权限则过滤

**权限检查**:
```java
@Component
public class EventFilterService {

    @Autowired
    private PermissionService permissionService;

    public boolean canUserAccessDevice(Long userId, String deviceId) {
        // 检查用户是否有权限访问该设备
        return permissionService.hasDevicePermission(userId, deviceId);
    }
}

// WebSocket消息拦截器
@Override
public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new ChannelInterceptor() {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            Long userId = getUserId(accessor);

            // 检查订阅权限
            String destination = accessor.getDestination();
            if (!permissionService.canSubscribe(userId, destination)) {
                throw new IllegalArgumentException("无权订阅该频道");
            }

            return message;
        }
    });
}
```

#### Scenario: 消息过滤

**Given** 有100个设备在线
**And** 管理员A只管理其中的20个设备
**When** 100个设备同时上报事件
**Then** 管理员A应该：
- 只收到其管理的20个设备的事件
- 其他80个设备的事件被过滤

**订阅过滤**:
```javascript
// 前端：订阅时传递过滤条件
stompClient.subscribe('/topic/device-events', (message) => {
  const event = JSON.parse(message.body)

  // 前端二次过滤（额外保障）
  if (userDeviceIds.includes(event.deviceId)) {
    displayEvent(event)
  }
}, {
  // 订阅头
  'filter-deviceIds': userDeviceIds.join(',')
})
```

---

## MODIFIED Requirements

*（本能力为新增，无修改的需求）*

---

## REMOVED Requirements

*（本能力为新增，无删除的需求）*

---

## 附录

### A. WebSocket消息格式

**DeviceEventMessage**:
```json
{
  "eventId": "evt_20250130_001",
  "deviceId": "CAM001",
  "deviceCode": "camera_001",
  "eventType": "FALL_DETECTION",
  "eventTypeName": "跌倒检测",
  "confidence": 0.95,
  "bbox": {"x":100,"y":150,"width":200,"height":300},
  "eventTime": "2025-01-30T10:30:00",
  "snapshotUrl": "https://minio.example.com/snapshots/evt_20250130_001.jpg"
}
```

**AlarmEventMessage**:
```json
{
  "alarmId": "alarm_20250130_001",
  "eventId": "evt_20250130_001",
  "deviceId": "CAM001",
  "alarmLevel": 3,
  "alarmLevelName": "高",
  "alarmMessage": "检测到跌倒，置信度0.95，设备CAM001，请立即处理！",
  "alarmTime": "2025-01-30T10:30:00",
  "eventType": "FALL_DETECTION",
  "ruleName": "跌倒检测-高置信度"
}
```

### B. 性能指标

| 指标 | 目标值 | 测量方法 |
|------|--------|----------|
| WebSocket连接数 | > 500 | JMeter并发测试 |
| 消息推送延迟 | P95 < 500ms | WebSocket消息时间戳 |
| 消息吞吐量 | > 1000消息/秒 | JMeter压力测试 |
| 连接成功率 | > 99% | 7天稳定性测试 |

### C. 错误处理

| 错误场景 | 处理方式 |
|---------|---------|
| WebSocket连接失败 | 指数退避重连（最多5次） |
| 消息发送失败 | 记录日志，重试1次 |
| 订阅无权限 | 关闭连接，返回错误码 |
| 心跳超时 | 断开连接，释放资源 |

---

**规格编写人**: IOE-DREAM 架构委员会
**创建日期**: 2025-01-30
**版本**: 1.0.0
