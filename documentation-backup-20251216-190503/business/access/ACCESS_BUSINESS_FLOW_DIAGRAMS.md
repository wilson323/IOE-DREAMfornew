# IOE-DREAM 智能门禁管理系统业务流程图

## 1. 门禁管理模块业务流程概览

### 1.1 门禁管理业务架构
```mermaid
graph TB
    subgraph "门禁管理体系"
        AM[门禁管理中心]
        CM[控制管理]
        ANTI[反潜回]
        RM[记录管理]
        DM[设备管理]
        AUTH[权限验证]
        ALARM[告警管理]
    end

    subgraph "关联系统"
        USER[用户系统]
        VIDEO[视频监控]
        VISITOR[访客系统]
        SECURITY[安保系统]
        OA[OA系统]
        NOTIFICATION[通知系统]
    end

    AM --> CM
    AM --> ANTI
    AM --> RM
    AM --> DM
    AM --> AUTH
    AM --> ALARM

    CM --> DEVICE[门禁设备]
    ANTI --> USER
    RM --> VIDEO
    AUTH --> VISITOR
    ALARM --> SECURITY
    CM --> OA
    ALARM --> NOTIFICATION
```

### 1.2 门禁管理数据流程
```mermaid
flowchart LR
    subgraph "数据源"
        USER[用户信息]
        CARD[卡片信息]
        DEVICE[设备信息]
        PERMISSION[权限数据]
        BIOMETRIC[生物特征]
        TIME[时间规则]
    end

    subgraph "门禁处理"
        VERIFY[身份验证]
        CHECK[权限检查]
        CONTROL[门禁控制]
        RECORD[记录通行]
        ANTI[反潜回检查]
    end

    subgraph "数据输出"
        ACCESS_LOG[通行记录]
        ALERT[告警信息]
        VIDEO_RECORD[视频联动]
        STATISTICS[统计数据]
        NOTIFICATION[通知消息]
    end

    USER --> VERIFY
    CARD --> CHECK
    DEVICE --> CONTROL
    PERMISSION --> CHECK
    BIOMETRIC --> VERIFY
    TIME --> CHECK

    VERIFY --> CHECK
    CHECK --> ANTI
    ANTI --> CONTROL
    CONTROL --> RECORD

    RECORD --> ACCESS_LOG
    ANTI --> ALERT
    RECORD --> VIDEO_RECORD
    CONTROL --> STATISTICS
    VERIFY --> NOTIFICATION
```

## 2. 门禁管理核心业务流程

### 2.1 门禁控制流程
```mermaid
flowchart TD
    START([开始]) --> REQUEST[门禁请求]
    REQUEST --> SCAN[刷卡/刷脸/密码]

    SCAN --> VERIFY{身份验证}
    VERIFY -->|验证成功| PERMISSION[权限检查]
    VERIFY -->|验证失败| DENY[拒绝通行]

    PERMISSION --> CHECK{权限检查}
    CHECK -->|有权限| TIME_CHECK[时间验证]
    CHECK -->|无权限| UNAUTHORIZED[无权访问]

    TIME_CHECK --> TIME_VALID{时间有效性}
    TIME_VALID -->|有效| ANTI_PASSBACK[反潜回检查]
    TIME_VALID -->|无效| TIME_RESTRICT[时间限制]

    ANTI_PASSBACK --> ANTI_CHECK{反潜回检查}
    TIME_RESTRICT --> TIME_DENY[时间拒绝]

    ANTI_CHECK -->|通过| OPEN[开门]
    ANTI_CHECK -->|拦截| ANTI_DENY[反潜回拦截]

    OPEN --> SUCCESS[通行成功]
    ANTI_DENY --> RECORD[记录违规]
    UNAUTHORIZED --> RECORD

    SUCCESS --> LOG[记录日志]
    RECORD --> LOG
    TIME_DENY --> LOG

    LOG --> NOTIFICATION[发送通知]
    DENY --> NOTIFICATION

    NOTIFICATION --> COMPLETE[处理完成]
    COMPLETE --> END([结束])

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style VERIFY fill:#fff3e0
    style CHECK fill:#fff3e0
    style TIME_CHECK fill:#fff3e0
    style ANTI_PASSBACK fill:#fff8e1
    style OPEN fill:#e8f5e8
    style SUCCESS fill:#e8f5e8
    style DENY fill:#ffebee
```

