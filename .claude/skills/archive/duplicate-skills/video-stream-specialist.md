# 视频流技术专家 (Video Stream Specialist)

**技能等级**: ★★★ 高级
**适用角色**: 视频监控工程师、流媒体开发工程师、IOT视频系统架构师
**前置技能**: 多协议设备适配专家、实时通讯技术、音视频编解码、网络传输优化
**预计学时**: 45小时

---

## 📋 技能概述

视频流技术专家专注于企业级视频监控系统的核心技术实现，包括RTSP/WebRTC/RTMP等流媒体协议、H.264/H.265视频编解码、实时音视频传输、云台控制(PTZ)、视频存储与回放等关键技术的深度应用。基于IOE-DREAM视频监控业务需求，掌握百万级视频设备接入的企业级解决方案。

---

## 🎯 核心能力要求

### 🎥 视频流协议掌握
- **RTSP协议**: 实时流传输协议、会话管理、RTP/RTCP传输控制
- **WebRTC协议**: 浏览器实时通讯、P2P连接、媒体协商、NAT穿透
- **RTMP协议**: 实时消息传输协议、直播推流、流媒体分发
- **ONVIF协议**: IP摄像头标准、设备发现、PTZ控制、媒体配置
- **HLS/DASH**: HTTP自适应流媒体、分片传输、码率自适应

### 🎬 音视频编解码
- **H.264/H.265**: 视频压缩标准、编码优化、质量控制
- **AAC/MP3**: 音频压缩编码、声道配置、比特率控制
- **FFmpeg**: 音视频转码、格式转换、滤镜处理
- **GStreamer**: 流媒体管道、实时处理、插件扩展
- **WebCodecs**: 浏览器原生编解码、硬件加速

### 🌐 网络传输优化
- **CDN分发**: 内容分发网络、边缘节点、缓存策略
- **QoS控制**: 服务质量控制、带宽管理、优先级调度
- **负载均衡**: 流媒体负载、连接调度、故障转移
- **P2P网络**: 点对点传输、网络拓扑优化、带宽节省

---

## 🛠️ 操作步骤

### 第一阶段：RTSP视频流处理 (12小时)

#### 1.1 RTSP服务器搭建
**目标**: 实现企业级RTSP流媒体服务器

**操作步骤**:
```java
// 1. 企业级RTSP服务器实现
@Component
@Slf4j
public class EnterpriseRTSPServer {

    @Resource
    private VideoStreamManager streamManager;

    @Resource
    private ConnectionPoolManager connectionPool;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap bootstrap;

    @PostConstruct
    public void initialize() throws InterruptedException {
        // 创建事件循环组
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());

        // 配置服务器
        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new RTSPServerInitializer())
            .option(ChannelOption.SO_BACKLOG, 1024)
            .childOption(ChannelOption.SO_KEEPALIVE, true);

        // 启动服务器
        ChannelFuture future = bootstrap.bind(8554).sync();
        log.info("RTSP服务器启动成功，监听端口: 8554");
    }

    /**
     * RTSP协议处理器
     */
    public class RTSPServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();

            // RTSP解码器
            pipeline.addLast("rtspDecoder", new RTSPDecoder());
            pipeline.addLast("rtspEncoder", new RTSPEncoder());

            // HTTP隧道支持
            pipeline.addLast("httpTunnel", new HttpTunnelHandler());

            // 视频流处理
            pipeline.addLast("streamHandler", new RTSPStreamHandler());

            // 异常处理
            pipeline.addLast("exceptionHandler", new RTSPExceptionHandler());
        }
    }

    /**
     * RTSP流处理器
     */
    public class RTSPStreamHandler extends SimpleChannelInboundHandler<RTSPRequest> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RTSPRequest request) {
            try {
                switch (request.getMethod()) {
                    case OPTIONS:
                        handleOptions(ctx, request);
                        break;
                    case DESCRIBE:
                        handleDescribe(ctx, request);
                        break;
                    case SETUP:
                        handleSetup(ctx, request);
                        break;
                    case PLAY:
                        handlePlay(ctx, request);
                        break;
                    case PAUSE:
                        handlePause(ctx, request);
                        break;
                    case TEARDOWN:
                        handleTeardown(ctx, request);
                        break;
                    default:
                        sendErrorResponse(ctx, 405, "Method Not Allowed");
                }
            } catch (Exception e) {
                log.error("RTSP请求处理失败", e);
                sendErrorResponse(ctx, 500, "Internal Server Error");
            }
        }
    }
}
```

