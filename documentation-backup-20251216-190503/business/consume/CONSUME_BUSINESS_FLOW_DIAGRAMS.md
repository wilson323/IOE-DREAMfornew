# IOE-DREAM 智慧消费管理系统业务流程图

## 1. 消费管理模块业务流程概览

### 1.1 消费管理业务架构
```mermaid
graph TB
    subgraph "消费管理体系"
        CM[消费管理中心]
        PM[支付管理]
        AM[账户管理]
        RM[充值管理]
        BM[业务管理]
        SM[统计分析]
        NM[通知管理]
    end

    subgraph "关联系统"
        AUTH[认证系统]
        ACCESS[门禁系统]
        OA[OA系统]
        NOTIFICATION[通知系统]
        FINANCE[财务系统]
        REPORT[报表系统]
    end

    CM --> PM
    CM --> AM
    CM --> RM
    CM --> BM
    CM --> SM
    CM --> NM

    PM --> AUTH
    AM --> ACCESS
    BM --> OA
    RM --> NOTIFICATION
    SM --> FINANCE
    NM --> REPORT
```

### 1.2 消费管理数据流程
```mermaid
flowchart LR
    subgraph "数据源"
        USER[用户信息]
        CARD[卡片信息]
        DEVICE[消费设备]
        PAYMENT[支付方式]
        PRODUCT[商品信息]
    end

    subgraph "消费处理"
        VERIFY[身份验证]
        CHECK[余额检查]
        PAY[支付处理]
        RECORD[记录消费]
        NOTIFY[结果通知]
    end

    subgraph "数据输出"
        TRANSACTION[交易记录]
        BALANCE[余额更新]
        REPORT[统计报表]
        ALERT[异常告警]
        LOG[消费日志]
    end

    USER --> VERIFY
    CARD --> CHECK
    DEVICE --> PAY
    PAYMENT --> PAY
    PRODUCT --> PAY

    VERIFY --> CHECK
    CHECK --> PAY
    PAY --> RECORD
    RECORD --> NOTIFY

    RECORD --> TRANSACTION
    PAY --> BALANCE
    RECORD --> REPORT
    PAY --> ALERT
    VERIFY --> LOG
```

## 2. 消费管理核心业务流程

### 2.1 消费支付流程
```mermaid
flowchart TD
    START([开始]) --> SWIPE[刷卡/刷脸/扫码]
    SWIPE --> VERIFY{身份验证}

    VERIFY -->|验证成功| CHECK[检查账户状态]
    VERIFY -->|验证失败| ERROR[身份错误]

    CHECK --> STATUS{账户状态}
    STATUS -->|正常| BALANCE[检查余额]
    STATUS -->|冻结| FROZEN[账户冻结]
    STATUS -->|注销| INVALID[账户无效]

    BALANCE --> SUFFICIENT{余额是否充足}
    SUFFICIENT -->|充足| PAYMENT[执行支付]
    SUFFICIENT -->|不足| INSUFFICIENT[余额不足]

    PAYMENT --> TRANSACTION[生成交易]
    TRANSACTION --> DEDUCT[扣除余额]
    DEDUCT --> RECORD[记录消费]

    INSUFFICIENT --> PROMPT[余额不足提示]
    FROZEN --> UNFREEZE[联系解冻]
    INVALID --> REREGISTER[重新办卡]

    RECORD --> SUCCESS[支付成功]
    PROMPT --> CANCEL[取消交易]

    SUCCESS --> NOTIFICATION[发送通知]
    CANCEL --> LOG[记录日志]
    UNFREEZE --> LOG
    REREGISTER --> LOG

    NOTIFICATION --> COMPLETE[交易完成]
    ERROR --> LOG
    LOG --> END([结束])
    COMPLETE --> END

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style VERIFY fill:#fff3e0
    style STATUS fill:#fff3e0
    style SUFFICIENT fill:#fff3e0
    style PAYMENT fill:#e8f5e8
    style SUCCESS fill:#e8f5e8
    style ERROR fill:#ffebee
```

