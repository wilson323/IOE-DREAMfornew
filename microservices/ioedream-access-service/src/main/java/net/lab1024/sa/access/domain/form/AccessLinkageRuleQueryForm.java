package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 查询联动规则表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@Schema(description = "查询联动规则表单")
public class AccessLinkageRuleQueryForm {

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "页大小", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "规则名称（模糊查询）")
    private String ruleName;

    @Schema(description = "联动类型(DOOR_LINKAGE-门联动, AREA_LINKAGE-区域联动)")
    private String ruleType;

    @Schema(description = "启用状态(0-禁用 1-启用)")
    private Integer enabled;
}
