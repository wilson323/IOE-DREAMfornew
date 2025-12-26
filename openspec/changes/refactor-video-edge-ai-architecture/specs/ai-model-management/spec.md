# Spec Delta: AI模型管理能力

**变更ID**: refactor-video-edge-ai-architecture
**能力**: ai-model-management
**状态**: 🟡 提案阶段
**创建日期**: 2025-01-30

---

## ADDED Requirements

### REQ-AI-MODEL-001: AI模型版本管理

**优先级**: P1
**描述**: 服务器端必须能够管理边缘设备的AI模型版本

**场景**:

#### Scenario: 注册新AI模型
**Given** AI团队开发了新版本跌倒检测模型 v2.0
**When** 管理员上传模型文件
**Then** 系统创建模型记录：
```json
{
  "modelId": "model_fall_detection_v2",
  "modelName": "跌倒检测模型",
  "modelVersion": "2.0.0",
  "modelType": "FALL_DETECTION",
  "modelPath": "/models/fall_detection_v2.tflite",
  "modelSize": 15728640,
  "status": "ACTIVE",
  "createTime": "2025-01-30T10:00:00Z"
}
```
**And** 模型文件存储到文件系统或对象存储
**And** 模型可通过API查询

#### Scenario: 查询设备模型版本
**Given** 摄像头设备已部署AI模型
**When** 管理员查询设备模型版本
**Then** 返回：
```json
{
  "deviceId": "camera_001",
  "currentModel": {
    "modelId": "model_fall_detection_v1",
    "modelVersion": "1.5.0",
    "updateTime": "2025-01-15T10:00:00Z"
  },
  "availableUpdate": {
    "modelId": "model_fall_detection_v2",
    "modelVersion": "2.0.0",
    "releaseTime": "2025-01-30T10:00:00Z"
  }
}
```
**And** 显示是否有新版本可用

---

### REQ-AI-MODEL-002: AI模型推送功能

**优先级**: P1
**描述**: 服务器端必须能够将AI模型推送到边缘设备

**场景**:

#### Scenario: 推送模型到单个设备
**Given** 有新版本跌倒检测模型
**When** 管理员选择推送到 camera_001
**Then** 系统执行：
  1. 加载模型文件
  2. 封装设备协议指令
  3. 建立设备连接
  4. 分块传输模型数据
  5. 验证传输完整性
  6. 设备返回确认
**And** 推送过程显示进度：
```json
{
  "taskId": "task_model_push_001",
  "deviceId": "camera_001",
  "modelId": "model_fall_detection_v2",
  "progress": 45,
  "status": "PUSHING",
  "startTime": "2025-01-30T10:05:00Z"
}
```
**And** 推送成功后更新设备模型版本记录

#### Scenario: 批量推送模型到多个设备
**Given** 有10台摄像头需要更新模型
**When** 管理员选择批量推送
**Then** 系统并行推送到所有设备
**And** 每个设备独立跟踪推送进度
**And** 失败设备自动重试3次
**And** 显示推送汇总：
  - 成功: 8台
  - 失败: 1台
  - 进行中: 1台

---

### REQ-AI-MODEL-003: 模型热更新机制

**优先级**: P1
**描述**: 支持在不中断服务的情况下更新AI模型

**场景**:

#### Scenario: 灰度发布新模型
**Given** 有新版本跌倒检测模型
**When** 管理员选择灰度发布
**Then** 系统执行：
  1. 第一批：推送到10%设备（监控24小时）
  2. 验证指标：
     - 检测准确率 > 90%
     - 误报率 < 5%
     - 设备性能无异常
  3. 第二批：推送到50%设备（监控48小时）
  4. 第三批：推送到剩余设备
**And** 每批发布后生成效果报告
**And** 发现问题自动回滚

#### Scenario: 模型回滚
**Given** 新模型v2.0出现误报率过高问题
**When** 管理员执行回滚操作
**Then** 系统执行：
  1. 停止新模型推送
  2. 推送旧模型v1.5到已更新设备
  3. 记录回滚日志：
```json
{
  "rollbackId": "rollback_001",
  "fromModel": "v2.0.0",
  "toModel": "v1.5.0",
  "reason": "误报率过高(15%)",
  "rollbackTime": "2025-01-30T15:00:00Z",
  "affectedDevices": 50
}
```
**And** 回滚完成后发送通知

---

### REQ-AI-MODEL-004: 模型效果监控

**优先级**: P2
**描述**: 监控AI模型在边缘设备的运行效果

**场景**:

#### Scenario: 查看模型性能指标
**Given** 模型已部署到设备
**When** 管理员查看模型监控
**Then** 显示：
  - 检测准确率：92%
  - 误报率：3.5%
  - 漏报率：4.5%
  - 平均推理时间：45ms
  - 设备CPU使用率：35%
  - 模型内存占用：120MB
**And** 对比新旧模型性能差异
**And** 标记异常指标

#### Scenario: 模型效果告警
**Given** 模型运行中
**When** 检测到模型性能异常（误报率>10%）
**Then** 系统自动告警
**And** 告警包含：
  - 异常指标：误报率 12%
  - 影响设备：camera_001, camera_005
  - 建议操作：回滚到v1.5
**And** 通知管理员处理

---

## MODIFIED Requirements

### MODIFIED-REQ-DEVICE-AI-EVENT-001: 设备AI事件上报

**原需求**: 设备上报AI事件

**新需求**: 设备AI事件必须包含模型版本信息

**场景**:

#### Scenario: AI事件包含模型版本
**Given** 设备使用v2.0模型完成分析
**When** 设备上报AI事件
**Then** 事件包含：
```json
{
  "eventId": "evt_20250130_001",
  "eventType": "FALL_DETECTION",
  "modelInfo": {
    "modelId": "model_fall_detection_v2",
    "modelVersion": "2.0.0"
  },
  "confidence": 0.95,
  ...
}
```
**And** 服务器记录使用的模型版本
**And** 支持按模型版本查询事件

---

## 验收标准

### 功能验收
- [ ] 模型注册和版本管理完成
- [ ] 模型推送到单个设备成功
- [ ] 批量推送功能正常
- [ ] 灰度发布机制完整
- [ ] 模型回滚功能可靠
- [ ] 设备模型版本查询准确
- [ ] 模型效果监控数据完整

### 性能验收
- [ ] 单个模型推送时间 < 5分钟（100MB模型）
- [ ] 批量推送支持100台设备并发
- [ ] 推送失败重试成功率 > 95%
- [ ] 模型切换不中断视频流

### 架构验收
- [ ] 符合边缘计算架构
- [ ] 模型管理统一集中
- [ ] 设备端AI自主运行
- [ ] 通过架构审查委员会评审

---

**📅 创建时间**: 2025-01-30
**👤 负责人**: 视频服务团队 + 设备通讯团队
**🔗 相关Spec**: device-ai-event-receiving, alarm-rule-engine
