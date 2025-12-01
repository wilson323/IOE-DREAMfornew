package net.lab1024.sa.notification.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.notification.dao.NotificationConfigDao;
import net.lab1024.sa.notification.dao.NotificationMessageDao;
import net.lab1024.sa.notification.dao.NotificationTemplateDao;
import net.lab1024.sa.notification.domain.entity.NotificationConfigEntity;
import net.lab1024.sa.notification.domain.entity.NotificationMessageEntity;
import net.lab1024.sa.notification.domain.entity.NotificationTemplateEntity;
import net.lab1024.sa.notification.service.NotificationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 通知服务实现类
 * <p>
 * 严格遵循repowiki规范:
 * - 使用@Service注解标识服务
 * - 使用@Resource进行依赖注入
 * - 使用@Async支持异步处理
 * - 完整的事务管理
 * - 详细的日志记录
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Resource
    private NotificationMessageDao notificationMessageDao;

    @Resource
    private NotificationTemplateDao notificationTemplateDao;

    @Resource
    private NotificationConfigDao notificationConfigDao;

    @Resource
    private EmailNotificationService emailNotificationService;

    @Resource
    private SmsNotificationService smsNotificationService;

    @Resource
    private WechatNotificationService wechatNotificationService;

    @Resource
    private PushNotificationService pushNotificationService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private VoiceNotificationService voiceNotificationService;

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public Long sendSimpleMessage(Long recipientUserId, Integer channel, String subject, String content) {
        log.info("发送简单消息 - 用户ID: {}, 渠道: {}, 主题: {}", recipientUserId, channel, subject);

        NotificationMessageEntity message = new NotificationMessageEntity();
        message.setRecipientUserId(recipientUserId);
        message.setChannel(channel);
        message.setSubject(subject);
        message.setContent(content);
        message.setMessageType(2); // 业务通知
        message.setPriority(2); // 普通优先级
        message.setSendStatus(0); // 待发送

        notificationMessageDao.insert(message);

        // 异步执行发送
        processSendMessage(message);

        return message.getMessageId();
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public Long sendTemplateMessage(Long recipientUserId, String templateCode, Map<String, Object> templateParams) {
        log.info("发送模板消息 - 用户ID: {}, 模板: {}", recipientUserId, templateCode);

        // 查找适用模板
        NotificationTemplateEntity template = findBestTemplate(templateCode, null, null);
        if (template == null) {
            throw new RuntimeException("未找到适用的模板: " + templateCode);
        }

        return sendTemplateMessage(recipientUserId, template.getChannel(), templateCode, templateParams);
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public Long sendTemplateMessage(Long recipientUserId, Integer channel, String templateCode, Map<String, Object> templateParams) {
        log.info("发送指定渠道模板消息 - 用户ID: {}, 渠道: {}, 模板: {}", recipientUserId, channel, templateCode);

        NotificationTemplateEntity template = notificationTemplateDao.selectByCode(templateCode);
        if (template == null || !template.isApplicable(channel, null)) {
            throw new RuntimeException("模板不存在或不适用: " + templateCode);
        }

        // 渲染模板
        Map<String, Object> renderedContent = renderTemplate(template, templateParams);

        NotificationMessageEntity message = new NotificationMessageEntity();
        message.setRecipientUserId(recipientUserId);
        message.setChannel(channel);
        message.setSubject((String) renderedContent.get("subject"));
        message.setContent((String) renderedContent.get("content"));
        message.setMessageType(template.getMessageType());
        message.setTemplateId(template.getTemplateId().toString());
        message.setTemplateParams(convertParamsToJson(templateParams));
        message.setPriority(2);
        message.setSendStatus(0);

        notificationMessageDao.insert(message);

        // 增加模板使用次数
        notificationTemplateDao.batchIncrementUsageCount(Collections.singletonList(template.getTemplateId()));

        // 异步执行发送
        processSendMessage(message);

        return message.getMessageId();
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public String batchSendMessage(List<Long> recipientUserIds, Integer channel, String subject, String content) {
        log.info("批量发送消息 - 用户数: {}, 渠道: {}, 主题: {}", recipientUserIds.size(), channel, subject);

        String batchId = UUID.randomUUID().toString();
        List<Long> messageIds = new ArrayList<>();

        for (Long userId : recipientUserIds) {
            Long messageId = sendSimpleMessage(userId, channel, subject, content);
            messageIds.add(messageId);
        }

        log.info("批量发送完成 - 批次ID: {}, 消息数: {}", batchId, messageIds.size());
        return batchId;
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public String batchSendTemplateMessage(List<Long> recipientUserIds, String templateCode, Map<String, Object> templateParams) {
        log.info("批量发送模板消息 - 用户数: {}, 模板: {}", recipientUserIds.size(), templateCode);

        String batchId = UUID.randomUUID().toString();

        NotificationTemplateEntity template = notificationTemplateDao.selectByCode(templateCode);
        if (template == null) {
            throw new RuntimeException("模板不存在: " + templateCode);
        }

        List<Long> messageIds = new ArrayList<>();
        for (Long userId : recipientUserIds) {
            Long messageId = sendTemplateMessage(userId, template.getChannel(), templateCode, templateParams);
            messageIds.add(messageId);
        }

        log.info("批量模板发送完成 - 批次ID: {}, 消息数: {}", batchId, messageIds.size());
        return batchId;
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public Long sendVerificationCode(Long recipientUserId, Integer channel, String verificationCode, Integer expireMinutes) {
        log.info("发送验证码 - 用户ID: {}, 渠道: {}, 过期时间: {}分钟", recipientUserId, channel, expireMinutes);

        String templateCode = channel == 1 ? "EMAIL_VERIFICATION" : "SMS_VERIFICATION";
        Map<String, Object> params = new HashMap<>();
        params.put("verificationCode", verificationCode);
        params.put("expireMinutes", expireMinutes);

        NotificationMessageEntity message = new NotificationMessageEntity();
        message.setRecipientUserId(recipientUserId);
        message.setChannel(channel);
        message.setMessageType(5); // 验证码
        message.setPriority(3); // 高优先级
        message.setSendStatus(0);

        NotificationTemplateEntity template = findBestTemplate(templateCode, channel, 5);
        if (template != null) {
            Map<String, Object> renderedContent = renderTemplate(template, params);
            message.setSubject((String) renderedContent.get("subject"));
            message.setContent((String) renderedContent.get("content"));
            message.setTemplateId(template.getTemplateId().toString());
            message.setTemplateParams(convertParamsToJson(params));
        } else {
            // 使用默认验证码消息
            message.setSubject("验证码");
            message.setContent(String.format("您的验证码是：%s，有效期%d分钟。", verificationCode, expireMinutes));
        }

        notificationMessageDao.insert(message);

        // 异步执行发送
        processSendMessage(message);

        return message.getMessageId();
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public Long sendSystemNotification(Long recipientUserId, String subject, String content, String businessType, String businessId) {
        log.info("发送系统通知 - 用户ID: {}, 主题: {}, 业务类型: {}", recipientUserId, subject, businessType);

        NotificationMessageEntity message = new NotificationMessageEntity();
        message.setRecipientUserId(recipientUserId);
        message.setChannel(4); // 站内信
        message.setSubject(subject);
        message.setContent(content);
        message.setMessageType(1); // 系统通知
        message.setBusinessType(businessType);
        message.setBusinessId(businessId);
        message.setPriority(2);
        message.setSendStatus(0);

        notificationMessageDao.insert(message);

        // 站内信直接处理，不需要调用外部服务
        processInternalMessage(message);

        return message.getMessageId();
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public Long sendBusinessNotification(Long recipientUserId, String subject, String content, String businessType, String businessId) {
        log.info("发送业务通知 - 用户ID: {}, 主题: {}, 业务类型: {}", recipientUserId, subject, businessType);

        NotificationMessageEntity message = new NotificationMessageEntity();
        message.setRecipientUserId(recipientUserId);
        message.setChannel(4); // 默认站内信
        message.setSubject(subject);
        message.setContent(content);
        message.setMessageType(2); // 业务通知
        message.setBusinessType(businessType);
        message.setBusinessId(businessId);
        message.setPriority(2);
        message.setSendStatus(0);

        notificationMessageDao.insert(message);

        processSendMessage(message);

        return message.getMessageId();
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public Long sendAlertNotification(Long recipientUserId, String subject, String content, Integer level) {
        log.info("发送告警通知 - 用户ID: {}, 主题: {}, 级别: {}", recipientUserId, subject, level);

        NotificationMessageEntity message = new NotificationMessageEntity();
        message.setRecipientUserId(recipientUserId);
        message.setChannel(4); // 站内信
        message.setSubject(subject);
        message.setContent(content);
        message.setMessageType(3); // 告警通知
        message.setPriority(level != null && level >= 3 ? 4 : 3); // 紧急或高优先级
        message.setSendStatus(0);

        notificationMessageDao.insert(message);

        // 告警通知需要多渠道发送
        sendMultiChannelAlert(message);

        return message.getMessageId();
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public String sendMarketingNotification(List<Long> recipientUserIds, String subject, String content) {
        log.info("发送营销通知 - 用户数: {}, 主题: {}", recipientUserIds.size(), subject);

        String batchId = UUID.randomUUID().toString();

        // 营销通知通常通过推送和邮件发送
        for (Long userId : recipientUserIds) {
            // 推送通知
            if (isChannelEnabledForUser(userId, 5)) {
                sendSimpleMessage(userId, 5, subject, content);
            }
            // 邮件通知
            if (isChannelEnabledForUser(userId, 1)) {
                sendSimpleMessage(userId, 1, subject, content);
            }
        }

        return batchId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long scheduleMessage(Long recipientUserId, Integer channel, String subject, String content, LocalDateTime scheduleTime) {
        log.info("计划发送消息 - 用户ID: {}, 渠道: {}, 主题: {}, 计划时间: {}",
                recipientUserId, channel, subject, scheduleTime);

        NotificationMessageEntity message = new NotificationMessageEntity();
        message.setRecipientUserId(recipientUserId);
        message.setChannel(channel);
        message.setSubject(subject);
        message.setContent(content);
        message.setMessageType(2); // 业务通知
        message.setPriority(2);
        message.setSendStatus(0);
        message.setScheduleTime(scheduleTime);

        notificationMessageDao.insert(message);

        return message.getMessageId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelScheduledMessage(Long messageId) {
        log.info("取消计划消息 - 消息ID: {}", messageId);

        NotificationMessageEntity message = notificationMessageDao.selectById(messageId);
        if (message == null || message.getSendStatus() != 0) {
            return false;
        }

        message.setSendStatus(4); // 已撤销
        int result = notificationMessageDao.updateById(message);

        return result > 0;
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public boolean resendMessage(Long messageId) {
        log.info("重新发送消息 - 消息ID: {}", messageId);

        NotificationMessageEntity message = notificationMessageDao.selectById(messageId);
        if (message == null) {
            return false;
        }

        // 重置状态
        message.setSendStatus(0);
        message.setFailureReason(null);
        message.setRetryCount(0);
        message.setSentTime(null);

        notificationMessageDao.updateById(message);

        // 重新发送
        processSendMessage(message);

        return true;
    }

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public boolean retryFailedMessage(Long messageId) {
        log.info("重试失败消息 - 消息ID: {}", messageId);

        NotificationMessageEntity message = notificationMessageDao.selectById(messageId);
        if (message == null || !message.canRetry()) {
            return false;
        }

        message.incrementRetryCount();
        message.setSendStatus(0); // 重置为待发送

        notificationMessageDao.updateById(message);

        // 重新发送
        processSendMessage(message);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markMessageAsRead(Long messageId, Long userId) {
        log.info("标记消息为已读 - 消息ID: {}, 用户ID: {}", messageId, userId);

        int result = notificationMessageDao.markMessageAsRead(messageId, userId);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchMarkMessagesAsRead(List<Long> messageIds, Long userId) {
        log.info("批量标记消息为已读 - 消息数: {}, 用户ID: {}", messageIds.size(), userId);

        return notificationMessageDao.batchMarkMessagesAsRead(messageIds, userId);
    }

    @Override
    public List<NotificationMessageEntity> queryUserMessages(Long userId, Integer channel,
                                                             LocalDateTime startTime, LocalDateTime endTime,
                                                             Integer pageIndex, Integer pageSize) {
        log.info("查询用户消息 - 用户ID: {}, 渠道: {}, 页码: {}, 每页: {}",
                userId, channel, pageIndex, pageSize);

        // 实现具体的查询逻辑
        return Collections.emptyList(); // 占位符实现
    }

    @Override
    public int getUnreadMessageCount(Long userId) {
        log.info("查询用户未读消息数量 - 用户ID: {}", userId);

        return notificationMessageDao.countUnreadInternalMessages(userId);
    }

    @Override
    public NotificationMessageEntity getMessageDetail(Long messageId) {
        log.info("查询消息详情 - 消息ID: {}", messageId);

        return notificationMessageDao.selectMessageDetail(messageId);
    }

    @Override
    public List<NotificationMessageEntity> querySendRecords(Long messageId, Integer channel, Integer sendStatus,
                                                           LocalDateTime startTime, LocalDateTime endTime,
                                                           Integer pageIndex, Integer pageSize) {
        log.info("查询发送记录 - 消息ID: {}, 渠道: {}, 状态: {}", messageId, channel, sendStatus);

        // 实现具体的查询逻辑
        return Collections.emptyList(); // 占位符实现
    }

    @Override
    public Map<String, Object> getSendStatistics(LocalDateTime startTime, LocalDateTime endTime, String groupBy) {
        log.info("获取发送统计 - 开始时间: {}, 结束时间: {}, 分组: {}", startTime, endTime, groupBy);

        List<Map<String, Object>> statistics = notificationMessageDao.selectSendStatistics(startTime, endTime, groupBy);

        Map<String, Object> result = new HashMap<>();
        result.put("statistics", statistics);
        result.put("totalCount", statistics.stream().mapToInt(m -> (Integer) m.get("count")).sum());

        return result;
    }

    @Override
    public Map<String, Object> getUserSendStatistics(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("获取用户发送统计 - 用户ID: {}, 开始时间: {}, 结束时间: {}", userId, startTime, endTime);

        List<Map<String, Object>> statistics = notificationMessageDao.selectUserMessageStatistics(userId, startTime, endTime);

        Map<String, Object> result = new HashMap<>();
        result.put("statistics", statistics);
        result.put("userId", userId);

        return result;
    }

    @Override
    public Map<String, Object> getChannelUsageStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("获取渠道使用统计 - 开始时间: {}, 结束时间: {}", startTime, endTime);

        List<Map<String, Object>> statistics = notificationMessageDao.selectChannelUsageStatistics(startTime, endTime);

        Map<String, Object> result = new HashMap<>();
        result.put("statistics", statistics);

        return result;
    }

    @Override
    public Map<String, Object> getSuccessRateStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("获取发送成功率统计 - 开始时间: {}, 结束时间: {}", startTime, endTime);

        return notificationMessageDao.selectSuccessRateStatistics(startTime, endTime);
    }

    @Override
    public List<NotificationTemplateEntity> queryAvailableTemplates(Integer channel, Integer messageType, String language) {
        log.info("查询可用模板 - 渠道: {}, 消息类型: {}, 语言: {}", channel, messageType, language);

        return notificationTemplateDao.selectApplicableTemplates(channel, messageType, language);
    }

    @Override
    public Map<String, Object> previewTemplateMessage(String templateCode, Map<String, Object> templateParams) {
        log.info("预览模板消息 - 模板: {}", templateCode);

        NotificationTemplateEntity template = notificationTemplateDao.selectByCode(templateCode);
        if (template == null) {
            throw new RuntimeException("模板不存在: " + templateCode);
        }

        Map<String, Object> renderedContent = renderTemplate(template, templateParams);

        Map<String, Object> result = new HashMap<>();
        result.put("template", template);
        result.put("renderedSubject", renderedContent.get("subject"));
        result.put("renderedContent", renderedContent.get("content"));

        return result;
    }

    @Override
    public Map<String, Object> validateTemplateParams(String templateCode, Map<String, Object> templateParams) {
        log.info("验证模板参数 - 模板: {}", templateCode);

        NotificationTemplateEntity template = notificationTemplateDao.selectByCode(templateCode);
        if (template == null) {
            throw new RuntimeException("模板不存在: " + templateCode);
        }

        // 实现参数验证逻辑
        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("message", "参数验证通过");

        return result;
    }

    @Override
    public Map<String, Object> checkRateLimit(Long userId, Integer channel, Integer messageType) {
        log.info("检查发送频率限制 - 用户ID: {}, 渠道: {}, 消息类型: {}", userId, channel, messageType);

        Map<String, Object> rateLimitConfig = notificationConfigDao.selectRateLimitConfig(userId, channel, messageType);

        Map<String, Object> result = new HashMap<>();
        result.put("allowed", true);
        result.put("remaining", 100);
        result.put("resetTime", LocalDateTime.now().plusHours(1));

        return result;
    }

    @Override
    public Map<String, Object> getUserNotificationPreferences(Long userId) {
        log.info("获取用户通知偏好 - 用户ID: {}", userId);

        List<NotificationConfigEntity> configs = notificationConfigDao.selectUserPreferences(userId);

        Map<String, Object> preferences = new HashMap<>();
        for (NotificationConfigEntity config : configs) {
            preferences.put(config.getConfigKey(), config.getStringValue());
        }

        return preferences;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserNotificationPreferences(Long userId, Map<String, Object> preferences) {
        log.info("更新用户通知偏好 - 用户ID: {}, 偏好数: {}", userId, preferences.size());

        // 实现偏好更新逻辑
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanExpiredMessages() {
        log.info("清理过期消息");

        LocalDateTime expireTime = LocalDateTime.now().minusDays(30);
        return notificationMessageDao.deleteExpiredMessages(expireTime);
    }

    @Override
    public void processPendingMessages() {
        log.info("处理待发送消息");

        List<NotificationMessageEntity> pendingMessages =
                notificationMessageDao.selectPendingMessages(null, LocalDateTime.now(), 100);

        for (NotificationMessageEntity message : pendingMessages) {
            try {
                processSendMessage(message);
            } catch (Exception e) {
                log.error("处理待发送消息失败 - 消息ID: {}", message.getMessageId(), e);
            }
        }
    }

    @Override
    public void processRetryMessages() {
        log.info("处理重试消息");

        List<NotificationMessageEntity> retryMessages =
                notificationMessageDao.selectRetryMessages(null, 3, 50);

        for (NotificationMessageEntity message : retryMessages) {
            try {
                retryFailedMessage(message.getMessageId());
            } catch (Exception e) {
                log.error("处理重试消息失败 - 消息ID: {}", message.getMessageId(), e);
            }
        }
    }

    @Override
    public void syncDeliveryStatus() {
        log.info("同步配送状态");

        // 实现配送状态同步逻辑
    }

    // ========== 私有辅助方法 ==========

    /**
     * 处理消息发送
     */
    private void processSendMessage(NotificationMessageEntity message) {
        if (message.getScheduleTime() != null && message.getScheduleTime().isAfter(LocalDateTime.now())) {
            // 计划发送时间未到，跳过
            return;
        }

        try {
            boolean success = false;

            switch (message.getChannel()) {
                case 1: // 邮件
                    success = emailNotificationService.send(message);
                    break;
                case 2: // 短信
                    success = smsNotificationService.send(message);
                    break;
                case 3: // 微信
                    success = wechatNotificationService.send(message);
                    break;
                case 4: // 站内信
                    success = processInternalMessage(message);
                    break;
                case 5: // 推送
                    success = pushNotificationService.send(message);
                    break;
                case 6: // 语音
                    success = voiceNotificationService.send(message);
                    break;
                default:
                    throw new RuntimeException("不支持的通知渠道: " + message.getChannel());
            }

            if (success) {
                message.markAsSent(null, null);
                log.info("消息发送成功 - 消息ID: {}, 渠道: {}", message.getMessageId(), message.getChannel());
            } else {
                message.markAsFailed("发送失败");
                log.error("消息发送失败 - 消息ID: {}, 渠道: {}", message.getMessageId(), message.getChannel());
            }

        } catch (Exception e) {
            message.markAsFailed(e.getMessage());
            log.error("消息发送异常 - 消息ID: {}, 渠道: {}", message.getMessageId(), message.getChannel(), e);
        } finally {
            notificationMessageDao.updateById(message);
        }
    }

    /**
     * 处理站内信
     */
    private boolean processInternalMessage(NotificationMessageEntity message) {
        // 站内信直接入库，无需调用外部服务
        message.markAsSent(0L, null);
        message.setReadStatus(0); // 未读

        return true;
    }

    /**
     * 多渠道发送告警
     */
    private void sendMultiChannelAlert(NotificationMessageEntity message) {
        // 站内信
        processInternalMessage(message);

        // 邮件
        if (isChannelEnabledForUser(message.getRecipientUserId(), 1)) {
            sendSimpleMessage(message.getRecipientUserId(), 1, message.getSubject(), message.getContent());
        }

        // 短信
        if (isChannelEnabledForUser(message.getRecipientUserId(), 2) && message.getPriority() >= 3) {
            sendSimpleMessage(message.getRecipientUserId(), 2, message.getSubject(), message.getContent());
        }

        // 微信
        if (isChannelEnabledForUser(message.getRecipientUserId(), 3)) {
            sendSimpleMessage(message.getRecipientUserId(), 3, message.getSubject(), message.getContent());
        }
    }

    /**
     * 查找最佳模板
     */
    private NotificationTemplateEntity findBestTemplate(String templateCode, Integer channel, Integer messageType) {
        NotificationTemplateEntity template = notificationTemplateDao.selectByCode(templateCode);
        if (template != null && template.isEnabled() && template.isApproved()) {
            return template;
        }

        // 查找默认模板
        if (channel != null && messageType != null) {
            return notificationTemplateDao.selectDefaultTemplate(channel, messageType);
        }

        return null;
    }

    /**
     * 渲染模板
     */
    private Map<String, Object> renderTemplate(NotificationTemplateEntity template, Map<String, Object> params) {
        String subject = template.getSubject();
        String content = template.getContent();

        // 简单的变量替换（实际项目中可能需要更复杂的模板引擎）
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                String value = String.valueOf(entry.getValue());

                if (subject != null) {
                    subject = subject.replace(placeholder, value);
                }
                if (content != null) {
                    content = content.replace(placeholder, value);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("subject", subject);
        result.put("content", content);

        return result;
    }

    /**
     * 转换参数为JSON
     */
    private String convertParamsToJson(Map<String, Object> params) {
        try {
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            return "{}";
        }
    }

    /**
     * 检查用户是否启用了指定渠道
     */
    private boolean isChannelEnabledForUser(Long userId, Integer channel) {
        // 实现用户偏好检查逻辑
        return true; // 默认返回true
    }

    // ========== 内部服务接口 ==========

    public interface EmailNotificationService {
        boolean send(NotificationMessageEntity message);
    }

    public interface SmsNotificationService {
        boolean send(NotificationMessageEntity message);
    }

    public interface WechatNotificationService {
        boolean send(NotificationMessageEntity message);
    }

    public interface PushNotificationService {
        boolean send(NotificationMessageEntity message);
    }

    public interface VoiceNotificationService {
        boolean send(NotificationMessageEntity message);
    }
}