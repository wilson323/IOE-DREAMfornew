# 消费模块前端API接口设计

## 概述

本文档详细描述了IOE-DREAM智能消费管理系统的完整前端API接口设计，包括Web端和移动端的全功能接口支持。系统提供实时消费、账户管理、退款处理、数据分析等核心功能，支持多种支付方式和设备接入。

### 技术架构
- **API协议**: RESTful API + WebSocket实时通信
- **认证方式**: Sa-Token + JWT
- **数据格式**: JSON
- **响应编码**: UTF-8
- **实时通信**: WebSocket双向消息推送
- **支付集成**: 微信支付、支付宝、银联支付

## API 基础配置

### 请求头配置
```http
Content-Type: application/json
Authorization: Bearer ${sa-token}
X-Client-Type: ${client_type} # web/mobile/mini-program/device
X-Device-Id: ${device_id} # 设备唯一标识
X-Platform-Version: ${version}
X-Terminal-Type: ${terminal_type} # POS/WEB/MOBILE/SELF_SERVICE
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
| 40900 | 消费冲突 | 409 |
| 42300 | 账户被锁定 | 423 |
| 42900 | 请求过于频繁 | 429 |
| 50000 | 服务器内部错误 | 500 |
| 90001 | 余额不足 | 400 |
| 90002 | 账户不存在 | 400 |
| 90003 | 设备离线 | 400 |
| 90004 | 支付失败 | 400 |
| 90005 | 退款超期 | 400 |
| 90006 | 重复消费 | 400 |

## 1. 账户管理
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
### 1.1 获取账户信息
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
GET /api/v1/consume/account/info
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "accountId": 1001,
    "accountNo": "ACC001",
    "userId": 1001,
    "userName": "张三",
    "userNo": "EMP001",
    "departmentName": "技术研发部",
    "balance": 1250.50,
    "availableBalance": 1200.50, // 可用余额（扣除冻结金额）
    "frozenAmount": 50.00,
    "accountType": "EMPLOYEE", // EMPLOYEE员工, VISITOR访客, TEMP临时
    "status": "ACTIVE", // ACTIVE正常, FROZEN冻结, CLOSED关闭
    "creditLimit": 500.00,
    "monthlyLimit": 3000.00,
    "dailyLimit": 200.00,
    "usedMonthly": 1250.50,
    "usedDaily": 45.50,
    "lastConsumeTime": "2024-01-02 12:30:00",
    "lastConsumeAmount": 25.50,
    "createdAt": "2023-01-01 09:00:00",
    "updatedAt": "2024-01-02 12:30:00"
  }
}
```

### 1.2 获取账户交易记录
```http
GET /api/v1/consume/account/transactions
```

**查询参数:**
```
accountId=1001
startDate=2024-01-01
endDate=2024-01-31
transactionType=CONSUME,REFUND,RECHARGE,DEDUCTION
status=SUCCESS,FAILED,PENDING
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
      "transactionId": "TXN_20240102_001",
      "accountId": 1001,
      "transactionType": "CONSUME",
      "amount": 25.50,
      "balanceBefore": 1276.00,
      "balanceAfter": 1250.50,
      "status": "SUCCESS",
      "description": "员工餐厅午餐",
      "merchantName": "员工餐厅",
      "merchantId": "M001",
      "deviceName": "POS001",
      "deviceId": "POS001",
      "createTime": "2024-01-02 12:30:00",
      "operatorName": "收银员李四",
      "refTransactionId": null,
      "refundable": true,
      "refundDeadline": "2024-01-09 12:30:00"
    }
  ],
  "pagination": {
    "current": 1,
    "size": 20,
    "total": 156,
    "pages": 8
  }
}
```

### 1.3 账户充值
```http
POST /api/v1/consume/account/recharge
```