#### 1.2 视频流管理器
**目标**: 实现高性能视频流管理

**操作步骤**:
```java
// 2. 企业级视频流管理器
@Component
public class VideoStreamManager {

    private final Map<String, VideoStream> activeStreams = new ConcurrentHashMap<>();
    private final Map<String, StreamStatistics> streamStats = new ConcurrentHashMap<>();
    private final ScheduledExecutorService streamExecutor = Executors.newScheduledThreadPool(10);

    /**
     * 创建视频流
     */
    public CompletableFuture<StreamResult> createStream(String deviceId, StreamConfig config) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 验证设备
                VideoDevice device = validateDevice(deviceId);
                if (device == null) {
                    return StreamResult.failure("设备不存在: " + deviceId);
                }

                // 2. 配置流参数
                StreamConfig streamConfig = configureStream(device, config);

                // 3. 建立RTSP连接
                RTSPConnection connection = establishRTSPConnection(device, streamConfig);

                // 4. 创建视频流
                VideoStream videoStream = VideoStream.builder()
                    .streamId(generateStreamId(deviceId))
                    .deviceId(deviceId)
                    .connection(connection)
                    .config(streamConfig)
                    .status(StreamStatus.INITIALIZING)
                    .createTime(System.currentTimeMillis())
                    .build();

                // 5. 启动流处理
                startStreamProcessing(videoStream);

                // 6. 注册流管理
                activeStreams.put(videoStream.getStreamId(), videoStream);
                streamStats.put(videoStream.getStreamId(), new StreamStatistics());

                // 7. 启动监控
                startStreamMonitoring(videoStream);

                return StreamResult.success(videoStream.getStreamId());

            } catch (Exception e) {
                log.error("创建视频流失败: {}", deviceId, e);
                return StreamResult.failure(e.getMessage());
            }
        });
    }

    /**
     * 启动流处理
     */
    private void startStreamProcessing(VideoStream videoStream) {
        CompletableFuture.runAsync(() -> {
            try {
                RTSPConnection connection = videoStream.getConnection();

                // 发送DESCRIBE请求
                RTSPRequest describeRequest = RTSPRequest.builder()
                    .method(RTSPMethod.DESCRIBE)
                    .uri(videoStream.getConfig().getStreamUri())
                    .build();

                RTSPResponse describeResponse = connection.sendRequest(describeRequest);

                if (describeResponse.getStatusCode() == 200) {
                    // 解析SDP信息
                    SDPInfo sdpInfo = parseSDP(describeResponse.getContent());

                    // 发送SETUP请求
                    setupVideoStream(videoStream, sdpInfo);

                    // 发送PLAY请求
                    playVideoStream(videoStream);

                    // 更新流状态
                    videoStream.setStatus(StreamStatus.PLAYING);
                    videoStream.setStartTime(System.currentTimeMillis());
                }

            } catch (Exception e) {
                log.error("视频流处理失败: {}", videoStream.getStreamId(), e);
                videoStream.setStatus(StreamStatus.ERROR);
                videoStream.setErrorMessage(e.getMessage());
            }
        });
    }

    /**
     * 视频流质量监控
     */
    @Scheduled(fixedRate = 5000)
    public void monitorStreamQuality() {
        activeStreams.values().parallelStream().forEach(stream -> {
            try {
                StreamStatistics stats = streamStats.get(stream.getStreamId());
                if (stats != null) {
                    // 计码率监控
                    calculateBitrate(stream, stats);

                    // 丢包率监控
                    calculatePacketLoss(stream, stats);

                    // 延迟监控
                    calculateLatency(stream, stats);

                    // 连接状态监控
                    monitorConnectionHealth(stream, stats);

                    // 质量告警
                    checkQualityAlerts(stream, stats);
                }
            } catch (Exception e) {
                log.error("流质量监控失败: {}", stream.getStreamId(), e);
            }
        });
    }
}
```

