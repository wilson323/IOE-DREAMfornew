# IOE-DREAM 考勤管理服务 - API接口文档

## 📋 API概述

**服务名称**: IOE-DREAM 考勤管理服务 API
**服务版本**: v1.0.0
**基础URL**: `https://attendance.ioedream.com/api/v1`
**认证方式**: Bearer Token (JWT)
**数据格式**: JSON
**字符编码**: UTF-8

---

## 🔐 认证授权

### 请求头

所有API请求都需要包含以下头部信息：

```http
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
X-Request-ID: <UUID>
X-Timestamp: <UNIX_TIMESTAMP>
X-Device-ID: <DEVICE_ID>
```

### Token获取

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "your_username",
  "password": "your_password",
  "deviceId": "device_001",
  "deviceType": "WEB"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_here",
    "expiresIn": 7200,
    "userInfo": {
      "userId": 1001,
      "username": "admin",
      "realName": "管理员",
      "departmentName": "技术部",
      "roles": ["ADMIN", "HR"]
    }
  },
  "timestamp": 1703020800000
}
```

---

## 📱 移动端API

### 1. 用户登录

#### 接口描述
移动端用户登录，支持设备信息记录和地理位置验证。

#### 请求地址
```http
POST /api/v1/mobile/login
```

#### 请求参数
**MobileLoginRequest**:
```json
{
  "username": "string",           // 用户名 (必填)
  "password": "string",           // 密码 (必填)
  "deviceId": "string",           // 设备ID (必填)
  "deviceType": "string",         // 设备类型 (必填)
  "deviceModel": "string",        // 设备型号 (选填)
  "osVersion": "string",          // 操作系统版本 (选填)
  "appVersion": "string",         // 应用版本 (选填)
  "pushToken": "string",          // 推送Token (选填)
  "ipAddress": "string",          // IP地址 (选填)
  "deviceInfo": {
    "location": {
      "latitude": 39.9042,        // 纬度 (必填)
      "longitude": 116.4074,      // 经度 (必填)
      "address": "北京市朝阳区",   // 地址 (选填)
      "accuracy": 10.5            // 精度 (选填)
    }
  }
}
```

#### 响应示例
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_here",
    "expiresIn": 7200,
    "userInfo": {
      "userId": 1001,
      "username": "testuser",
      "realName": "张三",
      "departmentName": "技术部",
      "position": "软件工程师",
      "employeeId": "EMP001",
      "avatar": "https://example.com/avatar.jpg",
      "permissions": ["ATTENDANCE_CLOCK_IN", "ATTENDANCE_CLOCK_OUT"]
    }
  },
  "timestamp": 1703020800000
}
```

### 2. 打卡签到

#### 接口描述
移动端打卡签到，支持生物识别验证和位置验证。

#### 请求地址
```http
POST /api/v1/mobile/clock-in
```

#### 请求参数
**MobileClockInRequest**:
```json
{
  "userId": 1001,                // 用户ID (必填)
  "deviceId": "MOBILE_001",       // 设备ID (必填)
  "deviceType": "MOBILE",         // 设备类型 (必填)
  "attendanceType": "IN",         // 考勤类型 (必填: IN/OUT)
  "clockTime": "2025-12-16T09:00:00", // 打卡时间 (必填)
  "location": {
    "latitude": 39.9042,          // 纬度 (必填)
    "longitude": 116.4074,        // 经度 (必填)
    "accuracy": 10.5,             // 精度 (必填)
    "altitude": 50.0,             // 海拔高度 (选填)
    "address": "北京市朝阳区建国门外大街", // 地址 (必填)
    "locationSource": "GPS",      // 位置来源 (必填: GPS/NETWORK)
    "geofenceId": "GEOFENCE_001", // 地理围栏ID (选填)
    "geofenceName": "办公区域",    // 地理围栏名称 (选填)
    "withinGeofence": true        // 是否在围栏内 (必填)
  },
  "biometricData": {
    "faceData": {
      "faceImage": "base64_encoded_face_image",     // 人脸图像 (base64)
      "faceFeatures": "base64_encoded_face_features", // 人脸特征
      "faceBoundingBox": "100,100,200,200",         // 人脸边界框
      "faceLandmarks": "keypoints_json",             // 人脸关键点
      "faceAngle": 0.0,                               // 人脸角度
      "faceSize": "200x200",                          // 人脸大小
      "lightingCondition": "GOOD",                   // 光照条件
      "isFrontal": true,                              // 是否正脸
      "eyeState": "OPEN",                             // 眼睛状态
      "mouthState": "CLOSED"                          // 嘴巴状态
    },
    "biometricType": "FACE",       // 生物识别类型 (必填)
    "confidence": 0.98,           // 置信度 (必填)
    "livenessDetected": true,     // 活体检测结果 (必填)
    "featureVector": "base64_encoded_features", // 特征向量
    "rawData": "base64_encoded_raw_data",           // 原始数据
    "qualityScore": 0.95,        // 数据质量分数 (必填)
    "captureTimestamp": 1703020800000 // 采集时间戳 (必填)
  },
  "remark": "正常上班打卡",       // 备注 (选填)
  "extendedAttributes": {}       // 扩展属性 (选填)
}
```

#### 响应示例
```json
{
  "code": 200,
  "message": "打卡成功",
  "data": {
    "recordId": "REC_20251216_001",
    "clockInTime": "2025-12-16T09:00:00",
    "clockType": "IN",
    "attendanceStatus": "NORMAL",
    "lateMinutes": 0,
    "earlyMinutes": 0,
    "workShiftName": "正常班",
    "workTime": "09:00-18:00",
    "locationValid": true,
    "biometricVerified": true,
    "verificationScore": 0.98,
    "breakRules": [],
    "message": "打卡成功",
    "nextAction": "CLOCK_OUT",
    "nextAllowedTime": "2025-12-16T18:00:00"
  },
  "timestamp": 1703020800000
}
```

