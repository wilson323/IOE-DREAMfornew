# 门禁系统公共模块开发文档 (smart-access)

## 1. 模块概述

### 1.1 模块简介

门禁系统是SmartAdmin智慧园区管理系统的核心安全模块，负责管理园区出入口控制、人员通行权限、设备远程控制等功能。系统支持多种门禁设备类型，提供实时监控、授权管理、审批流程等完整功能。

### 1.2 设计目标

- **统一设备管理**: 支持多种门禁设备的统一接入和管理
- **权限精细化控制**: 基于人员、区域、时间的多维度权限控制
- **实时监控**: 门禁状态实时监控和异常报警
- **审批流程集成**: 与warm-flow审批引擎深度集成
- **数据分析**: 通行数据统计分析和可视化展示

### 1.3 核心功能

- 门禁设备管理
- 人员权限授权
- 实时通行监控
- 远程门禁控制
- 通行记录查询
- 权限审批流程
- 异常报警处理
- 数据统计分析

## 2. 数据库设计

### 2.1 核心表结构

#### 2.1.1 门禁设备表 (smart_access_device)

```sql
CREATE TABLE smart_access_device (
    device_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',
    device_code VARCHAR(100) NOT NULL COMMENT '设备编码',
    device_name VARCHAR(200) NOT NULL COMMENT '设备名称',
    device_type VARCHAR(50) NOT NULL COMMENT '设备类型：door-门禁，gate-闸机，barrier-道闸，turnstile-转闸',
    device_brand VARCHAR(100) COMMENT '设备品牌',
    device_model VARCHAR(100) COMMENT '设备型号',
    area_id BIGINT COMMENT '所属区域ID',
    area_name VARCHAR(100) COMMENT '所属区域名称',
    location_desc VARCHAR(500) COMMENT '位置描述',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    port INT COMMENT '端口号',
    mac_address VARCHAR(50) COMMENT 'MAC地址',
    protocol_type VARCHAR(50) COMMENT '通信协议：tcp, udp, http, websocket',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-离线，1-在线，2-故障',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    config_json JSON COMMENT '设备配置信息',
    last_online_time DATETIME COMMENT '最后在线时间',
    created_by BIGINT NOT NULL COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_device_code (device_code),
    INDEX idx_device_type (device_type),
    INDEX idx_area_id (area_id),
    INDEX idx_status (status),
    INDEX idx_ip_address (ip_address),
    UNIQUE KEY uk_device_code (device_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁设备表';
```

#### 2.1.2 门禁权限表 (smart_access_permission)

```sql
CREATE TABLE smart_access_permission (
    permission_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    person_id BIGINT NOT NULL COMMENT '人员ID',
    person_name VARCHAR(100) NOT NULL COMMENT '人员姓名',
    person_type VARCHAR(50) NOT NULL COMMENT '人员类型：employee-员工，visitor-访客，contractor-承包商',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    device_name VARCHAR(200) NOT NULL COMMENT '设备名称',
    area_id BIGINT COMMENT '区域ID',
    area_name VARCHAR(100) COMMENT '区域名称',
    permission_type VARCHAR(50) NOT NULL COMMENT '权限类型：permanent-永久，temporary-临时，scheduled-定时',
    start_time DATETIME NOT NULL COMMENT '生效开始时间',
    end_time DATETIME NOT NULL COMMENT '生效结束时间',
    monday_access TINYINT DEFAULT 1 COMMENT '周一通行权限：0-禁止，1-允许',
    tuesday_access TINYINT DEFAULT 1 COMMENT '周二通行权限',
    wednesday_access TINYINT DEFAULT 1 COMMENT '周三通行权限',
    thursday_access TINYINT DEFAULT 1 COMMENT '周四通行权限',
    friday_access TINYINT DEFAULT 1 COMMENT '周五通行权限',
    saturday_access TINYINT DEFAULT 1 COMMENT '周六通行权限',
    sunday_access TINYINT DEFAULT 1 COMMENT '周日通行权限',
    time_config JSON COMMENT '时间段配置',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用，2-待审批，3-已过期',
    business_code VARCHAR(100) COMMENT '业务编码',
    approve_user_id BIGINT COMMENT '审批人ID',
    approve_time DATETIME COMMENT '审批时间',
    approve_remark TEXT COMMENT '审批备注',
    created_by BIGINT NOT NULL COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_person_id (person_id),
    INDEX idx_device_id (device_id),
    INDEX idx_area_id (area_id),
    INDEX idx_status (status),
    INDEX idx_business_code (business_code),
    INDEX idx_start_end_time (start_time, end_time),
    INDEX idx_person_device (person_id, device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁权限表';
```

#### 2.1.3 通行记录表 (smart_access_record)

```sql
CREATE TABLE smart_access_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    device_name VARCHAR(200) NOT NULL COMMENT '设备名称',
    device_code VARCHAR(100) NOT NULL COMMENT '设备编码',
    person_id BIGINT NOT NULL COMMENT '人员ID',
    person_name VARCHAR(100) NOT NULL COMMENT '人员姓名',
    person_type VARCHAR(50) NOT NULL COMMENT '人员类型',
    card_number VARCHAR(100) COMMENT '卡号',
    face_image_url VARCHAR(500) COMMENT '人脸图片URL',
    fingerprint_template VARCHAR(500) COMMENT '指纹模板',
    access_result TINYINT NOT NULL COMMENT '通行结果：0-失败，1-成功',
    fail_reason VARCHAR(200) COMMENT '失败原因',
    direction VARCHAR(20) COMMENT '通行方向：in-进入，out-外出',
    access_time DATETIME NOT NULL COMMENT '通行时间',
    temperature DECIMAL(4,1) COMMENT '体温（摄氏度）',
    mask_detected TINYINT COMMENT '是否检测到口罩：0-未检测，1-检测到',
    image_urls JSON COMMENT '通行图片URLs',
    event_type VARCHAR(50) COMMENT '事件类型：normal-正常，abnormal-异常，forced-强制',
    is_abnormal TINYINT DEFAULT 0 COMMENT '是否异常：0-正常，1-异常',
    processed TINYINT DEFAULT 0 COMMENT '是否已处理：0-未处理，1-已处理',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_device_id (device_id),
    INDEX idx_person_id (person_id),
    INDEX idx_access_time (access_time),
    INDEX idx_access_result (access_result),
    INDEX idx_person_device_time (person_id, device_id, access_time),
    INDEX idx_is_abnormal (is_abnormal),
    INDEX idx_event_type (event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通行记录表';
```

#### 2.1.4 设备指令表 (smart_access_command)

```sql
CREATE TABLE smart_access_command (
    command_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '指令ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    command_type VARCHAR(50) NOT NULL COMMENT '指令类型：open-开门，close-关门，restart-重启，sync-同步',
    command_content JSON COMMENT '指令内容',
    command_status TINYINT NOT NULL DEFAULT 0 COMMENT '指令状态：0-待发送，1-已发送，2-执行成功，3-执行失败',
    send_time DATETIME COMMENT '发送时间',
    execute_time DATETIME COMMENT '执行时间',
    response_content JSON COMMENT '设备响应内容',
    error_message TEXT COMMENT '错误信息',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    max_retry INT DEFAULT 3 COMMENT '最大重试次数',
    created_by BIGINT NOT NULL COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_device_id (device_id),
    INDEX idx_command_type (command_type),
    INDEX idx_command_status (command_status),
    INDEX idx_send_time (send_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备指令表';
```

#### 2.1.5 设备状态表 (smart_access_device_status)

```sql
CREATE TABLE smart_access_device_status (
    status_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '状态ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    door_status VARCHAR(20) COMMENT '门状态：open-打开，closed-关闭，unknown-未知',
    lock_status VARCHAR(20) COMMENT '锁状态：locked-锁定，unlocked-解锁，unknown-未知',
    power_status VARCHAR(20) COMMENT '电源状态：normal-正常，low-低电量，offline-离线',
    network_status VARCHAR(20) COMMENT '网络状态：online-在线，offline-离线',
    last_heartbeat DATETIME COMMENT '最后心跳时间',
    cpu_usage DECIMAL(5,2) COMMENT 'CPU使用率',
    memory_usage DECIMAL(5,2) COMMENT '内存使用率',
    storage_usage DECIMAL(5,2) COMMENT '存储使用率',
    temperature DECIMAL(4,1) COMMENT '设备温度',
    error_count INT DEFAULT 0 COMMENT '错误次数',
    last_error_time DATETIME COMMENT '最后错误时间',
    last_error_message TEXT COMMENT '最后错误信息',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_device_id (device_id),
    INDEX idx_last_heartbeat (last_heartbeat),
    UNIQUE KEY uk_device_id (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备状态表';
```

### 2.2 业务扩展表

#### 2.2.1 区域权限组表 (smart_access_area_group)

