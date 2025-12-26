# IOE-DREAM项目全局补充开发计划与规范

> **文档类型**: 全局补充开发计划与开发规范
> **制定日期**: 2025-12-24
> **参考依据**: GLOBAL_FUNCTION_COMPLETENESS_ANALYSIS_REPORT.md
> **文档版本**: v1.0.0

---

## 📋 文档说明

本文档基于《IOE-DREAM项目全局业务模块功能完整性分析报告》的发现，制定详细的补充开发计划、开发规范和注意事项，确保：
1. **全局一致性**：前后端数据模型、API接口、业务逻辑完全对齐
2. **避免冗余**：消除重复功能，统一实现方式
3. **严格规范**：遵循企业级开发规范，确保代码质量
4. **完整实现**：补充所有缺失功能，达到100%业务覆盖

---

## 🎯 总体目标

### 核心目标

| 维度 | 当前状态 | 目标状态 | 提升幅度 | 完成时限 |
|------|---------|---------|---------|---------|
| **功能完整性** | 68% (490/718) | 100% (718/718) | +32% | 6个月 |
| **API覆盖度** | 67% (295/440) | 100% (440/440) | +33% | 6个月 |
| **数据模型完整度** | 72% (90/124) | 100% (124/124) | +28% | 6个月 |
| **代码规范符合率** | ~70% | 95%+ | +25% | 持续 |
| **文档完整率** | ~60% | 100% | +40% | 持续 |

### 质量目标

| 质量维度 | 目标标准 | 验收方法 |
|---------|---------|---------|
| **代码质量** | SonarQube评分A+ | 自动化代码审查 |
| **测试覆盖率** | 单元测试≥80%，集成测试≥70% | 测试报告 |
| **API规范** | OpenAPI 3.0规范，100%文档化 | Swagger审查 |
| **性能标准** | API响应<500ms，首屏<2s | 性能测试 |
| **安全标准** | 无高危漏洞，通过OWASP检查 | 安全扫描 |

---

## 📊 补充开发任务清单

### Phase 1: P0级关键功能（第1-2月）

#### 1.1 门禁管理模块（19项，27人天）

##### 任务1.1.1: 设备自动发现功能（3人天）

**需求描述**：
```
功能名称: 设备自动发现
业务价值: 提升设备部署效率80%，减少手动配置工作量
技术要求: 支持网络扫描、协议识别、自动配置
```

**开发规范**：

```yaml
后端实现:
  Controller:
    - DeviceAutoDiscoveryController
    - API: POST /api/v1/device/auto-discover
  Service:
    - DeviceDiscoveryService
    - 支持多协议扫描（TCP/UDP/HTTP）
  Manager:
    - DiscoveryManager
    - 网络扫描管理
  DAO:
    - 无需新增DAO，使用现有DeviceDao

前端实现:
  页面: smart-admin-web-javascript/src/views/business/access/device-auto-discovery.vue
  组件:
    - DiscoveryProgress（发现进度）
    - DiscoveryResultList（结果列表）
    - DeviceConfigForm（设备配置）
  API调用: /api/v1/device/auto-discover

移动端实现:
  页面: smart-app/src/pages/access/device-auto-discover.vue
  功能: 发起发现、查看进度、批量导入

数据模型:
  输入: DiscoveryRequest { ipRange, portRange, protocols }
  输出: DiscoveryResponse { discoveredDevices, total, duration }
  实体: DeviceEntity（复用）

验证标准:
  ✅ 能扫描指定IP段
  ✅ 能识别设备类型
  ✅ 能批量添加设备
  ✅ 扫描进度实时显示
  ✅ 扫描超时< 3分钟
```

**注意事项**：
1. ⚠️ 网络扫描可能触发安全告警，需要添加IP白名单机制
2. ⚠️ 大规模扫描可能导致性能问题，建议分批次扫描（每次/24段）
3. ⚠️ 需要处理扫描超时和设备无响应的情况
4. ⚠️ 扫描结果需要去重（同一设备多协议识别）

**测试用例**：
```yaml
TC-DISCOVER-001: 正常扫描发现10台设备
TC-DISCOVER-002: 扫描空网段返回0台设备
TC-DISCOVER-003: 扫描超时处理
TC-DISCOVER-004: 批量添加发现的设备
TC-DISCOVER-005: 扫描进度实时更新
```

---

##### 任务1.1.2: 批量设备导入功能（2人天）

**需求描述**：
```
功能名称: Excel批量导入
业务价值: 支持100+设备批量导入，部署效率提升90%
技术要求: Excel解析、数据校验、批量插入、错误报告
```

**开发规范**：

```yaml
后端实现:
  Controller:
    - DeviceImportController
    - API: POST /api/v1/device/import
  Service:
    - DeviceImportService
    - 使用EasyExcel解析Excel
    - 数据校验和去重
  Manager:
    - 无需新增Manager
  DAO:
    - DeviceDao.batchInsert()

前端实现:
  页面: smart-admin-web-javascript/src/views/business/access/device-import.vue
  组件:
    - ExcelUploader（文件上传）
    - ImportPreview（预览表格）
    - ImportErrorReport（错误报告）
  功能: 下载模板、上传文件、预览数据、确认导入

移动端实现:
  页面: smart-app/src/pages/access/device-import.vue
  功能: 下载模板、选择文件、上传导入

数据格式（Excel模板）:
  列定义:
    - 设备名称（必填）
    - 设备类型（必填，下拉选择）
    - IP地址（必填，格式校验）
    - 端口（必填，1-65535）
    - 所属区域（必填，下拉选择）
    - 设备编码（选填，自动生成）
    - 备注（选填）

验证标准:
  ✅ Excel格式正确（.xlsx）
  ✅ 必填字段校验
  ✅ IP地址格式校验
  ✅ 端口范围校验
  ✅ 区域存在性校验
  ✅ 批量插入性能（1000条< 30秒）
  ✅ 错误数据明细报告
```

**注意事项**：
1. ⚠️ Excel文件大小限制（< 10MB）
2. ⚠️ 单次导入数量限制（< 1000条）
3. ⚠️ 需要处理编码问题（UTF-8）
4. ⚠️ 导入前数据校验，导入后返回成功/失败明细
5. ⚠️ 大量数据导入使用异步处理，前端轮询进度

**Excel模板格式**：
```xlsx
设备名称 | 设备类型 | IP地址 | 端口 | 所属区域 | 设备编码 | 备注
---------|---------|--------|------|---------|---------|------
主入口门禁 | 门禁控制器 | 192.168.1.100 | 8080 | A栋1楼 | DEV001 |
副入口门禁 | 门禁控制器 | 192.168.1.101 | 8080 | A栋1楼 | DEV002 |
```

