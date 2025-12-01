# 考勤管理系统API接口文档

> **版本**: v1.0
> **更新时间**: 2025-11-13
> **API基础路径**: `/api/v1/attendance`
> **认证方式**: Bearer Token (Sa-Token)

## 1. 接口概述

### 1.1 接口规范

- **协议**: HTTPS
- **数据格式**: JSON
- **字符编码**: UTF-8
- **时间格式**: ISO 8601 (YYYY-MM-DDTHH:mm:ss)
- **分页方式**: PageNumber + PageSize
- **响应格式**: 统一ResponseDTO格式

### 1.2 通用响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2025-11-13T10:30:00Z",
  "traceId": "trace-123456789"
}
```

### 1.3 错误码定义

| 错误码 | 说明 | HTTP状态码 |
|--------|------|------------|
| 200 | 操作成功 | 200 |
| 400 | 请求参数错误 | 400 |
| 401 | 未授权访问 | 401 |
| 403 | 权限不足 | 403 |
| 404 | 资源不存在 | 404 |
| 409 | 业务冲突 | 409 |
| 429 | 请求频率超限 | 429 |
| 500 | 服务器内部错误 | 500 |

## 2. 考勤打卡接口

### 2.1 考勤打卡

**接口地址**: `POST /api/v1/attendance/clock`

**接口描述**: 执行考勤打卡操作，支持多模态生物识别验证

**请求参数**:
```json
{
  "employeeId": 10001,
  "clockType": "IN", // IN-上班打卡, OUT-下班打卡
  "deviceId": "DEV001",
  "deviceType": "FACE_RECOGNITION", // FACE_RECOGNITION, FINGERPRINT, CARD, MOBILE
  "location": {
    "longitude": 116.4074,
    "latitude": 39.9042,
    "accuracy": 10.5,
    "address": "北京市朝阳区xxx大厦"
  },
  "biometricData": {
    "type": "FACE", // FACE, FINGERPRINT, PALM_PRINT, IRIS, VOICE
    "template": "base64编码的生物特征数据",
    "confidence": 0.95,
    "liveness": true
  },
  "deviceBinding": {
    "deviceId": "MOB001",
    "deviceFingerprint": "device_fingerprint_hash"
  },
  "timestamp": "2025-11-13T09:00:00Z"
}
```

**响应结果**:
```json
{
  "code": 200,
  "message": "打卡成功",
  "data": {
    "recordId": 123456,
    "employeeId": 10001,
    "employeeName": "张三",
    "clockType": "IN",
    "clockTime": "2025-11-13T09:00:00Z",
    "deviceName": "一楼门禁机",
    "verificationMethod": "人脸识别",
    "location": "北京市朝阳区xxx大厦",
    "verificationResult": "SUCCESS",
    "confidence": 0.95,
    "workShift": "标准班次",
    "isLate": false,
    "lateMinutes": 0
  }
}
```

### 2.2 批量打卡

**接口地址**: `POST /api/v1/attendance/clock/batch`

**接口描述**: 批量处理考勤打卡记录，适用于离线数据同步

**请求参数**:
```json
{
  "deviceId": "DEV001",
  "records": [
    {
      "employeeId": 10001,
      "clockType": "IN",
      "timestamp": "2025-11-13T08:55:00Z",
      "verificationMethod": "FINGERPRINT",
      "confidence": 0.98
    }
  ]
}
```

## 3. 考勤记录查询接口

### 3.1 获取个人考勤记录

**接口地址**: `GET /api/v1/attendance/records`

**接口描述**: 分页查询个人考勤记录

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| employeeId | Long | 是 | 员工ID |
| startDate | String | 是 | 开始日期 (YYYY-MM-DD) |
| endDate | String | 是 | 结束日期 (YYYY-MM-DD) |
| clockType | String | 否 | 打卡类型 (IN/OUT) |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 页大小，默认20 |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "pageNum": 1,
    "pageSize": 20,
    "total": 44,
    "pages": 3,
    "list": [
      {
        "recordId": 123456,
        "employeeId": 10001,
        "clockType": "IN",
        "clockTime": "2025-11-13T09:00:00Z",
        "deviceName": "一楼门禁机",
        "verificationMethod": "人脸识别",
        "location": "北京市朝阳区xxx大厦",
        "confidence": 0.95,
        "workShift": "标准班次",
        "isLate": false,
        "lateMinutes": 0,
        "abnormalFlag": false
      }
    ]
  }
}
```

