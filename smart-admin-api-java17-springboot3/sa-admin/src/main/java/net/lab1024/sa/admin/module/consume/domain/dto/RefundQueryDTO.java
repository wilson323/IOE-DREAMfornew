package net.lab1024.sa.admin.module.consume.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.consume.domain.enums.RefundStatusEnum;

import java.time.LocalDateTime;

/**
 * 退款查询DTO
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
@Schema(description = "退款查询参数")
public class RefundQueryDTO {

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "退款单号", example = "RF1694876400001234")
    private String refundNo;

    @Schema(description = "原订单号", example = "RC1694876400001234")
    private String originalOrderNo;

    @Schema(description = "退款状态", example = "PENDING")
    private RefundStatusEnum status;

    @Schema(description = "开始时间", example = "2023-11-01T00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2023-11-30T23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "最小金额", example = "10.00")
    private Double minAmount;

    @Schema(description = "最大金额", example = "1000.00")
    private Double maxAmount;

    @Schema(description = "第三方退款单号", example = "2023111722001234567890")
    private String thirdPartyRefundNo;
}