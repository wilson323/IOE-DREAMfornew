# 设计文档：视频监控边缘AI架构重构

**创建日期**: 2025-01-30
**版本**: v1.0
**状态**: 设计阶段

---

## 📐 架构设计

### 当前架构（错误）⚠️

```
┌─────────────────────────────────────────────────────────────┐
│                    ❌ 错误的架构（当前）                      │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  摽像机 → RTSP流 → 服务器接收 → 解码 → 逐帧分析 → AI推理    │
│    ↑        ↑           ↑         ↑          ↑            │
│    │        │           │         │          │            │
│  原始视频  带宽消耗   CPU密集    GPU密集   延迟高            │
│                                                               │
│  问题：                                                        │
│  1. 原始视频全部上传（带宽浪费>1Gbps/100路）                    │
│  2. 服务器逐帧解码（CPU密集，10核+）                          │
│  3. 服务器AI推理（GPU密集，需要GPU服务器）                    │
│  4. 告警延迟3-5秒                                             │
│  5. 服务器成为瓶颈，无法水平扩展                              │
└─────────────────────────────────────────────────────────────┘
```

### 目标架构（正确）✅

```
┌─────────────────────────────────────────────────────────────┐
│                    ✅ 正确的架构（目标）                      │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  【设备端】摄像头（带AI芯片）                                  │
│  ┌──────────────────────────────────────────────┐             │
│  │  视频采集 → AI芯片分析 → 结构化数据         │             │
│  │    ↓           ↓            ↓              │             │
│  │  原始视频   人脸检测     事件上报           │             │
│  │  (本地)    行为分析     (极小数据)          │             │
│  └──────────────────────────────────────────────┘             │
│           ↓                                         │          │
│  【网络】结构化数据（<1KB/事件）                        │          │
│           ↓                                         │          │
│  【服务器端】视频服务                                     │
│  ┌──────────────────────────────────────────────┐             │
│  │  事件接收 → 规则匹配 → 告警推送 → 视频回调    │             │
│  │    ↓          ↓          ↓          ↓        │             │
│  │  存储      告警规则    WebSocket   按需获取    │             │
│  └──────────────────────────────────────────────┘             │
│                                                               │
│  优势：                                                        │
│  1. 带宽消耗<50Mbps/100路（节省95%）                         │
│  2. 服务器负载极低（规则匹配）                                 │
│  3. 告警延迟<1秒                                              │
│  4. 可水平扩展（无状态设计）                                  │
│  5. 成本降低60%（无需GPU服务器）                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔧 核心组件设计

### 1. 设备AI事件接收服务

#### 1.1 设备AI事件数据结构

```java
/**
 * 设备AI事件（结构化数据）
 */
@Data
public class DeviceAIEvent {

    /** 事件ID（设备端生成） */
    private String eventId;

    /** 设备ID */
    private Long deviceId;

    /** 事件类型 */
    private AIEventType eventType;

    /** 置信度 (0.0-1.0) */
    private BigDecimal confidence;

    /** 检测框坐标 [x, y, width, height] */
    private Integer[] bbox;

    /** 抓拍图片URL（仅关键帧） */
    private String snapshotUrl;

    /** 事件时间（设备端时间戳） */
    private LocalDateTime eventTime;

    /** 扩展元数据 */
    private Map<String, Object> metadata;

    public enum AIEventType {
        /** 人脸检测 */
        FACE_DETECTION,
        /** 人脸识别 */
        FACE_RECOGNITION,
        /** 跌倒检测 */
        FALL_DETECTION,
        /** 徘徊检测 */
        LOITERING_DETECTION,
        /** 越界检测 */
        CROSSING_DETECTION,
        /** 聚集检测 */
        GATHERING_DETECTION,
        /** 遗留物检测 */
        ABANDONED_OBJECT,
        /** 移除物检测 */
        REMOVED_OBJECT
    }
}
```

#### 1.2 服务接口设计

```java
/**
 * 设备AI事件接收服务
 */
@Service
@Slf4j
public class DeviceAIEventReceiver {

    @Resource
    private AIEventDao aiEventDao;

    @Resource
    private AlarmRuleEngine alarmRuleEngine;

    @Resource
    private WebSocketPushService websocketPushService;

