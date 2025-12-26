# 设备自动发现功能 - 数据库设计文档

> **模块**: 门禁管理 - 设备自动发现
> **版本**: v1.0.0
> **日期**: 2025-01-30

---

## 📊 表设计

### 1. 设备发现任务表 (t_device_discovery_task)

**表说明**: 记录设备自动发现的任务信息

```sql
CREATE TABLE t_device_discovery_task (
    task_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    task_no VARCHAR(50) NOT NULL COMMENT '任务编号',

    -- 发现配置
    discovery_type TINYINT NOT NULL COMMENT '发现类型: 1-TCP扫描 2-UDP扫描 3-多播扫描 4-ONVIF 5-私有协议 6-SNMP',
    ip_range_start VARCHAR(50) COMMENT 'IP范围起始',
    ip_range_end VARCHAR(50) COMMENT 'IP范围结束',
    port_range VARCHAR(100) COMMENT '端口范围（逗号分隔）',
    timeout_seconds INT DEFAULT 180 COMMENT '超时时间（秒），默认3分钟',

    -- 任务状态
    task_status TINYINT NOT NULL DEFAULT 0 COMMENT '任务状态: 0-待执行 1-执行中 2-已完成 3-已失败 4-已停止',
    progress INT DEFAULT 0 COMMENT '进度百分比（0-100）',
    total_devices INT DEFAULT 0 COMMENT '发现设备总数',

    -- 统计信息
    success_count INT DEFAULT 0 COMMENT '成功发现设备数',
    failed_count INT DEFAULT 0 COMMENT '发现失败设备数',

    -- 执行信息
    started_time DATETIME COMMENT '开始时间',
    completed_time DATETIME COMMENT '完成时间',
    error_message TEXT COMMENT '错误信息',

    -- 审计字段
    created_by BIGINT COMMENT '创建人ID',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人ID',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除 1-已删除',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',

    PRIMARY KEY (task_id),
    UNIQUE KEY uk_task_no (task_no),
    KEY idx_task_status (task_status),
    KEY idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备发现任务表';
```

### 2. 设备发现结果表 (t_device_discovery_result)

**表说明**: 记录设备发现的详细结果

```sql
CREATE TABLE t_device_discovery_result (
    result_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '结果ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',

    -- 设备基本信息
    device_ip VARCHAR(50) NOT NULL COMMENT '设备IP地址',
    mac_address VARCHAR(50) COMMENT '设备MAC地址',
    device_name VARCHAR(100) COMMENT '设备名称',
    device_model VARCHAR(100) COMMENT '设备型号',
    device_type VARCHAR(50) COMMENT '设备类型',
    manufacturer VARCHAR(100) COMMENT '制造商',

    -- 连接信息
    protocol VARCHAR(20) COMMENT '发现协议: TCP/UDP/ONVIF/SNMP',
    port INT COMMENT '端口号',

    -- 固件信息
    firmware_version VARCHAR(50) COMMENT '固件版本',
    software_version VARCHAR(50) COMMENT '软件版本',
    hardware_version VARCHAR(50) COMMENT '硬件版本',

    -- 发现状态
    discovery_status TINYINT NOT NULL COMMENT '发现状态: 1-成功 2-失败',
    reach_status TINYINT COMMENT '可达性: 1-在线 2-离线',
    response_time_ms INT COMMENT '响应时间（毫秒）',

    -- 详细信息
    device_details JSON COMMENT '设备详细信息（JSON格式）',
    raw_response TEXT COMMENT '原始响应数据',

    -- 操作标记
    is_imported TINYINT NOT NULL DEFAULT 0 COMMENT '是否已导入: 0-未导入 1-已导入',
    import_device_id BIGINT COMMENT '导入后设备ID',
    import_time DATETIME COMMENT '导入时间',

    -- 错误信息
    error_message TEXT COMMENT '错误信息',

    -- 审计字段
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (result_id),
    KEY idx_task_id (task_id),
    KEY idx_device_ip (device_ip),
    KEY idx_discovery_status (discovery_status),
    KEY idx_is_imported (is_imported)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备发现结果表';
```

---

## 📋 字段说明

### t_device_discovery_task 字段说明

| 字段名 | 类型 | 说明 | 备注 |
|-------|------|------|------|
| task_id | BIGINT | 任务ID（主键） | 自增 |
| task_no | VARCHAR(50) | 任务编号 | 格式: DD-YYYYMMDD-001 |
| discovery_type | TINYINT | 发现类型 | 1-6 |
| ip_range_start | VARCHAR(50) | IP起始 | 示例: 192.168.1.1 |
| ip_range_end | VARCHAR(50) | IP结束 | 示例: 192.168.1.255 |
| port_range | VARCHAR(100) | 端口范围 | 示例: 80,443,8000 |
| timeout_seconds | INT | 超时时间 | 默认180秒（3分钟） |
| task_status | TINYINT | 任务状态 | 0-4 |
| progress | INT | 进度百分比 | 0-100 |
| total_devices | INT | 设备总数 | - |
| success_count | INT | 成功数 | - |
| failed_count | INT | 失败数 | - |

