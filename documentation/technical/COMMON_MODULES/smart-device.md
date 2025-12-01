# 设备管理公共模块开发文档

> **📋 版本**: v1.0.0
> **📋 创建时间**: 2025-01-10
> **📋 适用范围**: 所有业务模块的设备管理功能

---

## 📖 模块概述

### 模块简介
smart-device 是 SmartAdmin 项目的设备管理公共模块，提供统一的设备注册、配置、监控、控制功能，支持多种设备类型和协议的统一管理。

### 核心特性
- **多设备类型支持**: 摄像头、门禁控制器、消费终端、考勤机等
- **实时状态监控**: 设备在线状态、运行状态、故障检测
- **区域关联管理**: 设备与区域的层级关联和权限继承
- **设备分组管理**: 基于区域的设备分组和分类管理
- **远程控制**: 设备配置下发、参数更新、远程重启
- **设备认证**: 安全的设备认证和通信机制
- **故障诊断**: 自动故障检测和恢复建议
- **空间可视化**: 设备在区域中的空间位置和关系展示

### 业务关联
- **与区域管理集成**: 设备归属区域管理，支持区域权限继承
- **与权限系统集成**: 基于区域的设备访问权限控制
- **与监控系统集成**: 设备状态实时监控和告警推送
- **与数据分析集成**: 设备运行数据的统计分析和报表

---

## 🏗️ 架构设计

### 模块结构

```
smart-device/
├── controller/                    # 设备控制器
│   ├── DeviceController.java           # 设备管理控制器
│   ├── DeviceGroupController.java     # 设备分组控制器
│   ├── DeviceMonitorController.java   # 设备监控控制器
│   └── DeviceConfigController.java    # 设备配置控制器
├── service/                      # 设备服务层
│   ├── DeviceService.java              # 设备管理服务
│   ├── DeviceGroupService.java        # 设备分组服务
│   ├── DeviceMonitorService.java      # 设备监控服务
│   └── DeviceConfigService.java       # 设备配置服务
├── manager/                      # 设备管理层
│   ├── DeviceManager.java              # 设备管理器
│   ├── DeviceConnectionManager.java    # 设备连接管理器
│   ├── DeviceHeartbeatManager.java     # 设备心跳管理器
│   └── DeviceStateManager.java        # 设备状态管理器
├── dao/                          # 设备数据层
│   ├── DeviceDao.java                 # 设备DAO
│   ├── DeviceGroupDao.java           # 设备分组DAO
│   ├── DeviceMonitorDao.java         # 设备监控DAO
│   └── DeviceConfigDao.java          # 设备配置DAO
├── entity/                       # 设备实体
│   ├── DeviceEntity.java               # 设备实体
│   ├── DeviceGroupEntity.java        # 设备分组实体
│   ├── DeviceMonitorEntity.java      # 设备监控实体
│   └── DeviceConfigEntity.java       # 设备配置实体
├── protocol/                     # 设备协议
│   ├── DeviceProtocol.java            # 设备协议接口
│   ├── CameraProtocol.java           # 摄像头协议
│   ├── AccessProtocol.java           # 门禁协议
│   ├── AttendanceProtocol.java       # 考勤协议
│   └── ConsumptionProtocol.java      # 消费协议
└── notification/                # 设备通知
    ├── DeviceNotificationService.java # 设备通知服务
    ├── DeviceEventPublisher.java      # 设备事件发布器
    └── DeviceMessageHandler.java      # 设备消息处理器
```

### 核心设计模式

```java
// 设备策略模式
@Component
public class DeviceProtocolFactory {

    private final Map<DeviceType, DeviceProtocol> protocolMap = new ConcurrentHashMap<>();

    public DeviceProtocolFactory(List<DeviceProtocol> protocols) {
        protocols.forEach(protocol ->
            protocolMap.put(protocol.getSupportedDeviceType(), protocol));
    }

    /**
     * 获取设备协议处理器
     * @param deviceType 设备类型
     * @return 协议处理器
     */
    public DeviceProtocol getProtocol(DeviceType deviceType) {
        DeviceProtocol protocol = protocolMap.get(deviceType);
        if (protocol == null) {
            throw new UnsupportedOperationException("不支持的设备类型: " + deviceType);
        }
        return protocol;
    }

    /**
     * 支持的设备类型
     */
    public List<DeviceType> getSupportedDeviceTypes() {
        return new ArrayList<>(protocolMap.keySet());
    }
}

// 设备观察者模式
@Component
public class DeviceEventManager {

    private final Map<Long, List<DeviceEventListener>> listeners = new ConcurrentHashMap<>();

    /**
     * 注册设备事件监听器
     */
    public void registerListener(Long deviceId, DeviceEventListener listener) {
        listeners.computeIfAbsent(deviceId, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    /**
     * 发布设备事件
     */
    public void publishEvent(DeviceEvent event) {
        List<DeviceEventListener> deviceListeners = listeners.get(event.getDeviceId());
        if (deviceListeners != null) {
            deviceListeners.forEach(listener -> {
                try {
                    listener.onDeviceEvent(event);
                } catch (Exception e) {
                    log.error("处理设备事件失败", e);
                }
            });
        }
    }
}
```

---

## 🗄️ 数据库设计

### 设备表 (t_device)

```sql
CREATE TABLE t_device (
    device_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',
    device_code VARCHAR(100) NOT NULL UNIQUE COMMENT '设备编码',
    device_name VARCHAR(200) NOT NULL COMMENT '设备名称',
    device_type VARCHAR(50) NOT NULL COMMENT '设备类型',
    device_model VARCHAR(100) COMMENT '设备型号',
    device_brand VARCHAR(100) COMMENT '设备品牌',
    group_id BIGINT COMMENT '设备分组ID',
    area_id BIGINT COMMENT '所属区域ID',
    location_id BIGINT COMMENT '位置ID',
    ip_address VARCHAR(50) COMMENT '设备IP地址',
    port_number INT COMMENT '设备端口号',
    mac_address VARCHAR(50) COMMENT '设备MAC地址',
    serial_number VARCHAR(100) COMMENT '设备序列号',
    status TINYINT DEFAULT 1 COMMENT '设备状态：1-在线，0-离线',
    last_heartbeat_time DATETIME COMMENT '最后心跳时间',
    config_json JSON COMMENT '设备配置JSON',
    security_config JSON COMMENT '安全配置JSON',
    protocol_version VARCHAR(50) COMMENT '协议版本',
    firmware_version VARCHAR(50) COMMENT '固件版本',
    install_time DATETIME COMMENT '安装时间',
    expire_time DATETIME COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    version INT DEFAULT 1 COMMENT '版本号（乐观锁）',
    INDEX idx_device_code (device_code),
    INDEX idx_device_type (device_type),
    INDEX idx_group_id (group_id),
    INDEX idx_location_id (location_id),
    INDEX idx_status (status),
    INDEX idx_last_heartbeat (last_heartbeat_time),
    INDEX idx_deleted_flag (deleted_flag),
    UNIQUE KEY uk_device_serial (serial_number)
) COMMENT = '设备表';

-- 设备类型枚举值
INSERT INTO t_sys_dict (dict_type, dict_key, dict_value, sort_order, remark) VALUES
('DEVICE_TYPE', 'CAMERA', '摄像头', 1, '视频监控设备'),
('DEVICE_TYPE', 'ACCESS_CONTROLLER', '门禁控制器', 2, '门禁控制设备'),
('DEVICE_TYPE', 'ATTENDANCE_MACHINE', '考勤机', 3, '考勤打卡设备'),
('DEVICE_TYPE', 'CONSUMPTION_TERMINAL', '消费终端', 4, '消费支付设备'),
('DEVICE_TYPE', 'ALARM_DEVICE', '报警设备', 5, '报警检测设备');
```

### 设备分组表 (t_device_group)