---

##### 任务1.1.3: 固件升级管理功能（5人天）

**需求描述**：
```
功能名称: 远程固件升级
业务价值: 支持100+设备批量升级，运维效率提升95%
技术要求: 固件版本管理、批量升级、进度监控、失败回滚
```

**开发规范**：

```yaml
后端实现:
  Controller:
    - DeviceFirmwareController
    - API:
      - GET /api/v1/device/firmware/versions（固件列表）
      - POST /api/v1/device/firmware/upload（上传固件）
      - POST /api/v1/device/firmware/upgrade（执行升级）
      - GET /api/v1/device/firmware/progress/{taskId}（升级进度）
  Service:
    - DeviceFirmwareService
    - FirmwareUpgradeManager
  Manager:
    - FirmwareManager（固件文件管理）
    - UpgradeManager（升级任务管理）
  DAO:
    - DeviceFirmwareDao（新增）
    - FirmwareUpgradeTaskDao（新增）

前端实现:
  页面: smart-admin-web-javascript/src/views/business/access/device-firmware.vue
  组件:
    - FirmwareVersionList（版本列表）
    - FirmwareUploader（固件上传）
    - DeviceSelector（设备选择）
    - UpgradeProgress（升级进度）
    - UpgradeHistory（升级历史）

移动端实现:
  已实现: smart-app/src/pages/access/firmware-upgrade.vue
  需增强: 添加固件上传、历史管理功能

数据模型:
  Entity: DeviceFirmwareEntity
    - firmwareId: Long
    - firmwareName: String
    - version: String
    - deviceType: Integer
    - fileSize: Long
    - filePath: String
    - md5: String
    - uploadTime: LocalDateTime
    - status: Integer（0-禁用 1-启用）

  Entity: FirmwareUpgradeTaskEntity
    - taskId: Long
    - taskName: String
    - firmwareId: Long
    - deviceIds: String（JSON数组）
    - totalDevices: Integer
    - successDevices: Integer
    - failedDevices: Integer
    - progress: Integer
    - status: Integer（0-待执行 1-执行中 2-已完成 3-已失败）
    - createTime: LocalDateTime

验证标准:
  ✅ 固件上传成功
  ✅ MD5校验通过
  ✅ 升级进度实时显示
  ✅ 失败设备支持重试
  ✅ 升级失败自动回滚
  ✅ 升级日志详细记录
```

**注意事项**：
1. ⚠️ 固件文件存储需要配置存储路径（建议使用OSS）
2. ⚠️ 升级过程中设备不可用，需要提前通知用户
3. ⚠️ 大批量升级需要队列处理，避免占用资源
4. ⚠️ 升级失败需要有明确的错误码和处理建议
5. ⚠️ 需要记录升级历史，方便追溯和审计

**固件升级流程**：
```
1. 上传固件包 → 2. 校验MD5 → 3. 选择设备 → 4. 创建升级任务 →
5. 执行升级 → 6. 进度监控 → 7. 结果确认 → 8. 历史记录
```

---

##### 任务1.1.4: 全局反潜回功能（8人天）

**需求描述**：
```
功能名称: 全局反潜回（Anti-Passback）
业务价值: 防止尾随和重复验证，提升安全性
技术要求: 多种反潜回模式、实时检测、黑白名单、区域规则
```

**开发规范**：

```yaml
后端实现:
  Controller:
    - AntiPassbackConfigController
    - API:
      - GET /api/v1/anti-passback/config（查询配置）
      - POST /api/v1/anti-passback/config（保存配置）
      - GET /api/v1/anti-passback/whitelist（白名单）
      - POST /api/v1/anti-passback/whitelist（添加白名单）
      - GET /api/v1/anti-passback/logs（检测日志）
  Service:
    - AntiPassbackConfigService
    - AntiPassbackDetectionService（检测逻辑）
  Manager:
    - AntiPassbackManager（反潜回规则管理）
  DAO:
    - AntiPassbackConfigDao（新增）
    - AntiPassbackWhitelistDao（新增）
    - AntiPassbackLogDao（新增）

前端实现:
  页面: smart-admin-web-javascript/src/views/business/access/anti-passback-config.vue
  组件:
    - ModeSelector（模式选择）
    - TimeConfig（时间配置）
    - WhitelistManager（白名单管理）
    - AreaRuleConfig（区域规则）
    - DetectionLog（检测日志）

移动端实现:
  已实现: smart-app/src/pages/access/anti-passback.vue
  需增强: 添加白名单管理、区域规则、检测日志功能

数据模型:
  Entity: AntiPassbackConfigEntity
    - configId: Long
    - mode: Integer（1-全局 2-区域 3-软 4-硬）
    - timeInterval: Integer（秒）
    - timeIntervalMin: Integer
    - timeIntervalMax: Integer
    - globalEnabled: Boolean
    - createTime: LocalDateTime

  Entity: AntiPassbackWhitelistEntity
    - whitelistId: Long
    - userId: Long
    - userName: String
    - deviceId: String
    - areaId: Long
    - reason: String
    - expiryTime: LocalDateTime
    - createTime: LocalDateTime

  Entity: AntiPassbackLogEntity
    - logId: Long
    - userId: Long
    - userName: String
    - deviceId: String
    - areaId: Long
    - passType: Integer（1-正常 2-触发反潜回）
    - detectionTime: LocalDateTime

反潜回模式说明:
  1. 全局反潜回: 所有设备全局检测，用户通过任一设备后，其他设备在时间间隔内不可通行
  2. 区域反潜回: 同一区域内检测，通过A区域设备后，A区域其他设备在时间间隔内不可通行
  3. 软反潜回: 检测到反潜回行为时，记录日志但允许通行
  4. 硬反潜回: 检测到反潜回行为时，拒绝通行并触发告警

验证标准:
  ✅ 支持4种反潜回模式
  ✅ 时间间隔可配置（0-3600秒）
  ✅ 白名单用户不受限
  ✅ 检测日志完整记录
  ✅ 检测响应时间< 100ms
  ✅ 支持临时白名单（有效期）
```

**注意事项**：
1. ⚠️ 反潜回检测需要高实时性，建议使用Redis缓存通行记录
2. ⚠️ 硬反潜回可能影响用户体验，需要配置合理的提示信息
3. ⚠️ 白名单功能需要权限控制，避免滥用
4. ⚠️ 需要考虑高并发场景下的性能问题（批量通行）
5. ⚠️ 检测日志数据量大，需要定期归档和清理

