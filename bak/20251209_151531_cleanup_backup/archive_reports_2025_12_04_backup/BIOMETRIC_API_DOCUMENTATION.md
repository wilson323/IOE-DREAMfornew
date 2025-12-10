# 生物识别监控API文档

**服务名称**: ioedream-common-service  
**API基础路径**: `/api/v1/common/biometric/monitor`  
**文档版本**: 1.0.0  
**更新日期**: 2025-12-03

---

## 📋 API概览

生物识别监控API提供以下功能：
- 设备状态监控
- 识别性能监控
- 异常告警管理
- 系统健康检查
- 统计分析报告

---

## 🔌 API接口列表

### 1. 设备状态监控

#### 1.1 获取所有设备状态
```http
GET /api/v1/common/biometric/monitor/devices/status/all
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "deviceId": 1,
      "deviceName": "人脸识别设备001",
      "deviceStatus": 1,
      "online": true,
      "cpuUsage": 45.5,
      "memoryUsage": 60.2,
      "lastOnlineTime": "2025-12-03T10:30:00"
    }
  ]
}
```

#### 1.2 获取设备详情
```http
GET /api/v1/common/biometric/monitor/devices/{deviceId}/detail
```

**路径参数**:
- `deviceId` (Long): 设备ID

#### 1.3 获取设备健康状态
```http
GET /api/v1/common/biometric/monitor/devices/{deviceId}/health
```

#### 1.4 获取设备性能数据
```http
GET /api/v1/common/biometric/monitor/devices/{deviceId}/performance?startTime={startTime}&endTime={endTime}
```

**查询参数**:
- `startTime` (LocalDateTime): 开始时间
- `endTime` (LocalDateTime): 结束时间

---

### 2. 日志查询

#### 2.1 分页查询生物识别日志
```http
POST /api/v1/common/biometric/monitor/logs/page
```

