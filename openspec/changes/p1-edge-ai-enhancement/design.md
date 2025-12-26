# P1阶段：边缘AI架构增强 - 设计文档

**变更ID**: p1-edge-ai-enhancement
**文档类型**: 设计文档 (Design)
**创建日期**: 2025-01-30
**版本**: 1.0.0

---

## 1. 架构设计（Architecture Design）

### 1.1 整体架构

```
┌────────────────────────────────────────────────────────────────┐
│                       前端层（Vue 3）                           │
├────────────────────────────────────────────────────────────────┤
│  事件展示页面  │  告警管理页面  │  实时监控面板  │  数据报表   │
└──────────────┬───────────────────────────────────┬────────────┘
               │ WebSocket (STOMP)               │ HTTP/REST
               │                                   │
┌──────────────▼───────────────────────────────────▼────────────┐
│                      网关层（Gateway）                          │
├────────────────────────────────────────────────────────────────┤
│              WebSocket路由    │    API路由（/api/v1/）          │
└──────────────┬───────────────────────────────────┬────────────┘
               │                                   │
┌──────────────▼───────────────────────────────────▼────────────┐
│                    视频服务层（Video Service）                    │
├────────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
│  │ AI模型管理模块   │  │ WebSocket模块    │  │ 告警规则引擎 │ │
│  │                 │  │                 │  │  (P0已完成)  │ │
│  │ - 模型版本管理  │  │ - 连接管理       │  │              │ │
│  │ - 远程更新      │  │ - 消息推送       │  │ - 规则匹配   │ │
│  │ - 设备同步      │  │ - Pub/Sub       │  │ - 告警创建   │ │
│  └────────┬────────┘  └────────┬────────┘  └──────┬───────┘ │
│           │                    │                    │         │
│  ┌────────▼───────────────────▼────────────────────▼───────┐ │
│  │            核心业务层（P0已完成）                       │ │
│  │  - DeviceAIEventManager                                │ │
│  │  - AlarmRuleEngine                                     │ │
│  └────────┬───────────────────┬───────────────────────┬───┘ │
│           │                   │                       │     │
│  ┌────────▼────┐  ┌──────────▼────┐  ┌────────▼─────┐  │  │
│  │ MinIO存储   │  │  Redis缓存    │  │  MySQL数据库  │  │  │
│  │ (AI模型)    │  │  (Pub/Sub)    │  │  (业务数据)   │  │  │
│  └─────────────┘  └───────────────┘  └──────────────┘  │  │
│                                                           │
└───────────────────────────────────────────────────────────┘
```

### 1.2 模块依赖关系

```
P1阶段新增模块：

AI模型管理模块（独立）
├── AiModelManager（业务逻辑）
├── AiModelService（服务接口）
├── AiModelController（REST API）
└── MinIOClient（模型文件存储）

WebSocket实时推送模块（独立）
├── WebSocketConfig（配置）
├── WebSocketEventHandler（消息处理）
├── EventPushService（推送服务）
└── RedisMessageListener（订阅监听）

前端展示模块（依赖WebSocket推送）
├── 事件展示页面
├── 告警管理页面
└── 实时监控面板

测试完善模块（横切关注点）
├── 单元测试（JUnit 5 + Mockito）
├── 集成测试（TestContainers）
└── 性能测试（JMeter + Gatling）
```

---

## 2. AI模型管理模块设计

### 2.1 数据库设计

#### 表1：AI模型版本表