### 3.2 获取部门考勤统计

**接口地址**: `GET /api/v1/attendance/department/statistics`

**接口描述**: 获取部门考勤统计数据

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| departmentId | Long | 是 | 部门ID |
| statisticsType | String | 否 | 统计类型 (DAILY/WEEKLY/MONTHLY) |
| startDate | String | 是 | 开始日期 |
| endDate | String | 是 | 结束日期 |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "departmentId": 1001,
    "departmentName": "技术研发部",
    "statisticsType": "MONTHLY",
    "statisticsPeriod": "2025-11",
    "totalEmployees": 25,
    "presentEmployees": 23,
    "absentEmployees": 2,
    "attendanceRate": 0.92,
    "avgWorkHours": 8.2,
    "totalOvertimeHours": 45.5,
    "statistics": [
      {
        "date": "2025-11-01",
        "presentCount": 24,
        "absentCount": 1,
        "lateCount": 2,
        "earlyCount": 1
      }
    ]
  }
}
```

## 4. 考勤汇总接口

### 4.1 获取个人考勤汇总

**接口地址**: `GET /api/v1/attendance/summary/personal`

**接口描述**: 获取个人月度考勤汇总

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| employeeId | Long | 是 | 员工ID |
| month | String | 是 | 月份 (YYYY-MM) |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "employeeId": 10001,
    "employeeName": "张三",
    "departmentName": "技术研发部",
    "month": "2025-11",
    "workDays": 22,
    "actualDays": 20,
    "attendanceRate": 0.91,
    "lateCount": 2,
    "earlyCount": 1,
    "absentDays": 1,
    "leaveDays": 1,
    "overtimeHours": 12.5,
    "weekendOvertimeHours": 8.0,
    "holidayOvertimeHours": 0,
    "workHoursDetails": [
      {
        "date": "2025-11-01",
        "clockInTime": "09:00",
        "clockOutTime": "18:30",
        "workHours": 8.5,
        "overtimeHours": 0.5,
        "status": "NORMAL"
      }
    ],
    "biometricStats": {
      "faceRecognitionCount": 40,
      "fingerprintCount": 10,
      "palmPrintCount": 5,
      "successRate": 0.98
    }
  }
}
```

### 4.2 获取考勤报表

**接口地址**: `GET /api/v1/attendance/reports`

**接口描述**: 生成并下载考勤报表

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reportType | String | 是 | 报表类型 (PERSONAL/DEPARTMENT/COMPANY) |
| reportFormat | String | 是 | 报表格式 (PDF/EXCEL/CSV) |
| templateId | Long | 否 | 报表模板ID |
| filterParams | Object | 否 | 筛选参数 |

**响应结果**:
```json
{
  "code": 200,
  "message": "报表生成成功",
  "data": {
    "reportId": "RPT_20251113_001",
    "reportName": "技术研发部11月考勤报表",
    "reportType": "DEPARTMENT",
    "reportFormat": "PDF",
    "fileSize": 2048576,
    "downloadUrl": "/api/v1/attendance/reports/RPT_20251113_001/download",
    "generateTime": "2025-11-13T10:30:00Z",
    "expiryTime": "2025-11-14T10:30:00Z"
  }
}
```

## 5. 生物识别接口

### 5.1 生物特征注册

**接口地址**: `POST /api/v1/attendance/biometric/register`

**接口描述**: 注册新的生物特征模板

