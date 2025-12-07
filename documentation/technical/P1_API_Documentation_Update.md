# P1级API文档更新报告

**执行时间**: 2025-01-30  
**优先级**: P1级 (建议立即执行)  
**状态**: ✅ **文档更新完成**

---

## 📊 更新摘要

### 更新内容

✅ **Swagger文档更新**:
- ✅ 新增录像接口文档（2个接口）
- ✅ 新增预置位接口文档（2个接口）
- ✅ 验证现有接口文档完整性

✅ **前端API调用文档更新**:
- ✅ 更新设备管理API文档
- ✅ 更新PTZ控制API文档
- ✅ 更新录像API文档
- ✅ 更新预置位API文档

---

## 📋 Swagger文档更新

### 1. 录像接口文档

#### 1.1 开始录像接口

**接口路径**: `POST /api/v1/video/monitor/{deviceId}/record/start`

**Swagger注解**:
```java
@Operation(summary = "开始录制监控片段", description = "开始录制当前监控画面，返回录像ID")
@PostMapping("/{deviceId}/record/start")
public ResponseDTO<Long> startRecord(
        @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId,
        @Parameter(description = "流类型：MAIN-主码流，SUB-子码流") @RequestParam(defaultValue = "MAIN") String streamType)
```

**状态**: ✅ **已更新**

#### 1.2 停止录像接口

**接口路径**: `POST /api/v1/video/monitor/{deviceId}/record/stop`

**Swagger注解**:
```java
@Operation(summary = "停止录制监控片段", description = "停止录制当前监控画面")
@PostMapping("/{deviceId}/record/stop")
public ResponseDTO<String> stopRecord(
        @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId,
        @Parameter(description = "录像ID", required = true) @RequestParam String recordId)
```

**状态**: ✅ **已更新**

---

### 2. 预置位接口文档

#### 2.1 调用预置位接口

**接口路径**: `POST /api/v1/video/monitor/{deviceId}/preset/{presetNum}`

**Swagger注解**:
```java
@Operation(summary = "调用预置位", description = "调用设备的预置位")
@PostMapping("/{deviceId}/preset/{presetNum}")
public ResponseDTO<String> callPreset(
        @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId,
        @Parameter(description = "预置位编号", required = true) @PathVariable Integer presetNum)
```

**状态**: ✅ **已更新**

#### 2.2 获取预置位列表接口

**接口路径**: `GET /api/v1/video/monitor/{deviceId}/preset/list`

**Swagger注解**:
```java
@Operation(summary = "获取预置位列表", description = "获取设备的所有预置位列表")
@GetMapping("/{deviceId}/preset/list")
public ResponseDTO<List<Map<String, Object>>> getPresetList(
        @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId)
```

**状态**: ✅ **已更新**

---

## 📋 前端API调用文档更新

### 1. 设备管理API文档

#### 1.1 查询设备列表

**API方法**: `deviceApi.queryDeviceList(params)`

**接口路径**: `POST /api/v1/video/devices/page`

**请求参数**:
```javascript
{
  pageNum: 1,        // 页码
  pageSize: 10,      // 每页大小
  deviceName: '',    // 设备名称（可选）
  deviceType: '',    // 设备类型（可选）
  deviceStatus: '',  // 设备状态（可选）
  areaId: null       // 区域ID（可选）
}
```

**响应格式**:
```javascript
{
  code: 1,
  msg: 'success',
  data: {
    list: [],        // 设备列表
    total: 0,        // 总记录数
    pageNum: 1,      // 当前页码
    pageSize: 10     // 每页大小
  }
}
```

**状态**: ✅ **已更新**

#### 1.2 添加设备

**API方法**: `deviceApi.addDevice(params)`

**接口路径**: `POST /api/v1/video/devices`

**请求参数**:
```javascript
{
  deviceCode: '',      // 设备编码
  deviceName: '',      // 设备名称
  deviceType: '',      // 设备类型
  areaId: null,        // 区域ID
  location: '',         // 安装位置
  ip: '',               // IP地址
  port: 554,           // 端口号
  manufacturer: '',     // 制造商
  model: '',           // 型号
  firmwareVersion: '', // 固件版本
  description: ''      // 描述
}
```

**响应格式**:
```javascript
{
  code: 1,
  msg: 'success',
  data: 123  // 设备ID
}
```

**状态**: ✅ **已更新**

#### 1.3 更新设备

**API方法**: `deviceApi.updateDevice(deviceId, params)`

**接口路径**: `POST /api/v1/video/devices/{deviceId}`

**⚠️ 重要变更**: `deviceId`现在作为第一个参数传递

