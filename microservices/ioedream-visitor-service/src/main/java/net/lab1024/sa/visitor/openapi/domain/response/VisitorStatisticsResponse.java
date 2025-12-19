package net.lab1024.sa.visitor.openapi.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 访客统计响应
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@Schema(description = "访客统计响应")
public class VisitorStatisticsResponse {

    @Schema(description = "总访客数")
    private Long totalVisitors;

    @Schema(description = "今日访客数")
    private Long todayVisitors;

    @Schema(description = "本周访客数")
    private Long weeklyVisitors;

    @Schema(description = "本月访客数")
    private Long monthlyVisitors;

    @Schema(description = "待审批数")
    private Long pendingApprovals;

    @Schema(description = "黑名单数")
    private Long blacklistCount;
}