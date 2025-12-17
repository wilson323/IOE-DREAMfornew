# 访客管理模块API接口设计

## 1. 接口规范

### 1.1 基础配置
- **服务端口**: 8095
- **接口前缀**: `/api/v1/visitor`
- **认证方式**: Sa-Token
- **数据格式**: JSON

### 1.2 通用响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1640995200000
}
```

## 2. 访客管理接口

### 2.1 分页查询访客列表
```http
POST /api/v1/visitor/query
```

**请求参数:**
```json
{
  "name": "张三",
  "phone": "138****0001",
  "companyName": "ABC公司",
  "blacklisted": false,
  "pageNum": 1,
  "pageSize": 20
}
```

### 2.2 添加访客
```http
POST /api/v1/visitor/add
```

**请求参数:**
```json
{
  "name": "李四",
  "gender": 1,
  "idCard": "310101199001011234",
  "phone": "13800138001",
  "email": "lisi@company.com",
  "companyName": "ABC科技有限公司",
  "visitorLevel": "NORMAL"
}
```

### 2.3 加入黑名单
```http
POST /api/v1/visitor/addToBlacklist
```

**请求参数:**
```json
{
  "visitorId": 1001,
  "reason": "安全威胁",
  "blacklistType": "PERMANENT",
  "riskLevel": "HIGH"
}
```

## 3. 预约管理接口

### 3.1 创建预约
```http
POST /api/v1/visitor/reservation/create
```

**请求参数:**
```json
{
  "visitorInfo": {
    "name": "李四",
    "phone": "13800138001",
    "company": "ABC科技有限公司",
    "idCardNumber": "310101199001011234"
  },
  "appointmentInfo": {
    "appointmentType": "BUSINESS",
    "purpose": "技术方案讨论",
    "visitDate": "2024-01-15",
    "startTime": "09:00:00",
    "endTime": "12:00:00"
  },
  "hostInfo": {
    "hostId": 1001,
    "hostName": "张三"
  }
}
```

### 3.2 审批预约
```http
POST /api/v1/visitor/reservation/approve
```

**请求参数:**
```json
{
  "reservationId": 20240115001,
  "action": "APPROVED",
  "approveRemark": "同意访问",
  "conditions": [
    {"condition": "ESCORT_REQUIRED", "description": "需要陪同"}
  ]
}
```

### 3.3 取消预约
```http
POST /api/v1/visitor/reservation/cancel
```

**请求参数:**
```json
{
  "reservationId": 20240115001,
  "cancelReason": "会议取消",
  "notifyVisitor": true
}
```

## 4. 登记管理接口

### 4.1 访客签到
```http
POST /api/v1/visitor/registration/checkin
```

**请求参数:**
```json
{
  "visitCode": "VC202401150001",
  "verificationMethod": "FACE_RECOGNITION",
  "deviceInfo": {
    "deviceId": "FACE_KIOSK_001",
    "location": "公司大门"
  },
  "checkInLocation": {
    "latitude": 31.2304,
    "longitude": 121.4737
  }
}
```

### 4.2 访客签出
```http
POST /api/v1/visitor/registration/checkout
```

**请求参数:**
```json
{
  "visitCode": "VC202401150001",
  "checkOutTime": "2024-01-15 12:00:00",
  "feedback": {
    "visitSatisfaction": "SATISFIED",
    "visitRating": 5
  }
}
```

### 4.3 获取在场访客
```http
GET /api/v1/visitor/registration/current-visitors
```

## 5. 身份验证接口

### 5.1 人脸识别验证
```http
POST /api/v1/visitor/verify/face
```

**请求参数:**
```json
{
  "visitCode": "VC202401150001",
  "faceImage": "BASE64_ENCODED_IMAGE",
  "faceFeatures": "BASE64_FACE_FEATURES",
  "deviceInfo": {
    "deviceId": "FACE_KIOSK_001"
  },
  "validationInfo": {
    "livenessCheck": true,
    "antiSpoofing": true
  }
}
```

### 5.2 二维码验证
```http
POST /api/v1/visitor/verify/qrcode
```

**请求参数:**
```json
{
  "visitCode": "VC202401150001",
  "qrCodeData": "QR_CODE_SCAN_DATA",
  "deviceInfo": {
    "deviceId": "QR_READER_001"
  }
}
```

### 5.3 短信验证
```http
POST /api/v1/visitor/verify/sms
```

**请求参数:**
```json
{
  "visitCode": "VC202401150001",
  "phoneNumber": "13800138001",
  "verificationCode": "123456"
}
```

## 6. 物流管理接口

### 6.1 创建物流预约
```http
POST /api/v1/logistics/reservation/create
```

**请求参数:**
```json
{
  "driverInfo": {
    "driverName": "王师傅",
    "idCard": "310101198001011234",
    "phone": "13900139001",
    "driverLicense": "310101198001011234"
  },
  "vehicleInfo": {
    "plateNumber": "沪A12345",
    "vehicleType": "厢式货车",
    "loadCapacity": 5.0
  },
  "goodsInfo": {
    "goodsType": "电子产品",
    "goodsWeight": 2.5,
    "packageCount": 10
  },
  "operationInfo": {
    "reservationType": "DELIVERY",
    "operationType": "UNLOADING",
    "operationAreaId": 1001,
    "expectedArriveDate": "2024-01-15",
    "expectedArriveTimeStart": "09:00",
    "expectedArriveTimeEnd": "10:00"
  },
  "intervieweeId": 2001
}
```

### 6.2 物流车辆签到
```http
POST /api/v1/logistics/registration/checkin
```

**请求参数:**
```json
{
  "reservationCode": "LOG202401150001",
  "driverPhotoUrl": "https://...",
  "vehiclePhotoUrls": ["https://..."],
  "documentPhotoUrls": ["https://..."],
  "securityCheckItems": {
    "vehicleCondition": "PASSED",
    "driverLicenseValid": true,
    "vehiclePermitValid": true
  }
}
```

### 6.3 生成电子出门单
```http
POST /api/v1/logistics/exit-pass/generate
```

**请求参数:**
```json
{
  "registrationId": 3001,
  "goodsInfo": {
    "items": [
      {"name": "电子产品", "quantity": 10, "weight": 2.5}
    ]
  },
  "loadStatus": "UNLOADED",
  "warehouseOperator": "仓库管理员张三"
}
```

### 6.4 被访人确认出门单
```http
POST /api/v1/logistics/exit-pass/intervieweeConfirm
```

**请求参数:**
```json
{
  "passId": 4001,
  "confirmUser": "被访人李四",
  "signature": "BASE64_SIGNATURE",
  "confirmRemarks": "货物已确认"
}
```

### 6.5 保安检查出门单
```http
POST /api/v1/logistics/exit-pass/guardCheck
```

**请求参数:**
```json
{
  "passId": 4001,
  "checkResult": "PASSED",
  "guardPhotos": ["https://..."],
  "checkRemarks": "检查通过"
}
```

### 6.6 放行车辆
```http
POST /api/v1/logistics/exit-pass/release
```

**请求参数:**
```json
{
  "passId": 4001,
  "releaseRemarks": "正常放行"
}
```

## 7. 统计报表接口

### 7.1 访客统计
```http
GET /api/v1/visitor/reports/statistics
```

**查询参数:**
```
startDate=2024-01-01
endDate=2024-01-31
groupBy=DAY
```

### 7.2 物流统计
```http
GET /api/v1/logistics/reports/statistics
```

**查询参数:**
```
startDate=2024-01-01
endDate=2024-01-31
groupBy=DAY
```

## 8. 权限矩阵

| 功能 | 访客 | 接待员 | 部门经理 | 安保人员 | 管理员 |
|------|------|--------|----------|----------|--------|
| 预约申请 | ✓ | ✓ | ✓ | ✗ | ✓ |
| 预约审批 | ✗ | ✓ | ✓ | ✗ | ✓ |
| 访客登记 | ✓ | ✓ | ✓ | ✓ | ✓ |
| 黑名单管理 | ✗ | ✓ | ✓ | ✓ | ✓ |
| 物流预约 | ✗ | ✓ | ✓ | ✗ | ✓ |
| 出门单审批 | ✗ | ✗ | ✓ | ✓ | ✓ |
| 统计报表 | ✗ | ✓ | ✓ | ✗ | ✓ |
| 系统配置 | ✗ | ✗ | ✗ | ✗ | ✓ |

## 9. 错误码定义

| 错误码 | 说明 |
|--------|------|
| 70101 | 访客不存在 |
| 70102 | 访客已过期 |
| 70103 | 验证失败 |
| 70104 | 预约时间冲突 |
| 70105 | 访客被禁止 |
| 70106 | 生物特征不匹配 |
| 70201 | 预约不存在 |
| 70202 | 预约已取消 |
| 70203 | 预约已过期 |
| 70301 | 物流预约不存在 |
| 70302 | 出门单未确认 |

