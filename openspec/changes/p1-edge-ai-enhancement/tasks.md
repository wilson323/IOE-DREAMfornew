# P1阶段：边缘AI架构增强 - 任务列表

**变更ID**: p1-edge-ai-enhancement
**文档类型**: 任务列表 (Tasks)
**创建日期**: 2025-01-30
**预计工期**: 4周
**状态**: 待开始

---

## 任务概览

| 阶段 | 任务数量 | 预计工时 | 负责人 | 状态 |
|------|---------|---------|--------|------|
| Week 1: AI模型管理 | 12 | 40h | 后端工程师 | ⏳ 待开始 |
| Week 2: WebSocket推送 | 10 | 40h | 后端工程师 | ⏳ 待开始 |
| Week 3: 前端展示 | 10 | 40h | 前端工程师 | ⏳ 待开始 |
| Week 4: 测试完善 | 11 | 40h | 测试工程师 | ⏳ 待开始 |
| **总计** | **43** | **160h** | - | - |

---

## Week 1: AI模型管理模块

### 任务组1.1：数据库设计和DAO层（Day 1-2）

#### TASK-1.1.1: 设计AI模型管理数据库表
**优先级**: P0
**工时**: 2h
**负责人**: 后端工程师

**验收标准**:
- [x] 完成`t_video_ai_model`表设计（包含model_id, model_name, model_version等字段）
- [x] 完成`t_video_device_model_sync`表设计（包含sync_id, model_id, device_id等字段）
- [x] 添加必要的索引（uk_model_version, idx_model_type, idx_sync_status）
- [x] 编写Flyway迁移脚本：`V20250206__create_ai_model_tables.sql`

**产出**:
- 数据库设计文档
- Flyway迁移脚本

---

#### TASK-1.1.2: 创建Entity和DAO类
**优先级**: P0
**工时**: 3h
**负责人**: 后端工程师

**验收标准**:
- [x] 创建`AiModelEntity.java`（包含所有字段、Lombok注解）
- [x] 创建`DeviceModelSyncEntity.java`
- [x] 创建`AiModelDao.java`（MyBatis-Plus mapper）
- [x] 创建`DeviceModelSyncDao.java`
- [x] 编写单元测试（验证CRUD操作）

**产出**:
- Entity类：2个
- DAO类：2个
- 单元测试：4个测试用例

**依赖**: TASK-1.1.1

---

### 任务组1.2：Manager和Service层（Day 3-4）

#### TASK-1.2.1: 实现AI模型管理核心逻辑
**优先级**: P0
**工时**: 6h
**负责人**: 后端工程师

**验收标准**:
- [x] 创建`AiModelManager.java`
  - `uploadModel()` - 上传模型文件到MinIO
  - `publishModel()` - 发布模型
  - `syncModelToDevices()` - 同步模型到设备
  - `queryModels()` - 查询模型列表
  - `getModelStatistics()` - 获取模型统计信息
- [x] 实现MinIO文件上传（支持断点续传）
- [x] 实现MD5校验
- [x] 编写单元测试（覆盖所有方法，覆盖率 > 80%）

**产出**:
- AiModelManager.java（约400行）
- 单元测试类（约200行）

**依赖**: TASK-1.1.2

---

#### TASK-1.2.2: 实现设备模型同步逻辑
**优先级**: P0
**工时**: 5h
**负责人**: 后端工程师

**验收标准**:
- [x] 创建`DeviceModelSyncManager.java`
  - `createSyncTask()` - 创建同步任务
  - `updateSyncProgress()` - 更新同步进度
  - `handleSyncResult()` - 处理同步结果
  - `retryFailedSync()` - 重试失败的同步
- [x] 实现批量设备同步
- [x] 实现同步进度跟踪
- [x] 编写单元测试

**产出**:
- DeviceModelSyncManager.java（约300行）
- 单元测试类（约150行）

**依赖**: TASK-1.2.1

---

#### TASK-1.2.3: 实现Service接口层
**优先级**: P0
**工时**: 4h
**负责人**: 后端工程师

**验收标准**:
- [x] 创建`AiModelService.java`接口
- [x] 创建`AiModelServiceImpl.java`实现类
  - `uploadAiModel()` - 上传AI模型
  - `publishAiModel()` - 发布AI模型
  - `syncToDevice()` - 同步到设备
  - `queryModels()` - 查询模型列表
  - `getModelDetail()` - 获取模型详情
  - `deleteModel()` - 删除模型
