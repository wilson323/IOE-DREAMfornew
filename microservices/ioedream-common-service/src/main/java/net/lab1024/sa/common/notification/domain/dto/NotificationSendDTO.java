package net.lab1024.sa.common.notification.domain.dto;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通知发送DTO
 * <p>
 * 用于接收通知发送请求参数
 * 严格遵循CLAUDE.md规范:
 * - DTO类用于数据传输
 * - 使用@Valid进行参数验证
 * - 完整的Swagger文档注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "通知发送请求")
public class NotificationSendDTO {

    @NotNull(message = "通知类型不能为空")
    @Schema(description = "通知类型 (1-告警 2-系统 3-业务 9-测试)", example = "1")
    private Integer notificationType;

    @NotBlank(message = "通知标题不能为空")
    @Schema(description = "通知标题", example = "系统告警")
    private String title;

    @NotBlank(message = "通知内容不能为空")
    @Schema(description = "通知内容", example = "CPU使用率超过80%")
    private String content;

    @NotNull(message = "接收人类型不能为空")
    @Schema(description = "接收人类型 (1-指定用户 2-角色 3-部门)", example = "1")
    private Integer receiverType;

    @Schema(description = "接收人ID列表（逗号分隔）", example = "1,2,3")
    private String receiverIds;

    @NotNull(message = "通知渠道不能为空")
    @Schema(description = "通知渠道 (1-邮件 2-短信 3-Webhook 4-微信)", example = "1")
    private Integer channel;

    @Schema(description = "通知模板变量（用于模板替换）")
    private Map<String, Object> templateVariables;

    @Schema(description = "优先级 (1-高 2-普通 3-低)", example = "2")
    private Integer priority;
}
