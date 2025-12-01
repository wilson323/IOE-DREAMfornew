package net.lab1024.sa.infrastructure.config.domain.vo;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 配置项VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Data
public class ConfigItemVO {

    /**
     * 配置ID
     */
    private Long configId;

    /**
     * 应用名称
     */
    @NotBlank(message = "应用名称不能为空")
    private String application;

    /**
     * 环境标识
     */
    @NotBlank(message = "环境标识不能为空")
    private String profile;

    /**
     * 标签
     */
    private String label;

    /**
     * 配置键
     */
    @NotBlank(message = "配置键不能为空")
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 配置类型
     */
    private String configType;

    /**
     * 是否加密
     */
    private Boolean encrypted;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 版本号
     */
    private Long version;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 是否启用
     */
    private Boolean enabled;
}