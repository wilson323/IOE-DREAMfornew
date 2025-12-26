package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 移动端考勤状态查询请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "移动端考勤状态查询请求")
public class MobileAttendanceStatusRequest {

    @Schema(description = "用户ID", required = true, example = "1001")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "查询日期", required = true, example = "2025-12-16")
    @NotNull(message = "查询日期不能为空")
    private LocalDate queryDate;

    @Schema(description = "是否包含详情", example = "true")
    private Boolean includeDetails;

    @Schema(description = "是否包含统计", example = "true")
    private Boolean includeStatistics;

    @Schema(description = "设备ID", example = "MOBILE_001")
    private String deviceId;

    @Schema(description = "扩展属性")
    private java.util.Map<String, Object> extendedAttributes;
}
