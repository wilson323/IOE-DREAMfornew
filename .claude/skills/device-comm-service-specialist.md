# 设备通讯服务专家技能
## Device Communication Service Specialist

**🎯 技能定位**: IOE-DREAM智慧园区设备通讯业务专家，精通设备协议适配、连接管理、数据采集、远程控制等核心设备通讯功能

**⚡ 技能等级**: ★★★★★★ (顶级专家)
**🎯 适用场景**: 设备通讯开发、协议集成、连接管理、设备监控、远程控制
**📊 技能覆盖**: 协议适配 | 连接管理 | 数据采集 | 远程控制 | 设备监控 | 故障诊断 | 性能优化
**🔧 技术栈**: Spring Boot 3.5.8 + Netty + WebSocket + MQTT + Modbus + OPC-UA

---

## 📋 技能概述

### **核心专长**
- **多协议适配**: TCP/UDP、HTTP/HTTPS、WebSocket、MQTT、Modbus、OPC-UA
- **设备连接管理**: 长连接保活、断线重连、负载均衡、连接池管理
- **实时数据采集**: 高频数据采集、数据缓存、批量处理、实时推送
- **远程设备控制**: 命令下发、状态同步、安全控制、超时处理
- **设备健康监控**: 心跳检测、故障诊断、性能监控、告警通知
- **高并发架构**: Netty异步处理、消息队列、分布式协调

### **解决能力**
- **设备通讯开发**: 完整的设备通讯服务和协议适配器实现
- **连接管理优化**: 高稳定性、高可用的设备连接管理
- **数据处理架构**: 高性能的设备数据处理和存储方案
- **监控告警建设**: 全方位的设备状态监控和故障告警
- **性能调优**: 大规模设备接入的性能优化和扩展

---

## 🎯 业务场景覆盖

### 📡 设备协议适配
```java
// 设备协议适配器核心架构
@Component
public class DeviceProtocolAdapterManager {

    private final Map<String, DeviceProtocolAdapter> protocolAdapters = new ConcurrentHashMap<>();
    private final DeviceConnectionManager connectionManager;
    private final DeviceDataProcessor dataProcessor;

    public DeviceProtocolAdapterManager(DeviceConnectionManager connectionManager,
                                       DeviceDataProcessor dataProcessor) {
        this.connectionManager = connectionManager;
        this.dataProcessor = dataProcessor;
        initializeAdapters();
    }

    private void initializeAdapters() {
        // 注册各种协议适配器
        registerAdapter(new ModbusProtocolAdapter());
        registerAdapter(new TcpProtocolAdapter());
        registerAdapter(new HttpProtocolAdapter());
        registerAdapter(new WebSocketProtocolAdapter());
        registerAdapter(new MqttProtocolAdapter());
        registerAdapter(new CustomProtocolAdapter());
    }

    private void registerAdapter(DeviceProtocolAdapter adapter) {
        protocolAdapters.put(adapter.getProtocolType(), adapter);
    }

    public DeviceProtocolAdapter getAdapter(String protocolType) {
        DeviceProtocolAdapter adapter = protocolAdapters.get(protocolType);
        if (adapter == null) {
            throw new BusinessException("UNSUPPORTED_PROTOCOL", "不支持的协议类型: " + protocolType);
        }
        return adapter;
    }

    public CompletableFuture<DeviceCommandResult> sendCommand(Long deviceId, DeviceCommand command) {
        DeviceEntity device = getDeviceById(deviceId);
        DeviceProtocolAdapter adapter = getAdapter(device.getProtocolType());

        return adapter.sendCommand(device, command)
            .thenApply(result -> {
                // 记录命令执行日志
                logDeviceCommand(deviceId, command, result);
                return result;
            })
            .exceptionally(throwable -> {
                // 处理发送失败
                logCommandError(deviceId, command, throwable);
                throw new BusinessException("COMMAND_SEND_FAILED", "命令发送失败", throwable);
            });
    }
}
```