### 2.2 反潜回算法流程
```mermaid
flowchart TD
    START([开始]) --> TYPE{反潜回类型}
    TYPE -->|硬反潜回| HARD[硬反潜回模式]
    TYPE -->|软反潜回| SOFT[软反潜回模式]
    TYPE -->|区域反潜回| AREA[区域反潜回模式]
    TYPE -->|全局反潜回| GLOBAL[全局反潜回模式]

    HARD --> HARD_RULE[硬规则检查]
    SOFT --> SOFT_RULE[软规则检查]
    AREA --> AREA_RULE[区域规则检查]
    GLOBAL --> GLOBAL_RULE[全局规则检查]

    HARD_RULE --> MEMORY[进入记忆]
    SOFT_RULE --> WARNING[警告记录]
    AREA_RULE --> PAIR_CHECK[配对检查]
    GLOBAL_RULE --> GLOBAL_CHECK[全局检查]

    MEMORY --> EXIT_CHECK[离开检查]
    WARNING --> EXIT_CHECK
    PAIR_CHECK --> EXIT_CHECK
    GLOBAL_CHECK --> EXIT_CHECK

    EXIT_CHECK --> EXIT_VALID{离开验证}
    EXIT_VALID -->|正常| CLEAR[清理记录]
    EXIT_VALID -->|异常| VIOLATION[违规检测]

    CLEAR --> SUCCESS[检查通过]
    VIOLATION --> RECORD[记录违规]

    SUCCESS --> END([结束])
    RECORD --> END

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style TYPE fill:#fff3e0
    style MEMORY fill:#fff8e1
    style WARNING fill:#fff8e1
    style PAIR_CHECK fill:#fff8e1
    style EXIT_VALID fill:#fff3e0
    style VIOLATION fill:#ffebee
```

### 2.3 通行记录管理流程
```mermaid
flowchart TD
    START([开始]) --> ACCESS[门禁通行]
    ACCESS --> CAPTURE[捕获通行数据]

    CAPTURE --> USER_INFO[用户信息]
    CAPTURE --> DEVICE_INFO[设备信息]
    CAPTURE --> ACCESS_INFO[通行信息]
    CAPTURE --> BIOMETRIC[生物信息]

    USER_INFO --> ENCRYPT[数据加密]
    DEVICE_INFO --> ENCRYPT
    ACCESS_INFO --> ENCRYPT
    BIOMETRIC --> ENCRYPT

    ENCRYPT --> VALIDATE[数据验证]
    VALIDATE --> STORE[数据存储]

    STORE --> INDEX[建立索引]
    INDEX --> CLASSIFY[数据分类]

    CLASSIFY --> REAL_TIME[实时分析]
    CLASSIFY --> HISTORY[历史归档]
    CLASSIFY --> STATISTICS[统计分析]

    REAL_TIME --> MONITOR[实时监控]
    HISTORY --> ARCHIVE[历史查询]
    STATISTICS --> REPORT[生成报告]

    MONITOR --> ALERT{异常检测}
    ARCHIVE --> QUERY[数据查询]
    REPORT --> DISPLAY[报告展示]

    ALERT --> NOTIFY[发送告警]
    QUERY --> END([结束])
    DISPLAY --> END

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style CAPTURE fill:#f3e5f5
    style ENCRYPT fill:#fff8e1
    style VALIDATE fill:#fff3e0
    style CLASSIFY fill:#f3e5f5
    style ALERT fill:#ffebee
```

