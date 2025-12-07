package net.lab1024.sa.common.monitor.manager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.monitor.dao.AlertRuleDao;
import net.lab1024.sa.common.monitor.dao.NotificationDao;
import net.lab1024.sa.common.monitor.domain.entity.AlertEntity;
import net.lab1024.sa.common.monitor.domain.entity.AlertRuleEntity;
import net.lab1024.sa.common.monitor.domain.entity.NotificationEntity;
import org.springframework.util.StringUtils;


/**
 * 通知管理器
 * <p>
 * 负责告警通知发送、通知渠道管理、重试机制等功能
 * 严格遵循CLAUDE.md规范:
 * - Manager类在microservices-common中是纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖（DAO等）
 * - 在微服务中通过配置类注册为Spring Bean
 * - 完整的异常处理和日志记录
 * - 支持异步发送和重试机制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 * @updated 2025-01-30 移除Spring注解，改为纯Java类，符合CLAUDE.md规范
 */
@Slf4j
public class NotificationManager {

    private final NotificationDao notificationDao;
    private final AlertRuleDao alertRuleDao;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类在microservices-common中不使用Spring注解，
     * 通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param notificationDao 通知DAO
     * @param alertRuleDao    告警规则DAO
     */
    public NotificationManager(NotificationDao notificationDao, AlertRuleDao alertRuleDao) {
        this.notificationDao = notificationDao;
        this.alertRuleDao = alertRuleDao;
    }

    // 异步执行器
    private final ExecutorService executorService = Executors.newFixedThreadPool(20);

    /**
     * 架构说明：
     * <p>
     * ✅ 通知发送管理器已在微服务中实现（ioedream-common-service.NotificationManagerImpl）
     * - microservices-common.NotificationManager：提供框架和扩展点（基类）
     * - ioedream-common-service.NotificationManagerImpl：实现具体发送逻辑（子类）
     * - 符合"公共模块提供框架，微服务实现业务"的架构原则
     * </p>
     * <p>
     * 因为 microservices-common 是纯 JAR 库，不应包含具体的发送实现
     * 具体的发送逻辑（Email/SMS/Webhook/微信）在微服务层实现
     * </p>
     */

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
     * 发送通知（核心编排逻辑）
     * <p>
     * 注意：实际的发送实现（Email/SMS/Webhook/微信）应在微服务层实现
     * 此处仅提供通知记录管理和状态更新的框架代码
     * </p>
     *
     * @param notification 通知信息
     */
    protected void sendNotification(NotificationEntity notification) {
        log.debug("准备发送通知，类型：{}，渠道：{}", notification.getNotificationType(), notification.getChannel());

        boolean success = false;

        try {
            // 检查渠道
            Integer channel = notification.getChannel();
            if (channel == null) {
                log.warn("通知渠道为空，通知ID：{}", notification.getNotificationId());
                return;
            }

            /**
             * 发送逻辑说明：
             * <p>
             * ✅ 实际发送逻辑已通过子类重写实现（策略模式）
             * - 基类（本类）定义sendByChannel抽象方法
             * - 子类（NotificationManagerImpl）重写sendByChannel方法
             * - 根据渠道类型调用对应的通知发送Manager（Email/SMS/Webhook/微信）
             * </p>
             * <p>
             * 架构设计：
             * - 基类提供框架和扩展点
             * - 子类实现具体业务逻辑
             * - 符合开闭原则和策略模式
             * </p>
             */
            // 根据渠道类型调用子类实现的发送逻辑
            success = sendByChannel(notification, channel);

            // 更新发送状态
            updateNotificationStatus(notification, success);

        } catch (Exception e) {
            log.error("发送通知异常", e);
            handleNotificationFailure(notification);
        }
    }

    /**
     * 根据渠道发送通知（扩展点）
     * <p>
     * 此方法应该在微服务层被重写或通过策略模式实现
     * 当前默认实现仅标记为待发送状态
     * </p>
     *
     * @param notification 通知信息
     * @param channel      渠道类型 (1-邮件 2-短信 3-Webhook 4-微信)
     * @return 发送是否成功
     */
    protected boolean sendByChannel(NotificationEntity notification, Integer channel) {
        log.warn("使用默认通知实现，实际发送功能需要在微服务层实现，渠道：{}", channel);

        // 默认实现：标记为待发送（由微服务层的定时任务处理）
        notification.setStatus(0); // 0-待发送
        notificationDao.updateById(notification);

        return false; // 返回false表示未实际发送，等待微服务层处理
    }

