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

    @TableId(value = "config_id", type = IdType.AUTO)
    private Long configId;

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
}

