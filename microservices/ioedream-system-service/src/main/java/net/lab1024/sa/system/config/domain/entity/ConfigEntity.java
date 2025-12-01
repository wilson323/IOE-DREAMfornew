package net.lab1024.sa.system.config.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 系统配置实体
 *
 * @author IOE-DREAM Team
 * @date 2025/11/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_sys_config")
public class ConfigEntity extends BaseEntity {

    /**
     * 配置ID
     */
    @TableId(value = "config_id", type = IdType.AUTO)
    private Long configId;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置分组
     */
    private String configGroup;

    /**
     * 配置类型(STRING-字符串,NUMBER-数字,BOOLEAN-布尔值,JSON-JSON对象)
     */
    private String configType;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 是否系统内置(0-否,1-是)
     */
    private Integer isSystem;

    /**
     * 是否加密(0-否,1-是)
     */
    private Integer isEncrypt;

    /**
     * 是否只读(0-否,1-是)
     */
    private Integer isReadonly;

    /**
     * 状态(0-禁用 1-启用)
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 验证规则
     */
    private String validationRule;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 备注
     */
    private String remark;
}