**请求参数**:
```json
{
  "employeeId": 10001,
  "biometricType": "FACE", // FACE, FINGERPRINT, PALM_PRINT, IRIS, VOICE
  "templateData": "base64编码的生物特征模板",
  "qualityMetrics": {
    "imageQuality": 0.95,
    "featureCompleteness": 0.98,
    "templateAccuracy": 0.97
  },
  "enrollMetadata": {
    "deviceType": "FACE_RECOGNITION_DEVICE",
    "deviceId": "CAM001",
    "enrollEnvironment": "NORMAL_LIGHTING",
    "enrollAttempts": 3
  }
}
```

**响应结果**:
```json
{
  "code": 200,
  "message": "生物特征注册成功",
  "data": {
    "templateId": 789012,
    "employeeId": 10001,
    "biometricType": "FACE",
    "templateVersion": "v1.0",
    "enrollDate": "2025-11-13",
    "qualityScore": 0.96,
    "templateStatus": "ACTIVE"
  }
}
```

### 5.2 生物特征验证

**接口地址**: `POST /api/v1/attendance/biometric/verify`

**接口描述**: 验证生物识别身份

**请求参数**:
```json
{
  "employeeId": 10001,
  "biometricType": "FACE",
  "inputData": "base64编码的待验证生物特征",
  "verificationMode": "1:1", // 1:1-验证, 1:N-识别
  "confidenceThreshold": 0.85,
  "livenessRequired": true,
  "deviceInfo": {
    "deviceId": "CAM001",
    "deviceType": "FACE_RECOGNITION_DEVICE"
  }
}
```

**响应结果**:
```json
{
  "code": 200,
  "message": "验证成功",
  "data": {
    "verificationResult": "SUCCESS", // SUCCESS, FAILURE, TIMEOUT, INVALID_INPUT
    "employeeId": 10001,
    "employeeName": "张三",
    "biometricType": "FACE",
    "confidence": 0.96,
    "livenessResult": "PASS",
    "processingTime": 125, // 毫秒
    "templateVersion": "v1.0",
    "verificationTime": "2025-11-13T10:30:00Z"
  }
}
```

### 5.3 生物特征更新

**接口地址**: `PUT /api/v1/attendance/biometric/template`

**接口描述**: 更新生物特征模板

**请求参数**:
```json
{
  "templateId": 789012,
  "employeeId": 10001,
  "biometricType": "FACE",
  "newTemplateData": "base64编码的新生物特征模板",
  "updateReason": "质量提升更新",
  "qualityMetrics": {
    "imageQuality": 0.97,
    "featureCompleteness": 0.99,
    "templateAccuracy": 0.98
  }
}
```

## 6. 设备管理接口

### 6.1 获取设备列表

**接口地址**: `GET /api/v1/attendance/devices`

**接口描述**: 获取考勤设备列表

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deviceType | String | 否 | 设备类型 |
| deviceStatus | Integer | 否 | 设备状态 (0-离线, 1-在线) |
| areaCode | String | 否 | 区域编码 |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 页大小 |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "pageNum": 1,
    "pageSize": 20,
    "total": 15,
    "list": [
      {
        "deviceId": 1001,
        "deviceNo": "DEV001",
        "deviceName": "一楼门禁机",
        "deviceType": "FACE_RECOGNITION",
        "deviceStatus": 1,
        "ipAddress": "192.168.1.101",
        "installLocation": "一楼大厅",
        "lastHeartbeat": "2025-11-13T10:25:00Z",
        "biometricConfig": {
          "supportedTypes": ["FACE", "FINGERPRINT"],
          "confidenceThreshold": 0.85
        }
      }
    ]
  }
}
```

### 6.2 设备状态监控

**接口地址**: `GET /api/v1/attendance/devices/{deviceId}/status`

**接口描述**: 获取设备实时状态

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "deviceId": 1001,
    "deviceNo": "DEV001",
    "deviceName": "一楼门禁机",
    "deviceStatus": 1,
    "lastHeartbeat": "2025-11-13T10:25:00Z",
    "uptime": 86400, // 秒
    "cpuUsage": 15.5,
    "memoryUsage": 45.2,
    "diskUsage": 23.8,
    "networkStatus": "CONNECTED",
    "biometricEngineStatus": "NORMAL",
    "errorCount": 0,
    "warningCount": 2
  }
}
```

