package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门禁疏散点VO
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@Data
@Schema(description = "门禁疏散点VO")
public class AccessEvacuationPointVO {

    @Schema(description = "疏散点ID", example = "1")
    private Long pointId;

    @Schema(description = "疏散点名称", example = "A栋消防出口")
    private String pointName;

    @Schema(description = "关联区域ID", example = "1001")
    private Long areaId;

    @Schema(description = "疏散类型", example = "FIRE")
    private String evacuationType;

    @Schema(description = "关联门ID列表(JSON数组)", example = "[3001, 3002, 3003]")
    private String doorIds;

    @Schema(description = "优先级", example = "1")
    private Integer priority;

    @Schema(description = "启用状态 (1-启用 0-禁用)", example = "1")
    private Integer enabled;

    @Schema(description = "备注", example = "主消防疏散通道")
    private String remarks;

    @Schema(description = "创建时间", example = "2025-01-25T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-01-25T10:00:00")
    private LocalDateTime updateTime;
}
