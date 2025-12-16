# IOE-DREAM 智慧园区一卡通管理平台 - 设备兼容开发指南

> **版本**: v1.0.0
> **创建时间**: 2025-12-16
> **适用范围**: 门禁、考勤、消费、访客、视频监控等业务模块
> **核心目标**: 统一设备接入规范，实现多厂商设备无缝兼容

## 📋 文档概述

### 核心目标

为开发团队提供完整的设备兼容开发指导，确保各个业务模块能够：
1. **统一设备接入**：采用统一的协议适配器架构
2. **厂商无关**：业务逻辑与具体厂商设备解耦
3. **快速接入**：新厂商设备接入标准化、流程化
4. **稳定运行**：完善的错误处理和监控机制

### 适用范围

本文档适用于以下业务模块的开发：
- **门禁管理**：支持熵基科技、中控智慧等门禁设备
- **考勤管理**：支持多种考勤设备和识别方式
- **消费管理**：支持POS机、自助消费机等设备
- **访客管理**：支持访客机、人脸识别终端等设备
- **视频监控**：支持各厂商摄像头和NVR设备
- **设备通讯**：统一的设备通讯协议管理

## 🏗️ 架构设计概览

### 核心架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                    IOE-DREAM 业务服务层                              │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐   │
│  │ 门禁服务     │ │ 考勤服务     │ │ 消费服务     │ │ 访客服务     │   │
│  │ Access     │ │ Attendance │ │ Consume     │ │ Visitor     │   │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘   │
├─────────────────────────────────────────────────────────────────┤
│                    设备通讯微服务                                   │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │           ProtocolAdapterFactory                          │   │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │   │
│  │  │ 门禁协议适配 │ │ 消费协议适配 │ │ 考勤协议适配 │  ...      │   │
│  │  │ 器集合      │ │ 器集合      │ │ 器集合      │          │   │
│  │  └─────────────┘ └─────────────┘ └─────────────┘          │   │
│  └─────────────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────────┤
│                        物理设备层                                │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐   │
│  │ 熵基设备     │ │ 中控设备     │ │ 海康设备     │ │ 大华设备     │   │
│  │ Access     │ │ Consume     │ │ Video      │ │ Security   │   │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### 技术栈

| 组件 | 技术选型 | 说明 |
|------|----------|------|
| 微服务框架 | Spring Boot 3.5.8 | 主业务服务框架 |
| 协议适配器 | Java Interface + Factory Pattern | 统一协议处理架构 |
| 数据存储 | MySQL 8.0 + MyBatis-Plus | 协议消息和设备信息存储 |
| 缓存 | Redis | 设备映射和性能优化 |
| API网关 | Spring Cloud Gateway | 统一服务入口和路由 |
| 配置管理 | Nacos | 动态配置和协议注册 |

## 🔌 设备通讯协议架构

### 核心接口设计

```java
/**
 * 设备协议适配器统一接口
 * 所有厂商协议适配器必须实现此接口
 */
public interface ProtocolAdapter {

    // ==================== 协议标识接口 ====================

    /**
     * 获取协议类型标识
     * 格式：{厂商}_{设备类型}_{版本号}
     * 示例：ACCESS_ENTROPY_V4_8, CONSUME_ZKTECO_V1_0
     */
    String getProtocolType();

    /**
     * 获取设备厂商
     */
    String getManufacturer();

    /**
     * 获取协议版本
     */
    String getVersion();

    /**
     * 获取支持的设备型号列表
     */
    String[] getSupportedDeviceModels();

    /**
     * 检查是否支持指定设备型号
     */
    boolean isDeviceModelSupported(String deviceModel);

    // ==================== 消息处理核心接口 ====================

    /**
     * 解析设备消息
     * @param rawData 设备原始数据（字节数组）
     * @param deviceId 设备ID
     * @return 解析后的协议消息对象
     * @throws ProtocolParseException 协议解析异常
     */
    ProtocolMessage parseDeviceMessage(byte[] rawData, Long deviceId) throws ProtocolParseException;

    /**
     * 构建设备响应消息
     * @param messageType 消息类型
     * @param businessData 业务数据Map
     * @param deviceId 设备ID
     * @return 设备响应消息（字节数组）
     * @throws ProtocolBuildException 协议构建异常
     */
    byte[] buildDeviceResponse(String messageType, Map<String, Object> businessData, Long deviceId) throws ProtocolBuildException;

    // ==================== 业务数据处理接口 ====================

    /**
     * 处理门禁业务数据
     */
    Future<ProtocolProcessResult> processAccessBusiness(String businessType, Map<String, Object> businessData, Long deviceId);

    /**
     * 处理消费业务数据
     */
    Future<ProtocolProcessResult> processConsumeBusiness(String businessType, Map<String, Object> businessData, Long deviceId);

    /**
     * 处理考勤业务数据
     */
    Future<ProtocolProcessResult> processAttendanceBusiness(String businessType, Map<String, Object> businessData, Long deviceId);

    // ==================== 设备管理接口 ====================

    /**
     * 初始化设备连接
     */
    Future<ProtocolInitResult> initializeDevice(Map<String, Object> deviceInfo, Map<String, Object> config);

    /**
     * 获取设备状态
     */
    ProtocolDeviceStatus getDeviceStatus(Long deviceId);

    // ==================== 组件生命周期接口 ====================

    /**
     * 初始化协议适配器
     */
    void initialize();

    /**
     * 销毁协议适配器
     */
    void destroy();

    /**
     * 获取适配器状态
     */
    String getAdapterStatus();
}
```

### 协议适配器工厂

```java
/**
 * 协议适配器工厂
 * 负责所有协议适配器的注册、查找和管理
 */
@Component
public class ProtocolAdapterFactory {

    // 协议适配器注册表
    private final Map<String, ProtocolAdapter> adapterRegistry = new ConcurrentHashMap<>();

    // 设备型号到协议类型的映射
    private final Map<String, String> deviceModelToProtocolMap = new ConcurrentHashMap<>();

    /**
     * 注册协议适配器
     */
    public void registerAdapter(ProtocolAdapter adapter) {
        String protocolType = adapter.getProtocolType();
        adapterRegistry.put(protocolType, adapter);

        // 注册设备型号映射
        for (String model : adapter.getSupportedDeviceModels()) {
            deviceModelToProtocolMap.put(model.toUpperCase(), protocolType);
        }
    }

    /**
     * 根据设备型号获取适配器
     */
    public ProtocolAdapter getAdapterByDeviceModel(String deviceModel) {
        String protocolType = deviceModelToProtocolMap.get(deviceModel.toUpperCase());
        return protocolType != null ? adapterRegistry.get(protocolType) : null;
    }

    /**
     * 获取所有支持的设备型号
     */
    public List<String> getSupportedDeviceModels() {
        return new ArrayList<>(deviceModelToProtocolMap.keySet());
    }
}
```