**反潜回检测伪代码**：
```java
public boolean checkAntiPassback(Long userId, String deviceId, Long areaId) {
    // 1. 检查白名单
    if (isInWhitelist(userId)) {
        return true; // 白名单直接通过
    }

    // 2. 获取反潜回配置
    AntiPassbackConfig config = getConfig();
    if (!config.globalEnabled) {
        return true; // 未启用直接通过
    }

    // 3. 根据模式检查
    switch (config.mode) {
        case 1: // 全局模式
            return checkGlobal(userId, config.timeInterval);
        case 2: // 区域模式
            return checkArea(userId, areaId, config.timeInterval);
        case 3: // 软模式
            checkAndLogSoft(userId, deviceId, areaId);
            return true;
        case 4: // 硬模式
            return checkHard(userId, deviceId, areaId);
        default:
            return true;
    }
}
```

---

##### 任务1.1.5: 实时监控告警功能（6人天）

**需求描述**：
```
功能名称: 实时监控告警
业务价值: 及时发现和处理异常，提升系统安全性
技术要求: 告警规则配置、实时监控、消息推送、告警处理
```

**开发规范**：

```yaml
后端实现:
  Controller:
    - AlarmRuleController
    - API:
      - GET /api/v1/alarm/rules（规则列表）
      - POST /api/v1/alarm/rules（创建规则）
      - PUT /api/v1/alarm/rules/{id}（更新规则）
      - DELETE /api/v1/alarm/rules/{id}（删除规则）
      - GET /api/v1/alarm/records（告警记录）
      - POST /api/v1/alarm/records/{id}/handle（处理告警）
  Service:
    - AlarmRuleService
    - AlarmMonitorService（实时监控）
    - AlarmNotificationService（消息推送）
  Manager:
    - AlarmManager（告警处理）
  DAO:
    - AlarmRuleDao（新增）
    - AlarmRecordDao（新增）

前端实现:
  页面: smart-admin-web-javascript/src/views/business/access/alarm-config.vue
  组件:
    - RuleList（规则列表）
    - RuleEditor（规则编辑器）
    - AlarmRecords（告警记录）
    - AlarmChart（告警统计）

移动端实现:
  页面: smart-app/src/pages/access/alarm-list.vue
  功能: 告警列表、告警详情、告警处理

数据模型:
  Entity: AlarmRuleEntity
    - ruleId: Long
    - ruleName: String
    - alarmType: Integer（1-设备离线 2-设备故障 3-异常通行 4-区域告警）
    - triggerCondition: String（JSON，触发条件）
    - severity: Integer（1-低 2-中 3-高 4-紧急）
    - enabled: Boolean
    - notificationChannels: String（JSON，通知渠道）
    - createTime: LocalDateTime

  Entity: AlarmRecordEntity
    - recordId: Long
    - ruleId: Long
    - alarmType: Integer
    - alarmMessage: String
    - deviceId: String
    - areaId: Long
    - severity: Integer
    - status: Integer（0-待处理 1-已处理 2-已忽略）
    - handleResult: String
    - handleUserId: Long
    - handleTime: LocalDateTime
    - createTime: LocalDateTime

告警触发条件示例:
  设备离线: {"type": "device_offline", "duration": 300}
  设备故障: {"type": "device_error", "errorCode": "E001"}
  异常通行: {"type": "abnormal_access", "userId": 123}
  区域告警: {"type": "area_alarm", "areaId": 1, "count": 10}

验证标准:
  ✅ 告警规则灵活配置
  ✅ 告警实时检测< 5秒
  ✅ 支持多种通知渠道（短信/邮件/推送）
  ✅ 告警记录完整
  ✅ 告警处理可追溯
```

**注意事项**：
1. ⚠️ 实时监控需要WebSocket或定时轮询
2. ⚠️ 告警风暴可能引起性能问题，需要限流和去重
3. ⚠️ 告警推送需要考虑接收频率，避免骚扰
4. ⚠️ 告警级别不同，处理时效要求不同
5. ⚠️ 需要支持告警升级机制（长时间未处理自动升级）

---

#### 1.2 考勤管理模块（16项，32人天）

##### 任务1.2.1: 智能排班算法（12人天）

**需求描述**：
```
功能名称: AI智能排班
业务价值: 排班效率提升90%，人力资源配置最优化
技术要求: 规则引擎、AI算法、自动排班、手动调整
```

**开发规范**：

```yaml
后端实现:
  Controller:
    - IntelligentSchedulingController
    - API:
      - POST /api/v1/attendance/scheduling/auto-schedule（自动排班）
      - GET /api/v1/attendance/scheduling/preview（预览排班）
      - POST /api/v1/attendance/scheduling/adjust（手动调整）
      - GET /api/v1/attendance/scheduling/conflicts（冲突检测）
  Service:
    - IntelligentSchedulingService
    - SchedulingAlgorithmService（算法服务）
  Manager:
    - SchedulingManager（排班管理）
  DAO:
    - SchedulingResultDao（新增）
    - SchedulingConflictDao（新增）

技术选型:
  规则引擎: Aviator（轻量级规则引擎）
  约束求解器: OptaPlanner（优化规划）
  AI算法: TensorFlow（可选，机器学习优化）

前端实现:
  页面: smart-admin-web-javascript/src/views/business/attendance/intelligent-scheduling.vue
  组件:
    - SchedulingWizard（排班向导）
    - RuleConfig（规则配置）
    - SchedulingPreview（排班预览）
    - ConflictResolution（冲突解决）
    - SchedulingCalendar（排班日历）

移动端实现:
  页面: smart-app/src/pages/attendance/scheduling.vue
  功能: 查看排班、申请调班、审批调班

数据模型:
  Entity: SchedulingRuleEntity
    - ruleId: Long
    - ruleName: String
    - ruleType: Integer（1-硬约束 2-软约束 3-优化目标）
    - priority: Integer
    - condition: String（JSON）
    - weight: Double（权重）

  Entity: SchedulingResultEntity
    - resultId: Long
    - schedulingDate: LocalDate
    - userId: Long
    - shiftId: Long
    - areaId: Long
    - score: Double（匹配度分数）
    - status: Integer（0-待确认 1-已确认 2-已冲突）

排班规则类型:
  1. 硬约束（必须满足）:
     - 每天最少在岗人数
     - 连续工作天数限制
     - 每周最多工作小时数
     - 特殊岗位资质要求

  2. 软约束（尽量满足）:
     - 员工班次偏好
     - 公平性（工作量均衡）
     - 连续班次间隔

  3. 优化目标:
     - 人力成本最低
     - 员工满意度最高
     - 覆盖率最优

验证标准:
  ✅ 自动排班覆盖率100%
  ✅ 约束冲突检测准确
  ✅ 排班结果可导出
  ✅ 支持手动调整
  ✅ 排班算法< 30秒（100人，30天）
```