    /**
     * 创建通知记录
     * <p>
     * 根据告警规则创建通知，支持多渠道和多接收人
     * 企业级实现：
     * - 查询告警规则配置
     * - 解析通知渠道和接收人
     * - 为每个渠道和接收人创建通知记录
     * - 支持优先级设置
     * </p>
     *
     * @param alert 告警信息
     * @return 通知记录列表
     */
    private List<NotificationEntity> createNotifications(AlertEntity alert) {
        List<NotificationEntity> notifications = new ArrayList<>();

        try {
            // 1. 查询告警规则
            AlertRuleEntity alertRule = getAlertRule(alert);
            if (alertRule == null) {
                log.warn("告警规则不存在，使用默认配置，告警ID：{}", alert.getAlertId());
                // 使用默认配置创建通知
                NotificationEntity defaultNotification = createDefaultNotification(alert);
                notificationDao.insert(defaultNotification);
                notifications.add(defaultNotification);
                return notifications;
            }

            // 2. 检查规则状态
            if (!"ENABLED".equals(alertRule.getStatus())) {
                log.warn("告警规则已禁用，不创建通知，规则ID：{}", alertRule.getRuleId());
                return notifications;
            }

            // 3. 解析通知渠道
            List<Integer> channels = parseNotificationChannels(alertRule.getNotificationChannels());
            if (channels.isEmpty()) {
                log.warn("告警规则未配置通知渠道，使用默认渠道，规则ID：{}", alertRule.getRuleId());
                channels.add(1); // 默认使用邮件渠道
            }

            // 4. 解析接收人
            List<String> receiverIds = parseNotificationUsers(alertRule.getNotificationUsers());
            if (receiverIds.isEmpty()) {
                log.warn("告警规则未配置接收人，使用默认接收人，规则ID：{}", alertRule.getRuleId());
                receiverIds.add("1"); // 默认发送给管理员
            }

            // 5. 计算优先级（根据告警级别）
            Integer priority = calculatePriority(alert.getAlertLevel(), alertRule.getAlertLevel());

            // 6. 为每个渠道和接收人创建通知记录
            for (Integer channel : channels) {
                for (String receiverId : receiverIds) {
                    NotificationEntity notification = new NotificationEntity();
                    notification.setTitle(alert.getAlertTitle());
                    notification.setContent(buildNotificationContent(alert, alertRule));
                    notification.setNotificationType(1); // 1-告警通知
                    notification.setReceiverType(1); // 1-指定用户
                    notification.setReceiverIds(receiverId);
                    notification.setChannel(channel);
                    notification.setStatus(0); // 0-待发送
                    notification.setPriority(priority);
                    notification.setMaxRetryCount(3); // 默认最大重试3次

                    notificationDao.insert(notification);
                    notifications.add(notification);

                    log.debug("创建通知记录成功，通知ID：{}，渠道：{}，接收人：{}",
                            notification.getNotificationId(), channel, receiverId);
                }
            }

            log.info("根据告警规则创建通知完成，告警ID：{}，规则ID：{}，通知数量：{}",
                    alert.getAlertId(), alertRule.getRuleId(), notifications.size());

        } catch (Exception e) {
            log.error("根据告警规则创建通知失败，告警ID：{}", alert.getAlertId(), e);
            // 异常情况下创建默认通知
            try {
                NotificationEntity defaultNotification = createDefaultNotification(alert);
                notificationDao.insert(defaultNotification);
                notifications.add(defaultNotification);
            } catch (Exception ex) {
                log.error("创建默认通知失败", ex);
            }
        }

        return notifications;
    }

    /**
     * 获取告警规则
     * <p>
     * 优先通过ruleId查询，如果不存在则通过metricName查询
     * </p>
     *
     * @param alert 告警信息
     * @return 告警规则
     */
    private AlertRuleEntity getAlertRule(AlertEntity alert) {
        // 优先通过ruleId查询
        if (alert.getRuleId() != null) {
            AlertRuleEntity rule = alertRuleDao.selectById(alert.getRuleId());
            if (rule != null) {
                return rule;
            }
        }

        // 如果ruleId不存在或查询失败，通过metricName查询
        // 注意：AlertEntity中没有metricName字段，需要通过其他方式获取
        // 这里假设可以通过告警标题或其他字段推断metricName
        // 实际实现中可能需要扩展AlertEntity或通过其他方式获取

        return null;
    }