```sql
CREATE TABLE t_video_ai_model (
    model_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模型ID',
    model_name VARCHAR(128) NOT NULL COMMENT '模型名称',
    model_version VARCHAR(32) NOT NULL COMMENT '模型版本（如1.0.0）',
    model_type VARCHAR(64) NOT NULL COMMENT '模型类型: FALL_DETECTION, FACE_DETECTION等',
    file_path VARCHAR(512) NOT NULL COMMENT '模型文件路径（MinIO）',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    file_md5 VARCHAR(32) NOT NULL COMMENT '文件MD5校验',
    model_status TINYINT NOT NULL DEFAULT 0 COMMENT '模型状态: 0-草稿, 1-发布, 2-废弃',
    supported_events TEXT NULL COMMENT '支持的事件类型（JSON数组）',
    model_metadata TEXT NULL COMMENT '模型元数据（JSON）',
    accuracy_rate DECIMAL(5,4) NULL COMMENT '准确率',
    publish_time DATETIME NULL COMMENT '发布时间',
    published_by BIGINT NULL COMMENT '发布人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_model_version (model_name, model_version),
    INDEX idx_model_type (model_type),
    INDEX idx_model_status (model_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型版本表';
```

#### 表2：设备模型同步记录表

```sql
CREATE TABLE t_video_device_model_sync (
    sync_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '同步ID',
    model_id BIGINT NOT NULL COMMENT '模型ID',
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    sync_status TINYINT NOT NULL DEFAULT 0 COMMENT '同步状态: 0-待同步, 1-同步中, 2-成功, 3-失败',
    sync_progress INT DEFAULT 0 COMMENT '同步进度（0-100）',
    sync_start_time DATETIME NULL COMMENT '同步开始时间',
    sync_end_time DATETIME NULL COMMENT '同步结束时间',
    error_message VARCHAR(512) NULL COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_model_id (model_id),
    INDEX idx_device_id (device_id),
    INDEX idx_sync_status (sync_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备模型同步记录表';
```

### 2.2 核心组件设计

#### AiModelManager（业务逻辑层）

```java
@Component
public class AiModelManager {

    /**
     * 上传AI模型
     * 1. 文件上传到MinIO
     * 2. 计算MD5
     * 3. 保存元数据到数据库
     */
    public AiModelEntity uploadModel(MultipartFile file, AiModelUploadForm form);

    /**
     * 发布模型
     * 1. 验证模型状态
     * 2. 更新为发布状态
     * 3. 记录发布人和时间
     */
    public void publishModel(Long modelId, Long userId);

    /**
     * 远程更新设备模型
     * 1. 查询设备列表
     * 2. 创建同步任务
     * 3. 推送更新通知到设备
     * 4. 跟踪同步进度
     */
    public BatchSyncResult syncModelToDevices(Long modelId, List<String> deviceIds);

    /**
     * 查询模型列表
     */
    public List<AiModelEntity> queryModels(AiModelQueryForm form);

    /**
     * 查询同步记录
     */
    public List<DeviceModelSyncEntity> querySyncRecords(Long modelId, String deviceId);
}
```

#### AiModelController（REST API层）

```java
@RestController
@RequestMapping("/api/v1/video/ai-model")
public class AiModelController {

    /**
     * 上传AI模型
     * POST /api/v1/video/ai-model/upload
     */
    @PostMapping("/upload")
    public ResponseDTO<Long> uploadModel(
        @RequestParam("file") MultipartFile file,
        @RequestParam("modelName") String modelName,
        @RequestParam("modelVersion") String modelVersion,
        @RequestParam("modelType") String modelType
    );

    /**
     * 发布模型
     * POST /api/v1/video/ai-model/{modelId}/publish
     */
    @PostMapping("/{modelId}/publish")
    public ResponseDTO<Void> publishModel(@PathVariable Long modelId);

    /**
     * 同步模型到设备
     * POST /api/v1/video/ai-model/{modelId}/sync
     */
    @PostMapping("/{modelId}/sync")
    public ResponseDTO<BatchSyncResult> syncToDevices(
        @PathVariable Long modelId,
        @RequestBody List<String> deviceIds
    );

    /**
     * 查询模型列表
     * GET /api/v1/video/ai-model/list
     */
    @GetMapping("/list")
    public ResponseDTO<List<AiModelVO>> listModels(AiModelQueryForm form);

    /**
     * 查询同步记录
     * GET /api/v1/video/ai-model/{modelId}/sync-records
     */
    @GetMapping("/{modelId}/sync-records")
    public ResponseDTO<List<DeviceModelSyncVO>> getSyncRecords(
        @PathVariable Long modelId,
        @RequestParam(required = false) String deviceId
    );
}
```

