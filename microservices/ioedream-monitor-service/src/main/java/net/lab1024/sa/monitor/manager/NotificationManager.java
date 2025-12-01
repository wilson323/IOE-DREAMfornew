package net.lab1024.sa.monitor.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.monitor.dao.NotificationDao;
import net.lab1024.sa.monitor.domain.entity.AlertEntity;
import net.lab1024.sa.monitor.domain.entity.NotificationEntity;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 通知管理器
 *
 * 负责告警通知发送、通知渠道管理、重试机制等功能
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Component
public class NotificationManager {

    @Resource
    private NotificationDao notificationDao;

    @Resource
    private EmailNotificationManager emailNotificationManager;

    @Resource
    private SmsNotificationManager smsNotificationManager;

    @Resource
    private WebhookNotificationManager webhookNotificationManager;

    @Resource
    private WechatNotificationManager wechatNotificationManager;

    // 异步执行器
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    // 定时任务执行器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    /**
     * 发送告警通知
     *
     * @param alert 告警信息
     */
    public void sendAlertNotification(AlertEntity alert) {
        log.info("开始发送告警通知，告警ID：{}，标题：{}", alert.getAlertId(), alert.getAlertTitle());

        try {
            // 创建通知记录
            List<NotificationEntity> notifications = createNotifications(alert);

            // 异步发送通知
            for (NotificationEntity notification : notifications) {
                CompletableFuture.runAsync(() -> sendNotification(notification), executorService)
                        .exceptionally(throwable -> {
                            log.error("发送通知失败，通知ID：{}", notification.getNotificationId(), throwable);
                            handleNotificationFailure(notification);
                            return null;
                        });
            }

        } catch (Exception e) {
            log.error("创建告警通知失败，告警ID：{}", alert.getAlertId(), e);
        }
    }

    /**
     * 处理待发送通知
     */
    public void processPendingNotifications() {
        log.debug("处理待发送通知");

        try {
            List<NotificationEntity> pendingNotifications = notificationDao.selectPendingNotifications();

            for (NotificationEntity notification : pendingNotifications) {
                CompletableFuture.runAsync(() -> sendNotification(notification), executorService)
                        .exceptionally(throwable -> {
                            log.error("发送通知失败，通知ID：{}", notification.getNotificationId(), throwable);
                            handleNotificationFailure(notification);
                            return null;
                        });
            }

            if (!pendingNotifications.isEmpty()) {
                log.info("处理待发送通知完成，数量：{}", pendingNotifications.size());
            }

        } catch (Exception e) {
            log.error("处理待发送通知失败", e);
        }
    }

    /**
     * 处理需要重试的通知
     */
    public void processRetryNotifications() {
        log.debug("处理需要重试的通知");

        try {
            List<NotificationEntity> retryNotifications = notificationDao.selectRetryNotifications(LocalDateTime.now());

            for (NotificationEntity notification : retryNotifications) {
                CompletableFuture.runAsync(() -> retryNotification(notification), executorService)
                        .exceptionally(throwable -> {
                            log.error("重试发送通知失败，通知ID：{}", notification.getNotificationId(), throwable);
                            handleNotificationFailure(notification);
                            return null;
                        });
            }

            if (!retryNotifications.isEmpty()) {
                log.info("处理重试通知完成，数量：{}", retryNotifications.size());
            }

        } catch (Exception e) {
            log.error("处理重试通知失败", e);
        }
    }

    /**
     * 获取通知统计信息
     *
     * @param hours 时间范围（小时）
     * @return 统计信息
     */
    public Map<String, Object> getNotificationStatistics(Integer hours) {
        log.debug("获取通知统计信息，时间范围：{}小时", hours);

        try {
            List<Map<String, Object>> stats = notificationDao.selectNotificationStats(hours);

            Map<String, Object> result = new java.util.HashMap<>();

            for (Map<String, Object> stat : stats) {
                String status = (String) stat.get("status");
                Long count = (Long) stat.get("count");
                result.put(status.toLowerCase() + "Count", count);
            }

            return result;

        } catch (Exception e) {
            log.error("获取通知统计信息失败", e);
            return new java.util.HashMap<>();
        }
    }