### 2.2 账户管理流程
```mermaid
flowchart TD
    START([开始]) --> APPLY[账户申请]
    APPLY --> INFO[填写申请信息]
    INFO --> IDENTITY[身份验证]

    IDENTITY --> VERIFICATION{验证结果}
    VERIFICATION -->|验证通过| APPROVE[审批通过]
    VERIFICATION -->|验证失败| REJECT[申请被拒]

    APPROVE --> CREATE[创建账户]
    REJECT --> NOTIFY[通知申请人]

    CREATE --> CONFIG[配置账户参数]
    CONFIG --> ISSU[发放卡片]
    ISSU --> ACTIVATE[激活账户]

    ACTIVATE --> MANAGE[账户管理]
    MANAGE --> FREEZE{是否需要冻结}

    FREEZE -->|需要冻结| FREEZE_ACCOUNT[冻结账户]
    FREEZE -->|不需要| NORMAL[正常使用]

    FREEZE_ACCOUNT --> REASON[冻结原因记录]
    NORMAL --> MONITOR[使用监控]

    REASON --> LOG[记录日志]
    MONITOR --> UPDATE[更新状态]

    LOG --> END([结束])
    UPDATE --> END
    NOTIFY --> END

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style VERIFICATION fill:#fff3e0
    style CREATE fill:#e8f5e8
    style FREEZE fill:#fff3e0
    style NORMAL fill:#e8f5e8
```

### 2.3 充值管理流程
```mermaid
flowchart TD
    START([开始]) --> REQUEST[充值申请]
    REQUEST --> AMOUNT[填写充值金额]
    AMOUNT --> PAYMENT[选择支付方式]

    PAYMENT --> METHOD{支付方式}
    METHOD -->|现金| CASH[现金充值]
    METHOD -->|银行卡| BANK[银行卡充值]
    METHOD -->|微信| WECHAT[微信充值]
    METHOD -->|支付宝| ALIPAY[支付宝充值]

    CASH --> CASH_RECEIVE[收取现金]
    BANK --> CARD_PAY[刷卡支付]
    WECHAT --> WECHAT_PAY[微信支付]
    ALIPAY --> ALIPAY_PAY[支付宝支付]

    CASH_RECEIVE --> CASH_CONFIRM[现金确认]
    CARD_PAY --> ONLINE_VERIFY[在线验证]
    WECHAT_PAY --> ONLINE_VERIFY
    ALIPAY_PAY --> ONLINE_VERIFY

    ONLINE_VERIFY --> SUCCESS{支付验证}
    SUCCESS -->|成功| UPDATE[更新余额]
    SUCCESS -->|失败| RETRY[重试支付]

    CASH_CONFIRM --> UPDATE
    RETRY --> METHOD

    UPDATE --> RECORD[记录充值]
    RECORD --> RECEIPT[生成收据]
    RECEIPT --> COMPLETE[充值完成]

    style START fill:#e1f5fe
    style COMPLETE fill:#e8f5e8
    style METHOD fill:#fff3e0
    style SUCCESS fill:#fff3e0
    style UPDATE fill:#e8f5e8
    style ONLINE_VERIFY fill:#f3e5f5
```

### 2.4 退款管理流程
```mermaid
flowchart TD
    START([开始]) --> REFUND[退款申请]
    REFUND --> REASON[退款原因]
    REASON --> AMOUNT[退款金额]

    AMOUNT --> CHECK{金额检查}
    CHECK -->|合理| VERIFY[验证申请资格]
    CHECK -->|异常| MANUAL[人工审核]

    VERIFY --> ELIGIBLE{资格验证}
    ELIGIBLE -->|符合| APPROVE[审批通过]
    ELIGIBLE -->|不符合| REJECT[拒绝退款]

    APPROVE --> PROCESS[处理退款]
    REJECT --> NOTIFY[通知拒绝]
    MANUAL --> REVIEW[人工审核]

    REVIEW --> DECIDE{审核决定}
    DECIDE -->|通过| PROCESS
    DECIDE -->|拒绝| REJECT

    PROCESS --> REFUND_AMOUNT[退款处理]
    REFUND_AMOUNT --> RETURN[资金退回]

    RETURN --> RECORD[记录退款]
    RECORD --> COMPLETE[退款完成]

    NOTIFY --> END([结束])
    COMPLETE --> END

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style CHECK fill:#fff3e0
    style ELIGIBLE fill:#fff3e0
    style DECIDE fill:#fff3e0
    style PROCESS fill:#e8f5e8
    style COMPLETE fill:#e8f5e8
```

