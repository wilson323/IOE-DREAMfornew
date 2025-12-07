# 访客管理模块前端API接口设计

## 概述

本文档详细描述了IOE-DREAM智能访客管理系统的完整前端API接口设计，包括Web端和移动端的全功能接口支持。系统提供访客预约、身份验证、访客登记、实时通知等核心功能，支持多种验证方式和审批流程。

### 技术架构
- **API协议**: RESTful API + WebSocket实时通信
- **认证方式**: Sa-Token + JWT
- **数据格式**: JSON
- **响应编码**: UTF-8
- **实时通信**: WebSocket双向消息推送
- **生物识别**: 人脸识别、指纹识别
- **短信服务**: 阿里云短信、腾讯云短信

## API 基础配置

### 请求头配置
```http
Content-Type: application/json
Authorization: Bearer ${sa-token}
X-Client-Type: ${client_type} # web/mobile/mini-program/self-service
X-Device-Id: ${device_id} # 设备唯一标识
X-Platform-Version: ${version}
X-Validation-Type: ${validation_type} # FACE_FINGERPRINT_QR_CODE_PASSWORD
```

### 通用响应格式
```json
{
  "code": 200,
  "message": "success",
  "timestamp": 1640995200000,
  "data": {},
  "pagination": {
    "current": 1,
    "size": 20,
    "total": 100,
    "pages": 5
  },
  "extra": {}
}
```

### 错误码定义
| 错误码 | 说明 | HTTP状态码 |
|--------|------|-----------|
| 40100 | 未登录或token失效 | 401 |
| 40300 | 权限不足 | 403 |
| 40400 | 资源不存在 | 404 |
| 40900 | 访客时间冲突 | 409 |
| 42300 | 访客配额已满 | 423 |
| 42900 | 请求过于频繁 | 429 |
| 50000 | 服务器内部错误 | 500 |
| 70101 | 访客不存在 | 400 |
| 70102 | 访客已过期 | 400 |
| 70103 | 验证失败 | 400 |
| 70104 | 预约时间冲突 | 400 |
| 70105 | 访客被禁止 | 400 |
| 70106 | 生物特征不匹配 | 400 |

## 1. 访客预约
## 📋 IOE-DREAM七微服务架构

**核心架构组成**:
- **Gateway Service (8080)**: API网关
- **Common Service (8088)**: 公共模块微服务
- **DeviceComm Service (8087)**: 设备通讯微服务
- **OA Service (8089)**: OA微服务
- **Access Service (8090)**: 门禁服务
- **Attendance Service (8091)**: 考勤服务
- **Video Service (8092)**: 视频服务
- **Consume Service (8094)**: 消费服务
- **Visitor Service (8095)**: 访客服务

**架构特点**:
- 基于Spring Boot 3.5.8 + Java 17
- 严格遵循企业级微服务规范
- 支持高并发、高可用、水平扩展

**技术栈标准**:
- **数据库**: MySQL 8.0 + Druid连接池
- **缓存**: Redis + Caffeine多级缓存
- **注册中心**: Nacos
- **配置中心**: Nacos Config
- **认证授权**: Sa-Token

## 🏗️ 四层架构规范

**标准架构模式**:
```
Controller (接口控制层)
    ↓
Service (核心业务层)
    ↓
Manager (流程管理层)
    ↓
DAO (数据访问层)
```

**层级职责**:
- **Controller层**: HTTP请求处理、参数验证、权限控制
- **Service层**: 核心业务逻辑、事务管理、业务规则验证
- **Manager层**: 复杂流程编排、多数据组装、第三方服务集成
- **DAO层**: 数据库CRUD操作、SQL查询实现、数据访问边界

**严格禁止跨层访问**: Controller不能直接调用Manager/DAO！
### 1.1 创建访客预约
## ⚠️ IOE-DREAM零容忍规则（强制执行）

**必须遵守的架构规则**:
- ✅ **必须使用 @Resource 注入依赖**
- ✅ **必须使用 @Mapper 注解** (禁止@Repository)
- ✅ **必须使用 Dao 后缀** (禁止Repository)
- ✅ **必须使用 @RestController 注解**
- ✅ **必须使用 @Valid 参数校验**
- ✅ **必须返回统一ResponseDTO格式**
- ✅ **必须遵循四层架构边界**

**严格禁止事项**:
- ❌ **禁止使用 @Autowired 注入**
- ❌ **禁止使用 @Repository 注解**
- ❌ **禁止使用 Repository 后缀命名**
- ❌ **禁止跨层访问**
- ❌ **禁止在Controller中包含业务逻辑**
- ❌ **禁止直接访问数据库**

**违规后果**: P0级问题，立即修复，禁止合并！
```http
POST /api/v1/visitor/appointment/create
```