```sql
CREATE TABLE t_device_group (
    group_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分组ID',
    group_code VARCHAR(100) NOT NULL UNIQUE COMMENT '分组编码',
    group_name VARCHAR(200) NOT NULL COMMENT '分组名称',
    parent_group_id BIGINT DEFAULT 0 COMMENT '父分组ID',
    group_type VARCHAR(50) COMMENT '分组类型',
    group_level INT DEFAULT 1 COMMENT '分组层级',
    sort_order INT DEFAULT 0 COMMENT '排序',
    description TEXT COMMENT '分组描述',
    group_config JSON COMMENT '分组配置JSON',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    INDEX idx_parent_id (parent_group_id),
    INDEX idx_group_type (group_type),
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order)
) COMMENT = '设备分组表';
```

### 设备状态监控表 (t_device_monitor)

```sql
CREATE TABLE t_device_monitor (
    monitor_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '监控ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    monitor_type VARCHAR(50) NOT NULL COMMENT '监控类型',
    monitor_value VARCHAR(500) COMMENT '监控值',
    monitor_unit VARCHAR(20) COMMENT '监控单位',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-异常',
    threshold_min DECIMAL(10,2) COMMENT '最小阈值',
    threshold_max DECIMAL(10,2) COMMENT '最大阈值',
    alert_level TINYINT COMMENT '告警级别：1-提示，2-警告，3-严重',
    last_update_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_device_id (device_id),
    INDEX idx_monitor_type (monitor_type),
    INDEX idx_status (status),
    INDEX idx_last_update (last_update_time),
    INDEX idx_alert_level (alert_level)
) COMMENT = '设备状态监控表';
```

### 设备配置表 (t_device_config)

```sql
CREATE TABLE t_device_config (
    config_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    config_type VARCHAR(50) COMMENT '配置类型',
    is_encrypted TINYINT DEFAULT 0 COMMENT '是否加密：1-是，0-否',
    version INT DEFAULT 1 COMMENT '配置版本',
    effective_time DATETIME COMMENT '生效时间',
    expire_time DATETIME COMMENT '过期时间',
    status TINYINT DEFAULT 1 COMMENT '状态：1-生效，0-失效',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建人ID',
    deleted_flag TINYINT DEFAULT 0 COMMENT '删除标志：0-未删除，1-已删除',
    UNIQUE KEY uk_device_config (device_id, config_key, version),
    INDEX idx_device_id (device_id),
    INDEX idx_config_key (config_key),
    INDEX idx_status (status),
    INDEX idx_effective_time (effective_time),
    INDEX idx_expire_time (expire_time)
) COMMENT = '设备配置表';
```

### 设备事件表 (t_device_event)

```sql
CREATE TABLE t_device_event (
    event_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '事件ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型',
    event_code VARCHAR(100) COMMENT '事件编码',
    event_desc TEXT COMMENT '事件描述',
    event_level TINYINT COMMENT '事件级别：1-信息，2-警告，3-错误，4-严重',
    event_data JSON COMMENT '事件数据JSON',
    event_status TINYINT DEFAULT 1 COMMENT '事件状态：1-未处理，2-处理中，3-已处理',
    process_user_id BIGINT COMMENT '处理人ID',
    process_time DATETIME COMMENT '处理时间',
    process_remark TEXT COMMENT '处理备注',
    event_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '事件时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_device_id (device_id),
    INDEX idx_event_type (event_type),
    INDEX idx_event_level (event_level),
    INDEX idx_event_status (event_status),
    INDEX idx_event_time (event_time),
    INDEX idx_process_user (process_user_id)
) COMMENT = '设备事件表';
```

---

## 🔧 后端实现

### 核心控制器 (DeviceController)

```java
@RestController
@RequestMapping("/api/device")
@Tag(name = "设备管理", description = "设备管理相关接口")
public class DeviceController {

    @Resource
    private DeviceService deviceService;

    @GetMapping("/page")
    @Operation(summary = "分页查询设备")
    @SaCheckPermission("device:page")
    public ResponseDTO<PageResult<DeviceVO>> queryPage(DeviceQueryDTO queryDTO) {
        PageResult<DeviceVO> result = deviceService.queryPage(queryDTO);
        return ResponseDTO.ok(result);
    }

    @PostMapping
    @Operation(summary = "新增设备")
    @SaCheckPermission("device:add")
    public ResponseDTO<String> add(@Valid @RequestBody DeviceCreateDTO createDTO) {
        deviceService.add(createDTO);
        return ResponseDTO.ok();
    }

    @PutMapping("/{deviceId}")
    @Operation(summary = "修改设备")
    @SaCheckPermission("device:update")
    public ResponseDTO<String> update(@PathVariable Long deviceId,
                                     @Valid @RequestBody DeviceUpdateDTO updateDTO) {
        updateDTO.setDeviceId(deviceId);
        deviceService.update(updateDTO);
        return ResponseDTO.ok();
    }

    @DeleteMapping("/{deviceId}")
    @Operation(summary = "删除设备")
    @SaCheckPermission("device:delete")
    public ResponseDTO<String> delete(@PathVariable Long deviceId) {
        deviceService.delete(deviceId);
        return ResponseDTO.ok();
    }

    @GetMapping("/{deviceId}")
    @Operation(summary = "获取设备详情")
    @SaCheckPermission("device:detail")
    public ResponseDTO<DeviceDetailVO> getDetail(@PathVariable Long deviceId) {
        DeviceDetailVO detail = deviceService.getDetail(deviceId);
        return ResponseDTO.ok(detail);
    }

    @PostMapping("/{deviceId}/status")
    @Operation(summary = "控制设备状态")
    @SaCheckPermission("device:control")
    public ResponseDTO<String> controlDevice(@PathVariable Long deviceId,
                                            @Valid @RequestBody DeviceControlDTO controlDTO) {
        deviceService.controlDevice(deviceId, controlDTO);
        return ResponseDTO.ok();
    }

    @GetMapping("/{deviceId}/monitor")
    @Operation(summary = "获取设备监控数据")
    @SaCheckPermission("device:monitor")
    public ResponseDTO<List<DeviceMonitorVO>> getDeviceMonitor(@PathVariable Long deviceId) {
        List<DeviceMonitorVO> monitorData = deviceService.getDeviceMonitor(deviceId);
        return ResponseDTO.ok(monitorData);
    }

    @GetMapping("/{deviceId}/config")
    @Operation(summary = "获取设备配置")
    @SaCheckPermission("device:config")
    public ResponseDTO<List<DeviceConfigVO>> getDeviceConfig(@PathVariable Long deviceId) {
        List<DeviceConfigVO> config = deviceService.getDeviceConfig(deviceId);
        return ResponseDTO.ok(config);
    }

    @PutMapping("/{deviceId}/config")
    @Operation(summary = "更新设备配置")
    @SaCheckPermission("device:config:update")
    public ResponseDTO<String> updateDeviceConfig(@PathVariable Long deviceId,
                                                 @Valid @RequestBody DeviceConfigUpdateDTO configDTO) {
        deviceService.updateDeviceConfig(deviceId, configDTO);
        return ResponseDTO.ok();
    }
}
```

### 核心服务层 (DeviceService)