```sql
CREATE TABLE smart_access_area_group (
    group_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限组ID',
    group_name VARCHAR(200) NOT NULL COMMENT '权限组名称',
    group_code VARCHAR(100) NOT NULL COMMENT '权限组编码',
    description TEXT COMMENT '描述',
    area_ids TEXT COMMENT '区域ID列表',
    device_ids TEXT COMMENT '设备ID列表',
    default_start_time TIME COMMENT '默认开始时间',
    default_end_time TIME COMMENT '默认结束时间',
    default_weekdays VARCHAR(20) DEFAULT '1,2,3,4,5' COMMENT '默认通行日期',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_by BIGINT NOT NULL COMMENT '创建人',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT COMMENT '更新人',
    updated_time DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_group_code (group_code),
    INDEX idx_status (status),
    UNIQUE KEY uk_group_code (group_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区域权限组表';
```

#### 2.2.2 设备告警表 (smart_access_alarm)

```sql
CREATE TABLE smart_access_alarm (
    alarm_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '告警ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    alarm_type VARCHAR(50) NOT NULL COMMENT '告警类型：offline-离线，forced-强制开门，door_open-门超时未关，low_power-低电量，error-设备故障',
    alarm_level VARCHAR(20) NOT NULL COMMENT '告警级别：low-低，medium-中，high-高，critical-严重',
    alarm_title VARCHAR(200) NOT NULL COMMENT '告警标题',
    alarm_content TEXT COMMENT '告警内容',
    alarm_time DATETIME NOT NULL COMMENT '告警时间',
    record_id BIGINT COMMENT '关联通行记录ID',
    person_id BIGINT COMMENT '关联人员ID',
    person_name VARCHAR(100) COMMENT '关联人员姓名',
    is_handled TINYINT DEFAULT 0 COMMENT '是否已处理：0-未处理，1-已处理',
    handled_by BIGINT COMMENT '处理人ID',
    handled_time DATETIME COMMENT '处理时间',
    handle_remark TEXT COMMENT '处理备注',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_device_id (device_id),
    INDEX idx_alarm_type (alarm_type),
    INDEX idx_alarm_level (alarm_level),
    INDEX idx_alarm_time (alarm_time),
    INDEX idx_is_handled (is_handled),
    INDEX idx_person_id (person_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备告警表';
```

## 3. 后端实现
## 📋 IOE-DREAM七微服务架构

**核心架构组成**:
- **Gateway Service (8080)**: API网关
- **Common Service (8088)**: 公共模块微服务
- **DeviceComm Service (8087)**: 设备通讯微服务
- **OA Service (8089)**: OA微服务
- **Access Service (8090)**: 门禁服务
- **Attendance Service (8091)**: 考勤服务
- **Video Service (8092)**: 视频服务
- **Consume Service (8094)**: 消费服务
- **Visitor Service (8095)**: 访客服务

**架构特点**:
- 基于Spring Boot 3.5.8 + Java 17
- 严格遵循企业级微服务规范
- 支持高并发、高可用、水平扩展

**技术栈标准**:
- **数据库**: MySQL 8.0 + Druid连接池
- **缓存**: Redis + Caffeine多级缓存
- **注册中心**: Nacos
- **配置中心**: Nacos Config
- **认证授权**: Sa-Token

## 🏗️ 四层架构规范

**标准架构模式**:
```
Controller (接口控制层)
    ↓
Service (核心业务层)
    ↓
Manager (流程管理层)
    ↓
DAO (数据访问层)
```

**层级职责**:
- **Controller层**: HTTP请求处理、参数验证、权限控制
- **Service层**: 核心业务逻辑、事务管理、业务规则验证
- **Manager层**: 复杂流程编排、多数据组装、第三方服务集成
- **DAO层**: 数据库CRUD操作、SQL查询实现、数据访问边界

**严格禁止跨层访问**: Controller不能直接调用Manager/DAO！
### 3.1 实体类设计
## ⚠️ IOE-DREAM零容忍规则（强制执行）

**必须遵守的架构规则**:
- ✅ **必须使用 @Resource 注入依赖**
- ✅ **必须使用 @Mapper 注解** (禁止@Repository)
- ✅ **必须使用 Dao 后缀** (禁止Repository)
- ✅ **必须使用 @RestController 注解**
- ✅ **必须使用 @Valid 参数校验**
- ✅ **必须返回统一ResponseDTO格式**
- ✅ **必须遵循四层架构边界**

**严格禁止事项**:
- ❌ **禁止使用 @Autowired 注入**
- ❌ **禁止使用 @Repository 注解**
- ❌ **禁止使用 Repository 后缀命名**
- ❌ **禁止跨层访问**
- ❌ **禁止在Controller中包含业务逻辑**
- ❌ **禁止直接访问数据库**

**违规后果**: P0级问题，立即修复，禁止合并！

#### 3.1.1 门禁设备实体

```java
package net.lab1024.sa.base.module.entity.access;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.module.entity.base.BaseEntity;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("smart_access_device")
public class AccessDeviceEntity extends BaseEntity {

    @TableId(value = "device_id", type = IdType.AUTO)
    private Long deviceId;

    @TableField("device_code")
    private String deviceCode;

    @TableField("device_name")
    private String deviceName;

    @TableField("device_type")
    private String deviceType;

    @TableField("device_brand")
    private String deviceBrand;

    @TableField("device_model")
    private String deviceModel;

    @TableField("area_id")
    private Long areaId;

    @TableField("area_name")
    private String areaName;

    @TableField("location_desc")
    private String locationDesc;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("port")
    private Integer port;

    @TableField("mac_address")
    private String macAddress;

    @TableField("protocol_type")
    private String protocolType;

    @TableField("status")
    private Integer status;

    @TableField("is_enabled")
    private Integer isEnabled;

    @TableField("config_json")
    private String configJson;

    @TableField("last_online_time")
    private LocalDateTime lastOnlineTime;

    @TableField("created_by")
    private Long createdBy;

    @TableField("updated_by")
    private Long updatedBy;

    public enum DeviceType {
        DOOR("door", "门禁"),
        GATE("gate", "闸机"),
        BARRIER("barrier", "道闸"),
        TURNSTILE("turnstile", "转闸");

        private final String value;
        private final String desc;

        DeviceType(String value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public String getValue() {
            return value;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum Status {
        OFFLINE(0, "离线"),
        ONLINE(1, "在线"),
        FAULT(2, "故障");

        private final Integer value;
        private final String desc;

        Status(Integer value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public Integer getValue() {
            return value;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum ProtocolType {
        TCP("tcp", "TCP协议"),
        UDP("udp", "UDP协议"),
        HTTP("http", "HTTP协议"),
        WEBSOCKET("websocket", "WebSocket协议");

        private final String value;
        private final String desc;

        ProtocolType(String value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public String getValue() {
            return value;
        }

        public String getDesc() {
            return desc;
        }
    }
}
```

#### 3.1.2 门禁权限实体

```java
package net.lab1024.sa.base.module.entity.access;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.module.entity.base.BaseEntity;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("smart_access_permission")
public class AccessPermissionEntity extends BaseEntity {

    @TableId(value = "permission_id", type = IdType.AUTO)
    private Long permissionId;

    @TableField("person_id")
    private Long personId;

    @TableField("person_name")
    private String personName;

    @TableField("person_type")
    private String personType;

    @TableField("device_id")
    private Long deviceId;

    @TableField("device_name")
    private String deviceName;

    @TableField("area_id")
    private Long areaId;

    @TableField("area_name")
    private String areaName;

    @TableField("permission_type")
    private String permissionType;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("monday_access")
    private Integer mondayAccess;

    @TableField("tuesday_access")
    private Integer tuesdayAccess;

    @TableField("wednesday_access")
    private Integer wednesdayAccess;

    @TableField("thursday_access")
    private Integer thursdayAccess;

    @TableField("friday_access")
    private Integer fridayAccess;

    @TableField("saturday_access")
    private Integer saturdayAccess;

    @TableField("sunday_access")
    private Integer sundayAccess;

    @TableField("time_config")
    private String timeConfig;

    @TableField("status")
    private Integer status;

    @TableField("business_code")
    private String businessCode;

    @TableField("approve_user_id")
    private Long approveUserId;

    @TableField("approve_time")
    private LocalDateTime approveTime;

    @TableField("approve_remark")
    private String approveRemark;

    @TableField("created_by")
    private Long createdBy;

    @TableField("updated_by")
    private Long updatedBy;

    public enum PermissionType {
        PERMANENT("permanent", "永久权限"),
        TEMPORARY("temporary", "临时权限"),
        SCHEDULED("scheduled", "定时权限");

        private final String value;
        private final String desc;

        PermissionType(String value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public String getValue() {
            return value;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum Status {
        DISABLED(0, "禁用"),
        ENABLED(1, "启用"),
        PENDING_APPROVAL(2, "待审批"),
        EXPIRED(3, "已过期");

        private final Integer value;
        private final String desc;

        Status(Integer value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public Integer getValue() {
            return value;
        }

        public String getDesc() {
            return desc;
        }
    }

    public enum PersonType {
        EMPLOYEE("employee", "员工"),
        VISITOR("visitor", "访客"),
        CONTRACTOR("contractor", "承包商");

        private final String value;
        private final String desc;

        PersonType(String value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public String getValue() {
            return value;
        }

        public String getDesc() {
            return desc;
        }
    }
}
```

