package net.lab1024.sa.system.domain.vo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统一设备视图对象
 * <p>
 * 用于前端展示的设备信息
 * 严格遵循repowiki规范：完整的字段映射和格式化
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Data
@Schema(description = "统一设备视图对象")
public class UnifiedDeviceVO {

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", example = "1")
    private Long deviceId;

    /**
     * 设备编号
     */
    @Schema(description = "设备编号", example = "DEV001")
    private String deviceCode;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "门禁设备01")
    private String deviceName;

    /**
     * 设备类型
     */
    @Schema(description = "设备类型", example = "ACCESS")
    private String deviceType;

    /**
     * 设备类型名称
     */
    @Schema(description = "设备类型名称", example = "门禁设备")
    private String deviceTypeName;

    /**
     * 设备型号
     */
    @Schema(description = "设备型号", example = "AC-100")
    private String deviceModel;

    /**
     * 设备厂商
     */
    @Schema(description = "设备厂商", example = "海康威视")
    private String manufacturer;

    /**
     * 设备序列号
     */
    @Schema(description = "设备序列号", example = "SN123456789")
    private String serialNumber;

    /**
     * 设备IP地址
     */
    @Schema(description = "设备IP地址", example = "192.168.1.100")
    private String ipAddress;

    /**
     * 设备端口号
     */
    @Schema(description = "设备端口号", example = "8000")
    private Integer port;

    /**
     * 设备MAC地址
     */
    @Schema(description = "设备MAC地址", example = "00:11:22:33:44:55")
    private String macAddress;

    /**
     * 所属区域ID
     */
    @Schema(description = "所属区域ID", example = "1")
    private Long areaId;

    /**
     * 所属区域名称
     */
    @Schema(description = "所属区域名称", example = "1号楼")
    private String areaName;

    /**
     * 设备位置描述
     */
    @Schema(description = "设备位置描述", example = "1号楼1层大厅")
    private String location;

    /**
     * 在线状态：0-离线，1-在线
     */
    @Schema(description = "在线状态", example = "1")
    private Integer onlineStatus;

    /**
     * 在线状态名称
     */
    @Schema(description = "在线状态名称", example = "在线")
    private String onlineStatusName;

    /**
     * 启用状态：0-禁用，1-启用
     */
    @Schema(description = "启用状态", example = "1")
    private Integer enabled;

    /**
     * 启用状态名称
     */
    @Schema(description = "启用状态名称", example = "启用")
    private String enabledName;

    /**
     * 设备状态
     */
    @Schema(description = "设备状态", example = "ONLINE")
    private String deviceStatus;

    /**
     * 设备状态名称
     */
    @Schema(description = "设备状态名称", example = "在线")
    private String deviceStatusName;

    /**
     * 最后心跳时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "最后心跳时间", example = "2025-01-30 10:00:00")
    private LocalDateTime lastHeartbeat;

    /**
     * 工作模式
     */
    @Schema(description = "工作模式", example = "4")
    private Integer workMode;

    /**
     * 工作模式名称
     */
    @Schema(description = "工作模式名称", example = "混合模式")
    private String workModeName;

    /**
     * 设备描述
     */
    @Schema(description = "设备描述", example = "主要门禁设备")
    private String deviceDescription;

    /**
     * 设备配置信息（JSON格式）
     */
    @Schema(description = "设备配置信息", example = "{}")
    private String deviceConfig;

    /**
     * 扩展属性（JSON格式）
     */
    @Schema(description = "扩展属性", example = "{}")
    private String extendProperties;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间", example = "2025-01-30 10:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间", example = "2025-01-30 10:00:00")
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @Schema(description = "创建人ID", example = "1")
    private Long createUserId;

    /**
     * 创建人姓名
     */
    @Schema(description = "创建人姓名", example = "张三")
    private String createUserName;

    /**
     * 更新人ID
     */
    @Schema(description = "更新人ID", example = "1")
    private Long updateUserId;

    /**
     * 更新人姓名
     */
    @Schema(description = "更新人姓名", example = "李四")
    private String updateUserName;
}
