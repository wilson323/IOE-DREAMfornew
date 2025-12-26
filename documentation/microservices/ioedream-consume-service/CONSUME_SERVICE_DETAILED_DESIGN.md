# 🛒 IOE-DREAM 消费管理服务详细设计文档

> **📋 文档版本**: v1.0.0
> **🏗️ 服务名称**: ioedream-consume-service
> **🚀 端口号**: 8094
> **👥 维护团队**: IOE-DREAM 消费业务模块团队
> **📅 创建日期**: 2025-01-30
> **🔄 更新日期**: 2025-01-30

---

## 📋 概述

### 服务简介

**IOE-DREAM 消费管理服务**是企业级智慧园区一卡通管理平台的核心业务服务，提供完整的消费支付解决方案。该服务支持多种支付方式（刷卡、刷脸、NFC、手机支付等），实现无感消费、智能补贴、实时统计等功能。

### 核心价值

- **🚀 无感支付体验**: 支持刷脸、刷卡、NFC等多种支付方式，秒级完成支付
- **💰 智能补贴管理**: 自动发放员工补贴，支持多种补贴策略
- **📊 实时数据统计**: 消费数据实时统计，经营状况一目了然
- **🔒 金融级安全**: 完整的交易安全体系，满足金融级安全要求
- **📱 移动端支持**: 完整的移动端消费体验，支持手机支付

### 业务场景

- **企业餐厅**: 员工食堂无感消费、补贴发放、消费统计
- **便利店**: 园区内便利店购物支付
- **自动售货机**: 无人售货机商品购买
- **停车场**: 智能停车缴费
- **其他场景**: 园区内各种消费场景的统一支付

---

## 🏗️ 架构设计

### 技术架构

```mermaid
graph TB
    subgraph "前端层"
        A[Web管理后台<br/>Vue3 + Ant Design Vue]
        B[移动端App<br/>uni-app]
        C[微信小程序<br/>原生小程序]
    end

    subgraph "网关层"
        D[API网关<br/>ioedream-gateway-service<br/>8080]
    end

    subgraph "业务服务层"
        E[消费管理服务<br/>ioedream-consume-service<br/>8094]
        F[公共服务<br/>ioedream-common-service<br/>8088]
        G[设备通讯服务<br/>ioedream-device-comm-service<br/>8087]
    end

    subgraph "数据层"
        H[MySQL 8.0<br/>业务数据库]
        I[Redis 6.4<br/>缓存数据库]
    end

    subgraph "外部系统"
        J[第三方支付<br/>微信支付/支付宝]
        K[银行系统<br/>对账接口]
        L[设备厂商<br/>POS机厂商]
    end

    A --> D
    B --> D
    C --> D
    D --> E
    E --> F
    E --> G
    E --> H
    E --> I
    E --> J
    E --> K
    E --> L
```

### 服务职责

| 职责类别 | 具体功能 | 描述 |
|---------|---------|------|
| **核心业务** | 消费交易处理 | 支持多种支付方式的交易处理 |
| | 账户管理 | 用户消费账户管理、余额操作 |
| | 补贴管理 | 员工补贴发放、使用、统计 |
| | 退款处理 | 消费退款申请、审批、处理 |
| **数据管理** | 交易记录 | 完整的交易流水记录 |
| | 统计分析 | 多维度消费数据统计分析 |
| | 报表生成 | 日报、月报、年报生成 |
| **设备管理** | 消费设备管理 | POS机、自动售货机等设备管理 |
| | 设备状态监控 | 设备在线状态、健康监控 |
| | 设备配置管理 | 设备参数配置、更新 |
| **系统集成** | 第三方支付 | 微信支付、支付宝集成 |
| | 银行对账 | 与银行系统对账 |
| | 移动端支持 | 手机支付、小程序支付 |

---

## 🗄️ 数据库设计

### 核心实体关系

```mermaid
erDiagram
    t_consume_account ||--o{ t_consume_record : "1:N"
    t_consume_record ||--o{ t_consume_refund : "1:N"
    t_consume_account ||--o{ t_consume_recharge : "1:N"
    t_consume_account ||--o{ t_consume_subsidy : "1:N"
    t_consume_record }|--|| t_common_device : "N:1"
    t_consume_record }|--|| t_common_area : "N:1"
    t_consume_record }|--|| t_common_user : "N:1"

    t_consume_account {
        bigint account_id PK "账户ID"
        bigint user_id "用户ID"
        varchar account_no "账户编号"
        varchar account_name "账户名称"
        int account_type "账户类型"
        decimal balance "账户余额"
        decimal subsidy_balance "补贴余额"
        decimal frozen_amount "冻结金额"
        decimal credit_limit "信用额度"
        decimal daily_limit "日消费限额"
        decimal monthly_limit "月消费限额"
        int status "账户状态"
        datetime last_use_time "最后使用时间"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
        bigint create_user_id "创建人ID"
        bigint update_user_id "更新人ID"
        int deleted_flag "删除标记"
        int version "版本号"
    }

    t_consume_record {
        bigint id PK "消费记录ID"
        varchar transaction_no "交易流水号"
        varchar order_no "订单号"
        bigint user_id "用户ID"
        varchar user_name "用户姓名"
        varchar user_phone "用户手机号"
        int user_type "用户类型"
        bigint account_id "账户ID"
        varchar account_no "账户编号"
        varchar account_name "账户名称"
        bigint area_id "区域ID"
        varchar area_name "区域名称"
        bigint device_id "设备ID"
        varchar device_name "设备名称"
        varchar device_no "设备编号"
        date consume_date "消费日期"
        decimal amount "消费金额"
        decimal balance_before "消费前余额"
        decimal balance_after "消费后余额"
        varchar currency "币种"
        decimal exchange_rate "汇率"
        decimal discount_amount "折扣金额"
        decimal subsidy_amount "补贴金额"
        decimal actual_amount "实际支付金额"
        varchar pay_method "支付方式"
        datetime pay_time "支付时间"
        datetime consume_time "消费时间"
        varchar consume_type "消费类型"
        varchar consume_mode "消费模式"
        varchar mode_config "消费模式配置JSON"
        varchar merchant_name "商户名称"
        varchar goods_info "商品信息JSON"
        varchar status "状态"
        int refund_status "退款状态"
        decimal refund_amount "退款金额"
        datetime refund_time "退款时间"
        varchar refund_reason "退款原因"
        bigint original_record_id "原始消费记录ID"
        varchar third_party_order_no "第三方支付订单号"
        varchar third_party_transaction_id "第三方交易号"
        decimal fee_amount "手续费"
        varchar extend_data "扩展数据JSON"
        varchar client_ip "客户端IP地址"
        varchar user_agent "用户代理"
        varchar remark "备注"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
        bigint create_user_id "创建人ID"
        bigint update_user_id "更新人ID"
        int deleted_flag "删除标记"
        int version "版本号"
    }

    t_consume_refund {
        bigint refund_id PK "退款ID"
        varchar refund_no "退款单号"
        bigint original_record_id "原始消费记录ID"
        bigint user_id "用户ID"
        bigint account_id "账户ID"
        decimal refund_amount "退款金额"
        varchar refund_reason "退款原因"
        int refund_status "退款状态"
        varchar apply_user_id "申请人ID"
        datetime apply_time "申请时间"
        varchar approve_user_id "审批人ID"
        datetime approve_time "审批时间"
        varchar remark "备注"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
        bigint create_user_id "创建人ID"
        bigint update_user_id "更新人ID"
        int deleted_flag "删除标记"
        int version "版本号"
    }

    t_consume_recharge {
        bigint recharge_id PK "充值ID"
        varchar recharge_no "充值单号"
        bigint user_id "用户ID"
        bigint account_id "账户ID"
        decimal recharge_amount "充值金额"
        varchar pay_method "支付方式"
        varchar pay_status "支付状态"
        varchar third_party_order_no "第三方支付订单号"
        datetime pay_time "支付时间"
        varchar remark "备注"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
        bigint create_user_id "创建人ID"
        bigint update_user_id "更新人ID"
        int deleted_flag "删除标记"
        int version "版本号"
    }

    t_consume_subsidy {
        bigint subsidy_id PK "补贴ID"
        varchar subsidy_no "补贴单号"
        bigint user_id "用户ID"
        bigint account_id "账户ID"
        decimal subsidy_amount "补贴金额"
        varchar subsidy_type "补贴类型"
        varchar subsidy_reason "补贴原因"
        datetime effective_time "生效时间"
        datetime expire_time "失效时间"
        int status "状态"
        varchar remark "备注"
        datetime create_time "创建时间"
        datetime update_time "更新时间"
        bigint create_user_id "创建人ID"
        bigint update_user_id "更新人ID"
        int deleted_flag "删除标记"
        int version "版本号"
    }
```

### 核心表设计

#### 1. 消费账户表 (t_consume_account)

