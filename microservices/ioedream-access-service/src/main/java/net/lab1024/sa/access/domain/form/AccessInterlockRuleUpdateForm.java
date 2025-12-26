package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 门禁全局互锁规则 更新表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@Schema(description = "门禁全局互锁规则更新表单")
public class AccessInterlockRuleUpdateForm {

    @Schema(description = "规则名称", example = "实验室A/B互锁")
    private String ruleName;

    @Schema(description = "规则编码", example = "INTERLOCK_LAB_AB_001")
    private String ruleCode;

    @Schema(description = "区域A ID", example = "1001")
    private Long areaAId;

    @Schema(description = "区域B ID", example = "1002")
    private Long areaBId;

    @Schema(description = "互锁模式", example = "BIDIRECTIONAL")
    private String interlockMode;

    @Schema(description = "区域A关联门ID列表", example = "[2001, 2002]")
    private String areaADoorIds;

    @Schema(description = "区域B关联门ID列表", example = "[2003, 2004]")
    private String areaBDoorIds;

    @Schema(description = "解锁条件", example = "MANUAL")
    private String unlockCondition;

    @Schema(description = "自动解锁延迟秒数", example = "30")
    private Integer unlockDelaySeconds;

    @Schema(description = "优先级(1-100)", example = "100")
    private Integer priority;

    @Schema(description = "规则描述", example = "实验室A和B区域互锁")
    private String description;
}
