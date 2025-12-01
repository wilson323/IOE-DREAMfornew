package net.lab1024.sa.admin.module.consume.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.consume.service.ConsumeCacheService;
import net.lab1024.sa.admin.module.consume.service.ConsumeService;
import net.lab1024.sa.base.common.util.SmartDateUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 账户安全管理器
 * 负责支付密码验证、异常操作检测、账户安全控制等功能
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Slf4j
@Component
public class AccountSecurityManager {

    @Resource
    private ConsumeService consumeService;

    @Resource
    private ConsumeCacheService consumeCacheService;

    // 安全配置常量
    private static final int MAX_PASSWORD_ATTEMPTS = 5;
    private static final long PASSWORD_LOCK_DURATION = 30; // 分钟
    private static final int HIGH_FREQUENCY_THRESHOLD = 10; // 10分钟内
    private static final BigDecimal LARGE_AMOUNT_THRESHOLD = new BigDecimal("500.00");
    private static final String PAYMENT_PASSWORD_KEY_PREFIX = "payment_password_attempts:";
    private static final String SECURITY_ALERT_KEY_PREFIX = "security_alert:";

    /**
     * 支付密码验证结果
     */
    public static class PaymentPasswordResult {
        private boolean success;
        private boolean locked;
        private int remainingAttempts;
        private long lockDuration;
        private String message;

        public PaymentPasswordResult(boolean success, boolean locked, int remainingAttempts, long lockDuration, String message) {
            this.success = success;
            this.locked = locked;
            this.remainingAttempts = remainingAttempts;
            this.lockDuration = lockDuration;
            this.message = message;
        }

        public static PaymentPasswordResult success() {
            return new PaymentPasswordResult(true, false, MAX_PASSWORD_ATTEMPTS, 0, "密码验证成功");
        }

        public static PaymentPasswordResult failure(int remainingAttempts) {
            return new PaymentPasswordResult(false, false, remainingAttempts, 0, "密码错误，剩余尝试次数: " + remainingAttempts);
        }

        public static PaymentPasswordResult locked(long lockDuration) {
            return new PaymentPasswordResult(false, true, 0, lockDuration, "账户已锁定，请" + lockDuration + "分钟后重试");
        }

        // Getters
        public boolean isSuccess() { return success; }
        public boolean isLocked() { return locked; }
        public int getRemainingAttempts() { return remainingAttempts; }
        public long getLockDuration() { return lockDuration; }
        public String getMessage() { return message; }
    }

    /**
     * 风险检测结果
     */
    public static class RiskDetectionResult {
        private String riskLevel; // HIGH, MEDIUM, LOW, NORMAL
        private String riskType;
        private String message;
        private double riskScore;

        public RiskDetectionResult(String riskLevel, String riskType, String message, double riskScore) {
            this.riskLevel = riskLevel;
            this.riskType = riskType;
            this.message = message;
            this.riskScore = riskScore;
        }

        public static RiskDetectionResult highRisk(String message, String riskType) {
            return new RiskDetectionResult("HIGH", riskType, message, 80.0);
        }

        public static RiskDetectionResult mediumRisk(String message, String riskType) {
            return new RiskDetectionResult("MEDIUM", riskType, message, 60.0);
        }

        public static RiskDetectionResult normal(double riskScore) {
            return new RiskDetectionResult("NORMAL", null, "操作正常", riskScore);
        }

        // Getters
        public String getRiskLevel() { return riskLevel; }
        public String getRiskType() { return riskType; }
        public String getMessage() { return message; }
        public double getRiskScore() { return riskScore; }
    }

    /**
     * 验证支付密码
     *
     * @param userId 用户ID
     * @param password 支付密码
     * @return 验证结果
     */
    public PaymentPasswordResult verifyPaymentPassword(Long userId, String password) {
        try {
            // 1. 检查尝试次数
            AttemptCount attemptCount = getPaymentPasswordAttemptCount(userId);
            if (attemptCount.exceedsLimit()) {
                return PaymentPasswordResult.locked(attemptCount.getLockDuration());
            }

            // 2. 验证密码
            boolean passwordValid = validatePaymentPasswordInternal(userId, password);

            // 3. 更新尝试记录
            if (passwordValid) {
                clearPaymentPasswordAttempts(userId);
                log.info("用户支付密码验证成功: userId={}", userId);
                return PaymentPasswordResult.success();
            } else {
                incrementPaymentPasswordAttempts(userId);
                log.warn("用户支付密码验证失败: userId={}, remainingAttempts={}",
                        userId, MAX_PASSWORD_ATTEMPTS - attemptCount.getCount() - 1);
                return PaymentPasswordResult.failure(MAX_PASSWORD_ATTEMPTS - attemptCount.getCount());
            }

        } catch (Exception e) {
            log.error("支付密码验证异常: userId={}", userId, e);
            return PaymentPasswordResult.locked(PASSWORD_LOCK_DURATION);
        }
    }