### 🔌 TCP/UDP设备通讯
```java
// TCP设备通讯适配器
@Component
public class TcpProtocolAdapter implements DeviceProtocolAdapter {

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final ChannelGroup channelGroup;
    private final DeviceMessageHandler messageHandler;

    public TcpProtocolAdapter(DeviceMessageHandler messageHandler) {
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
        this.channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        this.messageHandler = messageHandler;
    }

    @Override
    public String getProtocolType() {
        return "TCP";
    }

    @Override
    public CompletableFuture<DeviceConnectionResult> connect(DeviceEntity device) {
        CompletableFuture<DeviceConnectionResult> future = new CompletableFuture<>();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 4));
                        pipeline.addLast(new DeviceMessageDecoder());
                        pipeline.addLast(new DeviceMessageEncoder());
                        pipeline.addLast(new DeviceClientHandler(device.getDeviceId(), messageHandler));
                    }
                });

        ChannelFuture connectFuture = bootstrap.connect(device.getIp(), device.getPort());
        connectFuture.addListener((ChannelFutureListener) future1 -> {
            if (future1.isSuccess()) {
                Channel channel = future1.channel();
                channelGroup.add(channel);

                // 设置连接成功回调
                future.complete(DeviceConnectionResult.success(channel));

                log.info("TCP设备连接成功: deviceId={}, ip={}, port={}",
                    device.getDeviceId(), device.getIp(), device.getPort());
            } else {
                // 设置连接失败回调
                future.completeExceptionally(future1.cause());

                log.error("TCP设备连接失败: deviceId={}, ip={}, port={}, error={}",
                    device.getDeviceId(), device.getIp(), device.getPort(), future1.cause().getMessage());
            }
        });

        return future;
    }

    @Override
    public CompletableFuture<DeviceCommandResult> sendCommand(DeviceEntity device, DeviceCommand command) {
        CompletableFuture<DeviceCommandResult> future = new CompletableFuture<>();

        Channel channel = getConnectedChannel(device.getDeviceId());
        if (channel == null || !channel.isActive()) {
            future.completeExceptionally(new BusinessException("DEVICE_NOT_CONNECTED", "设备未连接"));
            return future;
        }

        DeviceMessage message = DeviceMessage.builder()
            .deviceId(device.getDeviceId())
            .messageId(generateMessageId())
            .commandType(command.getCommandType())
            .params(command.getParams())
            .timestamp(System.currentTimeMillis())
            .build();

        ChannelFuture writeFuture = channel.writeAndFlush(message);
        writeFuture.addListener((ChannelFutureListener) future1 -> {
            if (future1.isSuccess()) {
                // 设置超时处理
                ScheduledFuture<?> timeoutFuture = channel.eventLoop().schedule(() -> {
                    future.completeExceptionally(new BusinessException("COMMAND_TIMEOUT", "命令执行超时"));
                }, 30, TimeUnit.SECONDS);

                // 缓存超时任务，用于命令响应时取消
                cacheTimeoutFuture(message.getMessageId(), timeoutFuture);

                future.complete(DeviceCommandResult.success(message.getMessageId()));
            } else {
                future.completeExceptionally(new BusinessException("COMMAND_SEND_FAILED", "命令发送失败", future1.cause()));
            }
        });

        return future;
    }
}
```

