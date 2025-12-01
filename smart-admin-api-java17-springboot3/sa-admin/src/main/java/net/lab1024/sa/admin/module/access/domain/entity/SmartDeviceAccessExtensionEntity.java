package net.lab1024.sa.admin.module.access.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 门禁设备扩展实体类
 * <p>
 * 基于SmartDeviceEntity的扩展表机制，增加门禁特有字段
 * 严格遵循扩展表架构设计，避免重复建设，基于现有设备管理增强和完善
 *
 * 扩展表架构：t_smart_device (基础设备表) + t_smart_device_access_extension (门禁设备扩展表)
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_smart_device_access_extension")
public class SmartDeviceAccessExtensionEntity extends BaseEntity {

    /**
     * 设备ID（关联SmartDeviceEntity.deviceId）
     */
    @NotNull(message = "设备ID不能为空")
    @TableField("device_id")
    private Long deviceId;

    /**
     * 门禁设备类型 (FINGERPRINT-指纹机, FACE-人脸识别, CARD-刷卡机, PASSWORD-密码机, QR-二维码)
     */
    @TableField("access_device_type")
    private String accessDeviceType;

    /**
     * 开门方式 (BIOMETRIC-生物识别, CARD-刷卡, PASSWORD-密码, QR-二维码, REMOTE-远程)
     */
    @TableField("open_method")
    private String openMethod;

    /**
     * 识别时间阈值(秒)
     */
    @TableField("recognition_threshold")
    private Integer recognitionThreshold;

    /**
     * 是否支持活体检测 (0-不支持, 1-支持)
     */
    @TableField("live_detection_enabled")
    private Integer liveDetectionEnabled;

    /**
     * 防拆报警启用 (0-禁用, 1-启用)
     */
    @TableField("tamper_alarm_enabled")
    private Integer tamperAlarmEnabled;

    /**
     * 开门延迟时间(秒)
     */
    @TableField("open_delay")
    private Integer openDelay;

    /**
     * 关门延迟时间(秒)
     */
    @TableField("close_delay")
    private Integer closeDelay;

    /**
     * 最大用户容量
     */
    @TableField("max_user_capacity")
    private Integer maxUserCapacity;

    /**
     * 当前用户数量
     */
    @TableField("current_user_count")
    private Integer currentUserCount;

    /**
     * 门锁类型 (ELECTRIC-电控锁, ELECTROMAGNETIC-电磁锁, ELECTRIC_STRIKE-电插锁)
     */
    @TableField("lock_type")
    private String lockType;

    /**
     * 锁状态 (LOCKED-锁定, UNLOCKED-解锁, FAULT-故障)
     */
    @TableField("lock_status")
    private String lockStatus;

    /**
     * 门磁状态 (CLOSE-关闭, OPEN-打开, FAULT-故障)
     */
    @TableField("door_sensor_status")
    private String doorSensorStatus;

    /**
     * 是否支持远程开门 (0-不支持, 1-支持)
     */
    @TableField("remote_open_enabled")
    private Integer remoteOpenEnabled;

    /**
     * 是否支持胁迫报警 (0-不支持, 1-支持)
     */
    @TableField("duress_alarm_enabled")
    private Integer duressAlarmEnabled;

    /**
     * 是否支持多卡开门 (0-不支持, 1-支持)
     */
    @TableField("multi_card_open_enabled")
    private Integer multiCardOpenEnabled;

    /**
     * 多卡开门数量
     */
    @TableField("multi_card_count")
    private Integer multiCardCount;

    /**
     * 首卡开门启用 (0-禁用, 1-启用)
     */
    @TableField("first_card_open_enabled")
    private Integer firstCardOpenEnabled;

    /**
     * 常开模式启用 (0-禁用, 1-启用)
     */
    @TableField("normally_open_enabled")
    private Integer normallyOpenEnabled;

    /**
     * 常开模式时间段(JSON格式)
     */
    @TableField("normally_open_period")
    private String normallyOpenPeriod;

