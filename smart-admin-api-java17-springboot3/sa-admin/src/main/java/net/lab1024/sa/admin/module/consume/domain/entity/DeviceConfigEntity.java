package net.lab1024.sa.admin.module.consume.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 消费设备配置实体类
 * 管理消费设备的配置和状态
 *
 * @author SmartAdmin Team
 * @date 2025/11/16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_consume_device_config")
public class DeviceConfigEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long configId;

    /**
     * 设备ID（关联设备管理表）
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备编号（唯一标识）
     */
    private String deviceNo;

    /**
     * 设备类型（POS/DOOR/TURNTABLE/KIOSK/VENDING等）
     */
    private String deviceType;

    /**
     * 所属区域ID
     */
    private String regionId;

    /**
     * 所属区域名称
     */
    private String regionName;

    /**
     * 设备位置描述
     */
    private String location;

    /**
     * 设备安装位置（经纬度）
     */
    private String coordinates;

    /**
     * 支持的消费模式（逗号分隔：FIXED_AMOUNT,FREE_AMOUNT,METERING等）
     */
    private String supportedModes;

    /**
     * 默认消费模式
     */
    private String defaultMode;

    /**
     * 设备状态（ONLINE/OFFLINE/MAINTENANCE/ERROR/DISABLED）
     */
    private String status;

    /**
     * 设备配置优先级（数值越大优先级越高）
     */
    private Integer priority;

    /**
     * 设备IP地址
     */
    private String ipAddress;

    /**
     * 设备MAC地址
     */
    private String macAddress;

    /**
     * 设备序列号
     */
    private String serialNumber;

    /**
     * 设备固件版本
     */
    private String firmwareVersion;

    /**
     * 设备软件版本
     */
    private String softwareVersion;

    /**
     * 设备制造商
     */
    private String manufacturer;

    /**
     * 设备型号
     */
    private String model;

    /**
     * 设备安装时间
     */
    private LocalDateTime installTime;

    /**
     * 设备上线时间
     */
    private LocalDateTime onlineTime;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeatTime;

    /**
     * 设备离线时间
     */
    private LocalDateTime offlineTime;

    /**
     * 设备启用时间
     */
    private LocalDateTime enableTime;

    /**
     * 设备禁用时间
     */
    private LocalDateTime disableTime;

    /**
     * 消费模式配置JSON（存储模式特定的配置参数）
     */
    private String modeConfig;

    /**
     * 设备硬件配置JSON
     */
    private String hardwareConfig;

    /**
     * 设备网络配置JSON
     */
    private String networkConfig;

    /**
     * 设备安全配置JSON
     */
    private String securityConfig;

    /**
     * 扩展数据JSON（存储扩展字段）
     */
    private String extendData;

    /**
     * 是否启用离线模式
     */
    private Boolean offlineMode;

    /**
     * 离线数据同步方式（AUTO/MANUAL）
     */
    private String offlineSyncMode;

    /**
     * 离线数据存储容量（MB）
     */
    private Integer offlineStorageCapacity;

    /**
     * 离线数据同步间隔（秒）
     */
    private Integer offlineSyncInterval;

    /**
     * 是否启用自动重启
     */
    private Boolean autoRestart;

    /**
     * 自动重启时间间隔（小时）
     */
    private Integer restartInterval;

    /**
     * 是否启用远程控制
     */
    private Boolean remoteControl;

    /**
     * 远程控制权限（JSON格式）
     */
    private String remoteControlPermissions;

    /**
     * 是否启用实时监控
     */
    private Boolean realTimeMonitoring;

    /**
     * 监控数据上报间隔（秒）
     */
    private Integer monitoringInterval;

    /**
     * 设备管理员ID
     */
    private Long adminUserId;

    /**
     * 设备管理员姓名
     */
    private String adminUserName;

    /**
     * 技术支持联系电话
     */
    private String supportPhone;

    /**
     * 技术支持邮箱
     */
    private String supportEmail;

    /**
     * 设备维护周期（天）
     */
    private Integer maintenanceCycle;

    /**
     * 最后维护时间
     */
    private LocalDateTime lastMaintenanceTime;

    /**
     * 下次维护时间
     */
    private LocalDateTime nextMaintenanceTime;

    /**
     * 设备使用次数统计
     */
    private Long usageCount;

    /**
     * 设备使用时长统计（小时）
     */
    private Long usageHours;

    /**
     * 设备故障次数
     */
    private Integer failureCount;

    /**
     * 设备运行状态码
     */
    private String statusCode;

    /**
     * 设备运行状态描述
     */
    private String statusDescription;

    /**
     * 设备性能指标JSON（CPU、内存、磁盘等）
     */
    private String performanceMetrics;

    /**
     * 是否启用设备日志
     */
    private Boolean enableLogging;

    /**
     * 日志保留天数
     */
    private Integer logRetentionDays;

    /**
     * 是否启用数据加密
     */
    private Boolean enableEncryption;

    /**
     * 加密密钥版本
     */
    private String encryptionKeyVersion;

    /**
     * 设备标签（JSON格式，用于分组管理）
     */
    private String deviceTags;

    /**
     * 设备分组
     */
    private String deviceGroup;

    /**
     * 备注
     */
    private String remark;
}