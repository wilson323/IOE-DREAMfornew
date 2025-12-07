package net.lab1024.sa.common.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 配置创建DTO
 * 整合自ioedream-system-service
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自system-service）
 */
@Data
public class ConfigCreateDTO {

    @NotBlank(message = "配置键不能为空")
    private String configKey;

    @NotBlank(message = "配置值不能为空")
    private String configValue;

    @NotBlank(message = "配置名称不能为空")
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

