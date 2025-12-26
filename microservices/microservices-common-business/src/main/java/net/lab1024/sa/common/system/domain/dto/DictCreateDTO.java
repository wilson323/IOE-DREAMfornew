package net.lab1024.sa.common.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典创建DTO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
@Schema(description = "字典创建DTO")
public class DictCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "字典类型编码", required = true, example = "GENDER")
    @NotBlank(message = "字典类型编码不能为空")
    private String dictTypeCode;

    @Schema(description = "字典数据编码", required = true, example = "MALE")
    @NotBlank(message = "字典数据编码不能为空")
    private String dictDataCode;

    @Schema(description = "字典数据值", required = true, example = "男")
    @NotBlank(message = "字典数据值不能为空")
    private String dictDataValue;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "性别字典")
    private String remark;
}

