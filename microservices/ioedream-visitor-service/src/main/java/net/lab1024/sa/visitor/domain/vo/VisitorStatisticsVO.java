package net.lab1024.sa.visitor.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 访客统计VO
 */
@Data
@Schema(description = "访客统计VO")
public class VisitorStatisticsVO {

    @Schema(description = "总数量")
    private Integer totalCount;

    @Schema(description = "待处理数量")
    private Integer pendingCount;

    @Schema(description = "已通过数量")
    private Integer approvedCount;

    @Schema(description = "已拒绝数量")
    private Integer rejectedCount;

    @Schema(description = "已完成数量")
    private Integer completedCount;
}
