package net.lab1024.sa.admin.module.consume.service.security;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.cache.RedisUtil;
import net.lab1024.sa.base.common.exception.SmartException;
import net.lab1024.sa.base.common.exception.UserErrorCode;
import net.lab1024.sa.base.common.util.SmartRedisUtil;
import net.lab1024.sa.base.module.support.heartbeat.core.HeartBeatManager;
import net.lab1024.sa.admin.module.consume.domain.entity.AccountEntity;
import net.lab1024.sa.admin.module.consume.domain.entity.ConsumeRecordEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 账户安全管理器
 * <p>
 * 负责账户安全相关功能，包括：
 * - 支付密码验证
 * - 消费限额管理
 * - 异常操作检测
 * - 账户冻结管理
 * - 安全通知机制
 * </p>
 *
 * @author SmartAdmin Team
 * @version 3.0.0
 * @since 2025-11-17
 */
@Slf4j
@Component
public class AccountSecurityManager {

    @Resource
    private HeartBeatManager heartBeatManager;

    // 缓存键前缀
    private static final String PAY_PWD_PREFIX = "security:pay_pwd:";
    private static final String LIMIT_PREFIX = "security:limit:";
    private static final String FREEZE_PREFIX = "security:freeze:";
    private static final String RISK_PREFIX = "security:risk:";

    // 默认限额配置
    private static final BigDecimal DEFAULT_SINGLE_LIMIT = new BigDecimal("1000.00");
    private static final BigDecimal DEFAULT_DAILY_LIMIT = new BigDecimal("5000.00");
    private static final BigDecimal DEFAULT_MONTHLY_LIMIT = new BigDecimal("20000.00");

    // 风险检测阈值
    private static final int MAX_DAILY_TRANSACTIONS = 20;
    private static final int MAX_HOURLY_TRANSACTIONS = 10;
    private static final BigDecimal SUSPICIOUS_AMOUNT_THRESHOLD = new BigDecimal("2000.00");

