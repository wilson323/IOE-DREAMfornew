package net.lab1024.sa.consume.entity;

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
 * 退款申请实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_refund_application")
@Schema(description = "退款申请实体")
public class RefundApplicationEntity extends BaseEntity {

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
     * 消费记录ID
     */
    @TableField("consume_record_id")
    @Schema(description = "消费记录ID")
    private Long consumeRecordId;
    /**
     * 退款金额
     */
    @TableField("refund_amount")
    @Schema(description = "退款金额")
    private java.math.BigDecimal refundAmount;
    /**
     * 退款原因
     */
    @TableField("refund_reason")
    @Schema(description = "退款原因")
    private String refundReason;
    /**
     * 申请状态
     */
    @TableField("status")
    @Schema(description = "申请状态")
    private String status;

    /**
     * 退款单号
     */
    @TableField("refund_no")
    @Schema(description = "退款单号")
    private String refundNo;

    /**
     * 支付记录ID
     */
    @TableField("payment_record_id")
    @Schema(description = "支付记录ID")
    private Long paymentRecordId;

    /**
     * 工作流实例ID
     * <p>
     * 关联OA审批流程实例，用于追踪审批状态
     * </p>
     */
    @TableField("workflow_instance_id")
    @Schema(description = "工作流实例ID")
    private Long workflowInstanceId;

    /**
     * 退款时间
     */
    @TableField("refund_time")
    @Schema(description = "退款时间")
    private java.time.LocalDateTime refundTime;

    // 缺失字段 - 根据错误日志添加
    /**
     * 用户ID (String类型兼容)
     */
    @TableField("user_id_str")
    @Schema(description = "用户ID(String)")
    private String userIdStr;

    /**
     * 用户姓名
     */
    @TableField("user_name")
    @Schema(description = "用户姓名")
    private String userName;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    @Schema(description = "联系电话")
    private String contactPhone;

    /**
     * 邮箱地址
     */
    @TableField("email")
    @Schema(description = "邮箱地址")
    private String email;

    /**
     * 退款类型
     */
    @TableField("refund_type")
    @Schema(description = "退款类型")
    private Integer refundType;

    /**
     * 退款方式
     */
    @TableField("refund_method")
    @Schema(description = "退款方式")
    private String refundMethod;

    /**
     * 审核人ID
     */
    @TableField("approver_id")
    @Schema(description = "审核人ID")
    private Long approverId;

    /**
     * 审核人姓名
     */
    @TableField("approver_name")
    @Schema(description = "审核人姓名")
    private String approverName;

    /**
     * 审核时间
     */
    @TableField("approve_time")
    @Schema(description = "审核时间")
    private LocalDateTime approveTime;

    /**
     * 审核意见
     */
    @TableField("approve_comment")
    @Schema(description = "审核意见")
    private String approveComment;

    /**
     * 处理状态
     */
    @TableField("process_status")
    @Schema(description = "处理状态")
    private Integer processStatus;

    /**
     * 附件信息 (JSON格式)
     */
    @TableField("attachments")
    @Schema(description = "附件信息")
    private String attachments;

    // 注意：createTime, updateTime, createUserId, updateUserId, deletedFlag, version
    // 已由BaseEntity提供，无需重复定义

    // 缺失的getter/setter方法 - 根据错误日志添加
    public String getUserIdStr() {
        return this.userIdStr;
    }

    public void setUserIdStr(String userIdStr) {
        this.userIdStr = userIdStr;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContactPhone() {
        return this.contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getRefundType() {
        return this.refundType;
    }

    public void setRefundType(Integer refundType) {
        this.refundType = refundType;
    }

    public String getRefundMethod() {
        return this.refundMethod;
    }

    public void setRefundMethod(String refundMethod) {
        this.refundMethod = refundMethod;
    }

    public Long getApproverId() {
        return this.approverId;
    }

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    public String getApproverName() {
        return this.approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public LocalDateTime getApproveTime() {
        return this.approveTime;
    }

    public void setApproveTime(LocalDateTime approveTime) {
        this.approveTime = approveTime;
    }

    public LocalDateTime getApprovalTime() {
        return this.approveTime;
    }

    public void setApprovalTime(LocalDateTime approvalTime) {
        this.approveTime = approvalTime;
    }

    public String getApproveComment() {
        return this.approveComment;
    }

    public void setApproveComment(String approveComment) {
        this.approveComment = approveComment;
    }

    public String getApprovalComment() {
        return this.approveComment;
    }

    public void setApprovalComment(String approvalComment) {
        this.approveComment = approvalComment;
    }

    public Integer getProcessStatus() {
        return this.processStatus;
    }

    public void setProcessStatus(Integer processStatus) {
        this.processStatus = processStatus;
    }

    public String getAttachments() {
        return this.attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }
}
