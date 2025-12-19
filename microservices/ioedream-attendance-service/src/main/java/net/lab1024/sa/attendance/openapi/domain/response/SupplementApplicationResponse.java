package net.lab1024.sa.attendance.openapi.domain.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 补卡申请响应
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "补卡申请响应")
public class SupplementApplicationResponse {

    @Schema(description = "申请ID", example = "50001")
    private Long applicationId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "补卡日期", example = "2025-12-16")
    private String supplementDate;

    @Schema(description = "打卡类型", example = "on")
    private String clockType;

    @Schema(description = "期望补卡时间", example = "2025-12-16T09:00:00")
    private LocalDateTime supplementTime;

    @Schema(description = "申请原因", example = "出差")
    private String reason;

    @Schema(description = "申请状态", example = "pending", allowableValues = { "pending", "approved", "rejected",
            "cancelled" })
    private String applicationStatus;

    @Schema(description = "审批意见", example = "同意")
    private String approveComment;

    @Schema(description = "审批时间", example = "2025-12-16T11:00:00")
    private LocalDateTime approveTime;

    @Schema(description = "创建时间", example = "2025-12-16T09:30:00")
    private LocalDateTime createTime;
}
