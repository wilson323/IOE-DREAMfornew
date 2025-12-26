package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 退款申请表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Data
@Schema(description = "退款申请表单")
public class ConsumeRefundApplyForm {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @NotNull(message = "消费记录ID不能为空")
    @Schema(description = "消费记录ID", example = "2001")
    private Long consumeId;

    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    @Schema(description = "退款金额", example = "50.00")
    private BigDecimal refundAmount;

    @NotBlank(message = "退款原因不能为空")
    @Schema(description = "退款原因", example = "菜品质量问题", allowableValues = {"菜品质量问题", "服务态度问题", "误操作消费", "其他原因"})
    private String refundReason;

    @Schema(description = "自定义退款原因", example = "详细描述退款原因")
    private String customReason;

    @Schema(description = "凭证图片URL", example = "https://example.com/image.jpg")
    private String proofImageUrl;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;
}
