# IOE-DREAM 消费服务 API 文档

## 服务概述

**服务名称**: ioedream-consume-service
**服务端口**: 8094
**版本**: v1.0.0
**描述**: 企业级消费管理微服务，提供产品管理、充值管理、补贴管理、设备管理等功能

## 基础信息

- **Base URL**: `http://localhost:8094/api/v1`
- **Content-Type**: `application/json`
- **字符编码**: `UTF-8`
- **认证方式**: JWT Token

## 通用响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1640150400000
}
```

### 响应码说明

| 响应码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 1. 产品管理 API

### 1.1 产品分类管理

#### 1.1.1 获取产品分类列表

**GET** `/consume/meal-categories`

**查询参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| parentId | Long | 否 | 父分类ID，null查询根分类 |
| includeChildren | Boolean | 否 | 是否包含子分类，默认false |
| status | Integer | 否 | 分类状态：1-启用，0-禁用 |

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "categoryId": 1,
        "categoryCode": "BREAKFAST",
        "categoryName": "早餐",
        "parentId": null,
        "categoryLevel": 1,
        "sortOrder": 1,
        "categoryIcon": "breakfast-icon.png",
        "categoryColor": "#FF6B6B",
        "isSystem": 1,
        "categoryStatus": 1,
        "availableTimePeriods": ["06:00-10:00"],
        "remark": "早餐分类",
        "createTime": "2025-12-21T10:00:00",
        "updateTime": "2025-12-21T10:00:00",
        "children": []
      }
    ],
    "total": 1
  }
}
```

#### 1.1.2 获取产品分类详情

**GET** `/consume/meal-categories/{categoryId}`

**路径参数:**

| 参数 | 类型 | 说明 |
|------|------|------|
| categoryId | Long | 分类ID |

#### 1.1.3 创建产品分类

**POST** `/consume/meal-categories`

**请求体:**
```json
{
  "categoryCode": "LUNCH",
  "categoryName": "午餐",
  "parentId": null,
  "categoryLevel": 1,
  "sortOrder": 2,
  "categoryIcon": "lunch-icon.png",
  "categoryColor": "#4ECDC4",
  "isSystem": 0,
  "availableTimePeriods": ["11:00-14:00"],
  "remark": "午餐分类"
}
```

#### 1.1.4 更新产品分类

**PUT** `/consume/meal-categories/{categoryId}`

#### 1.1.5 删除产品分类

**DELETE** `/consume/meal-categories/{categoryId}`

### 1.2 产品管理

#### 1.2.1 获取产品列表

**GET** `/consume/products`

**查询参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | Long | 否 | 分类ID |
| productName | String | 否 | 产品名称（模糊查询） |
| productStatus | Integer | 否 | 产品状态：1-上架，0-下架 |
| minPrice | BigDecimal | 否 | 最低价格 |
| maxPrice | BigDecimal | 否 | 最高价格 |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页大小，默认20 |

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "productId": 1,
        "productCode": "PROD_001",
        "productName": "营养早餐套餐",
        "productCategory": 1,
        "categoryName": "早餐",
        "basePrice": "15.00",
        "salePrice": "12.00",
        "costPrice": "8.00",
        "productStatus": 1,
        "stockQuantity": 100,
        "allowDiscount": true,
        "maxDiscountRate": "0.3",
        "saleTimePeriods": ["06:00-10:00"],
        "productImage": "breakfast-set.jpg",
        "productDescription": "营养丰富的早餐套餐",
        "productTags": "营养,早餐,热销",
        "ratingAverage": "4.5",
        "ratingCount": 150,
        "createTime": "2025-12-21T10:00:00",
        "updateTime": "2025-12-21T10:00:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

#### 1.2.2 获取产品详情

**GET** `/consume/products/{productId}`

#### 1.2.3 创建产品

**POST** `/consume/products`

**请求体:**
```json
{
  "productCode": "PROD_002",
  "productName": "健康午餐套餐",
  "productCategory": 2,
  "basePrice": "25.00",
  "salePrice": "20.00",
  "costPrice": "15.00",
  "stockQuantity": 50,
  "allowDiscount": true,
  "maxDiscountRate": "0.2",
  "saleTimePeriods": ["11:00-14:00"],
  "productImage": "lunch-set.jpg",
  "productDescription": "健康均衡的午餐套餐",
  "productTags": "健康,午餐,推荐"
}
```