**请求参数:**
```json
{
  "accountId": 1001,
  "amount": 100.00,
  "paymentMethod": "WECHAT", // WECHAT微信, ALIPAY支付宝, UNION银联, CASH现金
  "paymentChannel": "MOBILE", // MOBILE手机, WEB网页, POS终端, SELF_SERVICE自助
  "description": "账户充值",
  "payerInfo": {
    "name": "张三",
    "phone": "13800138000"
  }
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "充值订单创建成功",
  "data": {
    "rechargeId": "RCH_20240102_001",
    "orderId": "ORDER_20240102_001",
    "accountId": 1001,
    "amount": 100.00,
    "paymentMethod": "WECHAT",
    "status": "PENDING_PAYMENT", // PENDING_PAYMENT待支付, SUCCESS成功, FAILED失败
    "paymentInfo": {
      "qrCode": "weixin://wxpay/bizpayurl?pr=xxxx",
      "deepLink": "weixin://wxpay/bizpayurl?pr=xxxx",
      "expireTime": "2024-01-02 13:30:00"
    },
    "createTime": "2024-01-02 12:45:00",
    "expireTime": "2024-01-02 13:30:00"
  }
}
```

### 1.4 获取余额变动通知
```http
GET /api/v1/consume/account/balance-notifications
```

**查询参数:**
```
accountId=1001
startDate=2024-01-01
endDate=2024-01-31
notificationType=CONSUME,REFUND,RECHARGE,FREEZE,UNFREEZE
readStatus=false
page=1
size=20
```

## 2. 消费管理

### 2.1 发起消费
```http
POST /api/v1/consume/transaction/create
```

**请求参数:**
```json
{
  "accountId": 1001,
  "amount": 25.50,
  "merchantId": "M001",
  "deviceId": "POS001",
  "description": "员工餐厅午餐",
  "category": "MEAL", // MEAL餐饮, SNACK零食, DRINK饮品, GIFT礼品, OTHER其他
  "consumeMode": "CARD", // CARD刷卡, QR_CODE二维码, FACE人脸, FINGERPRINT指纹
  "consumeLocation": {
    "name": "员工餐厅",
    "address": "公司一楼东侧",
    "latitude": 31.2304,
    "longitude": 121.4737
  },
  "operatorInfo": {
    "operatorId": 2001,
    "operatorName": "收银员李四",
    "operatorRole": "CASHIER"
  },
  "paymentMethod": "BALANCE", // BALANCE余额, CREDIT信用, MIXED混合
  "splitPayment": null, // 分账支付信息
  "extraData": {
    "mealType": "LUNCH",
    "menuItems": [
      {
        "name": "红烧肉套餐",
        "price": 20.00,
        "quantity": 1
      },
      {
        "name": "紫菜蛋花汤",
        "price": 5.50,
        "quantity": 1
      }
    ]
  }
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "消费成功",
  "data": {
    "transactionId": "TXN_20240102_001",
    "accountId": 1001,
    "accountNo": "ACC001",
    "userName": "张三",
    "amount": 25.50,
    "balanceBefore": 1276.00,
    "balanceAfter": 1250.50,
    "merchantName": "员工餐厅",
    "merchantId": "M001",
    "description": "员工餐厅午餐",
    "category": "MEAL",
    "consumeMode": "CARD",
    "deviceName": "POS001",
    "operatorName": "收银员李四",
    "status": "SUCCESS",
    "createTime": "2024-01-02 12:30:00",
    "printInfo": {
      "receiptNo": "RCP20240102001",
      "needPrint": true,
      "printContent": "小票打印内容"
    },
    "notificationInfo": {
      "sendSMS": true,
      "sendEmail": false,
      "sendPush": true
    }
  }
}
```

### 2.2 快速扫码消费
```http
POST /api/v1/consume/transaction/qr-consume
```

**请求参数:**
```json
{
  "qrCode": "QR_CODE_123456", // 账户二维码
  "amount": 15.00,
  "merchantId": "M002",
  "deviceId": "MOBILE_001",
  "description": "咖啡厅消费",
  "category": "DRINK",
  "location": {
    "name": "咖啡厅",
    "address": "公司二楼"
  }
}
```

### 2.3 人脸识别消费
```http
POST /api/v1/consume/transaction/face-consume
```