## 7. 规则配置接口

### 7.1 获取考勤规则

**接口地址**: `GET /api/v1/attendance/rules`

**接口描述**: 获取考勤规则配置

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleType | String | 否 | 规则类型 |
| employeeId | Long | 否 | 员工ID（获取个人适用规则） |
| departmentId | Long | 否 | 部门ID |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "ruleId": 2001,
      "ruleName": "标准考勤规则",
      "ruleType": "ATTENDANCE",
      "ruleCode": "STANDARD_ATTENDANCE",
      "isEnabled": true,
      "ruleConfig": {
        "workTimeStart": "09:00",
        "workTimeEnd": "18:00",
        "lateTolerance": 10,
        "earlyTolerance": 10,
        "minWorkHours": 8.0,
        "overtimeMultiplier": 1.5
      },
      "applicableScope": {
        "departments": [1001, 1002],
        "positions": ["工程师", "主管"],
        "employees": [10001, 10002]
      }
    }
  ]
}
```

### 7.2 更新考勤规则

**接口地址**: `PUT /api/v1/attendance/rules/{ruleId}`

**接口描述**: 更新考勤规则配置

**请求参数**:
```json
{
  "ruleName": "弹性考勤规则",
  "ruleType": "ATTENDANCE",
  "ruleDescription": "支持弹性工作时间的考勤规则",
  "ruleConfig": {
    "flexibleStartTime": "08:00",
    "flexibleEndTime": "20:00",
    "coreStartTime": "10:00",
    "coreEndTime": "16:00",
    "lateTolerance": 30,
    "minWorkHours": 8.0
  },
  "warningConfig": {
    "noClockInThreshold": 3,
    "frequentLateThreshold": 5
  },
  "applicableScope": {
    "departments": [1001],
    "employees": []
  }
}
```

## 8. 异常处理接口

### 8.1 获取考勤异常列表

**接口地址**: `GET /api/v1/attendance/exceptions`

**接口描述**: 获取考勤异常记录

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| employeeId | Long | 否 | 员工ID |
| exceptionType | String | 否 | 异常类型 |
| startDate | String | 是 | 开始日期 |
| endDate | String | 是 | 结束日期 |
| status | String | 否 | 处理状态 (PENDING/PROCESSED/IGNORED) |

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "pageNum": 1,
    "pageSize": 20,
    "total": 8,
    "list": [
      {
        "exceptionId": 3001,
        "employeeId": 10001,
        "employeeName": "张三",
        "exceptionType": "LATE", // LATE, EARLY, ABSENT, NO_CLOCK_IN, NO_CLOCK_OUT
        "exceptionDate": "2025-11-13",
        "description": "迟到15分钟",
        "severity": "MEDIUM", // LOW, MEDIUM, HIGH
        "status": "PENDING",
        "createTime": "2025-11-13T09:15:00Z",
        "attachment": {
          "fileName": "迟到说明.pdf",
          "fileUrl": "/api/v1/files/download/FILE_001"
        }
      }
    ]
  }
}
```

### 8.2 处理考勤异常

**接口地址**: `POST /api/v1/attendance/exceptions/{exceptionId}/process`

**接口描述**: 处理考勤异常记录

**请求参数**:
```json
{
  "processAction": "APPROVE", // APPROVE, REJECT, IGNORE
  "processResult": "已核实迟到原因，予以批准",
  "adjustClockTime": "2025-11-13T09:00:00Z",
  "compensationType": "ANNUAL_LEAVE", // ANNUAL_LEAVE, SICK_LEAVE, COMP_TIME
  "compensationDuration": 15, // 分钟
  "attachmentIds": ["ATT_001", "ATT_002"]
}
```

## 9. 移动端接口

### 9.1 移动端打卡

**接口地址**: `POST /api/v1/attendance/mobile/clock`

**接口描述**: 移动端考勤打卡

