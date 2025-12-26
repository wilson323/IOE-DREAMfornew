package net.lab1024.sa.consume.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 补贴结果DTO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "补贴计算结果")
public class SubsidyResultDTO {

    @Schema(description = "是否匹配到规则")
    private Boolean matched;

    @Schema(description = "规则ID")
    private Long ruleId;

    @Schema(description = "规则编码")
    private String ruleCode;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "补贴金额")
    private BigDecimal subsidyAmount;

    @Schema(description = "补贴比例")
    private BigDecimal subsidyRate;

    @Schema(description = "原消费金额")
    private BigDecimal consumeAmount;

    @Schema(description = "计算详情")
    private String calculationDetail;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "是否成功")
    private Boolean success;

    public static SubsidyResultDTO noMatch() {
        return SubsidyResultDTO.builder()
                .matched(false)
                .success(true)
                .subsidyAmount(BigDecimal.ZERO)
                .build();
    }

    public static SubsidyResultDTO success(Long ruleId, String ruleCode, String ruleName,
                                           BigDecimal subsidyAmount, String detail) {
        return SubsidyResultDTO.builder()
                .matched(true)
                .ruleId(ruleId)
                .ruleCode(ruleCode)
                .ruleName(ruleName)
                .subsidyAmount(subsidyAmount)
                .success(true)
                .calculationDetail(detail)
                .build();
    }

    public static SubsidyResultDTO error(String errorMessage) {
        return SubsidyResultDTO.builder()
                .matched(false)
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
