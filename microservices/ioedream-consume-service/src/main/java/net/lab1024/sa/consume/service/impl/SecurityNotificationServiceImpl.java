/*
 * 安全通知服务实现
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.consume.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.util.SmartDateFormatterUtil;
import net.lab1024.sa.common.util.SmartStringUtil;
import net.lab1024.sa.common.util.SmartVerificationUtil;
import net.lab1024.sa.consume.dao.SecurityNotificationDao;
import net.lab1024.sa.consume.domain.entity.SecurityNotificationLogEntity;
import net.lab1024.sa.consume.domain.entity.SystemNotificationConfigEntity;
import net.lab1024.sa.consume.domain.entity.UserNotificationPreferenceEntity;
import net.lab1024.sa.consume.domain.enums.EmailPriority;
import net.lab1024.sa.consume.domain.enums.PushPriority;
import net.lab1024.sa.consume.domain.vo.BatchNotificationResult;
import net.lab1024.sa.consume.domain.vo.EmailRequest;
import net.lab1024.sa.consume.domain.vo.EmailResult;
import net.lab1024.sa.consume.domain.vo.NotificationStatistics;
import net.lab1024.sa.consume.domain.vo.PushNotificationRequest;
import net.lab1024.sa.consume.domain.vo.PushNotificationResult;
import net.lab1024.sa.consume.domain.vo.SecurityNotificationRecord;
import net.lab1024.sa.consume.domain.vo.SecurityNotificationResult;
import net.lab1024.sa.consume.domain.vo.SmsRequest;
import net.lab1024.sa.consume.domain.vo.SmsResult;
import net.lab1024.sa.consume.domain.vo.SystemNotificationConfig;
import net.lab1024.sa.consume.domain.vo.UserNotificationPreference;
import net.lab1024.sa.consume.domain.vo.WechatNotificationRequest;
import net.lab1024.sa.consume.domain.vo.WechatNotificationResult;
import net.lab1024.sa.consume.service.EmailService;
import net.lab1024.sa.consume.service.PushNotificationService;
import net.lab1024.sa.consume.service.SecurityNotificationService;
import net.lab1024.sa.consume.service.SmsService;
import net.lab1024.sa.consume.service.WechatNotificationService;

/**
 * 安全通知服务实现
 * 支持邮件、短信、App推送、微信等多渠道通知
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Slf4j
@Service
public class SecurityNotificationServiceImpl implements SecurityNotificationService {

    @Resource
    private SecurityNotificationDao securityNotificationDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private EmailService emailService;

    @Resource
    private SmsService smsService;

    @Resource
    private PushNotificationService pushNotificationService;

    @Resource
    private WechatNotificationService wechatNotificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Redis缓存键前缀
    private static final String CACHE_PREFIX_NOTIFICATION_CONFIG = "notification:config:";
    private static final String CACHE_PREFIX_SEND_LIMIT = "notification:limit:";
    private static final String CACHE_PREFIX_PREFERENCE = "notification:preference:";

    // 发送频率限制配置
    private static final int MAX_EMAIL_PER_HOUR = 10;
    private static final int MAX_SMS_PER_HOUR = 20;
    private static final int MAX_PUSH_PER_HOUR = 50;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SecurityNotificationResult sendNotification(Long personId, String notificationType, String message,
            Object relatedData) {
        log.info("发送安全通知: personId={}, type={}, message={}", personId, notificationType, message);

        try {
            // 1. 参数验证
            if (personId == null || SmartStringUtil.isEmpty(notificationType) || SmartStringUtil.isEmpty(message)) {
                return SecurityNotificationResult.failure("INVALID_PARAMETER", "参数不完整");
            }

            // 2. 获取用户通知偏好
            UserNotificationPreference preference = getNotificationPreference(personId);
            if (preference == null || !preference.isNotificationEnabled(notificationType)) {
                return SecurityNotificationResult.failure("NOTIFICATION_DISABLED", "用户已禁用此类型通知");
            }

            // 3. 检查发送频率限制
            if (!checkSendRateLimit(personId, notificationType)) {
                return SecurityNotificationResult.failure("RATE_LIMIT_EXCEEDED", "发送频率过高，请稍后再试");
            }

            // 4. 根据通知类型确定发送渠道
            List<String> channels = getNotificationChannels(notificationType, preference);
            List<SecurityNotificationResult> results = new ArrayList<>();

            // 5. 多渠道发送
            for (String channel : channels) {
                SecurityNotificationResult result = sendNotificationByChannel(personId, channel, message, relatedData);
                results.add(result);

                // 记录通知日志
                recordNotificationLog(personId, channel, notificationType, message, result);
            }

            // 6. 评估整体发送结果
            return evaluateNotificationResults(results);

        } catch (Exception e) {
            log.error("发送安全通知失败: personId={}, type={}", personId, notificationType, e);
            return SecurityNotificationResult.failure("SEND_ERROR", "发送失败: " + e.getMessage());
        }
    }

    @Override
    public SecurityNotificationResult sendEmailNotification(Long personId, String subject, String content,
            List<String> attachmentFiles) {
        log.info("发送邮件通知: personId={}, subject={}", personId, subject);

        try {
            // 1. 获取用户邮箱地址
            String emailAddress = getUserEmailAddress(personId);
            if (SmartStringUtil.isEmpty(emailAddress)) {
                return SecurityNotificationResult.failure("EMAIL_NOT_FOUND", "用户邮箱地址未配置");
            }

            // 2. 验证邮箱格式
            if (!emailAddress.matches(SmartVerificationUtil.EMAIL)) {
                return SecurityNotificationResult.failure("INVALID_EMAIL", "邮箱地址格式错误");
            }

            // 3. 检查邮件服务是否可用
            if (emailService == null) {
                return SecurityNotificationResult.failure("EMAIL_SERVICE_UNAVAILABLE", "邮件服务暂不可用");
            }

            // 4. 检查发送频率限制
            if (!checkEmailSendLimit(personId)) {
                return SecurityNotificationResult.failure("EMAIL_RATE_LIMIT", "邮件发送频率过高");
            }

            // 5. 发送邮件
            EmailRequest emailRequest = EmailRequest.builder()
                    .to(emailAddress)
                    .subject(subject)
                    .content(content)
                    .attachments(attachmentFiles)
                    .isHtml(true)
                    .priority(EmailPriority.HIGH)
                    .build();

            EmailResult emailResult = emailService.sendEmail(emailRequest);

            if (emailResult.isSuccess()) {
                // 更新发送计数
                updateEmailSendCount(personId);
                return SecurityNotificationResult.success("邮件发送成功", emailResult.getMessageId());
            } else {
                return SecurityNotificationResult.failure("EMAIL_SEND_FAILED", emailResult.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("发送邮件通知失败: personId={}", personId, e);
            return SecurityNotificationResult.failure("EMAIL_ERROR", "邮件发送异常: " + e.getMessage());
        }
    }

    @Override
    public SecurityNotificationResult sendSmsNotification(Long personId, String content, String templateCode,
            Map<String, Object> templateParams) {
        log.info("发送短信通知: personId={}, templateCode={}", personId, templateCode);

        try {
            // 1. 获取用户手机号码
            String phoneNumber = getUserPhoneNumber(personId);
            if (SmartStringUtil.isEmpty(phoneNumber)) {
                return SecurityNotificationResult.failure("PHONE_NOT_FOUND", "用户手机号码未配置");
            }

            // 2. 验证手机号格式
            if (!phoneNumber.matches(SmartVerificationUtil.PHONE_REGEXP)) {
                return SecurityNotificationResult.failure("INVALID_PHONE", "手机号码格式错误");
            }

            // 3. 检查短信服务是否可用
            if (smsService == null) {
                return SecurityNotificationResult.failure("SMS_SERVICE_UNAVAILABLE", "短信服务暂不可用");
            }

            // 4. 检查发送频率限制
            if (!checkSmsSendLimit(personId)) {
                return SecurityNotificationResult.failure("SMS_RATE_LIMIT", "短信发送频率过高");
            }

            // 5. 发送短信
            SmsRequest smsRequest = SmsRequest.builder()
                    .phoneNumber(phoneNumber)
                    .content(content)
                    .templateCode(templateCode)
                    .templateParams(templateParams)
                    .businessType("SECURITY_NOTIFICATION")
                    .build();

            SmsResult smsResult = smsService.sendSms(smsRequest);

            if (smsResult.isSuccess()) {
                // 更新发送计数
                updateSmsSendCount(personId);
                return SecurityNotificationResult.success("短信发送成功", smsResult.getMessageId());
            } else {
                return SecurityNotificationResult.failure("SMS_SEND_FAILED", smsResult.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("发送短信通知失败: personId={}", personId, e);
            return SecurityNotificationResult.failure("SMS_ERROR", "短信发送异常: " + e.getMessage());
        }
    }

    @Override
    public SecurityNotificationResult sendPushNotification(Long personId, String title, String content,
            String actionUrl, Map<String, Object> extraData) {
        log.info("发送推送通知: personId={}, title={}", personId, title);

        try {
            // 1. 获取用户设备令牌
            List<String> deviceTokens = getUserDeviceTokens(personId);
            if (deviceTokens == null || deviceTokens.isEmpty()) {
                return SecurityNotificationResult.failure("DEVICE_NOT_FOUND", "用户设备令牌未找到");
            }

            // 2. 检查推送服务是否可用
            if (pushNotificationService == null) {
                return SecurityNotificationResult.failure("PUSH_SERVICE_UNAVAILABLE", "推送服务暂不可用");
            }

            // 3. 检查发送频率限制
            if (!checkPushSendLimit(personId)) {
                return SecurityNotificationResult.failure("PUSH_RATE_LIMIT", "推送发送频率过高");
            }

            // 4. 构建推送消息
            PushNotificationRequest pushRequest = PushNotificationRequest.builder()
                    .deviceTokens(deviceTokens)
                    .title(title)
                    .content(content)
                    .actionUrl(actionUrl)
                    .extraData(extraData)
                    .priority(PushPriority.HIGH)
                    .ttl(3600) // 1小时过期
                    .build();

            // 5. 发送推送
            PushNotificationResult pushResult = pushNotificationService.sendPush(pushRequest);

            if (pushResult.isSuccess()) {
                // 更新发送计数
                updatePushSendCount(personId);
                return SecurityNotificationResult.success("推送发送成功", pushResult.getMessageId());
            } else {
                return SecurityNotificationResult.failure("PUSH_SEND_FAILED", pushResult.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("发送推送通知失败: personId={}", personId, e);
            return SecurityNotificationResult.failure("PUSH_ERROR", "推送发送异常: " + e.getMessage());
        }
    }

    @Override
    public SecurityNotificationResult sendWechatNotification(Long personId, String content, String messageType,
            Map<String, Object> templateData) {
        log.info("发送微信通知: personId={}, messageType={}", personId, messageType);

        try {
            // 1. 获取用户微信OpenID
            String openId = getUserWechatOpenId(personId);
            if (SmartStringUtil.isEmpty(openId)) {
                return SecurityNotificationResult.failure("WECHAT_NOT_BOUND", "用户未绑定微信");
            }

            // 2. 检查微信服务是否可用
            if (wechatNotificationService == null) {
                return SecurityNotificationResult.failure("WECHAT_SERVICE_UNAVAILABLE", "微信服务暂不可用");
            }

            // 3. 发送微信消息
            WechatNotificationRequest wechatRequest = WechatNotificationRequest.builder()
                    .openId(openId)
                    .content(content)
                    .messageType(SmartStringUtil.isEmpty(messageType) ? "text" : messageType)
                    .templateData(templateData)
                    .build();

            WechatNotificationResult wechatResult = wechatNotificationService.sendMessage(wechatRequest);

            if (wechatResult.getSuccess() != null && wechatResult.getSuccess()) {
                return SecurityNotificationResult.success("微信通知发送成功", wechatResult.getMessageId());
            } else {
                return SecurityNotificationResult.failure("WECHAT_SEND_FAILED", wechatResult.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("发送微信通知失败: personId={}", personId, e);
            return SecurityNotificationResult.failure("WECHAT_ERROR", "微信通知发送异常: " + e.getMessage());
        }
    }

    @Override
    public BatchNotificationResult batchSendNotification(List<Long> personIds, String notificationType, String message,
            Object relatedData) {
        log.info("批量发送通知: count={}, type={}", personIds.size(), notificationType);

        BatchNotificationResult result = new BatchNotificationResult();
        result.setTotalCount(personIds.size());

        List<SecurityNotificationResult> successResults = new ArrayList<>();
        List<SecurityNotificationResult> failureResults = new ArrayList<>();

        for (Long personId : personIds) {
            try {
                SecurityNotificationResult notificationResult = sendNotification(personId, notificationType, message,
                        relatedData);

                if (notificationResult.isSuccess()) {
                    successResults.add(notificationResult);
                } else {
                    failureResults.add(notificationResult);
                }
            } catch (Exception e) {
                log.error("批量发送通知失败: personId={}", personId, e);
                failureResults.add(SecurityNotificationResult.failure("BATCH_SEND_ERROR", "发送异常: " + e.getMessage()));
            }
        }

        result.setSuccessCount(successResults.size());
        result.setFailureCount(failureResults.size());
        result.setSuccessResults(successResults);
        result.setFailureResults(failureResults);

        log.info("批量发送通知完成: total={}, success={}, failure={}",
                result.getTotalCount(), result.getSuccessCount(), result.getFailureCount());

        return result;
    }

    @Override
    public SecurityNotificationResult sendEmergencyAlert(Long personId, String alertType, String alertLevel,
            String alertMessage, Object relatedData) {
        log.info("发送紧急安全告警: personId={}, type={}, level={}", personId, alertType, alertLevel);

        try {
            // 1. 构建紧急告警消息
            String title = String.format("【%s安全告警】", getAlertLevelDescription(alertLevel));
            String content = String.format("%s: %s", alertType, alertMessage);

            // 2. 确定发送渠道（紧急告警使用所有可用渠道）
            UserNotificationPreference preference = getNotificationPreference(personId);
            List<String> channels = Arrays.asList("EMAIL", "SMS", "PUSH");

            List<SecurityNotificationResult> results = new ArrayList<>();

            // 3. 多渠道发送
            for (String channel : channels) {
                if (isNotificationChannelAvailable(personId, channel)) {
                    SecurityNotificationResult result;
                    switch (channel) {
                        case "EMAIL":
                            result = sendEmailNotification(personId, title, content, null);
                            break;
                        case "SMS":
                            result = sendSmsNotification(personId, content, null, null);
                            break;
                        case "PUSH":
                            result = sendPushNotification(personId, title, content, null, null);
                            break;
                        default:
                            continue;
                    }
                    results.add(result);
                }
            }

            // 4. 记录紧急告警日志
            recordEmergencyAlertLog(personId, alertType, alertLevel, alertMessage, relatedData);

            return evaluateNotificationResults(results);

        } catch (Exception e) {
            log.error("发送紧急安全告警失败: personId={}", personId, e);
            return SecurityNotificationResult.failure("EMERGENCY_ALERT_ERROR", "紧急告警发送失败: " + e.getMessage());
        }
    }

    @Override
    public SecurityNotificationResult sendAccountStatusNotification(Long personId, String statusType,
            String statusMessage, Long operatorId) {
        log.info("发送账户状态变更通知: personId={}, statusType={}", personId, statusType);

        try {
            String title = "账户状态变更通知";
            String content = String.format("您的账户状态已变更为: %s\n详情: %s\n操作时间: %s",
                    getStatusTypeDescription(statusType),
                    statusMessage,
                    SmartDateFormatterUtil.formatDateTime(LocalDateTime.now()));

            return sendNotification(personId, "ACCOUNT_STATUS_CHANGE", content, Map.of(
                    "statusType", statusType,
                    "statusMessage", statusMessage,
                    "operatorId", operatorId,
                    "changeTime", LocalDateTime.now()));

        } catch (Exception e) {
            log.error("发送账户状态变更通知失败: personId={}", personId, e);
            return SecurityNotificationResult.failure("ACCOUNT_STATUS_ERROR", "账户状态通知发送失败: " + e.getMessage());
        }
    }

    @Override
    public SecurityNotificationResult sendTransactionAnomalyNotification(Long personId, String transactionId,
            String anomalyType, String anomalyMessage, Object transactionAmount) {
        log.info("发送交易异常通知: personId={}, transactionId={}", personId, transactionId);

        try {
            String title = "交易异常提醒";
            String content = String.format("检测到异常交易:\n交易号: %s\n异常类型: %s\n详情: %s\n金额: %s\n时间: %s",
                    transactionId,
                    anomalyType,
                    anomalyMessage,
                    transactionAmount != null ? transactionAmount.toString() : "未知",
                    SmartDateFormatterUtil.formatDateTime(LocalDateTime.now()));

            return sendNotification(personId, "TRANSACTION_ANOMALY", content, Map.of(
                    "transactionId", transactionId,
                    "anomalyType", anomalyType,
                    "anomalyMessage", anomalyMessage,
                    "amount", transactionAmount,
                    "anomalyTime", LocalDateTime.now()));

        } catch (Exception e) {
            log.error("发送交易异常通知失败: personId={}", personId, e);
            return SecurityNotificationResult.failure("TRANSACTION_ANOMALY_ERROR", "交易异常通知发送失败: " + e.getMessage());
        }
    }

    @Override
    public SecurityNotificationResult sendLoginAnomalyNotification(Long personId, String loginLocation, String loginIp,
            String loginDevice, LocalDateTime loginTime) {
        log.info("发送登录异常通知: personId={}, loginIp={}", personId, loginIp);

        try {
            String title = "登录异常提醒";
            String content = String.format("检测到异常登录:\n登录地点: %s\nIP地址: %s\n设备信息: %s\n时间: %s\n如果这不是您本人的操作，请立即修改密码。",
                    SmartStringUtil.isEmpty(loginLocation) ? "未知" : loginLocation,
                    SmartStringUtil.isEmpty(loginIp) ? "未知" : loginIp,
                    SmartStringUtil.isEmpty(loginDevice) ? "未知" : loginDevice,
                    SmartDateFormatterUtil.formatDateTime(loginTime != null ? loginTime : LocalDateTime.now()));

            return sendNotification(personId, "LOGIN_ANOMALY", content, Map.of(
                    "loginLocation", loginLocation,
                    "loginIp", loginIp,
                    "loginDevice", loginDevice,
                    "loginTime", loginTime != null ? loginTime : LocalDateTime.now()));

        } catch (Exception e) {
            log.error("发送登录异常通知失败: personId={}", personId, e);
            return SecurityNotificationResult.failure("LOGIN_ANOMALY_ERROR", "登录异常通知发送失败: " + e.getMessage());
        }
    }

    @Override
    public SecurityNotificationResult sendPasswordResetNotification(Long personId, String resetType,
            LocalDateTime resetTime, String resetMethod) {
        log.info("发送密码重置通知: personId={}, resetType={}", personId, resetType);

        try {
            String title = "密码重置通知";
            String content = String.format("您的%s已于 %s 通过 %s 方式重置。\n如果这不是您本人的操作，请立即联系客服。",
                    getResetTypeDescription(resetType),
                    SmartDateFormatterUtil.formatDateTime(resetTime != null ? resetTime : LocalDateTime.now()),
                    SmartStringUtil.isEmpty(resetMethod) ? "未知" : resetMethod);

            return sendNotification(personId, "PASSWORD_RESET", content, Map.of(
                    "resetType", resetType,
                    "resetTime", resetTime != null ? resetTime : LocalDateTime.now(),
                    "resetMethod", resetMethod));

        } catch (Exception e) {
            log.error("发送密码重置通知失败: personId={}", personId, e);
            return SecurityNotificationResult.failure("PASSWORD_RESET_ERROR", "密码重置通知发送失败: " + e.getMessage());
        }
    }

    @Override
    public SecurityNotificationResult sendLimitReminderNotification(Long personId, String limitType,
            Object currentUsage, Object limitAmount, Double usagePercentage) {
        log.info("发送限额提醒通知: personId={}, limitType={}, usagePercent={}%", personId, limitType, usagePercentage);

        try {
            String title = "消费限额提醒";
            String content = String.format("您的%s消费限额使用情况:\n当前使用: %s\n限额金额: %s\n使用率: %.1f%%\n请合理控制消费。",
                    getLimitTypeDescription(limitType),
                    currentUsage != null ? currentUsage.toString() : "0",
                    limitAmount != null ? limitAmount.toString() : "0",
                    usagePercentage != null ? usagePercentage : 0.0);

            return sendNotification(personId, "LIMIT_REMINDER", content, Map.of(
                    "limitType", limitType,
                    "currentUsage", currentUsage,
                    "limitAmount", limitAmount,
                    "usagePercentage", usagePercentage));

        } catch (Exception e) {
            log.error("发送限额提醒通知失败: personId={}", personId, e);
            return SecurityNotificationResult.failure("LIMIT_REMINDER_ERROR", "限额提醒通知发送失败: " + e.getMessage());
        }
    }

    @Override
    public boolean setNotificationPreference(Long personId, String notificationChannel, boolean isEnabled,
            List<String> notificationTypes) {
        try {
            UserNotificationPreferenceEntity preference = securityNotificationDao.selectPreferenceByPersonId(personId);

            if (preference == null) {
                preference = UserNotificationPreferenceEntity.builder()
                        .personId(personId)
                        .build();
            }

            // 更新渠道设置
            switch (notificationChannel.toUpperCase()) {
                case "EMAIL":
                    preference.setEmailEnabled(isEnabled);
                    break;
                case "SMS":
                    preference.setSmsEnabled(isEnabled);
                    break;
                case "PUSH":
                    preference.setPushEnabled(isEnabled);
                    break;
                case "WECHAT":
                    preference.setWechatEnabled(isEnabled);
                    break;
            }

            // 更新通知类型设置
            if (notificationTypes != null && !notificationTypes.isEmpty()) {
                preference.setNotificationTypes(String.join(",", notificationTypes));
            }

            preference.setUpdateTime(LocalDateTime.now());

            int result;
            if (preference.getId() == null) {
                result = securityNotificationDao.insertPreference(preference);
            } else {
                result = securityNotificationDao.updatePreference(preference);
            }

            // 清除缓存
            clearNotificationPreferenceCache(personId);

            return result > 0;

        } catch (Exception e) {
            log.error("设置用户通知偏好失败: personId={}, channel={}", personId, notificationChannel, e);
            return false;
        }
    }

    @Override
    public UserNotificationPreference getNotificationPreference(Long personId) {
        try {
            // 1. 从缓存获取
            String cacheKey = CACHE_PREFIX_PREFERENCE + personId;
            UserNotificationPreference preference = (UserNotificationPreference) redisTemplate.opsForValue()
                    .get(cacheKey);

            if (preference != null) {
                return preference;
            }

            // 2. 从数据库获取
            UserNotificationPreferenceEntity entity = securityNotificationDao.selectPreferenceByPersonId(personId);

            if (entity == null) {
                // 创建默认偏好设置
                preference = createDefaultNotificationPreference(personId);
            } else {
                preference = convertToNotificationPreference(entity);
            }

            // 3. 缓存结果
            redisTemplate.opsForValue().set(cacheKey, preference, 30, TimeUnit.MINUTES);

            return preference;

        } catch (Exception e) {
            log.error("获取用户通知偏好失败: personId={}", personId, e);
            return createDefaultNotificationPreference(personId);
        }
    }

    @Override
    public boolean isNotificationChannelAvailable(Long personId, String notificationChannel) {
        try {
            UserNotificationPreference preference = getNotificationPreference(personId);

            switch (notificationChannel.toUpperCase()) {
                case "EMAIL":
                    return preference.isEmailEnabled() && SmartStringUtil.isNotEmpty(getUserEmailAddress(personId));
                case "SMS":
                    return preference.isSmsEnabled() && SmartStringUtil.isNotEmpty(getUserPhoneNumber(personId));
                case "PUSH":
                    return preference.isPushEnabled() && !getUserDeviceTokens(personId).isEmpty();
                case "WECHAT":
                    return preference.isWechatEnabled() && SmartStringUtil.isNotEmpty(getUserWechatOpenId(personId));
                default:
                    return false;
            }

        } catch (Exception e) {
            log.error("检查通知渠道可用性失败: personId={}, channel={}", personId, notificationChannel, e);
            return false;
        }
    }

    @Override
    public List<SecurityNotificationRecord> getNotificationHistory(Long personId, String notificationType,
            LocalDateTime startTime, LocalDateTime endTime, Integer limit) {
        try {
            return securityNotificationDao.selectNotificationHistory(personId, notificationType, startTime, endTime,
                    limit != null ? limit : 50);
        } catch (Exception e) {
            log.error("获取通知历史记录失败: personId={}, type={}", personId, notificationType, e);
            return new ArrayList<>();
        }
    }

    @Override
    public NotificationStatistics getNotificationStatistics(Long personId, String timeRange) {
        try {
            return securityNotificationDao.selectNotificationStatistics(personId, timeRange);
        } catch (Exception e) {
            log.error("获取通知统计信息失败: personId={}, range={}", personId, timeRange, e);
            return new NotificationStatistics();
        }
    }

    @Override
    public boolean retryFailedNotification(Long notificationId, Integer retryCount) {
        try {
            SecurityNotificationLogEntity notificationLog = securityNotificationDao
                    .selectNotificationLogById(notificationId);

            if (notificationLog == null || "SUCCESS".equals(notificationLog.getStatus())) {
                return false;
            }

            // 检查重试次数限制
            if (retryCount != null && notificationLog.getRetryCount() >= retryCount) {
                return false;
            }

            // 增加重试次数
            notificationLog.setRetryCount(notificationLog.getRetryCount() + 1);
            notificationLog.setRetryTime(LocalDateTime.now());

            // 重新发送通知
            SecurityNotificationResult result = resendNotification(notificationLog);

            // 更新状态
            notificationLog.setStatus(result.isSuccess() ? "SUCCESS" : "FAILED");
            notificationLog.setErrorMessage(result.getMessage());
            notificationLog.setUpdateTime(LocalDateTime.now());

            securityNotificationDao.updateNotificationLog(notificationLog);

            return result.isSuccess();

        } catch (Exception e) {
            log.error("重试失败通知失败: notificationId={}", notificationId, e);
            return false;
        }
    }

    @Override
    public boolean cancelScheduledNotification(Long notificationId, String cancelReason) {
        try {
            SecurityNotificationLogEntity notificationLog = securityNotificationDao
                    .selectNotificationLogById(notificationId);

            if (notificationLog == null || !"SCHEDULED".equals(notificationLog.getStatus())) {
                return false;
            }

            // 更新为取消状态
            notificationLog.setStatus("CANCELLED");
            notificationLog.setCancelReason(cancelReason);
            notificationLog.setUpdateTime(LocalDateTime.now());

            return securityNotificationDao.updateNotificationLog(notificationLog) > 0;

        } catch (Exception e) {
            log.error("取消定时通知失败: notificationId={}", notificationId, e);
            return false;
        }
    }

    @Override
    public SecurityNotificationResult testNotificationChannel(Long personId, String notificationChannel,
            String testMessage) {
        log.info("测试通知渠道: personId={}, channel={}", personId, notificationChannel);

        String title = "通知渠道测试";
        String content = String.format("这是一条测试消息，用于验证%s通知渠道是否正常工作。\n测试时间: %s\n测试内容: %s",
                getChannelDescription(notificationChannel),
                SmartDateFormatterUtil.formatDateTime(LocalDateTime.now()),
                testMessage);

        switch (notificationChannel.toUpperCase()) {
            case "EMAIL":
                return sendEmailNotification(personId, title, content, null);
            case "SMS":
                return sendSmsNotification(personId, content, null, null);
            case "PUSH":
                return sendPushNotification(personId, title, content, null, null);
            case "WECHAT":
                return sendWechatNotification(personId, content, "text", null);
            default:
                return SecurityNotificationResult.failure("UNSUPPORTED_CHANNEL", "不支持的通知渠道");
        }
    }

    @Override
    public SystemNotificationConfig getSystemNotificationConfig() {
        try {
            String cacheKey = CACHE_PREFIX_NOTIFICATION_CONFIG + "system";
            SystemNotificationConfig config = (SystemNotificationConfig) redisTemplate.opsForValue().get(cacheKey);

            if (config != null) {
                return config;
            }

            SystemNotificationConfigEntity entity = securityNotificationDao.selectSystemNotificationConfig();

            if (entity == null) {
                config = createDefaultSystemNotificationConfig();
            } else {
                config = convertToSystemNotificationConfig(entity);
            }

            redisTemplate.opsForValue().set(cacheKey, config, 60, TimeUnit.MINUTES);

            return config;

        } catch (Exception e) {
            log.error("获取系统通知配置失败", e);
            return createDefaultSystemNotificationConfig();
        }
    }

    @Override
    public boolean updateSystemNotificationConfig(SystemNotificationConfig config) {
        try {
            SystemNotificationConfigEntity entity = convertToSystemNotificationConfigEntity(config);

            int result = securityNotificationDao.updateSystemNotificationConfig(entity);

            if (result > 0) {
                // 清除缓存
                String cacheKey = CACHE_PREFIX_NOTIFICATION_CONFIG + "system";
                redisTemplate.delete(cacheKey);
            }

            return result > 0;

        } catch (Exception e) {
            log.error("更新系统通知配置失败", e);
            return false;
        }
    }

    // ========== 私有辅助方法 ==========

    /**
     * 检查发送频率限制
     */
    private boolean checkSendRateLimit(Long personId, String notificationType) {
        try {
            String cacheKey = CACHE_PREFIX_SEND_LIMIT + personId + ":" + notificationType;
            Integer count = (Integer) redisTemplate.opsForValue().get(cacheKey);

            if (count == null) {
                redisTemplate.opsForValue().set(cacheKey, 1, 1, TimeUnit.HOURS);
                return true;
            }

            if (count >= getMaxNotificationCount(notificationType)) {
                return false;
            }

            redisTemplate.opsForValue().increment(cacheKey);
            return true;

        } catch (Exception e) {
            log.error("检查发送频率限制失败: personId={}, type={}", personId, notificationType, e);
            return true; // 出错时不阻塞发送
        }
    }

    /**
     * 检查邮件发送限制
     */
    private boolean checkEmailSendLimit(Long personId) {
        String cacheKey = CACHE_PREFIX_SEND_LIMIT + personId + ":EMAIL";
        Integer count = (Integer) redisTemplate.opsForValue().get(cacheKey);
        return count == null || count < MAX_EMAIL_PER_HOUR;
    }

    /**
     * 检查短信发送限制
     */
    private boolean checkSmsSendLimit(Long personId) {
        String cacheKey = CACHE_PREFIX_SEND_LIMIT + personId + ":SMS";
        Integer count = (Integer) redisTemplate.opsForValue().get(cacheKey);
        return count == null || count < MAX_SMS_PER_HOUR;
    }

    /**
     * 检查推送发送限制
     */
    private boolean checkPushSendLimit(Long personId) {
        String cacheKey = CACHE_PREFIX_SEND_LIMIT + personId + ":PUSH";
        Integer count = (Integer) redisTemplate.opsForValue().get(cacheKey);
        return count == null || count < MAX_PUSH_PER_HOUR;
    }

    /**
     * 根据渠道发送通知
     */
    private SecurityNotificationResult sendNotificationByChannel(Long personId, String channel, String message,
            Object relatedData) {
        switch (channel.toUpperCase()) {
            case "EMAIL":
                return sendEmailNotification(personId, "安全通知", message, null);
            case "SMS":
                return sendSmsNotification(personId, message, null, null);
            case "PUSH":
                return sendPushNotification(personId, "安全通知", message, null, null);
            case "WECHAT":
                return sendWechatNotification(personId, message, "text", null);
            default:
                return SecurityNotificationResult.failure("UNSUPPORTED_CHANNEL", "不支持的通知渠道: " + channel);
        }
    }

    /**
     * 评估通知发送结果
     */
    private SecurityNotificationResult evaluateNotificationResults(List<SecurityNotificationResult> results) {
        if (results.isEmpty()) {
            return SecurityNotificationResult.failure("NO_CHANNELS", "没有可用的通知渠道");
        }

        long successCount = results.stream().filter(SecurityNotificationResult::isSuccess).count();

        if (successCount == results.size()) {
            return SecurityNotificationResult.success("所有渠道发送成功");
        } else if (successCount > 0) {
            return SecurityNotificationResult.success("部分渠道发送成功",
                    String.format("成功: %d/%d", successCount, results.size()));
        } else {
            return SecurityNotificationResult.failure("ALL_FAILED", "所有渠道发送失败");
        }
    }

    /**
     * 记录通知日志
     */
    private void recordNotificationLog(Long personId, String channel, String type, String message,
            SecurityNotificationResult result) {
        try {
            SecurityNotificationLogEntity logEntity = SecurityNotificationLogEntity.builder()
                    .personId(personId)
                    .channel(channel)
                    .notificationType(type)
                    .content(message)
                    .status(result.isSuccess() ? "SUCCESS" : "FAILED")
                    .errorMessage(result.getMessage())
                    .messageId(result.getMessageId())
                    .createTime(LocalDateTime.now())
                    .build();

            securityNotificationDao.insertNotificationLog(logEntity);

        } catch (Exception e) {
            log.error("记录通知日志失败: personId={}, channel={}", personId, channel, e);
        }
    }

    /**
     * 记录紧急告警日志
     */
    private void recordEmergencyAlertLog(Long personId, String alertType, String alertLevel, String alertMessage,
            Object relatedData) {
        try {
            String extraData = null;
            if (relatedData != null) {
                extraData = objectMapper.writeValueAsString(relatedData);
            }

            SecurityNotificationLogEntity logEntity = SecurityNotificationLogEntity.builder()
                    .personId(personId)
                    .channel("EMERGENCY_ALERT")
                    .notificationType("SECURITY_ALERT")
                    .content(String.format("[%s] %s: %s", alertLevel, alertType, alertMessage))
                    .status("SUCCESS")
                    .extraData(extraData)
                    .createTime(LocalDateTime.now())
                    .build();

            securityNotificationDao.insertNotificationLog(logEntity);

        } catch (JsonProcessingException e) {
            log.error("序列化紧急告警数据失败: personId={}", personId, e);
        } catch (Exception e) {
            log.error("记录紧急告警日志失败: personId={}", personId, e);
        }
    }

    /**
     * 获取通知渠道
     */
    private List<String> getNotificationChannels(String notificationType, UserNotificationPreference preference) {
        List<String> channels = new ArrayList<>();

        if (preference.isEmailEnabled()) {
            channels.add("EMAIL");
        }
        if (preference.isSmsEnabled()) {
            channels.add("SMS");
        }
        if (preference.isPushEnabled()) {
            channels.add("PUSH");
        }
        if (preference.isWechatEnabled()) {
            channels.add("WECHAT");
        }

        return channels;
    }

    /**
     * 获取用户邮箱地址
     */
    private String getUserEmailAddress(Long personId) {
        // TODO: 调用用户服务获取邮箱地址
        return "user" + personId + "@example.com";
    }

    /**
     * 获取用户手机号码
     */
    private String getUserPhoneNumber(Long personId) {
        // TODO: 调用用户服务获取手机号码
        return "1380013800" + (personId % 10);
    }

    /**
     * 获取用户设备令牌
     */
    private List<String> getUserDeviceTokens(Long personId) {
        // TODO: 调用设备服务获取设备令牌
        return Arrays.asList("device_token_" + personId);
    }

    /**
     * 获取用户微信OpenID
     */
    private String getUserWechatOpenId(Long personId) {
        // TODO: 调用微信服务获取OpenID
        return "openid_" + personId;
    }

    /**
     * 重新发送通知
     */
    private SecurityNotificationResult resendNotification(SecurityNotificationLogEntity notificationLog) {
        return sendNotificationByChannel(
                notificationLog.getPersonId(),
                notificationLog.getChannel(),
                notificationLog.getContent(),
                null);
    }

    /**
     * 创建默认通知偏好
     */
    private UserNotificationPreference createDefaultNotificationPreference(Long personId) {
        return UserNotificationPreference.builder()
                .personId(personId)
                .emailEnabled(true)
                .smsEnabled(true)
                .pushEnabled(true)
                .wechatEnabled(false)
                .notificationTypes(Arrays.asList("SECURITY_ALERT", "ACCOUNT_CHANGE", "TRANSACTION_ANOMALY"))
                .build();
    }

    /**
     * 创建默认系统配置
     */
    private SystemNotificationConfig createDefaultSystemNotificationConfig() {
        return SystemNotificationConfig.builder()
                .emailEnabled(true)
                .smsEnabled(true)
                .pushEnabled(true)
                .wechatEnabled(false)
                .maxEmailPerHour(MAX_EMAIL_PER_HOUR)
                .maxSmsPerHour(MAX_SMS_PER_HOUR)
                .maxPushPerHour(MAX_PUSH_PER_HOUR)
                .retryAttempts(3)
                .retryInterval(5)
                .build();
    }

    /**
     * 转换通知偏好实体
     */
    private UserNotificationPreference convertToNotificationPreference(UserNotificationPreferenceEntity entity) {
        List<String> notificationTypes = new ArrayList<>();
        if (SmartStringUtil.isNotEmpty(entity.getNotificationTypes())) {
            notificationTypes = Arrays.asList(entity.getNotificationTypes().split(","));
        }

        return UserNotificationPreference.builder()
                .personId(entity.getPersonId())
                .emailEnabled(entity.getEmailEnabled())
                .smsEnabled(entity.getSmsEnabled())
                .pushEnabled(entity.getPushEnabled())
                .wechatEnabled(entity.getWechatEnabled())
                .notificationTypes(notificationTypes)
                .build();
    }

    /**
     * 转换系统配置实体
     */
    private SystemNotificationConfig convertToSystemNotificationConfig(SystemNotificationConfigEntity entity) {
        return SystemNotificationConfig.builder()
                .emailEnabled(entity.getEmailEnabled())
                .smsEnabled(entity.getSmsEnabled())
                .pushEnabled(entity.getPushEnabled())
                .wechatEnabled(entity.getWechatEnabled())
                .maxEmailPerHour(entity.getMaxEmailPerHour())
                .maxSmsPerHour(entity.getMaxSmsPerHour())
                .maxPushPerHour(entity.getMaxPushPerHour())
                .retryAttempts(entity.getRetryAttempts())
                .retryInterval(entity.getRetryInterval())
                .build();
    }

    /**
     * 转换系统配置实体
     */
    private SystemNotificationConfigEntity convertToSystemNotificationConfigEntity(SystemNotificationConfig config) {
        return SystemNotificationConfigEntity.builder()
                .emailEnabled(config.isEmailEnabled())
                .smsEnabled(config.isSmsEnabled())
                .pushEnabled(config.isPushEnabled())
                .wechatEnabled(config.isWechatEnabled())
                .maxEmailPerHour(config.getMaxEmailPerHour())
                .maxSmsPerHour(config.getMaxSmsPerHour())
                .maxPushPerHour(config.getMaxPushPerHour())
                .retryAttempts(config.getRetryAttempts())
                .retryInterval(config.getRetryInterval())
                .updateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 更新发送计数
     */
    private void updateEmailSendCount(Long personId) {
        String cacheKey = CACHE_PREFIX_SEND_LIMIT + personId + ":EMAIL";
        redisTemplate.opsForValue().increment(cacheKey);
        redisTemplate.expire(cacheKey, 1, TimeUnit.HOURS);
    }

    private void updateSmsSendCount(Long personId) {
        String cacheKey = CACHE_PREFIX_SEND_LIMIT + personId + ":SMS";
        redisTemplate.opsForValue().increment(cacheKey);
        redisTemplate.expire(cacheKey, 1, TimeUnit.HOURS);
    }

    private void updatePushSendCount(Long personId) {
        String cacheKey = CACHE_PREFIX_SEND_LIMIT + personId + ":PUSH";
        redisTemplate.opsForValue().increment(cacheKey);
        redisTemplate.expire(cacheKey, 1, TimeUnit.HOURS);
    }

    /**
     * 清除通知偏好缓存
     */
    private void clearNotificationPreferenceCache(Long personId) {
        String cacheKey = CACHE_PREFIX_PREFERENCE + personId;
        redisTemplate.delete(cacheKey);
    }

    /**
     * 获取最大通知次数
     */
    private int getMaxNotificationCount(String notificationType) {
        switch (notificationType) {
            case "EMAIL":
                return MAX_EMAIL_PER_HOUR;
            case "SMS":
                return MAX_SMS_PER_HOUR;
            case "PUSH":
                return MAX_PUSH_PER_HOUR;
            default:
                return 20; // 默认限制
        }
    }

    /**
     * 获取告警级别描述
     */
    private String getAlertLevelDescription(String alertLevel) {
        switch (alertLevel.toUpperCase()) {
            case "LOW":
                return "低风险";
            case "MEDIUM":
                return "中风险";
            case "HIGH":
                return "高风险";
            case "CRITICAL":
                return "严重风险";
            default:
                return "未知级别";
        }
    }

    /**
     * 获取状态类型描述
     */
    private String getStatusTypeDescription(String statusType) {
        switch (statusType.toUpperCase()) {
            case "FREEZE":
                return "冻结";
            case "UNFREEZE":
                return "解冻";
            case "LOCK":
                return "锁定";
            case "UNLOCK":
                return "解锁";
            case "SUSPEND":
                return "暂停";
            case "ACTIVATE":
                return "激活";
            default:
                return statusType;
        }
    }

    /**
     * 获取重置类型描述
     */
    private String getResetTypeDescription(String resetType) {
        switch (resetType.toUpperCase()) {
            case "PASSWORD":
                return "登录密码";
            case "PAYMENT_PASSWORD":
                return "支付密码";
            default:
                return "密码";
        }
    }

    /**
     * 获取限额类型描述
     */
    private String getLimitTypeDescription(String limitType) {
        switch (limitType.toUpperCase()) {
            case "DAILY":
                return "日";
            case "WEEKLY":
                return "周";
            case "MONTHLY":
                return "月";
            case "SINGLE":
                return "单笔";
            default:
                return limitType;
        }
    }

    /**
     * 获取渠道描述
     */
    private String getChannelDescription(String channel) {
        switch (channel.toUpperCase()) {
            case "EMAIL":
                return "邮件";
            case "SMS":
                return "短信";
            case "PUSH":
                return "App推送";
            case "WECHAT":
                return "微信";
            default:
                return channel;
        }
    }
}
