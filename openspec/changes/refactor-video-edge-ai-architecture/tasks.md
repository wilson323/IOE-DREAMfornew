# 实施任务清单：视频监控边缘AI架构重构

**变更ID**: refactor-video-edge-ai-architecture
**状态**: 🟡 提案阶段
**创建日期**: 2025-01-30
**预计总工时**: 5周

---

## 📋 任务优先级说明

- **P0**: 阻塞性任务，必须立即完成（架构修复）
- **P1**: 重要任务，2周内完成（核心功能）
- **P2**: 增强任务，1个月内完成（优化改进）

---

## 阶段1：架构修复（P0，1周）

### 1.1 删除服务器端AI分析代码

**任务ID**: TASK-001
**优先级**: P0
**预计时间**: 2天
**负责模块**: ioedream-video-service

**具体任务**:
1. ✅ 识别所有服务器端AI分析方法
   - 检查 `BehaviorDetectionManager.java`
   - 搜索所有 `byte[] frameData` 参数
   - 搜索所有 `AI模型` 相关TODO
2. ✅ 删除 `BehaviorDetectionManager.detectFall()` 方法
   - 文件: `microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/manager/BehaviorDetectionManager.java:161-182`
   - 删除方法实现和所有相关TODO
3. ✅ 删除 `BehaviorDetectionManager.detectAbnormalBehaviors()` 方法
   - 删除所有行为检测相关方法
4. ✅ 清理相关测试代码
   - 删除测试服务器端AI分析的测试用例
   - 更新测试注释

**验证标准**:
- [ ] 代码中不存在 `byte[] frameData` 参数
- [ ] 代码中不存在AI模型集成TODO
- [ ] 编译成功，无编译错误
- [ ] 所有测试通过

**依赖**: 无

---

### 1.2 创建设备AI事件接收服务

**任务ID**: TASK-002
**优先级**: P0
**预计时间**: 3天
**负责模块**: ioedream-video-service

**具体任务**:
1. ✅ 创建设备AI事件实体类
   - 文件: `microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/domain/entity/DeviceAIEvent.java`
   - 字段: eventId, deviceId, eventType, confidence, bbox, snapshot, timestamp
2. ✅ 创建设备AI事件DAO
   - 文件: `microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/dao/DeviceAIEventDao.java`
   - 表: `t_video_device_ai_event`
3. ✅ 创建设备AI事件接收Service
   - 文件: `microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/service/DeviceAIEventReceiver.java`
   - 方法: `handleDeviceAIEvent(DeviceAIEvent event)`
4. ✅ 创建设备AI事件接收Controller
   - 文件: `microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/controller/DeviceAIEventController.java`
   - API: `POST /api/v1/video/device/ai/event`
5. ✅ 创建数据库迁移脚本
   - 文件: `microservices/ioedream-video-service/src/main/resources/db/migration/V20250130__create_device_ai_event_table.sql`

**验证标准**:
- [ ] 设备AI事件能成功接收并存储
- [ ] 事件字段完整（eventType, confidence, bbox, snapshot）
- [ ] API能正常调用
- [ ] 数据库表结构正确
- [ ] 单元测试覆盖率>80%

**依赖**: TASK-001（删除旧代码）

---

### 1.3 创建告警规则引擎

**任务ID**: TASK-003
**优先级**: P0
**预计时间**: 2天
**负责模块**: ioedream-video-service

**具体任务**:
1. ✅ 创建告警规则实体类
   - 文件: `microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/domain/entity/AlarmRuleEntity.java`
   - 字段: ruleId, ruleName, eventType, minConfidence, alarmLevel, action
2. ✅ 创建告警规则DAO
   - 文件: `microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/dao/AlarmRuleDao.java`
3. ✅ 创建告警规则引擎Service
   - 文件: `microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/service/AlarmRuleEngine.java`
   - 方法: `matchRule(DeviceAIEvent event) -> AlarmAction`
4. ✅ 创建告警规则管理Controller
   - 文件: `microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/controller/AlarmRuleController.java`
   - API: CRUD接口

**验证标准**:
- [ ] 告警规则能正确匹配AI事件
- [ ] 告警等级评估准确
- [ ] 告警联动配置生效
- [ ] 单元测试覆盖率>80%

**依赖**: TASK-002（事件接收服务）

---

### 1.4 更新所有TODO注释

**任务ID**: TASK-004
**优先级**: P0
**预计时间**: 1天
**负责模块**: ioedream-video-service

**具体任务**:
1. ✅ 搜索所有AI模型相关TODO
   ```bash
   grep -r "AI模型" microservices/ioedream-video-service/src
   grep -r "集成.*检测" microservices/ioedream-video-service/src
   ```