### 2.3 MinIO存储设计

```
存储桶（Bucket）: ioedream-ai-models

目录结构:
ioedream-ai-models/
├── face-detection/
│   ├── v1.0.0/
│   │   └── face_detection_v1.0.0.onnx
│   ├── v1.1.0/
│   │   └── face_detection_v1.1.0.onnx
│   └── latest/
│       └── face_detection_v1.1.0.onnx (软链接)
├── fall-detection/
│   ├── v1.0.0/
│   │   └── fall_detection_v1.0.0.onnx
│   └── latest/
│       └── fall_detection_v1.0.0.onnx
└── behavior-detection/
    ├── v1.0.0/
    │   └── behavior_detection_v1.0.0.onnx
    └── latest/
        └── behavior_detection_v1.0.0.onnx
```

### 2.4 设备端更新流程

```
服务器端：
1. 上传新模型到MinIO
2. 发布模型（更新状态为"已发布"）
3. 选择目标设备
4. 创建同步任务记录
5. 通过MQTT/HTTP推送更新通知

设备端：
1. 接收更新通知
2. 拉取模型元数据
3. 下载模型文件（断点续传）
4. 校验文件MD5
5. 备份旧模型
6. 应用新模型
7. 上报更新结果

服务器端：
8. 更新同步记录状态
9. 记录设备端反馈
10. 统计更新成功率
```

---

## 3. WebSocket实时推送模块设计

### 3.1 WebSocket配置

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket端点：/ws/video
        registry.addEndpoint("/ws/video")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // 支持SockJS降级
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 消息代理：/topic（广播）、/queue（点对点）
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }
}
```

### 3.2 连接管理

```java
@Controller
public class WebSocketConnectionController {

    /**
     * 连接建立时
     * 1. 验证token
     * 2. 记录连接信息
     * 3. 发送欢迎消息
     */
    @MessageMapping("/ws/connect")
    public void handleConnect(StompHeaderAccessor accessor) {
        String userId = accessor.getUser().getName();
        log.info("[WebSocket] 用户连接: userId={}", userId);

        // 记录连接
        connectionManager.addConnection(userId, sessionId);

        // 发送欢迎消息
        messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/connected",
            "WebSocket连接成功"
        );
    }

    /**
     * 断开连接时
     * 1. 清理连接信息
     * 2. 取消订阅
     */
    @MessageMapping("/ws/disconnect")
    public void handleDisconnect(StompHeaderAccessor accessor) {
        String userId = accessor.getUser().getName();
        log.info("[WebSocket] 用户断开: userId={}", userId);

        connectionManager.removeConnection(userId, sessionId);
    }
}
```

### 3.3 事件推送服务

```java
@Service
public class EventPushService {

    /**
     * 推送设备AI事件
     */
    public void pushDeviceEvent(DeviceAIEventEntity event) {
        // 1. 构造消息
        DeviceEventMessage message = DeviceEventMessage.builder()
                .eventId(event.getEventId())
                .deviceId(event.getDeviceId())
                .eventType(event.getEventType())
                .confidence(event.getConfidence())
                .eventTime(event.getEventTime())
                .build();

        // 2. 广播到所有订阅者
        messagingTemplate.convertAndSend("/topic/device-events", message);

        log.debug("[WebSocket推送] 设备事件: eventId={}", event.getEventId());
    }

