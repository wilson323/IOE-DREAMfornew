package net.lab1024.sa.common.notification.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 通知配置新增表单
 * <p>
 * 用于新增通知配置的请求表单
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
@Schema(description = "通知配置新增表单")
public class NotificationConfigAddForm {

    /**
     * 配置键
     * <p>
     * 唯一标识配置项，格式：{CONFIG_TYPE}.{KEY_NAME}
     * 例如：ALERT.CHANNEL.DINGTALK.ENABLED、DINGTALK.WEBHOOK_URL
     * </p>
     */
    @NotBlank(message = "配置键不能为空")
    @Size(max = 200, message = "配置键长度不能超过200个字符")
    @Schema(description = "配置键", example = "ALERT.CHANNEL.DINGTALK.ENABLED", required = true)
    private String configKey;

    /**
     * 配置值
     * <p>
     * 配置的具体值，如果isEncrypted=1，则需要在Service层加密后存储
     * 支持JSON格式存储复杂配置
     * </p>
     */
    @NotBlank(message = "配置值不能为空")
    @Size(max = 2000, message = "配置值长度不能超过2000个字符")
    @Schema(description = "配置值", example = "true", required = true)
    private String configValue;

    /**
     * 配置类型
     * <p>
     * 通知渠道类型：
     * - ALERT：告警配置
     * - EMAIL：邮件配置
     * - SMS：短信配置
     * - DINGTALK：钉钉配置
     * - WECHAT：企业微信配置
     * - WEBHOOK：Webhook配置
     * </p>
     */
    @NotBlank(message = "配置类型不能为空")
    @Size(max = 50, message = "配置类型长度不能超过50个字符")
    @Schema(description = "配置类型", example = "ALERT", required = true)
    private String configType;

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
     * 是否加密
     * <p>
     * 0-未加密（明文存储）
     * 1-已加密（加密存储，读取时需要解密）
     * 敏感配置如密码、密钥等必须加密存储
     * </p>
     */
    @NotNull(message = "是否加密不能为空")
    @Schema(description = "是否加密：0-否 1-是", example = "0", required = true)
    private Integer isEncrypted;

    /**
     * 状态
     * <p>
     * 1-启用（配置生效）
     * 2-禁用（配置不生效）
     * </p>
     */
    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：1-启用 2-禁用", example = "1", required = true)
    private Integer status;
}

