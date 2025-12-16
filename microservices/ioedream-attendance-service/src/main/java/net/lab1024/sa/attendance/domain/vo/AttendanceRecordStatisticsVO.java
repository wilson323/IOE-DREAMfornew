package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 考勤记录统计视图对象
 * <p>
 * 用于返回考勤记录统计数据
 * 严格遵循CLAUDE.md规范：
 * - 使用VO后缀命名
 * - 包含统计字段
 * - 使用@Schema注解描述字段
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "考勤记录统计视图对象")
public class AttendanceRecordStatisticsVO {

    @Schema(description = "总打卡次数", example = "100")
    private Long totalCount;

    @Schema(description = "正常打卡次数", example = "80")
    private Long normalCount;

    @Schema(description = "迟到次数", example = "10")
    private Long lateCount;

    @Schema(description = "早退次数", example = "5")
    private Long earlyCount;

    @Schema(description = "缺勤次数", example = "3")
    private Long absentCount;

    @Schema(description = "加班次数", example = "2")
    private Long overtimeCount;

    @Schema(description = "正常率（百分比）", example = "80.0")
    private Double normalRate;

    @Schema(description = "迟到率（百分比）", example = "10.0")
    private Double lateRate;

    @Schema(description = "早退率（百分比）", example = "5.0")
    private Double earlyRate;

    @Schema(description = "缺勤率（百分比）", example = "3.0")
    private Double absentRate;
}



