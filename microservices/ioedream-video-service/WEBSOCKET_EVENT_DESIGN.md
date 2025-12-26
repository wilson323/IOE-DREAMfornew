# WebSocket事件推送架构设计

## 1. 概述

### 1.1 设计目标

为IOE-DREAM视频监控模块提供实时AI事件推送能力，实现：
- **低延迟**：边缘AI事件到前端展示延迟 < 500ms
- **高并发**：支持100+客户端同时连接
- **可扩展**：支持横向扩展，多实例部署
- **可靠性**：连接断开自动重连，消息不丢失

### 1.2 核心场景

```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐
│  边缘设备    │─────▶│ Video Service │─────▶│  前端页面    │
│ (AI识别)    │ HTTP │  (WebSocket)  │ WS   │  (实时展示)  │
└─────────────┘      └──────────────┘      └─────────────┘
     人脸检测              事件广播             实时告警
     行为分析              会话管理             动态更新
     异常检测              消息路由             历史回溯
```

### 1.3 技术选型

| 组件 | 技术方案 | 说明 |
|------|---------|------|
| WebSocket协议 | Spring WebSocket + STOMP | 基于Spring Boot 3.x原生支持 |
| 消息代理 | RabbitMQ | 可靠消息传递，支持集群 |
| 会话管理 | WebSocketSession | Spring WebSocket会话管理 |
| 消息格式 | JSON | 统一事件数据格式 |

---

## 2. 系统架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                     前端层 (Browser)                         │
├─────────────────────────────────────────────────────────────┤
│  SockJS客户端  ──▶  STOMP客户端  ──▶  事件订阅/接收         │
└──────────────────────┬──────────────────────────────────────┘
                       │ WebSocket连接
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                  视频服务层 (ioedream-video-service)         │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────────────┐  │
│  │ WebSocket控制器层 (Controller)                       │  │
│  │  ┌──────────────┐  ┌──────────────┐                │  │
│  │  │ WebSocketConfig│  │EventWebSocket│                │  │
│  │  │ (配置类)      │  │Controller    │                │  │
│  │  └──────────────┘  └──────────────┘                │  │
│  └──────────────────────────────────────────────────────┘  │
│                         │                                    │
│                         ▼                                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ 事件处理层 (Service)                                  │  │
│  │  ┌──────────────┐  ┌──────────────┐                │  │
│  │  │EventPushService│ │WebSocketSession│              │  │
│  │  │(事件推送)      │ │Manager        │              │  │
│  │  └──────────────┘  └──────────────┘                │  │
│  └──────────────────────────────────────────────────────┘  │
│                         │                                    │
│                         ▼                                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ 消息代理层 (Message Broker)                          │  │
│  │  ┌──────────────┐  ┌──────────────┐                │  │
│  │  │RabbitMQ      │  │Topic Exchange │              │  │
│  │  │(消息队列)     │  │(主题交换机)    │              │  │
│  │  └──────────────┘  └──────────────┘                │  │
│  └──────────────────────────────────────────────────────┘  │
└──────────────────────┬──────────────────────────────────────┘
                       │ REST API
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                  边缘设备层 (Edge Devices)                   │
├─────────────────────────────────────────────────────────────┤
│  AI摄像头  ──▶  AI事件检测  ──▶  HTTP上报事件                │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 数据流图

```
边缘AI事件上报流程:

1. [边缘设备] AI检测到人脸
   ↓
2. [边缘设备] HTTP POST /api/v1/video/edge/event
   ↓
3. [EventReceiveController] 接收事件
   ↓
4. [EventProcessService] 处理验证
   ↓
5. [EventPushService] 推送到RabbitMQ
   ↓
6. [RabbitMQ] Topic Exchange路由
   ↓
7. [WebSocket订阅者] 接收消息
   ↓
8. [前端页面] 实时展示事件
```

---

## 3. 核心组件设计

### 3.1 WebSocket配置

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单消息代理（用于开发测试）
        registry.enableSimpleBroker("/topic", "/queue");

        // 或者使用RabbitMQ作为消息代理（生产环境）
        // registry.enableStompBrokerRelay("/topic", "/queue")
        //        .setRelayHost("localhost")
        //        .setRelayPort(61613);

        // 设置应用目标前缀
        registry.setApplicationDestinationPrefixes("/app");

        // 设置用户目标前缀（用于点对点消息）
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 注册STOMP端点
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/video-events")  // WebSocket端点
                .setAllowedOriginPatterns("*")   // 允许跨域
                .withSockJS();                    // 启用SockJS支持
    }
}
```

### 3.2 事件数据模型

```java
/**
 * 边缘AI事件DTO
 */
@Data
@Schema(description = "边缘AI事件")
public class EdgeAIEventDTO {

    @Schema(description = "事件ID")
    private String eventId;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "事件类型")
    private String eventType;  // FACE_DETECTED, PERSON_COUNT, ABNORMAL_BEHAVIOR

    @Schema(description = "事件数据(JSON)")
    private Map<String, Object> eventData;

    @Schema(description = "置信度(0-1)")
    private BigDecimal confidence;

    @Schema(description = "事件时间戳")
    private Long timestamp;

    @Schema(description = "抓拍图片URL")
    private String imageUrl;
}
```

### 3.3 WebSocket控制器

```java
@Controller
public class EventWebSocketController {

    private static final String EVENT_TOPIC = "/topic/video-events";

