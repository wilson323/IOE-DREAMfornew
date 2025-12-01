/*
 * 验证码服务实现
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-20
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.service.impl;

import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import net.lab1024.sa.admin.module.consume.service.AccountService;
import net.lab1024.sa.admin.module.consume.service.SmsService;
import net.lab1024.sa.admin.module.consume.service.VerifyCodeService;
import net.lab1024.sa.base.common.cache.RedisUtil;

/**
 * 验证码服务实现
 * 提供验证码生成、发送、验证等功能
 *
 * @author SmartAdmin Team
 * @date 2025/01/20
 */
@Service
public class VerifyCodeServiceImpl implements VerifyCodeService {
    private static final Logger log = LoggerFactory.getLogger(VerifyCodeServiceImpl.class);

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private SmsService smsService;

    @Resource
    private AccountService accountService;

    /**
     * 验证码过期时间（秒），默认5分钟
     */
    @Value("${consume.verify-code.expire-seconds:300}")
    private Integer codeExpireSeconds;

    /**
     * 验证码长度，默认6位
     */
    @Value("${consume.verify-code.length:6}")
    private Integer codeLength;

    /**
     * 验证码Redis Key前缀
     */
    private static final String VERIFY_CODE_PREFIX = "verify_code:";

    /**
     * 验证码发送频率限制Key前缀
     */
    private static final String CODE_SEND_LIMIT_PREFIX = "verify_code_send_limit:";

    /**
     * 发送频率限制时间（秒），默认60秒内只能发送一次
     */
    private static final int SEND_LIMIT_SECONDS = 60;

    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public boolean generateAndSendCode(@NotNull Long personId, @NotNull String businessType) {
        log.info("开始生成验证码: personId={}, businessType={}", personId, businessType);

        try {
            // 1. 检查发送频率限制
            String limitKey = CODE_SEND_LIMIT_PREFIX + personId + ":" + businessType;
            if (redisUtil.hasKey(limitKey)) {
                long remainingTime = redisUtil.getExpire(limitKey);
                log.warn("验证码发送过于频繁: personId={}, 剩余时间={}秒", personId, remainingTime);
                return false;
            }

            // 2. 生成验证码
            String verifyCode = generateCode();
            log.info("生成验证码成功: personId={}, code={}", personId, verifyCode);

            // 3. 存储验证码到Redis
            String codeKey = VERIFY_CODE_PREFIX + personId + ":" + businessType;
            redisUtil.set(codeKey, verifyCode, codeExpireSeconds);
            log.info("验证码已存储到Redis: key={}, expire={}秒", codeKey, codeExpireSeconds);

            // 4. 设置发送频率限制
            redisUtil.set(limitKey, "1", SEND_LIMIT_SECONDS);

            // 5. 获取人员手机号并发送短信
            String phoneNumber = getPersonPhoneNumber(personId);
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                // 发送验证码短信
                smsService.sendVerificationCode(phoneNumber, verifyCode, codeExpireSeconds / 60);
                log.info("验证码短信发送成功: personId={}, phone={}", personId, phoneNumber);
            } else {
                log.warn("无法获取人员手机号，跳过短信发送: personId={}", personId);
            }

            return true;

        } catch (Exception e) {
            log.error("生成并发送验证码失败: personId={}, businessType={}", personId, businessType, e);
            return false;
        }
    }

    @Override
    public boolean verifyCode(@NotNull Long personId, @NotNull String verifyCode, @NotNull String businessType) {
        log.info("开始验证验证码: personId={}, businessType={}", personId, businessType);

        try {
            // 1. 获取存储的验证码
            String codeKey = VERIFY_CODE_PREFIX + personId + ":" + businessType;
            String storedCode = redisUtil.getBean(codeKey, String.class);

            if (storedCode == null) {
                log.warn("验证码不存在或已过期: personId={}, businessType={}", personId, businessType);
                return false;
            }

            // 2. 验证码比较（不区分大小写）
            boolean isValid = storedCode.equalsIgnoreCase(verifyCode);

            if (isValid) {
                // 3. 验证成功，删除验证码
                redisUtil.delete(codeKey);
                log.info("验证码验证成功: personId={}, businessType={}", personId, businessType);
            } else {
                log.warn("验证码验证失败: personId={}, businessType={}, inputCode={}", personId, businessType, verifyCode);
            }

            return isValid;

        } catch (Exception e) {
            log.error("验证验证码异常: personId={}, businessType={}", personId, businessType, e);
            return false;
        }
    }

    @Override
    public boolean hasCode(@NotNull Long personId, @NotNull String businessType) {
        try {
            String codeKey = VERIFY_CODE_PREFIX + personId + ":" + businessType;
            return redisUtil.hasKey(codeKey);
        } catch (Exception e) {
            log.error("检查验证码是否存在异常: personId={}, businessType={}", personId, businessType, e);
            return false;
        }
    }

    @Override
    public void deleteCode(@NotNull Long personId, @NotNull String businessType) {
        try {
            String codeKey = VERIFY_CODE_PREFIX + personId + ":" + businessType;
            redisUtil.delete(codeKey);
            log.info("验证码已删除: personId={}, businessType={}", personId, businessType);
        } catch (Exception e) {
            log.error("删除验证码异常: personId={}, businessType={}", personId, businessType, e);
        }
    }

    /**
     * 生成随机验证码
     *
     * @return 验证码字符串
     */
    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(secureRandom.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 获取人员手机号
     *
     * @param personId 人员ID
     * @return 手机号，如果获取失败返回null
     */
    private String getPersonPhoneNumber(Long personId) {
        try {
            // 通过AccountService获取账户信息，然后获取人员信息
            // 这里需要根据实际的数据模型来获取手机号
            // 暂时返回null，后续可以根据实际需求完善
            log.debug("获取人员手机号: personId={}", personId);
            // TODO: 根据实际数据模型实现获取手机号的逻辑
            return null;
        } catch (Exception e) {
            log.error("获取人员手机号失败: personId={}", personId, e);
            return null;
        }
    }
}