**请求参数:**
```json
{
  "visitorInfo": {
    "name": "李四",
    "phone": "13800138001",
    "email": "lisi@company.com",
    "company": "ABC科技有限公司",
    "position": "技术总监",
    "idCardType": "ID_CARD", // ID_CARD身份证, PASSPORT护照, OTHER其他
    "idCardNumber": "310101199001011234",
    "photoUrl": "https://api.ioe-dream.com/visitor/photos/001.jpg",
    "faceFeatures": "BASE64_FACE_FEATURES",
    "fingerprintData": "BASE64_FINGERPRINT_DATA"
  },
  "appointmentInfo": {
    "appointmentType": "BUSINESS", // BUSINESS商务, INTERVIEW面试, MAINTENANCE维修, DELIVERY送货
    "purpose": "技术方案讨论",
    "visitDate": "2024-01-15",
    "startTime": "2024-01-15 09:00:00",
    "endTime": "2024-01-15 12:00:00",
    "expectedDuration": 180, // 预计停留时长（分钟）
    "visitType": "SINGLE", // SINGLE单次, RECURRING重复
    "recurrencePattern": null
  },
  "hostInfo": {
    "hostId": 1001,
    "hostName": "张三",
    "hostPhone": "13800138000",
    "hostDepartment": "技术研发部",
    "hostPosition": "技术经理"
  },
  "accessInfo": {
    "accessAreas": ["A区", "B区"], // 可访问区域
    "needEscort": false, // 是否需要陪同
    "allowedDevices": ["GATE_001", "DOOR_003"], // 允许的门禁设备
    "accessLevel": "NORMAL" // NORMAL普通, VIP重要, RESTRICTED受限
  },
  "contactInfo": {
    "emergencyContact": "王五",
    "emergencyPhone": "13800138002",
    "relationship": "同事"
  },
  "notes": "讨论AI项目合作事宜",
  "attachments": [
    {
      "fileType": "DOCUMENT",
      "fileName": "合作意向书.pdf",
      "fileUrl": "https://api.ioe-dream.com/visitor/attachments/001.pdf",
      "fileSize": 1024567
    }
  ],
  "validationMethods": ["FACE_RECOGNITION", "PHONE_SMS"] // 验证方式
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "访客预约创建成功",
  "data": {
    "appointmentId": 20240115001,
    "appointmentNo": "VISIT-20240115-001",
    "status": "PENDING_APPROVAL", // PENDING_APPROVAL待审批, APPROVED已批准, REJECTED已拒绝, CANCELLED已取消, COMPLETED已完成
    "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
    "visitCode": "VC202401150001",
    "validFrom": "2024-01-15 08:00:00",
    "validTo": "2024-01-15 18:00:00",
    "createTime": "2024-01-10 10:30:00",
    "nextApprovalStep": {
      "step": 1,
      "stepName": "部门经理审批",
      "approverId": 2001,
      "approverName": "部门经理李经理",
      "estimatedApprovalTime": "2024-01-10 17:00:00"
    }
  }
}
```

### 1.2 获取预约列表
```http
GET /api/v1/visitor/appointment/list
```

**查询参数:**
```
status=PENDING_APPROVAL,APPROVED,COMPLETED,CANCELLED
hostId=1001
startDate=2024-01-01
endDate=2024-01-31
appointmentType=BUSINESS
visitorPhone=13800138001
page=1
size=20
sortField=createTime
sortOrder=desc
```

**响应数据:**
```json
{
  "code": 200,
  "data": [
    {
      "appointmentId": 20240115001,
      "appointmentNo": "VISIT-20240115-001",
      "status": "APPROVED",
      "visitorInfo": {
        "name": "李四",
        "phone": "13800138001",
        "company": "ABC科技有限公司",
        "photoUrl": "https://api.ioe-dream.com/visitor/photos/001.jpg"
      },
      "appointmentInfo": {
        "appointmentType": "BUSINESS",
        "purpose": "技术方案讨论",
        "visitDate": "2024-01-15",
        "startTime": "2024-01-15 09:00:00",
        "endTime": "2024-01-15 12:00:00"
      },
      "hostInfo": {
        "hostId": 1001,
        "hostName": "张三",
        "hostDepartment": "技术研发部"
      },
      "approvalInfo": {
        "finalApprover": "李经理",
        "approvedTime": "2024-01-10 16:45:00",
        "approvalNote": "同意访问"
      },
      "accessRecord": {
        "checkInTime": null,
        "checkOutTime": null,
        "actualDuration": null
      }
    }
  ]
}
```

### 1.3 更新预约状态
```http
POST /api/v1/visitor/appointment/{appointmentId}/status
```