```java
@Service
@Transactional(readOnly = true)
public class DeviceService {

    @Resource
    private DeviceManager deviceManager;
    @Resource
    private DeviceProtocolFactory protocolFactory;
    @Resource
    private DeviceMonitorService monitorService;
    @Resource
    private DeviceConfigService configService;

    public PageResult<DeviceVO> queryPage(DeviceQueryDTO queryDTO) {
        // 1. 参数验证
        validateQueryDTO(queryDTO);

        // 2. 查询设备数据
        PageResult<DeviceVO> result = deviceManager.queryPage(queryDTO);

        // 3. 补充设备状态信息
        enrichDeviceStatus(result.getRecords());

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(DeviceCreateDTO createDTO) {
        // 1. 验证设备编码唯一性
        validateDeviceCodeUnique(createDTO.getDeviceCode());

        // 2. 验证设备类型
        validateDeviceType(createDTO.getDeviceType());

        // 3. 创建设备
        DeviceEntity device = BeanUtil.copyProperties(createDTO, DeviceEntity.class);
        device.setStatus(0); // 默认离线状态
        device.setVersion(1);

        deviceManager.add(device);

        // 4. 初始化设备配置
        initializeDeviceConfig(device.getDeviceId(), createDTO.getDeviceType());

        // 5. 启动设备监控
        monitorService.startDeviceMonitor(device.getDeviceId());

        // 6. 发布设备创建事件
        eventPublisher.publishEvent(new DeviceCreateEvent(device.getDeviceId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(DeviceUpdateDTO updateDTO) {
        // 1. 验证设备存在性
        DeviceEntity device = deviceManager.getById(updateDTO.getDeviceId());
        if (device == null) {
            throw new SmartException("设备不存在");
        }

        // 2. 验证编码唯一性（排除自身）
        validateDeviceCodeUnique(updateDTO.getDeviceCode(), updateDTO.getDeviceId());

        // 3. 更新设备信息
        DeviceEntity updateEntity = BeanUtil.copyProperties(updateDTO, DeviceEntity.class);
        updateEntity.setVersion(device.getVersion() + 1); // 版本递增

        deviceManager.update(updateEntity);

        // 4. 发布设备更新事件
        eventPublisher.publishEvent(new DeviceUpdateEvent(device.getDeviceId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long deviceId) {
        // 1. 验证设备存在性
        DeviceEntity device = deviceManager.getById(deviceId);
        if (device == null) {
            throw new SmartException("设备不存在");
        }

        // 2. 停止设备监控
        monitorService.stopDeviceMonitor(deviceId);

        // 3. 软删除设备
        deviceManager.softDelete(deviceId);

        // 4. 发布设备删除事件
        eventPublisher.publishEvent(new DeviceDeleteEvent(deviceId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void controlDevice(Long deviceId, DeviceControlDTO controlDTO) {
        // 1. 验证设备存在性
        DeviceEntity device = deviceManager.getById(deviceId);
        if (device == null) {
            throw new SmartException("设备不存在");
        }

        // 2. 验证设备在线状态
        if (device.getStatus() != 1) {
            throw new SmartException("设备离线，无法控制");
        }

        // 3. 获取设备协议处理器
        DeviceProtocol protocol = protocolFactory.getProtocol(DeviceType.valueOf(device.getDeviceType()));

        // 4. 执行设备控制
        DeviceControlResult result = protocol.controlDevice(device, controlDTO);

        // 5. 记录控制结果
        deviceManager.recordControlResult(deviceId, controlDTO, result);

        // 6. 发布设备控制事件
        eventPublisher.publishEvent(new DeviceControlEvent(deviceId, controlDTO, result));
    }

    public List<DeviceMonitorVO> getDeviceMonitor(Long deviceId) {
        return monitorService.getDeviceMonitorData(deviceId);
    }

    public List<DeviceConfigVO> getDeviceConfig(Long deviceId) {
        return configService.getDeviceConfig(deviceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateDeviceConfig(Long deviceId, DeviceConfigUpdateDTO configDTO) {
        // 1. 验证设备存在性
        DeviceEntity device = deviceManager.getById(deviceId);
        if (device == null) {
            throw new SmartException("设备不存在");
        }

        // 2. 更新设备配置
        configService.updateDeviceConfig(deviceId, configDTO);

        // 3. 发布配置更新事件
        eventPublisher.publishEvent(new DeviceConfigUpdateEvent(deviceId, configDTO));
    }

    private void enrichDeviceStatus(List<DeviceVO> devices) {
        devices.forEach(device -> {
            // 获取设备最新状态
            DeviceStatus status = monitorService.getDeviceStatus(device.getDeviceId());
            device.setDeviceStatus(status.getStatus());
            device.setLastHeartbeatTime(status.getLastHeartbeatTime());
            device.setIsOnline(status.getIsOnline());
        });
    }

    private void validateQueryDTO(DeviceQueryDTO queryDTO) {
        if (queryDTO.getDeviceType() != null) {
            validateDeviceType(queryDTO.getDeviceType());
        }
    }

    private void validateDeviceCodeUnique(String deviceCode) {
        validateDeviceCodeUnique(deviceCode, null);
    }

    private void validateDeviceCodeUnique(String deviceCode, Long excludeDeviceId) {
        boolean exists = deviceManager.checkDeviceCodeExists(deviceCode, excludeDeviceId);
        if (exists) {
            throw new SmartException("设备编码已存在");
        }
    }

    private void validateDeviceType(String deviceType) {
        try {
            DeviceType.valueOf(deviceType);
        } catch (IllegalArgumentException e) {
            throw new SmartException("不支持的设备类型: " + deviceType);
        }
    }

    private void initializeDeviceConfig(Long deviceId, String deviceType) {
        // 根据设备类型初始化默认配置
        DeviceProtocol protocol = protocolFactory.getProtocol(DeviceType.valueOf(deviceType));
        List<DeviceConfig> defaultConfigs = protocol.getDefaultConfigs();

        defaultConfigs.forEach(config -> {
            DeviceConfigEntity configEntity = new DeviceConfigEntity();
            configEntity.setDeviceId(deviceId);
            configEntity.setConfigKey(config.getKey());
            configEntity.setConfigValue(config.getValue());
            configEntity.setConfigType(config.getType());
            configEntity.setIsEncrypted(config.getEncrypted() ? 1 : 0);
            configEntity.setVersion(1);
            configEntity.setStatus(1);
            configService.add(configEntity);
        });
    }
}
```

### 核心管理层 (DeviceManager)

```java
@Component
public class DeviceManager {

    @Resource
    private DeviceDao deviceDao;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private DeviceProtocolFactory protocolFactory;

    // 缓存常量
    private static final String CACHE_PREFIX = "device:";
    private static final String STATUS_PREFIX = "device:status:";
    private static final Duration CACHE_EXPIRE = Duration.ofMinutes(10);

    @Cacheable(value = "device", key = "#deviceId")
    public DeviceEntity getById(Long deviceId) {
        return deviceDao.selectById(deviceId);
    }

    @Cacheable(value = "device", key = "'page:' + #queryDTO.hashCode()")
    public PageResult<DeviceVO> queryPage(DeviceQueryDTO queryDTO) {
        // 构建查询条件
        QueryWrapper<DeviceEntity> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotBlank(queryDTO.getDeviceCode())) {
            queryWrapper.like("device_code", queryDTO.getDeviceCode());
        }
        if (StringUtils.isNotBlank(queryDTO.getDeviceName())) {
            queryWrapper.like("device_name", queryDTO.getDeviceName());
        }
        if (queryDTO.getDeviceType() != null) {
            queryWrapper.eq("device_type", queryDTO.getDeviceType());
        }
        if (queryDTO.getGroupId() != null) {
            queryWrapper.eq("group_id", queryDTO.getGroupId());
        }
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq("status", queryDTO.getStatus());
        }
        if (StringUtils.isNotBlank(queryDTO.getIpAddress())) {
            queryWrapper.like("ip_address", queryDTO.getIpAddress());
        }

        queryWrapper.eq("deleted_flag", 0)
                   .orderByDesc("create_time");

        // 分页查询
        Page<DeviceEntity> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<DeviceEntity> result = deviceDao.selectPage(page, queryWrapper);

        // 转换为VO
        List<DeviceVO> records = result.getRecords().stream().map(entity -> {
            DeviceVO vo = new DeviceVO();
            BeanUtil.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.<DeviceVO>builder()
            .records(records)
            .total(result.getTotal())
            .pageNum(result.getCurrent())
            .pageSize(result.getSize())
            .build();
    }

    @CacheEvict(value = "device", allEntries = true)
    public void add(DeviceEntity device) {
        deviceDao.insert(device);

        // 缓存设备基本信息
        cacheDeviceBasicInfo(device);
    }

    @CacheEvict(value = "device", allEntries = true)
    public void update(DeviceEntity device) {
        // 乐观锁更新
        QueryWrapper<DeviceEntity> updateWrapper = new QueryWrapper<>();
        updateWrapper.eq("device_id", device.getDeviceId())
                   .eq("version", device.getVersion() - 1);

        int updateCount = deviceDao.update(device, updateWrapper);
        if (updateCount == 0) {
            throw new SmartException("设备信息已变更，请刷新后重试");
        }

        // 更新缓存
        cacheDeviceBasicInfo(device);
    }

    @CacheEvict(value = "device", allEntries = true)
    public void softDelete(Long deviceId) {
        DeviceEntity device = new DeviceEntity();
        device.setDeviceId(deviceId);
        device.setDeletedFlag(1);
        device.setUpdateTime(LocalDateTime.now());

        deviceDao.updateById(device);

        // 清除缓存
        clearDeviceCache(deviceId);
    }

    public boolean checkDeviceCodeExists(String deviceCode) {
        return checkDeviceCodeExists(deviceCode, null);
    }

    public boolean checkDeviceCodeExists(String deviceCode, Long excludeDeviceId) {
        QueryWrapper<DeviceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("device_code", deviceCode)
                   .eq("deleted_flag", 0);

        if (excludeDeviceId != null) {
            queryWrapper.ne("device_id", excludeDeviceId);
        }

        return deviceDao.selectCount(queryWrapper) > 0;
    }

    public void recordControlResult(Long deviceId, DeviceControlDTO controlDTO, DeviceControlResult result) {
        // 记录设备控制日志
        DeviceControlLogEntity logEntity = new DeviceControlLogEntity();
        logEntity.setDeviceId(deviceId);
        logEntity.setControlType(controlDTO.getControlType());
        logEntity.setControlData(JsonUtils.toJsonString(controlDTO.getControlData()));
        logEntity.setResultCode(result.getCode());
        logEntity.setResultMessage(result.getMessage());
        logEntity.setControlTime(LocalDateTime.now());

        deviceControlLogDao.insert(logEntity);
    }

    private void cacheDeviceBasicInfo(DeviceEntity device) {
        String cacheKey = CACHE_PREFIX + device.getDeviceId();

        DeviceBasicInfo basicInfo = DeviceBasicInfo.builder()
            .deviceId(device.getDeviceId())
            .deviceCode(device.getDeviceCode())
            .deviceName(device.getDeviceName())
            .deviceType(device.getDeviceType())
            .ipAddress(device.getIpAddress())
            .portNumber(device.getPortNumber())
            .status(device.getStatus())
            .build();

        redisTemplate.opsForValue().set(cacheKey, basicInfo, CACHE_EXPIRE);
    }

    private void clearDeviceCache(Long deviceId) {
        String cacheKey = CACHE_PREFIX + deviceId;
        redisTemplate.delete(cacheKey);

        // 清除状态缓存
        String statusKey = STATUS_PREFIX + deviceId;
        redisTemplate.delete(statusKey);
    }
}
```

