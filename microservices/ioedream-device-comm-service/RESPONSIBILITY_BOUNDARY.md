# ioedream-device-comm-service 职责边界文档

## 服务定位

**服务名称**: ioedream-device-comm-service  
**端口**: 8087  
**服务类型**: 核心设备通讯服务

## 核心职责

提供设备协议适配、连接管理、数据采集等设备通讯功能。**⚠️ 重要说明：本服务不做设备识别，识别在设备端完成。**

## 三层架构设计

### 1. 协议适配层 (Protocol Adapter Layer)

**职责**:
- 多厂商设备协议适配
- 协议转换和标准化
- 协议版本管理

**支持协议**:
- 海康威视协议
- 大华协议
- 中控智慧协议
- 其他厂商协议

**核心组件**:
- `DeviceProtocolAdapter` - 协议适配器接口
- `HikvisionProtocolAdapter` - 海康协议适配器
- `DahuaProtocolAdapter` - 大华协议适配器
- `ZKTecoProtocolAdapter` - 中控协议适配器

**禁止职责**:
- ❌ 不处理业务逻辑（如门禁控制、考勤统计）
- ❌ 不管理设备实体（由common-service管理）
- ❌ 不做设备识别（识别在设备端完成）

### 2. 连接管理层 (Connection Management Layer)

**职责**:
- 设备连接池管理
- 连接状态监控
- 连接重连机制
- 连接健康检查

**核心组件**:
- `DeviceConnectionManager` - 连接管理器
- `ConnectionPool` - 连接池
- `ConnectionHealthMonitor` - 连接健康监控

**禁止职责**:
- ❌ 不处理业务连接逻辑（如门禁连接、考勤连接）
- ❌ 不管理设备业务状态（由业务服务管理）

### 3. 数据采集层 (Data Collection Layer)

**职责**:
- 设备数据采集
- 数据格式转换
- 数据推送和上报

**核心组件**:
- `DeviceDataCollector` - 数据采集器
- `DataTransformer` - 数据转换器
- `DataPublisher` - 数据发布器

**禁止职责**:
- ❌ 不处理业务数据逻辑（如考勤记录处理、消费记录处理）
- ❌ 不存储业务数据（只负责数据采集和转发）

## 与业务服务的关系

### 与access-service的关系

- **协议通信**: device-comm-service提供门禁设备协议通信能力
- **数据采集**: device-comm-service采集门禁设备数据并推送给access-service
- **职责分离**: device-comm-service负责通信，access-service负责业务逻辑

### 与attendance-service的关系

- **协议通信**: device-comm-service提供考勤设备协议通信能力
- **数据采集**: device-comm-service采集考勤设备数据并推送给attendance-service
- **职责分离**: device-comm-service负责通信，attendance-service负责业务逻辑

### 与consume-service的关系

- **协议通信**: device-comm-service提供消费设备协议通信能力
- **数据采集**: device-comm-service采集消费设备数据并推送给consume-service
- **职责分离**: device-comm-service负责通信，consume-service负责业务逻辑

### 与biometric-service的关系

- **模板下发**: device-comm-service负责将biometric-service管理的模板下发到设备
- **协议通信**: device-comm-service提供生物识别设备协议通信能力
- **职责分离**: device-comm-service负责通信，biometric-service负责模板管理

## 与common-service的关系

### 设备实体管理

- **设备CRUD**: common-service管理统一设备实体（DeviceEntity）
- **设备信息查询**: device-comm-service从common-service获取设备信息
- **职责分离**: common-service负责设备实体管理，device-comm-service负责设备通信

### 设备状态同步

- **状态上报**: device-comm-service将设备连接状态同步到common-service
- **状态查询**: device-comm-service从common-service查询设备状态
- **职责分离**: common-service负责设备状态存储，device-comm-service负责状态采集

## 设备交互模式

### Mode 1: 边缘自主验证（门禁系统）

**设备端**: 完成识别和验证  
**device-comm-service**: 负责模板下发和记录上传

### Mode 2: 中心实时验证（消费系统）

**设备端**: 采集数据  
**device-comm-service**: 负责数据采集和转发  
**consume-service**: 负责业务验证和处理

### Mode 3: 边缘识别+中心计算（考勤系统）

**设备端**: 完成识别  
**device-comm-service**: 负责数据采集和转发  
**attendance-service**: 负责业务计算和统计

### Mode 4: 混合验证（访客系统）

**设备端**: 根据场景选择验证模式  
**device-comm-service**: 负责数据采集和转发  
**visitor-service**: 负责业务验证和处理

### Mode 5: 边缘AI计算（视频监控）

**设备端**: 完成AI分析  
**device-comm-service**: 负责结构化数据采集和转发  
**video-service**: 负责业务处理和告警

## 禁止职责清单

### 业务逻辑禁止

- ❌ 不处理门禁控制逻辑（由access-service处理）
- ❌ 不处理考勤统计逻辑（由attendance-service处理）
- ❌ 不处理消费结算逻辑（由consume-service处理）
- ❌ 不处理访客审批逻辑（由visitor-service处理）
- ❌ 不处理视频分析逻辑（由video-service处理）

### 设备管理禁止

- ❌ 不管理设备实体（由common-service管理）
- ❌ 不处理设备业务配置（由业务服务处理）
- ❌ 不处理设备权限管理（由业务服务处理）

### 识别处理禁止

- ❌ 不做设备识别（识别在设备端完成）
- ❌ 不做生物特征提取（由biometric-service处理）
- ❌ 不做特征比对（由设备端完成）

## 数据访问规范

### DAO层规范

- ✅ 统一使用`@Mapper`注解
- ✅ 统一使用`Dao`后缀命名
- ✅ 继承`BaseMapper<Entity>`
- ❌ 禁止使用`@Repository`注解
- ❌ 禁止使用`Repository`后缀

### 依赖注入规范

- ✅ 统一使用`@Resource`注解
- ❌ 禁止使用`@Autowired`注解

## 版本信息

- **文档版本**: 1.0.0
- **创建日期**: 2025-01-30
- **最后更新**: 2025-01-30
- **维护团队**: IOE-DREAM架构团队


