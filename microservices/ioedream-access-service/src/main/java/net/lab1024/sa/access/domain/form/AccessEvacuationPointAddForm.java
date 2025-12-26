package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 门禁疏散点新增表单
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@Data
@Schema(description = "门禁疏散点新增表单")
public class AccessEvacuationPointAddForm {

    @Schema(description = "疏散点名称", example = "A栋消防出口")
    @NotBlank(message = "疏散点名称不能为空")
    private String pointName;

    @Schema(description = "关联区域ID", example = "1001")
    @NotNull(message = "关联区域不能为空")
    private Long areaId;

    @Schema(description = "疏散类型", example = "FIRE")
    @NotBlank(message = "疏散类型不能为空")
    private String evacuationType;

    @Schema(description = "关联门ID列表", example = "[3001, 3002, 3003]")
    @NotNull(message = "关联门不能为空")
    private List<Long> doorIds;

    @Schema(description = "优先级", example = "1")
    @NotNull(message = "优先级不能为空")
    private Integer priority;

    @Schema(description = "启用状态 (1-启用 0-禁用)", example = "1")
    private Integer enabled;

    @Schema(description = "备注", example = "主消防疏散通道")
    private String description;
}