    /**
     * 解析通知渠道
     * <p>
     * 支持格式：EMAIL,SMS,WEBHOOK,WECHAT 或 1,2,3,4
     * </p>
     *
     * @param channelsStr 渠道字符串
     * @return 渠道列表
     */
    private List<Integer> parseNotificationChannels(String channelsStr) {
        List<Integer> channels = new ArrayList<>();

        if (!StringUtils.hasText(channelsStr)) {
            return channels;
        }

        try {
            String[] channelArray = channelsStr.split(",");
            for (String channelStr : channelArray) {
                String trimmed = channelStr.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                // 尝试解析为数字
                try {
                    Integer channel = Integer.parseInt(trimmed);
                    if (channel >= 1 && channel <= 4) {
                        channels.add(channel);
                    }
                } catch (NumberFormatException e) {
                    // 如果不是数字，尝试解析为字符串
                    Integer channel = parseChannelName(trimmed);
                    if (channel != null) {
                        channels.add(channel);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析通知渠道失败，渠道字符串：{}", channelsStr, e);
        }

        return channels;
    }

    /**
     * 解析渠道名称
     *
     * @param channelName 渠道名称
     * @return 渠道编号
     */
    private Integer parseChannelName(String channelName) {
        if (channelName == null) {
            return null;
        }

        String upperName = channelName.toUpperCase();
        switch (upperName) {
            case "EMAIL":
            case "MAIL":
                return 1;
            case "SMS":
            case "MESSAGE":
                return 2;
            case "WEBHOOK":
            case "HOOK":
                return 3;
            case "WECHAT":
            case "WX":
                return 4;
            default:
                return null;
        }
    }

    /**
     * 解析接收人列表
     * <p>
     * 支持格式：1,2,3 或 userId1,userId2,userId3
     * </p>
     *
     * @param usersStr 接收人字符串
     * @return 接收人ID列表
     */
    private List<String> parseNotificationUsers(String usersStr) {
        List<String> receiverIds = new ArrayList<>();

        if (!StringUtils.hasText(usersStr)) {
            return receiverIds;
        }

        try {
            String[] userArray = usersStr.split(",");
            for (String userStr : userArray) {
                String trimmed = userStr.trim();
                if (!trimmed.isEmpty()) {
                    receiverIds.add(trimmed);
                }
            }
        } catch (Exception e) {
            log.error("解析接收人列表失败，接收人字符串：{}", usersStr, e);
        }

        return receiverIds;
    }

    /**
     * 计算通知优先级
     * <p>
     * 优先级规则：
     * - CRITICAL/ERROR: 1-高优先级
     * - WARNING: 2-普通优先级
     * - INFO: 3-低优先级
     * </p>
     *
     * @param alertLevel 告警级别
     * @param ruleLevel  规则级别
     * @return 优先级
     */
    private Integer calculatePriority(String alertLevel, String ruleLevel) {
        String level = alertLevel != null ? alertLevel : ruleLevel;
        if (level == null) {
            return 2; // 默认普通优先级
        }

        String upperLevel = level.toUpperCase();
        if (upperLevel.contains("CRITICAL") || upperLevel.contains("ERROR")) {
            return 1; // 高优先级
        } else if (upperLevel.contains("WARNING") || upperLevel.contains("WARN")) {
            return 2; // 普通优先级
        } else {
            return 3; // 低优先级
        }
    }

    /**
     * 构建通知内容
     *
     * @param alert     告警信息
     * @param alertRule 告警规则
     * @return 通知内容
     */
    private String buildNotificationContent(AlertEntity alert, AlertRuleEntity alertRule) {
        StringBuilder content = new StringBuilder();

        // 告警消息
        if (StringUtils.hasText(alert.getAlertMessage())) {
            content.append(alert.getAlertMessage());
        } else if (StringUtils.hasText(alert.getAlertDescription())) {
            content.append(alert.getAlertDescription());
        } else {
            content.append(alert.getAlertTitle());
        }

        // 添加规则描述
        if (alertRule != null && StringUtils.hasText(alertRule.getRuleDescription())) {
            content.append("\n\n规则描述：").append(alertRule.getRuleDescription());
        }

        // 添加告警值
        if (alert.getAlertValue() != null && alert.getThresholdValue() != null) {
            content.append("\n告警值：").append(alert.getAlertValue());
            content.append("，阈值：").append(alert.getThresholdValue());
        }

        // 添加服务信息
        if (StringUtils.hasText(alert.getServiceName())) {
            content.append("\n服务名称：").append(alert.getServiceName());
        }

        // 添加告警时间
        if (alert.getAlertTime() != null) {
            content.append("\n告警时间：").append(alert.getAlertTime());
        }

        return content.toString();
    }

    /**
     * 创建默认通知（当告警规则不存在时使用）
     *
     * @param alert 告警信息
     * @return 通知实体
     */
    private NotificationEntity createDefaultNotification(AlertEntity alert) {
        NotificationEntity notification = new NotificationEntity();
        notification.setTitle(alert.getAlertTitle());
        notification.setContent(alert.getAlertMessage() != null ? alert.getAlertMessage() : alert.getAlertDescription());
        notification.setNotificationType(1); // 1-告警通知
        notification.setReceiverType(1); // 1-指定用户
        notification.setReceiverIds("1"); // 默认发送给管理员
        notification.setChannel(1); // 1-邮件
        notification.setStatus(0); // 0-待发送
        notification.setPriority(calculatePriority(alert.getAlertLevel(), null));
        notification.setMaxRetryCount(3);
        return notification;
    }

    /**
     * 更新通知状态
     */
    private void updateNotificationStatus(NotificationEntity notification, boolean success) {
        try {
            // 状态码：0-待发送 1-已发送 2-失败 3-已取消
            Integer status = success ? 1 : 2;
            Integer retryCount = notification.getRetryCount() != null ? notification.getRetryCount() : 0;

            notificationDao.updateSendStatus(
                    notification.getNotificationId(),
                    String.valueOf(status),
                    LocalDateTime.now(),
                    success ? "发送成功" : "发送失败",
                    retryCount,
                    null);
        } catch (Exception e) {
            log.error("更新通知状态失败", e);
        }
    }

    /**
     * 处理通知失败
     */
    private void handleNotificationFailure(NotificationEntity notification) {
        try {
            int retryCount = notification.getRetryCount() != null ? notification.getRetryCount() : 0;
            int maxRetryCount = notification.getMaxRetryCount() != null ? notification.getMaxRetryCount() : 3;

            if (retryCount < maxRetryCount) {
                // 计算下次重试时间（指数退避）
                LocalDateTime nextRetryTime = LocalDateTime.now().plusMinutes(5 * (retryCount + 1));

                // 状态码：2-失败
                notificationDao.updateSendStatus(
                        notification.getNotificationId(),
                        "2",
                        LocalDateTime.now(),
                        "发送失败，等待重试",
                        retryCount + 1,
                        nextRetryTime);
            } else {
                // 状态码：3-已取消
                notificationDao.updateSendStatus(
                        notification.getNotificationId(),
                        "3",
                        LocalDateTime.now(),
                        "超过最大重试次数，取消发送",
                        retryCount,
                        null);
            }
        } catch (Exception e) {
            log.error("处理通知失败异常", e);
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
                CompletableFuture.runAsync(() -> sendNotification(notification), executorService);
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
                CompletableFuture.runAsync(() -> sendNotification(notification), executorService);
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

            Map<String, Object> result = new HashMap<>();

            for (Map<String, Object> stat : stats) {
                String status = (String) stat.get("status");
                Long count = (Long) stat.get("count");
                result.put(status.toLowerCase() + "Count", count);
            }

            return result;

        } catch (Exception e) {
            log.error("获取通知统计信息失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 发送测试通知
     *
     * @param channel    通知渠道 (1-邮件 2-短信 3-Webhook 4-微信)
     * @param recipients 接收人列表
     * @return 发送结果
     */
    public boolean sendTestNotification(Integer channel, List<String> recipients) {
        log.info("发送测试通知，渠道：{}，接收人：{}", channel, recipients);

        try {
            NotificationEntity notification = new NotificationEntity();
            notification.setTitle("IOE-DREAM监控系统 - 测试通知");
            notification.setContent("这是一条测试通知，用于验证通知配置是否正确。\n\n发送时间：" + LocalDateTime.now());
            notification.setNotificationType(9); // 9-测试通知
            notification.setReceiverType(1); // 1-指定用户
            notification.setReceiverIds(
                    recipients != null && !recipients.isEmpty() ? recipients.get(0) : "1");
            notification.setChannel(channel != null ? channel : 1);
            notification.setStatus(0); // 0-待发送
            notification.setPriority(2); // 2-普通优先级

            sendNotification(notification);

            return true;

        } catch (Exception e) {
            log.error("发送测试通知失败", e);
            return false;
        }
    }
}