**请求参数:**
```json
{
  "status": "APPROVED", // APPROVED批准, REJECTED拒绝, CANCELLED取消
  "reason": "经核实，访客信息真实有效，同意访问",
  "operatorInfo": {
    "operatorId": 1001,
    "operatorName": "张三",
    "operatorRole": "部门经理"
  },
  "conditions": [
    {
      "condition": "ESCORT_REQUIRED",
      "description": "需要全程陪同"
    },
    {
      "condition": "LIMITED_AREA",
      "description": "仅限技术研发部区域"
    }
  ]
}
```

### 1.4 取消预约
```http
POST /api/v1/visitor/appointment/{appointmentId}/cancel
```

**请求参数:**
```json
{
  "cancelReason": "会议取消",
  "cancelType": "HOST_REQUEST", // HOST_REQUEST接待方取消, VISITOR_REQUEST访客取消
  "requesterInfo": {
    "requesterId": 1001,
    "requesterName": "张三",
    "requesterRole": "接待人"
  },
  "notifyVisitor": true
}
```

### 1.5 获取预约详情
```http
GET /api/v1/visitor/appointment/{appointmentId}
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "appointmentId": 20240115001,
    "appointmentNo": "VISIT-20240115-001",
    "status": "APPROVED",
    "visitorInfo": {
      "visitorId": 5001,
      "name": "李四",
      "phone": "13800138001",
      "email": "lisi@company.com",
      "company": "ABC科技有限公司",
      "position": "技术总监",
      "idCardType": "ID_CARD",
      "idCardNumber": "310101199001011234",
      "photoUrl": "https://api.ioe-dream.com/visitor/photos/001.jpg",
      "faceFeatures": "BASE64_FACE_FEATURES",
      "fingerprintData": null,
      "visitCount": 3, // 历史访问次数
      "lastVisitDate": "2024-01-08"
    },
    "appointmentInfo": {
      "appointmentType": "BUSINESS",
      "purpose": "技术方案讨论",
      "visitDate": "2024-01-15",
      "startTime": "2024-01-15 09:00:00",
      "endTime": "2024-01-15 12:00:00",
      "expectedDuration": 180,
      "visitType": "SINGLE",
      "createdTime": "2024-01-10 10:30:00",
      "updatedTime": "2024-01-10 16:45:00"
    },
    "hostInfo": {
      "hostId": 1001,
      "hostName": "张三",
      "hostPhone": "13800138000",
      "hostDepartment": "技术研发部",
      "hostPosition": "技术经理",
      "hostEmail": "zhangsan@company.com"
    },
    "approvalInfo": {
      "workflow": [
        {
          "step": 1,
          "stepName": "部门经理审批",
          "approverId": 2001,
          "approverName": "李经理",
          "status": "APPROVED",
          "approvalTime": "2024-01-10 15:30:00",
          "comment": "同意访问"
        },
        {
          "step": 2,
          "stepName": "前台确认",
          "approverId": 3001,
          "approverName": "前台小李",
          "status": "APPROVED",
          "approvalTime": "2024-01-10 16:45:00",
          "comment": "已确认访客信息"
        }
      ],
      "finalStatus": "APPROVED",
      "finalApprover": "前台小李",
      "finalApprovalTime": "2024-01-10 16:45:00"
    },
    "accessInfo": {
      "accessAreas": ["A区", "B区"],
      "needEscort": false,
      "allowedDevices": ["GATE_001", "DOOR_003"],
      "accessLevel": "NORMAL",
      "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
      "visitCode": "VC202401150001",
      "validFrom": "2024-01-15 08:00:00",
      "validTo": "2024-01-15 18:00:00"
    },
    "accessRecord": {
      "checkInTime": null,
      "checkOutTime": null,
      "actualDuration": null,
      "accessLogs": [],
      "violations": []
    },
    "contactInfo": {
      "emergencyContact": "王五",
      "emergencyPhone": "13800138002",
      "relationship": "同事"
    },
    "notes": "讨论AI项目合作事宜",
    "attachments": [
      {
        "fileId": 1001,
        "fileName": "合作意向书.pdf",
        "fileUrl": "https://api.ioe-dream.com/visitor/attachments/001.pdf",
        "fileSize": 1024567,
        "uploadTime": "2024-01-10 10:35:00"
      }
    ]
  }
}
```

## 2. 访客验证

### 2.1 人脸识别验证
```http
POST /api/v1/visitor/verify/face
```