**请求参数:**
```json
{
  "faceImage": "BASE64_ENCODED_FACE_IMAGE",
  "amount": 10.00,
  "merchantId": "M003",
  "deviceId": "FACE_KIOSK_001",
  "description": "超市购物",
  "category": "GIFT",
  "location": {
    "name": "员工超市",
    "address": "公司一楼西侧"
  },
  "faceFeatures": {
    "featureData": "FEATURE_DATA",
    "matchThreshold": 0.85,
    "livenessCheck": true
  }
}
```

### 2.4 批量消费
```http
POST /api/v1/consume/transaction/batch
```

**请求参数:**
```json
{
  "batchId": "BATCH_20240102_001",
  "description": "部门聚餐结算",
  "transactions": [
    {
      "accountId": 1001,
      "amount": 45.00,
      "description": "张三的消费"
    },
    {
      "accountId": 1002,
      "amount": 38.00,
      "description": "李四的消费"
    }
  ],
  "paymentMethod": "BALANCE",
  "splitPayment": {
    "splitBy": "EQUAL", // EQUAL平均, CUSTOM自定义
    "totalAmount": 83.00,
    "splitDetails": []
  }
}
```

### 2.5 获取消费记录
```http
GET /api/v1/consume/transaction/records
```

**查询参数:**
```
accountId=1001
merchantId=M001
category=MEAL
status=SUCCESS
startDate=2024-01-01
endDate=2024-01-31
page=1
size=20
sortField=createTime
sortOrder=desc
```

## 3. 退款管理

### 3.1 申请退款
```http
POST /api/v1/consume/refund/apply
```

**请求参数:**
```json
{
  "transactionId": "TXN_20240102_001",
  "refundAmount": 25.50, // 全额退款
  "refundReason": "菜品质量问题",
  "refundType": "FULL", // FULL全额, PARTIAL部分
  "applicantInfo": {
    "applicantId": 1001,
    "applicantName": "张三",
    "applicantRole": "CUSTOMER"
  },
  "evidenceFiles": [
    {
      "fileType": "IMAGE",
      "fileName": "菜品问题.jpg",
      "fileUrl": "/files/refund/2024/01/02/evidence_001.jpg",
      "description": "菜品质量问题照片"
    }
  ],
  "contactInfo": {
    "phone": "13800138000",
    "email": "zhangsan@company.com"
  },
  "urgentLevel": "NORMAL", // LOW低, NORMAL普通, HIGH高, URGENT紧急
  "expectedProcessTime": "2024-01-02 18:00:00"
}
```

**响应数据:**
```json
{
  "code": 200,
  "message": "退款申请提交成功",
  "data": {
    "refundId": "REF_20240102_001",
    "applicationNo": "RF-20240102-001",
    "originalTransactionId": "TXN_20240102_001",
    "refundAmount": 25.50,
    "status": "PENDING_REVIEW", // PENDING_REVIEW待审核, APPROVED已批准, REJECTED已拒绝, PROCESSED已处理, CANCELLED已取消
    "submissionTime": "2024-01-02 14:30:00",
    "estimatedProcessTime": "2024-01-02 18:00:00",
    "nextProcessor": {
      "processorId": 3001,
      "processorName": "客服王五",
      "processorRole": "CUSTOMER_SERVICE"
    },
    "refundChannel": "BALANCE", // BALANCE原路返回, CASH现金
    "expectedRefundTime": "2024-01-02 19:00:00"
  }
}
```

### 3.2 审批退款申请
```http
POST /api/v1/consume/refund/approve
```

**请求参数:**
```json
{
  "refundId": "REF_20240102_001",
  "action": "APPROVE", // APPROVE批准, REJECT拒绝
  "comment": "经核实，菜品确实存在质量问题，同意退款",
  "processorInfo": {
    "processorId": 3001,
    "processorName": "客服王五"
  },
  "processResult": {
    "refundAmount": 25.50,
    "refundChannel": "BALANCE",
    "refundReason": "菜品质量问题",
    "processNotes": "已与商户确认，同意全额退款"
  }
}
```

### 3.3 处理退款
```http
POST /api/v1/consume/refund/process
```

