# 🔌 设备通讯协议总览

**文档版本**: v1.0.0
**创建日期**: 2025-11-16
**最后更新**: 2025-11-16
**维护者**: SmartAdmin Team

---

## 📋 概述

本文档提供了IOE-DREAM设备管理系统中所有设备类型通讯协议的全面概览，包括协议架构、技术方案、原型图设计和实施指导。系统采用分层协议架构，支持多种通讯协议的统一管理和数据交互。

---

## 🏗️ 协议架构总览

### 📊 协议分层架构图

```mermaid
graph TB
    subgraph "设备层 Device Layer"
        A1[门禁设备]
        A2[视频设备]
        A3[考勤设备]
        A4[消费设备]
        A5[身份认证设备]
    end

    subgraph "协议层 Protocol Layer"
        B1[TCP/UDP协议族]
        B2[HTTP/HTTPS协议族]
        B3[MQTT协议族]
        B4[WebSocket协议族]
        B5[ONVIF协议族]
        B6[RTSP协议族]
        B7[Modbus协议族]
        B8[RS485/422协议族]
    end

    subgraph "适配层 Adapter Layer"
        C1[协议识别器]
        C2[协议转换器]
        C3[数据解析器]
        C4[协议路由器]
    end

    subgraph "业务层 Business Layer"
        D1[设备连接管理]
        D2[设备状态管理]
        D3[设备控制管理]
        D4[数据采集管理]
    end

    subgraph "应用层 Application Layer"
        E1[门禁管理应用]
        E2[视频监控应用]
        E3[考勤管理应用]
        E4[消费管理应用]
        E5[身份认证应用]
    end

    A1 --> B1
    A1 --> B7
    A1 --> B8
    A2 --> B5
    A2 --> B6
    A3 --> B1
    A4 --> B4
    A5 --> B1

    B1 --> C1
    B2 --> C1
    B3 --> C1
    B4 --> C1
    B5 --> C1
    B6 --> C1
    B7 --> C1
    B8 --> C1

    C1 --> C2
    C2 --> C3
    C3 --> C4
    C4 --> D1

    D1 --> D2
    D2 --> D3
    D3 --> D4
    D4 --> E1
    D4 --> E2
    D4 --> E3
    D4 --> E4
    D4 --> E5
```

---

## 🚪 门禁设备通讯协议

### 📋 门禁设备协议矩阵

| 设备类型 | 主协议 | 备选协议 | 数据格式 | 通讯模式 | 实时性要求 |
|----------|--------|----------|----------|----------|------------|
| 门禁机 | TCP | UDP | JSON/XML | 客户端-服务器 | 高 (<100ms) |
| 读卡器 | TCP | RS485/Modbus | 自定义二进制 | 轮询/事件 | 高 (<200ms) |
| 指纹机 | TCP | USB | 自定义协议 | 客户端-服务器 | 中 (500ms-1s) |
| 人脸机 | TCP | HTTP | JSON/WebSocket | 客户端-服务器 | 中 (1-2s) |
| 密码键盘 | TCP | RS485 | 自定义二进制 | 事件上报 | 高 (<100ms) |
| 三辊闸 | TCP | Modbus | JSON | 控制命令 | 高 (<200ms) |
| 翼闸 | TCP | Modbus | JSON | 控制命令 | 高 (<200ms) |
| 摆闸 | TCP | Modbus | JSON | 控制命令 | 高 (<200ms) |

### 🔧 门禁设备原型设计

```mermaid
classDiagram
    class AccessDevice {
        +String deviceId
        +String deviceType
        +String ipAddress
        +Integer port
        +DeviceStatus status
        +DeviceConfig config

        +connect() boolean
        +disconnect() boolean
        +sendCommand(command) Response
        +getStatus() DeviceStatus
        +registerEvent(callback) void
    }

    class TCPAccessDevice {
        +Socket socket
        +InputStream input
        +OutputStream output
        +MessageQueue messageQueue

        +connect() boolean
        +sendHeartbeat() void
        +processMessage() void
        +handleDisconnection() void
    }

    class AccessDeviceMessage {
        +String deviceId
        +String messageType
        +String command
        +Object data
        +Long timestamp

        +serialize() byte[]
        +deserialize(data) AccessDeviceMessage
        +validate() boolean
    }

    AccessDevice <|-- TCPAccessDevice
    AccessDevice --> AccessDeviceMessage : processes
    TCPAccessDevice --> Socket : uses
```

