package net.lab1024.sa.admin.module.device.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备健康总览VO
 *
 * @author SmartAdmin Team
 * @date 2025-11-25
 */
@Data
@Schema(description = "设备健康总览")
public class DeviceHealthOverviewVO {

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备序列号")
    private String deviceSn;

    @Schema(description = "设备状态")
    private Integer deviceStatus;

    @Schema(description = "设备状态描述")
    private String deviceStatusText;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "当前健康评分")
    private BigDecimal currentHealthScore;

    @Schema(description = "健康等级")
    private String healthLevel;

    @Schema(description = "健康等级描述")
    private String healthLevelText;

    @Schema(description = "健康评分颜色")
    private String healthScoreColor;

    @Schema(description = "最后健康检查时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastHealthCheck;

    @Schema(description = "未解决告警数量")
    private Integer unresolvedAlerts;

    @Schema(description = "待处理维护数量")
    private Integer pendingMaintenance;

    @Schema(description = "进行中维护数量")
    private Integer inProgressMaintenance;

    @Schema(description = "在线状态")
    private Boolean isOnline;

    @Schema(description = "设备位置")
    private String location;
}