**请求体**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "deviceId": 1,
  "userId": 1001,
  "biometricType": "FACE",
  "startTime": "2025-12-01T00:00:00",
  "endTime": "2025-12-03T23:59:59"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "logId": 1,
        "deviceId": 1,
        "personId": 1001,
        "biometricType": "FACE",
        "operationType": "RECOGNIZE",
        "operationResult": "SUCCESS",
        "similarity": 0.95,
        "processingTime": 120,
        "createTime": "2025-12-03T10:30:00"
      }
    ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10
  }
}
```

---

### 3. 统计分析

#### 3.1 获取今日统计
```http
GET /api/v1/common/biometric/monitor/statistics/today
```

#### 3.2 获取历史统计
```http
GET /api/v1/common/biometric/monitor/statistics/history?startTime={startTime}&endTime={endTime}&statisticsType={type}
```

**查询参数**:
- `startTime` (LocalDateTime): 开始时间
- `endTime` (LocalDateTime): 结束时间
- `statisticsType` (String): 统计类型（day/week/month）

#### 3.3 获取成功率分析
```http
GET /api/v1/common/biometric/monitor/analysis/successRate?startTime={startTime}&endTime={endTime}
```

#### 3.4 获取响应时间分析
```http
GET /api/v1/common/biometric/monitor/analysis/responseTime?startTime={startTime}&endTime={endTime}
```

---

### 4. 告警管理

#### 4.1 获取告警列表
```http
GET /api/v1/common/biometric/monitor/alerts?alertLevel={level}&alertStatus={status}&days={days}
```

**查询参数**:
- `alertLevel` (Integer, 可选): 告警级别（1-低, 2-中, 3-高, 4-紧急）
- `alertStatus` (Integer, 可选): 告警状态（0-未处理, 1-处理中, 2-已处理）
- `days` (Integer, 可选): 查询天数（默认7天）

#### 4.2 处理告警
```http
POST /api/v1/common/biometric/monitor/alerts/{alertId}/handle?handleRemark={remark}
```

**路径参数**:
- `alertId` (Long): 告警ID

**查询参数**:
- `handleRemark` (String): 处理说明

---

### 5. 系统监控

#### 5.1 获取系统健康状态
```http
GET /api/v1/common/biometric/monitor/system/health
```

#### 5.2 获取系统负载
```http
GET /api/v1/common/biometric/monitor/system/load
```

#### 5.3 检查离线设备
```http
GET /api/v1/common/biometric/monitor/devices/checkOffline
```

#### 5.4 检查性能异常
```http
GET /api/v1/common/biometric/monitor/devices/checkPerformanceAbnormal
```

---

### 6. 高级功能

#### 6.1 获取准确率监控
```http
GET /api/v1/common/biometric/monitor/monitor/accuracy?hours={hours}
```

**查询参数**:
- `hours` (Integer): 监控时间范围（小时）

#### 6.2 获取用户活跃度
```http
GET /api/v1/common/biometric/monitor/statistics/userActivity?days={days}&userId={userId}
```

**查询参数**:
- `days` (Integer): 统计天数
- `userId` (Long, 可选): 用户ID（可选）

#### 6.3 获取维护提醒
```http
GET /api/v1/common/biometric/monitor/maintenance/reminders
```

#### 6.4 生成监控报告
```http
GET /api/v1/common/biometric/monitor/reports/generate?reportType={type}&startTime={startTime}&endTime={endTime}
```

**查询参数**:
- `reportType` (String): 报告类型（daily/weekly/monthly）
- `startTime` (LocalDateTime): 开始时间
- `endTime` (LocalDateTime): 结束时间

#### 6.5 导出监控数据
```http
GET /api/v1/common/biometric/monitor/export?exportType={type}&startTime={startTime}&endTime={endTime}
```

**查询参数**:
- `exportType` (String): 导出类型（logs/alerts/performance）
- `startTime` (LocalDateTime): 开始时间
- `endTime` (LocalDateTime): 结束时间

#### 6.6 获取仪表板数据
```http
GET /api/v1/common/biometric/monitor/dashboard
```

---

## 🔄 API路径变更对照表

| 功能 | 原路径（门禁服务） | 新路径（公共服务） |
|------|------------------|------------------|
| 获取所有设备状态 | `/api/v1/access/biometric/monitor/devices/status` | `/api/v1/common/biometric/monitor/devices/status/all` |
| 获取设备详情 | `/api/v1/access/biometric/monitor/devices/{id}` | `/api/v1/common/biometric/monitor/devices/{id}/detail` |
| 获取设备健康状态 | `/api/v1/access/biometric/monitor/devices/{id}/health` | `/api/v1/common/biometric/monitor/devices/{id}/health` |
| 分页查询日志 | `/api/v1/access/biometric/monitor/logs/page` | `/api/v1/common/biometric/monitor/logs/page` |
| 获取今日统计 | `/api/v1/access/biometric/monitor/statistics/today` | `/api/v1/common/biometric/monitor/statistics/today` |
| 获取告警列表 | `/api/v1/access/biometric/monitor/alerts` | `/api/v1/common/biometric/monitor/alerts` |
| 处理告警 | `/api/v1/access/biometric/monitor/alerts/{id}/handle` | `/api/v1/common/biometric/monitor/alerts/{id}/handle` |
| 获取系统健康状态 | `/api/v1/access/biometric/monitor/system/health` | `/api/v1/common/biometric/monitor/system/health` |

---

## 📝 调用示例

### Java代码示例（业务微服务中）

```java
@Service
public class AccessServiceImpl implements AccessService {

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 获取生物识别设备状态
     */
    public List<BiometricStatusVO> getBiometricDeviceStatus() {
        ResponseDTO<List<BiometricStatusVO>> response = gatewayServiceClient.callCommonService(
            "/biometric/monitor/devices/status/all",
            HttpMethod.GET,
            null,
            new TypeReference<ResponseDTO<List<BiometricStatusVO>>>() {}
        );
        return response.getData();
    }

    /**
     * 获取设备健康状态
     */
    public Map<String, Object> getDeviceHealth(Long deviceId) {
        ResponseDTO<Map<String, Object>> response = gatewayServiceClient.callCommonService(
            "/biometric/monitor/devices/" + deviceId + "/health",
            HttpMethod.GET,
            null,
            new TypeReference<ResponseDTO<Map<String, Object>>>() {}
        );
        return response.getData();
    }

    /**
     * 分页查询生物识别日志
     */
    public PageResult<BiometricLogEntity> queryBiometricLogs(BiometricQueryForm queryForm) {
        ResponseDTO<PageResult<BiometricLogEntity>> response = gatewayServiceClient.callCommonService(
            "/biometric/monitor/logs/page",
            HttpMethod.POST,
            queryForm,
            new TypeReference<ResponseDTO<PageResult<BiometricLogEntity>>>() {}
        );
        return response.getData();
    }
}
```

---

## ⚠️ 注意事项

1. **服务间调用**: 所有业务微服务必须通过`GatewayServiceClient`调用公共服务API，禁止直接调用
2. **API路径**: 所有API路径已从`/api/v1/access/biometric/monitor`改为`/api/v1/common/biometric/monitor`
3. **认证授权**: 所有API都需要通过网关进行认证授权
4. **错误处理**: 统一使用`ResponseDTO`格式返回，包含`code`、`message`、`data`字段

---

**文档维护**: IOE-DREAM架构委员会  
**最后更新**: 2025-12-03