## 🚪 门禁管理模块设备兼容指南

### 支持的厂商和设备

| 厂商 | 协议类型 | 支持设备型号 | 功能特性 |
|------|----------|--------------|----------|
| 熵基科技 | ACCESS_ENTROPY_V4_8 | MA300, MA300T, SC405, SC700, SC705, F18, TA800C, TA800T, WK2600 | 人脸识别、指纹识别、刷卡、二维码、活体检测 |
| 中控智慧 | ACCESS_ZKTECO_V2_0 | SC405, SC700, SC810, INPOS | 多模态识别、反潜回、胁迫报警、尾随检测 |
| 海康威视 | ACCESS_HIKVISION_V1_5 | DS-K2801, DS-K2802, DS-K2803 | 人脸识别、门磁联动、视频监控、报警推送 |
| 大华技术 | ACCESS_DAHUA_V2_0 | ASC1204C, ASC2204C, ASC3204C | 生物识别、门禁控制、访客管理、移动开门 |

### 门禁设备接入开发流程

#### 1. 创建协议适配器

```java
@Component
public class HikvisionAccessAdapter implements ProtocolAdapter {

    @Override
    public String getProtocolType() {
        return "ACCESS_HIKVISION_V1_5";
    }

    @Override
    public String getManufacturer() {
        return "海康威视";
    }

    @Override
    public String[] getSupportedDeviceModels() {
        return new String[]{"DS-K2801", "DS-K2802", "DS-K2803"};
    }

    @Override
    public ProtocolMessage parseDeviceMessage(byte[] rawData, Long deviceId) throws ProtocolParseException {
        // 解析海康威视门禁协议
        HikvisionAccessMessage message = new HikvisionAccessMessage();

        // 1. 解析协议头（海康威视协议格式）
        ByteBuffer buffer = ByteBuffer.wrap(rawData).order(ByteOrder.BIG_ENDIAN);
        buffer.getShort(); // 协议标识
        short dataLength = buffer.getShort();
        byte[] dataBytes = new byte[dataLength];
        buffer.get(dataBytes);

        // 2. 解析JSON格式的业务数据
        JSONObject jsonData = JSON.parseObject(new String(dataBytes, StandardCharsets.UTF_8));
        message.setDeviceId(jsonData.getString("DeviceID"));
        message.setEventType(jsonData.getString("EventType"));
        message.setCardNo(jsonData.getString("CardNo"));
        message.setVerifyResult(jsonData.getString("VerifyResult"));

        return message;
    }

    @Override
    public Future<ProtocolProcessResult> processAccessBusiness(String businessType, Map<String, Object> businessData, Long deviceId) {
        switch (businessType) {
            case "REAL_TIME_EVENT":
                return processAccessEvent(businessData, deviceId);
            case "ALARM_EVENT":
                return processAlarmEvent(businessData, deviceId);
            case "VIDEO_VERIFY":
                return processVideoVerification(businessData, deviceId);
            default:
                return CompletableFuture.completedFuture(
                    new ProtocolProcessResult(false, "不支持的业务类型: " + businessType)
                );
        }
    }

    private Future<ProtocolProcessResult> processVideoVerification(Map<String, Object> businessData, Long deviceId) {
        // 海康威视特色功能：视频联动验证
        CompletableFuture<ProtocolProcessResult> future = new CompletableFuture<>();

        // 1. 获取人脸图像数据
        String faceImage = (String) businessData.get("faceImage");

        // 2. 调用人脸识别服务
        faceRecognitionService.verifyFace(faceImage)
            .thenAccept(recognizeResult -> {
                // 3. 处理识别结果
                if (recognizeResult.isSuccess()) {
                    // 开门指令
                    sendOpenDoorCommand(deviceId, "视频验证通过");
                    future.complete(new ProtocolProcessResult(true, "视频验证成功"));
                } else {
                    // 拒绝开门，记录日志
                    future.complete(new ProtocolProcessResult(false, "视频验证失败"));
                }
            })
            .exceptionally(throwable -> {
                future.complete(new ProtocolProcessResult(false, "视频验证异常: " + throwable.getMessage()));
            });

        return future;
    }
}
```

#### 2. 创建门禁消息实体

```java
package net.lab1024.sa.device.comm.protocol.hikvision;

@Data
public class HikvisionAccessMessage implements ProtocolMessage {

    // 协议基本信息
    private String protocolVersion = "V1.5";
    private String deviceModel;
    private String deviceIp;
    private String macAddress;

    // 事件信息
    private String eventType; // CardEvent, FaceEvent, AlarmEvent
    private String cardNo;
    private String userId;
    private String userName;
    private String verifyResult; // Success, Fail, Timeout

    // 生物识别信息
    private String faceImage; // Base64编码的人脸图像
    private Float faceScore; // 人脸识别分数
    private String livenessResult; // Real, Photo, Video

    // 门控信息
    private String doorStatus; // Open, Close, ForceOpen
    private String lockStatus; // Lock, Unlock, Fault
    private String doorDirection; // In, Out, Unknown

    // 报警信息
    private String alarmType; // DoorForced, DoorOpenTooLong, Tamper
    private String alarmLevel; // Low, Medium, High, Critical
    private LocalDateTime alarmTime;

    // 处理信息
    private LocalDateTime receiveTime;
    private LocalDateTime processTime;
    private String processResult;
    private String errorMessage;
}
```

#### 3. 门禁服务集成

