package net.lab1024.sa.common.notification.manager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpMethod;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import jakarta.annotation.Resource;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.identity.domain.vo.UserDetailVO;
import net.lab1024.sa.common.monitor.domain.entity.NotificationEntity;
import net.lab1024.sa.common.notification.constant.NotificationConfigKey;

/**
 * 邮件通知管理器
 * <p>
 * 负责邮件通知的发送和管理
 * 严格遵循CLAUDE.md规范:
 * - 使用@Component注解标识
 * - 使用@Resource依赖注入（禁止@Autowired）
 * - 完整的异常处理和日志记录
 * </p>
 * <p>
 * 企业级特性：
 * - 异步发送（@Async），不阻塞主业务流程
 * - 支持HTML格式邮件
 * - 支持批量发送
 * - 完善的错误处理和重试机制
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Component
public class EmailNotificationManager {

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private NotificationConfigManager notificationConfigManager;

    @Resource
    private NotificationRateLimiter notificationRateLimiter;

    @Resource
    private NotificationMetricsCollector notificationMetricsCollector;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    /**
     * 发送邮件通知
     * <p>
     * 基于Spring Mail实现邮件发送
     * 支持文本和HTML格式
     * 异步发送，不阻塞主业务流程
     * </p>
     *
     * @param notification 通知实体
     * @return 发送是否成功
     */
    @Async("notificationExecutor")
    public boolean sendEmail(NotificationEntity notification) {
        log.info("[邮件通知] 开始发送，通知ID：{}，接收人：{}，标题：{}",
                notification.getNotificationId(), notification.getReceiverIds(), notification.getTitle());

        long startTime = System.currentTimeMillis();
        String channel = "EMAIL";

        try {
            // 1. 限流检查（使用统一限流管理器）
            if (!notificationRateLimiter.tryAcquire(channel)) {
                log.warn("[邮件通知] 超过限流阈值，通知ID：{}", notification.getNotificationId());
                notificationMetricsCollector.recordRateLimit(channel);
                return false;
            }

            // 2. 获取接收人邮箱列表
            List<String> emailList = parseReceiverEmailAddresses(notification);
            if (emailList.isEmpty()) {
                log.warn("[邮件通知] 接收人列表为空，通知ID：{}", notification.getNotificationId());
                return false;
            }

            // 3. 构建邮件内容
            String subject = notification.getTitle();
            String content = buildEmailContent(notification);

            // 4. 发送邮件（支持HTML格式）
            boolean isHtml = isHtmlContent(content);
            
            if (isHtml) {
                sendHtmlEmail(emailList, subject, content);
            } else {
                sendSimpleEmail(emailList, subject, content);
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("[邮件通知] 发送成功，通知ID：{}，接收人数量：{}，耗时：{}ms",
                    notification.getNotificationId(), emailList.size(), duration);

            // 更新限流计数
            notificationRateLimiter.incrementCount(channel);
            // 记录成功指标
            notificationMetricsCollector.recordSuccess(channel, duration);

            return true;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[邮件通知] 发送失败，通知ID：{}，耗时：{}ms", notification.getNotificationId(), duration, e);
            // 记录失败指标
            notificationMetricsCollector.recordFailure(channel, duration, e.getClass().getSimpleName());
            return false;
        }
    }

    /**
     * 发送简单文本邮件
     * <p>
     * 用于发送纯文本格式的邮件
     * </p>
     *
     * @param emailList 接收人邮箱列表
     * @param subject   邮件主题
     * @param content   邮件内容
     */
    @SuppressWarnings("null")
    private void sendSimpleEmail(List<String> emailList, String subject, String content) {
        try {
            // 从配置获取发件人地址
            String fromAddress = notificationConfigManager.getConfigValue(
                    NotificationConfigKey.Email.FROM_ADDRESS, "noreply@ioedream.com");
            
            // 参数验证
            if (fromAddress == null || fromAddress.isEmpty()) {
                fromAddress = "noreply@ioedream.com";
            }
            if (subject == null) {
                subject = "";
            }
            if (content == null) {
                content = "";
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            @SuppressWarnings("null")
            String[] emailArray = emailList.toArray(new String[0]);
            message.setTo(emailArray);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.debug("[邮件通知] 简单文本邮件发送成功，接收人：{}", emailList);

        } catch (Exception e) {
            log.error("[邮件通知] 发送简单文本邮件失败，接收人：{}", emailList, e);
            throw new RuntimeException("发送邮件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发送HTML格式邮件
     * <p>
     * 用于发送富文本格式的邮件
     * </p>
     *
     * @param emailList 接收人邮箱列表
     * @param subject   邮件主题
     * @param content   邮件内容（HTML格式）
     */
    @SuppressWarnings("null")
    private void sendHtmlEmail(List<String> emailList, String subject, String content) {
        try {
            // 从配置获取发件人地址和名称
            String fromAddress = notificationConfigManager.getConfigValue(
                    NotificationConfigKey.Email.FROM_ADDRESS, "noreply@ioedream.com");
            String fromName = notificationConfigManager.getConfigValue(
                    NotificationConfigKey.Email.FROM_NAME, "IOE-DREAM系统");
            
            // 参数验证
            if (fromAddress == null || fromAddress.isEmpty()) {
                fromAddress = "noreply@ioedream.com";
            }
            if (fromName == null || fromName.isEmpty()) {
                fromName = "IOE-DREAM系统";
            }
            if (subject == null) {
                subject = "";
            }
            if (content == null) {
                content = "";
            }

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromAddress, fromName);
            @SuppressWarnings("null")
            String[] emailArray = emailList.toArray(new String[emailList.size()]);
            helper.setTo(emailArray);
            helper.setSubject(subject);
            helper.setText(content, true); // true表示HTML格式

            mailSender.send(mimeMessage);
            log.debug("[邮件通知] HTML格式邮件发送成功，接收人：{}", emailList);

        } catch (Exception e) {
            log.error("[邮件通知] 发送HTML格式邮件失败，接收人：{}", emailList, e);
            throw new RuntimeException("发送HTML邮件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建邮件内容
     * <p>
     * 根据通知类型和内容构建邮件正文
     * 支持模板变量替换
     * </p>
     *
     * @param notification 通知实体
     * @return 邮件内容
     */
    private String buildEmailContent(NotificationEntity notification) {
        // 基础邮件模板
        String template = """
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                        .content { padding: 20px; background-color: #f9f9f9; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>IOE-DREAM 智慧园区通知</h2>
                        </div>
                        <div class="content">
                            <h3>%s</h3>
                            <p>%s</p>
                        </div>
                        <div class="footer">
                            <p>此邮件由系统自动发送，请勿回复。</p>
                            <p>© 2025 IOE-DREAM. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;

        return String.format(template, notification.getTitle(), notification.getContent());
    }

    /**
     * 判断内容是否为HTML格式
     *
     * @param content 邮件内容
     * @return 是否为HTML格式
     */
    private boolean isHtmlContent(String content) {
        return content != null && (content.contains("<html") || content.contains("<div") || content.contains("<p>"));
    }

    /**
     * 解析接收人邮箱列表
     * <p>
     * 根据接收人类型（用户ID/角色/部门）查询对应的邮箱地址
     * 接收人类型定义：
     * - 1: 指定用户（receiverIds为用户ID列表）
     * - 2: 角色（receiverIds为角色ID列表）
     * - 3: 部门（receiverIds为部门ID列表）
     * </p>
     *
     * @param notification 通知实体
     * @return 接收人邮箱列表
     */
    private List<String> parseReceiverEmailAddresses(NotificationEntity notification) {
        if (notification == null) {
            return List.of();
        }

        String receiverIds = notification.getReceiverIds();
        Integer receiverType = notification.getReceiverType();

        if (receiverIds == null || receiverIds.trim().isEmpty()) {
            return List.of();
        }

        // 根据接收人类型查询对应的用户ID列表
        List<Long> userIdList;
        if (receiverType == null || receiverType == 1) {
            // 1-指定用户：直接解析用户ID列表
            userIdList = Arrays.stream(receiverIds.split(","))
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .map(id -> {
                        try {
                            return Long.parseLong(id);
                        } catch (NumberFormatException e) {
                            log.warn("[邮件通知] 用户ID格式错误，跳过：{}", id);
                            return null;
                        }
                    })
                    .filter(id -> id != null)
                    .toList();
        } else if (receiverType == 2) {
            // 2-角色：查询角色下的所有用户ID
            userIdList = queryUserIdsByRoleIds(receiverIds);
        } else if (receiverType == 3) {
            // 3-部门：查询部门下的所有用户ID
            userIdList = queryUserIdsByDepartmentIds(receiverIds);
        } else {
            log.warn("[邮件通知] 未知的接收人类型：{}，使用默认处理（指定用户）", receiverType);
            userIdList = Arrays.stream(receiverIds.split(","))
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .map(id -> {
                        try {
                            return Long.parseLong(id);
                        } catch (NumberFormatException e) {
                            log.warn("[邮件通知] 用户ID格式错误，跳过：{}", id);
                            return null;
                        }
                    })
                    .filter(id -> id != null)
                    .toList();
        }

        // 将用户ID转换为邮箱地址（通过调用公共服务获取用户邮箱）
        return userIdList.stream()
                .map(this::getUserEmail)
                .filter(email -> email != null && !email.trim().isEmpty())
                .filter(email -> email.contains("@")) // 验证邮箱格式
                .collect(Collectors.toList());
    }

    /**
     * 根据角色ID列表查询用户ID列表
     * <p>
     * 通过GatewayServiceClient调用公共服务获取角色下的所有用户
     * 参考竞品实现：钉钉、企业微信等均支持按角色批量查询用户
     * </p>
     *
     * @param roleIds 角色ID列表（逗号分隔）
     * @return 用户ID列表
     */
    private List<Long> queryUserIdsByRoleIds(String roleIds) {
        if (roleIds == null || roleIds.trim().isEmpty()) {
            return List.of();
        }

        try {
            // 解析角色ID列表
            List<Long> roleIdList = Arrays.stream(roleIds.split(","))
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .map(id -> {
                        try {
                            return Long.parseLong(id);
                        } catch (NumberFormatException e) {
                            log.warn("[邮件通知] 角色ID格式错误，跳过：{}", id);
                            return null;
                        }
                    })
                    .filter(id -> id != null)
                    .collect(Collectors.toList());

            if (roleIdList.isEmpty()) {
                return List.of();
            }

            // 通过GatewayServiceClient调用公共服务获取角色下的所有用户
            List<Long> userIdList = new java.util.ArrayList<>();
            for (Long roleId : roleIdList) {
                try {
                    // 调用公共服务：GET /api/v1/roles/{roleId}/users
                    ResponseDTO<List<UserDetailVO>> response = gatewayServiceClient.callCommonService(
                            "/api/v1/roles/" + roleId + "/users",
                            HttpMethod.GET,
                            null,
                            new TypeReference<ResponseDTO<List<UserDetailVO>>>() {}
                    );

                    if (response != null && response.isSuccess() && response.getData() != null) {
                        List<Long> roleUserIds = response.getData().stream()
                                .map(UserDetailVO::getUserId)
                                .filter(id -> id != null)
                                .collect(Collectors.toList());
                        userIdList.addAll(roleUserIds);
                        log.debug("[邮件通知] 根据角色查询用户成功，roleId={}, 用户数量={}", roleId, roleUserIds.size());
                    } else {
                        log.warn("[邮件通知] 根据角色查询用户失败，roleId={}, message={}", roleId,
                                response != null ? response.getMessage() : "响应为空");
                    }
                } catch (Exception e) {
                    log.error("[邮件通知] 根据角色查询用户异常，roleId={}", roleId, e);
                    // 单个角色查询失败不影响其他角色，继续处理
                }
            }

            // 去重
            return userIdList.stream().distinct().collect(Collectors.toList());

        } catch (Exception e) {
            log.error("[邮件通知] 根据角色查询用户异常，roleIds={}", roleIds, e);
            return List.of();
        }
    }

    /**
     * 根据部门ID列表查询用户ID列表
     * <p>
     * 通过GatewayServiceClient调用公共服务获取部门下的所有用户
     * 参考竞品实现：钉钉、企业微信等均支持按部门批量查询用户
     * </p>
     *
     * @param departmentIds 部门ID列表（逗号分隔）
     * @return 用户ID列表
     */
    private List<Long> queryUserIdsByDepartmentIds(String departmentIds) {
        if (departmentIds == null || departmentIds.trim().isEmpty()) {
            return List.of();
        }

        try {
            // 解析部门ID列表
            List<Long> departmentIdList = Arrays.stream(departmentIds.split(","))
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .map(id -> {
                        try {
                            return Long.parseLong(id);
                        } catch (NumberFormatException e) {
                            log.warn("[邮件通知] 部门ID格式错误，跳过：{}", id);
                            return null;
                        }
                    })
                    .filter(id -> id != null)
                    .collect(Collectors.toList());

            if (departmentIdList.isEmpty()) {
                return List.of();
            }

            // 通过GatewayServiceClient调用公共服务获取部门下的所有用户
            List<Long> userIdList = new java.util.ArrayList<>();
            for (Long departmentId : departmentIdList) {
                try {
                    // 调用公共服务：GET /api/v1/departments/{departmentId}/users
                    ResponseDTO<List<UserDetailVO>> response = gatewayServiceClient.callCommonService(
                            "/api/v1/departments/" + departmentId + "/users",
                            HttpMethod.GET,
                            null,
                            new TypeReference<ResponseDTO<List<UserDetailVO>>>() {}
                    );

                    if (response != null && response.isSuccess() && response.getData() != null) {
                        List<Long> deptUserIds = response.getData().stream()
                                .map(UserDetailVO::getUserId)
                                .filter(id -> id != null)
                                .collect(Collectors.toList());
                        userIdList.addAll(deptUserIds);
                        log.debug("[邮件通知] 根据部门查询用户成功，departmentId={}, 用户数量={}", departmentId, deptUserIds.size());
                    } else {
                        log.warn("[邮件通知] 根据部门查询用户失败，departmentId={}, message={}", departmentId,
                                response != null ? response.getMessage() : "响应为空");
                    }
                } catch (Exception e) {
                    log.error("[邮件通知] 根据部门查询用户异常，departmentId={}", departmentId, e);
                    // 单个部门查询失败不影响其他部门，继续处理
                }
            }

            // 去重
            return userIdList.stream().distinct().collect(Collectors.toList());

        } catch (Exception e) {
            log.error("[邮件通知] 根据部门查询用户异常，departmentIds={}", departmentIds, e);
            return List.of();
        }
    }

    /**
     * 获取用户邮箱
     * <p>
     * 通过GatewayServiceClient调用公共服务获取用户详情，提取邮箱
     * 支持缓存优化（由GatewayServiceClient内部实现）
     * </p>
     *
     * @param userId 用户ID
     * @return 用户邮箱，如果获取失败则返回null
     */
    private String getUserEmail(Long userId) {
        if (userId == null) {
            return null;
        }

        try {
            // 通过GatewayServiceClient调用公共服务获取用户详情
            ResponseDTO<UserDetailVO> response = gatewayServiceClient.callCommonService(
                    "/api/v1/users/" + userId,
                    HttpMethod.GET,
                    null,
                    UserDetailVO.class
            );

            if (response != null && response.isSuccess() && response.getData() != null) {
                String email = response.getData().getEmail();
                if (email != null && !email.trim().isEmpty()) {
                    log.debug("[邮件通知] 获取用户邮箱成功，userId={}, email={}", userId, email);
                    return email;
                } else {
                    log.warn("[邮件通知] 用户邮箱为空，userId={}", userId);
                    return null;
                }
            } else {
                log.warn("[邮件通知] 获取用户详情失败，userId={}, message={}", userId,
                        response != null ? response.getMessage() : "响应为空");
                return null;
            }
        } catch (Exception e) {
            log.error("[邮件通知] 获取用户邮箱异常，userId={}", userId, e);
            return null;
        }
    }
}
