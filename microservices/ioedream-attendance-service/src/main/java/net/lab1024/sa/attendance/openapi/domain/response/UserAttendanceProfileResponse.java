package net.lab1024.sa.attendance.openapi.domain.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户考勤画像响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户考勤画像响应")
public class UserAttendanceProfileResponse {

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "统计周期（天）", example = "30")
    private Integer days;

    @Schema(description = "出勤率（0-1）", example = "0.98")
    private Double attendanceRate;

    @Schema(description = "准时率（0-1）", example = "0.95")
    private Double punctualityRate;

    @Schema(description = "异常次数", example = "2")
    private Integer abnormalCount;

    @Schema(description = "补卡次数", example = "1")
    private Integer supplementCount;

    @Schema(description = "画像标签")
    private List<String> tags;
}