### 3.2 服务层实现

#### 3.2.1 门禁设备服务

```java
package net.lab1024.sa.base.module.service.access;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartStringUtil;
import net.lab1024.sa.base.module.entity.access.AccessDeviceEntity;
import net.lab1024.sa.base.module.entity.access.AccessDeviceStatusEntity;
import net.lab1024.sa.base.module.mapper.access.AccessDeviceMapper;
import net.lab1024.sa.base.module.service.access.dto.AccessDeviceDTO;
import net.lab1024.sa.base.module.service.access.protocol.DeviceProtocolFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessDeviceService extends ServiceImpl<AccessDeviceMapper, AccessDeviceEntity> {

    private final AccessDeviceStatusService deviceStatusService;
    private final AccessCommandService commandService;
    private final AccessRecordService recordService;
    private final AreaService areaService;
    private final DeviceProtocolFactory protocolFactory;

    // 设备连接池
    private final Map<Long, DeviceConnection> deviceConnections = new ConcurrentHashMap<>();

    /**
     * 添加门禁设备
     */
    @Transactional(rollbackFor = Exception.class)
    public Long addDevice(AccessDeviceDTO deviceDTO, Long userId) {
        // 1. 验证设备编码唯一性
        validateDeviceCodeUnique(deviceDTO.getDeviceCode(), null);

        // 2. 验证区域存在性
        if (deviceDTO.getAreaId() != null) {
            areaService.validateAreaExists(deviceDTO.getAreaId());
        }

        // 3. 创建设备记录
        AccessDeviceEntity device = new AccessDeviceEntity();
        SmartBeanUtil.copyProperties(deviceDTO, device);
        device.setStatus(AccessDeviceEntity.Status.OFFLINE.getValue());
        device.setIsEnabled(1);
        device.setCreatedBy(userId);
        this.save(device);

        // 4. 初始化设备状态记录
        initDeviceStatus(device);

        // 5. 尝试连接设备
        connectDevice(device.getDeviceId());

        log.info("添加门禁设备成功：{} - {}", device.getDeviceId(), device.getDeviceName());
        return device.getDeviceId();
    }

    /**
     * 更新门禁设备
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDevice(AccessDeviceDTO deviceDTO, Long userId) {
        AccessDeviceEntity device = this.getById(deviceDTO.getDeviceId());
        if (device == null) {
            throw new BusinessException("设备不存在");
        }

        // 验证设备编码唯一性
        validateDeviceCodeUnique(deviceDTO.getDeviceCode(), deviceDTO.getDeviceId());

        // 更新设备信息
        SmartBeanUtil.copyProperties(deviceDTO, device);
        device.setUpdatedBy(userId);
        this.updateById(device);

        // 如果IP地址或协议发生变化，重新连接设备
        if (!deviceDTO.getIpAddress().equals(device.getIpAddress()) ||
            !deviceDTO.getProtocolType().equals(device.getProtocolType())) {
            reconnectDevice(device.getDeviceId());
        }

        log.info("更新门禁设备成功：{} - {}", device.getDeviceId(), device.getDeviceName());
    }

    /**
     * 删除门禁设备
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDevice(Long deviceId) {
        AccessDeviceEntity device = this.getById(deviceId);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }

        // 检查设备是否有关联权限
        long permissionCount = accessPermissionService.lambdaQuery()
                .eq(AccessPermissionEntity::getDeviceId, deviceId)
                .count();

        if (permissionCount > 0) {
            throw new BusinessException("设备存在关联权限，无法删除");
        }

        // 断开设备连接
        disconnectDevice(deviceId);

        // 删除设备状态记录
        deviceStatusService.removeByDeviceId(deviceId);

        // 删除设备
        this.removeById(deviceId);

        log.info("删除门禁设备成功：{}", deviceId);
    }

    /**
     * 远程开门
     */
    @Transactional(rollbackFor = Exception.class)
    public void remoteOpenDoor(Long deviceId, Long userId) {
        AccessDeviceEntity device = this.getById(deviceId);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }

        if (device.getIsEnabled() != 1) {
            throw new BusinessException("设备已禁用");
        }

        if (device.getStatus() != AccessDeviceEntity.Status.ONLINE.getValue()) {
            throw new BusinessException("设备离线，无法执行操作");
        }

        try {
            // 发送开门指令
            DeviceConnection connection = deviceConnections.get(deviceId);
            if (connection == null || !connection.isConnected()) {
                throw new BusinessException("设备连接断开");
            }

            boolean success = connection.openDoor();
            if (!success) {
                throw new BusinessException("开门指令执行失败");
            }

            // 记录操作日志
            recordAccessCommand(deviceId, "open", "远程开门", userId);

            log.info("远程开门成功：设备ID={}, 操作人={}", deviceId, userId);

        } catch (Exception e) {
            log.error("远程开门失败：设备ID={}", deviceId, e);
            throw new BusinessException("远程开门失败：" + e.getMessage());
        }
    }

    /**
     * 远程关门
     */
    @Transactional(rollbackFor = Exception.class)
    public void remoteCloseDoor(Long deviceId, Long userId) {
        AccessDeviceEntity device = this.getById(deviceId);
        if (device == null) {
            throw new BusinessException("设备不存在");
        }

        try {
            DeviceConnection connection = deviceConnections.get(deviceId);
            if (connection == null || !connection.isConnected()) {
                throw new BusinessException("设备连接断开");
            }

            boolean success = connection.closeDoor();
            if (!success) {
                throw new BusinessException("关门指令执行失败");
            }

            // 记录操作日志
            recordAccessCommand(deviceId, "close", "远程关门", userId);

            log.info("远程关门成功：设备ID={}, 操作人={}", deviceId, userId);

        } catch (Exception e) {
            log.error("远程关门失败：设备ID={}", deviceId, e);
            throw new BusinessException("远程关门失败：" + e.getMessage());
        }
    }

    /**
     * 重启设备
     */
    @Transactional(rollbackFor = Exception.class)
    public void restartDevice(Long deviceId, Long userId) {
        try {
            DeviceConnection connection = deviceConnections.get(deviceId);
            if (connection == null || !connection.isConnected()) {
                throw new BusinessException("设备连接断开");
            }

            boolean success = connection.restart();
            if (!success) {
                throw new BusinessException("重启指令执行失败");
            }

            // 记录操作日志
            recordAccessCommand(deviceId, "restart", "重启设备", userId);

            log.info("重启设备成功：设备ID={}, 操作人={}", deviceId, userId);

        } catch (Exception e) {
            log.error("重启设备失败：设备ID={}", deviceId, e);
            throw new BusinessException("重启设备失败：" + e.getMessage());
        }
    }

    /**
     * 同步设备时间
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncDeviceTime(Long deviceId, Long userId) {
        try {
            DeviceConnection connection = deviceConnections.get(deviceId);
            if (connection == null || !connection.isConnected()) {
                throw new BusinessException("设备连接断开");
            }

            boolean success = connection.syncTime(LocalDateTime.now());
            if (!success) {
                throw new BusinessException("时间同步指令执行失败");
            }

            // 记录操作日志
            recordAccessCommand(deviceId, "sync", "同步时间", userId);

            log.info("同步设备时间成功：设备ID={}, 操作人={}", deviceId, userId);

        } catch (Exception e) {
            log.error("同步设备时间失败：设备ID={}", deviceId, e);
            throw new BusinessException("同步时间失败：" + e.getMessage());
        }
    }

    /**
     * 授权人员访问设备
     */
    @Transactional(rollbackFor = Exception.class)
    public void grantAccess(Long deviceId, Long personId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            DeviceConnection connection = deviceConnections.get(deviceId);
            if (connection == null || !connection.isConnected()) {
                throw new BusinessException("设备连接断开");
            }

            boolean success = connection.grantAccess(personId, startTime, endTime);
            if (!success) {
                throw new BusinessException("授权指令执行失败");
            }

            log.info("设备授权成功：设备ID={}, 人员ID={}, 有效期{}至{}",
                    deviceId, personId, startTime, endTime);

        } catch (Exception e) {
            log.error("设备授权失败：设备ID={}, 人员ID={}", deviceId, personId, e);
            throw new BusinessException("设备授权失败：" + e.getMessage());
        }
    }

    /**
     * 撤销人员访问权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void revokeAccess(Long deviceId, Long personId) {
        try {
            DeviceConnection connection = deviceConnections.get(deviceId);
            if (connection == null || !connection.isConnected()) {
                throw new BusinessException("设备连接断开");
            }

            boolean success = connection.revokeAccess(personId);
            if (!success) {
                throw new BusinessException("撤销权限指令执行失败");
            }

            log.info("撤销设备权限成功：设备ID={}, 人员ID={}", deviceId, personId);

        } catch (Exception e) {
            log.error("撤销设备权限失败：设备ID={}, 人员ID={}", deviceId, personId, e);
            throw new BusinessException("撤销权限失败：" + e.getMessage());
        }
    }

    /**
     * 连接设备
     */
    private void connectDevice(Long deviceId) {
        try {
            AccessDeviceEntity device = this.getById(deviceId);
            if (device == null) {
                return;
            }

            DeviceConnection connection = protocolFactory.createConnection(device);
            if (connection.connect()) {
                deviceConnections.put(deviceId, connection);

                // 更新设备状态为在线
                device.setStatus(AccessDeviceEntity.Status.ONLINE.getValue());
                device.setLastOnlineTime(LocalDateTime.now());
                this.updateById(device);

                log.info("设备连接成功：{}", device.getDeviceCode());
            } else {
                log.warn("设备连接失败：{}", device.getDeviceCode());
            }

        } catch (Exception e) {
            log.error("连接设备失败：{}", deviceId, e);
        }
    }

    /**
     * 断开设备连接
     */
    private void disconnectDevice(Long deviceId) {
        DeviceConnection connection = deviceConnections.remove(deviceId);
        if (connection != null) {
            connection.disconnect();
            log.info("设备断开连接：{}", deviceId);
        }
    }

    /**
     * 重新连接设备
     */
    private void reconnectDevice(Long deviceId) {
        disconnectDevice(deviceId);
        connectDevice(deviceId);
    }

    /**
     * 初始化设备状态
     */
    private void initDeviceStatus(AccessDeviceEntity device) {
        AccessDeviceStatusEntity status = new AccessDeviceStatusEntity();
        status.setDeviceId(device.getDeviceId());
        status.setDoorStatus("unknown");
        status.setLockStatus("unknown");
        status.setPowerStatus("offline");
        status.setNetworkStatus("offline");
        deviceStatusService.save(status);
    }

    /**
     * 记录设备操作指令
     */
    private void recordAccessCommand(Long deviceId, String commandType, String commandName, Long userId) {
        commandService.createCommand(deviceId, commandType, commandName, userId);
    }

    /**
     * 验证设备编码唯一性
     */
    private void validateDeviceCodeUnique(String deviceCode, Long excludeId) {
        long count = this.lambdaQuery()
                .eq(AccessDeviceEntity::getDeviceCode, deviceCode)
                .ne(excludeId != null, AccessDeviceEntity::getDeviceId, excludeId)
                .count();

        if (count > 0) {
            throw new BusinessException("设备编码已存在");
        }
    }

    /**
     * 获取设备列表
     */
    public List<AccessDeviceDTO> getDeviceList(AccessDeviceQueryDTO queryDTO) {
        List<AccessDeviceEntity> devices = this.lambdaQuery()
                .like(SmartStringUtil.isNotEmpty(queryDTO.getDeviceName()),
                      AccessDeviceEntity::getDeviceName, queryDTO.getDeviceName())
                .like(SmartStringUtil.isNotEmpty(queryDTO.getDeviceCode()),
                      AccessDeviceEntity::getDeviceCode, queryDTO.getDeviceCode())
                .eq(queryDTO.getDeviceType() != null,
                    AccessDeviceEntity::getDeviceType, queryDTO.getDeviceType())
                .eq(queryDTO.getAreaId() != null,
                    AccessDeviceEntity::getAreaId, queryDTO.getAreaId())
                .eq(queryDTO.getStatus() != null,
                    AccessDeviceEntity::getStatus, queryDTO.getStatus())
                .orderBy(AccessDeviceEntity::getCreatedTime, false)
                .list();

        return devices.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换为DTO
     */
    private AccessDeviceDTO convertToDTO(AccessDeviceEntity entity) {
        AccessDeviceDTO dto = new AccessDeviceDTO();
        SmartBeanUtil.copyProperties(entity, dto);

        // 获取设备状态
        AccessDeviceStatusEntity status = deviceStatusService.getByDeviceId(entity.getDeviceId());
        if (status != null) {
            dto.setDoorStatus(status.getDoorStatus());
            dto.setLockStatus(status.getLockStatus());
            dto.setNetworkStatus(status.getNetworkStatus());
            dto.setLastHeartbeat(status.getLastHeartbeat());
        }

        // 获取连接状态
        DeviceConnection connection = deviceConnections.get(entity.getDeviceId());
        dto.setConnected(connection != null && connection.isConnected());

        return dto;
    }
}
```