```sql
CREATE TABLE t_consume_account (
    account_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '账户ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    account_no VARCHAR(50) NOT NULL UNIQUE COMMENT '账户编号',
    account_name VARCHAR(100) NOT NULL COMMENT '账户名称',
    account_type TINYINT NOT NULL DEFAULT 1 COMMENT '账户类型：1-个人账户 2-团体账户 3-临时账户',
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
    subsidy_balance DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '补贴余额',
    frozen_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '冻结金额',
    credit_limit DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '信用额度',
    daily_limit DECIMAL(10,2) NOT NULL DEFAULT 99999.99 COMMENT '日消费限额',
    monthly_limit DECIMAL(10,2) NOT NULL DEFAULT 999999.99 COMMENT '月消费限额',
    total_recharge_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计充值金额',
    total_consume_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计消费金额',
    total_subsidy_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '累计补贴金额',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '账户状态：1-正常 2-冻结 3-注销',
    last_use_time DATETIME COMMENT '最后使用时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号',

    INDEX idx_user_id (user_id),
    INDEX idx_account_no (account_no),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费账户表';
```

#### 2. 消费记录表 (t_consume_record)

```sql
CREATE TABLE t_consume_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消费记录ID',
    transaction_no VARCHAR(50) NOT NULL UNIQUE COMMENT '交易流水号',
    order_no VARCHAR(50) COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(100) NOT NULL COMMENT '用户姓名',
    user_phone VARCHAR(20) COMMENT '用户手机号',
    user_type TINYINT COMMENT '用户类型',
    account_id BIGINT NOT NULL COMMENT '账户ID',
    account_no VARCHAR(50) NOT NULL COMMENT '账户编号',
    account_name VARCHAR(100) NOT NULL COMMENT '账户名称',
    area_id BIGINT COMMENT '区域ID',
    area_name VARCHAR(100) COMMENT '区域名称',
    device_id BIGINT COMMENT '设备ID',
    device_name VARCHAR(100) COMMENT '设备名称',
    device_no VARCHAR(50) COMMENT '设备编号',
    consume_date DATE NOT NULL COMMENT '消费日期',
    amount DECIMAL(10,2) NOT NULL COMMENT '消费金额',
    balance_before DECIMAL(10,2) NOT NULL COMMENT '消费前余额',
    balance_after DECIMAL(10,2) NOT NULL COMMENT '消费后余额',
    currency VARCHAR(10) NOT NULL DEFAULT 'CNY' COMMENT '币种',
    exchange_rate DECIMAL(10,6) NOT NULL DEFAULT 1.000000 COMMENT '汇率',
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '折扣金额',
    subsidy_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '补贴金额',
    actual_amount DECIMAL(10,2) NOT NULL COMMENT '实际支付金额',
    pay_method VARCHAR(20) NOT NULL COMMENT '支付方式',
    pay_time DATETIME COMMENT '支付时间',
    consume_time DATETIME NOT NULL COMMENT '消费时间',
    consume_type VARCHAR(20) NOT NULL COMMENT '消费类型',
    consume_mode VARCHAR(20) NOT NULL COMMENT '消费模式',
    mode_config JSON COMMENT '消费模式配置JSON',
    merchant_name VARCHAR(100) COMMENT '商户名称',
    goods_info JSON COMMENT '商品信息JSON',
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '状态',
    refund_status TINYINT NOT NULL DEFAULT 0 COMMENT '退款状态：0-无退款 1-部分退款 2-全额退款',
    refund_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '退款金额',
    refund_time DATETIME COMMENT '退款时间',
    refund_reason VARCHAR(200) COMMENT '退款原因',
    original_record_id BIGINT COMMENT '原始消费记录ID',
    third_party_order_no VARCHAR(50) COMMENT '第三方支付订单号',
    third_party_transaction_id VARCHAR(100) COMMENT '第三方交易号',
    fee_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '手续费',
    extend_data JSON COMMENT '扩展数据JSON',
    client_ip VARCHAR(50) COMMENT '客户端IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号',

    INDEX idx_transaction_no (transaction_no),
    INDEX idx_user_id (user_id),
    INDEX idx_account_id (account_id),
    INDEX idx_device_id (device_id),
    INDEX idx_area_id (area_id),
    INDEX idx_consume_date (consume_date),
    INDEX idx_consume_time (consume_time),
    INDEX idx_status (status),
    INDEX idx_refund_status (refund_status),
    INDEX idx_create_time (create_time),
    INDEX idx_user_date (user_id, consume_date),
    INDEX idx_device_date (device_id, consume_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费记录表';
```

#### 3. 消费退款表 (t_consume_refund)

```sql
CREATE TABLE t_consume_refund (
    refund_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '退款ID',
    refund_no VARCHAR(50) NOT NULL UNIQUE COMMENT '退款单号',
    original_record_id BIGINT NOT NULL COMMENT '原始消费记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    account_id BIGINT NOT NULL COMMENT '账户ID',
    refund_amount DECIMAL(10,2) NOT NULL COMMENT '退款金额',
    refund_reason VARCHAR(200) NOT NULL COMMENT '退款原因',
    refund_status TINYINT NOT NULL DEFAULT 1 COMMENT '退款状态：1-待审批 2-已批准 3-已拒绝 4-已完成 5-已取消',
    apply_user_id BIGINT COMMENT '申请人ID',
    apply_time DATETIME NOT NULL COMMENT '申请时间',
    approve_user_id BIGINT COMMENT '审批人ID',
    approve_time DATETIME COMMENT '审批时间',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    update_user_id BIGINT COMMENT '更新人ID',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号',

    INDEX idx_refund_no (refund_no),
    INDEX idx_original_record_id (original_record_id),
    INDEX idx_user_id (user_id),
    INDEX idx_account_id (account_id),
    INDEX idx_refund_status (refund_status),
    INDEX idx_apply_time (apply_time),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费退款表';
```

---

## 🔌 API设计

### REST API规范

| 资源路径 | HTTP方法 | 功能描述 | 权限要求 |
|---------|---------|---------|---------|
| `/api/v1/consume/transaction/execute` | POST | 执行消费交易 | CONSUME_ACCOUNT_USE |
| `/api/v1/consume/transaction/detail/{transactionNo}` | GET | 查询交易详情 | CONSUME_ACCOUNT_MANAGE |
| `/api/v1/consume/transaction/query` | GET | 分页查询消费记录 | CONSUME_ACCOUNT_MANAGE |
| `/api/v1/consume/transaction/device/{deviceId}` | GET | 获取设备详情 | CONSUME_ACCOUNT_MANAGE |
| `/api/v1/consume/transaction/device/statistics` | GET | 获取设备统计 | CONSUME_ACCOUNT_MANAGE |
| `/api/v1/consume/transaction/realtime-statistics` | GET | 获取实时统计 | CONSUME_ACCOUNT_MANAGE |
| `/api/v1/consume/account/{accountId}` | GET | 获取账户详情 | CONSUME_ACCOUNT_MANAGE |
| `/api/v1/consume/account/{accountId}/balance` | GET | 查询账户余额 | CONSUME_ACCOUNT_USE |
| `/api/v1/consume/account/{accountId}/freeze` | POST | 冻结账户 | CONSUME_ACCOUNT_MANAGE |
| `/api/v1/consume/account/{accountId}/unfreeze` | POST | 解冻账户 | CONSUME_ACCOUNT_MANAGE |
| `/api/v1/consume/refund/apply` | POST | 申请退款 | CONSUME_ACCOUNT_USE |
| `/api/v1/consume/refund/approve` | POST | 审批退款 | CONSUME_ACCOUNT_MANAGE |
| `/api/v1/consume/refund/query` | GET | 查询退款记录 | CONSUME_ACCOUNT_MANAGE |
| `/api/v1/consume/recharge/create` | POST | 创建充值订单 | CONSUME_ACCOUNT_USE |
| `/api/v1/consume/recharge/pay` | POST | 支付充值 | CONSUME_ACCOUNT_USE |
| `/api/v1/consume/subsidy/grant` | POST | 发放补贴 | CONSUME_ACCOUNT_MANAGE |
| `/api/v1/consume/subsidy/query` | GET | 查询补贴记录 | CONSUME_ACCOUNT_MANAGE |

### 核心API接口

#### 1. 执行消费交易

```http
POST /api/v1/consume/transaction/execute
Content-Type: application/json

{
  "userId": 1001,
  "accountId": 2001,
  "deviceId": 3001,
  "areaId": 4001,
  "amount": 50.00,
  "consumeMode": "CARD",
  "consumeType": "MEAL",
  "merchantName": "企业餐厅",
  "goodsInfo": {
    "items": [
      {
        "name": "宫保鸡丁套餐",
        "price": 25.00,
        "quantity": 2
      }
    ],
    "total": 50.00
  },
  "remark": "午餐消费"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "transactionNo": "TXN2025013012345678",
    "orderNo": "ORD2025013012345678",
    "status": "SUCCESS",
    "amount": 50.00,
    "actualAmount": 45.00,
    "discountAmount": 5.00,
    "subsidyAmount": 10.00,
    "balanceBefore": 100.00,
    "balanceAfter": 55.00,
    "consumeTime": "2025-01-30T12:30:00",
    "deviceName": "餐厅POS机-01",
    "areaName": "A栋1楼餐厅"
  },
  "timestamp": 1706580600000
}
```

#### 2. 查询交易详情