```java
@Service
public class AccessServiceImpl implements AccessService {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Resource
    private FaceRecognitionService faceRecognitionService;

    @Resource
    private VideoSurveillanceService videoSurveillanceService;

    /**
     * 处理门禁事件（厂商无关）
     */
    @Override
    public ResponseDTO<AccessResultVO> processAccessEvent(AccessEventForm eventForm) {
        try {
            // 1. 根据设备信息获取协议适配器
            ProtocolAdapter adapter = getProtocolAdapterByDevice(eventForm.getDeviceId());

            // 2. 构建业务数据
            Map<String, Object> businessData = new HashMap<>();
            businessData.put("eventType", eventForm.getEventType());
            businessData.put("cardNo", eventForm.getCardNo());
            businessData.put("verifyMethod", eventForm.getVerifyMethod());
            businessData.put("accessTime", eventForm.getAccessTime());

            // 3. 调用设备通讯服务处理业务
            ResponseDTO<ProtocolProcessResult> result = gatewayServiceClient.callDeviceCommService(
                "/api/v1/device-comm/process-access",
                HttpMethod.POST,
                Map.of(
                    "protocolType", adapter.getProtocolType(),
                    "businessType", "REAL_TIME_EVENT",
                    "businessData", businessData,
                    "deviceId", eventForm.getDeviceId()
                ),
                ProtocolProcessResult.class
            );

            // 4. 处理业务结果
            if (result.getData().getSuccess()) {
                // 保存通行记录
                saveAccessRecord(eventForm, result.getData());

                // 视频联动（如果设备支持）
                if (adapter.getManufacturer().contains("海康威视") ||
                    adapter.getManufacturer().contains("大华")) {
                    triggerVideoLinkage(eventForm);
                }

                return ResponseDTO.ok(buildAccessResult(eventForm));
            } else {
                return ResponseDTO.error("ACCESS_FAILED", "门禁处理失败: " + result.getData().getProcessDetails());
            }

        } catch (Exception e) {
            log.error("门禁事件处理异常", e);
            return ResponseDTO.error("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }

    /**
     * 根据设备获取协议适配器
     */
    private ProtocolAdapter getProtocolAdapterByDevice(Long deviceId) {
        // 1. 查询设备信息
        DeviceEntity device = deviceService.getById(deviceId);

        // 2. 通过设备型号获取适配器
        return protocolAdapterFactory.getAdapterByDeviceModel(device.getDeviceModel());
    }

    /**
     * 视频联动处理
     */
    private void triggerVideoLinkage(AccessEventForm eventForm) {
        // 1. 获取关联的摄像头
        List<CameraEntity> cameras = getLinkedCameras(eventForm.getAccessPointId());

        // 2. 抓拍门禁事件图片
        for (CameraEntity camera : cameras) {
            videoSurveillanceService.captureImage(camera.getCameraId(), eventForm.getEventId())
                .thenAccept(imageUrl -> {
                    // 3. 保存关联图片
                    saveAccessEventImage(eventForm.getEventId(), imageUrl);
                });
        }

        // 4. 触发录像
        videoSurveillanceService.startRecording(cameras, "门禁事件录像");
    }
}
```

## ⏰ 考勤管理模块设备兼容指南

### 支持的厂商和设备

| 厂商 | 协议类型 | 支持设备型号 | 功能特性 |
|------|----------|--------------|----------|
| 熵基科技 | ATTENDANCE_ENTROPY_V4_0 | MA300, MA300T, F18, TA800C | 人脸考勤、指纹考勤、刷卡考勤、活体检测 |
| 中控智慧 | ATTENDANCE_ZKTECO_V1_5 | SC405, SC700, SC810, U160 | 多模态考勤、位置验证、照片采集 |
| 科密 | ATTENDANCE_COMEY_V2_1 | CA-1000, CA-2000, CA-3000 | 人脸识别、指纹识别、虹膜识别 |
| 汉王 | ATTENDANCE_HANVONG_V1_8 | ASI3202X, ASI4221X, ASI7202X | 人脸考勤、活体检测、门禁考勤联动 |

### 考勤设备接入开发流程

#### 1. 创建考勤协议适配器

```java
@Component
public class ComeyAttendanceAdapter implements ProtocolAdapter {

    @Override
    public String getProtocolType() {
        return "ATTENDANCE_COMEY_V2_1";
    }

    @Override
    public String getManufacturer() {
        return "科密";
    }

    @Override
    public String[] getSupportedDeviceModels() {
        return new String[]{"CA-1000", "CA-2000", "CA-3000"};
    }

    @Override
    public ProtocolMessage parseDeviceMessage(byte[] rawData, Long deviceId) throws ProtocolParseException {
        // 解析科密考勤协议
        ComeyAttendanceMessage message = new ComeyAttendanceMessage();

        // 科密协议特点：支持多模态生物识别
        ByteBuffer buffer = ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN);

        // 1. 解析协议头
        buffer.getShort(); // COMEY标识
        short messageType = buffer.getShort();
        int dataLength = buffer.getInt();

        // 2. 根据消息类型解析
        switch (messageType) {
            case 0x01: // 考勤记录
                parseAttendanceRecord(buffer, message);
                break;
            case 0x02: // 生物特征注册
                parseBiometricRegistration(buffer, message);
                break;
            case 0x03: // 设备状态
                parseDeviceStatus(buffer, message);
                break;
        }

        return message;
    }

    @Override
    public Future<ProtocolProcessResult> processAttendanceBusiness(String businessType, Map<String, Object> businessData, Long deviceId) {
        switch (businessType) {
            case "ATTENDANCE_RECORD":
                return processAttendanceRecord(businessData, deviceId);
            case "BIOMETRIC_REGISTRATION":
                return processBiometricRegistration(businessData, deviceId);
            case "SCHEDULE_SYNC":
                return processScheduleSync(businessData, deviceId);
            case "LOCATION_VERIFY":
                return processLocationVerification(businessData, deviceId);
            default:
                return CompletableFuture.completedFuture(
                    new ProtocolProcessResult(false, "不支持的考勤业务类型: " + businessType)
                );
        }
    }

    private Future<ProtocolProcessResult> processBiometricRegistration(Map<String, Object> businessData, Long deviceId) {
        // 科密特色功能：虹膜识别注册
        CompletableFuture<ProtocolProcessResult> future = new CompletableFuture<>();

        try {
            // 1. 获取生物特征数据
            String irisImageData = (String) businessData.get("irisImage");
            String fingerprintData = (String) businessData.get("fingerprintData");
            String faceData = (String) businessData.get("faceData");

            // 2. 多模态特征注册
            List<BiometricFeature> features = new ArrayList<>();

            // 虹膜特征注册
            if (irisImageData != null) {
                irisRecognitionService.registerIris(deviceId, irisImageData)
                    .thenAccept(irisFeature -> features.add(irisFeature));
            }

            // 指纹特征注册
            if (fingerprintData != null) {
                fingerprintService.registerFingerprint(deviceId, fingerprintData)
                    .thenAccept(fingerprintFeature -> features.add(fingerprintFeature));
            }

            // 人脸特征注册
            if (faceData != null) {
                faceRecognitionService.registerFace(deviceId, faceData)
                    .thenAccept(faceFeature -> features.add(faceFeature));
            }

            // 3. 处理注册结果
            CompletableFuture.allOf(features.toArray(new CompletableFuture[0]))
                .thenAccept(results -> {
                    // 保存生物特征到数据库
                    saveBiometricFeatures(deviceId, features);

                    // 通知注册成功
                    future.complete(new ProtocolProcessResult(true, "多模态生物特征注册成功"));
                })
                .exceptionally(throwable -> {
                    future.complete(new ProtocolProcessResult(false, "生物特征注册失败: " + throwable.getMessage()));
                });

        } catch (Exception e) {
            future.complete(new ProtocolProcessResult(false, "生物特征注册异常: " + e.getMessage()));
        }

        return future;
    }

    private Future<ProtocolProcessResult> processLocationVerification(Map<String, Object> businessData, Long deviceId) {
        // 科密特色功能：Wi-Fi定位验证
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 获取设备定位信息
                String wifiBssid = (String) businessData.get("wifiBssid");
                String rssi = (String) businessData.get("rssi");
                String deviceMac = (String) businessData.get("deviceMac");

                // 2. 验证是否在允许的考勤范围内
                AttendanceArea area = getAttendanceAreaByDevice(deviceId);
                boolean inRange = locationService.isInRange(area, wifiBssid, deviceMac, Integer.parseInt(rssi));

                if (inRange) {
                    return new ProtocolProcessResult(true, "位置验证通过，在允许的考勤范围内");
                } else {
                    return new ProtocolProcessResult(false, "位置验证失败，不在允许的考勤范围内");
                }

            } catch (Exception e) {
                return new ProtocolProcessResult(false, "位置验证异常: " + e.getMessage());
            }
        });
    }
}
```