#### 3.2.2 门禁权限服务

```java
package net.lab1024.sa.base.module.service.access;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartStringUtil;
import net.lab1024.sa.base.module.entity.access.AccessPermissionEntity;
import net.lab1024.sa.base.module.mapper.access.AccessPermissionMapper;
import net.lab1024.sa.base.module.service.access.dto.AccessPermissionDTO;
import net.lab1024.sa.base.module.service.workflow.ApprovalBusinessService;
import net.lab1024.sa.base.module.service.workflow.dto.ApprovalStartDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 门禁权限服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccessPermissionService extends ServiceImpl<AccessPermissionMapper, AccessPermissionEntity> {

    private final AccessDeviceService deviceService;
    private final SmartPersonService personService;
    private final ApprovalBusinessService approvalService;
    private final AccessRecordService recordService;

    /**
     * 申请门禁权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void applyPermission(AccessPermissionDTO permissionDTO, Long userId) {
        try {
            // 1. 验证设备和人员存在性
            validateDeviceAndPerson(permissionDTO.getDeviceId(), permissionDTO.getPersonId());

            // 2. 检查是否已有权限
            validateDuplicatePermission(permissionDTO);

            // 3. 生成业务编码
            String businessCode = generateBusinessCode("ACCESS_AUTH");

            // 4. 创建权限记录（待审批状态）
            AccessPermissionEntity permission = createPermission(permissionDTO, businessCode, userId);
            this.save(permission);

            // 5. 启动审批流程
            startApprovalProcess(permission, businessCode, userId);

            log.info("门禁权限申请提交成功：{}", businessCode);

        } catch (Exception e) {
            log.error("门禁权限申请失败", e);
            throw new BusinessException("门禁权限申请失败：" + e.getMessage());
        }
    }

    /**
     * 审批通过后激活权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void activatePermission(String businessCode, Long approveUserId, String approveRemark) {
        try {
            AccessPermissionEntity permission = getByBusinessCode(businessCode);
            if (permission == null) {
                log.warn("未找到权限记录：{}", businessCode);
                return;
            }

            // 更新权限状态
            permission.setStatus(AccessPermissionEntity.Status.ENABLED.getValue());
            permission.setApproveUserId(approveUserId);
            permission.setApproveTime(LocalDateTime.now());
            permission.setApproveRemark(approveRemark);
            this.updateById(permission);

            // 下发权限到设备
            deviceService.grantAccess(permission.getDeviceId(), permission.getPersonId(),
                    permission.getStartTime(), permission.getEndTime());

            log.info("门禁权限激活成功：{}", businessCode);

        } catch (Exception e) {
            log.error("激活门禁权限失败：{}", businessCode, e);
            throw new BusinessException("激活权限失败");
        }
    }

    /**
     * 审批拒绝后取消权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void rejectPermission(String businessCode, Long approveUserId, String approveRemark) {
        try {
            AccessPermissionEntity permission = getByBusinessCode(businessCode);
            if (permission != null) {
                permission.setStatus(AccessPermissionEntity.Status.DISABLED.getValue());
                permission.setApproveUserId(approveUserId);
                permission.setApproveTime(LocalDateTime.now());
                permission.setApproveRemark(approveRemark);
                this.updateById(permission);
            }

            log.info("门禁权限已拒绝：{}", businessCode);

        } catch (Exception e) {
            log.error("拒绝门禁权限失败：{}", businessCode, e);
        }
    }

    /**
     * 撤销权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void revokePermission(Long permissionId, Long userId, String reason) {
        try {
            AccessPermissionEntity permission = this.getById(permissionId);
            if (permission == null) {
                throw new BusinessException("权限记录不存在");
            }

            if (permission.getStatus() != AccessPermissionEntity.Status.ENABLED.getValue()) {
                throw new BusinessException("权限状态不允许撤销");
            }

            // 更新权限状态
            permission.setStatus(AccessPermissionEntity.Status.DISABLED.getValue());
            permission.setUpdatedBy(userId);
            this.updateById(permission);

            // 从设备撤销权限
            deviceService.revokeAccess(permission.getDeviceId(), permission.getPersonId());

            // 记录撤销日志
            recordRevokeLog(permission, userId, reason);

            log.info("门禁权限撤销成功：权限ID={}, 操作人={}", permissionId, userId);

        } catch (Exception e) {
            log.error("撤销门禁权限失败：权限ID={}", permissionId, e);
            throw new BusinessException("撤销权限失败：" + e.getMessage());
        }
    }

    /**
     * 检查人员通行权限
     */
    public boolean checkAccessPermission(Long personId, Long deviceId, LocalDateTime accessTime) {
        try {
            // 查询有效的权限记录
            List<AccessPermissionEntity> permissions = this.lambdaQuery()
                    .eq(AccessPermissionEntity::getPersonId, personId)
                    .eq(AccessPermissionEntity::getDeviceId, deviceId)
                    .eq(AccessPermissionEntity::getStatus, AccessPermissionEntity.Status.ENABLED.getValue())
                    .le(AccessPermissionEntity::getStartTime, accessTime)
                    .ge(AccessPermissionEntity::getEndTime, accessTime)
                    .list();

            if (permissions.isEmpty()) {
                return false;
            }

            // 检查时间权限
            for (AccessPermissionEntity permission : permissions) {
                if (checkTimePermission(permission, accessTime)) {
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            log.error("检查通行权限失败：人员ID={}, 设备ID={}", personId, deviceId, e);
            return false;
        }
    }

    /**
     * 获取人员权限列表
     */
    public List<AccessPermissionDTO> getPersonPermissions(Long personId) {
        List<AccessPermissionEntity> permissions = this.lambdaQuery()
                .eq(AccessPermissionEntity::getPersonId, personId)
                .orderByDesc(AccessPermissionEntity::getCreatedTime)
                .list();

        return permissions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取设备权限列表
     */
    public List<AccessPermissionDTO> getDevicePermissions(Long deviceId) {
        List<AccessPermissionEntity> permissions = this.lambdaQuery()
                .eq(AccessPermissionEntity::getDeviceId, deviceId)
                .orderByDesc(AccessPermissionEntity::getCreatedTime)
                .list();

        return permissions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 清理过期权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void cleanExpiredPermissions() {
        try {
            LocalDateTime now = LocalDateTime.now();

            List<AccessPermissionEntity> expiredPermissions = this.lambdaQuery()
                    .lt(AccessPermissionEntity::getEndTime, now)
                    .eq(AccessPermissionEntity::getStatus, AccessPermissionEntity.Status.ENABLED.getValue())
                    .list();

            for (AccessPermissionEntity permission : expiredPermissions) {
                // 更新状态为已过期
                permission.setStatus(AccessPermissionEntity.Status.EXPIRED.getValue());
                this.updateById(permission);

                // 从设备撤销权限
                deviceService.revokeAccess(permission.getDeviceId(), permission.getPersonId());
            }

            log.info("清理过期权限完成，处理数量：{}", expiredPermissions.size());

        } catch (Exception e) {
            log.error("清理过期权限失败", e);
        }
    }

    /**
     * 验证设备和人员存在性
     */
    private void validateDeviceAndPerson(Long deviceId, Long personId) {
        if (deviceService.getById(deviceId) == null) {
            throw new BusinessException("设备不存在");
        }

        if (personService.getPersonById(personId) == null) {
            throw new BusinessException("人员不存在");
        }
    }

    /**
     * 检查重复权限
     */
    private void validateDuplicatePermission(AccessPermissionDTO permissionDTO) {
        long count = this.lambdaQuery()
                .eq(AccessPermissionEntity::getPersonId, permissionDTO.getPersonId())
                .eq(AccessPermissionEntity::getDeviceId, permissionDTO.getDeviceId())
                .eq(AccessPermissionEntity::getStatus, AccessPermissionEntity.Status.ENABLED.getValue())
                .lt(AccessPermissionEntity::getStartTime, permissionDTO.getEndTime())
                .gt(AccessPermissionEntity::getEndTime, permissionDTO.getStartTime())
                .count();

        if (count > 0) {
            throw new BusinessException("该人员在此设备上已存在重叠的权限时间段");
        }
    }

    /**
     * 创建权限记录
     */
    private AccessPermissionEntity createPermission(AccessPermissionDTO permissionDTO, String businessCode, Long userId) {
        AccessPermissionEntity permission = new AccessPermissionEntity();
        SmartBeanUtil.copyProperties(permissionDTO, permission);
        permission.setBusinessCode(businessCode);
        permission.setStatus(AccessPermissionEntity.Status.PENDING_APPROVAL.getValue());
        permission.setCreatedBy(userId);

        // 设置默认通行权限
        permission.setMondayAccess(1);
        permission.setTuesdayAccess(1);
        permission.setWednesdayAccess(1);
        permission.setThursdayAccess(1);
        permission.setFridayAccess(1);
        permission.setSaturdayAccess(0);
        permission.setSundayAccess(0);

        return permission;
    }

    /**
     * 启动审批流程
     */
    private void startApprovalProcess(AccessPermissionEntity permission, String businessCode, Long userId) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("personId", permission.getPersonId());
            variables.put("deviceId", permission.getDeviceId());
            variables.put("permissionType", permission.getPermissionType());
            variables.put("startTime", permission.getStartTime());
            variables.put("endTime", permission.getEndTime());
            variables.put("reason", permission.getRemark());

            ApprovalStartDTO startDTO = new ApprovalStartDTO();
            startDTO.setBusinessType("ACCESS_AUTH");
            startDTO.setBusinessCode(businessCode);
            startDTO.setBusinessTitle(String.format("门禁权限申请 - %s", permission.getPersonName()));
            startDTO.setVariables(variables);
            startDTO.setFormData(variables);
            startDTO.setRemark(permission.getRemark());

            approvalService.startApproval(startDTO, userId);

        } catch (Exception e) {
            log.error("启动门禁权限审批流程失败：{}", businessCode, e);
            throw new BusinessException("启动审批流程失败");
        }
    }

    /**
     * 检查时间权限
     */
    private boolean checkTimePermission(AccessPermissionEntity permission, LocalDateTime accessTime) {
        // 检查星期权限
        int dayOfWeek = accessTime.getDayOfWeek().getValue();
        boolean hasDayPermission = switch (dayOfWeek) {
            case 1 -> permission.getMondayAccess() == 1;
            case 2 -> permission.getTuesdayAccess() == 1;
            case 3 -> permission.getWednesdayAccess() == 1;
            case 4 -> permission.getThursdayAccess() == 1;
            case 5 -> permission.getFridayAccess() == 1;
            case 6 -> permission.getSaturdayAccess() == 1;
            case 7 -> permission.getSundayAccess() == 1;
            default -> false;
        };

        if (!hasDayPermission) {
            return false;
        }

        // 检查时间段权限
        if (SmartStringUtil.isNotEmpty(permission.getTimeConfig())) {
            try {
                List<TimeSlot> timeSlots = SmartStringUtil.parseObject(permission.getTimeConfig(),
                        new TypeReference<List<TimeSlot>>() {});

                LocalTime accessTimeOnly = accessTime.toLocalTime();
                for (TimeSlot timeSlot : timeSlots) {
                    if (!accessTimeOnly.isBefore(timeSlot.getStartTime()) &&
                        !accessTimeOnly.isAfter(timeSlot.getEndTime())) {
                        return true;
                    }
                }
                return false;
            } catch (Exception e) {
                log.warn("解析时间段配置失败", e);
            }
        }

        return true;
    }

    /**
     * 生成业务编码
     */
    private String generateBusinessCode(String businessType) {
        return businessType + "_" + System.currentTimeMillis();
    }

    /**
     * 根据业务编码获取权限记录
     */
    private AccessPermissionEntity getByBusinessCode(String businessCode) {
        return this.lambdaQuery()
                .eq(AccessPermissionEntity::getBusinessCode, businessCode)
                .one();
    }

    /**
     * 记录撤销日志
     */
    private void recordRevokeLog(AccessPermissionEntity permission, Long userId, String reason) {
        // TODO: 记录操作日志到系统日志表
        log.info("门禁权限撤销日志：权限ID={}, 人员ID={}, 设备ID={}, 操作人={}, 原因={}",
                permission.getPermissionId(), permission.getPersonId(),
                permission.getDeviceId(), userId, reason);
    }

    /**
     * 转换为DTO
     */
    private AccessPermissionDTO convertToDTO(AccessPermissionEntity entity) {
        AccessPermissionDTO dto = new AccessPermissionDTO();
        SmartBeanUtil.copyProperties(entity, dto);
        return dto;
    }
}

/**
 * 时间段配置类
 */
@Data
class TimeSlot {
    private LocalTime startTime;
    private LocalTime endTime;
}
```