### 2.4 设备管理流程
```mermaid
flowchart TD
    START([开始]) --> DEVICE[门禁设备]
    DEVICE --> SCAN[扫描设备]
    SCAN --> IDENTIFY{设备识别}

    IDENTIFY -->|已知设备| CONFIG[设备配置]
    IDENTIFY -->|未知设备| REGISTER[设备注册]

    REGISTER --> INFO[设备信息]
    INFO --> APPROVE[设备审批]
    APPROVE --> CONFIG

    CONFIG --> PARAMETER[参数设置]
    PARAMETER --> CONNECT[连接设备]
    CONNECT --> ONLINE[设备上线]

    ONLINE --> MONITOR[设备监控]
    MONITOR --> STATUS{设备状态}

    STATUS -->|正常| MAINTAIN[维持连接]
    STATUS -->|异常| DIAGNOSE[故障诊断]

    MAINTAIN --> HEARTBEAT[心跳检测]
    DIAGNOSE --> REPAIR[故障修复]

    HEARTBEAT --> CHECK{健康检查}
    REPAIR --> RECONNECT[重新连接]

    CHECK -->|健康| CONTINUE[继续监控]
    CHECK -->|不健康| MAINTAIN
    RECONNECT --> MONITOR

    CONTINUE --> END([结束])

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style IDENTIFY fill:#fff3e0
    style CONFIG fill:#e8f5e8
    style ONLINE fill:#e8f5e8
    style STATUS fill:#fff3e0
    style MONITOR fill:#f3e5f5
    style DIAGNOSE fill:#fff8e1
```

### 2.5 权限管理流程
```mermaid
flowchart TD
    START([开始]) --> USER[用户请求]
    USER --> AUTH[身份认证]

    AUTH --> SUCCESS{认证结果}
    SUCCESS -->|成功| PERMISSION[权限查询]
    SUCCESS -->|失败| LOGIN_FAIL[登录失败]

    PERMISSION --> USER_PERM[用户权限]
    USER_PERM --> ROLE_PERM[角色权限]
    ROLE_PERM --> AREA_PERM[区域权限]
    AREA_PERM --> TIME_PERM[时间权限]

    TIME_PERM --> COMBINE[权限组合]
    COMBINE --> VALIDATE{权限验证}

    VALIDATE -->|有权限| GRANT[授权通行]
    VALIDATE -->|无权限| DENY[拒绝通行]

    GRANT --> ALLOW[允许访问]
    DENY --> RECORD[记录拒绝]

    ALLOW --> ACCESS[访问控制]
    RECORD --> LOG[记录日志]

    ACCESS --> SUCCESS[授权成功]
    LOG --> END([结束])

    LOGIN_FAIL --> END
    SUCCESS --> END

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style AUTH fill:#fff3e0
    style SUCCESS fill:#fff3e0
    style COMBINE fill:#fff8e1
    style VALIDATE fill:#fff3e0
    style GRANT fill:#e8f5e8
    style DENY fill:#ffebee
```

## 3. 门禁管理智能功能流程

### 3.1 智能异常检测流程
```mermaid
flowchart TD
    START([开始]) --> MONITOR[实时监控]
    MONITOR --> COLLECT[收集数据]

    COLLECT --> PATTERN[行为模式分析]
    PATTERN --> AI{AI异常检测}

    AI -->|正常| CONTINUE[继续监控]
    AI -->|异常| ANALYZE[异常分析]

    ANALYZE --> LEVEL{异常等级}
    LEVEL -->|低风险| LOG[记录日志]
    LEVEL -->|中风险| WARNING[风险警告]
    LEVEL -->|高风险| ALERT[风险告警]

    LOG --> IGNORE[忽略处理]
    WARNING --> INVESTIGATE[调查处理]
    ALERT --> EMERGENCY[紧急处理]

    INVESTIGATE --> VERIFY[人工验证]
    EMERGENCY --> BLOCK[紧急阻断]

    VERIFY --> DECIDE{处理决定}
    BLOCK --> LOCK[锁定设备]

    DECIDE -->|处理| RESOLVE[解决问题]
    DECIDE -->|忽略| IGNORE

    RESOLVE --> MONITOR
    LOCK --> SECURITY[安全处理]

    SECURITY --> VERIFY
    IGNORE --> CONTINUE
    CONTINUE --> END([结束])

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style PATTERN fill:#f3e5f5
    style AI fill:#fff3e0
    style LEVEL fill:#fff3e0
    style INVESTIGATE fill:#fff8e1
    style EMERGENCY fill:#ff5252
    style DECIDE fill:#fff3e0
    style LOCK fill:#ffebee
```