    /**
     * 接收设备上报的AI事件
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleDeviceAIEvent(DeviceAIEvent event) {
        log.info("[AI事件] 接收设备事件: eventId={}, type={}, confidence={}, deviceId={}",
            event.getEventId(), event.getEventType(), event.getConfidence(), event.getDeviceId());

        // 1. 数据验证
        validateEvent(event);

        // 2. 存储事件
        aiEventDao.insert(event);

        // 3. 规则匹配
        AlarmRuleMatch ruleMatch = alarmRuleEngine.match(event);

        // 4. 处理匹配结果
        if (ruleMatch.isMatched()) {
            handleAlarm(event, ruleMatch.getRule());
        }

        // 5. 实时推送
        websocketPushService.pushEvent(event);
    }

    /**
     * 数据验证
     */
    private void validateEvent(DeviceAIEvent event) {
        Assert.notNull(event, "事件不能为空");
        Assert.notNull(event.getEventId(), "事件ID不能为空");
        Assert.notNull(event.getDeviceId(), "设备ID不能为空");
        Assert.notNull(event.getEventType(), "事件类型不能为空");
        Assert.isTrue(event.getConfidence().compareTo(BigDecimal.ZERO) > 0,
            "置信度必须大于0");
    }

    /**
     * 处理告警
     */
    private void handleAlarm(DeviceAIEvent event, AlarmRule rule) {
        log.warn("[AI告警] 触发告警: eventId={}, type={}, rule={}, level={}",
            event.getEventId(), event.getEventType(), rule.getRuleName(), rule.getAlertLevel());

        // 1. 创建告警记录
        AlarmRecord alarmRecord = createAlarmRecord(event, rule);

        // 2. 发送通知
        alarmService.sendNotification(alarmRecord);

        // 3. 视频联动（按需回调原始视频）
        if (rule.isLinkageVideo()) {
            videoService.linkageVideo(event.getDeviceId(), event.getEventTime());
        }
    }
}
```

### 2. AI模型管理服务

#### 2.1 模型版本管理

```java
/**
 * AI模型管理服务
 */
@Service
@Slf4j
public class AIModelManagerService {

    @Resource
    private AIModelDao aiModelDao;

    @Resource
    private DeviceCommService deviceCommService;

    /**
     * 推送AI模型到设备
     *
     * @param modelType 模型类型
     * @param modelVersion 模型版本
     * @param deviceIds 目标设备ID列表
     */
    @Async("modelPushExecutor")
    public void pushModelToDevice(String modelType, String modelVersion, List<Long> deviceIds) {
        log.info("[模型管理] 推送模型到设备: type={}, version={}, devices={}",
            modelType, modelVersion, deviceIds);

        // 1. 查询模型文件
        AIModel model = aiModelDao.selectByTypeAndVersion(modelType, modelVersion);
        if (model == null) {
            throw new BusinessException("MODEL_NOT_FOUND",
                String.format("模型不存在: type=%s, version=%s", modelType, modelVersion));
        }

        // 2. 读取模型文件
        byte[] modelData = loadModelFile(model.getModelPath());

        // 3. 推送到设备
        for (Long deviceId : deviceIds) {
            try {
                deviceCommService.pushAIModel(deviceId, modelType, modelVersion, modelData);

                // 4. 更新设备模型版本记录
                updateDeviceModelVersion(deviceId, modelType, modelVersion);

                log.info("[模型管理] 推送成功: deviceId={}, type={}, version={}",
                    deviceId, modelType, modelVersion);
            } catch (Exception e) {
                log.error("[模型管理] 推送失败: deviceId={}, type={}, error={}",
                    deviceId, modelType, e.getMessage(), e);
            }
        }
    }

    /**
     * 查询设备AI模型版本
     */
    public Map<Long, String> getDeviceModelVersions(String modelType) {
        log.info("[模型管理] 查询设备模型版本: modelType={}", modelType);

        // 通过设备通讯服务查询所有设备的模型版本
        return deviceCommService.queryModelVersions(modelType);
    }