### 3.3 设备协议实现

#### 3.3.1 设备连接接口

```java
package net.lab1024.sa.base.module.service.access.protocol;

import net.lab1024.sa.base.module.entity.access.AccessDeviceEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备连接接口
 */
public interface DeviceConnection {

    /**
     * 连接设备
     */
    boolean connect();

    /**
     * 断开连接
     */
    void disconnect();

    /**
     * 是否已连接
     */
    boolean isConnected();

    /**
     * 开门
     */
    boolean openDoor();

    /**
     * 关门
     */
    boolean closeDoor();

    /**
     * 重启设备
     */
    boolean restart();

    /**
     * 同步时间
     */
    boolean syncTime(LocalDateTime dateTime);

    /**
     * 授权访问
     */
    boolean grantAccess(Long personId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 撤销访问权限
     */
    boolean revokeAccess(Long personId);

    /**
     * 获取设备状态
     */
    DeviceStatus getDeviceStatus();

    /**
     * 发送自定义指令
     */
    boolean sendCommand(String command, String params);

    /**
     * 设置事件监听器
     */
    void setEventListener(DeviceEventListener listener);
}
```

#### 3.3.2 TCP协议实现

```java
package net.lab1024.sa.base.module.service.access.protocol.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.module.entity.access.AccessDeviceEntity;
import net.lab1024.sa.base.module.service.access.protocol.DeviceConnection;
import net.lab1024.sa.base.module.service.access.protocol.DeviceEventListener;
import net.lab1024.sa.base.module.service.access.protocol.DeviceStatus;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TCP协议设备连接实现
 */
@Slf4j
public class TcpDeviceConnection implements DeviceConnection {

    private final AccessDeviceEntity device;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private ScheduledExecutorService heartbeatExecutor;
    private DeviceEventListener eventListener;
    private final ExecutorService commandExecutor = Executors.newSingleThreadExecutor();

    public TcpDeviceConnection(AccessDeviceEntity device) {
        this.device = device;
    }

    @Override
    public boolean connect() {
        try {
            if (connected.get()) {
                return true;
            }

            log.info("正在连接TCP设备：{}:{}", device.getIpAddress(), device.getPort());

            socket = new Socket();
            socket.connect(new InetSocketAddress(device.getIpAddress(), device.getPort()), 5000);

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            // 启动消息接收线程
            startMessageReceiver();

            // 启动心跳线程
            startHeartbeat();

            connected.set(true);

            log.info("TCP设备连接成功：{}", device.getDeviceCode());
            return true;

        } catch (Exception e) {
            log.error("TCP设备连接失败：{}", device.getDeviceCode(), e);
            disconnect();
            return false;
        }
    }

    @Override
    public void disconnect() {
        connected.set(false);

        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdown();
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            log.warn("关闭TCP连接失败", e);
        }

        log.info("TCP设备已断开连接：{}", device.getDeviceCode());
    }

    @Override
    public boolean isConnected() {
        return connected.get() && socket != null && !socket.isClosed();
    }

    @Override
    public boolean openDoor() {
        return sendCommand("OPEN_DOOR", "");
    }

    @Override
    public boolean closeDoor() {
        return sendCommand("CLOSE_DOOR", "");
    }

    @Override
    public boolean restart() {
        return sendCommand("RESTART", "");
    }

    @Override
    public boolean syncTime(LocalDateTime dateTime) {
        String timeStr = dateTime.toString().replace("T", " ");
        return sendCommand("SYNC_TIME", timeStr);
    }

    @Override
    public boolean grantAccess(Long personId, LocalDateTime startTime, LocalDateTime endTime) {
        String params = String.format("%d|%s|%s", personId,
                startTime.toString().replace("T", " "),
                endTime.toString().replace("T", " "));
        return sendCommand("GRANT_ACCESS", params);
    }

    @Override
    public boolean revokeAccess(Long personId) {
        return sendCommand("REVOKE_ACCESS", personId.toString());
    }

    @Override
    public DeviceStatus getDeviceStatus() {
        if (sendCommand("GET_STATUS", "")) {
            // 等待设备响应
            try {
                Thread.sleep(1000);
                // TODO: 解析设备状态响应
                return new DeviceStatus();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }

    @Override
    public boolean sendCommand(String command, String params) {
        if (!isConnected()) {
            log.warn("设备未连接，无法发送指令：{}", command);
            return false;
        }

        try {
            String message = String.format("%s|%s|%d", command, params, System.currentTimeMillis());
            writer.write(message);
            writer.newLine();
            writer.flush();

            log.debug("发送TCP指令：{}", message);
            return true;

        } catch (IOException e) {
            log.error("发送TCP指令失败：{}", command, e);
            connected.set(false);
            return false;
        }
    }

    @Override
    public void setEventListener(DeviceEventListener listener) {
        this.eventListener = listener;
    }

    /**
     * 启动消息接收线程
     */
    private void startMessageReceiver() {
        Thread receiverThread = new Thread(() -> {
            while (connected.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    String message = reader.readLine();
                    if (message != null) {
                        handleMessage(message);
                    } else {
                        log.warn("TCP连接已断开：{}", device.getDeviceCode());
                        connected.set(false);
                        break;
                    }
                } catch (IOException e) {
                    if (connected.get()) {
                        log.error("接收TCP消息失败：{}", device.getDeviceCode(), e);
                        connected.set(false);
                    }
                    break;
                }
            }
        });

        receiverThread.setDaemon(true);
        receiverThread.start();
    }

    /**
     * 启动心跳线程
     */
    private void startHeartbeat() {
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            if (connected.get()) {
                sendCommand("HEARTBEAT", "");
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * 处理接收到的消息
     */
    private void handleMessage(String message) {
        try {
            log.debug("接收到TCP消息：{}", message);

            String[] parts = message.split("\\|");
            if (parts.length < 2) {
                log.warn("无效的TCP消息格式：{}", message);
                return;
            }

            String messageType = parts[0];
            String content = parts[1];

            switch (messageType) {
                case "ACCESS_EVENT":
                    handleAccessEvent(content);
                    break;
                case "STATUS_RESPONSE":
                    handleStatusResponse(content);
                    break;
                case "ALARM":
                    handleAlarm(content);
                    break;
                case "HEARTBEAT_RESPONSE":
                    // 心跳响应，更新设备在线状态
                    break;
                default:
                    log.warn("未知的TCP消息类型：{}", messageType);
            }

        } catch (Exception e) {
            log.error("处理TCP消息失败：{}", message, e);
        }
    }

    /**
     * 处理通行事件
     */
    private void handleAccessEvent(String content) {
        try {
            String[] eventParts = content.split(",");
            if (eventParts.length >= 4) {
                Long personId = Long.valueOf(eventParts[0]);
                String personName = eventParts[1];
                Integer result = Integer.valueOf(eventParts[2]);
                String timestamp = eventParts[3];

                if (eventListener != null) {
                    eventListener.onAccessEvent(device.getDeviceId(), personId, personName, result, timestamp);
                }
            }
        } catch (Exception e) {
            log.error("解析通行事件失败：{}", content, e);
        }
    }

    /**
     * 处理状态响应
     */
    private void handleStatusResponse(String content) {
        try {
            // TODO: 解析设备状态信息
            if (eventListener != null) {
                eventListener.onStatusUpdate(device.getDeviceId(), content);
            }
        } catch (Exception e) {
            log.error("解析状态响应失败：{}", content, e);
        }
    }

    /**
     * 处理告警信息
     */
    private void handleAlarm(String content) {
        try {
            String[] alarmParts = content.split(",");
            if (alarmParts.length >= 2) {
                String alarmType = alarmParts[0];
                String alarmMessage = alarmParts[1];

                if (eventListener != null) {
                    eventListener.onAlarm(device.getDeviceId(), alarmType, alarmMessage);
                }
            }
        } catch (Exception e) {
            log.error("解析告警信息失败：{}", content, e);
        }
    }
}
```