## 💳 消费管理模块设备兼容指南

### 支持的厂商和设备

| 厂商 | 协议类型 | 支持设备型号 | 功能特性 |
|------|----------|--------------|----------|
| 中控智慧 | CONSUME_ZKTECO_V1_0 | IC-600T, F2, SC700, SC810, IC-700A, IC-800A | 刷卡消费、离线消费、充值管理、补贴发放 |
| 熵基科技 | CONSUME_ENTROPY_V2_0 | MA300, SC700 | 生物识别消费、移动支付、会员管理 |
| 新中新 | CONSUME_XINZHONG_V1_5 | XZ-POS100, XZ-POS200 | 触摸屏消费、云同步、广告显示 |
| 拓达 | CONSUME_TUODA_V2_2 | TDA-100, TDA-200 | 双屏显示、打印小票、会员卡识别 |

### 消费设备接入开发流程

#### 1. 创建消费协议适配器

```java
@Component
public class XinzhongConsumeAdapter implements ProtocolAdapter {

    @Override
    public String getProtocolType() {
        return "CONSUME_XINZHONG_V1_5";
    }

    @Override
    public String getManufacturer() {
        return "中新新";
    }

    @Override
    public String[] getSupportedDeviceModels() {
        return new String[]{"XZ-POS100", "XZ-POS200"};
    }

    @Override
    public ProtocolMessage parseDeviceMessage(byte[] rawData, Long deviceId) throws ProtocolParseException {
        // 解析中新新消费协议
        XinzhongConsumeMessage message = new XinzhongConsumeMessage();

        // 中新新协议特点：支持双屏显示和广告推送
        ByteBuffer buffer = ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN);

        // 1. 解析消费记录
        parseConsumeRecord(buffer, message);

        // 2. 解析设备状态（第二屏状态）
        parseSecondScreenStatus(buffer, message);

        // 3. 解析广告播放统计
        parseAdvertPlayStats(buffer, message);

        return message;
    }

    @Override
    public Future<ProtocolProcessResult> processConsumeBusiness(String businessType, Map<String, Object> businessData, Long deviceId) {
        switch (businessType) {
            case "CONSUME_RECORD":
                return processConsumeRecord(businessData, deviceId);
            case "OFFLINE_SYNC":
                return processOfflineSync(businessData, deviceId);
            case "ADVERTISE_PLAY":
                return processAdvertisePlay(businessData, deviceId);
            case "SCREEN_UPDATE":
                return processSecondScreenUpdate(businessData, deviceId);
            case "PAYMENT_RESULT":
                return processPaymentResult(businessData, deviceId);
            default:
                return CompletableFuture.completedFuture(
                    new ProtocolProcessResult(false, "不支持的消费业务类型: " + businessType)
                );
        }
    }

    private Future<ProtocolProcessResult> processAdvertisePlay(Map<String, Object> businessData, Long deviceId) {
        // 中新新特色功能：广告投放统计
        CompletableFuture<ProtocolProcessResult> future = new CompletableFuture<>();

        try {
            // 1. 获取广告信息
            String advertId = (String) businessData.get("advertId");
            String advertType = (String) businessData.get("advertType");
            Integer playDuration = (Integer) businessData.get("playDuration");
            Integer displayCount = (Integer) businessData.get("displayCount");

            // 2. 记录广告播放日志
            AdvertPlayLog log = new AdvertPlayLog();
            log.setAdvertId(advertId);
            log.setDeviceId(deviceId);
            log.setAdvertType(advertType);
            log.setPlayDuration(playDuration);
            log.setDisplayCount(displayCount);
            log.setPlayTime(LocalDateTime.now());

            advertPlayLogService.save(log);

            // 3. 更新广告播放统计
            advertService.updatePlayStats(advertId, deviceId, playDuration);

            // 4. 触发广告费用结算
            advertService.settleAdvertCost(advertId, displayCount);

            future.complete(new ProtocolProcessResult(true, "广告播放统计记录成功"));

        } catch (Exception e) {
            future.complete(new ProtocolProcessResult(false, "广告播放统计失败: " + e.getMessage()));
        }

        return future;
    }

    private Future<ProtocolProcessResult> processSecondScreenUpdate(Map<String, Object> businessData, Long deviceId) {
        // 中新新特色功能：第二屏内容更新
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 获取屏幕内容
                String screenContent = (String) businessData.get("screenContent");
                String contentType = (String) businessData.get("contentType");
                Integer displayDuration = (Integer) businessData.get("displayDuration");

                // 2. 构建屏幕更新指令
                Map<String, Object> updateCommand = Map.of(
                    "command", "UPDATE_SECOND_SCREEN",
                    "contentType", contentType,
                    "content", screenContent,
                    "duration", displayDuration
                );

                // 3. 发送更新指令到设备
                sendCommandToDevice(deviceId, updateCommand);

                // 4. 记录屏幕内容日志
                ScreenContentLog log = new ScreenContentLog();
                log.setDeviceId(deviceId);
                log.setContentType(contentType);
                log.setContent(screenContent);
                log.setDisplayDuration(displayDuration);
                log.setUpdateTime(LocalDateTime.now());
                screenContentLogService.save(log);

                return new ProtocolProcessResult(true, "第二屏内容更新成功");

            } catch (Exception e) {
                return new ProtocolProcessResult(false, "第二屏内容更新失败: " + e.getMessage());
            }
        });
    }

    private void sendCommandToDevice(Long deviceId, Map<String, Object> command) {
        // 通过设备通讯服务发送指令
        ProtocolAdapter adapter = protocolAdapterFactory.getAdapterByDeviceId(deviceId);
        if (adapter != null) {
            try {
                byte[] commandData = adapter.buildDeviceResponse("COMMAND", command, deviceId);
                deviceCommunicationService.sendCommand(deviceId, commandData);
            } catch (Exception e) {
                log.error("发送设备指令失败, deviceId={}", deviceId, e);
            }
        }
    }
}
```

