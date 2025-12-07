package net.lab1024.sa.common.system.domain.dto;

import lombok.Data;

/**
 * 配置更新DTO
 * 整合自ioedream-system-service
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自system-service）
 */
@Data
public class ConfigUpdateDTO {

    private String configValue;
    private String configName;
    private String configGroup;
    private String description;
    private Integer status;
    private Integer sortOrder;
}