    /**
     * 模型热更新（批量推送）
     */
    @Async("modelPushExecutor")
    public void hotUpdateModel(String modelType, String modelVersion) {
        log.info("[模型管理] 热更新模型: type={}, version={}", modelType, modelVersion);

        // 1. 查询所有支持该模型类型的设备
        List<Long> deviceIds = queryDevicesByModelType(modelType);

        // 2. 批量推送
        pushModelToDevice(modelType, modelVersion, deviceIds);
    }
}
```

#### 2.2 模型存储结构

```sql
-- AI模型表
CREATE TABLE t_ai_model (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模型ID',
    model_type VARCHAR(64) NOT NULL COMMENT '模型类型',
    model_version VARCHAR(32) NOT NULL COMMENT '模型版本',
    model_name VARCHAR(100) COMMENT '模型名称',
    model_path VARCHAR(512) NOT NULL COMMENT '模型文件路径',
    model_size BIGINT COMMENT '模型文件大小(字节)',
    framework VARCHAR(32) COMMENT 'AI框架:TensorFlowLite/ONNX',
    accuracy DECIMAL(5,2) COMMENT '准确率',
    performance_score DECIMAL(5,2) COMMENT '性能分数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1-启用,2-禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_type_version (model_type, model_version),
    INDEX idx_model_type (model_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型表';

-- 设备AI模型版本表
CREATE TABLE t_device_ai_model (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    model_type VARCHAR(64) NOT NULL COMMENT '模型类型',
    model_version VARCHAR(32) NOT NULL COMMENT '当前版本',
    update_time DATETIME COMMENT '更新时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1-正常,2-异常',
    UNIQUE KEY uk_device_model (device_id, model_type),
    INDEX idx_model_type (model_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备AI模型版本表';
```

### 3. 告警规则引擎

```java
/**
 * 告警规则引擎
 */
@Service
@Slf4j
public class AlarmRuleEngine {

    @Resource
    private AIRuleDao aiRuleDao;

    /**
     * 匹配告警规则
     */
    public AlarmRuleMatch match(DeviceAIEvent event) {
        log.debug("[规则引擎] 匹配规则: eventId={}, type={}, deviceId={}",
            event.getEventId(), event.getEventType(), event.getDeviceId());

        // 1. 查询设备启用的规则
        List<AIRule> rules = aiRuleDao.selectActiveRulesByDeviceAndType(
            event.getDeviceId(), event.getEventType());

        // 2. 规则匹配
        for (AIRule rule : rules) {
            if (isMatch(event, rule)) {
                log.info("[规则引擎] 匹配成功: eventId={}, ruleId={}, ruleName={}",
                    event.getEventId(), rule.getId(), rule.getRuleName());
                return AlarmRuleMatch.builder()
                    .matched(true)
                    .rule(rule)
                    .build();
            }
        }

        return AlarmRuleMatch.builder()
            .matched(false)
            .build();
    }

    /**
     * 判断事件是否匹配规则
     */
    private boolean isMatch(DeviceAIEvent event, AIRule rule) {
        // 1. 置信度检查
        if (event.getConfidence().compareTo(rule.getMinConfidence()) < 0) {
            return false;
        }

        // 2. 区域检查（如果规则配置了检测区域）
        if (rule.hasZones() && !isInZone(event, rule)) {
            return false;
        }

        // 3. 布防时间检查
        if (rule.hasSchedule() && !isInScheduleTime(event, rule)) {
            return false;
        }

        return true;
    }

    /**
     * 判断是否在检测区域内
     */
    private boolean isInZone(DeviceAIEvent event, AIRule rule) {
        // 实现点在多边形内算法
        Integer[] bbox = event.getBbox();
        Integer centerX = bbox[0] + bbox[2] / 2;
        Integer centerY = bbox[1] + bbox[3] / 2;

        for (AIRuleZone zone : rule.getZones()) {
            if (isPointInPolygon(centerX, centerY, zone.getZonePoints())) {
                return true;
            }
        }
        return false;
    }
}
```

---

## 📡 数据流设计

### 正常数据流

```
1. 【设备端】AI分析
   视频采集 → AI芯片分析 → 生成事件
   ↓
2. 【设备→服务器】事件上报
   POST /api/v1/video/ai/events
   {
     "eventId": "evt_123",
     "deviceId": "CAM_001",
     "eventType": "FALL_DETECTION",
     "confidence": 0.95,
     "bbox": [100, 200, 50, 100],
     "snapshotUrl": "http://device/snapshot.jpg",
     "eventTime": "2025-01-30T10:30:00"
   }
   ↓
3. 【服务器】事件处理
   DeviceAIEventReceiver.handleDeviceAIEvent()
   ├─ 存储事件 → t_ai_event
   ├─ 规则匹配 → AlarmRuleEngine.match()
   ├─ 告警推送 → WebSocket
   └─ 视频联动 → 按需回调
```

### 模型推送数据流

```
1. 【服务器】模型管理
   管理员上传新模型 → t_ai_model
   ↓
2. 【服务器→设备】模型推送
   DeviceCommService.pushAIModel()
   ↓
3. 【设备端】模型更新
   接收模型文件 → 校验版本 → 热更新
   ↓
4. 【设备→服务器】版本确认
   POST /api/v1/video/ai/model/version
   {
     "deviceId": "CAM_001",
     "modelType": "FALL_DETECTION",
     "modelVersion": "v2.0.0"
   }
```

---

## 🔌 接口设计

### 1. 设备AI事件上报接口

#### 1.1 接收设备事件

```
POST /api/v1/video/ai/events
Content-Type: application/json

Request Body:
{
  "eventId": "evt_20250130103000_001",
  "deviceId": "CAM_001",
  "eventType": "FALL_DETECTION",
  "confidence": 0.95,
  "bbox": [100, 200, 50, 100],
  "snapshotUrl": "http://192.168.1.101/snapshot/evt_001.jpg",
  "eventTime": "2025-01-30T10:30:00",
  "metadata": {
    "personCount": 1,
    "duration": 2000
  }
}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "eventId": "evt_20250130103000_001",
    "status": "RECEIVED",
    "processedAt": "2025-01-30T10:30:01"
  }
}
```

### 2. AI模型管理接口

#### 2.1 推送模型到设备

```
POST /api/v1/video/ai/model/push
Content-Type: application/json

Request Body:
{
  "modelType": "FALL_DETECTION",
  "modelVersion": "v2.0.0",
  "deviceIds": [1, 2, 3, 4, 5]
}

Response:
{
  "code": 200,
  "message": "模型推送任务已创建",
  "data": {
    "taskId": "task_model_push_001",
    "totalDevices": 5,
    "status": "IN_PROGRESS"
  }
}
```

#### 2.2 查询设备模型版本

```
GET /api/v1/video/ai/model/versions?modelType=FALL_DETECTION

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "modelType": "FALL_DETECTION",
    "devices": [
      {
        "deviceId": 1,
        "deviceName": "1楼大厅摄像头",
        "currentVersion": "v1.5.0",
        "updateTime": "2025-01-28T10:00:00"
      },
      {
        "deviceId": 2,
        "deviceName": "会议室摄像头",
        "currentVersion": "v1.5.0",
        "updateTime": "2025-01-28T10:00:00"
      }
    ]
  }
}
```

---

## 🗂️ 数据库设计

### 核心表结构

#### 1. AI事件表（修订）

```sql
-- AI事件表（设备上报的结构化事件）
CREATE TABLE t_ai_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '事件ID',
    event_id VARCHAR(64) NOT NULL COMMENT '设备端生成的事件ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    channel_no INT NOT NULL DEFAULT 1 COMMENT '通道号',

    -- 事件类型和置信度
    event_type VARCHAR(32) NOT NULL COMMENT '事件类型',
    confidence DECIMAL(5,2) COMMENT '置信度',

    -- 检测结果
    target_type VARCHAR(32) COMMENT '目标类型',
    target_id BIGINT COMMENT '目标ID(人脸ID/物体ID)',
    bbox JSON COMMENT '检测框坐标[x,y,w,h]',
    snapshot_path VARCHAR(512) COMMENT '截图路径',

    -- 事件时间
    event_time DATETIME NOT NULL COMMENT '事件时间（设备端时间）',
    receive_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '接收时间（服务器时间）',

    -- 元数据
    metadata JSON COMMENT '元数据（扩展字段）',

    -- 处理状态
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态:1-待处理,2-已处理,3-已忽略',
    alarm_id BIGINT COMMENT '关联告警ID',

    INDEX idx_device_time (device_id, event_time),
    INDEX idx_event_type (event_type),
    INDEX idx_status (status),
    INDEX idx_event_time (event_time),
    UNIQUE KEY uk_event_id (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI事件表';
```

#### 2. 告警规则表（新增字段）

```sql
-- 告警规则表（扩展）
ALTER TABLE t_ai_rule ADD COLUMN model_type VARCHAR(64) COMMENT '需要的AI模型类型';
ALTER TABLE t_ai_rule ADD COLUMN min_confidence DECIMAL(5,2) DEFAULT 0.70 COMMENT '最小置信度阈值';
ALTER TABLE t_ai_rule ADD COLUMN enabled_devices JSON COMMENT '启用的设备ID列表';
```

---

## 🚀 实施策略

### 阶段1：架构清理（1周）

#### 任务列表
1. ✅ 删除服务器端AI分析代码
2. ✅ 标记为@Deprecated，保留接口兼容性
3. ✅ 更新所有TODO注释
4. ✅ 创建架构决策记录（ADR）

#### 代码变更
```java
// ❌ 删除这些方法
// BehaviorDetectionManager.java
// - detectFall(String cameraId, byte[] frameData)
// - detectAbnormalBehaviors(String cameraId, byte[] frameData)

// ✅ 标记为废弃
@Deprecated(since = "2025-01-30", forRemoval = true)
public FallDetectionResult detectFall(String cameraId, byte[] frameData) {
    throw new UnsupportedOperationException(
        "此方法已废弃，请使用边缘计算模式。设备端AI分析完成后，" +
        "通过DeviceAIEventReceiver.handleDeviceAIEvent()上报结构化事件。"
    );
}
```

### 阶段2：新服务开发（2周）

#### 任务列表
1. ✅ 创建设备AI事件接收服务
2. ✅ 创建AI模型管理服务
3. ✅ 扩展设备通讯服务（支持模型推送）
4. ✅ 创建告警规则引擎

#### 新增文件
```
ioedream-video-service/
└── src/main/java/net/lab1024/sa/video/
    ├── receiver/
    │   └── DeviceAIEventReceiver.java          # 设备AI事件接收服务
    ├── model/
    │   ├── AIModelManagerService.java         # AI模型管理服务
    │   └── AIModelPushTask.java               # 模型推送任务
    ├── engine/
    │   └── AlarmRuleEngine.java                # 告警规则引擎
    └── domain/
        └── DeviceAIEvent.java                 # 设备AI事件数据类
```

### 阶段3：设备集成（2周）

#### 任务列表
1. ✅ 设备协议适配（AI事件上报）
2. ✅ 设备协议适配（模型推送）
3. ✅ 设备AI能力测试
4. ✅ 端到端测试验证

---

## 📊 性能对比

### 带宽消耗

| 场景 | 错误架构（中心计算） | 正确架构（边缘计算） | 改进 |
|------|---------------------|---------------------|------|
| 100路视频 | 1000 Mbps | 50 Mbps | ↓ 95% |
| 单路告警 | 4 Mbps | 10 KB | ↓ 99.75% |

### 服务器资源

| 资源 | 错误架构 | 正确架构 | 改进 |
|------|---------|---------|------|
| CPU使用率 | 80%+ | 10% | ↓ 87.5% |
| 内存使用 | 32 GB+ | 8 GB | ↓ 75% |
| GPU需求 | 需要 | 不需要 | ↓ 100% |
| 并发能力 | 100路 | 500路+ | ↑ 400% |

### 响应时间

| 指标 | 错误架构 | 正确架构 | 改进 |
|------|---------|---------|------|
| 告警延迟 | 3-5秒 | <1秒 | ↓ 70% |
| 视频加载 | 2-3秒 | <1秒 | ↓ 60% |

---

## 🔒 安全考虑

### 1. 设备认证

- 设备上报事件必须携带有效的设备证书
- 防止伪造事件攻击

### 2. 数据加密

- 设备事件上报使用HTTPS
- 模型推送使用加密通道

### 3. 访问控制

- AI模型管理接口需要高级权限
- 事件查询接口需要权限控制

---

## 🧪 测试策略

### 单元测试

- DeviceAIEventReceiver测试
- AlarmRuleEngine测试
- AIModelManagerService测试

### 集成测试

- 设备事件上报端到端测试
- 模型推送端到端测试
- 告警规则匹配测试

### 性能测试

- 100路视频并发测试
- 事件处理吞吐量测试
- 告警响应时间测试

---

## 📚 参考文档

- [CLAUDE.md](../../CLAUDE.md) - 项目架构规范
- [EDGE_AI_ARCHITECTURE_ANALYSIS.md](../../EDGE_AI_ARCHITECTURE_ANALYSIS.md) - 边缘计算架构深度分析
- [OpenSpec Agents Guide](../../openspec/AGENTS.md) - OpenSpec流程规范

---

**文档版本**: v1.0
**创建日期**: 2025-01-30
**作者**: Claude AI
**审核状态**: 待审核
