package net.lab1024.sa.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 退款记录视图对象
 * <p>
 * 用于返回退款记录的详细信息
 * 包含退款状态、审批信息、处理结果等
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
@Schema(description = "退款记录视图对象")
public class RefundRecordVO {

    @Schema(description = "退款ID", example = "1")
    private Long refundId;

    @Schema(description = "交易号", example = "TX202512090001")
    private String transactionNo;

    @Schema(description = "原消费金额", example = "100.00")
    private BigDecimal originalAmount;

    @Schema(description = "退款金额", example = "50.00")
    private BigDecimal refundAmount;

    @Schema(description = "退款原因", example = "误操作")
    private String refundReason;

    @Schema(description = "退款说明", example = "用户误操作，申请退款")
    private String description;

    @Schema(description = "退款状态：1-待审批，2-已审批，3-已拒绝，4-已处理，5-已取消", example = "1")
    private Integer refundStatus;

    @Schema(description = "退款状态名称", example = "待审批")
    private String refundStatusName;

    @Schema(description = "退款方式：1-原路退回，2-退回余额，3-退回银行卡", example = "1")
    private Integer refundMethod;

    @Schema(description = "退款方式名称", example = "原路退回")
    private String refundMethodName;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String userName;

    @Schema(description = "申请人ID", example = "1")
    private Long applicantId;

    @Schema(description = "申请人姓名", example = "张三")
    private String applicantName;

    @Schema(description = "申请时间", example = "2025-12-09T10:30:00")
    private LocalDateTime applyTime;

    @Schema(description = "审批人ID", example = "2")
    private Long approverId;

    @Schema(description = "审批人姓名", example = "李四")
    private String approverName;

    @Schema(description = "审批时间", example = "2025-12-09T14:30:00")
    private LocalDateTime approveTime;

    @Schema(description = "审批状态：0-待审批，1-已通过，2-已拒绝", example = "1")
    private Integer approvalStatus;

    @Schema(description = "审批状态名称", example = "已通过")
    private String approvalStatusName;

    @Schema(description = "审批意见", example = "同意退款")
    private String approvalComment;

    @Schema(description = "处理人ID", example = "3")
    private Long processorId;

    @Schema(description = "处理人姓名", example = "王五")
    private String processorName;

    @Schema(description = "处理时间", example = "2025-12-09T16:00:00")
    private LocalDateTime processTime;

    @Schema(description = "处理结果：1-成功，2-失败", example = "1")
    private Integer processResult;

    @Schema(description = "处理结果名称", example = "成功")
    private String processResultName;

    @Schema(description = "处理备注", example = "退款已成功到账")
    private String processRemark;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "联系邮箱", example = "user@example.com")
    private String contactEmail;

    @Schema(description = "附件信息", example = "{\"images\": [\"url1\", \"url2\"]}")
    private Map<String, Object> attachments;

    @Schema(description = "是否紧急处理", example = "false")
    private Boolean urgent;

    @Schema(description = "期望处理时间", example = "2025-12-10T18:00:00")
    private String expectedProcessTime;

    @Schema(description = "实际退款账号", example = "6222021234567890")
    private String refundAccount;

    @Schema(description = "退款流水号", example = "RF202512090001")
    private String refundTransactionNo;

    @Schema(description = "失败原因", example = "原账号已冻结")
    private String failureReason;

    @Schema(description = "创建时间", example = "2025-12-09T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-09T16:00:00")
    private LocalDateTime updateTime;

    @Schema(description = "操作历史")
    private List<RefundOperationVO> operationHistory;

    @Schema(description = "扩展属性")
    private Map<String, Object> extendedAttributes;
}