**质量要求**:
- ✅ 流处理能力：支持1000+并发视频流
- ✅ 延迟控制：端到端延迟 < 200ms
- ✅ 稳定性：流断线重连时间 < 5秒
- ✅ 资源优化：CPU使用率 < 30%，内存使用 < 2GB

### 第二阶段：WebRTC实时通讯 (10小时)

#### 2.1 WebRTC信令服务器
**目标**: 实现WebRTC信令协商和媒体传输

**操作步骤**:
```java
// 3. WebRTC信令服务器实现
@Component
@Slf4j
public class WebRTCSignalingServer {

    @Resource
    private WebSocketSessionManager sessionManager;

    @Resource
    private PeerConnectionManager peerConnectionManager;

    /**
     * WebSocket信令处理
     */
    @OnOpen
    public void onOpen(Session session) {
        sessionManager.addSession(session.getId(), session);
        log.info("WebRTC会话建立: {}", session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            // 解析信令消息
            WebRTCSignal signal = WebRTCSignal.fromJson(message);

            switch (signal.getType()) {
                case OFFER:
                    handleOffer(session, signal);
                    break;
                case ANSWER:
                    handleAnswer(session, signal);
                    break;
                case ICE_CANDIDATE:
                    handleIceCandidate(session, signal);
                    break;
                case HANGUP:
                    handleHangup(session, signal);
                    break;
                default:
                    log.warn("未知信令类型: {}", signal.getType());
            }
        } catch (Exception e) {
            log.error("WebRTC信令处理失败", e);
            sendError(session, "信令处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理Offer信令
     */
    private void handleOffer(Session session, WebRTCSignal signal) {
        CompletableFuture.runAsync(() -> {
            try {
                String roomId = signal.getRoomId();
                String userId = signal.getUserId();

                // 获取或创建房间
                WebRTCRoom room = getOrCreateRoom(roomId);

                // 创建PeerConnection
                PeerConnection peerConnection = peerConnectionManager.createPeerConnection(
                    userId, roomId);

                // 设置远程描述
                SessionDescription remoteDesc = new SessionDescription(
                    SessionDescription.Type.OFFER,
                    signal.getSdp());

                peerConnection.setRemoteDescription(remoteDesc);

                // 创建应答
                SessionDescription answer = peerConnection.createAnswer();

                // 设置本地描述
                peerConnection.setLocalDescription(answer);

                // 发送Answer信令
                WebRTCSignal answerSignal = WebRTCSignal.builder()
                    .type(SignalType.ANSWER)
                    .roomId(roomId)
                    .userId(userId)
                    .sdp(answer.getDescription())
                    .build();

                sendMessage(session, answerSignal);

                // 添加到房间
                room.addParticipant(userId, peerConnection);

            } catch (Exception e) {
                log.error("处理Offer信令失败", e);
                sendError(session, "Offer处理失败: " + e.getMessage());
            }
        });
    }

    /**
     * 处理ICE候选
     */
    private void handleIceCandidate(Session session, WebRTCSignal signal) {
        CompletableFuture.runAsync(() -> {
            try {
                String roomId = signal.getRoomId();
                String userId = signal.getUserId();

                WebRTCRoom room = getRoom(roomId);
                if (room != null) {
                    // 转发ICE候选给房间内其他用户
                    for (String otherUserId : room.getParticipants()) {
                        if (!otherUserId.equals(userId)) {
                            PeerConnection otherPeer = room.getPeerConnection(otherUserId);
                            if (otherPeer != null) {
                                IceCandidate candidate = new IceCandidate(
                                    signal.getCandidate().getSdpMid(),
                                    signal.getCandidate().getSdpMLineIndex(),
                                    signal.getCandidate().getCandidate());

                                otherPeer.addIceCandidate(candidate);

                                // 转发信令
                                Session otherSession = sessionManager.getSession(otherUserId);
                                if (otherSession != null && otherSession.isOpen()) {
                                    sendMessage(otherSession, signal);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("处理ICE候选失败", e);
            }
        });
    }
}
```