### 📡 门禁设备通讯流程原型

```mermaid
sequenceDiagram
    participant D as 门禁设备
    participant S as 服务器
    participant P as 协议处理器
    participant C as 业务控制器

    D->>S: TCP连接请求
    S->>S: 验证设备信息
    S->>P: 注册设备连接
    P->>C: 设备上线通知

    loop 设备运行
        D->>S: 心跳包
        S->>P: 设备状态更新
        P->>C: 状态实时同步

        alt 开门事件
            D->>S: 开门请求
            S->>P: 解析开门指令
            P->>C: 权限验证
            C-->>P: 验证结果
            P-->>S: 开门命令
            S-->>D: 开门指令
            D-->>S: 开门结果
            S-->>P: 执行结果
            P-->>C: 事件记录
        end
    end
```

### 📊 门禁设备协议详细规范

#### TCP协议格式 (门禁机)
```json
{
  "header": {
    "version": "1.0",
    "deviceId": "ACCESS_001",
    "messageType": "REQUEST",
    "sequenceId": 1001,
    "timestamp": 1634412345678
  },
  "body": {
    "command": "OPEN_DOOR",
    "parameters": {
      "doorId": "MAIN_DOOR",
      "accessType": "CARD",
      "cardId": "CARD_123456",
      "userId": "USER_001"
    }
  }
}
```

#### RS485/Modbus协议格式 (读卡器)
```python
# Modbus RTU 数据包格式
packet = [0x01, 0x03, 0x00, 0x00, 0x00, 0x02, 0xC5, 0xDA]
# 功能码0x03: 读取保持寄存器
# 起始地址0x0000: 读卡器状态
# 寄存器数量0x0002: 状态信息长度
```

---

## 📹 视频设备通讯协议

### 📋 视频设备协议矩阵

| 设备类型 | 主协议 | 备选协议 | 数据格式 | 编码标准 | 带宽要求 |
|----------|--------|----------|----------|----------|----------|
| 网络摄像头 | RTSP | ONVIF | H.264/H.265 | H.265优先 | 2-8Mbps |
| 智能球机 | RTSP/ONVIF | TCP | H.264/H.265 | H.265优先 | 4-12Mbps |
| NVR录像机 | HTTP/HTTPS | FTP | MP4/FLV | H.264优先 | 上传10-50Mbps |
| 视频服务器 | RTMP/WebRTC | HTTP | H.264/H.265 | 自适应 | 推流10-100Mbps |

### 🔧 视频设备原型设计

```mermaid
classDiagram
    class VideoDevice {
        +String deviceId
        +String streamUrl
        +StreamConfig config
        +DeviceStatus status
        +PTZController ptz

        +startStream() boolean
        +stopStream() boolean
        +captureImage() byte[]
        +setPTZ(preset) void
        +getStreamInfo() StreamInfo
    }

    class RTSPVideoDevice {
        +RTSPClient rtspClient
        +VideoDecoder decoder
        +StreamBuffer buffer
        +FrameListener frameListener

        +connect() boolean
        +setupStream() void
        +processFrame() void
        +handleStreamError() void
    }

    class ONVIFVideoDevice {
        +OnvifClient onvifClient
        +PTZController ptzController
        +MediaService mediaService
        +DeviceService deviceService

        +discoverServices() void
        +setupPTZ() void
        +configureStream() void
        +getCapabilities() DeviceCapabilities
    }

    VideoDevice <|-- RTSPVideoDevice
    VideoDevice <|-- ONVIFVideoDevice
    RTSPVideoDevice --> RTSPClient : uses
    ONVIFVideoDevice --> PTZController : uses
```

### 🎥 视频流处理原型架构

```mermaid
graph LR
    A[摄像头] -->|RTSP| B[RTSP服务器]
    B -->|H.264流| C[流媒体服务器]
    C -->|HLS/DASH| D[CDN分发]
    C -->|RTMP| E[实时直播]
    C -->|WebRTC| F[浏览器播放]

    subgraph "后端处理"
        C --> G[视频转码服务]
        G --> H[智能分析服务]
        H --> I[事件检测]
        I --> J[告警推送]
    end

    subgraph "前端播放"
        F --> K[Vue3播放器]
        F --> L[移动端播放器]
        F --> M[大屏播放器]
    end
```

### 📡 ONVIF协议交互流程原型

