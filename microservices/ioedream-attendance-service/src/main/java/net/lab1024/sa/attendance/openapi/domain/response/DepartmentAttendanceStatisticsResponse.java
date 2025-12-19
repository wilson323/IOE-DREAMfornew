package net.lab1024.sa.attendance.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 部门考勤统计响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "部门考勤统计响应")
public class DepartmentAttendanceStatisticsResponse {

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "开始日期", example = "2025-12-01")
    private String startDate;

    @Schema(description = "结束日期", example = "2025-12-31")
    private String endDate;

    @Schema(description = "统计类型", example = "SUMMARY")
    private String statisticsType;

    @Schema(description = "部门人数", example = "32")
    private Integer employeeCount;

    @Schema(description = "异常总数", example = "5")
    private Integer abnormalTotal;

    @Schema(description = "统计明细")
    private List<DepartmentEmployeeStatistics> employeeStatistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "部门员工统计")
    public static class DepartmentEmployeeStatistics {

        @Schema(description = "用户ID", example = "1001")
        private Long userId;

        @Schema(description = "用户名", example = "admin")
        private String username;

        @Schema(description = "真实姓名", example = "系统管理员")
        private String realName;

        @Schema(description = "应出勤天数", example = "22")
        private Integer shouldAttendDays;

        @Schema(description = "实际出勤天数", example = "22")
        private Integer actualAttendDays;

        @Schema(description = "迟到次数", example = "0")
        private Integer lateCount;

        @Schema(description = "早退次数", example = "0")
        private Integer earlyLeaveCount;

        @Schema(description = "缺勤次数", example = "0")
        private Integer absentCount;

        @Schema(description = "异常次数", example = "1")
        private Integer abnormalCount;
    }
}

