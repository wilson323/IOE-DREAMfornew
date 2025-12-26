package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 考勤报表视图对象
 * <p>
 * 用于考勤报表响应数据
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
@Schema(description = "考勤报表视图对象")
public class AttendanceReportVO {

    /**
     * 员工ID
     */
    @Schema(description = "员工ID", example = "1001")
    private Long employeeId;

    /**
     * 员工姓名
     */
    @Schema(description = "员工姓名", example = "张三")
    private String employeeName;

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
     * 报表日期
     */
    @Schema(description = "报表日期", example = "2024-01-01")
    private LocalDate reportDate;

    /**
     * 出勤天数
     */
    @Schema(description = "出勤天数", example = "22")
    private Integer attendanceDays;

    /**
     * 缺勤天数
     */
    @Schema(description = "缺勤天数", example = "0")
    private Integer absenceDays;

    /**
     * 迟到次数
     */
    @Schema(description = "迟到次数", example = "2")
    private Integer lateCount;

    /**
     * 早退次数
     */
    @Schema(description = "早退次数", example = "1")
    private Integer earlyLeaveCount;

    /**
     * 加班时长（小时）
     */
    @Schema(description = "加班时长（小时）", example = "8.5")
    private BigDecimal overtimeHours;

    /**
     * 工作总时长（小时）
     */
    @Schema(description = "工作总时长（小时）", example = "176")
    private BigDecimal totalWorkHours;
}