### 2.5 余额管理流程
```mermaid
flowchart TD
    START([开始]) --> BALANCE[余额查询]
    BALANCE --> DISPLAY[显示余额]

    DISPLAY --> OPERATION{操作选择}
    OPERATION -->|查询| QUERY[详细查询]
    OPERATION -->|转账| TRANSFER[余额转账]
    OPERATION -->|冻结| FREEZE[余额冻结]
    OPERATION -->|解冻| UNFREEZE[余额解冻]

    QUERY --> HISTORY[消费历史]
    TRANSFER --> TRANSFER_VERIFY[转账验证]
    FREEZE --> FREEZE_REASON[冻结原因]
    UNFREEZE --> UNFREEZE_REASON[解冻原因]

    TRANSFER_VERIFY --> VALID{转账验证}
    VALID -->|通过| TRANSFER_PROCESS[转账处理]
    VALID -->|失败| TRANSFER_ERROR[转账错误]

    FREEZE_REASON --> FREEZE_ACCOUNT[冻结账户]
    UNFREEZE_REASON --> UNFREEZE_ACCOUNT[解冻账户]

    TRANSFER_PROCESS --> TRANSFER_SUCCESS[转账成功]
    TRANSFER_ERROR --> TRANSFER_CANCEL[取消转账]
    FREEZE_ACCOUNT --> FREEZE_LOG[冻结日志]
    UNFREEZE_ACCOUNT --> UNFREEZE_LOG[解冻日志]

    TRANSFER_SUCCESS --> NOTIFICATION[转账通知]
    TRANSFER_CANCEL --> LOG[操作日志]
    FREEZE_LOG --> LOG
    UNFREEZE_LOG --> LOG

    NOTIFICATION --> END([结束])
    HISTORY --> END
    LOG --> END

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style OPERATION fill:#fff3e0
    style VALID fill:#fff3e0
    style TRANSFER_PROCESS fill:#e8f5e8
    style TRANSFER_SUCCESS fill:#e8f5e8
```

## 3. 消费管理智能功能流程

### 3.1 智能推荐流程
```mermaid
flowchart TD
    START([开始]) --> USER_ANALYZE[用户行为分析]
    USER_ANALYZE --> HISTORY[消费历史]
    USER_ANALYZE --> PREFERENCE[偏好分析]

    HISTORY --> PATTERN[消费模式识别]
    PREFERENCE --> RECOMMEND[推荐商品]

    PATTERN --> AI{AI智能推荐}
    RECOMMEND --> AI

    AI --> |个性化推荐| PERSONALIZE[个性化商品]
    AI --> |趋势推荐| TREND[趋势商品]
    AI --> |相似推荐| SIMILAR[相似商品]

    PERSONALIZE --> SCORE[推荐评分]
    TREND --> SCORE
    SIMILAR --> SCORE

    SCORE --> RANK[推荐排序]
    RANK --> DISPLAY[显示推荐]

    DISPLAY --> FEEDBACK{用户反馈}
    FEEDBACK -->|满意| LEARN[学习优化]
    FEEDBACK -->|不满意| ADJUST[调整策略]

    LEARN --> MODEL[模型更新]
    ADJUST --> AI

    MODEL --> COMPLETE[推荐完成]
    COMPLETE --> END([结束])

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style AI fill:#fff3e0
    style FEEDBACK fill:#fff3e0
    style SCORE fill:#fff8e1
    style LEARN fill:#f3e5f5
```

### 3.2 风险监控流程
```mermaid
flowchart TD
    START([开始]) --> MONITOR[实时监控]
    MONITOR --> COLLECT[收集数据]

    COLLECT --> AMOUNT[金额监控]
    COLLECT --> FREQUENCY[频率监控]
    COLLECT --> LOCATION[位置监控]
    COLLECT --> TIME[时间监控]

    AMOUNT --> ABNORMAL{金额异常}
    FREQUENCY --> ABNORMAL
    LOCATION --> ABNORMAL
    TIME --> ABNORMAL

    ABNORMAL -->|正常| CONTINUE[继续监控]
    ABNORMAL -->|异常| ANALYZE[异常分析]

    ANALYZE --> RISK{风险评估}
    RISK -->|低风险| WARNING[风险警告]
    RISK -->|中风险| ALERT[风险告警]
    RISK -->|高风险| BLOCK[风险阻断]

    WARNING --> LOG[记录日志]
    ALERT --> NOTIFICATION[通知相关人员]
    BLOCK --> PREVENT[阻断交易]

    PREVENT --> VERIFY[人工验证]
    NOTIFICATION --> ACTION[处理措施]
    LOG --> CONTINUE

    VERIFY --> DECISION{处理决定}
    ACTION --> DECISION

    DECISION -->|允许| ALLOW[允许交易]
    DECISION -->|拒绝| DENY[拒绝交易]

    ALLOW --> MONITOR
    DENY --> LOG

    CONTINUE --> END([结束])
    LOG --> END

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style ABNORMAL fill:#fff3e0
    style RISK fill:#ffebee
    style DECISION fill:#fff3e0
    style BLOCK fill:#ff5252
```

