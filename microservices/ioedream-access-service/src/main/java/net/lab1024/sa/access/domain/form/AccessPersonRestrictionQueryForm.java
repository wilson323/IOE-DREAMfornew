package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 门禁人员限制查询表单
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@Data
@Schema(description = "门禁人员限制查询表单")
public class AccessPersonRestrictionQueryForm extends net.lab1024.sa.common.domain.PageParam {

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "限制类型", example = "BLACKLIST")
    private String restrictionType;

    @Schema(description = "关联区域ID", example = "1001")
    private Long areaId;

    @Schema(description = "启用状态 (1-启用 0-禁用)", example = "1")
    private Integer enabled;
}
