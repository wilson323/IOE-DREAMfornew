package net.lab1024.sa.notification.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 通知模板实体
 * <p>
 * 管理各种通知消息的模板
 * 严格遵循repowiki规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 继承BaseEntity获取公共字段
 * - 支持多渠道模板管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Data
@Accessors(chain = true)
@TableName("t_notification_template")
public class NotificationTemplateEntity extends BaseEntity {

    /**
     * 模板ID
     */
    @TableId(value = "template_id", type = IdType.AUTO)
    private Long templateId;

    /**
     * 模板编码（唯一标识）
     */
    private String templateCode;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 适用渠道：1-邮件 2-短信 3-微信 4-站内信 5-推送 6-语音
     */
    private Integer channel;

    /**
     * 消息类型：1-系统通知 2-业务通知 3-告警通知 4-营销通知 5-验证码
     */
    private Integer messageType;

    /**
     * 模板主题
     */
    private String subject;

    /**
     * 模板内容（支持变量占位符，如${userName}、${content}等）
     */
    private String content;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 模板参数定义（JSON格式，定义模板中可用的变量）
     */
    private String paramDefinition;

    /**
     * 模板状态：1-启用 0-禁用
     */
    private Integer status = 1;

    /**
     * 是否为默认模板：1-是 0-否
     */
    private Integer isDefault = 0;

    /**
     * 模板版本号
     */
    private String templateVersion = "1.0";

    /**
     * 优先级（数值越大优先级越高）
     */
    private Integer priority = 0;

    /**
     * 标签（用于分类和搜索）
     */
    private String tags;

    /**
     * 语言类型：zh-CN、en-US等
     */
    private String language = "zh-CN";

    /**
     * 使用次数统计
     */
    private Long usageCount = 0L;

    /**
     * 最后使用时间
     */
    private java.time.LocalDateTime lastUsedTime;

    /**
     * 审批状态：0-草稿 1-待审批 2-已审批 3-已拒绝
     */
    private Integer approvalStatus = 2;

    /**
     * 审批人ID
     */
    private Long approvalBy;

    /**
     * 审批时间
     */
    private java.time.LocalDateTime approvalTime;

    /**
     * 审批意见
     */
    private String approvalComment;

    /**
     * 扩展配置（JSON格式，用于特殊渠道的额外配置）
     */
    private String extensions;

    /**
     * 获取渠道描述
     */
    public String getChannelDesc() {
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
     * 获取审批状态描述
     */
    public String getApprovalStatusDesc() {
        switch (approvalStatus) {
            case 0:
                return "草稿";
            case 1:
                return "待审批";
            case 2:
                return "已审批";
            case 3:
                return "已拒绝";
            default:
                return "未知状态";
        }
    }

    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 是否为默认模板
     */
    public boolean isDefaultTemplate() {
        return isDefault != null && isDefault == 1;
    }

    /**
     * 是否已审批
     */
    public boolean isApproved() {
        return approvalStatus != null && approvalStatus == 2;
    }

    /**
     * 增加使用次数
     */
    public void incrementUsageCount() {
        this.usageCount = (this.usageCount == null ? 0L : this.usageCount) + 1;
        this.lastUsedTime = java.time.LocalDateTime.now();
    }

    /**
     * 检查模板是否可用于指定渠道和类型
     */
    public boolean isApplicable(Integer targetChannel, Integer targetMessageType) {
        return isEnabled() && isApproved() &&
               channel.equals(targetChannel) && messageType.equals(targetMessageType);
    }
}