**请求参数:**
```json
{
  "visitCode": "VC202401150001",
  "faceImage": "BASE64_ENCODED_FACE_IMAGE",
  "faceFeatures": "BASE64_FACE_FEATURES",
  "deviceInfo": {
    "deviceId": "FACE_KIOSK_001",
    "deviceName": "大门人脸识别终端",
    "deviceLocation": "公司大门"
  },
  "validationInfo": {
    "confidence": 0.85,
    "livenessCheck": true,
    "antiSpoofing": true
  },
  "timestamp": "2024-01-15 09:00:00"
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "验证成功",
  "data": {
    "verificationId": "VF_20240115_001",
    "visitCode": "VC202401150001",
    "verificationResult": "SUCCESS",
    "matchInfo": {
      "matchScore": 0.95,
      "visitorId": 5001,
      "visitorName": "李四",
      "company": "ABC科技有限公司",
      "visitPurpose": "技术方案讨论",
      "hostName": "张三",
      "expectedDuration": 180
    },
    "accessInfo": {
      "checkInTime": "2024-01-15 09:00:05",
      "validUntil": "2024-01-15 12:00:00",
      "accessAreas": ["A区", "B区"],
      "needEscort": false,
      "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
    },
    "deviceInfo": {
      "deviceId": "FACE_KIOSK_001",
      "deviceName": "大门人脸识别终端",
      "location": {
        "latitude": 31.2304,
        "longitude": 121.4737,
        "address": "公司大门"
      }
    },
    "verificationDetails": {
      "faceDetection": {
        "detected": true,
        "faceBoundingBox": {
          "x": 120,
          "y": 80,
          "width": 100,
          "height": 120
        }
      },
      "livenessCheck": {
        "isLive": true,
        "livenessScore": 0.92,
        "antiSpoofingPassed": true
      }
    }
  }
}
```

### 2.2 二维码验证
```http
POST /api/v1/visitor/verify/qrcode
```

**请求参数:**
```json
{
  "visitCode": "VC202401150001",
  "qrCodeData": "QR_CODE_SCAN_DATA",
  "deviceInfo": {
    "deviceId": "QR_READER_001",
    "deviceName": "二维码扫描器",
    "deviceLocation": "前台"
  },
  "validationInfo": {
    "checkExpired": true,
    "checkBlacklist": true
  }
}
```

### 2.3 手机短信验证
```http
POST /api/v1/visitor/verify/sms
```

**请求参数:**
```json
{
  "visitCode": "VC202401150001",
  "phoneNumber": "13800138001",
  "verificationCode": "123456",
  "deviceInfo": {
    "deviceId": "MOBILE_001",
    "deviceName": "移动端"
  }
}
```

### 2.4 密码验证
```http
POST /api/v1/visitor/verify/password
```

**请求参数:**
```json
{
  "visitCode": "VC202401150001",
  "password": "VISITOR_PASSWORD",
  "deviceInfo": {
    "deviceId": "TERMINAL_001",
    "deviceName": "访客终端"
  }
}
```

### 2.5 指纹验证
```http
POST /api/v1/visitor/verify/fingerprint
```

**请求参数:**
```json
{
  "visitCode": "VC202401150001",
  "fingerprintData": "BASE64_FINGERPRINT_DATA",
  "deviceInfo": {
    "deviceId": "FINGERPRINT_READER_001",
    "deviceName": "指纹识别器"
  },
  "validationInfo": {
    "matchThreshold": 0.85,
    "fingerIndex": "RIGHT_INDEX"
  }
}
```

## 3. 访客登记

### 3.1 访客签到
```http
POST /api/v1/visitor/check-in
```

**请求参数:**
```json
{
  "visitCode": "VC202401150001",
  "checkInTime": "2024-01-15 09:00:00",
  "verificationMethod": "FACE_RECOGNITION", // FACE_RECOGNITION, QR_CODE, SMS, PASSWORD, FINGERPRINT
  "deviceInfo": {
    "deviceId": "FACE_KIOSK_001",
    "deviceName": "大门人脸识别终端",
    "location": "公司大门"
  },
  "checkInLocation": {
    "latitude": 31.2304,
    "longitude": 121.4737,
    "address": "公司大门",
    "floor": "1F"
  },
  "escortInfo": {
    "hasEscort": false,
    "escortName": null,
    "escortPhone": null
  },
  "additionalInfo": {
    "temperature": 36.5,
    "healthStatus": "NORMAL",
    "specialRequirements": null
  }
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "签到成功",
  "data": {
    "checkInId": "CI_20240115_001",
    "visitCode": "VC202401150001",
    "checkInTime": "2024-01-15 09:00:00",
    "visitorInfo": {
      "visitorId": 5001,
      "visitorName": "李四",
      "company": "ABC科技有限公司"
    },
    "accessInfo": {
      "accessPass": {
        "passId": "PASS_20240115_001",
        "passType": "TEMPORARY", // TEMPORARY临时, PERMANENT永久
        "validUntil": "2024-01-15 18:00:00",
        "accessAreas": ["A区", "B区"],
        "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
      },
      "welcomeMessage": "欢迎访问ABC科技有限公司",
      "hostNotification": {
        "hostId": 1001,
        "hostName": "张三",
        "notificationSent": true,
        "message": "您的访客李四已到达"
      }
    }
  }
}
```

