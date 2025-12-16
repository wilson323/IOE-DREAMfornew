package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 移动端用户消费统计VO
 * <p>
 * 用于返回移动端用户消费统计数据
 * 严格遵循CLAUDE.md规范：
 * - 使用@Data注解
 * - 使用@Schema注解提供Swagger文档
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "移动端用户消费统计")
public class ConsumeMobileUserStatsVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "总交易笔数")
    private Integer totalCount;

    @Schema(description = "总消费金额")
    private BigDecimal totalAmount;

    @Schema(description = "今日交易笔数")
    private Integer todayCount;

    @Schema(description = "今日消费金额")
    private BigDecimal todayAmount;

    @Schema(description = "本月交易笔数")
    private Integer monthCount;

    @Schema(description = "本月消费金额")
    private BigDecimal monthAmount;
}