**请求参数**:
```javascript
// deviceId: 设备ID（第一个参数）
// params: 更新数据（第二个参数）
{
  deviceName: '',      // 设备名称
  deviceType: '',      // 设备类型
  areaId: null,        // 区域ID
  location: '',         // 安装位置
  ip: '',               // IP地址
  port: 554,           // 端口号
  manufacturer: '',     // 制造商
  model: '',           // 型号
  firmwareVersion: '', // 固件版本
  description: ''      // 描述
}
```

**响应格式**:
```javascript
{
  code: 1,
  msg: 'success',
  data: null
}
```

**状态**: ✅ **已更新**

#### 1.4 删除设备

**API方法**: `deviceApi.deleteDevice(deviceId)`

**接口路径**: `DELETE /api/v1/video/devices/{deviceId}`

**请求参数**: `deviceId` - 设备ID

**响应格式**:
```javascript
{
  code: 1,
  msg: 'success',
  data: null
}
```

**状态**: ✅ **已更新**

#### 1.5 获取设备详情

**API方法**: `deviceApi.getDeviceInfo(deviceId)`

**接口路径**: `GET /api/v1/video/devices/{deviceId}`

**请求参数**: `deviceId` - 设备ID

**响应格式**:
```javascript
{
  code: 1,
  msg: 'success',
  data: {
    deviceId: 123,
    deviceCode: '',
    deviceName: '',
    // ... 其他设备信息
  }
}
```

**状态**: ✅ **已更新**

#### 1.6 获取设备统计

**API方法**: `deviceApi.getDeviceStatistics()`

**接口路径**: `GET /api/v1/video/devices/statistics`

**响应格式**:
```javascript
{
  code: 1,
  msg: 'success',
  data: {
    totalDevices: 100,
    onlineDevices: 95,
    offlineDevices: 5,
    // ... 其他统计信息
  }
}
```

**状态**: ✅ **已更新**

---

### 2. PTZ控制API文档

#### 2.1 云台控制

**API方法**: `monitorApi.ptzControl(deviceId, params)`

**接口路径**: `POST /api/v1/video/devices/{deviceId}/ptz`

**⚠️ 重要变更**: `deviceId`现在作为第一个参数传递

**请求参数**:
```javascript
// deviceId: 设备ID（第一个参数）
// params: 控制参数（第二个参数）
{
  command: 'up',      // 控制命令：up/down/left/right/zoom_in/zoom_out/focus_in/focus_out
  speed: 5,           // 控制速度：1-10
  duration: 1000      // 持续时间（毫秒）
}
```

**响应格式**:
```javascript
{
  code: 1,
  msg: 'success',
  data: '控制成功'
}
```

**状态**: ✅ **已更新**

---

### 3. 录像API文档

#### 3.1 开始录像

**API方法**: `monitorApi.startRecord(deviceId, streamType)`

**接口路径**: `POST /api/v1/video/monitor/{deviceId}/record/start`

**⚠️ 重要变更**: `deviceId`现在作为第一个参数传递

**请求参数**:
```javascript
// deviceId: 设备ID（第一个参数）
// streamType: 流类型（第二个参数，可选，默认'MAIN'）
// 可选值：'MAIN'（主码流）、'SUB'（子码流）
```

**响应格式**:
```javascript
{
  code: 1,
  msg: 'success',
  data: 1234567890  // 录像ID
}
```

**使用示例**:
```javascript
// 开始录制主码流
const recordId = await monitorApi.startRecord(deviceId, 'MAIN');

// 开始录制子码流
const recordId = await monitorApi.startRecord(deviceId, 'SUB');
```

**状态**: ✅ **已更新**

#### 3.2 停止录像

**API方法**: `monitorApi.stopRecord(deviceId, recordId)`

**接口路径**: `POST /api/v1/video/monitor/{deviceId}/record/stop`

**⚠️ 重要变更**: `deviceId`和`recordId`现在作为参数传递

**请求参数**:
```javascript
// deviceId: 设备ID（第一个参数）
// recordId: 录像ID（第二个参数）
```

**响应格式**:
```javascript
{
  code: 1,
  msg: 'success',
  data: '录像已停止'
}
```

**使用示例**:
```javascript
// 停止录像
await monitorApi.stopRecord(deviceId, recordId);
```

**状态**: ✅ **已更新**

---

### 4. 预置位API文档

#### 4.1 获取预置位列表

**API方法**: `monitorApi.getPresetList(deviceId)`

**接口路径**: `GET /api/v1/video/monitor/{deviceId}/preset/list`

**请求参数**: `deviceId` - 设备ID

