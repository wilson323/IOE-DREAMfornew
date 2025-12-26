# P0-2 消费账户余额扣减功能 - 实施计划

> **创建时间**: 2025-12-23 09:00
> **实施方案**: 方案A - 完整实施（10天工作量）
> **预计代码量**: 2500+行

---

## 📋 功能概述

### 核心功能

1. **在线消费余额扣减**
   - 用户通过POS机、扫码等方式消费时
   - 实时调用账户服务扣减余额
   - 支持分布式事务和幂等性

2. **离线消费余额扣减**
   - 网络故障时，设备先记录消费
   - 网络恢复后，自动补扣余额
   - 补偿机制保证最终一致性

3. **退款余额增加**
   - 用户申请退款时
   - 调用账户服务增加余额
   - 支持部分退款和全额退款

---

## 🗄️ 数据库表设计

### 1. 消费账户表 (t_consume_account)

```sql
CREATE TABLE t_consume_account (
    account_id            BIGINT          NOT NULL AUTO_INCREMENT COMMENT '账户ID',
    user_id               BIGINT          NOT NULL COMMENT '用户ID',
    account_no            VARCHAR(32)     NOT NULL COMMENT '账户编号',
    account_name          VARCHAR(50)     NOT NULL COMMENT '账户名称',
    balance               DECIMAL(10,2)  NOT NULL DEFAULT 0.00 COMMENT '账户余额',
    frozen_amount         DECIMAL(10,2)  NOT NULL DEFAULT 0.00 COMMENT '冻结金额',
    available_amount      DECIMAL(10,2)  NOT NULL COMMENT '可用余额',
    account_type          TINYINT         NOT NULL DEFAULT 1 COMMENT '账户类型 1-个人 2-企业',
    account_status        TINYINT         NOT NULL DEFAULT 1 COMMENT '账户状态 1-正常 2-冻结 3-注销',
    credit_limit          DECIMAL(10,2)  NOT NULL DEFAULT 0.00 COMMENT '信用额度',
    password              VARCHAR(64)              DEFAULT NULL COMMENT '支付密码',
    security_question     VARCHAR(100)            DEFAULT NULL COMMENT '安全问题',
    security_answer       VARCHAR(100)            DEFAULT NULL COMMENT '安全答案',
    last_consume_time     DATETIME                 DEFAULT NULL COMMENT '最后消费时间',
    last_recharge_time    DATETIME                 DEFAULT NULL COMMENT '最后充值时间',
    remark                VARCHAR(500)             DEFAULT NULL COMMENT '备注',
    create_time           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag          TINYINT         NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    PRIMARY KEY (account_id),
    UNIQUE KEY uk_account_no (account_no),
    UNIQUE KEY uk_user_id (user_id, deleted_flag),
    KEY idx_account_status (account_status, deleted_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费账户表';
```

### 2. 消费记录表 (t_consume_record)

