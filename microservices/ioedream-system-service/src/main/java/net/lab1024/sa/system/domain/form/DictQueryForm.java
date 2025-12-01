package net.lab1024.sa.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.page.PageForm;

/**
 * 数据字典查询表单
 * <p>
 * 用于字典列表查询的数据传输对象
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "数据字典查询表单")
public class DictQueryForm extends PageForm {

    // Lombok @Data 会从父类 PageForm 继承 getPageNum() 和 getPageSize() 方法
    // 如果 IDE 报错，请确保 Lombok 插件已正确安装和配置

    /**
     * 查询类型（type-字典类型，data-字典项）
     */
    @Schema(description = "查询类型", example = "type", allowableValues = { "type", "data" })
    private String queryType;

    /**
     * 字典类型名称（模糊查询）
     */
    @Schema(description = "字典类型名称（模糊查询）", example = "用户")
    private String dictTypeName;

    /**
     * 字典类型编码（精确查询）
     */
    @Schema(description = "字典类型编码（精确查询）", example = "USER_STATUS")
    private String dictTypeCode;

    /**
     * 字典标签（模糊查询）
     */
    @Schema(description = "字典标签（模糊查询）", example = "启用")
    private String dictLabel;

    /**
     * 字典值（模糊查询）
     */
    @Schema(description = "字典值（模糊查询）", example = "1")
    private String dictValue;

    /**
     * 字典类型ID
     */
    @Schema(description = "字典类型ID", example = "1")
    private Long dictTypeId;

    /**
     * 状态（1-启用，0-禁用）
     */
    @Schema(description = "状态", example = "1", allowableValues = { "0", "1" })
    private Integer status;

    /**
     * 是否内置（1-内置，0-自定义）
     */
    @Schema(description = "是否内置", example = "0", allowableValues = { "0", "1" })
    private Integer isSystem;

    /**
     * 是否默认值
     */
    @Schema(description = "是否默认值", example = "0", allowableValues = { "0", "1" })
    private Integer isDefault;

    /**
     * 创建时间开始
     */
    @Schema(description = "创建时间开始", example = "2025-01-01 00:00:00")
    private String createTimeStart;

    /**
     * 创建时间结束
     */
    @Schema(description = "创建时间结束", example = "2025-01-31 23:59:59")
    private String createTimeEnd;

    /**
     * 排序字段
     */
    @Schema(description = "排序字段", example = "sortNumber", allowableValues = { "dictTypeId", "dictDataId", "sortNumber",
            "createTime", "updateTime" })
    private String sortField;

    /**
     * 排序方式（asc-升序，desc-降序）
     */
    @Schema(description = "排序方式", example = "asc", allowableValues = { "asc", "desc" })
    private String sortOrder;

    /**
     * 是否只查询启用的
     */
    @Schema(description = "是否只查询启用的", example = "true")
    private Boolean onlyEnabled;

    /**
     * 是否只查询非内置
     */
    @Schema(description = "是否只查询非内置", example = "false")
    private Boolean onlyCustom;
}