### 3.3 离线消费流程
```mermaid
flowchart TD
    START([开始]) --> OFFLINE[检测网络状态]
    OFFLINE --> NETWORK{网络连接}

    NETWORK -->|在线| ONLINE[在线消费]
    NETWORK -->|离线| OFFLINE_MODE[离线模式]

    OFFLINE_MODE --> CACHE[读取缓存数据]
    CACHE --> VALIDATE{数据验证}

    VALIDATE -->|数据有效| LOCAL_CONSUME[本地消费]
    VALIDATE -->|数据无效| ERROR[错误处理]

    LOCAL_CONSUME --> RECORD[本地记录]
    RECORD --> QUEUE[加入队列]

    QUEUE --> SYNC_QUEUE[同步队列管理]
    SYNC_QUEUE --> STORE[本地存储]

    ONLINE --> SYNC_CHECK{同步检查}
    SYNC_CHECK -->|有离线数据| SYNC[同步数据]
    SYNC_CHECK -->|无离线数据| DIRECT[直接消费]

    SYNC --> UPLOAD[上传离线数据]
    DIRECT --> NORMAL[正常消费]

    UPLOAD --> UPDATE[更新余额]
    UPDATE --> CLEAN[清理缓存]

    ERROR --> RECOVERY[数据恢复]
    RECOVERY --> LOCAL_CONSUME

    NORMAL --> COMPLETE[消费完成]
    CLEAN --> COMPLETE

    COMPLETE --> END([结束])

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style NETWORK fill:#fff3e0
    style VALIDATE fill:#fff3e0
    style SYNC_CHECK fill:#fff8e1
    style UPDATE fill:#e8f5e8
    style ERROR fill:#ffebee
```

## 4. 消费管理集成流程

### 4.1 门禁系统集成流程
```mermaid
flowchart TD
    START([开始]) --> ACCESS[门禁访问]
    ACCESS --> USER[用户识别]
    USER --> CONSUME[消费验证]

    CONSUME --> TYPE{消费类型}
    TYPE -->|餐费| MEAL[餐费消费]
    TYPE -->|购物| SHOPPING[购物消费]
    TYPE -->|服务| SERVICE[服务消费]

    MEAL --> CAFETERIA[餐厅消费]
    SHOPPING --> STORE[商店消费]
    SERVICE --> FACILITY[设施服务]

    CAFETERIA --> PAY[执行支付]
    STORE --> PAY
    FACILITY --> PAY

    PAY --> SUCCESS{支付结果}
    SUCCESS -->|成功| ACCESS_ALLOW[允许通行]
    SUCCESS -->|失败| ACCESS_DENY[拒绝通行]

    ACCESS_ALLOW --> GATE[开启门禁]
    ACCESS_DENY --> LOCK[锁定门禁]

    GATE --> RECORD[记录通行]
    LOCK --> LOG[记录日志]

    RECORD --> END([结束])
    LOG --> END

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    type --> PAY
    style SUCCESS fill:#fff3e0
    style ACCESS_ALLOW fill:#e8f5e8
    style ACCESS_DENY fill:#ffebee
```

