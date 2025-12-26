package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Builder;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费退款记录视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Builder
@Schema(description = "消费退款记录视图对象")
public class ConsumeRefundRecordVO {

    @Schema(description = "退款ID", example = "1001")
    private Long refundId;

    @Schema(description = "原消费记录ID", example = "2001")
    private Long consumeRecordId;

    @Schema(description = "账户ID", example = "1001")
    private Long accountId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String username;

    @Schema(description = "用户编号", example = "EMP001")
    private String userCode;

    @Schema(description = "原消费金额", example = "25.50")
    private BigDecimal originalAmount;

    @Schema(description = "退款金额", example = "25.50")
    private BigDecimal refundAmount;

    @Schema(description = "退款类型", example = "FULL")
    private String refundType;

    @Schema(description = "退款类型名称", example = "全额退款")
    private String refundTypeName;

    @Schema(description = "退款原因", example = "菜品质量问题")
    private String refundReason;

    @Schema(description = "退款状态", example = "APPROVED")
    private String status;

    @Schema(description = "退款状态名称", example = "已批准")
    private String statusName;

    @Schema(description = "申请人", example = "张三")
    private String applicant;

    @Schema(description = "申请时间", example = "2025-12-21T14:30:00")
    private LocalDateTime applyTime;

    @Schema(description = "审批人", example = "李管理员")
    private String approver;

    @Schema(description = "审批时间", example = "2025-12-21T15:00:00")
    private LocalDateTime approveTime;

    @Schema(description = "审批意见", example = "同意退款")
    private String approveReason;

    @Schema(description = "执行时间", example = "2025-12-21T15:05:00")
    private LocalDateTime executeTime;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "附件URL", example = "http://example.com/image.jpg")
    private String attachmentUrl;

    @Schema(description = "创建时间", example = "2025-12-21T14:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-21T15:05:00")
    private LocalDateTime updateTime;

    @Schema(description = "备注", example = "菜品有异物要求退款")
    private String remark;

    @Schema(description = "原消费订单号", example = "ORDER202512210001")
    private String originalOrderNo;

    @Schema(description = "退款交易流水号", example = "REFUND202512210001")
    private String refundTransactionNo;

    @Schema(description = "退款方式", example = "BALANCE")
    private String refundMethod;

    @Schema(description = "退款方式名称", example = "退回余额")
    private String refundMethodName;
}