2. ✅ 更新TODO注释为正确架构
   - 旧: `TODO: 集成跌倒检测AI模型`
   - 新: `设备端AI分析，服务器接收结构化事件`
3. ✅ 添加边缘计算架构说明注释
   - 在关键类添加架构说明
   - 引用 CLAUDE.md 边缘计算章节

**验证标准**:
- [ ] 代码中不存在AI模型集成TODO
- [ ] 所有TODO注释符合边缘计算架构
- [ ] 代码审查通过

**依赖**: TASK-001, TASK-002

---

### 1.5 创建架构决策记录（ADR）

**任务ID**: TASK-005
**优先级**: P0
**预计时间**: 1天
**负责模块**: 文档

**具体任务**:
1. ✅ 创建ADR文档
   - 文件: `documentation/decisions/ADR-001-edge-ai-architecture.md`
   - 内容:
     - 背景：为什么服务器端AI分析是错误的
     - 决策：采用边缘计算架构
     - 影响分析：性能、成本、可扩展性
     - 权衡：设备要求 vs 服务器负载
2. ✅ 更新 CLAUDE.md
   - 强化边缘计算架构说明
   - 添加架构违规示例

**验证标准**:
- [ ] ADR文档完整清晰
- [ ] CLAUDE.md更新准确
- [ ] 架构委员会评审通过

**依赖**: TASK-001, TASK-002

---

## 阶段2：模型管理（P1，2周）

### 2.1 创建AI模型管理服务

**任务ID**: TASK-006
**优先级**: P1
**预计时间**: 5天
**负责模块**: ioedream-video-service

**具体任务**:
1. ✅ 创建AI模型实体类
   - 文件: `microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/domain/entity/AIModelEntity.java`
   - 字段: modelId, modelName, modelVersion, modelType, modelPath, status
2. ✅ 创建AI模型DAO
   - 文件: `microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/dao/AIModelDao.java`
3. ✅ 创建AI模型管理Service
   - 文件: `microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/service/AIModelManagerService.java`
   - 方法:
     - `pushModelToDevice(String modelId, String deviceId)`
     - `getDeviceModelVersion(String deviceId)`
     - `updateModel(String modelId, MultipartFile modelFile)`
4. ✅ 创建AI模型管理Controller
   - 文件: `microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/controller/AIModelController.java`
   - API: CRUD + 推送接口

**验证标准**:
- [ ] AI模型CRUD功能正常
- [ ] 模型推送到设备成功
- [ ] 设备模型版本查询准确
- [ ] 单元测试覆盖率>80%
- [ ] 集成测试通过

**依赖**: TASK-003（告警规则引擎）

---

### 2.2 实现模型推送功能

**任务ID**: TASK-007
**优先级**: P1
**预计时间**: 3天
**负责模块**: ioedream-video-service, ioedream-device-comm-service

**具体任务**:
1. ✅ 扩展设备通讯服务
   - 文件: `microservices/ioedream-device-comm-service/src/main/java/net/lab1024/sa/device/service/DeviceProtocolService.java`
   - 添加模型推送协议方法
2. ✅ 创建模型推送任务
   - 文件: `microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/task/ModelPushTask.java`
   - 异步推送机制
3. ✅ 实现推送进度跟踪
   - 实时推送进度反馈
   - 失败重试机制

**验证标准**:
- [ ] 模型能成功推送到设备
- [ ] 推送进度实时可见
- [ ] 推送失败自动重试
- [ ] 集成测试通过

**依赖**: TASK-006（AI模型管理服务）

---

### 2.3 实现设备模型版本查询

**任务ID**: TASK-008
**优先级**: P1
**预计时间**: 2天
**负责模块**: ioedream-video-service, ioedream-device-comm-service

**具体任务**:
1. ✅ 扩展设备通讯服务
   - 添加模型版本查询协议方法
2. ✅ 创建模型版本同步任务
   - 定时同步设备模型版本
   - 版本差异告警
3. ✅ 创建模型版本管理界面
   - 设备模型版本列表
   - 版本分布统计

**验证标准**:
- [ ] 设备模型版本查询准确
- [ ] 版本同步及时
- [ ] 版本差异告警生效
- [ ] 单元测试覆盖率>80%

**依赖**: TASK-007（模型推送功能）

---

### 2.4 实现模型热更新机制

**任务ID**: TASK-009
**优先级**: P1
**预计时间**: 3天
**负责模块**: ioedream-video-service, ioedream-device-comm-service

**具体任务**:
1. ✅ 创建模型热更新Service
   - 文件: `microservices/ioedream-video-service/src/main/java/net/lab1024/sa/video/service/ModelHotUpdateService.java`
   - 方法: `hotUpdateModel(String modelId, List<String> deviceIds)`
