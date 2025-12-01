package net.lab1024.sa.notification.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 通知配置实体
 * <p>
 * 管理用户的通知偏好和系统配置
 * 严格遵循repowiki规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 继承BaseEntity获取公共字段
 * - 支持用户级和系统级配置
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_notification_config")
public class NotificationConfigEntity extends BaseEntity {

    /**
     * 配置ID
     */
    @TableId(value = "config_id", type = IdType.AUTO)
    private Long configId;

    /**
     * 用户ID（用户级配置时使用）
     */
    private Long userId;

    /**
     * 配置类型：1-系统配置 2-用户配置
     */
    private Integer configType;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 适用渠道：1-邮件 2-短信 3-微信 4-站内信 5-推送 6-语音，0表示所有渠道
     */
    private Integer channel;

    /**
     * 适用消息类型：1-系统通知 2-业务通知 3-告警通知 4-营销通知 5-验证码，0表示所有类型
     */
    private Integer messageType;

    /**
     * 配置状态：1-启用 0-禁用
     */
    private Integer status = 1;

    /**
     * 是否为默认配置：1-是 0-否
     */
    private Integer isDefault = 0;

    /**
     * 配置分类
     */
    private String category;

    /**
     * 数据类型：string、number、boolean、json
     */
    private String dataType = "string";

    /**
     * 验证规则（JSON格式）
     */
    private String validationRules;

    /**
     * 排序权重
     */
    private Integer sortOrder = 0;

    /**
     * 是否只读：1-是 0-否
     */
    private Integer readonly = 0;

    /**
     * 扩展属性（JSON格式）
     */
    private String extensions;

    /**
     * 获取配置类型描述
     */
    public String getConfigTypeDesc() {
        return configType != null && configType == 1 ? "系统配置" : "用户配置";
    }

    /**
     * 获取渠道描述
     */
    public String getChannelDesc() {
        if (channel == null || channel == 0) {
            return "所有渠道";
        }
        switch (channel) {
            case 1:
                return "邮件";
            case 2:
                return "短信";
            case 3:
                return "微信";
            case 4:
                return "站内信";
            case 5:
                return "推送";
            case 6:
                return "语音";
            default:
                return "未知渠道";
        }
    }

    /**
     * 获取消息类型描述
     */
    public String getMessageTypeDesc() {
        if (messageType == null || messageType == 0) {
            return "所有类型";
        }
        switch (messageType) {
            case 1:
                return "系统通知";
            case 2:
                return "业务通知";
            case 3:
                return "告警通知";
            case 4:
                return "营销通知";
            case 5:
                return "验证码";
            default:
                return "未知类型";
        }
    }

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        return status != null && status == 1 ? "启用" : "禁用";
    }

    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 是否为默认配置
     */
    public boolean isDefaultConfig() {
        return isDefault != null && isDefault == 1;
    }

    /**
     * 是否只读
     */
    public boolean isReadonly() {
        return readonly != null && readonly == 1;
    }

    /**
     * 是否适用于指定渠道
     */
    public boolean isApplicableToChannel(Integer targetChannel) {
        return channel == null || channel == 0 || channel.equals(targetChannel);
    }

    /**
     * 是否适用于指定消息类型
     */
    public boolean isApplicableToMessageType(Integer targetMessageType) {
        return messageType == null || messageType == 0 || messageType.equals(targetMessageType);
    }

    /**
     * 是否适用于指定用户
     */
    public boolean isApplicableToUser(Long targetUserId) {
        return configType == null || configType == 1 || (configType == 2 && userId != null && userId.equals(targetUserId));
    }

    /**
     * 获取布尔类型的配置值
     */
    public Boolean getBooleanValue() {
        if ("boolean".equals(dataType)) {
            return Boolean.valueOf(configValue);
        }
        return null;
    }

    /**
     * 获取数字类型的配置值
     */
    public Integer getIntValue() {
        if ("number".equals(dataType)) {
            try {
                return Integer.parseInt(configValue);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取字符串类型的配置值
     */
    public String getStringValue() {
        return configValue;
    }
}