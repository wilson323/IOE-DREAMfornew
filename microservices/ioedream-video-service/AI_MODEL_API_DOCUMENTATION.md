# AI模型管理API文档

## 概述

AI模型管理模块提供AI模型的上传、发布、同步到设备等功能，支持边缘AI计算场景。

## 基础信息

- **服务名称**: ioedream-video-service
- **服务端口**: 8092
- **API版本**: v1
- **基础路径**: `/api/v1/video/ai-model`

## API接口列表

### 1. 上传AI模型

**接口描述**: 上传AI模型文件到MinIO存储

**请求方式**: `POST`

**请求路径**: `/api/v1/video/ai-model/upload`

**请求参数**:
- **Content-Type**: `multipart/form-data`

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 模型文件（最大500MB，支持.onnx等格式） |
| modelName | String | 是 | 模型名称（字母、数字、下划线、中划线） |
| modelVersion | String | 是 | 模型版本（格式：x.y.z，例如：1.0.0） |
| modelType | String | 是 | 模型类型（例如：YOLOv8, RESNET, BERT等） |
| supportedEvents | String | 否 | 支持的事件类型（逗号分隔，例如：FALL,ABNORMAL_GAIT） |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 1,
  "timestamp": 1705992000000
}
```

**错误码**:
- `1001`: 模型文件不能为空
- `1002`: 模型名称不能为空
- `1003`: 模型版本格式不正确
- `1004`: 模型文件大小超过限制（最大500MB）
- `1005`: 该模型版本已存在

---

### 2. 发布AI模型

**接口描述**: 将草稿状态的AI模型发布为可用状态

**请求方式**: `POST`

**请求路径**: `/api/v1/video/ai-model/{modelId}/publish`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| modelId | Long | 是 | 模型ID |

**请求头**:
- `X-User-Id`: 当前用户ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1705992000000
}
```

**错误码**:
- `1006`: 模型不存在
- `1007`: 只有草稿状态的模型才能发布

---

### 3. 同步模型到设备

**接口描述**: 将已发布的AI模型同步到指定的边缘设备

**请求方式**: `POST`

**请求路径**: `/api/v1/video/ai-model/{modelId}/sync`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| modelId | Long | 是 | 模型ID |

**请求体**:
```json
{
  "modelId": 1,
  "deviceIds": ["CAM001", "CAM002", "CAM003"]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "sync_abc123",
  "timestamp": 1705992000000
}
```

**返回数据**:
- `syncTaskId`: 同步任务ID，用于查询同步进度

**错误码**:
- `1006`: 模型不存在
- `1008`: 只有已发布的模型才能同步到设备
- `1009`: 设备ID列表不能为空

---

### 4. 查询AI模型列表

**接口描述**: 分页查询AI模型列表

**请求方式**: `GET`

**请求路径**: `/api/v1/video/ai-model/list`

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| modelType | String | 否 | 模型类型筛选 |
| modelStatus | Integer | 否 | 模型状态筛选（0-草稿，1-已发布，2-已弃用） |
| pageNum | Integer | 是 | 页码（从1开始） |
| pageSize | Integer | 是 | 每页大小 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "modelId": 1,
        "modelName": "fall_detection",
        "modelVersion": "1.0.0",
        "modelType": "YOLOv8",
        "filePath": "ai-models/YOLOv8/fall_detection/1.0.0/model.onnx",
        "fileSize": 104857600,
        "fileSizeMb": "100.00",
        "fileMd5": "abc123def456",
        "supportedEvents": "FALL,ABNORMAL_GAIT",
        "modelStatus": 1,
        "modelStatusName": "已发布",
        "accuracyRate": 0.95,
        "accuracyPercent": "95%",
        "publishTime": "2025-01-30T12:00:00",
        "publishedBy": 1001,
        "createTime": "2025-01-30T10:00:00",
        "updateTime": "2025-01-30T12:00:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 1
  },
  "timestamp": 1705992000000
}
```

---

### 5. 获取AI模型详情

**接口描述**: 获取指定AI模型的详细信息

**请求方式**: `GET`

**请求路径**: `/api/v1/video/ai-model/{modelId}`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| modelId | Long | 是 | 模型ID |

**响应示例**: 同"查询AI模型列表"中的单条数据格式

**错误码**:
- `1006`: 模型不存在

---

### 6. 查询同步进度

**接口描述**: 查询AI模型到设备的同步进度

**请求方式**: `GET`

**请求路径**: `/api/v1/video/ai-model/{modelId}/sync-progress`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| modelId | Long | 是 | 模型ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalDevices": 10,
    "pendingDevices": 2,
    "syncingDevices": 3,
    "successDevices": 4,
    "failedDevices": 1,
    "progress": 50,
    "progressDesc": "50%"
  },
  "timestamp": 1705992000000
}
```

---

### 7. 删除AI模型

**接口描述**: 删除草稿状态的AI模型

