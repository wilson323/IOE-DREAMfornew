package net.lab1024.sa.system.domain.vo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据字典项视图对象 *
 * <p>
 * 用于前端展示的字典项信息
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
@Data
@Schema(description = "数据字典项视图对象")
public class DictDataVO {

    /**
     * 字典数据ID
     */
    @Schema(description = "字典数据ID", example = "1")
    private Long dictDataId;

    /**
     * 字典类型ID
     */
    @Schema(description = "字典类型ID", example = "1")
    private Long dictTypeId;

    /**
     * 字典类型编码
     */
    @Schema(description = "字典类型编码", example = "USER_STATUS")
    private String dictTypeCode;

    /**
     * 字典类型名称
     */
    @Schema(description = "字典类型名称", example = "用户状态")
    private String dictTypeName;

    /**
     * 字典标签
     */
    @Schema(description = "字典标签", example = "启用")
    private String dictLabel;

    /**
     * 字典值
     */
    @Schema(description = "字典值", example = "1")
    private String dictValue;

    /**
     * 字典标签（国际化）
     */
    @Schema(description = "字典标签（国际化）", example = "enabled")
    private String dictLabelI18n;

    /**
     * 标签颜色（前端显示用）
     */
    @Schema(description = "标签颜色", example = "success")
    private String labelColor;

    /**
     * 标签样式（前端显示用）
     */
    @Schema(description = "标签样式", example = "primary")
    private String labelStyle;

    /**
     * 字典项描述
     */
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
    @Schema(description = "样式属性", example = "primary")
    private String cssClass;

    /**
     * 表格字典样式
     */
    @Schema(description = "表格字典样式", example = "success")
    private String listClass;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "用户启用状态")
    private String remark;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private Integer status;

    /**
     * 状态名称
     */
    @Schema(description = "状态名称", example = "启用")
    private String statusName;

    /**
     * 是否默认值
     */
    @Schema(description = "是否默认值", example = "0")
    private Integer isDefault;

    /**
     * 是否默认值名称
     */
    @Schema(description = "是否默认值名称", example = "否")
    private String isDefaultName;

    /**
     * 是否内置
     */
    @Schema(description = "是否内置", example = "1")
    private Integer isSystem;

    /**
     * 是否内置名称
     */
    @Schema(description = "是否内置名称", example = "否")
    private String isSystemName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间", example = "2025-01-01 12:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间", example = "2025-01-01 12:00:00")
    private LocalDateTime updateTime;

    /**
     * 创建
     */
    @Schema(description = "创建人", example = "管理员")
    private String createUserName;

    /**
     * 更新
     */
    @Schema(description = "更新人", example = "管理员")
    private String updateUserName;

    /**
     * 扩展字段解析结果
     */
    @Schema(description = "扩展字段", example = "{}")
    private Object extendInfo;
}
