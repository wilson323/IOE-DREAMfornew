package net.lab1024.sa.common.notification.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 通知配置更新表单
 * <p>
 * 用于更新通知配置的请求表单
 * 严格遵循CLAUDE.md规范:
 * - Form类用于接收前端请求参数
 * - 使用jakarta.validation进行参数验证
 * - 完整的Swagger文档注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "通知配置更新表单")
public class NotificationConfigUpdateForm {

    /**
     * 配置ID
     * <p>
     * 主键，用于标识要更新的配置
     * </p>
     */
    @NotNull(message = "配置ID不能为空")
    @Schema(description = "配置ID", example = "1", required = true)
    private Long configId;

    /**
     * 配置值
     * <p>
     * 配置的具体值，如果isEncrypted=1，则需要在Service层加密后存储
     * 支持JSON格式存储复杂配置
     * </p>
     */
    @Size(max = 2000, message = "配置值长度不能超过2000个字符")
    @Schema(description = "配置值", example = "true")
    private String configValue;

    /**
     * 配置描述
     * <p>
     * 配置项的说明信息，便于管理员理解配置用途
     * </p>
     */
    @Size(max = 500, message = "配置描述长度不能超过500个字符")
    @Schema(description = "配置描述", example = "钉钉通知渠道启用状态")
    private String configDesc;

    /**
     * 状态
     * <p>
     * 1-启用（配置生效）
     * 2-禁用（配置不生效）
     * </p>
     */
    @Schema(description = "状态：1-启用 2-禁用", example = "1")
    private Integer status;
}

