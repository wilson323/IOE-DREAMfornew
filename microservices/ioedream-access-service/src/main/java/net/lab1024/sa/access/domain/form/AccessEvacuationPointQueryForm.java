package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 门禁疏散点查询表单
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@Data
@Schema(description = "门禁疏散点查询表单")
public class AccessEvacuationPointQueryForm extends net.lab1024.sa.common.domain.PageParam {

    @Schema(description = "疏散点名称（模糊查询）", example = "消防出口")
    private String pointName;

    @Schema(description = "关联区域ID", example = "1001")
    private Long areaId;

    @Schema(description = "疏散类型", example = "FIRE")
    private String evacuationType;

    @Schema(description = "启用状态 (1-启用 0-禁用)", example = "1")
    private Integer enabled;
}