#### 1.2.4 更新产品

**PUT** `/consume/products/{productId}`

#### 1.2.5 删除产品

**DELETE** `/consume/products/{productId}`

#### 1.2.6 产品上架/下架

**PUT** `/consume/products/{productId}/status`

**请求体:**
```json
{
  "productStatus": 1
}
```

#### 1.2.7 获取产品库存统计

**GET** `/consume/products/stock-statistics`

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalProducts": 50,
    "totalStock": 5000,
    "lowStockCount": 5,
    "lowStockProducts": [
      {
        "productId": 1,
        "productName": "即将售罄产品",
        "stockQuantity": 5
      }
    ]
  }
}
```

---

## 2. 充值管理 API

### 2.1 充值记录管理

#### 2.1.1 获取充值记录列表

**GET** `/consume/recharges`

**查询参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 否 | 用户ID |
| userName | String | 否 | 用户姓名（模糊查询） |
| rechargeWay | Integer | 否 | 充值方式：1-现金，2-微信，3-支付宝，4-银行卡，5-转账 |
| rechargeStatus | Integer | 否 | 充值状态：1-成功，2-失败，3-处理中，4-已冲正 |
| rechargeChannel | Integer | 否 | 充值渠道：1-线上，2-线下 |
| startTime | String | 否 | 开始时间（yyyy-MM-dd HH:mm:ss） |
| endTime | String | 否 | 结束时间（yyyy-MM-dd HH:mm:ss） |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页大小，默认20 |

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "recordId": 1,
        "userId": 1001,
        "userName": "张三",
        "rechargeAmount": "100.00",
        "beforeBalance": "50.00",
        "afterBalance": "150.00",
        "rechargeWay": 2,
        "rechargeWayName": "微信",
        "rechargeChannel": 1,
        "rechargeChannelName": "线上",
        "transactionNo": "RCG202512212210001001",
        "thirdPartyNo": "WX123456789",
        "rechargeStatus": 1,
        "rechargeStatusName": "成功",
        "rechargeTime": "2025-12-21T10:30:00",
        "arrivalTime": "2025-12-21T10:30:05",
        "deviceId": 1,
        "deviceCode": "POS_001",
        "operatorId": 1,
        "operatorName": "收银员",
        "rechargeDescription": "微信充值",
        "remark": "正常充值",
        "batchNo": null,
        "auditStatus": 2,
        "auditStatusName": "已审核",
        "auditorId": 2,
        "auditorName": "财务",
        "auditTime": "2025-12-21T11:00:00",
        "auditRemark": "审核通过"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

#### 2.1.2 获取充值记录详情

**GET** `/consume/recharges/{recordId}`

#### 2.1.3 创建充值记录

**POST** `/consume/recharges`

**请求体:**
```json
{
  "userId": 1002,
  "userName": "李四",
  "rechargeAmount": "200.00",
  "beforeBalance": "0.00",
  "rechargeWay": 3,
  "rechargeChannel": 1,
  "thirdPartyNo": "ALIPAY123456",
  "deviceId": 1,
  "deviceCode": "POS_002",
  "operatorId": 1,
  "operatorName": "收银员",
  "rechargeDescription": "支付宝充值",
  "remark": "首次充值"
}
```

#### 2.1.4 批量充值

**POST** `/consume/recharges/batch`

**请求体:**
```json
{
  "userIds": [1001, 1002, 1003],
  "amount": "100.00",
  "rechargeWay": "微信",
  "remark": "月度补贴充值"
}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "message": "批量充值成功",
    "totalCount": 3,
    "successCount": 3,
    "failedCount": 0,
    "successIds": [1001, 1002, 1003],
    "failedUsers": [],
    "totalAmount": "300.00",
    "timestamp": "2025-12-21T14:30:00"
  }
}
```

#### 2.1.5 充值记录冲正

**POST** `/consume/recharges/{recordId}/reverse`

**请求体:**
```json
{
  "reason": "用户误操作，需要冲正"
}
```

### 2.2 充值统计

#### 2.2.1 获取充值统计

**GET** `/consume/recharges/statistics`

**查询参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 否 | 用户ID |
| startDate | String | 否 | 开始日期（yyyy-MM-dd） |
| endDate | String | 否 | 结束日期（yyyy-MM-dd） |

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalCount": 100,
    "totalAmount": "10000.00",
    "successCount": 95,
    "successAmount": "9500.00",
    "failureCount": 5,
    "processingCount": 0,
    "successRate": "95.0000",
    "averageAmount": "100.00",
    "maxAmount": "1000.00",
    "minAmount": "10.00",
    "todayCount": 5,
    "todayAmount": "500.00",
    "monthCount": 25,
    "monthAmount": "2500.00",
    "statisticsTime": "2025-12-21T15:00:00"
  }
}
```