### 📡 WebSocket实时通讯
```java
// WebSocket设备通讯适配器
@Component
public class WebSocketProtocolAdapter implements DeviceProtocolAdapter {

    private final WebSocketServer webSocketServer;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public WebSocketProtocolAdapter() {
        this.webSocketServer = new WebSocketServer();
        webSocketServer.setWebSocketHandler(new DeviceWebSocketHandler());
    }

    @Override
    public String getProtocolType() {
        return "WebSocket";
    }

    @Override
    public CompletableFuture<DeviceConnectionResult> connect(DeviceEntity device) {
        CompletableFuture<DeviceConnectionResult> future = new CompletableFuture<>();

        try {
            // 建立WebSocket连接
            WebSocketSession session = webSocketServer.connect(device.getWebSocketUrl(), device.getDeviceId());
            sessions.put(device.getDeviceId(), session);

            // 设置连接监听
            session.addMessageHandler(new DeviceWebSocketMessageHandler(device.getDeviceId()));

            future.complete(DeviceConnectionResult.success(session));

        } catch (Exception e) {
            future.completeExceptionally(new BusinessException("WEBSOCKET_CONNECT_FAILED", "WebSocket连接失败", e));
        }

        return future;
    }

    @Override
    public CompletableFuture<DeviceCommandResult> sendCommand(DeviceEntity device, DeviceCommand command) {
        WebSocketSession session = sessions.get(device.getDeviceId());
        if (session == null || !session.isOpen()) {
            return CompletableFuture.failedFuture(
                new BusinessException("WEBSOCKET_NOT_CONNECTED", "WebSocket未连接"));
        }

        try {
            String message = JsonUtils.toJson(command);
            session.getAsyncRemote().sendText(message);

            return CompletableFuture.completedFuture(
                DeviceCommandResult.success(command.getCommandId()));

        } catch (Exception e) {
            return CompletableFuture.failedFuture(
                new BusinessException("WEBSOCKET_SEND_FAILED", "WebSocket消息发送失败", e));
        }
    }

    private class DeviceWebSocketHandler implements WebSocketHandler {

        @Override
        public void onOpen(Session session, EndpointConfig config) {
            String deviceId = (String) config.getUserProperties().get("deviceId");
            log.info("WebSocket连接打开: deviceId={}", deviceId);
        }

        @Override
        public void onMessage(String message, Session session) {
            String deviceId = (String) session.getUserProperties().get("deviceId");

            try {
                DeviceDataMessage dataMessage = JsonUtils.fromJson(message, DeviceDataMessage.class);
                processDeviceData(deviceId, dataMessage);
            } catch (Exception e) {
                log.error("处理WebSocket消息失败: deviceId={}, message={}", deviceId, message, e);
            }
        }

        @Override
        public void onClose(Session session, CloseReason closeReason) {
            String deviceId = (String) session.getUserProperties().get("deviceId");
            sessions.remove(deviceId);

            log.info("WebSocket连接关闭: deviceId={}, reason={}", deviceId, closeReason);

            // 触发重连逻辑
            scheduleReconnect(deviceId);
        }

        @Override
        public void onError(Session session, Throwable thr) {
            String deviceId = (String) session.getUserProperties().get("deviceId");
            log.error("WebSocket连接错误: deviceId={}", deviceId, thr);
        }
    }
}
```

---

## 🏗️ 架构设计规范

### 四层架构实现

#### Controller层 - 接口控制层
```java
@RestController
@RequestMapping("/api/v1/device/comm")
@Tag(name = "设备通讯管理")
@Validated
public class DeviceCommController {

    @Resource
    private DeviceCommService deviceCommService;

    @PostMapping("/connect/{deviceId}")
    @Operation(summary = "连接设备")
    public ResponseDTO<Void> connectDevice(@PathVariable Long deviceId) {
        deviceCommService.connectDevice(deviceId);
        return ResponseDTO.ok();
    }

    @PostMapping("/disconnect/{deviceId}")
    @Operation(summary = "断开设备连接")
    public ResponseDTO<Void> disconnectDevice(@PathVariable Long deviceId) {
        deviceCommService.disconnectDevice(deviceId);
        return ResponseDTO.ok();
    }

    @PostMapping("/command/send")
    @Operation(summary = "发送设备命令")
    public ResponseDTO<DeviceCommandResultVO> sendCommand(@Valid @RequestBody SendCommandRequestDTO request) {
        DeviceCommandResult result = deviceCommService.sendCommand(request.getDeviceId(), request.getCommand());
        return ResponseDTO.ok(convertToVO(result));
    }

    @GetMapping("/status/{deviceId}")
    @Operation(summary = "获取设备连接状态")
    public ResponseDTO<DeviceConnectionStatusVO> getDeviceStatus(@PathVariable Long deviceId) {
        DeviceConnectionStatus status = deviceCommService.getDeviceStatus(deviceId);
        return ResponseDTO.ok(convertToVO(status));
    }
}
```

