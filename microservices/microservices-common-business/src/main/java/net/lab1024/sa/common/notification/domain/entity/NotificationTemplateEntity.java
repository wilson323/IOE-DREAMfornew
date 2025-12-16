package net.lab1024.sa.common.notification.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 通知模板实体类
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 继承BaseEntity（包含审计字段）
 * - 使用@TableName指定表名
 * - 使用@TableId指定主键
 * - 使用@TableLogic实现软删除
 * - 完整的Swagger文档注解
 * </p>
 * <p>
 * 企业级特性：
 * - 支持多种模板类型（邮件、短信、微信、站内信、推送）
 * - 支持变量替换（JSON格式存储变量列表）
 * - 支持模板状态管理（启用/禁用）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_notification_template")
@Schema(description = "通知模板实体")
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationTemplateEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 模板ID（主键）
     * <p>
     * 统一使用id作为主键字段名，通过@TableId的value属性映射到数据库列template_id
     * 符合实体类主键命名规范：统一使用id，避免方法引用错误
     * </p>
     */
    @TableId(value = "template_id", type = IdType.ASSIGN_ID)
    @Schema(description = "模板ID")
    private Long id;

    @NotBlank(message = "模板编码不能为空")
    @TableField("template_code")
    @Schema(description = "模板编码", example = "EMAIL_LOGIN_SUCCESS")
    private String templateCode;

    @NotBlank(message = "模板名称不能为空")
    @TableField("template_name")
    @Schema(description = "模板名称", example = "登录成功邮件模板")
    private String templateName;

    @NotNull(message = "模板类型不能为空")
    @TableField("template_type")
    @Schema(description = "模板类型：1-邮件 2-短信 3-微信 4-站内信 5-推送", example = "1")
    private Integer templateType;

    @TableField("subject")
    @Schema(description = "主题（邮件用）", example = "登录成功通知")
    private String subject;

    @NotBlank(message = "模板内容不能为空")
    @TableField("content")
    @Schema(description = "模板内容", example = "尊敬的{{userName}}，您已成功登录系统。")
    private String content;

    @TableField("variables")
    @Schema(description = "变量列表（JSON数组）", example = "[\"userName\", \"loginTime\"]")
    private String variables;

    @NotNull(message = "状态不能为空")
    @TableField("status")
    @Schema(description = "状态：1-启用 2-禁用", example = "1")
    private Integer status;

    @TableField("remark")
    @Schema(description = "备注")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记：0-未删除 1-已删除")
    private Integer deletedFlag;
}
