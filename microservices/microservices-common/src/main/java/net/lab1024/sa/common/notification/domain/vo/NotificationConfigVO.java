package net.lab1024.sa.common.notification.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 通知配置视图对象
 * <p>
 * 用于返回通知配置信息
 * 严格遵循CLAUDE.md规范:
 * - VO类用于视图展示
 * - 不包含敏感信息（加密后的值）
 * - 完整的Swagger文档注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "通知配置视图对象")
public class NotificationConfigVO {

    @Schema(description = "配置ID")
    private Long configId;

    @Schema(description = "配置键", example = "DINGTALK.WEBHOOK_URL")
    private String configKey;

    @Schema(description = "配置值（已解密，敏感信息脱敏）")
    private String configValue;

    @Schema(description = "配置类型", example = "DINGTALK")
    private String configType;

    @Schema(description = "配置描述")
    private String configDesc;

    @Schema(description = "是否加密：0-否 1-是", example = "1")
    private Integer isEncrypted;

    @Schema(description = "状态：1-启用 2-禁用", example = "1")
    private Integer status;
}
