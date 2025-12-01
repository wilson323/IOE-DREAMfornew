# SmartAdmin 数据字典

> 版本: v1.2.0
> 责任人: 架构组 × 数据治理组
> 最后更新: 2025-11-17
> 更新内容: 完善RAC权限中间件数据字典，新增权限策略、缓存、审计等表结构

## 1. 命名规范
- **数据库**: 使用 `t_{domain}_{entity}` 小写下划线; 主键命名 `{entity}_id`。
- **后端字段**: Java 实体采用 `camelCase`，与数据库字段通过 MyBatis-Plus 自动映射。
- **前端字段**: 保持 `camelCase`，与接口返回值一致，禁止随意更名。
- **时间字段**: `createTime`, `updateTime` 对应 DB 字段 `create_time`, `update_time`。
- **布尔字段**: 数据库使用 `tinyint(1)`，字段命名 `is_xxx`，前端使用 `boolean`。

## 2. 核心表与字段

### 2.1 权限域
| 表名 | 字段 | 类型 | 前端字段 | 说明 |
| --- | --- | --- | --- | --- |
| `t_sys_role` | `role_id` | BIGINT | `roleId` | 角色主键 |
| | `role_code` | VARCHAR(64) | `roleCode` | 唯一角色编码 |
| | `role_name` | VARCHAR(128) | `roleName` | 角色名称 |
| | `data_scope` | VARCHAR(32) | `dataScope` | 数据权限范围 |
| `t_sys_role_menu` | `role_id` | BIGINT | `roleId` | 角色ID |
| | `menu_id` | BIGINT | `menuId` | 菜单ID |
| `t_sys_role_resource` | `resource_code` | VARCHAR(128) | `resourceCode` | 接口权限码 |

### 2.2 设备域
| 表名 | 字段 | 类型 | 前端字段 | 说明 |
| --- | --- | --- | --- | --- |
| `t_device_info` | `device_id` | BIGINT | `deviceId` | 设备唯一ID |
| | `device_sn` | VARCHAR(64) | `deviceSn` | 序列号 |
| | `device_type` | VARCHAR(32) | `deviceType` | 枚举: ACCESS/POS/CAMERA |
| | `online_status` | TINYINT | `onlineStatus` | 0离线/1在线/2故障 |
| | `firmware_version` | VARCHAR(32) | `firmwareVersion` | 固件版本 |
| `t_device_config` | `config_json` | JSON | `configJson` | 设备配置内容 |

### 2.3 区域域（统一模型）
| 表名 | 字段 | 类型 | 前端字段 | 说明 |
| --- | --- | --- | --- | --- |
| `t_area_info` | `area_id` | BIGINT | `areaId` | 区域主键 |
| | `area_code` | VARCHAR(32) | `areaCode` | 区域编码（系统唯一） |
| | `area_name` | VARCHAR(100) | `areaName` | 区域名称 |
| | `area_type` | TINYINT | `areaType` | 区域类型 1园区 2建筑 3楼层 4房间 5区域 6其他 |
| | `parent_id` | BIGINT | `parentId` | 上级区域ID（0表示根区域） |
| | `path` | VARCHAR(1024) | `path` | 层级路径 /1/3/9 |
| | `level` | INT | `level` | 层级深度（根区域为0） |
| | `path_hash` | VARCHAR(64) | `pathHash` | 路径哈希（快速层级查询） |
| | `sort_order` | INT | `sortOrder` | 排序号（同层级排序） |
| | `description` | VARCHAR(500) | `description` | 区域描述 |
| | `building_id` | BIGINT | `buildingId` | 所在建筑ID |
| | `floor_id` | BIGINT | `floorId` | 所在楼层ID |
| | `area` | DECIMAL(10,2) | `area` | 区域面积（平方米） |
| | `capacity` | INT | `capacity` | 容纳人数 |
| | `access_enabled` | TINYINT | `accessEnabled` | 是否启用门禁 0禁用 1启用 |
| | `access_level` | TINYINT | `accessLevel` | 访问权限级别 |
| | `longitude` | DECIMAL(10,7) | `longitude` | 经度坐标 |
| | `latitude` | DECIMAL(10,7) | `latitude` | 纬度坐标 |
| | `status` | TINYINT | `status` | 区域状态 0停用 1正常 2维护中 |
| `t_area_device` | `area_id` | BIGINT | `areaId` | 区域ID |
| | `device_id` | BIGINT | `deviceId` | 关联设备 |