### 3. 打卡签退

#### 接口描述
移动端打卡签退。

#### 请求地址
```http
POST /api/v1/mobile/clock-out
```

#### 请求参数
```json
{
  "userId": 1001,
  "deviceId": "MOBILE_001",
  "deviceType": "MOBILE",
  "attendanceType": "OUT",
  "clockTime": "2025-12-16T18:00:00",
  "location": {
    "latitude": 39.9042,
    "longitude": 116.4074,
    "accuracy": 10.5,
    "altitude": 50.0,
    "address": "北京市朝阳区建国门外大街",
    "locationSource": "GPS",
    "geofenceId": "GEOFENCE_001",
    "geofenceName": "办公区域",
    "withinGeofence": true
  },
  "biometricData": {
    "faceData": {
      "faceImage": "base64_encoded_face_image",
      "faceFeatures": "base64_encoded_face_features",
      "faceBoundingBox": "100,100,200,200",
      "faceLandmarks": "keypoints_json",
      "faceAngle": 0.0,
      "faceSize": "200x200",
      "lightingCondition": "GOOD",
      "isFrontal": true,
      "eyeState": "OPEN",
      "mouthState": "CLOSED"
    },
    "biometricType": "FACE",
    "confidence": 0.98,
    "livenessDetected": true,
    "featureVector": "base64_encoded_features",
    "rawData": "base64_encoded_raw_data",
    "qualityScore": 0.95,
    "captureTimestamp": 1703020800000
  },
  "remark": "正常下班打卡",
  "extendedAttributes": {}
}
```

#### 响应示例
```json
{
  "code": 200,
  "message": "签退成功",
  "data": {
    "recordId": "REC_20251216_002",
    "clockOutTime": "2025-12-16T18:00:00",
    "clockType": "OUT",
    "attendanceStatus": "NORMAL",
    "workHours": 8.0,
    "overtimeHours": 0.0,
    "earlyDepartureMinutes": 0,
    "locationValid": true,
    "biometricVerified": true,
    "verificationScore": 0.98,
    "message": "签退成功",
    "dailySummary": {
      "clockInTime": "2025-12-16T09:00:00",
      "clockOutTime": "2025-12-16T18:00:00",
      "totalWorkHours": 8.0,
      "overtimeHours": 0.0,
      "lateMinutes": 0,
      "earlyDepartureMinutes": 0,
      "attendanceStatus": "NORMAL"
    }
  },
  "timestamp": 1703020800000
}
```

### 4. 生物识别验证

#### 接口描述
生物识别验证，支持人脸、指纹、虹膜、声纹等多种生物识别方式。

#### 请求地址
```http
POST /api/v1/mobile/biometric/verify
```

#### 请求参数
**MobileBiometricVerifyRequest**:
```json
{
  "userId": 1001,                     // 用户ID (必填)
  "biometricType": "FACE",            // 生物识别类型 (必填: FACE/FINGERPRINT/IRIS/VOICE)
  "biometricData": {
    "faceData": {
      "faceImage": "base64_encoded_face_image",
      "faceFeatures": "base64_encoded_face_features",
      "faceBoundingBox": "100,100,200,200",
      "faceLandmarks": "keypoints_json",
      "faceAngle": 0.0,
      "faceSize": "200x200",
      "lightingCondition": "GOOD",
      "isFrontal": true,
      "eyeState": "OPEN",
      "mouthState": "CLOSED"
    },
    "fingerprintData": [
      {
        "fingerprintId": "FINGER_001",
        "fingerType": "THUMB",
        "handType": "RIGHT",
        "fingerprintImage": "base64_encoded_fingerprint_image",
        "fingerprintFeatures": "base64_encoded_fingerprint_features",
        "qualityScore": 0.92,
        "pressureLevel": "MEDIUM",
        "contactArea": 0.85,
        "skinCondition": "NORMAL"
      }
    ],
    "irisData": {
      "irisImage": "base64_encoded_iris_image",
      "irisFeatures": "base64_encoded_iris_features",
      "eyeType": "LEFT",
      "pupilDiameter": 4.5,
      "irisRadius": 6.2,
      "illuminationLevel": "NORMAL",
      "imageSharpness": 0.94,
      "hasGlasses": false,
      "glassesType": "NONE"
    },
    "voiceData": {
      "voiceData": "base64_encoded_voice_data",
      "voiceFeatures": "base64_encoded_voice_features",
      "duration": 3.5,
      "sampleRate": 16000,
      "audioQuality": "HIGH",
      "signalToNoiseRatio": 25.5,
      "voiceType": "PHRASE",
      "voiceContent": "请说出你的姓名",
      "volumeLevel": "MEDIUM",
      "speechRate": "NORMAL"
    },
    "featureVector": "base64_encoded_feature_vector",
    "rawData": "base64_encoded_raw_data",
    "qualityScore": 0.95,
    "captureTimestamp": 1703020800000
  },
  "verificationScenario": "CLOCK_IN",  // 验证场景 (选填: CLOCK_IN/CLOCK_OUT/LOGIN/ACCESS)
  "deviceId": "MOBILE_001",           // 设备ID (选填)
  "verificationThreshold": 0.85,       // 验证阈值 (选填)
  "enableLivenessCheck": true,        // 是否启用活体检测 (选填)
  "enableAntiSpoofing": true,         // 是否启用防欺骗检测 (选填)
  "extendedAttributes": {}             // 扩展属性 (选填)
}
```

