# IOE-DREAM P0级关键缺失功能 - 立即执行任务清单

> 基于《GLOBAL_FUNCTION_COMPLETENESS_ANALYSIS_REPORT.md》的深度分析结果
> 立即执行优先级：最高优先级 (P0级)
> 执行周期：1个月（26人天）

---

## 📊 执行摘要

**当前完成度**: 79% (55/70项P0级功能已实现)
**关键缺失**: 3项P0级核心功能未实现
**目标完成度**: 83% (58/70项P0级功能)

### 🎯 立即执行的3项P0级核心功能

| 优先级 | 功能模块 | 功能名称 | 当前状态 | 风险等级 | 预计工作量 |
|--------|---------|---------|----------|----------|-----------|
| ⭐⭐⭐ | **消费管理** | **离线消费同步机制** | ❌ 0% | 🔴 **高** | 6人天 |
| ⭐⭐⭐ | **视频监控** | **视频地图展示** | ❌ 0% | 🟠 中 | 6人天 |
| ⭐⭐⭐ | **消费管理** | **补贴规则引擎** | 🟡 25% | 🟠 中 | 8人天 |

---

## 🚀 任务1: 离线消费同步机制 (6人天)

### 📌 功能描述
实现移动端在无网络环境下的消费记录本地存储，网络恢复后自动同步到服务器，确保数据完整性和一致性。

### 🎯 验收标准
- [ ] 离线消费可正常记录（IndexedDB存储）
- [ ] 网络恢复后自动同步（后台静默同步）
- [ ] 冲突解决机制正常工作（Last-Write-Wins + 人工审核）
- [ ] 数据完整性验证通过（签名验证、重复检测）
- [ ] 性能测试通过（1000条<30秒）
- [ ] 白名单模式支持（离线降级）

### 🔧 技术方案

#### 前端实现（移动端）
- **数据库**: IndexedDB本地存储
- **关键表**: `offline_consume` (离线消费记录表)
- **核心方法**:
  - `recordOfflineConsume()` - 记录离线消费
  - `autoSync()` - 自动同步
  - `calculateLocalBalance()` - 本地余额计算
  - `resolveConflict()` - 冲突解决

#### 后端实现
- **Service**: `OfflineConsumeSyncService`
- **核心方法**:
  - `batchSync()` - 批量同步
  - `validateRecords()` - 记录校验
  - `detectConflicts()` - 冲突检测
  - `resolveConflicts()` - 冲突解决

### 📋 详细任务清单

#### 第1天：数据库 + Entity/DAO
- [ ] 1.1 创建数据库表 `t_offline_consume_record`
  - [ ] 添加字段：id, userId, deviceId, amount, consumeTime, syncStatus, syncTime, conflictReason, createdAt
  - [ ] 添加索引：idx_user_sync, idx_device_time
- [ ] 1.2 创建Entity类 `OfflineConsumeEntity`
  - [ ] 字段映射
  - [ ] MyBatis-Plus注解
- [ ] 1.3 创建DAO接口 `OfflineConsumeDao`
  - [ ] 继承BaseMapper
  - [ ] 自定义查询方法

#### 第2天：Service层开发
- [ ] 2.1 创建 `OfflineConsumeSyncService`
  - [ ] `batchSync()` - 批量同步逻辑
  - [ ] `validateRecords()` - 余额/时效/签名校验
  - [ ] `detectConflicts()` - 时间戳对比冲突检测
  - [ ] `resolveConflicts()` - 冲突解决策略
- [ ] 2.2 事务管理
  - [ ] @Transactional注解
  - [ ] 回滚机制
- [ ] 2.3 异常处理
  - [ ] 自定义异常类
  - [ ] 异常捕获和处理

#### 第3天：Controller + 单元测试
- [ ] 3.1 创建 `OfflineConsumeSyncController`
  - [ ] POST `/api/consume/offline/sync` - 批量同步接口
  - [ ] GET `/api/consume/offline/records` - 查询离线记录
  - [ ] GET `/api/consume/offline/conflicts` - 查询冲突记录
- [ ] 3.2 参数验证
  - [ ] @Valid注解
  - [ ] 自定义验证器
