# AI模型管理能力规格

**能力ID**: ai-model-management
**优先级**: P0
**创建日期**: 2025-01-30
**状态**: 提案中

---

## ADDED Requirements

### REQ-AI-MODEL-001: 模型上传和版本管理

**优先级**: P0
**需求描述**: 系统必须支持AI模型文件的上传、版本管理和元数据记录。

**用户故事**: 作为系统管理员，我希望能够上传AI模型文件到服务器，并自动记录版本信息，以便统一管理所有模型版本。

**场景**:

#### Scenario: 上传新模型

**Given** 用户已登录并有管理员权限
**When** 用户上传AI模型文件并填写模型信息
**Then** 系统应该：
- 验证文件格式（支持.onnx, .pb, .tflite）
- 验证文件大小（≤ 500MB）
- 计算文件MD5
- 上传文件到MinIO存储
- 保存模型元数据到数据库
- 返回模型ID

**验证标准**:
```java
// API测试
POST /api/v1/video/ai-model/upload
Content-Type: multipart/form-data

// 请求
{
  "file": <model_file>,
  "modelName": "Face Detection v2",
  "modelVersion": "2.0.0",
  "modelType": "FACE_DETECTION",
  "supportedEvents": ["FACE_DETECTION"]
}

// 响应
{
  "code": 200,
  "message": "success",
  "data": 123456  // modelId
}

// 数据库验证
SELECT * FROM t_video_ai_model WHERE model_id = 123456
- model_name = "Face Detection v2"
- model_version = "2.0.0"
- model_type = "FACE_DETECTION"
- file_size = <actual size>
- file_md5 = <calculated md5>
- model_status = 0  // 草稿
```

#### Scenario: 查询模型列表

**Given** 数据库中有多个模型版本
**When** 管理员查询模型列表
**Then** 系统应该：
- 返回所有模型（支持分页）
- 按版本号降序排列
- 显示模型状态（草稿、发布、废弃）
- 显示发布时间和发布人

**API**:
```
GET /api/v1/video/ai-model/list?page=1&pageSize=20&modelType=FACE_DETECTION

Response:
{
  "code": 200,
  "data": {
    "list": [
      {
        "modelId": 123456,
        "modelName": "Face Detection v2",
        "modelVersion": "2.0.0",
        "modelType": "FACE_DETECTION",
        "modelStatus": 1,  // 已发布
        "publishTime": "2025-01-30 10:00:00",
        "fileSize": 104857600  // 100MB
      }
    ],
    "total": 15
  }
}
```

---

### REQ-AI-MODEL-002: 模型发布

**优先级**: P0
**需求描述**: 系统必须支持模型发布流程，只有已发布的模型才能同步到设备。

**用户故事**: 作为系统管理员，我希望能够发布AI模型，标记为可用状态，以便设备能够下载和使用。

**场景**:

#### Scenario: 发布模型

**Given** 模型处于草稿状态
**When** 管理员发布模型
**Then** 系统应该：
- 验证模型状态（必须是草稿）
- 更新模型状态为"已发布"
- 记录发布时间和发布人
- 创建模型发布记录
- 通知订阅的设备（可选）

**API**:
```
POST /api/v1/video/ai-model/{modelId}/publish

Response:
{
  "code": 200,
  "message": "模型发布成功"
}

// 数据库验证
SELECT * FROM t_video_ai_model WHERE model_id = 123456
- model_status = 1  // 已发布
- publish_time = <current time>
- published_by = <user_id>
```

#### Scenario: 防止重复发布

**Given** 模型已发布
**When** 管理员再次尝试发布
**Then** 系统应该：
- 返回错误提示
- 不更新模型状态

**API**:
```
POST /api/v1/video/ai-model/{modelId}/publish

Response:
{
  "code": "MODEL_ALREADY_PUBLISHED",
  "message": "模型已发布，不能重复发布"
}
```