### 4.2 OA系统集成流程
```mermaid
flowchart TD
    START([开始]) --> OA[OA系统]
    OA --> REQUEST[消费请求]
    REQUEST --> APPROVE{审批需求}

    APPROVE -->|需要审批| WORKFLOW[审批流程]
    APPROVE -->|直接消费| DIRECT[直接消费]

    WORKFLOW --> SUBMIT[提交审批]
    SUBMIT --> MANAGER[经理审批]
    MANAGER --> APPROVAL{审批结果}

    APPROVAL -->|通过| CONSUME_ALLOW[允许消费]
    APPROVAL -->|拒绝| CONSUME_DENY[拒绝消费]

    DIRECT --> CONSUME
    CONSUME_ALLOW --> CONSUME

    CONSUME --> FINANCE[财务系统]
    CONSUME_DENY --> NOTIFY[通知拒绝]

    FINANCE --> ACCOUNTING[财务记账]
    ACCOUNTING --> RECONCILIATION[对账处理]

    RECONCILIATION --> COMPLETE[处理完成]
    NOTIFY --> END([结束])
    COMPLETE --> END

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style APPROVE fill:#fff3e0
    style APPROVAL fill:#fff3e0
    style CONSUME_ALLOW fill:#e8f5e8
    style FINANCE fill:#f3e5f5
```

## 5. 消费管理数据分析流程

### 5.1 消费统计分析流程
```mermaid
flowchart TD
    START([开始]) --> COLLECT[收集消费数据]
    COLLECT --> PROCESS[数据处理]

    PROCESS --> CATEGORIZE[数据分类]
    CATEGORIZE --> TIME[时间维度]
    CATEGORIZE --> USER[用户维度]
    CATEGORIZE --> PRODUCT[商品维度]
    CATEGORIZE --> LOCATION[位置维度]

    TIME --> TREND[趋势分析]
    USER --> BEHAVIOR[行为分析]
    PRODUCT --> SALES[销售分析]
    LOCATION --> AREA[区域分析]

    TREND --> PREDICT[预测分析]
    BEHAVIOR --> PROFILE[用户画像]
    SALES --> OPTIMIZE[商品优化]
    AREA --> LAYOUT[布局优化]

    PREDICT --> FORECAST[销售预测]
    PROFILE --> RECOMMEND[精准推荐]
    OPTIMIZE --> STRATEGY[营销策略]
    LAYOUT --> PLAN[布局计划]

    FORECAST --> DASHBOARD[生成仪表板]
    RECOMMEND --> DASHBOARD
    STRATEGY --> DASHBOARD
    PLAN --> DASHBOARD

    DASHBOARD --> REPORT[生成报表]
    REPORT --> END([结束])

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style CATEGORIZE fill:#f3e5f5
    style PREDICT fill:#fff8e1
    style DASHBOARD fill:#e8f5e8
```

### 5.2 财务对账流程
```mermaid
flowchart TD
    START([开始]) --> TRANSACTION[交易数据]
    TRANSACTION --> FINANCE[财务数据]

    FINANCE --> MATCH{数据匹配}
    MATCH -->|匹配成功| RECONCILE[对账处理]
    MATCH -->|不匹配| DIFFERENCE[差异处理]

    RECONCILE --> BALANCE[余额核对]
    DIFFERENCE --> INVESTIGATE[差异调查]

    BALANCE --> CHECK{余额检查}
    INVESTIGATE --> RESOLVE[差异解决]

    CHECK -->|平衡| COMPLETE[对账完成]
    CHECK -->|不平衡| ADJUST[余额调整]

    RESOLVE --> BALANCE
    ADJUST --> CONFIRM[调整确认]

    CONFIRM --> AUDIT[审计验证]
    COMPLETE --> REPORT[生成报告]
    AUDIT --> REPORT

    REPORT --> END([结束])

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style MATCH fill:#fff3e0
    style CHECK fill:#fff3e0
    style INVESTIGATE fill:#fff8e1
    style COMPLETE fill:#e8f5e8
    style DIFFERENCE fill:#ffebee
```

## 6. 消费管理实施状态分析

### 6.1 功能实现现状
| 功能模块 | 实现状态 | 完成度 | 关键特性 |
|---------|---------|--------|----------|
| **支付管理** | 部分实现 | 70% | 基础支付、余额检查 |
| **账户管理** | 部分实现 | 80% | 账户创建、状态管理 |
| **充值管理** | 部分实现 | 75% | 多种充值方式 |
| **消费记录** | 已实现 | 90% | 完整记录查询 |
| **退款管理** | 部分实现 | 60% | 基础退款流程 |
| **余额管理** | 部分实现 | 65% | 余额查询、冻结 |
| **风险监控** | 未实现 | 30% | 异常检测、风险控制 |
| **智能推荐** | 未实现 | 20% | 个性化推荐 |
| **离线消费** | 未实现 | 25% | 离线支持、数据同步 |

