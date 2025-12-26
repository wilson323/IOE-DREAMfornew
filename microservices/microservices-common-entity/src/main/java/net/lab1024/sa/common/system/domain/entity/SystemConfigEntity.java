package net.lab1024.sa.common.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 系统配置实体类
 * <p>
 * 对应数据库表: t_system_config
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_system_config")
public class SystemConfigEntity extends BaseEntity {

    /**
     * 配置ID（主键）
     */
    @TableId(value = "config_id", type = IdType.AUTO)
    private Long configId;

    /**
     * 配置键
     */
    @TableField("config_key")
    private String configKey;

    /**
     * 配置值
     */
    @TableField("config_value")
    private String configValue;

    /**
     * 配置名称
     */
    @TableField("config_name")
    private String configName;

    /**
     * 配置描述
     */
    @TableField("description")
    private String description;

    /**
     * 配置类型：SYSTEM-系统 BUSINESS-业务
     */
    @TableField("config_type")
    private String configType;

    /**
     * 是否加密：0-否 1-是
     */
    @TableField("is_encrypted")
    private Integer isEncrypted;
}
