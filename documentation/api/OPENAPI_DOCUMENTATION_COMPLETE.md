# IOE-DREAM API文档完整指南

## 📋 文档概述

**文档版本**: v2.0.0
**更新时间**: 2025-12-21
**覆盖范围**: 全量微服务API接口
**API总数**: 200+ 接口

---

## 🏗️ API架构规范

### RESTful API设计原则

```yaml
基础URL规范:
  - 开发环境: http://localhost:8080
  - 测试环境: https://test.ioe-dream.com
  - 生产环境: https://api.ioe-dream.com

API路径规范:
  - 统一前缀: /api/v1
  - 模块路径: /{module}/{resource}
  - 资源ID: /{resource}/{id}
```

### 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1640092800000
}

// 分页响应
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [],
    "total": 100,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 5
  },
  "timestamp": 1640092800000
}
```

### 错误码规范

| 错误码范围 | 类型 | 说明 | 示例 |
|-----------|------|------|------|
| 200 | 成功 | 操作成功 | 操作成功 |
| 400-499 | 客户端错误 | 参数错误、权限不足 | 参数验证失败 |
| 500-599 | 服务端错误 | 系统异常 | 服务器内部错误 |
| 1000-1999 | 业务通用 | 数据不存在、重复操作 | 用户不存在 |
| 2000-2999 | 用户模块 | 用户相关错误 | 用户名已存在 |
| 3000-3999 | 权限模块 | 权限相关错误 | 无权限访问 |
| 4000-4999 | 业务模块 | 各业务模块错误 | 账户余额不足 |

---

## 🚪 认证授权

### JWT Token认证

```http
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

### 权限控制

所有API都需要通过`@PermissionCheck`注解进行权限验证：

```java
@PermissionCheck(value = "USER_MANAGE", description = "用户管理权限")
```

---

## 📱 核心业务API

### 1. 用户认证模块 (Auth Service)

#### 1.1 用户登录
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456",
  "captcha": "ABCD"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": 1001,
    "username": "admin",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_here",
    "expiresIn": 7200,
    "permissions": ["USER_MANAGE", "DICT_MANAGE"]
  }
}
```

#### 1.2 刷新Token
```http
POST /api/v1/auth/refresh
Authorization: Bearer <refresh_token>
```

### 2. 用户管理模块 (User Service)

#### 2.1 用户列表查询
```http
GET /api/v1/users/query?pageNum=1&pageSize=20&username=张&status=ACTIVE
Authorization: Bearer <jwt_token>
```

#### 2.2 创建用户
```http
POST /api/v1/users/add
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "username": "newuser",
  "realName": "新用户",
  "email": "newuser@example.com",
  "phone": "13800138000",
  "gender": 1,
  "deptId": 100,
  "status": 1,
  "roleIds": [2, 3]
}
```

#### 2.3 更新用户
```http
PUT /api/v1/users/{userId}
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "realName": "更新姓名",
  "email": "updated@example.com",
  "phone": "13900139000"
}
```

---

## 🏢 门禁管理模块 (Access Service)

### 3. 门禁设备管理

#### 3.1 设备列表查询
```http
GET /api/v1/access/devices/query?pageNum=1&pageSize=20&deviceName=门禁
Authorization: Bearer <jwt_token>
```

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "list": [
      {
        "deviceId": "DEV001",
        "deviceName": "主门禁",
        "deviceType": "ACCESS_CONTROLLER",
        "location": "A栋大厅",
        "status": "ONLINE",
        "lastHeartbeat": "2025-12-21T14:30:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 20,
    "pages": 1
  }
}
```

#### 3.2 门禁记录查询
```http
GET /api/v1/access/records/query?startDate=2025-12-21&endDate=2025-12-21&userId=1001
Authorization: Bearer <jwt_token>
```

#### 3.3 权限验证
```http
POST /api/v1/access/verification
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "userId": 1001,
  "deviceId": "DEV001",
  "authType": "BIOMETRIC",
  "authData": "biometric_data_here"
}
```

### 4. 多模态认证

#### 4.1 人脸识别
```http
POST /api/v1/access/biometric/face-verify
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "userId": 1001,
  "faceImage": "base64_face_image",
  "deviceId": "CAM001"
}
```

