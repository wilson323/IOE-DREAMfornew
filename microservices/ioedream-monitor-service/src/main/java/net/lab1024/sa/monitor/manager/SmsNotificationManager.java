package net.lab1024.sa.monitor.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.monitor.domain.entity.NotificationEntity;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 短信通知管理器
 *
 * 负责发送短信通知
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Component
public class SmsNotificationManager {

    @Resource
    private SmsConfigManager smsConfigManager;

    /**
     * 发送短信
     *
     * @param notification 通知信息
     * @return 发送结果
     */
    public boolean sendSms(NotificationEntity notification) {
        log.debug("发送短信通知，收件人：{}，内容：{}", notification.getRecipient(), notification.getNotificationContent());

        try {
            // 根据配置选择短信服务商
            String provider = smsConfigManager.getProvider();

            boolean result;
            switch (provider.toLowerCase()) {
                case "aliyun":
                    result = sendAliyunSms(notification);
                    break;
                case "tencent":
                    result = sendTencentSms(notification);
                    break;
                case "huawei":
                    result = sendHuaweiSms(notification);
                    break;
                default:
                    log.warn("不支持的短信服务商：{}", provider);
                    result = false;
                    break;
            }

            if (result) {
                log.info("短信发送成功，收件人：{}，内容长度：{}", notification.getRecipient(), notification.getNotificationContent().length());
            }

            return result;

        } catch (Exception e) {
            log.error("短信发送失败，收件人：{}，错误：{}", notification.getRecipient(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送阿里云短信
     */
    private boolean sendAliyunSms(NotificationEntity notification) {
        log.debug("使用阿里云短信服务发送短信");

        try {
            // 这里应该调用阿里云短信SDK
            // 简化实现，模拟发送过程

            String phone = notification.getRecipient();
            String content = notification.getNotificationContent();
            String templateCode = smsConfigManager.getAliyunTemplateCode();
            String signName = smsConfigManager.getAliyunSignName();

            // 模拟API调用
            boolean success = simulateAliyunSmsApi(phone, content, templateCode, signName);

            if (success) {
                log.debug("阿里云短信发送成功，手机号：{}", maskPhone(phone));
            }

            return success;

        } catch (Exception e) {
            log.error("阿里云短信发送失败", e);
            return false;
        }
    }

    /**
     * 发送腾讯云短信
     */
    private boolean sendTencentSms(NotificationEntity notification) {
        log.debug("使用腾讯云短信服务发送短信");

        try {
            String phone = notification.getRecipient();
            String content = notification.getNotificationContent();
            String templateId = smsConfigManager.getTencentTemplateId();
            String signName = smsConfigManager.getTencentSignName();

            // 模拟API调用
            boolean success = simulateTencentSmsApi(phone, content, templateId, signName);

            if (success) {
                log.debug("腾讯云短信发送成功，手机号：{}", maskPhone(phone));
            }

            return success;

        } catch (Exception e) {
            log.error("腾讯云短信发送失败", e);
            return false;
        }
    }

    /**
     * 发送华为云短信
     */
    private boolean sendHuaweiSms(NotificationEntity notification) {
        log.debug("使用华为云短信服务发送短信");

        try {
            String phone = notification.getRecipient();
            String content = notification.getNotificationContent();
            String templateId = smsConfigManager.getHuaweiTemplateId();
            String signName = smsConfigManager.getHuaweiSignName();

            // 模拟API调用
            boolean success = simulateHuaweiSmsApi(phone, content, templateId, signName);

            if (success) {
                log.debug("华为云短信发送成功，手机号：{}", maskPhone(phone));
            }

            return success;

        } catch (Exception e) {
            log.error("华为云短信发送失败", e);
            return false;
        }
    }

    /**
     * 测试短信配置
     *
     * @param phone 测试手机号
     * @return 测试结果
     */
    public boolean testSmsConfig(String phone) {
        log.debug("测试短信配置，手机号：{}", maskPhone(phone));

        try {
            NotificationEntity testNotification = new NotificationEntity();
            testNotification.setRecipient(phone);
            testNotification.setNotificationTitle("IOE-DREAM监控服务测试");
            testNotification.setNotificationContent("这是一条测试短信，用于验证短信配置是否正确。");
            testNotification.setSendTime(LocalDateTime.now());

            return sendSms(testNotification);

        } catch (Exception e) {
            log.error("短信配置测试失败", e);
            return false;
        }
    }

    // 模拟方法，实际项目中需要调用真实的短信API
    private boolean simulateAliyunSmsApi(String phone, String content, String templateCode, String signName) {
        // 模拟API调用，95%成功率
        return Math.random() > 0.05;
    }

    private boolean simulateTencentSmsApi(String phone, String content, String templateId, String signName) {
        // 模拟API调用，95%成功率
        return Math.random() > 0.05;
    }

    private boolean simulateHuaweiSmsApi(String phone, String content, String templateId, String signName) {
        // 模拟API调用，95%成功率
        return Math.random() > 0.05;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}