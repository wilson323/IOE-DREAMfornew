package net.lab1024.sa.common.notification.domain.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通知配置新增表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
public class NotificationConfigAddForm {

    @NotBlank(message = "配置类型不能为空")
    private String configType;

    @NotBlank(message = "配置名称不能为空")
    private String configName;

    @NotBlank(message = "配置键不能为空")
    private String configKey;

    @NotBlank(message = "配置值不能为空")
    private String configValue;

    private String configJson;

    private String configDesc;

    @NotNull(message = "是否加密不能为空")
    private Integer isEncrypted;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private Integer sortOrder;
}