#### Service层 - 核心业务层
```java
@Service
@Transactional(rollbackFor = Exception.class)
public class DeviceCommServiceImpl implements DeviceCommService {

    @Resource
    private DeviceCommManager deviceCommManager;

    @Resource
    private DeviceDao deviceDao;

    @Resource
    private DeviceConnectionDao connectionDao;

    @Override
    public void connectDevice(Long deviceId) {
        // 业务规则验证
        DeviceEntity device = validateDevice(deviceId);

        // 核心业务逻辑
        deviceCommManager.connectDevice(device);
    }

    @Override
    public DeviceCommandResult sendCommand(Long deviceId, DeviceCommand command) {
        // 验证设备状态
        DeviceEntity device = validateDeviceConnection(deviceId);

        // 验证命令权限
        validateCommandPermission(device, command);

        // 执行命令发送
        return deviceCommManager.sendCommand(device, command);
    }

    private DeviceEntity validateDevice(Long deviceId) {
        DeviceEntity device = deviceDao.selectById(deviceId);
        if (device == null) {
            throw new BusinessException("DEVICE_NOT_FOUND", "设备不存在");
        }
        if (device.getStatus() != DeviceStatusEnum.ONLINE.getCode()) {
            throw new BusinessException("DEVICE_OFFLINE", "设备未上线");
        }
        return device;
    }

    private DeviceEntity validateDeviceConnection(Long deviceId) {
        DeviceConnectionEntity connection = connectionDao.selectByDeviceId(deviceId);
        if (connection == null || connection.getStatus() != ConnectionStatusEnum.CONNECTED.getCode()) {
            throw new BusinessException("DEVICE_NOT_CONNECTED", "设备未连接");
        }
        return deviceDao.selectById(deviceId);
    }
}
```