---

## 🎨 前端实现

### 设备状态管理 (useDeviceStore)

```javascript
// /store/device.js
import { defineStore } from 'pinia'
import { deviceApi } from '/@/api/device'
import { useWebSocket } from '/@/composables/useWebSocket'

export const useDeviceStore = defineStore('device', {
  state: () => ({
    // 设备列表
    deviceList: [],
    // 设备总数
    deviceTotal: 0,
    // 设备分组树
    deviceGroups: [],
    // 设备状态映射
    deviceStatusMap: new Map(),
    // 设备监控数据
    deviceMonitorData: new Map(),
    // 选中的设备
    selectedDevices: [],
    // WebSocket连接
    wsConnection: null
  }),

  getters: {
    // 获取在线设备列表
    onlineDevices: (state) => {
      return state.deviceList.filter(device => device.status === 1)
    },

    // 获取设备状态文本
    getDeviceStatusText: (state) => (deviceId) => {
      const status = state.deviceStatusMap.get(deviceId)
      return status ? (status.isOnline ? '在线' : '离线') : '未知'
    },

    // 获取设备状态颜色
    getDeviceStatusColor: (state) => (deviceId) => {
      const status = state.deviceStatusMap.get(deviceId)
      return status && status.isOnline ? '#52c41a' : '#ff4d4f'
    },

    // 获取设备监控数据
    getDeviceMonitorData: (state) => (deviceId, monitorType) => {
      const monitorData = state.deviceMonitorData.get(deviceId)
      return monitorData ? monitorData[monitorType] : null
    }
  },

  actions: {
    // 获取设备列表
    async fetchDeviceList(params = {}) {
      try {
        const result = await deviceApi.queryPage({
          pageNum: params.pageNum || 1,
          pageSize: params.pageSize || 10,
          ...params
        })

        this.deviceList = result.data.records
        this.deviceTotal = result.data.total

        // 更新设备状态缓存
        this.deviceList.forEach(device => {
          this.updateDeviceStatus(device.deviceId, {
            status: device.deviceStatus,
            isOnline: device.isOnline,
            lastHeartbeatTime: device.lastHeartbeatTime
          })
        })

        return result.data
      } catch (error) {
        console.error('获取设备列表失败:', error)
        throw error
      }
    },

    // 获取设备分组树
    async fetchDeviceGroups() {
      try {
        const result = await deviceApi.getDeviceGroups()
        this.deviceGroups = result.data
        return result.data
      } catch (error) {
        console.error('获取设备分组失败:', error)
        throw error
      }
    },

    // 更新设备状态
    updateDeviceStatus(deviceId, status) {
      this.deviceStatusMap.set(deviceId, status)
    },

    // 更新设备监控数据
    updateDeviceMonitorData(deviceId, monitorType, data) {
      if (!this.deviceMonitorData.has(deviceId)) {
        this.deviceMonitorData.set(deviceId, {})
      }
      this.deviceMonitorData.get(deviceId)[monitorType] = data
    },

    // 控制设备
    async controlDevice(deviceId, controlType, controlData) {
      try {
        const result = await deviceApi.controlDevice(deviceId, {
          controlType,
          controlData
        })
        return result.data
      } catch (error) {
        console.error('控制设备失败:', error)
        throw error
      }
    },

    // 获取设备配置
    async fetchDeviceConfig(deviceId) {
      try {
        const result = await deviceApi.getDeviceConfig(deviceId)
        return result.data
      } catch (error) {
        console.error('获取设备配置失败:', error)
        throw error
      }
    },

    // 更新设备配置
    async updateDeviceConfig(deviceId, configData) {
      try {
        const result = await deviceApi.updateDeviceConfig(deviceId, {
          configs: configData
        })
        return result.data
      } catch (error) {
        console.error('更新设备配置失败:', error)
        throw error
      }
    },

    // 初始化WebSocket连接
    initWebSocket() {
      if (this.wsConnection) {
        this.wsConnection.close()
      }

      const { connect, subscribe, disconnect } = useWebSocket('/ws/device')

      this.wsConnection = connect()

      // 订阅设备状态变更
      subscribe('device:status', (data) => {
        this.updateDeviceStatus(data.deviceId, {
          status: data.status,
          isOnline: data.isOnline,
          lastHeartbeatTime: data.lastHeartbeatTime
        })
      })

      // 订阅设备监控数据
      subscribe('device:monitor', (data) => {
        this.updateDeviceMonitorData(data.deviceId, data.monitorType, data.value)
      })

      // 订阅设备事件
      subscribe('device:event', (data) => {
        this.handleDeviceEvent(data)
      })
    },

    // 处理设备事件
    handleDeviceEvent(event) {
      // 显示事件通知
      notification[event.eventLevel === 3 ? 'error' : 'warning']({
        message: `设备事件 - ${event.eventType}`,
        description: event.eventDesc,
        duration: 0
      })

      // 可以在这里添加其他事件处理逻辑
    },

    // 关闭WebSocket连接
    closeWebSocket() {
      if (this.wsConnection) {
        this.wsConnection.close()
        this.wsConnection = null
      }
    },

    // 选中设备
    selectDevices(deviceIds) {
      this.selectedDevices = deviceIds
    },

    // 清空选中
    clearSelection() {
      this.selectedDevices = []
    }
  }
})
```

### 设备状态卡片组件 (DeviceStatusCard)

