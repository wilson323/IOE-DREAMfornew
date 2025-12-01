package net.lab1024.sa.admin.module.consume.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.consume.domain.enums.RechargeStatusEnum;
import net.lab1024.sa.admin.module.consume.domain.enums.RechargeTypeEnum;

import java.time.LocalDateTime;

/**
 * 充值查询DTO
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Data
@Schema(description = "充值查询参数")
public class RechargeQueryDTO {

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "订单号", example = "RC1694876400001234")
    private String orderNo;

    @Schema(description = "充值方式", example = "ALIPAY")
    private RechargeTypeEnum rechargeType;

    @Schema(description = "充值状态", example = "SUCCESS")
    private RechargeStatusEnum status;

    @Schema(description = "支付渠道", example = "ALIPAY_APP")
    private String payChannel;

    @Schema(description = "开始时间", example = "2023-11-01T00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2023-11-30T23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "最小金额", example = "10.00")
    private Double minAmount;

    @Schema(description = "最大金额", example = "1000.00")
    private Double maxAmount;

    @Schema(description = "第三方订单号", example = "2023111722001234567890")
    private String thirdPartyOrderNo;

    @Schema(description = "备注关键字", example = "用户")
    private String remarkKeyword;
}