**请求参数:**
```json
{
  "refundId": "REF_20240102_001",
  "refundChannel": "BALANCE",
  "refundAmount": 25.50,
  "processMethod": "AUTO", // AUTO自动, MANUAL手动
  "processorInfo": {
    "processorId": 3002,
    "processorName": "财务赵六"
  },
  "verificationInfo": {
    "originalVerified": true,
    "amountVerified": true,
    "accountVerified": true
  }
}
```

### 3.4 获取退款记录
```http
GET /api/v1/consume/refund/records
```

**查询参数:**
```
accountId=1001
transactionId=TXN_20240102_001
status=PENDING_REVIEW,APPROVED,REJECTED,PROCESSED
startDate=2024-01-01
endDate=2024-01-31
page=1
size=20
```

## 4. 设备管理

### 4.1 设备注册
```http
POST /api/v1/consume/device/register
```

**请求参数:**
```json
{
  "deviceName": "POS收银机003",
  "deviceId": "POS003",
  "deviceType": "POS", // POS收银机, SELF_SERVICE自助终端, MOBILE手机, TABLET平板
  "merchantId": "M001",
  "location": {
    "name": "员工餐厅",
    "address": "公司一楼东侧",
    "latitude": 31.2304,
    "longitude": 121.4737
  },
  "deviceConfig": {
    "supportedPayments": ["BALANCE", "WECHAT", "ALIPAY"],
    "consumeModes": ["CARD", "QR_CODE", "FACE"],
    "autoReceipt": true,
    "printReceipt": true,
    "cameraEnabled": true,
    "faceRecognition": true
  },
  "networkInfo": {
    "ipAddress": "192.168.1.103",
    "macAddress": "aa:bb:cc:dd:ee:ff",
    "wifiSSID": "Company_WiFi"
  },
  "operatorInfo": {
    "operatorId": 1001,
    "operatorName": "管理员张三"
  }
}
```

### 4.2 设备心跳上报
```http
POST /api/v1/consume/device/heartbeat
```

**请求参数:**
```json
{
  "deviceId": "POS001",
  "timestamp": 1640995200000,
  "status": "ONLINE", // ONLINE在线, OFFLINE离线, MAINTENANCE维护中, ERROR故障
  "deviceInfo": {
    "cpuUsage": 25.5,
    "memoryUsage": 60.2,
    "diskUsage": 45.8,
    "temperature": 35.2,
    "batteryLevel": 85.5
  },
  "networkStatus": {
    "connected": true,
    "signalStrength": -45,
    "latency": 15
  },
  "serviceStatus": {
    "paymentService": "NORMAL",
    "cardReader": "NORMAL",
    "printer": "NORMAL",
    "camera": "NORMAL"
  },
  "errorInfo": null
}
```

### 4.3 获取设备状态
```http
GET /api/v1/consume/device/status
```

**查询参数:**
```
deviceId=POS001
merchantId=M001
deviceType=POS
status=ONLINE,OFFLINE
page=1
size=20
```

**响应数据:**
```json
{
  "code": 200,
  "data": [
    {
      "deviceId": "POS001",
      "deviceName": "POS收银机001",
      "deviceType": "POS",
      "merchantId": "M001",
      "merchantName": "员工餐厅",
      "status": "ONLINE",
      "lastHeartbeat": "2024-01-02 12:45:00",
      "location": {
        "name": "员工餐厅",
        "address": "公司一楼东侧"
      },
      "deviceInfo": {
        "cpuUsage": 25.5,
        "memoryUsage": 60.2,
        "diskUsage": 45.8,
        "temperature": 35.2
      },
      "todayStats": {
        "transactionCount": 156,
        "totalAmount": 2580.50,
        "averageAmount": 16.54,
        "successRate": 99.4
      },
      "uptime": 99850, // 运行时间（秒）
      "version": "v2.1.0"
    }
  ]
}
```

### 4.4 设备远程控制
```http
POST /api/v1/consume/device/control
```