### 枚举值说明

**discovery_type（发现类型）**:
```java
public enum DiscoveryType {
    TCP_SCAN(1, "TCP扫描"),
    UDP_SCAN(2, "UDP扫描"),
    MULTICAST_SCAN(3, "多播扫描"),
    ONVIF(4, "ONVIF协议"),
    PRIVATE_PROTOCOL(5, "私有协议"),
    SNMP(6, "SNMP协议");
}
```

**task_status（任务状态）**:
```java
public enum TaskStatus {
    PENDING(0, "待执行"),
    RUNNING(1, "执行中"),
    COMPLETED(2, "已完成"),
    FAILED(3, "已失败"),
    STOPPED(4, "已停止");
}
```

**discovery_status（发现状态）**:
```java
public enum DiscoveryStatus {
    SUCCESS(1, "成功"),
    FAILED(2, "失败");
}
```

**reach_status（可达性）**:
```java
public enum ReachStatus {
    ONLINE(1, "在线"),
    OFFLINE(2, "离线");
}
```

---

## 🔗 索引设计

### 主要索引
1. **主键索引**: `task_id`, `result_id`（自动创建）
2. **唯一索引**: `uk_task_no`（任务编号唯一）
3. **普通索引**:
   - `idx_task_status`（任务状态查询优化）
   - `idx_created_time`（时间范围查询）
   - `idx_task_id`（任务关联查询）
   - `idx_device_ip`（设备IP查询）
   - `idx_discovery_status`（发现状态筛选）
   - `idx_is_imported`（导入状态筛选）

### 索引优化策略
- **覆盖索引**: 为常用查询组合创建覆盖索引
- **前缀索引**: 对VARCHAR字段使用前缀索引
- **分区表**: 大数据量时按created_time分区

---

## 📊 初始化数据

### 设备发现类型字典

```sql
INSERT INTO t_sys_dict_type (dict_type_code, dict_type_name, sort_order, created_time, updated_time)
VALUES ('DISCOVERY_TYPE', '设备发现类型', 100, NOW(), NOW());

INSERT INTO t_sys_dict_data (dict_type_code, dict_code, dict_value, sort_order, created_time, updated_time)
VALUES
('DISCOVERY_TYPE', 'TCP_SCAN', 'TCP扫描', 1, NOW(), NOW()),
('DISCOVERY_TYPE', 'UDP_SCAN', 'UDP扫描', 2, NOW(), NOW()),
('DISCOVERY_TYPE', 'MULTICAST_SCAN', '多播扫描', 3, NOW(), NOW()),
('DISCOVERY_TYPE', 'ONVIF', 'ONVIF协议', 4, NOW(), NOW()),
('DISCOVERY_TYPE', 'PRIVATE_PROTOCOL', '私有协议', 5, NOW(), NOW()),
('DISCOVERY_TYPE', 'SNMP', 'SNMP协议', 6, NOW(), NOW());
```

---

## 🔧 使用示例

### 示例1: 创建设备发现任务

```sql
INSERT INTO t_device_discovery_task (
    task_no, discovery_type, ip_range_start, ip_range_end,
    port_range, timeout_seconds, task_status, created_by
) VALUES (
    'DD-20250130-001',
    1,
    '192.168.1.1',
    '192.168.1.255',
    '80,443,8000,37777',
    180,
    0,
    1
);
```

### 示例2: 查询执行中的任务

```sql
SELECT task_id, task_no, discovery_type, progress, total_devices, started_time
FROM t_device_discovery_task
WHERE task_status = 1
ORDER BY started_time DESC;
```

### 示例3: 查询任务发现结果

```sql
SELECT
    r.device_ip,
    r.device_name,
    r.device_model,
    r.discovery_status,
    r.reach_status,
    r.response_time_ms
FROM t_device_discovery_result r
WHERE r.task_id = 1
ORDER BY r.discovery_status, r.device_ip;
```

---

## ⚠️ 注意事项

### 性能优化
1. **批量插入**: 使用批量插入优化导入性能
2. **索引延迟**: 大批量导入时延迟索引创建
3. **分区表**: 单表超过1000万记录时考虑分区

### 数据清理
1. **历史数据清理**: 定期清理3个月前的任务数据
2. **结果清理**: 导入后的结果定期归档
3. **日志清理**: 原始响应数据定期清理

### 安全考虑
1. **IP范围验证**: 防止扫描内网敏感IP段
2. **权限控制**: 限制设备发现权限
3. **审计日志**: 记录所有发现操作

---

## 📝 变更历史

| 版本 | 日期 | 变更内容 | 变更人 |
|------|------|---------|--------|
| v1.0.0 | 2025-01-30 | 初始版本 | AI Assistant |

---

**文档创建时间**: 2025-01-30
**数据库类型**: MySQL 8.0+
**字符集**: utf8mb4_unicode_ci
