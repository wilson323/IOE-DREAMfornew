package net.lab1024.sa.attendance.openapi.domain.response;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 月度考勤统计响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "月度考勤统计响应")
public class MonthlyAttendanceStatisticsResponse {

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "年月（YYYY-MM）", example = "2025-12")
    private String yearMonth;

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

    @Schema(description = "补卡次数", example = "1")
    private Integer supplementCount;

    @Schema(description = "请假时长（小时）", example = "8.0")
    private BigDecimal leaveHours;

    @Schema(description = "加班时长（小时）", example = "6.5")
    private BigDecimal overtimeHours;

    @Schema(description = "异常记录列表")
    private List<AbnormalAttendanceResponse> abnormalRecords;
}
