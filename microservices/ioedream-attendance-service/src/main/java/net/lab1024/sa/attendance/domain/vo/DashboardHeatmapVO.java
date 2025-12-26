package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 部门热力图数据视图对象
 * <p>
 * 用于部门考勤热力图展示
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "部门热力图数据视图对象")
public class DashboardHeatmapVO {

    /**
     * 热力图类型：DEPARTMENT-部门 ATTENDANCE_RATE-出勤率 WORK_HOURS-工作时长
     */
    @Schema(description = "热力图类型", example = "DEPARTMENT")
    private String heatmapType;

    /**
     * 统计日期
     */
    @Schema(description = "统计日期", example = "2025-12-23")
    private String statisticsDate;

    /**
     * 热力图数据
     */
    @Schema(description = "热力图数据")
    private List<HeatmapItem> heatmapData;

    /**
     * 最小值
     */
    @Schema(description = "最小值", example = "85.0")
    private BigDecimal minValue;

    /**
     * 最大值
     */
    @Schema(description = "最大值", example = "98.0")
    private BigDecimal maxValue;

    /**
     * 平均值
     */
    @Schema(description = "平均值", example = "92.5")
    private BigDecimal averageValue;

    /**
     * 热力图数据项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "热力图数据项")
    public static class HeatmapItem {

        /**
         * 部门ID
         */
        @Schema(description = "部门ID", example = "10")
        private Long departmentId;

        /**
         * 部门名称
         */
        @Schema(description = "部门名称", example = "技术部")
        private String departmentName;

        /**
         * 数值（出勤率/工作时长等）
         */
        @Schema(description = "数值", example = "95.5")
        private BigDecimal value;

        /**
         * 热度级别（1-5）
         */
        @Schema(description = "热度级别", example = "5")
        private Integer heatLevel;

        /**
         * 员工数量
         */
        @Schema(description = "员工数量", example = "50")
        private Integer employeeCount;

        /**
         * 额外信息
         */
        @Schema(description = "额外信息")
        private Map<String, Object> extraInfo;
    }
}