#### Manager层 - 复杂流程管理层
```java
// ✅ 正确：Manager类为纯Java类，不使用Spring注解
public class DeviceCommManager {

    private final DeviceDao deviceDao;
    private final DeviceConnectionDao connectionDao;
    private final DeviceProtocolAdapterManager protocolAdapterManager;
    private final DeviceMessageHandler messageHandler;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    // 构造函数注入依赖
    public DeviceCommManager(DeviceDao deviceDao, DeviceConnectionDao connectionDao,
                           DeviceProtocolAdapterManager protocolAdapterManager,
                           DeviceMessageHandler messageHandler,
                           RedisTemplate<String, Object> redisTemplate,
                           RabbitTemplate rabbitTemplate) {
        this.deviceDao = deviceDao;
        this.connectionDao = connectionDao;
        this.protocolAdapterManager = protocolAdapterManager;
        this.messageHandler = messageHandler;
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional(rollbackFor = Exception.class)
    public DeviceConnectionResult connectDevice(DeviceEntity device) {
        try {
            // 获取协议适配器
            DeviceProtocolAdapter adapter = protocolAdapterManager.getAdapter(device.getProtocolType());

            // 建立连接
            CompletableFuture<DeviceConnectionResult> connectionFuture = adapter.connect(device);
            DeviceConnectionResult result = connectionFuture.get(30, TimeUnit.SECONDS);

            if (result.isSuccess()) {
                // 更新连接状态
                updateConnectionStatus(device.getDeviceId(), ConnectionStatusEnum.CONNECTED);

                // 启动心跳检测
                startHeartbeatCheck(device);

                // 发送连接成功事件
                sendDeviceConnectedEvent(device);

                log.info("设备连接成功: deviceId={}, protocol={}", device.getDeviceId(), device.getProtocolType());
            }

            return result;

        } catch (Exception e) {
            log.error("设备连接失败: deviceId={}, error={}", device.getDeviceId(), e.getMessage(), e);

            // 更新连接状态
            updateConnectionStatus(device.getDeviceId(), ConnectionStatusEnum.DISCONNECTED);

            throw new BusinessException("DEVICE_CONNECT_FAILED", "设备连接失败", e);
        }
    }

    public DeviceCommandResult sendCommand(DeviceEntity device, DeviceCommand command) {
        try {
            // 获取协议适配器
            DeviceProtocolAdapter adapter = protocolAdapterManager.getAdapter(device.getProtocolType());

            // 发送命令
            CompletableFuture<DeviceCommandResult> commandFuture = adapter.sendCommand(device, command);
            DeviceCommandResult result = commandFuture.get(command.getTimeoutSeconds(), TimeUnit.SECONDS);

            // 记录命令执行
            recordCommandExecution(device.getDeviceId(), command, result);

            return result;

        } catch (TimeoutException e) {
            log.warn("设备命令执行超时: deviceId={}, command={}", device.getDeviceId(), command.getCommandType());

            // 记录超时
            recordCommandTimeout(device.getDeviceId(), command);

            throw new BusinessException("COMMAND_TIMEOUT", "命令执行超时");

        } catch (Exception e) {
            log.error("设备命令执行失败: deviceId={}, command={}, error={}",
                device.getDeviceId(), command.getCommandType(), e.getMessage(), e);

            // 记录失败
            recordCommandFailure(device.getDeviceId(), command, e);

            throw new BusinessException("COMMAND_EXECUTION_FAILED", "命令执行失败", e);
        }
    }

    private void updateConnectionStatus(Long deviceId, ConnectionStatusEnum status) {
        DeviceConnectionEntity connection = connectionDao.selectByDeviceId(deviceId);
        if (connection != null) {
            connection.setStatus(status.getCode());
            connection.setUpdateTime(LocalDateTime.now());
            connectionDao.updateById(connection);
        }
    }

    private void startHeartbeatCheck(DeviceEntity device) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkDeviceHeartbeat(device);
            } catch (Exception e) {
                log.error("设备心跳检查失败: deviceId={}", device.getDeviceId(), e);
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    private void checkDeviceHeartbeat(DeviceEntity device) {
        try {
            DeviceProtocolAdapter adapter = protocolAdapterManager.getAdapter(device.getProtocolType());

            // 发送心跳命令
            DeviceCommand heartbeatCommand = DeviceCommand.builder()
                .commandType("HEARTBEAT")
                .timeoutSeconds(10)
                .build();

            CompletableFuture<DeviceCommandResult> heartbeatFuture = adapter.sendCommand(device, heartbeatCommand);
            DeviceCommandResult result = heartbeatFuture.get(10, TimeUnit.SECONDS);

            if (!result.isSuccess()) {
                // 心跳失败，标记设备离线
                handleDeviceOffline(device);
            }

        } catch (Exception e) {
            // 心跳异常，标记设备离线
            handleDeviceOffline(device);
        }
    }

    private void handleDeviceOffline(DeviceEntity device) {
        log.warn("设备离线: deviceId={}", device.getDeviceId());

        // 更新连接状态
        updateConnectionStatus(device.getDeviceId(), ConnectionStatusEnum.DISCONNECTED);

        // 更新设备状态
        device.setStatus(DeviceStatusEnum.OFFLINE.getCode());
        deviceDao.updateById(device);

        // 发送设备离线事件
        sendDeviceOfflineEvent(device);

        // 断开连接
        try {
            DeviceProtocolAdapter adapter = protocolAdapterManager.getAdapter(device.getProtocolType());
            adapter.disconnect(device.getDeviceId());
        } catch (Exception e) {
            log.error("断开设备连接失败: deviceId={}", device.getDeviceId(), e);
        }
    }

    private void sendDeviceConnectedEvent(DeviceEntity device) {
        DeviceConnectedEvent event = DeviceConnectedEvent.builder()
            .deviceId(device.getDeviceId())
            .deviceName(device.getDeviceName())
            .protocolType(device.getProtocolType())
            .timestamp(LocalDateTime.now())
            .build();

        rabbitTemplate.convertAndSend("device.connected", event);
    }

    private void sendDeviceOfflineEvent(DeviceEntity device) {
        DeviceOfflineEvent event = DeviceOfflineEvent.builder()
            .deviceId(device.getDeviceId())
            .deviceName(device.getDeviceName())
            .protocolType(device.getProtocolType())
            .timestamp(LocalDateTime.now())
            .build();

        rabbitTemplate.convertAndSend("device.offline", event);
    }
}
```

