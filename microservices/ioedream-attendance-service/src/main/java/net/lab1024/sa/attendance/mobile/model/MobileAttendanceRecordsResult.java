package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 移动端考勤记录列表结果
 * <p>
 * 封装移动端考勤记录列表响应结果
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "移动端考勤记录列表结果")
public class MobileAttendanceRecordsResult {

    /**
     * 员工ID
     */
    @Schema(description = "员工ID", example = "1001")
    private Long employeeId;

    /**
     * 考勤记录列表
     */
    @Schema(description = "考勤记录列表")
    private List<MobileAttendanceRecord> records;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数", example = "100")
    private Integer totalCount;
}
