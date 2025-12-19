package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 移动端排行榜结果
 * <p>
 * 封装移动端排行榜响应结果
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
@Schema(description = "移动端排行榜结果")
public class MobileLeaderboardResult {

    /**
     * 员工ID
     */
    @Schema(description = "员工ID", example = "12345")
    private Long employeeId;

    /**
     * 排行榜类型
     */
    @Schema(description = "排行榜类型", example = "ATTENDANCE")
    private String leaderboardType;

    /**
     * 排行榜列表
     */
    @Schema(description = "排行榜列表")
    private List<Map<String, Object>> leaderboard;

    /**
     * 排行榜列表（兼容字段）
     */
    @Schema(description = "排行榜列表")
    private List<Object> rankings;

    /**
     * 当前用户排名
     */
    @Schema(description = "当前用户排名", example = "10")
    private Integer userRank;
}

