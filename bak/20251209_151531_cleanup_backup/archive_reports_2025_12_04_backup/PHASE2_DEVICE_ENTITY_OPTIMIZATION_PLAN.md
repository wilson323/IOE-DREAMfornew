# Phase 2: 设备实体优化执行计划

**执行日期**: 2025-12-03  
**任务**: Task 2.2 - 设备管理优化  
**状态**: ⏳ 准备执行

---

## 📊 设备实体分析结果

### 发现的设备实体类

| 实体类 | 位置 | 字段数 | 状态 | 建议 |
|--------|------|--------|------|------|
| **DeviceEntity** | `microservices-common` | 17个基础字段 | ✅ 公共实体 | ✅ **保留**（统一设备实体） |
| **DeviceEntity** | `ioedream-common-core` | 17个基础字段 | ⚠️ 可能重复 | ⚠️ **需检查** |
| **DeviceEntity** | `ioedream-device-comm-service` | 100+字段 | ⚠️ 设备通讯专用 | ⚠️ **需评估** |
| **AttendanceDeviceEntity** | `ioedream-attendance-service` | 未知 | ⚠️ 考勤设备 | ⚠️ **需统一** |
| **VideoDeviceEntity** | `ioedream-device-comm-service` | 未知 | ⚠️ 视频设备 | ⚠️ **需统一** |
| **UnifiedDeviceEntity** | `archive/deprecated-services` | 未知 | ✅ 已废弃 | ✅ **忽略** |
| **PhysicalDeviceEntity** | `archive/deprecated-services` | 未知 | ✅ 已废弃 | ✅ **忽略** |

### 实体类差异分析

#### 1. microservices-common/DeviceEntity（公共实体）

**特点**:
- ✅ 基础字段（设备ID、名称、编号、类型等）
- ✅ 使用`extendedAttributes`（JSON）存储扩展属性
- ✅ 继承`BaseEntity`（包含创建时间、更新时间等）
- ✅ 表名：`t_common_device`

**字段列表**:
- deviceId, deviceName, deviceCode, deviceType
- areaId, ipAddress, port
- status, deviceStatus, enabledFlag
- sortOrder, configJson, lastOnlineTime
- extendedAttributes（JSON扩展属性）

#### 2. ioedream-device-comm-service/DeviceEntity（设备通讯专用）

**特点**:
- ⚠️ 728行代码，包含100+字段
- ⚠️ 包含大量设备通讯特定字段（协议、连接状态、性能指标等）
- ⚠️ 包含业务方法（isOnline、isHealthy等）
- ⚠️ 表名：`t_device`（不同表）

**关键字段**:
- 协议相关：protocolType, protocolVersion
- 连接状态：connectStatus, lastHeartbeatTime
- 性能指标：cpuUsage, memoryUsage, storageUsage
- 位置信息：buildingId, floorId, longitude, latitude
- 维护信息：warrantyEndTime, nextMaintenanceTime
- 安全配置：encryptionKey, authUsername, authPassword

**评估**:
- ✅ 设备通讯服务需要这些详细字段
- ⚠️ 但应该统一使用公共DeviceEntity + extendedAttributes
- ⚠️ 或者保留作为设备通讯服务的业务特定实体

#### 3. ioedream-common-core/DeviceEntity

**状态**: ⚠️ **需检查是否与microservices-common重复**

---

## 🎯 优化方案

### 方案A：统一到公共DeviceEntity（推荐）

**策略**:
1. ✅ 所有业务服务使用`microservices-common`的`DeviceEntity`
2. ✅ 业务特定字段存储在`extendedAttributes`（JSON）
3. ✅ 设备通讯服务的详细字段迁移到extendedAttributes
4. ✅ 删除重复的设备实体类

**优点**:
- ✅ 统一数据模型
- ✅ 减少代码冗余
- ✅ 便于维护

**缺点**:
- ⚠️ 需要大量代码修改
- ⚠️ JSON字段查询性能可能较差
- ⚠️ 类型安全性降低

### 方案B：保留设备通讯专用实体（备选）

**策略**:
1. ✅ 公共DeviceEntity用于基础设备管理
2. ✅ 设备通讯服务保留专用DeviceEntity（业务特定）
3. ✅ 其他业务服务使用公共DeviceEntity

**优点**:
- ✅ 类型安全
- ✅ 查询性能好
- ✅ 代码修改量小

**缺点**:
- ⚠️ 存在两套设备实体
- ⚠️ 需要明确职责划分

---

## 🚀 执行计划

### Step 1: 检查ioedream-common-core重复（15分钟）

**任务**:
1. 检查`ioedream-common-core`中的`DeviceEntity`是否与`microservices-common`重复
2. 如果重复，删除`ioedream-common-core`中的实体
3. 更新引用

### Step 2: 统一AttendanceDeviceEntity和VideoDeviceEntity（2小时）

**任务**:
1. 分析`AttendanceDeviceEntity`和`VideoDeviceEntity`的字段
2. 将业务特定字段迁移到公共`DeviceEntity`的`extendedAttributes`
3. 更新业务代码使用公共`DeviceEntity`
4. 删除重复实体类

### Step 3: 评估设备通讯服务DeviceEntity（1小时）

**任务**:
1. 分析设备通讯服务DeviceEntity的必要性
2. 如果保留，明确职责划分
3. 如果统一，迁移字段到extendedAttributes

### Step 4: 更新所有引用（1小时）

**任务**:
1. 更新所有DAO引用
2. 更新所有Service引用
3. 更新所有Manager引用
4. 验证编译

---

## 📋 执行清单

### Task 2.2.1: 检查common-core重复
- [ ] 检查`ioedream-common-core/DeviceEntity`是否重复
- [ ] 如果重复，删除并更新引用
- [ ] 验证编译

### Task 2.2.2: 统一业务设备实体
- [ ] 分析`AttendanceDeviceEntity`字段
- [ ] 分析`VideoDeviceEntity`字段
- [ ] 迁移到公共`DeviceEntity`
- [ ] 更新业务代码
- [ ] 删除重复实体
- [ ] 验证编译

### Task 2.2.3: 设备通讯服务评估
- [ ] 分析设备通讯服务DeviceEntity必要性
- [ ] 制定保留或统一方案
- [ ] 执行方案
- [ ] 验证编译

### Task 2.2.4: 更新所有引用
- [ ] 更新DAO引用
- [ ] 更新Service引用
- [ ] 更新Manager引用
- [ ] 验证编译

---

**预计工作量**: 4-5小时  
**优先级**: P0  
**状态**: ⏳ 准备执行

