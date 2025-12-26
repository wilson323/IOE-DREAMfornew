package net.lab1024.sa.common.notification.service;

/**
 * 通知服务接口
 * <p>
 * 提供邮件、短信、钉钉等多种通知方式
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface NotificationService {

    /**
     * 发送用户锁定通知
     *
     * @param username 用户名
     * @param failureCount 失败次数
     * @param lockMinutes 锁定时长（分钟）
     */
    void sendUserLockedNotification(String username, int failureCount, int lockMinutes);

    /**
     * 发送用户解锁通知
     *
     * @param username 用户名
     */
    void sendUserUnlockedNotification(String username);

    /**
     * 发送登录成功通知
     *
     * @param username 用户名
     * @param loginTime 登录时间
     * @param loginIp 登录IP
     */
    void sendLoginSuccessNotification(String username, String loginTime, String loginIp);

    /**
     * 发送登录失败通知
     *
     * @param username 用户名
     * @param failureCount 失败次数
     * @param maxFailures 最大失败次数
     */
    void sendLoginFailureNotification(String username, int failureCount, int maxFailures);
}