#### 响应示例
```json
{
  "code": 200,
  "message": "验证成功",
  "data": {
    "success": true,
    "verificationScore": 0.98,
    "biometricType": "FACE",
    "matchedUserId": 1001,
    "matchedTemplateId": "TEMPLATE_001",
    "verificationStatus": "VERIFIED",
    "livenessResult": true,
    "antiSpoofingResult": true,
    "verificationTimeMs": 1250,
    "verificationTimestamp": 1703020800000,
    "detailedResult": {
      "featureMatchScore": 0.98,
      "imageQualityScore": 0.95,
      "livenessScore": 0.99,
      "antiSpoofingScore": 0.97,
      "confidence": "HIGH",
      "qualityAssessment": {
        "overallQuality": "EXCELLENT",
        "clarityScore": 0.94,
        "brightnessScore": 0.88,
        "contrastScore": 0.92,
        "noiseLevel": 0.15,
        "hasOcclusion": false,
        "occlusionType": "NONE",
        "meetsMinimumQuality": true
      },
      "matchingDetails": {
        "templateDistance": 0.05,
        "similarityThreshold": 0.85,
        "candidateCount": 5,
        "bestMatchScore": 0.98,
        "secondBestScore": 0.76,
        "matchDifference": 0.22,
        "matchingAlgorithm": "DEEP_FACE",
        "templateVersion": "v2.1"
      },
      "processingSteps": [
        {
          "stepName": "FACE_DETECTION",
          "stepStatus": "SUCCESS",
          "processingTimeMs": 150,
          "stepDetails": "检测到1张人脸",
          "outputResult": "face_detected=true",
          "errorMessage": ""
        },
        {
          "stepName": "FEATURE_EXTRACTION",
          "stepStatus": "SUCCESS",
          "processingTimeMs": 300,
          "stepDetails": "特征提取完成",
          "outputResult": "features_extracted=true",
          "errorMessage": ""
        },
        {
          "stepName": "TEMPLATE_MATCHING",
          "stepStatus": "SUCCESS",
          "processingTimeMs": 500,
          "stepDetails": "模板匹配成功",
          "outputResult": "match_found=true",
          "errorMessage": ""
        }
      ]
    },
    "recommendation": "验证通过",
    "errorCode": "BIOMETRIC_VERIFICATION_SUCCESS",
    "extendedAttributes": {}
  },
  "timestamp": 1703020800000
}
```

### 5. 考勤状态查询

#### 接口描述
查询用户指定日期的考勤状态和统计数据。

#### 请求地址
```http
GET /api/v1/mobile/attendance/status
```

#### 请求参数
```http
GET /api/v1/mobile/attendance/status?userId=1001&queryDate=2025-12-16&includeDetails=true&includeStatistics=true
```

**参数说明**:
- `userId`: 用户ID (必填)
- `queryDate`: 查询日期 (必填, 格式: YYYY-MM-DD)
- `includeDetails`: 是否包含详细信息 (选填, 默认: false)
- `includeStatistics`: 是否包含统计信息 (选填, 默认: false)

#### 响应示例
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "userId": 1001,
    "queryDate": "2025-12-16",
    "clockInTime": "2025-12-16T09:00:00",
    "clockOutTime": "2025-12-16T18:00:00",
    "attendanceStatus": "NORMAL",
    "workHours": 8.0,
    "overtimeHours": 0.0,
    "lateMinutes": 0,
    "earlyDepartureMinutes": 0,
    "shiftInfo": {
      "shiftId": 1,
      "shiftName": "正常班",
      "workTime": "09:00-18:00",
      "restTime": "12:00-13:00",
      "workDays": "MON-FRI"
    },
    "attendanceRecords": [
      {
        "recordId": "REC_20251216_001",
        "userId": 1001,
        "clockType": "IN",
        "clockTime": "2025-12-16T09:00:00",
        "location": "北京市朝阳区建国门外大街",
        "verificationMethod": "FACE",
        "verificationScore": 0.98,
        "deviceInfo": "MOBILE_001",
        "remark": "正常上班打卡"
      },
      {
        "recordId": "REC_20251216_002",
        "userId": 1001,
        "clockType": "OUT",
        "clockTime": "2025-12-16T18:00:00",
        "location": "北京市朝阳区建国门外大街",
        "verificationMethod": "FACE",
        "verificationScore": 0.97,
        "deviceInfo": "MOBILE_001",
        "remark": "正常下班打卡"
      }
    ],
    "statistics": {
      "monthlyAttendanceDays": 22,
      "monthlyWorkHours": 176.0,
      "monthlyOvertimeHours": 8.0,
      "lateCount": 1,
      "earlyDepartureCount": 0,
      "absenceCount": 0,
      "attendanceRate": 95.5,
      "onTimeRate": 95.5
    }
  },
  "timestamp": 1703020800000
}
```

### 6. 考勤记录列表

#### 接口描述
分页查询用户的考勤记录列表。

#### 请求地址
```http
GET /api/v1/mobile/attendance/records
```

#### 请求参数
```http
GET /api/v1/mobile/attendance/records?userId=1001&startDate=2025-12-01&endDate=2025-12-16&pageNum=1&pageSize=20
```

**参数说明**:
- `userId`: 用户ID (必填)
- `startDate`: 开始日期 (必填, 格式: YYYY-MM-DD)
- `endDate`: 结束日期 (必填, 格式: YYYY-MM-DD)
- `pageNum`: 页码 (选填, 默认: 1)
- `pageSize`: 每页大小 (选填, 默认: 20, 最大: 100)

#### 响应示例
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "pageNum": 1,
    "pageSize": 20,
    "total": 15,
    "pages": 1,
    "list": [
      {
        "recordId": "REC_20251216_001",
        "userId": 1001,
        "clockDate": "2025-12-16",
        "clockType": "IN",
        "clockTime": "2025-12-16T09:00:00",
        "attendanceStatus": "NORMAL",
        "workShiftName": "正常班",
        "location": "北京市朝阳区建国门外大街",
        "verificationMethod": "FACE",
        "verificationScore": 0.98,
        "deviceInfo": "MOBILE_001",
        "lateMinutes": 0,
        "remark": "正常上班打卡"
      },
      {
        "recordId": "REC_20251215_001",
        "userId": 1001,
        "clockDate": "2025-12-15",
        "clockType": "IN",
        "clockTime": "2025-12-15T08:55:00",
        "attendanceStatus": "EARLY",
        "workShiftName": "正常班",
        "location": "北京市朝阳区建国门外大街",
        "verificationMethod": "FACE",
        "verificationScore": 0.97,
        "deviceInfo": "MOBILE_001",
        "lateMinutes": 0,
        "remark": "早到打卡"
      }
    ]
  },
  "timestamp": 1703020800000
}
```