## 👥 访客管理模块设备兼容指南

### 支持的厂商和设备

| 厂商 | 协议类型 | 支持设备型号 | 功能特性 |
|------|----------|--------------|----------|
| 熵基科技 | VISITOR_ENTROPY_V3_0 | MA300, SC700, TA800C | 人脸识别访客、二维码预约、身份证读取 |
| 中控智慧 | VISITOR_ZKTECO_V2_1 | SC405, SC810, SC602 | 多模态访客、黑名单检查、访客卡发放 |
| 海康威视 | VISITOR_HIKVISION_V1_8 | DS-K2801, DS-K5607 | 人脸访客、视频联动、门禁控制 |
| 大华技术 | VISITOR_DAHUA_V2_0 | ASC1204C, ASI3213X-L | 人脸识别访客、访客机集成、电梯控制 |

### 访客设备接入开发流程

#### 1. 创建访客协议适配器

```java
@Component
public class HikvisionVisitorAdapter implements ProtocolAdapter {

    @Override
    public String getProtocolType() {
        return "VISITOR_HIKVISION_V1_8";
    }

    @Override
    public String getManufacturer() {
        return "海康威视";
    }

    @Override
    public String[] getSupportedDeviceModels() {
        return new String[]{"DS-K2801", "DS-K5607"};
    }

    @Override
    public ProtocolMessage parseDeviceMessage(byte[] rawData, Long deviceId) throws ProtocolParseException {
        // 解析海康威视访客协议
        HikvisionVisitorMessage message = new HikvisionVisitorMessage();

        // 海康威视协议特点：深度集成视频监控
        ByteBuffer buffer = ByteBuffer.wrap(rawData).order(ByteOrder.BIG_ENDIAN);

        // 1. 解析访客预约信息
        parseVisitorAppointment(buffer, message);

        // 2. 解析访客到访信息
        parseVisitorArrival(buffer, message);

        // 3. 解析视频联动信息
        parseVideoLinkage(buffer, message);

        return message;
    }

    @Override
    public Future<ProtocolProcessResult> processAccessBusiness(String businessType, Map<String, Object> businessData, Long deviceId) {
        switch (businessType) {
            case "VISITOR_REGISTRATION":
                return processVisitorRegistration(businessData, deviceId);
            case "VISITOR_ARRIVAL":
                return processVisitorArrival(businessData, deviceId);
            case "VISITOR_DEPARTURE":
                return processVisitorDeparture(businessData, deviceId);
            case "BLACKLIST_CHECK":
                return processBlacklistCheck(businessData, deviceId);
            case "VIDEO_VERIFY":
                return processVideoVerification(businessData, deviceId);
            default:
                return CompletableFuture.completedFuture(
                    new ProtocolProcessResult(false, "不支持的访客业务类型: " + businessType)
                );
        }
    }

    private Future<ProtocolProcessResult> processVisitorRegistration(Map<String, Object> businessData, Long deviceId) {
        // 海康威视特色功能：视频抓拍访客照片
        CompletableFuture<ProtocolProcessResult> future = new CompletableFuture<>();

        try {
            // 1. 获取访客信息
            String visitorName = (String) businessData.get("visitorName");
            String idCardNumber = (String) businessData.get("idCardNumber");
            String phoneNumber = (String) businessData.get("phoneNumber");
            String visitReason = (String) businessData.get("visitReason");

            // 2. 调用身份证读取器读取访客信息
            idCardReaderService.readIdCard(deviceId)
                .thenAccept(idCardInfo -> {
                    // 3. 验证身份证信息
                    if (validateIdCardInfo(idCardInfo)) {
                        // 4. 抓拍访客照片（海康威视摄像头）
                        captureVisitorPhoto(deviceId, idCardInfo.getName())
                            .thenAccept(photoUrl -> {
                                // 5. 创建访客记录
                                VisitorEntity visitor = createVisitorRecord(idCardInfo, photoUrl, businessData);

                                // 6. 生成访客码
                                generateVisitorQrCode(visitor.getVisitorId())
                                    .thenAccept(qrCodeUrl -> {
                                        visitor.setQrCodeUrl(qrCodeUrl);
                                        visitorService.save(visitor);

                                        // 7. 发送访客通知
                                        sendVisitorNotification(visitor);

                                        future.complete(new ProtocolProcessResult(true, "访客注册成功，照片已抓拍"));
                                    });
                            });
                    } else {
                        future.complete(new ProtocolProcessResult(false, "身份证信息验证失败"));
                    }
                })
                .exceptionally(throwable -> {
                    future.complete(new ProtocolProcessResult(false, "身份证读取失败: " + throwable.getMessage()));
                });

        } catch (Exception e) {
            future.complete(new ProtocolProcessResult(false, "访客注册异常: " + e.getMessage()));
        }

        return future;
    }

    private CompletableFuture<String> captureVisitorPhoto(Long deviceId, String visitorName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 获取关联的摄像头
                List<CameraEntity> cameras = getVisitorCameras(deviceId);

                if (cameras.isEmpty()) {
                    throw new RuntimeException("未找到关联的摄像头");
                }

                CameraEntity camera = cameras.get(0);

                // 调用海康威视摄像头抓拍接口
                return videoSurveillanceService.capturePhoto(
                    camera.getCameraId(),
                    visitorName,
                    "访客照片"
                );

            } catch (Exception e) {
                throw new RuntimeException("访客照片抓拍失败", e);
            }
        });
    }

    private Future<ProtocolProcessResult> processVideoVerification(Map<String, Object> businessData, Long deviceId) {
        // 海康威视特色功能：实时人脸识别验证
        CompletableFuture<ProtocolProcessResult> future = new CompletableFuture<>();

        try {
            // 1. 获取访客信息和人脸图像
            Long visitorId = (Long) businessData.get("visitorId");
            String faceImage = (String) businessData.get("faceImage");

            // 2. 获取访客注册的人脸特征
            VisitorEntity visitor = visitorService.getById(visitorId);
            String registeredFaceFeature = visitor.getFaceFeature();

            // 3. 进行人脸比对
            faceRecognitionService.compareFace(faceImage, registeredFaceFeature)
                .thenAccept(compareResult -> {
                    if (compareResult.getScore() > 0.8) {
                        // 4. 验证通过，开门并记录日志
                        openVisitorDoor(visitorId, deviceId);

                        // 5. 抓拍到访照片
                        captureVisitPhoto(visitorId, deviceId)
                            .thenAccept(photoUrl -> {
                                saveVisitRecord(visitorId, photoUrl, "SUCCESS");
                                future.complete(new ProtocolProcessResult(true, "人脸识别验证通过"));
                            });

                    } else {
                        // 6. 验证失败，记录日志并告警
                        saveVisitRecord(visitorId, null, "FAILED");
                        sendSecurityAlert("访客人脸识别验证失败", visitorId);

                        future.complete(new ProtocolProcessResult(false, "人脸识别验证失败"));
                    }
                })
                .exceptionally(throwable -> {
                    future.complete(new ProtocolProcessResult(false, "人脸识别验证异常: " + throwable.getMessage()));
                });

        } catch (Exception e) {
            future.complete(new ProtocolProcessResult(false, "视频验证处理异常: " + e.getMessage()));
        }

        return future;
    }

    private List<CameraEntity> getVisitorCameras(Long deviceId) {
        // 查询与访客机关联的摄像头
        return cameraService.listByAccessPointId(getAccessPointIdByDevice(deviceId));
    }

    private Long getAccessPointIdByDevice(Long deviceId) {
        // 通过设备ID查询对应的访问点
        DeviceEntity device = deviceService.getById(deviceId);
        return device.getAccessPointId();
    }
}
```

