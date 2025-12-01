/*
 * 推送通知服务接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.service;

import jakarta.validation.constraints.NotNull;
import net.lab1024.sa.admin.module.consume.domain.vo.PushNotificationRequest;
import net.lab1024.sa.admin.module.consume.domain.vo.PushNotificationResult;

import java.util.List;
import java.util.Map;

/**
 * 推送通知服务接口
 * 提供App推送通知功能
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
public interface PushNotificationService {

    /**
     * 发送推送通知
     *
     * @param pushRequest 推送请求
     * @return 发送结果
     */
    PushNotificationResult sendPush(@NotNull PushNotificationRequest pushRequest);

    /**
     * 发送单个设备推送
     *
     * @param deviceToken 设备令牌
     * @param title 推送标题
     * @param content 推送内容
     * @return 发送结果
     */
    PushNotificationResult sendPush(@NotNull String deviceToken, @NotNull String title, @NotNull String content);

    /**
     * 批量发送推送
     *
     * @param pushRequests 推送请求列表
     * @return 批量发送结果
     */
    List<PushNotificationResult> batchSendPush(@NotNull List<PushNotificationRequest> pushRequests);

    /**
     * 广播推送（发送给所有用户）
     *
     * @param title 推送标题
     * @param content 推送内容
     * @return 发送结果
     */
    PushNotificationResult broadcastPush(@NotNull String title, @NotNull String content);

    /**
     * 按标签推送
     *
     * @param tags 标签列表
     * @param title 推送标题
     * @param content 推送内容
     * @return 发送结果
     */
    PushNotificationResult sendPushByTags(@NotNull List<String> tags, @NotNull String title, @NotNull String content);

    /**
     * 按用户ID推送
     *
     * @param userIds 用户ID列表
     * @param title 推送标题
     * @param content 推送内容
     * @return 发送结果
     */
    PushNotificationResult sendPushByUserIds(@NotNull List<Long> userIds, @NotNull String title, @NotNull String content);

    /**
     * 发送静默推送
     *
     * @param deviceToken 设备令牌
     * @param extraData 额外数据
     * @return 发送结果
     */
    PushNotificationResult sendSilentPush(@NotNull String deviceToken, @NotNull Map<String, Object> extraData);

    /**
     * 检查推送服务状态
     *
     * @return 服务状态
     */
    PushServiceStatus checkServiceStatus();

    /**
     * 获取推送统计信息
     *
     * @param timeRange 时间范围
     * @return 统计信息
     */
    PushStatistics getSendStatistics(String timeRange);

    /**
     * 验证设备令牌格式
     *
     * @param deviceToken 设备令牌
     * @return 是否有效
     */
    boolean validateDeviceToken(@NotNull String deviceToken);

    /**
     * 测试推送发送
     *
     * @param testDeviceToken 测试设备令牌
     * @return 测试结果
     */
    PushNotificationResult testPushSending(@NotNull String testDeviceToken);

    /**
     * 查询推送状态
     *
     * @param messageId 消息ID
     * @return 推送状态
     */
    PushNotificationResult queryPushStatus(@NotNull String messageId);

    /**
     * 获取设备注册列表
     *
     * @param userId 用户ID
     * @return 设备令牌列表
     */
    List<String> getUserDeviceTokens(@NotNull Long userId);

    /**
     * 注册设备令牌
     *
     * @param userId 用户ID
     * @param deviceToken 设备令牌
     * @param deviceInfo 设备信息
     * @return 注册结果
     */
    boolean registerDeviceToken(@NotNull Long userId, @NotNull String deviceToken, Map<String, Object> deviceInfo);

    /**
     * 注销设备令牌
     *
     * @param userId 用户ID
     * @param deviceToken 设备令牌
     * @return 注销结果
     */
    boolean unregisterDeviceToken(@NotNull Long userId, @NotNull String deviceToken);

    /**
     * 获取推送模板列表
     *
     * @return 模板列表
     */
    List<PushTemplate> getTemplateList();
}

/**
 * 推送模板
 */
class PushTemplate {
    private String templateCode;
    private String templateName;
    private String title;
    private String content;
    private Map<String, Object> templateParams;
    private Boolean isActive;
}

/**
 * 推送服务状态
 */
class PushServiceStatus {
    private boolean available;
    private String serviceProvider;
    private String connectionStatus;
    private java.time.LocalDateTime lastCheckTime;
    private Long responseTimeMs;
    private String statusDescription;
    private Integer registeredDeviceCount;
}

/**
 * 推送统计信息
 */
class PushStatistics {
    private String timeRange;
    private Integer totalSent;
    private Integer successSent;
    private Integer failedSent;
    private Integer deliveredCount;
    private Integer openedCount;
    private Double successRate;
    private Double openRate;
    private Map<String, Integer> platformCount; // iOS/Android统计
    private java.time.LocalDateTime startTime;
    private java.time.LocalDateTime endTime;
}