package net.lab1024.sa.common.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 系统字典实体
 * <p>
 * 对应数据库表: t_system_dict
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_system_dict")
public class SystemDictEntity extends BaseEntity {

    @TableId(value = "dict_id", type = IdType.AUTO)
    private Long dictId;

    @TableField("dict_type_id")
    private Long dictTypeId;

    @TableField("type_code")
    private String typeCode;

    @TableField("dict_label")
    private String dictLabel;

    @TableField("dict_value")
    private String dictValue;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("status")
    private Integer status;

    @TableField("is_default")
    private Integer isDefault;

    @TableField("remark")
    private String remark;
}

