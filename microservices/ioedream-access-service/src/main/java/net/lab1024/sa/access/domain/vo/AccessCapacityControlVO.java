package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门禁容量控制VO
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@Data
@Schema(description = "门禁容量控制VO")
public class AccessCapacityControlVO {

    @Schema(description = "容量控制ID", example = "1")
    private Long controlId;

    @Schema(description = "关联区域ID", example = "1001")
    private Long areaId;

    @Schema(description = "最大容量", example = "100")
    private Integer maxCapacity;

    @Schema(description = "当前人数", example = "50")
    private Integer currentCount;

    @Schema(description = "控制模式", example = "STRICT")
    private String controlMode;

    @Schema(description = "告警阈值(百分比)", example = "80")
    private Integer alertThreshold;

    @Schema(description = "进入限制状态 (1-禁止 0-允许)", example = "0")
    private Integer entryBlocked;

    @Schema(description = "启用状态 (1-启用 0-禁用)", example = "1")
    private Integer enabled;

    @Schema(description = "备注", example = "会议室A容量控制")
    private String remarks;

    @Schema(description = "创建时间", example = "2025-01-25T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-01-25T10:00:00")
    private LocalDateTime updateTime;
}