#### 2.2.2 获取充值方式统计

**GET** `/consume/recharges/statistics/methods`

**查询参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | String | 是 | 开始日期（yyyy-MM-dd） |
| endDate | String | 是 | 结束日期（yyyy-MM-dd） |

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "rechargeWayStatistics": [
      {
        "rechargeWay": 2,
        "rechargeWayName": "微信",
        "count": 50,
        "amount": "5000.00"
      },
      {
        "rechargeWay": 3,
        "rechargeWayName": "支付宝",
        "count": 30,
        "amount": "3000.00"
      }
    ],
    "rechargeChannelStatistics": [
      {
        "rechargeChannel": 1,
        "rechargeChannelName": "线上",
        "count": 70,
        "amount": "7000.00"
      },
      {
        "rechargeChannel": 2,
        "rechargeChannelName": "线下",
        "count": 30,
        "amount": "3000.00"
      }
    ]
  }
}
```

---

## 3. 补贴管理 API

### 3.1 补贴管理

#### 3.1.1 获取补贴列表

**GET** `/consume/subsidies`

**查询参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 否 | 用户ID |
| userName | String | 否 | 用户姓名（模糊查询） |
| subsidyType | Integer | 否 | 补贴类型：1-餐饮补贴，2-交通补贴，3-通讯补贴 |
| subsidyStatus | Integer | 否 | 补贴状态：1-待发放，2-已发放，3-已过期，4-已使用，5-已作废 |
| startTime | String | 否 | 开始时间（yyyy-MM-dd HH:mm:ss） |
| endTime | String | 否 | 结束时间（yyyy-MM-dd HH:mm:ss） |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页大小，默认20 |

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "subsidyId": 1,
        "subsidyCode": "SUBSIDY_001",
        "subsidyName": "12月餐饮补贴",
        "userId": 1001,
        "userName": "张三",
        "subsidyType": 1,
        "subsidyTypeName": "餐饮补贴",
        "subsidyPeriod": 1,
        "subsidyPeriodName": "月度",
        "subsidyAmount": "300.00",
        "usedAmount": "120.50",
        "remainingAmount": "179.50",
        "subsidyStatus": 2,
        "subsidyStatusName": "已发放",
        "effectiveDate": "2025-12-01T00:00:00",
        "expiryDate": "2025-12-31T23:59:59",
        "issueDate": "2025-12-01T09:00:00",
        "issuerId": 1,
        "issuerName": "人力资源部",
        "applicableMerchants": ["食堂", "餐厅"],
        "usageTimePeriods": ["07:00-20:00"],
        "dailyLimit": "50.00",
        "dailyUsedAmount": "25.00",
        "dailyUsageDate": "2025-12-21",
        "maxDiscountRate": "1.00",
        "autoRenew": false,
        "remark": "12月员工餐饮补贴",
        "createTime": "2025-12-01T08:00:00",
        "updateTime": "2025-12-21T15:30:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

#### 3.1.2 获取补贴详情

**GET** `/consume/subsidies/{subsidyId}`

#### 3.1.3 创建补贴

**POST** `/consume/subsidies`

**请求体:**
```json
{
  "subsidyCode": "SUBSIDY_002",
  "subsidyName": "1月餐饮补贴",
  "userId": 1002,
  "userName": "李四",
  "subsidyType": 1,
  "subsidyPeriod": 1,
  "subsidyAmount": "300.00",
  "effectiveDate": "2025-01-01T00:00:00",
  "expiryDate": "2025-01-31T23:59:59",
  "applicableMerchants": ["食堂", "餐厅"],
  "usageTimePeriods": ["07:00-20:00"],
  "dailyLimit": "50.00",
  "autoRenew": false,
  "remark": "1月员工餐饮补贴"
}
```

#### 3.1.4 更新补贴

**PUT** `/consume/subsidies/{subsidyId}`

#### 3.1.5 删除补贴

**DELETE** `/consume/subsidies/{subsidyId}`

#### 3.1.6 补贴发放

**POST** `/consume/subsidies/{subsidyId}/issue`

**请求体:**
```json
{
  "issueDate": "2025-12-21T16:00:00"
}
```

#### 3.1.7 批量发放补贴

**POST** `/consume/subsidies/batch-issue`

**请求体:**
```json
{
  "subsidyIds": [1, 2, 3],
  "issuerId": 1,
  "issuerName": "人力资源部"
}
```

#### 3.1.8 补贴使用

**POST** `/consume/subsidies/{subsidyId}/use`

**请求体:**
```json
{
  "amount": "25.50",
  "consumeRecordId": 12345
}
```

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "message": "使用成功",
    "usedAmount": "25.50",
    "remainingAmount": "274.50"
  }
}
```