```vue
<template>
  <a-card
    class="device-status-card"
    :class="{ 'device-online': isOnline, 'device-offline': !isOnline }"
    :hoverable="true"
    @click="handleCardClick"
  >
    <template #title>
      <div class="device-title">
        <a-avatar
          :size="32"
          :src="deviceIcon"
          :style="{ backgroundColor: statusColor }"
        >
          <template #icon>
            <component :is="statusIcon" />
          </template>
        </a-avatar>
        <div class="device-info">
          <div class="device-name">{{ device.deviceName }}</div>
          <div class="device-code">{{ device.deviceCode }}</div>
        </div>
        <div class="device-status">
          <a-tag :color="statusColor">
            {{ statusText }}
          </a-tag>
        </div>
      </div>
    </template>

    <template #extra>
      <a-dropdown>
        <a-button type="text" size="small">
          <template #icon><MoreOutlined /></template>
        </a-button>
        <template #overlay>
          <a-menu>
            <a-menu-item @click="handleViewDetail">
              <template #icon><EyeOutlined /></template>
              查看详情
            </a-menu-item>
            <a-menu-item @click="handleEditDevice" v-permission="['device:update']">
              <template #icon><EditOutlined /></template>
              编辑设备
            </a-menu-item>
            <a-menu-item @click="handleControlDevice" v-permission="['device:control']">
              <template #icon><ControlOutlined /></template>
              控制设备
            </a-menu-item>
            <a-menu-divider />
            <a-menu-item @click="handleDeleteDevice" danger v-permission="['device:delete']">
              <template #icon><DeleteOutlined /></template>
              删除设备
            </a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </template>

    <div class="device-content">
      <div class="device-basic-info">
        <a-descriptions size="small" :column="1">
          <a-descriptions-item label="设备类型">
            {{ deviceTypeName }}
          </a-descriptions-item>
          <a-descriptions-item label="设备分组">
            {{ groupName }}
          </a-descriptions-item>
          <a-descriptions-item label="IP地址">
            {{ device.ipAddress }}
          </a-descriptions-item>
          <a-descriptions-item label="最后心跳">
            {{ formatLastHeartbeat }}
          </a-descriptions-item>
        </a-descriptions>
      </div>

      <!-- 监控数据 -->
      <div class="device-monitor" v-if="monitorData && monitorData.length > 0">
        <a-divider>监控数据</a-divider>
        <div class="monitor-grid">
          <div
            v-for="monitor in monitorData"
            :key="monitor.monitorType"
            class="monitor-item"
          >
            <div class="monitor-label">{{ monitor.monitorName }}</div>
            <div class="monitor-value" :class="getMonitorStatusClass(monitor)">
              {{ monitor.monitorValue }} {{ monitor.monitorUnit }}
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="device-actions">
        <a-space>
          <a-button
            size="small"
            :disabled="!isOnline"
            @click="handleControlDevice"
            v-permission="['device:control']"
          >
            <template #icon><ControlOutlined /></template>
            控制
          </a-button>
          <a-button
            size="small"
            @click="handleViewMonitor"
            v-permission="['device:monitor']"
          >
            <template #icon><LineChartOutlined /></template>
            监控
          </a-button>
          <a-button
            size="small"
            @click="handleViewConfig"
            v-permission="['device:config']"
          >
            <template #icon><SettingOutlined /></template>
            配置
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 设备详情弹窗 -->
    <DeviceDetailModal
      v-model:visible="detailVisible"
      :device-id="device.deviceId"
      @refresh="handleRefresh"
    />

    <!-- 设备控制弹窗 -->
    <DeviceControlModal
      v-model:visible="controlVisible"
      :device-id="device.deviceId"
      :device="device"
      @success="handleControlSuccess"
    />

    <!-- 设备监控弹窗 -->
    <DeviceMonitorModal
      v-model:visible="monitorVisible"
      :device-id="device.deviceId"
    />

    <!-- 设备配置弹窗 -->
    <DeviceConfigModal
      v-model:visible="configVisible"
      :device-id="device.deviceId"
    />
  </a-card>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useDeviceStore } from '/@/store/device'
import { usePermissionStore } from '/@/store/permission'
import { notification } from 'ant-design-vue'
import {
  MoreOutlined,
  EyeOutlined,
  EditOutlined,
  ControlOutlined,
  DeleteOutlined,
  LineChartOutlined,
  SettingOutlined,
  WifiOutlined,
  DisconnectOutlined
} from '@ant-design/icons-vue'
import { formatDateTime } from '/@/utils/format'
import DeviceDetailModal from './DeviceDetailModal.vue'
import DeviceControlModal from './DeviceControlModal.vue'
import DeviceMonitorModal from './DeviceMonitorModal.vue'
import DeviceConfigModal from './DeviceConfigModal.vue'

const props = defineProps({
  device: {
    type: Object,
    required: true
  },
  monitorData: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['refresh', 'select'])

const router = useRouter()
const deviceStore = useDeviceStore()
const permissionStore = usePermissionStore()

const detailVisible = ref(false)
const controlVisible = ref(false)
const monitorVisible = ref(false)
const configVisible = ref(false)

// 计算属性
const isOnline = computed(() => {
  const status = deviceStore.deviceStatusMap.get(props.device.deviceId)
  return status ? status.isOnline : false
})

const statusText = computed(() => {
  return deviceStore.getDeviceStatusText(props.device.deviceId)
})

const statusColor = computed(() => {
  return deviceStore.getDeviceStatusColor(props.device.deviceId)
})

const statusIcon = computed(() => {
  return isOnline.value ? WifiOutlined : DisconnectOutlined
})

const deviceIcon = computed(() => {
  const iconMap = {
    'CAMERA': '📹',
    'ACCESS_CONTROLLER': '🚪',
    'ATTENDANCE_MACHINE': '⏰',
    'CONSUMPTION_TERMINAL': '💳',
    'ALARM_DEVICE': '🚨'
  }
  return iconMap[props.device.deviceType] || '📱'
})

const deviceTypeName = computed(() => {
  const typeMap = {
    'CAMERA': '摄像头',
    'ACCESS_CONTROLLER': '门禁控制器',
    'ATTENDANCE_MACHINE': '考勤机',
    'CONSUMPTION_TERMINAL': '消费终端',
    'ALARM_DEVICE': '报警设备'
  }
  return typeMap[props.device.deviceType] || '未知设备'
})

const groupName = computed(() => {
  const group = deviceStore.deviceGroups.find(g => g.groupId === props.device.groupId)
  return group ? group.groupName : '未分组'
})

const formatLastHeartbeat = computed(() => {
  const status = deviceStore.deviceStatusMap.get(props.device.deviceId)
  return status && status.lastHeartbeatTime
    ? formatDateTime(status.lastHeartbeatTime)
    : '从未心跳'
})

// 方法
const handleCardClick = () => {
  emit('select', props.device.deviceId)
}

const handleViewDetail = () => {
  detailVisible.value = true
}

const handleEditDevice = () => {
  router.push(`/device/edit/${props.device.deviceId}`)
}

const handleControlDevice = () => {
  if (!isOnline.value) {
    notification.warning({
      message: '设备离线',
      description: '设备当前离线，无法进行控制操作'
    })
    return
  }
  controlVisible.value = true
}

const handleDeleteDevice = () => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除设备"${props.device.deviceName}"吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await deviceApi.delete(props.device.deviceId)
        notification.success({
          message: '删除成功',
          description: '设备删除成功'
        })
        emit('refresh')
      } catch (error) {
        notification.error({
          message: '删除失败',
          description: error.message
        })
      }
    }
  })
}

const handleViewMonitor = () => {
  monitorVisible.value = true
}

const handleViewConfig = () => {
  configVisible.value = true
}

const handleRefresh = () => {
  emit('refresh')
}

const handleControlSuccess = () => {
  notification.success({
    message: '控制成功',
    description: '设备控制指令发送成功'
  })
}

const getMonitorStatusClass = (monitor) => {
  if (monitor.status === 0) {
    return 'monitor-error'
  } else if (monitor.alertLevel === 3) {
    return 'monitor-warning'
  }
  return 'monitor-normal'
}

// 生命周期
onMounted(() => {
  // 初始化WebSocket连接
  if (!deviceStore.wsConnection) {
    deviceStore.initWebSocket()
  }
})

onUnmounted(() => {
  // 这里不关闭WebSocket，因为它可能被其他组件使用
})
</script>

<style lang="less" scoped>
.device-status-card {
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  &.device-online {
    border-left: 4px solid #52c41a;
  }

  &.device-offline {
    border-left: 4px solid #ff4d4f;
  }

  .device-title {
    display: flex;
    align-items: center;
    gap: 12px;

    .device-info {
      flex: 1;

      .device-name {
        font-weight: 500;
        font-size: 16px;
        color: #262626;
      }

      .device-code {
        font-size: 12px;
        color: #8c8c8c;
        margin-top: 2px;
      }
    }

    .device-status {
      .ant-tag {
        font-size: 12px;
      }
    }
  }

  .device-content {
    .device-basic-info {
      margin-bottom: 16px;
    }

    .device-monitor {
      .monitor-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 12px;
        margin-top: 12px;

        .monitor-item {
          text-align: center;
          padding: 8px;
          border: 1px solid #f0f0f0;
          border-radius: 6px;

          .monitor-label {
            font-size: 12px;
            color: #8c8c8c;
            margin-bottom: 4px;
          }

          .monitor-value {
            font-size: 16px;
            font-weight: 500;

            &.monitor-normal {
              color: #52c41a;
            }

            &.monitor-warning {
              color: #faad14;
            }

            &.monitor-error {
              color: #ff4d4f;
            }
          }
        }
      }
    }

    .device-actions {
      margin-top: 16px;
      text-align: center;
    }
  }
}
</style>
```