    /**
     * 清理历史通知数据
     *
     * @param days 保留天数
     */
    public void cleanHistoryNotifications(Integer days) {
        log.info("开始清理{}天前的历史通知数据", days);

        try {
            LocalDateTime beforeTime = LocalDateTime.now().minusDays(days);
            int deletedCount = notificationDao.deleteHistoryNotifications(beforeTime);

            log.info("历史通知数据清理完成，删除记录数：{}", deletedCount);

        } catch (Exception e) {
            log.error("清理历史通知数据失败", e);
        }
    }

    /**
     * 手动发送测试通知
     *
     * @param notificationType 通知类型
     * @param recipient        接收人
     * @param title            标题
     * @param content          内容
     * @return 发送结果
     */
    public Map<String, Object> sendTestNotification(String notificationType, String recipient, String title, String content) {
        log.info("发送测试通知，类型：{}，接收人：{}", notificationType, recipient);

        Map<String, Object> result = new java.util.HashMap<>();

        try {
            NotificationEntity testNotification = new NotificationEntity();
            testNotification.setNotificationType(notificationType);
            testNotification.setRecipient(recipient);
            testNotification.setNotificationTitle(title);
            testNotification.setNotificationContent(content);
            testNotification.setSendStatus("PENDING");
            testNotification.setRetryCount(0);
            testNotification.setMaxRetryCount(3);
            testNotification.setCreateTime(LocalDateTime.now());

            boolean success = sendSingleNotification(testNotification);

            result.put("success", success);
            result.put("message", success ? "测试通知发送成功" : "测试通知发送失败");
            result.put("timestamp", LocalDateTime.now());

        } catch (Exception e) {
            log.error("发送测试通知失败", e);
            result.put("success", false);
            result.put("message", "测试通知发送失败：" + e.getMessage());
            result.put("timestamp", LocalDateTime.now());
        }

        return result;
    }

    // 私有方法实现

    private List<NotificationEntity> createNotifications(AlertEntity alert) {
        List<NotificationEntity> notifications = new java.util.ArrayList<>();

        // 根据告警规则配置创建不同类型的通知
        // 这里简化处理，实际应该根据告警规则的通知配置来创建

        // 邮件通知
        if (shouldSendEmailNotification(alert)) {
            NotificationEntity emailNotification = createEmailNotification(alert);
            notifications.add(emailNotification);
        }

        // 短信通知
        if (shouldSendSmsNotification(alert)) {
            NotificationEntity smsNotification = createSmsNotification(alert);
            notifications.add(smsNotification);
        }

        // Webhook通知
        if (shouldSendWebhookNotification(alert)) {
            NotificationEntity webhookNotification = createWebhookNotification(alert);
            notifications.add(webhookNotification);
        }

        // 微信通知
        if (shouldSendWechatNotification(alert)) {
            NotificationEntity wechatNotification = createWechatNotification(alert);
            notifications.add(wechatNotification);
        }

        return notifications;
    }

    private void sendNotification(NotificationEntity notification) {
        log.debug("发送通知，ID：{}，类型：{}", notification.getNotificationId(), notification.getNotificationType());

        boolean success = sendSingleNotification(notification);

        if (success) {
            // 更新发送成功状态
            updateNotificationSuccess(notification);
        } else {
            // 处理发送失败
            handleNotificationFailure(notification);
        }
    }

    private boolean sendSingleNotification(NotificationEntity notification) {
        try {
            switch (notification.getNotificationType().toUpperCase()) {
                case "EMAIL":
                    return emailNotificationManager.sendEmail(notification);
                case "SMS":
                    return smsNotificationManager.sendSms(notification);
                case "WEBHOOK":
                    return webhookNotificationManager.sendWebhook(notification);
                case "WECHAT":
                    return wechatNotificationManager.sendWechat(notification);
                default:
                    log.warn("不支持的通知类型：{}", notification.getNotificationType());
                    return false;
            }
        } catch (Exception e) {
            log.error("发送通知异常，类型：{}", notification.getNotificationType(), e);
            return false;
        }
    }

    private void retryNotification(NotificationEntity notification) {
        log.debug("重试发送通知，ID：{}，重试次数：{}", notification.getNotificationId(), notification.getRetryCount());

        boolean success = sendSingleNotification(notification);

        if (success) {
            updateNotificationSuccess(notification);
        } else {
            handleNotificationFailure(notification);
        }
    }