### 7. 获取工作班次

#### 接口描述
获取用户的当天工作班次信息。

#### 请求地址
```http
GET /api/v1/mobile/shift/current
```

#### 请求参数
```http
GET /api/v1/mobile/shift/current?userId=1001&date=2025-12-16
```

**参数说明**:
- `userId`: 用户ID (必填)
- `date`: 查询日期 (必填, 格式: YYYY-MM-DD, 默认: 今天)

#### 响应示例
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "shiftId": 1,
    "shiftName": "正常班",
    "shiftType": "REGULAR",
    "workDate": "2025-12-16",
    "startTime": "2025-12-16T09:00:00",
    "endTime": "2025-12-16T18:00:00",
    "restStartTime": "2025-12-16T12:00:00",
    "restEndTime": "2025-12-16T13:00:00",
    "workHours": 8.0,
    "locationName": "总部大厦",
    "geofenceIds": ["GEOFENCE_001", "GEOFENCE_002"],
    "allowedClockInRange": "08:30-09:30",
    "allowedClockOutRange": "17:30-19:00",
    "specialNotes": "今日有重要会议，请准时到岗",
    "nextAction": "CLOCK_IN",
    "nextAllowedTime": "2025-12-16T08:30:00",
    "isWorkingDay": true,
    "isHoliday": false,
    "isWeekend": false
  },
  "timestamp": 1703020800000
}
```

### 8. 离线数据同步

#### 接口描述
移动端离线数据同步，用于网络恢复后同步离线期间的考勤数据。

#### 请求地址
```http
POST /api/v1/mobile/sync/offline-data
```

#### 请求参数
```json
{
  "userId": 1001,
  "deviceId": "MOBILE_001",
  "syncType": "FULL",              // 同步类型 (FULL/INCREMENTAL)
  "lastSyncTime": "2025-12-15T18:00:00", // 上次同步时间
  "offlineData": [
    {
      "recordType": "CLOCK_IN",     // 记录类型 (CLOCK_IN/CLOCK_OUT)
      "recordTime": "2025-12-15T09:00:00", // 记录时间
      "location": {
        "latitude": 39.9042,
        "longitude": 116.4074,
        "accuracy": 10.5,
        "address": "北京市朝阳区建国门外大街",
        "locationSource": "GPS"
      },
      "biometricData": {
        "faceImage": "base64_encoded_face_image",
        "faceFeatures": "base64_encoded_face_features",
        "qualityScore": 0.95,
        "confidence": 0.98,
        "livenessDetected": true
      },
      "deviceId": "MOBILE_001",
      "remark": "离线打卡记录"
    }
  ]
}
```

#### 响应示例
```json
{
  "code": 200,
  "message": "同步成功",
  "data": {
    "syncId": "SYNC_20251216_001",
    "totalRecords": 5,
    "successRecords": 5,
    "failedRecords": 0,
    "syncStartTime": "2025-12-16T10:00:00",
    "syncEndTime": "2025-12-16T10:00:30",
    "syncDuration": 30000,
    "results": [
      {
        "recordIndex": 1,
        "recordType": "CLOCK_IN",
        "status": "SUCCESS",
        "recordId": "REC_20251215_001",
        "message": "同步成功"
      },
      {
        "recordIndex": 2,
        "recordType": "CLOCK_OUT",
        "status": "SUCCESS",
        "recordId": "REC_20251215_002",
        "message": "同步成功"
      }
    ]
  },
  "timestamp": 1703020800000
}
```

### 9. 设备信息上报

#### 接口描述
移动端设备信息上报，用于设备管理和安全监控。

#### 请求地址
```http
POST /api/v1/mobile/device/report
```

#### 请求参数
```json
{
  "userId": 1001,
  "deviceId": "MOBILE_001",
  "deviceType": "ANDROID",
  "deviceModel": "Samsung Galaxy S21",
  "osVersion": "Android 12",
  "appVersion": "1.0.0",
  "imei": "123456789012345",
  "macAddress": "AA:BB:CC:DD:EE:FF",
  "screenResolution": "1080x2400",
  "storageInfo": {
    "totalStorage": 128000000000,
    "availableStorage": 64000000000,
    "usedStorage": 64000000000
  },
  "batteryInfo": {
    "level": 85,
    "status": "CHARGING",
    "health": "GOOD"
  },
  "networkInfo": {
    "type": "WIFI",
    "strength": "EXCELLENT",
    "operator": "China Mobile"
  },
  "locationInfo": {
    "latitude": 39.9042,
    "longitude": 116.4074,
    "accuracy": 10.5,
    "address": "北京市朝阳区建国门外大街"
  },
  "securityInfo": {
    "isRooted": false,
    "isAppSecured": true,
    "isDeviceEncrypted": true,
    "biometricEnabled": true
  },
  "appUsageStats": {
    "appVersion": "1.0.0",
    "lastUpdateTime": "2025-12-16T10:00:00",
    "totalUsageTime": 3600000,
    "lastActiveTime": "2025-12-16T10:30:00"
  }
}
```

#### 响应示例
```json
{
  "code": 200,
  "message": "设备信息上报成功",
  "data": {
    "deviceId": "MOBILE_001",
    "reportId": "REPORT_20251216_001",
    "status": "ACTIVE",
    "lastReportTime": "2025-12-16T10:00:00",
    "securityStatus": "SAFE",
    "recommendations": [
      "建议更新应用版本到最新版本",
      "建议开启设备定位服务"
    ]
  },
  "timestamp": 1703020800000
}
```

### 10. 获取应用配置

#### 接口描述
获取移动端应用配置，包括功能开关、参数配置等。

#### 请求地址
```http
GET /api/v1/mobile/config
```

#### 请求参数
```http
GET /api/v1/mobile/config?userId=1001&deviceType=MOBILE&appVersion=1.0.0
```

**参数说明**:
- `userId`: 用户ID (必填)
- `deviceType`: 设备类型 (必填)
- `appVersion`: 应用版本 (必填)

#### 响应示例
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "configVersion": "1.0.0",
    "configUpdateTime": "2025-12-16T10:00:00",
    "features": {
      "biometricVerification": {
        "enabled": true,
        "supportedTypes": ["FACE", "FINGERPRINT"],
        "livenessCheck": true,
        "antiSpoofing": true,
        "threshold": 0.85
      },
      "locationVerification": {
        "enabled": true,
        "geofenceEnabled": true,
        "requiredAccuracy": 50,
        "maxDistance": 500
      },
      "offlineMode": {
        "enabled": true,
        "maxOfflineRecords": 100,
        "autoSync": true,
        "syncInterval": 300
      },
      "realTimeNotification": {
        "enabled": true,
        "pushEnabled": true,
        "notificationTypes": ["REMINDER", "ALERT", "SYSTEM"]
      }
    },
    "businessRules": {
      "clockInTimeWindow": {
        "startTime": "08:30",
        "endTime": "10:00",
        "allowEarly": true,
        "allowLate": true,
        "maxLateMinutes": 30
      },
      "clockOutTimeWindow": {
        "startTime": "17:00",
        "endTime": "20:00",
        "allowEarly": true,
        "allowOvertime": true
      },
      "attendanceSettings": {
        "autoDeduction": false,
        "restTimeIncluded": true,
        "overtimeCalculation": "EXCLUDE_REST"
      }
    },
    "uiSettings": {
      "theme": "LIGHT",
      "language": "zh_CN",
      "dateFormat": "YYYY-MM-DD",
      "timeFormat": "24H",
      "locationDisplay": true
    },
    "securitySettings": {
      "sessionTimeout": 7200,
      "maxLoginAttempts": 5,
      "lockoutDuration": 900,
      "requireBiometric": false,
      "encryptionEnabled": true
    },
    "networkSettings": {
      "apiEndpoint": "https://attendance.ioedream.com/api/v1",
      "timeout": 10000,
      "retryAttempts": 3,
      "retryDelay": 1000
    },
    "updateSettings": {
      "autoUpdate": true,
      "checkInterval": 86400,
      "allowBetaUpdates": false,
      "mandatoryUpdate": false
    }
  },
  "timestamp": 1703020800000
}
```