### 6.2 数据库表结构需求
```sql
-- 消费交易表
CREATE TABLE IF NOT EXISTS `t_consume_transaction` (
    `transaction_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '交易ID',
    `transaction_no` VARCHAR(50) NOT NULL COMMENT '交易编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `account_id` BIGINT NOT NULL COMMENT '账户ID',
    `card_id` VARCHAR(50) COMMENT '卡片ID',
    `device_id` BIGINT COMMENT '设备ID',
    `device_type` TINYINT DEFAULT 1 COMMENT '设备类型：1-消费机 2-POS机 3-移动端',
    `transaction_type` TINYINT NOT NULL COMMENT '交易类型：1-消费 2-充值 3-退款 4-转账',
    `payment_method` TINYINT NOT NULL COMMENT '支付方式：1-现金 2-银行卡 3-微信 4-支付宝 5-余额',
    `original_amount` DECIMAL(12,2) NOT NULL COMMENT '原始金额',
    `discount_amount` DECIMAL(12,2) DEFAULT 0 COMMENT '优惠金额',
    `actual_amount` DECIMAL(12,2) NOT NULL COMMENT '实际金额',
    `before_balance` DECIMAL(12,2) NOT NULL COMMENT '交易前余额',
    `after_balance` DECIMAL(12,2) NOT NULL COMMENT '交易后余额',
    `transaction_status` TINYINT DEFAULT 1 COMMENT '交易状态：1-处理中 2-成功 3-失败 4-已取消',
    `failure_reason` VARCHAR(500) COMMENT '失败原因',
    `merchant_id` BIGINT COMMENT '商户ID',
    `merchant_name` VARCHAR(200) COMMENT '商户名称',
    `category_id` BIGINT COMMENT '商品分类ID',
    `category_name` VARCHAR(100) COMMENT '商品分类名称',
    `product_id` VARCHAR(100) COMMENT '商品ID',
    `product_name` VARCHAR(200) COMMENT '商品名称',
    `quantity` INT DEFAULT 1 COMMENT '数量',
    `unit_price` DECIMAL(10,2) COMMENT '单价',
    `location_id` BIGINT COMMENT '位置ID',
    `location_name` VARCHAR(200) COMMENT '位置名称',
    `consumer_ip` VARCHAR(45) COMMENT '消费IP',
    `risk_level` TINYINT DEFAULT 1 COMMENT '风险等级：1-低 2-中 3-高',
    `offline_flag` TINYINT DEFAULT 0 COMMENT '离线标志：0-在线 1-离线',
    `sync_status` TINYINT DEFAULT 0 COMMENT '同步状态：0-未同步 1-已同步 2-同步失败',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标识',
    PRIMARY KEY (`transaction_id`),
    UNIQUE KEY `uk_transaction_no` (`transaction_no`, `deleted_flag`),
    KEY `idx_user_transaction` (`user_id`, `transaction_time`, `deleted_flag`),
    KEY `idx_account_transaction` (`account_id`, `transaction_time`, `deleted_flag`),
    KEY `idx_card_transaction` (`card_id`, `transaction_time`, `deleted_flag`),
    KEY `idx_device_transaction` (`device_id`, `transaction_time`, `deleted_flag`),
    KEY `idx_transaction_type` (`transaction_type`, `transaction_status`, `deleted_flag`),
    KEY `idx_payment_method` (`payment_method`, `deleted_flag`),
    KEY `idx_create_time` (`create_time`, `deleted_flag`),
    KEY `idx_merchant` (`merchant_id`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费交易表';

-- 账户信息表
CREATE TABLE IF NOT EXISTS `t_consume_account` (
    `account_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '账户ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `account_no` VARCHAR(50) NOT NULL COMMENT '账户编号',
    `account_type` TINYINT DEFAULT 1 COMMENT '账户类型：1-个人 2-团体 3-临时',
    `account_status` TINYINT DEFAULT 1 COMMENT '账户状态：1-正常 2-冻结 3-注销 4-挂失',
    `current_balance` DECIMAL(12,2) DEFAULT 0 COMMENT '当前余额',
    `available_balance` DECIMAL(12,2) DEFAULT 0 COMMENT '可用余额',
    `frozen_amount` DECIMAL(12,2) DEFAULT 0 COMMENT '冻结金额',
    `credit_limit` DECIMAL(12,2) DEFAULT 0 COMMENT '信用额度',
    `monthly_limit` DECIMAL(12,2) DEFAULT 0 COMMENT '月度限额',
    `daily_limit` DECIMAL(12,2) DEFAULT 0 COMMENT '日度限额',
    `single_limit` DECIMAL(12,2) DEFAULT 0 COMMENT '单笔限额',
    `card_count` INT DEFAULT 0 COMMENT '卡片数量',
    `primary_card_id` VARCHAR(50) COMMENT '主卡片ID',
    `enable_online` TINYINT DEFAULT 1 COMMENT '启用在线支付：0-禁用 1-启用',
    `enable_offline` TINYINT DEFAULT 1 COMMENT '启用离线支付：0-禁用 1-启用',
    `last_consume_time` DATETIME COMMENT '最后消费时间',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `password_salt` VARCHAR(100) COMMENT '密码盐值',
    `security_question` VARCHAR(200) COMMENT '安全问题',
    `security_answer` VARCHAR(200) COMMENT '安全问题答案',
    `remark` VARCHAR(1000) COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标识',
    PRIMARY KEY (`account_id`),
    UNIQUE KEY `uk_account_no` (`account_no`, `deleted_flag`),
    UNIQUE KEY `uk_user_account` (`user_id`, `deleted_flag`),
    KEY `idx_account_status` (`account_status`, `deleted_flag`),
    KEY `idx_card_id` (`primary_card_id`, `deleted_flag`),
    KEY `idx_create_time` (`create_time`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账户信息表';
```

### 6.3 关键技术实现要求

#### 6.3.1 支付管理引擎
```java
// 支付管理引擎接口
public interface ConsumePaymentEngine {

    /**
     * 执行支付
     */
    @CircuitBreaker(name = "consumePayment")
    CompletableFuture<ResponseDTO<PaymentResult>> executePayment(
            PaymentRequest request
    );

    /**
     * 处理退款
     */
    @CircuitBreaker(name = "consumeRefund")
    CompletableFuture<ResponseDTO<RefundResult>> processRefund(
            RefundRequest request
    );

    /**
     * 风险检测
     */
    CompletableFuture<ResponseDTO<RiskAssessment>> assessRisk(
            PaymentRequest request
    );
}
```

#### 6.3.2 账户管理服务
```java
// 账户管理服务接口
public interface ConsumeAccountService {

    /**
     * 创建账户
     */
    @CircuitBreaker(name = "consumeAccount")
    CompletableFuture<ResponseDTO<AccountResult>> createAccount(
            AccountCreateForm form
    );

    /**
     * 余额操作
     */
    CompletableFuture<ResponseDTO<BalanceUpdateResult>> updateBalance(
            Long accountId,
            BigDecimal amount,
            BalanceOperation operation
    );

    /**
     * 账户冻结/解冻
     */
    CompletableFuture<ResponseDTO<Void>> freezeAccount(
            Long accountId,
            FreezeType type,
            String reason
    );
}
```

## 7. 消费管理优化建议

### 7.1 性能优化
- **支付优化**: 支持异步支付、批量处理、预扣费机制
- **缓存策略**: Redis缓存用户余额、商户信息、商品信息
- **数据库优化**: 分区表设计、索引优化、读写分离
- **并发控制**: 分布式锁、乐观锁、幂等性保证

### 7.2 安全增强
- **交易安全**: 交易签名、防重放攻击、交易限额
- **数据加密**: 敏感信息加密存储和传输
- **审计追踪**: 完整的交易审计链和操作日志
- **风险控制**: 实时风控、异常检测、自动阻断

### 7.3 用户体验优化
- **离线支持**: 完善的离线消费和数据同步机制
- **智能推荐**: 个性化商品推荐和营销策略
- **多端支持**: Web端、移动端、POS机多端统一
- **实时通知**: 支付成功、余额变动、异常告警实时通知

---

**文档版本**: v1.0.0
**创建时间**: 2025-12-16
**维护团队**: IOE-DREAM消费管理团队
**下次更新**: 根据实际实施进度定期更新