- [ ] 3.3 单元测试
  - [ ] 同步逻辑测试
  - [ ] 冲突检测测试
  - [ ] 异常处理测试

#### 第4天：前端IndexedDB封装
- [ ] 4.1 创建 `offline-db.js`
  - [ ] IndexedDB初始化
  - [ ] 数据库版本管理
  - [ ] 表结构定义
- [ ] 4.2 创建 `offline-consume-store.js`
  - [ ] `addOfflineRecord()` - 添加离线记录
  - [ ] `getPendingRecords()` - 获取待同步记录
  - [ ] `updateSyncStatus()` - 更新同步状态
  - [ ] `calculateLocalBalance()` - 计算本地余额
- [ ] 4.3 离线消费记录逻辑
  - [ ] 本地余额检查
  - [ ] 记录添加到IndexedDB
  - [ ] 本地余额扣减

#### 第5天：前端同步逻辑
- [ ] 5.1 创建 `offline-sync-manager.js`
  - [ ] `autoSync()` - 自动同步（网络状态监听）
  - [ ] `syncRecord()` - 单条记录同步
  - [ ] `handleSyncError()` - 同步错误处理
  - [ ] `resolveConflict()` - 冲突处理
- [ ] 5.2 网络状态监听
  - [ ] `online` 事件监听
  - [ ] `offline` 事件监听
  - [ ] 自动触发同步
- [ ] 5.3 同步状态UI
  - [ ] 同步进度显示
  - [ ] 同步结果提示
  - [ ] 冲突记录处理

#### 第6天：集成测试 + 性能优化
- [ ] 6.1 集成测试
  - [ ] 端到端离线消费流程测试
  - [ ] 网络恢复同步测试
  - [ ] 冲突解决测试
- [ ] 6.2 性能测试
  - [ ] 1000条记录同步<30秒
  - [ ] IndexedDB查询性能
  - [ ] 批量操作优化
- [ ] 6.3 代码优化
  - [ ] 批量操作优化
  - [ ] 内存占用优化
  - [ ] 错误处理完善

### 📁 交付物

**后端文件**:
- `OfflineConsumeEntity.java`
- `OfflineConsumeDao.java`
- `OfflineConsumeSyncService.java`
- `OfflineConsumeSyncController.java`
- `OfflineConsumeMapper.xml`

**前端文件**:
- `offline-db.js`
- `offline-consume-store.js`
- `offline-sync-manager.js`
- `offline-sync.vue`

**数据库脚本**:
- `V1.0.0__create_offline_consume_table.sql`

**测试文件**:
- `OfflineConsumeSyncServiceTest.java`
- `OfflineSyncIntegrationTest.java`

---

## 🗺️ 任务2: 视频地图展示功能 (6人天)

### 📌 功能描述
在地图上实时显示视频设备位置和状态，支持点击查看实时视频，告警时设备闪烁提示。

### 🎯 验收标准
- [ ] 地图正常显示（Leaflet + OpenStreetMap）
- [ ] 设备标注准确（经纬度精确）
- [ ] 点击可播放视频（实时流）
- [ ] 实时状态更新（30秒刷新）
- [ ] 告警闪烁提示（WebSocket推送）
- [ ] 设备过滤功能（按状态/类型）
- [ ] 标注聚合优化（密集区域）

### 🔧 技术方案

#### 前端技术栈
- **地图库**: Leaflet.js
- **地图数据**: OpenStreetMap
- **实时通信**: WebSocket
- **视频播放**: Video.js

#### 后端API
- **Controller**: `VideoMapController`
- **核心接口**:
  - GET `/api/video/map/devices` - 获取设备地图数据
  - GET `/api/video/map/device/{id}/status` - 获取设备实时状态

### 📋 详细任务清单

#### 第1天：后端API开发
- [ ] 1.1 创建 `VideoDeviceMapVO`
  - [ ] deviceId, deviceName, latitude, longitude, status, streamUrl
- [ ] 1.2 创建 `VideoMapController`
  - [ ] `getDeviceMapData()` - 获取设备地图数据
  - [ ] `getDeviceRealtimeStatus()` - 获取实时状态
- [ ] 1.3 设备位置数据
  - [ ] 添加设备经纬度字段
  - [ ] 批量更新设备位置
