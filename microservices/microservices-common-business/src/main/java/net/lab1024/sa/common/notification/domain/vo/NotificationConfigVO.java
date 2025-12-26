package net.lab1024.sa.common.notification.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知配置视图对象
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
public class NotificationConfigVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long configId;

    private String configType;

    private String configName;

    private String configKey;

    private String configValue;

    private String configJson;

    private String configDesc;

    private Integer isEncrypted;

    private Integer status;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
