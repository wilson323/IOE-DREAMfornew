package net.lab1024.sa.gateway.service;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.gateway.config.CaptchaConfig;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

/**
 * 验证码服务
 * <p>
 * 企业级验证码生成和验证服务
 * - 支持配置化管理
 * - 支持Redis存储
 * - 支持多级缓存
 * - 支持验证码验证
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
// 架构问题：网关服务是响应式WebFlux，@Transactional在响应式环境中不工作
// 验证码服务应移至common-service
// @Service
// @Transactional(rollbackFor = Exception.class)
@Slf4j
public class CaptchaService {


    @Resource
    private CaptchaConfig captchaConfig;

    // 临时注释Redis依赖，验证码服务将迁移到common-service
    // @Resource
    // private RedisTemplate<String, String> redisTemplate;

    private final ThreadLocal<Random> randomThreadLocal = ThreadLocal.withInitial(Random::new);

    /**
     * 生成验证码
     *
     * @return 验证码信息（uuid, base64图片, 过期时间）
     */
    public CaptchaResult generateCaptcha() {
        try {
            // 生成随机验证码文本
            String captchaText = generateRandomCode();
            String captchaUuid = UUID.randomUUID().toString();

            // 生成验证码图片
            BufferedImage image = generateCaptchaImage(captchaText);

            // 转换为Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);

            // 存储到Redis
            saveCaptchaToRedis(captchaUuid, captchaText);

            log.info("验证码生成成功，uuid: {}, code: {}", captchaUuid, captchaText);

            return CaptchaResult.builder()
                    .captchaUuid(captchaUuid)
                    .captchaBase64Image(base64Image)
                    .expireSeconds(captchaConfig.getExpireSeconds())
                    .build();

        } catch (Exception e) {
            log.error("[验证码服务] 生成验证码失败", e);
            throw new SystemException("CAPTCHA_GENERATE_ERROR", "生成验证码失败", e);
        }
    }

    /**
     * 验证验证码
     *
     * @param captchaUuid 验证码UUID
     * @param captchaCode 用户输入的验证码
     * @return 是否验证通过
     */
    public boolean verifyCaptcha(String captchaUuid, String captchaCode) {
        if (!captchaConfig.isEnabled()) {
            log.warn("验证码验证已禁用，直接通过");
            return true;
        }

        if (captchaUuid == null || captchaCode == null) {
            log.warn("验证码UUID或验证码为空");
            return false;
        }

        try {
            // 从Redis获取验证码
            String storedCode = getCaptchaFromRedis(captchaUuid);
            if (storedCode == null) {
                log.warn("验证码已过期或不存在，uuid: {}", captchaUuid);
                return false;
            }

            // 验证码比较（支持配置是否区分大小写）
            boolean isValid = captchaConfig.isCaseSensitive()
                    ? captchaCode.equals(storedCode)
                    : captchaCode.equalsIgnoreCase(storedCode);

            if (isValid) {
                // 验证成功后删除验证码（防止重复使用）
                deleteCaptchaFromRedis(captchaUuid);
                log.info("验证码验证成功，uuid: {}", captchaUuid);
            } else {
                log.warn("验证码验证失败，uuid: {}, 输入: {}, 正确: {}", captchaUuid, captchaCode, storedCode);
            }

            return isValid;

        } catch (Exception e) {
            log.error("验证码验证异常，uuid: {}", captchaUuid, e);
            return false;
        }
    }

    /**
     * 生成随机验证码文本
     */
    private String generateRandomCode() {
        Random random = randomThreadLocal.get();
        StringBuilder code = new StringBuilder();
        String chars = captchaConfig.getChars();

        for (int i = 0; i < captchaConfig.getLength(); i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        return code.toString();
    }

    /**
     * 生成验证码图片
     */
    private BufferedImage generateCaptchaImage(String code) {
        int width = captchaConfig.getImageWidth();
        int height = captchaConfig.getImageHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        Random random = randomThreadLocal.get();

        // 设置背景色
        int[] bgColor = captchaConfig.getBackgroundColor();
        g.setColor(new Color(bgColor[0], bgColor[1], bgColor[2]));
        g.fillRect(0, 0, width, height);

        // 设置字体
        g.setFont(new Font(captchaConfig.getFontName(), Font.BOLD, captchaConfig.getFontSize()));

        // 绘制验证码文本
        int charSpacing = width / (code.length() + 1);
        for (int i = 0; i < code.length(); i++) {
            // 随机颜色
            int textColorMax = captchaConfig.getTextColorMax();
            g.setColor(new Color(
                    random.nextInt(textColorMax),
                    random.nextInt(textColorMax),
                    random.nextInt(textColorMax)
            ));

            // 随机旋转角度
            int rotationDegree = captchaConfig.getRotationDegree();
            int degree = random.nextInt(rotationDegree * 2) - rotationDegree;
            double theta = degree * Math.PI / 180;

            // 计算位置
            int x = charSpacing + i * charSpacing;
            int y = height * 2 / 3;

            // 旋转并绘制字符
            g.rotate(theta, x, y);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
            g.rotate(-theta, x, y);
        }

        // 绘制干扰线
        int interferenceLineCount = captchaConfig.getInterferenceLineCount();
        int interferenceLineColorMax = captchaConfig.getInterferenceLineColorMax();
        for (int i = 0; i < interferenceLineCount; i++) {
            g.setColor(new Color(
                    random.nextInt(interferenceLineColorMax),
                    random.nextInt(interferenceLineColorMax),
                    random.nextInt(interferenceLineColorMax)
            ));
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            g.drawLine(x1, y1, x2, y2);
        }

        g.dispose();
        return image;
    }

    /**
     * 保存验证码到Redis
     */
    private void saveCaptchaToRedis(String captchaUuid, String captchaText) {
        if (captchaUuid == null || captchaText == null) {
            log.warn("验证码UUID或文本为空，跳过保存");
            return;
        }
        // 临时注释Redis操作，验证码服务将迁移到common-service
        // String prefix = captchaConfig.getRedisKeyPrefix() != null ? captchaConfig.getRedisKeyPrefix() : "captcha:";
        // String key = prefix + captchaUuid;
        // redisTemplate.opsForValue().set(key, captchaText, captchaConfig.getExpireSeconds(), TimeUnit.SECONDS);
        // log.debug("验证码已存储到Redis，key: {}", key);
        log.debug("验证码生成成功（临时模式，未存储到Redis）: {}", captchaUuid);
    }

    /**
     * 从Redis获取验证码
     */
    private String getCaptchaFromRedis(String captchaUuid) {
        // 临时返回null，验证码服务将迁移到common-service
        // String key = captchaConfig.getRedisKeyPrefix() + captchaUuid;
        // return redisTemplate.opsForValue().get(key);
        return null;
    }

    /**
     * 从Redis删除验证码（临时空实现）
     */
    private void deleteCaptchaFromRedis(String captchaUuid) {
        // 临时空实现，验证码服务将迁移到common-service
        // String key = captchaConfig.getRedisKeyPrefix() + captchaUuid;
        // redisTemplate.delete(key);
        // log.debug("验证码已从Redis删除，key: {}", key);
    }

    /**
     * 验证码结果DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class CaptchaResult {
        /**
         * 验证码UUID
         */
        private String captchaUuid;

        /**
         * 验证码Base64图片
         */
        private String captchaBase64Image;

        /**
         * 过期时间（秒）
         */
        private int expireSeconds;
    }
}