**请求参数:**
```json
{
  "deviceId": "POS001",
  "command": "RESTART", // RESTART重启, SHUTDOWN关机, UPDATE更新, CONFIGURE配置, RESET重置
  "parameters": {
    "updateUrl": "http://update.ioe-dream.com/pos/v2.1.1.apk",
    "forceRestart": false,
    "saveData": true
  },
  "operatorInfo": {
    "operatorId": 1001,
    "operatorName": "管理员张三",
    "operatorRole": "ADMIN"
  }
}
```

## 5. 商户管理

### 5.1 获取商户列表
```http
GET /api/v1/consume/merchant/list
```

**查询参数:**
```
merchantType=RESTAURANT,SUPERMARKET,Coffee
status=ACTIVE,INACTIVE,SUSPENDED
region=SHANGHAI,BEIJING
page=1
size=20
```

**响应数据:**
```json
{
  "code": 200,
  "data": [
    {
      "merchantId": "M001",
      "merchantName": "员工餐厅",
      "merchantType": "RESTAURANT",
      "status": "ACTIVE",
      "contactInfo": {
        "phone": "021-12345678",
        "email": "restaurant@company.com",
        "address": "公司一楼东侧"
      },
      "businessHours": {
        "openTime": "07:30",
        "closeTime": "21:00",
        "weekends": "08:00-20:00"
      },
      "deviceCount": 3,
      "todayStats": {
        "transactionCount": 156,
        "totalAmount": 2580.50,
        "customerCount": 89
      },
      "managerInfo": {
        "managerId": 2001,
        "managerName": "餐厅经理",
        "phone": "13800138001"
      }
    }
  ]
}
```

### 5.2 商户配置管理
```http
POST /api/v1/consume/merchant/configure
```

**请求参数:**
```json
{
  "merchantId": "M001",
  "configType": "BUSINESS_HOURS", // BUSINESS_HOURS营业时间, PAYMENT_SETTINGS支付设置, REFUND_POLICY退款政策
  "configData": {
    "businessHours": {
      "weekdays": "07:30-21:00",
      "weekends": "08:00-20:00",
      "holidays": "08:00-19:00"
    },
    "paymentSettings": {
      "supportedPayments": ["BALANCE", "WECHAT", "ALIPAY"],
      "maxSingleAmount": 500.00,
      "maxDailyAmount": 2000.00
    },
    "refundPolicy": {
      "refundDeadline": 24, // 退款期限（小时）
      "autoRefundThreshold": 50.00, // 自动退款阈值
      "requireApproval": false // 小额退款是否需要审批
    }
  },
  "operatorInfo": {
    "operatorId": 1001,
    "operatorName": "管理员张三"
  }
}
```

## 6. 统计报表

### 6.1 获取消费统计报表
```http
GET /api/v1/consume/report/consumption
```

**查询参数:**
```
startDate=2024-01-01
endDate=2024-01-31
merchantId=M001
category=MEAL,DRINK,SNACK
groupBy=DAY,WEEK,MONTH
dimension=USER,MERCHANT,DEVICE
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
      "totalTransactions": 4856,
      "totalAmount": 98562.50,
      "averageAmount": 20.30,
      "uniqueCustomers": 1234,
      "peakHour": "12:30-13:30",
      "peakDay": "2024-01-15"
    },
    "categoryStats": [
      {
        "category": "MEAL",
        "transactionCount": 3567,
        "totalAmount": 71340.00,
        "averageAmount": 20.00,
        "percentage": 72.4
      },
      {
        "category": "DRINK",
        "transactionCount": 856,
        "totalAmount": 17120.00,
        "averageAmount": 20.00,
        "percentage": 17.4
      }
    ],
    "merchantStats": [
      {
        "merchantId": "M001",
        "merchantName": "员工餐厅",
        "transactionCount": 2156,
        "totalAmount": 43120.00,
        "averageAmount": 20.00
      }
    ],
    "timeSeriesData": [
      {
        "date": "2024-01-01",
        "transactionCount": 156,
        "totalAmount": 3120.00,
        "uniqueCustomers": 89
      }
    ]
  }
}
```

