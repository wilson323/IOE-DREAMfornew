package net.lab1024.sa.consume.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 退款申请表单
 * <p>
 * 用于处理退款申请的请求参数
 * 包含退款金额、原因、说明等核心信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
@Schema(description = "退款申请表单")
public class RefundRequestForm {

    @Schema(description = "交易号", example = "TX202512090001", required = true)
    @NotBlank(message = "交易号不能为空")
    @Size(max = 100, message = "交易号长度不能超过100个字符")
    private String transactionNo;

    @Schema(description = "退款金额", example = "50.00", required = true)
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0.01")
    private BigDecimal refundAmount;

    @Schema(description = "退款原因", example = "误操作", required = true)
    @NotBlank(message = "退款原因不能为空")
    @Size(max = 500, message = "退款原因长度不能超过500个字符")
    private String refundReason;

    @Schema(description = "退款说明", example = "用户误操作，申请退款")
    @Size(max = 1000, message = "退款说明长度不能超过1000个字符")
    private String description;

    @Schema(description = "退款方式：1-原路退回，2-退回余额，3-退回银行卡", example = "1")
    private Integer refundMethod = 1;

    @Schema(description = "联系电话", example = "13800138000")
    @Size(max = 20, message = "联系电话长度不能超过20个字符")
    private String contactPhone;

    @Schema(description = "联系邮箱", example = "user@example.com")
    @Size(max = 100, message = "联系邮箱长度不能超过100个字符")
    private String contactEmail;

    @Schema(description = "附件信息（JSON格式）", example = "{\"images\": [\"url1\", \"url2\"]}")
    private String attachments;

    @Schema(description = "是否紧急处理", example = "false")
    private Boolean urgent = false;

    @Schema(description = "期望处理时间", example = "2025-12-10T18:00:00")
    private String expectedProcessTime;
}