**注意事项**：
1. ⚠️ 排班算法复杂度高，需要分阶段实现（先规则后AI）
2. ⚠️ 大规模排班计算耗时长，建议异步处理
3. ⚠️ 需要考虑员工请假、加班等特殊情况
4. ⚠️ 排班结果需要审批流程
5. ⚠️ 需要支持排班模板和快速复制

**智能排班流程**：
```
1. 配置排班规则 → 2. 选择排班周期 → 3. 执行自动排班 →
4. 查看排班结果 → 5. 解决冲突 → 6. 手动微调 →
7. 导出排班表 → 8. 发布通知
```

---

##### 任务1.2.2: 考勤规则配置功能（8人天）

**需求描述**：
```
功能名称: 可视化规则配置
业务价值: 支持复杂考勤规则，配置灵活度提升95%
技术要求: 规则引擎、可视化配置器、实时预览、版本管理
```

**开发规范**：

```yaml
后端实现:
  Controller:
    - AttendanceRuleController
    - API:
      - GET /api/v1/attendance/rules（规则列表）
      - POST /api/v1/attendance/rules（创建规则）
      - PUT /api/v1/attendance/rules/{id}（更新规则）
      - DELETE /api/v1/attendance/rules/{id}（删除规则）
      - POST /api/v1/attendance/rules/preview（预览效果）
  Service:
    - AttendanceRuleService
    - RuleEngineService（规则引擎）
  Manager:
    - RuleManager（规则管理）
  DAO:
    - AttendanceRuleDao（新增）

前端实现:
  页面: smart-admin-web-javascript/src/views/business/attendance/rule-config.vue
  组件:
    - RuleList（规则列表）
    - RuleBuilder（规则构建器）
    - VisualEditor（可视化编辑器）
    - RulePreview（效果预览）
    - RuleVersion（版本管理）

移动端实现:
  页面: smart-app/src/pages/attendance/rule-config.vue
  功能: 查看规则、规则生效日期

技术选型:
  规则引擎: Aviator（表达式求值）
  表单设计器: FormMaking（可选）
  流程设计器: Vue Bpmn（可选）

数据模型:
  Entity: AttendanceRuleEntity
    - ruleId: Long
    - ruleName: String
    - ruleType: Integer（1-打卡规则 2-请假规则 3-加班规则）
    - conditionExpression: String（Aviator表达式）
    - actionExpression: String（执行动作）
    - priority: Integer
    - enabled: Boolean
    - effectDate: LocalDate
    - expiryDate: LocalDate

规则表达式示例:
  打卡规则:
    - 条件: between(checkin_time, '08:00', '09:00') && checkin_location == 'office'
    - 动作: status = 'normal'

  请假规则:
    - 条件: leave_days >= 3 && leave_type == 'annual'
    - 动作: require_approval()

验证标准:
  ✅ 规则配置可视化
  ✅ 规则表达式语法正确
  ✅ 规则冲突自动检测
  ✅ 规则生效时间精确
  ✅ 规则版本可追溯
```

**注意事项**：
1. ⚠️ 规则表达式需要语法校验，避免执行错误
2. ⚠️ 规则优先级需要明确定义，避免冲突
3. ⚠️ 规则生效需要考虑历史数据处理
4. ⚠️ 复杂规则需要提供规则模板
5. ⚠️ 规则变更需要通知相关人员

---

##### 任务1.2.3: 异常申诉审批流程（5人天）

**需求描述**：
```
功能名称: 考勤异常申诉
业务价值: 提升员工满意度，减少考勤争议
技术要求: 申诉提交、审批流程、结果通知、数据修正
```

**开发规范**：

```yaml
后端实现:
  Controller:
    - AttendanceAppealController
    - API:
      - POST /api/v1/attendance/appeal（提交申诉）
      - GET /api/v1/attendance/appeal/list（申诉列表）
      - PUT /api/v1/attendance/appeal/{id}/approve（审批）
      - PUT /api/v1/attendance/appeal/{id}/reject（驳回）
  Service:
    - AttendanceAppealService
  Manager:
    - AppealManager（申诉管理）
  DAO:
    - AttendanceAppealDao（新增）

前端实现:
  页面: smart-admin-web-javascript/src/views/business/attendance/appeal.vue
  组件:
    - AppealForm（申诉表单）
    - AppealList（申诉列表）
    - ApprovalFlow（审批流程）

移动端实现:
  页面: smart-app/src/pages/attendance/appeal.vue
  功能: 提交申诉、查看进度、接收通知

数据模型:
  Entity: AttendanceAppealEntity
    - appealId: Long
    - userId: Long
    - attendanceRecordId: Long
    - appealReason: String
    - appealType: Integer（1-忘记打卡 2-设备故障 3-外出办事）
    - attachmentUrl: String
    - appealStatus: Integer（0-待审批 1-已通过 2-已驳回）
    - approverId: Long
    - approverComment: String
    - approveTime: LocalDateTime
    - createTime: LocalDateTime

验证标准:
  ✅ 申诉类型完整
  ✅ 附件上传支持
  ✅ 审批流程完整
  ✅ 审批结果通知
  ✅ 数据自动修正
```

**注意事项**：
1. ⚠️ 申诉通过后需要自动修正考勤记录
2. ⚠️ 需要记录申诉历史，防止重复申诉
3. ⚠️ 审批权限需要配置，避免越权审批
4. ⚠️ 申诉驳回需要说明理由
5. ⚠️ 需要统计申诉通过率，优化考勤规则

---

#### 1.3 消费管理模块（14项，28人天）

##### 任务1.3.1: 离线消费同步机制（6人天）

**需求描述**：
```
功能名称: 离线消费数据同步
业务价值: 网络故障时消费不中断，数据零丢失
技术要求: 本地缓存、自动同步、数据校验、冲突处理
```

**开发规范**：

```yaml
后端实现:
  Controller:
    - OfflineSyncController
    - API:
      - POST /api/v1/consume/offline/upload（上传离线数据）
      - GET /api/v1/consume/offline/status（同步状态）
      - POST /api/v1/consume/offline/verify（数据校验）
  Service:
    - OfflineConsumeService
    - OfflineSyncService（同步服务）
  Manager:
    - OfflineDataManager（离线数据管理）
  DAO:
    - 无需新增DAO

前端实现:
  组件: OfflineDataManager（离线数据管理器）
  功能:
    - IndexedDB本地存储
    - 网络状态监听
    - 自动同步上传
    - 冲突数据处理

移动端实现:
  组件: @/utils/offline-manager.js
  功能: 离线数据缓存、网络恢复同步

数据同步流程:
  1. 消费时: 写入本地IndexedDB
  2. 网络恢复: 自动上传到服务器
  3. 服务器校验: 数据完整性、重复性校验
  4. 同步结果: 更新本地状态（成功/失败）
  5. 失败重试: 最多3次重试

数据模型:
  离线消费记录:
    - recordId: String（本地生成UUID）
    - userId: Long
    - deviceId: String
    - amount: BigDecimal
    - consumeTime: LocalDateTime
    - syncStatus: Integer（0-待同步 1-已同步 2-同步失败）
    - syncTime: LocalDateTime
    - retryCount: Integer

验证标准:
  ✅ 离线数据本地持久化
  ✅ 网络恢复自动同步
  ✅ 数据校验准确（重复、金额、时间）
  ✅ 同步失败重试机制
  ✅ 冲突数据人工审核
```