**请求方式**: `DELETE`

**请求路径**: `/api/v1/video/ai-model/{modelId}`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| modelId | Long | 是 | 模型ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "timestamp": 1705992000000
}
```

**错误码**:
- `1006`: 模型不存在
- `1010`: 只有草稿状态的模型才能删除

---

## 数据模型

### AiModelEntity (AI模型实体)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| modelId | Long | 模型ID（主键） |
| modelName | String | 模型名称 |
| modelVersion | String | 模型版本（格式：x.y.z） |
| modelType | String | 模型类型 |
| filePath | String | MinIO存储路径 |
| fileSize | Long | 文件大小（字节） |
| fileMd5 | String | 文件MD5值 |
| supportedEvents | String | 支持的事件类型 |
| modelStatus | Integer | 模型状态（0-草稿，1-已发布，2-已弃用） |
| accuracyRate | BigDecimal | 准确率（0-1） |
| publishTime | LocalDateTime | 发布时间 |
| publishedBy | Long | 发布人ID |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |
| deletedFlag | Integer | 删除标记 |

### DeviceModelSyncEntity (设备模型同步实体)

| 字段名 | 类型 | 说明 |
|--------|------|------|
| syncId | Long | 同步ID（主键） |
| modelId | Long | 模型ID |
| deviceId | String | 设备ID |
| syncStatus | Integer | 同步状态（0-待同步，1-同步中，2-成功，3-失败） |
| syncProgress | Integer | 同步进度（0-100） |
| errorMessage | String | 错误信息 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

---

## 状态说明

### 模型状态 (modelStatus)

| 值 | 名称 | 说明 |
|----|------|------|
| 0 | 草稿 | 模型已上传但未发布，可以修改或删除 |
| 1 | 已发布 | 模型已发布，可以同步到设备使用 |
| 2 | 已弃用 | 模型已弃用，不再使用 |

### 同步状态 (syncStatus)

| 值 | 名称 | 说明 |
|----|------|------|
| 0 | 待同步 | 等待同步到设备 |
| 1 | 同步中 | 正在同步中 |
| 2 | 成功 | 同步成功 |
| 3 | 失败 | 同步失败 |

---

## 业务流程

### 1. 模型上传流程

```
用户上传模型文件
    ↓
验证文件大小（≤500MB）
    ↓
验证版本格式（x.y.z）
    ↓
检查版本唯一性
    ↓
上传到MinIO
    ↓
创建模型记录（状态：草稿）
    ↓
返回模型ID
```

### 2. 模型发布流程

```
用户发布模型
    ↓
验证模型存在
    ↓
验证状态为草稿
    ↓
更新状态为已发布
    ↓
记录发布时间和发布人
    ↓
发布成功
```

### 3. 模型同步流程

```
用户选择模型和设备
    ↓
验证模型已发布
    ↓
生成同步任务ID
    ↓
为每个设备创建同步记录
    ↓
触发设备同步（异步）
    ↓
返回同步任务ID
```

---

## 注意事项

1. **文件大小限制**: 单个模型文件最大500MB
2. **版本格式**: 必须符合 `x.y.z` 格式（例如：1.0.0）
3. **模型状态**: 只有已发布的模型才能同步到设备
4. **删除限制**: 只有草稿状态的模型才能删除
5. **同名版本**: 同一模型的同名版本不能重复上传

---

## 示例代码

### Java示例

```java
// 1. 上传模型
RestTemplate restTemplate = new RestTemplate();
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.MULTIPART_FORM_DATA);

MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
body.add("file", new FileSystemResource("fall_detection.onnx"));
body.add("modelName", "fall_detection");
body.add("modelVersion", "1.0.0");
body.add("modelType", "YOLOv8");
body.add("supportedEvents", "FALL,ABNORMAL_GAIT");

HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
ResponseEntity<Map> response = restTemplate.postForEntity(
    "http://localhost:8092/api/v1/video/ai-model/upload",
    requestEntity,
    Map.class
);

// 2. 发布模型
Long modelId = 1L;
Long userId = 1001L;
restTemplate.exchange(
    "http://localhost:8092/api/v1/video/ai-model/" + modelId + "/publish",
    HttpMethod.POST,
    new HttpEntity<>(headers),
    Void.class
);

// 3. 同步到设备
AiModelSyncForm syncForm = new AiModelSyncForm();
syncForm.setModelId(1L);
syncForm.setDeviceIds(Arrays.asList("CAM001", "CAM002"));

restTemplate.exchange(
    "http://localhost:8092/api/v1/video/ai-model/1/sync",
    HttpMethod.POST,
    new HttpEntity<>(syncForm, headers),
    String.class
);
```

---

## 更新日志

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2025-01-30 | 初始版本，支持AI模型上传、发布、同步等基础功能 |

---

**文档维护**: IOE-DREAM AI Team
**最后更新**: 2025-01-30