- [ ] 1.4 单元测试
  - [ ] API测试
  - [ ] 数据转换测试

#### 第2天：地图组件集成
- [ ] 2.1 安装依赖
  - [ ] `npm install leaflet`
  - [ ] `npm install leaflet/dist/leaflet.css`
- [ ] 2.2 创建 `video-map-display.vue`
  - [ ] 地图容器初始化
  - [ ] 地图底图加载（OpenStreetMap）
  - [ ] 地图控件（缩放、比例尺）
- [ ] 2.3 设备标注
  - [ ] 标注图标（在线/离线/告警）
  - [ ] 标注点击事件
  - [ ] 标注弹窗（设备信息）

#### 第3天：视频播放弹窗
- [ ] 3.1 创建 `video-player-modal.vue`
  - [ ] 弹窗组件
  - [ ] 视频播放器集成（Video.js）
  - [ ] 实时流播放
- [ ] 3.2 设备点击联动
  - [ ] 点击标注显示弹窗
  - [ ] 传递设备信息和流地址
  - [ ] 弹窗关闭处理
- [ ] 3.3 视频播放优化
  - [ ] 自动播放控制
  - [ ] 错误处理
  - [ ] 性能优化

#### 第4天：实时状态更新
- [ ] 4.1 WebSocket集成
  - [ ] WebSocket连接管理
  - [ ] 设备状态推送订阅
  - [ ] 断线重连机制
- [ ] 4.2 状态实时更新
  - [ ] 设备状态更新
  - [ ] 标注图标刷新
  - [ ] 告警闪烁动画
- [ ] 4.3 定时轮询（备用方案）
  - [ ] 30秒定时器
  - [ ] 设备状态刷新
  - [ ] 性能优化

#### 第5天：告警闪烁 + 过滤功能
- [ ] 5.1 告警闪烁
  - [ ] 告警设备标识
  - [ ] 闪烁动画效果
  - [ ] 告警提示弹窗
- [ ] 5.2 设备过滤
  - [ ] 按状态过滤（在线/离线/告警）
  - [ ] 按类型过滤（摄像头/NVR）
  - [ ] 按区域过滤
- [ ] 5.3 标注聚合
  - [ ] 密集区域检测
  - [ ] 聚合标注显示
  - [ ] 点击展开子标注

#### 第6天：测试 + 优化
- [ ] 6.1 功能测试
  - [ ] 地图显示测试
  - [ ] 设备标注测试
  - [ ] 视频播放测试
  - [ ] 实时更新测试
- [ ] 6.2 性能测试
  - [ ] 1000个设备标注加载时间
  - [ ] 实时更新性能
  - [ ] 内存占用
- [ ] 6.3 兼容性测试
  - [ ] Chrome浏览器
  - [ ] Firefox浏览器
  - [ ] Edge浏览器

### 📁 交付物

**后端文件**:
- `VideoDeviceMapVO.java`
- `VideoMapController.java`

**前端文件**:
- `video-map-display.vue`
- `video-player-modal.vue`
- `map-marker-icon.js`

**测试文件**:
- `VideoMapControllerTest.java`
- `VideoMapE2ETest.java`

---

## 💰 任务3: 补贴规则引擎完善 (8人天)

### 📌 功能描述
基于Aviator表达式引擎实现灵活的补贴规则配置，支持多种补贴类型、发放周期和条件判断。

### 🎯 验收标准
- [ ] Aviator表达式正常执行
- [ ] 预置规则模板可用（餐补/交通补/岗位补）
- [ ] 规则验证正确（语法检查、变量检查）
- [ ] 定时发放正常（每日/每周/每月）
- [ ] 补贴计算准确（金额精确）
- [ ] 前端规则编辑器（语法高亮、自动提示）
- [ ] 规则测试功能（模拟执行）

### 🔧 技术方案

#### 后端技术栈
- **规则引擎**: Aviator 5.x
- **表达式语言**: Aviator表达式
- **任务调度**: Quartz

#### 前端技术栈
- **编辑器**: Monaco Editor
- **语法高亮**: 自定义Aviator语法
- **自动提示**: 变量/函数提示

### 📋 详细任务清单

