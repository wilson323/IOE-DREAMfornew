# 设备通讯管理模块

> **模块版本**: v1.0.0
> **服务名称**: ioedream-device-comm-service
> **服务端口**: 8087
> **创建时间**: 2025-12-16
> **最后更新**: 2025-12-16
> **模块负责人**: IOE-DREAM架构委员会

---

## 📋 模块职责

设备通讯管理模块是IOE-DREAM智慧园区一卡通管理平台的基础设施模块，负责统一管理各类设备的通信协议、连接状态、数据采集和远程控制。

### 核心业务价值

- **统一协议**: 支持多种设备通信协议的统一接入
- **实时监控**: 设备状态实时监控和故障预警
- **远程控制**: 设备远程配置和控制操作
- **数据采集**: 设备数据采集和标准化处理
- **连接管理**: 设备连接池和负载均衡

---

## 🏗️ 技术架构

### 微服务架构定位

```
ioedream-device-comm-service (8087)
    ↓ 提供设备通讯服务给
ioedream-access-service (8090)     - 门禁设备通信
ioedream-attendance-service (8091) - 考勤设备通信
ioedream-video-service (8092)      - 视频设备通信
ioedream-consume-service (8094)    - 消费设备通信
```

### 支持的设备协议

| 协议类型 | 设备类型 | 通信方式 | 特点 |
|---------|----------|----------|------|
| **TCP/IP** | 门禁控制器、考勤机 | Socket长连接 | 实时性强、稳定可靠 |
| **HTTP/HTTPS** | 智能摄像头、POS机 | RESTful API | 标准化、易于集成 |
| **WebSocket** | 实时监控设备 | 双向通信 | 实时推送、低延迟 |
| **Modbus** | 工业控制设备 | 串口通信 | 工业标准、可靠性高 |
| **SDK专用** | 生物识别设备 | 厂商SDK | 功能完整、性能优异 |

---

## 🎯 核心功能模块

### 1. 设备连接管理

- **连接池管理**: 维护设备连接池，支持连接复用
- **心跳检测**: 定期检测设备连接状态
- **自动重连**: 连接断开时自动重连机制
- **负载均衡**: 多设备负载均衡和故障转移

### 2. 协议适配器

- **协议解析**: 各种设备协议的解析和封装
- **数据转换**: 设备数据到标准格式的转换
- **命令封装**: 标准命令到设备协议的封装
- **异常处理**: 通信异常的处理和恢复

### 3. 设备状态监控

- **在线状态**: 设备在线/离线状态监控
- **性能指标**: 设备响应时间、成功率监控
- **告警机制**: 设备故障和异常告警
- **统计分析**: 设备运行数据统计分析

### 4. 数据采集处理

- **实时数据**: 设备实时数据采集
- **批量数据**: 历史数据批量获取
- **数据缓存**: 采集数据的临时缓存
- **数据分发**: 数据到业务模块的分发

---

## 📁 入口与启动

### 后端入口文件

```
microservices/ioedream-device-comm-service/src/main/java/net/lab1024/sa/device/comm/
├── controller/
│   ├── DeviceCommController.java          # 设备通讯控制器 ✅
│   ├── DeviceStatusController.java        # 设备状态控制器 ✅
│   └── ProtocolConfigController.java      # 协议配置控制器 🔄
├── service/
│   ├── DeviceCommService.java             # 设备通讯服务接口 ✅
│   ├── DeviceStatusService.java           # 设备状态服务接口 ✅
│   └── impl/
│       ├── DeviceCommServiceImpl.java     # 服务实现 ✅
│       └── DeviceStatusServiceImpl.java   # 状态服务实现 ✅
├── manager/
│   ├── ConnectionManager.java             # 连接管理器 ✅
│   ├── ProtocolAdapterManager.java         # 协议适配管理器 ✅
│   └── DataCollectManager.java            # 数据采集管理器 🔄
├── dao/
│   ├── DeviceInfoDao.java                 # 设备信息DAO ✅
│   ├── DeviceStatusDao.java               # 设备状态DAO ✅
│   └── ProtocolConfigDao.java             # 协议配置DAO 🔄
└── domain/
    ├── entity/
    │   ├── DeviceInfoEntity.java          # 设备信息实体 ✅
    │   ├── DeviceStatusEntity.java        # 设备状态实体 ✅
    │   └── ProtocolConfigEntity.java      # 协议配置实体 🔄
    └── vo/
        ├── DeviceStatusVO.java            # 设备状态视图 ✅
        └── ProtocolConfigVO.java          # 协议配置视图 🔄
```

### 启动配置

**Spring Boot 启动类**: `DeviceCommApplication.java`
**端口配置**: 8087
**上下文路径**: `/api/device/comm`

---

## 🔌 对外接口

### 设备连接管理接口

```java
@RestController
@RequestMapping("/api/device/comm/connection")
public class DeviceCommController {

    @PostMapping("/connect")
    @PermissionCheck("device:connect")
    public ResponseDTO<String> connectDevice(@Valid @RequestBody DeviceConnectForm form);

    @PostMapping("/disconnect")
    @PermissionCheck("device:disconnect")
    public ResponseDTO<String> disconnectDevice(@RequestParam Long deviceId);

    @GetMapping("/status/{deviceId}")
    @PermissionCheck("device:status:query")
    public ResponseDTO<DeviceStatusVO> getDeviceStatus(@PathVariable Long deviceId);
}
```

### 协议配置接口

```java
@RestController
@RequestMapping("/api/device/comm/protocol")
public class ProtocolConfigController {

    @PostMapping("/config")
    @PermissionCheck("device:protocol:config")
    public ResponseDTO<String> configProtocol(@Valid @RequestBody ProtocolConfigForm form);

    @GetMapping("/list")
    @PermissionCheck("device:protocol:query")
    public ResponseDTO<List<ProtocolConfigVO>> getProtocolList();
}
```

