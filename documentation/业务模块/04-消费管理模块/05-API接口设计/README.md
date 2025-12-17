# 消费管理模块 - API接口设计

> **版本**: v2.0.0  
> **更新日期**: 2025-12-17

---

## 1. 接口规范

- **基础路径**: `/api/v1/consume`
- **认证方式**: Sa-Token
- **数据格式**: JSON
- **编码**: UTF-8

---

## 2. 核心接口

### 2.1 消费接口

#### 消费处理
```
POST /api/v1/consume/transaction
```

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| accountId | String | 是 | 账户ID |
| areaId | String | 是 | 区域ID |
| deviceId | String | 是 | 设备ID |
| consumeMode | String | 是 | 消费模式 |
| amount | Integer | 否 | 消费金额(分) |
| mealId | String | 否 | 餐别ID |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "transactionNo": "TRX20251217001",
    "consumeMoney": 1200,
    "balanceAfter": 8800
  }
}
```

---

### 2.2 充值接口

#### 创建充值订单
```
POST /api/v1/consume/recharge/create
```

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| accountId | String | 是 | 账户ID |
| amount | Integer | 是 | 充值金额(分) |
| paymentType | String | 是 | 支付方式 |

---

### 2.3 区域接口

#### 区域列表
```
GET /api/v1/consume/area/list
```

#### 区域详情
```
GET /api/v1/consume/area/{id}
```

#### 创建区域
```
POST /api/v1/consume/area
```

---

### 2.4 账户接口

#### 账户查询
```
GET /api/v1/consume/account/{id}
```

#### 账户余额
```
GET /api/v1/consume/account/{id}/balance
```

---

## 3. 错误码

| 错误码 | 说明 |
|--------|------|
| 4001 | 账户不存在 |
| 4002 | 余额不足 |
| 4003 | 区域无权限 |
| 4004 | 消费模式不支持 |
| 4005 | 超出限额 |

---

*本文档持续更新中*

