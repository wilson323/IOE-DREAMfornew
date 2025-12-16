package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 排班统计信息视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "排班统计信息视图对象")
public class SchedulingStatisticsVO {

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    @Schema(description = "统计期间", example = "2025-01-01~2025-01-31")
    private String period;

    @Schema(description = "预测期间", example = "2025-02-01~2025-02-28")
    private String forecastPeriod;

    @Schema(description = "员工总数", example = "50")
    private Integer totalEmployees;

    @Schema(description = "排班员工数", example = "48")
    private Integer scheduledEmployees;

    @Schema(description = "班次总数", example = "8")
    private Integer totalShifts;

    @Schema(description = "排班记录总数", example = "1200")
    private Integer totalSchedules;

    @Schema(description = "正常排班数", example = "1150")
    private Integer normalSchedules;

    @Schema(description = "临时排班数", example = "50")
    private Integer temporarySchedules;

    @Schema(description = "活跃排班数", example = "1180")
    private Integer activeSchedules;

    @Schema(description = "总工作时长", example = "9600.0")
    private Double totalWorkHours;

    @Schema(description = "平均工作时长/员工", example = "200.0")
    private Double averageWorkHoursPerEmployee;

    @Schema(description = "排班利用率", example = "95.8")
    private Double scheduleUtilization;

    @Schema(description = "预测工作时长", example = "0.0")
    private Double predictedWorkHours;

    @Schema(description = "置信度", example = "0.0")
    private Double confidenceLevel;

    @Schema(description = "成本估算", example = "240000.0")
    private Double estimatedCost;

    @Schema(description = "效率评分", example = "88.5")
    private Double efficiencyScore;

    @Schema(description = "公平性评分", example = "92.1")
    private Double fairnessScore;

    @Schema(description = "合规性评分", example = "95.0")
    private Double complianceScore;

    @Schema(description = "综合评分", example = "91.9")
    private Double overallScore;

    @Schema(description = "统计时间", example = "2025-01-01 10:00:00")
    private LocalDateTime statisticsTime;

    @Schema(description = "备注", example = "统计数据正常")
    private String remark;
}