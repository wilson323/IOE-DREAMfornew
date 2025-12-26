package net.lab1024.sa.common.notification.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 通知配置实体
 * <p>
 * 对应数据库表: t_notification_config
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_notification_config")
public class NotificationConfigEntity extends BaseEntity {

    @TableId(value = "config_id", type = IdType.AUTO)
    private Long configId;

    @TableField("config_type")
    private String configType;

    @TableField("config_name")
    private String configName;

    @TableField("config_key")
    private String configKey;

    @TableField("config_value")
    private String configValue;

    @TableField("config_json")
    private String configJson;

    @TableField("config_desc")
    private String configDesc;

    @TableField("is_encrypted")
    private Integer isEncrypted;

    @TableField("status")
    private Integer status;

    @TableField("remark")
    private String remark;

    /**
     * 获取ID（兼容BaseEntity的getId方法）
     * <p>
     * 由于使用configId作为主键，需要提供getId方法以兼容代码中的entity.getId()调用
     * </p>
     *
     * @return 配置ID
     */
    public Long getId() {
        return configId;
    }

    /**
     * 设置ID（兼容BaseEntity的setId方法）
     *
     * @param id 配置ID
     */
    public void setId(Long id) {
        this.configId = id;
    }
}

