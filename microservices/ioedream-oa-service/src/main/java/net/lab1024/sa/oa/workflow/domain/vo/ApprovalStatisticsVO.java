package net.lab1024.sa.oa.workflow.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

/**
 * 审批统计视图对象
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - VO类用于数据传输，不包含业务逻辑
 * - 字段命名清晰，符合业务语义
 * - 完整的字段注释
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
public class ApprovalStatisticsVO {

    /**
     * 统计日期
     */
    private LocalDate statisticsDate;

    /**
     * 统计维度（DAY/WEEK/MONTH/YEAR）
     */
    private String statisticsDimension;

    /**
     * 总申请数
     */
    private Long totalCount;

    /**
     * 待办数量
     */
    private Long todoCount;

    /**
     * 已完成数量
     */
    private Long completedCount;

    /**
     * 已通过数
     */
    private Long approvedCount;

    /**
     * 已驳回数
     */
    private Long rejectedCount;

    /**
     * 进行中数
     */
    private Long processingCount;

    /**
     * 已撤销数
     */
    private Long withdrawnCount;

    /**
     * 转办数量
     */
    private Long transferCount;

    /**
     * 委派数量
     */
    private Long delegateCount;

    /**
     * 超时数量
     */
    private Long overdueCount;

    /**
     * 通过率（百分比）
     */
    private BigDecimal approvalRate;

    /**
     * 驳回率（百分比）
     */
    private BigDecimal rejectionRate;

    /**
     * 平均审批时长（小时）
     */
    private BigDecimal avgDuration;

    /**
     * 最长审批时长（小时）
     */
    private BigDecimal maxDuration;

    /**
     * 最短审批时长（小时）
     */
    private BigDecimal minDuration;
}