### 6.2 获取账户消费分析
```http
GET /api/v1/consume/report/account-analysis
```

**查询参数:**
```
accountId=1001
startDate=2024-01-01
endDate=2024-01-31
analysisType=CONSUMPTION_PATTERN
```

### 6.3 获取设备使用统计
```http
GET /api/v1/consume/report/device-stats
```

**查询参数:**
```
deviceId=POS001
merchantId=M001
startDate=2024-01-01
endDate=2024-01-31
```

## 7. 移动端专用接口

### 7.1 生成账户二维码
```http
GET /api/v1/consume/mobile/account/qrcode
```

**查询参数:**
```
accountId=1001
qrType=ACCOUNT
expireTime=300
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
    "qrContent": "QR_CODE_CONTENT_123456",
    "expireTime": "2024-01-02 13:00:00",
    "accountInfo": {
      "accountNo": "ACC001",
      "userName": "张三",
      "balance": 1250.50
    }
  }
}
```

### 7.2 扫码支付
```http
POST /api/v1/consume/mobile/scan-pay
```

**请求参数:**
```json
{
  "qrCode": "QR_CODE_CONTENT_123456",
  "amount": 15.00,
  "description": "咖啡厅消费",
  "location": {
    "name": "咖啡厅",
    "address": "公司二楼",
    "latitude": 31.2304,
    "longitude": 121.4737
  },
  "paymentMethod": "BALANCE",
  "extraData": {
    "tipAmount": 2.00,
    "notes": "香草拿铁"
  }
}
```

### 7.3 NFC支付
```http
POST /api/v1/consume/mobile/nfc-pay
```

**请求参数:**
```json
{
  "nfcData": "NFC_DATA_123456",
  "amount": 20.00,
  "description": "超市购物",
  "location": {
    "name": "员工超市",
    "address": "公司一楼西侧"
  }
}
```

### 7.4 获取消费历史图表
```http
GET /api/v1/consume/mobile/consumption-chart
```

**查询参数:**
```
accountId=1001
period=WEEK,MONTH,YEAR
chartType=LINE,BAR,PIE
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "chartType": "LINE",
    "period": "WEEK",
    "datasets": [
      {
        "label": "消费金额",
        "data": [
          { "date": "2024-01-01", "value": 45.50 },
          { "date": "2024-01-02", "value": 25.50 },
          { "date": "2024-01-03", "value": 38.00 }
        ],
        "color": "#1989fa"
      }
    ],
    "summary": {
      "totalAmount": 109.00,
      "averageDaily": 36.33,
      "maxAmount": 45.50,
      "minAmount": 25.50
    }
  }
}
```

### 7.5 获取附近商户
```http
GET /api/v1/consume/mobile/nearby-merchants
```

**查询参数:**
```
latitude=31.2304
longitude=121.4737
radius=1000
merchantType=RESTAURANT,COFFEE,SUPERMARKET
openNow=true
```

**响应数据:**
```json
{
  "code": 200,
  "data": [
    {
      "merchantId": "M001",
      "merchantName": "员工餐厅",
      "merchantType": "RESTAURANT",
      "distance": 50.5,
      "address": "公司一楼东侧",
      "isOpen": true,
      "businessHours": "07:30-21:00",
      "averagePrice": 20.00,
      "rating": 4.5,
      "imageUrl": "/images/merchants/restaurant.jpg"
    }
  ]
}
```

## 8. WebSocket 实时推送

### 8.1 连接WebSocket
```
ws://localhost:8080/ws/consume/{userId}?token={sa-token}
```

### 8.2 消息类型

#### 8.2.1 交易成功通知
```json
{
  "type": "TRANSACTION_SUCCESS",
  "timestamp": 1640995200000,
  "data": {
    "transactionId": "TXN_20240102_001",
    "accountId": 1001,
    "amount": 25.50,
    "balanceAfter": 1250.50,
    "merchantName": "员工餐厅",
    "description": "员工餐厅午餐",
    "message": "您已成功消费25.50元，当前余额1250.50元"
  }
}
```

