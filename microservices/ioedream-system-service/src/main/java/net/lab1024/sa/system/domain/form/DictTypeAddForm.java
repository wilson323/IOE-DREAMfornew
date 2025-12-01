package net.lab1024.sa.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 数据字典类型新增表单
 * <p>
 * 用于字典类型新增的数据验证和传输
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
@Data
@Schema(description = "数据字典类型新增表单")
public class DictTypeAddForm {

    /**
     * 字典类型名称
     */
    @NotBlank(message = "字典类型名称不能为空")
    @Size(max = 100, message = "字典类型名称长度不能超过100个字符")
    @Schema(description = "字典类型名称", example = "用户状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictTypeName;

    /**
     * 字典类型编码
     */
    @NotBlank(message = "字典类型编码不能为空")
    @Size(max = 50, message = "字典类型编码长度不能超过50个字符")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "字典类型编码必须以大写字母开头，只能包含大写字母、数字和下划线")
    @Schema(description = "字典类型编码", example = "USER_STATUS", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictTypeCode;

    /**
     * 字典类型描述
     */
    @Size(max = 500, message = "字典类型描述长度不能超过500个字符")
    @Schema(description = "字典类型描述", example = "用户状态字典")
    private String description;

    /**
     * 状态（1-启用，0-禁用）
     */
    @Schema(description = "状态", example = "1", allowableValues = { "0", "1" })
    private Integer status;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    private Integer sortNumber;

    /**
     * 是否内置(0-否，1-是)
     */
    @Schema(description = "是否内置", example = "0", allowableValues = { "0", "1" })
    private Integer isSystem;

    /**
     * 扩展字段（JSON格式）
     */
    @Schema(description = "扩展字段（JSON格式）", example = "{\"category\": \"system\", \"module\": \"user\"}")
    private String extendInfo;
}
