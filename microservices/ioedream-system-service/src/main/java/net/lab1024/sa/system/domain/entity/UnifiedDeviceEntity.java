package net.lab1024.sa.system.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 统一设备实体类
 * <p>
 * 严格遵循repowiki业务架构规范：
 * - 整合多种设备类型的统一数据模型
 * - 支持门禁、视频、消费、考勤等设备类型
 * - 统一的设备管理，便于维护和扩展
 * - 使用设备类型字段区分不同的设备业务逻辑
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_unified_device")
@Schema(description = "统一设备实体")
public class UnifiedDeviceEntity extends BaseEntity {

    /**
     * 设备ID
     */
    @TableId(value = "device_id", type = IdType.AUTO)
    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    /**
     * 设备编号（设备唯一标识）
     */
    @TableField("device_code")
    @Schema(description = "设备编号", example = "DEV001")
    private String deviceCode;

    /**
     * 设备名称
     */
    @TableField("device_name")
    @Schema(description = "设备名称", example = "门禁设备01")
    private String deviceName;

    /**
     * 设备类型
     * ACCESS-门禁设备, VIDEO-视频设备, CONSUME-消费设备, ATTENDANCE-考勤设备, SMART-智能设备
     */
    @TableField("device_type")
    @Schema(description = "设备类型", example = "ACCESS")
    private String deviceType;

    /**
     * 设备型号
     */
    @TableField("device_model")
    @Schema(description = "设备型号", example = "AC-100")
    private String deviceModel;

    /**
     * 设备厂商
     */
    @TableField("manufacturer")
    @Schema(description = "设备厂商", example = "海康威视")
    private String manufacturer;

    /**
     * 设备序列号
     */
    @TableField("serial_number")
    @Schema(description = "设备序列号", example = "SN123456789")
    private String serialNumber;

    /**
     * 设备IP地址
     */
    @TableField("ip_address")
    @Schema(description = "设备IP地址", example = "192.168.1.100")
    private String ipAddress;

    /**
     * 设备端口号
     */
    @TableField("port")
    @Schema(description = "设备端口号", example = "8000")
    private Integer port;

    /**
     * 设备MAC地址
     */
    @TableField("mac_address")
    @Schema(description = "设备MAC地址", example = "00:11:22:33:44:55")
    private String macAddress;

    /**
     * 所属区域ID（主要用于门禁设备）
     */
    @TableField("area_id")
    @Schema(description = "所属区域ID", example = "1")
    private Long areaId;

    /**
     * 所属区域名称
     */
    @TableField("area_name")
    @Schema(description = "所属区域名称", example = "1号楼")
    private String areaName;

    /**
     * 设备位置描述
     */
    @TableField("location")
    @Schema(description = "设备位置描述", example = "1号楼1层大厅")
    private String location;

    /**
     * 在线状态：0-离线，1-在线
     */
    @TableField("online_status")
    @Schema(description = "在线状态", example = "1")
    private Integer onlineStatus;

    /**
     * 启用状态：0-禁用，1-启用
     */
    @TableField("enabled")
    @Schema(description = "启用状态", example = "1")
    private Integer enabled;

    /**
     * 设备状态
     * ONLINE-在线, OFFLINE-离线, FAULT-故障, MAINTENANCE-维护中, DELETED-已删除
     */
    @TableField("device_status")
    @Schema(description = "设备状态", example = "ONLINE")
    private String deviceStatus;

    /**
     * 最后心跳时间
     */
    @TableField("last_heartbeat")
    @Schema(description = "最后心跳时间")
    private LocalDateTime lastHeartbeat;

    /**
     * 工作模式（主要用于门禁设备）
     * 0-普通模式，1-刷卡模式，2-人脸模式，3-指纹模式，4-混合模式
     */
    @TableField("work_mode")
    @Schema(description = "工作模式", example = "4")
    private Integer workMode;

    /**
     * 设备描述
     */
    @TableField("device_description")
    @Schema(description = "设备描述", example = "主要门禁设备")
    private String deviceDescription;

    /**
     * 设备配置信息（JSON格式）
     */
    @TableField("device_config")
    @Schema(description = "设备配置信息", example = "{}")
    private String deviceConfig;

    /**
     * 扩展属性（JSON格式）
     */
    @TableField("extend_properties")
    @Schema(description = "扩展属性", example = "{}")
    private String extendProperties;
}