#### 8.2.2 余额变动通知
```json
{
  "type": "BALANCE_CHANGE",
  "timestamp": 1640995200000,
  "data": {
    "accountId": 1001,
    "balanceBefore": 1276.00,
    "balanceAfter": 1250.50,
    "changeAmount": -25.50,
    "changeType": "CONSUME",
    "transactionId": "TXN_20240102_001",
    "message": "您的账户余额已变动"
  }
}
```

#### 8.2.3 退款状态变更
```json
{
  "type": "REFUND_STATUS_CHANGE",
  "timestamp": 1640995200000,
  "data": {
    "refundId": "REF_20240102_001",
    "status": "PROCESSED",
    "refundAmount": 25.50,
    "originalTransactionId": "TXN_20240102_001",
    "processorName": "财务赵六",
    "message": "您的退款申请已处理完成"
  }
}
```

#### 8.2.4 设备状态变更
```json
{
  "type": "DEVICE_STATUS_CHANGE",
  "timestamp": 1640995200000,
  "data": {
    "deviceId": "POS001",
    "deviceName": "POS收银机001",
    "status": "OFFLINE",
    "lastOnlineTime": "2024-01-02 12:45:00",
    "merchantName": "员工餐厅",
    "message": "设备已离线，请检查网络连接"
  }
}
```

#### 8.2.5 大额消费预警
```json
{
  "type": "LARGE_AMOUNT_ALERT",
  "timestamp": 1640995200000,
  "data": {
    "transactionId": "TXN_20240102_002",
    "accountId": 1001,
    "amount": 450.00,
    "threshold": 300.00,
    "merchantName": "员工超市",
    "description": "大额购物消费",
    "message": "检测到您有一笔大额消费450.00元，请确认是否本人操作"
  }
}
```

## 9. 批量操作接口

### 9.1 批量导入账户
```http
POST /api/v1/consume/batch/import-accounts
Content-Type: multipart/form-data
```

**请求参数:**
```
file: 账户Excel文件
type: ACCOUNT_IMPORT
validateOnly: true
sendNotification: false
```

### 9.2 批量导入设备
```http
POST /api/v1/consume/batch/import-devices
Content-Type: multipart/form-data
```

### 9.3 批量退款处理
```http
POST /api/v1/consume/batch/refund
```

**请求参数:**
```json
{
  "refundIds": ["REF_20240102_001", "REF_20240102_002"],
  "action": "APPROVE", // APPROVE批准, REJECT拒绝
  "comment": "批量处理退款申请",
  "operatorInfo": {
    "operatorId": 3001,
    "operatorName": "客服王五"
  }
}
```

### 9.4 批量设备控制
```http
POST /api/v1/consume/batch/device-control
```

**请求参数:**
```json
{
  "deviceIds": ["POS001", "POS002", "POS003"],
  "command": "RESTART",
  "parameters": {
    "forceRestart": false,
    "saveData": true
  }
}
```

## 10. 系统配置接口

### 10.1 获取消费规则配置
```http
GET /api/v1/consume/config/rules
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "consumeRules": {
      "maxSingleAmount": 500.00,
      "maxDailyAmount": 2000.00,
      "maxMonthlyAmount": 10000.00,
      "consumeInterval": 30, // 消费间隔（秒）
      "allowNegativeBalance": false,
      "autoFreezeThreshold": 1000.00
    },
    "refundRules": {
      "refundDeadline": 24, // 退款期限（小时）
      "autoRefundThreshold": 50.00,
      "requireApproval": true,
      "approvalAmount": 100.00
    },
    "paymentRules": {
      "supportedPayments": ["BALANCE", "WECHAT", "ALIPAY"],
      "allowSplitPayment": true,
      "splitPaymentLimit": 3,
      "creditEnabled": true,
      "creditLimit": 500.00
    },
    "notificationRules": {
      "transactionNotification": true,
      "balanceAlert": true,
      "largeAmountAlert": true,
      "deviceOfflineAlert": true
    }
  }
}
```

### 10.2 更新系统配置
```http
POST /api/v1/consume/config/update
```