### 3.2 视频联动分析流程
```mermaid
flowchart TD
    START([开始]) --> ACCESS[门禁事件]
    ACCESS --> TRIGGER[触发联动]

    TRIGGER --> VIDEO_CHECK[视频系统检查]
    VIDEO_CHECK --> ONLINE{视频在线}

    ONLINE -->|在线| CAPTURE[捕获视频]
    ONLINE -->|离线| QUEUE[加入队列]

    CAPTURE --> ANALYZE[视频分析]
    QUEUE --> WAIT[等待上线]

    ANALYZE --> FACE[人脸识别]
    ANALYZE --> OBJECT[物体识别]
    ANALYZE --> BEHAVIOR[行为分析]

    FACE --> MATCH[人脸匹配]
    OBJECT --> TRACK[物体追踪]
    BEHAVIOR --> ANOMALY[异常检测]

    MATCH --> CONFIRM[身份确认]
    TRACK --> RECORD[轨迹记录]
    ANOMALY -> RISK[风险评估]

    CONFIRM --> SUCCESS[确认成功]
    RECORD --> SUCCESS
    RISK --> ALARM[触发告警]

    SUCCESS --> INTEGRATE[数据集成]
    WAIT --> CAPTURE
    ALARM -> SECURITY[安全响应]

    INTEGRATE --> COMPLETE[联动完成]
    SECURITY --> COMPLETE

    COMPLETE --> END([结束])
    WAIT --> END

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style TRIGGER fill:#fff3e0
    style ONLINE fill:#fff3e0
    style ANALYZE fill:#f3e5f5
    style MATCH fill:#fff8e1
    style CONFIRM fill:#e8f5e8
    style INTEGRATE fill:#8f5e8
    style ALARM fill:#ffebee
```

### 3.3 智能调度流程
```mermaid
flowchart TD
    START([开始]) --> LOAD[负载监控]
    LOAD --> ANALYZE[负载分析]

    ANALYZE --> OPTIMIZE{优化需求}
    OPTIMIZE -->|需要优化| SCHEDULE[智能调度]
    OPTIMIZE -->|负载均衡| BALANCE[负载均衡]

    SCHEDULE --> PREDICT[预测调度]
    BALANCE --> DISTRIBUTE[负载分配]

    PREDICT --> ALGORITHM[调度算法]
    DISTRIBUTE --> ALGORITHM

    ALGORITHM --> GENETIC[遗传算法]
    ALGORITHM --> GREEDY[贪心算法]
    ALGORITHM --> ANT[蚁群算法]

    GENETIC --> EVOLVE[进化优化]
    GREEDY --> LOCAL[局部优化]
    ANT --> GLOBAL[全局优化]

    EVOLVE --> SELECT[最优方案]
    LOCAL --> SELECT
    GLOBAL --> SELECT

    SELECT --> EXECUTE[执行调度]
    EXECUTE --> MONITOR[监控效果]

    MONITOR --> EVALUATE{效果评估}
    EVALUATE -->|满意| SUCCESS[调度成功]
    EVALUATE -->|不满意| ADJUST[调整参数]

    SUCCESS --> CONTINUE[继续监控]
    ADJUST --> ALGORITHM

    CONTINUE --> END([结束])

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style ANALYZE fill:#f3e5f5
    style OPTIMIZE fill:#fff3e0
    style ALGORITHM fill:#fff8e1
    style EVOLVE fill:#f3e5f5
    style SELECT fill:#fff3e0
    style MONITOR fill:#f3e5f5
    style EVALUATE fill:#fff3e0
```

## 4. 门禁管理集成流程

### 4.1 访客系统集成流程
```mermaid
flowchart TD
    START([开始]) --> VISITOR[访客系统]
    VISITOR --> APPROVAL[访客审批]
    APPROVAL --> PERMISSION[权限授权]

    PERMISSION --> ACCESS[门禁请求]
    ACCESS --> VISITOR_VERIFY[访客验证]

    VISITOR_VERIFY --> SUCCESS{访客验证}
    SUCCESS -->|成功| TEMPORARY[临时权限]
    SUCCESS -->|失败| REJECT[拒绝访问]

    TEMPORARY --> TIME_LIMIT[时间限制]
    TIME_LIMIT --> AREA_LIMIT[区域限制]

    AREA_LIMIT --> DEVICE_LIMIT[设备限制]
    DEVICE_LIMIT --> BIO_VERIFICATION[生物验证]

    BIO_VERIFICATION --> VERIFY{生物验证}
    VERIFY -->|通过| TEMP_ACCESS[临时访问]
    VERIFY -->|失败| ACCESS_DENY[拒绝访问]

    TEMP_ACCESS --> ACCESS_LOG[访问记录]
    ACCESS_DENY --> LOG[记录日志]

    ACCESS_LOG --> VIDEO[视频记录]
    LOG --> END([结束])
    VIDEO --> END

    REJECT --> END

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style APPROVAL fill:#fff3e0
    style VISITOR_VERIFY fill:#fff3e0
    style TEMPORARY fill:#fff8e1
    style BIO_VERIFICATION fill:#fff3e0
    style TEMP_ACCESS fill:#e8f5e8
    style ACCESS_DENY fill:#ffebee
```

