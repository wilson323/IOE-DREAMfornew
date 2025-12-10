package net.lab1024.sa.consume.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 移动端快捷消费请求DTO
 * 简化版移动端消费请求数据结构
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "移动端快捷消费请求")
public class MobileQuickConsumeRequestDTO {

    @Schema(description = "订单ID", example = "MOBILE_20250130001")
    @NotNull(message = "订单ID不能为空")
    private String orderId;

    @Schema(description = "账户ID", example = "1")
    @NotNull(message = "账户ID不能为空")
    private Long accountId;

    @Schema(description = "消费金额", example = "50.00")
    @NotNull(message = "消费金额不能为空")
    @DecimalMin(value = "0.01", message = "消费金额必须大于0")
    private BigDecimal amount;

    @Schema(description = "设备ID", example = "MOBILE_001")
    private String deviceId;

    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    @Schema(description = "消费类型", example = "DINING")
    private String consumeType;

    @Schema(description = "消费描述", example = "午餐消费")
    private String description;
}