**注意事项**：
1. ⚠️ 离线数据需要加密存储，防止篡改
2. ⚠️ 本地存储容量有限，需要定期清理
3. ⚠️ 同步失败需要明确提示，避免用户以为已成功
4. ⚠️ 大量离线数据同步可能影响性能，需要批量处理
5. ⚠️ 需要支持手动触发同步，方便用户操作

---

##### 任务1.3.2: 补贴管理规则引擎（8人天）

**需求描述**：
```
功能名称: 补贴规则引擎
业务价值: 支持复杂补贴政策，自动化发放补贴
技术要求: 规则配置、自动计算、实时发放、报表统计
```

**开发规范**：

```yaml
后端实现:
  Controller:
    - SubsidyRuleController
    - API:
      - GET /api/v1/consume/subsidy/rules（规则列表）
      - POST /api/v1/consume/subsidy/rules（创建规则）
      - PUT /api/v1/consume/subsidy/rules/{id}（更新规则）
      - POST /api/v1/consume/subsidy/calculate（计算补贴）
      - POST /api/v1/consume/subsidy/grant（发放补贴）
  Service:
    - SubsidyRuleService
    - SubsidyCalculationService（计算服务）
  Manager:
    - SubsidyManager（补贴管理）
  DAO:
    - SubsidyRuleDao（新增）
    - SubsidyRecordDao（新增）

前端实现:
  页面: smart-admin-web-javascript/src/views/business/consume/subsidy-rule.vue
  组件:
    - RuleEditor（规则编辑器）
    - SubsidyCalculator（补贴计算器）
    - GrantRecord（发放记录）

移动端实现:
  页面: smart-app/src/pages/consume/subsidy.vue
  功能: 补贴余额、补贴明细

数据模型:
  Entity: SubsidyRuleEntity
    - ruleId: Long
    - ruleName: String
    - subsidyType: Integer（1-餐补 2-交通补 3-岗位补贴）
    - conditionExpression: String（Aviator表达式）
    - amountExpression: String（金额表达式）
    - grantCycle: Integer（1-每日 2-每周 3-每月）
    - enabled: Boolean

  Entity: SubsidyRecordEntity
    - recordId: Long
    - userId: Long
    - ruleId: Long
    - subsidyAmount: BigDecimal
    - grantDate: LocalDate
    - status: Integer（0-待发放 1-已发放 2-已取消）

补贴规则示例:
  餐补规则:
    - 条件: work_days >= 22 && department == '研发部'
    - 金额: 20 * work_days

  岗位补贴:
    - 条件: position_level == 'P7' && performance_score >= 90
    - 金额: 1000

验证标准:
  ✅ 规则配置灵活
  ✅ 计算准确无误
  ✅ 发放及时（T+1）
  ✅ 支持追溯和撤销
  ✅ 报表统计完整
```

**注意事项**：
1. ⚠️ 补贴规则复杂，需要充分测试
2. ⚠️ 补贴发放涉及财务，需要审计日志
3. ⚠️ 规则变更需要谨慎，避免影响历史数据
4. ⚠️ 需要支持补贴预览，方便用户了解
5. ⚠️ 异常情况需要人工介入处理

---

#### 1.4 访客管理模块（11项，22人天）

##### 任务1.4.1: 访客预约审批流程（6人天）

**需求描述**：
```
功能名称: 访客预约审批
业务价值: 规范访客管理，提升安全性和效率
技术要求: 预约申请、多级审批、状态通知、变更管理
```

**开发规范**：

```yaml
后端实现:
  Controller:
    - VisitorAppointmentController
    - API:
      - POST /api/v1/visitor/appointment（提交预约）
      - GET /api/v1/visitor/appointment/list（预约列表）
      - PUT /api/v1/visitor/appointment/{id}/approve（审批）
      - PUT /api/v1/visitor/appointment/{id}/reject（驳回）
      - PUT /api/v1/visitor/appointment/{id}/cancel（取消）
  Service:
    - VisitorAppointmentService
  Manager:
    - AppointmentManager（预约管理）
  DAO:
    - VisitorAppointmentDao（新增）

前端实现:
  页面: smart-admin-web-javascript/src/views/business/visitor/appointment.vue
  组件:
    - AppointmentForm（预约表单）
    - ApprovalFlow（审批流程）
    - AppointmentCalendar（预约日历）

移动端实现:
  页面: smart-app/src/pages/visitor/appointment.vue
  功能: 提交预约、查看进度、接收通知

数据模型:
  Entity: VisitorAppointmentEntity
    - appointmentId: Long
    - visitorName: String
    - visitorPhone: String
    - visitDate: LocalDate
    - visitStartTime: LocalTime
    - visitEndTime: LocalTime
    - visitReason: String
    - intervieweeId: Long
    - appointmentStatus: Integer（0-待审批 1-已通过 2-已驳回 3-已取消）
    - approverId: Long
    - approverComment: String
    - approveTime: LocalDateTime
    - createTime: LocalDateTime

验证标准:
  ✅ 预约信息完整
  ✅ 审批流程灵活（单人/多人/多级）
  ✅ 审批结果通知及时
  ✅ 预约变更可追溯
  ✅ 支持预约取消和改期
```

**注意事项**：
1. ⚠️ 审批流程需要支持配置（不同部门不同流程）
2. ⚠️ 预约冲突检测（同一访客同一时间多次预约）
3. ⚠️ 审批超时需要自动提醒或升级
4. ⚠️ 访客信息需要保护（脱敏展示）
5. ⚠️ 取消预约需要权限控制

---

##### 任务1.4.2: 电子通行证管理（5人天）

**需求描述**：
```
功能名称: 电子通行证（二维码通行证）
业务价值: 无接触访客体验，提升通行效率
技术要求: 二维码生成、有效期限、使用记录、自动撤销
```

**开发规范**：

