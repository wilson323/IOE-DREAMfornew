package net.lab1024.sa.device.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能设备视图对象 - 微服务版本
 * <p>
 * 严格遵循repowiki规范：
 * - 统一VO类设计模式
 * - 完整的字段注释和说明
 * - 支持序列化和API响应
 * - 敏感信息过滤
 *
 * @author IOE-DREAM Team
 * @date 2025-11-29
 */
@Data
@Schema(description = "智能设备视图对象")
public class SmartDeviceVO {

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "设备编码")
    private String deviceCode;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "设备类型描述")
    private String deviceTypeDesc;

    @Schema(description = "设备状态")
    private String deviceStatus;

    @Schema(description = "设备状态描述")
    private String deviceStatusDesc;

    @Schema(description = "设备IP地址")
    private String ipAddress;

    @Schema(description = "设备端口")
    private Integer port;

    @Schema(description = "协议类型")
    private String protocolType;

    @Schema(description = "设备位置")
    private String location;

    @Schema(description = "设备描述")
    private String description;

    @Schema(description = "制造商")
    private String manufacturer;

    @Schema(description = "设备型号")
    private String deviceModel;

    @Schema(description = "固件版本")
    private String firmwareVersion;

    @Schema(description = "安装日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime installDate;

    @Schema(description = "最后在线时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "分组ID")
    private Long groupId;

    @Schema(description = "分组名称")
    private String groupName;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "是否在线")
    private Boolean online;

    @Schema(description = "在线时长(分钟)")
    private Long onlineDuration;

    @Schema(description = "运行状态")
    private String runningStatus;

    @Schema(description = "健康评分(0-100)")
    private Integer healthScore;
}