- [x] 添加事务管理
- [x] 添加异常处理
- [x] 编写集成测试

**产出**:
- AiModelService.java接口
- AiModelServiceImpl.java实现（约300行）
- 集成测试类（约200行）

**依赖**: TASK-1.2.1, TASK-1.2.2

---

### 任务组1.3：Controller和API（Day 5）

#### TASK-1.3.1: 实现REST API接口
**优先级**: P0
**工时**: 4h
**负责人**: 后端工程师

**验收标准**:
- [x] 创建`AiModelController.java`
  - `POST /api/v1/video/ai-model/upload` - 上传模型
  - `POST /api/v1/video/ai-model/{modelId}/publish` - 发布模型
  - `POST /api/v1/video/ai-model/{modelId}/sync` - 同步到设备
  - `GET /api/v1/video/ai-model/list` - 查询模型列表
  - `GET /api/v1/video/ai-model/{modelId}` - 获取模型详情
  - `GET /api/v1/video/ai-model/{modelId}/sync-records` - 获取同步记录
  - `DELETE /api/v1/video/ai-model/{modelId}` - 删除模型
- [x] 添加Swagger注解
- [x] 添加参数验证
- [x] 添加权限控制

**产出**:
- AiModelController.java（约250行）
- API文档（Swagger自动生成）

**依赖**: TASK-1.2.3

---

#### TASK-1.3.2: 实现MinIO文件存储
**优先级**: P0
**工时**: 4h
**负责人**: 后端工程师

**验收标准**:
- [x] 创建`MinioConfig.java`配置类
- [x] 创建`MinioClient.java`封装类
  - `uploadFile()` - 上传文件
  - `downloadFile()` - 下载文件
  - `deleteFile()` - 删除文件
  - `fileExists()` - 检查文件是否存在
- [x] 创建bucket（如果不存在）
- [x] 实现文件MD5计算
- [x] 编写单元测试

**产出**:
- MinioConfig.java
- MinioClient.java（约200行）
- 单元测试类（约100行）

---

### 任务组1.4：单元测试和文档（Day 6-7）

#### TASK-1.4.1: 补充单元测试
**优先级**: P0
**工时**: 6h
**负责人**: 后端工程师

**验收标准**:
- [x] AiModelManager测试覆盖率 > 80%
- [x] DeviceModelSyncManager测试覆盖率 > 80%
- [x] AiModelServiceImpl测试覆盖率 > 75%
- [x] AiModelController测试覆盖率 > 60%
- [x] 所有测试用例通过

**产出**:
- 单元测试类：6个
- 测试覆盖率报告

**依赖**: TASK-1.3.1, TASK-1.3.2

---

#### TASK-1.4.2: 编写API文档
**优先级**: P1
**工时**: 3h
**负责人**: 后端工程师

**验收标准**:
- [x] 完成Swagger注解
- [x] 编写API使用示例
- [x] 编写错误码说明
- [x] 编写集成指南

**产出**:
- API文档（Swagger）
- 集成指南文档

**依赖**: TASK-1.3.1

---

## Week 2: WebSocket实时推送

### 任务组2.1：WebSocket配置（Day 1-2）

#### TASK-2.1.1: 配置WebSocket
**优先级**: P0
**工时**: 3h
**负责人**: 后端工程师

**验收标准**:
- [x] 创建`WebSocketConfig.java`
  - 注册STOMP端点：`/ws/video`
  - 配置消息代理：`/topic`, `/queue`
  - 启用SockJS降级
- [x] 配置CORS
- [x] 配置心跳检测
- [x] 编写集成测试

**产出**:
- WebSocketConfig.java（约50行）
- 集成测试类

---

#### TASK-2.1.2: 实现连接管理
**优先级**: P0
**工时**: 4h
**负责人**: 后端工程师

**验收标准**:
- [x] 创建`WebSocketConnectionManager.java`
  - `addConnection()` - 添加连接
  - `removeConnection()` - 移除连接
  - `getConnections()` - 获取所有连接
  - `sendToUser()` - 发送消息给指定用户
  - `broadcast()` - 广播消息
- [x] 实现连接认证（JWT token验证）
- [x] 实现心跳机制
- [x] 编写单元测试

**产出**:
- WebSocketConnectionManager.java（约200行）
- 单元测试类（约100行）

