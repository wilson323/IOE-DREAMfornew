/*
 * 邮件服务接口
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.service;

import jakarta.validation.constraints.NotNull;
import net.lab1024.sa.admin.module.consume.domain.vo.EmailRequest;
import net.lab1024.sa.admin.module.consume.domain.vo.EmailResult;
import net.lab1024.sa.admin.module.consume.domain.enums.EmailServiceStatus;
import net.lab1024.sa.admin.module.consume.domain.vo.EmailStatistics;

import java.util.List;

/**
 * 邮件服务接口
 * 提供邮件发送功能
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
public interface EmailService {

    /**
     * 发送邮件
     *
     * @param emailRequest 邮件请求
     * @return 发送结果
     */
    EmailResult sendEmail(@NotNull EmailRequest emailRequest);

    /**
     * 发送邮件给多个收件人
     *
     * @param to 收件人列表
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return 发送结果
     */
    EmailResult sendEmail(@NotNull List<String> to, @NotNull String subject, @NotNull String content);

    /**
     * 发送HTML邮件
     *
     * @param to 收件人
     * @param subject 邮件主题
     * @param htmlContent HTML内容
     * @param attachments 附件列表
     * @return 发送结果
     */
    EmailResult sendHtmlEmail(@NotNull String to, @NotNull String subject, @NotNull String htmlContent,
                              List<String> attachments);

    /**
     * 发送模板邮件
     *
     * @param to 收件人
     * @param templateCode 模板代码
     * @param templateParams 模板参数
     * @return 发送结果
     */
    EmailResult sendTemplateEmail(@NotNull String to, @NotNull String templateCode,
                                  java.util.Map<String, Object> templateParams);

    /**
     * 批量发送邮件
     *
     * @param emailRequests 邮件请求列表
     * @return 批量发送结果
     */
    List<EmailResult> batchSendEmail(@NotNull List<EmailRequest> emailRequests);

    /**
     * 检查邮件服务状态
     *
     * @return 服务状态
     */
    EmailServiceStatus checkServiceStatus();

    /**
     * 获取邮件发送统计
     *
     * @param timeRange 时间范围
     * @return 统计信息
     */
    EmailStatistics getSendStatistics(String timeRange);

    /**
     * 验证邮件地址格式
     *
     * @param emailAddress 邮件地址
     * @return 是否有效
     */
    boolean validateEmailAddress(@NotNull String emailAddress);

    /**
     * 测试邮件发送
     *
     * @param testEmail 测试邮箱地址
     * @return 测试结果
     */
    EmailResult testEmailSending(@NotNull String testEmail);
}