```yaml
后端实现:
  Controller:
    - EPassportController
    - API:
      - POST /api/v1/visitor/epassport/generate（生成通行证）
      - GET /api/v1/visitor/epassport/{id}（查询通行证）
      - POST /api/v1/visitor/epassport/{id}/verify（验证通行证）
      - PUT /api/v1/visitor/epassport/{id}/revoke（撤销通行证）
  Service:
    - EPassportService
  Manager:
    - EPassportManager（通行证管理）
  DAO:
    - EPassportDao（新增）

技术选型:
  二维码库: ZXing（生成）或QRCode.js
  加密算法: AES（二维码内容加密）

前端实现:
  组件: EPassportView（通行证展示）
  功能: 生成二维码、显示有效期、使用状态

移动端实现:
  页面: smart-app/src/pages/visitor/epassport.vue
  功能: 通行证展示、刷新二维码

数据模型:
  Entity: EPassportEntity
    - passportId: Long
    - appointmentId: Long
    - visitorId: Long
    - qrCodeData: String（加密的二维码数据）
    - validStartTime: LocalDateTime
    - validEndTime: LocalDateTime
    - status: Integer（0-正常 1-已使用 2-已撤销 3-已过期）
    - useCount: Integer（使用次数）
    - maxUseCount: Integer（最大使用次数）
    - createTime: LocalDateTime

二维码数据结构（加密前）:
  {
    "passportId": 123456,
    "visitorId": 789,
    "visitorName": "张三",
    "visitDate": "2024-12-24",
    "validTime": ["09:00", "18:00"],
    "areas": [1, 2, 3],
    "timestamp": 1703423400000
  }

验证标准:
  ✅ 二维码生成成功
  ✅ 二维码数据加密
  ✅ 有效期控制准确
  ✅ 使用次数限制
  ✅ 自动撤销过期证
```

**注意事项**：
1. ⚠️ 二维码必须加密，防止伪造
2. ⚠️ 二维码有效期建议不超过24小时
3. ⚠️ 使用次数需要合理限制（1次或多次）
4. ⚠️ 二维码失效后自动清理
5. ⚠️ 支持刷新二维码（防截屏）

---

#### 1.5 视频监控模块（10项，23人天）

##### 任务1.5.1: 地图展示功能（6人天）

**需求描述**：
```
功能名称: 视频监控地图展示
业务价值: 可视化设备分布，快速定位和调取
技术要求: 地图组件、设备标注、实时状态、点击预览
```

**开发规范**：

```yaml
后端实现:
  Controller:
    - VideoMapController
    - API:
      - GET /api/v1/video/map/devices（地图设备数据）
      - GET /api/v1/video/map/{id}/preview（点击预览）
  Service:
    - VideoMapService
  Manager:
    - 无需新增Manager
  DAO:
    - 无需新增DAO

前端实现:
  页面: smart-admin-web-javascript/src/views/business/video/video-map.vue
  组件:
    - MapContainer（地图容器）
    - DeviceMarker（设备标注）
    - VideoPreview（视频预览）
    - DeviceFilter（设备筛选）

技术选型:
  地图组件: 百度地图API / 高德地图API / Leaflet（开源）
  视频播放: Video.js / h265web.js（H265支持）

移动端实现:
  页面: smart-app/src/pages/video/map.vue
  功能: 地图展示、设备点击、视频预览

数据模型:
  地图设备数据:
    - deviceId: String
    - deviceName: String
    - latitude: Double
    - longitude: Double
    - status: Integer（1-在线 2-离线）
    - deviceType: Integer
    - streamUrl: String（播放地址）

验证标准:
  ✅ 地图加载正常
  ✅ 设备标注准确
  ✅ 实时状态更新
  ✅ 点击快速预览
  ✅ 支持设备筛选
```

**注意事项**：
1. ⚠️ 地图API需要申请密钥（Key）
2. ⚠️ 设备GPS信息需要提前配置
3. ⚠️ 大量设备标注可能影响性能，需要聚合显示
4. ⚠️ 需要支持设备搜索和定位
5. ⚠️ 移动端地图性能优化（避免卡顿）

---

## 📐 开发规范

### 2.1 代码规范

#### 2.1.1 后端开发规范

```yaml
包结构规范:
  net.lab1024.sa.{service}.controller      # Controller层
  net.lab1024.sa.{service}.service        # Service接口
  net.lab1024.sa.{service}.service.impl    # Service实现
  net.lab1024.sa.{service}.manager        # Manager层（业务编排）
  net.lab1024.sa.{service}.dao            # DAO层
  net.lab1024.sa.{service}.domain         # 领域对象（Form/VO）
  net.lab1024.sa.{service}.config         # 配置类

命名规范:
  Controller: XxxController（如DeviceController）
  Service接口: XxxService（如DeviceService）
  Service实现: XxxServiceImpl（如DeviceServiceImpl）
  Manager: XxxManager（如DeviceManager）
  DAO: XxxDao（如DeviceDao）
  Entity: XxxEntity（如DeviceEntity）
  Form: XxxForm / XxxAddForm / XxxUpdateForm
  VO: XxxVO / XxxListVO / XxxDetailVO

注解规范:
  Controller:
    - @RestController
    - @RequestMapping("/api/v1/xxx")
    - @Tag(name = "XXX管理", description = "XXX接口")
    - @RequiredArgsConstructor（Lombok）

  Service实现:
    - @Service
    - @Transactional(rollbackFor = Exception.class)
    - @Slf4j
    - @RequiredArgsConstructor

  Manager:
    - @Component或@Service（根据是否需要Spring管理）
    - @Slf4j
    - 构造函数注入依赖

  DAO:
    - @Mapper
    - 无需Spring注解（MyBatis-Plus自动扫描）

方法命名规范:
  查询: getXxx, queryXxx, listXxx, pageXxx
  新增: addXxx, createXxx, insertXxx
  修改: updateXxx, modifyXxx
  删除: deleteXxx, removeXxx
  统计: countXxx, statisticsXxx

日志规范:
  必须使用 @Slf4j 注解
  日志级别: ERROR（异常）、WARN（警告）、INFO（业务关键点）、DEBUG（调试）
  日志格式: [模块名] 操作描述: 参数1={}, 参数2={}
  禁止: System.out.println、printStackTrace
```

#### 2.1.2 前端开发规范

