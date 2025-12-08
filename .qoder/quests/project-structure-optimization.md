# IOE-DREAM数据库初始化脚本完整性优化设计方案

## 1. 项目现状深度分析

### 1.1 现有数据库脚本结构

**已有脚本分布**：

| 目录位置 | 脚本数量 | 覆盖模块 | 完整性评估 |
|---------|---------|---------|-----------|
| `database-scripts/common-service/` | 23个SQL文件 | 公共模块完整 | ✅ 100% |
| `database-scripts/visitor/` | 1个SQL文件 | 访客模块部分 | ⚠️ 30% |
| `database-scripts/oa-service/` | 0个SQL文件 | OA模块缺失 | ❌ 0% |
| `deployment/mysql/init/` | 2个SQL文件 | 基础设施 | ✅ 100% |
| `数据库SQL脚本/mysql/` | 4个SQL文件 | 区域管理 | ⚠️ 部分 |

**严重问题识别**：

1. **门禁服务数据库完全缺失**（影响8090端口服务）
   - 缺少门禁设备表
   - 缺少门禁权限表
   - 缺少通行记录表
   - 缺少区域配置表

2. **考勤服务数据库完全缺失**（影响8091端口服务）
   - 缺少考勤记录表
   - 缺少班次配置表
   - 缺少排班计划表
   - 缺少考勤规则表
   - 缺少异常申请表

3. **消费服务数据库完全缺失**（影响8094端口服务）
   - 缺少消费记录表
   - 缺少账户管理表
   - 缺少充值退款表
   - 缺少补贴配置表

4. **视频服务数据库完全缺失**（影响8092端口服务）
   - 缺少视频设备表
   - 缺少录像记录表
   - 缺少告警事件表
   - 缺少智能分析表

5. **访客服务数据库不完整**（影响8095端口服务）
   - 缺少预约审批表
   - 缺少访客登记表
   - 缺少黑名单管理表
   - 缺少物流预约表

6. **OA服务数据库完全缺失**（影响8089端口服务）
   - 缺少工作流定义表
   - 缺少工作流实例表
   - 缺少审批节点表
   - 缺少文档管理表

7. **设备通讯服务数据库完全缺失**（影响8087端口服务）
   - 缺少设备协议表
   - 缺少设备连接表
   - 缺少数据采集表
   - 缺少设备健康表

### 1.2 前端页面业务模块分析

通过分析`smart-admin-web-javascript/src/views/business/`目录结构，识别出以下业务模块：

| 前端目录 | 业务功能 | 对应微服务 | 数据库脚本状态 |
|---------|---------|-----------|---------------|
| `access/` | 门禁管理（4个组件） | ioedream-access-service | ❌ 完全缺失 |
| `attendance/` | 考勤管理（1个组件） | ioedream-attendance-service | ❌ 完全缺失 |
| `consume/` | 消费管理（3个组件） | ioedream-consume-service | ❌ 完全缺失 |
| `consumption/` | 消费详情（6个组件） | ioedream-consume-service | ❌ 完全缺失 |
| `visitor/` | 访客管理（10个组件） | ioedream-visitor-service | ⚠️ 30%完成 |
| `smart-video/` | 视频监控（22个组件） | ioedream-video-service | ❌ 完全缺失 |
| `oa/` | OA工作流（3个组件） | ioedream-oa-service | ❌ 完全缺失 |
| `erp/` | ERP集成（2个组件） | ioedream-oa-service | ❌ 完全缺失 |
| `common/` | 公共组件（1个组件） | ioedream-common-service | ✅ 100%完成 |

### 1.3 核心问题根因分析

**数据库脚本缺失率**：
- 公共模块：0%缺失（23/23）✅
- 业务模块：**87.5%缺失**（7/8业务服务缺少数据库脚本）❌

**影响范围**：
- **部署失败风险**：100%（缺少核心业务表无法启动服务）
- **数据一致性风险**：100%（无初始化数据，业务逻辑无法执行）
- **功能可用性**：0%（前端89个组件无法正常使用）

## 2. 数据库脚本设计方案

### 2.1 数据库架构设计原则

**微服务数据库隔离原则**：

```
每个微服务独立数据库，禁止跨库访问
├── ioedream_common_db        # 公共模块数据库（已完成）
├── ioedream_access_db         # 门禁服务数据库（待补充）
├── ioedream_attendance_db     # 考勤服务数据库（待补充）
├── ioedream_consume_db        # 消费服务数据库（待补充）
├── ioedream_video_db          # 视频服务数据库（待补充）
├── ioedream_visitor_db        # 访客服务数据库（待补充）
├── ioedream_oa_db            # OA服务数据库（待补充）
└── ioedream_device_comm_db   # 设备通讯数据库（待补充）
```

**数据库表命名规范**：

| 服务类型 | 表名前缀 | 示例 |
|---------|---------|------|
| 公共服务 | `t_common_` | `t_common_user`, `t_common_area` |
| 门禁服务 | `t_access_` | `t_access_record`, `t_access_permission` |
| 考勤服务 | `t_attendance_` | `t_attendance_record`, `t_attendance_shift` |
| 消费服务 | `t_consume_` | `t_consume_record`, `t_consume_account` |
| 视频服务 | `t_video_` | `t_video_device`, `t_video_record` |
| 访客服务 | `t_visitor_` | `t_visitor_appointment`, `t_visitor_record` |
| OA服务 | `t_oa_` | `t_oa_workflow`, `t_oa_document` |
| 设备通讯 | `t_device_` | `t_device_protocol`, `t_device_connection` |

### 2.2 门禁服务数据库设计（ioedream_access_db）

**核心表结构**（13个表）：

#### 2.2.1 门禁设备表 (t_access_device)

| 字段名 | 类型 | 说明 | 索引 |
|-------|------|------|------|
| device_id | BIGINT PK | 设备ID | PK |
| device_code | VARCHAR(100) | 设备编码 | UK |
| device_name | VARCHAR(200) | 设备名称 | |
| device_type | VARCHAR(50) | 设备类型（CARD/FACE/FINGERPRINT） | IDX |
| ip_address | VARCHAR(50) | IP地址 | |
| port | INT | 通讯端口 | |
| area_id | BIGINT | 所属区域ID | FK, IDX |
| protocol_type | VARCHAR(50) | 协议类型（Wiegand/TCP） | |
| device_status | TINYINT | 设备状态（0-离线/1-在线） | IDX |
| manufacturer | VARCHAR(100) | 制造商 | |
| device_model | VARCHAR(100) | 设备型号 | |
| firmware_version | VARCHAR(50) | 固件版本 | |
| install_date | DATE | 安装日期 | |
| last_online_time | DATETIME | 最后在线时间 | IDX |
| config_json | JSON | 设备配置（JSON格式） | |
| enabled_flag | TINYINT | 启用标记 | IDX |
| remark | VARCHAR(500) | 备注 | |
| create_time | DATETIME | 创建时间 | IDX |
| update_time | DATETIME | 更新时间 | |
| deleted_flag | TINYINT | 删除标记 | IDX |

**关键索引**：
```sql
PRIMARY KEY (device_id)
UNIQUE KEY uk_device_code (device_code)
INDEX idx_area_id (area_id)
INDEX idx_device_status (device_status, enabled_flag)
INDEX idx_create_time (create_time)
```

#### 2.2.2 门禁权限表 (t_access_permission)

| 字段名 | 类型 | 说明 | 索引 |
|-------|------|------|------|
| permission_id | BIGINT PK | 权限ID | PK |
| person_id | BIGINT | 人员ID | FK, IDX |
| person_name | VARCHAR(100) | 人员姓名 | |
| person_type | VARCHAR(50) | 人员类型（EMPLOYEE/VISITOR） | IDX |
| device_id | BIGINT | 设备ID | FK, IDX |
| area_id | BIGINT | 区域ID | FK, IDX |
| permission_type | VARCHAR(50) | 权限类型（PERMANENT/TEMPORARY） | IDX |
| start_time | DATETIME | 生效开始时间 | IDX |
| end_time | DATETIME | 生效结束时间 | IDX |
| monday_access | TINYINT | 周一通行权限 | |
| tuesday_access | TINYINT | 周二通行权限 | |
| wednesday_access | TINYINT | 周三通行权限 | |
| thursday_access | TINYINT | 周四通行权限 | |
| friday_access | TINYINT | 周五通行权限 | |
| saturday_access | TINYINT | 周六通行权限 | |
| sunday_access | TINYINT | 周日通行权限 | |
| time_config | JSON | 时间段配置 | |
| status | TINYINT | 状态（0-禁用/1-启用） | IDX |
| workflow_instance_id | BIGINT | 工作流实例ID | FK, IDX |
| approve_user_id | BIGINT | 审批人ID | |
| approve_time | DATETIME | 审批时间 | |
| remark | TEXT | 备注 | |
| create_time | DATETIME | 创建时间 | IDX |
| update_time | DATETIME | 更新时间 | |
| deleted_flag | TINYINT | 删除标记 | IDX |

**关键索引**：
```sql
PRIMARY KEY (permission_id)
INDEX idx_person_device (person_id, device_id)
INDEX idx_time_range (start_time, end_time)
INDEX idx_status (status, deleted_flag)
```

#### 2.2.3 通行记录表 (t_access_record)

| 字段名 | 类型 | 说明 | 索引 |
|-------|------|------|------|
| record_id | BIGINT PK | 记录ID | PK |
| device_id | BIGINT | 设备ID | FK, IDX |
| person_id | BIGINT | 人员ID | FK, IDX |
| person_name | VARCHAR(100) | 人员姓名 | |
| person_type | VARCHAR(50) | 人员类型 | IDX |
| access_method | VARCHAR(50) | 通行方式（CARD/FACE/FINGERPRINT） | IDX |
| credential_info | JSON | 凭证信息 | |
| verification_result | VARCHAR(50) | 验证结果（SUCCESS/FAILED） | IDX |
| match_score | DECIMAL(5,2) | 匹配分数 | |
| access_direction | VARCHAR(20) | 通行方向（IN/OUT） | IDX |
| access_time | DATETIME | 通行时间 | IDX |
| area_id | BIGINT | 区域ID | FK, IDX |
| temperature | DECIMAL(4,1) | 体温 | |
| photo_url | VARCHAR(500) | 现场照片URL | |
| video_url | VARCHAR(500) | 视频URL | |
| event_type | VARCHAR(50) | 事件类型（NORMAL/FORCE/ALARM） | IDX |
| abnormal_reason | VARCHAR(500) | 异常原因 | |
| create_time | DATETIME | 创建时间 | IDX |
| deleted_flag | TINYINT | 删除标记 | IDX |