## 📹 视频监控模块设备兼容指南

### 支持的厂商和设备

| 厂商 | 协议类型 | 支持设备型号 | 功能特性 |
|------|----------|--------------|----------|
| 海康威视 | VIDEO_HIKVISION_V2_0 | DS-2CDxxxx, DS-2DFxxxx, DS-2DExxxx | 实时流媒体、云台控制、智能分析、人脸识别 |
| 大华技术 | VIDEO_DAHUA_V2_1 | DH-IPC-HFWxxxx, DH-SD-xxxx, DH-NVRxxxx | 实时监控、智能跟踪、行为分析、车牌识别 |
| 宇石科技 | VIDEO_EBPS_V1_5 | IPC-Bxxxx, NVR-xxxx, PTZ-xxxx | AI视频分析、人员统计、异常检测、3D行为分析 |
| 华为 | VIDEO_HUAWEI_V1_8 | Mxxxx系列C系列, SDxxxx系列 | AI智能分析、云存储、边缘计算、5G传输 |

### 视频设备接入开发流程

#### 1. 创建视频协议适配器

```java
@Component
public class DahuaVideoAdapter implements ProtocolAdapter {

    @Override
    public String getProtocolType() {
        return "VIDEO_DAHUA_V2_1";
    }

    @Override
    public String getManufacturer() {
        return "大华技术";
    }

    @Override
    public String[] getSupportedDeviceModels() {
        return new String[]{"DH-IPC-HFW5442", "DH-SD-6AL245", "DH-NVR5216"};
    }

    @Override
    public ProtocolMessage parseDeviceMessage(byte[] rawData, Long deviceId) throws ProtocolParseException {
        // 解析大华视频协议
        DahuaVideoMessage message = new DahuaVideoMessage();

        // 大华协议特点：支持智能分析和边缘计算
        ByteBuffer buffer = ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN);

        // 1. 解析协议头
        parseProtocolHeader(buffer, message);

        // 2. 解析视频流信息
        parseVideoStreamInfo(buffer, message);

        // 3. 解析AI分析结果
        parseAIAnalysisResult(buffer, message);

        return message;
    }

    @Override
    public Future<ProtocolProcessResult> processAccessBusiness(String businessType, Map<String, Object> businessData, Long deviceId) {
        // 视频设备主要处理门禁联动事件
        switch (businessType) {
            case "ACCESS_CONTROL_LINKAGE":
                return processAccessControlLinkage(businessData, deviceId);
            case "VIDEO_ANALYSIS_EVENT":
                return processVideoAnalysisEvent(businessData, deviceId);
            case "ALARM_DETECTION":
                return processAlarmDetection(businessData, deviceId);
            default:
                return CompletableFuture.completedFuture(
                    new ProtocolProcessResult(false, "不支持的业务类型: " + businessType)
                );
        }
    }

    private Future<ProtocolProcessResult> processVideoAnalysisEvent(Map<String, Object> businessData, Long deviceId) {
        // 大华特色功能：边缘AI分析
        CompletableFuture<ProtocolProcessResult> future = new CompletableFuture<>();

        try {
            // 1. 获取AI分析结果
            String eventType = (String) businessData.get("eventType");
            String eventDescription = (String) businessData.get("eventDescription");
            Double confidence = (Double) businessData.get("confidence");
            String imageUrl = (String) businessData.get("imageUrl");
            String videoUrl = (String) businessData.get("videoUrl");

            // 2. 根据事件类型处理
            switch (eventType) {
                case "PERSON_COUNT":
                    future.complete(processPersonCount(businessData, deviceId));
                    break;
                case "FACE_DETECTION":
                    future.complete(processFaceDetection(businessData, deviceId));
                    break;
                case "PERIMETER_INTRUSION":
                    future.complete(processPerimeterIntrusion(businessData, deviceId));
                    break;
                case "ABANDONED_OBJECT":
                    future.complete(processAbandonedObject(businessData, deviceId));
                    break;
                case "CROWD_DENSITY":
                    future.complete(processCrowdDensity(businessData, deviceId));
                    break;
                case "LOITERING":
                    future.complete(processLoitering(businessData, deviceId));
                    break;
                case "FIGHTING":
                    future.complete(processFighting(businessData, deviceId));
                    break;
                default:
                    // 未知事件类型，记录日志
                    log.warn("未知的视频分析事件类型: {}", eventType);
                    future.complete(new ProtocolProcessResult(true, "事件已记录"));
            }

        } catch (Exception e) {
            future.complete(new ProtocolProcessResult(false, "视频分析事件处理异常: " + e.getMessage()));
        }

        return future;
    }

    private ProtocolProcessResult processPersonCount(Map<String, Object> businessData, Long deviceId) {
        // 大华特色功能：人员数量统计
        try {
            // 1. 获取人员数量和区域信息
            Integer personCount = (Integer) businessData.get("personCount");
            String regionId = (String) businessData.get("regionId");
            Integer maxCapacity = (Integer) businessData.get("maxCapacity");
            Double density = (Double) businessData.get("density");

            // 2. 保存人员统计数据
            VideoAnalysisEvent event = new VideoAnalysisEvent();
            event.setDeviceId(deviceId);
            event.setEventType("PERSON_COUNT");
            event.setEventTime(LocalDateTime.now());
            event.setPersonCount(personCount);
            event.setRegionId(regionId);
            event.setMaxCapacity(maxCapacity);
            event.setDensity(density);
            event.setConfidence((Double) businessData.get("confidence"));

            videoAnalysisEventService.save(event);

            // 3. 检查是否超过容量限制
            if (personCount > maxCapacity) {
                // 触发容量告警
                triggerCapacityAlert(regionId, personCount, maxCapacity);
            }

            return new ProtocolProcessResult(true, String.format("人员统计完成，当前人数：%d，密度：%.2f", personCount, density));

        } catch (Exception e) {
            log.error("人员数量统计处理失败", e);
            return new ProtocolProcessResult(false, "人员数量统计处理失败: " + e.getMessage());
        }
    }

    private ProtocolProcessResult processFaceDetection(Map<String, Object> businessData, Long deviceId) {
        // 大华特色功能：人脸检测和识别
        try {
            // 1. 获取人脸检测结果
            List<FaceDetectionResult> faceResults = (List<FaceDetectionResult>) businessData.get("faceResults");
            String imageUrl = (String) businessData.get("imageUrl");

            // 2. 人脸比对识别
            for (FaceDetectionResult faceResult : faceResults) {
                String faceImage = faceResult.getFaceImage();
                String boundingBox = faceResult.getBoundingBox();

                // 调用人脸识别服务
                faceRecognitionService.recognizeFace(faceImage)
                    .thenAccept(recognizeResult -> {
                        if (recognizeResult.isMatched()) {
                            // 匹配成功，记录人员信息
                            recordFaceMatch(deviceId, recognizeResult, imageUrl, boundingBox);
                        } else {
                            // 匹配失败，记录为陌生人
                            recordUnknownPerson(deviceId, faceImage, imageUrl, boundingBox);
                        }
                    });
            }

            return new ProtocolProcessResult(true, String.format("人脸检测完成，检测到%d张人脸", faceResults.size()));

        } catch (Exception e) {
            log.error("人脸检测处理失败", e);
            return new ProtocolProcessResult(false, "人脸检测处理失败: " + e.getMessage());
        }
    }

    private ProtocolProcessResult processPerimeterIntrusion(Map<String, Object> businessData, Long deviceId) {
        // 大华特色功能：周界入侵检测
        try {
            // 1. 获取入侵信息
            String intrusionType = (String) businessData.get("intrusionType");
            String intrusionDirection = (String) businessData.get("intrusionDirection");
            String intrusionPoint = (String) businessData.get("intrusionPoint");
            String alarmLevel = (String) businessData.get("alarmLevel");
            String videoUrl = (String) businessData.get("videoUrl");

            // 2. 保存入侵事件
            SecurityAlarmEvent alarmEvent = new SecurityAlarmEvent();
            alarmEvent.setDeviceId(deviceId);
            alarmEvent.setEventType("PERIMETER_INTRUSION");
            alarmEvent.setAlarmTime(LocalDateTime.now());
            alarmEvent.setIntrusionType(intrusionType);
            alarmEvent.setIntrusionDirection(intrusionDirection);
            alarmEvent.setIntrusionPoint(intrusionPoint);
            alarmEvent.setAlarmLevel(alarmLevel);
            alarmEvent.setVideoUrl(videoUrl);

            securityAlarmEventService.save(alarmEvent);

            // 3. 触发安全告警
            triggerSecurityAlarm(alarmEvent);

            // 4. 联动门禁控制
            linkageAccessControl(alarmEvent);

            return new ProtocolProcessResult(true, "周界入侵检测完成，已触发告警");

        } catch (Exception e) {
            log.error("周界入侵检测处理失败", e);
            return new ProtocolProcessResult(false, "周界入侵检测处理失败: " + e.getMessage());
        }
    }

    private void triggerSecurityAlarm(SecurityAlarmEvent alarmEvent) {
        // 发送实时告警通知
        alarmNotificationService.sendAlarm(alarmEvent);

        // 推送告警到管理端
        websocketService.sendAlarmToAdmins(alarmEvent);

        // 记录告警日志
        alarmLogService.saveAlarmLog(alarmEvent);
    }

    private void linkageAccessControl(SecurityAlarmEvent alarmEvent) {
        // 获取关联的门禁设备
        List<AccessDeviceEntity> accessDevices = getLinkedAccessDevices(alarmEvent.getDeviceId());

        // 根据告警级别执行不同的控制策略
        switch (alarmEvent.getAlarmLevel()) {
            case "HIGH":
                // 高级别告警：立即锁定所有关联门禁
                for (AccessDeviceEntity device : accessDevices) {
                    lockAccessDevice(device.getDeviceId());
                }
                break;
            case "MEDIUM":
                // 中级别告警：要求二次验证
                for (AccessDeviceEntity device : accessDevices) {
                    requireSecondaryVerification(device.getDeviceId());
                }
                break;
            case "LOW":
                // 低级别告警：记录日志，正常通行
                for (AccessDeviceEntity device : accessDevices) {
                    recordAccessLog(device.getDeviceId(), "安全告警：" + alarmEvent.getIntrusionType());
                }
                break;
        }
    }
}
```