    private void handleNotificationFailure(NotificationEntity notification) {
        int retryCount = notification.getRetryCount() + 1;
        int maxRetryCount = notification.getMaxRetryCount() != null ? notification.getMaxRetryCount() : 3;

        if (retryCount >= maxRetryCount) {
            // 超过最大重试次数，标记为发送失败
            updateNotificationFailed(notification);
        } else {
            // 安排下次重试
            scheduleRetry(notification, retryCount);
        }
    }

    private void updateNotificationSuccess(NotificationEntity notification) {
        notification.setSendStatus("SENT");
        notification.setSendTime(LocalDateTime.now());
        notificationDao.updateSendStatus(notification.getNotificationId(), "SENT",
                                        LocalDateTime.now(), null, null, null);
    }

    private void updateNotificationFailed(NotificationEntity notification) {
        notification.setSendStatus("FAILED");
        notification.setRetryCount(notification.getMaxRetryCount());
        notificationDao.updateSendStatus(notification.getNotificationId(), "FAILED",
                                        LocalDateTime.now(), "Max retry exceeded",
                                        notification.getMaxRetryCount(), null);
    }

    private void scheduleRetry(NotificationEntity notification, int retryCount) {
        // 计算下次重试时间（指数退避）
        long delayMinutes = (long) Math.pow(2, retryCount) * 5; // 5, 10, 20, 40...
        LocalDateTime nextRetryTime = LocalDateTime.now().plusMinutes(delayMinutes);

        notification.setRetryCount(retryCount);
        notification.setNextRetryTime(nextRetryTime);

        notificationDao.updateSendStatus(notification.getNotificationId(), "PENDING",
                                        null, "Retry scheduled", retryCount, nextRetryTime);

        // 安排重试任务
        scheduler.schedule(() -> {
            retryNotification(notification);
        }, delayMinutes, java.util.concurrent.TimeUnit.MINUTES);
    }

    private NotificationEntity createEmailNotification(AlertEntity alert) {
        NotificationEntity notification = new NotificationEntity();
        notification.setAlertId(alert.getAlertId());
        notification.setNotificationType("EMAIL");
        notification.setNotificationTitle("【告警】" + alert.getAlertTitle());
        notification.setNotificationContent(buildEmailContent(alert));
        notification.setRecipient(getEmailRecipients(alert));
        notification.setRecipientType("USER");
        notification.setSendStatus("PENDING");
        notification.setRetryCount(0);
        notification.setMaxRetryCount(3);
        notification.setPriority(mapAlertLevelToPriority(alert.getAlertLevel()));
        notification.setCreateTime(LocalDateTime.now());
        notification.setDeletedFlag(0);
        return notification;
    }

    private NotificationEntity createSmsNotification(AlertEntity alert) {
        NotificationEntity notification = new NotificationEntity();
        notification.setAlertId(alert.getAlertId());
        notification.setNotificationType("SMS");
        notification.setNotificationTitle(alert.getAlertTitle());
        notification.setNotificationContent(buildSmsContent(alert));
        notification.setRecipient(getSmsRecipients(alert));
        notification.setRecipientType("USER");
        notification.setSendStatus("PENDING");
        notification.setRetryCount(0);
        notification.setMaxRetryCount(3);
        notification.setPriority(mapAlertLevelToPriority(alert.getAlertLevel()));
        notification.setCreateTime(LocalDateTime.now());
        notification.setDeletedFlag(0);
        return notification;
    }

    private NotificationEntity createWebhookNotification(AlertEntity alert) {
        NotificationEntity notification = new NotificationEntity();
        notification.setAlertId(alert.getAlertId());
        notification.setNotificationType("WEBHOOK");
        notification.setNotificationTitle("【告警】" + alert.getAlertTitle());
        notification.setNotificationContent(buildWebhookContent(alert));
        notification.setRecipient(getWebhookUrl(alert));
        notification.setRecipientType("URL");
        notification.setSendStatus("PENDING");
        notification.setRetryCount(0);
        notification.setMaxRetryCount(5);
        notification.setPriority(mapAlertLevelToPriority(alert.getAlertLevel()));
        notification.setCreateTime(LocalDateTime.now());
        notification.setDeletedFlag(0);
        return notification;
    }

