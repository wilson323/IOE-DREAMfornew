package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 门禁容量控制查询表单
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@Data
@Schema(description = "门禁容量控制查询表单")
public class AccessCapacityControlQueryForm extends net.lab1024.sa.common.domain.PageParam {

    @Schema(description = "关联区域ID", example = "1001")
    private Long areaId;

    @Schema(description = "控制模式", example = "STRICT")
    private String controlMode;

    @Schema(description = "启用状态 (1-启用 0-禁用)", example = "1")
    private Integer enabled;
}