### 2.4 人员域（统一模型）
| 表名 | 字段 | 类型 | 前端字段 | 说明 |
| --- | --- | --- | --- | --- |
| `t_person_profile` | `person_id` | BIGINT | `personId` | 人员主键 |
| | `person_code` | VARCHAR(32) | `personCode` | 人员工号（系统唯一） |
| | `person_name` | VARCHAR(64) | `personName` | 姓名 |
| | `gender` | TINYINT | `gender` | 性别 1男 2女 |
| | `email` | VARCHAR(128) | `email` | 邮箱 |
| | `phone` | VARCHAR(32) | `phone` | 电话 |
| | `department_id` | BIGINT | `departmentId` | 部门ID |
| | `position` | VARCHAR(64) | `position` | 职位 |
| | `person_status` | TINYINT | `personStatus` | 人员状态 0黑名单 1在职 2停用 3离职 |
| | `id_card` | VARCHAR(32) | `idCard` | 身份证号 |
| | `id_masked` | VARCHAR(128) | `idMasked` | 证件号脱敏显示 |
| | `join_date` | DATE | `joinDate` | 入职日期 |
| | `address` | VARCHAR(256) | `address` | 地址 |
| | `ext_json` | JSON | `extJson` | 扩展信息JSON |
| `t_person_credential` | `credential_id` | BIGINT | `credentialId` | 凭证ID |
| | `person_id` | BIGINT | `personId` | 人员ID |
| | `credential_type` | VARCHAR(32) | `credentialType` | 凭证类型 CARD|BIOMETRIC|PASSWORD|QR_CODE |
| | `credential_value` | VARCHAR(256) | `credentialValue` | 凭证值 |
| | `credential_name` | VARCHAR(100) | `credentialName` | 凭证名称 |
| | `effective_time` | DATETIME | `effectiveTime` | 生效时间 |
| | `expire_time` | DATETIME | `expireTime` | 失效时间 |
| | `status` | TINYINT | `status` | 状态 0禁用 1启用 |
| `t_area_person` | `id` | BIGINT | `id` | 主键ID |
| | `area_id` | BIGINT | `areaId` | 区域ID |
| | `person_id` | BIGINT | `personId` | 人员ID |
| | `data_scope` | VARCHAR(32) | `dataScope` | 数据域 AREA|DEPT|SELF|CUSTOM |
| | `permission_level` | TINYINT | `permissionLevel` | 权限级别 |
| | `effective_time` | DATETIME | `effectiveTime` | 生效时间 |
| | `expire_time` | DATETIME | `expireTime` | 失效时间 |
| | `status` | TINYINT | `status` | 状态 0禁用 1启用 |
| `t_rbac_resource` | `resource_id` | BIGINT | `resourceId` | 资源ID |
| | `resource_code` | VARCHAR(128) | `resourceCode` | 资源编码（唯一） |
| | `resource_name` | VARCHAR(128) | `resourceName` | 资源名称 |
| | `resource_type` | VARCHAR(32) | `resourceType` | 资源类型 API|MENU|BUTTON|DATA |
| | `module` | VARCHAR(32) | `module` | 所属模块 ACCESS|ATTENDANCE|CONSUME|VIDEO|SYSTEM |
| | `description` | VARCHAR(256) | `description` | 资源描述 |
| | `parent_id` | BIGINT | `parentId` | 父级资源ID |
| `t_rbac_role` | `role_id` | BIGINT | `roleId` | 角色ID |
| | `role_code` | VARCHAR(64) | `roleCode` | 角色编码（唯一） |
| | `role_name` | VARCHAR(128) | `roleName` | 角色名称 |
| | `description` | VARCHAR(256) | `description` | 角色描述 |
| | `role_level` | TINYINT | `roleLevel` | 角色级别 |
| `t_rbac_role_resource` | `id` | BIGINT | `id` | 主键ID |
| | `role_id` | BIGINT | `roleId` | 角色ID |
| | `resource_id` | BIGINT | `resourceId` | 资源ID |
| | `action` | VARCHAR(64) | `action` | 动作 READ|WRITE|DELETE|APPROVE|* |
| | `condition_json` | JSON | `conditionJson` | RAC条件JSON |
| | `data_scope` | VARCHAR(32) | `dataScope` | 数据域限制 |
| `t_rbac_user_role` | `user_id` | BIGINT | `userId` | 用户ID |
| | `role_id` | BIGINT | `roleId` | 角色ID |
| | `effective_time` | DATETIME | `effectiveTime` | 生效时间 |
| | `expire_time` | DATETIME | `expireTime` | 失效时间 |
| | `status` | TINYINT | `status` | 状态 0禁用 1启用 |