### 3.2 补贴统计

#### 3.2.1 获取补贴统计

**GET** `/consume/subsidies/statistics`

**查询参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | String | 否 | 开始日期（yyyy-MM-dd） |
| endDate | String | 否 | 结束日期（yyyy-MM-dd） |
| userId | Long | 否 | 用户ID |
| departmentId | Long | 否 | 部门ID |

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalSubsidies": 100,
    "totalAmount": "30000.00",
    "usedAmount": "15000.00",
    "remainingAmount": "15000.00",
    "issuedCount": 80,
    "expiredCount": 5,
    "usedCount": 75,
    "typeStatistics": [
      {
        "subsidyType": 1,
        "subsidyTypeName": "餐饮补贴",
        "count": 60,
        "amount": "18000.00"
      }
    ],
    "departmentStatistics": [
      {
        "departmentId": 1,
        "departmentName": "技术部",
        "count": 30,
        "amount": "9000.00"
      }
    ]
  }
}
```

#### 3.2.2 获取用户补贴汇总

**GET** `/consume/subsidies/user-summary/{userId}`

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalSubsidies": 5,
    "totalAmount": "1500.00",
    "usedAmount": "750.50",
    "remainingAmount": "749.50",
    "expiringSoonCount": 2,
    "expiringSoon": [
      {
        "subsidyId": 1,
        "subsidyName": "即将过期补贴",
        "expiryDate": "2025-12-25T23:59:59",
        "remainingAmount": "100.00"
      }
    ],
    "nearlyDepletedCount": 1,
    "nearlyDepleted": [
      {
        "subsidyId": 2,
        "subsidyName": "即将用完补贴",
        "remainingAmount": "20.00"
      }
    ]
  }
}
```

---

## 4. 设备管理 API

### 4.1 消费设备管理

#### 4.1.1 获取设备列表

**GET** `/consume/devices`

**查询参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| deviceCode | String | 否 | 设备编码（模糊查询） |
| deviceName | String | 否 | 设备名称（模糊查询） |
| deviceType | Integer | 否 | 设备类型：1-POS机，2-自助终端，3-移动终端 |
| deviceStatus | Integer | 否 | 设备状态：1-在线，2-离线，3-故障，4-维护中 |
| locationId | Long | 否 | 位置ID |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页大小，默认20 |

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "deviceId": 1,
        "deviceCode": "POS_001",
        "deviceName": "餐厅1号POS机",
        "deviceType": 1,
        "deviceTypeName": "POS机",
        "deviceModel": "PAX_A920",
        "deviceStatus": 1,
        "deviceStatusName": "在线",
        "locationId": 1,
        "locationName": "1号餐厅",
        "ipAddress": "192.168.1.101",
        "macAddress": "AA:BB:CC:DD:EE:FF",
        "lastHeartbeatTime": "2025-12-21T15:45:00",
        "configuration": {
          "printReceipt": true,
          "allowDiscount": true,
          "maxAmount": "1000.00"
        },
        "installDate": "2025-01-15T00:00:00",
        "manufacturer": "PAX",
        "serialNumber": "SN123456789",
        "softwareVersion": "v2.1.0",
        "remark": "主收银台POS机",
        "createTime": "2025-01-15T10:00:00",
        "updateTime": "2025-12-21T15:45:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

