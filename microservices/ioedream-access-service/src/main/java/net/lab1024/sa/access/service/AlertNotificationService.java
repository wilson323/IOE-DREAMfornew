package net.lab1024.sa.access.service;

import net.lab1024.sa.common.entity.access.AlertNotificationEntity;
import net.lab1024.sa.common.entity.access.DeviceAlertEntity;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.List;

/**
 * 告警通知服务接口
 * <p>
 * 提供多渠道告警通知功能：
 * - 短信通知（SMS）
 * - 邮件通知（EMAIL）
 * - 应用推送（PUSH）
 * - WebSocket实时推送
 * </p>
 * <p>
 * 核心功能：
 * - 通知发送
 * - 通知重试机制
 * - 通知状态跟踪
 * - 通知失败处理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AlertNotificationService {

    /**
     * 发送告警通知
     * <p>
     * 根据规则配置的通知方式，向指定接收人发送告警通知
     * </p>
     *
     * @param alert      告警实体
     * @param ruleId     规则ID
     * @param recipients 接收人列表（JSON数组格式）
     * @return 是否成功
     */
    ResponseDTO<Void> sendNotification(DeviceAlertEntity alert, Long ruleId, String recipients);

    /**
     * 发送短信通知
     *
     * @param notification 通知实体
     * @return 是否成功
     */
    ResponseDTO<Boolean> sendSmsNotification(AlertNotificationEntity notification);

    /**
     * 发送邮件通知
     *
     * @param notification 通知实体
     * @return 是否成功
     */
    ResponseDTO<Boolean> sendEmailNotification(AlertNotificationEntity notification);

    /**
     * 发送应用推送通知
     *
     * @param notification 通知实体
     * @return 是否成功
     */
    ResponseDTO<Boolean> sendPushNotification(AlertNotificationEntity notification);

    /**
     * 发送WebSocket实时推送
     *
     * @param notification 通知实体
     * @return 是否成功
     */
    ResponseDTO<Boolean> sendWebSocketNotification(AlertNotificationEntity notification);

    /**
     * 重试失败的通知
     * <p>
     * 查询所有发送失败且未达最大重试次数的通知，重新发送
     * </p>
     *
     * @return 重试成功的数量
     */
    ResponseDTO<Integer> retryFailedNotifications();

    /**
     * 查询告警的所有通知记录
     *
     * @param alertId 告警ID
     * @return 通知记录列表
     */
    ResponseDTO<List<AlertNotificationEntity>> queryNotificationByAlertId(Long alertId);

    /**
     * 查询指定接收人的未读通知
     *
     * @param recipientId 接收人ID
     * @return 未读通知列表
     */
    ResponseDTO<List<AlertNotificationEntity>> queryUnreadNotifications(Long recipientId);

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     * @return 是否成功
     */
    ResponseDTO<Void> markNotificationAsRead(Long notificationId);

    /**
     * 批量标记通知为已读
     *
     * @param notificationIds 通知ID列表
     * @return 标记成功的数量
     */
    ResponseDTO<Integer> batchMarkAsRead(List<Long> notificationIds);
}