#### 2.2 PeerConnection管理器
**目标**: 实现WebRTC点对点连接管理

**操作步骤**:
```java
// 4. WebRTC PeerConnection管理器
@Component
public class PeerConnectionManager {

    private final Map<String, PeerConnection> peerConnections = new ConcurrentHashMap<>();
    private final Map<String, WebRTCRoom> rooms = new ConcurrentHashMap<>();

    @Resource
    private MediaStreamManager mediaStreamManager;

    /**
     * 创建PeerConnection
     */
    public PeerConnection createPeerConnection(String userId, String roomId) {
        try {
            // 创建PeerConnectionFactory
            PeerConnectionFactory factory = new PeerConnectionFactory();

            // 创建PeerConnection
            PeerConnection peerConnection = factory.createPeerConnection(
                createPeerConnectionObserver(userId, roomId));

            // 添加媒体流
            addMediaStreams(peerConnection);

            // 缓存连接
            String connectionId = generateConnectionId(userId, roomId);
            peerConnections.put(connectionId, peerConnection);

            return peerConnection;

        } catch (Exception e) {
            log.error("创建PeerConnection失败: userId={}, roomId={}", userId, roomId, e);
            throw new WebRTCException("PeerConnection创建失败", e);
        }
    }

    /**
     * 创建连接观察者
     */
    private PeerConnection.Observer createPeerConnectionObserver(String userId, String roomId) {
        return new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState newState) {
                log.info("信令状态变更: userId={}, roomId={}, state={}",
                    userId, roomId, newState);
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState newState) {
                log.info("ICE连接状态变更: userId={}, roomId={}, state={}",
                    userId, roomId, newState);

                if (newState == PeerConnection.IceConnectionState.CONNECTED) {
                    onPeerConnected(userId, roomId);
                } else if (newState == PeerConnection.IceConnectionState.DISCONNECTED) {
                    onPeerDisconnected(userId, roomId);
                }
            }

            @Override
            public void onIceCandidate(IceCandidate candidate) {
                log.debug("ICE候选: userId={}, roomId={}, candidate={}",
                    userId, roomId, candidate);

                // 转发ICE候选
                forwardIceCandidate(userId, roomId, candidate);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                log.info("收到媒体流: userId={}, roomId={}, stream={}",
                    userId, roomId, mediaStream.getId());

                // 处理远端媒体流
                handleRemoteMediaStream(userId, roomId, mediaStream);
            }

            @Override
            public void onIceConnectionReceivingChange(PeerConnection.IceConnectionReceivingState newState) {
                log.debug("ICE接收状态变更: userId={}, roomId={}, state={}",
                    userId, roomId, newState);
            }
        };
    }

    /**
     * 性能监控
     */
    @Scheduled(fixedRate = 10000)
    public void monitorPeerConnections() {
        int totalConnections = peerConnections.size();
        int connectedCount = (int) peerConnections.values().stream()
            .map(pc -> pc.getIceConnectionState())
            .filter(state -> state == PeerConnection.IceConnectionState.CONNECTED)
            .count();

        double connectionRate = totalConnections > 0 ? (double) connectedCount / totalConnections : 0;

        log.info("WebRTC连接状态 - 总连接: {}, 已连接: {}, 连接率: {:.2%}",
            totalConnections, connectedCount, connectionRate * 100);

        // 性能告警
        if (connectionRate < 0.8) {
            log.warn("WebRTC连接率过低: {:.2%}", connectionRate * 100);
        }
    }
}
```

