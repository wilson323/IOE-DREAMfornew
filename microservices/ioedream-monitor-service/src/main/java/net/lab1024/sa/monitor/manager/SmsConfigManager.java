package net.lab1024.sa.monitor.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 短信配置管理器
 *
 * 负责短信相关配置管理
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@Component
public class SmsConfigManager {

    @Value("${monitor.notification.sms.provider:aliyun}")
    private String provider;

    // 阿里云短信配置
    @Value("${monitor.notification.sms.aliyun.access-key:}")
    private String aliyunAccessKey;

    @Value("${monitor.notification.sms.aliyun.secret-key:}")
    private String aliyunSecretKey;

    @Value("${monitor.notification.sms.aliyun.sign-name:IOE-DREAM}")
    private String aliyunSignName;

    @Value("${monitor.notification.sms.aliyun.template-code:SMS_123456789}")
    private String aliyunTemplateCode;

    // 腾讯云短信配置
    @Value("${monitor.notification.sms.tencent.secret-id:}")
    private String tencentSecretId;

    @Value("${monitor.notification.sms.tencent.secret-key:}")
    private String tencentSecretKey;

    @Value("${monitor.notification.sms.tencent.sign-name:IOE-DREAM}")
    private String tencentSignName;

    @Value("${monitor.notification.sms.tencent.template-id:123456}")
    private String tencentTemplateId;

    // 华为云短信配置
    @Value("${monitor.notification.sms.huawei.app-key:}")
    private String huaweiAppKey;

    @Value("${monitor.notification.sms.huawei.app-secret:}")
    private String huaweiAppSecret;

    @Value("${monitor.notification.sms.huawei.sign-name:IOE-DREAM}")
    private String huaweiSignName;

    @Value("${monitor.notification.sms.huawei.template-id:ffffffff553544434d5a41534c465342}")
    private String huaweiTemplateId;

    @Value("${monitor.notification.sms.huawei.sender:1234567890}")
    private String huaweiSender;

    // Getter方法
    public String getProvider() { return provider; }
    public String getAliyunAccessKey() { return aliyunAccessKey; }
    public String getAliyunSecretKey() { return aliyunSecretKey; }
    public String getAliyunSignName() { return aliyunSignName; }
    public String getAliyunTemplateCode() { return aliyunTemplateCode; }
    public String getTencentSecretId() { return tencentSecretId; }
    public String getTencentSecretKey() { return tencentSecretKey; }
    public String getTencentSignName() { return tencentSignName; }
    public String getTencentTemplateId() { return tencentTemplateId; }
    public String getHuaweiAppKey() { return huaweiAppKey; }
    public String getHuaweiAppSecret() { return huaweiAppSecret; }
    public String getHuaweiSignName() { return huaweiSignName; }
    public String getHuaweiTemplateId() { return huaweiTemplateId; }
    public String getHuaweiSender() { return huaweiSender; }
}