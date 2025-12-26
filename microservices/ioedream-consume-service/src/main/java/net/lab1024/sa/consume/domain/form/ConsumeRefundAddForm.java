package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费退款添加表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Schema(description = "消费退款添加表单")
public class ConsumeRefundAddForm {

    @NotNull(message = "原消费记录ID不能为空")
    @Schema(description = "原消费记录ID", example = "1001")
    private Long consumeRecordId;

    @NotNull(message = "账户ID不能为空")
    @Schema(description = "账户ID", example = "1001")
    private Long accountId;

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    @Schema(description = "退款金额", example = "25.50")
    private BigDecimal refundAmount;

    @NotBlank(message = "退款原因不能为空")
    @Schema(description = "退款原因", example = "菜品质量问题")
    private String refundReason;

    @Schema(description = "退款类型", example = "FULL", allowableValues = {"FULL", "PARTIAL"})
    private String refundType;

    @Schema(description = "退款类型名称", example = "全额退款")
    private String refundTypeName;

    @Schema(description = "申请人", example = "张三")
    private String applicant;

    @Schema(description = "申请时间")
    private java.time.LocalDateTime applyTime;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "备注", example = "菜品有异物要求退款")
    private String remark;

    @Schema(description = "附件URL", example = "http://example.com/image.jpg")
    private String attachmentUrl;
}