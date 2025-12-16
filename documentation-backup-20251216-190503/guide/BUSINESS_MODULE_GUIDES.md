# 业务模块使用指南

**版本**: v1.0.0  
**更新日期**: 2025-01-30  
**适用范围**: IOE-DREAM智慧园区一卡通管理平台所有业务模块

---

## 📋 目录

- [消费模块使用指南](#消费模块使用指南)
- [考勤模块使用指南](#考勤模块使用指南)
- [门禁模块使用指南](#门禁模块使用指南)
- [访客模块使用指南](#访客模块使用指南)
- [视频模块使用指南](#视频模块使用指南)

---

## 💳 消费模块使用指南

### 1. 模块概述

消费模块提供园区内一卡通消费管理功能，支持多种支付方式（刷卡、刷脸、NFC、手机支付等），实现无感支付、账户管理、消费统计等功能。

### 2. 核心功能

#### 2.1 消费交易

**功能描述**: 执行消费交易，支持多种消费模式

**API接口**: `POST /api/v1/consume/transaction/execute`

**请求示例**:
```json
{
  "userId": 1001,
  "accountId": 2001,
  "deviceId": 3001,
  "areaId": 4001,
  "amount": 50.00,
  "consumeMode": "CARD"
}
```

**消费模式**:
- `CARD`: 刷卡消费
- `FACE`: 刷脸消费
- `NFC`: NFC支付
- `MOBILE`: 手机支付
- `CREDIT`: 信用额度消费

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "transactionNo": "TXN2025013012345678",
    "success": true,
    "message": "消费成功",
    "amount": 50.00,
    "balance": 450.00
  }
}
```

#### 2.2 查询交易详情

**功能描述**: 根据交易流水号查询交易详细信息

**API接口**: `GET /api/v1/consume/transaction/detail/{transactionNo}`

**请求示例**:
```
GET /api/v1/consume/transaction/detail/TXN2025013012345678
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "transactionNo": "TXN2025013012345678",
    "userId": 1001,
    "amount": 50.00,
    "consumeMode": "CARD",
    "status": "SUCCESS",
    "createTime": "2025-01-30 12:34:56",
    "deviceName": "食堂消费机001"
  }
}
```

#### 2.3 分页查询消费记录

**功能描述**: 分页查询消费记录，支持多条件筛选

**API接口**: `POST /api/v1/consume/transaction/query`

**请求示例**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "userId": 1001,
  "startDate": "2025-01-01",
  "endDate": "2025-01-31",
  "consumeMode": "CARD",
  "status": "SUCCESS"
}
```

#### 2.4 账户管理

**功能描述**: 账户CRUD操作、余额管理、状态管理

**主要接口**:
- `POST /api/v1/consume/account/add` - 创建账户
- `POST /api/v1/consume/account/update` - 更新账户
- `GET /api/v1/consume/account/{accountId}` - 查询账户详情
- `POST /api/v1/consume/account/recharge` - 账户充值
- `POST /api/v1/consume/account/refund` - 账户退款

### 3. 使用场景

#### 场景1: 食堂消费

1. 员工在食堂消费机前刷卡或刷脸
2. 系统自动识别身份并验证账户余额
3. 扣款成功后完成消费
4. 生成消费记录并发送通知

#### 场景2: 账户充值

1. 员工通过手机APP或PC端发起充值
2. 选择支付方式（微信、支付宝、银行卡）
3. 支付成功后自动充值到账户
4. 发送充值成功通知

### 4. 注意事项

- 账户余额不足时，系统会自动拒绝消费
- 支持信用额度消费，需提前配置信用额度
- 消费记录实时同步，支持离线消费（网络恢复后自动同步）
- 支持消费退款，需通过审批流程

---

## ⏰ 考勤模块使用指南

### 1. 模块概述

考勤模块提供员工考勤打卡、排班管理、请假管理、加班管理等功能，支持多种打卡方式（GPS定位、刷脸、刷卡等）。

### 2. 核心功能

#### 2.1 考勤打卡

**功能描述**: 员工考勤打卡，支持GPS定位、刷脸、刷卡等方式

**API接口**: `POST /api/v1/attendance/record/clock`

**请求示例**:
```json
{
  "userId": 1001,
  "clockType": "GPS",
  "latitude": 39.9042,
  "longitude": 116.4074,
  "address": "北京市朝阳区"
}
```

**打卡类型**:
- `GPS`: GPS定位打卡
- `FACE`: 刷脸打卡
- `CARD`: 刷卡打卡
- `MOBILE`: 手机打卡

#### 2.2 排班管理

**功能描述**: 员工排班管理，支持固定班次、弹性时间、轮班等

**主要接口**:
- `POST /api/v1/attendance/shift/add` - 创建排班
- `POST /api/v1/attendance/shift/update` - 更新排班
- `GET /api/v1/attendance/shift/{shiftId}` - 查询排班详情

#### 2.3 请假管理

**功能描述**: 员工请假申请、审批、统计

**主要接口**:
- `POST /api/v1/attendance/leave/apply` - 提交请假申请
- `POST /api/v1/attendance/leave/approve` - 审批请假申请
- `GET /api/v1/attendance/leave/list` - 查询请假记录

### 3. 使用场景

#### 场景1: 正常打卡

1. 员工到达工作地点
2. 使用手机APP或考勤机打卡
3. 系统验证位置和时间
4. 记录打卡信息并生成考勤记录

#### 场景2: 请假申请

1. 员工提交请假申请
2. 上级审批请假申请
3. 审批通过后更新考勤记录
4. 自动计算请假时长和剩余假期

### 4. 注意事项

- GPS打卡需要开启位置权限
- 支持离线打卡（网络恢复后自动同步）
- 请假申请需要走审批流程
- 考勤数据支持导出Excel报表

---

## 🚪 门禁模块使用指南

### 1. 模块概述

门禁模块提供门禁控制、通行记录、权限管理等功能，支持多种识别方式（刷卡、刷脸、NFC、二维码等）。

### 2. 核心功能

#### 2.1 门禁通行

**功能描述**: 门禁通行验证，支持多种识别方式

**API接口**: `POST /api/v1/access/record/verify`

**请求示例**:
```json
{
  "userId": 1001,
  "deviceId": 3001,
  "verifyType": "FACE",
  "verifyData": "face_template_data"
}
```

**识别类型**:
- `CARD`: 刷卡识别
- `FACE`: 刷脸识别
- `NFC`: NFC识别
- `QRCODE`: 二维码识别
- `FINGERPRINT`: 指纹识别

#### 2.2 权限申请

**功能描述**: 门禁权限申请、审批、授权

**主要接口**:
- `POST /api/v1/access/permission/apply` - 提交权限申请
- `POST /api/v1/access/permission/approve` - 审批权限申请
- `GET /api/v1/access/permission/list` - 查询权限列表

#### 2.3 通行记录

**功能描述**: 查询门禁通行记录

**API接口**: `POST /api/v1/access/record/query`

**请求示例**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "userId": 1001,
  "startDate": "2025-01-01",
  "endDate": "2025-01-31"
}
```

### 3. 使用场景

#### 场景1: 正常通行

1. 员工到达门禁设备前
2. 使用刷卡、刷脸等方式识别身份
3. 系统验证权限
4. 权限验证通过后开门并记录通行记录

#### 场景2: 权限申请

1. 员工提交门禁权限申请
2. 上级审批权限申请
3. 审批通过后自动授权
4. 权限生效后可以正常通行

### 4. 注意事项

- 权限申请需要走审批流程
- 支持临时权限（有效期限制）
- 通行记录实时记录，支持查询和导出
- 支持紧急开门（需特殊权限）

---

## 👥 访客模块使用指南

### 1. 模块概述

访客模块提供访客预约、登记、管理等功能，支持在线预约、现场登记、访客轨迹追踪等。

### 2. 核心功能

#### 2.1 访客预约

**功能描述**: 访客在线预约，支持提前预约和临时预约

**API接口**: `POST /api/v1/visitor/appointment/create`

**请求示例**:
```json
{
  "visitorName": "张三",
  "visitorPhone": "13800138000",
  "visitorIdCard": "110101199001011234",
  "visitDate": "2025-02-01",
  "visitTime": "09:00",
  "hostUserId": 1001,
  "visitPurpose": "商务洽谈",
  "areaIds": [4001, 4002]
}
```

#### 2.2 访客登记

**功能描述**: 访客现场登记，支持人脸识别登记

**API接口**: `POST /api/v1/visitor/record/register`

**请求示例**:
```json
{
  "visitorName": "李四",
  "visitorPhone": "13900139000",
  "visitorIdCard": "110101199002021234",
  "hostUserId": 1001,
  "visitPurpose": "临时访问"
}
```

#### 2.3 访客查询

**功能描述**: 查询访客记录和预约信息

**主要接口**:
- `GET /api/v1/visitor/appointment/{appointmentId}` - 查询预约详情
- `POST /api/v1/visitor/record/query` - 分页查询访客记录

### 3. 使用场景

#### 场景1: 在线预约

1. 访客或接待人员在线提交预约申请
2. 系统自动发送预约确认通知
3. 预约时间到达时，访客到现场登记
4. 登记完成后获得临时门禁权限

#### 场景2: 现场登记

1. 访客到达现场
2. 使用身份证或人脸识别登记
3. 系统验证身份并生成访客记录
4. 发放临时门禁权限

### 4. 注意事项

- 访客预约需要提前申请
- 支持访客轨迹追踪
- 访客离开时自动回收权限
- 支持访客黑名单管理

---

## 📹 视频模块使用指南

### 1. 模块概述

视频模块提供视频监控、录像回放、设备管理等功能，支持实时监控、录像查询、设备控制等。

### 2. 核心功能

#### 2.1 设备查询

**功能描述**: 查询视频设备列表和详情

**API接口**: `GET /api/v1/video/device/list`

**请求示例**:
```
GET /api/v1/video/device/list?areaId=4001&status=ONLINE
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "deviceId": 5001,
        "deviceName": "监控摄像头001",
        "deviceType": "CAMERA",
        "status": "ONLINE",
        "areaId": 4001,
        "location": "园区入口"
      }
    ],
    "total": 1
  }
}
```

#### 2.2 录像回放

**功能描述**: 查询录像记录并支持回放

**API接口**: `POST /api/v1/video/play/query`

**请求示例**:
```json
{
  "deviceId": 5001,
  "startTime": "2025-01-30 09:00:00",
  "endTime": "2025-01-30 18:00:00"
}
```

#### 2.3 设备控制

**功能描述**: 控制视频设备（云台控制、录像控制等）

**主要接口**:
- `POST /api/v1/video/device/control` - 设备控制
- `POST /api/v1/video/device/record/start` - 开始录像
- `POST /api/v1/video/device/record/stop` - 停止录像

### 3. 使用场景

#### 场景1: 实时监控

1. 管理员打开监控页面
2. 选择要查看的设备
3. 实时查看监控画面
4. 支持多画面同时查看

#### 场景2: 录像回放

1. 选择要回放的设备和时间范围
2. 查询录像记录
3. 播放录像内容
4. 支持快进、慢放、截图等功能

### 4. 注意事项

- 视频设备需要在线才能查看实时画面
- 录像数据存储有时间限制（默认30天）
- 支持录像下载和导出
- 设备控制需要特殊权限

---

## 📚 相关文档

- [API接口文档](./API_DOCUMENTATION.md)
- [开发指南](./DEVELOPMENT_GUIDE.md)
- [部署指南](./DEPLOYMENT_GUIDE.md)
- [故障排查指南](./TROUBLESHOOTING_GUIDE.md)

---

**维护人**: IOE-DREAM Team  
**最后更新**: 2025-01-30

