package net.lab1024.sa.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 验证码配置类
 * <p>
 * 遵循企业级配置管理规范，避免硬编码
 * 所有验证码相关配置统一管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "captcha")
public class CaptchaConfig {

    /**
     * 验证码长度（默认4位）
     */
    private int length = 4;

    /**
     * 验证码过期时间（秒，默认5分钟）
     */
    private int expireSeconds = 300;

    /**
     * 验证码字符集（排除易混淆字符：0/O, 1/I/l）
     */
    private String chars = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";

    /**
     * 图片宽度（像素）
     */
    private int imageWidth = 100;

    /**
     * 图片高度（像素）
     */
    private int imageHeight = 40;

    /**
     * 字体大小
     */
    private int fontSize = 28;

    /**
     * 字体名称
     */
    private String fontName = "Arial";

    /**
     * 背景颜色RGB
     */
    private int[] backgroundColor = {240, 240, 240};

    /**
     * 文字颜色RGB范围（最大值）
     */
    private int textColorMax = 150;

    /**
     * 干扰线数量
     */
    private int interferenceLineCount = 5;

    /**
     * 干扰线颜色RGB范围（最大值）
     */
    private int interferenceLineColorMax = 200;

    /**
     * 字符旋转角度范围（±度数）
     */
    private int rotationDegree = 14;

    /**
     * Redis键前缀
     */
    private String redisKeyPrefix = "captcha:";

    /**
     * 是否启用验证码验证
     */
    private boolean enabled = true;

    /**
     * 是否区分大小写
     */
    private boolean caseSensitive = false;
}