/*
 * 支付密码验证服务实现
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.dao.PaymentPasswordDao;
import net.lab1024.sa.admin.module.consume.domain.entity.PaymentPasswordEntity;
import net.lab1024.sa.admin.module.consume.domain.vo.PaymentPasswordResult;
import net.lab1024.sa.admin.module.consume.domain.vo.PasswordSecurityPolicy;
import net.lab1024.sa.admin.module.consume.domain.vo.PasswordVerificationHistory;
import net.lab1024.sa.admin.module.consume.service.AccountService;
import net.lab1024.sa.admin.module.consume.service.PaymentPasswordService;
import net.lab1024.sa.base.common.cache.RedisUtil;
import net.lab1024.sa.base.common.exception.SmartException;
import net.lab1024.sa.base.common.util.SmartIpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 支付密码验证服务实现
 * 提供支付密码验证、管理、安全检测等完整功能
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Service
@Slf4j
public class PaymentPasswordServiceImpl implements PaymentPasswordService {

    @Resource
    private PaymentPasswordDao paymentPasswordDao;

    @Resource
    private AccountService accountService;

    @Value("${consume.payment.max-attempts:5}")
    private Integer maxAttempts;

    @Value("${consume.payment.lock-duration-minutes:30}")
    private Integer lockDurationMinutes;

    @Value("${consume.password.min-length:6}")
    private Integer minPasswordLength;

    @Value("${consume.password.require-complex:true}")
    private Boolean requireComplexPassword;

    private static final String PAYMENT_PASSWORD_PREFIX = "payment_password:";
    private static final String ATTEMPT_COUNT_PREFIX = "payment_attempt:";
    private static final String LOCK_PREFIX = "payment_lock:";
    private static final String VERIFICATION_HISTORY_PREFIX = "payment_history:";

    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentPasswordResult verifyPassword(@NotNull Long personId, @NotNull String password, Long deviceId) {
        log.info("开始验证支付密码: personId={}, deviceId={}", personId, deviceId);

        try {
            // 1. 参数验证
            if (!StringUtils.hasText(password)) {
                return recordVerificationFailure(personId, deviceId, "INVALID_PARAM",
                    "密码不能为空", SmartIpUtil.getClientIp());
            }

            // 2. 检查密码是否被锁定
            PaymentPasswordResult lockResult = checkPasswordLock(personId);
            if (lockResult != null) {
                return lockResult;
            }

            // 3. 获取支付密码信息
            PaymentPasswordEntity passwordEntity = paymentPasswordDao.selectByPersonId(personId);
            if (passwordEntity == null) {
                return recordVerificationFailure(personId, deviceId, "PASSWORD_NOT_SET",
                    "未设置支付密码", SmartIpUtil.getClientIp());
            }

            // 4. 检查密码状态
            if (!"ACTIVE".equals(passwordEntity.getStatus())) {
                return recordVerificationFailure(personId, deviceId, "PASSWORD_DISABLED",
                    "支付密码已被禁用", SmartIpUtil.getClientIp());
            }

            // 5. 检查密码是否过期
            if (passwordEntity.getExpiredTime() != null && passwordEntity.getExpiredTime().isBefore(LocalDateTime.now())) {
                return recordVerificationFailure(personId, deviceId, "PASSWORD_EXPIRED",
                    "支付密码已过期", SmartIpUtil.getClientIp());
            }

            // 6. 验证密码
            String hashedPassword = hashPassword(password, passwordEntity.getSalt());
            if (!hashedPassword.equals(passwordEntity.getPasswordHash())) {
                return handlePasswordVerificationFailure(personId, deviceId, SmartIpUtil.getClientIp());
            }

            // 7. 验证成功，清除失败计数
            clearVerificationAttempts(personId);

            PaymentPasswordResult result = PaymentPasswordResult.success("支付密码验证成功");
            result.setClientIp(SmartIpUtil.getClientIp());
            result.setDeviceInfo(getDeviceInfo(deviceId));
            result.setVerifyMethod("PASSWORD");
            result.setSecurityLevel("HIGH");

            // 记录验证成功
            recordPasswordVerification(personId, deviceId, result, SmartIpUtil.getClientIp());

            log.info("支付密码验证成功: personId={}", personId);
            return result;

        } catch (Exception e) {
            log.error("支付密码验证异常: personId={}, error={}", personId, e.getMessage(), e);
            return PaymentPasswordResult.failure("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentPasswordResult setPassword(@NotNull Long personId, String oldPassword,
                                           @NotNull String newPassword, @NotNull String confirmPassword, Long deviceId) {
        log.info("开始设置支付密码: personId={}, deviceId={}", personId, deviceId);

        try {
            // 1. 参数验证
            if (!StringUtils.hasText(newPassword)) {
                return PaymentPasswordResult.failure("INVALID_PARAM", "新密码不能为空");
            }

            if (!newPassword.equals(confirmPassword)) {
                return PaymentPasswordResult.failure("PASSWORD_MISMATCH", "两次输入的密码不一致");
            }

            // 2. 密码复杂度验证
            PaymentPasswordResult strengthResult = checkPasswordStrength(newPassword);
            if (!strengthResult.isSuccess()) {
                return strengthResult;
            }

            // 3. 检查是否已设置支付密码
            PaymentPasswordEntity existingPassword = paymentPasswordDao.selectByPersonId(personId);

            if (existingPassword != null) {
                // 修改密码场景，需要验证旧密码
                if (!StringUtils.hasText(oldPassword)) {
                    return PaymentPasswordResult.failure("OLD_PASSWORD_REQUIRED", "修改密码需要提供旧密码");
                }

                String hashedOldPassword = hashPassword(oldPassword, existingPassword.getSalt());
                if (!hashedOldPassword.equals(existingPassword.getPasswordHash())) {
                    return PaymentPasswordResult.failure("OLD_PASSWORD_INCORRECT", "旧密码错误");
                }

                // 更新密码
                return updatePassword(existingPassword, newPassword, deviceId);
            } else {
                // 首次设置密码
                return createPassword(personId, newPassword, deviceId);
            }

        } catch (Exception e) {
            log.error("设置支付密码异常: personId={}, error={}", personId, e.getMessage(), e);
            return PaymentPasswordResult.failure("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentPasswordResult changePassword(@NotNull Long personId, @NotNull String oldPassword,
                                              @NotNull String newPassword, @NotNull String confirmPassword, Long deviceId) {
        log.info("开始修改支付密码: personId={}, deviceId={}", personId, deviceId);

        try {
            // 1. 参数验证
            if (!StringUtils.hasText(oldPassword)) {
                return PaymentPasswordResult.failure("INVALID_PARAM", "旧密码不能为空");
            }

            if (!StringUtils.hasText(newPassword)) {
                return PaymentPasswordResult.failure("INVALID_PARAM", "新密码不能为空");
            }

            if (!newPassword.equals(confirmPassword)) {
                return PaymentPasswordResult.failure("PASSWORD_MISMATCH", "两次输入的密码不一致");
            }

            if (oldPassword.equals(newPassword)) {
                return PaymentPasswordResult.failure("SAME_PASSWORD", "新密码不能与旧密码相同");
            }

            // 2. 密码复杂度验证
            PaymentPasswordResult strengthResult = checkPasswordStrength(newPassword);
            if (!strengthResult.isSuccess()) {
                return strengthResult;
            }

            // 3. 获取现有密码
            PaymentPasswordEntity passwordEntity = paymentPasswordDao.selectByPersonId(personId);
            if (passwordEntity == null) {
                return PaymentPasswordResult.failure("PASSWORD_NOT_SET", "未设置支付密码");
            }

            // 4. 验证旧密码
            String hashedOldPassword = hashPassword(oldPassword, passwordEntity.getSalt());
            if (!hashedOldPassword.equals(passwordEntity.getPasswordHash())) {
                return PaymentPasswordResult.failure("OLD_PASSWORD_INCORRECT", "旧密码错误");
            }

            // 5. 更新密码
            return updatePassword(passwordEntity, newPassword, deviceId);

        } catch (Exception e) {
            log.error("修改支付密码异常: personId={}, error={}", personId, e.getMessage(), e);
            return PaymentPasswordResult.failure("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentPasswordResult resetPassword(@NotNull Long personId, @NotNull String resetType,
                                             String verifyCode, @NotNull String newPassword,
                                             @NotNull String confirmPassword, Long deviceId) {
        log.info("开始重置支付密码: personId={}, resetType={}, deviceId={}", personId, resetType, deviceId);

        try {
            // 1. 验证重置类型
            if (!"FORGOT".equals(resetType) && !"MANUAL".equals(resetType)) {
                return PaymentPasswordResult.failure("INVALID_RESET_TYPE", "不支持的重置类型");
            }

            // 2. 参数验证
            if (!StringUtils.hasText(newPassword)) {
                return PaymentPasswordResult.failure("INVALID_PARAM", "新密码不能为空");
            }

            if (!newPassword.equals(confirmPassword)) {
                return PaymentPasswordResult.failure("PASSWORD_MISMATCH", "两次输入的密码不一致");
            }

            // 3. 密码复杂度验证
            PaymentPasswordResult strengthResult = checkPasswordStrength(newPassword);
            if (!strengthResult.isSuccess()) {
                return strengthResult;
            }

            // 4. 验证码验证（如果是忘记密码）
            if ("FORGOT".equals(resetType)) {
                if (!StringUtils.hasText(verifyCode)) {
                    return PaymentPasswordResult.failure("VERIFY_CODE_REQUIRED", "重置密码需要验证码");
                }

                // TODO: 实现验证码验证逻辑
                // if (!verifyCodeService.verifyCode(personId, verifyCode)) {
                //     return PaymentPasswordResult.failure("VERIFY_CODE_INCORRECT", "验证码错误");
                // }
            }

            // 5. 获取现有密码记录
            PaymentPasswordEntity passwordEntity = paymentPasswordDao.selectByPersonId(personId);

            if (passwordEntity == null) {
                // 创建新密码记录
                return createPassword(personId, newPassword, deviceId);
            } else {
                // 重置现有密码
                return updatePassword(passwordEntity, newPassword, deviceId);
            }

        } catch (Exception e) {
            log.error("重置支付密码异常: personId={}, error={}", personId, e.getMessage(), e);
            return PaymentPasswordResult.failure("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    @Override
    public boolean hasPaymentPassword(@NotNull Long personId) {
        try {
            PaymentPasswordEntity passwordEntity = paymentPasswordDao.selectByPersonId(personId);
            return passwordEntity != null && "ACTIVE".equals(passwordEntity.getStatus());
        } catch (Exception e) {
            log.error("检查支付密码设置状态异常: personId={}, error={}", personId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isPasswordExpired(@NotNull Long personId) {
        try {
            PaymentPasswordEntity passwordEntity = paymentPasswordDao.selectByPersonId(personId);
            if (passwordEntity == null || passwordEntity.getExpiredTime() == null) {
                return false;
            }
            return passwordEntity.getExpiredTime().isBefore(LocalDateTime.now());
        } catch (Exception e) {
            log.error("检查密码过期状态异常: personId={}, error={}", personId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePasswordExpiry(@NotNull Long personId, Integer expiredDays) {
        try {
            PaymentPasswordEntity passwordEntity = paymentPasswordDao.selectByPersonId(personId);
            if (passwordEntity == null) {
                return false;
            }

            LocalDateTime newExpiredTime = expiredDays != null ?
                LocalDateTime.now().plusDays(expiredDays) : null;

            passwordEntity.setExpiredTime(newExpiredTime);
            passwordEntity.setUpdateTime(LocalDateTime.now());

            int updateResult = paymentPasswordDao.updateById(passwordEntity);
            return updateResult > 0;
        } catch (Exception e) {
            log.error("更新密码过期时间异常: personId={}, expiredDays={}, error={}",
                personId, expiredDays, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public PasswordSecurityPolicy getPasswordSecurityPolicy() {
        return PasswordSecurityPolicy.builder()
                .minLength(minPasswordLength)
                .requireComplex(requireComplexPassword)
                .maxAttempts(maxAttempts)
                .lockDurationMinutes(lockDurationMinutes)
                .build();
    }

    @Override
    public PaymentPasswordResult checkPasswordStrength(@NotNull String password) {
        log.debug("检查密码强度: passwordLength={}", password.length());

        try {
            // 1. 基本长度检查
            if (password.length() < minPasswordLength) {
                return PaymentPasswordResult.failure("PASSWORD_TOO_SHORT",
                    String.format("密码长度不能少于%d位", minPasswordLength));
            }

            // 2. 复杂度检查（如果启用）
            if (requireComplexPassword) {
                boolean hasLetter = password.matches(".*[a-zA-Z].*");
                boolean hasDigit = password.matches(".*[0-9].*");
                boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

                if (!hasLetter || !hasDigit) {
                    return PaymentPasswordResult.failure("PASSWORD_TOO_SIMPLE",
                        "密码必须包含字母和数字");
                }
            }

            // 3. 常见弱密码检查
            if (isWeakPassword(password)) {
                return PaymentPasswordResult.failure("PASSWORD_TOO_WEAK", "密码过于简单，请使用更复杂的密码");
            }

            return PaymentPasswordResult.success("密码强度检查通过");

        } catch (Exception e) {
            log.error("密码强度检查异常: error={}", e.getMessage(), e);
            return PaymentPasswordResult.failure("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordPasswordVerification(@NotNull Long personId, Long deviceId,
                                         PaymentPasswordResult verifyResult, String clientIp) {
        try {
            // 记录验证历史到Redis
            String historyKey = VERIFICATION_HISTORY_PREFIX + personId;
            String historyEntry = String.format("%s|%s|%s|%s|%s",
                LocalDateTime.now(), deviceId, clientIp,
                verifyResult.isSuccess() ? "SUCCESS" : "FAILED",
                verifyResult.getMessage());

            // 使用List存储历史记录，最多保留100条
            RedisUtil.lPush(historyKey, historyEntry);
            RedisUtil.lTrim(historyKey, 0, 99);
            RedisUtil.expire(historyKey, 7, TimeUnit.DAYS);

            // 更新统计信息
            updatePasswordStatistics(personId, verifyResult.isSuccess());

        } catch (Exception e) {
            log.error("记录密码验证行为异常: personId={}, error={}", personId, e.getMessage(), e);
        }
    }

    @Override
    public boolean detectAbnormalAttempts(@NotNull Long personId, Long deviceId, Integer timeWindowMinutes) {
        try {
            String historyKey = VERIFICATION_HISTORY_PREFIX + personId;
            List<String> history = RedisUtil.lRange(historyKey, 0, -1);

            if (history == null || history.isEmpty()) {
                return false;
            }

            // 分析最近时间窗口内的验证行为
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(timeWindowMinutes != null ? timeWindowMinutes : 30);
            long recentFailures = history.stream()
                .filter(entry -> {
                    String[] parts = entry.split("\\|");
                    if (parts.length < 5) return false;

                    try {
                        LocalDateTime entryTime = LocalDateTime.parse(parts[0]);
                        String status = parts[3];
                        return entryTime.isAfter(cutoffTime) && "FAILED".equals(status);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();

            // 如果在时间窗口内失败次数超过阈值，认为存在异常行为
            boolean hasAbnormalAttempts = recentFailures >= maxAttempts;

            if (hasAbnormalAttempts) {
                log.warn("检测到异常密码尝试行为: personId={}, deviceId={}, recentFailures={}",
                    personId, deviceId, recentFailures);
            }

            return hasAbnormalAttempts;

        } catch (Exception e) {
            log.error("检测异常密码尝试行为异常: personId={}, error={}", personId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentPasswordResult lockPassword(@NotNull Long personId, @NotNull String lockReason, Integer lockMinutes) {
        log.info("开始锁定支付密码: personId={}, lockReason={}, lockMinutes={}", personId, lockReason, lockMinutes);

        try {
            PaymentPasswordEntity passwordEntity = paymentPasswordDao.selectByPersonId(personId);
            if (passwordEntity == null) {
                return PaymentPasswordResult.failure("PASSWORD_NOT_SET", "未设置支付密码");
            }

            // 设置锁定状态
            passwordEntity.setStatus("LOCKED");
            passwordEntity.setLockReason(lockReason);
            passwordEntity.setLockTime(LocalDateTime.now());

            LocalDateTime lockUntil = lockMinutes != null ?
                LocalDateTime.now().plusMinutes(lockMinutes) : LocalDateTime.now().plusMinutes(lockDurationMinutes);
            passwordEntity.setLockUntil(lockUntil);
            passwordEntity.setUpdateTime(LocalDateTime.now());

            int updateResult = paymentPasswordDao.updateById(passwordEntity);
            if (updateResult <= 0) {
                return PaymentPasswordResult.failure("UPDATE_FAILED", "锁定操作失败");
            }

            // 设置Redis锁定标记
            String lockKey = LOCK_PREFIX + personId;
            RedisUtil.set(lockKey, lockUntil.toString(), lockMinutes != null ? lockMinutes : lockDurationMinutes, TimeUnit.MINUTES);

            PaymentPasswordResult result = PaymentPasswordResult.locked(lockUntil, 0);
            result.setMessage("支付密码已被锁定: " + lockReason);

            log.info("支付密码锁定成功: personId={}, lockUntil={}", personId, lockUntil);
            return result;

        } catch (Exception e) {
            log.error("锁定支付密码异常: personId={}, error={}", personId, e.getMessage(), e);
            return PaymentPasswordResult.failure("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentPasswordResult unlockPassword(@NotNull Long personId, String unlockReason) {
        log.info("开始解锁支付密码: personId={}, unlockReason={}", personId, unlockReason);

        try {
            PaymentPasswordEntity passwordEntity = paymentPasswordDao.selectByPersonId(personId);
            if (passwordEntity == null) {
                return PaymentPasswordResult.failure("PASSWORD_NOT_SET", "未设置支付密码");
            }

            // 解除锁定状态
            passwordEntity.setStatus("ACTIVE");
            passwordEntity.setLockReason(null);
            passwordEntity.setLockTime(null);
            passwordEntity.setLockUntil(null);
            passwordEntity.setUpdateTime(LocalDateTime.now());

            int updateResult = paymentPasswordDao.updateById(passwordEntity);
            if (updateResult <= 0) {
                return PaymentPasswordResult.failure("UPDATE_FAILED", "解锁操作失败");
            }

            // 清除Redis锁定标记和失败计数
            clearVerificationAttempts(personId);
            String lockKey = LOCK_PREFIX + personId;
            RedisUtil.delete(lockKey);

            PaymentPasswordResult result = PaymentPasswordResult.success("支付密码解锁成功");
            result.setMessage(unlockReason);

            log.info("支付密码解锁成功: personId={}", personId);
            return result;

        } catch (Exception e) {
            log.error("解锁支付密码异常: personId={}, error={}", personId, e.getMessage(), e);
            return PaymentPasswordResult.failure("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    @Override
    public boolean isPasswordLocked(@NotNull Long personId) {
        try {
            // 首先检查Redis缓存
            String lockKey = LOCK_PREFIX + personId;
            String lockUntilStr = RedisUtil.get(lockKey);

            if (StringUtils.hasText(lockUntilStr)) {
                try {
                    LocalDateTime lockUntil = LocalDateTime.parse(lockUntilStr);
                    return lockUntil.isAfter(LocalDateTime.now());
                } catch (Exception e) {
                    // Redis数据异常，清除缓存
                    RedisUtil.delete(lockKey);
                }
            }

            // 检查数据库
            PaymentPasswordEntity passwordEntity = paymentPasswordDao.selectByPersonId(personId);
            if (passwordEntity == null || !"LOCKED".equals(passwordEntity.getStatus())) {
                return false;
            }

            LocalDateTime lockUntil = passwordEntity.getLockUntil();
            if (lockUntil == null) {
                return false;
            }

            boolean isLocked = lockUntil.isAfter(LocalDateTime.now());

            // 如果已过锁定时间，更新Redis缓存
            if (!isLocked) {
                RedisUtil.delete(lockKey);
            }

            return isLocked;

        } catch (Exception e) {
            log.error("检查密码锁定状态异常: personId={}, error={}", personId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setPaymentPasswordEnabled(@NotNull Long personId, boolean enabled, String reason) {
        try {
            PaymentPasswordEntity passwordEntity = paymentPasswordDao.selectByPersonId(personId);
            if (passwordEntity == null) {
                return false;
            }

            passwordEntity.setStatus(enabled ? "ACTIVE" : "DISABLED");
            passwordEntity.setUpdateTime(LocalDateTime.now());

            int updateResult = paymentPasswordDao.updateById(passwordEntity);
            return updateResult > 0;

        } catch (Exception e) {
            log.error("设置支付密码启用状态异常: personId={}, enabled={}, error={}", personId, enabled, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public PaymentPasswordResult verifyBiometric(@NotNull Long personId, @NotNull String biometricData,
                                               @NotNull String biometricType, Long deviceId) {
        log.info("开始生物特征验证: personId={}, biometricType={}, deviceId={}", personId, biometricType, deviceId);

        try {
            // TODO: 实现生物特征验证逻辑
            // 这里需要集成生物特征识别服务
            // 1. 获取用户注册的生物特征模板
            // 2. 进行特征匹配验证
            // 3. 返回验证结果

            // 临时实现：模拟生物特征验证
            boolean verificationSuccess = simulateBiometricVerification(personId, biometricData, biometricType);

            if (verificationSuccess) {
                PaymentPasswordResult result = PaymentPasswordResult.biometricResult(true,
                    "生物特征验证成功", biometricType);
                result.setClientIp(SmartIpUtil.getClientIp());
                result.setDeviceInfo(getDeviceInfo(deviceId));
                recordPasswordVerification(personId, deviceId, result, SmartIpUtil.getClientIp());

                log.info("生物特征验证成功: personId={}, biometricType={}", personId, biometricType);
                return result;
            } else {
                PaymentPasswordResult result = PaymentPasswordResult.biometricResult(false,
                    "生物特征验证失败", biometricType);
                recordPasswordVerification(personId, deviceId, result, SmartIpUtil.getClientIp());

                log.warn("生物特征验证失败: personId={}, biometricType={}", personId, biometricType);
                return result;
            }

        } catch (Exception e) {
            log.error("生物特征验证异常: personId={}, biometricType={}, error={}", personId, biometricType, e.getMessage(), e);
            return PaymentPasswordResult.failure("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    @Override
    public List<PasswordVerificationHistory> getVerificationHistory(@NotNull Long personId, Integer limit) {
        try {
            String historyKey = VERIFICATION_HISTORY_PREFIX + personId;
            List<String> historyEntries = RedisUtil.lRange(historyKey, 0,
                limit != null ? limit - 1 : 19);

            return historyEntries.stream()
                .map(this::parseHistoryEntry)
                .filter(entry -> entry != null)
                .toList();

        } catch (Exception e) {
            log.error("获取密码验证历史异常: personId={}, error={}", personId, e.getMessage(), e);
            return List.of();
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 检查密码锁定状态
     */
    private PaymentPasswordResult checkPasswordLock(Long personId) {
        if (isPasswordLocked(personId)) {
            PaymentPasswordEntity passwordEntity = paymentPasswordDao.selectByPersonId(personId);
            LocalDateTime lockUntil = passwordEntity != null ? passwordEntity.getLockUntil() :
                LocalDateTime.now().plusMinutes(lockDurationMinutes);

            PaymentPasswordResult result = PaymentPasswordResult.locked(lockUntil, 0);
            result.setMessage("支付密码已被锁定，请稍后重试");
            return result;
        }
        return null;
    }

    /**
     * 处理密码验证失败
     */
    private PaymentPasswordResult handlePasswordVerificationFailure(Long personId, Long deviceId, String clientIp) {
        String attemptKey = ATTEMPT_COUNT_PREFIX + personId;
        Integer attempts = RedisUtil.get(attemptKey, Integer.class);
        attempts = attempts != null ? attempts + 1 : 1;

        // 设置失败次数，带过期时间
        RedisUtil.set(attemptKey, attempts.toString(), lockDurationMinutes, TimeUnit.MINUTES);

        // 检查是否需要锁定
        if (attempts >= maxAttempts) {
            return lockPassword(personId, String.format("密码验证失败次数过多(%d次)", attempts), lockDurationMinutes);
        }

        PaymentPasswordResult result = PaymentPasswordResult.failure("PASSWORD_INCORRECT", "支付密码错误");
        result.setRemainingAttempts(maxAttempts - attempts);
        result.setClientIp(clientIp);
        result.setSecurityLevel("MEDIUM");

        // 记录验证失败
        recordPasswordVerification(personId, deviceId, result, clientIp);

        log.warn("支付密码验证失败: personId={}, attempts={}, remainingAttempts={}",
            personId, attempts, maxAttempts - attempts);

        return result;
    }

    /**
     * 记录验证失败
     */
    private PaymentPasswordResult recordVerificationFailure(Long personId, Long deviceId,
                                                           String errorCode, String message, String clientIp) {
        PaymentPasswordResult result = PaymentPasswordResult.failure(errorCode, message);
        result.setClientIp(clientIp);
        result.setSecurityLevel("MEDIUM");

        recordPasswordVerification(personId, deviceId, result, clientIp);
        return result;
    }

    /**
     * 清除验证尝试次数
     */
    private void clearVerificationAttempts(Long personId) {
        String attemptKey = ATTEMPT_COUNT_PREFIX + personId;
        RedisUtil.delete(attemptKey);
    }

    /**
     * 创建新密码记录
     */
    private PaymentPasswordResult createPassword(Long personId, String newPassword, Long deviceId) {
        try {
            String salt = generateSalt();
            String hashedPassword = hashPassword(newPassword, salt);

            PaymentPasswordEntity passwordEntity = new PaymentPasswordEntity();
            passwordEntity.setPersonId(personId);
            passwordEntity.setPasswordHash(hashedPassword);
            passwordEntity.setSalt(salt);
            passwordEntity.setStatus("ACTIVE");
            passwordEntity.setCreateTime(LocalDateTime.now());
            passwordEntity.setUpdateTime(LocalDateTime.now());

            int insertResult = paymentPasswordDao.insert(passwordEntity);
            if (insertResult <= 0) {
                return PaymentPasswordResult.failure("INSERT_FAILED", "创建密码记录失败");
            }

            PaymentPasswordResult result = PaymentPasswordResult.success("支付密码设置成功");
            result.setClientIp(SmartIpUtil.getClientIp());
            result.setDeviceInfo(getDeviceInfo(deviceId));
            result.setVerifyMethod("PASSWORD");
            result.setSecurityLevel("HIGH");

            log.info("支付密码创建成功: personId={}", personId);
            return result;

        } catch (Exception e) {
            log.error("创建支付密码异常: personId={}, error={}", personId, e.getMessage(), e);
            return PaymentPasswordResult.failure("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    /**
     * 更新密码记录
     */
    private PaymentPasswordResult updatePassword(PaymentPasswordEntity passwordEntity, String newPassword, Long deviceId) {
        try {
            String newSalt = generateSalt();
            String newHashedPassword = hashPassword(newPassword, newSalt);

            passwordEntity.setPasswordHash(newHashedPassword);
            passwordEntity.setSalt(newSalt);
            passwordEntity.setStatus("ACTIVE");
            passwordEntity.setUpdateTime(LocalDateTime.now());
            passwordEntity.setLastModifyTime(LocalDateTime.now());

            int updateResult = paymentPasswordDao.updateById(passwordEntity);
            if (updateResult <= 0) {
                return PaymentPasswordResult.failure("UPDATE_FAILED", "更新密码失败");
            }

            // 清除相关的失败尝试记录
            clearVerificationAttempts(passwordEntity.getPersonId());

            PaymentPasswordResult result = PaymentPasswordResult.success("支付密码更新成功");
            result.setClientIp(SmartIpUtil.getClientIp());
            result.setDeviceInfo(getDeviceInfo(deviceId));
            result.setVerifyMethod("PASSWORD");
            result.setSecurityLevel("HIGH");

            log.info("支付密码更新成功: personId={}", passwordEntity.getPersonId());
            return result;

        } catch (Exception e) {
            log.error("更新支付密码异常: personId={}, error={}", passwordEntity.getPersonId(), e.getMessage(), e);
            return PaymentPasswordResult.failure("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    /**
     * 生成密码盐值
     */
    private String generateSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 哈希密码
     */
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + salt;
            byte[] hash = digest.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }

    /**
     * 检查是否为弱密码
     */
    private boolean isWeakPassword(String password) {
        String lowerPassword = password.toLowerCase();

        // 常见弱密码列表
        String[] weakPasswords = {
            "123456", "123456789", "password", "12345678", "qwerty",
            "123123", "111111", "12345", "1234567", "sunshine",
            "qwertyuiop", "iloveyou", "princess", "admin", "welcome",
            "666666", "abc123", "football", "1234567890", "123qwe",
            "000000", "1234", "qweasd", "zxcvbnm"
        };

        for (String weakPassword : weakPasswords) {
            if (lowerPassword.equals(weakPassword) || lowerPassword.contains(weakPassword)) {
                return true;
            }
        }

        // 检查是否为连续数字或字母
        if (isSequentialChars(password)) {
            return true;
        }

        // 检查是否为重复字符
        if (isRepeatingChars(password)) {
            return true;
        }

        return false;
    }

    /**
     * 检查是否为连续字符
     */
    private boolean isSequentialChars(String password) {
        if (password.length() < 3) {
            return false;
        }

        String lowerPassword = password.toLowerCase();
        for (int i = 0; i < lowerPassword.length() - 2; i++) {
            char c1 = lowerPassword.charAt(i);
            char c2 = lowerPassword.charAt(i + 1);
            char c3 = lowerPassword.charAt(i + 2);

            if ((c2 == c1 + 1 && c3 == c2 + 1) || (c2 == c1 - 1 && c3 == c2 - 1)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查是否为重复字符
     */
    private boolean isRepeatingChars(String password) {
        if (password.length() < 3) {
            return false;
        }

        for (int i = 0; i < password.length() - 2; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);
            char c3 = password.charAt(i + 2);

            if (c1 == c2 && c2 == c3) {
                return true;
            }
        }

        return false;
    }

    /**
     * 模拟生物特征验证
     */
    private boolean simulateBiometricVerification(Long personId, String biometricData, String biometricType) {
        // 临时实现：模拟生物特征验证
        // 实际项目中需要集成生物特征识别SDK
        log.debug("模拟生物特征验证: personId={}, biometricType={}, dataLength={}",
            personId, biometricType, biometricData.length());

        // 简单的模拟逻辑：数据长度大于10且包含有效字符则认为验证成功
        return biometricData.length() > 10 && biometricData.matches("^[A-Za-z0-9+/=]+$");
    }

    /**
     * 获取设备信息
     */
    private String getDeviceInfo(Long deviceId) {
        if (deviceId == null) {
            return "未知设备";
        }
        // TODO: 根据deviceId查询设备信息
        return "设备-" + deviceId;
    }

    /**
     * 更新密码统计信息
     */
    private void updatePasswordStatistics(Long personId, boolean success) {
        try {
            String statsKey = PAYMENT_PASSWORD_PREFIX + "stats:" + personId;
            String today = LocalDateTime.now().toLocalDate().toString();

            if (success) {
                RedisUtil.hIncrBy(statsKey, "success_count:" + today, 1);
            } else {
                RedisUtil.hIncrBy(statsKey, "failure_count:" + today, 1);
            }

            RedisUtil.expire(statsKey, 30, TimeUnit.DAYS);

        } catch (Exception e) {
            log.error("更新密码统计信息异常: personId={}, success={}, error={}", personId, success, e.getMessage(), e);
        }
    }

    /**
     * 解析历史记录条目
     */
    private PasswordVerificationHistory parseHistoryEntry(String entry) {
        try {
            String[] parts = entry.split("\\|");
            if (parts.length < 5) {
                return null;
            }

            PasswordVerificationHistory history = new PasswordVerificationHistory();
            history.setVerifyTime(LocalDateTime.parse(parts[0]));
            history.setDeviceId(parts[1].isEmpty() ? null : Long.parseLong(parts[1]));
            history.setClientIp(parts[2]);
            history.setResult(parts[3]);
            history.setMessage(parts[4]);

            return history;
        } catch (Exception e) {
            log.warn("解析密码验证历史记录异常: entry={}, error={}", entry, e.getMessage());
            return null;
        }
    }
}