### 2.2 访客签出
```http
POST /api/v1/visitor/check-out
```

**请求参数:**
```json
{
  "visitCode": "VC202401150001",
  "checkOutTime": "2024-01-15 12:00:00",
  "deviceInfo": {
    "deviceId": "EXIT_GATE_001",
    "deviceName": "出口门禁",
    "location": "公司大门出口"
  },
  "actualDuration": 180, // 实际停留时长（分钟）
  "feedback": {
    "visitSatisfaction": "SATISFIED", // VERY_SATISFIED, SATISFIED, NEUTRAL, DISSATISFIED
    "visitRating": 5,
    "comments": "访问很顺利，接待人员很专业",
    "suggestions": "建议增加停车位"
  }
}
```

### 3.3 获取访问记录
```http
GET /api/v1/visitor/access-records
```

**查询参数:**
```
visitCode=VC202401150001
visitorId=5001
hostId=1001
startDate=2024-01-01
endDate=2024-01-31
status=CHECKED_IN,CHECKED_OUT
accessArea=A区,B区
page=1
size=20
```

**响应数据:**
```json
{
  "code": 200,
  "data": [
    {
      "accessId": 1001,
      "visitCode": "VC202401150001",
      "visitorInfo": {
        "visitorId": 5001,
        "visitorName": "李四",
        "phone": "13800138001",
        "company": "ABC科技有限公司",
        "photoUrl": "https://api.ioe-dream.com/visitor/photos/001.jpg"
      },
      "hostInfo": {
        "hostId": 1001,
        "hostName": "张三",
        "hostDepartment": "技术研发部"
      },
      "appointmentInfo": {
        "visitDate": "2024-01-15",
        "startTime": "09:00:00",
        "endTime": "12:00:00",
        "purpose": "技术方案讨论"
      },
      "accessInfo": {
        "checkInTime": "2024-01-15 09:00:00",
        "checkOutTime": "2024-01-15 12:00:00",
        "actualDuration": 180,
        "checkInDevice": "FACE_KIOSK_001",
        "checkOutDevice": "EXIT_GATE_001",
        "accessAreas": ["A区"],
        "violations": []
      }
    }
  ]
}
```

## 4. 黑名单管理

### 4.1 添加黑名单
```http
POST /api/v1/visitor/blacklist/add
```

**请求参数:**
```json
{
  "visitorInfo": {
    "name": "违法人员",
    "phone": "13900139001",
    "idCardNumber": "310101199001011234",
    "company": null,
    "photoUrl": "https://api.ioe-dream.com/blacklist/photos/001.jpg",
    "faceFeatures": "BASE64_FACE_FEATURES",
    "fingerprintData": "BASE64_FINGERPRINT_DATA"
  },
  "blacklistInfo": {
    "blacklistType": "PERMANENT", // PERMANENT永久, TEMPORARY临时, RESTRICTED受限
    "reason": "安全威胁",
    "description": "该人员存在安全威胁，禁止访问",
    "riskLevel": "HIGH", // LOW低, MEDIUM中, HIGH高, CRITICAL严重
    "effectiveDate": "2024-01-01",
    "expiryDate": null,
    "alertLevel": "CRITICAL",
    "alertMessage": "高风险人员，立即通知安保部"
  },
  "operatorInfo": {
    "operatorId": 1001,
    "operatorName": "安保主管",
    "operatorRole": "安保主管"
  },
  "evidenceFiles": [
    {
      "fileType": "DOCUMENT",
      "fileName": "违规记录.pdf",
      "fileUrl": "https://api.ioe-dream.com/blacklist/evidence/001.pdf",
      "description": "违规行为记录"
    }
  ]
}
```

### 4.2 检查黑名单
```http
POST /api/v1/visitor/blacklist/check
```

**请求参数:**
```json
{
  "checkType": "FACE_FEATURES", // FACE_FEATURES人脸特征, PHONE_NUMBER手机号, ID_CARD身份证号
  "checkData": "BASE64_FACE_FEATURES_OR_PHONE_OR_ID",
  "visitorInfo": {
    "name": "测试人员",
    "phone": "13800138000"
  }
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "检查完成",
  "data": {
    "isBlacklisted": true,
    "matchInfo": {
      "blacklistId": 1001,
      "matchType": "FACE_FEATURES",
      "matchScore": 0.96,
      "blacklistedVisitor": {
        "blacklistId": 1001,
        "name": "违法人员",
        "phone": "13900139001",
        "riskLevel": "HIGH",
        "reason": "安全威胁",
        "alertLevel": "CRITICAL"
      }
    },
    "alertInfo": {
      "shouldAlert": true,
      "alertLevel": "CRITICAL",
      "alertMessage": "检测到黑名单人员",
      "notifySecurity": true,
      "notifyAdmin": true
    }
  }
}
```

## 5. 统计报表

