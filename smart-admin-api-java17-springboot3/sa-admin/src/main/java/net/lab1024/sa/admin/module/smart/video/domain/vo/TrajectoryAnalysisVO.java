package net.lab1024.sa.admin.module.smart.video.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 轨迹分析VO
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Data
@Schema(description = "轨迹分析信息")
public class TrajectoryAnalysisVO {

    @Schema(description = "分析ID")
    private Long analysisId;

    @Schema(description = "人员ID")
    private Long personId;

    @Schema(description = "人员姓名")
    private String personName;

    @Schema(description = "轨迹点列表")
    private List<TrajectoryPoint> trajectoryPoints;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "总距离(米)")
    private Double totalDistance;

    @Schema(description = "平均速度(米/秒)")
    private Double averageSpeed;

    @Schema(description = "热力图数据")
    private String heatmapData;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    // 添加Controller需要的字段
    @Schema(description = "处理时间(毫秒)")
    private Long processingTime;

    /**
     * 轨迹点
     */
    @Data
    @Schema(description = "轨迹点信息")
    public static class TrajectoryPoint {
        @Schema(description = "设备ID")
        private Long deviceId;

        @Schema(description = "设备名称")
        private String deviceName;

        @Schema(description = "X坐标")
        private Double x;

        @Schema(description = "Y坐标")
        private Double y;

        @Schema(description = "检测时间")
        private LocalDateTime detectionTime;

        @Schema(description = "截图路径")
        private String snapshotPath;
    }
}