    private NotificationEntity createWechatNotification(AlertEntity alert) {
        NotificationEntity notification = new NotificationEntity();
        notification.setAlertId(alert.getAlertId());
        notification.setNotificationType("WECHAT");
        notification.setNotificationTitle("【告警】" + alert.getAlertTitle());
        notification.setNotificationContent(buildWechatContent(alert));
        notification.setRecipient(getWechatRecipients(alert));
        notification.setRecipientType("GROUP");
        notification.setSendStatus("PENDING");
        notification.setRetryCount(0);
        notification.setMaxRetryCount(3);
        notification.setPriority(mapAlertLevelToPriority(alert.getAlertLevel()));
        notification.setCreateTime(LocalDateTime.now());
        notification.setDeletedFlag(0);
        return notification;
    }

    // 辅助方法

    private boolean shouldSendEmailNotification(AlertEntity alert) {
        return !"INFO".equals(alert.getAlertLevel());
    }

    private boolean shouldSendSmsNotification(AlertEntity alert) {
        return "CRITICAL".equals(alert.getAlertLevel());
    }

    private boolean shouldSendWebhookNotification(AlertEntity alert) {
        return true; // 默认发送webhook通知
    }

    private boolean shouldSendWechatNotification(AlertEntity alert) {
        return !"INFO".equals(alert.getAlertLevel());
    }

    private String getEmailRecipients(AlertEntity alert) {
        // 实际应该从配置或数据库获取
        return "admin@ioe-dream.com";
    }

    private String getSmsRecipients(AlertEntity alert) {
        // 实际应该从配置或数据库获取
        return "13800138000";
    }

    private String getWebhookUrl(AlertEntity alert) {
        // 实际应该从配置获取
        return "https://hooks.slack.com/services/xxx";
    }

    private String getWechatRecipients(AlertEntity alert) {
        // 实际应该从配置获取
        return "ops_group";
    }

    private String buildEmailContent(AlertEntity alert) {
        return String.format(
            "告警详情：\n" +
            "标题：%s\n" +
            "描述：%s\n" +
            "级别：%s\n" +
            "服务：%s\n" +
            "时间：%s\n" +
            "当前值：%s\n" +
            "阈值：%s",
            alert.getAlertTitle(),
            alert.getAlertDescription(),
            alert.getAlertLevel(),
            alert.getServiceName(),
            alert.getAlertTime(),
            alert.getAlertValue(),
            alert.getThresholdValue()
        );
    }

    private String buildSmsContent(AlertEntity alert) {
        return String.format(
            "【IOE-DREAM】%s：%s，服务：%s，时间：%s",
            alert.getAlertLevel(),
            alert.getAlertTitle(),
            alert.getServiceName(),
            alert.getAlertTime()
        );
    }

    private String buildWebhookContent(AlertEntity alert) {
        // 返回JSON格式的webhook内容
        return String.format(
            "{\"alert_id\":%d,\"title\":\"%s\",\"level\":\"%s\",\"service\":\"%s\",\"time\":\"%s\"}",
            alert.getAlertId(),
            alert.getAlertTitle(),
            alert.getAlertLevel(),
            alert.getServiceName(),
            alert.getAlertTime()
        );
    }

    private String buildWechatContent(AlertEntity alert) {
        return String.format(
            "🚨 **%s**\n\n" +
            "告警标题：%s\n" +
            "告警级别：%s\n" +
            "影响服务：%s\n" +
            "告警时间：%s\n" +
            "告警描述：%s",
            alert.getAlertLevel(),
            alert.getAlertTitle(),
            alert.getAlertLevel(),
            alert.getServiceName(),
            alert.getAlertTime(),
            alert.getAlertDescription()
        );
    }

    private String mapAlertLevelToPriority(String alertLevel) {
        switch (alertLevel) {
            case "CRITICAL":
                return "URGENT";
            case "ERROR":
                return "HIGH";
            case "WARNING":
                return "NORMAL";
            case "INFO":
            default:
                return "LOW";
        }
    }
}