### 2.8 RAC权限中间件扩展表

#### 权限策略表 (t_rbac_permission_policy)
| 表名 | 字段 | 类型 | 前端字段 | 说明 |
| --- | --- | --- | --- | --- |
| `t_rbac_permission_policy` | `policy_id` | BIGINT | `policyId` | 策略主键ID |
| | `policy_name` | VARCHAR(128) | `policyName` | 策略名称 |
| | `policy_code` | VARCHAR(64) | `policyCode` | 策略编码（唯一） |
| | `resource_pattern` | VARCHAR(256) | `resourcePattern` | 资源匹配模式（支持通配符） |
| | `action_pattern` | VARCHAR(128) | `actionPattern` | 动作匹配模式（支持通配符） |
| | `condition_expression` | TEXT | `conditionExpression` | RAC条件表达式 |
| | `data_scope_constraint` | VARCHAR(32) | `dataScopeConstraint` | 数据域约束 |
| | `priority` | INT | `priority` | 策略优先级（数字越大优先级越高） |
| | `effect` | VARCHAR(16) | `effect` | 策略效果 ALLOW/DENY |
| | `description` | VARCHAR(512) | `description` | 策略描述 |
| | `enabled` | TINYINT | `enabled` | 是否启用 0禁用 1启用 |

#### 权限缓存表 (t_rbac_permission_cache)
| 表名 | 字段 | 类型 | 前端字段 | 说明 |
| --- | --- | --- | --- | --- |
| `t_rbac_permission_cache` | `cache_id` | BIGINT | `cacheId` | 缓存主键ID |
| | `user_id` | BIGINT | `userId` | 用户ID |
| | `resource_code` | VARCHAR(128) | `resourceCode` | 资源编码 |
| | `action` | VARCHAR(64) | `action` | 动作 |
| | `data_scope_result` | JSON | `dataScopeResult` | 数据域查询结果 |
| | `permission_result` | TINYINT | `permissionResult` | 权限结果 0拒绝 1允许 |
| | `cache_key` | VARCHAR(256) | `cacheKey` | 缓存键 |
| | `expire_time` | DATETIME | `expireTime` | 过期时间 |
| | `created_time` | DATETIME | `createdTime` | 创建时间 |