## 🔧 统一设备接入开发流程

### 标准接入步骤

1. **厂商协议分析**
   - 获取厂商官方协议文档
   - 分析消息格式和数据结构
   - 确定支持的设备型号和功能
   - 识别协议扩展点

2. **协议适配器开发**
   - 实现 `ProtocolAdapter` 接口
   - 创建消息实体类
   - 实现消息解析和构建
   - 实现业务数据处理

3. **工厂注册集成**
   - 添加到自动注册列表
   - 配置设备型号映射
   - 测试适配器功能

4. **业务服务集成**
   - 集成到对应业务模块
   - 实现厂商特色功能
   - 处理异常和错误

5. **测试验证**
   - 单元测试适配器功能
   - 集成测试业务流程
   - 性能测试和优化

### 开发规范要求

1. **命名规范**
   ```java
   // 协议类型命名规范：{业务领域}_{厂商}_{版本号}
   public static final String PROTOCOL_TYPE = "ACCESS_ENTROPY_V4_8";

   // 适配器类命名规范：{厂商}+{业务}+Adapter
   public class EntropyAccessAdapter implements ProtocolAdapter
   ```

2. **异常处理规范**
   ```java
   // 抛出标准异常类型
   throw new ProtocolParseException("协议解析失败: " + e.getMessage(), e);
   throw new ProtocolBuildException("协议构建失败: " + e.getMessage(), e);
   ```