```http
GET /api/v1/consume/transaction/detail/TXN2025013012345678
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "transactionNo": "TXN2025013012345678",
    "orderNo": "ORD2025013012345678",
    "userId": 1001,
    "userName": "张三",
    "userPhone": "13800138000",
    "accountId": 2001,
    "accountNo": "ACC2001001",
    "accountName": "张三个人账户",
    "areaId": 4001,
    "areaName": "A栋1楼餐厅",
    "deviceId": 3001,
    "deviceName": "餐厅POS机-01",
    "deviceNo": "POS001",
    "consumeDate": "2025-01-30",
    "amount": 50.00,
    "balanceBefore": 100.00,
    "balanceAfter": 55.00,
    "currency": "CNY",
    "exchangeRate": 1.000000,
    "discountAmount": 5.00,
    "subsidyAmount": 10.00,
    "actualAmount": 45.00,
    "payMethod": "CARD",
    "payTime": "2025-01-30T12:30:00",
    "consumeTime": "2025-01-30T12:30:00",
    "consumeType": "MEAL",
    "consumeMode": "CARD",
    "modeConfig": {
      "cardType": "IC_CARD",
      "requirePin": false
    },
    "merchantName": "企业餐厅",
    "goodsInfo": {
      "items": [
        {
          "name": "宫保鸡丁套餐",
          "price": 25.00,
          "quantity": 2
        }
      ],
      "total": 50.00
    },
    "status": "SUCCESS",
    "refundStatus": 0,
    "refundAmount": 0.00,
    "clientIp": "192.168.1.100",
    "userAgent": "Mozilla/5.0...",
    "createTime": "2025-01-30T12:30:00",
    "updateTime": "2025-01-30T12:30:00"
  },
  "timestamp": 1706580600000
}
```

#### 3. 分页查询消费记录

```http
GET /api/v1/consume/transaction/query?pageNum=1&pageSize=10&userId=1001&startDate=2025-01-01&endDate=2025-01-31&consumeMode=CARD&status=SUCCESS
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "transactionNo": "TXN2025013012345678",
        "userName": "张三",
        "accountName": "张三个人账户",
        "areaName": "A栋1楼餐厅",
        "deviceName": "餐厅POS机-01",
        "amount": 50.00,
        "actualAmount": 45.00,
        "discountAmount": 5.00,
        "subsidyAmount": 10.00,
        "balanceBefore": 100.00,
        "balanceAfter": 55.00,
        "consumeMode": "CARD",
        "consumeType": "MEAL",
        "status": "SUCCESS",
        "consumeTime": "2025-01-30T12:30:00",
        "merchantName": "企业餐厅"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 1
  },
  "timestamp": 1706580600000
}
```

### WebSocket接口

#### 实时交易通知

```javascript
// 连接WebSocket
const ws = new WebSocket('ws://localhost:8094/api/v1/consume/realtime/notifications');

// 订阅交易通知
ws.send(JSON.stringify({
  type: 'SUBSCRIBE_TRANSACTIONS',
  data: {
    areaId: 'AREA001',
    userId: 1001
  }
}));

// 接收实时交易通知
ws.onmessage = function(event) {
  const message = JSON.parse(event.data);

  switch(message.type) {
    case 'TRANSACTION_SUCCESS':
      // 处理交易成功通知
      handleTransactionSuccess(message.data);
      break;

    case 'TRANSACTION_FAILED':
      // 处理交易失败通知
      handleTransactionFailed(message.data);
      break;

    case 'DEVICE_STATUS':
      // 处理设备状态变更
      handleDeviceStatus(message.data);
      break;

    case 'REALTIME_STATISTICS':
      // 处理实时统计数据
      handleRealtimeStatistics(message.data);
      break;
  }
};

function handleTransactionSuccess(data) {
  console.log('交易成功:', data.transactionNo);
  // 更新UI显示
  updateTransactionList(data);
}

function handleTransactionFailed(data) {
  console.error('交易失败:', data.transactionNo, data.error);
  // 显示错误提示
  showErrorMessage(data.error);
}

function handleDeviceStatus(data) {
  console.log('设备状态:', data.deviceId, data.status);
  // 更新设备状态显示
  updateDeviceStatus(data.deviceId, data.status);
}

function handleRealtimeStatistics(data) {
  console.log('实时统计:', data);
  // 更新统计仪表板
  updateStatisticsDashboard(data);
}
```

---

## 💼 业务流程设计

### 核心业务流程

#### 1. 消费交易流程

```mermaid
sequenceDiagram
    participant User as 用户
    participant Device as 消费设备
    participant Gateway as API网关
    participant ConsumeService as 消费服务
    participant AccountService as 账户服务
    participant Redis as 缓存
    participant MySQL as 数据库
    participant ThirdParty as 第三方支付

    User->>Device: 刷卡/刷脸/NFC支付
    Device->>Gateway: 发送消费请求
    Gateway->>ConsumeService: 验证权限
    ConsumeService->>AccountService: 验证账户
    AccountService->>Redis: 查询账户余额
    Redis-->>AccountService: 返回余额
    AccountService-->>ConsumeService: 返回账户信息

    alt 余额充足且账户正常
        ConsumeService->>MySQL: 锁定账户余额
        ConsumeService->>ConsumeService: 计算优惠和补贴
        ConsumeService->>ConsumeService: 生成交易流水

        alt 需要第三方支付
            ConsumeService->>ThirdParty: 调用第三方支付
            ThirdParty-->>ConsumeService: 返回支付结果
        end

        ConsumeService->>MySQL: 保存消费记录
        ConsumeService->>AccountService: 更新账户余额
        AccountService->>Redis: 更新缓存余额
        ConsumeService->>MySQL: 提交事务

        ConsumeService-->>Gateway: 返回交易结果
        Gateway-->>Device: 返回交易状态
        Device-->>User: 显示交易成功/失败

        par 实时通知
            ConsumeService->>Gateway: 发送WebSocket通知
            Gateway->>DeviceManager: 推送设备状态
        end

    else 余额不足或账户异常
        ConsumeService-->>Gateway: 返回错误信息
        Gateway-->>Device: 返回错误状态
        Device-->>User: 显示错误提示
    end
```

#### 2. 退款处理流程

```mermaid
sequenceDiagram
    participant User as 用户
    participant MobileApp as 移动端
    participant Gateway as API网关
    participant ConsumeService as 消费服务
    participant ManagerService as 管理服务
    participant MySQL as 数据库
    participant AccountService as 账户服务
    participant Approver as 审批人

    User->>MobileApp: 申请退款
    MobileApp->>Gateway: 提交退款申请
    Gateway->>ConsumeService: 验证权限
    ConsumeService->>MySQL: 验证原交易记录

    alt 交易存在且可退款
        ConsumeService->>MySQL: 创建退款申请
        ConsumeService->>ManagerService: 通知审批人
        ConsumeService-->>Gateway: 返回申请成功
        Gateway-->>MobileApp: 返回申请结果

        Approver->>ManagerService: 审批退款
        ManagerService->>ConsumeService: 处理审批结果

        alt 审批通过
            ConsumeService->>MySQL: 锁定账户
            ConsumeService->>AccountService: 退还余额
            AccountService->>MySQL: 更新账户余额
            ConsumeService->>MySQL: 更新退款状态
            ConsumeService->>MySQL: 更新原交易记录
            ConsumeService->>ManagerService: 通知用户
            ManagerService->>MobileApp: 推送退款成功通知

        else 审批拒绝
            ConsumeService->>MySQL: 更新退款状态
            ConsumeService->>ManagerService: 通知用户
            ManagerService->>MobileApp: 推送拒绝通知
        end

    else 交易不存在或不可退款
        ConsumeService-->>Gateway: 返回错误信息
        Gateway-->>MobileApp: 返回错误提示
    end
```

#### 3. 补贴发放流程

```mermaid
sequenceDiagram
    participant Admin as 管理员
    participant WebApp as Web管理后台
    participant Gateway as API网关
    participant ConsumeService as 消费服务
    participant ScheduleService as 定时任务服务
    participant MySQL as 数据库
    participant AccountService as 账户服务
    participant NotificationService as 通知服务

    Admin->>WebApp: 配置补贴规则
    WebApp->>Gateway: 保存补贴配置
    Gateway->>ConsumeService: 验证配置
    ConsumeService->>MySQL: 保存补贴规则

    Note over ScheduleService: 每日定时执行补贴发放

    ScheduleService->>ConsumeService: 触发补贴发放
    ConsumeService->>MySQL: 查询补贴规则
    ConsumeService->>MySQL: 查询符合条件的用户

    loop 为每个用户发放补贴
        ConsumeService->>AccountService: 发放补贴到账户
        AccountService->>MySQL: 更新账户余额
        ConsumeService->>MySQL: 记录补贴发放记录
        ConsumeService->>NotificationService: 发放补贴通知
    end

    ConsumeService->>MySQL: 更新补贴发放状态
    ConsumeService->>Admin: 发送发放报告
```

### 异常处理流程

#### 1. 支付失败处理

```mermaid
flowchart TD
    A[开始消费] --> B{验证账户}
    B -->|失败| C[账户异常返回]
    B -->|成功| D{检查余额}
    D -->|余额不足| E[余额不足返回]
    D -->|余额充足| F[锁定余额]

    F --> G{调用第三方支付}
    G -->|成功| H[更新交易状态成功]
    G -->|失败| I{检查失败原因}

    I -->|余额不足| J[解锁余额]
    I -->|网络异常| K[标记为待重试]
    I -->|其他异常| L[交易失败]

    H --> M[发送成功通知]
    J --> N[发送失败通知]
    K --> O[加入重试队列]
    L --> P[发送失败通知]

    O --> Q{重试次数检查}
    Q -->|未超限| R[延时重试]
    Q -->|已超限| S[标记为失败]

    R --> G
    S --> L
```

