package net.lab1024.sa.attendance.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 考勤统计查询表单
 * <p>
 * 用于考勤统计数据查询请求参数
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
@Schema(description = "考勤统计查询表单")
public class AttendanceStatisticsQueryForm {

    /**
     * 开始日期
     */
    @Schema(description = "开始日期", example = "2024-01-01")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @Schema(description = "结束日期", example = "2024-01-31")
    private LocalDate endDate;

    /**
     * 员工ID（查询个人统计时使用）
     */
    @Schema(description = "员工ID", example = "1001")
    private Long employeeId;

    /**
     * 部门ID（查询部门统计时使用）
     */
    @Schema(description = "部门ID", example = "10")
    private Long departmentId;

    /**
     * 统计类型：PERSONAL-个人 DEPARTMENT-部门 COMPANY-公司
     */
    @Schema(description = "统计类型", example = "DEPARTMENT")
    private String statisticsType;
}