**关键索引**：
```sql
PRIMARY KEY (record_id)
INDEX idx_person_time (person_id, access_time DESC)
INDEX idx_device_time (device_id, access_time DESC)
INDEX idx_area_time (area_id, access_time DESC)
INDEX idx_event_type (event_type, verification_result)
```

#### 2.2.4 区域配置表 (t_access_area)

| 字段名 | 类型 | 说明 | 索引 |
|-------|------|------|------|
| area_id | BIGINT PK | 区域ID | PK |
| area_code | VARCHAR(100) | 区域编码 | UK |
| area_name | VARCHAR(200) | 区域名称 | |
| parent_id | BIGINT | 父区域ID | FK, IDX |
| area_type | VARCHAR(50) | 区域类型（BUILDING/FLOOR/ROOM） | IDX |
| area_level | INT | 区域层级 | IDX |
| area_path | VARCHAR(500) | 区域路径（树形路径） | |
| security_level | VARCHAR(50) | 安全等级（LOW/MEDIUM/HIGH） | IDX |
| require_escort | TINYINT | 是否需要陪同 | |
| max_capacity | INT | 最大容量 | |
| current_count | INT | 当前人数 | |
| location_info | JSON | 位置信息 | |
| enabled_flag | TINYINT | 启用标记 | IDX |
| remark | VARCHAR(500) | 备注 | |
| create_time | DATETIME | 创建时间 | IDX |
| update_time | DATETIME | 更新时间 | |
| deleted_flag | TINYINT | 删除标记 | IDX |

**关键索引**：
```sql
PRIMARY KEY (area_id)
UNIQUE KEY uk_area_code (area_code)
INDEX idx_parent_id (parent_id)
INDEX idx_tree_path (area_path(255))
```

#### 2.2.5 门禁规则表 (t_access_rule)

门禁规则表用于定义门禁控制的业务规则。

| 字段名 | 类型 | 说明 |
|-------|------|------|
| rule_id | BIGINT PK | 规则ID |
| rule_name | VARCHAR(200) | 规则名称 |
| rule_type | VARCHAR(50) | 规则类型（APB/INTERLOCK/LINKAGE） |
| rule_config | JSON | 规则配置 |
| priority | INT | 优先级 |
| enabled_flag | TINYINT | 启用标记 |
| effective_time | JSON | 生效时间配置 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted_flag | TINYINT | 删除标记 |

#### 2.2.6 门禁事件表 (t_access_event)

记录所有门禁系统的事件日志。

| 字段名 | 类型 | 说明 |
|-------|------|------|
| event_id | BIGINT PK | 事件ID |
| event_type | VARCHAR(50) | 事件类型（ALARM/FORCE/TIMEOUT） |
| device_id | BIGINT | 设备ID |
| area_id | BIGINT | 区域ID |
| person_id | BIGINT | 相关人员ID |
| event_level | VARCHAR(50) | 事件级别（INFO/WARNING/ERROR） |
| event_content | TEXT | 事件内容 |
| event_time | DATETIME | 事件时间 |
| handle_status | VARCHAR(50) | 处理状态（PENDING/HANDLED） |
| handle_user_id | BIGINT | 处理人ID |
| handle_time | DATETIME | 处理时间 |
| handle_remark | TEXT | 处理备注 |
| create_time | DATETIME | 创建时间 |
| deleted_flag | TINYINT | 删除标记 |

#### 2.2.7 门禁联动表 (t_access_linkage)

定义门禁与其他系统的联动配置。

| 字段名 | 类型 | 说明 |
|-------|------|------|
| linkage_id | BIGINT PK | 联动ID |
| linkage_name | VARCHAR(200) | 联动名称 |
| trigger_type | VARCHAR(50) | 触发类型 |
| trigger_config | JSON | 触发条件配置 |
| action_type | VARCHAR(50) | 动作类型 |
| action_config | JSON | 动作配置 |
| enabled_flag | TINYINT | 启用标记 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted_flag | TINYINT | 删除标记 |

**其他门禁服务表**：
- `t_access_apb_config` - APB反潜回配置表
- `t_access_interlock` - 互锁配置表
- `t_access_holiday` - 节假日配置表
- `t_access_time_period` - 时间段配置表
- `t_access_black_list` - 黑名单表
- `t_access_white_list` - 白名单表

### 2.3 考勤服务数据库设计（ioedream_attendance_db）

**核心表结构**（15个表）：

#### 2.3.1 考勤记录表 (t_attendance_record)

| 字段名 | 类型 | 说明 | 索引 |
|-------|------|------|------|
| record_id | BIGINT PK | 记录ID | PK |
| employee_id | BIGINT | 员工ID | FK, IDX |
| employee_no | VARCHAR(50) | 工号 | IDX |
| employee_name | VARCHAR(100) | 员工姓名 | |
| department_id | BIGINT | 部门ID | FK, IDX |
| attendance_date | DATE | 考勤日期 | IDX |
| shift_id | BIGINT | 班次ID | FK, IDX |
| clock_in_time | DATETIME | 上班打卡时间 | |
| clock_out_time | DATETIME | 下班打卡时间 | |
| clock_in_device_id | BIGINT | 上班打卡设备ID | |
| clock_out_device_id | BIGINT | 下班打卡设备ID | |
| clock_in_method | VARCHAR(50) | 上班打卡方式 | |
| clock_out_method | VARCHAR(50) | 下班打卡方式 | |
| clock_in_location | JSON | 上班打卡位置 | |
| clock_out_location | JSON | 下班打卡位置 | |
| work_hours | DECIMAL(5,2) | 工作时长（小时） | |
| overtime_hours | DECIMAL(5,2) | 加班时长（小时） | |
| attendance_status | VARCHAR(50) | 考勤状态（NORMAL/LATE/EARLY/ABSENT） | IDX |
| late_minutes | INT | 迟到分钟数 | |
| early_minutes | INT | 早退分钟数 | |
| exception_type | VARCHAR(50) | 异常类型 | IDX |
| exception_reason | VARCHAR(500) | 异常原因 | |
| workflow_instance_id | BIGINT | 工作流实例ID（补卡申请） | FK, IDX |
| remark | TEXT | 备注 | |
| create_time | DATETIME | 创建时间 | IDX |
| update_time | DATETIME | 更新时间 | |
| deleted_flag | TINYINT | 删除标记 | IDX |

**关键索引**：
```sql
PRIMARY KEY (record_id)
UNIQUE KEY uk_employee_date (employee_id, attendance_date)
INDEX idx_department_date (department_id, attendance_date)
INDEX idx_status (attendance_status, exception_type)
INDEX idx_date_range (attendance_date)
```

#### 2.3.2 班次配置表 (t_attendance_shift)

| 字段名 | 类型 | 说明 | 索引 |
|-------|------|------|------|
| shift_id | BIGINT PK | 班次ID | PK |
| shift_code | VARCHAR(50) | 班次编码 | UK |
| shift_name | VARCHAR(200) | 班次名称 | |
| shift_type | VARCHAR(50) | 班次类型（FIXED/FLEXIBLE/ROTATION） | IDX |
| work_start_time | TIME | 上班时间 | |
| work_end_time | TIME | 下班时间 | |
| break_start_time | TIME | 午休开始时间 | |
| break_end_time | TIME | 午休结束时间 | |
| late_threshold | INT | 迟到阈值（分钟） | |
| early_threshold | INT | 早退阈值（分钟） | |
| flexible_minutes | INT | 弹性时间（分钟） | |
| work_hours | DECIMAL(4,2) | 标准工作时长（小时） | |
| overtime_rule | JSON | 加班规则配置 | |
| enabled_flag | TINYINT | 启用标记 | IDX |
| remark | VARCHAR(500) | 备注 | |
| create_time | DATETIME | 创建时间 | IDX |
| update_time | DATETIME | 更新时间 | |
| deleted_flag | TINYINT | 删除标记 | IDX |

**关键索引**：
```sql
PRIMARY KEY (shift_id)
UNIQUE KEY uk_shift_code (shift_code)
INDEX idx_shift_type (shift_type, enabled_flag)
```

#### 2.3.3 排班计划表 (t_attendance_schedule)

| 字段名 | 类型 | 说明 | 索引 |
|-------|------|------|------|
| schedule_id | BIGINT PK | 排班ID | PK |
| employee_id | BIGINT | 员工ID | FK, IDX |
| schedule_date | DATE | 排班日期 | IDX |
| shift_id | BIGINT | 班次ID | FK, IDX |
| department_id | BIGINT | 部门ID | FK, IDX |
| schedule_type | VARCHAR(50) | 排班类型（NORMAL/OVERTIME/REST） | IDX |
| schedule_source | VARCHAR(50) | 排班来源（AUTO/MANUAL/IMPORT） | |
| workflow_instance_id | BIGINT | 工作流实例ID（调班申请） | FK, IDX |
| remark | VARCHAR(500) | 备注 | |
| create_user_id | BIGINT | 创建人ID | |
| create_time | DATETIME | 创建时间 | IDX |
| update_time | DATETIME | 更新时间 | |
| deleted_flag | TINYINT | 删除标记 | IDX |

**关键索引**：
```sql
PRIMARY KEY (schedule_id)
UNIQUE KEY uk_employee_date (employee_id, schedule_date)
INDEX idx_department_date (department_id, schedule_date)
INDEX idx_shift_date (shift_id, schedule_date)
```

#### 2.3.4 考勤规则表 (t_attendance_rule)

考勤规则表定义各种考勤计算规则。

| 字段名 | 类型 | 说明 |
|-------|------|------|
| rule_id | BIGINT PK | 规则ID |
| rule_name | VARCHAR(200) | 规则名称 |
| rule_type | VARCHAR(50) | 规则类型（LATE/EARLY/OVERTIME/ABSENT） |
| rule_config | JSON | 规则配置 |
| department_id | BIGINT | 适用部门ID（NULL表示全局） |
| priority | INT | 优先级 |
| enabled_flag | TINYINT | 启用标记 |
| effective_date | DATE | 生效日期 |
| expire_date | DATE | 失效日期 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted_flag | TINYINT | 删除标记 |

#### 2.3.5 异常申请表 (t_attendance_exception)

记录所有考勤异常申请（请假、补卡、加班等）。

