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
 * 门禁门实体类
 * <p>
 * 基于设备ID和区域ID的门表，实现门禁区域的物理门管理
 * 严格遵循扩展表架构设计，避免重复建设，基于现有设备和区域管理增强和完善
 *
 * 关联架构：SmartDeviceEntity + AreaEntity + SmartAccessDoorEntity
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_smart_access_door")
public class SmartAccessDoorEntity extends BaseEntity {

    /**
     * 门ID（主键）
     */
    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long doorId;

    /**
     * 设备ID（关联SmartDeviceEntity.deviceId）
     */
    @NotNull(message = "设备ID不能为空")
    @TableField("device_id")
    private Long deviceId;

    /**
     * 区域ID（关联AreaEntity.areaId）
     */
    @NotNull(message = "区域ID不能为空")
    @TableField("area_id")
    private Long areaId;

    /**
     * 门编号
     */
    @TableField("door_number")
    private String doorNumber;

    /**
     * 门名称
     */
    @TableField("door_name")
    private String doorName;

    /**
     * 门类型 (MAIN-主门, SIDE-侧门, EMERGENCY-紧急门, FIRE-消防门, STAFF-员工门, VISITOR-访客门)
     */
    @TableField("door_type")
    private String doorType;

    /**
     * 门方向 (IN-入口, OUT-出口, IN_OUT-双向)
     */
    @TableField("door_direction")
    private String doorDirection;

    /**
     * 门状态 (CLOSED-关闭, OPEN-打开, LOCKED-锁定, FAULT-故障, MAINTENANCE-维护)
     */
    @TableField("door_status")
    private String doorStatus;

    /**
     * 锁状态 (LOCKED-锁定, UNLOCKED-解锁, ERROR-错误)
     */
    @TableField("lock_status")
    private String lockStatus;

    /**
     * 门磁状态 (CLOSE-关闭, OPEN-打开, FAULT-故障, UNKNOWN-未知)
     */
    @TableField("door_sensor_status")
    private String doorSensorStatus;

    /**
     * 开门方式 (KEY-钥匙, CARD-刷卡, BIOMETRIC-生物识别, PASSWORD-密码, REMOTE-远程, PUSH-推门)
     */
    @TableField("open_method")
    private String openMethod;

    /**
     * 验证方式组合(JSON格式)
     */
    @TableField("verification_methods")
    private String verificationMethods;

    /**
     * 是否支持远程开门 (0-不支持, 1-支持)
     */
    @TableField("remote_open_enabled")
    private Integer remoteOpenEnabled;

    /**
     * 是否支持胁迫开门 (0-不支持, 1-支持)
     */
    @TableField("duress_open_enabled")
    private Integer duressOpenEnabled;

    /**
     * 开门延迟时间(秒)
     */
    @TableField("open_delay_time")
    private Integer openDelayTime;

    /**
     * 关门延迟时间(秒)
     */
    @TableField("close_delay_time")
    private Integer closeDelayTime;

    /**
     * 保持开门时间(秒，0表示不保持)
     */
    @TableField("hold_open_time")
    private Integer holdOpenTime;

    /**
     * 最大开门力度
     */
    @TableField("max_open_force")
    private Integer maxOpenForce;

    /**
     * 是否启用双向控制 (0-单向, 1-双向)
     */
    @TableField("bidirectional_control_enabled")
    private Integer bidirectionalControlEnabled;

    /**
     * 是否启用门磁保护 (0-禁用, 1-启用)
     */
    @TableField("door_sensor_protection_enabled")
    private Integer doorSensorProtectionEnabled;

    /**
     * 是否启用开锁检测 (0-禁用, 1-启用)
     */
    @TableField("unlock_detection_enabled")
    private Integer unlockDetectionEnabled;

    /**
     * 门位置描述
     */
    @TableField("door_location")
    private String doorLocation;

    /**
     * 门坐标信息 (JSON格式: {x: 100, y: 200, floor: 1})
     */
    @TableField("door_coordinates")
    private String doorCoordinates;

    /**
     * 绑定的读头ID列表(JSON格式)
     */
    @TableField("bound_reader_ids")
    private String boundReaderIds;

    /**
     * 绑定的摄像头ID列表(JSON格式)
     */
    @TableField("bound_camera_ids")
    private String boundCameraIds;

    /**
     * 绑定的报警器ID列表(JSON格式)
     */
    @TableField("bound_alarm_ids")
    private String boundAlarmIds;

    /**
     * 开门记录配置(JSON格式)
     */
    @TableField("access_record_config")
    private String accessRecordConfig;

    /**
     * 时间段权限配置(JSON格式)
     */
    @TableField("time_period_config")
    private String timePeriodConfig;

    /**
     * 权限级别要求
     */
    @TableField("required_access_level")
    private Integer requiredAccessLevel;

    /**
     * 是否启用反潜 (0-禁用, 1-启用)
     */
    @TableField("anti_passback_enabled")
    private Integer antiPassbackEnabled;

    /**
     * 反潜区域范围 (0-设备, 1-区域, 2-全区域)
     */
    @TableField("anti_passback_scope")
    private Integer antiPassbackScope;

    /**
     * 最后开门时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("last_open_time")
    private LocalDateTime lastOpenTime;

    /**
     * 今日开门次数
     */
    @TableField("today_open_count")
    private Integer todayOpenCount;

    /**
     * 本月开门次数
     */
    @TableField("month_open_count")
    private Integer monthOpenCount;

    /**
     * 异常开门次数
     */
    @TableField("abnormal_open_count")
    private Integer abnormalOpenCount;

    /**
     * 门配置参数(JSON格式)
     */
    @TableField("door_config")
    private String doorConfig;

    /**
     * 门维护状态 (NORMAL-正常, MAINTENANCE-维护, MALFUNCTION-故障)
     */
    @TableField("maintenance_status")
    private String maintenanceStatus;

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

    // Getter和Setter方法
    public Long getDoorId() {
        return doorId;
    }

    public void setDoorId(Long doorId) {
        this.doorId = doorId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public String getDoorNumber() {
        return doorNumber;
    }

    public void setDoorNumber(String doorNumber) {
        this.doorNumber = doorNumber;
    }

    public String getDoorName() {
        return doorName;
    }

    public void setDoorName(String doorName) {
        this.doorName = doorName;
    }

    public String getDoorType() {
        return doorType;
    }

    public void setDoorType(String doorType) {
        this.doorType = doorType;
    }

    public String getDoorDirection() {
        return doorDirection;
    }

    public void setDoorDirection(String doorDirection) {
        this.doorDirection = doorDirection;
    }

    public String getDoorStatus() {
        return doorStatus;
    }

    public void setDoorStatus(String doorStatus) {
        this.doorStatus = doorStatus;
    }
}