package net.lab1024.sa.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 移动端消费记录VO
 * 移动端优化的消费记录展示格式
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "移动端消费记录")
public class MobileConsumeRecordVO {

    @Schema(description = "记录ID", example = "1")
    private Long recordId;

    @Schema(description = "订单ID", example = "MOBILE_20250130001")
    private String orderId;

    @Schema(description = "消费金额", example = "50.00")
    private BigDecimal amount;

    @Schema(description = "消费类型", example = "DINING")
    private String consumeType;

    @Schema(description = "消费类型名称", example = "餐饮")
    private String consumeTypeName;

    @Schema(description = "消费时间", example = "2025-01-30 12:30:00")
    private LocalDateTime consumeTime;

    @Schema(description = "商户名称", example = "食堂一楼")
    private String merchantName;

    @Schema(description = "设备名称", example = "POS机001")
    private String deviceName;

    @Schema(description = "状态", example = "SUCCESS")
    private String status;

    @Schema(description = "状态描述", example = "成功")
    private String statusDescription;

    @Schema(description = "是否可退款", example = "true")
    private Boolean refundable;
}