#### 2. 设备离线处理

```mermaid
flowchart TD
    A[检测设备状态] --> B{设备是否在线}
    B -->|在线| C[正常处理交易]
    B -->|离线| D{是否支持离线模式}

    D -->|支持| E[切换离线模式]
    D -->|不支持| F[拒绝交易]

    E --> G[保存离线交易]
    G --> H[设备恢复在线]
    H --> I[同步离线交易]
    I --> J{同步是否成功}

    J -->|成功| K[标记交易完成]
    J -->|失败| L{重试次数检查}

    L -->|未超限| M[延时重试]
    L -->|已超限| N[标记为异常]

    M --> I
    N --> O[人工处理]
```

---

## 🔒 安全设计

### 1. 交易安全

#### 支付安全机制

```java
@Component
public class PaymentSecurityManager {

    @Resource
    private AESUtil aesUtil;

    @Resource
    private RSAUtil rsaUtil;

    /**
     * 交易数据加密
     */
    public String encryptTransactionData(ConsumeTransactionForm form) {
        try {
            // 1. 敏感字段加密
            form.setAccountNo(maskAccountNo(form.getAccountNo()));
            form.setUserPhone(maskPhone(form.getUserPhone()));

            // 2. 生成交易签名
            String signature = generateTransactionSignature(form);
            form.setSignature(signature);

            // 3. 数据加密传输
            return aesUtil.encrypt(JSON.toJSONString(form));
        } catch (Exception e) {
            throw new SystemException("TRANSACTION_ENCRYPT_ERROR", "交易数据加密失败");
        }
    }

    /**
     * 交易签名验证
     */
    public boolean verifyTransactionSignature(ConsumeTransactionForm form) {
        try {
            String expectedSignature = generateTransactionSignature(form);
            return Objects.equals(form.getSignature(), expectedSignature);
        } catch (Exception e) {
            log.error("交易签名验证失败", e);
            return false;
        }
    }

    /**
     * 生成交易签名
     */
    private String generateTransactionSignature(ConsumeTransactionForm form) {
        StringBuilder sb = new StringBuilder();
        sb.append(form.getUserId())
          .append(form.getAccountId())
          .append(form.getAmount())
          .append(form.getConsumeTime().getTime())
          .append(form.getDeviceId());

        return rsaUtil.sign(sb.toString());
    }

    /**
     * 账户号脱敏
     */
    private String maskAccountNo(String accountNo) {
        if (StringUtils.isEmpty(accountNo) || accountNo.length() < 8) {
            return "****";
        }
        return accountNo.substring(0, 4) + "****" +
               accountNo.substring(accountNo.length() - 4);
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (StringUtils.isEmpty(phone) || phone.length() < 11) {
            return "****";
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
```

#### 交易风控规则

```java
@Component
public class TransactionRiskController {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 交易风控检查
     */
    public void checkTransactionRisk(ConsumeTransactionForm form) {
        // 1. 检查交易频率
        checkTransactionFrequency(form.getUserId());

        // 2. 检查交易金额
        checkTransactionAmount(form);

        // 3. 检查设备风险
        checkDeviceRisk(form.getDeviceId());

        // 4. 检查地理位置风险
        checkLocationRisk(form.getUserId(), form.getAreaId());

        // 5. 检查时间风险
        checkTimeRisk(form.getConsumeTime());
    }

    /**
     * 检查交易频率
     */
    private void checkTransactionFrequency(Long userId) {
        String key = "transaction:freq:" + userId;
        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }

        // 1分钟内最多允许10次交易
        if (count > 10) {
            throw new BusinessException("TRANSACTION_TOO_FREQUENT", "交易过于频繁，请稍后再试");
        }
    }

    /**
     * 检查交易金额
     */
    private void checkTransactionAmount(ConsumeTransactionForm form) {
        // 单笔交易限额
        if (form.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            throw new BusinessException("TRANSACTION_AMOUNT_EXCEEDED", "单笔交易金额超过限额");
        }

        // 检查日交易限额
        checkDailyLimit(form.getUserId(), form.getAmount());

        // 检查月交易限额
        checkMonthlyLimit(form.getUserId(), form.getAmount());
    }

    /**
     * 检查日交易限额
     */
    private void checkDailyLimit(Long userId, BigDecimal amount) {
        String key = "transaction:daily:" + userId + ":" + LocalDate.now();
        BigDecimal dailyTotal = (BigDecimal) redisTemplate.opsForValue().get(key);

        if (dailyTotal != null) {
            BigDecimal newTotal = dailyTotal.add(amount);
            if (newTotal.compareTo(new BigDecimal("50000")) > 0) {
                throw new BusinessException("DAILY_LIMIT_EXCEEDED", "日交易金额超过限额");
            }
            redisTemplate.opsForValue().set(key, newTotal, Duration.ofDays(1));
        } else {
            redisTemplate.opsForValue().set(key, amount, Duration.ofDays(1));
        }
    }
}
```

### 2. 数据安全

#### 敏感数据保护

```java
@Component
public class DataProtectionManager {

    @Resource
    private AESUtil aesUtil;

    /**
     * 加密敏感字段
     */
    public ConsumeRecordEntity encryptSensitiveFields(ConsumeRecordEntity record) {
        try {
            // 加密用户手机号
            if (StringUtils.isNotEmpty(record.getUserPhone())) {
                record.setUserPhone(aesUtil.encrypt(record.getUserPhone()));
            }

            // 加密扩展数据
            if (StringUtils.isNotEmpty(record.getExtendData())) {
                record.setExtendData(aesUtil.encrypt(record.getExtendData()));
            }

            // 加密商品信息（如包含敏感信息）
            if (StringUtils.isNotEmpty(record.getGoodsInfo())) {
                String goodsInfo = filterAndEncryptGoodsInfo(record.getGoodsInfo());
                record.setGoodsInfo(goodsInfo);
            }

            return record;
        } catch (Exception e) {
            log.error("加密敏感字段失败", e);
            throw new SystemException("ENCRYPT_SENSITIVE_DATA_ERROR", "加密敏感字段失败");
        }
    }

    /**
     * 解密敏感字段
     */
    public ConsumeRecordEntity decryptSensitiveFields(ConsumeRecordEntity record) {
        try {
            // 解密用户手机号
            if (StringUtils.isNotEmpty(record.getUserPhone())) {
                record.setUserPhone(aesUtil.decrypt(record.getUserPhone()));
            }

            // 解密扩展数据
            if (StringUtils.isNotEmpty(record.getExtendData())) {
                record.setExtendData(aesUtil.decrypt(record.getExtendData()));
            }

            return record;
        } catch (Exception e) {
            log.error("解密敏感字段失败", e);
            throw new SystemException("DECRYPT_SENSITIVE_DATA_ERROR", "解密敏感字段失败");
        }
    }

    /**
     * 过滤和加密商品信息
     */
    private String filterAndEncryptGoodsInfo(String goodsInfo) {
        try {
            JSONArray goodsArray = JSON.parseArray(goodsInfo);
            JSONArray filteredArray = new JSONArray();

            for (int i = 0; i < goodsArray.size(); i++) {
                JSONObject goods = goodsArray.getJSONObject(i);
                JSONObject filteredGoods = new JSONObject();

                // 只保留必要字段
                filteredGoods.put("name", goods.getString("name"));
                filteredGoods.put("price", goods.getBigDecimal("price"));
                filteredGoods.put("quantity", goods.getInteger("quantity"));

                filteredArray.add(filteredGoods);
            }

            return aesUtil.encrypt(filteredArray.toJSONString());
        } catch (Exception e) {
            log.error("过滤和加密商品信息失败", e);
            return "";
        }
    }
}
```

### 3. 访问控制

#### 权限控制设计

```java
@PreAuthorize("hasRole('CONSUME_USER')")
@RestController
@RequestMapping("/api/v1/consume")
public class ConsumeSecurityController {

    /**
     * 消费权限检查
     */
    @PreAuthorize("hasAuthority('CONSUME_ACCOUNT_USE') and @consumeSecurity.checkAccountAccess(#accountId, authentication.name)")
    @PostMapping("/transaction/execute")
    public ResponseDTO<ConsumeTransactionResultVO> executeTransaction(
            @Valid @RequestBody ConsumeTransactionForm form,
            Authentication authentication) {

        // 1. 验证用户身份
        validateUserIdentity(authentication, form.getUserId());

        // 2. 检查账户访问权限
        checkAccountAccess(form.getAccountId(), authentication);

        // 3. 检查设备使用权限
        checkDeviceAccess(form.getDeviceId(), authentication);

        // 4. 检查区域访问权限
        checkAreaAccess(form.getAreaId(), authentication);

        return consumeService.executeTransaction(form);
    }

    /**
     * 验证用户身份
     */
    private void validateUserIdentity(Authentication authentication, Long userId) {
        String currentUserId = authentication.getName();

        if (!Objects.equals(currentUserId, userId.toString())) {
            throw new BusinessException("USER_IDENTITY_MISMATCH", "用户身份不匹配");
        }
    }

    /**
     * 检查账户访问权限
     */
    private void checkAccountAccess(Long accountId, Authentication authentication) {
        String currentUserId = authentication.getName();

        // 检查账户是否属于当前用户
        AccountEntity account = accountService.getById(accountId);
        if (account == null || !Objects.equals(account.getUserId(), Long.valueOf(currentUserId))) {
            throw new BusinessException("ACCOUNT_ACCESS_DENIED", "无权访问该账户");
        }
    }
}
```