```sql
CREATE TABLE t_consume_record (
    record_id             BIGINT          NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    account_id            BIGINT          NOT NULL COMMENT '账户ID',
    user_id               BIGINT          NOT NULL COMMENT '用户ID',
    user_name             VARCHAR(50)     NOT NULL COMMENT '用户姓名',
    device_id             VARCHAR(32)     NOT NULL COMMENT '设备ID',
    device_name           VARCHAR(50)              DEFAULT NULL COMMENT '设备名称',
    merchant_id           BIGINT          NOT NULL COMMENT '商户ID',
    merchant_name         VARCHAR(100)             DEFAULT NULL COMMENT '商户名称',
    amount                DECIMAL(10,2)  NOT NULL COMMENT '消费金额',
    original_amount       DECIMAL(10,2)             DEFAULT NULL COMMENT '原始金额（优惠前）',
    discount_amount       DECIMAL(10,2)  NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
    consume_type          VARCHAR(20)     NOT NULL COMMENT '消费类型 MEAL-餐饮 SNACK-零食 DRINK-饮品',
    consume_type_name     VARCHAR(50)              DEFAULT NULL COMMENT '消费类型名称',
    product_detail        TEXT                     DEFAULT NULL COMMENT '商品明细(JSON)',
    payment_method        VARCHAR(20)     NOT NULL COMMENT '支付方式 BALANCE-余额 CARD-卡 CASH-现金',
    order_no              VARCHAR(64)     NOT NULL COMMENT '订单号',
    transaction_no        VARCHAR(64)             DEFAULT NULL COMMENT '交易流水号',
    transaction_status    TINYINT         NOT NULL DEFAULT 1 COMMENT '交易状态 1-成功 2-处理中 3-失败',
    consume_status        TINYINT         NOT NULL DEFAULT 1 COMMENT '消费状态 1-正常 2-已退款 3-已撤销',
    consume_time          DATETIME        NOT NULL COMMENT '消费时间',
    consume_location      VARCHAR(100)             DEFAULT NULL COMMENT '消费地点',
    refund_status         TINYINT         NOT NULL DEFAULT 0 COMMENT '退款状态 0-未退款 1-部分退款 2-全额退款',
    refund_amount         DECIMAL(10,2)  NOT NULL DEFAULT 0.00 COMMENT '退款金额',
    refund_time           DATETIME                 DEFAULT NULL COMMENT '退款时间',
    refund_reason         VARCHAR(200)             DEFAULT NULL COMMENT '退款原因',
    offline_flag          TINYINT         NOT NULL DEFAULT 0 COMMENT '离线标记 0-在线 1-离线',
    sync_status           TINYINT         NOT NULL DEFAULT 1 COMMENT '同步状态 0-未同步 1-已同步',
    sync_time             DATETIME                 DEFAULT NULL COMMENT '同步时间',
    remark                VARCHAR(500)             DEFAULT NULL COMMENT '备注',
    create_time           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag          TINYINT         NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    PRIMARY KEY (record_id),
    UNIQUE KEY uk_order_no (order_no, deleted_flag),
    KEY idx_account_id (account_id, consume_time),
    KEY idx_user_id (user_id, consume_time),
    KEY idx_transaction_no (transaction_no),
    KEY idx_sync_status (offline_flag, sync_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费记录表';
```

### 3. 账户变动记录表 (t_consume_account_transaction)

```sql
CREATE TABLE t_consume_account_transaction (
    transaction_id        BIGINT          NOT NULL AUTO_INCREMENT COMMENT '交易ID',
    account_id            BIGINT          NOT NULL COMMENT '账户ID',
    user_id               BIGINT          NOT NULL COMMENT '用户ID',
    transaction_type      VARCHAR(20)     NOT NULL COMMENT '交易类型 CONSUME-消费 RECHARGE-充值 REFUND-退款 DEDUCT-扣减 ADJUST-调整',
    transaction_no        VARCHAR(64)     NOT NULL COMMENT '交易流水号',
    business_no           VARCHAR(64)     NOT NULL COMMENT '业务编号',
    amount                DECIMAL(10,2)  NOT NULL COMMENT '变动金额 正-增加 负-减少',
    balance_before        DECIMAL(10,2)  NOT NULL COMMENT '变动前余额',
    balance_after         DECIMAL(10,2)  NOT NULL COMMENT '变动后余额',
    frozen_amount_before  DECIMAL(10,2)  NOT NULL DEFAULT 0.00 COMMENT '变动前冻结金额',
    frozen_amount_after   DECIMAL(10,2)  NOT NULL DEFAULT 0.00 COMMENT '变动后冻结金额',
    related_record_id     BIGINT                   DEFAULT NULL COMMENT '关联记录ID',
    related_order_no      VARCHAR(64)             DEFAULT NULL COMMENT '关联订单号',
    transaction_status    TINYINT         NOT NULL DEFAULT 1 COMMENT '交易状态 1-成功 2-处理中 3-失败',
    fail_reason           VARCHAR(200)             DEFAULT NULL COMMENT '失败原因',
    transaction_time      DATETIME        NOT NULL COMMENT '交易时间',
    operator_id           BIGINT                   DEFAULT NULL COMMENT '操作员ID',
    operator_name         VARCHAR(50)              DEFAULT NULL COMMENT '操作员姓名',
    remark                VARCHAR(500)             DEFAULT NULL COMMENT '备注',
    create_time           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag          TINYINT         NOT NULL DEFAULT 0 COMMENT '删除标记 0-未删除 1-已删除',
    PRIMARY KEY (transaction_id),
    UNIQUE KEY uk_transaction_no (transaction_no),
    KEY idx_account_id (account_id, transaction_time),
    KEY idx_user_id (user_id, transaction_time),
    KEY idx_business_no (business_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账户变动记录表';
```