**响应格式**:
```javascript
{
  code: 1,
  msg: 'success',
  data: [
    {
      presetNum: 1,
      presetName: '预置位1',
      enabled: true
    },
    {
      presetNum: 2,
      presetName: '预置位2',
      enabled: true
    }
    // ... 更多预置位
  ]
}
```

**使用示例**:
```javascript
// 获取预置位列表
const presetList = await monitorApi.getPresetList(deviceId);
```

**状态**: ✅ **已更新**

#### 4.2 调用预置位

**API方法**: `monitorApi.gotoPreset(deviceId, presetNum)`

**接口路径**: `POST /api/v1/video/monitor/{deviceId}/preset/{presetNum}`

**⚠️ 重要变更**: `deviceId`和`presetNum`现在作为参数传递

**请求参数**:
```javascript
// deviceId: 设备ID（第一个参数）
// presetNum: 预置位编号（第二个参数）
```

**响应格式**:
```javascript
{
  code: 1,
  msg: 'success',
  data: '调用预置位成功'
}
```

**使用示例**:
```javascript
// 调用预置位1
await monitorApi.gotoPreset(deviceId, 1);

// 调用预置位2
await monitorApi.gotoPreset(deviceId, 2);
```

**状态**: ✅ **已更新**

---

## 📋 移动端API文档更新

### 1. 告警API文档

#### 1.1 获取活跃告警列表

**API方法**: `alarmApi.getActiveAlarms(limit)`

**接口路径**: `GET /api/v1/video/alarms/active`

**⚠️ 重要变更**: 路径从`/alarm/active`改为`/alarms/active`（复数形式）

**请求参数**:
```javascript
{
  limit: 10  // 返回数量限制（可选，默认10）
}
```

**响应格式**:
```javascript
{
  code: 1,
  msg: 'success',
  data: [
    {
      alarmId: 123,
      deviceId: 456,
      alarmType: 'MOTION',
      alarmLevel: 'HIGH',
      alarmMessage: '检测到移动',
      alarmTime: '2025-01-30 10:00:00'
    }
    // ... 更多告警
  ]
}
```

**状态**: ✅ **已更新**

#### 1.2 获取告警统计

**API方法**: `alarmApi.getAlarmStatistics(timeRange)`

**接口路径**: `GET /api/v1/video/alarms/statistics`

**⚠️ 重要变更**: 路径从`/alarm/statistics`改为`/alarms/statistics`（复数形式）

**请求参数**:
```javascript
{
  timeRange: 'DAY'  // 时间范围：DAY/WEEK/MONTH（可选，默认DAY）
}
```

**响应格式**:
```javascript
{
  code: 1,
  msg: 'success',
  data: {
    totalAlarms: 100,
    unprocessedAlarms: 10,
    processedAlarms: 90,
    // ... 其他统计信息
  }
}
```

**状态**: ✅ **已更新**

### 2. 设备统计API文档

#### 2.1 获取设备统计

**API方法**: `deviceApi.getDeviceStatistics()`

**接口路径**: `GET /api/v1/video/devices/statistics`

**⚠️ 重要变更**: 路径从`/device/statistics`改为`/devices/statistics`（复数形式）

**响应格式**:
```javascript
{
  code: 1,
  msg: 'success',
  data: {
    totalDevices: 100,
    onlineDevices: 95,
    offlineDevices: 5,
    // ... 其他统计信息
  }
}
```

**状态**: ✅ **已更新**

---

## ✅ 文档更新完成确认

### Swagger文档

- ✅ 录像接口文档已更新（2个接口）
- ✅ 预置位接口文档已更新（2个接口）
- ✅ 所有接口都有完整的`@Operation`和`@Parameter`注解

### 前端API调用文档

- ✅ 设备管理API文档已更新（6个接口）
- ✅ PTZ控制API文档已更新（1个接口）
- ✅ 录像API文档已更新（2个接口）
- ✅ 预置位API文档已更新（2个接口）
- ✅ 移动端API文档已更新（3个接口）

### API方法签名变更说明

- ✅ 所有变更的方法签名都已记录
- ✅ 提供了使用示例代码
- ✅ 标注了重要变更提示

---

## 📋 后续工作建议

### P1级工作（建议立即执行）

1. **Swagger UI验证**:
   - ⏳ 启动后端服务，访问Swagger UI
   - ⏳ 验证新增接口是否正常显示
   - ⏳ 测试接口文档的完整性和准确性

2. **前端文档集成**:
   - ⏳ 将API文档集成到前端文档站点
   - ⏳ 更新前端开发指南
   - ⏳ 添加API调用示例代码

---

**报告生成**: P1级API文档更新报告  
**维护责任人**: 架构委员会  
**执行时间**: 2025-01-30  
**状态**: ✅ **文档更新完成**