```yaml
目录结构规范:
  src/views/business/{module}/          # 业务页面
  src/components/business/              # 业务组件
  src/api/business/{module}/            # API接口
  src/store/modules/business/            # 状态管理

命名规范:
  页面: xxx.vue（kebab-case，如device-list.vue）
  组件: PascalCase（如DeviceCard.vue）
  API: xxxApi（如deviceApi）
  Store: xxxStore（如deviceStore）

组件规范:
  <template>
    <!-- 根节点只能有一个元素 -->
    <view class="page-container">
      <!-- 内容 -->
    </view>
  </template>

  <script setup>
  import { ref, reactive, computed, onMounted } from 'vue'

  // 响应式数据
  const data = ref([])
  const config = reactive({})

  // 计算属性
  const computed = computed(() => {})

  // 生命周期
  onMounted(() => {})

  // 方法
  const method = () => {}
  </script>

  <style lang="scss" scoped>
  // 样式
  </style>

样式规范:
  使用SCSS预处理器
  使用BEM命名（block__element--modifier）
  使用rpx单位（移动端适配）
  避免深层嵌套（最多3层）
```

#### 2.1.3 API接口规范

```yaml
RESTful API规范:
  GET /api/v1/resource              # 获取列表
  GET /api/v1/resource/{id}         # 获取详情
  POST /api/v1/resource             # 创建资源
  PUT /api/v1/resource/{id}         # 更新资源
  DELETE /api/v1/resource/{id}      # 删除资源
  GET /api/v1/resource/page         # 分页查询

响应格式统一:
  成功:
    {
      "code": 200,
      "message": "success",
      "data": { ... },
      "timestamp": 1703423400000
    }

  失败:
    {
      "code": 400,
      "message": "参数错误",
      "data": null,
      "timestamp": 1703423400000
    }

错误码规范:
  200: 成功
  400: 请求参数错误
  401: 未授权
  403: 禁止访问
  404: 资源不存在
  500: 服务器内部错误
  1000+: 业务错误码

分页参数:
  pageNum: 当前页码（从1开始）
  pageSize: 每页大小
  返回: PageResult<T>

排序参数:
  orderBy: 排序字段
  sortOrder: asc/desc
```

### 2.2 数据库设计规范

```yaml
表命名规范:
  前缀: t_业务模块_功能
  示例: t_access_device（门禁设备表）
       t_attendance_shift（考勤班次表）
       t_consume_account（消费账户表）

字段命名规范:
  主键: id（Long，自增或雪花算法）
  外键: xxx_id（如user_id, device_id）
  创建时间: create_time（LocalDateTime）
  更新时间: update_time（LocalDateTime）
  删除标记: deleted_flag（Boolean/TinyInt）
  版本号: version（Integer，乐观锁）

索引规范:
  主键索引: PRIMARY KEY (id)
  唯一索引: UNIQUE KEY uk_xxx (xxx)
  普通索引: INDEX idx_xxx (xxx)
  联合索引: INDEX idx_xxx_yyy (xxx, yyy)

字段类型规范:
  字符串: VARCHAR(n)或TEXT
  整数: INT或BIGINT
  小数: DECIMAL(m, d)
  日期: DATETIME或LocalDateTime
  布尔: TINYINT(1)

字段约束:
  NOT NULL: 非空约束
  DEFAULT: 默认值
  COMMENT: 字段注释（必须）
```

### 2.3 测试规范

```yaml
单元测试规范:
  覆盖率目标: ≥80%
  测试框架: JUnit 5 + Mockito
  测试类命名: XxxTest（如DeviceServiceTest）
  测试方法命名: test_method_scenario_expectedResult

  示例:
  @Test
  void testGetDeviceById_deviceExists_returnDevice() {
      // given
      Long deviceId = 1L;
      DeviceEntity mockDevice = createMockDevice();
      when(deviceDao.selectById(deviceId)).thenReturn(mockDevice);

      // when
      DeviceEntity result = deviceService.getDeviceById(deviceId);

      // then
      assertNotNull(result);
      assertEquals(deviceId, result.getDeviceId());
  }

集成测试规范:
  覆盖率目标: ≥70%
  测试框架: Spring Boot Test + MockMvc
  测试重点: API接口、业务流程、数据流转

E2E测试规范:
  覆盖率目标: ≥60%
  测试框架: Playwright / Selenium
  测试重点: 用户操作流程、关键业务场景
```

### 2.4 文档规范

```yaml
代码注释规范:
  类注释: 说明类的职责和功能
  方法注释: 说明方法的功能、参数、返回值
  复杂逻辑注释: 解释算法和业务逻辑
  禁止无意义注释（如// i++）

API文档规范:
  使用Swagger/OpenAPI 3.0自动生成
  包含: 接口说明、请求参数、响应格式、错误码、示例

技术文档规范:
  概要设计: 需求背景、功能清单、架构设计
  详细设计: 数据模型、业务流程、接口设计
  用户手册: 操作步骤、截图说明、常见问题
  运维文档: 部署指南、配置说明、故障处理
```

## ⚠️ 注意事项

### 3.1 开发注意事项

```yaml
性能注意事项:
  1. 避免N+1查询问题（使用MyBatis-Plus的@TableField或JOIN）
  2. 大数据量查询必须分页（每页< 1000条）
  3. 列表查询必须加索引
  4. 避免大事务（事务尽可能短）
  5. 使用Redis缓存热点数据

安全注意事项:
  1. 所有输入必须校验（@Valid或@Validated）
  2. 密码必须加密存储（BCrypt）
  3. 敏感信息必须脱敏（手机号、身份证）
  4. 接口必须权限控制（@RequiresPermission）
  5. 防止SQL注入（使用MyBatis-Plus参数绑定）

并发注意事项:
  1. 查询接口加缓存（Redis）
  2. 更新接口加分布式锁（Redisson）
  3. 库存扣减使用原子操作（Lua脚本）
  4. 消息队列削峰填谷
  5. 限流防止雪崩（Guava RateLimiter）
```

### 3.2 全局一致性保障

```yaml
数据一致性保障:
  1. 前后端数据模型完全对齐（Entity ↔ Form/VO）
  2. API响应格式统一（ResponseDTO）
  3. 错误码体系统一
  4. 时间格式统一（LocalDateTime / ISO 8601）
  5. 金额精度统一（BigDecimal，2位小数）

业务一致性保障:
  1. 统一的业务术语（设备/区域/用户）
  2. 统一的状态枚举（使用常量或枚举类）
  3. 统一的审批流程（使用工作流引擎）
  4. 统一的权限模型（RBAC）
  5. 统一的日志格式

技术一致性保障:
  1. 统一的框架版本（Spring Boot 3.5.8）
  2. 统一的依赖管理（parent POM）
  3. 统一的代码规范（CheckStyle + P3C）
  4. 统一的构建工具（Maven 3.9+）
  5. 统一的部署方式（Docker）
```

### 3.3 避免冗余

