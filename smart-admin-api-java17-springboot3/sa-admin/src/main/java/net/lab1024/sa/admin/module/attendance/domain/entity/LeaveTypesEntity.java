package net.lab1024.sa.admin.module.attendance.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import net.lab1024.sa.base.common.entity.BaseEntity;

import java.math.BigDecimal;

/**
 * 假种表实体
 *
 * 假种管理的核心实体，支持各种假期类型的配置和管理
 *
 * @author IOE-DREAM Team
 * @since 2025-11-25
 */
@Data
@TableName("t_leave_types")
public class LeaveTypesEntity extends BaseEntity {

    /**
     * 假种ID
     */
    private Long leaveTypeId;

    /**
     * 假种编码
     */
    @TableField("`leave_type_code`")
    private String leaveTypeCode;

    /**
     * 假种名称
     */
    @TableField("`leave_type_name`")
    private String leaveTypeName;

    /**
     * 假期分类: LEGAL-法定假期 WELFARE-福利假期 SICK-病假 PERSONAL-事假
     */
    @TableField("`leave_category`")
    private String leaveCategory;

    /**
     * 每年最大天数
     */
    @TableField("`max_days_per_year`")
    private Integer maxDaysPerYear;

    /**
     * 每次最大天数
     */
    @TableField("`max_days_per_time`")
    private Integer maxDaysPerTime;

    /**
     * 最少提前申请天数
     */
    @TableField("`min_advance_days`")
    private Integer minAdvanceDays;

    /**
     * 是否需要证明
     */
    @TableField("`require_proof`")
    private Boolean requireProof;

    /**
     * 工资扣除比例 0.0000-1.0000
     */
    @TableField("`salary_deduction_rate`")
    private BigDecimal salaryDeductionRate;

    /**
     * 审批流程配置 JSON格式
     * {"levels":3,"approvers":[{"level":1,"role":"manager"},{"level":2,"role":"hr"}]}
     */
    @TableField("`approval_flow`")
    private String approvalFlow;

    /**
     * 适用条件 JSON格式
     * {"minServiceMonths":3,"maxAge":60,"departments":[1,2,3]}
     */
    @TableField("`applicable_conditions`")
    private String applicableConditions;

    /**
     * 假种描述
     */
    @TableField("`description`")
    private String description;

    /**
     * 排序
     */
    @TableField("`sort_order`")
    private Integer sortOrder;

    /**
     * 状态 1-启用 0-禁用
     */
    @TableField("`status`")
    private Boolean status;

    /**
     * 假期分类描述（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String leaveCategoryDesc;

    /**
     * 是否带薪假期（查询时填充，不存储）
     */
    @TableField(exist = false)
    private Boolean isPaidLeave;

    /**
     * 审批流程描述（查询时填充，不存储）
     */
    @TableField(exist = false)
    private String approvalFlowDesc;

    /**
     * 剩余可用天数（查询时填充，不存储）
     */
    @TableField(exist = false)
    private Integer remainingDays;

    /**
     * 是否需要提前申请（查询时填充，不存储）
     */
    @TableField(exist = false)
    private Boolean needAdvanceApplication;
}