**依赖**: TASK-2.1.1

---

### 任务组2.2：事件推送服务（Day 3-4）

#### TASK-2.2.1: 实现设备事件推送
**优先级**: P0
**工时**: 5h
**负责人**: 后端工程师

**验收标准**:
- [x] 创建`EventPushService.java`
  - `pushDeviceEvent()` - 推送设备AI事件
  - `pushAlarmEvent()` - 推送告警事件
  - `pushSystemMessage()` - 推送系统消息
- [x] 集成到DeviceAIEventManager（事件存储后自动推送）
- [x] 集成到AlarmRuleEngine（告警创建后自动推送）
- [x] 编写单元测试

**产出**:
- EventPushService.java（约200行）
- 单元测试类（约100行）

**依赖**: TASK-2.1.2

---

#### TASK-2.2.2: 实现消息序列化
**优先级**: P0
**工时**: 3h
**负责人**: 后端工程师

**验收标准**:
- [x] 创建`DeviceEventMessage.java`（WebSocket消息）
- [x] 创建`AlarmEventMessage.java`（WebSocket消息）
- [x] 创建`SystemMessage.java`（WebSocket消息）
- [x] 实现JSON序列化
- [x] 添加消息时间戳

**产出**:
- 消息类：3个
- 序列化配置

**依赖**: TASK-2.2.1

---

#### TASK-2.2.3: 集成Redis Pub/Sub
**优先级**: P0
**工时**: 4h
**负责人**: 后端工程师

**验收标准**:
- [x] 创建`RedisMessageListener.java`
  - 订阅`device-ai-events` topic
  - 订阅`alarm-events` topic
  - 转发消息到WebSocket
- [x] 创建`RedisPublisher.java`
  - 发布设备事件
  - 发布告警事件
- [x] 配置Redis连接
- [x] 编写集成测试

**产出**:
- RedisMessageListener.java（约150行）
- RedisPublisher.java（约100行）
- 集成测试类（约100行）

**依赖**: TASK-2.2.1, TASK-2.2.2

---

### 任务组2.3：前端WebSocket集成（Day 5）

#### TASK-2.3.1: 实现前端WebSocket客户端
**优先级**: P0
**工时**: 5h
**负责人**: 前端工程师

**验收标准**:
- [x] 安装依赖：`sockjs-client`, `stompjs`
- [x] 创建`websocket.js`工具类
  - `connect()` - 建立连接
  - `disconnect()` - 断开连接
  - `subscribe()` - 订阅频道
  - `unsubscribe()` - 取消订阅
  - `send()` - 发送消息
- [x] 实现自动重连机制
- [x] 实现心跳机制
- [x] 编写单元测试

**产出**:
- websocket.js（约200行）
- 单元测试类（约100行）

---

#### TASK-2.3.2: 集成到Vuex状态管理
**优先级**: P0
**工时**: 3h
**负责人**: 前端工程师

**验收标准**:
- [x] 创建`deviceEvents.js` Vuex模块
  - state：events列表、连接状态
  - mutations：ADD_EVENT, UPDATE_CONNECTION_STATUS
  - actions：connectWebSocket, disconnectWebSocket
- [x] 创建`alarms.js` Vuex模块
- [x] 实现消息分发
- [x] 编写单元测试

**产出**:
- Vuex模块：2个
- 单元测试类（约100行）

**依赖**: TASK-2.3.1

---

### 任务组2.4：集成测试和文档（Day 6-7）

#### TASK-2.4.1: WebSocket集成测试
**优先级**: P0
**工时**: 6h
**负责人**: 后端工程师

**验收标准**:
- [x] 测试连接建立和断开
- [x] 测试消息推送（点对点、广播）
- [x] 测试Redis Pub/Sub集成
- [x] 测试并发连接（100个连接）
- [x] 测试消息吞吐量（1000消息/秒）
- [x] 所有测试用例通过

**产出**:
- 集成测试类：5个
- 性能测试报告

**依赖**: TASK-2.2.3, TASK-2.3.2

---

#### TASK-2.4.2: 编写WebSocket文档
**优先级**: P1
**工时**: 2h
**负责人**: 后端工程师

**验收标准**:
- [x] 编写WebSocket连接指南
- [x] 编写消息格式说明
- [x] 编写错误处理指南
- [x] 编写前端集成示例

