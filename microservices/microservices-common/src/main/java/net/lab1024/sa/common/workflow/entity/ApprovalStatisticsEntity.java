package net.lab1024.sa.common.workflow.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 审批统计实体
 * <p>
 * 用于统计审批效率、通过率等指标，比钉钉更完善的统计功能
 * 严格遵循CLAUDE.md规范：
 * - 继承BaseEntity获取审计字段
 * - 使用@TableName指定数据库表名
 * </p>
 * <p>
 * 功能说明：
 * - 统计审批效率（平均审批时长、最长审批时长等）
 * - 统计通过率（通过率、驳回率等）
 * - 统计审批人效率（各审批人的审批效率）
 * - 支持按时间维度统计（日、周、月、年）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_common_approval_statistics")
public class ApprovalStatisticsEntity extends BaseEntity {

    /**
     * 统计ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 业务类型
     */
    @TableField("business_type")
    private String businessType;

    /**
     * 统计日期
     */
    @TableField("statistics_date")
    private LocalDate statisticsDate;

    /**
     * 统计维度
     * <p>
     * DAILY-按日统计
     * WEEKLY-按周统计
     * MONTHLY-按月统计
     * YEARLY-按年统计
     * </p>
     */
    @TableField("statistics_dimension")
    private String statisticsDimension;

    /**
     * 总申请数
     */
    @TableField("total_count")
    private Integer totalCount;

    /**
     * 已通过数
     */
    @TableField("approved_count")
    private Integer approvedCount;

    /**
     * 已驳回数
     */
    @TableField("rejected_count")
    private Integer rejectedCount;

    /**
     * 进行中数
     */
    @TableField("processing_count")
    private Integer processingCount;

    /**
     * 已撤销数
     */
    @TableField("withdrawn_count")
    private Integer withdrawnCount;

    /**
     * 通过率（百分比，0-100）
     */
    @TableField("approval_rate")
    private BigDecimal approvalRate;

    /**
     * 平均审批时长（小时）
     */
    @TableField("avg_approval_hours")
    private BigDecimal avgApprovalHours;

    /**
     * 最长审批时长（小时）
     */
    @TableField("max_approval_hours")
    private BigDecimal maxApprovalHours;

    /**
     * 最短审批时长（小时）
     */
    @TableField("min_approval_hours")
    private BigDecimal minApprovalHours;

    /**
     * 平均节点数
     */
    @TableField("avg_node_count")
    private BigDecimal avgNodeCount;

    /**
     * 超时审批数
     */
    @TableField("timeout_count")
    private Integer timeoutCount;

    /**
     * 超时率（百分比，0-100）
     */
    @TableField("timeout_rate")
    private BigDecimal timeoutRate;

    /**
     * 审批人统计（JSON格式）
     * <p>
     * 示例：
     * {
     *   "approver_stats": [
     *     {
     *       "approver_id": 1,
     *       "approver_name": "张三",
     *       "approval_count": 10,
     *       "avg_approval_hours": 2.5,
     *       "timeout_count": 1
     *     }
     *   ]
     * }
     * </p>
     */
    @TableField("approver_statistics")
    private String approverStatistics;

    /**
     * 部门统计（JSON格式）
     * <p>
     * 按部门统计审批数据
     * </p>
     */
    @TableField("department_statistics")
    private String departmentStatistics;

    /**
     * 统计时间
     */
    @TableField("statistics_time")
    private LocalDateTime statisticsTime;
}