#### 权限审计日志表 (t_rbac_permission_audit)
| 表名 | 字段 | 类型 | 前端字段 | 说明 |
| --- | --- | --- | --- | --- |
| `t_rbac_permission_audit` | `audit_id` | BIGINT | `auditId` | 审计主键ID |
| | `user_id` | BIGINT | `userId` | 用户ID |
| | `username` | VARCHAR(64) | `username` | 用户名 |
| | `resource_code` | VARCHAR(128) | `resourceCode` | 请求资源编码 |
| | `action` | VARCHAR(64) | `action` | 请求动作 |
| | `request_method` | VARCHAR(16) | `requestMethod` | HTTP方法 |
| | `request_url` | VARCHAR(512) | `requestUrl` | 请求URL |
| | `client_ip` | VARCHAR(64) | `clientIp` | 客户端IP |
| | `user_agent` | VARCHAR(512) | `userAgent` | 用户代理 |
| | `permission_result` | TINYINT | `permissionResult` | 权限检查结果 0拒绝 1允许 |
| | `denied_reason` | VARCHAR(256) | `deniedReason` | 拒绝原因 |
| | `execution_time_ms` | BIGINT | `executionTimeMs` | 执行耗时（毫秒） |
| | `cache_hit` | TINYINT | `cacheHit` | 是否缓存命中 0否 1是 |
| | `created_time` | DATETIME | `createdTime` | 审计时间 |

#### 数据域解析缓存表 (t_rbac_data_scope_cache)
| 表名 | 字段 | 类型 | 前端字段 | 说明 |
| --- | --- | --- | --- | --- |
| `t_rbac_data_scope_cache` | `cache_id` | BIGINT | `cacheId` | 缓存主键ID |
| | `user_id` | BIGINT | `userId` | 用户ID |
| | `data_scope_type` | VARCHAR(32) | `dataScopeType` | 数据域类型 |
| | `scope_parameters` | JSON | `scopeParameters` | 域参数（areaIds, deptIds等） |
| | `resolved_area_ids` | JSON | `resolvedAreaIds` | 解析后的区域ID列表 |
| | `resolved_dept_ids` | JSON | `resolvedDeptIds` | 解析后的部门ID列表 |
| | `cache_key` | VARCHAR(256) | `cacheKey` | 缓存键 |
| | `expire_time` | DATETIME | `expireTime` | 过期时间 |
| | `created_time` | DATETIME | `createdTime` | 创建时间 |

### 2.5 告警域
| 表名 | 字段 | 类型 | 前端字段 | 说明 |
| --- | --- | --- | --- | --- |
| `t_alert_rule` | `rule_id` | BIGINT | `ruleId` | 告警规则ID |
| | `alert_level` | VARCHAR(16) | `alertLevel` | 告警级别 |
| | `trigger_expression` | TEXT | `triggerExpression` | 触发条件 |
| `t_alert_event` | `event_id` | BIGINT | `eventId` | 告警事件 |
| | `occurred_at` | DATETIME | `occurredAt` | 发生时间 |
| | `status` | VARCHAR(16) | `status` | 状态: NEW/ACK/RESOLVED |

### 2.6 工作流域
| 表名 | 字段 | 类型 | 前端字段 | 说明 |
| --- | --- | --- | --- | --- |
| `t_workflow_process` | `process_id` | BIGINT | `processId` | 流程主键 |
| | `process_key` | VARCHAR(64) | `processKey` | BPMN Key |
| | `status` | VARCHAR(16) | `status` | 状态 |
| `t_workflow_task` | `task_id` | BIGINT | `taskId` | 任务ID |
| | `assignee` | VARCHAR(64) | `assignee` | 处理人 |
| | `due_time` | DATETIME | `dueTime` | 截止时间 |

