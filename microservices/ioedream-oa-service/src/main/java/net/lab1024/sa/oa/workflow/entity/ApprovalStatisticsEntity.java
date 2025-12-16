package net.lab1024.sa.oa.workflow.entity;

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
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_common_approval_statistics")
public class ApprovalStatisticsEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("business_type")
    private String businessType;

    @TableField("statistics_date")
    private LocalDate statisticsDate;

    @TableField("statistics_dimension")
    private String statisticsDimension;

    @TableField("total_count")
    private Integer totalCount;

    @TableField("approved_count")
    private Integer approvedCount;

    @TableField("rejected_count")
    private Integer rejectedCount;

    @TableField("processing_count")
    private Integer processingCount;

    @TableField("withdrawn_count")
    private Integer withdrawnCount;

    @TableField("approval_rate")
    private BigDecimal approvalRate;

    @TableField("avg_approval_hours")
    private BigDecimal avgApprovalHours;

    @TableField("max_approval_hours")
    private BigDecimal maxApprovalHours;

    @TableField("min_approval_hours")
    private BigDecimal minApprovalHours;

    @TableField("avg_node_count")
    private BigDecimal avgNodeCount;

    @TableField("timeout_count")
    private Integer timeoutCount;

    @TableField("timeout_rate")
    private BigDecimal timeoutRate;

    @TableField("approver_statistics")
    private String approverStatistics;

    @TableField("department_statistics")
    private String departmentStatistics;

    @TableField("statistics_time")
    private LocalDateTime statisticsTime;
}