    /**
     * 推送告警事件
     */
    public void pushAlarmEvent(AlarmRecordEntity alarm) {
        // 1. 构造消息
        AlarmEventMessage message = AlarmEventMessage.builder()
                .alarmId(alarm.getAlarmId())
                .alarmLevel(alarm.getAlarmLevel())
                .alarmMessage(alarm.getAlarmMessage())
                .alarmTime(alarm.getAlarmTime())
                .build();

        // 2. 广播到所有订阅者
        messagingTemplate.convertAndSend("/topic/alarms", message);

        // 3. 推送到特定管理员（如果有分配）
        if (alarm.getHandlerId() != null) {
            messagingTemplate.convertAndSendToUser(
                String.valueOf(alarm.getHandlerId()),
                "/queue/my-alarms",
                message
            );
        }

        log.info("[WebSocket推送] 告警事件: alarmId={}, level={}",
                alarm.getAlarmId(), alarm.getAlarmLevel());
    }
}
```

### 3.4 Redis Pub/Sub集成

```java
@Component
public class RedisMessageListener {

    /**
     * 监听设备AI事件
     */
    @RedisListener(topic = "device-ai-events")
    public void onDeviceEvent(String message) {
        DeviceAIEventEntity event = JSON.parseObject(message, DeviceAIEventEntity.class);
        eventPushService.pushDeviceEvent(event);
    }

    /**
     * 监听告警事件
     */
    @RedisListener(topic = "alarm-events")
    public void onAlarmEvent(String message) {
        AlarmRecordEntity alarm = JSON.parseObject(message, AlarmRecordEntity.class);
        eventPushService.pushAlarmEvent(alarm);
    }
}
```

### 3.5 前端WebSocket集成

```javascript
// Vue 3 + SockJS + STOMP
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

export default {
    data() {
        return {
            stompClient: null,
            connected: false
        }
    },

    methods: {
        connectWebSocket() {
            // 1. 建立WebSocket连接
            const socket = new SockJS('http://localhost:8092/ws/video')
            this.stompClient = Stomp.over(socket)

            // 2. 连接认证
            const headers = {
                'Authorization': 'Bearer ' + getToken()
            }

            // 3. 建立连接
            this.stompClient.connect(headers, () => {
                this.connected = true
                console.log('[WebSocket] 连接成功')

                // 4. 订阅设备事件
                this.subscribeDeviceEvents()

                // 5. 订阅告警事件
                this.subscribeAlarms()
            })
        },

        subscribeDeviceEvents() {
            this.stompClient.subscribe('/topic/device-events', (message) => {
                const event = JSON.parse(message.body)
                console.log('[WebSocket] 收到设备事件:', event)
                this.$store.dispatch('addDeviceEvent', event)
            })
        },

        subscribeAlarms() {
            this.stompClient.subscribe('/topic/alarms', (message) => {
                const alarm = JSON.parse(message.body)
                console.log('[WebSocket] 收到告警:', alarm)
                this.$store.dispatch('addAlarm', alarm)
                this.playAlarmSound(alarm.alarmLevel)
            })
        },

        playAlarmSound(alarmLevel) {
            if (alarmLevel >= 3) {
                // 高级别告警：播放紧急音效
                this.$audio.play('emergency.mp3')
            } else {
                // 普通告警：播放提示音
                this.$audio.play('notification.mp3')
            }
        }
    }
}
```

---

## 4. 前端展示模块设计

### 4.1 页面结构

```
前端应用（Vue 3 + Ant Design Vue）
├── views/
│   ├── video/
│   │   ├── DeviceEvents.vue          # 事件展示页面
│   │   ├── AlarmManagement.vue        # 告警管理页面
│   │   └── RealtimeMonitor.vue        # 实时监控面板
│   └── ...
├── components/
│   ├── EventList.vue                 # 事件列表组件
│   ├── AlarmList.vue                 # 告警列表组件
│   ├── EventDetailModal.vue          # 事件详情弹窗
│   └── RealtimeChart.vue             # 实时图表组件
└── store/
    ├── modules/
    │   ├── deviceEvents.js           # 事件状态管理
    │   └── alarms.js                 # 告警状态管理
    └── ...