```yaml
代码冗余消除:
  1. 抽取公共业务组件（设备选择、用户选择、区域选择）
  2. 抽取通用工具类（日期处理、字符串处理、文件处理）
  3. 抽取通用注解（日志、权限、缓存）
  4. 抽取统一异常处理
  5. 抽取统一响应封装

功能冗余消除:
  1. 统一设备管理（不允许各模块重复实现设备CRUD）
  2. 统一用户管理（不允许各模块重复实现用户CRUD）
  3. 统一区域管理（不允许各模块重复实现区域CRUD）
  4. 统一审批流程（使用工作流引擎，不允许硬编码）
  5. 统一日志记录（使用日志注解，不允许手动记录）

数据冗余消除:
  1. 使用视图（View）而非冗余字段
  2. 使用JOIN查询而非数据重复
  3. 使用缓存而非重复查询
  4. 定期清理历史数据
  5. 使用分区表优化大表
```

---

## 📈 实施计划

### 总体时间线

```yaml
Phase 1: P0级核心功能（第1-2月）
  团队规模: 5人
  工作量: 132人天
  目标: 核心业务功能完整，系统可用性提升到85%

Phase 2: P1级重要功能（第3-4月）
  团队规模: 4人
  工作量: 96人天
  目标: 用户体验提升，系统完整性提升到92%

Phase 3: P2级优化功能（第5-6月）
  团队规模: 2人
  工作量: 45人天
  目标: 系统完善度提升到98%，达到生产级标准

总计: 6个月，58项任务，273人天
```

### 详细里程碑

```yaml
里程碑1: 门禁管理核心功能完成（第1个月）
  ✅ 设备自动发现功能上线
  ✅ 批量设备导入功能上线
  ✅ 全局反潜回功能上线
  ✅ 实时监控告警功能上线
  验收标准: 功能测试通过，性能达标

里程碑2: 考勤管理核心功能完成（第2个月）
  ✅ 智能排班算法上线
  ✅ 考勤规则配置功能上线
  ✅ 异常申诉审批流程上线
  ✅ 跨天班次处理修复
  验收标准: 功能测试通过，用户满意度≥85%

里程碑3: 消费管理核心功能完成（第3个月）
  ✅ 离线消费同步机制上线
  ✅ 补贴管理规则引擎上线
  ✅ 商品库存管理功能上线
  验收标准: 功能测试通过，数据零丢失

里程碑4: 访客管理核心功能完成（第4个月）
  ✅ 访客预约审批流程上线
  ✅ 电子通行证管理上线
  ✅ 黑名单自动识别上线
  验收标准: 功能测试通过，访客体验提升

里程碑5: 视频监控核心功能完成（第5个月）
  ✅ 地图展示功能上线
  ✅ 解码上墙管理完善
  ✅ 视频质量诊断上线
  验收标准: 功能测试通过，监控覆盖率100%

里程碑6: 系统整体优化完成（第6个月）
  ✅ 所有P0/P1级功能上线
  ✅ P2级优化完成
  ✅ 文档完善率100%
  ✅ 测试覆盖率达标
  验收标准: 生产环境就绪，可对外交付
```

---

## ✅ 验收标准

### 功能验收

```yaml
P0级功能验收:
  ✅ 所有19项P0级功能已实现并测试通过
  ✅ 核心业务流程无阻塞性问题
  ✅ 关键功能性能达标（响应时间< 500ms）
  ✅ 安全性测试通过（无高危漏洞）

P1级功能验收:
  ✅ 所有24项P1级功能已实现并测试通过
  ✅ 用户体验良好（满意度≥85%）
  ✅ 扩展性良好（支持新功能扩展）

P2级功能验收:
  ✅ 所有15项P2级功能已实现并测试通过
  ✅ 系统性能优秀（响应时间< 300ms）
  ✅ 代码质量优秀（SonarQube评分A+）
```

### 质量验收

```yaml
代码质量:
  ✅ SonarQube评分A+（≥90分）
  ✅ 代码规范符合率≥95%
  ✅ 无高危安全漏洞
  ✅ 无代码坏味道

测试覆盖:
  ✅ 单元测试覆盖率≥80%
  ✅ 集成测试覆盖率≥70%
  ✅ E2E测试覆盖率≥60%
  ✅ 性能测试通过

文档完整:
  ✅ API文档完整率100%
  ✅ 用户手册完整率100%
  ✅ 运维文档完整率100%
  ✅ 代码注释完整率≥90%
```

### 性能验收

```yaml
响应时间:
  ✅ API接口P95响应时间< 500ms
  ✅ 页面首屏加载时间< 2s
  ✅ 移动端页面切换< 200ms

吞吐量:
  ✅ 支持并发用户数≥1000
  ✅ TPS≥500（核心接口）

稳定性:
  ✅ 系统可用性≥99.5%
  ✅ MTTR（平均恢复时间）< 30分钟
  ✅ MTBF（平均故障间隔）≥ 168小时
```

---

## 📚 附录

### A. 术语表

| 术语 | 全称 | 说明 |
|------|------|------|
| P0/P1/P2 | Priority 0/1/2 | 优先级，P0最高 |
| API | Application Programming Interface | 应用程序编程接口 |
| CRUD | Create, Read, Update, Delete | 增删改查 |
| DAO | Data Access Object | 数据访问对象 |
| VO | Value Object | 值对象 |
| DTO | Data Transfer Object | 数据传输对象 |
| E2E | End to End | 端到端 |
| TPS | Transactions Per Second | 每秒事务数 |
| MTTR | Mean Time To Recovery | 平均恢复时间 |
| MTBF | Mean Time Between Failures | 平均故障间隔 |

### B. 参考文档

1. 《IOE-DREAM项目全局业务模块功能完整性分析报告》
2. 《IOE-DREAM CLAUDE.md - 全局架构规范》
3. 《移动端优化路线图》
4. 《集成测试方案》
5. 《性能优化指南》

### C. 工具和框架

#### 开发工具

```yaml
IDE: IntelliJ IDEA（推荐） / VS Code
版本控制: Git + GitHub
构建工具: Maven 3.9+
代码质量: SonarQube
项目管理: JIRA / ZenTao
```

#### 技术框架

```yaml
后端框架: Spring Boot 3.5.8
持久化: MyBatis-Plus 3.5.15
数据库: MySQL 8.0.35
缓存: Redis + Caffeine
安全: Spring Security + JWT
工作流: Flowable（可选）
规则引擎: Aviator

前端框架: Vue 3.4
UI组件: Ant Design Vue 4
状态管理: Pinia
HTTP客户端: Axios 1.6
构建工具: Vite 5

移动端框架: uni-app 3.0
UI组件: uni-ui 1.5
构建工具: Vite 4
```

---

**文档版本**: v1.0.0
**创建时间**: 2024-12-24
**文档作者**: Claude Code AI 助手
**审核人**: 待定
**批准人**: 待定

---

*本文档将根据实际执行情况持续更新*