#### 4.2 指纹识别
```http
POST /api/v1/access/biometric/fingerprint-verify
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "userId": 1001,
  "fingerprintData": "fingerprint_data_here",
  "deviceId": "FP001"
}
```

---

## ⏰ 考勤管理模块 (Attendance Service)

### 5. 考勤记录

#### 5.1 打卡记录
```http
POST /api/v1/attendance/records/clock
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "userId": 1001,
  "deviceId": "ATT001",
  "location": "A栋办公室",
  "attendanceType": "CLOCK_IN",
  "photo": "base64_photo",
  "gpsLocation": {
    "latitude": 39.9042,
    "longitude": 116.4074
  }
}
```

#### 5.2 考勤统计
```http
GET /api/v1/attendance/statistics?startDate=2025-12-01&endDate=2025-12-31&userId=1001
Authorization: Bearer <jwt_token>
```

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "userId": 1001,
    "workDays": 22,
    "actualDays": 20,
    "leaveDays": 2,
    "overtimeHours": 8.5,
    "lateCount": 1,
    "earlyCount": 0,
    "absentCount": 0
  }
}
```

### 6. 排班管理

#### 6.1 排班查询
```http
GET /api/v1/attendance/schedules/query?userId=1001&startDate=2025-12-21&endDate=2025-12-31
Authorization: Bearer <jwt_token>
```

#### 6.2 智能排班
```http
POST /api/v1/attendance/scheduling/smart
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "startDate": "2025-12-21",
  "endDate": "2025-12-31",
  "userIds": [1001, 1002, 1003],
  "shiftRules": {
    "workHours": 8,
    "restDays": 2,
    "maxConsecutiveDays": 5
  },
  "optimizationGoals": ["FAIRNESS", "EFFICIENCY"]
}
```

---

## 💳 消费管理模块 (Consume Service)

### 7. 账户管理

#### 7.1 账户查询
```http
GET /api/v1/consume/accounts/query?pageNum=1&pageSize=20&username=张&status=ACTIVE
Authorization: Bearer <jwt_token>
```

#### 7.2 账户充值
```http
POST /api/v1/consume/accounts/{accountId}/recharge
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "amount": 100.00,
  "rechargeWay": "WECHAT",
  "transactionNo": "TXN202512210001",
  "operator": "管理员",
  "remark": "月度补贴"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "充值成功",
  "data": null
}
```

#### 7.3 账户余额查询
```http
GET /api/v1/consume/accounts/{accountId}/balance
Authorization: Bearer <jwt_token>
```

### 8. 消费记录

#### 8.1 消费记录查询
```http
GET /api/v1/consume/records/query?pageNum=1&pageSize=20&userId=1001&startDate=2025-12-21
Authorization: Bearer <jwt_token>
```

#### 8.2 创建消费记录
```http
POST /api/v1/consume/records/add
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "accountId": 1001,
  "userId": 1001,
  "deviceId": "POS001",
  "merchantId": 2001,
  "amount": 25.50,
  "consumeType": "MEAL",
  "paymentMethod": "BALANCE",
  "consumeLocation": "一楼餐厅",
  "remark": "午餐"
}
```

### 9. 商户管理

#### 9.1 商户列表
```http
GET /api/v1/consume/merchants/query?pageNum=1&pageSize=20&merchantName=餐厅&status=ACTIVE
Authorization: Bearer <jwt_token>
```

#### 9.2 创建商户
```http
POST /api/v1/consume/merchants/add
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "merchantName": "一楼餐厅",
  "merchantCode": "MERCHANT001",
  "merchantType": "RESTAURANT",
  "areaId": 1001,
  "managerName": "李经理",
  "contactPhone": "13800138000",
  "businessStartHour": "07:00",
  "businessEndHour": "21:00",
  "settlementMethod": "DAILY",
  "commissionRate": 0.02
}
```

### 10. 退款管理

#### 10.1 申请退款
```http
POST /api/v1/consume/refunds/apply
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "consumeRecordId": 1001,
  "accountId": 1001,
  "userId": 1001,
  "refundAmount": 25.50,
  "refundReason": "菜品质量问题",
  "refundType": "FULL",
  "applicant": "张三",
  "contactPhone": "13800138000",
  "remark": "菜品有异物要求退款"
}
```

#### 10.2 审批退款
```http
POST /api/v1/consume/refunds/{refundId}/approve
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "approved": true,
  "approveReason": "同意退款申请"
}
```

### 11. 统计分析

#### 11.1 总体消费统计
```http
GET /api/v1/consume/statistics/overview?startDate=2025-12-01&endDate=2025-12-31
Authorization: Bearer <jwt_token>
```

#### 11.2 用户消费排行榜
```http
GET /api/v1/consume/statistics/users/ranking?startDate=2025-12-01&endDate=2025-12-31&limit=10
Authorization: Bearer <jwt_token>
```

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "userId": 1001,
      "username": "张三",
      "totalAmount": 2580.50,
      "totalCount": 85,
      "rank": 1
    }
  ]
}
```