---

## 🔧 设备协议实现

### 设备协议接口 (DeviceProtocol)

```java
public interface DeviceProtocol {

    /**
     * 获取支持的设备类型
     */
    DeviceType getSupportedDeviceType();

    /**
     * 获取默认设备配置
     */
    List<DeviceConfig> getDefaultConfigs();

    /**
     * 设备连接
     */
    DeviceConnection connect(DeviceEntity device, ConnectionConfig config);

    /**
     * 设备断开连接
     */
    void disconnect(DeviceConnection connection);

    /**
     * 控制设备
     */
    DeviceControlResult controlDevice(DeviceEntity device, DeviceControlDTO controlDTO);

    /**
     * 获取设备状态
     */
    DeviceStatus getDeviceStatus(DeviceConnection connection);

    /**
     * 获取设备监控数据
     */
    List<MonitorData> getMonitorData(DeviceConnection connection);

    /**
     * 设备心跳检测
     */
    boolean heartbeat(DeviceConnection connection);

    /**
     * 处理设备事件
     */
    void handleDeviceEvent(DeviceEvent event);
}
```

### 摄像头协议实现 (CameraProtocol)

```java
@Component
public class CameraProtocol implements DeviceProtocol {

    @Resource
    private CameraClientFactory clientFactory;

    @Override
    public DeviceType getSupportedDeviceType() {
        return DeviceType.CAMERA;
    }

    @Override
    public List<DeviceConfig> getDefaultConfigs() {
        return Arrays.asList(
            DeviceConfig.builder()
                .key("resolution")
                .value("1920x1080")
                .type("STRING")
                .description("视频分辨率")
                .encrypted(false)
                .build(),
            DeviceConfig.builder()
                .key("fps")
                .value("25")
                .type("INTEGER")
                .description("帧率")
                .encrypted(false)
                .build(),
            DeviceConfig.builder()
                .key("username")
                .value("admin")
                .type("STRING")
                .description("登录用户名")
                .encrypted(true)
                .build(),
            DeviceConfig.builder()
                .key("password")
                .value("")
                .type("STRING")
                .description("登录密码")
                .encrypted(true)
                .build()
        );
    }

    @Override
    public DeviceConnection connect(DeviceEntity device, ConnectionConfig config) {
        try {
            CameraClient client = clientFactory.createClient(device, config);

            // 建立连接
            client.connect();

            // 验证连接
            if (!client.isAlive()) {
                throw new DeviceConnectionException("摄像头连接失败");
            }

            return CameraConnection.builder()
                .deviceId(device.getDeviceId())
                .client(client)
                .connectTime(LocalDateTime.now())
                .status(ConnectionStatus.CONNECTED)
                .build();

        } catch (Exception e) {
            throw new DeviceConnectionException("连接摄像头失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect(DeviceConnection connection) {
        if (connection instanceof CameraConnection) {
            CameraConnection cameraConnection = (CameraConnection) connection;
            try {
                cameraConnection.getClient().disconnect();
                cameraConnection.setStatus(ConnectionStatus.DISCONNECTED);
            } catch (Exception e) {
                log.error("断开摄像头连接失败", e);
            }
        }
    }

    @Override
    public DeviceControlResult controlDevice(DeviceEntity device, DeviceControlDTO controlDTO) {
        try {
            CameraConnection connection = getCameraConnection(device.getDeviceId());
            CameraClient client = connection.getClient();

            switch (controlDTO.getControlType()) {
                case "PTZ_CONTROL":
                    return controlPTZ(client, controlDTO.getControlData());
                case "PRESET_GOTO":
                    return gotoPreset(client, controlDTO.getControlData());
                case "RECORD_START":
                    return startRecord(client);
                case "RECORD_STOP":
                    return stopRecord(client);
                default:
                    return DeviceControlResult.fail("不支持的摄像头控制类型: " + controlDTO.getControlType());
            }

        } catch (Exception e) {
            return DeviceControlResult.fail("控制摄像头失败: " + e.getMessage());
        }
    }

    @Override
    public DeviceStatus getDeviceStatus(DeviceConnection connection) {
        if (connection instanceof CameraConnection) {
            CameraConnection cameraConnection = (CameraConnection) connection;
            CameraClient client = cameraConnection.getClient();

            try {
                CameraStatus status = client.getStatus();

                return DeviceStatus.builder()
                    .deviceId(cameraConnection.getDeviceId())
                    .isOnline(client.isAlive())
                    .status(status.isRecording() ? 2 : 1) // 1-空闲，2-录制中
                    .lastHeartbeatTime(LocalDateTime.now())
                    .build();

            } catch (Exception e) {
                return DeviceStatus.builder()
                    .deviceId(cameraConnection.getDeviceId())
                    .isOnline(false)
                    .status(0)
                    .lastHeartbeatTime(LocalDateTime.now())
                    .build();
            }
        }
        return null;
    }

    @Override
    public List<MonitorData> getMonitorData(DeviceConnection connection) {
        if (connection instanceof CameraConnection) {
            CameraConnection cameraConnection = (CameraConnection) connection;
            CameraClient client = cameraConnection.getClient();

            try {
                CameraInfo info = client.getCameraInfo();

                return Arrays.asList(
                    MonitorData.builder()
                        .monitorType("CPU_USAGE")
                        .monitorName("CPU使用率")
                        .value(info.getCpuUsage())
                        .unit("%")
                        .status(info.getCpuUsage() < 80 ? 1 : 0)
                        .alertLevel(info.getCpuUsage() >= 90 ? 3 : info.getCpuUsage() >= 80 ? 2 : 1)
                        .build(),
                    MonitorData.builder()
                        .monitorType("MEMORY_USAGE")
                        .monitorName("内存使用率")
                        .value(info.getMemoryUsage())
                        .unit("%")
                        .status(info.getMemoryUsage() < 85 ? 1 : 0)
                        .alertLevel(info.getMemoryUsage() >= 95 ? 3 : info.getMemoryUsage() >= 85 ? 2 : 1)
                        .build(),
                    MonitorData.builder()
                        .monitorType("DISK_USAGE")
                        .monitorName("磁盘使用率")
                        .value(info.getDiskUsage())
                        .unit("%")
                        .status(info.getDiskUsage() < 90 ? 1 : 0)
                        .alertLevel(info.getDiskUsage() >= 95 ? 3 : info.getDiskUsage() >= 90 ? 2 : 1)
                        .build()
                );

            } catch (Exception e) {
                log.error("获取摄像头监控数据失败", e);
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public boolean heartbeat(DeviceConnection connection) {
        if (connection instanceof CameraConnection) {
            CameraConnection cameraConnection = (CameraConnection) connection;
            CameraClient client = cameraConnection.getClient();

            try {
                return client.isAlive();
            } catch (Exception e) {
                log.error("摄像头心跳检测失败", e);
                return false;
            }
        }
        return false;
    }

    @Override
    public void handleDeviceEvent(DeviceEvent event) {
        if (event.getDeviceType() == DeviceType.CAMERA) {
            switch (event.getEventType()) {
                case "MOTION_DETECTED":
                    handleMotionDetected(event);
                    break;
                case "LOST_CONNECTION":
                    handleLostConnection(event);
                    break;
                case "RECORD_COMPLETE":
                    handleRecordComplete(event);
                    break;
            }
        }
    }

    private DeviceControlResult controlPTZ(CameraClient client, Map<String, Object> controlData) {
        try {
            PTZCommand ptzCommand = PTZCommand.builder()
                .pan((Double) controlData.get("pan"))
                .tilt((Double) controlData.get("tilt"))
                .zoom((Double) controlData.get("zoom"))
                .speed((Integer) controlData.getOrDefault("speed", 5))
                .build();

            client.controlPTZ(ptzCommand);

            return DeviceControlResult.success("PTZ控制成功");

        } catch (Exception e) {
            return DeviceControlResult.fail("PTZ控制失败: " + e.getMessage());
        }
    }

    private DeviceControlResult gotoPreset(CameraClient client, Map<String, Object> controlData) {
        try {
            Integer presetId = (Integer) controlData.get("presetId");
            client.gotoPreset(presetId);

            return DeviceControlResult.success("转到预置位成功");

        } catch (Exception e) {
            return DeviceControlResult.fail("转到预置位失败: " + e.getMessage());
        }
    }

    private DeviceControlResult startRecord(CameraClient client) {
        try {
            client.startRecord();
            return DeviceControlResult.success("开始录像成功");

        } catch (Exception e) {
            return DeviceControlResult.fail("开始录像失败: " + e.getMessage());
        }
    }

    private DeviceControlResult stopRecord(CameraClient client) {
        try {
            client.stopRecord();
            return DeviceControlResult.success("停止录像成功");

        } catch (Exception e) {
            return DeviceControlResult.fail("停止录像失败: " + e.getMessage());
        }
    }

    private void handleMotionDetected(DeviceEvent event) {
        // 处理移动检测事件
        log.info("摄像头移动检测事件: {}", event.getDeviceId());

        // 可以在这里触发录像、告警等操作
        eventPublisher.publishEvent(new MotionDetectedEvent(event.getDeviceId(), event.getEventData()));
    }

    private void handleLostConnection(DeviceEvent event) {
        // 处理连接丢失事件
        log.warn("摄像头连接丢失: {}", event.getDeviceId());

        // 更新设备状态为离线
        deviceStatusService.updateDeviceStatus(event.getDeviceId(), false);
    }

    private void handleRecordComplete(DeviceEvent event) {
        // 处理录像完成事件
        log.info("摄像头录像完成: {}", event.getDeviceId());

        // 可以在这里进行文件处理、通知等操作
        eventPublisher.publishEvent(new RecordCompleteEvent(event.getDeviceId(), event.getEventData()));
    }

    private CameraConnection getCameraConnection(Long deviceId) {
        // 从连接管理器获取摄像头连接
        return deviceConnectionManager.getConnection(deviceId, CameraConnection.class);
    }
}
```