```

### 4.2 事件展示页面设计

```vue
<template>
  <div class="device-events-page">
    <!-- 筛选器 -->
    <a-card title="筛选条件" class="filter-card">
      <a-form layout="inline">
        <a-form-item label="设备">
          <a-select v-model="filter.deviceId" style="width: 200px">
            <a-select-option value="">全部设备</a-select-option>
            <a-select-option v-for="device in devices"
                            :key="device.deviceId"
                            :value="device.deviceId">
              {{ device.deviceName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="事件类型">
          <a-select v-model="filter.eventType" style="width: 150px">
            <a-select-option value="">全部类型</a-select-option>
            <a-select-option value="FALL_DETECTION">跌倒检测</a-select-option>
            <a-select-option value="LOITERING_DETECTION">徘徊检测</a-select-option>
            <a-select-option value="GATHERING_DETECTION">聚集检测</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="时间范围">
          <a-range-picker v-model="filter.timeRange"
                         show-time
                         format="YYYY-MM-DD HH:mm:ss" />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" @click="handleSearch">
            查询
          </a-button>
          <a-button @click="handleReset">重置</a-button>
          <a-button @click="handleExport">导出Excel</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 事件列表 -->
    <a-card title="设备AI事件" class="events-card">
      <a-table
        :columns="columns"
        :data-source="events"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        :scroll="{ x: 1500 }">

        <!-- 事件类型列 -->
        <template #eventType="{ record }">
          <a-tag :color="getEventTypeColor(record.eventType)">
            {{ getEventTypeName(record.eventType) }}
          </a-tag>
        </template>

        <!-- 置信度列 -->
        <template #confidence="{ record }">
          <a-progress
            :percent="record.confidence * 100"
            :stroke-color="getConfidenceColor(record.confidence)"
            size="small" />
        </template>

        <!-- 抓拍图片列 -->
        <template #snapshot="{ record }">
          <a-image
            v-if="record.snapshotUrl"
            :src="record.snapshotUrl"
            :width="100"
            :preview="{ src: record.snapshotUrl }" />
          <span v-else>-</span>
        </template>

        <!-- 操作列 -->
        <template #action="{ record }">
          <a-button type="link" @click="handleViewDetail(record)">
            查看详情
          </a-button>
          <a-button type="link" @click="handleViewVideo(record)">
            查看视频
          </a-button>
        </template>
      </a-table>
    </a-card>

    <!-- 事件详情弹窗 -->
    <EventDetailModal
      v-model="detailVisible"
      :event="selectedEvent" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useStore } from 'vuex'
import { getDeviceEvents, exportDeviceEvents } from '@/api/video'

const store = useStore()
const events = ref([])
const loading = ref(false)
const selectedEvent = ref(null)
const detailVisible = ref(false)

// WebSocket连接
onMounted(() => {
  store.dispatch('websocket/connect')

  // 订阅实时事件
  store.commit('deviceEvents/SET_CALLBACK', (newEvent) => {
    events.value.unshift(newEvent)
  })
})

// 查询事件
const handleSearch = async () => {
  loading.value = true
  try {
    const res = await getDeviceEvents(filter.value)
    events.value = res.data
  } finally {
    loading.value = false
  }
}

// 导出Excel
const handleExport = async () => {
  const blob = await exportDeviceEvents(filter.value)
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `设备AI事件_${Date.now()}.xlsx`
  a.click()
}
</script>
```

### 4.3 告警管理页面设计

```vue
<template>
  <div class="alarm-management-page">
    <!-- 告警统计卡片 -->
    <a-row :gutter="16" class="stats-row">
      <a-col :span="6">
        <a-statistic
          title="待处理告警"
          :value="stats.pending"
          :value-style="{ color: '#ff4d4f' }" />
      </a-col>
      <a-col :span="6">
        <a-statistic
          title="今日告警总数"
          :value="stats.todayTotal" />
      </a-col>
      <a-col :span="6">
        <a-statistic
          title="本周告警总数"
          :value="stats.weekTotal" />
      </a-col>
      <a-col :span="6">
        <a-statistic
          title="处理完成率"
          :value="stats.completionRate"
          suffix="%" />
      </a-col>
    </a-row>

    <!-- 告警列表 -->
    <a-card title="告警列表" class="alarms-card">
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="0" tab="待处理">
          <AlarmList
            :alarms="pendingAlarms"
            @handle="handleAlarm" />
        </a-tab-pane>

        <a-tab-pane key="1" tab="处理中">
          <AlarmList
            :alarms="processingAlarms"
            @handle="handleAlarm" />
        </a-tab-pane>

        <a-tab-pane key="2" tab="已处理">
          <AlarmList
            :alarms="completedAlarms"
            :view-only="true" />
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 处理告警弹窗 -->
    <AlarmHandleModal
      v-model="handleVisible"
      :alarm="selectedAlarm"
      @confirm="handleAlarmConfirm" />
  </div>
</template>
```

---

## 5. 测试体系设计

### 5.1 单元测试

#### 测试覆盖率目标

| 层级 | 目标覆盖率 | 说明 |
|------|-----------|------|
| Manager层 | > 80% | 核心业务逻辑 |
| Service层 | > 75% | 服务接口 |
| Controller层 | > 60% | API接口 |

#### 示例：AiModelManager测试

```java
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AiModelManagerTest {

    @Autowired
    private AiModelManager aiModelManager;

    @MockBean
    private MinIOClient minioClient;

    @MockBean
    private AiModelDao aiModelDao;

    @Test
    void uploadModel_shouldSaveModel() {
        // Given
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("model_v1.0.0.onnx");
        when(file.getBytes()).thenReturn(new byte[1024]);

        AiModelUploadForm form = new AiModelUploadForm();
        form.setModelName("Face Detection");
        form.setModelVersion("1.0.0");
        form.setModelType("FACE_DETECTION");

        // When
        AiModelEntity result = aiModelManager.uploadModel(file, form);

        // Then
        assertNotNull(result);
        assertEquals("Face Detection", result.getModelName());
        assertEquals("1.0.0", result.getModelVersion());
        assertEquals("FACE_DETECTION", result.getModelType());
        verify(minioClient).uploadFile(any(), any());
        verify(aiModelDao).insert(any());
    }
}
```

### 5.2 集成测试

#### TestContainers配置

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class DeviceAIEventIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static RedisContainer<?> redis = new RedisContainer<>("redis:7-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void receiveDeviceEvent_shouldCreateEvent() {
        // Given
        DeviceAIEventForm form = new DeviceAIEventForm();
        form.setDeviceId("CAM001");
        form.setEventType("FALL_DETECTION");
        form.setConfidence(new BigDecimal("0.95"));
        form.setEventTime(LocalDateTime.now());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DeviceAIEventForm> request = new HttpEntity<>(form, headers);

        // When
        ResponseEntity<ResponseDTO> response = restTemplate.postForEntity(
            "/api/v1/video/device/ai/event",
            request,
            ResponseDTO.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getData());
    }
}
```

### 5.3 性能测试

#### JMeter测试计划

```
测试场景1：并发设备事件接收
├─ 线程数：100（模拟100个设备）
├─ 循环次数：100
├─ 目标吞吐量：> 1000 事件/秒
└─ 响应时间 P95：< 100ms

测试场景2：WebSocket并发连接
├─ 线程数：500（模拟500个前端连接）
├─ 持续时间：10分钟
├─ 目标成功率：> 99%
└─ 消息延迟：< 500ms

测试场景3：告警规则匹配性能
├─ 规则数量：100
├─ 事件数量：10000
├─ 目标处理时间：< 50ms/事件
└─ CPU使用率：< 60%
```

---

## 6. 部署设计

### 6.1 配置管理

```yaml
# application-prod.yml
spring:
  # MinIO配置
  servlet:
    multipart:
      max-file-size: 500MB
      max-request-size: 500MB

minio:
  endpoint: http://minio:9000
  access-key: ${MINIO_ACCESS_KEY}
  secret-key: ${MINIO_SECRET_KEY}
  bucket: ioedream-ai-models

# WebSocket配置
websocket:
  enabled: true
  endpoint: /ws/video
  allowed-origins: ${ALLOWED_ORIGINS}
  heartbeat-interval: 30000  # 30秒心跳

# Redis配置（Pub/Sub）
spring:
  redis:
    host: ${REDIS_HOST}
    port: 6379
    pub-sub:
      device-events-topic: device-ai-events
      alarm-events-topic: alarm-events
```

### 6.2 部署流程

```
1. 构建阶段
   ├─ 后端编译：mvn clean package -DskipTests
   ├─ 前端编译：npm run build:prod
   └─ Docker镜像：docker build -t video-service:p1 .

2. 部署阶段
   ├─ 数据库迁移：Flyway migrate
   ├─ 服务部署：kubectl apply -f video-service.yaml
   ├─ MinIO配置：创建bucket、设置权限
   └─ Redis配置：配置Pub/Sub topic

3. 验证阶段
   ├─ 健康检查：curl http://video-service/actuator/health
   ├─ API测试：Postman测试套件
   ├─ WebSocket测试：wscat连接测试
   └─ 性能测试：JMeter压测
```

---

## 7. 监控设计

### 7.1 应用监控

```java
@Component
public class WebSocketMetrics {

    private final MeterRegistry meterRegistry;

    /**
     * 记录WebSocket连接数
     */
    public void recordConnection(int connectionCount) {
        Gauge.builder("websocket.connections", connectionCount)
                .description("当前WebSocket连接数")
                .register(meterRegistry);
    }

    /**
     * 记录消息推送量
     */
    public void recordMessagePush(String messageType, int count) {
        Counter.builder("websocket.messages.push")
                .tag("type", messageType)
                .description("WebSocket消息推送量")
                .register(meterRegistry)
                .increment(count);
    }
}
```

### 7.2 业务监控

```
关键指标：
1. AI模型更新成功率
   - 仪表盘：更新成功率趋势图
   - 告警：成功率 < 95%

2. WebSocket消息推送延迟
   - 仪表盘：P50/P95/P99延迟
   - 告警：P95 > 500ms

3. 前端页面加载时间
   - 仪表盘：平均加载时间
   - 告警：P95 > 2秒

4. 系统资源使用率
   - 仪表盘：CPU、内存、网络
   - 告警：CPU > 80% 或 内存 > 80%
```

---

## 8. 技术选型理由

### 8.1 WebSocket vs SSE

| 特性 | WebSocket | SSE |
|------|-----------|-----|
| 双向通信 | ✅ 支持 | ❌ 仅服务端推送 |
| 浏览器兼容性 | ✅ 全面 | ⚠️ IE不支持 |
| 连接开销 | 中 | 低 |
| 复杂度 | 高 | 低 |

**决策**：选择 **WebSocket**，因为：
- 需要双向通信（前端可以发送心跳）
- 更好的浏览器兼容性
- Spring对WebSocket支持完善

### 8.2 MinIO vs 本地存储

| 特性 | MinIO | 本地存储 |
|------|-------|----------|
| 分布式 | ✅ 支持 | ❌ 单机 |
| 扩展性 | ✅ 水平扩展 | ❌ 受限于单机磁盘 |
| 高可用 | ✅ 多副本 | ❌ 单点故障 |
| 运维成本 | 中 | 低 |

**决策**：选择 **MinIO**，因为：
- AI模型文件较大（100-500MB）
- 需要支持多节点部署
- 未来需要支持CDN加速

---

**设计人**: IOE-DREAM 架构委员会
**创建日期**: 2025-01-30
**版本**: 1.0.0