### 2.7 生物识别监控域
| 表名/视图 | 字段 | 类型 | 前端字段 | 说明 |
| --- | --- | --- | --- | --- |
| `t_biometric_monitor_snapshot` | `engine_id` | VARCHAR(64) | `engineId` | 引擎/边缘节点唯一ID。 |
| | `registered_algorithms` | JSON | `registeredAlgorithms` | 运行中的算法集合。 |
| | `algorithm_statuses` | JSON | `algorithmStatuses` | 各算法状态, 结构 `{FACE:"READY"}`。 |
| | `successful_authentications` | BIGINT | `successfulAuthentications` | 成功累计次数。 |
| | `failed_authentications` | BIGINT | `failedAuthentications` | 失败累计次数。 |
| | `total_processing_time_ms` | BIGINT | `totalProcessingTimeMs` | 累积耗时。 |
| | `average_processing_time_ms` | BIGINT | `averageProcessingTimeMs` | 平均耗时。 |
| | `success_rate` | DECIMAL(6,4) | `successRate` | 成功率=success/(success+fail)。 |
| | `system_resource_usage` | JSON | `systemResourceUsage` | CPU/Memory 等指标。 |
| | `heartbeat_time` | DATETIME | `heartbeatTime` | 最近心跳时间。 |
| | `stale` | TINYINT | `stale` | 是否超时(1=超时)。 |
| `t_biometric_alert_log` | `alert_id` | VARCHAR(64) | `alertId` | 告警主键, 来自UUID。 |
| | `engine_id` | VARCHAR(64) | `engineId` | 告警所属引擎。 |
| | `alert_code` | VARCHAR(64) | `alertCode` | 规则或手工编码。 |
| | `alert_level` | VARCHAR(16) | `level` | INFO/WARNING/CRITICAL。 |
| | `status` | VARCHAR(16) | `status` | ACTIVE/ACK/RESOLVED。 |
| | `message` | VARCHAR(512) | `message` | 告警描述。 |
| | `resolution` | VARCHAR(512) | `resolution` | 处理意见。 |
| | `created_at` | DATETIME | `createdAt` | 触发时间。 |
| | `updated_at` | DATETIME | `updatedAt` | 最近更新时间。 |

## 3. 枚举与字典

### 3.1 RAC权限中间件枚举

#### 数据域类型 (DataScope)
| 枚举值 | 描述 | 使用场景 | 备注 |
| --- | --- | --- | --- |
| `ALL` | 全部数据权限 | 超级管理员 | 可访问所有数据 |
| `AREA` | 区域数据权限 | 区域管理员 | 限定可访问的区域范围 |
| `DEPT` | 部门数据权限 | 部门管理员 | 限定可访问的部门范围 |
| `SELF` | 个人数据权限 | 普通用户 | 仅限访问本人数据 |
| `CUSTOM` | 自定义数据域 | 特殊业务场景 | 根据业务规则自定义 |
| `NONE` | 无数据域限制 | 系统级资源 | 不进行数据域过滤 |

#### 策略效果 (PolicyEffect)
| 枚举值 | 描述 | 使用场景 | 备注 |
| --- | --- | --- | --- |
| `ALLOW` | 允许访问 | 权限允许策略 | 明确允许访问资源 |
| `DENY` | 拒绝访问 | 权限拒绝策略 | 明确拒绝访问资源，优先级最高 |

#### 权限检查结果 (PermissionResult)
| 枚举值 | 描述 | 含义 | 备注 |
| --- | --- | --- | --- |
| `0` | 拒绝 | 权限检查不通过 | 访问被拒绝 |
| `1` | 允许 | 权限检查通过 | 访问被允许 |

#### 权限级别 (PermissionLevel)
| 级别 | 描述 | 权限范围 | 备注 |
| --- | --- | --- | --- |
| `1` | 只读权限 | 仅查看数据 | 基础访问权限 |
| `2` | 操作权限 | 查看和操作 | 一般业务操作 |
| `3` | 管理权限 | 完整管理功能 | 部门管理员级别 |
| `4` | 系统权限 | 系统配置管理 | 系统管理员级别 |
| `5` | 超级权限 | 所有功能权限 | 超级管理员级别 |

#### 角色级别 (RoleLevel)
| 级别 | 描述 | 对应用户类型 | 备注 |
| --- | --- | --- | --- |
| `1` | 普通用户 | 一般员工 | 基础权限 |
| `2` | 业务用户 | 业务操作员 | 业务相关权限 |
| `3` | 主管用户 | 部门主管 | 部门管理权限 |
| `4` | 管理员 | 系统管理员 | 系统管理权限 |
| `5` | 超级管理员 | 最高权限 | 所有权限 |