---

## 🧪 测试策略

### 1. 单元测试

```java
@SpringBootTest
class DeviceServiceTest {

    @Resource
    private DeviceService deviceService;

    @Resource
    private DeviceDao deviceDao;

    @MockBean
    private DeviceProtocolFactory protocolFactory;

    @MockBean
    private DeviceProtocol deviceProtocol;

    @Test
    void testAddDevice() {
        // 准备测试数据
        DeviceCreateDTO createDTO = new DeviceCreateDTO();
        createDTO.setDeviceCode("TEST001");
        createDTO.setDeviceName("测试设备");
        createDTO.setDeviceType("CAMERA");
        createDTO.setIpAddress("192.168.1.100");
        createDTO.setPortNumber(554);

        // Mock协议工厂
        when(protocolFactory.getProtocol(DeviceType.CAMERA)).thenReturn(deviceProtocol);
        when(deviceProtocol.getDefaultConfigs()).thenReturn(Collections.emptyList());

        // 执行测试
        assertDoesNotThrow(() -> deviceService.add(createDTO));

        // 验证结果
        DeviceEntity device = deviceDao.selectOne(
            new QueryWrapper<DeviceEntity>().eq("device_code", "TEST001")
        );
        assertNotNull(device);
        assertEquals("测试设备", device.getDeviceName());
        assertEquals("CAMERA", device.getDeviceType());
        assertEquals(0, device.getStatus()); // 默认离线状态
    }

    @Test
    void testControlDevice() {
        // 创建测试设备
        DeviceEntity device = createTestDevice();
        device.setStatus(1); // 在线状态
        deviceDao.insert(device);

        // 准备控制数据
        DeviceControlDTO controlDTO = new DeviceControlDTO();
        controlDTO.setControlType("PTZ_CONTROL");
        controlDTO.setControlData(Map.of("pan", 10.0, "tilt", 5.0, "zoom", 1.0));

        // Mock协议
        when(protocolFactory.getProtocol(DeviceType.CAMERA)).thenReturn(deviceProtocol);
        when(deviceProtocol.controlDevice(any(), any())).thenReturn(
            DeviceControlResult.success("控制成功")
        );

        // 执行测试
        assertDoesNotThrow(() -> deviceService.controlDevice(device.getDeviceId(), controlDTO));

        // 验证协议被调用
        verify(deviceProtocol).controlDevice(any(), eq(controlDTO));
    }

    @Test
    void testControlOfflineDevice() {
        // 创建离线设备
        DeviceEntity device = createTestDevice();
        device.setStatus(0); // 离线状态
        deviceDao.insert(device);

        DeviceControlDTO controlDTO = new DeviceControlDTO();
        controlDTO.setControlType("PTZ_CONTROL");

        // 执行测试，应该抛出异常
        assertThrows(SmartException.class,
            () -> deviceService.controlDevice(device.getDeviceId(), controlDTO));
    }

    private DeviceEntity createTestDevice() {
        DeviceEntity device = new DeviceEntity();
        device.setDeviceCode("TEST001");
        device.setDeviceName("测试设备");
        device.setDeviceType("CAMERA");
        device.setIpAddress("192.168.1.100");
        device.setPortNumber(554);
        device.setCreateTime(LocalDateTime.now());
        return device;
    }
}
```

### 2. 集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DeviceIntegrationTest {

    @Resource
    private TestRestTemplate restTemplate;

    @Test
    void testDeviceManagementFlow() {
        String token = authenticate("admin", "123456");

        // 1. 添加设备
        DeviceCreateDTO createDTO = new DeviceCreateDTO();
        createDTO.setDeviceCode("INTEGRATION_TEST_001");
        createDTO.setDeviceName("集成测试设备");
        createDTO.setDeviceType("CAMERA");
        createDTO.setIpAddress("192.168.1.100");
        createDTO.setPortNumber(554);

        ResponseEntity<ResponseDTO<String>> addResponse = restTemplate.exchange(
            "/api/device",
            HttpMethod.POST,
            createEntityWithToken(token, createDTO),
            new ParameterizedTypeReference<ResponseDTO<String>>() {}
        );

        assertEquals(200, addResponse.getStatusCodeValue());

        // 2. 查询设备列表
        ResponseEntity<ResponseDTO<PageResult<DeviceVO>>> queryResponse = restTemplate.exchange(
            "/api/device/page?pageNum=1&pageSize=10",
            HttpMethod.GET,
            createEntityWithToken(token, null),
            new ParameterizedTypeReference<ResponseDTO<PageResult<DeviceVO>>>() {}
        );

        assertEquals(200, queryResponse.getStatusCodeValue());
        assertFalse(queryResponse.getBody().getData().getRecords().isEmpty());

        // 3. 获取设备详情
        DeviceVO device = queryResponse.getBody().getData().getRecords().get(0);
        ResponseEntity<ResponseDTO<DeviceDetailVO>> detailResponse = restTemplate.exchange(
            "/api/device/" + device.getDeviceId(),
            HttpMethod.GET,
            createEntityWithToken(token, null),
            new ParameterizedTypeReference<ResponseDTO<DeviceDetailVO>>() {}
        );

        assertEquals(200, detailResponse.getStatusCodeValue());
        assertEquals("INTEGRATION_TEST_001", detailResponse.getBody().getData().getDeviceCode());

        // 4. 更新设备
        DeviceUpdateDTO updateDTO = new DeviceUpdateDTO();
        updateDTO.setDeviceName("更新后的设备名称");

        ResponseEntity<ResponseDTO<String>> updateResponse = restTemplate.exchange(
            "/api/device/" + device.getDeviceId(),
            HttpMethod.PUT,
            createEntityWithToken(token, updateDTO),
            new ParameterizedTypeReference<ResponseDTO<String>>() {}
        );

        assertEquals(200, updateResponse.getStatusCodeValue());

        // 5. 删除设备
        ResponseEntity<ResponseDTO<String>> deleteResponse = restTemplate.exchange(
            "/api/device/" + device.getDeviceId(),
            HttpMethod.DELETE,
            createEntityWithToken(token, null),
            new ParameterizedTypeReference<ResponseDTO<String>>() {}
        );

        assertEquals(200, deleteResponse.getStatusCodeValue());
    }
}
```

---

## 📊 性能优化

### 1. 设备连接池管理

```java
@Component
public class DeviceConnectionPool {