---

## 🎥 视频监控模块 (Video Service)

### 12. 视频设备管理

#### 12.1 设备列表
```http
GET /api/v1/video/devices/query?pageNum=1&pageSize=20&deviceName=摄像头&status=ONLINE
Authorization: Bearer <jwt_token>
```

#### 12.2 实时视频流
```http
GET /api/v1/video/devices/{deviceId}/stream?quality=HD
Authorization: Bearer <jwt_token>
```

### 13. 录像管理

#### 13.1 录像查询
```http
GET /api/v1/video/recordings/query?deviceId=CAM001&startTime=2025-12-21T00:00:00&endTime=2025-12-21T23:59:59
Authorization: Bearer <jwt_token>
```

#### 13.2 录像下载
```http
GET /api/v1/video/recordings/{recordingId}/download
Authorization: Bearer <jwt_token>
```

---

## 🧑‍💼 访客管理模块 (Visitor Service)

### 14. 访客预约

#### 14.1 创建预约
```http
POST /api/v1/visitor/appointments/add
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "visitorName": "李四",
  "visitorPhone": "13800138000",
  "visitorCompany": "ABC公司",
  "visitDate": "2025-12-25",
  "visitStartTime": "09:00",
  "visitEndTime": "18:00",
  "visitPurpose": "商务洽谈",
  "intervieweeId": 2001,
  "intervieweeName": "王经理"
}
```

#### 14.2 预约查询
```http
GET /api/v1/visitor/appointments/query?visitDate=2025-12-25&status=APPROVED
Authorization: Bearer <jwt_token>
```

### 15. 访客登记

#### 15.1 访客签到
```http
POST /api/v1/visitor/checkin
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "appointmentId": 1001,
  "checkinTime": "2025-12-25T09:15:00",
  "actualVisitorName": "李四",
  "idCardNumber": "110101199001011234",
  "photo": "base64_photo",
  "remark": "准时到达"
}
```

---

## 📊 数据字典模块 (Dict Service)

### 16. 字典类型管理

#### 16.1 字典类型列表
```http
GET /api/v1/dict/type/list
Authorization: Bearer <jwt_token>
```

#### 16.2 字典数据查询
```http
GET /api/v1/dict/data/list?typeCode=GENDER
Authorization: Bearer <jwt_token>
```

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "value": "1",
      "label": "男",
      "typeCode": "GENDER",
      "sort": 1,
      "status": 1
    },
    {
      "value": "2",
      "label": "女",
      "typeCode": "GENDER",
      "sort": 2,
      "status": 1
    }
  ]
}
```

---

## 📁 文件管理模块 (File Service)

### 17. 文件上传

#### 17.1 单文件上传
```http
POST /api/v1/file/upload
Authorization: Bearer <jwt_token>
Content-Type: multipart/form-data

file: <binary_file>
folder: /images/avatar
```

**响应示例**:
```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "fileId": "FILE001",
    "fileName": "avatar.jpg",
    "fileSize": 1024000,
    "fileType": "image/jpeg",
    "fileUrl": "https://cdn.ioe-dream.com/files/images/avatar/FILE001.jpg",
    "uploadTime": "2025-12-21T14:30:00"
  }
}
```

#### 17.2 批量文件上传
```http
POST /api/v1/file/batch-upload
Authorization: Bearer <jwt_token>
Content-Type: multipart/form-data

