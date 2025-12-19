package net.lab1024.sa.attendance.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 补卡申请查询请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@Schema(description = "补卡申请查询请求")
public class SupplementApplicationQueryRequest {

    @Schema(description = "页码", example = "1")
    private Integer pageNum;

    @Schema(description = "页大小", example = "20")
    private Integer pageSize;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "申请状态", example = "pending", allowableValues = { "pending", "approved", "rejected",
            "cancelled" })
    private String applicationStatus;

    @Schema(description = "开始日期", example = "2025-12-01")
    private String startDate;

    @Schema(description = "结束日期", example = "2025-12-31")
    private String endDate;
}