### 5.1 访客统计报表
```http
GET /api/v1/visitor/reports/statistics
```

**查询参数:**
```
startDate=2024-01-01
endDate=2024-01-31
groupBy=DAY,WEEK,MONTH
departmentId=100
hostId=1001
appointmentType=BUSINESS,INTERVIEW
accessLevel=NORMAL,VIP
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "reportPeriod": {
      "startDate": "2024-01-01",
      "endDate": "2024-01-31",
      "totalDays": 31
    },
    "summary": {
      "totalAppointments": 156,
      "approvedAppointments": 142,
      "completedVisits": 138,
      "cancelledVisits": 8,
      "noShows": 4,
      "approvalRate": 91.0,
      "completionRate": 97.2
    },
    "dailyStats": [
      {
        "date": "2024-01-01",
        "appointments": 5,
        "visits": 4,
        "noShows": 1,
        "avgDuration": 145.5
      }
    ],
    "appointmentTypeStats": [
      {
        "appointmentType": "BUSINESS",
        "count": 98,
        "percentage": 62.8
      },
      {
        "appointmentType": "INTERVIEW",
        "count": 32,
        "percentage": 20.5
      },
      {
        "appointmentType": "MAINTENANCE",
        "count": 16,
        "percentage": 10.3
      }
    ],
    "topHosts": [
      {
        "hostId": 1001,
        "hostName": "张三",
        "hostDepartment": "技术研发部",
        "totalVisitors": 25,
        "avgDuration": 120.5
      }
    ],
    "topCompanies": [
      {
        "companyName": "ABC科技有限公司",
        "visitCount": 18,
        "visitorCount": 12,
        "avgDuration": 135.2
      }
    ]
  }
}
```

### 5.2 访客满意度调查
```http
GET /api/v1/visitor/reports/satisfaction
```

**查询参数:**
```
startDate=2024-01-01
endDate=2024-01-31
departmentId=100
rating=1,2,3,4,5
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "totalReviews": 138,
    "overallRating": 4.6,
    "ratingDistribution": {
      "5": 85,
      "4": 32,
      "3": 15,
      "2": 4,
      "1": 2
    },
    "satisfactionCategories": [
      {
        "category": "接待服务",
        "averageRating": 4.8,
        "feedback": [
          {
            "rating": 5,
            "comment": "接待人员非常专业，服务周到"
          },
          {
            "rating": 4,
            "comment": "服务不错，但等待时间稍长"
          }
        ]
      },
      {
        "category": "设施环境",
        "averageRating": 4.5,
        "feedback": [
          {
            "rating": 4,
            "comment": "会议室环境良好"
          }
        ]
      },
      {
        "category": "访问流程",
        "averageRating": 4.4,
        "feedback": [
          {
            "rating": 5,
            "comment": "验证流程快速便捷"
          }
        ]
      }
    ],
    "improvementSuggestions": [
      "增加停车位",
      "优化等待区域环境",
      "提供免费WiFi"
    ]
  }
}
```

## 6. 移动端专用接口

### 6.1 移动端预约
```http
POST /api/v1/visitor/mobile/appointment
```

**请求参数:**
```json
{
  "visitorInfo": {
    "name": "李四",
    "phone": "13800138001",
    "company": "ABC科技有限公司",
    "photoUrl": "CAPTURED_PHOTO_URL",
    "idCardFront": "CAPTURED_ID_FRONT_URL",
    "idCardBack": "CAPTURED_ID_BACK_URL",
    "faceFeatures": "CAPTURED_FACE_FEATURES"
  },
  "appointmentInfo": {
    "appointmentType": "BUSINESS",
    "purpose": "技术方案讨论",
    "visitDate": "2024-01-15",
    "timeSlot": "09:00-12:00",
    "quickSelect": true
  },
  "hostInfo": {
    "hostName": "张三",
    "hostPhone": "13800138000",
    "hostDepartment": "技术研发部"
  },
  "mobileInfo": {
    "deviceId": "MOBILE_001",
    "appVersion": "2.1.0",
    "osType": "ANDROID",
    "location": {
      "latitude": 31.2304,
      "longitude": 121.4737
    }
  }
}
```

### 6.2 移动端验证
```http
POST /api/v1/visitor/mobile/verify
```

**请求参数:**
```json
{
  "visitCode": "VC202401150001",
  "verificationType": "FACE_RECOGNITION", // FACE_RECOGNITION, QR_CODE, SMS
  "verificationData": {
    "faceImage": "CAPTURED_FACE_IMAGE",
    "faceFeatures": "CAPTURED_FACE_FEATURES",
    "qrCodeData": "SCANNED_QR_DATA",
    "smsCode": "SMS_VERIFICATION_CODE"
  },
  "mobileInfo": {
    "deviceType": "ANDROID",
    "deviceId": "MOBILE_001",
    "appVersion": "2.1.0"
  },
  "location": {
    "latitude": 31.2304,
    "longitude": 121.4737,
    "accuracy": 10.5
  }
}
```