### 4.2 OA系统集成流程
```mermaid
flowchart TD
    START([开始]) --> OA[OA系统]
    OA --> EMPLOYEE[员工管理]
    EMPLOYEE --> WORKFLOW[工作流程]

    WORKFLOW --> REQUEST[权限申请]
    REQUEST --> APPROVE[审批流程]

    APPROVE --> FINAL_APPROVE[最终审批]
    FINAL_APPROVE --> PERMISSION[权限配置]

    PERMISSION --> SYNC[同步到门禁]
    SYNC --> UPDATE[更新权限]

    UPDATE --> NOTIFICATION[通知更新]
    NOTIFICATION --> EFFECTIVE[权限生效]

    EFFECTIVE --> ACCESS[门禁使用]
    ACCESS --> RECORD[使用记录]

    RECORD --> FEEDBACK[反馈给OA]
    FEEDBACK --> UPDATE

    UPDATE --> COMPLETE[同步完成]
    COMPLETE --> END([结束])

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style WORKFLOW fill:#fff3e0
    style FINAL_APPROVE fill:#fff8e1
    style PERMISSION fill:#e8f5e8
    style SYNC fill:#f3e5f5
    style UPDATE fill:#fff3e0
```

## 5. 门禁管理数据分析流程

### 5.1 通行统计分析流程
```mermaid
flowchart TD
    START([开始]) --> COLLECT[收集通行数据]
    COLLECT --> PROCESS[数据处理]

    PROCESS --> FILTER[数据过滤]
    FILTER --> CLEAN[数据清洗]

    CLEAN --> CLASSIFY[数据分类]
    CLASSIFY --> TIME[时间维度]
    CLASSIFY --> USER[用户维度]
    CLASSIFY --> DEVICE[设备维度]
    CLASSIFY --> AREA[区域维度]

    TIME --> TREND[趋势分析]
    USER --> BEHAVIOR[行为分析]
    DEVICE --> EFFICIENCY[效率分析]
    AREA --> FLOW[流量分析]

    TREND --> PREDICT[趋势预测]
    BEHAVIOR --> PROFILE[用户画像]
    EFFICIENCY --> OPTIMIZE[设备优化]
    FLOW --> LAYOUT[布局优化]

    PREDICT --> FORECAST[流量预测]
    PROFILE --> PERSONALIZE[个性化服务]
    OPTIMIZE --> SCHEDULE[调度计划]
    LAYOUT --> PLAN[布局计划]

    FORECAST --> DASHBOARD[生成仪表板]
    PERSONALIZE --> DASHBOARD
    SCHEDULE --> DASHBOARD
    PLAN --> DASHBOARD

    DASHBOARD --> REPORT[生成报告]
    REPORT --> END([结束])

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style PROCESS fill:#f3e5f5
    style CLASSIFY fill:#fff3e0
    style TREND fill:#fff8e1
    style PREDICT fill:#fff3e0
    style DASHBOARD fill:#e8f5e8
```