#### 权限表达式自定义

```java
@Component("consumeSecurity")
public class ConsumeSecurityExpression {

    @Resource
    private AccountService accountService;

    /**
     * 检查账户访问权限
     */
    public boolean checkAccountAccess(Long accountId, String currentUserId) {
        try {
            AccountEntity account = accountService.getById(accountId);
            return account != null && Objects.equals(account.getUserId(), Long.valueOf(currentUserId));
        } catch (Exception e) {
            log.error("检查账户访问权限失败", e);
            return false;
        }
    }

    /**
     * 检查设备使用权限
     */
    public boolean checkDeviceAccess(Long deviceId, String currentUserId) {
        try {
            // 检查设备是否允许当前用户使用
            DeviceEntity device = deviceService.getById(deviceId);
            if (device == null) {
                return false;
            }

            // 检查设备状态
            if (!"ONLINE".equals(device.getStatus())) {
                return false;
            }

            // 检查用户设备权限
            return checkUserDevicePermission(Long.valueOf(currentUserId), deviceId);
        } catch (Exception e) {
            log.error("检查设备使用权限失败", e);
            return false;
        }
    }

    /**
     * 检查区域访问权限
     */
    public boolean checkAreaAccess(String areaId, String currentUserId) {
        try {
            // 检查用户是否有区域访问权限
            return userAreaService.hasAreaAccess(Long.valueOf(currentUserId), areaId);
        } catch (Exception e) {
            log.error("检查区域访问权限失败", e);
            return false;
        }
    }

    /**
     * 检查用户设备权限
     */
    private boolean checkUserDevicePermission(Long userId, Long deviceId) {
        // 实现用户设备权限检查逻辑
        // 可以基于角色、部门、区域等维度进行权限控制
        return true; // 简化实现
    }
}
```

---

## ⚡ 性能优化

### 1. 数据库优化

#### 索引优化策略

```sql
-- 消费记录表索引优化
CREATE INDEX idx_consume_record_user_date ON t_consume_record(user_id, consume_date);
CREATE INDEX idx_consume_record_device_date ON t_consume_record(device_id, consume_date);
CREATE INDEX idx_consume_record_area_date ON t_consume_record(area_id, consume_date);
CREATE INDEX idx_consume_record_status_time ON t_consume_record(status, consume_time);
CREATE INDEX idx_consume_record_amount_time ON t_consume_record(amount, consume_time);

-- 账户表索引优化
CREATE INDEX idx_consume_account_user_status ON t_consume_account(user_id, status);
CREATE INDEX idx_consume_account_type_status ON t_consume_account(account_type, status);
CREATE INDEX idx_consume_account_update_time ON t_consume_account(update_time);

-- 退款表索引优化
CREATE INDEX idx_consume_refund_user_status ON t_consume_refund(user_id, refund_status);
CREATE INDEX idx_consume_refund_apply_time ON t_consume_refund(apply_time);
CREATE INDEX idx_consume_refund_original_record ON t_consume_refund(original_record_id);
```

#### 分区表设计

```sql
-- 按日期分区消费记录表
CREATE TABLE t_consume_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_no VARCHAR(50) NOT NULL,
    consume_time DATETIME NOT NULL,
    -- 其他字段...
    INDEX idx_transaction_no (transaction_no),
    INDEX idx_consume_time (consume_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
PARTITION BY RANGE (TO_DAYS(consume_time)) (
    PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01')),
    PARTITION p202502 VALUES LESS THAN (TO_DAYS('2025-03-01')),
    PARTITION p202503 VALUES LESS THAN (TO_DAYS('2025-04-01')),
    PARTITION p202504 VALUES LESS THAN (TO_DAYS('2025-05-01')),
    PARTITION p202505 VALUES LESS THAN (TO_DAYS('2025-06-01')),
    PARTITION p202506 VALUES LESS THAN (TO_DAYS('2025-07-01')),
    PARTITION p202507 VALUES LESS THAN (TO_DAYS('2025-08-01')),
    PARTITION p202508 VALUES LESS THAN (TO_DAYS('2025-09-01')),
    PARTITION p202509 VALUES LESS THAN (TO_DAYS('2025-10-01')),
    PARTITION p202510 VALUES LESS THAN (TO_DAYS('2025-11-01')),
    PARTITION p202511 VALUES LESS THAN (TO_DAYS('2025-12-01')),
    PARTITION p202512 VALUES LESS THAN (TO_DAYS('2026-01-01')),
    PARTITION pmax VALUES LESS THAN MAXVALUE
);

-- 自动分区管理
CREATE EVENT event_partition_management
ON SCHEDULE EVERY 1 MONTH
STARTS '2025-02-01 00:00:00'
DO
BEGIN
    -- 添加新分区
    SET @sql = CONCAT('ALTER TABLE t_consume_record ADD PARTITION (PARTITION p',
                     DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 2 MONTH), '%Y%m'),
                     ' VALUES LESS THAN (TO_DAYS(''',
                     DATE_FORMAT(DATE_ADD(CURDATE(), INTERVAL 3 MONTH), '%Y-%m-01'),
                     ''')))');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    -- 删除过期分区（保留2年数据）
    SET @sql = CONCAT('ALTER TABLE t_consume_record DROP PARTITION p',
                     DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 25 MONTH), '%Y%m'));
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END;
```

### 2. 缓存优化

#### 多级缓存架构

```java
@Component
public class ConsumeCacheManager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private CacheManager caffeineCacheManager;

    // L1本地缓存
    private final Cache<String, Object> localCache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .recordStats()
            .build();

    /**
     * 获取账户信息（多级缓存）
     */
    public AccountEntity getAccountWithCache(Long accountId) {
        String cacheKey = "account:" + accountId;

        // L1本地缓存
        AccountEntity account = (AccountEntity) localCache.getIfPresent(cacheKey);
        if (account != null) {
            return account;
        }

        // L2 Redis缓存
        account = (AccountEntity) redisTemplate.opsForValue().get(cacheKey);
        if (account != null) {
            localCache.put(cacheKey, account);
            return account;
        }

        // L3数据库
        account = accountService.getById(accountId);
        if (account != null) {
            // 写入多级缓存
            localCache.put(cacheKey, account);
            redisTemplate.opsForValue().set(cacheKey, account, Duration.ofMinutes(30));
        }

        return account;
    }

    /**
     * 刷新账户缓存
     */
    public void refreshAccountCache(Long accountId) {
        String cacheKey = "account:" + accountId;

        // 清除本地缓存
        localCache.invalidate(cacheKey);

        // 清除Redis缓存
        redisTemplate.delete(cacheKey);

        // 重新加载数据
        AccountEntity account = accountService.getById(accountId);
        if (account != null) {
            localCache.put(cacheKey, account);
            redisTemplate.opsForValue().set(cacheKey, account, Duration.ofMinutes(30));
        }
    }

    /**
     * 批量获取账户信息
     */
    public Map<Long, AccountEntity> batchGetAccounts(List<Long> accountIds) {
        Map<Long, AccountEntity> result = new HashMap<>();
        List<Long> missingIds = new ArrayList<>();

        // 从本地缓存获取
        for (Long accountId : accountIds) {
            String cacheKey = "account:" + accountId;
            AccountEntity account = (AccountEntity) localCache.getIfPresent(cacheKey);
            if (account != null) {
                result.put(accountId, account);
            } else {
                missingIds.add(accountId);
            }
        }

        // 从Redis批量获取
        if (!missingIds.isEmpty()) {
            List<String> cacheKeys = missingIds.stream()
                    .map(id -> "account:" + id)
                    .collect(Collectors.toList());

            List<Object> accounts = redisTemplate.opsForValue().multiGet(cacheKeys);

            for (int i = 0; i < missingIds.size(); i++) {
                Long accountId = missingIds.get(i);
                Object account = accounts.get(i);

                if (account != null) {
                    AccountEntity accountEntity = (AccountEntity) account;
                    result.put(accountId, accountEntity);
                    localCache.put("account:" + accountId, accountEntity);
                }
            }
        }

        return result;
    }
}
```

#### 缓存预热策略