2. ✅ 实现灰度发布机制
   - 分批推送模型
   - 监控模型效果
3. ✅ 实现模型回滚机制
   - 快速回滚到旧版本
   - 回滚日志记录

**验证标准**:
- [ ] 模型热更新无中断
- [ ] 灰度发布可控
- [ ] 模型回滚快速可靠
- [ ] 集成测试通过

**依赖**: TASK-008（模型版本查询）

---

## 阶段3：设备集成（P1，2周）

### 3.1 设备协议适配（AI事件上报）

**任务ID**: TASK-010
**优先级**: P1
**预计时间**: 3天
**负责模块**: ioedream-device-comm-service

**具体任务**:
1. ✅ 创建AI事件上报协议适配器
   - 文件: `microservices/ioedream-device-comm-service/src/main/java/net/lab1024/sa/device/protocol/ai/AIEventProtocolAdapter.java`
   - 支持多种设备厂商协议（海康、大华、宇视）
2. ✅ 实现协议解析
   - 解析设备上报的AI事件
   - 统一转换为DeviceAIEvent
3. ✅ 创建协议测试用例
   - 模拟设备上报事件
   - 验证解析正确性

**验证标准**:
- [ ] 支持主流设备厂商协议
- [ ] AI事件解析准确
- [ ] 单元测试覆盖率>80%
- [ ] 协议测试通过

**依赖**: TASK-002（设备AI事件接收服务）

---

### 3.2 设备协议适配（模型推送）

**任务ID**: TASK-011
**优先级**: P1
**预计时间**: 3天
**负责模块**: ioedream-device-comm-service

**具体任务**:
1. ✅ 创建模型推送协议适配器
   - 文件: `microservices/ioedream-device-comm-service/src/main/java/net/lab1024/sa/device/protocol/ai/ModelPushProtocolAdapter.java`
   - 支持多种设备厂商协议
2. ✅ 实现模型推送指令封装
   - 封装模型文件
   - 生成推送指令
3. ✅ 创建协议测试用例
   - 模拟模型推送
   - 验证推送成功

**验证标准**:
- [ ] 支持主流设备厂商协议
- [ ] 模型推送成功
- [ ] 单元测试覆盖率>80%
- [ ] 协议测试通过

**依赖**: TASK-007（模型推送功能）

---

### 3.3 设备AI能力测试

**任务ID**: TASK-012
**优先级**: P1
**预计时间**: 4天
**负责模块**: 测试团队

**具体任务**:
1. ✅ 创建设备AI能力测试环境
   - 准备支持AI的智能摄像头
   - 搭建测试网络环境
2. ✅ 创建端到端测试用例
   - 设备端AI分析测试
   - AI事件上报测试
   - 告警规则匹配测试
3. ✅ 性能测试
   - 测试告警延迟（目标<1秒）
   - 测试并发处理能力
   - 测试带宽节省（目标>95%）
4. ✅ 稳定性测试
   - 长时间运行测试
   - 异常场景测试

**验证标准**:
- [ ] 端到端测试通过
- [ ] 性能指标达标
- [ ] 稳定性测试通过
- [ ] 测试报告完整

**依赖**: TASK-010（AI事件上报协议）

---

### 3.4 端到端测试验证

**任务ID**: TASK-013
**优先级**: P1
**预计时间**: 3天
**负责模块**: 测试团队

**具体任务**:
1. ✅ 创建集成测试用例
   - 文件: `microservices/ioedream-video-service/src/test/java/net/lab1024/sa/video/integration/VideoEdgeAIIntegrationTest.java`
   - 测试场景:
     - 设备上报AI事件 → 服务器接收 → 告警规则匹配 → 实时推送
     - 模型推送 → 设备更新 → AI分析 → 事件上报
2. ✅ 创建性能测试用例
   - 文件: `microservices/ioedream-video-service/src/test/java/net/lab1024/sa/video/performance/VideoEdgeAIPerformanceTest.java`
   - 测试指标:
     - 事件处理延迟 < 100ms
     - 告警推送延迟 < 1s
     - 并发处理能力 > 500路视频
3. ✅ 执行完整测试
   - 功能测试
   - 性能测试
   - 稳定性测试
4. ✅ 生成测试报告
   - 功能覆盖率
   - 性能指标
   - 问题清单

**验证标准**:
- [ ] 所有测试用例通过
- [ ] 性能指标达标
- [ ] 测试报告完整
- [ ] 验收通过

**依赖**: TASK-012（设备AI能力测试）

---

## 验收任务

### 4.1 架构合规性检查

**任务ID**: TASK-014
**优先级**: P0
**预计时间**: 2天
**负责模块**: 架构委员会

