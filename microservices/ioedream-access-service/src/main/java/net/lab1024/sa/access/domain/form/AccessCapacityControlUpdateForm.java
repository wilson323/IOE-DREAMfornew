package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 门禁容量控制更新表单
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@Data
@Schema(description = "门禁容量控制更新表单")
public class AccessCapacityControlUpdateForm {

    @Schema(description = "容量控制ID", example = "1")
    private Long controlId;

    @Schema(description = "最大容量", example = "150")
    private Integer maxCapacity;

    @Schema(description = "控制模式", example = "WARNING")
    private String controlMode;

    @Schema(description = "告警阈值(百分比)", example = "85")
    private Integer alertThreshold;

    @Schema(description = "备注", example = "会议室A容量控制(更新)")
    private String description;
}
