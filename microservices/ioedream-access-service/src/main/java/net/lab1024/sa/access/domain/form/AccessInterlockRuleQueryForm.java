package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 门禁全局互锁规则 查询表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Data
@Schema(description = "门禁全局互锁规则查询表单")
public class AccessInterlockRuleQueryForm {

    @Schema(description = "当前页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "规则名称(模糊查询)", example = "实验室")
    private String ruleName;

    @Schema(description = "互锁模式", example = "BIDIRECTIONAL")
    private String interlockMode;

    @Schema(description = "解锁条件", example = "MANUAL")
    private String unlockCondition;

    @Schema(description = "启用状态", example = "1")
    private Integer enabled;
}