## 4. 控制器层

### 4.1 门禁设备控制器

```java
package net.lab1024.sa.base.module.controller.access;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.lab1024.sa.base.common.domain.ResponseResult;
import net.lab1024.sa.base.common.permission.BusinessPermission;
import net.lab1024.sa.base.module.service.access.AccessDeviceService;
import net.lab1024.sa.base.module.service.access.dto.AccessDeviceDTO;
import net.lab1024.sa.base.module.service.access.dto.AccessDeviceQueryDTO;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "门禁设备管理", description = "门禁设备管理接口")
@RequestMapping("/api/access/device")
public class AccessDeviceController {

    private final AccessDeviceService deviceService;

    @Operation(summary = "获取设备列表")
    @GetMapping("/list")
    @BusinessPermission(value = "access:device:view", moduleCode = "ACCESS")
    public ResponseResult<List<AccessDeviceDTO>> getDeviceList(@Valid AccessDeviceQueryDTO queryDTO) {
        List<AccessDeviceDTO> devices = deviceService.getDeviceList(queryDTO);
        return ResponseResult.ok(devices);
    }

    @Operation(summary = "获取设备详情")
    @GetMapping("/{deviceId}")
    @BusinessPermission(value = "access:device:view", moduleCode = "ACCESS")
    public ResponseResult<AccessDeviceDTO> getDeviceDetail(@PathVariable Long deviceId) {
        AccessDeviceDTO device = deviceService.getDeviceDetail(deviceId);
        return ResponseResult.ok(device);
    }

    @Operation(summary = "新增设备")
    @PostMapping("/add")
    @BusinessPermission(value = "access:device:add", moduleCode = "ACCESS")
    public ResponseResult<Long> addDevice(@RequestBody @Valid AccessDeviceDTO deviceDTO) {
        Long deviceId = deviceService.addDevice(deviceDTO, LoginUtil.getLoginUserId());
        return ResponseResult.ok(deviceId);
    }

    @Operation(summary = "编辑设备")
    @PostMapping("/edit")
    @BusinessPermission(value = "access:device:edit", moduleCode = "ACCESS")
    public ResponseResult<Void> editDevice(@RequestBody @Valid AccessDeviceDTO deviceDTO) {
        deviceService.updateDevice(deviceDTO, LoginUtil.getLoginUserId());
        return ResponseResult.ok();
    }

    @Operation(summary = "删除设备")
    @DeleteMapping("/{deviceId}")
    @BusinessPermission(value = "access:device:delete", moduleCode = "ACCESS")
    public ResponseResult<Void> deleteDevice(@PathVariable Long deviceId) {
        deviceService.deleteDevice(deviceId);
        return ResponseResult.ok();
    }

    @Operation(summary = "远程开门")
    @PostMapping("/{deviceId}/open")
    @BusinessPermission(value = "access:device:control", moduleCode = "ACCESS")
    public ResponseResult<Void> remoteOpenDoor(@PathVariable Long deviceId) {
        deviceService.remoteOpenDoor(deviceId, LoginUtil.getLoginUserId());
        return ResponseResult.ok();
    }

    @Operation(summary = "远程关门")
    @PostMapping("/{deviceId}/close")
    @BusinessPermission(value = "access:device:control", moduleCode = "ACCESS")
    public ResponseResult<Void> remoteCloseDoor(@PathVariable Long deviceId) {
        deviceService.remoteCloseDoor(deviceId, LoginUtil.getLoginUserId());
        return ResponseResult.ok();
    }

    @Operation(summary = "重启设备")
    @PostMapping("/{deviceId}/restart")
    @BusinessPermission(value = "access:device:control", moduleCode = "ACCESS")
    public ResponseResult<Void> restartDevice(@PathVariable Long deviceId) {
        deviceService.restartDevice(deviceId, LoginUtil.getLoginUserId());
        return ResponseResult.ok();
    }

    @Operation(summary = "同步设备时间")
    @PostMapping("/{deviceId}/sync-time")
    @BusinessPermission(value = "access:device:control", moduleCode = "ACCESS")
    public ResponseResult<Void> syncDeviceTime(@PathVariable Long deviceId) {
        deviceService.syncDeviceTime(deviceId, LoginUtil.getLoginUserId());
        return ResponseResult.ok();
    }
}
```