3. **日志记录规范**
   ```java
   // 结构化日志记录
   log.info("[{}] {} 处理消息: deviceSn={}, messageType={}, processTime={}ms",
       protocolType, operation, deviceSn, messageType, processTime);
   ```

4. **配置管理规范**
   ```yaml
   # 协议配置示例
   device-communication:
     protocols:
       new-vendor:
         enabled: true
         adapter-class: com.example.NewVendorAdapter
         device-models: [NV-100, NV-200]
         features: [face_recognition, offline_sync]
   ```

## 📋 设备兼容性检查清单

### 开发阶段检查清单

#### 协议适配器开发
- [ ] 实现 `ProtocolAdapter` 接口的所有方法
- [ ] 创建完整的协议消息实体类
- [ ] 实现协议头解析和构建逻辑
- [ ] 实现业务数据处理方法
- [ ] 添加必要的验证和错误处理
- [ ] 完成日志记录和性能监控

#### 消息处理
- [ ] 支持二进制数据解析
- [ ] 支持十六进制字符串解析
- [ ] 实现数据完整性验证
- [ ] 处理网络字节序转换
- [ ] 实现数据压缩和解压缩（如需要）

#### 业务集成
- [ ] 与对应业务模块服务集成
- [ ] 实现厂商特色功能
- [ ] 支持异步业务处理
- [ ] 实现事务管理
- [ ] 添加必要的缓存优化

#### 测试验证
- [ ] 编写单元测试用例
- [ ] 编写集成测试用例
- [ ] 进行性能测试
- [ ] 进行稳定性测试
- [ ] 验证多设备并发处理

### 部署阶段检查清单

#### 配置验证
- [ ] 协议适配器正确注册到工厂
- [ ] 设备型号映射配置正确
- [ ] 设备参数配置完整
- [ ] 网络连接配置正确
- [ ] 缓存配置合理

#### 功能验证
- [ ] 设备连接和通信正常
- [ ] 消息解析和构建正确
- [ ] 业务处理逻辑正常
- [ ] 错误处理机制有效
- [ ] 监控指标正常

#### 性能验证
- [ ] 消息处理延迟达标
- [ ] 并发处理能力达标
- [ ] 内存使用合理
- [ ] CPU使用合理
- [ ] 网络带宽使用合理

## 🚀 快速接入模板

### 新厂商协议接入模板

```java
/**
 * {厂商}{业务}协议V{版本号}适配器
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since {创建日期}
 */
@Slf4j
@Component
public class {Vendor}{Business}Adapter implements ProtocolAdapter {

    // ==================== 协议常量定义 ====================

    /** 协议类型标识 */
    private static final String PROTOCOL_TYPE = "{BUSINESS}_{VENDOR}_V{VERSION}";

    /** 支持的设备型号 */
    private static final String[] SUPPORTED_DEVICE_MODELS = {
        "{MODEL1}", "{MODEL2}", "{MODEL3}"
    };

    // ==================== 协议标识接口实现 ====================

    @Override
    public String getProtocolType() {
        return PROTOCOL_TYPE;
    }

    @Override
    public String getManufacturer() {
        return "{厂商}";
    }

    @Override
    public String getVersion() {
        return "V{VERSION}";
    }

    @Override
    public String[] getSupportedDeviceModels() {
        return SUPPORTED_DEVICE_MODELS.clone();
    }

    @Override
    public boolean isDeviceModelSupported(String deviceModel) {
        return Arrays.asList(SUPPORTED_DEVICE_MODELS).contains(deviceModel.toUpperCase());
    }

    // ==================== 消息处理核心接口实现 ====================

    @Override
    public ProtocolMessage parseDeviceMessage(byte[] rawData, Long deviceId) throws ProtocolParseException {
        log.debug("[{厂商}{业务}协议] 开始解析设备消息, deviceId={}, dataLength={}", deviceId, rawData.length);

        try {
            // 1. 基础数据验证
            validateRawData(rawData);

            // 2. 解析协议头
            {Vendor}{Business}Message message = parseProtocolHeader(rawData);

            // 3. 根据消息类型解析业务数据
            parseBusinessData(rawData, message);

            log.debug("[{厂商}{业务}协议] 消息解析完成, messageType={}, deviceSn={}",
                message.getMessageTypeName(), message.getDeviceId());

            return message;

        } catch (Exception e) {
            log.error("[{厂商}{业务}协议] 消息解析失败, deviceId={}", deviceId, e);
            throw new ProtocolParseException("消息解析失败: " + e.getMessage(), e);
        }
    }

    // ==================== 私有辅助方法 ====================

    private void validateRawData(byte[] rawData) throws ProtocolParseException {
        if (rawData == null || rawData.length < MIN_MESSAGE_LENGTH) {
            throw new ProtocolParseException("数据长度不足，无法解析协议头");
        }
    }

    private {Vendor}{Business}Message parseProtocolHeader(byte[] rawData) {
        // 根据厂商协议格式解析协议头
        ByteBuffer buffer = ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN);

        {Vendor}{Business}Message message = new {Vendor}{Business}Message();

        // 解析协议标识、版本、设备ID等
        // TODO: 实现具体的协议头解析逻辑

        return message;
    }

    private void parseBusinessData(byte[] rawData, {Vendor}{Business}Message message) {
        // 根据消息类型解析业务数据
        switch (message.getMessageTypeCode()) {
            case MSG_TYPE_{BUSINESS}_EVENT:
                parse{Business}Event(rawData, message);
                break;
            case MSG_TYPE_DEVICE_STATUS:
                parseDeviceStatus(rawData, message);
                break;
            // 其他消息类型...
        }
    }

    // TODO: 实现其他必要方法
    @Override
    public byte[] buildDeviceResponse(String messageType, Map<String, Object> businessData, Long deviceId) throws ProtocolBuildException {
        // 实现响应消息构建
        return new byte[0];
    }

    @Override
    public Future<ProtocolProcessResult> process{Business}Business(String businessType, Map<String, Object> businessData, Long deviceId) {
        // 实现业务处理逻辑
        return CompletableFuture.completedFuture(new ProtocolProcessResult());
    }

    // 其他接口方法实现...
}
```

这个设备兼容开发指南为IOE-DREAM智慧园区一卡通管理平台提供了完整、标准化的设备接入流程，确保各个业务模块能够快速、规范地兼容不同厂商的设备，实现真正的厂商无关性。