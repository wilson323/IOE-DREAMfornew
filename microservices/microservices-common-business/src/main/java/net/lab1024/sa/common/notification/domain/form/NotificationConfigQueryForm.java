package net.lab1024.sa.common.notification.domain.form;

import lombok.Data;

/**
 * 通知配置查询表单
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
public class NotificationConfigQueryForm {

    private String configType;

    private String configName;

    private String configKey;

    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
