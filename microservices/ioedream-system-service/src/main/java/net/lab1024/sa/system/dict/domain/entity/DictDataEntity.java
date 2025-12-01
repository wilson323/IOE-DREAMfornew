package net.lab1024.sa.system.dict.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 字典数据实体
 *
 * @author IOE-DREAM Team
 * @date 2025/11/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_dict_data")
public class DictDataEntity extends BaseEntity {

    /**
     * 字典数据ID
     */
    @TableId(value = "dict_data_id", type = IdType.AUTO)
    private Long dictDataId;

    /**
     * 字典类型ID
     */
    private Long dictTypeId;

    /**
     * 字典类型编码
     */
    private String dictTypeCode;

    /**
     * 字典标签
     */
    private String dictLabel;

    /**
     * 字典值
     */
    private String dictValue;

    /**
     * 样式属性
     */
    private String cssClass;

    /**
     * 表格字典样式
     */
    private String listClass;

    /**
     * 是否默认值(0-否,1-是)
     */
    private Integer isDefault;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态(0-禁用 1-启用)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