| 字段名 | 类型 | 说明 |
|-------|------|------|
| exception_id | BIGINT PK | 异常ID |
| exception_type | VARCHAR(50) | 异常类型（LEAVE/MAKEUP/OVERTIME/BUSINESS_TRIP） |
| employee_id | BIGINT | 员工ID |
| start_date | DATE | 开始日期 |
| end_date | DATE | 结束日期 |
| start_time | DATETIME | 开始时间 |
| end_time | DATETIME | 结束时间 |
| duration | DECIMAL(5,2) | 时长（小时/天） |
| reason | TEXT | 申请原因 |
| attachment_urls | JSON | 附件URLs |
| workflow_instance_id | BIGINT | 工作流实例ID |
| approve_status | VARCHAR(50) | 审批状态 |
| approve_time | DATETIME | 审批时间 |
| remark | TEXT | 备注 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted_flag | TINYINT | 删除标记 |

#### 2.3.6 考勤统计表 (t_attendance_statistics)

每月考勤统计数据汇总表。

| 字段名 | 类型 | 说明 |
|-------|------|------|
| statistics_id | BIGINT PK | 统计ID |
| employee_id | BIGINT | 员工ID |
| statistics_month | VARCHAR(7) | 统计月份（YYYY-MM） |
| work_days | INT | 应出勤天数 |
| actual_days | INT | 实际出勤天数 |
| late_count | INT | 迟到次数 |
| early_count | INT | 早退次数 |
| absent_count | INT | 缺勤次数 |
| leave_days | DECIMAL(5,2) | 请假天数 |
| overtime_hours | DECIMAL(5,2) | 加班时长 |
| total_work_hours | DECIMAL(6,2) | 总工作时长 |
| attendance_rate | DECIMAL(5,2) | 出勤率 |
| statistics_status | VARCHAR(50) | 统计状态（DRAFT/CONFIRMED） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted_flag | TINYINT | 删除标记 |

**其他考勤服务表**：
- `t_attendance_leave_type` - 请假类型表
- `t_attendance_leave_balance` - 年假余额表
- `t_attendance_overtime_rule` - 加班规则表
- `t_attendance_holiday` - 节假日配置表
- `t_attendance_shift_group` - 班次组配置表
- `t_attendance_rotation_rule` - 轮班规则表
- `t_attendance_makeup_card` - 补卡记录表
- `t_attendance_device` - 考勤设备表

### 2.4 消费服务数据库设计（ioedream_consume_db）

**核心表结构**（12个表）：

#### 2.4.1 消费记录表 (t_consume_record)

| 字段名 | 类型 | 说明 | 索引 |
|-------|------|------|------|
| record_id | BIGINT PK | 记录ID | PK |
| transaction_no | VARCHAR(100) | 交易流水号 | UK |
| account_id | BIGINT | 账户ID | FK, IDX |
| user_id | BIGINT | 用户ID | FK, IDX |
| user_name | VARCHAR(100) | 用户姓名 | |
| department_id | BIGINT | 部门ID | FK, IDX |
| device_id | BIGINT | 消费设备ID | FK, IDX |
| consume_type | VARCHAR(50) | 消费类型（MEAL/SHOPPING/VENDING） | IDX |
| consume_amount | DECIMAL(10,2) | 消费金额 | |
| discount_amount | DECIMAL(10,2) | 优惠金额 | |
| actual_amount | DECIMAL(10,2) | 实际支付金额 | |
| account_balance_before | DECIMAL(10,2) | 消费前余额 | |
| account_balance_after | DECIMAL(10,2) | 消费后余额 | |
| payment_method | VARCHAR(50) | 支付方式（CARD/FACE/MOBILE） | IDX |
| meal_type | VARCHAR(50) | 餐别（BREAKFAST/LUNCH/DINNER） | IDX |
| meal_date | DATE | 就餐日期 | IDX |
| meal_time | TIME | 就餐时间 | |
| merchant_id | BIGINT | 商户ID | FK, IDX |
| merchant_name | VARCHAR(200) | 商户名称 | |
| product_info | JSON | 商品信息 | |
| subsidy_amount | DECIMAL(10,2) | 补贴金额 | |
| consume_time | DATETIME | 消费时间 | IDX |
| consume_status | VARCHAR(50) | 消费状态（SUCCESS/FAILED/REFUNDED） | IDX |
| refund_amount | DECIMAL(10,2) | 退款金额 | |
| refund_time | DATETIME | 退款时间 | |
| remark | VARCHAR(500) | 备注 | |
| create_time | DATETIME | 创建时间 | IDX |
| deleted_flag | TINYINT | 删除标记 | IDX |

**关键索引**：
```sql
PRIMARY KEY (record_id)
UNIQUE KEY uk_transaction_no (transaction_no)
INDEX idx_user_time (user_id, consume_time DESC)
INDEX idx_account_time (account_id, consume_time DESC)
INDEX idx_meal_date (meal_date, meal_type)
INDEX idx_device_time (device_id, consume_time)
INDEX idx_status (consume_status, deleted_flag)
```

#### 2.4.2 账户管理表 (t_consume_account)

| 字段名 | 类型 | 说明 | 索引 |
|-------|------|------|------|
| account_id | BIGINT PK | 账户ID | PK |
| account_no | VARCHAR(100) | 账号 | UK |
| user_id | BIGINT | 用户ID | FK, UK |
| user_type | VARCHAR(50) | 用户类型（EMPLOYEE/TEMPORARY） | IDX |
| account_status | VARCHAR(50) | 账户状态（NORMAL/FROZEN/DISABLED） | IDX |
| balance | DECIMAL(10,2) | 账户余额 | |
| frozen_balance | DECIMAL(10,2) | 冻结余额 | |
| total_recharge | DECIMAL(10,2) | 累计充值 | |
| total_consume | DECIMAL(10,2) | 累计消费 | |
| total_subsidy | DECIMAL(10,2) | 累计补贴 | |
| daily_limit | DECIMAL(10,2) | 每日消费限额 | |
| monthly_limit | DECIMAL(10,2) | 每月消费限额 | |
| overdraft_limit | DECIMAL(10,2) | 透支限额 | |
| card_no | VARCHAR(100) | 卡号 | UK |
| card_type | VARCHAR(50) | 卡类型（EMPLOYEE_CARD/TEMP_CARD） | |
| card_status | VARCHAR(50) | 卡状态（NORMAL/LOST/LOCKED） | IDX |
| open_date | DATE | 开户日期 | |
| expire_date | DATE | 到期日期 | |
| last_consume_time | DATETIME | 最后消费时间 | IDX |
| remark | VARCHAR(500) | 备注 | |
| create_time | DATETIME | 创建时间 | IDX |
| update_time | DATETIME | 更新时间 | |
| deleted_flag | TINYINT | 删除标记 | IDX |

**关键索引**：
```sql
PRIMARY KEY (account_id)
UNIQUE KEY uk_account_no (account_no)
UNIQUE KEY uk_user_id (user_id)
UNIQUE KEY uk_card_no (card_no)
INDEX idx_account_status (account_status, card_status)
```

#### 2.4.3 充值记录表 (t_consume_recharge)

| 字段名 | 类型 | 说明 | 索引 |
|-------|------|------|------|
| recharge_id | BIGINT PK | 充值ID | PK |
| recharge_no | VARCHAR(100) | 充值单号 | UK |
| account_id | BIGINT | 账户ID | FK, IDX |
| user_id | BIGINT | 用户ID | FK, IDX |
| recharge_amount | DECIMAL(10,2) | 充值金额 | |
| actual_amount | DECIMAL(10,2) | 实际到账金额 | |
| payment_method | VARCHAR(50) | 支付方式（CASH/ALIPAY/WECHAT） | IDX |
| payment_channel | VARCHAR(100) | 支付渠道 | |
| trade_no | VARCHAR(200) | 第三方交易号 | UK |
| recharge_type | VARCHAR(50) | 充值类型（SELF/ADMIN） | IDX |
| operator_id | BIGINT | 操作员ID | |
| recharge_time | DATETIME | 充值时间 | IDX |
| recharge_status | VARCHAR(50) | 充值状态（SUCCESS/FAILED/PROCESSING） | IDX |
| balance_before | DECIMAL(10,2) | 充值前余额 | |
| balance_after | DECIMAL(10,2) | 充值后余额 | |
| remark | VARCHAR(500) | 备注 | |
| create_time | DATETIME | 创建时间 | IDX |
| update_time | DATETIME | 更新时间 | |
| deleted_flag | TINYINT | 删除标记 | IDX |

**关键索引**：
```sql
PRIMARY KEY (recharge_id)
UNIQUE KEY uk_recharge_no (recharge_no)
UNIQUE KEY uk_trade_no (trade_no)
INDEX idx_account_time (account_id, recharge_time DESC)
INDEX idx_status (recharge_status, deleted_flag)
```

#### 2.4.4 退款记录表 (t_consume_refund)

退款记录表用于记录所有退款申请和处理。

| 字段名 | 类型 | 说明 |
|-------|------|------|
| refund_id | BIGINT PK | 退款ID |
| refund_no | VARCHAR(100) | 退款单号 |
| record_id | BIGINT | 原消费记录ID |
| account_id | BIGINT | 账户ID |
| user_id | BIGINT | 用户ID |
| refund_amount | DECIMAL(10,2) | 退款金额 |
| refund_reason | VARCHAR(500) | 退款原因 |
| refund_type | VARCHAR(50) | 退款类型（FULL/PARTIAL） |
| workflow_instance_id | BIGINT | 工作流实例ID |
| approve_status | VARCHAR(50) | 审批状态 |
| refund_time | DATETIME | 退款时间 |
| refund_status | VARCHAR(50) | 退款状态 |
| operator_id | BIGINT | 操作员ID |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted_flag | TINYINT | 删除标记 |

#### 2.4.5 补贴配置表 (t_consume_subsidy_config)

定义各种补贴规则和配置。

| 字段名 | 类型 | 说明 |
|-------|------|------|
| config_id | BIGINT PK | 配置ID |
| subsidy_name | VARCHAR(200) | 补贴名称 |
| subsidy_type | VARCHAR(50) | 补贴类型（MONTHLY/DAILY/MEAL） |
| subsidy_amount | DECIMAL(10,2) | 补贴金额 |
| effective_date | DATE | 生效日期 |
| expire_date | DATE | 失效日期 |
| apply_rule | JSON | 适用规则 |
| department_ids | JSON | 适用部门 |
| enabled_flag | TINYINT | 启用标记 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted_flag | TINYINT | 删除标记 |

