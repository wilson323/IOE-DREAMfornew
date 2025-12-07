package net.lab1024.sa.common.notification.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 通知配置实体
 * <p>
 * 用于管理各种通知渠道的配置信息
 * 严格遵循CLAUDE.md规范:
 * - 继承BaseEntity提供审计字段
 * - 使用MyBatis-Plus注解
 * - 使用jakarta.validation进行参数验证
 * - 支持配置加密存储
 * </p>
 * <p>
 * 企业级特性：
 * - 配置加密存储（敏感信息如密码、密钥等）
 * - 配置状态管理（启用/禁用）
 * - 配置类型分类（EMAIL/SMS/DINGTALK/WECHAT/WEBHOOK）
 * - 配置版本管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_notification_config")
@Schema(description = "通知配置实体")
public class NotificationConfigEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     * <p>
     * 主键，使用雪花算法生成
     * </p>
     */
    @TableId(value = "config_id", type = IdType.ASSIGN_ID)
    @Schema(description = "配置ID")
    private Long configId;

    /**
     * 配置键
     * <p>
     * 唯一标识配置项，格式：{CONFIG_TYPE}.{KEY_NAME}
     * 例如：DINGTALK.WEBHOOK_URL、EMAIL.SMTP_HOST
     * </p>
     */
    @NotBlank(message = "配置键不能为空")
    @TableField("config_key")
    @Schema(description = "配置键", example = "DINGTALK.WEBHOOK_URL")
    private String configKey;

    /**
     * 配置值
     * <p>
     * 配置的具体值，如果isEncrypted=1，则为加密后的值
     * 支持JSON格式存储复杂配置
     * </p>
     */
    @NotBlank(message = "配置值不能为空")
    @TableField("config_value")
    @Schema(description = "配置值")
    private String configValue;

    /**
     * 配置类型
     * <p>
     * 通知渠道类型：
     * - EMAIL：邮件配置
     * - SMS：短信配置
     * - DINGTALK：钉钉配置
     * - WECHAT：企业微信配置
     * - WEBHOOK：Webhook配置
     * </p>
     */
    @NotBlank(message = "配置类型不能为空")
    @TableField("config_type")
    @Schema(description = "配置类型", example = "DINGTALK")
    private String configType;

    /**
     * 配置描述
     * <p>
     * 配置项的说明信息，便于管理员理解配置用途
     * </p>
     */
    @TableField("config_desc")
    @Schema(description = "配置描述")
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
    @TableField("is_encrypted")
    @Schema(description = "是否加密：0-否 1-是", example = "1")
    private Integer isEncrypted;

    /**
     * 状态
     * <p>
     * 1-启用（配置生效）
     * 2-禁用（配置不生效）
     * </p>
     */
    @NotNull(message = "状态不能为空")
    @TableField("status")
    @Schema(description = "状态：1-启用 2-禁用", example = "1")
    private Integer status;

    /**
     * 创建时间
     * <p>
     * 继承自BaseEntity，自动填充
     * </p>
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     * <p>
     * 继承自BaseEntity，自动填充
     * </p>
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 删除标记
     * <p>
     * 继承自BaseEntity，使用软删除
     * </p>
     */
    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记：0-未删除 1-已删除")
    private Integer deletedFlag;
}
