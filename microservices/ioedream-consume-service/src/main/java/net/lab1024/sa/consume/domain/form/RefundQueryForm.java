package net.lab1024.sa.consume.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.domain.form.BaseQueryForm;

import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款查询表单
 * <p>
 * 用于退款记录的查询条件筛选
 * 支持多维度查询：按用户、交易号、状态、时间等
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "退款查询表单")
public class RefundQueryForm extends BaseQueryForm {

    @Schema(description = "退款ID", example = "1")
    private Long refundId;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "交易号", example = "TX202512090001")
    private String transactionNo;

    @Schema(description = "退款状态：1-待审批，2-已审批，3-已拒绝，4-已处理，5-已取消", example = "1")
    private Integer refundStatus;

    @Schema(description = "审批状态：0-待审批，1-已通过，2-已拒绝", example = "0")
    private Integer approvalStatus;

    @Schema(description = "退款方式：1-原路退回，2-退回余额，3-退回银行卡", example = "1")
    private Integer refundMethod;

    @Schema(description = "最小退款金额", example = "10.00")
    @Min(value = 0, message = "最小退款金额不能小于0")
    private BigDecimal minRefundAmount;

    @Schema(description = "最大退款金额", example = "1000.00")
    @Min(value = 0, message = "最大退款金额不能小于0")
    private BigDecimal maxRefundAmount;

    @Schema(description = "申请人ID", example = "1")
    private Long applicantId;

    @Schema(description = "审批人ID", example = "2")
    private Long approverId;

    @Schema(description = "开始申请时间", example = "2025-12-01T00:00:00")
    private LocalDateTime startApplyTime;

    @Schema(description = "结束申请时间", example = "2025-12-31T23:59:59")
    private LocalDateTime endApplyTime;

    @Schema(description = "开始审批时间", example = "2025-12-01T00:00:00")
    private LocalDateTime startApproveTime;

    @Schema(description = "结束审批时间", example = "2025-12-31T23:59:59")
    private LocalDateTime endApproveTime;

    @Schema(description = "退款原因关键词", example = "误操作")
    private String refundReasonKeyword;

    @Schema(description = "审批意见关键词", example = "同意")
    private String approvalCommentKeyword;

    @Schema(description = "是否包含已删除记录", example = "false")
    private Boolean includeDeleted;

    @Schema(description = "排序字段：applyTime-申请时间，approveTime-审批时间，refundAmount-退款金额", example = "applyTime")
    private String sortBy;

    @Schema(description = "排序方向：asc-升序，desc-降序", example = "desc")
    private String sortDirection;
}