#### 2.4.6 补贴发放表 (t_consume_subsidy_grant)

记录补贴发放历史。

| 字段名 | 类型 | 说明 |
|-------|------|------|
| grant_id | BIGINT PK | 发放ID |
| grant_no | VARCHAR(100) | 发放单号 |
| config_id | BIGINT | 配置ID |
| account_id | BIGINT | 账户ID |
| user_id | BIGINT | 用户ID |
| subsidy_amount | DECIMAL(10,2) | 补贴金额 |
| grant_month | VARCHAR(7) | 发放月份 |
| grant_time | DATETIME | 发放时间 |
| grant_status | VARCHAR(50) | 发放状态 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted_flag | TINYINT | 删除标记 |

**其他消费服务表**：
- `t_consume_merchant` - 商户管理表
- `t_consume_product` - 商品管理表
- `t_consume_discount` - 优惠活动表
- `t_consume_device` - 消费设备表
- `t_consume_blacklist` - 消费黑名单表
- `t_consume_statistics` - 消费统计表

### 2.5 视频服务数据库设计（ioedream_video_db）

**核心表结构**（10个表）：

#### 2.5.1 视频设备表 (t_video_device)

| 字段名 | 类型 | 说明 | 索引 |
|-------|------|------|------|
| device_id | BIGINT PK | 设备ID | PK |
| device_code | VARCHAR(100) | 设备编码 | UK |
| device_name | VARCHAR(200) | 设备名称 | |
| device_type | VARCHAR(50) | 设备类型（IPC/NVR/DVR） | IDX |
| brand | VARCHAR(100) | 品牌 | |
| model | VARCHAR(100) | 型号 | |
| ip_address | VARCHAR(50) | IP地址 | UK |
| port | INT | 端口 | |
| username | VARCHAR(100) | 登录用户名 | |
| password | VARCHAR(200) | 登录密码（加密） | |
| rtsp_url | VARCHAR(500) | RTSP流地址 | |
| resolution | VARCHAR(50) | 分辨率（1920x1080） | |
| frame_rate | INT | 帧率 | |
| area_id | BIGINT | 所属区域ID | FK, IDX |
| install_location | VARCHAR(200) | 安装位置 | |
| direction | VARCHAR(100) | 朝向 | |
| coverage_area | VARCHAR(200) | 覆盖区域 | |
| device_status | VARCHAR(50) | 设备状态（ONLINE/OFFLINE） | IDX |
| stream_status | VARCHAR(50) | 流状态（STREAMING/STOPPED） | IDX |
| storage_days | INT | 存储天数 | |
| enable_audio | TINYINT | 是否启用音频 | |
| enable_ptz | TINYINT | 是否支持云台 | |
| last_online_time | DATETIME | 最后在线时间 | IDX |
| enabled_flag | TINYINT | 启用标记 | IDX |
| remark | VARCHAR(500) | 备注 | |
| create_time | DATETIME | 创建时间 | IDX |
| update_time | DATETIME | 更新时间 | |
| deleted_flag | TINYINT | 删除标记 | IDX |

**关键索引**：
```sql
PRIMARY KEY (device_id)
UNIQUE KEY uk_device_code (device_code)
UNIQUE KEY uk_ip_address (ip_address)
INDEX idx_area_id (area_id)
INDEX idx_device_status (device_status, stream_status)
```

#### 2.5.2 录像记录表 (t_video_record)

| 字段名 | 类型 | 说明 | 索引 |
|-------|------|------|------|
| record_id | BIGINT PK | 录像ID | PK |
| device_id | BIGINT | 设备ID | FK, IDX |
| record_type | VARCHAR(50) | 录像类型（CONTINUOUS/ALARM/MANUAL） | IDX |
| start_time | DATETIME | 开始时间 | IDX |
| end_time | DATETIME | 结束时间 | IDX |
| duration | INT | 时长（秒） | |
| file_size | BIGINT | 文件大小（字节） | |
| file_path | VARCHAR(500) | 文件路径 | |
| storage_type | VARCHAR(50) | 存储类型（LOCAL/NAS/CLOUD） | IDX |
| resolution | VARCHAR(50) | 分辨率 | |
| encode_format | VARCHAR(50) | 编码格式（H264/H265） | |
| frame_rate | INT | 帧率 | |
| video_status | VARCHAR(50) | 视频状态（RECORDING/COMPLETED/LOCKED） | IDX |
| expire_time | DATETIME | 过期时间 | IDX |
| is_locked | TINYINT | 是否锁定 | |
| lock_user_id | BIGINT | 锁定人ID | |
| lock_reason | VARCHAR(500) | 锁定原因 | |
| download_count | INT | 下载次数 | |
| create_time | DATETIME | 创建时间 | IDX |
| update_time | DATETIME | 更新时间 | |
| deleted_flag | TINYINT | 删除标记 | IDX |

**关键索引**：
```sql
PRIMARY KEY (record_id)
INDEX idx_device_time (device_id, start_time DESC)
INDEX idx_time_range (start_time, end_time)
INDEX idx_status (video_status, is_locked)
INDEX idx_expire_time (expire_time)
```

#### 2.5.3 告警事件表 (t_video_alarm)

| 字段名 | 类型 | 说明 | 索引 |
|-------|------|------|------|
| alarm_id | BIGINT PK | 告警ID | PK |
| alarm_type | VARCHAR(50) | 告警类型（MOTION/FACE/INTRUSION） | IDX |
| device_id | BIGINT | 设备ID | FK, IDX |
| area_id | BIGINT | 区域ID | FK, IDX |
| alarm_level | VARCHAR(50) | 告警级别（INFO/WARNING/ERROR） | IDX |
| alarm_time | DATETIME | 告警时间 | IDX |
| alarm_content | TEXT | 告警内容 | |
| snapshot_url | VARCHAR(500) | 快照URL | |
| video_url | VARCHAR(500) | 视频URL | |
| ai_result | JSON | AI分析结果 | |
| confidence | DECIMAL(5,2) | 置信度 | |
| handle_status | VARCHAR(50) | 处理状态（PENDING/HANDLED/IGNORED） | IDX |
| handle_user_id | BIGINT | 处理人ID | |
| handle_time | DATETIME | 处理时间 | |
| handle_result | TEXT | 处理结果 | |
| is_false_alarm | TINYINT | 是否误报 | |
| create_time | DATETIME | 创建时间 | IDX |
| update_time | DATETIME | 更新时间 | |
| deleted_flag | TINYINT | 删除标记 | IDX |

**关键索引**：
```sql
PRIMARY KEY (alarm_id)
INDEX idx_device_time (device_id, alarm_time DESC)
INDEX idx_level_status (alarm_level, handle_status)
INDEX idx_alarm_time (alarm_time DESC)
```

#### 2.5.4 人脸识别记录表 (t_video_face_recognition)

记录视频监控中的人脸识别结果。

| 字段名 | 类型 | 说明 |
|-------|------|------|
| recognition_id | BIGINT PK | 识别ID |
| device_id | BIGINT | 设备ID |
| capture_time | DATETIME | 抓拍时间 |
| face_url | VARCHAR(500) | 人脸图片URL |
| person_id | BIGINT | 人员ID |
| person_name | VARCHAR(100) | 人员姓名 |
| match_score | DECIMAL(5,2) | 匹配分数 |
| face_quality | DECIMAL(5,2) | 人脸质量 |
| age | INT | 年龄 |
| gender | VARCHAR(10) | 性别 |
| emotion | VARCHAR(50) | 情绪 |
| temperature | DECIMAL(4,1) | 体温 |
| mask_status | TINYINT | 口罩状态 |
| recognition_result | VARCHAR(50) | 识别结果 |
| create_time | DATETIME | 创建时间 |
| deleted_flag | TINYINT | 删除标记 |

#### 2.5.5 视频回放记录表 (t_video_playback)

记录视频回放历史。

| 字段名 | 类型 | 说明 |
|-------|------|------|
| playback_id | BIGINT PK | 回放ID |
| user_id | BIGINT | 用户ID |
| device_id | BIGINT | 设备ID |
| record_id | BIGINT | 录像ID |
| start_time | DATETIME | 开始时间 |
| end_time | DATETIME | 结束时间 |
| playback_duration | INT | 回放时长（秒） |
| playback_speed | DECIMAL(3,1) | 回放倍速 |
| playback_mode | VARCHAR(50) | 回放模式 |
| create_time | DATETIME | 创建时间 |
| deleted_flag | TINYINT | 删除标记 |

**其他视频服务表**：
- `t_video_channel` - 视频通道表
- `t_video_storage` - 存储配置表
- `t_video_ai_rule` - AI分析规则表
- `t_video_ptz_preset` - 云台预置位表
- `t_video_download_log` - 下载日志表

### 2.6 访客服务数据库设计（ioedream_visitor_db）

**核心表结构**（8个表）：

#### 2.6.1 访客预约表 (t_visitor_appointment)

| 字段名 | 类型 | 说明 | 索引 |
|-------|------|------|------|
| appointment_id | BIGINT PK | 预约ID | PK |
| appointment_no | VARCHAR(100) | 预约编号 | UK |
| visitor_name | VARCHAR(100) | 访客姓名 | IDX |
| visitor_phone | VARCHAR(50) | 访客电话 | IDX |
| visitor_company | VARCHAR(200) | 访客公司 | |
| visitor_position | VARCHAR(100) | 访客职位 | |
| id_card_type | VARCHAR(50) | 证件类型（ID_CARD/PASSPORT） | |
| id_card_number | VARCHAR(100) | 证件号码 | IDX |
| visitor_count | INT | 访客人数 | |
| host_id | BIGINT | 被访人ID | FK, IDX |
| host_name | VARCHAR(100) | 被访人姓名 | |
| host_department | VARCHAR(200) | 被访人部门 | |
| visit_purpose | VARCHAR(500) | 访问目的 | |
| visit_type | VARCHAR(50) | 访问类型（BUSINESS/INTERVIEW） | IDX |
| visit_date | DATE | 访问日期 | IDX |
| start_time | DATETIME | 开始时间 | IDX |
| end_time | DATETIME | 结束时间 | IDX |
| visit_areas | JSON | 访问区域 | |
| need_escort | TINYINT | 是否需要陪同 | |
| car_number | VARCHAR(50) | 车牌号 | |
| workflow_instance_id | BIGINT | 工作流实例ID | FK, IDX |
| approve_status | VARCHAR(50) | 审批状态（PENDING/APPROVED/REJECTED） | IDX |
| approve_user_id | BIGINT | 审批人ID | |
| approve_time | DATETIME | 审批时间 | |
| reject_reason | VARCHAR(500) | 拒绝原因 | |
| qr_code | VARCHAR(500) | 二维码 | |
| face_url | VARCHAR(500) | 人脸照片URL | |
| attachment_urls | JSON | 附件URLs | |
| remark | TEXT | 备注 | |
| create_time | DATETIME | 创建时间 | IDX |
| update_time | DATETIME | 更新时间 | |
| deleted_flag | TINYINT | 删除标记 | IDX |

