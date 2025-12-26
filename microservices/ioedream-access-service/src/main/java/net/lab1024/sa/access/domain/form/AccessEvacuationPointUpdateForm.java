package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 门禁疏散点更新表单
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@Data
@Schema(description = "门禁疏散点更新表单")
public class AccessEvacuationPointUpdateForm {

    @Schema(description = "疏散点ID", example = "1")
    private Long pointId;

    @Schema(description = "疏散点名称", example = "A栋消防出口(更新)")
    private String pointName;

    @Schema(description = "关联区域ID", example = "1001")
    private Long areaId;

    @Schema(description = "疏散类型", example = "FIRE")
    private String evacuationType;

    @Schema(description = "关联门ID列表", example = "[3001, 3002, 3003]")
    private List<Long> doorIds;

    @Schema(description = "优先级", example = "2")
    private Integer priority;

    @Schema(description = "备注", example = "主消防疏散通道(更新)")
    private String description;
}
