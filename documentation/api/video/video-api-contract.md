# 视频模块API接口契约文档

**生成时间**: 2025-01-30  
**文档版本**: v1.1.0 - 边缘AI计算模式  
**模块**: 视频监控模块 (ioedream-video-service)  
**设备交互模式**: Mode 5 - 边缘AI计算  
**状态**: ⚠️ **待实现**

---

## 📋 文档说明

本文档定义了视频模块的所有API接口契约，包括：
- 移动端API接口（待实现）
- PC端API接口（待实现）
- 设备管理API接口
- 视频播放API接口
- 录像回放API接口
- AI分析API接口

**API基础路径**: `/api/v1/video` 或 `/api/v1/mobile/video`

> ⚠️ **注意**: 视频模块Controller待创建，以下接口为规划接口

---

## ⭐ 设备交互模式说明

### Mode 5: 边缘AI计算模式

**核心理念**: 设备端AI分析，服务器端管理

**交互流程**:
```
【模板下发】软件 → 设备
  ├─ 重点人员底库（黑名单/VIP/员工）
  ├─ AI模型更新（定期推送新版本）
  └─ 告警规则配置（区域入侵/徘徊检测）

【实时分析】设备端AI处理
  视频采集 → AI芯片分析 → 人脸检测+识别
            ↓
  行为分析 → 异常检测（徘徊/聚集/越界）
            ↓
  结构化数据 → 上传服务器

【服务器处理】软件端
  接收结构化数据 → 存储（人脸抓拍/行为事件）
  告警规则匹配 → 实时推送告警
  人脸检索 → 以图搜图/轨迹追踪
  视频联动 → 告警时调取原始视频

【原始视频】设备端存储
  ⚠️ 原始视频不上传，设备端录像7-30天
  ⚠️ 只有告警/案件时，才回调原始视频
```

**API接口说明**:
- ✅ 设备端通过HTTP/MQTT上传结构化数据（人脸抓拍、行为事件）
- ✅ 服务器端接收结构化数据并匹配告警规则
- ✅ 告警时支持回调原始视频
- ✅ 支持AI模型远程更新