### 5.2 安全审计流程
```mermaid
flowchart TD
    START([开始]) --> AUDIT[审计范围]
    AUDIT --> PERIOD[审计周期]
    PERIOD --> SCOPE[审计范围]

    SCOPE --> COLLECT[收集审计数据]
    COLLECT --> RECORD[记录数据]
    COLLECT --> LOG[日志数据]

    RECORD --> CHECK[合规检查]
    LOG --> CHECK
    RECORD --> CHECK

    CHECK --> VIOLATION{违规检查}
    VIOLATION -->|无违规| COMPLIANT[合规处理]
    VIOLATION -->|有违规| VIOLATION_HANDLE[违规处理]

    COMPLIANT --> REPORT[生成合规报告]
    VIOLATION --> INVESTIGATE[违规调查]

    INVESTIGATE --> ROOT_CAUSE[根因分析]
    ROOT_CAUSE --> CORRECTIVE[纠正措施]

    CORRECTIVE --> IMPLEMENT[实施措施]
    REPORT --> REVIEW[报告审核]

    IMPLEMENT --> MONITOR[监控效果]
    REVIEW --> PUBLISH[发布结果]

    MONITOR --> EVALUATE[效果评估]
    PUBLISH --> END([结束])

    EVALUATE --> PUBLISH

    style START fill:#e1f5fe
    style END fill:#e8f5e8
    style AUDIT fill:#f3e5f5
    scope fill:#fff3e0
    style CHECK fill:#fff3e0
    style VIOLATION fill:#ffebee
    style INVESTIGATE fill:#fff8e1
    style CORRECTIVE fill:#f3e5f5
    style IMPLEMENT fill:#8f5e8
```

## 6. 门禁管理实施状态分析

### 6.1 功能实现现状
| 功能模块 | 实现状态 | 完成度 | 关键特性 |
|---------|---------|--------|----------|
| **门禁控制** | 部分实现 | 70% | 基础控制、权限检查 |
| **反潜回算法** | 未实现 | 25% | 基础概念、无完整实现 |
| **通行记录** | 部分实现 | 80% | 基础记录、查询功能 |
| **设备管理** | 部分实现 | 65% | 设备连接、状态监控 |
| **权限管理** | 部分实现 | 60% | 基础权限、角色管理 |
| **智能检测** | 未实现 | 30% | 异常检测、AI分析 |
| **视频联动** | 规划中 | 40% | 视频集成、人脸识别 |
| **智能调度** | 未实现 | 20% | 负载均衡、AI优化 |

