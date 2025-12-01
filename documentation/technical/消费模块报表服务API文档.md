# 消费模块报表服务API文档

**版本**: v3.0.0  
**更新时间**: 2025-11-19  
**严格遵循repowiki规范**

---

## 📋 目录

1. [概述](#概述)
2. [基础报表生成](#基础报表生成)
3. [高级分析报表](#高级分析报表)
4. [报表管理](#报表管理)
5. [性能优化](#性能优化)
6. [使用示例](#使用示例)

---

## 概述

报表服务（`ReportService`）是消费模块的核心服务之一，提供全面的数据统计、分析和报表生成功能。

### 核心特性

- ✅ **19个核心方法**：覆盖所有报表需求
- ✅ **多级缓存策略**：提升查询性能
- ✅ **完整单元测试**：确保代码质量
- ✅ **严格规范遵循**：符合repowiki编码标准

### 缓存策略

| 方法 | 缓存时间 | 说明 |
|------|---------|------|
| `getConsumeSummary` | 10分钟 | 消费汇总统计 |
| `getDashboardData` | 1分钟 | 仪表盘实时数据 |
| `getRealTimeStatistics` | 30秒 | 实时统计数据 |

---

## 基础报表生成

### 1. 生成消费报表

**方法**: `generateConsumeReport(Map<String, Object> params)`

**参数说明**:
- `startTime` (LocalDateTime): 开始时间，默认最近30天
- `endTime` (LocalDateTime): 结束时间，默认当前时间
- `timeDimension` (String): 时间维度（DAY/MONTH），默认DAY
- `deviceId` (Long): 设备ID，可选
- `consumeMode` (String): 消费模式，可选

**返回数据**:
```json
{
  "reportType": "CONSUME",
  "startTime": "2025-11-01T00:00:00",
  "endTime": "2025-11-19T23:59:59",
  "data": [
    {
      "date": "2025-11-01",
      "amount": 1000.00,
      "count": 50
    }
  ],
  "summary": {
    "totalAmount": 50000.00,
    "totalCount": 2500
  }
}
```

### 2. 生成充值报表

**方法**: `generateRechargeReport(Map<String, Object> params)`

**参数说明**:
- `startTime` (LocalDateTime): 开始时间
- `endTime` (LocalDateTime): 结束时间
- `userId` (Long): 用户ID，可选

**返回数据**:
```json
{
  "reportType": "RECHARGE",
  "startTime": "2025-11-01T00:00:00",
  "endTime": "2025-11-19T23:59:59",
  "data": [
    {
      "userId": 1001,
      "userName": "张三",
      "amount": 500.00,
      "rechargeTime": "2025-11-01T10:00:00"
    }
  ],
  "summary": {
    "totalAmount": 100000.00,
    "totalCount": 200
  }
}
```

### 3. 生成用户消费统计报表

**方法**: `generateUserConsumeReport(Map<String, Object> params)`

**参数说明**:
- `startTime` (LocalDateTime): 开始时间
- `endTime` (LocalDateTime): 结束时间
- `userId` (Long): 用户ID，可选

**返回数据**:
```json
{
  "reportType": "USER_CONSUME",
  "data": [
    {
      "userId": 1001,
      "userName": "张三",
      "totalAmount": 2000.00,
      "totalCount": 100,
      "avgAmount": 20.00
    }
  ]
}
```

### 4. 生成设备使用报表

**方法**: `generateDeviceUsageReport(Map<String, Object> params)`

**参数说明**:
- `startTime` (LocalDateTime): 开始时间
- `endTime` (LocalDateTime): 结束时间
- `deviceId` (Long): 设备ID，可选

**返回数据**:
```json
{
  "reportType": "DEVICE_USAGE",
  "data": [
    {
      "deviceId": 2001,
      "deviceName": "食堂1号机",
      "totalAmount": 5000.00,
      "totalCount": 250,
      "usageRate": 0.85
    }
  ]
}
```

---

## 高级分析报表

### 1. 消费汇总

**方法**: `getConsumeSummary(String timeDimension, LocalDateTime startTime, LocalDateTime endTime, Long deviceId, String consumeMode)`

**缓存**: 10分钟

**示例**:
```java
Map<String, Object> summary = reportService.getConsumeSummary(
    "DAY", 
    LocalDateTime.now().minusDays(30), 
    LocalDateTime.now(), 
    null, 
    null
);
```

**返回数据**:
```json
{
  "totalAmount": 50000.00,
  "totalCount": 2500,
  "avgAmount": 20.00,
  "details": {
    "2025-11-01": {
      "amount": 1000.00,
      "count": 50
    }
  },
  "timeDimension": "DAY"
}
```

### 2. 消费趋势

**方法**: `getConsumeTrend(String timeDimension, LocalDateTime startTime, LocalDateTime endTime, String trendType, Long deviceId, String consumeMode)`

**参数说明**:
- `timeDimension`: DAY/MONTH/YEAR/HOUR
- `trendType`: AMOUNT（金额）/COUNT（次数）

**返回数据**:
```json
[
  {
    "time": "2025-11-01",
    "amount": 1000.00,
    "count": 50
  }
]
```

### 3. 消费模式分布

**方法**: `getConsumeModeDistribution(LocalDateTime startTime, LocalDateTime endTime, Long deviceId)`

**返回数据**:
```json
[
  {
    "consumeMode": "STANDARD",
    "amount": 30000.00,
    "count": 1500,
    "percentage": 60.0
  }
]
```

### 4. 设备消费排行

**方法**: `getDeviceRanking(LocalDateTime startTime, LocalDateTime endTime, String rankingType, Integer limit)`

**参数说明**:
- `rankingType`: AMOUNT（金额）/COUNT（次数）
- `limit`: 返回数量限制，默认10

**返回数据**:
```json
[
  {
    "rank": 1,
    "deviceId": 2001,
    "deviceName": "食堂1号机",
    "amount": 5000.00,
    "count": 250
  }
]
```

### 5. 用户消费排行

**方法**: `getUserRanking(LocalDateTime startTime, LocalDateTime endTime, String rankingType, Integer limit)`

**返回数据**:
```json
[
  {
    "rank": 1,
    "userId": 1001,
    "userName": "张三",
    "amount": 2000.00,
    "count": 100
  }
]
```

### 6. 时段分布

**方法**: `getHourDistribution(LocalDateTime startTime, LocalDateTime endTime, Long deviceId)`

**返回数据**:
```json
[
  {
    "hour": 12,
    "amount": 5000.00,
    "count": 250
  }
]
```

### 7. 地区分布

**方法**: `getRegionDistribution(LocalDateTime startTime, LocalDateTime endTime, String regionMetric)`

**参数说明**:
- `regionMetric`: AMOUNT（金额）/COUNT（次数）

**返回数据**:
```json
[
  {
    "regionId": 3001,
    "regionName": "A区",
    "amount": 20000.00,
    "count": 1000
  }
]
```

### 8. 同比环比

**方法**: `getComparisonData(String comparisonType, LocalDateTime startTime, LocalDateTime endTime, Long deviceId, String consumeMode)`

**参数说明**:
- `comparisonType`: YEAR_OVER_YEAR（同比）/MONTH_OVER_MONTH（环比）

**返回数据**:
```json
{
  "currentAmount": 50000.00,
  "previousAmount": 45000.00,
  "growthRate": 11.11,
  "comparisonType": "YEAR_OVER_YEAR"
}
```

### 9. 仪表盘数据

**方法**: `getDashboardData(LocalDateTime startTime, LocalDateTime endTime)`

**缓存**: 1分钟

**返回数据**:
```json
{
  "todayAmount": 5000.00,
  "todayCount": 250,
  "totalAmount": 50000.00,
  "totalCount": 2500,
  "avgAmount": 20.00,
  "updateTime": "2025-11-19T15:30:00"
}
```

### 10. 实时统计

**方法**: `getRealTimeStatistics()`

**缓存**: 30秒

**返回数据**:
```json
{
  "todayAmount": 5000.00,
  "todayCount": 250,
  "hourAmount": 500.00,
  "hourCount": 25,
  "updateTime": "2025-11-19T15:30:00",
  "onlineUsers": 0
}
```

### 11. 异常检测

**方法**: `getAnomalyDetection(LocalDateTime startTime, LocalDateTime endTime, String detectionType)`

**返回数据**:
```json
[
  {
    "recordId": 12345,
    "amount": 1000.00,
    "payTime": "2025-11-19T10:00:00",
    "anomalyType": "HIGH",
    "deviation": 500.00,
    "avgAmount": 20.00
  }
]
```

### 12. 预测分析

**方法**: `getForecastAnalysis(String forecastType, LocalDateTime startTime, LocalDateTime endTime, Integer forecastPeriod)`

**返回数据**:
```json
{
  "forecastAmount": 55000.00,
  "forecastCount": 2750,
  "confidence": 0.85,
  "forecastPeriod": 7,
  "historicalAvgAmount": 20.00,
  "trend": 0.5
}
```

---

## 报表管理

### 1. 导出报表

**方法**: `exportReport(String reportType, Map<String, Object> params, String format)`

**参数说明**:
- `reportType`: CONSUME/RECHARGE/USER_CONSUME/DEVICE_USAGE
- `format`: CSV（当前支持）

**返回**: 导出文件相对路径

**示例**:
```java
String filePath = reportService.exportReport(
    "CONSUME", 
    params, 
    "CSV"
);
```

### 2. 获取报表列表

**方法**: `getReportList(Map<String, Object> params)`

**参数说明**:
- `reportType` (String): 报表类型筛选，可选

**返回数据**:
```json
[
  {
    "reportId": 12345,
    "fileName": "consume_report_20251119_153000.csv",
    "reportType": "CONSUME",
    "fileSize": 102400,
    "createTime": "2025-11-19T15:30:00"
  }
]
```

### 3. 删除报表

**方法**: `deleteReport(Long reportId)`

**返回**: boolean（删除成功/失败）

---

## 性能优化

### 缓存策略

报表服务采用多级缓存策略，使用`ConsumeCacheService`实现：

1. **消费汇总缓存**（10分钟）
   - 缓存键: `report:summary:{timeDimension}:{startTime}:{endTime}:{deviceId}:{consumeMode}`
   - 适用于: 历史数据统计查询

2. **仪表盘数据缓存**（1分钟）
   - 缓存键: `report:dashboard:{startTime}:{endTime}`
   - 适用于: 实时数据展示

3. **实时统计缓存**（30秒）
   - 缓存键: `report:realtime:{yyyyMMddHHmm}`
   - 适用于: 高频实时查询

### 缓存失效

- 自动过期：根据TTL自动失效
- 手动清除：可通过`ConsumeCacheService.evictConsumeStatsCache()`清除

---

## 使用示例

### Java代码示例

```java
@Resource
private ReportService reportService;

// 1. 获取消费汇总
Map<String, Object> summary = reportService.getConsumeSummary(
    "DAY",
    LocalDateTime.now().minusDays(30),
    LocalDateTime.now(),
    null,
    null
);

// 2. 获取仪表盘数据
Map<String, Object> dashboard = reportService.getDashboardData(
    LocalDateTime.now().minusDays(7),
    LocalDateTime.now()
);

// 3. 导出报表
Map<String, Object> params = new HashMap<>();
params.put("startTime", LocalDateTime.now().minusDays(30));
params.put("endTime", LocalDateTime.now());
String filePath = reportService.exportReport("CONSUME", params, "CSV");
```

### 前端调用示例

```javascript
// 获取消费汇总
const summary = await api.post('/api/consume/report/summary', {
  timeDimension: 'DAY',
  startTime: '2025-11-01T00:00:00',
  endTime: '2025-11-19T23:59:59'
});

// 获取仪表盘数据
const dashboard = await api.post('/api/consume/report/dashboard', {
  startTime: '2025-11-01T00:00:00',
  endTime: '2025-11-19T23:59:59'
});
```

---

## 注意事项

1. **时间参数**: 所有时间参数使用`LocalDateTime`类型
2. **缓存策略**: 实时数据缓存时间短，历史数据缓存时间长
3. **异常处理**: 所有方法都有完整的异常处理和日志记录
4. **性能优化**: 大数据量查询建议使用分页或限制时间范围
5. **编码规范**: 严格遵循repowiki规范（jakarta包名、@Resource注入、SLF4J日志）

---

## 更新日志

- **2025-11-19**: 添加缓存策略和单元测试，完善API文档
- **2025-11-18**: 实现所有19个核心方法
- **2025-11-17**: 初始版本，实现基础报表功能