#### DAO层 - 数据访问层
```java
@Mapper
public interface DeviceDao extends BaseMapper<DeviceEntity> {

    @Transactional(readOnly = true)
    DeviceEntity selectByDeviceCode(@Param("deviceCode") String deviceCode);

    @Transactional(readOnly = true)
    List<DeviceEntity> selectByProtocolType(@Param("protocolType") String protocolType);

    @Transactional(rollbackFor = Exception.class)
    int updateStatus(@Param("deviceId") Long deviceId, @Param("status") Integer status);

    @Transactional(rollbackFor = Exception.class)
    int updateLastHeartbeatTime(@Param("deviceId") Long deviceId,
                               @Param("heartbeatTime") LocalDateTime heartbeatTime);

    @Select("SELECT * FROM t_common_device WHERE status = 1 AND deleted_flag = 0 " +
            "ORDER BY last_heartbeat_time DESC LIMIT #{limit}")
    List<DeviceEntity> selectRecentActiveDevices(@Param("limit") int limit);

    @Transactional(readOnly = true)
    List<DeviceEntity> selectDevicesNeedingHeartbeatCheck(@Param("lastCheckTime") LocalDateTime lastCheckTime);
}

@Mapper
public interface DeviceConnectionDao extends BaseMapper<DeviceConnectionEntity> {

    @Transactional(readOnly = true)
    DeviceConnectionEntity selectByDeviceId(@Param("deviceId") Long deviceId);

    @Transactional(readOnly = true)
    List<DeviceConnectionEntity> selectByStatus(@Param("status") Integer status);

    @Transactional(rollbackFor = Exception.class)
    int updateStatus(@Param("deviceId") Long deviceId, @Param("status") Integer status);

    @Transactional(rollbackFor = Exception.class)
    int updateConnectInfo(@Param("deviceId") Long deviceId,
                         @Param("connectTime") LocalDateTime connectTime,
                         @Param("disconnectTime") LocalDateTime disconnectTime,
                         @Param("status") Integer status);
}
```

---

## 📊 技能质量指标体系

### 核心质量指标
| 指标名称 | 目标值 | 说明 | 测量方法 |
|---------|--------|------|----------|
| **设备连接成功率** | ≥98% | 设备连接建立成功比例 | 连接成功率监控 |
| **命令执行准确率** | ≥99% | 设备命令执行成功率 | 命令执行监控 |
| **消息传输延迟** | ≤100ms | 设备消息传输延迟 | 延迟监控 |
| **连接保活率** | ≥95% | 设备连接保持稳定比例 | 连接稳定性监控 |
| **并发连接数** | ≥10000 | 同时支持设备连接数 | 并发性能测试 |

### 性能指标
| 指标名称 | 目标值 | 说明 | 测量方法 |
|---------|--------|------|----------|
| **设备接入响应时间** | ≤2s | 新设备接入响应时间 | 接入性能测试 |
| **命令响应时间** | ≤1s | 设备命令响应时间 | 命令性能测试 |
| **消息吞吐量** | ≥10000/秒 | 消息处理吞吐量 | 吞吐量测试 |
| **系统可用性** | ≥99.9% | 服务可用性比例 | 可用性监控 |

