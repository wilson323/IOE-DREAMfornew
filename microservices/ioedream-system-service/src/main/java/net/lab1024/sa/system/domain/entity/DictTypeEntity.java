package net.lab1024.sa.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 数据字典类型实体
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
@TableName("t_dict_type")
@Schema(description = "数据字典类型实体")
public class DictTypeEntity extends BaseEntity {

    /**
     * 字典类型ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "字典类型ID", example = "1")
    private Long dictTypeId;

    /**
     * 字典类型名称
     */
    @Schema(description = "字典类型名称", example = "用户状态")
    private String dictTypeName;

    /**
     * 字典类型编码
     */
    @Schema(description = "字典类型编码", example = "USER_STATUS")
    private String dictTypeCode;

    /**
     * 字典类型描述
     */
    @Schema(description = "字典类型描述", example = "用户状态字典")
    private String description;

    /**
     * 状态（1-启用，0-禁用）
     */
    @Schema(description = "状态", example = "1")
    private Integer status;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    private Integer sortNumber;

    /**
     * 是否内置（1-内置，0-自定义）
     */
    @Schema(description = "是否内置", example = "1")
    private Integer isSystem;

    /**
     * 扩展字段（JSON格式）
     */
    @Schema(description = "扩展字段", example = "{}")
    private String extendInfo;
}