---

### REQ-AI-MODEL-003: 设备模型同步

**优先级**: P0
**需求描述**: 系统必须支持将AI模型远程同步到设备，并跟踪同步进度和结果。

**用户故事**: 作为运维人员，我希望能够选择一批设备，远程更新它们的AI模型，并查看同步进度，以便批量管理设备。

**场景**:

#### Scenario: 同步模型到单个设备

**Given** 模型已发布
**And** 设备在线
**When** 管理员同步模型到设备
**Then** 系统应该：
- 创建同步任务记录
- 发送模型更新通知到设备（MQTT/HTTP）
- 跟踪同步进度
- 记录同步结果

**API**:
```
POST /api/v1/video/ai-model/{modelId}/sync
Content-Type: application/json

{
  "deviceIds": ["CAM001", "CAM002", "CAM003"]
}

Response:
{
  "code": 200,
  "data": {
    "syncTaskId": "sync_20250130_001",
    "totalDevices": 3,
    "pendingDevices": 3
  }
}

// 数据库验证
SELECT * FROM t_video_device_model_sync WHERE model_id = 123456
- 3条记录，每条对应一个设备
- sync_status = 0  // 待同步
```

#### Scenario: 批量同步进度跟踪

**Given** 同步任务正在执行
**When** 管理员查询同步进度
**Then** 系统应该：
- 返回总设备数
- 返回各状态设备数（待同步、同步中、成功、失败）
- 返回同步进度百分比

**API**:
```
GET /api/v1/video/ai-model/{modelId}/sync-records

Response:
{
  "code": 200,
  "data": {
    "totalDevices": 10,
    "pendingDevices": 2,
    "syncingDevices": 3,
    "successDevices": 4,
    "failedDevices": 1,
    "progress": 50  // (4+1) / 10 * 100
  }
}
```

#### Scenario: 设备同步失败处理

**Given** 设备同步失败
**When** 系统接收设备失败报告
**Then** 系统应该：
- 更新同步状态为"失败"
- 记录错误信息
- 允许管理员重试同步

**错误日志**:
```
[AI模型同步] 同步失败: modelId=123456, deviceId=CAM001, error=网络超时

数据库更新：
UPDATE t_video_device_model_sync
SET sync_status = 3,  // 失败
    error_message = "网络超时: 连接设备失败"
WHERE model_id = 123456 AND device_id = "CAM001"
```

---

### REQ-AI-MODEL-004: 模型文件存储

**优先级**: P0
**需求描述**: 系统必须使用MinIO存储AI模型文件，支持断点续传和文件校验。

**用户故事**: 作为系统，我需要安全地存储AI模型文件，支持断点续传，并确保文件完整性，以便设备能够可靠地下载模型。

**场景**:

#### Scenario: 上传模型到MinIO

**Given** MinIO服务可用
**When** 管理员上传模型文件
**Then** 系统应该：
- 检查bucket是否存在，不存在则创建
- 按模型类型和版本组织目录结构
- 上传文件到MinIO
- 生成文件访问URL
- 返回上传结果

**目录结构**:
```
ioedream-ai-models/
├── face-detection/
│   ├── v1.0.0/
│   │   └── face_detection_v1.0.0.onnx
│   ├── v1.1.0/
│   │   └── face_detection_v1.1.0.onnx
│   └── latest/
│       └── face_detection_v1.1.0.onnx  // 软链接到最新版本
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

#### Scenario: 文件MD5校验

**Given** 模型文件已上传
**When** 设备下载模型文件
**Then** 系统应该：
- 计算文件MD5
- 在响应头中返回MD5
- 设备验证文件完整性

**API**:
```
GET /api/v1/video/ai-model/{modelId}/download

Response Headers:
Content-Disposition: attachment; filename="face_detection_v1.1.0.onnx"
Content-MD5: d41d8cd98f00b204e9800998ecf8427e
Content-Length: 104857600