```mermaid
sequenceDiagram
    participant C as 客户端
    participant D as 摄像头
    participant S as ONVIF服务
    participant M as 媒体服务

    C->>S: 发现设备请求
    S->>D: WS-Discovery
    D-->>S: 设备服务描述
    S-->>C: 设备列表

    C->>D: 获取设备能力
    D-->>C: PTZ能力信息

    C->>S: 建立媒体服务
    S->>M: 请求媒体配置
    M-->>S: 媒体URL
    S-->>C: RTSP流地址

    C->>D: RTSP连接
    D->>C: 视频流数据

    C->>D: PTZ控制命令
    D-->>C: 执行结果
```

---

## ⏰ 考勤设备通讯协议

### 📋 考勤设备协议矩阵

| 设备类型 | 主协议 | 备选协议 | 数据格式 | 同步频率 | 数据量 |
|----------|--------|----------|----------|----------|--------|
| 指纹考勤机 | TCP | HTTP | JSON/XML | 实时/批量 | 1-5KB |
| 人脸考勤机 | TCP | HTTP | JSON+图片 | 实时/批量 | 10-100KB |
| IC卡考勤机 | TCP | UDP | 自定义二进制 | 实时 | 500B-2KB |
| 二维码考勤机 | HTTP | HTTPS | JSON | 实时 | 1-5KB |

### 🔧 考勤设备原型设计

```mermaid
classDiagram
    class AttendanceDevice {
        +String deviceId
        +DeviceType deviceType
        +DeviceConfig config
        +AttendanceRecord lastRecord

        +connect() boolean
        +authenticate(user) AuthResult
        +recordAttendance() boolean
        +syncData() void
        +getDeviceStatus() DeviceStatus
    }

    class FingerprintAttendanceDevice {
        +FingerprintProcessor processor
        +TemplateManager templateManager
        +RecordBuffer buffer

        +captureFingerprint() Fingerprint
        +matchFingerprint() MatchResult
        +storeAttendanceRecord() void
        +syncTemplates() void
    }

    class AttendanceSyncService {
        +SyncQueue syncQueue
        +ConflictResolver resolver
        +DataValidator validator

        +queueSync(record) void
        +processSync() void
        +handleConflict() void
        +validateData() boolean
    }

    AttendanceDevice <|-- FingerprintAttendanceDevice
    AttendanceDevice --> AttendanceSyncService : uses
    FingerprintAttendanceDevice --> FingerprintProcessor : uses
    AttendanceSyncService --> SyncQueue : manages
```

### 📋 考勤数据同步流程原型

```mermaid
sequenceDiagram
    participant D as 考勤设备
    participant G as 数据网关
    participant S as 同步服务
    participant Q as 消息队列
    participant B as 业务服务
    participant DB as 数据库

    D->>G: 考勤记录
    G->>S: 数据验证
    S->>S: 数据转换

    alt 批量同步
        S->>Q: 批量同步消息
        Q->>B: 批量处理
        B->>DB: 批量入库
    else 实时同步
        S->>B: 实时处理
        B->>DB: 单条入库
    end

    B-->>S: 处理结果
    S-->>G: 同步确认
    G-->>D: 确认回执
```

---

## 💳 消费设备通讯协议

### 📋 消费设备协议矩阵

| 设备类型 | 主协议 | 备选协议 | 数据格式 | 交易安全 | 实时性 |
|----------|--------|----------|----------|----------|--------|
| 消费终端 | WebSocket | TCP | JSON | 加密签名 | 极高 |
| 充值机 | TCP | HTTP | JSON | SSL/TLS | 高 |
| 查询机 | HTTP | HTTPS | JSON | HTTPS | 中 |
| 收银机 | WebSocket | TCP | JSON | PCI-DSS | 极高 |

### 🔧 消费设备原型设计

```mermaid
classDiagram
    class ConsumeDevice {
        +String deviceId
        +DeviceType deviceType
        +PaymentConfig config
        +SecureSession session
        +TransactionState state

        +initSession() boolean
        +processPayment() TransactionResult
        +validateAmount() boolean
        +encryptData() String
        +signTransaction() String
    }

    class WebSocketConsumeDevice {
        +WebSocketSession session
        +MessageHandler handler
        +TransactionProcessor processor
        +HeartbeatManager heartbeat

        +connect() boolean
        +sendRequest() void
        +handleMessage() void
        +processPayment() TransactionResult
    }

    class PaymentProcessor {
        +PaymentGateway gateway
        +RiskControl riskControl
        +AccountService accountService
        +AuditLogger logger

        +processPayment() PaymentResult
        +validateRisk() RiskResult
        +updateAccount() boolean
        +recordTransaction() void
    }

    ConsumeDevice <|-- WebSocketConsumeDevice
    ConsumeDevice --> PaymentProcessor : uses
    WebSocketConsumeDevice --> TransactionProcessor : uses
    PaymentProcessor --> PaymentGateway : uses
```

