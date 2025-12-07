package net.lab1024.sa.common.system.domain.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 配置VO
 * 整合自ioedream-system-service
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自system-service）
 */
@Data
public class ConfigVO {

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
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

