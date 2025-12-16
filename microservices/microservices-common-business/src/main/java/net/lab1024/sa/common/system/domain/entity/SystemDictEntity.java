package net.lab1024.sa.common.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 系统字典实体
 * 整合自ioedream-system-service
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自system-service）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_dict_data")
public class SystemDictEntity extends BaseEntity {

    /**
     * 字典数据ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列dict_data_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "dict_data_id", type = IdType.AUTO)
    private Long id;

    private Long dictTypeId;
    private String dictTypeCode;
    private String dictLabel;
    private String dictValue;
    private String cssClass;
    private String listClass;
    private Integer isDefault;
    private Integer sortOrder;
    private Integer status;
    private String remark;
}

