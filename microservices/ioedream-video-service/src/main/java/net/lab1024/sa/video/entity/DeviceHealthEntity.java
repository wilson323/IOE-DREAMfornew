package net.lab1024.sa.video.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备健康检查实体
 * <p>
 * 记录视频设备健康检查信息，包括：
 * - 设备健康度评分
 * - 健康指标数据（CPU、内存、网络、存储等）
 * - 健康状态判断
 * - 告警记录
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_video_device_health")
@Schema(description = "设备健康检查实体")
public class DeviceHealthEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "健康检查ID", example = "1")
    private Long healthId;

    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    @Schema(description = "设备编号", example = "CAM001")
    private String deviceCode;

    @Schema(description = "设备名称", example = "1楼大厅摄像头")
    private String deviceName;

    @Schema(description = "健康度评分（0-100）", example = "85")
    private Integer healthScore;

    @Schema(description = "健康状态：1-健康 2-亚健康 3-不健康 4-离线", example = "1")
    private Integer healthStatus;

    @Schema(description = "CPU使用率（%）", example = "45.5")
    private BigDecimal cpuUsage;

    @Schema(description = "内存使用率（%）", example = "62.3")
    private BigDecimal memoryUsage;

    @Schema(description = "磁盘使用率（%）", example = "78.5")
    private BigDecimal diskUsage;

    @Schema(description = "网络延迟（ms）", example = "25")
    private Integer networkLatency;

    @Schema(description = "网络丢包率（%）", example = "0.1")
    private BigDecimal packetLoss;

    @Schema(description = "帧率（fps）", example = "25")
    private Integer frameRate;

    @Schema(description = "码率（Kbps）", example = "4096")
    private Integer bitRate;

    @Schema(description = "运行时间（小时）", example = "720")
    private Long uptime;

    @Schema(description = "温度（℃）", example = "45")
    private Integer temperature;

    @Schema(description = "告警级别：0-正常 1-提示 2-警告 3-严重", example = "0")
    private Integer alarmLevel;

    @Schema(description = "告警信息")
    private String alarmMessage;

    @Schema(description = "检查时间", example = "2025-12-26T10:00:00")
    private LocalDateTime checkTime;

    @Schema(description = "创建时间", example = "2025-12-26T10:00:00")
    private LocalDateTime createTime;
}