**请求参数:**
```json
{
  "configType": "CONSUME_RULES",
  "configData": {
    "maxSingleAmount": 800.00,
    "maxDailyAmount": 3000.00,
    "consumeInterval": 20
  },
  "operatorInfo": {
    "operatorId": 1001,
    "operatorName": "系统管理员"
  }
}
```

## 11. 移动端支付集成

### 11.1 微信支付
```http
POST /api/v1/consume/payment/wechat
```

**请求参数:**
```json
{
  "transactionId": "TXN_20240102_001",
  "amount": 25.50,
  "description": "员工餐厅午餐",
  "openid": "USER_OPENID",
  "tradeType": "JSAPI", // JSAPI公众号支付, NATIVE扫码支付, APP APP支付
  "extraData": {
    "attach": "消费支付",
    "detail": "员工餐厅午餐消费"
  }
}
```

**响应数据:**
```json
{
  "code": 200,
  "data": {
    "paymentId": "PAY_20240102_001",
    "appId": "wx1234567890",
    "timeStamp": "1640995200",
    "nonceStr": "RANDOM_STRING",
    "package": "prepay_id=wx123456789",
    "signType": "RSA",
    "paySign": "SIGNATURE",
    "expireTime": "2024-01-02 13:30:00"
  }
}
```

### 11.2 支付宝支付
```http
POST /api/v1/consume/payment/alipay
```

### 11.3 银联支付
```http
POST /api/v1/consume/payment/unionpay
```

## 12. 移动端离线功能

### 12.1 离线支付
```http
POST /api/v1/consume/mobile/offline-pay
```

**请求参数:**
```json
{
  "accountId": 1001,
  "amount": 15.00,
  "description": "离线消费",
  "offlineMode": true,
  "signature": "OFFLINE_SIGNATURE",
  "nonce": "RANDOM_NONCE",
  "timestamp": 1640995200000,
  "location": {
    "name": "咖啡厅",
    "latitude": 31.2304,
    "longitude": 121.4737
  }
}
```

### 12.2 离线数据同步
```http
POST /api/v1/consume/mobile/sync
```

**请求参数:**
```json
{
  "lastSyncTime": 1640908800000,
  "offlineTransactions": [
    {
      "transactionId": "OFFLINE_TXN_001",
      "accountId": 1001,
      "amount": 15.00,
      "timestamp": 1640995200000,
      "signature": "OFFLINE_SIGNATURE"
    }
  ]
}
```

---

## 接口权限矩阵

| 功能模块 | 用户 | 收银员 | 商户管理员 | 系统管理员 |
|---------|------|--------|-----------|-----------|
| 账户查询 | ✓(本人) | ✓ | ✓(本商户) | ✓ |
| 消费记录 | ✓(本人) | ✓ | ✓(本商户) | ✓ |
| 发起消费 | ✓ | ✓ | ✗ | ✗ |
| 账户充值 | ✓ | ✓ | ✗ | ✓ |
| 申请退款 | ✓(本人) | ✓ | ✓ | ✓ |
| 审批退款 | ✗ | ✓ | ✓ | ✓ |
| 设备管理 | ✗ | ✗ | ✓ | ✓ |
| 商户管理 | ✗ | ✗ | ✓(本人) | ✓ |
| 统计报表 | ✓(本人) | ✓(本商户) | ✓(本商户) | ✓ |
| 系统配置 | ✗ | ✗ | ✗ | ✓ |

---

## 版本说明

- **当前版本**: v2.0.0
- **发布日期**: 2024-01-15
- **兼容性**: 向下兼容v1.x版本
- **更新内容**:
  - 新增移动端支付集成接口
  - 增强设备管理和控制功能
  - 优化批量操作性能
  - 完善离线支付和同步机制
  - 新增商户管理功能

---

## 技术支持

如有API使用问题，请联系：
- **技术支持**: tech-support@ioe-dream.com
- **API文档**: https://api.ioe-dream.com/docs/consume
- **SDK下载**: https://github.com/ioe-dream/sdks
- **问题反馈**: https://github.com/ioe-dream/issues