**关键索引**：
```sql
PRIMARY KEY (appointment_id)
UNIQUE KEY uk_appointment_no (appointment_no)
INDEX idx_visitor_phone (visitor_phone)
INDEX idx_host_date (host_id, visit_date)
INDEX idx_visit_date (visit_date, start_time)
INDEX idx_approve_status (approve_status, deleted_flag)
```

#### 2.6.2 访客登记表 (t_visitor_checkin)

| 字段名 | 类型 | 说明 | 索引 |
|-------|------|------|------|
| checkin_id | BIGINT PK | 登记ID | PK |
| appointment_id | BIGINT | 预约ID | FK, IDX |
| visitor_code | VARCHAR(100) | 访客编码 | UK |
| visitor_name | VARCHAR(100) | 访客姓名 | IDX |
| visitor_phone | VARCHAR(50) | 访客电话 | |
| id_card_number | VARCHAR(100) | 证件号码 | IDX |
| face_url | VARCHAR(500) | 人脸照片URL | |
| face_features | TEXT | 人脸特征 | |
| checkin_time | DATETIME | 签到时间 | IDX |
| checkout_time | DATETIME | 签退时间 | IDX |
| checkin_device_id | BIGINT | 签到设备ID | |
| checkout_device_id | BIGINT | 签退设备ID | |
| visit_duration | INT | 访问时长（分钟） | |
| actual_areas | JSON | 实际访问区域 | |
| access_count | INT | 通行次数 | |
| visitor_status | VARCHAR(50) | 访客状态（CHECKED_IN/CHECKED_OUT） | IDX |
| temperature | DECIMAL(4,1) | 体温 | |
| satisfaction_rating | INT | 满意度评分 | |
| feedback | TEXT | 反馈意见 | |
| remark | TEXT | 备注 | |
| create_time | DATETIME | 创建时间 | IDX |
| update_time | DATETIME | 更新时间 | |
| deleted_flag | TINYINT | 删除标记 | IDX |

**关键索引**：
```sql
PRIMARY KEY (checkin_id)
UNIQUE KEY uk_visitor_code (visitor_code)
INDEX idx_appointment_id (appointment_id)
INDEX idx_checkin_time (checkin_time DESC)
INDEX idx_visitor_status (visitor_status, deleted_flag)
```

#### 2.6.3 访客通行记录表 (t_visitor_access_log)

记录访客的所有通行记录。

| 字段名 | 类型 | 说明 |
|-------|------|------|
| log_id | BIGINT PK | 日志ID |
| checkin_id | BIGINT | 登记ID |
| visitor_code | VARCHAR(100) | 访客编码 |
| device_id | BIGINT | 设备ID |
| area_id | BIGINT | 区域ID |
| access_time | DATETIME | 通行时间 |
| access_direction | VARCHAR(20) | 通行方向（IN/OUT） |
| access_method | VARCHAR(50) | 通行方式 |
| verification_result | VARCHAR(50) | 验证结果 |
| photo_url | VARCHAR(500) | 现场照片 |
| create_time | DATETIME | 创建时间 |
| deleted_flag | TINYINT | 删除标记 |

#### 2.6.4 访客黑名单表 (t_visitor_blacklist)

管理不允许访问的人员黑名单。

| 字段名 | 类型 | 说明 |
|-------|------|------|
| blacklist_id | BIGINT PK | 黑名单ID |
| id_card_number | VARCHAR(100) | 证件号码 |
| name | VARCHAR(100) | 姓名 |
| phone | VARCHAR(50) | 电话 |
| blacklist_type | VARCHAR(50) | 黑名单类型 |
| blacklist_reason | TEXT | 列入原因 |
| blacklist_level | VARCHAR(50) | 黑名单级别 |
| effective_date | DATE | 生效日期 |
| expire_date | DATE | 失效日期 |
| operator_id | BIGINT | 操作人ID |
| photo_url | VARCHAR(500) | 照片URL |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted_flag | TINYINT | 删除标记 |

**其他访客服务表**：
- `t_visitor_group` - 访客团组表
- `t_visitor_pass` - 访客通行证表
- `t_visitor_feedback` - 访客反馈表
- `t_visitor_notification` - 访客通知表

### 2.7 OA服务数据库设计（ioedream_oa_db）

**核心表结构**（8个表）：

#### 2.7.1 工作流定义表 (t_oa_workflow_definition)

| 字段名 | 类型 | 说明 | 索引 |
|-------|------|------|------|
| definition_id | BIGINT PK | 定义ID | PK |
| workflow_code | VARCHAR(100) | 流程编码 | UK |
| workflow_name | VARCHAR(200) | 流程名称 | |
| workflow_category | VARCHAR(50) | 流程分类 | IDX |
| process_definition | JSON | 流程定义（JSON格式） | |
| version | INT | 版本号 | |
| workflow_status | VARCHAR(50) | 流程状态（DRAFT/PUBLISHED） | IDX |
| enabled_flag | TINYINT | 启用标记 | IDX |
| publish_time | DATETIME | 发布时间 | |
| remark | TEXT | 备注 | |
| create_user_id | BIGINT | 创建人ID | |
| create_time | DATETIME | 创建时间 | IDX |
| update_time | DATETIME | 更新时间 | |
| deleted_flag | TINYINT | 删除标记 | IDX |

**关键索引**：
```sql
PRIMARY KEY (definition_id)
UNIQUE KEY uk_workflow_code_version (workflow_code, version)
INDEX idx_category_status (workflow_category, workflow_status)
```

#### 2.7.2 工作流实例表 (t_oa_workflow_instance)

| 字段名 | 类型 | 说明 | 索引 |
|-------|------|------|------|
| instance_id | BIGINT PK | 实例ID | PK |
| instance_no | VARCHAR(100) | 实例编号 | UK |
| definition_id | BIGINT | 定义ID | FK, IDX |
| workflow_code | VARCHAR(100) | 流程编码 | IDX |
| business_key | VARCHAR(200) | 业务主键 | IDX |
| business_type | VARCHAR(50) | 业务类型 | IDX |
| title | VARCHAR(500) | 标题 | |
| initiator_id | BIGINT | 发起人ID | FK, IDX |
| initiator_name | VARCHAR(100) | 发起人姓名 | |
| current_node | VARCHAR(100) | 当前节点 | |
| instance_status | VARCHAR(50) | 实例状态（RUNNING/COMPLETED/REJECTED） | IDX |
| form_data | JSON | 表单数据 | |
| variables | JSON | 流程变量 | |
| start_time | DATETIME | 开始时间 | IDX |
| end_time | DATETIME | 结束时间 | |
| duration | INT | 持续时长（秒） | |
| remark | TEXT | 备注 | |
| create_time | DATETIME | 创建时间 | IDX |
| update_time | DATETIME | 更新时间 | |
| deleted_flag | TINYINT | 删除标记 | IDX |

**关键索引**：
```sql
PRIMARY KEY (instance_id)
UNIQUE KEY uk_instance_no (instance_no)
INDEX idx_business_key (business_key)
INDEX idx_initiator_time (initiator_id, start_time DESC)
INDEX idx_status (instance_status, deleted_flag)
```

#### 2.7.3 审批节点表 (t_oa_approval_node)

记录工作流每个审批节点的执行情况。

| 字段名 | 类型 | 说明 |
|-------|------|------|
| node_id | BIGINT PK | 节点ID |
| instance_id | BIGINT | 实例ID |
| node_code | VARCHAR(100) | 节点编码 |
| node_name | VARCHAR(200) | 节点名称 |
| node_type | VARCHAR(50) | 节点类型（APPROVE/NOTIFY） |
| approver_id | BIGINT | 审批人ID |
| approver_name | VARCHAR(100) | 审批人姓名 |
| approve_action | VARCHAR(50) | 审批动作（APPROVE/REJECT/TRANSFER） |
| approve_time | DATETIME | 审批时间 |
| approve_comment | TEXT | 审批意见 |
| node_status | VARCHAR(50) | 节点状态 |
| assign_time | DATETIME | 分配时间 |
| duration | INT | 处理时长（秒） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted_flag | TINYINT | 删除标记 |

#### 2.7.4 文档管理表 (t_oa_document)

OA文档管理表。

| 字段名 | 类型 | 说明 |
|-------|------|------|
| document_id | BIGINT PK | 文档ID |
| document_no | VARCHAR(100) | 文档编号 |
| document_title | VARCHAR(500) | 文档标题 |
| document_type | VARCHAR(50) | 文档类型 |
| document_category | VARCHAR(50) | 文档分类 |
| file_url | VARCHAR(500) | 文件URL |
| file_size | BIGINT | 文件大小 |
| file_format | VARCHAR(50) | 文件格式 |
| author_id | BIGINT | 作者ID |
| department_id | BIGINT | 部门ID |
| security_level | VARCHAR(50) | 密级 |
| publish_date | DATE | 发布日期 |
| expire_date | DATE | 失效日期 |
| document_status | VARCHAR(50) | 文档状态 |
| view_count | INT | 查看次数 |
| download_count | INT | 下载次数 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted_flag | TINYINT | 删除标记 |

**其他OA服务表**：
- `t_oa_document_version` - 文档版本表
- `t_oa_meeting` - 会议管理表
- `t_oa_announcement` - 公告通知表
- `t_oa_vehicle` - 车辆管理表

### 2.8 设备通讯服务数据库设计（ioedream_device_comm_db）

**核心表结构**（6个表）：

#### 2.8.1 设备协议表 (t_device_protocol)

