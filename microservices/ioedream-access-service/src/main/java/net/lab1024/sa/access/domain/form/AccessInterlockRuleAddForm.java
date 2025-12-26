package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 门禁全局互锁规则 新增表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@Schema(description = "门禁全局互锁规则新增表单")
public class AccessInterlockRuleAddForm {

    @NotBlank(message = "规则名称不能为空")
    @Schema(description = "规则名称", example = "实验室A/B互锁")
    private String ruleName;

    @NotBlank(message = "规则编码不能为空")
    @Schema(description = "规则编码", example = "INTERLOCK_LAB_AB_001")
    private String ruleCode;

    @NotNull(message = "区域A ID不能为空")
    @Schema(description = "区域A ID", example = "1001")
    private Long areaAId;

    @NotNull(message = "区域B ID不能为空")
    @Schema(description = "区域B ID", example = "1002")
    private Long areaBId;

    @NotBlank(message = "互锁模式不能为空")
    @Schema(description = "互锁模式", example = "BIDIRECTIONAL", allowableValues = {"BIDIRECTIONAL", "UNIDIRECTIONAL"})
    private String interlockMode;

    @Schema(description = "区域A关联门ID列表", example = "[2001, 2002]")
    private String areaADoorIds;

    @Schema(description = "区域B关联门ID列表", example = "[2003, 2004]")
    private String areaBDoorIds;

    @NotBlank(message = "解锁条件不能为空")
    @Schema(description = "解锁条件", example = "MANUAL", allowableValues = {"MANUAL", "TIMER", "AUTO"})
    private String unlockCondition;

    @Schema(description = "自动解锁延迟秒数", example = "30")
    private Integer unlockDelaySeconds;

    @Schema(description = "优先级(1-100,数值越大优先级越高)", example = "100")
    private Integer priority;

    @Schema(description = "规则描述", example = "实验室A和B区域互锁，同时只能进入一个区域")
    private String description;
}
