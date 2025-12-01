/*
 * 微信通知服务接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.service;

import jakarta.validation.constraints.NotNull;
import net.lab1024.sa.admin.module.consume.domain.vo.WechatNotificationRequest;
import net.lab1024.sa.admin.module.consume.domain.vo.WechatNotificationResult;

import java.util.List;
import java.util.Map;

/**
 * 微信通知服务接口
 * 提供微信公众号/企业微信通知功能
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
public interface WechatNotificationService {

    /**
     * 发送微信消息
     *
     * @param wechatRequest 微信请求
     * @return 发送结果
     */
    WechatNotificationResult sendMessage(@NotNull WechatNotificationRequest wechatRequest);

    /**
     * 发送文本消息
     *
     * @param openId 用户OpenID
     * @param content 文本内容
     * @return 发送结果
     */
    WechatNotificationResult sendTextMessage(@NotNull String openId, @NotNull String content);

    /**
     * 发送模板消息
     *
     * @param openId 用户OpenID
     * @param templateId 模板ID
     * @param data 模板数据
     * @param url 跳转链接
     * @return 发送结果
     */
    WechatNotificationResult sendTemplateMessage(@NotNull String openId, @NotNull String templateId,
                                                Map<String, Object> data, String url);

    /**
     * 批量发送微信消息
     *
     * @param wechatRequests 微信请求列表
     * @return 批量发送结果
     */
    List<WechatNotificationResult> batchSendMessage(@NotNull List<WechatNotificationRequest> wechatRequests);

    /**
     * 发送图文消息
     *
     * @param openId 用户OpenID
     * @param title 标题
     * @param description 描述
     * @param url 跳转链接
     * @param picUrl 图片链接
     * @return 发送结果
     */
    WechatNotificationResult sendNewsMessage(@NotNull String openId, @NotNull String title,
                                            String description, @NotNull String url, String picUrl);

    /**
     * 发送小程序消息
     *
     * @param openId 用户OpenID
     * @param templateId 模板ID
     * @param page 小程序页面路径
     * @param data 模板数据
     * @return 发送结果
     */
    WechatNotificationResult sendMiniProgramMessage(@NotNull String openId, @NotNull String templateId,
                                                   String page, Map<String, Object> data);

    /**
     * 检查微信服务状态
     *
     * @return 服务状态
     */
    WechatServiceStatus checkServiceStatus();

    /**
     * 获取微信发送统计
     *
     * @param timeRange 时间范围
     * @return 统计信息
     */
    WechatStatistics getSendStatistics(String timeRange);

    /**
     * 验证OpenID格式
     *
     * @param openId OpenID
     * @return 是否有效
     */
    boolean validateOpenId(@NotNull String openId);

    /**
     * 获取用户关注状态
     *
     * @param openId 用户OpenID
     * @return 关注状态
     */
    boolean isUserSubscribed(@NotNull String openId);

    /**
     * 测试微信消息发送
     *
     * @param testOpenId 测试OpenID
     * @return 测试结果
     */
    WechatNotificationResult testMessageSending(@NotNull String testOpenId);

    /**
     * 查询消息发送状态
     *
     * @param messageId 消息ID
     * @return 发送状态
     */
    WechatNotificationResult queryMessageStatus(@NotNull String messageId);

    /**
     * 获取微信模板列表
     *
     * @return 模板列表
     */
    List<WechatTemplate> getTemplateList();

    /**
     * 获取用户OpenID列表
     *
     * @param userId 用户ID
     * @return OpenID列表
     */
    List<String> getUserOpenIds(@NotNull Long userId);

    /**
     * 绑定用户OpenID
     *
     * @param userId 用户ID
     * @param openId OpenID
     * @param userInfo 用户信息
     * @return 绑定结果
     */
    boolean bindUserOpenId(@NotNull Long userId, @NotNull String openId, Map<String, Object> userInfo);

    /**
     * 解绑用户OpenID
     *
     * @param userId 用户ID
     * @param openId OpenID
     * @return 解绑结果
     */
    boolean unbindUserOpenId(@NotNull Long userId, @NotNull String openId);

    /**
     * 发送安全通知模板消息
     *
     * @param openId 用户OpenID
     * @param securityType 安全类型
     * @param message 消息内容
     * @param actionUrl 操作链接
     * @return 发送结果
     */
    WechatNotificationResult sendSecurityTemplateMessage(@NotNull String openId, @NotNull String securityType,
                                                        @NotNull String message, String actionUrl);
}

/**
 * 微信模板
 */
class WechatTemplate {
    private String templateId;
    private String title;
    private String content;
    private Map<String, String> exampleData;
    private Boolean isActive;
}

/**
 * 微信服务状态
 */
class WechatServiceStatus {
    private boolean available;
    private String accountType; // SERVICE_ACCOUNT / SUBSCRIPTION_ACCOUNT / ENTERPRISE
    private String connectionStatus;
    private java.time.LocalDateTime lastCheckTime;
    private Long responseTimeMs;
    private String statusDescription;
    private Integer subscriberCount;
}

/**
 * 微信统计信息
 */
class WechatStatistics {
    private String timeRange;
    private Integer totalSent;
    private Integer successSent;
    private Integer failedSent;
    private Integer deliveredCount;
    private Integer readCount;
    private Double successRate;
    private Double readRate;
    private Map<String, Integer> messageTypeCount;
    private java.time.LocalDateTime startTime;
    private java.time.LocalDateTime endTime;
}