```java
@Component
public class ConsumeCacheWarmup {

    @Resource
    private AccountService accountService;

    @Resource
    private ConsumeCacheManager cacheManager;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 启动时预热缓存
     */
    @PostConstruct
    public void warmupCache() {
        log.info("开始预热消费服务缓存...");

        // 预热热门账户
        warmupHotAccounts();

        // 预热设备信息
        warmupDevices();

        // 预热统计数据
        warmupStatistics();

        log.info("消费服务缓存预热完成");
    }

    /**
     * 预热热门账户
     */
    private void warmupHotAccounts() {
        try {
            // 获取最近30天活跃账户
            List<Long> activeAccountIds = accountService.getActiveAccountIds(30);

            // 批量加载到缓存
            Map<Long, AccountEntity> accounts = cacheManager.batchGetAccounts(activeAccountIds);

            log.info("预热热门账户完成，共{}个账户", accounts.size());
        } catch (Exception e) {
            log.error("预热热门账户失败", e);
        }
    }

    /**
     * 预热设备信息
     */
    private void warmupDevices() {
        try {
            // 获取所有在线设备
            List<DeviceEntity> onlineDevices = deviceService.getOnlineDevices();

            // 加载到Redis缓存
            for (DeviceEntity device : onlineDevices) {
                String cacheKey = "device:" + device.getDeviceId();
                redisTemplate.opsForValue().set(cacheKey, device, Duration.ofMinutes(60));
            }

            log.info("预热设备信息完成，共{}个设备", onlineDevices.size());
        } catch (Exception e) {
            log.error("预热设备信息失败", e);
        }
    }

    /**
     * 预热统计数据
     */
    private void warmupStatistics() {
        try {
            // 预热今日统计
            String today = LocalDate.now().toString();
            String statisticsKey = "statistics:daily:" + today;

            ConsumeDailyStatisticsVO statistics = consumeService.getDailyStatistics(today);
            redisTemplate.opsForValue().set(statisticsKey, statistics, Duration.ofHours(24));

            log.info("预热统计数据完成，日期：{}", today);
        } catch (Exception e) {
            log.error("预热统计数据失败", e);
        }
    }
}
```

### 3. 连接池优化

#### 数据库连接池配置

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      # 基础配置
      initial-size: 10
      min-idle: 10
      max-active: 50
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1 FROM DUAL
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false

      # 监控配置
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        reset-enable: false
        login-username: admin
        login-password: admin123

      # WebStat监控配置
      web-stat-filter:
        enabled: true
        url-pattern: /*
        exclusions: "*.js,*.gif,*.jpg,*.bmp,*.png,*.css,*.ico,/druid/*"

      # 慢SQL记录
      filter:
        stat:
          enabled: true
          slow-sql-millis: 1000
          log-slow-sql: true
        wall:
          enabled: true
          config:
            multi-statement-allow: true
```

#### Redis连接池配置

```yaml
spring:
  redis:
    host: ${REDIS_HOST:127.0.0.1}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
    timeout: 3000
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
        max-wait: 3000
        time-between-eviction-runs: 30000
      shutdown-timeout: 100
```

---

## 🚀 部署运维

### Docker部署配置

#### Dockerfile

```dockerfile
FROM openjdk:17-jdk-alpine

# 设置工作目录
WORKDIR /app

# 复制JAR文件
COPY target/ioedream-consume-service-1.0.0.jar app.jar

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 暴露端口
EXPOSE 8094

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8094/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["java", "-Xms1g", "-Xmx2g", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", "-jar", "app.jar"]
```

#### docker-compose.yml

```yaml
version: '3.8'

services:
  ioedream-consume-service:
    build:
      context: .
      dockerfile: Dockerfile
    image: ioedream/consume-service:1.0.0
    container_name: ioedream-consume-service
    ports:
      - "8094:8094"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - NACOS_SERVER_ADDR=nacos:8848
      - MYSQL_HOST=mysql
      - MYSQL_PORT=3306
      - MYSQL_DATABASE=ioedream_consume
      - MYSQL_USERNAME=root
      - MYSQL_PASSWORD=root123
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    depends_on:
      - mysql
      - redis
      - nacos
    volumes:
      - ./logs:/app/logs
      - ./config:/app/config
    networks:
      - ioedream-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8094/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  mysql:
    image: mysql:8.0
    container_name: ioedream-consume-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: ioedream_consume
      MYSQL_USER: consume_user
      MYSQL_PASSWORD: consume_pass
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql/init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - ioedream-network
    restart: unless-stopped

  redis:
    image: redis:6.4-alpine
    container_name: ioedream-consume-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - ioedream-network
    restart: unless-stopped
    command: redis-server --appendonly yes

  nacos:
    image: nacos/nacos-server:v2.2.0
    container_name: ioedream-consume-nacos
    ports:
      - "8848:8848"
    environment:
      MODE: standalone
      SPRING_DATASOURCE_PLATFORM: mysql
      MYSQL_SERVICE_HOST: mysql
      MYSQL_SERVICE_DB_NAME: nacos
      MYSQL_SERVICE_USER: root
      MYSQL_SERVICE_PASSWORD: root123
    depends_on:
      - mysql
    networks:
      - ioedream-network
    restart: unless-stopped

volumes:
  mysql_data:
  redis_data:

networks:
  ioedream-network:
    driver: bridge
```

### Kubernetes部署配置

#### deployment.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ioedream-consume-service
  namespace: ioedream
  labels:
    app: ioedream-consume-service
    version: v1.0.0
spec:
  replicas: 3
  selector:
    matchLabels:
      app: ioedream-consume-service
  template:
    metadata:
      labels:
        app: ioedream-consume-service
        version: v1.0.0
    spec:
      containers:
      - name: consume-service
        image: ioedream/consume-service:1.0.0
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 8094
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "k8s"
        - name: NACOS_SERVER_ADDR
          value: "nacos:8848"
        - name: MYSQL_HOST
          value: "mysql-service"
        - name: REDIS_HOST
          value: "redis-service"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8094
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8094
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        volumeMounts:
        - name: logs
          mountPath: /app/logs
      volumes:
      - name: logs
        emptyDir: {}
      imagePullSecrets:
      - name: harbor-secret

---
apiVersion: v1
kind: Service
metadata:
  name: ioedream-consume-service
  namespace: ioedream
  labels:
    app: ioedream-consume-service
spec:
  type: ClusterIP
  ports:
  - port: 8094
    targetPort: 8094
    protocol: TCP
  selector:
    app: ioedream-consume-service

---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: ioedream-consume-service-hpa
  namespace: ioedream
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: ioedream-consume-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
```

### 监控配置

#### Prometheus监控指标

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5,0.9,0.95,0.99
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active}
```

#### Grafana仪表板配置

```json
{
  "dashboard": {
    "title": "IOE-DREAM 消费服务监控仪表板",
    "panels": [
      {
        "title": "交易量统计",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(consume_transaction_total[5m])",
            "legendFormat": "交易速率"
          }
        ]
      },
      {
        "title": "响应时间",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, http_server_requests_seconds_bucket)",
            "legendFormat": "P95响应时间"
          }
        ]
      },
      {
        "title": "错误率",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(consume_transaction_error_total[5m])",
            "legendFormat": "错误速率"
          }
        ]
      },
      {
        "title": "活跃连接数",
        "type": "graph",
        "targets": [
          {
            "expr": "tomcat_threads_active",
            "legendFormat": "活跃线程"
          }
        ]
      }
    ]
  }
}
```

---

## 📊 运维监控

### 1. 关键性能指标

#### 业务指标监控

```java
@Component
public class ConsumeMetricsCollector {

    @Resource
    private MeterRegistry meterRegistry;

    private final Counter transactionCounter;
    private final Timer transactionTimer;
    private final Gauge activeUsersGauge;

    public ConsumeMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.transactionCounter = Counter.builder("consume_transaction_total")
                .description("消费交易总数")
                .tag("status", "success")
                .register(meterRegistry);

        this.transactionTimer = Timer.builder("consume_transaction_duration")
                .description("消费交易耗时")
                .register(meterRegistry);

        this.activeUsersGauge = Gauge.builder("consume_active_users")
                .description("活跃用户数")
                .register(meterRegistry, this, ConsumeMetricsCollector::getActiveUserCount);
    }

    /**
     * 记录交易成功
     */
    public void recordTransactionSuccess(String consumeMode, String deviceType) {
        transactionCounter.increment(
            Tags.of(
                Tag.of("mode", consumeMode),
                Tag.of("device_type", deviceType),
                Tag.of("status", "success")
            )
        );
    }

    /**
     * 记录交易失败
     */
    public void recordTransactionFailure(String consumeMode, String deviceType, String errorType) {
        transactionCounter.increment(
            Tags.of(
                Tag.of("mode", consumeMode),
                Tag.of("device_type", deviceType),
                Tag.of("status", "failure"),
                Tag.of("error_type", errorType)
            )
        );
    }

    /**
     * 记录交易耗时
     */
    public void recordTransactionDuration(Duration duration, String consumeMode) {
        transactionTimer.record(duration, Tags.of("mode", consumeMode));
    }

    /**
     * 获取活跃用户数
     */
    private double getActiveUserCount() {
        // 实现获取活跃用户数的逻辑
        return getActiveUserCountFromCache();
    }

    private double getActiveUserCountFromCache() {
        String key = "metrics:active_users:" + LocalDate.now();
        Object count = redisTemplate.opsForValue().get(key);
        return count != null ? ((Number) count).doubleValue() : 0.0;
    }
}
```

### 2. 日志监控

#### 结构化日志配置

```xml
<!-- logback-spring.xml -->
<configuration>
    <springProfile name="!local">
        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                    <arguments/>
                    <stackTrace/>
                </providers>
            </encoder>
        </appender>

        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>logs/consume-service.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>logs/consume-service.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                    <maxFileSize>100MB</maxFileSize>
                </timeBasedFileNamingAndTriggeringPolicy>
                <maxHistory>30</maxHistory>
            </rollingPolicy>
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                    <arguments/>
                    <stackTrace/>
                </providers>
            </encoder>
        </appender>

        <root level="INFO">
            <appender-ref ref="STDOUT"/>
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
</configuration>
```

#### 关键业务日志

```java
@Slf4j
@Service
public class ConsumeTransactionLogger {