#### 第1-2天：规则引擎核心开发
- [ ] 1.1 集成Aviator依赖
  - [ ] 添加 `aviator` 依赖到pom.xml
  - [ ] 版本管理（5.3.3）
- [ ] 1.2 创建 `SubsidyRuleEngine`
  - [ ] `executeRule()` - 执行规则
  - [ ] `validateRule()` - 验证规则
  - [ ] `compileExpression()` - 编译表达式
- [ ] 1.3 环境变量管理
  - [ ] 构建执行环境（Map<String, Object>）
  - [ ] 变量注入（用户信息、考勤数据等）
  - [ ] 函数注册（自定义函数）
- [ ] 1.4 性能优化
  - [ ] 表达式预编译缓存
  - [ ] 执行结果缓存
  - [ ] 并发执行优化

#### 第3天：预置规则模板
- [ ] 3.1 创建 `SubsidyRuleTemplates` 类
  - [ ] `MEAL_SUBSIDY_RULE` - 餐补规则（按工作日）
  - [ ] `TRANSPORT_SUBSIDY_RULE` - 交通补规则（按距离）
  - [ ] `POSITION_SUBSIDY_RULE` - 岗位补贴规则（按级别）
- [ ] 3.2 规则文档
  - [ ] 规则说明
  - [ ] 变量说明
  - [ ] 示例代码
- [ ] 3.3 规则模板管理
  - [ ] 模板加载
  - [ ] 模板版本管理
  - [ ] 模板导入导出

#### 第4天：规则管理Service + 定时任务
- [ ] 4.1 创建 `SubsidyRuleService`
  - [ ] `createRule()` - 创建规则
  - [ ] `updateRule()` - 更新规则
  - [ ] `deleteRule()` - 删除规则
  - [ ] `enableRule()` - 启用/禁用规则
- [ ] 4.2 创建 `SubsidyCalculateService`
  - [ ] `calculateSubsidy()` - 计算补贴
  - [ ] `batchCalculate()` - 批量计算
  - [ ] `executeRuleByType()` - 按类型执行规则
- [ ] 4.3 定时任务集成
  - [ ] Quartz Job定义
  - [ ] 发放周期配置（每日/每周/每月）
  - [ ] 任务执行日志

#### 第5天：前端规则编辑器
- [ ] 5.1 安装Monaco Editor
  - [ ] `npm install monaco-editor`
- [ ] 5.2 创建 `rule-editor.vue`
  - [ ] Monaco Editor集成
  - [ ] Aviator语法高亮
  - [ ] 代码编辑器配置
- [ ] 5.3 自动提示功能
  - [ ] 变量提示（user, date, attendance等）
  - [ ] 函数提示（if, return,运算符等）
  - [ ] 关键字提示
- [ ] 5.4 语法验证
  - [ ] 实时语法检查
  - [ ] 错误提示显示
  - [ ] 格式化功能

#### 第6-7天：前端规则管理页面
- [ ] 6.1 创建 `subsidy-rule-config.vue`
  - [ ] 规则列表展示
  - [ ] 规则创建/编辑表单
  - [ ] 规则启用/禁用操作
  - [ ] 规则删除确认
- [ ] 6.2 规则测试功能
  - [ ] 模拟数据输入
  - [ ] 规则执行测试
  - [ ] 结果展示
  - [ ] 性能指标显示
- [ ] 6.3 规则模板选择
  - [ ] 预置模板列表
  - [ ] 模板应用功能
  - [ ] 模板自定义
- [ ] 6.4 规则历史版本
  - [ ] 版本列表
  - [ ] 版本对比
  - [ ] 版本回滚

#### 第8天：集成测试 + 性能优化
- [ ] 8.1 集成测试
  - [ ] 端到端规则创建流程
  - [ ] 规则执行流程
  - [ ] 定时发放流程
- [ ] 8.2 性能测试
  - [ ] 规则执行性能（<100ms）
  - [ ] 批量计算性能（1000人<10s）
  - [ ] 内存占用
- [ ] 8.3 压力测试
  - [ ] 并发规则执行
  - [ ] 长时间运行稳定性