**具体任务**:
1. ✅ 运行架构合规性检查脚本
   ```bash
   scripts/architecture-compliance-check.sh
   ```
2. ✅ 人工代码审查
   - 检查边缘计算架构符合性
   - 检查无服务器端AI分析代码
   - 检查依赖关系正确
3. ✅ 性能基准测试
   - 带宽消耗验证（目标：减少95%+）
   - 服务器负载验证（目标：降低70%+）
   - 告警延迟验证（目标：<1秒）
4. ✅ 生成架构合规性报告
   - 文件: `VIDEO_EDGE_AI_ARCHITECTURE_COMPLIANCE_REPORT.md`

**验证标准**:
- [ ] 架构合规性检查通过
- [ ] 性能指标达标
- [ ] 架构委员会评审通过
- [ ] 合规性报告完整

**依赖**: TASK-013（端到端测试验证）

---

### 4.2 文档更新

**任务ID**: TASK-015
**优先级**: P0
**预计时间**: 2天
**负责模块**: 文档团队

**具体任务**:
1. ✅ 更新 CLAUDE.md
   - 强化边缘计算架构说明
   - 添加架构决策引用
   - 更新视频模块架构描述
2. ✅ 更新业务模块文档
   - 文件: `documentation/业务模块/05-视频管理模块/04-行为分析/README.md`
   - 更新架构描述
   - 更新API接口文档
   - 更新技术实现说明
3. ✅ 创建架构迁移指南
   - 文件: `documentation/technical/VIDEO_EDGE_AI_MIGRATION_GUIDE.md`
   - 迁移步骤
   - 常见问题
   - 最佳实践
4. ✅ 更新 todo-list.md
   - 修正AI集成任务描述
   - 删除服务器端AI模型集成任务
   - 添加边缘计算架构任务

**验证标准**:
- [ ] 所有文档更新完整
- [ ] 文档与代码实现一致
- [ ] 文档审查通过

**依赖**: TASK-014（架构合规性检查）

---

## 📊 任务汇总

### 按优先级统计

| 优先级 | 任务数 | 预计时间 |
|--------|--------|----------|
| P0 | 8 | 2周 |
| P1 | 7 | 3周 |
| **总计** | **15** | **5周** |

### 按阶段统计

| 阶段 | 任务数 | 预计时间 | 状态 |
|------|--------|----------|------|
| 阶段1：架构修复 | 5 | 1周 | 🟡 待开始 |
| 阶段2：模型管理 | 4 | 2周 | ⏳ 待启动 |
| 阶段3：设备集成 | 4 | 2周 | ⏳ 待启动 |
| 验收 | 2 | 并行 | ⏳ 待启动 |

### 关键路径

```
TASK-001 (删除旧代码)
  ↓
TASK-002 (创建事件接收服务)
  ↓
TASK-003 (创建告警规则引擎)
  ↓
TASK-006 (创建AI模型管理服务)
  ↓
TASK-007 (实现模型推送功能)
  ↓
TASK-008 (实现模型版本查询)
  ↓
TASK-009 (实现模型热更新机制)
  ↓
TASK-010 (设备协议适配-事件上报)
  ↓
TASK-012 (设备AI能力测试)
  ↓
TASK-013 (端到端测试验证)
  ↓
TASK-014 (架构合规性检查)
  ↓
TASK-015 (文档更新)
```

### 并行任务

**阶段1可并行**:
- TASK-001 → TASK-002 → TASK-003（顺序依赖）
- TASK-004, TASK-005（可与TASK-002, TASK-003并行）

**阶段2可并行**:
- TASK-006 → TASK-007 → TASK-008 → TASK-009（顺序依赖）

**阶段3可并行**:
- TASK-010, TASK-011（可与阶段2部分任务并行）
- TASK-012（依赖TASK-010）
- TASK-013（依赖TASK-012）

---

## ✅ 验收标准总览

### 功能验收
- [ ] 服务器端不再接收原始视频帧进行AI分析
- [ ] 服务器端正确接收设备AI事件（结构化数据）
- [ ] 设备AI事件正确存储和索引
- [ ] 告警规则匹配正确工作
- [ ] 实时告警推送正常

### 性能验收
- [ ] 视频流上传带宽减少95%+
- [ ] 服务器CPU使用率降低70%+
- [ ] 告警延迟<1秒（设备端AI分析+上报）
- [ ] 系统可支持500路视频并发

### 架构验收
- [ ] 代码实现符合CLAUDE.md边缘计算架构
- [ ] 通过架构审查委员会评审
- [ ] 无架构违规问题

---

**📅 创建时间**: 2025-01-30
**✍️ 负责人**: 架构委员会 + 后端团队
**🔄 更新频率**: 每周更新任务进度