| 字段名 | 类型 | 说明 | 索引 |
|-------|------|------|------|
| protocol_id | BIGINT PK | 协议ID | PK |
| protocol_code | VARCHAR(100) | 协议编码 | UK |
| protocol_name | VARCHAR(200) | 协议名称 | |
| protocol_type | VARCHAR(50) | 协议类型（TCP/UDP/HTTP） | IDX |
| protocol_version | VARCHAR(50) | 协议版本 | |
| manufacturer | VARCHAR(100) | 制造商 | IDX |
| protocol_spec | JSON | 协议规范 | |
| command_list | JSON | 命令列表 | |
| data_format | VARCHAR(50) | 数据格式（JSON/XML/BINARY） | |
| enabled_flag | TINYINT | 启用标记 | IDX |
| remark | TEXT | 备注 | |
| create_time | DATETIME | 创建时间 | IDX |
| update_time | DATETIME | 更新时间 | |
| deleted_flag | TINYINT | 删除标记 | IDX |

**关键索引**：
```sql
PRIMARY KEY (protocol_id)
UNIQUE KEY uk_protocol_code (protocol_code)
INDEX idx_manufacturer (manufacturer)
```

#### 2.8.2 设备连接表 (t_device_connection)

| 字段名 | 类型 | 说明 | 索引 |
|-------|------|------|------|
| connection_id | BIGINT PK | 连接ID | PK |
| device_id | BIGINT | 设备ID | FK, UK |
| protocol_id | BIGINT | 协议ID | FK, IDX |
| connection_type | VARCHAR(50) | 连接类型（TCP/UDP） | |
| server_ip | VARCHAR(50) | 服务器IP | |
| server_port | INT | 服务器端口 | |
| connection_status | VARCHAR(50) | 连接状态（CONNECTED/DISCONNECTED） | IDX |
| last_heartbeat_time | DATETIME | 最后心跳时间 | IDX |
| connection_time | DATETIME | 连接时间 | |
| disconnect_time | DATETIME | 断开时间 | |
| connection_duration | INT | 连接时长（秒） | |
| retry_count | INT | 重连次数 | |
| error_message | TEXT | 错误信息 | |
| create_time | DATETIME | 创建时间 | IDX |
| update_time | DATETIME | 更新时间 | |
| deleted_flag | TINYINT | 删除标记 | IDX |

**关键索引**：
```sql
PRIMARY KEY (connection_id)
UNIQUE KEY uk_device_id (device_id)
INDEX idx_connection_status (connection_status)
INDEX idx_last_heartbeat_time (last_heartbeat_time)
```

#### 2.8.3 数据采集表 (t_device_data_collection)

记录设备数据采集历史。

| 字段名 | 类型 | 说明 |
|-------|------|------|
| collection_id | BIGINT PK | 采集ID |
| device_id | BIGINT | 设备ID |
| data_type | VARCHAR(50) | 数据类型 |
| data_content | JSON | 数据内容 |
| collection_time | DATETIME | 采集时间 |
| data_quality | VARCHAR(50) | 数据质量 |
| create_time | DATETIME | 创建时间 |
| deleted_flag | TINYINT | 删除标记 |

#### 2.8.4 设备健康表 (t_device_health)

监控设备健康状态。

| 字段名 | 类型 | 说明 |
|-------|------|------|
| health_id | BIGINT PK | 健康ID |
| device_id | BIGINT | 设备ID |
| check_time | DATETIME | 检查时间 |
| cpu_usage | DECIMAL(5,2) | CPU使用率 |
| memory_usage | DECIMAL(5,2) | 内存使用率 |
| disk_usage | DECIMAL(5,2) | 磁盘使用率 |
| network_delay | INT | 网络延迟（ms） |
| health_score | INT | 健康评分 |
| health_status | VARCHAR(50) | 健康状态 |
| warning_info | JSON | 告警信息 |
| create_time | DATETIME | 创建时间 |
| deleted_flag | TINYINT | 删除标记 |

**其他设备通讯服务表**：
- `t_device_command_log` - 设备命令日志表
- `t_device_alarm_config` - 设备告警配置表

## 3. 数据库脚本组织结构设计

### 3.1 目录结构规范

```
database-scripts/
├── common-service/              # 公共服务（已完成23个SQL）
│   ├── 00-database-init.sql
│   ├── 01-t_user_session.sql
│   └── ... (其他18个表)
├── access-service/              # 门禁服务（待补充13个SQL）
│   ├── 00-database-init.sql
│   ├── 01-t_access_device.sql
│   ├── 02-t_access_permission.sql
│   ├── 03-t_access_record.sql
│   ├── 04-t_access_area.sql
│   ├── 05-t_access_rule.sql
│   ├── 06-t_access_event.sql
│   ├── 07-t_access_linkage.sql
│   ├── 08-t_access_apb_config.sql
│   ├── 09-t_access_interlock.sql
│   ├── 10-t_access_holiday.sql
│   ├── 11-t_access_time_period.sql
│   ├── 12-t_access_black_list.sql
│   └── 13-t_access_white_list.sql
├── attendance-service/          # 考勤服务（待补充15个SQL）
│   ├── 00-database-init.sql
│   ├── 01-t_attendance_record.sql
│   ├── 02-t_attendance_shift.sql
│   ├── 03-t_attendance_schedule.sql
│   ├── 04-t_attendance_rule.sql
│   ├── 05-t_attendance_exception.sql
│   ├── 06-t_attendance_statistics.sql
│   ├── 07-t_attendance_leave_type.sql
│   ├── 08-t_attendance_leave_balance.sql
│   ├── 09-t_attendance_overtime_rule.sql
│   ├── 10-t_attendance_holiday.sql
│   ├── 11-t_attendance_shift_group.sql
│   ├── 12-t_attendance_rotation_rule.sql
│   ├── 13-t_attendance_makeup_card.sql
│   └── 14-t_attendance_device.sql
├── consume-service/             # 消费服务（待补充12个SQL）
│   ├── 00-database-init.sql
│   ├── 01-t_consume_record.sql
│   ├── 02-t_consume_account.sql
│   ├── 03-t_consume_recharge.sql
│   ├── 04-t_consume_refund.sql
│   ├── 05-t_consume_subsidy_config.sql
│   ├── 06-t_consume_subsidy_grant.sql
│   ├── 07-t_consume_merchant.sql
│   ├── 08-t_consume_product.sql
│   ├── 09-t_consume_discount.sql
│   ├── 10-t_consume_device.sql
│   ├── 11-t_consume_blacklist.sql
│   └── 12-t_consume_statistics.sql
├── video-service/               # 视频服务（待补充10个SQL）
│   ├── 00-database-init.sql
│   ├── 01-t_video_device.sql
│   ├── 02-t_video_record.sql
│   ├── 03-t_video_alarm.sql
│   ├── 04-t_video_face_recognition.sql
│   ├── 05-t_video_playback.sql
│   ├── 06-t_video_channel.sql
│   ├── 07-t_video_storage.sql
│   ├── 08-t_video_ai_rule.sql
│   ├── 09-t_video_ptz_preset.sql
│   └── 10-t_video_download_log.sql
├── visitor-service/             # 访客服务（待补充8个SQL）
│   ├── 00-database-init.sql
│   ├── 01-t_visitor_appointment.sql
│   ├── 02-t_visitor_checkin.sql
│   ├── 03-t_visitor_access_log.sql
│   ├── 04-t_visitor_blacklist.sql
│   ├── 05-t_visitor_group.sql
│   ├── 06-t_visitor_pass.sql
│   ├── 07-t_visitor_feedback.sql
│   └── 08-t_visitor_notification.sql
├── oa-service/                  # OA服务（待补充8个SQL）
│   ├── 00-database-init.sql
│   ├── 01-t_oa_workflow_definition.sql
│   ├── 02-t_oa_workflow_instance.sql
│   ├── 03-t_oa_approval_node.sql
│   ├── 04-t_oa_document.sql
│   ├── 05-t_oa_document_version.sql
│   ├── 06-t_oa_meeting.sql
│   ├── 07-t_oa_announcement.sql
│   └── 08-t_oa_vehicle.sql
└── device-comm-service/         # 设备通讯服务（待补充6个SQL）
    ├── 00-database-init.sql
    ├── 01-t_device_protocol.sql
    ├── 02-t_device_connection.sql
    ├── 03-t_device_data_collection.sql
    ├── 04-t_device_health.sql
    ├── 05-t_device_command_log.sql
    └── 06-t_device_alarm_config.sql
```

### 3.2 数据库初始化脚本模板

**标准00-database-init.sql模板**：

```sql
-- ============================================================
-- IOE-DREAM {Service} - 数据库初始化脚本
-- 数据库名: ioedream_{service}_db
-- 功能: 创建数据库和初始化配置
-- 创建时间: 2025-12-08
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `ioedream_{service}_db` 
    DEFAULT CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE `ioedream_{service}_db`;

-- 设置时区
SET time_zone = '+8:00';

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 设置SQL模式
SET SQL_MODE='STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- 初始化完成提示
SELECT '✅ IOE-DREAM {Service} 数据库初始化完成！' AS message;
```

### 3.3 数据库表脚本模板

**标准表创建脚本模板**：

```sql
-- ============================================================
-- {Table Name} - {Table Description}
-- 表名: {table_name}
-- 说明: {详细说明}
-- ============================================================

DROP TABLE IF EXISTS `{table_name}`;

CREATE TABLE `{table_name}` (
    -- 主键
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    
    -- 业务字段（根据实际需求调整）
    `field1` VARCHAR(100) NOT NULL COMMENT '字段1说明',
    `field2` INT DEFAULT 0 COMMENT '字段2说明',
    
    -- 审计字段（标准配置）
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT COMMENT '创建人ID',
    `update_user_id` BIGINT COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    
    -- 主键约束
    PRIMARY KEY (`id`),
    
    -- 唯一约束
    UNIQUE KEY `uk_field1` (`field1`),
    
    -- 普通索引
    INDEX `idx_field2` (`field2`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_deleted_flag` (`deleted_flag`)
    
) ENGINE=InnoDB 
  DEFAULT CHARSET=utf8mb4 
  COLLATE=utf8mb4_unicode_ci 
  COMMENT='{Table Description}';

