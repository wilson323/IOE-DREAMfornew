package net.lab1024.sa.monitor.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.monitor.domain.entity.NotificationEntity;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * 邮件通知管理器
 *
 * 负责发送邮件通知
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Component
public class EmailNotificationManager {

    @Resource
    private EmailConfigManager emailConfigManager;

    /**
     * 发送邮件
     *
     * @param notification 通知信息
     * @return 发送结果
     */
    public boolean sendEmail(NotificationEntity notification) {
        log.debug("发送邮件通知，收件人：{}，标题：{}", notification.getRecipient(), notification.getNotificationTitle());

        try {
            Properties props = getMailProperties();
            Session session = createMailSession(props);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailConfigManager.getFromAddress()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(notification.getRecipient()));
            message.setSubject(notification.getNotificationTitle());
            message.setText(notification.getNotificationContent());
            message.setSentDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));

            Transport.send(message);

            log.info("邮件发送成功，收件人：{}，标题：{}", notification.getRecipient(), notification.getNotificationTitle());
            return true;

        } catch (MessagingException e) {
            log.error("邮件发送失败，收件人：{}，错误：{}", notification.getRecipient(), e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("邮件发送异常，收件人：{}", notification.getRecipient(), e);
            return false;
        }
    }

    private Properties getMailProperties() {
        Properties props = new Properties();

        props.put("mail.smtp.host", emailConfigManager.getSmtpHost());
        props.put("mail.smtp.port", emailConfigManager.getSmtpPort());
        props.put("mail.smtp.auth", emailConfigManager.isSmtpAuth());
        props.put("mail.smtp.starttls.enable", emailConfigManager.isStarttlsEnable());
        props.put("mail.smtp.ssl.enable", emailConfigManager.isSslEnable());
        props.put("mail.debug", emailConfigManager.isDebug());

        // 连接池配置
        props.put("mail.smtp.connectiontimeout", "30000");
        props.put("mail.smtp.timeout", "60000");
        props.put("mail.smtp.writetimeout", "60000");

        return props;
    }

    private Session createMailSession(Properties props) {
        if (emailConfigManager.isSmtpAuth()) {
            return Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            emailConfigManager.getUsername(),
                            emailConfigManager.getPassword()
                    );
                }
            });
        } else {
            return Session.getInstance(props);
        }
    }

    /**
     * 发送HTML格式邮件
     *
     * @param notification 通知信息
     * @param htmlContent  HTML内容
     * @return 发送结果
     */
    public boolean sendHtmlEmail(NotificationEntity notification, String htmlContent) {
        log.debug("发送HTML邮件通知，收件人：{}，标题：{}", notification.getRecipient(), notification.getNotificationTitle());

        try {
            Properties props = getMailProperties();
            Session session = createMailSession(props);

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailConfigManager.getFromAddress()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(notification.getRecipient()));
            message.setSubject(notification.getNotificationTitle());
            message.setContent(htmlContent, "text/html;charset=UTF-8");
            message.setSentDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));

            Transport.send(message);

            log.info("HTML邮件发送成功，收件人：{}，标题：{}", notification.getRecipient(), notification.getNotificationTitle());
            return true;

        } catch (MessagingException e) {
            log.error("HTML邮件发送失败，收件人：{}，错误：{}", notification.getRecipient(), e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("HTML邮件发送异常，收件人：{}", notification.getRecipient(), e);
            return false;
        }
    }

    /**
     * 测试邮件配置
     *
     * @return 测试结果
     */
    public boolean testEmailConfig() {
        log.debug("测试邮件配置");

        try {
            Properties props = getMailProperties();
            Session session = createMailSession(props);

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailConfigManager.getFromAddress()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailConfigManager.getTestRecipient()));
            message.setSubject("IOE-DREAM监控服务 - 邮件配置测试");
            message.setText("这是一封测试邮件，用于验证邮件配置是否正确。\n\n如果您收到此邮件，说明邮件配置正常。");
            message.setSentDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));

            Transport.send(message);

            log.info("邮件配置测试成功");
            return true;

        } catch (Exception e) {
            log.error("邮件配置测试失败", e);
            return false;
        }
    }
}