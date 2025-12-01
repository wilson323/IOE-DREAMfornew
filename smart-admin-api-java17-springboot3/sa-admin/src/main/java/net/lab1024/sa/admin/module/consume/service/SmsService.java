/*
 * 短信服务接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.service;

import jakarta.validation.constraints.NotNull;
import net.lab1024.sa.admin.module.consume.domain.vo.SmsRequest;
import net.lab1024.sa.admin.module.consume.domain.vo.SmsResult;

import java.util.List;
import java.util.Map;

/**
 * 短信服务接口
 * 提供短信发送功能
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
public interface SmsService {

    /**
     * 发送短信
     *
     * @param smsRequest 短信请求
     * @return 发送结果
     */
    SmsResult sendSms(@NotNull SmsRequest smsRequest);

    /**
     * 发送短信给单个手机号
     *
     * @param phoneNumber 手机号码
     * @param content 短信内容
     * @return 发送结果
     */
    SmsResult sendSms(@NotNull String phoneNumber, @NotNull String content);

    /**
     * 发送模板短信
     *
     * @param phoneNumber 手机号码
     * @param templateCode 模板代码
     * @param templateParams 模板参数
     * @return 发送结果
     */
    SmsResult sendTemplateSms(@NotNull String phoneNumber, @NotNull String templateCode,
                              Map<String, Object> templateParams);

    /**
     * 批量发送短信
     *
     * @param smsRequests 短信请求列表
     * @return 批量发送结果
     */
    List<SmsResult> batchSendSms(@NotNull List<SmsRequest> smsRequests);

    /**
     * 发送验证码短信
     *
     * @param phoneNumber 手机号码
     * @param verificationCode 验证码
     * @param expireMinutes 过期时间（分钟）
     * @return 发送结果
     */
    SmsResult sendVerificationCode(@NotNull String phoneNumber, @NotNull String verificationCode,
                                  Integer expireMinutes);

    /**
     * 检查短信服务状态
     *
     * @return 服务状态
     */
    SmsServiceStatus checkServiceStatus();

    /**
     * 获取短信发送统计
     *
     * @param timeRange 时间范围
     * @return 统计信息
     */
    SmsStatistics getSendStatistics(String timeRange);

    /**
     * 验证手机号码格式
     *
     * @param phoneNumber 手机号码
     * @return 是否有效
     */
    boolean validatePhoneNumber(@NotNull String phoneNumber);

    /**
     * 检查短信发送频率限制
     *
     * @param phoneNumber 手机号码
     * @param businessType 业务类型
     * @return 是否允许发送
     */
    boolean checkSendRateLimit(@NotNull String phoneNumber, @NotNull String businessType);

    /**
     * 测试短信发送
     *
     * @param testPhone 测试手机号
     * @return 测试结果
     */
    SmsResult testSmsSending(@NotNull String testPhone);

    /**
     * 查询短信发送状态
     *
     * @param messageId 消息ID
     * @return 发送状态
     */
    SmsResult querySmsStatus(@NotNull String messageId);

    /**
     * 获取短信模板列表
     *
     * @return 模板列表
     */
    List<SmsTemplate> getTemplateList();

    /**
     * 获取可用短信额度
     *
     * @return 可用额度
     */
    Integer getAvailableQuota();

    /**
     * 发送国际短信
     *
     * @param phoneNumber 手机号码（含国际区号）
     * @param content 短信内容
     * @param countryCode 国家代码
     * @return 发送结果
     */
    SmsResult sendInternationalSms(@NotNull String phoneNumber, @NotNull String content,
                                  @NotNull String countryCode);
}

/**
 * 短信模板
 */
class SmsTemplate {
    private String templateCode;
    private String templateName;
    private String templateContent;
    private Map<String, Object> templateParams;
    private Boolean isActive;
}

/**
 * 短信服务状态
 */
class SmsServiceStatus {
    private boolean available;
    private String serviceProvider;
    private String connectionStatus;
    private java.time.LocalDateTime lastCheckTime;
    private Long responseTimeMs;
    private String statusDescription;
    private Integer availableQuota;
}

/**
 * 短信统计信息
 */
class SmsStatistics {
    private String timeRange;
    private Integer totalSent;
    private Integer successSent;
    private Integer failedSent;
    private Double successRate;
    private Long averageResponseTimeMs;
    private Map<String, Integer> businessTypeCount;
    private Map<String, Integer> carrierCount;
    private java.time.LocalDateTime startTime;
    private java.time.LocalDateTime endTime;
}