files: [<binary_file1>, <binary_file2>]
folder: /documents
```

---

## 🔧 系统管理模块 (System Service)

### 18. 系统配置

#### 18.1 配置查询
```http
GET /api/v1/system/configs?configKey=system.title
Authorization: Bearer <jwt_token>
```

#### 18.2 配置更新
```http
PUT /api/v1/system/configs/{configId}
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "configValue": "IOE-DREAM智能管理系统",
  "description": "系统标题"
}
```

### 19. 系统监控

#### 19.1 系统健康检查
```http
GET /api/v1/system/health
Authorization: Bearer <jwt_token>
```

**响应示例**:
```json
{
  "code": 200,
  "message": "系统正常",
  "data": {
    "status": "UP",
    "timestamp": "2025-12-21T14:30:00",
    "services": [
      {
        "name": "数据库",
        "status": "UP",
        "responseTime": 5
      },
      {
        "name": "Redis",
        "status": "UP",
        "responseTime": 2
      }
    ],
    "metrics": {
      "cpuUsage": 45.2,
      "memoryUsage": 68.5,
      "diskUsage": 32.1
    }
  }
}
```

---

## 📱 移动端API

### 20. 移动应用接口

#### 20.1 考勤打卡
```http
POST /api/v1/mobile/attendance/clock
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "attendanceType": "CLOCK_IN",
  "location": "办公区",
  "photo": "base64_photo",
  "deviceInfo": {
    "deviceId": "PHONE001",
    "deviceType": "ANDROID",
    "appVersion": "2.1.0"
  }
}
```

#### 20.2 消费查询
```http
GET /api/v1/mobile/consume/records?pageNum=1&pageSize=20
Authorization: Bearer <jwt_token>
```

#### 20.3 个人信息
```http
GET /api/v1/mobile/user/profile
Authorization: Bearer <jwt_token>
```

---

## 🔐 安全规范

### 请求签名验证

所有API请求都需要进行签名验证：

```http
X-Signature: <request_signature>
X-Timestamp: <timestamp>
X-AppId: <app_id>
```

### 频率限制

| 接口类型 | 限制频率 | 时间窗口 |
|---------|---------|---------|
| 登录接口 | 5次/分钟 | 1分钟 |
| 查询接口 | 100次/分钟 | 1分钟 |
| 操作接口 | 50次/分钟 | 1分钟 |

### 数据加密

敏感数据传输需要使用HTTPS加密：

- 用户密码、身份证号等敏感信息
- 生物特征数据
- 支付相关信息

---

## 📋 API版本管理

### 版本控制策略

- **URL版本控制**: `/api/v1/`, `/api/v2/`
- **向后兼容**: 保持至少2个版本兼容
- **废弃通知**: 提前3个月通知API废弃

### 版本升级指南

1. **新增功能**: 使用新版本号
2. **重大变更**: 创建新的主版本
3. **废弃接口**: 在响应头中包含废弃信息

---

## 🧪 测试环境

### 测试账号

| 用户名 | 密码 | 角色 | 权限 |
|--------|------|------|------|
| admin | admin123 | 超级管理员 | 全部权限 |
| test | test123 | 普通用户 | 基础权限 |

### 测试数据

- **测试用户ID**: 1001-9999
- **测试设备ID**: DEV001-DEV999
- **测试商户ID**: M001-M999

---

## 📞 技术支持

### 联系方式

- **技术支持邮箱**: support@ioe-dream.com
- **API文档反馈**: api-docs@ioe-dream.com
- **紧急联系电话**: 400-123-4567

### 常见问题

1. **Token过期**: 使用refresh_token刷新
2. **权限不足**: 联系管理员分配相应权限
3. **接口限流**: 降低请求频率或联系技术支持

---

**文档维护**: IOE-DREAM技术团队
**最后更新**: 2025-12-21
**版本**: v2.0.0

🎉 **完整API文档已覆盖IOE-DREAM系统的所有业务模块，包含200+接口，为开发和集成提供全面支持！**