---

## 🏢 管理端API

### 1. 考勤记录查询

#### 接口描述
分页查询考勤记录，支持多条件筛选。

#### 请求地址
```http
GET /api/v1/attendance/records
```

#### 请求参数
```http
GET /api/v1/attendance/records?userId=1001&startDate=2025-12-01&endDate=2025-12-16&status=NORMAL&pageNum=1&pageSize=20
```

**参数说明**:
- `userId`: 用户ID (选填)
- `departmentId`: 部门ID (选填)
- `startDate`: 开始日期 (必填)
- `endDate`: 结束日期 (必填)
- `status`: 考勤状态 (选填: NORMAL/LATE/EARLY/ABSENCE/OVERTIME)
- `pageNum`: 页码 (选填, 默认: 1)
- `pageSize`: 每页大小 (选填, 默认: 20)

#### 响应示例
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "pageNum": 1,
    "pageSize": 20,
    "total": 150,
    "pages": 8,
    "list": [
      {
        "recordId": "REC_20251216_001",
        "userId": 1001,
        "username": "张三",
        "realName": "张三",
        "departmentName": "技术部",
        "clockDate": "2025-12-16",
        "clockType": "IN",
        "clockTime": "2025-12-16T09:00:00",
        "attendanceStatus": "NORMAL",
        "workShiftName": "正常班",
        "location": "北京市朝阳区建国门外大街",
        "verificationMethod": "FACE",
        "verificationScore": 0.98,
        "deviceInfo": "MOBILE_001",
        "lateMinutes": 0,
        "earlyMinutes": 0,
        "overtimeHours": 0.0,
        "remark": "正常上班打卡"
      }
    ]
  },
  "timestamp": 1703020800000
}
```

### 2. 考勤统计报表

#### 接口描述
生成考勤统计报表，支持个人、部门、公司等维度统计。

#### 请求地址
```http
POST /api/v1/attendance/reports/statistics
```

#### 请求参数
```json
{
  "reportType": "DEPARTMENT",        // 报表类型 (PERSON/DEPARTMENT/COMPANY)
  "departmentId": 1,               // 部门ID (当reportType=DEPARTMENT时必填)
  "userId": 1001,                   // 用户ID (当reportType=PERSON时必填)
  "startDate": "2025-12-01",         // 开始日期 (必填)
  "endDate": "2025-12-16",           // 结束日期 (必填)
  "groupBy": "DAY",                 // 分组方式 (DAY/WEEK/MONTH)
  "includeDetails": true,            // 是否包含详细信息
  "format": "JSON"                  // 输出格式 (JSON/EXCEL/PDF)
}
```

#### 响应示例
```json
{
  "code": 200,
  "message": "报表生成成功",
  "data": {
    "reportId": "REPORT_20251216_001",
    "reportType": "DEPARTMENT",
    "reportPeriod": "2025-12-01至2025-12-16",
    "generatedTime": "2025-12-16T10:00:00",
    "summary": {
      "totalEmployees": 25,
      "workingDays": 12,
      "totalAttendanceDays": 280,
      "totalWorkHours": 2240.0,
      "averageWorkHours": 8.0,
      "attendanceRate": 95.2,
      "onTimeRate": 92.5,
      "lateCount": 15,
      "earlyDepartureCount": 8,
      "absenceCount": 5,
      "overtimeHours": 120.0
    },
    "departmentStats": [
      {
        "departmentId": 1,
        "departmentName": "技术部",
        "employeeCount": 10,
        "attendanceDays": 110,
        "workHours": 880.0,
        "attendanceRate": 96.5,
        "onTimeRate": 94.0,
        "lateCount": 6,
        "earlyDepartureCount": 3,
        "absenceCount": 2,
        "overtimeHours": 56.0
      }
    ],
    "dailyStats": [
      {
        "date": "2025-12-16",
        "workingEmployees": 23,
        "attendanceRate": 92.0,
        "onTimeRate": 87.0,
        "averageWorkHours": 8.2,
        "overtimeHours": 12.0
      }
    ],
    "details": [
      {
        "userId": 1001,
        "realName": "张三",
        "departmentName": "技术部",
        "position": "软件工程师",
        "attendanceDays": 11,
        "totalWorkHours": 88.0,
        "attendanceRate": 91.7,
        "onTimeRate": 90.9,
        "lateCount": 1,
        "earlyDepartureCount": 0,
        "absenceCount": 1,
        "overtimeHours": 8.0
      }
    ]
  },
  "timestamp": 1703020800000
}
```

### 3. 工作班次管理

#### 接口描述
管理工作班次，包括创建、更新、删除、查询等操作。

#### 创建班次
```http
POST /api/v1/attendance/shifts
```

**请求参数**:
```json
{
  "shiftName": "正常班",
  "shiftType": "REGULAR",
  "startTime": "09:00",
  "endTime": "18:00",
  "restStartTime": "12:00",
  "restEndTime": "13:00",
  "workDays": ["MON", "TUE", "WED", "THU", "FRI"],
  "description": "正常工作班次",
  "effectiveDate": "2025-12-01",
  "expiryDate": "2025-12-31",
  "departmentIds": [1, 2, 3],
  "locationIds": [1],
  "flexibleSettings": {
    "enabled": false,
    "earlyStartTime": "08:00",
    "lateEndTime": "10:00"
  }
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "班次创建成功",
  "data": {
    "shiftId": 1,
    "shiftName": "正常班",
    "shiftType": "REGULAR",
    "startTime": "09:00",
    "endTime": "18:00",
    "workHours": 8.0,
    "status": "ACTIVE"
  },
  "timestamp": 1703020800000
}
```

### 4. 排班管理

#### 接口描述
智能排班管理，支持自动排班和手动调整。

#### 自动排班
```http
POST /api/v1/attendance/scheduling/auto
```

**请求参数**:
```json
{
  "departmentId": 1,
  "startDate": "2025-12-01",
  "endDate": "2025-12-31",
  "algorithm": "GENETIC",          // 算法类型 (GENETIC/GREEDY/BACKTRACK)
  "constraints": {
    "maxConsecutiveDays": 5,
    "minRestDays": 2,
    "maxWeeklyHours": 48,
    "requiredEmployees": 10,
    "skillRequirements": ["SKILL_001", "SKILL_002"]
  },
  "preferences": {
    "fairness": true,
    "balanceWorkload": true,
    "considerEmployeePreferences": true,
    "minimizeChanges": true
  }
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "自动排班成功",
  "data": {
    "scheduleId": "SCHEDULE_20251216_001",
    "totalEmployees": 25,
    "totalDays": 22,
    "totalShifts": 550,
    "coverage": 98.5,
    "fairnessScore": 0.92,
    "optimizationScore": 0.89,
    "schedules": [
      {
        "employeeId": 1001,
        "employeeName": "张三",
        "date": "2025-12-16",
        "shiftId": 1,
        "shiftName": "正常班",
        "startTime": "09:00",
        "endTime": "18:00",
        "workHours": 8.0,
        "location": "总部大厦"
      }
    ],
    "conflicts": [
      {
        "type": "OVERLAP",
        "description": "员工张三在2025-12-16有排班冲突",
        "severity": "MEDIUM",
        "suggestion": "手动调整排班"
      }
    ]
  },
  "timestamp": 1703020800000
}
```

---

## ⚡ 实时计算API

### 1. 事件处理

#### 接口描述
处理考勤相关的实时事件。

#### 请求地址
```http
POST /api/v1/realtime/events
```

#### 请求参数
```json
{
  "eventId": "EVENT_20251216_001",
  "eventType": "CLOCK_IN",
  "eventTime": "2025-12-16T09:00:00",
  "userId": 1001,
  "deviceId": "MOBILE_001",
  "location": {
    "latitude": 39.9042,
    "longitude": 116.4074,
    "address": "北京市朝阳区建国门外大街"
  },
  "biometricData": {
    "verificationScore": 0.98,
    "livenessDetected": true,
    "antiSpoofingPassed": true
  },
  "attributes": {
    "verificationMethod": "FACE",
    "geofenceValid": true,
    "withinAllowedTime": true
  }
}
```

#### 响应示例
```json
{
  "code": 200,
  "message": "事件处理成功",
  "data": {
    "eventId": "EVENT_20251216_001",
    "processTime": 150,
    "calculationResult": {
      "attendanceStatus": "NORMAL",
      "lateMinutes": 0,
      "earlyMinutes": 0,
      "overtimeHours": 0.0,
      "validLocation": true,
      "validTime": true,
      "validBiometric": true
    },
    "triggeredActions": [
      {
        "actionType": "NOTIFICATION",
        "target": "MANAGER",
        "message": "张三已准时打卡上班"
      },
      {
        "actionType": "UPDATE_DASHBOARD",
        "target": "HR_SYSTEM",
        "data": "attendance_update"
      }
    ]
  },
  "timestamp": 1703020800000
}
```

### 2. 规则执行

#### 接口描述
执行考勤规则引擎验证。

#### 请求地址
```http
POST /api/v1/rules/execute
```

#### 请求参数
```json
{
  "ruleChain": "ATTENDANCE_VALIDATION",
  "context": {
    "userId": 1001,
    "eventId": "EVENT_20251216_001",
    "eventType": "CLOCK_IN",
    "clockTime": "2025-12-16T09:00:00",
    "location": {
      "latitude": 39.9042,
      "longitude": 116.4074,
      "geofenceId": "GEOFENCE_001"
    },
    "shiftInfo": {
      "shiftId": 1,
      "startTime": "09:00",
      "endTime": "18:00"
    },
    "biometricScore": 0.98
  }
}
```

#### 响应示例
```json
{
  "code": 200,
  "message": "规则执行完成",
  "data": {
    "ruleChain": "ATTENDANCE_VALIDATION",
    "executionTime": 250,
    "result": {
      "passed": true,
      "score": 0.95,
      "results": [
        {
          "ruleName": "LOCATION_VALIDATION",
          "passed": true,
          "score": 1.0,
          "details": "位置验证通过"
        },
        {
          "ruleName": "TIME_VALIDATION",
          "passed": true,
          "score": 1.0,
          "details": "时间验证通过"
        },
        {
          "ruleName": "BIOMETRIC_VALIDATION",
          "passed": true,
          "score": 0.85,
          "details": "生物识别验证通过"
        }
      ]
    },
    "recommendations": [],
    "violations": []
  },
  "timestamp": 1703020800000
}
```

---

## 🔧 系统管理API

### 1. 健康检查

#### 接口描述
检查系统健康状态和性能指标。

#### 请求地址
```http
GET /api/v1/system/health
```

#### 响应示例
```json
{
  "code": 200,
  "message": "系统健康",
  "data": {
    "status": "UP",
    "timestamp": 1703020800000,
    "components": {
      "database": {
        "status": "UP",
        "details": {
          "connectionPool": {
            "active": 5,
            "idle": 15,
            "total": 20,
            "max": 50
          },
          "responseTime": 15
        }
      },
      "redis": {
        "status": "UP",
        "details": {
          "connectionPool": {
            "active": 2,
            "idle": 8,
            "total": 10,
            "max": 20
          },
          "responseTime": 5
        }
      },
      "rabbitmq": {
        "status": "UP",
        "details": {
          "connection": "ESTABLISHED",
          "queues": 3,
          "messages": 0
        }
      },
      "nacos": {
        "status": "UP",
        "details": {
          "connection": "ESTABLISHED",
          "services": 7
        }
      }
    },
    "metrics": {
      "jvm": {
        "memory": {
          "used": "1.2GB",
          "max": "2.0GB",
          "usagePercent": 60
        },
        "gc": {
          "youngGC": {
            "count": 15,
            "time": 45
          },
          "oldGC": {
            "count": 2,
            "time": 120
          }
        }
      },
      "threads": {
        "active": 25,
        "pool": 50,
        "daemon": 15
      },
      "cpu": {
        "usagePercent": 35
      }
    }
  },
  "timestamp": 1703020800000
}
```

### 2. 性能监控

#### 接口描述
获取系统性能监控指标。

#### 请求地址
```http
GET /api/v1/system/metrics
```

#### 请求参数
```http
GET /api/v1/system/metrics?startTime=2025-12-16T09:00:00&endTime=2025-12-16T10:00:00&granularity=MINUTE
```

**参数说明**:
- `startTime`: 开始时间 (必填)
- `endTime`: 结束时间 (必填)
- `granularity`: 时间粒度 (选填: MINUTE/HOUR/DAY, 默认: MINUTE)

#### 响应示例
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "timeRange": {
      "startTime": "2025-12-16T09:00:00",
      "endTime": "2025-12-16T10:00:00",
      "granularity": "MINUTE"
    },
    "metrics": {
      "request": {
        "totalRequests": 1250,
        "successRequests": 1230,
        "errorRequests": 20,
        "averageResponseTime": 120,
        "p50ResponseTime": 100,
        "p95ResponseTime": 180,
        "p99ResponseTime": 250
      },
      "business": {
        "clockInCount": 45,
        "clockOutCount": 42,
        "biometricVerificationCount": 87,
        "locationVerificationCount": 90,
        "averageVerificationScore": 0.94
      },
      "resource": {
        "cpu": {
          "average": 45,
          "peak": 78,
          "min": 25
        },
        "memory": {
          "average": 65,
          "peak": 85,
          "min": 55
        },
        "database": {
          "averageConnections": 12,
          "peakConnections": 25,
          "queryAverageTime": 15
        },
        "cache": {
          "hitRate": 92.5,
          "missRate": 7.5,
          "evictions": 5
        }
      }
    }
  },
  "timestamp": 1703020800000
}
```

