package net.lab1024.sa.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 数据字典项新增表单
 * <p>
 * 用于字典项新增的数据验证和传输
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
@Data
@Schema(description = "数据字典项新增表单")
public class DictDataAddForm {

    /**
     * 字典类型ID
     */
    @Schema(description = "字典类型ID", example = "1")
    private Long dictTypeId;

    /**
     * 字典类型编码（用于通过编码查找字典类型）
     */
    @Schema(description = "字典类型编码", example = "USER_STATUS")
    private String dictTypeCode;

    /**
     * 字典标签
     */
    @NotBlank(message = "字典标签不能为空")
    @Size(max = 100, message = "字典标签长度不能超过100个字符")
    @Schema(description = "字典标签", example = "启用", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictLabel;

    /**
     * 字典值
     */
    @NotBlank(message = "字典值不能为空")
    @Size(max = 50, message = "字典值长度不能超过50个字符")
    @Schema(description = "字典值", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictValue;

    /**
     * 字典标签（国际化）
     */
    @Size(max = 100, message = "字典标签国际化长度不能超过100个字符")
    @Schema(description = "字典标签（国际化）", example = "enabled")
    private String dictLabelI18n;

    /**
     * 标签颜色（前端显示用）
     */
    @Size(max = 20, message = "标签颜色长度不能超过20个字符")
    @Schema(description = "标签颜色", example = "success", allowableValues = { "default", "primary", "success", "info",
            "warning", "danger" })
    private String labelColor;

    /**
     * 标签样式（前端显示用）
     */
    @Size(max = 20, message = "标签样式长度不能超过20个字符")
    @Schema(description = "标签样式", example = "primary", allowableValues = { "default", "primary", "secondary", "success",
            "danger", "warning", "info", "light", "dark" })
    private String labelStyle;

    /**
     * 字典项描述
     */
    @Size(max = 500, message = "字典项描述长度不能超过500个字符")
    @Schema(description = "字典项描述", example = "用户启用状态")
    private String description;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    private Integer sortNumber;

    /**
     * 排序号（兼容sortOrder）
     */
    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    /**
     * 样式属性
     */
    @Size(max = 100, message = "样式属性长度不能超过100个字符")
    @Schema(description = "样式属性", example = "primary")
    private String cssClass;

    /**
     * 表格字典样式
     */
    @Size(max = 100, message = "表格字典样式长度不能超过100个字符")
    @Schema(description = "表格字典样式", example = "success")
    private String listClass;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注", example = "用户启用状态")
    private String remark;

    /**
     * 状态（1-启用，0-禁用）
     */
    @Schema(description = "状态", example = "1", allowableValues = { "0", "1" })
    private Integer status;

    /**
     * 是否默认值
     */
    @Schema(description = "是否默认值", example = "0", allowableValues = { "0", "1" })
    private Integer isDefault;

    /**
     * 是否内置(0-否，1-是)
     */
    @Schema(description = "是否内置", example = "0", allowableValues = { "0", "1" })
    private Integer isSystem;

    /**
     * 扩展字段（JSON格式）
     */
    @Schema(description = "扩展字段（JSON格式）", example = "{\"icon\": \"check\", \"badge\": \"new\"}")
    private String extendInfo;
}
