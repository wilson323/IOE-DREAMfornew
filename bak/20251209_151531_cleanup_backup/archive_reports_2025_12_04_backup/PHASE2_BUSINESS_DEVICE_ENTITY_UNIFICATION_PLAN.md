# Phase 2: 业务特定设备实体统一方案

**执行日期**: 2025-12-03  
**任务**: 统一业务特定设备实体到公共DeviceEntity  
**状态**: ⏳ 准备执行

---

## 📊 业务特定设备实体分析

### 发现的业务特定设备实体

| 实体类 | 位置 | 字段数 | 使用情况 | 状态 |
|--------|------|--------|---------|------|
| **AttendanceDeviceEntity** | `ioedream-attendance-service` | 30+字段 | 1个文件引用 | ⚠️ 临时实体（注释标记） |
| **VideoDeviceEntity** | `ioedream-device-comm-service` | 1000+行 | 未知 | ⚠️ 视频设备专用 |
| **DeviceEntity** | `ioedream-device-comm-service` | 728行 | 设备通讯专用 | ⚠️ 设备通讯专用 |

### 实体字段分析

#### 1. AttendanceDeviceEntity（考勤设备）

**业务特定字段**（应迁移到extendedAttributes）:
- `attendanceDeviceType` - 考勤机类型
- `attendanceMethod` - 考勤方式
- `recognitionThreshold` - 识别时间阈值
- `liveDetectionEnabled` - 活体检测支持
- `wifiEnabled`, `wifiSSID` - WiFi配置
- `fourGEnabled`, `simCardNumber` - 4G配置
- `maxUserCapacity`, `currentUserCount` - 用户容量
- `maxRecordCount`, `currentRecordCount` - 记录容量
- `accuracyThreshold` - 识别精度阈值
- `attendanceMode` - 考勤规则模式
- `supportedRules` - 支持的考勤规则（JSON）
- `photoSaveEnabled`, `photoQuality` - 照片配置
- `temperatureDetectionEnabled`, `temperatureThreshold` - 温度检测
- `maskRecognitionEnabled`, `maskAccuracyThreshold` - 口罩识别
- `offlineAttendanceEnabled`, `offlineStorageDays` - 离线考勤
- `syncMode`, `autoSyncInterval` - 数据同步
- `gpsEnabled`, `gpsAccuracyRange` - GPS定位
- `attendanceAreaId` - 考勤区域ID
- `installLocationDetail` - 安装位置详情
- `adminUserId` - 管理员ID

**基础字段**（公共DeviceEntity已有）:
- 设备ID、名称、编号等 → 使用公共DeviceEntity

#### 2. VideoDeviceEntity（视频设备）

**特点**:
- 1000+行代码
- 包含大量视频设备特定字段
- 需要详细分析后统一

#### 3. DeviceEntity（设备通讯服务）

**特点**:
- 728行代码
- 包含设备通讯特定字段（协议、连接状态、性能指标等）
- 可能需要保留（业务特定）

---

## 🎯 统一方案

### 方案：使用extendedAttributes存储业务特定字段

**策略**:
1. ✅ 统一使用`microservices-common`的`DeviceEntity`作为基础实体
2. ✅ 业务特定字段存储在`extendedAttributes`（JSON格式）
3. ✅ 创建工具类处理extendedAttributes的读写
4. ✅ 更新业务代码使用公共DeviceEntity + extendedAttributes
5. ✅ 删除重复的业务特定实体类

### 统一步骤

#### Step 1: 创建extendedAttributes工具类（30分钟）

**文件**: `microservices-common/src/main/java/net/lab1024/sa/common/organization/util/DeviceExtendedAttributesUtil.java`

**功能**:
- 读取业务特定字段
- 写入业务特定字段
- 类型安全的getter/setter方法

#### Step 2: 统一AttendanceDeviceEntity（2小时）

**任务**:
1. 分析AttendanceDeviceEntity的所有字段
2. 创建AttendanceDeviceExtendedAttributes类（用于类型安全）
3. 更新使用AttendanceDeviceEntity的代码
4. 迁移数据到公共DeviceEntity的extendedAttributes
5. 删除AttendanceDeviceEntity

#### Step 3: 统一VideoDeviceEntity（3小时）

**任务**:
1. 分析VideoDeviceEntity的所有字段
2. 创建VideoDeviceExtendedAttributes类
3. 更新使用VideoDeviceEntity的代码
4. 迁移数据
5. 删除VideoDeviceEntity

#### Step 4: 评估DeviceEntity（设备通讯服务）（1小时）

**任务**:
1. 分析设备通讯服务的DeviceEntity必要性
2. 决定是否保留或统一

---

## 📋 执行清单

### Step 1: 创建工具类
- [ ] 创建`DeviceExtendedAttributesUtil.java`
- [ ] 创建`AttendanceDeviceExtendedAttributes.java`（DTO类）
- [ ] 创建`VideoDeviceExtendedAttributes.java`（DTO类）

### Step 2: 统一AttendanceDeviceEntity
- [ ] 分析所有字段
- [ ] 更新使用AttendanceDeviceEntity的代码
- [ ] 迁移数据逻辑
- [ ] 删除AttendanceDeviceEntity
- [ ] 验证编译

### Step 3: 统一VideoDeviceEntity
- [ ] 分析所有字段
- [ ] 更新使用VideoDeviceEntity的代码
- [ ] 迁移数据逻辑
- [ ] 删除VideoDeviceEntity
- [ ] 验证编译

### Step 4: 评估设备通讯服务DeviceEntity
- [ ] 分析必要性
- [ ] 制定方案
- [ ] 执行方案

---

## 🚀 立即执行计划

### 优先级：先统一AttendanceDeviceEntity（工作量小）

**原因**:
1. ✅ 只有1个文件引用
2. ✅ 字段相对较少（30+字段）
3. ✅ 文件中有临时标记，说明可以统一

**工作量**: 2小时

---

**Phase 2 业务设备实体统一状态**: ⏳ **准备执行**  
**下一步**: 创建extendedAttributes工具类，然后统一AttendanceDeviceEntity

