package net.lab1024.sa.attendance.domain.form;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 考勤记录查询表单
 * <p>
 * 用于考勤记录分页查询条件
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 包含查询条件字段
 * - 使用@Schema注解描述字段
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "考勤记录查询表单")
public class AttendanceRecordQueryForm {

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "员工ID", example = "1")
    private Long employeeId;

    @Schema(description = "开始日期", example = "2025-01-01")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2025-01-31")
    private LocalDate endDate;

    @Schema(description = "考勤状态：NORMAL-正常，LATE-迟到，EARLY-早退，ABSENT-缺勤，OVERTIME-加班", example = "NORMAL")
    private String status;

    @Schema(description = "考勤类型：CHECK_IN-上班打卡，CHECK_OUT-下班打卡", example = "CHECK_IN")
    private String attendanceType;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;
}



