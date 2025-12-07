package net.lab1024.sa.common.notification.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 通知模板视图对象
 * <p>
 * 用于返回通知模板信息
 * 严格遵循CLAUDE.md规范:
 * - VO类用于视图展示
 * - 完整的Swagger文档注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "通知模板视图对象")
public class NotificationTemplateVO {

    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "模板编码", example = "EMAIL_LOGIN_SUCCESS")
    private String templateCode;

    @Schema(description = "模板名称", example = "登录成功邮件模板")
    private String templateName;

    @Schema(description = "模板类型：1-邮件 2-短信 3-微信 4-站内信 5-推送", example = "1")
    private Integer templateType;

    @Schema(description = "主题（邮件用）", example = "登录成功通知")
    private String subject;

    @Schema(description = "模板内容", example = "尊敬的{{userName}}，您已成功登录系统。")
    private String content;

    @Schema(description = "变量列表（JSON数组）", example = "[\"userName\", \"loginTime\"]")
    private String variables;

    @Schema(description = "状态：1-启用 2-禁用", example = "1")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