---

## 📊 错误码说明

### 通用错误码

| 错误码 | 说明 | HTTP状态码 |
|--------|------|-----------|
| 200 | 成功 | 200 |
| 400 | 请求参数错误 | 400 |
| 401 | 未授权 | 401 |
| 403 | 禁止访问 | 403 |
| 404 | 资源不存在 | 404 |
| 405 | 请求方法不允许 | 405 |
| 409 | 资源冲突 | 409 |
| 429 | 请求过于频繁 | 429 |
| 500 | 服务器内部错误 | 500 |
| 503 | 服务不可用 | 503 |

### 业务错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 1001 | 用户名或密码错误 | 检查用户名密码 |
| 1002 | 用户已被禁用 | 联系管理员 |
| 1003 | 用户不存在 | 检查用户ID |
| 1004 | Token已过期 | 重新登录 |
| 1005 | Token无效 | 重新获取Token |
| 2001 | 生物识别验证失败 | 重新进行生物识别 |
| 2002 | 生物识别质量低 | 重新采集生物特征 |
| 2003 | 生物识别模板不存在 | 注册生物特征 |
| 2004 | 生物识别活体检测失败 | 检查活体检测设置 |
| 3001 | 位置验证失败 | 检查设备定位权限 |
| 3002 | 不在地理围栏内 | 移动到指定区域 |
| 3003 | 位置精度不足 | 改善GPS信号 |
| 4001 | 考勤记录已存在 | 避免重复打卡 |
| 4002 | 不在允许打卡时间 | 检查班次时间 |
| 4003 | 班次信息不存在 | 检查排班配置 |
| 4004 | 超出打卡次数限制 | 检查打卡规则 |
| 5001 | 数据库连接失败 | 检查数据库状态 |
| 5002 | 缓存服务不可用 | 检查Redis状态 |
| 5003 | 消息队列异常 | 检查RabbitMQ状态 |
| 5004 | 文件上传失败 | 检查文件权限 |

