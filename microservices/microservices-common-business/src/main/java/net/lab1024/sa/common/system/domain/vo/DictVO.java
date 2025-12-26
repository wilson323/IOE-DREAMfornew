package net.lab1024.sa.common.system.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典视图对象
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
@Schema(description = "字典视图对象")
public class DictVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "字典ID", example = "1001")
    private Long dictId;

    @Schema(description = "字典类型编码", example = "GENDER")
    private String dictTypeCode;

    @Schema(description = "字典类型名称", example = "性别")
    private String dictTypeName;

    @Schema(description = "字典数据编码", example = "MALE")
    private String dictDataCode;

    @Schema(description = "字典数据值", example = "男")
    private String dictDataValue;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "状态描述", example = "启用")
    private String statusDesc;

    @Schema(description = "备注", example = "性别字典")
    private String remark;
}