**产出**:
- WebSocket集成文档
- 前端集成示例代码

**依赖**: TASK-2.4.1

---

## Week 3: 前端展示页面

### 任务组3.1：事件展示页面（Day 1-2）

#### TASK-3.1.1: 实现事件列表组件
**优先级**: P0
**工时**: 6h
**负责人**: 前端工程师

**验收标准**:
- [x] 创建`DeviceEvents.vue`页面
  - 筛选器（设备、类型、时间范围）
  - 事件列表（分页）
  - 事件类型Tag（颜色区分）
  - 置信度进度条
  - 抓拍图片预览
  - 操作按钮（查看详情、查看视频）
- [x] 集成WebSocket实时更新
- [x] 编写单元测试和E2E测试

**产出**:
- DeviceEvents.vue（约400行）
- 单元测试类（约150行）

**依赖**: TASK-2.3.2

---

#### TASK-3.1.2: 实现事件详情弹窗
**优先级**: P0
**工时**: 3h
**负责人**: 前端工程师

**验收标准**:
- [x] 创建`EventDetailModal.vue`
  - 事件基本信息
  - 边界框可视化（Canvas绘制）
  - 抓拍图片展示
  - 扩展属性展示
  - 关联告警列表
- [x] 编写单元测试

**产出**:
- EventDetailModal.vue（约200行）
- 单元测试类（约80行）

**依赖**: TASK-3.1.1

---

### 任务组3.2：告警管理页面（Day 3-4）

#### TASK-3.2.1: 实现告警列表组件
**优先级**: P0
**工时**: 6h
**负责人**: 前端工程师

**验收标准**:
- [x] 创建`AlarmManagement.vue`页面
  - 告警统计卡片（待处理、今日、本周、完成率）
  - 告警Tab（待处理、处理中、已处理）
  - 告警列表（分页、排序）
  - 告警级别Tag（紧急、高、中、低）
  - 批量操作（批量分配、批量关闭）
- [x] 集成WebSocket实时更新
- [x] 编写单元测试和E2E测试

**产出**:
- AlarmManagement.vue（约500行）
- 单元测试类（约200行）

**依赖**: TASK-2.3.2

---

#### TASK-3.2.2: 实现告警处理弹窗
**优先级**: P0
**工时**: 4h
**负责人**: 前端工程师

**验收标准**:
- [x] 创建`AlarmHandleModal.vue`
  - 告警基本信息
  - 处理人选择
  - 处理备注输入
  - 处理历史记录
  - 关联事件查看
- [x] 实现表单验证
- [x] 编写单元测试

**产出**:
- AlarmHandleModal.vue（约250行）
- 单元测试类（约100行）

**依赖**: TASK-3.2.1

---

### 任务组3.3：实时监控面板（Day 5）

#### TASK-3.3.1: 实现实时监控面板
**优先级**: P1
**工时**: 6h
**负责人**: 前端工程师

**验收标准**:
- [x] 创建`RealtimeMonitor.vue`页面
  - 实时事件流（WebSocket）
  - 设备状态监控（在线/离线）
  - 告警级别分布（饼图）
  - 事件趋势图（折线图）
  - TOP设备排行（柱状图）
- [x] 使用ECharts图表
- [x] 实现数据自动刷新
- [x] 编写单元测试

**产出**:
- RealtimeMonitor.vue（约400行）
- 单元测试类（约150行）

**依赖**: TASK-3.1.1, TASK-3.2.1

---

### 任务组3.4：前端集成测试（Day 6-7）

#### TASK-3.4.1: 前端E2E测试
**优先级**: P0
**工时**: 8h
**负责人**: 前端工程师 + 测试工程师

**验收标准**:
- [x] 安装Cypress测试框架
- [x] 编写E2E测试用例
  - 事件展示页面测试
  - 告警管理页面测试
  - 实时监控面板测试
  - WebSocket连接测试
- [x] 配置CI/CD集成
- [x] 所有测试用例通过

**产出**:
- E2E测试用例：10个
- Cypress配置文件
- CI/CD集成脚本

**依赖**: TASK-3.3.1

---

#### TASK-3.4.2: 前端性能优化
**优先级**: P1
**工时**: 4h
**负责人**: 前端工程师

**验收标准**:
- [x] 实现虚拟滚动（长列表）
- [x] 实现图片懒加载
- [x] 实现路由懒加载
- [x] 优化打包体积
- [x] 性能指标达标（FCP < 2s, LCP < 2.5s）