**质量要求**:
- ✅ 并发连接：支持10000+同时在线PeerConnection
- ✅ 连接建立时间：P95 < 3秒
- ✅ 视频质量：支持720p/1080p实时传输
- ✅ 延迟控制：端到端延迟 < 150ms

### 第三阶段：视频存储与回放 (10小时)

#### 3.1 视频录制服务
**目标**: 实现企业级视频录制和存储

**操作步骤**:
```java
// 5. 企业级视频录制服务
@Component
public class VideoRecordingService {

    @Resource
    private VideoStorageManager storageManager;

    @Resource
    private VideoTranscoder transcoder;

    @Resource
    private RecordingScheduler recordingScheduler;

    /**
     * 开始视频录制
     */
    public CompletableFuture<RecordingResult> startRecording(String deviceId, RecordingConfig config) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 验证录制配置
                validateRecordingConfig(config);

                // 2. 创建录制会话
                RecordingSession session = RecordingSession.builder()
                    .sessionId(generateSessionId())
                    .deviceId(deviceId)
                    .config(config)
                    .status(RecordingStatus.INITIALIZING)
                    .startTime(System.currentTimeMillis())
                    .build();

                // 3. 配置录制参数
                VideoCapture capture = setupVideoCapture(deviceId, config);

                // 4. 开始录制
                capture.startRecording(session);

                // 5. 存储管理
                storageManager.allocateStorage(session);

                // 6. 转码任务
                if (config.isEnableTranscoding()) {
                    scheduleTranscoding(session);
                }

                // 7. 录制调度
                recordingScheduler.scheduleRecording(session);

                return RecordingResult.success(session.getSessionId());

            } catch (Exception e) {
                log.error("开始视频录制失败: {}", deviceId, e);
                return RecordingResult.failure(e.getMessage());
            }
        });
    }

    /**
     * 实时视频处理
     */
    private void processRealTimeVideo(String deviceId, byte[] frameData, RecordingSession session) {
        CompletableFuture.runAsync(() -> {
            try {
                // 1. 视频帧解码
                VideoFrame frame = decodeFrame(frameData, session.getConfig());

                // 2. 录制存储
                if (session.getStatus() == RecordingStatus.RECORDING) {
                    storageManager.storeFrame(session, frame);
                }

                // 3. 实时转码
                if (session.getConfig().isRealTimeTranscoding()) {
                    transcoder.transcodeFrame(frame, session);
                }

                // 4. 运动检测
                if (session.getConfig().isMotionDetection()) {
                    detectMotion(frame, session);
                }

                // 5. 录制统计
                updateRecordingStatistics(session, frame);

            } catch (Exception e) {
                log.error("实时视频处理失败: {}", deviceId, e);
                handleRecordingError(session, e);
            }
        });
    }

    /**
     * 录制存储管理
     */
    @Component
    public static class VideoStorageManager {

        @Resource
        private FileStorageService fileStorage;

        @Resource
        private CloudStorageService cloudStorage;

        private final Map<String, StorageSession> storageSessions = new ConcurrentHashMap<>();

        /**
         * 分配存储空间
         */
        public void allocateStorage(RecordingSession session) {
            try {
                String storagePath = buildStoragePath(session);

                StorageSession storageSession = StorageSession.builder()
                    .sessionId(session.getSessionId())
                    .storagePath(storagePath)
                    .totalCapacity(calculateRequiredCapacity(session))
                    .usedCapacity(0)
                    .createTime(System.currentTimeMillis())
                    .build();

                storageSessions.put(session.getSessionId(), storageSession);

                // 创建本地存储目录
                createStorageDirectory(storagePath);

                log.info("录制存储分配完成: sessionId={}, path={}",
                    session.getSessionId(), storagePath);

            } catch (Exception e) {
                log.error("存储分配失败: {}", session.getSessionId(), e);
                throw new StorageException("存储分配失败", e);
            }
        }

        /**
         * 存储视频帧
         */
        public void storeFrame(RecordingSession session, VideoFrame frame) {
            try {
                StorageSession storageSession = storageSessions.get(session.getSessionId());
                if (storageSession == null) {
                    return;
                }

                // 生成文件名
                String fileName = generateFrameFileName(session, frame);
                String filePath = storageSession.getStoragePath() + "/" + fileName;

                // 存储视频帧
                fileStorage.storeFrame(frame, filePath);

                // 更新存储使用情况
                storageSession.setUsedCapacity(storageSession.getUsedCapacity() + frame.getSize());

                // 云存储备份
                if (session.getConfig().isCloudBackup()) {
                    cloudStorage.backupFile(filePath);
                }

            } catch (Exception e) {
                log.error("视频帧存储失败: {}", session.getSessionId(), e);
            }
        }

        /**
         * 存储清理
         */
        @Scheduled(fixedRate = 3600000) // 每小时执行
        public void cleanupStorage() {
            storageSessions.values().parallelStream().forEach(session -> {
                try {
                    // 清理过期的录制文件
                    cleanupExpiredFiles(session);

                    // 检查存储空间
                    checkStorageSpace(session);

                } catch (Exception e) {
                    log.error("存储清理失败: {}", session.getSessionId(), e);
                }
            });
        }
    }
}
```