### 💰 消费交易流程原型

```mermaid
sequenceDiagram
    participant T as 消费终端
    participant P as 支付网关
    participant A as 账户服务
    participant R as 风控系统
    participant D as 数据库

    T->>P: 支付请求
    P->>R: 风险评估
    R-->>P: 风险结果

    alt 风险通过
        P->>A: 账户验证
        A-->>P: 账户信息
        P->>A: 扣款处理
        A-->>P: 扣款结果

        alt 扣款成功
            P->>D: 记录交易
            P-->>T: 支付成功
        else 扣款失败
            P-->>T: 支付失败
            P->>D: 记录失败
        end
    else 风险拒绝
        P-->>T: 交易拒绝
    end
```

---

## 🔐 身份认证设备通讯协议

### 📋 身份认证设备协议矩阵

| 设备类型 | 主协议 | 备选协议 | 生物特征 | 安全等级 | 认证速度 |
|----------|--------|----------|----------|----------|----------|
| 指纹识别器 | TCP | USB | 指纹模板 | 高 | 1-2秒 |
| 人脸识别机 | TCP | HTTP | 人脸特征 | 最高 | 2-3秒 |
| IC卡读卡器 | TCP | RS485 | 卡片信息 | 中 | <1秒 |
| 多模态终端 | WebSocket | TCP | 多种特征 | 最高 | 3-5秒 |

### 🔧 身份认证设备原型设计

```mermaid
classDiagram
    class AuthDevice {
        +String deviceId
        +AuthMode authMode
        +BiometricConfig config
        +SecureStorage storage
        +AuthResult lastResult

        +authenticate(user) AuthResult
        +enrollBiometric() boolean
        +verifyBiometric() boolean
        +updateTemplate() void
        +getAuthHistory() List
    }

    class FingerprintAuthDevice {
        +FingerprintScanner scanner
        +FeatureExtractor extractor
        +TemplateMatcher matcher
        +SecureStorage storage

        +captureFingerprint() Fingerprint
        +extractFeatures() FeatureVector
        +matchTemplate() MatchScore
        +storeTemplate() void
    }

    class FaceAuthDevice {
        +Camera camera
        +FaceDetector detector
        +FaceRecognizer recognizer
        +AntiSpoofingChecker antispoof

        +captureFace() FaceImage
        +detectFace() FaceBox
        +recognizeFace() RecognitionResult
        +checkLiveness() LivenessResult
        +generateFaceId() String
    }

    AuthDevice <|-- FingerprintAuthDevice
    AuthDevice <|-- FaceAuthDevice
    FingerprintAuthDevice --> FeatureExtractor : uses
    FaceAuthDevice --> FaceRecognizer : uses
```

### 🔍 多模态认证流程原型

```mermaid
sequenceDiagram
    participant U as 用户
    participant D as 认证设备
    participant F as 特征提取
    participant M = 特征匹配
    participant A as 认证服务
    participant S as 会话服务

    U->>D: 开始认证
    D->>F: 采集特征
    F->>M: 特征比对
    M-->>F: 匹配结果
    F-->>D: 认证结果

    alt 认证成功
        D->>A: 认证确认
        A->>S: 创建会话
        S-->>D: 会话令牌
        D-->>U: 认证成功
    else 认证失败
        D-->>U: 认证失败
    end
```

---

## 🔄 协议管理框架

### 📋 协议管理器架构

```mermaid
classDiagram
    class ProtocolManager {
        +Map<String, ProtocolAdapter> adapters
        +MessageRouter router
        +ConnectionPool connectionPool
        +MessageSerializer serializer

        +registerProtocol() void
        +createAdapter() ProtocolAdapter
        +routeMessage() void
        +serializeMessage() byte[]
        +deserializeMessage() Object
    }

    class ProtocolAdapter {
        +ProtocolConfig config
        +MessageHandler handler
        +ConnectionManager manager
        +ErrorHandler error

        +connect() boolean
        +disconnect() void
        +sendMessage() void
        +receiveMessage() Object
        +handleError() void
    }

    class ProtocolFactory {
        +Map<String, ProtocolBuilder> builders
        +ComponentScanner scanner

        +createProtocol() ProtocolAdapter
        +getAvailableProtocols() List
        +scanProtocolPlugins() void
        +validateProtocol() boolean
    }

    ProtocolManager --> ProtocolAdapter : manages
    ProtocolManager --> MessageRouter : routes
    ProtocolManager --> MessageSerializer : serializes
    ProtocolFactory --> ProtocolAdapter : creates
```