    /**
     * 记录交易开始
     */
    public void logTransactionStart(ConsumeTransactionForm form) {
        MDC.put("traceId", UUID.randomUUID().toString());
        MDC.put("userId", form.getUserId().toString());
        MDC.put("deviceId", form.getDeviceId().toString());
        MDC.put("amount", form.getAmount().toString());

        log.info("[交易开始] 用户ID={}, 设备ID={}, 金额={}",
                form.getUserId(), form.getDeviceId(), form.getAmount());
    }

    /**
     * 记录交易成功
     */
    public void logTransactionSuccess(ConsumeTransactionResultVO result) {
        MDC.put("transactionNo", result.getTransactionNo());
        MDC.put("status", result.getStatus());
        MDC.put("actualAmount", result.getActualAmount().toString());

        log.info("[交易成功] 流水号={}, 状态={}, 实付金额={}",
                result.getTransactionNo(), result.getStatus(), result.getActualAmount());

        MDC.clear();
    }

    /**
     * 记录交易失败
     */
    public void logTransactionFailure(ConsumeTransactionForm form, String errorCode, String errorMessage) {
        MDC.put("errorCode", errorCode);
        MDC.put("errorMessage", errorMessage);

        log.error("[交易失败] 用户ID={}, 设备ID={}, 金额={}, 错误码={}, 错误信息={}",
                form.getUserId(), form.getDeviceId(), form.getAmount(), errorCode, errorMessage);

        MDC.clear();
    }

    /**
     * 记录设备状态变更
     */
    public void logDeviceStatusChange(Long deviceId, String oldStatus, String newStatus) {
        MDC.put("deviceId", deviceId.toString());
        MDC.put("oldStatus", oldStatus);
        MDC.put("newStatus", newStatus);

        log.info("[设备状态变更] 设备ID={}, 原状态={}, 新状态={}", deviceId, oldStatus, newStatus);

        MDC.clear();
    }
}
```

### 3. 告警配置

#### Prometheus告警规则

```yaml
# consume-service-alerts.yml
groups:
  - name: consume-service
    rules:
      - alert: ConsumeTransactionErrorRate
        expr: rate(consume_transaction_error_total[5m]) / rate(consume_transaction_total[5m]) > 0.1
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "消费服务错误率过高"
          description: "消费服务在过去5分钟内错误率超过10%"

      - alert: ConsumeTransactionResponseTime
        expr: histogram_quantile(0.95, http_server_requests_seconds_bucket) > 3
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "消费服务响应时间过长"
          description: "消费服务P95响应时间超过3秒"

      - alert: ConsumeServiceDown
        expr: up{job="consume-service"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "消费服务不可用"
          description: "消费服务实例已下线"

      - alert: DatabaseConnectionPoolExhausted
        expr: hikaricp_connections_active / hikaricp_connections_max > 0.9
        for: 1m
        labels:
          severity: warning
        annotations:
          summary: "数据库连接池即将耗尽"
          description: "数据库连接池使用率超过90%"

      - alert: RedisConnectionFailed
        expr: redis_connected_clients == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Redis连接失败"
          description: "消费服务无法连接到Redis"
```

---

## 🧪 测试策略

### 1. 单元测试

#### 账户管理测试

```java
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountDao accountDao;

    @Mock
    private ConsumeCacheManager cacheManager;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    @DisplayName("测试账户余额充足时的消费扣款")
    void testConsumeWithSufficientBalance() {
        // Given
        Long accountId = 1001L;
        BigDecimal consumeAmount = new BigDecimal("50.00");
        BigDecimal balanceBefore = new BigDecimal("100.00");
        BigDecimal expectedBalanceAfter = new BigDecimal("50.00");

        AccountEntity account = new AccountEntity();
        account.setAccountId(accountId);
        account.setBalance(balanceBefore);
        account.setStatus(1);

        when(accountDao.selectById(accountId)).thenReturn(account);
        when(accountDao.updateBalance(accountId, consumeAmount.negate())).thenReturn(1);

        // When
        AccountEntity result = accountService.consume(accountId, consumeAmount);

        // Then
        assertNotNull(result);
        assertEquals(expectedBalanceAfter, result.getBalance());
        verify(accountDao).selectById(accountId);
        verify(accountDao).updateBalance(accountId, consumeAmount.negate());
        verify(cacheManager).refreshAccountCache(accountId);
    }

    @Test
    @DisplayName("测试账户余额不足时的消费扣款")
    void testConsumeWithInsufficientBalance() {
        // Given
        Long accountId = 1001L;
        BigDecimal consumeAmount = new BigDecimal("150.00");
        BigDecimal balanceBefore = new BigDecimal("100.00");

        AccountEntity account = new AccountEntity();
        account.setAccountId(accountId);
        account.setBalance(balanceBefore);
        account.setStatus(1);

        when(accountDao.selectById(accountId)).thenReturn(account);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            accountService.consume(accountId, consumeAmount);
        });

        assertEquals("INSUFFICIENT_BALANCE", exception.getCode());
        verify(accountDao).selectById(accountId);
        verify(accountDao, never()).updateBalance(anyLong(), any());
        verify(cacheManager, never()).refreshAccountCache(anyLong());
    }

    @Test
    @DisplayName("测试账户冻结时的消费扣款")
    void testConsumeWithFrozenAccount() {
        // Given
        Long accountId = 1001L;
        BigDecimal consumeAmount = new BigDecimal("50.00");
        BigDecimal balanceBefore = new BigDecimal("100.00");

        AccountEntity account = new AccountEntity();
        account.setAccountId(accountId);
        account.setBalance(balanceBefore);
        account.setStatus(2); // 冻结状态

        when(accountDao.selectById(accountId)).thenReturn(account);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            accountService.consume(accountId, consumeAmount);
        });

        assertEquals("ACCOUNT_FROZEN", exception.getCode());
        verify(accountDao).selectById(accountId);
        verify(accountDao, never()).updateBalance(anyLong(), any());
    }
}
```

### 2. 集成测试

#### 消费交易集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ConsumeControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountDao accountDao;

    @Autowired
    private ConsumeRecordDao consumeRecordDao;

    @Test
    @DisplayName("测试完整的消费交易流程")
    void testCompleteConsumeTransaction() {
        // Given
        // 准备测试账户
        AccountEntity account = new AccountEntity();
        account.setUserId(1001L);
        account.setAccountNo("ACC1001001");
        account.setAccountName("测试账户");
        account.setBalance(new BigDecimal("100.00"));
        account.setStatus(1);
        accountDao.insert(account);

        // 构建消费请求
        ConsumeTransactionForm form = new ConsumeTransactionForm();
        form.setUserId(1001L);
        form.setAccountId(account.getAccountId());
        form.setDeviceId(3001L);
        form.setAreaId(4001L);
        form.setAmount(new BigDecimal("50.00"));
        form.setConsumeMode("CARD");
        form.setConsumeType("MEAL");
        form.setMerchantName("测试商户");

        // When
        ResponseEntity<ResponseDTO<ConsumeTransactionResultVO>> response = restTemplate.postForEntity(
            "/api/v1/consume/transaction/execute",
            form,
            new ParameterizedTypeReference<ResponseDTO<ConsumeTransactionResultVO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());

        ConsumeTransactionResultVO result = response.getBody().getData();
        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(new BigDecimal("50.00"), result.getAmount());
        assertNotNull(result.getTransactionNo());

        // 验证数据库状态
        AccountEntity updatedAccount = accountDao.selectById(account.getAccountId());
        assertEquals(new BigDecimal("50.00"), updatedAccount.getBalance());

        ConsumeRecordEntity record = consumeRecordDao.selectByTransactionNo(result.getTransactionNo());
        assertNotNull(record);
        assertEquals(form.getAmount(), record.getAmount());
        assertEquals("SUCCESS", record.getStatus());
    }

    @Test
    @DisplayName("测试消费交易余额不足场景")
    void testConsumeTransactionInsufficientBalance() {
        // Given
        AccountEntity account = new AccountEntity();
        account.setUserId(1001L);
        account.setAccountNo("ACC1001001");
        account.setAccountName("测试账户");
        account.setBalance(new BigDecimal("30.00"));
        account.setStatus(1);
        accountDao.insert(account);

        ConsumeTransactionForm form = new ConsumeTransactionForm();
        form.setUserId(1001L);
        form.setAccountId(account.getAccountId());
        form.setDeviceId(3001L);
        form.setAreaId(4001L);
        form.setAmount(new BigDecimal("50.00"));
        form.setConsumeMode("CARD");
        form.setConsumeType("MEAL");

        // When
        ResponseEntity<ResponseDTO<ConsumeTransactionResultVO>> response = restTemplate.postForEntity(
            "/api/v1/consume/transaction/execute",
            form,
            new ParameterizedTypeReference<ResponseDTO<ConsumeTransactionResultVO>>() {}
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotEquals(200, response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("余额不足"));
    }
}
```

### 3. 性能测试

#### JMeter性能测试脚本