**质量要求**:
- ✅ 录制质量：支持4K/30fps高质量录制
- ✅ 存储效率：H.265编码，存储空间节省50%
- ✅ 存储容量：支持PB级海量存储
- ✅ 检索速度：视频检索响应时间 < 1秒

### 第四阶段：系统集成与优化 (8小时)

#### 4.1 视频监控系统集成
**目标**: 实现完整的视频监控系统集成

**操作步骤**:
```java
// 6. 视频监控系统集成控制器
@RestController
@RequestMapping("/api/video")
@Slf4j
@Validated
public class VideoMonitoringController {

    @Resource
    private VideoStreamService streamService;

    @Resource
    private VideoRecordingService recordingService;

    @Resource
    private VideoPlaybackService playbackService;

    @Resource
    private PTZControlService ptzService;

    /**
     * 获取视频流URL
     */
    @GetMapping("/stream/{deviceId}")
    @Operation(summary = "获取视频流URL", description = "获取指定设备的视频播放地址")
    public ResponseDTO<StreamURLResponse> getStreamUrl(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "RTSP") String protocol,
            @RequestParam(defaultValue = "MAIN") String channel) {

        try {
            StreamURLResponse streamUrl = streamService.getStreamUrl(deviceId, protocol, channel);
            return ResponseDTO.ok(streamUrl);

        } catch (Exception e) {
            log.error("获取视频流URL失败: deviceId={}", deviceId, e);
            return ResponseDTO.error(ResponseCode.ERROR, "获取视频流失败: " + e.getMessage());
        }
    }

    /**
     * 开始录制
     */
    @PostMapping("/recording/{deviceId}/start")
    @Operation(summary = "开始录制", description = "开始录制指定设备的视频")
    public ResponseDTO<RecordingResponse> startRecording(
            @PathVariable String deviceId,
            @Valid @RequestBody RecordingRequest recordingRequest) {

        try {
            RecordingResponse recording = recordingService.startRecording(deviceId, recordingRequest);
            return ResponseDTO.ok(recording);

        } catch (Exception e) {
            log.error("开始录制失败: deviceId={}", deviceId, e);
            return ResponseDTO.error(ResponseCode.ERROR, "开始录制失败: " + e.getMessage());
        }
    }

    /**
     * PTZ控制
     */
    @PostMapping("/ptz/{deviceId}/control")
    @Operation(summary = "PTZ控制", description = "控制摄像头的云台转动")
    public ResponseDTO<PTZResponse> controlPTZ(
            @PathVariable String deviceId,
            @Valid @RequestBody PTZRequest ptzRequest) {

        try {
            PTZResponse ptzResponse = ptzService.controlPTZ(deviceId, ptzRequest);
            return ResponseDTO.ok(ptzResponse);

        } catch (Exception e) {
            log.error("PTZ控制失败: deviceId={}", deviceId, e);
            return ResponseDTO.error(ResponseCode.ERROR, "PTZ控制失败: " + e.getMessage());
        }
    }

    /**
     * 视频回放
     */
    @GetMapping("/playback/{recordingId}")
    @Operation(summary = "视频回放", description = "回放指定的录制视频")
    public ResponseDTO<PlaybackURLResponse> getPlaybackUrl(
            @PathVariable String recordingId,
            @RequestParam(defaultValue = "MP4") String format,
            @RequestParam(defaultValue = "0") long startTime,
            @RequestParam(defaultValue = "-1") long endTime) {

        try {
            PlaybackURLResponse playbackUrl = playbackService.getPlaybackUrl(
                recordingId, format, startTime, endTime);
            return ResponseDTO.ok(playbackUrl);

        } catch (Exception e) {
            log.error("获取回放URL失败: recordingId={}", recordingId, e);
            return ResponseDTO.error(ResponseCode.ERROR, "获取回放URL失败: " + e.getMessage());
        }
    }
}
```

