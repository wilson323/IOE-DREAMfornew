package net.lab1024.sa.admin.module.consume.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.consume.domain.enums.RefundStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款结果DTO
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
@Schema(description = "退款结果")
public class RefundResultDTO {

    @Schema(description = "退款单号", example = "RF1694876400001234")
    private String refundNo;

    @Schema(description = "原订单号", example = "RC1694876400001234")
    private String originalOrderNo;

    @Schema(description = "退款金额", example = "50.00")
    private BigDecimal refundAmount;

    @Schema(description = "退款状态", example = "PENDING")
    private RefundStatusEnum status;

    @Schema(description = "退款原因", example = "SERVICE_QUALITY")
    private String refundReason;

    @Schema(description = "处理结果", example = "退款申请已提交")
    private String message;

    @Schema(description = "创建时间", example = "2023-11-17T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "处理时间", example = "2023-11-17T11:30:00")
    private LocalDateTime processTime;

    @Schema(description = "第三方退款单号", example = "2023111722001234567890")
    private String thirdPartyRefundNo;

    @Schema(description = "手续费", example = "0.00")
    private BigDecimal feeAmount;

    @Schema(description = "实际退款金额", example = "50.00")
    private BigDecimal actualRefundAmount;

    @Schema(description = "退款方式", example = "ORIGINAL_PAYMENT")
    private String refundMethod;

    @Schema(description = "预计到账时间", example = "2023-11-17T12:00:00")
    private LocalDateTime expectedArrivalTime;

    @Schema(description = "处理人员ID", example = "1001")
    private Long processorId;

    @Schema(description = "处理备注", example = "用户申请退款，已审核通过")
    private String processRemark;
}