**请求参数**:
```json
{
  "employeeId": 10001,
  "clockType": "IN",
  "location": {
    "longitude": 116.4074,
    "latitude": 39.9042,
    "accuracy": 5.2,
    "address": "北京市朝阳区xxx大厦"
  },
  "deviceInfo": {
    "deviceId": "MOB_001",
    "deviceName": "iPhone 13",
    "osVersion": "iOS 16.0",
    "appVersion": "2.1.0"
  },
  "securityChecks": {
    "wifiSSID": "Office_WiFi",
    "bluetoothDevices": ["BT_001"],
    "vpnConnected": true,
    "rootDetected": false,
    "mockLocationDetected": false
  },
  "timestamp": "2025-11-13T09:00:00Z"
}
```

### 9.2 获取考勤点列表

**接口地址**: `GET /api/v1/attendance/mobile/points`

**接口描述**: 获取可用的考勤点信息

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "pointId": 4001,
      "pointName": "总部大楼",
      "pointCode": "HQ_MAIN",
      "location": {
        "longitude": 116.4074,
        "latitude": 39.9042,
        "effectiveRadius": 100
      },
      "availableTime": "08:00-20:00",
      "supportedMethods": ["GPS", "WIFI", "BLUETOOTH"],
      "status": "ACTIVE",
      "description": "总部大楼主考勤点"
    }
  ]
}
```

## 10. 统计分析接口

### 10.1 获取考勤趋势分析

**接口地址**: `GET /api/v1/attendance/analytics/trends`

**接口描述**: 获取考勤趋势分析数据

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| analysisType | String | 是 | 分析类型 (ATTENDANCE_RATE/OVERTIME/ABSENTEEISM) |
| dimension | String | 否 | 分析维度 (DAILY/WEEKLY/MONTHLY) |
| startDate | String | 是 | 开始日期 |
| endDate | String | 是 | 结束日期 |
| scopeType | String | 否 | 范围类型 (COMPANY/DEPARTMENT/INDIVIDUAL) |
| scopeId | Long | 否 | 范围ID |

**响应结果**:
```json
{
  "code": 200,
  "message": "分析成功",
  "data": {
    "analysisType": "ATTENDANCE_RATE",
    "dimension": "DAILY",
    "period": "2025-11-01至2025-11-13",
    "trendData": [
      {
        "date": "2025-11-01",
        "value": 0.95,
        "changeRate": 0.02,
        "targetValue": 0.90
      }
    ],
    "summary": {
      "averageRate": 0.92,
      "maxRate": 0.98,
      "minRate": 0.85,
      "trendDirection": "STABLE"
    },
    "insights": [
      {
        "type": "IMPROVEMENT",
        "description": "本周出勤率较上周提升2%",
        "suggestion": "继续保持当前的考勤管理策略"
      }
    ]
  }
}
```

### 10.2 生物识别统计

**接口地址**: `GET /api/v1/attendance/analytics/biometric`

**接口描述**: 获取生物识别使用统计

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "summary": {
      "totalVerifications": 15420,
      "successfulVerifications": 15185,
      "successRate": 0.985,
      "averageProcessingTime": 156
    },
    "methodBreakdown": [
      {
        "biometricType": "FACE",
        "totalCount": 8920,
        "successCount": 8795,
        "successRate": 0.986,
        "averageConfidence": 0.94
      },
      {
        "biometricType": "FINGERPRINT",
        "totalCount": 4560,
        "successCount": 4512,
        "successRate": 0.989,
        "averageConfidence": 0.97
      }
    ],
    "devicePerformance": [
      {
        "deviceId": "DEV001",
        "deviceName": "一楼门禁机",
        "totalCount": 5120,
        "successRate": 0.983,
        "averageProcessingTime": 145
      }
    ],
    "qualityMetrics": {
      "imageQualityDistribution": {
        "excellent": 0.65,
        "good": 0.25,
        "fair": 0.08,
        "poor": 0.02
      },
      "failureReasons": [
        {
          "reason": "低质量图像",
          "count": 85,
          "percentage": 0.55
        },
        {
          "reason": "活体检测失败",
          "count": 45,
          "percentage": 0.29
        }
      ]
    }
  }
}
```