    private final Map<Long, BlockingQueue<DeviceConnection>> connectionPools = new ConcurrentHashMap<>();
    private final Map<Long, AtomicInteger> connectionCounts = new ConcurrentHashMap<>();

    private static final int MAX_CONNECTIONS_PER_DEVICE = 5;
    private static final int INITIAL_CONNECTIONS_PER_DEVICE = 2;

    /**
     * 获取设备连接
     */
    public DeviceConnection getConnection(Long deviceId, Supplier<DeviceConnection> connectionSupplier) {
        BlockingQueue<DeviceConnection> pool = connectionPools.computeIfAbsent(deviceId, k -> {
            // 初始化连接池
            BlockingQueue<DeviceConnection> queue = new LinkedBlockingQueue<>();
            for (int i = 0; i < INITIAL_CONNECTIONS_PER_DEVICE; i++) {
                try {
                    queue.add(connectionSupplier.get());
                } catch (Exception e) {
                    log.error("创建设备连接失败", e);
                }
            }
            return queue;
        });

        try {
            // 尝试从池中获取连接
            DeviceConnection connection = pool.poll(1, TimeUnit.SECONDS);
            if (connection != null && isConnectionValid(connection)) {
                return connection;
            }

            // 如果池中没有可用连接，创建新连接
            if (connectionCounts.computeIfAbsent(deviceId, k -> new AtomicInteger(0)).get() < MAX_CONNECTIONS_PER_DEVICE) {
                DeviceConnection newConnection = connectionSupplier.get();
                connectionCounts.get(deviceId).incrementAndGet();
                return newConnection;
            }

            // 等待可用连接
            return pool.take();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DeviceConnectionException("获取设备连接被中断", e);
        }
    }

    /**
     * 归还设备连接
     */
    public void returnConnection(DeviceConnection connection) {
        if (connection != null && isConnectionValid(connection)) {
            BlockingQueue<DeviceConnection> pool = connectionPools.get(connection.getDeviceId());
            if (pool != null) {
                pool.offer(connection);
            }
        }
    }

    /**
     * 检查连接有效性
     */
    private boolean isConnectionValid(DeviceConnection connection) {
        try {
            return connection != null &&
                   connection.getStatus() == ConnectionStatus.CONNECTED &&
                   deviceManager.isDeviceAlive(connection.getDeviceId());
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 2. 设备状态缓存优化

```java
@Component
public class DeviceStatusCacheManager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存策略：L1本地缓存 + L2 Redis缓存
    private final Cache<String, DeviceStatus> localCache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build();

    /**
     * 缓存设备状态
     */
    public void cacheDeviceStatus(Long deviceId, DeviceStatus status) {
        String cacheKey = buildCacheKey(deviceId);

        // 更新本地缓存
        localCache.put(cacheKey, status);

        // 更新Redis缓存
        redisTemplate.opsForValue().set(
            cacheKey,
            status,
            Duration.ofMinutes(10)
        );

        // 发布状态变更事件
        publishStatusChangeEvent(deviceId, status);
    }

    /**
     * 获取设备状态
     */
    public DeviceStatus getDeviceStatus(Long deviceId) {
        String cacheKey = buildCacheKey(deviceId);

        // 1. 先从本地缓存获取
        DeviceStatus status = localCache.getIfPresent(cacheKey);
        if (status != null) {
            return status;
        }

        // 2. 从Redis缓存获取
        try {
            status = (DeviceStatus) redisTemplate.opsForValue().get(cacheKey);
            if (status != null) {
                localCache.put(cacheKey, status);
                return status;
            }
        } catch (Exception e) {
            log.error("从Redis获取设备状态失败", e);
        }

        // 3. 缓存未命中，返回null
        return null;
    }

    /**
     * 批量获取设备状态
     */
    public Map<Long, DeviceStatus> batchGetDeviceStatus(List<Long> deviceIds) {
        Map<Long, DeviceStatus> result = new HashMap<>();
        List<Long> missedIds = new ArrayList<>();

        // 1. 从本地缓存批量获取
        deviceIds.forEach(deviceId -> {
            String cacheKey = buildCacheKey(deviceId);
            DeviceStatus status = localCache.getIfPresent(cacheKey);
            if (status != null) {
                result.put(deviceId, status);
            } else {
                missedIds.add(deviceId);
            }
        });

        // 2. 从Redis缓存批量获取缺失的状态
        if (!missedIds.isEmpty()) {
            List<String> cacheKeys = missedIds.stream()
                .map(this::buildCacheKey)
                .collect(Collectors.toList());

            try {
                List<Object> values = redisTemplate.opsForValue().multiGet(cacheKeys);
                for (int i = 0; i < missedIds.size(); i++) {
                    Long deviceId = missedIds.get(i);
                    Object value = values.get(i);
                    if (value instanceof DeviceStatus) {
                        DeviceStatus status = (DeviceStatus) value;
                        result.put(deviceId, status);
                        localCache.put(buildCacheKey(deviceId), status);
                    }
                }
            } catch (Exception e) {
                log.error("批量获取设备状态失败", e);
            }
        }

        return result;
    }

    private String buildCacheKey(Long deviceId) {
        return "device:status:" + deviceId;
    }

    private void publishStatusChangeEvent(Long deviceId, DeviceStatus status) {
        DeviceStatusChangeEvent event = new DeviceStatusChangeEvent();
        event.setDeviceId(deviceId);
        event.setDeviceStatus(status);
        event.setTimestamp(LocalDateTime.now());

        eventPublisher.publishEvent(event);
    }
}
```

---

## 📋 检查清单

### 开发前检查

- [ ] 是否已明确支持的设备类型？
- [ ] 是否已确认设备协议要求？
- [ ] 是否已了解设备监控指标？
- [ ] 是否已确认安全认证要求？

### 开发中检查

- [ ] 是否实现了设备协议工厂？
- [ ] 是否添加了设备连接管理？
- [ ] 是否实现了设备状态监控？
- [ ] 是否添加了设备缓存策略？
- [ ] 是否实现了WebSocket实时推送？

### 部署前检查

- [ ] 设备连接池配置是否正确？
- [ ] 设备协议注册是否完整？
- [ ] 设备状态缓存是否生效？
- [ ] WebSocket连接是否正常？
- [ ] 设备控制功能是否安全？

---

**📞 技术支持**：架构师团队

**📚 相关文档**：
- [权限管理公共模块](./smart-permission.md)
- [地理位置公共模块](./smart-location.md)
- [实时数据公共模块](./smart-realtime.md)
- [综合开发规范文档](../DEV_STANDARDS.md)

---

*最后更新：2025-01-10*
*维护者：SmartAdmin开发团队*