package net.lab1024.sa.admin.module.system.area.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 区域配置实体类
 *
 * @author SmartAdmin
 * @date 2025-01-10
 */
@Data
@TableName("t_area_config")
public class AreaConfigEntity {

    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
    private Long configId;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 配置类型
     */
    private String configType;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置描述
     */
    private String configDesc;

    /**
     * 是否加密：1-是，0-否
     */
    private Integer isEncrypted;

    /**
     * 是否默认：1-是，0-否
     */
    private Integer isDefault;

    /**
     * 配置版本
     */
    private Integer version;

    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 状态：1-生效，0-失效
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 删除标志：0-未删除，1-已删除
     */
    private Integer deletedFlag;

}
