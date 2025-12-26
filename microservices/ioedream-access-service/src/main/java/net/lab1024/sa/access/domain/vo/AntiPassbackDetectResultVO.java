package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 反潜回检测结果VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "反潜回检测结果")
public class AntiPassbackDetectResultVO {

    /**
     * 检测结果
     * 1-正常通行
     * 2-软反潜回（告警但允许）
     * 3-硬反潜回（阻止通行）
     */
    @Schema(description = "检测结果", example = "1")
    private Integer result;

    /**
     * 结果描述
     */
    @Schema(description = "结果描述", example = "正常通行")
    private String resultMessage;

    /**
     * 是否允许通行
     */
    @Schema(description = "是否允许通行", example = "true")
    private Boolean allowPass;

    /**
     * 违规类型
     * 1-时间窗口内重复
     * 2-跨区域异常
     * 3-频次超限
     */
    @Schema(description = "违规类型", example = "1")
    private Integer violationType;

    /**
     * 违规描述
     */
    @Schema(description = "违规描述")
    private String violationMessage;

    /**
     * 检测耗时（毫秒）
     */
    @Schema(description = "检测耗时（毫秒）", example = "45")
    private Long detectionTime;

    /**
     * 检测记录ID
     */
    @Schema(description = "检测记录ID", example = "1001")
    private Long recordId;

    /**
     * 触发的配置ID
     */
    @Schema(description = "配置ID", example = "1")
    private Long configId;

    /**
     * 最近通行记录（用于前端展示）
     */
    @Schema(description = "最近通行记录")
    private RecentPassInfo recentPass;

    /**
     * 最近通行信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "最近通行信息")
    public static class RecentPassInfo {
        @Schema(description = "通行时间", example = "1706584800000")
        private Long passTime;

        @Schema(description = "设备名称", example = "A栋1楼门禁")
        private String deviceName;

        @Schema(description = "区域名称", example = "A栋1楼大厅")
        private String areaName;

        @Schema(description = "距本次通行时间（秒）", example = "120")
        private Long secondsAgo;
    }
}
