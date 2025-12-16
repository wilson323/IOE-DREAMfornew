package net.lab1024.sa.common.notification.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 通知配置查询表单
 * <p>
 * 用于查询通知配置的请求表单
 * 严格遵循CLAUDE.md规范:
 * - Form类用于接收前端请求参数
 * - 支持分页查询
 * - 完整的Swagger文档注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "通知配置查询表单")
public class NotificationConfigQueryForm {

    /**
     * 配置键（模糊查询）
     */
    @Size(max = 200, message = "配置键长度不能超过200个字符")
    @Schema(description = "配置键（模糊查询）", example = "ALERT.CHANNEL")
    private String configKey;

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
    @Size(max = 50, message = "配置类型长度不能超过50个字符")
    @Schema(description = "配置类型", example = "ALERT")
    private String configType;

    /**
     * 状态
     * <p>
     * 1-启用（配置生效）
     * 2-禁用（配置不生效）
     * null-查询所有状态
     * </p>
     */
    @Schema(description = "状态：1-启用 2-禁用", example = "1")
    private Integer status;

    /**
     * 当前页码
     * <p>
     * 默认值为1
     * </p>
     */
    @Schema(description = "当前页码", example = "1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     * <p>
     * 默认值为10
     * </p>
     */
    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
}