#### 资源类型 (ResourceType)
| 类型 | 描述 | 示例 | 备注 |
| --- | --- | --- | --- |
| `API` | 接口资源 | REST API端点 | 后端接口权限控制 |
| `MENU` | 菜单资源 | 前端菜单项 | 前端菜单显示控制 |
| `BUTTON` | 按钮资源 | 页面操作按钮 | 前端按钮权限控制 |
| `DATA` | 数据资源 | 数据表/字段 | 数据访问权限控制 |

#### 动作类型 (ActionType)
| 动作 | 描述 | HTTP方法 | 备注 |
| --- | --- | --- | --- |
| `READ` | 读取权限 | GET | 查询数据、查看页面 |
| `WRITE` | 写入权限 | POST/PUT | 创建数据、编辑操作 |
| `DELETE` | 删除权限 | DELETE | 删除数据、移除操作 |
| `APPROVE` | 审批权限 | POST | 审批流程、状态变更 |
| `*` | 所有权限 | ANY | 全操作权限 |

#### 凭证类型 (CredentialType)
| 类型 | 描述 | 示例 | 安全级别 |
| --- | --- | --- | --- |
| `CARD` | IC卡凭证 | 门禁卡、员工卡 | 中等 |
| `BIOMETRIC` | 生物特征 | 指纹、人脸、虹膜 | 高 |
| `PASSWORD` | 密码凭证 | 登录密码、PIN码 | 中等 |
| `QR_CODE` | 二维码凭证 | 动态二维码 | 中等 |

### 3.2 通用枚举

#### 通用状态枚举
| 枚举值 | 描述 | 颜色 | 备注 |
| --- | --- | --- | --- |
| `device_type` | 设备类型 | ACCESS/POS/CAMERA | 前后端保持大写枚举 |
| `alert_level` | 告警级别 | LOW/MEDIUM/HIGH/CRITICAL | 对应 UI 颜色 |
| `data_scope` | 数据范围 | ALL/DEPT/AREA/CUSTOM | 权限过滤 |

### 3.3 RAC权限模型示例

#### 门禁系统权限配置示例
| 资源编码 | 资源名称 | 动作 | 数据域 | 角色要求 |
| --- | --- | --- | --- | --- |
| `smart:access:device` | 门禁设备管理 | READ/WRITE/DELETE | AREA/DEPT | 门禁管理员 |
| `smart:access:verify` | 门禁通行校验 | READ | SELF/AREA | 所有用户 |
| `smart:access:remote` | 远程开门控制 | WRITE | AREA | 区域管理员 |
| `smart:access:log` | 门禁记录查询 | READ | DEPT/AREA | 安全管理员 |

#### 考勤系统权限配置示例
| 资源编码 | 资源名称 | 动作 | 数据域 | 角色要求 |
| --- | --- | --- | --- | --- |
| `smart:attendance:punch` | 考勤打卡 | WRITE | SELF | 所有用户 |
| `smart:attendance:record` | 考勤记录查询 | READ | SELF/DEPT | 部门主管 |
| `smart:attendance:schedule` | 排班管理 | WRITE | DEPT | 考勤管理员 |
| `smart:attendance:statistics` | 考勤统计 | READ | DEPT/AREA | 管理层 |

#### 消费系统权限配置示例
| 资源编码 | 资源名称 | 动作 | 数据域 | 角色要求 |
| --- | --- | --- | --- | --- |
| `smart:consume:account` | 消费账户管理 | READ/WRITE | SELF/DEPT | 财务管理员 |
| `smart:consume:pay` | 消费支付 | WRITE | SELF | 所有用户 |
| `smart:consume:record` | 消费记录查询 | READ | SELF/DEPT | 财务人员 |
| `smart:consume:refund` | 消费退款 | WRITE | DEPT | 消费管理员 |

### 3.4 RAC权限中间件使用示例