### 6.2 数据库表结构需求
```sql
-- 门禁通行记录表
CREATE TABLE IF NOT EXISTS `t_access_record` (
    `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `card_id` VARCHAR(50) COMMENT '卡片ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `device_code` VARCHAR(50) COMMENT '设备编码',
    `area_id` BIGINT COMMENT '区域ID',
    `area_name` VARCHAR(200) COMMENT '区域名称',
    `access_type` TINYINT NOT NULL COMMENT '通行类型：1-进入 2-离开',
    `access_method` TINYINT NOT NULL COMMENT '验证方式：1-刷卡 2-密码 3-人脸 4-指纹 5-虹膜',
    `access_result` TINYINT NOT NULL COMMENT '通行结果：1-成功 2-失败 3-异常',
    `access_time` DATETIME NOT NULL COMMENT '通行时间',
    `duration` INT DEFAULT 0 COMMENT '停留时长（秒）',
    `temperature` DECIMAL(4,1) COMMENT '体温值',
    `face_image_url` VARCHAR(500) COMMENT '人脸图片URL',
    `card_status` TINYINT COMMENT '卡片状态：1-正常 2-挂失 3-注销',
    `violation_level` TINYINT DEFAULT 0 COMMENT '违规级别：0-无 1-轻微 2-一般 3-严重',
    `anti_passback_check` TINYINT DEFAULT 0 COMMENT '反潜回检查：0-未检查 1-通过 2-拦截',
    `risk_score` INT DEFAULT 0 COMMENT '风险评分（0-100）',
    `visitor_id` BIGINT COMMENT '访客ID',
    `employee_id` BIGINT COMMENT '员工ID',
    `biometric_id` VARCHAR(100) COMMENT '生物特征ID',
    `client_ip` VARCHAR(45) COMMENT '客户端IP',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`record_id`),
    KEY `idx_user_access` (`user_id`, `access_time`, `deleted_flag`),
    KEY `idx_device_access` (`device_id`, `access_time`, `deleted_flag`),
    KEY `idx_card_access` (`card_id`, `access_time`, `deleted_flag`),
    KEY `idx_area_access` (`area_id`, `access_time`, `deleted_flag`),
    KEY `idx_access_type` (`access_type`, `access_time`, `deleted_flag`),
    KEY `idx_access_result` (`access_result`, `access_time`, `deleted_flag`),
    KEY `idx_create_time` (`create_time`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门禁通行记录表';

-- 反潜回记录表
CREATE TABLE IF NOT EXISTS `t_anti_passback_record` (
    `record_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `device_id` BIGINT NOT NULL COMMENT '设备ID',
    `anti_passback_type` TINYINT NOT NULL COMMENT '反潜回类型：1-硬反潜回 2-软反潜回 3-区域反潜回 4-全局反潜回',
    `entry_device_id` BIGINT COMMENT '进入设备ID',
    `exit_device_id` BIGINT COMMENT '离开设备ID',
    `entry_time` DATETIME NOT NULL COMMENT '进入时间',
    `exit_time` DATETIME COMMENT '离开时间',
    `entry_area_id` BIGINT COMMENT '进入区域ID',
    `exit_area_id` BIGINT COMMENT '离开区域ID',
    `violation_type` TINYINT NOT NULL COMMENT '违规类型：1-重复进入 2-未配对进出 3-跨区域异常',
    `violation_level` TINYINT NOT NULL COMMENT '违规级别：1-轻微 2-一般 3-严重',
    `auto_cleared` TINYINT DEFAULT 0 COMMENT '自动清除：0-未清除 1-已清除',
    `clear_time` DATETIME COMMENT '清除时间',
    `risk_assessment` TEXT COMMENT '风险评估（JSON格式）',
    `recommended_action` VARCHAR(500) COMMENT '建议处理方式',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`record_id`),
    KEY `idx_user_anti_passback` (`user_id`, `anti_passback_type`, `create_time`),
    KEY `idx_device_anti_passback` (`device_id`, `create_time`),
    KEY `idx_entry_exit_time` (`entry_time`, `exit_time`),
    KEY `idx_violation_type` (`violation_type`, `create_time`),
    KEY `idx_auto_cleared` (`auto_cleared`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反潜回记录表';
```

### 6.3 关键技术实现要求

#### 6.3.1 反潜回引擎接口
```java
// 反潜回引擎接口
public interface AntiPassbackEngine {

    /**
     * 执行反潜回检查
     */
    @CircuitBreaker(name = "antiPassback")
    CompletableFuture<ResponseDTO<AntiPassbackResult>> performAntiPassbackCheck(
            Long userId,
            Long deviceId,
            Long areaId,
            AccessType accessType
    );

    /**
     * 检查区域反潜回
     */
    CompletableFuture<ResponseDTO<AntiPassbackResult>> checkAreaAntiPassback(
            Long userId,
            Long entryAreaId,
            Long exitAreaId,
            String direction
    );

    /**
     * 清理用户反潜回记录
     */
    CompletableFuture<ResponseDTO<Void>> clearUserAntiPassbackRecords(
            Long userId,
            Long deviceId
    );
}
```

#### 6.3.2 门禁控制服务
```java
// 门禁控制服务接口
public interface AccessControlService {

    /**
     * 验证门禁权限
     */
    @CircuitBreaker(name = "accessControl")
    CompletableFuture<ResponseDTO<AccessResult>> validateAccessPermission(
            Long userId,
            Long deviceId,
            List<Long> areaIds,
            AccessType accessType
    );

    /**
     * 执行门禁控制
     */
    CompletableFuture<ResponseDTO<Void>> executeAccessControl(
            Long deviceId,
            AccessCommand command
    );

    /**
     * 获取设备状态
     */
    CompletableFuture<ResponseDTO<DeviceStatus>> getDeviceStatus(
            Long deviceId
    );
}
```

## 7. 门禁管理优化建议

### 7.1 性能优化
- **缓存策略**: Redis缓存用户权限、设备状态、反潜回记录
- **批量处理**: 支持批量权限验证和设备控制
- **异步处理**: 使用异步处理提高并发性能
- **数据库优化**: 合理索引设计、分区表策略

### 7.2 安全增强
- **多因子认证**: 支持多种生物识别和卡片验证
- **数据加密**: 敏感数据加密存储和传输
- **实时监控**: 异常检测和实时告警机制
- **审计追踪**: 完整的操作审计链

### 7.3 智能化提升
- **AI异常检测**: 机器学习算法识别异常行为
- **智能调度**: 基于负载和时间的智能设备调度
- **预测分析**: 通行趋势预测和容量规划
- **自适应优化**: 根据使用模式自动优化参数

---

**文档版本**: v1.0.0
**创建时间**: 2025-12-16
**维护团队**: IOE-DREAM门禁管理团队
**下次更新**: 根据实际实施进度定期更新