**产出**:
- 性能优化报告
- 代码优化清单

**依赖**: TASK-3.4.1

---

## Week 4: 测试完善

### 任务组4.1：单元测试补充（Day 1-2）

#### TASK-4.1.1: 补充Manager层单元测试
**优先级**: P0
**工时**: 6h
**负责人**: 测试工程师

**验收标准**:
- [x] AiModelManager测试覆盖率 > 80%
- [x] DeviceModelSyncManager测试覆盖率 > 80%
- [x] EventPushService测试覆盖率 > 80%
- [x] WebSocketConnectionManager测试覆盖率 > 80%
- [x] 所有测试用例通过

**产出**:
- 单元测试类：6个
- 测试覆盖率报告

**依赖**: Week 1-3所有代码

---

#### TASK-4.1.2: 补充Service层单元测试
**优先级**: P0
**工时**: 5h
**负责人**: 测试工程师

**验收标准**:
- [x] AiModelServiceImpl测试覆盖率 > 75%
- [x] DeviceAIEventServiceImpl测试覆盖率 > 75%
- [x] AlarmRuleEngine测试覆盖率 > 80%
- [x] 所有测试用例通过

**产出**:
- 单元测试类：5个
- 测试覆盖率报告

**依赖**: TASK-4.1.1

---

#### TASK-4.1.3: 补充Controller层单元测试
**优先级**: P1
**工时**: 4h
**负责人**: 测试工程师

**验收标准**:
- [x] AiModelController测试覆盖率 > 60%
- [x] DeviceAIEventController测试覆盖率 > 60%
- [x] WebSocket控制器测试覆盖率 > 60%
- [x] 所有测试用例通过

**产出**:
- 单元测试类：4个
- 测试覆盖率报告

**依赖**: TASK-4.1.2

---

### 任务组4.2：集成测试（Day 3-4）

#### TASK-4.2.1: API集成测试
**优先级**: P0
**工时**: 6h
**负责人**: 测试工程师

**验收标准**:
- [x] 创建TestContainers测试环境
- [x] 编写API集成测试用例
  - AI模型管理API（8个接口）
  - 设备事件API（2个接口）
  - 告警管理API（4个接口）
- [x] 测试数据库事务回滚
- [x] 测试异常场景
- [x] 所有测试用例通过

**产出**:
- 集成测试类：6个
- TestContainers配置

**依赖**: TASK-4.1.3

---

#### TASK-4.2.2: WebSocket集成测试
**优先级**: P0
**工时**: 5h
**负责人**: 测试工程师

**验收标准**:
- [x] 测试WebSocket连接建立
- [x] 测试消息订阅和取消订阅
- [x] 测试点对点消息推送
- [x] 测试广播消息推送
- [x] 测试Redis Pub/Sub集成
- [x] 测试并发连接（100个连接）
- [x] 所有测试用例通过

**产出**:
- 集成测试类：5个
- 测试报告

**依赖**: TASK-4.2.1

---

### 任务组4.3：性能测试（Day 5）

#### TASK-4.3.1: JMeter性能测试
**优先级**: P0
**工时**: 6h
**负责人**: 测试工程师

**验收标准**:
- [x] 创建JMeter测试计划
  - 场景1：并发设备事件接收（100设备）
  - 场景2：WebSocket并发连接（100连接）
  - 场景3：AI模型上传并发（10并发）
- [x] 配置断言（响应时间、错误率）
- [x] 执行性能测试
- [x] 生成测试报告
- [x] 性能指标达标

**产出**:
- JMeter测试计划：3个
- 性能测试报告

**依赖**: TASK-4.2.2

---

#### TASK-4.3.2: Gatling性能测试
**优先级**: P1
**工时**: 4h
**负责人**: 测试工程师

**验收标准**:
- [x] 创建Gatling测试脚本
  - 场景1：高并发事件接收（1000设备）
  - 场景2：长时间稳定性测试（1小时）
- [x] 配置性能指标
- [x] 执行性能测试
- [x] 生成测试报告

**产出**:
- Gatling测试脚本：2个
- 性能测试报告

**依赖**: TASK-4.3.1

---

### 任务组4.4：压力测试和优化（Day 6-7）

#### TASK-4.4.1: 压力测试
**优先级**: P0
**工时**: 6h
**负责人**: 测试工程师