#### 4.1.2 获取设备详情

**GET** `/consume/devices/{deviceId}`

#### 4.1.3 创建设备

**POST** `/consume/devices`

**请求体:**
```json
{
  "deviceCode": "POS_002",
  "deviceName": "餐厅2号POS机",
  "deviceType": 1,
  "deviceModel": "PAX_A920",
  "locationId": 1,
  "ipAddress": "192.168.1.102",
  "macAddress": "BB:CC:DD:EE:FF:AA",
  "configuration": {
    "printReceipt": true,
    "allowDiscount": true,
    "maxAmount": "1000.00"
  },
  "installDate": "2025-12-21T16:00:00",
  "manufacturer": "PAX",
  "serialNumber": "SN987654321",
  "softwareVersion": "v2.1.0",
  "remark": "备用收银台POS机"
}
```

#### 4.1.4 更新设备

**PUT** `/consume/devices/{deviceId}`

#### 4.1.5 删除设备

**DELETE** `/consume/devices/{deviceId}`

#### 4.1.6 设备状态更新

**PUT** `/consume/devices/{deviceId}/status`

**请求体:**
```json
{
  "deviceStatus": 3,
  "remark": "设备故障，需要维修"
}
```

#### 4.1.7 设备配置更新

**PUT** `/consume/devices/{deviceId}/configuration`

**请求体:**
```json
{
  "printReceipt": false,
  "allowDiscount": true,
  "maxAmount": "500.00"
}
```

#### 4.1.8 设备心跳上报

**POST** `/consume/devices/{deviceId}/heartbeat`

**请求体:**
```json
{
  "deviceStatus": 1,
  "systemInfo": {
    "cpuUsage": 15.5,
    "memoryUsage": 45.2,
    "diskUsage": 23.8,
    "temperature": 35.6
  },
  "networkInfo": {
    "signalStrength": 85,
    "networkType": "WiFi"
  }
}
```

### 4.2 设备统计

#### 4.2.1 获取设备统计

**GET** `/consume/devices/statistics`

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalDevices": 20,
    "onlineDevices": 18,
    "offlineDevices": 1,
    "faultDevices": 1,
    "maintenanceDevices": 0,
    "onlineRate": 90.0,
    "deviceTypeStatistics": [
      {
        "deviceType": 1,
        "deviceTypeName": "POS机",
        "count": 15
      },
      {
        "deviceType": 2,
        "deviceTypeName": "自助终端",
        "count": 5
      }
    ],
    "locationStatistics": [
      {
        "locationId": 1,
        "locationName": "1号餐厅",
        "deviceCount": 10
      }
    ]
  }
}
```

---

## 5. 消费记录 API

### 5.1 消费记录管理

#### 5.1.1 获取消费记录列表

**GET** `/consume/records`

**查询参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 否 | 用户ID |
| userName | String | 否 | 用户姓名（模糊查询） |
| deviceId | Long | 否 | 设备ID |
| categoryId | Long | 否 | 产品分类ID |
| productId | Long | 否 | 产品ID |
| startTime | String | 否 | 开始时间（yyyy-MM-dd HH:mm:ss） |
| endTime | String | 否 | 结束时间（yyyy-MM-dd HH:mm:ss） |
| minAmount | BigDecimal | 否 | 最小金额 |
| maxAmount | BigDecimal | 否 | 最大金额 |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页大小，默认20 |

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "recordId": 1,
        "userId": 1001,
        "userName": "张三",
        "deviceId": 1,
        "deviceCode": "POS_001",
        "categoryId": 1,
        "categoryName": "早餐",
        "productId": 1,
        "productName": "营养早餐套餐",
        "originalAmount": "15.00",
        "discountAmount": "3.00",
        "subsidyAmount": "5.00",
        "actualAmount": "7.00",
        "paymentMethod": 2,
        "paymentMethodName": "余额支付",
        "consumeTime": "2025-12-21T08:30:00",
        "transactionNo": "CONSUME202512210830001",
        "operatorId": 1,
        "operatorName": "收银员",
        "remark": "正常消费",
        "createTime": "2025-12-21T08:30:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 20
  }
}
```