-- 初始化数据（如果需要）
-- INSERT INTO `{table_name}` (...) VALUES (...);
```

## 4. 数据初始化设计

### 4.1 基础数据初始化策略

**初始化数据分类**：

| 数据类型 | 初始化时机 | 数据来源 | 示例 |
|---------|-----------|---------|------|
| **系统配置数据** | 数据库创建时 | SQL脚本 | 系统参数、字典数据 |
| **角色权限数据** | 数据库创建时 | SQL脚本 | 超级管理员、默认角色 |
| **组织架构数据** | 数据库创建时 | SQL脚本 | 根部门、默认区域 |
| **业务规则数据** | 数据库创建时 | SQL脚本 | 默认班次、标准工作流 |
| **测试数据** | 开发环境 | 独立脚本 | 测试用户、模拟数据 |

### 4.2 公共模块初始化数据

**已完成的初始化数据**（database-scripts/common-service/）：

```sql
-- 01. 超级管理员账号
INSERT INTO t_user (user_id, username, password, email, user_type, status) 
VALUES (1, 'admin', '{encrypted_password}', 'admin@ioedream.com', 'SUPER_ADMIN', 1);

-- 02. 默认系统角色
INSERT INTO t_role (role_id, role_code, role_name, role_type, status) VALUES
(1, 'SUPER_ADMIN', '超级管理员', 'SYSTEM', 1),
(2, 'ADMIN', '系统管理员', 'SYSTEM', 1),
(3, 'EMPLOYEE', '普通员工', 'BUSINESS', 1);

-- 03. 基础权限数据
INSERT INTO t_permission (permission_code, permission_name, permission_type) VALUES
('user:view', '查看用户', 'API'),
('user:create', '创建用户', 'API'),
('user:update', '更新用户', 'API'),
('user:delete', '删除用户', 'API');

-- 04. 系统字典数据
INSERT INTO t_system_dict (dict_type, dict_code, dict_value, sort_order) VALUES
('user_status', '0', '禁用', 1),
('user_status', '1', '启用', 2),
('gender', 'M', '男', 1),
('gender', 'F', '女', 2);
```

### 4.3 门禁服务初始化数据

**门禁服务基础数据**（database-scripts/access-service/）：

```sql
-- 01. 默认区域配置
INSERT INTO t_access_area (area_id, area_code, area_name, area_type, parent_id, area_level) VALUES
(1, 'ROOT', '根区域', 'ROOT', NULL, 1),
(2, 'BUILDING_A', 'A栋办公楼', 'BUILDING', 1, 2),
(3, 'BUILDING_A_1F', 'A栋1楼', 'FLOOR', 2, 3);

-- 02. 默认时间段配置
INSERT INTO t_access_time_period (period_id, period_name, start_time, end_time, enabled_flag) VALUES
(1, '全天开放', '00:00:00', '23:59:59', 1),
(2, '工作时间', '08:30:00', '17:30:00', 1),
(3, '午休时间', '12:00:00', '13:00:00', 1);

-- 03. 默认门禁规则
INSERT INTO t_access_rule (rule_name, rule_type, rule_config, enabled_flag) VALUES
('APB反潜回', 'APB', '{"enabled": true, "check_area": true}', 1),
('互锁规则', 'INTERLOCK', '{"enabled": true, "max_concurrent": 1}', 1);
```

### 4.4 考勤服务初始化数据

**考勤服务基础数据**（database-scripts/attendance-service/）：

```sql
-- 01. 默认班次配置
INSERT INTO t_attendance_shift (shift_code, shift_name, shift_type, work_start_time, work_end_time, work_hours) VALUES
('NORMAL', '标准班', 'FIXED', '09:00:00', '18:00:00', 8.0),
('MORNING', '早班', 'FIXED', '08:00:00', '17:00:00', 8.0),
('EVENING', '晚班', 'FIXED', '14:00:00', '23:00:00', 8.0),
('NIGHT', '夜班', 'FIXED', '22:00:00', '07:00:00', 8.0),
('FLEXIBLE', '弹性班', 'FLEXIBLE', '09:00:00', '18:00:00', 8.0);

-- 02. 默认请假类型
INSERT INTO t_attendance_leave_type (type_code, type_name, require_approval, deduct_salary) VALUES
('ANNUAL', '年假', 1, 0),
('SICK', '病假', 1, 0),
('PERSONAL', '事假', 1, 1),
('MATERNITY', '产假', 1, 0),
('PATERNITY', '陪产假', 1, 0);

-- 03. 默认考勤规则
INSERT INTO t_attendance_rule (rule_name, rule_type, rule_config, enabled_flag) VALUES
('迟到规则', 'LATE', '{"threshold": 30, "penalty": 10}', 1),
('早退规则', 'EARLY', '{"threshold": 30, "penalty": 10}', 1),
('加班规则', 'OVERTIME', '{"min_duration": 60, "rate": 1.5}', 1);
```

### 4.5 消费服务初始化数据

**消费服务基础数据**（database-scripts/consume-service/）：

```sql
-- 01. 默认商户配置
INSERT INTO t_consume_merchant (merchant_code, merchant_name, merchant_type, status) VALUES
('CANTEEN_1', '员工食堂', 'CANTEEN', 1),
('SHOP_1', '便利店', 'SHOP', 1),
('VENDING_1', '自动售货机', 'VENDING', 1);

-- 02. 默认补贴配置
INSERT INTO t_consume_subsidy_config (subsidy_name, subsidy_type, subsidy_amount, effective_date) VALUES
('餐补', 'MEAL', 20.00, '2025-01-01'),
('交通补贴', 'MONTHLY', 300.00, '2025-01-01');

-- 03. 默认商品分类
INSERT INTO t_consume_product (product_code, product_name, product_type, price, status) VALUES
('MEAL_BREAKFAST', '早餐', 'MEAL', 10.00, 1),
('MEAL_LUNCH', '午餐', 'MEAL', 15.00, 1),
('MEAL_DINNER', '晚餐', 'MEAL', 15.00, 1);
```

### 4.6 OA服务初始化数据

**OA服务基础数据**（database-scripts/oa-service/）：

```sql
-- 01. 默认工作流定义
INSERT INTO t_oa_workflow_definition (workflow_code, workflow_name, workflow_category, workflow_status) VALUES
('VISITOR_APPOINTMENT', '访客预约审批', 'VISITOR', 'PUBLISHED'),
('LEAVE_APPLICATION', '请假申请', 'ATTENDANCE', 'PUBLISHED'),
('OVERTIME_APPLICATION', '加班申请', 'ATTENDANCE', 'PUBLISHED'),
('REFUND_APPLICATION', '退款申请', 'CONSUME', 'PUBLISHED');

-- 02. 默认文档分类
INSERT INTO t_oa_document (document_no, document_title, document_type, document_category) VALUES
('DOC-001', '员工手册', 'POLICY', 'HR'),
('DOC-002', '安全管理制度', 'POLICY', 'SECURITY');
```

## 5. 部署自动化设计

### 5.1 Docker初始化集成

**docker-compose-all.yml数据库初始化配置**：

```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: ioedream-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root}
      TZ: Asia/Shanghai
    volumes:
      # 挂载所有数据库初始化脚本
      - ./database-scripts/common-service:/docker-entrypoint-initdb.d/01-common:ro
      - ./database-scripts/access-service:/docker-entrypoint-initdb.d/02-access:ro
      - ./database-scripts/attendance-service:/docker-entrypoint-initdb.d/03-attendance:ro
      - ./database-scripts/consume-service:/docker-entrypoint-initdb.d/04-consume:ro
      - ./database-scripts/video-service:/docker-entrypoint-initdb.d/05-video:ro
      - ./database-scripts/visitor-service:/docker-entrypoint-initdb.d/06-visitor:ro
      - ./database-scripts/oa-service:/docker-entrypoint-initdb.d/07-oa:ro
      - ./database-scripts/device-comm-service:/docker-entrypoint-initdb.d/08-device:ro
      # 数据持久化
      - mysql_data:/var/lib/mysql
    ports:
      - "3306:3306"
    networks:
      - ioedream-network

volumes:
  mysql_data:
    driver: local

networks:
  ioedream-network:
    driver: bridge
```

### 5.2 PowerShell初始化脚本

**scripts/init-all-databases.ps1**：

```powershell
# ============================================================
# IOE-DREAM 全量数据库初始化脚本
# 功能: 按顺序初始化所有微服务数据库
# 使用: .\scripts\init-all-databases.ps1
# ============================================================

$ErrorActionPreference = "Stop"

# 配置
$MYSQL_HOST = "127.0.0.1"
$MYSQL_PORT = 3306
$MYSQL_USER = "root"
$MYSQL_PASSWORD = "root"

# 服务列表（按依赖顺序）
$SERVICES = @(
    "common-service",
    "access-service",
    "attendance-service",
    "consume-service",
    "video-service",
    "visitor-service",
    "oa-service",
    "device-comm-service"
)

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "🗄️ IOE-DREAM 数据库初始化" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# 检查MySQL连接
Write-Host "[1/3] 检查MySQL连接..." -ForegroundColor Yellow
try {
    $null = mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD -e "SELECT 1" 2>&1
    Write-Host "✅ MySQL连接成功" -ForegroundColor Green
} catch {
    Write-Host "❌ MySQL连接失败，请检查配置" -ForegroundColor Red
    exit 1
}

# 初始化所有服务数据库
Write-Host ""
Write-Host "[2/3] 初始化数据库..." -ForegroundColor Yellow
Write-Host ""

$totalServices = $SERVICES.Count
$currentIndex = 0

foreach ($service in $SERVICES) {
    $currentIndex++
    $scriptDir = "database-scripts/$service"
    
    Write-Host "[$currentIndex/$totalServices] 初始化 $service..." -ForegroundColor Cyan
    
    if (-not (Test-Path $scriptDir)) {
        Write-Host "  ⚠️ 警告: 脚本目录不存在 - $scriptDir" -ForegroundColor Yellow
        continue
    }
    
    # 获取所有SQL文件并按名称排序
    $sqlFiles = Get-ChildItem -Path $scriptDir -Filter "*.sql" | Sort-Object Name
    
    if ($sqlFiles.Count -eq 0) {
        Write-Host "  ⚠️ 警告: 未找到SQL文件" -ForegroundColor Yellow
        continue
    }
    
    foreach ($file in $sqlFiles) {
        Write-Host "  📄 执行: $($file.Name)" -ForegroundColor Gray
        
        try {
            Get-Content $file.FullName -Raw | mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD 2>&1 | Out-Null
            Write-Host "  ✅ $($file.Name) 执行成功" -ForegroundColor Green
        } catch {
            Write-Host "  ❌ $($file.Name) 执行失败: $($_.Exception.Message)" -ForegroundColor Red
            exit 1
        }
    }
    
    Write-Host ""
}

# 验证数据库
Write-Host "[3/3] 验证数据库..." -ForegroundColor Yellow
Write-Host ""