## 11. 系统管理接口

### 11.1 系统健康检查

**接口地址**: `GET /api/v1/attendance/health`

**接口描述**: 获取系统健康状态

**响应结果**:
```json
{
  "code": 200,
  "message": "系统正常",
  "data": {
    "overallStatus": "UP",
    "components": [
      {
        "name": "database",
        "status": "UP",
        "details": {
          "connectionPool": "8/20",
          "responseTime": "15ms"
        }
      },
      {
        "name": "redis",
        "status": "UP",
        "details": {
          "memory": "256MB/1GB",
          "connections": 12
        }
      },
      {
        "name": "biometric_engine",
        "status": "UP",
        "details": {
          "activeThreads": 5,
          "queueSize": 2,
          "averageResponseTime": "120ms"
        }
      }
    ],
    "metrics": {
      "uptime": "15天8小时32分钟",
      "totalRequests": 125860,
      "errorRate": 0.002,
      "averageResponseTime": "89ms"
    }
  }
}
```

### 11.2 获取系统配置

**接口地址**: `GET /api/v1/attendance/config`

**接口描述**: 获取系统配置信息

**响应结果**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "systemConfig": {
      "version": "v1.2.0",
      "supportedBiometricTypes": ["FACE", "FINGERPRINT", "PALM_PRINT", "IRIS"],
      "defaultConfidenceThreshold": 0.85,
      "maxFileSize": 10485760,
      "supportedImageFormats": ["JPG", "PNG", "BMP"],
      "sessionTimeout": 3600
    },
    "businessRules": {
      "maxLateTolerance": 30,
      "minClockInterval": 300, // 5分钟
      "maxDailyRecords": 10,
      "overtimeMinDuration": 30 // 30分钟
    },
    "securityConfig": {
      "maxLoginAttempts": 5,
      "passwordMinLength": 8,
      "tokenExpiry": 7200,
      "encryptionEnabled": true
    }
  }
}
```

## 12. WebSocket 实时通信

### 12.1 连接建立

**连接地址**: `wss://api.example.com/ws/attendance`

**认证**: 通过连接参数传递token
```
wss://api.example.com/ws/attendance?token=your_access_token
```

### 12.2 消息格式

**客户端发送消息**:
```json
{
  "type": "SUBSCRIBE",
  "channel": "ATTENDANCE_UPDATE",
  "parameters": {
    "employeeId": 10001
  }
}
```

**服务端推送消息**:
```json
{
  "type": "ATTENDANCE_UPDATE",
  "timestamp": "2025-11-13T10:30:00Z",
  "data": {
    "employeeId": 10001,
    "eventType": "CLOCK_IN",
    "eventData": {
      "recordId": 123456,
      "clockTime": "2025-11-13T09:00:00Z",
      "deviceName": "一楼门禁机",
      "verificationMethod": "人脸识别"
    }
  }
}
```

### 12.3 支持的频道

| 频道名称 | 说明 | 参数 |
|----------|------|------|
| ATTENDANCE_UPDATE | 考勤记录更新 | employeeId, departmentId |
| DEVICE_STATUS | 设备状态变化 | deviceId |
| SYSTEM_ALERT | 系统告警 | alertType |
| BIOMETRIC_RESULT | 生物识别结果 | employeeId, deviceId |

## 13. 接口测试说明

### 13.1 Postman集合

提供完整的Postman测试集合，包含所有API接口的测试用例，支持环境变量配置和自动化测试。

### 13.2 接口调试

- 使用Swagger UI进行接口调试：`/doc.html`
- 支持在线测试和参数验证
- 提供详细的错误信息和建议

### 13.3 性能测试

- 接口响应时间要求：< 200ms (P95)
- 并发处理能力：≥ 1000 TPS
- 系统可用性：≥ 99.9%

---

**注意**:
1. 所有接口都需要通过Sa-Token进行身份验证
2. 生物特征数据传输时使用HTTPS加密
3. 敏感操作需要额外的权限验证
4. 建议在生产环境中实施接口限流和安全防护