- [ ] 8.4 代码优化
  - [ ] 缓存优化
  - [ ] SQL优化
  - [ ] 代码重构

### 📁 交付物

**后端文件**:
- `SubsidyRuleEngine.java`
- `SubsidyRuleTemplates.java`
- `SubsidyRuleService.java`
- `SubsidyCalculateService.java`
- `SubsidyRuleEntity.java`
- `SubsidyRuleController.java`
- `SubsidyJob.java` (Quartz Job)

**前端文件**:
- `rule-editor.vue`
- `subsidy-rule-config.vue`
- `rule-test-modal.vue`
- `subsidy-rule-templates.js`

**数据库脚本**:
- `V1.0.0__create_subsidy_rule_table.sql`

**测试文件**:
- `SubsidyRuleEngineTest.java`
- `SubsidyCalculateServiceTest.java`

---

## 📅 整体执行时间表

| 周次 | 主要任务 | 工作量 | 里程碑 |
|------|---------|--------|--------|
| **第1周** | 离线消费同步机制（第1-3天） | 3人天 | 后端Service+Controller完成 |
| | 离线消费同步机制（第4-5天） | 2人天 | 前端IndexedDB+同步逻辑完成 |
| **第2周** | 离线消费同步机制（第6天完成） + 视频地图（第1-2天） | 3人天 | **离线消费同步功能上线** |
| | 视频地图展示（第3-5天） | 3人天 | 地图显示+视频播放完成 |
| **第3周** | 视频地图展示（第6天完成） + 补贴规则引擎（第1-2天） | 3人天 | **视频地图展示功能上线** |
| | 补贴规则引擎（第3-5天） | 3人天 | 规则引擎核心+模板完成 |
| **第4周** | 补贴规则引擎（第6-7天） | 2人天 | 前端编辑器+管理页面完成 |
| | 补贴规则引擎（第8天完成） | 1人天 | **补贴规则引擎功能上线** |
| **第5周** | 系统测试 + 性能优化 + 文档完善 | 5人天 | **全部3项功能测试通过** |
| **总计** | **5周（26人天）** | 26人天 | **完成度83%** |

---

## ✅ 验收标准汇总

### 功能完整性
- [ ] 离线消费同步机制：100%功能实现
- [ ] 视频地图展示：100%功能实现
- [ ] 补贴规则引擎：100%功能实现

### 质量标准
- [ ] 单元测试覆盖率≥80%
- [ ] 集成测试全部通过
- [ ] 无P0/P1级Bug
- [ ] SonarQube评分A+
- [ ] 代码规范检查通过

### 性能标准
- [ ] 离线同步：1000条<30秒
- [ ] 视频地图：1000设备标注加载<5秒
- [ ] 规则执行：单条规则<100ms
- [ ] 批量计算：1000人<10秒

### 文档标准
- [ ] API文档100%完整（Swagger）
- [ ] 开发文档详细
- [ ] 部署文档清晰
- [ ] 用户手册友好

---

## 📊 完成后整体完成度

**当前完成度**: 79% (55/70项P0级功能)
**新增完成**: 3项核心P0级功能
**目标完成度**: **83%** (58/70项P0级功能)

### 改进效果

#### 离线消费同步机制
- ✅ 消费系统可用性提升30%
- ✅ 网络不稳定场景支持能力提升100%
- ✅ 数据一致性保障能力提升

#### 视频地图展示
- ✅ 可视化管理能力提升50%
- ✅ 设备定位效率提升
- ✅ 告警响应速度提升

#### 补贴规则引擎
- ✅ 补贴管理灵活性提升100%
- ✅ 复杂补贴策略支持能力
- ✅ 运营效率提升40%

---

## 🚀 下一步计划

完成3项P0级核心功能后，继续实施P1级功能（14项）：

**P1级优先级功能**:
1. 人脸记录留存（访客管理）
2. 多设备打卡支持（考勤管理）
3. 考勤数据智能分析（考勤管理）
4. 访客轨迹追踪（访客管理）
5. 智能推荐功能（消费管理）

**预计周期**: 2个月（40人天）

---

**文档版本**: v1.0
**创建日期**: 2025-12-25
**最后更新**: 2025-12-25
**负责人**: IOE-DREAM开发团队