    /**
     * 检测异常操作
     *
     * @param userId 用户ID
     * @param amount 消费金额
     * @param deviceId 设备ID
     * @return 风险检测结果
     */
    public RiskDetectionResult detectAnomalousOperation(Long userId, BigDecimal amount, Long deviceId) {
        try {
            double riskScore = 0.0;
            String highestRiskLevel = "NORMAL";
            String highestRiskType = null;
            String highestRiskMessage = "操作正常";

            // 1. 高频交易检测
            RiskDetectionResult frequencyResult = detectHighFrequencyTransactions(userId);
            if (frequencyResult.getRiskScore() > riskScore) {
                riskScore = frequencyResult.getRiskScore();
                highestRiskLevel = frequencyResult.getRiskLevel();
                highestRiskType = frequencyResult.getRiskType();
                highestRiskMessage = frequencyResult.getMessage();
            }

            // 2. 大额交易检测
            RiskDetectionResult amountResult = detectLargeAmountTransaction(amount);
            if (amountResult.getRiskScore() > riskScore) {
                riskScore = amountResult.getRiskScore();
                highestRiskLevel = amountResult.getRiskLevel();
                highestRiskType = amountResult.getRiskType();
                highestRiskMessage = amountResult.getMessage();
            }

            // 3. 异常地点/设备检测
            RiskDetectionResult locationResult = detectUnusualLocationOrDevice(userId, deviceId);
            if (locationResult.getRiskScore() > riskScore) {
                riskScore = locationResult.getRiskScore();
                highestRiskLevel = locationResult.getRiskLevel();
                highestRiskType = locationResult.getRiskType();
                highestRiskMessage = locationResult.getMessage();
            }

            // 4. 时间异常检测
            RiskDetectionResult timeResult = detectUnusualTimePattern(userId);
            if (timeResult.getRiskScore() > riskScore) {
                riskScore = timeResult.getRiskScore();
                highestRiskLevel = timeResult.getRiskLevel();
                highestRiskType = timeResult.getRiskType();
                highestRiskMessage = timeResult.getMessage();
            }

            // 5. 记录安全事件
            if (riskScore > 50.0) {
                recordSecurityAlert(userId, highestRiskLevel, highestRiskType, highestRiskMessage);
            }

            log.debug("风险检测结果: userId={}, riskLevel={}, riskScore={}", userId, highestRiskLevel, riskScore);
            return new RiskDetectionResult(highestRiskLevel, highestRiskType, highestRiskMessage, riskScore);

        } catch (Exception e) {
            log.error("异常操作检测异常: userId={}", userId, e);
            return RiskDetectionResult.mediumRisk("检测异常", "SYSTEM_ERROR");
        }
    }

    /**
     * 获取用户安全状态
     *
     * @param userId 用户ID
     * @return 安全状态信息
     */
    public Map<String, Object> getUserSecurityStatus(Long userId) {
        Map<String, Object> status = new HashMap<>();

        try {
            // 支付密码状态
            AttemptCount attemptCount = getPaymentPasswordAttemptCount(userId);
            status.put("paymentPasswordLocked", attemptCount.exceedsLimit());
            status.put("remainingAttempts", attemptCount.exceedsLimit() ? 0 : MAX_PASSWORD_ATTEMPTS - attemptCount.getCount());
            if (attemptCount.exceedsLimit()) {
                status.put("lockDuration", attemptCount.getLockDuration());
                status.put("lockEndTime", attemptCount.getLockEndTime());
            }

            // 最近风险事件
            List<Map<String, Object>> recentAlerts = getRecentSecurityAlerts(userId, 24); // 最近24小时
            status.put("recentSecurityAlerts", recentAlerts);
            status.put("alertCount24h", recentAlerts.size());

            // 账户安全评分
            double securityScore = calculateSecurityScore(userId);
            status.put("securityScore", securityScore);
            status.put("securityLevel", getSecurityLevel(securityScore));

            // 建议措施
            status.put("recommendations", getSecurityRecommendations(userId));

        } catch (Exception e) {
            log.error("获取用户安全状态异常: userId={}", userId, e);
            status.put("error", "获取安全状态失败");
        }

        return status;
    }

