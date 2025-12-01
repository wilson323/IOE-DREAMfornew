/*
 * 安全通知服务接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotNull;
import net.lab1024.sa.admin.module.consume.domain.vo.BatchNotificationResult;
import net.lab1024.sa.admin.module.consume.domain.vo.NotificationStatistics;
import net.lab1024.sa.admin.module.consume.domain.vo.SecurityNotificationRecord;
import net.lab1024.sa.admin.module.consume.domain.vo.SecurityNotificationResult;
import net.lab1024.sa.admin.module.consume.domain.vo.SystemNotificationConfig;
import net.lab1024.sa.admin.module.consume.domain.vo.UserNotificationPreference;

/**
 * 安全通知服务接口
 * 提供邮件、短信、推送等多渠道安全通知功能
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
public interface SecurityNotificationService {

    /**
     * 发送安全通知
     *
     * @param personId         人员ID
     * @param notificationType 通知类型
     * @param message          通知消息
     * @param relatedData      相关数据
     * @return 发送结果
     */
    SecurityNotificationResult sendNotification(@NotNull Long personId, @NotNull String notificationType,
            @NotNull String message, Object relatedData);

    /**
     * 发送邮件通知
     *
     * @param personId        人员ID
     * @param subject         邮件主题
     * @param content         邮件内容
     * @param attachmentFiles 附件文件列表
     * @return 发送结果
     */
    SecurityNotificationResult sendEmailNotification(@NotNull Long personId, @NotNull String subject,
            @NotNull String content, List<String> attachmentFiles);

    /**
     * 发送短信通知
     *
     * @param personId       人员ID
     * @param content        短信内容
     * @param templateCode   短信模板代码
     * @param templateParams 模板参数
     * @return 发送结果
     */
    SecurityNotificationResult sendSmsNotification(@NotNull Long personId, @NotNull String content,
            String templateCode, Map<String, Object> templateParams);

    /**
     * 发送App推送通知
     *
     * @param personId  人员ID
     * @param title     推送标题
     * @param content   推送内容
     * @param actionUrl 点击跳转URL
     * @param extraData 额外数据
     * @return 发送结果
     */
    SecurityNotificationResult sendPushNotification(@NotNull Long personId, @NotNull String title,
            @NotNull String content, String actionUrl,
            Map<String, Object> extraData);

    /**
     * 发送微信通知
     *
     * @param personId     人员ID
     * @param content      微信消息内容
     * @param messageType  消息类型（text/news/template等）
     * @param templateData 模板数据
     * @return 发送结果
     */
    SecurityNotificationResult sendWechatNotification(@NotNull Long personId, @NotNull String content,
            String messageType, Map<String, Object> templateData);

    /**
     * 批量发送通知
     *
     * @param personIds        人员ID列表
     * @param notificationType 通知类型
     * @param message          通知消息
     * @param relatedData      相关数据
     * @return 批量发送结果
     */
    BatchNotificationResult batchSendNotification(@NotNull List<Long> personIds, @NotNull String notificationType,
            @NotNull String message, Object relatedData);

    /**
     * 发送紧急安全告警
     *
     * @param personId     人员ID
     * @param alertType    告警类型
     * @param alertLevel   告警级别（LOW/MEDIUM/HIGH/CRITICAL）
     * @param alertMessage 告警消息
     * @param relatedData  相关数据
     * @return 发送结果
     */
    SecurityNotificationResult sendEmergencyAlert(@NotNull Long personId, @NotNull String alertType,
            @NotNull String alertLevel, @NotNull String alertMessage,
            Object relatedData);

    /**
     * 发送账户状态变更通知
     *
     * @param personId      人员ID
     * @param statusType    状态类型（FREEZE/UNFREEZE/LOCK/UNLOCK等）
     * @param statusMessage 状态变更消息
     * @param operatorId    操作人ID
     * @return 发送结果
     */
    SecurityNotificationResult sendAccountStatusNotification(@NotNull Long personId, @NotNull String statusType,
            @NotNull String statusMessage, Long operatorId);

    /**
     * 发送交易异常通知
     *
     * @param personId          人员ID
     * @param transactionId     交易ID
     * @param anomalyType       异常类型
     * @param anomalyMessage    异常消息
     * @param transactionAmount 交易金额
     * @return 发送结果
     */
    SecurityNotificationResult sendTransactionAnomalyNotification(@NotNull Long personId, @NotNull String transactionId,
            @NotNull String anomalyType, @NotNull String anomalyMessage,
            Object transactionAmount);

    /**
     * 发送登录异常通知
     *
     * @param personId      人员ID
     * @param loginLocation 登录地点
     * @param loginIp       登录IP
     * @param loginDevice   登录设备
     * @param loginTime     登录时间
     * @return 发送结果
     */
    SecurityNotificationResult sendLoginAnomalyNotification(@NotNull Long personId, String loginLocation,
            String loginIp, String loginDevice,
            LocalDateTime loginTime);

    /**
     * 发送密码重置通知
     *
     * @param personId    人员ID
     * @param resetType   重置类型（PASSWORD/PAYMENT_PASSWORD）
     * @param resetTime   重置时间
     * @param resetMethod 重置方式（EMAIL/SMS/MANUAL）
     * @return 发送结果
     */
    SecurityNotificationResult sendPasswordResetNotification(@NotNull Long personId, @NotNull String resetType,
            LocalDateTime resetTime, String resetMethod);

    /**
     * 发送限额提醒通知
     *
     * @param personId        人员ID
     * @param limitType       限额类型（DAILY/MONTHLY/SINGLE）
     * @param currentUsage    当前使用量
     * @param limitAmount     限额金额
     * @param usagePercentage 使用百分比
     * @return 发送结果
     */
    SecurityNotificationResult sendLimitReminderNotification(@NotNull Long personId, @NotNull String limitType,
            Object currentUsage, Object limitAmount,
            Double usagePercentage);

    /**
     * 设置用户通知偏好
     *
     * @param personId            人员ID
     * @param notificationChannel 通知渠道（EMAIL/SMS/PUSH/WECHAT）
     * @param isEnabled           是否启用
     * @param notificationTypes   通知类型列表
     * @return 设置结果
     */
    boolean setNotificationPreference(@NotNull Long personId, @NotNull String notificationChannel,
            boolean isEnabled, List<String> notificationTypes);

    /**
     * 获取用户通知偏好
     *
     * @param personId 人员ID
     * @return 通知偏好设置
     */
    UserNotificationPreference getNotificationPreference(@NotNull Long personId);

    /**
     * 检查通知渠道可用性
     *
     * @param personId            人员ID
     * @param notificationChannel 通知渠道
     * @return 是否可用
     */
    boolean isNotificationChannelAvailable(@NotNull Long personId, @NotNull String notificationChannel);

    /**
     * 获取通知历史记录
     *
     * @param personId         人员ID
     * @param notificationType 通知类型
     * @param startTime        开始时间
     * @param endTime          结束时间
     * @param limit            记录数量限制
     * @return 通知历史记录
     */
    List<SecurityNotificationRecord> getNotificationHistory(@NotNull Long personId, String notificationType,
            LocalDateTime startTime, LocalDateTime endTime,
            Integer limit);

    /**
     * 获取通知发送统计
     *
     * @param personId  人员ID
     * @param timeRange 时间范围（DAILY/WEEKLY/MONTHLY）
     * @return 统计信息
     */
    NotificationStatistics getNotificationStatistics(@NotNull Long personId, String timeRange);

    /**
     * 重试失败的通知
     *
     * @param notificationId 通知ID
     * @param retryCount     重试次数
     * @return 重试结果
     */
    boolean retryFailedNotification(@NotNull Long notificationId, Integer retryCount);

    /**
     * 取消待发送的通知
     *
     * @param notificationId 通知ID
     * @param cancelReason   取消原因
     * @return 取消结果
     */
    boolean cancelScheduledNotification(@NotNull Long notificationId, String cancelReason);

    /**
     * 测试通知渠道
     *
     * @param personId            人员ID
     * @param notificationChannel 通知渠道
     * @param testMessage         测试消息
     * @return 测试结果
     */
    SecurityNotificationResult testNotificationChannel(@NotNull Long personId, @NotNull String notificationChannel,
            @NotNull String testMessage);

    /**
     * 获取系统通知配置
     *
     * @return 系统通知配置
     */
    SystemNotificationConfig getSystemNotificationConfig();

    /**
     * 更新系统通知配置
     *
     * @param config 通知配置
     * @return 更新结果
     */
    boolean updateSystemNotificationConfig(@NotNull SystemNotificationConfig config);
}
