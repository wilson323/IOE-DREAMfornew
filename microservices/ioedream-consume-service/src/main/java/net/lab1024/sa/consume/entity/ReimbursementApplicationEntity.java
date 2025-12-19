package net.lab1024.sa.consume.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 报销申请实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_reimbursement_application")
@Schema(description = "报销申请实体")
public class ReimbursementApplicationEntity extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
    /**
     * 申请ID
     */
    @TableField("application_id")
    @Schema(description = "申请ID")
    private Long applicationId;
    /**
     * 申请编号
     */
    @TableField("application_no")
    @Schema(description = "申请编号")
    private String applicationNo;
    /**
     * 用户ID
     */
    @TableField("user_id")
    @Schema(description = "用户ID")
    private Long userId;
    /**
     * 总金额
     */
    @TableField("total_amount")
    @Schema(description = "总金额")
    private java.math.BigDecimal totalAmount;
    /**
     * 报销类型
     */
    @TableField("reimbursement_type")
    @Schema(description = "报销类型")
    private String reimbursementType;
    /**
     * 申请状态
     */
    @TableField("status")
    @Schema(description = "申请状态")
    private String status;

    /**
     * 报销事由
     * <p>
     * 由申请人填写，用于审批与入账说明
     * </p>
     */
    @TableField("reason")
    @Schema(description = "报销事由")
    private String reason;
    /**
     * 备注
     */
    @TableField("remark")
    @Schema(description = "备注")
    private String remark;

    /**
     * 报销单号
     * <p>
     * 根据编译错误日志添加，用于业务查询和流程关联
     * </p>
     */
    @TableField("reimbursement_no")
    @Schema(description = "报销单号")
    private String reimbursementNo;

    /**
     * 报销金额
     * <p>
     * 根据编译错误日志添加，用于报销金额查询
     * 与totalAmount保持一致，提供兼容性方法
     * </p>
     */
    @TableField("reimbursement_amount")
    @Schema(description = "报销金额")
    private BigDecimal reimbursementAmount;

    /**
     * 工作流实例ID
     * <p>
     * 根据编译错误日志添加，关联OA工作流实例
     * </p>
     */
    @TableField("workflow_instance_id")
    @Schema(description = "工作流实例ID")
    private Long workflowInstanceId;

    /**
     * 审核意见
     * <p>
     * 根据编译错误日志添加，存储审批意见
     * </p>
     */
    @TableField("approval_comment")
    @Schema(description = "审核意见")
    private String approvalComment;

    /**
     * 审核时间
     * <p>
     * 根据编译错误日志添加，记录审批时间
     * </p>
     */
    @TableField("approval_time")
    @Schema(description = "审核时间")
    private LocalDateTime approvalTime;

    // 注意：createTime, updateTime, createUserId, updateUserId, deletedFlag, version
    // 已由BaseEntity提供，无需重复定义

    // 兼容性方法
    /**
     * 获取报销单号
     * <p>
     * 优先使用reimbursementNo字段，否则使用applicationNo
     * </p>
     */
    public String getReimbursementNo() {
        return this.reimbursementNo != null ? this.reimbursementNo : this.applicationNo;
    }

    /**
     * 设置报销单号
     */
    public void setReimbursementNo(String reimbursementNo) {
        this.reimbursementNo = reimbursementNo;
        // 同时设置applicationNo以保持一致性
        if (this.applicationNo == null) {
            this.applicationNo = reimbursementNo;
        }
    }

    /**
     * 获取报销金额
     * <p>
     * 优先使用reimbursementAmount字段，否则使用totalAmount
     * </p>
     */
    public BigDecimal getReimbursementAmount() {
        return this.reimbursementAmount != null ? this.reimbursementAmount : this.totalAmount;
    }

    /**
     * 设置报销金额
     */
    public void setReimbursementAmount(BigDecimal reimbursementAmount) {
        this.reimbursementAmount = reimbursementAmount;
        // 同时设置totalAmount以保持一致性
        if (this.totalAmount == null) {
            this.totalAmount = reimbursementAmount;
        }
    }
}