#### 4.2 性能监控与优化
**目标**: 建立完整的视频监控系统性能监控

**操作步骤**:
```java
// 7. 视频系统性能监控
@Component
public class VideoSystemMonitor {

    @Resource
    private MeterRegistry meterRegistry;

    private final Timer streamProcessingTimer;
    private final Counter streamErrorCounter;
    private final Gauge activeStreamsGauge;
    private final Gauge totalBandwidthGauge;

    public VideoSystemMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.streamProcessingTimer = Timer.builder("video.stream.processing.time")
            .description("视频流处理时间")
            .register(meterRegistry);
        this.streamErrorCounter = Counter.builder("video.stream.errors")
            .description("视频流错误次数")
            .register(meterRegistry);
        this.activeStreamsGauge = Gauge.builder("video.streams.active")
            .description("活跃视频流数量")
            .register(meterRegistry, this, VideoSystemMonitor::getActiveStreamCount);
        this.totalBandwidthGauge = Gauge.builder("video.bandwidth.total")
            .description("总带宽使用")
            .register(meterRegistry, this, VideoSystemMonitor::getTotalBandwidth);
    }

    /**
     * 记录流处理时间
     */
    public void recordStreamProcessingTime(String streamType, long duration) {
        streamProcessingTimer.record(duration, TimeUnit.MILLISECONDS,
            Tags.of("type", streamType));
    }

    /**
     * 记录流错误
     */
    public void recordStreamError(String streamType, String errorType) {
        streamErrorCounter.increment(Tags.of("type", streamType, "error", errorType));
    }

    /**
     * 性能告警
     */
    @Scheduled(fixedRate = 60000)
    public void checkPerformanceAlerts() {
        // 检查活跃流数量
        int activeStreams = getActiveStreamCount();
        if (activeStreams > 1000) {
            log.warn("活跃视频流数量过多: {}, 建议扩容", activeStreams);
            sendAlert("高并发告警", "活跃视频流数量: " + activeStreams);
        }

        // 检查错误率
        double errorRate = calculateErrorRate();
        if (errorRate > 0.05) { // 5%
            log.warn("视频流错误率过高: {:.2%}%, 建议检查网络和设备状态", errorRate * 100);
            sendAlert("错误率告警", "视频流错误率: " + String.format("%.2f%%", errorRate * 100));
        }

        // 检查带宽使用
        double totalBandwidth = getTotalBandwidth();
        if (totalBandwidth > 1000 * 1024 * 1024) { // 1GB/s
            log.warn("带宽使用过高: {:.2f}GB/s, 建议优化传输策略", totalBandwidth / 1024 / 1024 / 1024);
            sendAlert("带宽告警", "带宽使用: " + String.format("%.2fGB/s", totalBandwidth / 1024 / 1024 / 1024));
        }
    }
}
```