    /**
     * 设备配置信息(JSON格式)
     */
    @TableField("device_config")
    private String deviceConfig;

    /**
     * 协议配置
     */
    @TableField("protocol_config")
    private String protocolConfig;

    /**
     * 设备认证密钥
     */
    @TableField("auth_key")
    private String authKey;

    /**
     * 最后同步时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_sync_time")
    private LocalDateTime lastSyncTime;

    /**
     * 最后心跳时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_heartbeat_time")
    private LocalDateTime lastHeartbeatTime;

    /**
     * 设备状态 (ONLINE-在线, OFFLINE-离线, FAULT-故障, MAINTENANCE-维护中)
     */
    @TableField("device_status")
    private String deviceStatus;

    /**
     * 设备IP地址
     */
    @TableField("device_ip")
    private String deviceIp;

    /**
     * 设备端口
     */
    @TableField("device_port")
    private Integer devicePort;

    /**
     * 设备版本号
     */
    @TableField("firmware_version")
    private String firmwareVersion;

    /**
     * 备注信息
     */
    @TableField("remark")
    private String remark;

    /**
     * 扩展配置1
     */
    @TableField("ext_config1")
    private String extConfig1;

    /**
     * 扩展配置2
     */
    @TableField("ext_config2")
    private String extConfig2;

    /**
     * 扩展配置3
     */
    @TableField("ext_config3")
    private String extConfig3;

    // ==================== 门禁配置字段 ====================

    /**
     * 扩展ID（主键）
     */
    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long extensionId;

    /**
     * 访问模式
     */
    @TableField("access_mode")
    private String accessMode;

    /**
     * 开门模式
     */
    @TableField("open_mode")
    private String openMode;

    /**
     * 锁模式
     */
    @TableField("lock_mode")
    private String lockMode;

    /**
     * 验证模式
     */
    @TableField("verification_mode")
    private String verificationMode;

    /**
     * 防回溯启用
     */
    @TableField("anti_passback_enabled_flag")
    private Boolean antiPassbackEnabled;

    /**
     * 胁迫开门启用
     */
    @TableField("duress_open_enabled_flag")
    private Boolean duressOpenEnabled;

    /**
     * 时区启用
     */
    @TableField("time_zone_enabled")
    private Boolean timeZoneEnabled;

    /**
     * 访问时区
     */
    @TableField("access_time_zone")
    private String accessTimeZone;

    // ==================== 业务方法 ====================

    /**
     * 获取是否支持远程开门（兼容方法）
     */
    public Boolean getRemoteOpenEnabled() {
        return remoteOpenEnabled != null && remoteOpenEnabled == 1;
    }

    /**
     * 设置是否支持远程开门（兼容方法）
     */
    public void setRemoteOpenEnabled(Boolean enabled) {
        this.remoteOpenEnabled = enabled != null && enabled ? 1 : 0;
    }

    /**
     * 获取防回溯启用状态
     */
    public Boolean getAntiPassbackEnabled() {
        return antiPassbackEnabled != null && antiPassbackEnabled;
    }

    /**
     * 设置防回溯启用状态
     */
    public void setAntiPassbackEnabled(Boolean enabled) {
        this.antiPassbackEnabled = enabled;
    }

    /**
     * 获取胁迫开门启用状态
     */
    public Boolean getDuressOpenEnabled() {
        return duressOpenEnabled != null && duressOpenEnabled;
    }

    /**
     * 设置胁迫开门启用状态
     */
    public void setDuressOpenEnabled(Boolean enabled) {
        this.duressOpenEnabled = enabled;
    }

    /**
     * 获取时区启用状态（Integer转Boolean）
     */
    public Boolean getTimeZoneEnabled() {
        return timeZoneEnabled != null && timeZoneEnabled;
    }

    /**
     * 设置时区启用状态
     */
    public void setTimeZoneEnabled(Boolean enabled) {
        this.timeZoneEnabled = enabled != null && enabled;
    }

    // 手动添加基础getter/setter方法以确保Lombok注解失效时的兼容性
    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }
}