package net.lab1024.sa.notification.service;

import net.lab1024.sa.notification.domain.entity.NotificationMessageEntity;
import net.lab1024.sa.notification.domain.entity.NotificationTemplateEntity;

import java.util.List;
import java.util.Map;

/**
 * 通知服务接口
 * <p>
 * 提供完整的多渠道通知服务
 * 严格遵循repowiki规范:
 * - 定义清晰的服务契约
 * - 支持多种通知渠道
 * - 提供异步发送能力
 * - 包含完整的统计功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
public interface NotificationService {

    /**
     * 发送简单文本消息
     *
     * @param recipientUserId 接收用户ID
     * @param channel          发送渠道
     * @param subject          消息主题
     * @param content          消息内容
     * @return 消息ID
     */
    Long sendSimpleMessage(Long recipientUserId, Integer channel, String subject, String content);

    /**
     * 发送模板消息
     *
     * @param recipientUserId 接收用户ID
     * @param templateCode     模板编码
     * @param templateParams   模板参数
     * @return 消息ID
     */
    Long sendTemplateMessage(Long recipientUserId, String templateCode, Map<String, Object> templateParams);

    /**
     * 发送指定渠道的模板消息
     *
     * @param recipientUserId 接收用户ID
     * @param channel          发送渠道
     * @param templateCode     模板编码
     * @param templateParams   模板参数
     * @return 消息ID
     */
    Long sendTemplateMessage(Long recipientUserId, Integer channel, String templateCode, Map<String, Object> templateParams);

    /**
     * 批量发送消息
     *
     * @param recipientUserIds 接收用户ID列表
     * @param channel          发送渠道
     * @param subject          消息主题
     * @param content          消息内容
     * @return 批次ID
     */
    String batchSendMessage(List<Long> recipientUserIds, Integer channel, String subject, String content);

    /**
     * 批量发送模板消息
     *
     * @param recipientUserIds 接收用户ID列表
     * @param templateCode     模板编码
     * @param templateParams   模板参数
     * @return 批次ID
     */
    String batchSendTemplateMessage(List<Long> recipientUserIds, String templateCode, Map<String, Object> templateParams);

    /**
     * 发送验证码
     *
     * @param recipientUserId 接收用户ID
     * @param channel          发送渠道（通常为短信或邮件）
     * @param verificationCode 验证码
     * @param expireMinutes    过期时间（分钟）
     * @return 消息ID
     */
    Long sendVerificationCode(Long recipientUserId, Integer channel, String verificationCode, Integer expireMinutes);

    /**
     * 发送系统通知
     *
     * @param recipientUserId 接收用户ID
     * @param subject          消息主题
     * @param content          消息内容
     * @param businessType     业务类型
     * @param businessId       业务ID
     * @return 消息ID
     */
    Long sendSystemNotification(Long recipientUserId, String subject, String content, String businessType, String businessId);

    /**
     * 发送业务通知
     *
     * @param recipientUserId 接收用户ID
     * @param subject          消息主题
     * @param content          消息内容
     * @param businessType     业务类型
     * @param businessId       业务ID
     * @return 消息ID
     */
    Long sendBusinessNotification(Long recipientUserId, String subject, String content, String businessType, String businessId);

    /**
     * 发送告警通知
     *
     * @param recipientUserId 接收用户ID
     * @param subject          告警主题
     * @param content          告警内容
     * @param level            告警级别
     * @return 消息ID
     */
    Long sendAlertNotification(Long recipientUserId, String subject, String content, Integer level);

    /**
     * 发送营销通知
     *
     * @param recipientUserIds 接收用户ID列表
     * @param subject          营销主题
     * @param content          营销内容
     * @return 批次ID
     */
    String sendMarketingNotification(List<Long> recipientUserIds, String subject, String content);

    /**
     * 计划发送消息
     *
     * @param recipientUserId 接收用户ID
     * @param channel          发送渠道
     * @param subject          消息主题
     * @param content          消息内容
     * @param scheduleTime     计划发送时间
     * @return 消息ID
     */
    Long scheduleMessage(Long recipientUserId, Integer channel, String subject, String content, java.time.LocalDateTime scheduleTime);

    /**
     * 取消计划消息
     *
     * @param messageId 消息ID
     * @return 是否成功
     */
    boolean cancelScheduledMessage(Long messageId);

    /**
     * 重新发送消息
     *
     * @param messageId 消息ID
     * @return 是否成功
     */
    boolean resendMessage(Long messageId);

    /**
     * 重试失败的消息
     *
     * @param messageId 消息ID
     * @return 是否成功
     */
    boolean retryFailedMessage(Long messageId);

    /**
     * 标记站内信为已读
     *
     * @param messageId 消息ID
     * @param userId    用户ID
     * @return 是否成功
     */
    boolean markMessageAsRead(Long messageId, Long userId);

    /**
     * 批量标记站内信为已读
     *
     * @param messageIds 消息ID列表
     * @param userId     用户ID
     * @return 成功数量
     */
    int batchMarkMessagesAsRead(List<Long> messageIds, Long userId);

    /**
     * 查询用户消息列表
     *
     * @param userId    用户ID
     * @param channel   渠道（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param pageIndex 页码
     * @param pageSize  每页数量
     * @return 消息列表
     */
    List<NotificationMessageEntity> queryUserMessages(Long userId, Integer channel,
                                                      java.time.LocalDateTime startTime, java.time.LocalDateTime endTime,
                                                      Integer pageIndex, Integer pageSize);

    /**
     * 查询用户未读消息数量
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    int getUnreadMessageCount(Long userId);

    /**
     * 查询消息详情
     *
     * @param messageId 消息ID
     * @return 消息详情
     */
    NotificationMessageEntity getMessageDetail(Long messageId);

    /**
     * 查询发送记录
     *
     * @param messageId   消息ID
     * @param channel     渠道（可选）
     * @param sendStatus  发送状态（可选）
     * @param startTime   开始时间（可选）
     * @param endTime     结束时间（可选）
     * @param pageIndex   页码
     * @param pageSize    每页数量
     * @return 发送记录列表
     */
    List<NotificationMessageEntity> querySendRecords(Long messageId, Integer channel, Integer sendStatus,
                                                    java.time.LocalDateTime startTime, java.time.LocalDateTime endTime,
                                                    Integer pageIndex, Integer pageSize);

    /**
     * 获取发送统计
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param groupBy   分组方式
     * @return 统计数据
     */
    Map<String, Object> getSendStatistics(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime, String groupBy);

    /**
     * 获取用户发送统计
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计数据
     */
    Map<String, Object> getUserSendStatistics(Long userId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);

    /**
     * 获取渠道使用统计
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 统计数据
     */
    Map<String, Object> getChannelUsageStatistics(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);

    /**
     * 获取发送成功率统计
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 成功率统计
     */
    Map<String, Object> getSuccessRateStatistics(java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);

    /**
     * 查询可用模板
     *
     * @param channel     渠道
     * @param messageType 消息类型
     * @param language    语言
     * @return 模板列表
     */
    List<NotificationTemplateEntity> queryAvailableTemplates(Integer channel, Integer messageType, String language);

    /**
     * 预览模板消息
     *
     * @param templateCode   模板编码
     * @param templateParams 模板参数
     * @return 预览结果
     */
    Map<String, Object> previewTemplateMessage(String templateCode, Map<String, Object> templateParams);

    /**
     * 验证模板参数
     *
     * @param templateCode   模板编码
     * @param templateParams 模板参数
     * @return 验证结果
     */
    Map<String, Object> validateTemplateParams(String templateCode, Map<String, Object> templateParams);

    /**
     * 检查发送频率限制
     *
     * @param userId    用户ID
     * @param channel   渠道
     * @param messageType 消息类型
     * @return 检查结果
     */
    Map<String, Object> checkRateLimit(Long userId, Integer channel, Integer messageType);

    /**
     * 获取用户通知偏好
     *
     * @param userId 用户ID
     * @return 偏好设置
     */
    Map<String, Object> getUserNotificationPreferences(Long userId);

    /**
     * 更新用户通知偏好
     *
     * @param userId      用户ID
     * @param preferences 偏好设置
     * @return 是否成功
     */
    boolean updateUserNotificationPreferences(Long userId, Map<String, Object> preferences);

    /**
     * 清理过期消息
     *
     * @return 清理数量
     */
    int cleanExpiredMessages();

    /**
     * 处理待发送的消息（系统任务调用）
     */
    void processPendingMessages();

    /**
     * 处理重试消息（系统任务调用）
     */
    void processRetryMessages();

    /**
     * 同步配送状态（系统任务调用）
     */
    void syncDeliveryStatus();
}