**质量要求**:
- ✅ 系统监控：100%覆盖关键性能指标
- ✅ 告警及时：异常发现到告警时间 < 1分钟
- ✅ 扩展性：支持水平扩展和负载均衡
- ✅ 可用性：系统可用性 ≥ 99.9%

---

## ⚠️ 注意事项

### 🔐 安全要求
- **访问控制**: 视频流访问需要严格的权限验证
- **数据加密**: 敏感视频数据传输需要端到端加密
- **隐私保护**: 录像数据存储需要符合隐私保护法规
- **审计日志**: 完整的视频操作审计日志

### 🚀 性能要求
- **延迟控制**: 实时视频延迟 < 200ms
- **带宽优化**: 采用自适应码率和网络优化
- **存储效率**: 视频压缩比 ≥ 100:1
- **并发处理**: 支持1000+并发视频流

### 🛡️ 可靠性要求
- **故障恢复**: 自动故障检测和恢复机制
- **数据完整性**: 视频数据完整性校验和修复
- **备份策略**: 多层次数据备份和灾难恢复
- **负载均衡**: 智能负载均衡和流量调度

---

## 📊 评估标准

### 操作时间要求
- **视频流接入**: 30分钟/设备类型
- **WebRTC集成**: 2天/系统
- **录制系统**: 3天/完整实现
- **性能优化**: 1天/模块

### 技术指标要求
- **视频质量**: 支持4K/30fps实时传输
- **并发能力**: 1000+并发视频流
- **存储效率**: H.265编码压缩比 ≥ 100:1
- **检索速度**: 视频检索响应时间 < 1秒

### 质量标准
- **系统可用性**: ≥ 99.9%
- **视频延迟**: P95 < 200ms
- **存储空间**: 支持PB级扩展
- **检索性能**: 1秒内响应

---

## 🎯 应用场景

### 典型应用场景
1. **实时监控**: 企业安防、工厂监控、园区管理
2. **视频会议**: 远程会议、在线教育、医疗诊断
3. **直播推流**: 活动直播、产品发布、在线教育
4. **录像存储**: 监控录像、证据保全、合规存储

### 最佳实践示例
```java
// 最佳实践：自适应码率控制
@Component
public class AdaptiveBitrateController {

    /**
     * 自适应码率调整
     */
    public VideoQuality adjustBitrate(VideoStream stream, NetworkCondition condition) {
        VideoQuality currentQuality = stream.getCurrentQuality();
        VideoQuality targetQuality = calculateTargetQuality(currentQuality, condition);

        if (targetQuality != currentQuality) {
            try {
                // 调整编码参数
                adjustEncodingParameters(stream, targetQuality);

                // 通知客户端码率变更
                notifyBitrateChange(stream.getStreamId(), targetQuality);

                log.info("自适应码率调整: streamId={}, {} -> {}",
                    stream.getStreamId(), currentQuality, targetQuality);

            } catch (Exception e) {
                log.error("自适应码率调整失败", e);
            }
        }

        return targetQuality;
    }

    /**
     * 计算目标质量
     */
    private VideoQuality calculateTargetQuality(VideoQuality currentQuality, NetworkCondition condition) {
        int bandwidth = condition.getBandwidth(); // kbps
        int packetLoss = condition.getPacketLoss(); // %
        int latency = condition.getLatency(); // ms

        // 根据网络条件计算最合适的视频质量
        if (bandwidth < 500 || packetLoss > 5 || latency > 1000) {
            return VideoQuality.LOW;
        } else if (bandwidth < 1500 || packetLoss > 2 || latency > 500) {
            return VideoQuality.MEDIUM;
        } else if (bandwidth < 3000 || packetLoss > 1 || latency > 200) {
            return VideoQuality.HIGH;
        } else {
            return VideoQuality.ULTRA_HIGH;
        }
    }
}
```

---

**💡 专业提示**: 视频流技术专家需要具备深厚的音视频技术功底和丰富的实战经验，能够处理复杂的视频流传输、编解码、存储和优化问题，确保企业级视频监控系统的高质量、高可用和高性能表现。