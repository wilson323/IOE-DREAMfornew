package net.lab1024.sa.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 数据字典项实体类
 * <p>
 * 严格遵循repowiki实体规范：
 * - 继承BaseEntity获取审计字段
 * - 使用jakarta包名
 * - 完整的审计字段
 * - 软删除支持
 * - 标准注解配置
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_dict_data")
@Schema(description = "数据字典项实体")
public class DictDataEntity extends BaseEntity {

    /**
     * 字典数据ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "字典数据ID", example = "1")
    private Long dictDataId;

    /**
     * 字典类型ID
     */
    @Schema(description = "字典类型ID", example = "1")
    private Long dictTypeId;

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
     * 状态（1-启用，0-禁用）
     */
    @Schema(description = "状态", example = "1")
    private Integer status;

    /**
     * 是否默认值
     */
    @Schema(description = "是否默认值", example = "0")
    private Integer isDefault;

    /**
     * 是否内置(0-否，1-是)
     */
    @Schema(description = "是否内置", example = "1")
    private Integer isSystem;

    /**
     * 扩展字段（JSON格式）
     */
    @Schema(description = "扩展字段", example = "{}")
    private String extendInfo;
}