    /**
     * 订阅实时事件
     * 前端: stompClient.subscribe('/topic/video-events', handler)
     */
    @SubscribeMapping(EVENT_TOPIC)
    public List<EdgeAIEventDTO> subscribeEvents() {
        // 返回最近10条事件作为初始数据
        return eventPushService.getRecentEvents(10);
    }
}
```

### 3.4 事件推送服务

```java
@Service
@Slf4j
public class EventPushService {

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 推送事件到所有订阅者
     */
    public void pushEvent(EdgeAIEventDTO event) {
        log.info("[事件推送] 推送AI事件: eventType={}, deviceId={}, eventId={}",
                event.getEventType(), event.getDeviceId(), event.getEventId());

        messagingTemplate.convertAndSend(EVENT_TOPIC, event);
    }

    /**
     * 推送事件到指定用户
     */
    public void pushEventToUser(Long userId, EdgeAIEventDTO event) {
        String userDestination = "/user/" + userId + "/queue/video-events";
        messagingTemplate.convertAndSend(userDestination, event);
    }
}
```

### 3.5 边缘事件接收控制器

```java
@RestController
@RequestMapping("/api/v1/video/edge")
public class EdgeEventController {

    @Resource
    private EventProcessService eventProcessService;

    /**
     * 接收边缘设备上报的AI事件
     */
    @PostMapping("/event")
    public ResponseDTO<Void> receiveEdgeEvent(@RequestBody EdgeAIEventDTO event) {
        log.info("[边缘事件] 接收AI事件: eventType={}, deviceId={}, confidence={}",
                event.getEventType(), event.getDeviceId(), event.getConfidence());

        eventProcessService.processEdgeEvent(event);
        return ResponseDTO.ok();
    }
}
```

---

## 4. 前端集成示例

### 4.1 SockJS + STOMP客户端

```javascript
// 1. 创建WebSocket连接
const socket = new SockJS('http://localhost:8092/ws/video-events');
const stompClient = Stomp.over(socket);

// 2. 连接配置
stompClient.connect({}, function (frame) {
    console.log('WebSocket连接成功:', frame);

    // 3. 订阅实时事件
    stompClient.subscribe('/topic/video-events', function (message) {
        const event = JSON.parse(message.body);
        console.log('收到AI事件:', event);
        handleAIEvent(event);
    });

}, function (error) {
    console.error('WebSocket连接失败:', error);
    // 自动重连逻辑
    setTimeout(connectWebSocket, 5000);
});

// 4. 处理AI事件
function handleAIEvent(event) {
    switch (event.eventType) {
        case 'FACE_DETECTED':
            showFaceAlert(event);
            break;
        case 'PERSON_COUNT':
            updatePersonCount(event);
            break;
        case 'ABNORMAL_BEHAVIOR':
            showAbnormalAlert(event);
            break;
    }
}
```

### 4.2 React Hook示例

```javascript
function useVideoEvents() {
    const [events, setEvents] = useState([]);
    const [connected, setConnected] = useState(false);

    useEffect(() => {
        const socket = new SockJS('http://localhost:8092/ws/video-events');
        const stompClient = Stomp.over(socket);

        stompClient.connect({}, () => {
            setConnected(true);
            stompClient.subscribe('/topic/video-events', (message) => {
                const event = JSON.parse(message.body);
                setEvents(prev => [event, ...prev].slice(0, 100));
            });
        });

        return () => {
            stompClient.disconnect();
            setConnected(false);
        };
    }, []);

    return { events, connected };
}
```

---

## 5. 部署配置

### 5.1 Maven依赖

```xml
<!-- Spring WebSocket -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>

<!-- Spring Messaging (STOMP) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>

<!-- SockJS客户端 (前端) -->
<!-- 通过npm安装: npm install sockjs-client @stomp/stompjs -->
```

### 5.2 配置文件

```yaml
# application.yml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

  # WebSocket配置
  websocket:
    enabled: true
    allowed-origins: "*"
    heartbeat:
      outgoing: 25000  # 心跳发送间隔(ms)
      incoming: 25000  # 心跳接收间隔(ms)
```

---

## 6. 监控与优化

### 6.1 性能指标

| 指标 | 目标值 | 监控方法 |
|------|--------|---------|
| 连接建立时间 | < 1s | WebSocket握手日志 |
| 事件推送延迟 | < 500ms | 时间戳对比 |
| 并发连接数 | 100+ | Actuator metrics |
| 消息吞吐量 | 1000 msg/s | RabbitMQ管理界面 |

### 6.2 优化策略

1. **连接池管理**：限制单机最大连接数
2. **消息压缩**：大图片URL化，不通过WS传输
3. **心跳保活**：定期心跳检测断开连接
4. **消息去重**：基于eventId避免重复推送
5. **限流保护**：单设备事件频率限制

---

## 7. 测试计划

### 7.1 单元测试

- WebSocketConfig配置测试
- EventPushService推送测试
- EventProcessService处理测试

### 7.2 集成测试

- WebSocket连接建立测试
- 事件订阅接收测试
- 多客户端并发测试
- 异常断线重连测试

### 7.3 性能测试

- 100并发连接压力测试
- 1000 msg/s消息吞吐测试
- 24小时稳定性测试

---

## 8. 安全考虑

### 8.1 认证授权

```java
@Configuration
public class WebSocketSecurityConfig {

    /**
     * WebSocket握手拦截器
     */
    public class AuthChannelInterceptor implements ChannelInterceptor {
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
            String token = accessor.getFirstNativeHeader("X-Auth-Token");

            // JWT token验证
            if (!jwtTokenUtil.validateToken(token)) {
                throw new AuthenticationCredentialsNotFoundException("未授权");
            }
            return message;
        }
    }
}
```

### 8.2 数据过滤

- 按区域权限过滤事件
- 按设备权限过滤事件
- 敏感信息脱敏处理

---

**文档版本**: v1.0
**编写日期**: 2025-01-30
**作者**: IOE-DREAM Team