#### 5.1.2 获取消费记录详情

**GET** `/consume/records/{recordId}`

#### 5.1.3 创建消费记录

**POST** `/consume/records`

**请求体:**
```json
{
  "userId": 1002,
  "deviceId": 1,
  "productId": 1,
  "originalAmount": "15.00",
  "discountRate": "0.2",
  "subsidyAmount": "5.00",
  "paymentMethod": 2,
  "operatorId": 1,
  "remark": "早餐消费"
}
```

#### 5.1.4 消费记录退款

**POST** `/consume/records/{recordId}/refund`

**请求体:**
```json
{
  "refundAmount": "15.00",
  "refundReason": "用户要求退款",
  "refundType": 1
}
```

### 5.2 消费统计

#### 5.2.1 获取消费统计

**GET** `/consume/records/statistics`

**查询参数:**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | String | 否 | 开始日期（yyyy-MM-dd） |
| endDate | String | 否 | 结束日期（yyyy-MM-dd） |
| userId | Long | 否 | 用户ID |
| deviceId | Long | 否 | 设备ID |
| categoryId | Long | 否 | 产品分类ID |

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalRecords": 500,
    "totalAmount": "15000.00",
    "totalDiscount": "1200.00",
    "totalSubsidy": "3000.00",
    "actualAmount": "10800.00",
    "averageAmount": "30.00",
    "maxAmount": "200.00",
    "minAmount": "5.00",
    "todayRecords": 25,
    "todayAmount": "750.00",
    "categoryStatistics": [
      {
        "categoryId": 1,
        "categoryName": "早餐",
        "recordCount": 200,
        "amount": "4000.00"
      }
    ],
    "deviceStatistics": [
      {
        "deviceId": 1,
        "deviceCode": "POS_001",
        "recordCount": 300,
        "amount": "9000.00"
      }
    ],
    "hourlyDistribution": [
      {
        "hour": 8,
        "recordCount": 50,
        "amount": "1500.00"
      }
    ]
  }
}
```

---

## 6. 系统管理 API

### 6.1 健康检查

#### 6.1.1 服务健康检查

**GET** `/actuator/health`

**响应示例:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "6.2.6"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 250685575168,
        "free": 125342787584,
        "threshold": 10485760
      }
    }
  }
}
```

#### 6.1.2 服务信息

**GET** `/actuator/info`

### 6.2 系统配置

#### 6.2.1 获取系统配置

**GET** `/consume/system/config`

**响应示例:**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "systemName": "IOE-DREAM消费管理系统",
    "version": "1.0.0",
    "allowRegistration": false,
    "defaultPassword": "123456",
    "passwordMinLength": 6,
    "sessionTimeout": 1800,
    "maxLoginAttempts": 5,
    "lockoutDuration": 900,
    "supportedPaymentMethods": [1, 2, 3],
    "defaultCurrency": "CNY",
    "dateFormat": "yyyy-MM-dd HH:mm:ss",
    "timezone": "Asia/Shanghai"
  }
}
```

---

## 错误码说明

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 400001 | 参数验证失败 | 检查请求参数格式和必填字段 |
| 400002 | 业务规则验证失败 | 检查业务逻辑约束条件 |
| 404001 | 资源不存在 | 确认资源ID是否正确 |
| 404002 | 父资源不存在 | 确认父资源ID是否正确 |
| 409001 | 资源编码重复 | 修改编码或使用其他编码 |
| 409002 | 资源冲突 | 检查资源约束关系 |
| 500001 | 数据库操作异常 | 检查数据库连接和SQL语句 |
| 500002 | 第三方服务异常 | 检查外部服务可用性 |
| 500003 | 系统内部异常 | 联系系统管理员 |

---

## 版本更新日志

### v1.0.0 (2025-12-21)
- 初始版本发布
- 实现产品管理、充值管理、补贴管理、设备管理等核心功能
- 支持RESTful API和完整的CRUD操作
- 提供详细的统计和报表功能

---

## 联系方式

- **开发团队**: IOE-DREAM Team
- **技术支持**: tech-support@ioe-dream.com
- **文档维护**: development@ioe-dream.com

---

*本文档最后更新时间: 2025-12-21 16:00:00*