**详细文档**: 参考 [视频业务模块文档](../../业务模块/05-视频管理模块/00-视频微服务总体设计文档.md#-边缘ai计算模式mode-5)

---

## 📱 一、移动端API接口（规划）

### 1.1 设备管理接口

**基础路径**: `/api/v1/mobile/video/device`

#### 1.1.1 获取设备列表

**接口**: `GET /api/v1/mobile/video/device/list`

**功能**: 获取视频设备列表

**请求参数**:
- `areaId`: string - 区域ID（可选）
- `deviceType`: string - 设备类型（可选）
- `status`: number - 设备状态（可选）

**响应数据**:
```typescript
interface VideoDeviceVO {
  deviceId: number;          // 设备ID
  deviceName: string;        // 设备名称
  deviceType: string;        // 设备类型
  areaId: string;           // 区域ID
  areaName: string;         // 区域名称
  status: number;           // 设备状态（1-在线 2-离线 3-故障）
  streamUrl?: string;        // 视频流地址
  thumbnailUrl?: string;     // 缩略图URL
  location?: string;        // 设备位置
}
```

---

#### 1.1.2 获取设备详情

**接口**: `GET /api/v1/mobile/video/device/{deviceId}`

**功能**: 获取指定视频设备详情

**路径参数**:
- `deviceId`: number - 设备ID

**响应数据**: `VideoDeviceVO`

---

### 1.2 视频播放接口

**基础路径**: `/api/v1/mobile/video/play`

#### 1.2.1 获取视频流地址

**接口**: `POST /api/v1/mobile/video/play/stream`

**功能**: 获取视频流播放地址

**请求参数**:
```typescript
interface VideoStreamRequest {
  deviceId: number;          // 设备ID
  channelId?: number;        // 通道ID（可选）
  streamType?: string;       // 流类型（MAIN/SUB，可选）
  protocol?: string;         // 协议类型（RTSP/RTMP/HLS，可选）
}
```

**响应数据**:
```typescript
interface VideoStreamResult {
  streamUrl: string;         // 视频流地址
  streamType: string;        // 流类型
  protocol: string;          // 协议类型
  expireTime?: string;       // 过期时间（ISO格式）
  token?: string;            // 访问令牌（可选）
}
```

---

#### 1.2.2 获取视频截图

**接口**: `GET /api/v1/mobile/video/device/{deviceId}/snapshot`

**功能**: 获取视频设备截图

**路径参数**:
- `deviceId`: number - 设备ID

**请求参数**:
- `channelId`: number - 通道ID（可选）

**响应数据**: 图片二进制数据或Base64编码

---

### 1.3 录像回放接口

**基础路径**: `/api/v1/mobile/video/playback`

#### 1.3.1 查询录像列表

**接口**: `POST /api/v1/mobile/video/playback/query`

**功能**: 查询录像列表

**请求参数**:
```typescript
interface PlaybackQueryRequest {
  deviceId: number;          // 设备ID
  channelId?: number;        // 通道ID（可选）
  startTime: string;         // 开始时间（ISO格式）
  endTime: string;          // 结束时间（ISO格式）
  recordType?: string;       // 录像类型（TIMED/ALARM/MANUAL，可选）
}
```

**响应数据**:
```typescript
interface PlaybackRecordVO {
  recordId: number;          // 录像ID
  deviceId: number;          // 设备ID
  channelId: number;         // 通道ID
  startTime: string;         // 开始时间
  endTime: string;          // 结束时间
  duration: number;         // 时长（秒）
  recordType: string;        // 录像类型
  fileSize: number;         // 文件大小（字节）
  fileUrl?: string;         // 文件URL（可选）
}
```

---

#### 1.3.2 获取录像播放地址

**接口**: `POST /api/v1/mobile/video/playback/stream`

**功能**: 获取录像播放地址

**请求参数**:
```typescript
interface PlaybackStreamRequest {
  recordId: number;          // 录像ID
  startTime: string;         // 开始时间（ISO格式）
  endTime: string;          // 结束时间（ISO格式）
  protocol?: string;         // 协议类型（可选）
}
```

**响应数据**: `VideoStreamResult`

---

### 1.4 PTZ控制接口

**基础路径**: `/api/v1/mobile/video/ptz`

#### 1.4.1 PTZ控制

**接口**: `POST /api/v1/mobile/video/ptz/control`

**功能**: 控制PTZ云台

**请求参数**:
```typescript
interface PTZControlRequest {
  deviceId: number;          // 设备ID
  channelId?: number;        // 通道ID（可选）
  action: string;           // 控制动作（UP/DOWN/LEFT/RIGHT/ZOOM_IN/ZOOM_OUT/FOCUS_NEAR/FOCUS_FAR）
  speed?: number;           // 速度（1-8，可选）
  duration?: number;        // 持续时间（毫秒，可选）
}
```

**响应数据**: `ResponseDTO<Void>`

---

#### 1.4.2 获取PTZ预设位

**接口**: `GET /api/v1/mobile/video/ptz/presets/{deviceId}`

**功能**: 获取PTZ预设位列表

**路径参数**:
- `deviceId`: number - 设备ID

**响应数据**:
```typescript
interface PTZPresetVO {
  presetId: number;         // 预设位ID
  presetName: string;       // 预设位名称
  presetNo: number;         // 预设位编号
  thumbnailUrl?: string;     // 缩略图URL（可选）
}
```

---

#### 1.4.3 调用PTZ预设位

**接口**: `POST /api/v1/mobile/video/ptz/preset/call`

**功能**: 调用PTZ预设位

**请求参数**:
```typescript
interface PTZPresetCallRequest {
  deviceId: number;          // 设备ID
  presetId: number;         // 预设位ID
  channelId?: number;        // 通道ID（可选）
}
```

**响应数据**: `ResponseDTO<Void>`

---

### 1.5 AI分析接口

**基础路径**: `/api/v1/mobile/video/ai`

#### 1.5.1 获取AI分析结果

**接口**: `GET /api/v1/mobile/video/ai/analysis/{deviceId}`

**功能**: 获取设备AI分析结果

**路径参数**:
- `deviceId`: number - 设备ID

**请求参数**:
- `analysisType`: string - 分析类型（FACE/BEHAVIOR/CROWD/VEHICLE，可选）
- `startTime`: string - 开始时间（ISO格式，可选）
- `endTime`: string - 结束时间（ISO格式，可选）

**响应数据**:
```typescript
interface AIAnalysisResultVO {
  deviceId: number;          // 设备ID
  analysisType: string;      // 分析类型
  resultCount: number;       // 结果数量
  results: AIAnalysisItem[]; // 分析结果列表
}

interface AIAnalysisItem {
  resultId: number;         // 结果ID
  analysisType: string;     // 分析类型
  confidence: number;       // 置信度（0-1）
  timestamp: string;        // 时间戳
  imageUrl?: string;        // 图片URL
  details?: object;         // 详细信息
}
```

---

## 💻 二、PC端API接口（规划）

### 2.1 设备管理接口

**基础路径**: `/api/v1/video/device`

#### 2.1.1 设备查询

**接口**: `POST /api/v1/video/device/query`

**功能**: 分页查询视频设备

**请求参数**:
```typescript
interface VideoDeviceQueryForm {
  deviceName?: string;       // 设备名称（可选）
  deviceType?: string;       // 设备类型（可选）
  areaId?: string;          // 区域ID（可选）
  status?: number;          // 设备状态（可选）
  pageNum: number;         // 页码
  pageSize: number;        // 每页大小
}
```

**响应数据**: `PageResult<VideoDeviceVO>`

---

#### 2.1.2 设备管理

**接口列表**:
- `GET /api/v1/video/device/{id}` - 查询设备详情
- `POST /api/v1/video/device/add` - 添加设备
- `PUT /api/v1/video/device/update` - 更新设备
- `DELETE /api/v1/video/device/{id}` - 删除设备
- `POST /api/v1/video/device/status/update` - 更新设备状态
- `POST /api/v1/video/device/test/connection` - 测试设备连接

---

### 2.2 视频监控接口

**基础路径**: `/api/v1/video/monitor`

#### 2.2.1 多画面监控

**接口**: `POST /api/v1/video/monitor/multi-view`

**功能**: 获取多画面监控视频流

**请求参数**:
```typescript
interface MultiViewRequest {
  deviceIds: number[];       // 设备ID列表
  layout: string;           // 布局类型（1x1/2x2/3x3/4x4）
  streamType?: string;      // 流类型（可选）
}
```

**响应数据**:
```typescript
interface MultiViewResult {
  layout: string;           // 布局类型
  streams: VideoStreamResult[]; // 视频流列表
}
```

---

#### 2.2.2 电视墙控制

**接口**: `POST /api/v1/video/monitor/tv-wall/control`

**功能**: 控制电视墙显示

**请求参数**:
```typescript
interface TVWallControlRequest {
  wallId: number;           // 电视墙ID
  layout: string;           // 布局类型
  deviceIds: number[];      // 设备ID列表
  action?: string;          // 控制动作（可选）
}
```

**响应数据**: `ResponseDTO<Void>`

---

### 2.3 录像管理接口

**基础路径**: `/api/v1/video/record`

#### 2.3.1 录像查询

**接口**: `POST /api/v1/video/record/query`

**功能**: 分页查询录像记录

**请求参数**:
```typescript
interface VideoRecordQueryForm {
  deviceId?: number;        // 设备ID（可选）
  channelId?: number;       // 通道ID（可选）
  startTime?: string;       // 开始时间（ISO格式）
  endTime?: string;        // 结束时间（ISO格式）
  recordType?: string;      // 录像类型（可选）
  pageNum: number;         // 页码
  pageSize: number;        // 每页大小
}
```

**响应数据**: `PageResult<PlaybackRecordVO>`

---

#### 2.3.2 录像下载

**接口**: `POST /api/v1/video/record/download`

**功能**: 下载录像文件

**请求参数**:
```typescript
interface VideoRecordDownloadRequest {
  recordIds: number[];      // 录像ID列表
  format?: string;         // 下载格式（MP4/AVI，可选）
}
```

**响应数据**: 下载文件或下载链接

---

### 2.4 AI分析接口

**基础路径**: `/api/v1/video/ai`

#### 2.4.1 AI分析查询

**接口**: `POST /api/v1/video/ai/analysis/query`

**功能**: 查询AI分析结果

**请求参数**:
```typescript
interface AIAnalysisQueryForm {
  deviceId?: number;        // 设备ID（可选）
  analysisType?: string;    // 分析类型（可选）
  startTime?: string;       // 开始时间（ISO格式）
  endTime?: string;        // 结束时间（ISO格式）
  confidence?: number;     // 置信度阈值（可选）
  pageNum: number;         // 页码
  pageSize: number;        // 每页大小
}
```

**响应数据**: `PageResult<AIAnalysisItem>`

---

#### 2.4.2 AI分析统计

**接口**: `POST /api/v1/video/ai/statistics`

**功能**: 获取AI分析统计数据

**请求参数**:
```typescript
interface AIAnalysisStatisticsRequest {
  deviceIds?: number[];     // 设备ID列表（可选）
  analysisType?: string;    // 分析类型（可选）
  startTime: string;        // 开始时间（ISO格式）
  endTime: string;         // 结束时间（ISO格式）
  groupBy?: string;        // 分组方式（HOUR/DAY/MONTH，可选）
}
```

**响应数据**:
```typescript
interface AIAnalysisStatisticsVO {
  totalCount: number;       // 总数量
  statistics: AIStatisticsItem[]; // 统计数据列表
}

interface AIStatisticsItem {
  time: string;            // 时间
  count: number;          // 数量
  analysisType: string;    // 分析类型
}
```

---

## 📋 三、API接口规范

### 3.1 统一响应格式

所有API接口统一使用`ResponseDTO<T>`格式：

```typescript
interface ResponseDTO<T> {
  code: number;        // 业务状态码（200表示成功）
  message: string;     // 提示信息
  data: T;            // 响应数据
  timestamp: number;   // 时间戳
}
```

### 3.2 错误码规范

| 错误码范围 | 类型 | 说明 |
|-----------|------|------|
| 200 | 成功 | 操作成功 |
| 400-499 | 客户端错误 | 参数错误、未授权、禁止访问 |
| 500-599 | 服务端错误 | 服务器内部错误 |
| 8000-8999 | 视频模块错误 | 视频相关业务错误 |

### 3.3 认证授权

- **移动端接口**: 使用`@SaCheckLogin`注解，需要登录认证
- **PC端接口**: 使用`@PreAuthorize`注解，需要角色权限验证

### 3.4 参数验证

- 所有POST/PUT请求使用`@Valid`注解进行参数验证
- 使用Jakarta Validation注解（`@NotNull`, `@NotBlank`, `@Size`等）

---

## 📋 四、前端API接口文件

### 4.1 移动端API文件

**文件路径**: `smart-app/src/api/business/video/video-api.js`

**当前状态**: ⚠️ 需要创建和完善

**待实现接口**:
- ⚠️ 设备管理接口
- ⚠️ 视频播放接口
- ⚠️ 录像回放接口
- ⚠️ PTZ控制接口
- ⚠️ AI分析接口

### 4.2 PC端API文件

**文件路径**: `smart-admin-web-javascript/src/api/business/smart-video/*-api.js`

**当前状态**: ⚠️ 需要检查和完善

**待补充接口**:
- ⚠️ 设备管理接口
- ⚠️ 视频监控接口
- ⚠️ 录像管理接口
- ⚠️ AI分析接口

---

## 🎯 五、下一步行动

### 5.1 立即执行

1. 📋 创建视频模块Controller（移动端和PC端）
2. 📋 创建移动端API接口文件
3. 📋 完善PC端API接口文件

### 5.2 本周完成

1. 📋 创建完整的API接口契约文档索引
2. 📋 检查前端和移动端API接口文件完整性
3. 📋 补充缺失的API接口实现

---

**文档生成**: IOE-DREAM 架构委员会  
**审核状态**: 待审核  
**下一步行动**: 创建API接口契约文档索引