### 数据采集接口

```java
@RestController
@RequestMapping("/api/device/comm/data")
public class DataCollectController {

    @PostMapping("/collect/{deviceId}")
    @PermissionCheck("device:data:collect")
    public ResponseDTO<DeviceDataVO> collectData(@PathVariable Long deviceId);

    @PostMapping("/batch-collect")
    @PermissionCheck("device:data:batch")
    public ResponseDTO<List<DeviceDataVO>> batchCollectData(@RequestBody List<Long> deviceIds);
}
```

---

## 🗄️ 数据模型

### 核心数据表

#### 1. 设备信息表 (t_device_info)

```sql
CREATE TABLE `t_device_info` (
    `device_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '设备ID',
    `device_code` VARCHAR(50) NOT NULL COMMENT '设备编码',
    `device_name` VARCHAR(100) NOT NULL COMMENT '设备名称',
    `device_type` VARCHAR(30) NOT NULL COMMENT '设备类型',
    `protocol_type` VARCHAR(20) NOT NULL COMMENT '协议类型',
    `ip_address` VARCHAR(15) NOT NULL COMMENT 'IP地址',
    `port` INT NOT NULL COMMENT '端口号',
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    `config_json` TEXT COMMENT '设备配置JSON',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '删除标志',
    PRIMARY KEY (`device_id`),
    UNIQUE KEY `uk_device_code` (`device_code`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备信息表';
```

#### 2. 设备状态表 (t_device_status)

```sql
CREATE TABLE `t_device_status` (
    `status_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '状态ID',
    `device_id` BIGINT(20) NOT NULL COMMENT '设备ID',
    `online_status` TINYINT(1) NOT NULL COMMENT '在线状态：1-在线 0-离线',
    `last_heartbeat` DATETIME COMMENT '最后心跳时间',
    `response_time` INT COMMENT '响应时间(ms)',
    `error_count` INT DEFAULT 0 COMMENT '错误次数',
    `status_message` VARCHAR(500) COMMENT '状态消息',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`status_id`),
    UNIQUE KEY `uk_device_status` (`device_id`),
    KEY `idx_heartbeat_time` (`last_heartbeat`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备状态表';
```

#### 3. 协议配置表 (t_protocol_config)

```sql
CREATE TABLE `t_protocol_config` (
    `config_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `protocol_type` VARCHAR(20) NOT NULL COMMENT '协议类型',
    `config_name` VARCHAR(100) NOT NULL COMMENT '配置名称',
    `config_json` TEXT NOT NULL COMMENT '配置参数JSON',
    `description` VARCHAR(500) COMMENT '配置描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`config_id`),
    KEY `idx_protocol_type` (`protocol_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协议配置表';
```

---

## 🔧 核心配置

### application.yml 设备通讯模块配置

```yaml
# 设备通讯模块配置
device:
  comm:
    # 连接池配置
    connection:
      max-pool-size: 50          # 最大连接池大小
      max-idle-time: 300000      # 最大空闲时间(ms)
      connection-timeout: 10000   # 连接超时时间(ms)
      read-timeout: 30000        # 读取超时时间(ms)

    # 心跳配置
    heartbeat:
      enabled: true              # 启用心跳检测
      interval: 30000           # 心跳间隔(ms)
      timeout: 10000            # 心跳超时(ms)
      retry-count: 3            # 重试次数

    # 协议配置
    protocol:
      tcp:
        keep-alive: true        # TCP保活
        so-timeout: 30000       # Socket超时
      http:
        connection-timeout: 10000  # HTTP连接超时
        read-timeout: 30000        # HTTP读取超时
      websocket:
        ping-interval: 30000       # WebSocket心跳间隔
        max-idle-timeout: 600000   # 最大空闲超时

    # 数据采集配置
    data:
      collect-enabled: true     # 启用数据采集
      batch-size: 100          # 批量采集大小
      queue-size: 1000         # 队列大小
      thread-pool-size: 10     # 线程池大小
```

---

## 🧪 测试与质量

### 测试状态

- **单元测试覆盖率**: 75% ✅
- **集成测试**: 部分完成 🔄
- **性能测试**: 待开始 ⏳
- **协议兼容性测试**: 部分完成 🔄

### 质量检查清单

```bash
✅ 四层架构规范: 95% 合规
✅ @Resource依赖注入: 100% 合规
✅ 权限控制注解: 90% 合规
✅ 数据库索引优化: 85% 完成
🔄 单元测试覆盖率: 75% (目标80%)
⏳ 协议适配器测试: 60% (目标90%)
```

---

## 📞 支持与维护

### 技术支持

- **设备通讯团队**: 负责设备通讯协议开发和维护
- **架构委员会**: 负责整体架构设计和技术决策
- **运维团队**: 负责设备监控和故障处理

### 相关文档

- **[设备通讯协议规范](../技术规范/设备通讯协议规范.md)**
- **[API接口文档](../API接口/设备通讯API.md)**
- **[故障排查手册](../运维手册/设备通讯故障排查.md)**

---

## 🔄 变更记录

### 2025-12-16 v1.0.0 - 初始版本
**新增功能**:
- ✅ 完成设备连接管理核心功能
- ✅ 实现TCP/HTTP/WebSocket协议支持
- ✅ 建立设备状态监控机制
- ✅ 创建基础数据采集功能

**下一步计划**:
- 🎯 扩展更多设备协议支持
- 🎯 优化连接池性能和稳定性
- 🎯 完善数据采集和处理机制
- 🎯 建立完整的监控告警体系

---

**📞 如有问题，请联系设备通讯开发团队或查阅相关技术文档。**