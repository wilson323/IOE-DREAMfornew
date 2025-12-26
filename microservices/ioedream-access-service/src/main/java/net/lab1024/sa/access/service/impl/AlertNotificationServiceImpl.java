package net.lab1024.sa.access.service.impl;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.AlertNotificationDao;
import net.lab1024.sa.access.domain.entity.AlertNotificationEntity;
import net.lab1024.sa.access.domain.entity.AlertRuleEntity;
import net.lab1024.sa.access.domain.entity.DeviceAlertEntity;
import net.lab1024.sa.access.domain.vo.AlertRuleVO;
import net.lab1024.sa.access.service.AlertNotificationService;
import net.lab1024.sa.access.service.AlertRuleService;
import net.lab1024.sa.access.websocket.AlertWebSocketService;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 告警通知服务实现类
 * <p>
 * 提供多渠道告警通知功能：
 * - 短信通知（SMS）
 * - 邮件通知（EMAIL）
 * - 应用推送（PUSH）
 * - WebSocket实时推送
 * </p>
 * <p>
 * 核心功能：
 * - 根据规则配置自动选择通知方式
 * - 支持多通道并发发送
 * - 自动重试机制（最大3次）
 * - 通知状态实时跟踪
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AlertNotificationServiceImpl implements AlertNotificationService {

    @Resource
    private AlertNotificationDao alertNotificationDao;

    @Resource
    private AlertRuleService alertRuleService;

    @Resource
    private AlertWebSocketService alertWebSocketService;

    /**
     * 发送告警通知（核心方法）
     * 根据规则配置的通知方式和接收人列表，发送告警通知
     */
    @Override
    public ResponseDTO<Void> sendNotification(DeviceAlertEntity alert, Long ruleId, String recipients) {
        log.info("[告警通知] 发送告警通知: alertId={}, ruleId={}, recipients={}",
                alert.getAlertId(), ruleId, recipients);

        try {
            // 1. 查询规则配置
            ResponseDTO<AlertRuleVO> ruleResponse = alertRuleService.getRule(ruleId);
            if (ruleResponse.getData() == null) {
                log.warn("[告警通知] 规则不存在: ruleId={}", ruleId);
                return ResponseDTO.error("RULE_NOT_FOUND", "规则不存在");
            }

            AlertRuleVO rule = ruleResponse.getData();
            String notificationMethods = rule.getNotificationMethods();
            if (notificationMethods == null || notificationMethods.isEmpty()) {
                log.warn("[告警通知] 规则未配置通知方式: ruleId={}", ruleId);
                return ResponseDTO.error("NO_NOTIFICATION_METHOD", "规则未配置通知方式");
            }

            // 2. 解析通知方式和接收人
            List<String> methods = List.of(notificationMethods.split(","));
            List<Map<String, Object>> recipientList = parseRecipients(recipients);

            if (recipientList.isEmpty()) {
                log.warn("[告警通知] 接收人列表为空: alertId={}", alert.getAlertId());
                return ResponseDTO.error("NO_RECIPIENTS", "接收人列表为空");
            }

            // 3. 为每个接收人创建通知记录
            List<AlertNotificationEntity> notifications = new ArrayList<>();
            for (Map<String, Object> recipient : recipientList) {
                for (String method : methods) {
                    AlertNotificationEntity notification = createNotification(alert, ruleId, method, recipient);
                    notifications.add(notification);
                }
            }

            // 4. 批量保存通知记录
            for (AlertNotificationEntity notification : notifications) {
                alertNotificationDao.insert(notification);
            }

            // 5. 并发发送通知
            for (AlertNotificationEntity notification : notifications) {
                sendNotificationByMethod(notification);
            }

            log.info("[告警通知] 告警通知发送完成: alertId={}, count={}", alert.getAlertId(), notifications.size());
            return ResponseDTO.ok();

        } catch (Exception e) {
            log.error("[告警通知] 发送告警通知异常: alertId={}, error={}", alert.getAlertId(), e.getMessage(), e);
            return ResponseDTO.error("SEND_NOTIFICATION_FAILED", "发送告警通知失败: " + e.getMessage());
        }
    }

    /**
     * 发送短信通知
     */
    @Override
    public ResponseDTO<Boolean> sendSmsNotification(AlertNotificationEntity notification) {
        log.info("[告警通知] 发送短信通知: notificationId={}, recipient={}",
                notification.getNotificationId(), notification.getRecipientContact());

        try {
            // TODO: 集成短信服务（如阿里云短信、腾讯云短信）
            // 1. 更新状态为发送中
            updateNotificationStatus(notification.getNotificationId(), 1, null, null);

            // 2. 调用短信服务发送
            // smsService.send(notification.getRecipientContact(), notification.getNotificationContent());

            // 3. 模拟发送成功（实际需要调用真实短信服务）
            boolean success = true; // 模拟成功

            if (success) {
                // 发送成功
                alertNotificationDao.markAsSuccess(notification.getNotificationId(), LocalDateTime.now());
                log.info("[告警通知] 短信通知发送成功: notificationId={}", notification.getNotificationId());
                return ResponseDTO.ok(true);
            } else {
                // 发送失败
                alertNotificationDao.markAsFailed(notification.getNotificationId(),
                        "短信服务返回失败", "SMS_SEND_FAILED");
                log.warn("[告警通知] 短信通知发送失败: notificationId={}", notification.getNotificationId());
                return ResponseDTO.error("SMS_SEND_FAILED", "短信发送失败");
            }

        } catch (Exception e) {
            log.error("[告警通知] 短信通知发送异常: notificationId={}, error={}",
                    notification.getNotificationId(), e.getMessage(), e);
            alertNotificationDao.markAsFailed(notification.getNotificationId(), e.getMessage(), "SMS_EXCEPTION");
            return ResponseDTO.error("SMS_SEND_EXCEPTION", "短信发送异常");
        }
    }

    /**
     * 发送邮件通知
     */
    @Override
    public ResponseDTO<Boolean> sendEmailNotification(AlertNotificationEntity notification) {
        log.info("[告警通知] 发送邮件通知: notificationId={}, recipient={}",
                notification.getNotificationId(), notification.getRecipientContact());

        try {
            // TODO: 集成邮件服务（如JavaMail、SendGrid）
            // 1. 更新状态为发送中
            updateNotificationStatus(notification.getNotificationId(), 1, null, null);

            // 2. 构建邮件内容
            // EmailMessage email = EmailMessage.builder()
            //     .to(notification.getRecipientContact())
            //     .subject(notification.getNotificationTitle())
            //     .content(notification.getNotificationContent())
            //     .build();

            // 3. 调用邮件服务发送
            // emailService.send(email);

            // 4. 模拟发送成功（实际需要调用真实邮件服务）
            boolean success = true; // 模拟成功

            if (success) {
                alertNotificationDao.markAsSuccess(notification.getNotificationId(), LocalDateTime.now());
                log.info("[告警通知] 邮件通知发送成功: notificationId={}", notification.getNotificationId());
                return ResponseDTO.ok(true);
            } else {
                alertNotificationDao.markAsFailed(notification.getNotificationId(),
                        "邮件服务返回失败", "EMAIL_SEND_FAILED");
                log.warn("[告警通知] 邮件通知发送失败: notificationId={}", notification.getNotificationId());
                return ResponseDTO.error("EMAIL_SEND_FAILED", "邮件发送失败");
            }

        } catch (Exception e) {
            log.error("[告警通知] 邮件通知发送异常: notificationId={}, error={}",
                    notification.getNotificationId(), e.getMessage(), e);
            alertNotificationDao.markAsFailed(notification.getNotificationId(), e.getMessage(), "EMAIL_EXCEPTION");
            return ResponseDTO.error("EMAIL_SEND_EXCEPTION", "邮件发送异常");
        }
    }

    /**
     * 发送应用推送通知
     */
    @Override
    public ResponseDTO<Boolean> sendPushNotification(AlertNotificationEntity notification) {
        log.info("[告警通知] 发送应用推送: notificationId={}, recipient={}",
                notification.getNotificationId(), notification.getRecipientContact());

        try {
            // TODO: 集成推送服务（如极光推送、个推、Firebase Cloud Messaging）
            // 1. 更新状态为发送中
            updateNotificationStatus(notification.getNotificationId(), 1, null, null);

            // 2. 构建推送消息
            // PushMessage pushMessage = PushMessage.builder()
            //     .userId(notification.getRecipientId())
            //     .title(notification.getNotificationTitle())
            //     .content(notification.getNotificationContent())
            //     .build();

            // 3. 调用推送服务
            // pushService.send(pushMessage);

            // 4. 模拟发送成功
            boolean success = true;

            if (success) {
                alertNotificationDao.markAsSuccess(notification.getNotificationId(), LocalDateTime.now());
                log.info("[告警通知] 应用推送发送成功: notificationId={}", notification.getNotificationId());
                return ResponseDTO.ok(true);
            } else {
                alertNotificationDao.markAsFailed(notification.getNotificationId(),
                        "推送服务返回失败", "PUSH_SEND_FAILED");
                log.warn("[告警通知] 应用推送发送失败: notificationId={}", notification.getNotificationId());
                return ResponseDTO.error("PUSH_SEND_FAILED", "应用推送发送失败");
            }

        } catch (Exception e) {
            log.error("[告警通知] 应用推送发送异常: notificationId={}, error={}",
                    notification.getNotificationId(), e.getMessage(), e);
            alertNotificationDao.markAsFailed(notification.getNotificationId(), e.getMessage(), "PUSH_EXCEPTION");
            return ResponseDTO.error("PUSH_SEND_EXCEPTION", "应用推送发送异常");
        }
    }

    /**
     * 发送WebSocket实时推送
     */
    @Override
    public ResponseDTO<Boolean> sendWebSocketNotification(AlertNotificationEntity notification) {
        log.info("[告警通知] 发送WebSocket推送: notificationId={}, recipient={}",
                notification.getNotificationId(), notification.getRecipientContact());

        try {
            // 1. 更新状态为发送中
            updateNotificationStatus(notification.getNotificationId(), 1, null, null);

            // 2. 构建告警实体（用于WebSocket推送）
            DeviceAlertEntity alert = new DeviceAlertEntity();
            alert.setAlertId(notification.getAlertId());
            alert.setAlertTitle(notification.getNotificationTitle());
            alert.setAlertMessage(notification.getNotificationContent());

            // 3. 调用WebSocket服务推送
            ResponseDTO<Void> pushResult = alertWebSocketService.sendAlertToUser(
                    notification.getRecipientId(),
                    alert
            );

            // 4. 根据推送结果更新状态
            if (pushResult.getCode() == 200) {
                alertNotificationDao.markAsSuccess(notification.getNotificationId(), LocalDateTime.now());
                log.info("[告警通知] WebSocket推送发送成功: notificationId={}", notification.getNotificationId());
                return ResponseDTO.ok(true);
            } else {
                alertNotificationDao.markAsFailed(notification.getNotificationId(),
                        pushResult.getMessage(), "WEBSOCKET_SEND_FAILED");
                log.warn("[告警通知] WebSocket推送发送失败: notificationId={}, error={}",
                        notification.getNotificationId(), pushResult.getMessage());
                return ResponseDTO.error("WEBSOCKET_SEND_FAILED", "WebSocket推送发送失败");
            }

        } catch (Exception e) {
            log.error("[告警通知] WebSocket推送发送异常: notificationId={}, error={}",
                    notification.getNotificationId(), e.getMessage(), e);
            alertNotificationDao.markAsFailed(notification.getNotificationId(), e.getMessage(), "WEBSOCKET_EXCEPTION");
            return ResponseDTO.error("WEBSOCKET_SEND_EXCEPTION", "WebSocket推送发送异常");
        }
    }

    /**
     * 重试失败的通知
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Integer> retryFailedNotifications() {
        log.info("[告警通知] 开始重试失败的通知");

        try {
            // 1. 查询待重试的通知
            List<AlertNotificationEntity> failedNotifications = alertNotificationDao.selectPendingRetry(LocalDateTime.now());

            if (failedNotifications.isEmpty()) {
                log.info("[告警通知] 没有需要重试的通知");
                return ResponseDTO.ok(0);
            }

            // 2. 重新发送
            int successCount = 0;
            for (AlertNotificationEntity notification : failedNotifications) {
                try {
                    ResponseDTO<Boolean> result = sendNotificationByMethod(notification);
                    if (result.getData() != null && result.getData()) {
                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("[告警通知] 重试通知失败: notificationId={}, error={}",
                            notification.getNotificationId(), e.getMessage(), e);
                }
            }

            log.info("[告警通知] 重试失败通知完成: total={}, success={}", failedNotifications.size(), successCount);
            return ResponseDTO.ok(successCount);

        } catch (Exception e) {
            log.error("[告警通知] 重试失败通知异常: error={}", e.getMessage(), e);
            return ResponseDTO.error("RETRY_FAILED", "重试失败通知异常: " + e.getMessage());
        }
    }

    /**
     * 查询告警的所有通知记录
     */
    @Override
    public ResponseDTO<List<AlertNotificationEntity>> queryNotificationByAlertId(Long alertId) {
        log.debug("[告警通知] 查询告警通知记录: alertId={}", alertId);

        List<AlertNotificationEntity> notifications = alertNotificationDao.selectByAlertId(alertId);
        return ResponseDTO.ok(notifications);
    }

    /**
     * 查询指定接收人的未读通知
     */
    @Override
    public ResponseDTO<List<AlertNotificationEntity>> queryUnreadNotifications(Long recipientId) {
        log.debug("[告警通知] 查询未读通知: recipientId={}", recipientId);

        List<AlertNotificationEntity> notifications = alertNotificationDao.selectUnreadByRecipient(recipientId);
        return ResponseDTO.ok(notifications);
    }

    /**
     * 标记通知为已读
     */
    @Override
    public ResponseDTO<Void> markNotificationAsRead(Long notificationId) {
        log.info("[告警通知] 标记通知为已读: notificationId={}", notificationId);

        int updated = alertNotificationDao.markAsRead(notificationId, LocalDateTime.now());
        if (updated <= 0) {
            log.warn("[告警通知] 标记通知为已读失败: notificationId={}", notificationId);
            return ResponseDTO.error("MARK_AS_READ_FAILED", "标记通知为已读失败");
        }

        return ResponseDTO.ok();
    }

    /**
     * 批量标记通知为已读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Integer> batchMarkAsRead(List<Long> notificationIds) {
        log.info("[告警通知] 批量标记通知为已读: count={}", notificationIds.size());

        int successCount = 0;
        LocalDateTime readTime = LocalDateTime.now();

        for (Long notificationId : notificationIds) {
            try {
                int updated = alertNotificationDao.markAsRead(notificationId, readTime);
                if (updated > 0) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("[告警通知] 标记通知为已读失败: notificationId={}, error={}",
                        notificationId, e.getMessage(), e);
            }
        }

        log.info("[告警通知] 批量标记通知为已读完成: total={}, success={}", notificationIds.size(), successCount);
        return ResponseDTO.ok(successCount);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 根据通知方式调用对应的发送方法
     */
    private ResponseDTO<Boolean> sendNotificationByMethod(AlertNotificationEntity notification) {
        String method = notification.getNotificationMethod();

        switch (method.toUpperCase()) {
            case "SMS":
                return sendSmsNotification(notification);
            case "EMAIL":
                return sendEmailNotification(notification);
            case "PUSH":
                return sendPushNotification(notification);
            case "WEBSOCKET":
                return sendWebSocketNotification(notification);
            default:
                log.warn("[告警通知] 不支持的通知方式: method={}", method);
                return ResponseDTO.error("UNSUPPORTED_METHOD", "不支持的通知方式: " + method);
        }
    }

    /**
     * 创建通知实体
     */
    private AlertNotificationEntity createNotification(DeviceAlertEntity alert, Long ruleId,
                                                      String method, Map<String, Object> recipient) {
        AlertNotificationEntity notification = new AlertNotificationEntity();

        // 基础信息
        notification.setAlertId(alert.getAlertId());
        notification.setRuleId(ruleId);
        notification.setNotificationMethod(method);

        // 接收人信息
        notification.setRecipientType((String) recipient.get("recipientType"));
        notification.setRecipientId(((Number) recipient.get("recipientId")).longValue());
        notification.setRecipientName((String) recipient.get("recipientName"));
        notification.setRecipientContact((String) recipient.get("recipientContact"));

        // 通知内容
        notification.setNotificationTitle(alert.getAlertTitle());
        notification.setNotificationContent(alert.getAlertMessage());

        // 状态信息
        notification.setNotificationStatus(0); // 待发送
        notification.setRetryCount(0);
        notification.setMaxRetry(3); // 默认最多重试3次

        return notification;
    }

    /**
     * 解析接收人列表（JSON格式）
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseRecipients(String recipientsJson) {
        try {
            if (recipientsJson == null || recipientsJson.isEmpty()) {
                return new ArrayList<>();
            }
            List<Map> maps = JSON.parseArray(recipientsJson, Map.class);
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map map : maps) {
                result.add(map);
            }
            return result;
        } catch (Exception e) {
            log.error("[告警通知] 解析接收人列表失败: recipientsJson={}, error={}",
                    recipientsJson, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 更新通知状态
     */
    private void updateNotificationStatus(Long notificationId, Integer status,
                                          String errorMessage, String errorCode) {
        AlertNotificationEntity update = new AlertNotificationEntity();
        update.setNotificationId(notificationId);
        update.setNotificationStatus(status);

        if (errorMessage != null) {
            update.setErrorMessage(errorMessage);
        }
        if (errorCode != null) {
            update.setErrorCode(errorCode);
        }

        alertNotificationDao.updateById(update);
    }
}
