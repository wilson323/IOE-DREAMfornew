package net.lab1024.sa.consume.domain.form;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

import net.lab1024.sa.common.domain.form.BaseQueryForm;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费退款查询表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Schema(description = "消费退款查询表单")
public class ConsumeRefundQueryForm extends BaseQueryForm {

    @Schema(description = "退款ID", example = "1001")
    private Long refundId;

    @Schema(description = "原消费记录ID", example = "1001")
    private Long consumeRecordId;

    @Schema(description = "账户ID", example = "1001")
    private Long accountId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String username;

    @Schema(description = "退款类型", example = "FULL")
    private String refundType;

    @Schema(description = "退款状态", example = "PENDING")
    private String status;

    @Schema(description = "申请人", example = "张三")
    private String applicant;

    @Schema(description = "最小金额", example = "0.01")
    private BigDecimal minAmount;

    @Schema(description = "最大金额", example = "1000.00")
    private BigDecimal maxAmount;

    @Schema(description = "申请开始时间", example = "2025-12-21T00:00:00")
    private LocalDateTime applyTimeStart;

    @Schema(description = "申请结束时间", example = "2025-12-21T23:59:59")
    private LocalDateTime applyTimeEnd;

    @Schema(description = "关键词搜索", example = "质量")
    private String keyword;
}