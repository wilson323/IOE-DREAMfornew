package net.lab1024.sa.common.monitor.manager;

import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.monitor.domain.entity.NotificationEntity;
import net.lab1024.sa.common.notification.manager.EmailNotificationManager;
import net.lab1024.sa.common.notification.manager.SmsNotificationManager;
import net.lab1024.sa.common.notification.manager.WebhookNotificationManager;
import net.lab1024.sa.common.notification.manager.WechatNotificationManager;

/**
 * 通知管理器实现类（微服务层）
 * <p>
 * 继承microservices-common中的NotificationManager基类
 * 实现具体的通知发送逻辑
 * </p>
 * <p>
 * 架构说明：
 * - microservices-common.NotificationManager：提供框架和扩展点
 * - ioedream-common-service.NotificationManagerImpl：实现具体发送逻辑
 * - 符合"公共模块提供框架，微服务实现业务"的架构原则
 * </p>
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - 使用@Component注解标识Manager层
 * - 使用@Resource依赖注入（禁止@Autowired）
 * - 完整的异常处理和日志记录
 * - 支持4种通知渠道（邮件/短信/Webhook/微信）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-05
 */
@Slf4j
@Component("notificationManagerImpl")
public class NotificationManagerImpl extends net.lab1024.sa.common.monitor.manager.NotificationManager {

    @Resource
    private EmailNotificationManager emailNotificationManager;

    @Resource
    private SmsNotificationManager smsNotificationManager;

    @Resource
    private WebhookNotificationManager webhookNotificationManager;

    @Resource
    private WechatNotificationManager wechatNotificationManager;

    /**
     * 构造函数
     * <p>
     * 调用父类构造函数，传入依赖的DAO
     * 注入各种通知管理器
     * </p>
     *
     * @param notificationDao 通知DAO（由Spring容器注入）
     * @param alertRuleDao    告警规则DAO（由Spring容器注入）
     * @param emailNotificationManager 邮件通知管理器
     * @param smsNotificationManager 短信通知管理器
     * @param webhookNotificationManager Webhook通知管理器
     * @param wechatNotificationManager 微信通知管理器
     */
    public NotificationManagerImpl(
            net.lab1024.sa.common.monitor.dao.NotificationDao notificationDao,
            net.lab1024.sa.common.monitor.dao.AlertRuleDao alertRuleDao,
            EmailNotificationManager emailNotificationManager,
            SmsNotificationManager smsNotificationManager,
            WebhookNotificationManager webhookNotificationManager,
            WechatNotificationManager wechatNotificationManager) {
        super(notificationDao, alertRuleDao);
        this.emailNotificationManager = emailNotificationManager;
        this.smsNotificationManager = smsNotificationManager;
        this.webhookNotificationManager = webhookNotificationManager;
        this.wechatNotificationManager = wechatNotificationManager;
    }

    /**
     * 重写sendByChannel方法，实现具体的通知发送逻辑
     * <p>
     * 根据渠道类型调用对应的通知发送Manager：
     * - 1: 邮件通知
     * - 2: 短信通知
     * - 3: Webhook通知
     * - 4: 微信通知
     * </p>
     *
     * @param notification 通知信息
     * @param channel      渠道类型 (1-邮件 2-短信 3-Webhook 4-微信)
     * @return 发送是否成功
     */
    @Override
    protected boolean sendByChannel(NotificationEntity notification, Integer channel) {
        log.info("发送通知，通知ID：{}，渠道：{}，标题：{}",
                 notification.getNotificationId(), channel, notification.getTitle());

        boolean success = false;

        try {
            switch (channel) {
                case 1: // EMAIL
                    success = emailNotificationManager.sendEmail(notification);
                    break;
                case 2: // SMS
                    success = smsNotificationManager.sendSms(notification);
                    break;
                case 3: // WEBHOOK
                    success = webhookNotificationManager.sendWebhook(notification);
                    break;
                case 4: // WECHAT
                    success = wechatNotificationManager.sendWechat(notification);
                    break;
                default:
                    log.warn("不支持的通知渠道：{}，通知ID：{}", channel, notification.getNotificationId());
                    return false;
            }

            if (success) {
                log.info("通知发送成功，通知ID：{}，渠道：{}", notification.getNotificationId(), channel);
            } else {
                log.warn("通知发送失败，通知ID：{}，渠道：{}", notification.getNotificationId(), channel);
            }

            return success;

        } catch (Exception e) {
            log.error("发送通知异常，通知ID：{}，渠道：{}", notification.getNotificationId(), channel, e);
            return false;
        }
    }
}