Response Body: <file_binary>
```

#### Scenario: 断点续传支持

**Given** 模型文件较大（100-500MB）
**And** 网络不稳定
**When** 上传或下载中断
**Then** 系统应该：
- 支持分块上传
- 支持断点续传
- 记录已上传/下载的块
- 自动恢复传输

**API（分块上传）**:
```
POST /api/v1/video/ai-model/upload-chunk

Headers:
X-Upload-Id: upload_20250130_001
X-Chunk-Index: 0
X-Total-Chunks: 10
X-File-MD5: d41d8cd98f00b204e9800998ecf8427e

Body: <chunk_data>

Response:
{
  "code": 200,
  "message": "块上传成功",
  "data": {
    "uploadedChunks": [0],
    "remainingChunks": 9
  }
}
```

---

## MODIFIED Requirements

*（本能力为新增，无修改的需求）*

---

## REMOVED Requirements

*（本能力为新增，无删除的需求）*

---

## 附录

### A. 数据模型

**AiModelEntity**:
```java
@TableName("t_video_ai_model")
public class AiModelEntity {
    private Long modelId;
    private String modelName;
    private String modelVersion;
    private String modelType;
    private String filePath;
    private Long fileSize;
    private String fileMd5;
    private Integer modelStatus;  // 0:草稿, 1:发布, 2:废弃
    private String supportedEvents;  // JSON数组
    private String modelMetadata;  // JSON
    private BigDecimal accuracyRate;
    private LocalDateTime publishTime;
    private Long publishedBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deletedFlag;
}
```

**DeviceModelSyncEntity**:
```java
@TableName("t_video_device_model_sync")
public class DeviceModelSyncEntity {
    private Long syncId;
    private Long modelId;
    private String deviceId;
    private Integer syncStatus;  // 0:待同步, 1:同步中, 2:成功, 3:失败
    private Integer syncProgress;  // 0-100
    private LocalDateTime syncStartTime;
    private LocalDateTime syncEndTime;
    private String errorMessage;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

### B. API端点列表

| 端点 | 方法 | 描述 | 权限 |
|------|------|------|------|
| /api/v1/video/ai-model/upload | POST | 上传AI模型 | ADMIN |
| /api/v1/video/ai-model/upload-chunk | POST | 分块上传 | ADMIN |
| /api/v1/video/ai-model/{modelId}/publish | POST | 发布模型 | ADMIN |
| /api/v1/video/ai-model/{modelId}/sync | POST | 同步到设备 | ADMIN |
| /api/v1/video/ai-model/list | GET | 查询模型列表 | ADMIN |
| /api/v1/video/ai-model/{modelId} | GET | 获取模型详情 | ADMIN |
| /api/v1/video/ai-model/{modelId}/download | GET | 下载模型 | ADMIN |
| /api/v1/video/ai-model/{modelId}/sync-records | GET | 获取同步记录 | ADMIN |
| /api/v1/video/ai-model/{modelId} | DELETE | 删除模型 | ADMIN |

### C. 错误码定义

| 错误码 | 说明 | HTTP状态码 |
|--------|------|------------|
| MODEL_FILE_TOO_LARGE | 模型文件超过500MB | 400 |
| MODEL_INVALID_FORMAT | 模型文件格式不支持 | 400 |
| MODEL_VERSION_EXISTS | 模型版本已存在 | 409 |
| MODEL_NOT_PUBLISHED | 模型未发布 | 400 |
| MODEL_ALREADY_PUBLISHED | 模型已发布 | 400 |
| DEVICE_OFFLINE | 设备离线 | 400 |
| SYNC_FAILED | 同步失败 | 500 |
| MINIO_CONNECTION_ERROR | MinIO连接失败 | 500 |

---

**规格编写人**: IOE-DREAM 架构委员会
**创建日期**: 2025-01-30
**版本**: 1.0.0