    /**
     * 验证支付密码
     *
     * @param personId 人员ID
     * @param password 支付密码
     * @return 验证结果
     */
    public PaymentPasswordResult verifyPaymentPassword(Long personId, String password) {
        try {
            log.debug("开始验证支付密码: personId={}", personId);

            // 1. 检查密码尝试次数
            String attemptKey = PAY_PWD_PREFIX + "attempt:" + personId;
            Integer attemptCount = SmartRedisUtil.get(attemptKey, Integer.class);
            if (attemptCount == null) {
                attemptCount = 0;
            }

            // 2. 检查是否超过最大尝试次数
            if (attemptCount >= 5) {
                log.warn("支付密码尝试次数过多，账户临时锁定: personId={}", personId);
                return PaymentPasswordResult.failure("PAY_PASSWORD_LOCKED", "支付密码错误次数过多，请稍后再试");
            }

            // 3. 获取存储的支付密码
            String storedPassword = getStoredPaymentPassword(personId);
            if (!StringUtils.hasText(storedPassword)) {
                return PaymentPasswordResult.failure("PAY_PASSWORD_NOT_SET", "支付密码未设置");
            }

            // 4. 验证密码
            boolean isPasswordValid = verifyPassword(password, storedPassword);

            // 5. 更新尝试次数
            if (isPasswordValid) {
                // 验证成功，清除尝试记录
                SmartRedisUtil.delete(attemptKey);
                log.debug("支付密码验证成功: personId={}", personId);
                return PaymentPasswordResult.success();
            } else {
                // 验证失败，增加尝试次数
                attemptCount++;
                SmartRedisUtil.set(attemptKey, attemptCount, 30, TimeUnit.MINUTES);
                log.warn("支付密码验证失败: personId={}, attemptCount={}", personId, attemptCount);

                String message = String.format("支付密码错误，剩余尝试次数: %d", 5 - attemptCount);
                return PaymentPasswordResult.failure("PAY_PASSWORD_ERROR", message);
            }

        } catch (Exception e) {
            log.error("验证支付密码异常: personId={}", personId, e);
            return PaymentPasswordResult.failure("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    /**
     * 设置支付密码
     *
     * @param personId 人员ID
     * @param password 支付密码
     * @return 设置结果
     */
    public boolean setPaymentPassword(Long personId, String password) {
        try {
            log.info("设置支付密码: personId={}", personId);

            // 1. 验证密码强度
            if (!validatePasswordStrength(password)) {
                return false;
            }

            // 2. 加密并存储密码
            String encryptedPassword = encryptPassword(password);
            String passwordKey = PAY_PWD_PREFIX + personId;
            SmartRedisUtil.set(passwordKey, encryptedPassword, 365, TimeUnit.DAYS);

            // 3. 清除尝试记录
            String attemptKey = PAY_PWD_PREFIX + "attempt:" + personId;
            SmartRedisUtil.delete(attemptKey);

            log.info("支付密码设置成功: personId={}", personId);
            return true;

        } catch (Exception e) {
            log.error("设置支付密码失败: personId={}", personId, e);
            return false;
        }
    }

    /**
     * 检查消费限额
     *
     * @param account 账户信息
     * @param amount 消费金额
     * @return 限额检查结果
     */
    public LimitCheckResult checkConsumeLimit(AccountEntity account, BigDecimal amount) {
        try {
            Long personId = account.getPersonId();
            log.debug("检查消费限额: personId={}, amount={}", personId, amount);

            LimitCheckResult result = new LimitCheckResult();

            // 1. 检查单次限额
            BigDecimal singleLimit = account.getSingleLimit() != null ?
                account.getSingleLimit() : DEFAULT_SINGLE_LIMIT;
            if (amount.compareTo(singleLimit) > 0) {
                result.setExceeded(true);
                result.setReason("SINGLE_LIMIT_EXCEEDED");
                result.setMessage(String.format("超出单次消费限额: %.2f元", singleLimit));
                return result;
            }

            // 2. 检查日度限额
            BigDecimal dailyLimit = account.getDailyLimit() != null ?
                account.getDailyLimit() : DEFAULT_DAILY_LIMIT;
            BigDecimal todayAmount = getTodayConsumeAmount(personId);
            if (todayAmount.add(amount).compareTo(dailyLimit) > 0) {
                result.setExceeded(true);
                result.setReason("DAILY_LIMIT_EXCEEDED");
                result.setMessage(String.format("超出日度消费限额: %.2f元，已消费: %.2f元", dailyLimit, todayAmount));
                return result;
            }

            // 3. 检查月度限额
            BigDecimal monthlyLimit = account.getMonthlyLimit() != null ?
                account.getMonthlyLimit() : DEFAULT_MONTHLY_LIMIT;
            BigDecimal monthlyAmount = getMonthlyConsumeAmount(personId);
            if (monthlyAmount.add(amount).compareTo(monthlyLimit) > 0) {
                result.setExceeded(true);
                result.setReason("MONTHLY_LIMIT_EXCEEDED");
                result.setMessage(String.format("超出月度消费限额: %.2f元，已消费: %.2f元", monthlyLimit, monthlyAmount));
                return result;
            }

            // 4. 检查时间限制
            if (!isWithinConsumeTimeLimit(personId)) {
                result.setExceeded(true);
                result.setReason("TIME_LIMIT_EXCEEDED");
                result.setMessage("当前时间不允许消费");
                return result;
            }

            result.setExceeded(false);
            log.debug("消费限额检查通过: personId={}", personId);
            return result;

        } catch (Exception e) {
            log.error("检查消费限额异常: personId={}, amount={}", account.getPersonId(), amount, e);
            LimitCheckResult errorResult = new LimitCheckResult();
            errorResult.setExceeded(true);
            errorResult.setReason("SYSTEM_ERROR");
            errorResult.setMessage("系统异常，请稍后重试");
            return errorResult;
        }
    }

    /**
     * 检测异常操作
     *
     * @param personId 人员ID
     * @param consumeRecord 消费记录
     * @return 风险检测结果
     */
    public RiskDetectionResult detectAnomalousOperation(Long personId, ConsumeRecordEntity consumeRecord) {
        try {
            log.debug("检测异常操作: personId={}, amount={}", personId, consumeRecord.getAmount());

            RiskDetectionResult result = new RiskDetectionResult();
            List<String> riskFactors = new ArrayList<>();

            // 1. 检测高频交易
            int hourlyTransactions = getHourlyTransactionCount(personId);
            if (hourlyTransactions > MAX_HOURLY_TRANSACTIONS) {
                riskFactors.add("高频交易: 1小时内" + hourlyTransactions + "笔交易");
            }

            int dailyTransactions = getDailyTransactionCount(personId);
            if (dailyTransactions > MAX_DAILY_TRANSACTIONS) {
                riskFactors.add("超频交易: 1日内" + dailyTransactions + "笔交易");
            }

            // 2. 检测大额交易
            if (consumeRecord.getAmount().compareTo(SUSPICIOUS_AMOUNT_THRESHOLD) > 0) {
                riskFactors.add("大额交易: " + consumeRecord.getAmount() + "元");
            }

            // 3. 检测异地交易
            if (isUnusualLocation(personId, consumeRecord.getDeviceId(), consumeRecord.getClientIp())) {
                riskFactors.add("异地交易或新设备");
            }

            // 4. 检测异常时间
            if (isUnusualTime(personId, consumeRecord.getPayTime().toLocalTime())) {
                riskFactors.add("异常时间交易");
            }

            // 5. 检测金额异常
            if (isUnusualAmount(personId, consumeRecord.getAmount())) {
                riskFactors.add("异常金额交易");
            }

            // 6. 计算风险等级
            int riskScore = calculateRiskScore(riskFactors);
            RiskLevel riskLevel = determineRiskLevel(riskScore);

            result.setRiskLevel(riskLevel);
            result.setRiskScore(riskScore);
            result.setRiskFactors(riskFactors);

            // 7. 记录风险信息
            recordRiskInfo(personId, consumeRecord, result);

            // 8. 高风险操作触发冻结
            if (riskLevel == RiskLevel.HIGH) {
                log.warn("检测到高风险操作，触发临时冻结: personId={}, riskScore={}", personId, riskScore);
                freezeAccountTemporarily(personId, "高风险操作检测", 2, TimeUnit.HOURS);
                sendSecurityAlert(personId, "高风险操作检测", riskFactors);
            }

            log.debug("异常操作检测完成: personId={}, riskLevel={}, score={}", personId, riskLevel, riskScore);
            return result;

        } catch (Exception e) {
            log.error("检测异常操作异常: personId={}", personId, e);
            RiskDetectionResult errorResult = new RiskDetectionResult();
            errorResult.setRiskLevel(RiskLevel.MEDIUM);
            errorResult.setRiskScore(50);
            errorResult.setRiskFactors(List.of("系统检测异常"));
            return errorResult;
        }
    }

    /**
     * 冻结账户
     *
     * @param personId 人员ID
     * @param reason 冻结原因
     * @param duration 冻结时长
     * @param timeUnit 时间单位
     * @return 冻结结果
     */
    public boolean freezeAccount(Long personId, String reason, long duration, TimeUnit timeUnit) {
        try {
            log.info("冻结账户: personId={}, reason={}, duration={}", personId, reason, duration);

            String freezeKey = FREEZE_PREFIX + personId;
            FreezeInfo freezeInfo = new FreezeInfo();
            freezeInfo.setPersonId(personId);
            freezeInfo.setReason(reason);
            freezeInfo.setFreezeTime(LocalDateTime.now());
            freezeInfo.setDuration(duration);
            freezeInfo.setTimeUnit(timeUnit);

            SmartRedisUtil.set(freezeKey, freezeInfo, duration, timeUnit);

            // 发送冻结通知
            sendSecurityAlert(personId, "账户冻结通知", List.of(reason));

            log.info("账户冻结成功: personId={}", personId);
            return true;

        } catch (Exception e) {
            log.error("冻结账户失败: personId={}", personId, e);
            return false;
        }
    }

    /**
     * 解冻账户
     *
     * @param personId 人员ID
     * @return 解冻结果
     */
    public boolean unfreezeAccount(Long personId) {
        try {
            log.info("解冻账户: personId={}", personId);

            String freezeKey = FREEZE_PREFIX + personId;
            SmartRedisUtil.delete(freezeKey);

            log.info("账户解冻成功: personId={}", personId);
            return true;

        } catch (Exception e) {
            log.error("解冻账户失败: personId={}", personId, e);
            return false;
        }
    }

    /**
     * 检查账户是否被冻结
     *
     * @param personId 人员ID
     * @return 是否被冻结
     */
    public boolean isAccountFrozen(Long personId) {
        try {
            String freezeKey = FREEZE_PREFIX + personId;
            return SmartRedisUtil.hasKey(freezeKey);
        } catch (Exception e) {
            log.error("检查账户冻结状态失败: personId={}", personId, e);
            return false;
        }
    }

    /**
     * 获取账户冻结信息
     *
     * @param personId 人员ID
     * @return 冻结信息
     */
    public FreezeInfo getFreezeInfo(Long personId) {
        try {
            String freezeKey = FREEZE_PREFIX + personId;
            return SmartRedisUtil.get(freezeKey, FreezeInfo.class);
        } catch (Exception e) {
            log.error("获取账户冻结信息失败: personId={}", personId, e);
            return null;
        }
    }

    // 私有辅助方法

    private String getStoredPaymentPassword(Long personId) {
        String passwordKey = PAY_PWD_PREFIX + personId;
        return SmartRedisUtil.get(passwordKey, String.class);
    }

    private boolean verifyPassword(String inputPassword, String storedPassword) {
        // TODO: 实现具体的密码验证逻辑
        // 这里应该使用BCrypt等安全的密码验证方式
        return inputPassword.equals(storedPassword); // 临时简化实现
    }

    private boolean validatePasswordStrength(String password) {
        // TODO: 实现密码强度验证
        return password != null && password.length() >= 6;
    }

    private String encryptPassword(String password) {
        // TODO: 实现密码加密
        return password; // 临时简化实现
    }

    private BigDecimal getTodayConsumeAmount(Long personId) {
        // TODO: 查询今日消费金额
        return BigDecimal.ZERO;
    }

    private BigDecimal getMonthlyConsumeAmount(Long personId) {
        // TODO: 查询本月消费金额
        return BigDecimal.ZERO;
    }

    private boolean isWithinConsumeTimeLimit(Long personId) {
        // TODO: 检查消费时间限制
        return true;
    }

    private int getHourlyTransactionCount(Long personId) {
        // TODO: 统计1小时内交易次数
        return 0;
    }

    private int getDailyTransactionCount(Long personId) {
        // TODO: 统计1日内交易次数
        return 0;
    }

    private boolean isUnusualLocation(Long personId, String deviceId, String clientIp) {
        // TODO: 检测异常位置
        return false;
    }

    private boolean isUnusualTime(Long personId, LocalTime transactionTime) {
        // TODO: 检测异常时间
        return false;
    }

    private boolean isUnusualAmount(Long personId, BigDecimal amount) {
        // TODO: 检测异常金额
        return false;
    }

    private int calculateRiskScore(List<String> riskFactors) {
        int score = 0;
        for (String factor : riskFactors) {
            if (factor.contains("高频")) score += 20;
            if (factor.contains("大额")) score += 30;
            if (factor.contains("异地")) score += 25;
            if (factor.contains("异常时间")) score += 15;
            if (factor.contains("异常金额")) score += 20;
        }
        return Math.min(score, 100);
    }

    private RiskLevel determineRiskLevel(int riskScore) {
        if (riskScore >= 80) return RiskLevel.HIGH;
        if (riskScore >= 50) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    private void recordRiskInfo(Long personId, ConsumeRecordEntity consumeRecord, RiskDetectionResult result) {
        try {
            String riskKey = RISK_PREFIX + personId + ":" + consumeRecord.getRecordId();
            SmartRedisUtil.set(riskKey, result, 30, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("记录风险信息失败: personId={}", personId, e);
        }
    }

    private void freezeAccountTemporarily(Long personId, String reason, long duration, TimeUnit timeUnit) {
        freezeAccount(personId, "临时冻结: " + reason, duration, timeUnit);
    }

    private void sendSecurityAlert(Long personId, String alertType, List<String> details) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "SECURITY_ALERT");
            notification.put("userId", personId);
            notification.put("alertType", alertType);
            notification.put("details", details);
            notification.put("timestamp", System.currentTimeMillis());

            heartBeatManager.broadcastToUserDevices(personId, notification);

        } catch (Exception e) {
            log.error("发送安全警报失败: personId={}", personId, e);
        }
    }

    // 内部类定义

    public static class PaymentPasswordResult {
        private boolean success;
        private String errorCode;
        private String errorMessage;

        private PaymentPasswordResult(boolean success, String errorCode, String errorMessage) {
            this.success = success;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public static PaymentPasswordResult success() {
            return new PaymentPasswordResult(true, null, null);
        }

        public static PaymentPasswordResult failure(String errorCode, String errorMessage) {
            return new PaymentPasswordResult(false, errorCode, errorMessage);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getErrorCode() { return errorCode; }
        public String getErrorMessage() { return errorMessage; }
    }

    public static class LimitCheckResult {
        private boolean exceeded;
        private String reason;
        private String message;

        // Getters and Setters
        public boolean isExceeded() { return exceeded; }
        public void setExceeded(boolean exceeded) { this.exceeded = exceeded; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class RiskDetectionResult {
        private RiskLevel riskLevel;
        private int riskScore;
        private List<String> riskFactors;

        // Getters and Setters
        public RiskLevel getRiskLevel() { return riskLevel; }
        public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
        public int getRiskScore() { return riskScore; }
        public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
        public List<String> getRiskFactors() { return riskFactors; }
        public void setRiskFactors(List<String> riskFactors) { this.riskFactors = riskFactors; }
    }

    public static class FreezeInfo {
        private Long personId;
        private String reason;
        private LocalDateTime freezeTime;
        private long duration;
        private TimeUnit timeUnit;

        // Getters and Setters
        public Long getPersonId() { return personId; }
        public void setPersonId(Long personId) { this.personId = personId; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public LocalDateTime getFreezeTime() { return freezeTime; }
        public void setFreezeTime(LocalDateTime freezeTime) { this.freezeTime = freezeTime; }
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        public TimeUnit getTimeUnit() { return timeUnit; }
        public void setTimeUnit(TimeUnit timeUnit) { this.timeUnit = timeUnit; }
    }

    public enum RiskLevel {
        LOW, MEDIUM, HIGH
    }
}