    /**
     * 解锁用户支付密码
     *
     * @param userId 用户ID
     * @param adminUserId 管理员用户ID
     * @return 是否成功
     */
    public boolean unlockPaymentPassword(Long userId, Long adminUserId) {
        try {
            clearPaymentPasswordAttempts(userId);
            recordSecurityOperation(userId, "UNLOCK_PASSWORD", "管理员解锁支付密码", adminUserId);
            log.info("管理员解锁用户支付密码: userId={}, adminUserId={}", userId, adminUserId);
            return true;
        } catch (Exception e) {
            log.error("解锁支付密码失败: userId={}, adminUserId={}", userId, adminUserId, e);
            return false;
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 验证支付密码内部实现
     */
    private boolean validatePaymentPasswordInternal(Long userId, String password) {
        try {
            // TODO: 实际的支付密码验证逻辑
            // 这里简化实现，实际应该从数据库获取加密的密码进行验证
            Map<String, Object> accountInfo = consumeService.getAccountBalance(userId);
            if (accountInfo != null && accountInfo.containsKey("paymentPassword")) {
                String storedPassword = (String) accountInfo.get("paymentPassword");
                return password.equals(storedPassword); // 简化验证，实际应使用加密比较
            }
            return false;
        } catch (Exception e) {
            log.error("支付密码验证内部错误: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 获取支付密码尝试次数
     */
    private AttemptCount getPaymentPasswordAttemptCount(Long userId) {
        try {
            String cacheKey = PAYMENT_PASSWORD_KEY_PREFIX + userId;
            AttemptCount count = consumeCacheService.getCachedValue(cacheKey, AttemptCount.class);
            if (count == null) {
                count = new AttemptCount();
            }

            // 检查是否已过锁定时间
            if (count.isLocked() && count.getLockEndTime().isBefore(LocalDateTime.now())) {
                count = new AttemptCount(); // 重置
                consumeCacheService.setCachedValue(cacheKey, count, 24 * 60 * 60);
            }

            return count;
        } catch (Exception e) {
            log.error("获取支付密码尝试次数失败: userId={}", userId, e);
            return new AttemptCount();
        }
    }

    /**
     * 增加支付密码尝试次数
     */
    private void incrementPaymentPasswordAttempts(Long userId) {
        try {
            String cacheKey = PAYMENT_PASSWORD_KEY_PREFIX + userId;
            AttemptCount count = getPaymentPasswordAttemptCount(userId);

            count.increment();

            // 如果达到最大次数，设置锁定
            if (count.getCount() >= MAX_PASSWORD_ATTEMPTS) {
                count.setLocked(true);
                count.setLockEndTime(LocalDateTime.now().plusMinutes(PASSWORD_LOCK_DURATION));
            }

            consumeCacheService.setCachedValue(cacheKey, count, 24 * 60 * 60);
        } catch (Exception e) {
            log.error("增加支付密码尝试次数失败: userId={}", userId, e);
        }
    }

    /**
     * 清除支付密码尝试记录
     */
    private void clearPaymentPasswordAttempts(Long userId) {
        try {
            String cacheKey = PAYMENT_PASSWORD_KEY_PREFIX + userId;
            consumeCacheService.deleteCachedValue(cacheKey);
        } catch (Exception e) {
            log.error("清除支付密码尝试记录失败: userId={}", userId, e);
        }
    }

    /**
     * 检测高频交易
     */
    private RiskDetectionResult detectHighFrequencyTransactions(Long userId) {
        try {
            String cacheKey = "consume_frequency:" + userId + ":" + LocalDateTime.now().minusMinutes(10).toString();
            Integer count = consumeCacheService.getCachedValue(cacheKey, Integer.class);

            if (count != null && count >= HIGH_FREQUENCY_THRESHOLD) {
                return RiskDetectionResult.highRisk(
                        String.format("检测到高频交易，10分钟内消费%d次", count),
                        "HIGH_FREQUENCY"
                );
            }

            return RiskDetectionResult.normal(0.0);
        } catch (Exception e) {
            log.error("高频交易检测失败: userId={}", userId, e);
            return RiskDetectionResult.normal(0.0);
        }
    }

    /**
     * 检测大额交易
     */
    private RiskDetectionResult detectLargeAmountTransaction(BigDecimal amount) {
        try {
            if (amount != null && amount.compareTo(LARGE_AMOUNT_THRESHOLD) >= 0) {
                return RiskDetectionResult.mediumRisk(
                        String.format("检测到大额交易: ¥%.2f", amount),
                        "LARGE_AMOUNT"
                );
            }
            return RiskDetectionResult.normal(0.0);
        } catch (Exception e) {
            log.error("大额交易检测失败: amount={}", amount, e);
            return RiskDetectionResult.normal(0.0);
        }
    }

    /**
     * 检测异常地点或设备
     */
    private RiskDetectionResult detectUnusualLocationOrDevice(Long userId, Long deviceId) {
        try {
            // TODO: 实现异常地点/设备检测逻辑
            // 这里简化实现
            return RiskDetectionResult.normal(0.0);
        } catch (Exception e) {
            log.error("异常地点/设备检测失败: userId={}, deviceId={}", userId, deviceId, e);
            return RiskDetectionResult.normal(0.0);
        }
    }

    /**
     * 检测异常时间模式
     */
    private RiskDetectionResult detectUnusualTimePattern(Long userId) {
        try {
            // TODO: 实现异常时间模式检测逻辑
            // 这里简化实现
            return RiskDetectionResult.normal(0.0);
        } catch (Exception e) {
            log.error("异常时间模式检测失败: userId={}", userId, e);
            return RiskDetectionResult.normal(0.0);
        }
    }

    /**
     * 记录安全警报
     */
    private void recordSecurityAlert(Long userId, String riskLevel, String riskType, String message) {
        try {
            Map<String, Object> alert = new HashMap<>();
            alert.put("userId", userId);
            alert.put("riskLevel", riskLevel);
            alert.put("riskType", riskType);
            alert.put("message", message);
            alert.put("timestamp", LocalDateTime.now());

            String cacheKey = SECURITY_ALERT_KEY_PREFIX + userId + ":" + System.currentTimeMillis();
            consumeCacheService.setCachedValue(cacheKey, alert, 7 * 24 * 60 * 60); // 保存7天

            log.warn("记录安全警报: userId={}, riskLevel={}, riskType={}, message={}",
                    userId, riskLevel, riskType, message);
        } catch (Exception e) {
            log.error("记录安全警报失败: userId={}", userId, e);
        }
    }

    /**
     * 记录安全操作
     */
    private void recordSecurityOperation(Long userId, String operation, String description, Long operatorId) {
        try {
            log.info("安全操作记录: userId={}, operation={}, description={}, operatorId={}",
                    userId, operation, description, operatorId);
            // TODO: 可以记录到数据库或审计日志
        } catch (Exception e) {
            log.error("记录安全操作失败", e);
        }
    }

    /**
     * 获取最近安全警报
     */
    private List<Map<String, Object>> getRecentSecurityAlerts(Long userId, int hours) {
        try {
            // TODO: 实现从缓存或数据库获取最近安全警报的逻辑
            return List.of();
        } catch (Exception e) {
            log.error("获取最近安全警报失败: userId={}", userId, e);
            return List.of();
        }
    }

    /**
     * 计算安全评分
     */
    private double calculateSecurityScore(Long userId) {
        try {
            // TODO: 基于多个因素计算安全评分
            // 简化实现，返回基础分数
            return 85.0;
        } catch (Exception e) {
            log.error("计算安全评分失败: userId={}", userId, e);
            return 50.0;
        }
    }

    /**
     * 获取安全等级
     */
    private String getSecurityLevel(double score) {
        if (score >= 90.0) {
            return "HIGH";
        } else if (score >= 70.0) {
            return "MEDIUM";
        } else if (score >= 50.0) {
            return "LOW";
        } else {
            return "CRITICAL";
        }
    }

    /**
     * 获取安全建议
     */
    private List<String> getSecurityRecommendations(Long userId) {
        List<String> recommendations = new java.util.ArrayList<>();

        try {
            AttemptCount attemptCount = getPaymentPasswordAttemptCount(userId);

            if (attemptCount.exceedsLimit()) {
                recommendations.add("建议联系管理员解锁支付密码");
            } else if (attemptCount.getCount() > 2) {
                recommendations.add("建议检查支付密码设置");
            }

            List<Map<String, Object>> recentAlerts = getRecentSecurityAlerts(userId, 24);
            if (recentAlerts.size() > 0) {
                recommendations.add("建议启用双因素认证");
            }

            if (recommendations.isEmpty()) {
                recommendations.add("账户安全状态良好");
            }

        } catch (Exception e) {
            log.error("获取安全建议失败: userId={}", userId, e);
            recommendations.add("建议定期检查安全设置");
        }

        return recommendations;
    }

    /**
     * 尝试次数内部类
     */
    private static class AttemptCount {
        private int count = 0;
        private boolean locked = false;
        private LocalDateTime lockEndTime;

        public int getCount() { return count; }
        public boolean isLocked() { return locked; }
        public LocalDateTime getLockEndTime() { return lockEndTime; }

        public void increment() {
            if (!locked) {
                count++;
            }
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
        }

        public void setLockEndTime(LocalDateTime lockEndTime) {
            this.lockEndTime = lockEndTime;
        }

        public boolean exceedsLimit() {
            return count >= MAX_PASSWORD_ATTEMPTS;
        }

        public long getLockDuration() {
            if (lockEndTime == null) return 0;
            return java.time.Duration.between(LocalDateTime.now(), lockEndTime).toMinutes();
        }
    }
}