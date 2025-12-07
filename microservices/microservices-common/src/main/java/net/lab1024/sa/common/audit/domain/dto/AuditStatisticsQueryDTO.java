package net.lab1024.sa.common.audit.domain.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 审计统计查询DTO
 * <p>
 * 用于审计统计分析的查询条件
 * 严格遵循CLAUDE.md规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 包含完整的统计查询条件
 * - 支持多种统计维度和过滤条件
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Data
@Accessors(chain = true)
public class AuditStatisticsQueryDTO {

    /**
     * 用户ID（统计特定用户的操作）
     */
    private Long userId;

    /**
     * 模块名称（统计特定模块的操作）
     */
    private String moduleName;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 操作类型过滤
     */
    private Integer operationType;

    /**
     * 结果状态过滤
     */
    private Integer resultStatus;

    /**
     * 风险等级过滤
     */
    private Integer riskLevel;

    /**
     * 是否包含分组统计
     */
    private Boolean includeGroupBy;

    /**
     * 统计类型：daily-按天, hourly-按小时, user-按用户, module-按模块
     */
    private String statisticsType;

    /**
     * 是否包含趋势分析
     */
    private Boolean includeTrend;

    /**
     * 是否包含对比分析（与上一周期对比）
     */
    private Boolean includeComparison;

    /**
     * 对比周期类型：week-周对比, month-月对比, year-年对比
     */
    private String comparisonType;

    /**
     * Top数量限制（用于用户活跃度、模块使用量等Top统计）
     */
    private Integer topLimit = 10;

    /**
     * 设置默认统计时间范围（最近7天）
     */
    public void setDefaultTimeRange() {
        if (startTime == null && endTime == null) {
            endTime = LocalDateTime.now();
            startTime = endTime.minusDays(7);
        }
    }

    /**
     * 验证查询参数
     */
    public boolean isValid() {
        // 验证时间范围
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            return false;
        }

        // 验证操作类型
        if (operationType != null && (operationType < 1 || operationType > 10)) {
            return false;
        }

        // 验证结果状态
        if (resultStatus != null && (resultStatus < 1 || resultStatus > 3)) {
            return false;
        }

        // 验证风险等级
        if (riskLevel != null && (riskLevel < 1 || riskLevel > 3)) {
            return false;
        }

        // 验证Top数量
        if (topLimit != null && (topLimit < 1 || topLimit > 100)) {
            return false;
        }

        return true;
    }
}