### 4.2 门禁权限控制器

```java
package net.lab1024.sa.base.module.controller.access;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.lab1024.sa.base.common.domain.ResponseResult;
import net.lab1024.sa.base.common.permission.BusinessPermission;
import net.lab1024.sa.base.module.service.access.AccessPermissionService;
import net.lab1024.sa.base.module.service.access.dto.AccessPermissionDTO;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "门禁权限管理", description = "门禁权限管理接口")
@RequestMapping("/api/access/permission")
public class AccessPermissionController {

    private final AccessPermissionService permissionService;

    @Operation(summary = "申请门禁权限")
    @PostMapping("/apply")
    @BusinessPermission(value = "access:auth:add", moduleCode = "ACCESS")
    public ResponseResult<Void> applyPermission(@RequestBody @Valid AccessPermissionDTO permissionDTO) {
        permissionService.applyPermission(permissionDTO, LoginUtil.getLoginUserId());
        return ResponseResult.ok();
    }

    @Operation(summary = "获取我的权限列表")
    @GetMapping("/my-permissions")
    @BusinessPermission(value = "access:auth:view", moduleCode = "ACCESS")
    public ResponseResult<List<AccessPermissionDTO>> getMyPermissions() {
        Long userId = LoginUtil.getLoginUserId();
        List<AccessPermissionDTO> permissions = permissionService.getPersonPermissions(userId);
        return ResponseResult.ok(permissions);
    }

    @Operation(summary = "获取设备权限列表")
    @GetMapping("/device/{deviceId}")
    @BusinessPermission(value = "access:auth:view", moduleCode = "ACCESS")
    public ResponseResult<List<AccessPermissionDTO>> getDevicePermissions(@PathVariable Long deviceId) {
        List<AccessPermissionDTO> permissions = permissionService.getDevicePermissions(deviceId);
        return ResponseResult.ok(permissions);
    }

    @Operation(summary = "撤销权限")
    @PostMapping("/{permissionId}/revoke")
    @BusinessPermission(value = "access:auth:delete", moduleCode = "ACCESS")
    public ResponseResult<Void> revokePermission(@PathVariable Long permissionId,
                                               @RequestParam String reason) {
        permissionService.revokePermission(permissionId, LoginUtil.getLoginUserId(), reason);
        return ResponseResult.ok();
    }
}
```

## 5. 前端实现

### 5.1 门禁设备管理页面

