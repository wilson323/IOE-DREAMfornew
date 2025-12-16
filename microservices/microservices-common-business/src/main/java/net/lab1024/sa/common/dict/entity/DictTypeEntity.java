package net.lab1024.sa.common.dict.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典类型实体
 * <p>
 * 企业级真实实现 - 对应数据库表 t_dict_type
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_dict_type")
public class DictTypeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字典类型ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列type_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "type_id", type = IdType.AUTO)
    private Long id;

    /**
     * 字典类型编码（唯一）
     */
    @TableField("type_code")
    private String typeCode;

    /**
     * 字典类型名称
     */
    @TableField("type_name")
    private String typeName;

    /**
     * 排序号
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 备注说明
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    @TableField("create_user_id")
    private Long createUserId;

    /**
     * 更新人ID
     */
    @TableField("update_user_id")
    private Long updateUserId;

    /**
     * 删除标记 0-未删除 1-已删除
     */
    @TableLogic
    @TableField("deleted_flag")
    private Integer deletedFlag;
}