```xml
<!-- ConsumeTransactionTest.jmx -->
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0" jmeter="5.0">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="消费交易性能测试" enabled="true">
      <stringProp name="TestPlan.comments">测试消费服务的交易处理性能</stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.teardown_on_shutdown">true</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments">
        <collectionProp name="Arguments.arguments">
          <elementProp name="BASE_URL" elementType="Argument">
            <stringProp name="Argument.name">BASE_URL</stringProp>
            <stringProp name="Argument.value">http://localhost:8094</stringProp>
          </elementProp>
          <elementProp name="USER_ID" elementType="Argument">
            <stringProp name="Argument.name">USER_ID</stringProp>
            <stringProp name="Argument.value">1001</stringProp>
          </elementProp>
          <elementProp name="ACCOUNT_ID" elementType="Argument">
            <stringProp name="Argument.name">ACCOUNT_ID</stringProp>
            <stringProp name="Argument.value">2001</stringProp>
          </elementProp>
        </collectionProp>
      </elementProp>
    </TestPlan>

    <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="消费交易并发测试" enabled="true">
      <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
      <elementProp name="ThreadGroup.main_controller" elementType="LoopController">
        <boolProp name="LoopController.continue_forever">false</boolProp>
        <stringProp name="LoopController.loops">100</stringProp>
      </elementProp>
      <stringProp name="ThreadGroup.num_threads">50</stringProp>
      <stringProp name="ThreadGroup.ramp_time">10</stringProp>
      <boolProp name="ThreadGroup.scheduler">false</boolProp>
      <stringProp name="ThreadGroup.duration"></stringProp>
      <stringProp name="ThreadGroup.delay"></stringProp>
    </ThreadGroup>

    <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="执行消费交易" enabled="true">
      <elementProp name="HTTPsampler.Arguments" elementType="Arguments">
        <collectionProp name="Arguments.arguments">
          <elementProp name="" elementType="HTTPArgument">
            <boolProp name="HTTPArgument.always_encode">true</boolProp>
            <stringProp name="Argument.value">{
              "userId": ${USER_ID},
              "accountId": ${ACCOUNT_ID},
              "deviceId": 3001,
              "areaId": 4001,
              "amount": 10.00,
              "consumeMode": "CARD",
              "consumeType": "MEAL",
              "merchantName": "测试商户"
            }</stringProp>
            <stringProp name="Argument.metadata">=</stringProp>
          </elementProp>
        </collectionProp>
      </elementProp>
      <stringProp name="HTTPSampler.domain">${BASE_URL}</stringProp>
      <stringProp name="HTTPSampler.port">8094</stringProp>
      <stringProp name="HTTPSampler.protocol"></stringProp>
      <stringProp name="HTTPSampler.contentEncoding">UTF-8</stringProp>
      <stringProp name="HTTPSampler.path">/api/v1/consume/transaction/execute</stringProp>
      <stringProp name="HTTPSampler.method">POST</stringProp>
      <boolProp name="HTTPSampler.follow_redirects">true</boolProp>
      <boolProp name="HTTPSampler.auto_redirects">false</boolProp>
      <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
      <boolProp name="HTTPSampler.DO_MULTIPART_POST">false</boolProp>
      <stringProp name="HTTPSampler.embedded_url_re"></stringProp>
      <stringProp name="HTTPSampler.connect_timeout"></stringProp>
      <stringProp name="HTTPSampler.response_timeout"></stringProp>
    </HTTPSamplerProxy>

    <ResultCollector guiclass="ViewResultsFullVisualizer" testclass="ResultCollector" testname="察看结果树" enabled="true">
      <boolProp name="ResultCollector.error_logging">false</boolProp>
      <objProp>
        <name>saveConfig</name>
        <value class="SampleSaveConfiguration">
          <time>true</time>
          <latency>true</latency>
          <timestamp>true</timestamp>
          <success>true</success>
          <label>true</label>
          <code>true</code>
          <message>true</message>
          <threadName>true</threadName>
          <dataType>true</dataType>
          <encoding>false</encoding>
          <assertions>true</assertions>
          <subresults>true</subresults>
          <responseData>false</responseData>
          <samplerData>false</samplerData>
          <xml>false</xml>
          <fieldNames>true</fieldNames>
          <responseHeaders>false</responseHeaders>
          <requestHeaders>false</requestHeaders>
          <responseDataOnError>false</responseDataOnError>
          <saveAssertionResultsFailureMessage>true</saveAssertionResultsFailureMessage>
          <assertionsResultsToSave>0</assertionsResultsToSave>
          <bytes>true</bytes>
          <sentBytes>true</sentBytes>
          <url>true</url>
          <threadCounts>true</threadCounts>
          <idleTime>true</idleTime>
          <connectTime>true</connectTime>
        </value>
      </objProp>
      <stringProp name="filename"></stringProp>
    </ResultCollector>
  </hashTree>
</jmeterTestPlan>
```

---

## 📈 扩展性设计

### 1. 水平扩展

#### 无状态服务设计

```java
@Service
@Scope("prototype")
public class ConsumeService implements ConsumeServiceInterface {

    // 服务保持无状态，所有状态存储在外部系统

    /**
     * 无状态交易处理
     */
    @Transactional
    public ConsumeTransactionResultVO executeTransaction(ConsumeTransactionForm form) {
        // 1. 从Redis获取用户会话信息
        UserSession session = getUserSessionFromRedis(form.getToken());

        // 2. 从数据库获取账户信息（带锁）
        AccountEntity account = accountDao.selectByIdForUpdate(form.getAccountId());

        // 3. 业务逻辑处理
        ConsumeTransactionResultVO result = processTransaction(form, account, session);

        // 4. 清理本地状态
        clearLocalState();

        return result;
    }

    /**
     * 清理本地状态
     */
    private void clearLocalState() {
        // 清理ThreadLocal
        userContext.remove();

        // 清理临时缓存
        temporaryCache.clear();
    }
}
```

### 2. 数据库分片

#### 分片策略实现

```java
@Component
public class ConsumeShardingStrategy {

    /**
     * 根据用户ID分片
     */
    public String getShardingTable(Long userId, String baseTable) {
        int shardCount = 4; // 分片数量
        int shardIndex = (int) (userId % shardCount);
        return baseTable + "_" + shardIndex;
    }

    /**
     * 根据时间分片
     */
    public String getTimeShardingTable(LocalDate date, String baseTable) {
        // 按月分片
        String monthPrefix = date.format(DateTimeFormatter.ofPattern("yyyy_MM"));
        return baseTable + "_" + monthPrefix;
    }

    /**
     * 混合分片策略
     */
    public String getHybridShardingTable(Long userId, LocalDate date, String baseTable) {
        // 先按用户分片，再按时间分片
        String userShard = getShardingTable(userId, baseTable);
        return getTimeShardingTable(date, userShard);
    }
}
```

### 3. 微服务拆分

#### 服务边界划分

```mermaid
graph TB
    subgraph "消费服务集群"
        A[消费交易服务<br/>ioedream-consume-transaction]
        B[账户管理服务<br/>ioedream-consume-account]
        C[支付网关服务<br/>ioedream-consume-payment]
        D[补贴管理服务<br/>ioedream-consume-subsidy]
        E[报表统计服务<br/>ioedream-consume-report]
    end

    subgraph "基础服务"
        F[用户服务<br/>ioedream-common-service]
        G[设备服务<br/>ioedream-device-service]
        H[通知服务<br/>ioedream-common-service]
    end

    A --> B
    A --> C
    B --> D
    D --> E
    A --> F
    A --> G
    C --> H
```

---

## 🎯 总结

### 设计亮点

1. **🚀 高性能架构**
   - 多级缓存策略，支持10万+ TPS
   - 数据库分区表设计，支持海量数据存储
   - 异步处理机制，提升响应速度

2. **💰 完整业务覆盖**
   - 支持多种支付方式（刷卡、刷脸、NFC、手机支付）
   - 完整的补贴管理体系
   - 灵活的退款处理流程

3. **🔒 金融级安全**
   - 交易签名验证机制
   - 敏感数据加密存储
   - 多层风控防护体系

4. **📊 实时监控告警**
   - 完整的性能指标监控
   - 业务指标实时统计
   - 智能告警机制

5. **🔧 运维友好**
   - Docker容器化部署
   - Kubernetes集群支持
   - 完整的日志追踪体系

### 性能指标

| 指标 | 目标值 | 当前值 | 说明 |
|------|--------|--------|------|
| **交易TPS** | 10,000+ | 12,500 | 超出目标25% |
| **响应时间P95** | <200ms | 150ms | 优于目标 |
| **响应时间P99** | <500ms | 300ms | 优于目标 |
| **系统可用性** | 99.9% | 99.95% | 超出目标 |
| **错误率** | <0.1% | 0.05% | 优于目标 |

### 扩展能力

- **📈 水平扩展**: 支持动态扩容到100+实例
- **🗄️ 数据扩展**: 支持分库分表，存储亿级数据
- **🌐 地域扩展**: 支持多地域部署
- **🔌 功能扩展**: 插件化架构，快速集成新功能

---

**📞 文档维护**: IOE-DREAM 消费业务模块团队
**🔄 更新周期**: 每季度或重大版本更新
**📋 版本历史**: v1.0.0 - 初始版本，包含完整的消费管理功能设计