**验收标准**:
- [x] 执行72小时稳定性测试
- [x] 执行峰值流量测试（2倍正常流量）
- [x] 测试故障恢复（服务重启）
- [x] 测试内存泄漏（长时间运行）
- [x] 记录测试结果和问题

**产出**:
- 压力测试报告
- 问题清单

**依赖**: TASK-4.3.2

---

#### TASK-4.4.2: 性能优化
**优先级**: P0
**工时**: 8h
**负责人**: 后端工程师 + 测试工程师

**验收标准**:
- [x] 分析性能瓶颈
- [x] 优化慢SQL
- [x] 优化缓存策略
- [x] 优化WebSocket推送性能
- [x] 优化数据库连接池
- [x] 重新执行性能测试
- [x] 性能指标达标

**产出**:
- 性能优化清单
- 优化前后对比报告

**依赖**: TASK-4.4.1

---

#### TASK-4.4.3: 文档完善
**优先级**: P1
**工时**: 4h
**负责人**: 所有工程师

**验收标准**:
- [x] 完善所有API文档（Swagger）
- [x] 完善部署文档
- [x] 完善运维文档
- [x] 完善用户手册
- [x] 创建P1阶段总结报告

**产出**:
- API文档
- 部署文档
- 运维文档
- 用户手册
- P1阶段总结报告

**依赖**: TASK-4.4.2

---

## 依赖关系图

```
Week 1: AI模型管理
TASK-1.1.1 → TASK-1.1.2 → TASK-1.2.1 → TASK-1.2.2
                           ↓
                     TASK-1.2.3 → TASK-1.3.1
                     TASK-1.3.2 ↗
                           ↓
                     TASK-1.4.1, TASK-1.4.2

Week 2: WebSocket推送
TASK-2.1.1 → TASK-2.1.2 → TASK-2.2.1 → TASK-2.2.2
                           ↓
                     TASK-2.2.3 → TASK-2.4.1
TASK-2.3.1 → TASK-2.3.2 ↗
                           ↓
                     TASK-2.4.2

Week 3: 前端展示
TASK-2.3.2 → TASK-3.1.1 → TASK-3.1.2
           ↓
TASK-3.2.1 → TASK-3.2.2
           ↓
TASK-3.3.1 → TASK-3.4.1 → TASK-3.4.2

Week 4: 测试完善
Week 1-3 → TASK-4.1.1 → TASK-4.1.2 → TASK-4.1.3
                        ↓
                  TASK-4.2.1 → TASK-4.2.2
                        ↓
                  TASK-4.3.1 → TASK-4.3.2
                        ↓
                  TASK-4.4.1 → TASK-4.4.2 → TASK-4.4.3
```

---

## 里程碑和验收标准

| 里程碑 | 交付日期 | 验收标准 | 状态 |
|--------|---------|---------|------|
| **M1: AI模型管理完成** | Week 1 | • 所有API实现并测试<br>• 文档完整<br>• 编译通过 | ⏳ 待开始 |
| **M2: WebSocket推送完成** | Week 2 | • WebSocket连接正常<br>• 消息推送延迟<500ms<br>• 集成测试通过 | ⏳ 待开始 |
| **M3: 前端展示完成** | Week 3 | • 所有页面实现<br>• E2E测试通过<br>• 性能达标 | ⏳ 待开始 |
| **M4: P1阶段完成** | Week 4 | • 所有测试通过<br>• 性能指标达标<br>• 文档完整<br>• 验收通过 | ⏳ 待开始 |

---

## 风险和缓解措施

| 风险 | 影响 | 概率 | 缓解措施 | 负责人 |
|------|------|------|----------|--------|
| WebSocket连接不稳定 | 高 | 中 | 心跳机制、自动重连 | 后端工程师 |
| 前端性能不达标 | 中 | 中 | 虚拟滚动、懒加载 | 前端工程师 |
| 测试覆盖不足 | 高 | 低 | 强制测试门禁、CI检查 | 测试工程师 |
| 性能测试不达标 | 高 | 中 | 早期性能测试、持续优化 | 测试工程师 |
| MinIO存储问题 | 中 | 低 | 使用本地存储降级 | 后端工程师 |

---

**任务列表创建人**: IOE-DREAM 架构委员会
**创建日期**: 2025-01-30
**版本**: 1.0.0
**预计开始**: 2025-01-31