### 6.3 移动端拍照功能
```http
POST /api/v1/visitor/mobile/capture-photo
```

**请求参数:**
```json
{
  "photoType": "VISITOR_PHOTO", // VISITOR_PHOTO访客照片, ID_CARD_FRONT身份证正面, ID_CARD_BACK身份证背面
  "imageData": "BASE64_ENCODED_IMAGE",
  "qualityCheck": true,
  "faceDetection": true,
  "documentDetection": true,
  "mobileInfo": {
    "cameraType": "FRONT",
    "resolution": "1920x1080",
    "deviceModel": "Pixel 6"
  }
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "拍照成功",
  "data": {
    "photoId": "PHOTO_20240115_001",
    "photoUrl": "https://api.ioe-dream.com/visitor/photos/mobile/20240115_001.jpg",
    "photoType": "VISITOR_PHOTO",
    "imageQuality": {
      "resolution": "1920x1080",
      "fileSize": 2048576,
      "sharpness": 85,
      "brightness": 75,
      "contrast": 80
    },
    "detectionResults": {
      "faceDetected": true,
      "faceBoundingBox": {
        "x": 120,
        "y": 80,
        "width": 100,
        "height": 120
      },
      "faceFeatures": "EXTRACTED_FACE_FEATURES"
    },
    "processingTime": 150
  }
}
```

### 6.4 移动端位置验证
```http
POST /api/v1/visitor/mobile/location-verify
```

**请求参数:**
```
visitCode=VC202401150001
currentLocation=31.2304,121.4737
requiredLocation=31.2300,121.4735
radius=100
```

## 7. WebSocket 实时推送

### 7.1 连接WebSocket
```
ws://localhost:8080/ws/visitor/{userId}?token={sa-token}
```

### 7.2 消息类型

#### 7.2.1 预约状态变更通知
```json
{
  "type": "APPOINTMENT_STATUS_CHANGE",
  "timestamp": 1640995200000,
  "data": {
    "appointmentId": 20240115001,
    "appointmentNo": "VISIT-20240115-001",
    "oldStatus": "PENDING_APPROVAL",
    "newStatus": "APPROVED",
    "visitorName": "李四",
    "hostName": "张三",
    "approverName": "李经理",
    "approvalTime": "2024-01-10 16:45:00",
    "message": "您的访客预约已获批准"
  }
}
```

#### 7.2.2 访客到达通知
```json
{
  "type": "VISITOR_ARRIVAL",
  "timestamp": 1640995200000,
  "data": {
    "visitCode": "VC202401150001",
    "visitorName": "李四",
    "company": "ABC科技有限公司",
    "checkInTime": "2024-01-15 09:00:00",
    "hostName": "张三",
    "hostPhone": "13800138000",
    "checkInLocation": "公司大门",
    "photoUrl": "https://api.ioe-dream.com/visitor/photos/001.jpg",
    "message": "您的访客李四已到达"
  }
}
```

#### 7.2.3 访客离开通知
```json
{
  "type": "VISITOR_DEPARTURE",
  "timestamp": 1640995200000,
  "data": {
    "visitCode": "VC202401150001",
    "visitorName": "李四",
    "company": "ABC科技有限公司",
    "checkOutTime": "2024-01-15 12:00:00",
    "visitDuration": 180,
    "hostName": "张三",
    "feedbackRating": 5,
    "message": "您的访客李四已完成访问"
  }
}
```

#### 7.2.4 紧急告警通知
```json
{
  "type": "SECURITY_ALERT",
  "timestamp": 1640995200000,
  "data": {
    "alertType": "BLACKLISTED_VISITOR",
    "alertLevel": "CRITICAL",
    "visitorName": "违法人员",
    "visitCode": "VC202401150002",
    "riskLevel": "HIGH",
    "alertMessage": "检测到黑名单人员尝试进入",
    "location": "公司大门",
    "photoUrl": "https://api.ioe-dream.com/blacklist/photos/001.jpg",
    "requiredActions": ["SECURITY_INTERVENTION", "POLICE_NOTIFICATION"],
    "message": "安全威胁：检测到黑名单人员"
  }
}
```

## 8. 批量操作接口

### 8.1 批量导入访客
```http
POST /api/v1/visitor/batch/import
Content-Type: multipart/form-data
```

**请求参数:**
```
file: 访客Excel文件
importType=APPOINTMENT,VISITOR_INFO
validateOnly=false
sendNotification=true
```

### 8.2 批量导出访客记录
```http
POST /api/v1/visitor/batch/export
```

