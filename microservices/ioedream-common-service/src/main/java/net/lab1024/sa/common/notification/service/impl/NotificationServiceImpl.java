package net.lab1024.sa.common.notification.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.notification.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 通知服务实现类
 * <p>
 * 提供邮件、短信、钉钉等多种通知方式
 * 当前版本：仅记录日志，暂不集成实际的通知服务
 * 后续可根据需要集成邮件服务、短信服务、钉钉机器人等
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void sendUserLockedNotification(String username, int failureCount, int lockMinutes) {
        log.info("[通知服务] 发送用户锁定通知: username={}, count={}, duration={}分钟",
                 username, failureCount, lockMinutes);

        // TODO: 集成实际的通知服务（邮件、短信、钉钉等）
        // 当前版本仅记录日志
        log.warn("[通知服务] 用户账号已锁定 - 用户名: {}, 失败次数: {}, 锁定时长: {}分钟",
                username, failureCount, lockMinutes);

        // 示例：邮件通知内容（待实现）
        // String emailContent = buildLockEmailContent(username, failureCount, lockMinutes);
        // emailService.send(user.getEmail(), "账号安全提醒", emailContent);

        // 示例：短信通知内容（待实现）
        // String smsContent = buildLockSmsContent(username, failureCount, lockMinutes);
        // smsService.send(user.getPhone(), smsContent);

        // 示例：钉钉通知（待实现）
        // dingTalkService.sendAlert("账号安全告警", username + " 因连续登录失败被锁定");
    }

    @Override
    public void sendUserUnlockedNotification(String username) {
        log.info("[通知服务] 发送用户解锁通知: username={}", username);

        // TODO: 集成实际的通知服务
        log.warn("[通知服务] 用户账号已解锁 - 用户名: {}", username);
    }

    @Override
    public void sendLoginSuccessNotification(String username, String loginTime, String loginIp) {
        log.debug("[通知服务] 发送登录成功通知: username={}, time={}, ip={}",
                  username, loginTime, loginIp);

        // TODO: 可选：记录登录成功通知（如需要）
        // 注意：登录成功通知可能过于频繁，建议根据实际情况启用
    }

    @Override
    public void sendLoginFailureNotification(String username, int failureCount, int maxFailures) {
        log.warn("[通知服务] 发送登录失败通知: username={}, count={}/{}",
                 username, failureCount, maxFailures);

        // TODO: 集成实际的通知服务
        // 可在失败次数接近阈值时提前通知用户
        if (failureCount >= maxFailures - 2) {
            log.warn("[通知服务] 警告：用户即将被锁定 - 用户名: {}, 失败次数: {}/{}",
                    username, failureCount, maxFailures);
        }
    }

    /**
     * 构建锁定通知邮件内容（待实现）
     */
    private String buildLockEmailContent(String username, int failureCount, int lockMinutes) {
        String lockTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return String.format(
            "尊敬的%s用户：\n\n" +
            "您的账号于%s因连续登录失败%d次而被临时锁定。\n\n" +
            "锁定时长：%d分钟\n\n" +
            "如有疑问请联系管理员。\n\n" +
            "此邮件由系统自动发送，请勿直接回复。",
            username, lockTime, failureCount, lockMinutes
        );
    }

    /**
     * 构建锁定通知短信内容（待实现）
     */
    private String buildLockSmsContent(String username, int failureCount, int lockMinutes) {
        return String.format(
            "【账号安全提醒】尊敬的%s，您的账号因连续登录失败%d次已被临时锁定，锁定时长%d分钟。如有疑问请联系管理员。",
            username, failureCount, lockMinutes
        );
    }
}