---

## 🔌 API接口设计

### 1. 消费账户服务

#### 1.1 查询账户余额
```java
GET /api/v1/consume/account/balance/{accountId}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "accountId": 1001,
    "userId": 10001,
    "balance": 1000.00,
    "frozenAmount": 0.00,
    "availableAmount": 1000.00
  }
}
```

#### 1.2 在线消费扣减余额
```java
POST /api/v1/consume/account/deduct

Request:
{
  "accountId": 1001,
  "userId": 10001,
  "amount": 25.50,
  "orderNo": "ORDER20251223001",
  "consumeType": "MEAL",
  "consumeLocation": "一楼餐厅"
}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "recordId": 10001,
    "balanceBefore": 1000.00,
    "balanceAfter": 974.50,
    "transactionNo": "TXN20251223001"
  }
}
```

#### 1.3 离线消费补扣余额
```java
POST /api/v1/consume/offline/sync

Request:
{
  "offlineRecords": [
    {
      "accountId": 1001,
      "userId": 10001,
      "amount": 25.50,
      "orderNo": "OFFLINE20251223001",
      "deviceId": "POS001",
      "consumeTime": "2025-12-23 12:00:00"
    }
  ]
}

Response:
{
  "code": 200,
  "message": "同步成功",
  "data": {
    "successCount": 1,
    "failCount": 0,
    "details": [...]
  }
}
```

#### 1.4 退款增加余额
```java
POST /api/v1/consume/refund

Request:
{
  "recordId": 10001,
  "refundAmount": 25.50,
  "refundReason": "菜品质量问题"
}

Response:
{
  "code": 200,
  "message": "退款成功",
  "data": {
    "refundId": 20001,
    "balanceBefore": 974.50,
    "balanceAfter": 1000.00,
    "transactionNo": "REFUND20251223001"
  }
}
```

---

## 🏗️ 实施步骤

### 阶段2.1：创建数据库迁移脚本 ✅
- [x] V20251223__create_consume_account_table.sql
- [x] V20251223__create_consume_record_table.sql
- [x] V20251223__create_consume_account_transaction_table.sql

### 阶段2.2：创建Entity和DAO（待实施）
- [ ] ConsumeAccountEntity + ConsumeAccountDao
- [ ] ConsumeRecordEntity + ConsumeRecordDao
- [ ] ConsumeAccountTransactionEntity + ConsumeAccountTransactionDao

### 阶段2.3：创建Manager层（待实施）
- [ ] ConsumeAccountManager
- [ ] ConsumeRecordManager
- [ ] ConsumeTransactionManager

### 阶段2.4：实现Service层（待实施）
- [ ] ConsumeAccountServiceImpl
- [ ] ConsumeRecordServiceImpl
- [ ] ConsumeRefundServiceImpl

### 阶段2.5：集成账户服务（待实施）
- [ ] 在线消费扣减调用AccountServiceClient.decreaseBalance
- [ ] 退款增加调用AccountServiceClient.increaseBalance
- [ ] 离线消费补偿机制

### 阶段2.6：测试验证（待实施）
- [ ] Entity单元测试
- [ ] Service集成测试
- [ ] 端到端测试
- [ ] 确保100%测试通过率

---

**文档状态**: 已完成设计阶段，准备进入代码实施阶段
**下一步**: 开始创建Entity和DAO