**请求参数:**
```json
{
  "exportType": "APPOINTMENT_REPORT", // APPOINTMENT_REPORT预约报告, VISITOR_LIST访客列表, ACCESS_RECORDS访问记录
  "filter": {
    "startDate": "2024-01-01",
    "endDate": "2024-01-31",
    "hostId": 1001,
    "status": "COMPLETED"
  },
  "format": "EXCEL", // EXCEL, PDF, CSV
  "includePhotos": true,
  "emailTo": "admin@company.com",
  "groupBy": "DAY,WEEK"
}
```

### 8.3 批量审批
```http
POST /api/v1/visitor/batch/approve
```

**请求参数:**
```json
{
  "appointmentIds": [20240115001, 20240115002],
  "action": "APPROVE", // APPROVE批准, REJECT拒绝, CANCEL取消
  "reason": "经审核，访客信息真实有效",
  "conditions": [
    {
      "condition": "ESCORT_REQUIRED",
      "description": "需要全程陪同"
    }
  ],
  "operatorInfo": {
    "operatorId": 1001,
    "operatorName": "管理员"
  }
}
```

## 9. 系统配置接口

### 9.1 获取访客系统配置
```http
GET /api/v1/visitor/config/system
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "appointmentConfig": {
      "maxAdvanceDays": 30,
      "maxDailyVisitors": 100,
      "appointmentApprovalRequired": true,
      "autoApprovalConditions": [
        {
          "condition": "RETURNING_VISITOR",
          "threshold": 5,
          "approvalExempted": true
        }
      ],
      "cancellationPolicy": {
        "allowCancellation": true,
        "minCancelHours": 2,
        "cancellationReasonRequired": true
      }
    },
    "validationConfig": {
      "supportedMethods": ["FACE_RECOGNITION", "QR_CODE", "SMS", "PASSWORD", "FINGERPRINT"],
      "faceRecognition": {
        "confidence": 0.85,
        "livenessCheck": true,
        "antiSpoofing": true
      },
      "smsVerification": {
        "codeLength": 6,
        "validMinutes": 5,
        "maxAttempts": 3
      }
    },
    "blacklistConfig": {
      "autoBlacklist": true,
      "blacklistTriggers": [
        {
          "trigger": "VIOLENCE_RECORD",
          "autoBlacklist": true
        },
        {
          "trigger": "MULTIPLE_NO_SHOWS",
          "threshold": 3,
          "autoBlacklist": true
        }
      ],
      "reviewRequired": true,
      "blacklistDuration": {
        "temporary": 30,
        "permanent": null
      }
    },
    "notificationConfig": {
      "appointmentNotification": true,
      "arrivalNotification": true,
      "departureNotification": true,
      "alertNotification": true,
      "smsEnabled": true,
      "emailEnabled": true,
      "pushNotification": true
    },
    "accessConfig": {
      "defaultAccessAreas": ["大厅", "会议室"],
      "maxVisitDuration": 480,
      "afterHoursAccess": false,
      "weekendAccess": true,
      "accessLevelMapping": {
        "EMPLOYEE": "NORMAL",
        "CONTRACTOR": "NORMAL",
        "INTERVIEW_CANDIDATE": "RESTRICTED",
        "VIP": "VIP"
      }
    }
  }
}
```

---

## 接口权限矩阵

| 功能模块 | 访客 | 接待员 | 部门经理 | 系统管理员 | 超级管理员 |
|---------|------|--------|----------|-----------|-----------|
| 预约申请 | ✓ | ✓ | ✓ | ✓ | ✓ |
| 预约审批 | ✗ | ✓ | ✓ | ✓ | ✓ |
| 访客验证 | ✓(本人) | ✓ | ✓ | ✓ | ✓ |
| 访客登记 | ✓(本人) | ✓ | ✓ | ✓ | ✓ |
| 访客查询 | ✓(本人) | ✓ | ✓ | ✓ | ✓ |
| 访客统计 | ✗ | ✓ | ✓(部门) | ✓ | ✓ |
| 黑名单管理 | ✗ | ✓ | ✓ | ✓ | ✓ |
| 系统配置 | ✗ | ✗ | ✓ | ✓ | ✓ |
| 批量操作 | ✗ | ✓ | ✓ | ✓ | ✓ |

---

## 版本说明

- **当前版本**: v2.0.0
- **发布日期**: 2024-01-15
- **兼容性**: 向下兼容v1.x版本
- **更新内容**:
  - 新增移动端专用接口
  - 增强生物识别验证功能
  - 优化批量操作性能
  - 完善实时推送机制
  - 新增满意度调查功能

---

## 技术支持

如有API使用问题，请联系：
- **技术支持**: tech-support@ioe-dream.com
- **API文档**: https://api.ioe-dream.com/docs/visitor
- **SDK下载**: https://github.com/ioe-dream/sdks
- **问题反馈**: https://github.com/ioe-dream/issues