package net.lab1024.sa.admin.module.consume.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 退款请求DTO
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
@Schema(description = "退款请求参数")
public class RefundRequestDTO {

    @Schema(description = "退款单号", example = "RF1694876400001234")
    private String refundNo;

    @NotNull(message = "原订单号不能为空")
    @Schema(description = "原订单号", example = "RC1694876400001234")
    private String originalOrderNo;

    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    @Schema(description = "退款金额", example = "50.00")
    private BigDecimal refundAmount;

    @NotNull(message = "退款原因不能为空")
    @Schema(description = "退款原因", example = "SERVICE_QUALITY")
    private String refundReason;

    @Size(max = 500, message = "退款说明长度不能超过500个字符")
    @Schema(description = "退款说明", example = "商品质量问题申请退款")
    private String refundDescription;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "客户端IP地址", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "设备信息", example = "iPhone 13 Pro")
    private String deviceInfo;

    @Schema(description = "附件信息", example = "图片或文档")
    private String attachments;

    @Schema(description = "联系方式", example = "user@example.com")
    private String contactInfo;

    @Schema(description = "扩展参数")
    private String extendParams;
}