### 🛠️ 协议适配器实现模式

```java
// 协议适配器接口
public interface ProtocolAdapter {
    boolean connect(String address, int port);
    void disconnect();
    void sendHeartbeat();
    void sendCommand(DeviceCommand command);
    DeviceMessage receiveMessage();
    boolean isConnected();
    DeviceStatus getStatus();
}

// TCP协议适配器实现
@Component
public class TCPProtocolAdapter implements ProtocolAdapter {
    private Socket socket;
    private Input input;
    private Output output;
    private MessageQueue messageQueue;

    @Override
    public boolean connect(String address, int port) {
        try {
            socket = new Socket(address, port);
            input = socket.getInputStream();
            output = socket.getOutputStream();
            startMessageListener();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void sendCommand(DeviceCommand command) {
        try {
            byte[] data = serializeCommand(command);
            output.write(data);
            output.flush();
        } catch (Exception e) {
            handleError(e);
        }
    }
}
```

---

## 📊 协议性能指标

### 📋 协议性能对比表

| 协议类型 | 连接延迟 | 数据吞吐量 | 并发连接数 | 内存占用 | CPU占用 |
|----------|----------|------------|--------------|----------|----------|
| TCP | 10-50ms | 1-10MB/s | 1000 | 5-10MB | 2-5% |
| HTTP | 20-100ms | 10-50MB/s | 500 | 2-5MB | 1-3% |
| WebSocket | 5-20ms | 5-20MB/s | 2000 | 10-20MB | 3-8% |
| MQTT | 5-15ms | 100KB-1MB/s | 5000 | 1-2MB | 1-2% |
| RTSP | 5-15ms | 2-8MB/s | 500 | 3-8MB | 5-10% |
| ONVIF | 50-200ms | 1-5MB/s | 100 | 5-15MB | 2-5% |

### 🎯 协议选择建议

#### 高实时性场景 (延迟 < 50ms)
- **推荐协议**: WebSocket, MQTT
- **适用设备**: 门禁控制、实时监控

#### 高可靠性场景 (数据完整性)
- **推荐协议**: TCP, HTTP/HTTPS
- **适用设备**: 消费交易、身份认证

#### 高带宽场景 (视频流传输)
- **推荐协议**: RTSP, WebRTC
- **适用设备**: 视频监控、视频分析

---

## 🔧 协议开发指南

### 📋 协议适配器开发流程

1. **需求分析**
   - 设备通讯协议调研
   - 数据格式定义
   - 性能指标要求

2. **接口设计**
   - ProtocolAdapter接口实现
   - 消息格式定义
   - 错误处理机制

3. **编码实现**
   - 协议解析器编写
   - 数据序列化实现
   - 连接管理逻辑

4. **测试验证**
   - 单元测试编写
   - 集成测试验证
   - 性能基准测试

5. **部署上线**
   - 协议注册配置
   - 监控指标设置
   - 故障处理预案

### 📝 协议开发模板

```java
@Component
public class CustomDeviceAdapter implements ProtocolAdapter {
    private static final Logger log = LoggerFactory.getLogger(CustomDeviceAdapter.class);

    @Resource
    private MessageSerializer serializer;

    @Resource
    private ErrorHandler errorHandler;

    @Override
    public boolean connect(String address, int port) {
        try {
            // 连接设备
            initializeConnection(address, port);

            // 发送握手协议
            sendHandshake();

            // 验证连接
            return validateConnection();

        } catch (Exception e) {
            log.error("设备连接失败", e);
            errorHandler.handleError(e);
            return false;
        }
    }

    private void initializeConnection(String address, int port) {
        // 实现连接逻辑
    }

    private void sendHandshake() {
        // 实现握手协议
    }

    private boolean validateConnection() {
        // 实现连接验证
        return true;
    }
}
```

---

**⚠️ 重要提醒**: 本文档定义了设备管理系统的通讯协议架构和实现标准。所有新设备类型的接入都必须严格遵循本文档中的协议设计原则和开发规范。协议适配器的开发必须经过完整的设计评审和测试验证。