### 版本管理
- **主版本**: v1.0.0 - 初始版本
- **文档版本**: v2.0.0 - IOE-DREAM七微服务专业版
- **创建时间**: 2025-12-08
- **最后更新**: 2025-12-08
- **变更类型**: MAJOR - 新技能创建

---

## 🛠️ 开发规范和最佳实践

### 连接管理最佳实践
```java
// ✅ 正确的连接管理
@Component
public class DeviceConnectionManager {

    private final Map<Long, DeviceConnection> connections = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatScheduler = Executors.newScheduledThreadPool(2);

    public void manageConnection(Long deviceId, DeviceConnection connection) {
        connections.put(deviceId, connection);

        // 启动心跳检测
        startHeartbeat(deviceId);

        // 设置断线重连
        setupReconnect(deviceId);
    }

    private void startHeartbeat(Long deviceId) {
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            DeviceConnection connection = connections.get(deviceId);
            if (connection != null && !connection.isAlive()) {
                handleConnectionLost(deviceId);
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    // 正确的资源清理
    @PreDestroy
    public void destroy() {
        connections.values().forEach(DeviceConnection::close);
        heartbeatScheduler.shutdown();
    }
}
```

### 异常处理最佳实践
```java
// ✅ 正确的异常处理
@Service
public class DeviceCommServiceImpl implements DeviceCommService {

    public DeviceCommandResult sendCommand(Long deviceId, DeviceCommand command) {
        try {
            return deviceCommManager.sendCommand(deviceId, command);
        } catch (DeviceConnectionException e) {
            log.warn("设备连接异常: deviceId={}", deviceId, e);
            throw new BusinessException("DEVICE_CONNECTION_ERROR", "设备连接异常", e);
        } catch (CommandTimeoutException e) {
            log.warn("命令执行超时: deviceId={}, command={}", deviceId, command.getCommandType());
            throw new BusinessException("COMMAND_TIMEOUT", "命令执行超时", e);
        } catch (Exception e) {
            log.error("设备命令执行失败: deviceId={}, command={}", deviceId, command.getCommandType(), e);
            throw new SystemException("DEVICE_COMMAND_ERROR", "设备命令执行失败", e);
        }
    }
}
```

---

## 🔗 相关文档参考

### 核心架构文档
- **📋 CLAUDE.md**: 全局架构规范 (强制遵循)
- **🏗️ 四层架构详解**: Controller→Service→Manager→DAO架构模式
- **🔧 依赖注入规范**: 统一使用@Resource注解
- **📦 DAO层规范**: 统一使用Dao后缀和@Mapper注解

### 技术栈文档
- **Spring Boot 3.5.8**: 微服务框架文档
- **Netty**: 网络通讯框架文档
- **WebSocket**: 实时通讯协议文档
- **MQTT**: 物联网通讯协议文档

### 设备协议文档
- **Modbus**: 工业通讯协议文档
- **OPC-UA**: 工业自动化通讯协议
- **HTTP/HTTPS**: Web协议文档
- **TCP/UDP**: 网络传输协议文档

---

**📋 重要提醒**:
1. 本技能严格遵循IOE-DREAM四层架构规范
2. 所有代码示例使用Jakarta EE 3.0+包名规范
3. 统一使用@Resource依赖注入，禁止使用@Autowired
4. 统一使用@Mapper注解和Dao后缀命名
5. 必须支持多种设备协议和连接方式
6. 重点关注高并发、高可用的设备通讯架构

**让我们一起建设稳定、高效的设备通讯体系！** 🚀

---
**文档版本**: v2.0.0 - IOE-DREAM七微服务专业版
**创建时间**: 2025-12-08
**最后更新**: 2025-12-08
**技能等级**: ★★★★★ (顶级专家)
**适用架构**: Spring Boot 3.5.8 + Netty + WebSocket + MQTT