#### 后端注解使用示例
```java
// 门禁设备管理权限
@RequireResource(
    resource = "smart:access:device",
    action = "READ",
    dataScope = DataScope.AREA,
    message = "您没有权限查询门禁设备列表"
)
public ResponseDTO<PageResult<AccessDeviceEntity>> getDevicePage(@Valid @RequestBody PageParam pageParam) {
    return ResponseDTO.ok(accessDeviceService.getDevicePage(pageParam));
}

// 远程开门控制权限
@RequireResource(
    resource = "smart:access:remote",
    action = "WRITE",
    dataScope = DataScope.AREA,
    message = "您没有权限远程控制门禁设备"
)
public ResponseDTO<String> remoteOpenDoor(@PathVariable Long accessDeviceId) {
    return ResponseDTO.ok(accessDeviceService.remoteOpenDoor(accessDeviceId));
}

// 考勤打卡权限（仅限个人）
@RequireResource(
    resource = "smart:attendance:punch",
    action = "WRITE",
    dataScope = DataScope.SELF,
    message = "您只能为自己进行考勤打卡"
)
public ResponseDTO<String> punchIn(@Valid @RequestBody PunchRequest request) {
    return ResponseDTO.ok(attendanceService.punchIn(request));
}
```

#### 前端权限组件使用示例
```vue
<!-- 基础权限检查 -->
<template>
  <a-button v-permission="'smart:access:device:read'">
    查看设备
  </a-button>

  <!-- 数组权限检查（OR逻辑） -->
  <div v-permission="['smart:access:device:write', 'smart:access:device:delete']">
    <a-button>编辑设备</a-button>
    <a-button>删除设备</a-button>
  </div>

  <!-- 对象格式权限检查 -->
  <a-button v-permission="{
    resource: 'smart:access:device',
    action: 'WRITE',
    dataScope: 'AREA',
    areaId: currentAreaId
  }">
    远程开门
  </a-button>

  <!-- 权限包装器（支持fallback） -->
  <PermissionWrapper
    :permission="'smart:access:device:delete'"
    :fallback="() => h('span', '无权限删除')"
  >
    <a-button type="danger" @click="deleteDevice">
      删除设备
    </a-button>
  </PermissionWrapper>
</template>

<script setup>
import { usePermission, useDataScope } from '@/utils/permission'

const { hasPermission, canRead, canWrite } = usePermission()
const { hasAreaPermission, hasDeptPermission } = useDataScope()

// 组合式API权限检查
const checkDevicePermission = async () => {
  const canEdit = await hasPermission('smart:access:device', 'WRITE')
  const canAccessArea = await hasAreaPermission(currentAreaId)
  return canEdit && canAccessArea
}
</script>
```

#### 权限配置最佳实践

1. **资源编码规范**
   - 格式：`{module}:{submodule}:{resource}`
   - 示例：`smart:access:device`, `smart:attendance:punch`
   - 保持一致性和可读性

2. **数据域选择原则**
   - **ALL**: 仅用于超级管理员功能
   - **AREA**: 区域相关的业务数据
   - **DEPT**: 部门内部的管理数据
   - **SELF**: 个人相关的操作和数据
   - **CUSTOM**: 特殊业务规则场景

3. **权限检查性能优化**
   - 合理使用缓存（TTL: 30分钟）
   - 批量权限检查减少API调用
   - 前端权限预检查减少后端请求

4. **安全注意事项**
   - DENY策略优先级高于ALLOW
   - 敏感操作必须进行权限验证
   - 定期审计权限配置和使用情况

## 4. 维护流程
1. 新增表或字段必须在合并前更新本数据字典。
2. 修改字段命名需同步更新前端实体与接口文档。
3. 每季度由数据治理组核对数据库与文档一致性。
4. RAC权限中间件相关表变更需同步更新权限策略和缓存配置。
5. 权限审计日志建议按月归档，保留最近6个月数据。

## 5. 参考资料
- `docs/COMMON_MODULES_ANALYSIS.md`
- `docs/SECURITY/role_resource_matrix.md`
- `docs/SmartAdmin规范体系_v4/数据规范.md`