---

## 🔗 接口限流说明

### 限流策略

| 接口类型 | 限流规则 | 时间窗口 |
|---------|---------|----------|
| 登录接口 | 5次/分钟 | 1分钟 |
| 打卡接口 | 10次/分钟 | 1分钟 |
| 生物识别 | 20次/分钟 | 1分钟 |
| 查询接口 | 100次/分钟 | 1分钟 |
| 报表生成 | 10次/小时 | 1小时 |
| 数据同步 | 50次/小时 | 1小时 |

### 限流响应

```json
{
  "code": 429,
  "message": "请求过于频繁，请稍后重试",
  "data": {
    "limit": 5,
    "remaining": 0,
    "resetTime": 1703020860000,
    "retryAfter": 60
  },
  "timestamp": 1703020800000
}
```

---

## 📝 接口版本说明

### 版本管理

- **当前版本**: v1.0.0
- **版本策略**: 语义化版本控制 (主版本.次版本.修订版本)
- **兼容性**: 向后兼容，新版本保持旧版本接口可用性

### 版本更新

- **主版本更新**: 不兼容的API修改
- **次版本更新**: 向后兼容的功能性新增
- **修订版本更新**: 向后兼容的问题修正

### 版本标识

```http
GET /api/v1/version
```

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "apiVersion": "v1.0.0",
    "buildTime": "2025-12-16T10:00:00",
    "gitCommit": "abc123def456",
    "environment": "production",
    "supportVersions": ["v1.0.0"],
    "deprecatedVersions": [],
    "discontinuedVersions": ["v0.9.0"]
  },
  "timestamp": 1703020800000
}
```

---

## 📞 技术支持

### 联系方式

- **技术支持邮箱**: support@ioedream.com
- **API文档更新**: https://docs.ioedream.com/api/attendance
- **开发者社区**: https://community.ioedream.com

### 问题反馈

如果您在使用API过程中遇到问题，请提供以下信息：

1. **API接口**: 具体的接口地址和请求参数
2. **请求时间**: 问题发生的时间
3. **错误信息**: 完整的错误响应信息
4. **环境信息**: 调用环境（测试/生产）
5. **重现步骤**: 详细的问题重现步骤

### 更新日志

#### v1.0.0 (2025-12-16)
- ✅ 新增移动端考勤API接口
- ✅ 新增生物识别验证接口
- ✅ 新增位置验证接口
- ✅ 新增离线数据同步接口
- ✅ 新增实时计算事件接口
- ✅ 新增规则引擎执行接口
- ✅ 完善系统管理和监控接口

---

**📅 文档更新时间**: 2025年12月16日
**📝 文档维护**: IOE-DREAM API团队
**🔄 版本**: v1.0.0
**📞 联系方式**: support@ioedream.com