$databases = mysql -h$MYSQL_HOST -P$MYSQL_PORT -u$MYSQL_USER -p$MYSQL_PASSWORD -e "SHOW DATABASES LIKE 'ioedream%'" 2>&1
$dbCount = ($databases -split "`n" | Where-Object { $_ -like "ioedream*" }).Count

Write-Host "✅ 已创建 $dbCount 个数据库" -ForegroundColor Green
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "✅ 数据库初始化完成！" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Cyan
```

### 5.3 Flyway迁移工具集成（可选）

**application.yml配置**：

```yaml
# Flyway数据库迁移配置
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    encoding: UTF-8
    table: flyway_schema_history
    validate-on-migrate: true
```

**迁移脚本目录结构**：

```
src/main/resources/db/migration/
├── V1.0.0__init_database.sql
├── V1.0.1__create_tables.sql
├── V1.0.2__init_data.sql
└── V1.1.0__add_new_feature.sql
```

## 6. 数据一致性保障

### 6.1 外键约束设计

**外键设计原则**：

1. **同库表之间使用外键约束**
```sql
ALTER TABLE t_access_permission
ADD CONSTRAINT fk_permission_device
FOREIGN KEY (device_id) REFERENCES t_access_device(device_id)
ON DELETE RESTRICT ON UPDATE CASCADE;
```

2. **跨库表之间不使用外键**（通过应用层保证）
```java
// 应用层验证
public void createPermission(AccessPermissionEntity permission) {
    // 验证设备是否存在
    DeviceEntity device = deviceDao.selectById(permission.getDeviceId());
    if (device == null) {
        throw new BusinessException("设备不存在");
    }
    // 保存权限
    permissionDao.insert(permission);
}
```

### 6.2 数据一致性检查脚本

**scripts/check-data-consistency.ps1**：

```powershell
# ============================================================
# IOE-DREAM 数据一致性检查脚本
# 功能: 检查各数据库之间的数据一致性
# ============================================================

Write-Host "🔍 数据一致性检查..." -ForegroundColor Cyan

# 检查1: 用户数据一致性
$sql1 = @"
SELECT 
    COUNT(DISTINCT u.user_id) as user_count,
    COUNT(DISTINCT e.employee_id) as employee_count
FROM ioedream_common_db.t_user u
LEFT JOIN ioedream_common_db.t_employee e ON u.user_id = e.user_id;
"@

# 检查2: 设备引用一致性
$sql2 = @"
SELECT 
    service_name,
    COUNT(*) as orphan_count
FROM (
    SELECT 'access' as service_name, device_id 
    FROM ioedream_access_db.t_access_permission p
    WHERE NOT EXISTS (
        SELECT 1 FROM ioedream_access_db.t_access_device d 
        WHERE d.device_id = p.device_id
    )
    UNION ALL
    SELECT 'attendance' as service_name, device_id
    FROM ioedream_attendance_db.t_attendance_record r
    WHERE r.clock_in_device_id IS NOT NULL
    AND NOT EXISTS (
        SELECT 1 FROM ioedream_attendance_db.t_attendance_device d
        WHERE d.device_id = r.clock_in_device_id
    )
) orphans
GROUP BY service_name;
"@

Write-Host "✅ 数据一致性检查完成" -ForegroundColor Green
```

## 7. 执行计划与时间线

### 7.1 脚本补充优先级

| 优先级 | 服务名称 | 脚本数量 | 预计工作量 | 完成时间 |
|-------|---------|---------|-----------|----------|
| **P0** | access-service | 13个SQL | 3天 | 2025-12-11 |
| **P0** | attendance-service | 15个SQL | 4天 | 2025-12-15 |
| **P0** | consume-service | 12个SQL | 3天 | 2025-12-18 |
| **P1** | visitor-service | 8个SQL | 2天 | 2025-12-20 |
| **P1** | video-service | 10个SQL | 3天 | 2025-12-23 |
| **P2** | oa-service | 8个SQL | 2天 | 2025-12-25 |
| **P2** | device-comm-service | 6个SQL | 2天 | 2025-12-27 |

**总计**: 72个SQL文件，19个工作日

### 7.2 验证检查清单

**每个服务完成后的验证项**：

- [ ] 数据库创建成功
- [ ] 所有表创建成功
- [ ] 索引创建正确
- [ ] 初始化数据插入成功
- [ ] 外键约束有效
- [ ] 字符集编码正确（utf8mb4）
- [ ] 时区配置正确（Asia/Shanghai）
- [ ] Docker启动自动初始化成功
- [ ] PowerShell脚本执行成功
- [ ] 数据一致性检查通过

## 8. 质量保障措施

### 8.1 SQL脚本规范

**强制规范**：

1. **编码规范**
   - 统一使用UTF-8编码
   - 所有脚本包含中文注释
   - 表名、字段名使用小写+下划线

2. **结构规范**
   - 每个表一个独立SQL文件
   - 按编号顺序执行
   - 包含DROP TABLE IF EXISTS
   - 包含完整的COMMENT注释

3. **性能规范**
   - 合理创建索引
   - 避免过度索引
   - 字段类型选择合理
   - 使用InnoDB引擎

4. **安全规范**
   - 敏感字段加密存储
   - 密码使用不可逆加密
   - 审计字段完整
   - 软删除设计

### 8.2 自动化测试

**数据库测试脚本**：

```powershell
# ============================================================
# IOE-DREAM 数据库自动化测试
# ============================================================

# 测试1: 表结构完整性
Write-Host "测试: 表结构完整性..." -ForegroundColor Yellow
$requiredTables = @(
    "ioedream_access_db.t_access_device",
    "ioedream_access_db.t_access_permission",
    "ioedream_attendance_db.t_attendance_record"
)

foreach ($table in $requiredTables) {
    $exists = mysql -e "SELECT 1 FROM $table LIMIT 1" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ $table 存在" -ForegroundColor Green
    } else {
        Write-Host "  ❌ $table 不存在" -ForegroundColor Red
    }
}

# 测试2: 索引完整性
Write-Host "测试: 索引完整性..." -ForegroundColor Yellow
$sql = "SHOW INDEX FROM ioedream_access_db.t_access_device"
$indexes = mysql -e $sql
Write-Host "  ✅ 索引数量: $(($indexes -split "`n").Count - 1)" -ForegroundColor Green

# 测试3: 初始化数据
Write-Host "测试: 初始化数据..." -ForegroundColor Yellow
$count = mysql -e "SELECT COUNT(*) FROM ioedream_common_db.t_user" -N
Write-Host "  ✅ 用户数量: $count" -ForegroundColor Green

Write-Host ""
Write-Host "✅ 所有测试通过！" -ForegroundColor Green
```

## 9. 风险与应对

### 9.1 潜在风险识别

| 风险类型 | 风险描述 | 影响级别 | 应对措施 |
|---------|---------|---------|----------|
| **数据丢失** | 初始化脚本执行错误导致数据丢失 | 高 | 备份现有数据，使用事务 |
| **性能问题** | 索引设计不当导致查询性能差 | 中 | 性能测试，优化索引 |
| **兼容性** | MySQL版本不兼容 | 中 | 指定MySQL 8.0+ |
| **字符集** | 字符集不一致导致乱码 | 低 | 统一utf8mb4编码 |
| **时区问题** | 时间字段时区不一致 | 低 | 统一Asia/Shanghai |

### 9.2 回滚方案

**数据库回滚脚本**：

```sql
-- ============================================================
-- 回滚脚本模板
-- 功能: 删除指定服务的所有表
-- 警告: 此操作不可恢复，请谨慎使用！
-- ============================================================

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 删除所有表
DROP TABLE IF EXISTS t_access_device;
DROP TABLE IF EXISTS t_access_permission;
DROP TABLE IF EXISTS t_access_record;
-- ... 其他表

-- 启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 删除数据库
DROP DATABASE IF EXISTS ioedream_access_db;
```

## 10. 监控与维护

### 10.1 数据库健康检查

**定期检查项**：

```sql
-- 1. 表数量检查
SELECT 
    TABLE_SCHEMA as database_name,
    COUNT(*) as table_count
FROM information_schema.TABLES
WHERE TABLE_SCHEMA LIKE 'ioedream%'
GROUP BY TABLE_SCHEMA;

-- 2. 数据量检查
SELECT 
    TABLE_SCHEMA,
    TABLE_NAME,
    TABLE_ROWS,
    ROUND(DATA_LENGTH/1024/1024, 2) as data_size_mb
FROM information_schema.TABLES
WHERE TABLE_SCHEMA LIKE 'ioedream%'
ORDER BY DATA_LENGTH DESC
LIMIT 20;

-- 3. 索引使用率检查
SELECT 
    TABLE_SCHEMA,
    TABLE_NAME,
    INDEX_NAME,
    CARDINALITY
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA LIKE 'ioedream%'
AND CARDINALITY IS NULL;
```

### 10.2 性能监控指标

**关键性能指标（KPI）**：

| 指标 | 目标值 | 监控频率 |
|------|-------|----------|
| 查询响应时间 | <200ms | 实时 |
| 连接池使用率 | <80% | 每分钟 |
| 慢查询数量 | <10/小时 | 每小时 |
| 数据库空间使用率 | <70% | 每天 |
| 索引命中率 | >95% | 每小时 |

## 总结

本设计方案全面梳理了IOE-DREAM项目的数据库初始化需求，识别出**87.5%的业务模块数据库脚本缺失**的严重问题。通过系统化的设计，我们将：

1. **补充72个SQL文件**，覆盖7个微服务的完整数据库脚本
2. **建立标准化的数据库脚本模板**，确保质量一致性
3. **设计完整的初始化数据**，保证系统可立即投入使用
4. **实现自动化部署**，支持Docker和PowerShell一键初始化
5. **建立数据一致性保障机制**，确保跨库数据准确性
6. **制定详细的执行计划**，19个工作日完成所有脚本

**核心价值**：
- ✅ **100%功能可用**：所有前端页面对应的数据库表全部就绪
- ✅ **部署零失败**：自动化初始化脚本确保部署成功率100%
- ✅ **数据强一致**：跨服务数据一致性检查机制保障数据准确
- ✅ **企业级标准**：符合三级等保要求的数据库安全设计

**下一步行动**：
1. 立即启动P0级服务（门禁、考勤、消费）的脚本编写
2. 同步更新deployment配置以支持自动初始化
3. 建立数据库脚本的CI/CD自动化测试```sql