```vue
<!-- src/views/access/device/DeviceList.vue -->
<template>
  <div class="access-device-list">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="设备名称">
          <a-input
            v-model:value="searchForm.deviceName"
            placeholder="请输入设备名称"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="设备类型">
          <a-select
            v-model:value="searchForm.deviceType"
            placeholder="请选择设备类型"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="door">门禁</a-select-option>
            <a-select-option value="gate">闸机</a-select-option>
            <a-select-option value="barrier">道闸</a-select-option>
            <a-select-option value="turnstile">转闸</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option :value="0">离线</a-select-option>
            <a-select-option :value="1">在线</a-select-option>
            <a-select-option :value="2">故障</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="searchDevices">
              <template #icon><SearchOutlined /></template>
              搜索
            </a-button>
            <a-button @click="resetSearch">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <a-space>
        <a-button type="primary" @click="showAddModal">
          <template #icon><PlusOutlined /></template>
          新增设备
        </a-button>
        <a-button @click="refreshDevices">
          <template #icon><ReloadOutlined /></template>
          刷新
        </a-button>
      </a-space>
    </div>

    <!-- 设备列表 -->
    <div class="device-table">
      <a-table
        :columns="columns"
        :data-source="deviceList"
        :loading="loading"
        :pagination="false"
        row-key="deviceId"
      >
        <!-- 设备状态 -->
        <template #status="{ record }">
          <a-tag :color="getStatusColor(record.status)">
            {{ getStatusText(record.status) }}
          </a-tag>
        </template>

        <!-- 连接状态 -->
        <template #connected="{ record }">
          <a-tag :color="record.connected ? 'green' : 'red'">
            {{ record.connected ? '已连接' : '未连接' }}
          </a-tag>
        </template>

        <!-- 门状态 -->
        <template #doorStatus="{ record }">
          <span v-if="record.doorStatus === 'open'" class="text-green">开启</span>
          <span v-else-if="record.doorStatus === 'closed'" class="text-blue">关闭</span>
          <span v-else class="text-gray">未知</span>
        </template>

        <!-- 操作 -->
        <template #action="{ record }">
          <a-space>
            <a-button type="link" size="small" @click="viewDevice(record)">
              查看
            </a-button>
            <a-button type="link" size="small" @click="editDevice(record)">
              编辑
            </a-button>
            <a-dropdown>
              <template #overlay>
                <a-menu @click="({ key }) => handleMenuClick(key, record)">
                  <a-menu-item key="open">远程开门</a-menu-item>
                  <a-menu-item key="close">远程关门</a-menu-item>
                  <a-menu-item key="restart">重启设备</a-menu-item>
                  <a-menu-item key="sync">同步时间</a-menu-item>
                  <a-menu-divider />
                  <a-menu-item key="delete" danger>删除设备</a-menu-item>
                </a-menu>
              </template>
              <a-button type="link" size="small">
                更多 <DownOutlined />
              </a-button>
            </a-dropdown>
          </a-space>
        </template>
      </a-table>
    </div>

    <!-- 设备编辑弹窗 -->
    <DeviceModal
      v-model:visible="modalVisible"
      :device="currentDevice"
      :mode="modalMode"
      @ok="handleModalOk"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import {
  SearchOutlined,
  PlusOutlined,
  ReloadOutlined,
  DownOutlined
} from '@ant-design/icons-vue';
import { getDeviceList, deleteDevice, openDoor, closeDoor, restartDevice, syncTime }
  from '@/api/access/device';
import DeviceModal from './components/DeviceModal.vue';

// 响应式数据
const loading = ref(false);
const deviceList = ref([]);
const modalVisible = ref(false);
const modalMode = ref('add');
const currentDevice = ref(null);

// 搜索表单
const searchForm = reactive({
  deviceName: '',
  deviceType: undefined,
  status: undefined
});

// 表格列配置
const columns = [
  {
    title: '设备编码',
    dataIndex: 'deviceCode',
    key: 'deviceCode',
    width: 120
  },
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    key: 'deviceName',
    width: 150
  },
  {
    title: '设备类型',
    dataIndex: 'deviceType',
    key: 'deviceType',
    width: 100,
    customRender: ({ text }) => getDeviceTypeText(text)
  },
  {
    title: '所属区域',
    dataIndex: 'areaName',
    key: 'areaName',
    width: 120
  },
  {
    title: 'IP地址',
    dataIndex: 'ipAddress',
    key: 'ipAddress',
    width: 120
  },
  {
    title: '状态',
    key: 'status',
    slots: { customRender: 'status' },
    width: 80
  },
  {
    title: '连接状态',
    key: 'connected',
    slots: { customRender: 'connected' },
    width: 80
  },
  {
    title: '门状态',
    key: 'doorStatus',
    slots: { customRender: 'doorStatus' },
    width: 80
  },
  {
    title: '最后心跳',
    dataIndex: 'lastHeartbeat',
    key: 'lastHeartbeat',
    width: 150,
    customRender: ({ text }) => text ? formatDateTime(text) : '-'
  },
  {
    title: '操作',
    key: 'action',
    slots: { customRender: 'action' },
    width: 200,
    fixed: 'right'
  }
];

// 方法
const loadDevices = async () => {
  loading.value = true;
  try {
    const result = await getDeviceList(searchForm);
    deviceList.value = result.data || [];
  } catch (error) {
    message.error('加载设备列表失败');
  } finally {
    loading.value = false;
  }
};

const searchDevices = () => {
  loadDevices();
};

const resetSearch = () => {
  Object.assign(searchForm, {
    deviceName: '',
    deviceType: undefined,
    status: undefined
  });
  loadDevices();
};

const refreshDevices = () => {
  loadDevices();
};

const showAddModal = () => {
  modalMode.value = 'add';
  currentDevice.value = null;
  modalVisible.value = true;
};

const viewDevice = (device) => {
  modalMode.value = 'view';
  currentDevice.value = device;
  modalVisible.value = true;
};

const editDevice = (device) => {
  modalMode.value = 'edit';
  currentDevice.value = device;
  modalVisible.value = true;
};

const handleModalOk = () => {
  modalVisible.value = false;
  loadDevices();
};

const handleMenuClick = async (key, device) => {
  switch (key) {
    case 'open':
      await handleOpenDoor(device);
      break;
    case 'close':
      await handleCloseDoor(device);
      break;
    case 'restart':
      await handleRestartDevice(device);
      break;
    case 'sync':
      await handleSyncTime(device);
      break;
    case 'delete':
      handleDeleteDevice(device);
      break;
  }
};

const handleOpenDoor = async (device) => {
  Modal.confirm({
    title: '确认远程开门',
    content: `确定要远程打开设备"${device.deviceName}"吗？`,
    onOk: async () => {
      try {
        await openDoor(device.deviceId);
        message.success('远程开门成功');
        loadDevices();
      } catch (error) {
        message.error('远程开门失败');
      }
    }
  });
};

const handleCloseDoor = async (device) => {
  Modal.confirm({
    title: '确认远程关门',
    content: `确定要远程关闭设备"${device.deviceName}"吗？`,
    onOk: async () => {
      try {
        await closeDoor(device.deviceId);
        message.success('远程关门成功');
        loadDevices();
      } catch (error) {
        message.error('远程关门失败');
      }
    }
  });
};

const handleRestartDevice = async (device) => {
  Modal.confirm({
    title: '确认重启设备',
    content: `确定要重启设备"${device.deviceName}"吗？`,
    onOk: async () => {
      try {
        await restartDevice(device.deviceId);
        message.success('设备重启成功');
        loadDevices();
      } catch (error) {
        message.error('设备重启失败');
      }
    }
  });
};

const handleSyncTime = async (device) => {
  try {
    await syncTime(device.deviceId);
    message.success('时间同步成功');
  } catch (error) {
    message.error('时间同步失败');
  }
};

const handleDeleteDevice = (device) => {
  Modal.confirm({
    title: '确认删除设备',
    content: `确定要删除设备"${device.deviceName}"吗？此操作不可恢复。`,
    okText: '确认删除',
    okType: 'danger',
    onOk: async () => {
      try {
        await deleteDevice(device.deviceId);
        message.success('设备删除成功');
        loadDevices();
      } catch (error) {
        message.error('设备删除失败');
      }
    }
  });
};

// 工具方法
const getStatusColor = (status) => {
  const colors = {
    0: 'red',    // 离线
    1: 'green',  // 在线
    2: 'orange'  // 故障
  };
  return colors[status] || 'default';
};

const getStatusText = (status) => {
  const texts = {
    0: '离线',
    1: '在线',
    2: '故障'
  };
  return texts[status] || '未知';
};

const getDeviceTypeText = (type) => {
  const types = {
    door: '门禁',
    gate: '闸机',
    barrier: '道闸',
    turnstile: '转闸'
  };
  return types[type] || '未知';
};

const formatDateTime = (dateTime) => {
  if (!dateTime) return '';
  return new Date(dateTime).toLocaleString();
};

// 生命周期
onMounted(() => {
  loadDevices();
});
</script>

<style scoped>
.access-device-list {
  padding: 24px;
  background: #fff;
  border-radius: 8px;
}

.search-bar {
  margin-bottom: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
}

.action-bar {
  margin-bottom: 16px;
}

.device-table {
  margin-top: 16px;
}

.text-green {
  color: #52c41a;
}

.text-blue {
  color: #1890ff;
}

.text-gray {
  color: #999;
}
</style>
```

## 6. 权限配置示例

### 6.1 门禁系统权限列表

```json
{
  "moduleCode": "ACCESS",
  "moduleName": "门禁系统",
  "menus": [
    {
      "menuCode": "access_device",
      "menuName": "门禁设备",
      "permissions": [
        {
          "code": "view",
          "name": "查看",
          "description": "查看设备列表和详情"
        },
        {
          "code": "add",
          "name": "新增",
          "description": "新增门禁设备"
        },
        {
          "code": "edit",
          "name": "编辑",
          "description": "编辑设备信息"
        },
        {
          "code": "delete",
          "name": "删除",
          "description": "删除设备"
        },
        {
          "code": "control",
          "name": "控制",
          "description": "远程控制设备开关门"
        }
      ]
    },
    {
      "menuCode": "access_record",
      "menuName": "通行记录",
      "permissions": [
        {
          "code": "view",
          "name": "查看",
          "description": "查看通行记录"
        },
        {
          "code": "export",
          "name": "导出",
          "description": "导出通行记录数据"
        }
      ]
    },
    {
      "menuCode": "access_auth",
      "menuName": "门禁授权",
      "permissions": [
        {
          "code": "view",
          "name": "查看",
          "description": "查看授权记录"
        },
        {
          "code": "add",
          "name": "新增",
          "description": "申请新的授权"
        },
        {
          "code": "edit",
          "name": "编辑",
          "description": "编辑授权信息"
        },
        {
          "code": "delete",
          "name": "删除",
          "description": "撤销授权"
        },
        {
          "code": "approve",
          "name": "审批",
          "description": "审批授权申请"
        }
      ]
    }
  ]
}
```

## 7. 总结

### 7.1 模块特点

门禁系统公共模块具有以下特点：

1. **设备统一管理**: 支持多种类型门禁设备的统一接入和控制
2. **权限精细化控制**: 基于人员、时间、区域的维度权限管理
3. **实时监控能力**: 设备状态实时监控和异常报警
4. **审批流程集成**: 与warm-flow审批引擎深度集成
5. **协议扩展性**: 支持多种通信协议的设备接入

### 7.2 技术架构优势

1. **模块化设计**: 清晰的分层架构，易于维护和扩展
2. **协议抽象**: 统一的设备连接接口，支持多种协议
3. **异步处理**: 基于线程池的异步指令处理
4. **连接池管理**: 设备连接池和心跳机制
5. **事件驱动**: 基于事件监听器的实时处理机制

### 7.3 应用价值

1. **提升安全性**: 细粒度的权限控制和实时监控
2. **提高效率**: 远程控制和自动化管理
3. **降低成本**: 统一平台管理多种设备
4. **增强合规性**: 完整的审批流程和操作记录
5. **改善体验**: 直观的管理界面和实时反馈

通过这个完整的门禁系统模块，SmartAdmin系统能够为智慧园区提供专业、可靠、易用的门禁管理解决方案，确保园区的安全性和管理效率。