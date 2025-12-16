package net.lab1024.sa.common.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 系统配置实体
 * 整合自ioedream-system-service
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自system-service）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_config")
public class SystemConfigEntity extends BaseEntity {

    /**
     * 配置ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列config_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "config_id", type = IdType.AUTO)
    private Long id;

    private String configKey;
    private String configValue;
    private String configName;
    private String configGroup;
    private String configType;
    private String defaultValue;
    private Integer isSystem;
    private Integer isEncrypt;
    private Integer isReadonly;
    private Integer status;
    private Integer sortOrder;
    private String validationRule;
    private String description;
    private String remark;

    /**
     * 获取配置描述
     */
    public String getConfigDesc() {
        return description;
    }

    /**
     * 设置配置描述
     */
    public void setConfigDesc(String configDesc) {
        this.description = configDesc;
    }
}

