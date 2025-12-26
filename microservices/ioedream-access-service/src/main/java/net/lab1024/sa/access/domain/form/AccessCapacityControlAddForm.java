package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 门禁容量控制新增表单
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@Data
@Schema(description = "门禁容量控制新增表单")
public class AccessCapacityControlAddForm {

    @Schema(description = "关联区域ID", example = "1001")
    @NotNull(message = "关联区域不能为空")
    private Long areaId;

    @Schema(description = "最大容量", example = "100")
    @NotNull(message = "最大容量不能为空")
    private Integer maxCapacity;

    @Schema(description = "控制模式", example = "STRICT")
    @NotNull(message = "控制模式不能为空")
    private String controlMode;

    @Schema(description = "告警阈值(百分比)", example = "80")
    private Integer alertThreshold;

    @Schema(description = "启用状态 (1-启用 0-禁用)", example = "1")
    private Integer enabled;

    @Schema(description = "备注", example = "会议室A容量控制")
    private String description;
}
