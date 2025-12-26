package net.lab1024.sa.common.notification.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通知配置更新表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
public class NotificationConfigUpdateForm {

    @NotNull(message = "配置ID不能为空")
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
}
