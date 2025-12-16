package net.lab1024.sa.common.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 基础查询表单
 * <p>
 * 提供分页查询的基础功能
 * 所有查询Form类都应该继承此类
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
@Schema(description = "基础查询表单")
public class BaseQueryForm {

    @Schema(description = "页码", example = "1", minimum = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @Schema(description = "页大小", example = "20", minimum = "1", maximum = "100")
    @Min(value = 1, message = "页大小必须大于0")
    @Max(value = 100, message = "页大小不能超过100")
    private Integer pageSize = 20;

    @Schema(description = "排序字段", example = "createTime")
    private String sortField;

    @Schema(description = "排序方向：asc-升序，desc-降序", example = "desc")
    private String sortOrder = "desc";
}